package charlesgunn.jreality.worlds;


import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.CENTER_ON_BOUNDING_BOX;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_WIDTH;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.USE_OLD_TRANSPARENCY;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;

import charlesgunn.anim.core.AnimatedThing;
import charlesgunn.anim.core.KeyFrameAnimatedBean;
import charlesgunn.anim.core.Settable;
import charlesgunn.anim.jreality.SceneGraphAnimator;
import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.geometry.projective.CircleFactory;
import charlesgunn.jreality.geometry.projective.StarLinesFactory;
import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.jreality.tools.RotateShapeTool;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.GlslProgram;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.CameraUtility;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.util.SceneGraphUtility;

/**
 * This class implements stereographic projection from the unit sphere to the plane 
 * z = -1 (the canonical SP maps to the plane z = 0 but then one can't see the whole
 * sphere, hence the shift).
 * 
 * The sphere is modeled by a quadmesh; the plane is modeled by a disk parametrized via
 * polar coordinates, but the radial steps are exponential to get higher sampling near the center.
 * 
 * The stereographic projection  itself is represented via texture mapping. That is, if P
 * is a point on the sphere and S(P) is the image of P on the plane, then S(P) is assigned the
 * texture coordinates of P. Thus the disk remains fixed, but the texture coordinates of its vertices
 * are changed to be consistent with the rotated sphere.  To be precise, the pre-images of 
 * the vertices of the disk (representing the image plane) are precomputed. Say the point X in the 
 * plane comes from Y on the sphere.  When the sphere is rotated by rotation R, one calculates
 * Z = R^(-1)(Y). Then X is given texture coordinates for the "unmoved" point Z (theta, phi).
 * 
 * @author Charles Gunn
 *
 */public class StereographicProjectionDemo extends LoadableScene implements Settable {
	
	Color bkgd = new Color(0,0,130);
	int supersample = 2;
	int udim = 360/supersample, vdim = 180/supersample, pudim = 200/supersample, pvdim = 200/supersample;
	int uTextureScale = 36, vTextureScale = 36;
	double planeScale = 15,
			mercatorParameter = 0.0;		// 1: meerator projection (complex log of stereographic projection)
	int starSamples = 16;
	private GlslProgram fragShaderSphere, fragShaderPlane;
	private SceneGraphComponent world,
	fakeSphereSGC, // totally transparent lo-res sphere used only for picking/rotating
		sphereSGC,			// child of fakeSphereSGC, contains real, hi-res sphere w/ texture
		planeHolderSGC,
			planeSGC,			// projection plane z=-1
			realPlaneSGC,
		starSGC;			// set of projection lines 
	// following arrays are there to hold both lo-res and hi-res geometry versions 
	// for use on slower machines.  
	double[][] diskOnSphere = null;
	double[][][] textureCoords = { new double[(pudim/2)*(pvdim/2)][2], new double[(pudim)*(pvdim)][2]};
	double[][] currentTextureCoords = textureCoords[1];
	private IndexedFaceSet[] texturedDisk = new IndexedFaceSet[2];
	IndexedFaceSet currentTexturedDisk;
	private IndexedFaceSet[] sphericalPatch = new IndexedFaceSet[2],
		stereoProjOfSphere = new IndexedFaceSet[2];
	IndexedFaceSet currentSphericalPatch, currentStereoProjPatch;
	double[][] currentSphereVerts;
	int[][] currentSphereInds;
	IndexedLineSet currentStar;
	IndexedFaceSetFactory stPrFac[] = new IndexedFaceSetFactory[2];
	String earthBetterTextureName = "/gunn_local/TUB-HomepageOct19/WWW/Pictures/textures/Earth2048-XXX.jpg"; //grid256.jpg")); //
	String earthTextureName = "/gunn_local/TUB-HomepageOct19/WWW/Pictures/textures/Earth2048Light.jpg"; //grid256.jpg")); //
	String marsTextureName = "/gunn_local/TUB-HomepageOct19/WWW/Pictures/textures/mars2048.jpg"; //grid256.jpg")); //
	private ImageData earthImage, marsImage, earthBetterImage;
	boolean correctTextures = false, 	// fix a texturing problem? have time only when not moving
		projectSphere = true,			// create planar image by really projecting sphere geometry, not just using texture coordinate tricks
		needsCorrection = false,			// whether it's been fixed since the last move
		matrixChanged = false,				// used by the timer
		showTerrain = true, 				// whether the terrain image should be shown
		showGrid = true, 					// whether the grid texture should be shown
		mirror = false, 					// flip the orientation of the earth (then plane looks right)
		showEarth = true, 					// show the earth terrain?  if not, then mars
		showStar = false,					// show some projection lines? (didactic)
		showBetterEarth = false,			// show the better earth model? (possibly not completely legal)
		lores = false,						// whether to use lores geometry
		animating = false,
		doRadiosity = false;				// when the sphere isn't visible, shouldn't do radiosity.
	long lastInteractionTime = 0;
	private double[] sphereMatrix = Rn.identityMatrix(4), 
		inverseSphereMatrix = Rn.identityMatrix(4);
	private Texture2D earthTexture2d;
	private Texture2D gridTexture2d;
	private Timer timer;
	private static Attribute attributeForName = Attribute.attributeForName("lightmap coordinates");
	private StarLinesFactory  starFactory;
	
	@Override
	/**
	 * This generates the basic scene graph component
	 */
	public SceneGraphComponent makeWorld() {
		world = new SceneGraphComponent("world");
		MatrixBuilder.euclidean().rotateX(-2*Math.PI/5).assignTo(world);
		Appearance ap = new Appearance();
		world.setAppearance(ap);
		ap.setAttribute(EDGE_DRAW, false);	
//		ap.setAttribute(TUBES_DRAW, false);
		ap.setAttribute(LINE_WIDTH, 1.5);
		ap.setAttribute(CENTER_ON_BOUNDING_BOX, false);  // rotate tool checks this
		ap.setAttribute("polygonShader.diffuseColor", new Color(1f,1f,1f,1f));
		ap.setAttribute("polygonShader.ambientColor", new Color(1f,1f,1f,1f));
		ap.setAttribute("polygonShader.ambientCoefficient", .2);
		ap.setAttribute("polygonShader.specularCoefficient", 0.0);
		// The following node contains a simplified sphere for faster picking
		// (needed in order for the rotate tool to have an object to rotate)
		// if we're not over the sphere, then the standard rotate tool is activated on the whole world
		fakeSphereSGC = SceneGraphUtility.createFullSceneGraphComponent("fake sphere");
		fakeSphereSGC.addTool(new RotateShapeTool());
		fakeSphereSGC.getAppearance().setAttribute(TRANSPARENCY_ENABLED, true);
		fakeSphereSGC.getAppearance().setAttribute(TRANSPARENCY, 1.0);
//		fakeSphereSGC.getAppearance().setAttribute(CommonAttributes.IGNORE_ALPHA0, false);
		fakeSphereSGC.setGeometry(new Sphere());
		
		// this node contains the real geometry for the sphere
		sphereSGC = new SceneGraphComponent("sphere");
		fakeSphereSGC.addChild(sphereSGC);
		// create two different resolutions of (theta,phi) patches convering whole sphere
		sphericalPatch[0] = SphereUtility.sphericalPatch(0.0, 0.0, 360.0, 179.999, udim/2, vdim/2, 1.0);
		sphericalPatch[1] = SphereUtility.sphericalPatch(0.0, 0.0, 360.0, 179.999, udim, vdim, 1.0);
		currentSphericalPatch = sphericalPatch[1];
		currentSphereVerts = currentSphericalPatch.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		currentSphereInds = currentSphericalPatch.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		sphereSGC.setGeometry(currentSphericalPatch);
		// load the texture map
		try {
			earthImage = ImageData.load(Input.getInput(earthTextureName));
			System.err.println("Earth image loaded "+earthImage.getWidth());
		} catch (IOException e) {
			e.printStackTrace();
		}
		ap = world.getAppearance(); //new Appearance();
		ap.setAttribute("polygonShader.diffuseColor", showEarth ? Color.white : new Color(40, 140, 255));
		earthTexture2d = TextureUtility.createTexture(ap, "polygonShader", 0, earthImage);
		if (mirror) earthTexture2d.setTextureMatrix(new Matrix(P3.makeReflectionMatrix(null, 
				new double[]{1,0,0,0}, Pn.EUCLIDEAN)));
		SimpleTextureFactory stf = new SimpleTextureFactory();
		stf.setType(SimpleTextureFactory.TextureType.LINE);
		stf.update();
		gridTexture2d = TextureUtility.createTexture(ap, "polygonShader", 1, stf.getImageData());
		gridTexture2d.setTextureMatrix(new Matrix(P3.makeStretchMatrix(null,new double[]{uTextureScale,vTextureScale,1})));
		TextureUtility.createTexture(ap, "polygonShader", 2, stf.getImageData());
		sphereSGC.setAppearance(new Appearance());
		ap = sphereSGC.getAppearance();
		ap.setAttribute(TRANSPARENCY_ENABLED, false);
		ap.setAttribute(TRANSPARENCY,0.0);
		sphereSGC.setPickable(false);
		

		// Set up the plane geometry: a large disk
		planeHolderSGC = new SceneGraphComponent("plane holder");
		planeHolderSGC.setAppearance(new Appearance());
//		CopyVisitor cv = new CopyVisitor();
//		cv.copyAttr(sphereSGC.getAppearance(), planeHolderSGC.getAppearance());
		realPlaneSGC = new SceneGraphComponent("stereo projected disk");
		planeSGC = new SceneGraphComponent("textured disk");
		planeHolderSGC.addChildren(planeSGC, realPlaneSGC);
		texturedDisk[0] = GeometryUtilityOverflow.texturedDisk(pudim/2, pvdim/2, true, true);
		texturedDisk[1] = GeometryUtilityOverflow.texturedDisk(pudim, pvdim, true, true);
		texturedDisk[0].setVertexAttributes(attributeForName,
				texturedDisk[0].getVertexAttributes(Attribute.COORDINATES));
		texturedDisk[1].setVertexAttributes(attributeForName,
				texturedDisk[1].getVertexAttributes(Attribute.COORDINATES));
		currentTexturedDisk = texturedDisk[1];
		planeSGC.setGeometry(currentTexturedDisk);
		// setup the better quality repn
		// we need to have indexed face set factories in order to reject certain faces
		for (int i = 0; i<2; ++i)	{
			stPrFac[i] = new IndexedFaceSetFactory();
			stPrFac[i].setVertexCount(sphericalPatch[i].getNumPoints());
			stPrFac[i].setVertexCoordinates(sphericalPatch[i].getVertexAttributes(Attribute.COORDINATES));
			stPrFac[i].setVertexTextureCoordinates(sphericalPatch[i].getVertexAttributes(Attribute.TEXTURE_COORDINATES));
			stPrFac[i].setFaceCount(sphericalPatch[i].getNumFaces());
			stPrFac[i].setFaceIndices(sphericalPatch[i].getFaceAttributes(Attribute.INDICES));
			stPrFac[i].setGenerateFaceNormals(true);
			stPrFac[i].setGenerateVertexNormals(true);	
			stPrFac[i].update();
		}
		stereoProjOfSphere[0] = stPrFac[0].getIndexedFaceSet();
		currentStereoProjPatch =stereoProjOfSphere[1] = stPrFac[1].getIndexedFaceSet();
		realPlaneSGC.setGeometry(currentStereoProjPatch);
		realPlaneSGC.setVisible(false);
		MatrixBuilder.euclidean().scale(planeScale).assignTo(planeSGC);
		MatrixBuilder.euclidean().translate(0,0,-1.002).scale(2).assignTo(planeHolderSGC);
		PickUtility.setPickable(planeHolderSGC, false, false, true);

		boolean useVertShader = true;
		ap = sphereSGC.getAppearance();
		ap.setAttribute("useGLSL", true);
		fragShaderSphere = null;
		String vertShaderSource = "charlesgunn/jreality/resources/standard3dlabs.vert";
//		String vertShaderSource = "de/jreality/jogl/shader/resources/euclidean.vert";		
		try {
			fragShaderSphere = new GlslProgram(ap, "polygonShader",   
					useVertShader ? Input.getInput(vertShaderSource) : null,
					Input.getInput("charlesgunn/jreality/resources/textureBlendWithRadiosity.frag")
			    );
		} catch (IOException e) {
			e.printStackTrace();
		}
		fragShaderSphere.setUniform("sampler",0);
		fragShaderSphere.setUniform("sampler2",1);	
		fragShaderSphere.setUniform("showTerrain", true);
		fragShaderSphere.setUniform("showGrid", true);
		fragShaderSphere.setUniform("BlendFactor", 1f);
		fragShaderSphere.setUniform("doRadiosity", false);

		ap = planeHolderSGC.getAppearance();
		ap.setAttribute("useGLSL", true);
		fragShaderPlane = null;
		try {
			fragShaderPlane = new GlslProgram(ap, "polygonShader",   
					useVertShader ? Input.getInput(vertShaderSource) : null,
					Input.getInput("charlesgunn/jreality/resources/textureBlendWithRadiosity.frag")
			    );
		} catch (IOException e) {
			e.printStackTrace();
		}
		fragShaderPlane.setUniform("sampler",0);
		fragShaderPlane.setUniform("sampler2",1);	
		fragShaderPlane.setUniform("showTerrain", true);
		fragShaderPlane.setUniform("showGrid", true);
		fragShaderPlane.setUniform("doRadiosity", doRadiosity);
		fragShaderPlane.setUniform("BlendFactor", 1f);
		fragShaderPlane.setUniform("attenExagg", planeScale);
		fragShaderPlane.setUniform("attenBlend", .5f);

		updateTextureCoordinates(Rn.identityMatrix(4));
		world.addChildren(fakeSphereSGC,planeHolderSGC);

		// construct star of lines through projection point
		starFactory = new StarLinesFactory();
		starSGC = starFactory.getSceneGraphComponent();
		starSGC.getAppearance().setAttribute(SceneGraphAnimator.ANIMATED, false);
		starSGC.setPickable(false);
		starSGC.setVisible(showStar);
		world.addChild(starSGC);
		
		timer = new Timer(20,new ActionListener()	{
			
			public void actionPerformed(ActionEvent e) {
				long time = System.currentTimeMillis();
				if (needsCorrection && time - lastInteractionTime > 100) {
					correctTextures = true;
					needsCorrection = false;
					updateTextureCoordinates(inverseSphereMatrix);
					lastInteractionTime = System.currentTimeMillis();
//					System.err.println("Correcting textures");
				}
				if (matrixChanged)	{
					if (correctTextures == true)	{
						if (planeSGC.getGeometry() != currentTexturedDisk) planeSGC.setGeometry( texturedDisk[lores ? 0 : 1]);
					}
					correctTextures = false;
					updateTextureCoordinates(inverseSphereMatrix);
					starFactory.setTransform(sphereMatrix);
					if (showStar) starFactory.update();
					lastInteractionTime = System.currentTimeMillis();
					needsCorrection = true;		
					matrixChanged = false;
				}
			}
			
		});
		timer.start();
		fakeSphereSGC.getTransformation().addTransformationListener(new TransformationListener() {

			public void transformationMatrixChanged(TransformationEvent ev) {
				final double[] matrix = fakeSphereSGC.getTransformation().getMatrix();
				matrix[3] = matrix[7] = matrix[11] = 0.0;
				System.arraycopy(matrix, 0, sphereMatrix, 0, 16);
				inverseSphereMatrix = Rn.inverse(null, matrix);
				matrixChanged = true;
				}
	
			});
		return world;
	}
	private void loadMars() {
		try {
			marsImage = ImageData.load(Input.getInput(marsTextureName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void loadEarthBetter() {
		try {
			earthBetterImage = ImageData.load(Input.getInput(earthBetterTextureName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void updateTextureCoordinates(final double[] imatrix) {
		final double[][] diskPoints = currentTexturedDisk.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		diskOnSphere = new double[diskPoints.length][];
		for (int i = 0; i<diskPoints.length; ++i)	{
			diskOnSphere[i] = CircleFactory.inverseStereoProj(null, planeScale*diskPoints[i][0], planeScale*diskPoints[i][1]);
		}
		double s, t;
		int count = 0;
		int pv = (lores ? pvdim/2 : pvdim);
		int pu = (lores ? pudim/2 : pudim);
		double[][] cv = currentTextureCoords;
		for (int i = 0; i<pv; ++i)	{
			for (int j = 0; j<pu; ++j)	{
			double[] inv = Rn.matrixTimesVector(null, imatrix, diskOnSphere[j+i*pu]);
			s = Math.atan2(inv[1], inv[0])/(Math.PI*2);
			s += .5;
			t = Math.asin(-inv[2])/(Math.PI);
			t += .5;
			cv[count][0] = s;
			cv[count][1] = t;
			count++;
			}
		}
		currentTexturedDisk.setVertexAttributes(Attribute.TEXTURE_COORDINATES, 
				StorageModel.DOUBLE_ARRAY.array(2).createReadOnly(cv));
//		correctTextures = false;
		if (animating) correctTextures = true;
		if (animating || correctTextures)	{
			if (!projectSphere) {
				IndexedFaceSet foo = IndexedFaceSetUtility.removeTextureCoordinateJumps(currentTexturedDisk, .5);
				foo.setVertexAttributes(attributeForName, foo.getVertexAttributes(Attribute.COORDINATES));
				planeSGC.setGeometry(foo);		
			} else {
				double[][] verts = Rn.matrixTimesVector(null, sphereMatrix, currentSphereVerts);
				double[][] nverts = new double[verts.length][3];
				for (int i = 0; i< verts.length; ++i)	{
					CircleFactory.stereoProj(nverts[i], verts[i]);
				}
				stPrFac[1].setVertexCoordinates(nverts);
				stPrFac[1].setVertexAttribute(attributeForName, Pn.dehomogenize(nverts, nverts));
				Vector<int[]> facelist = new Vector<int[]>();
				for (int i = 0; i<currentSphereInds.length; ++i){
					int[] kk = currentSphereInds[i];
					double[] v0 = nverts[kk[0]], v1 = nverts[kk[1]], v2 = nverts[kk[2]],
						v3 = nverts[kk[3]];
					double[] dettie = {v1[0] -v0[0],v1[1] - v0[1], v2[0] - v0[0], v2[1] - v0[1]};
					
					double d1 = Rn.determinant(dettie);
					dettie = new double[]{v2[0] -v0[0],v2[1] - v0[1], v3[0] - v0[0], v3[1] - v0[1]};
					double d2 = Rn.determinant(dettie);
					if (d1 > 0 || d2 > 0) {
//						System.err.println("rejecting face "+i);
						continue;
					}
					facelist.add(kk);
				}
				int[][] newinds =  facelist.toArray(new int[facelist.size()][]);
				stPrFac[1].setFaceCount(newinds.length);
				stPrFac[1].setFaceIndices(newinds);
				stPrFac[1].update();
			}
		} 
		realPlaneSGC.setVisible(correctTextures && projectSphere);
		planeSGC.setVisible(!realPlaneSGC.isVisible());
		fragShaderPlane.setUniform("attenExagg", planeSGC.isVisible() ?  planeScale : 1);

	}
		
	@Override
	/**
	 * This method is called once after the viewer has been set and gives us the chance to
	 * customize things.
	 */
	public void customize(JMenuBar menuBar, final PluginSceneLoader psl) {
		final Viewer viewer = psl.getViewer();
		MatrixBuilder.euclidean().translate(0, -1, 4).assignTo(CameraUtility.getCameraNode(viewer));
		AnimationPlugin apl = psl.getAnimationPlugin();
		apl.getAnimated().add(new AnimatedThing(this));
		Camera c = CameraUtility.getCamera(viewer);
		KeyFrameAnimatedBean<Camera> ab = new KeyFrameAnimatedBean<Camera>(c);
		ab.setName("cameraBean");
		apl.getAnimated().add(ab);					
//		KeyFrameAnimatedDelegate<Double> dd = new KeyFrameAnimatedDelegate<Double> () {
//
//			public void propagateCurrentValue(Double t) {
//				texSource.setBlendFactor(t);
//			}
//
//			public Double gatherCurrentValue(Double t) {
//				return texSource.getBlendFactor();
//			}
//			
//		};
//		KeyFrameAnimatedDouble animAlpha = new KeyFrameAnimatedDouble(dd );
//		animAlpha.setName("animAlpha");
//		apl.getAnimated().add(animAlpha);
		viewer.getSceneRoot().getAppearance().setAttribute(USE_OLD_TRANSPARENCY, true);
		viewer.getSceneRoot().getAppearance().setAttribute(BACKGROUND_COLOR, bkgd);
		// this is important since we are using same image twice in scene graph
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.ONE_TEXTURE2D_PER_IMAGE, true);
		CameraUtility.getCamera(viewer).setFar(50.0);
		// add a key listener
		((Component) viewer.getViewingComponent()).addKeyListener( new KeyAdapter()	{

			public void keyPressed(KeyEvent e)	{ 
				switch(e.getKeyCode())	{
					
				case KeyEvent.VK_H:
					System.out.println("	1:  toggle earth");
					System.out.println("	2:  toggle grid");
					System.out.println("	3:  flip orientation");
					System.out.println("	4:  toggle mars/earth");
					break;
	
				case KeyEvent.VK_1:
					showTerrain = !showTerrain;
					fragShaderSphere.setUniform("showTerrain", showTerrain);
					fragShaderPlane.setUniform("showTerrain", showTerrain);
					System.err.println("toggling show earth");
					world.getAppearance().setAttribute("polygonShader.diffuseColor", showTerrain ? Color.white : new Color(40, 140, 255));
					viewer.renderAsync();
					break;
					
				case KeyEvent.VK_2:
					showGrid = !showGrid;
					fragShaderSphere.setUniform("showGrid", showGrid);
					fragShaderPlane.setUniform("showGrid", showGrid);
					System.err.println("toggling show grid");
					viewer.renderAsync();
					break;
					
				case KeyEvent.VK_3:
					mirror = !mirror;
						if (mirror) earthTexture2d.setTextureMatrix(new Matrix(P3.makeReflectionMatrix(null, 
								new double[]{1,0,0,0}, Pn.EUCLIDEAN)));
						else earthTexture2d.setTextureMatrix(new Matrix());						
					viewer.renderAsync();
					break;
				
				case KeyEvent.VK_4:
					showEarth = !showEarth;
					if (!showEarth && marsImage == null) loadMars();
						 earthTexture2d.setImage(showEarth ? earthImage : marsImage);
					viewer.renderAsync();
					break;
				
				case KeyEvent.VK_5:
					lores = !lores;
					currentTexturedDisk = texturedDisk[ lores ? 0 : 1];
					planeSGC.setGeometry(currentTexturedDisk);
					currentSphericalPatch = sphericalPatch[ lores ? 0 : 1];
					sphereSGC.setGeometry(currentSphericalPatch);
					currentTextureCoords = textureCoords[ lores ? 0 : 1];
					updateTextureCoordinates(inverseSphereMatrix);
					viewer.renderAsync();
					break;
				
				case KeyEvent.VK_6:
					showStar = !showStar;
					starSGC.setVisible(showStar);
					viewer.renderAsync();
					break;
				
					
				case KeyEvent.VK_7:
					showBetterEarth = !showBetterEarth;
					if (showBetterEarth && earthBetterImage == null) loadEarthBetter();
					for (int i = 0; i<2; ++i)	{
						 earthTexture2d.setImage(showBetterEarth ? earthBetterImage : earthImage);
					}
					viewer.renderAsync();
					break;
				}
			}
		});
	}
	@Override
	public Component getInspector(Viewer v) {
		JPanel mypanel = new JPanel();
		mypanel.setName("ReadMe");
		JTextArea textarea = new JTextArea(10,20);
		textarea.setEditable(false);
		textarea.append("This is a demo of stereographic projection.\n"+
				"The unit sphere is projected from the North Pole \n"+
				"onto the plane z=-1. I use z=-1 instead of the\n" +
				"z=0 plane so the sphere remains completely visible.\n\n"+
				"When the mouse is over the sphere, dragging rotates\n"+
				"the sphere.\n\n"+
				"Note that while the sphere is rotating there is a \n" +
				"slight error in the texturing, going between\n " +
				"the images of the two poles.  When the sphere stops,\n " +
				"this error is corrected.\n\n"+
				"Otherwise, dragging the mouse invokes the current tool\n"+
				"on the world. Default tool is rotate tool\n"+
				"(Tools are selectable from the tool bar.)\n\n"+
				"Keyboard controls:\n"+
				"    '1':    toggle terrain texture\n"+
				"    '2':    toggle grid texture\n"+
				"    '3':    toggle orientation\n"+
				"    '4':    toggle earth/mars terrain\n" +
				"    '5':    toggle lo/hi res geometry models\n"+
				"    '6':    toggle display of projection lines\n"+
				"    'h':    display general help overlay for viewer\n\n"+
				"Try key '5' if the program runs too slow on your computer.\n"+
				"Use mouse click wheel to zoom in and out.\n"+
				"Shift-cntl-f  toggles fullscreen mode.\n"+
				"Click on the tab 'Scene Graph' to explore structure\n"+
				"\nEarth image: http://awka.sourceforge.net/xglobe.html\n"+
				"Mars image: www.nasa.org\n"+
				"\nAuthor: Charles Gunn\n"+
				"    gunn at math.tu-berlin.de\n" +
				"Contact me if you know of better public-domain\n " +
				"earth or mars images.\n");
		mypanel.add(textarea);
		return mypanel;
	}

	@Override
	public boolean hasInspector() {
		return true;
	}
	
	public void setValueAtTime(double t) {
		// TODO Auto-generated method stub
		
	}
	 
	public void startAnimation() {
		animating = true;
	}
	
	public void stopAnimation() {
		animating = false;
	}
//	public static void main(String[] args) {
//		final ViewerApp va = TestViewerApp.mainImpl(
//				new String[]{"charlesgunn.geometry.stereoproj.StereographicProjectionDemo"});
//		va.setAttachNavigator(true);
//		va.setAttachBeanShell(true);
//		va.update();
//		va.display();
//	}

}
