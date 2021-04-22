package charlesgunn.jreality.geometry.projective;

import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.CENTER_ON_BOUNDING_BOX;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_WIDTH;
import static de.jreality.shader.CommonAttributes.PICKABLE;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.USE_OLD_TRANSPARENCY;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;

import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.jreality.tools.RotateShapeTool;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Sphere;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.SelectionManagerImpl;
import de.jreality.util.CameraUtility;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.util.SceneGraphUtility;

public class CircleDemo extends LoadableScene{
	
	Color bkgd = new Color(0,0,130);
	int udim = 60, vdim = 40, pudim = 100, pvdim = 100;
	int uTextureScale = 40, vTextureScale = 30;
	double planeScale = 15;
	int starSamples = 16;
	private SceneGraphComponent fakeSphereSGC, // totally transparent lo-res sphere used only for rotating
		sphereSGC,			// child of fakeSphereSGC, contains real, hi-res sphere w/ texture
		planeSGC,			// projection plane z=-1
		starSGC;			// set of projection lines 
	double[][] diskOnSphere = null;
	double[][][] textureCoords = { new double[(pudim/2)*(pvdim/2)][2], new double[(pudim)*(pvdim)][2]};
	double[][] currentTextureCoords = textureCoords[1];
	private IndexedFaceSet[] texturedDisk = new IndexedFaceSet[2];
	IndexedFaceSet currentTexturedDisk;
	private IndexedFaceSet[] sphericalPatch = new IndexedFaceSet[2];
	IndexedFaceSet currentSphericalPatch;
	IndexedLineSet currentStar;
	String gridTextureName = "/gunn_local/TUB-HomepageOct19/WWW/Pictures/textures/grid256.jpg"; //
	String earthBetterTextureName = "/gunn_local/TUB-HomepageOct19/WWW/Pictures/textures/Earth2048-XXX.jpg"; //grid256.jpg")); //
	String earthTextureName = "/gunn_local/TUB-HomepageOct19/WWW/Pictures/textures/Earth2048.jpg"; //grid256.jpg")); //
	String marsTextureName = "/gunn_local/TUB-HomepageOct19/WWW/Pictures/textures/mars2048.jpg"; //http://www.math.tu-berlin.de/~gunn/Pictures/textures/mars2048.jpg"; //grid256.jpg")); //
	private ImageData earthImage, marsImage, earthBetterImage;
	boolean correctTextures = false, 
		needsCorrection = false,
		matrixChanged = false,
		showTerrain = true, 
		showGrid = true, 
		mirror = false, 
		showEarth = true, 
		showStar = false,
		showBetterEarth = false,
		lores = false;
	long lastInteractionTime = 0;
	private double[] sphereMatrix = Rn.identityMatrix(4), 
		inverseSphereMatrix = Rn.identityMatrix(4);
	Appearance[][] aps = new Appearance[2][2];
	private Texture2D earthTexture2d[] = new Texture2D[2];
	private Texture2D gridTexture2d[] = new Texture2D[2];
	private Timer timer;

	@Override
	public SceneGraphComponent makeWorld() {
		final SceneGraphComponent world = new SceneGraphComponent("world");
		MatrixBuilder.euclidean().rotateX(-Math.PI/3).assignTo(world);
//		world.addTool(new PickShowTool());
		Appearance ap = new Appearance();
		world.setAppearance(ap);
		ap.setAttribute(EDGE_DRAW, false);	
		ap.setAttribute(TUBES_DRAW, false);
		ap.setAttribute(LINE_WIDTH, 1.7);
		ap.setAttribute(CENTER_ON_BOUNDING_BOX, false);
		ap.setAttribute("polygonShader.diffuseColor", new Color(1f,1f,1f,1f));
		fakeSphereSGC = SceneGraphUtility.createFullSceneGraphComponent("fake sphere");
		fakeSphereSGC.addTool(new RotateShapeTool());
		fakeSphereSGC.getAppearance().setAttribute(TRANSPARENCY_ENABLED, true);
		fakeSphereSGC.getAppearance().setAttribute(TRANSPARENCY, 1.0);
//		MatrixBuilder.euclidean().scale(1.01).assignTo(fakeSphereSGC);
		fakeSphereSGC.setGeometry(new Sphere());
		
		sphereSGC = new SceneGraphComponent("sphere");
		fakeSphereSGC.addChild(sphereSGC);
		sphericalPatch[0] = SphereUtility.sphericalPatch(0.0, 0.0, 360.0, 180.0, udim/2, vdim/2, 1.0);
		sphericalPatch[1] = SphereUtility.sphericalPatch(0.0, 0.0, 360.0, 180.0, udim, vdim, 1.0);
		currentSphericalPatch = sphericalPatch[1];
		sphereSGC.setGeometry(currentSphericalPatch);
		try {
			earthImage = ImageData.load(Input.getInput(earthTextureName));
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		for (int i = 0; i<2; ++i)	{
			for (int j = 0; j<2; ++j)	{
				aps[i][j] = new Appearance();
				aps[i][j].setAttribute(TRANSPARENCY_ENABLED, false);
				aps[i][j].setAttribute(TRANSPARENCY,0.0);
				aps[i][j].setAttribute(PICKABLE,false);
				if (i==1)	{		// show earth
					aps[i][j].setAttribute("polygonShader.diffuseColor", Color.white);
					earthTexture2d[j] = (Texture2D) AttributeEntityUtility.createAttributeEntity(
							Texture2D.class, "polygonShader.lightMap", aps[i][j], true);
					earthTexture2d[j].setImage(earthImage);
					earthTexture2d[j].setApplyMode(Texture2D.GL_MODULATE);					
					if (mirror) earthTexture2d[j].setTextureMatrix(new Matrix(P3.makeReflectionMatrix(null, 
							new double[]{1,0,0,0}, Pn.EUCLIDEAN)));
				} else
					aps[i][j].setAttribute("polygonShader.diffuseColor", new Color(40, 40, 255));
				if (j==1)	{		// show grids
					gridTexture2d[i] = (Texture2D) AttributeEntityUtility
				       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d", aps[i][j], true);
					SimpleTextureFactory stf = new SimpleTextureFactory();
					stf.setType(SimpleTextureFactory.TextureType.LINE);
					stf.update();
					gridTexture2d[i].setImage(stf.getImageData());
					gridTexture2d[i].setTextureMatrix(new Matrix(P3.makeStretchMatrix(null,new double[]{uTextureScale,vTextureScale,1})));
					gridTexture2d[i].setApplyMode(Texture2D.GL_DECAL);
//					gridTexture2d[i].setBlendColor(new Color(1f, 1f, 1f, 1f));							
				}
			}
		}
		sphereSGC.setAppearance(aps[ showTerrain ? 1 : 0][showGrid ? 1 : 0]);

		world.addChild(fakeSphereSGC);
		
		planeSGC = new SceneGraphComponent("textured disk");
		texturedDisk[0] = GeometryUtilityOverflow.texturedDisk(pudim/2, pvdim/2);
		texturedDisk[1] = GeometryUtilityOverflow.texturedDisk(pudim, pvdim);
		currentTexturedDisk = texturedDisk[1];
		planeSGC.setGeometry(currentTexturedDisk);
		MatrixBuilder.euclidean().translate(0,0,-1.002).scale(planeScale*2).translate(0,0,0).assignTo(planeSGC);
		ap = sphereSGC.getAppearance(); //new Appearance();
		planeSGC.setAppearance(ap);
		PickUtility.setPickable(planeSGC, false, false, true);
		
		updateDiskOnSphere();
		updateTextureCoordinates(Rn.identityMatrix(4));
		world.addChild(planeSGC);

		// construct star of lines through projection point
		starFactory = new StarLinesFactory();
		starSGC = starFactory.getSceneGraphComponent();
		starSGC.setPickable( false);
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
					starFactory.update();
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
	private void updateDiskOnSphere() {
		final double[][] diskPoints = currentTexturedDisk.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		diskOnSphere = new double[diskPoints.length][];
		for (int i = 0; i<diskPoints.length; ++i)	{
			diskOnSphere[i] = CircleFactory.inverseStereoProj(null, planeScale*diskPoints[i][0], planeScale*diskPoints[i][1]);
		}
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
		if (correctTextures)	{
			IndexedFaceSet foo = IndexedFaceSetUtility.removeTextureCoordinateJumps(currentTexturedDisk, .5);
			planeSGC.setGeometry(foo);			
		} 
	}
		
	@Override
	public void customize(JMenuBar menuBar, final Viewer viewer) {
		SceneGraphPath toFake = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), fakeSphereSGC).get(0);
		SelectionManager smi = SelectionManagerImpl.selectionManagerForViewer(viewer);
		smi.addSelection(smi.getSelectionPath());
		smi.addSelection(toFake);

		MatrixBuilder.euclidean().translate(0, -1, 4).assignTo(CameraUtility.getCameraNode(viewer));
		DefaultMatrixSupport.getSharedInstance().storeDefaultMatrices(viewer.getSceneRoot());
		DirectionalLight dl = new DirectionalLight();
		dl.setIntensity(.25);
		dl.setColor(Color.white);
		SceneGraphComponent lightSGC = new SceneGraphComponent();
		MatrixBuilder.euclidean().rotateX(-Math.PI/2).assignTo(lightSGC);
		lightSGC.setLight(dl);
		viewer.getSceneRoot().getAppearance().setAttribute(USE_OLD_TRANSPARENCY, true);
		viewer.getSceneRoot().getAppearance().setAttribute(BACKGROUND_COLOR, bkgd);
		CameraUtility.getCamera(viewer).setFar(50.0);
		CameraUtility.getCameraNode(viewer).addChild(lightSGC);
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
					sphereSGC.setAppearance(aps[ showTerrain ? 1 : 0][showGrid ? 1 : 0]);
					planeSGC.setAppearance(aps[ showTerrain ? 1 : 0][showGrid ? 1 : 0]);
					viewer.renderAsync();
					break;
					
				case KeyEvent.VK_2:
					showGrid = !showGrid;
					sphereSGC.setAppearance(aps[ showTerrain ? 1 : 0][showGrid ? 1 : 0]);
					planeSGC.setAppearance(aps[ showTerrain ? 1 : 0][showGrid ? 1 : 0]);
					viewer.renderAsync();
					break;
					
				case KeyEvent.VK_3:
					mirror = !mirror;
					for (int i = 0; i<2; ++i)	{
						if (mirror) earthTexture2d[i].setTextureMatrix(new Matrix(P3.makeReflectionMatrix(null, 
								new double[]{1,0,0,0}, Pn.EUCLIDEAN)));
						else earthTexture2d[i].setTextureMatrix(new Matrix());						
					}
					viewer.renderAsync();
					break;
				
				case KeyEvent.VK_4:
					showEarth = !showEarth;
					if (!showEarth && marsImage == null) loadMars();
					for (int i = 0; i<2; ++i)	{
						 earthTexture2d[i].setImage(showEarth ? earthImage : marsImage);
					}
					viewer.renderAsync();
					break;
				
				case KeyEvent.VK_5:
					lores = !lores;
					currentTexturedDisk = texturedDisk[ lores ? 0 : 1];
					planeSGC.setGeometry(currentTexturedDisk);
					currentSphericalPatch = sphericalPatch[ lores ? 0 : 1];
					sphereSGC.setGeometry(currentSphericalPatch);
					currentTextureCoords = textureCoords[ lores ? 0 : 1];
					updateDiskOnSphere();
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
						 earthTexture2d[i].setImage(showBetterEarth ? earthBetterImage : earthImage);
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
	static double[] projPoint = {0,0,1};
	private StarLinesFactory starFactory;
	static double[][] planeEquations = {{1,0,0,0},{0,1,0,0},{0,0,1,0}};
	static Matrix movePlane = new Matrix(), expandSphere = new Matrix();
	static {
		MatrixBuilder.euclidean().translate(0, 0, 1).scale(2).translate(0,0,-1).assignTo(movePlane);		
		MatrixBuilder.euclidean().scale(1.01).assignTo(expandSphere);
	}
	private class StarLinesFactory		{
		int samples = 32;
		int curves = 3;		// first attempt: three orthogonal great circles of sample rays
		IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
		int[][] indices = new int[curves*samples][2];
		double[][] verts = new double[curves*samples][3];
		double[] transform = Rn.identityMatrix(4);
		SceneGraphComponent sgc, star, plane, sphere, sphereIcon;
		CircleFactory[] circles = new CircleFactory[curves];
		Color starColor = new Color(220,255,255);
		
		StarLinesFactory()	{
			sgc = new SceneGraphComponent("star root");
			star = new SceneGraphComponent("star");
			plane = new SceneGraphComponent("plane");
			sphere = new SceneGraphComponent("sphere");
			sphereIcon = new SceneGraphComponent("sphereIcon");
			sgc.addChildren(star, sphereIcon);
			MatrixBuilder.euclidean().translate(0,0,1).scale(.05).assignTo(sphereIcon);
			sphereIcon.setGeometry(new Sphere());
			Appearance ap = new Appearance();
			sphereIcon.setAppearance(ap);
			ap.setAttribute("polygonShader.diffuseColor", starColor);
			ap = new Appearance();
			sgc.setAppearance(ap);
			movePlane.assignTo(star);
			ap.setAttribute("lineShader.diffuseColor", starColor);
			ap.setAttribute(EDGE_DRAW, true);
			for (int i = 0; i<3; ++i)	{
				circles[i] = new CircleFactory();
				circles[i].setPlaneEquation(planeEquations[i]);
				circles[i].update();
				sgc.addChildren(plane, sphere);
				plane.addChild(circles[i].getPlaneSGC());
				sphere.addChild(circles[i].getSphereSGC());
				movePlane.assignTo(plane);
				expandSphere.assignTo(sphere);
				for (int j = 0; j<samples; ++j)	{
					double angle = (j*Math.PI*2.0)/(samples);
					int ind = i*samples+j;
					verts[ind][i] = Math.cos(angle);
					verts[ind][(i+1)%3] = Math.sin(angle);
					verts[ind][(i+2)%3] = 0.0;
					indices[ind][0] = ind;
					indices[ind][1] = ind+curves*samples;
				}
			}
//			verts[curves*samples] = projPoint;
			ilsf.setVertexCount(2*verts.length);
			ilsf.setEdgeCount(indices.length);
			ilsf.setEdgeIndices(indices);
			update();
			IndexedLineSet currentStar = ilsf.getIndexedLineSet();
			star.setGeometry(currentStar);
		}
		
		void setTransform(double[] t)	{
			transform = t;
		}
		
		SceneGraphComponent getSceneGraphComponent()	{
			return sgc;
		}
		
		void update()	{
			int n = curves*samples;
			for (int i = 0; i<curves; ++i)	{
				double[] newP = Rn.matrixTimesVector(null, transform, planeEquations[i]);
				circles[i].setPlaneEquation(newP);
				circles[i].update();
			}
			double[][] newVerts = new double[n*2][3];
			Rn.matrixTimesVector(newVerts, transform, verts);
			for (int i = 0; i<curves*samples; ++i)	{
				newVerts[i] = CircleFactory.stereoProj(newVerts[i], newVerts[i]);
				newVerts[i][2] = 1.0;
				double length = Rn.euclideanNorm(newVerts[i]);
				double factor = .1/length;
				newVerts[i][2] = 0.0;
				Rn.linearCombination(newVerts[i+n], 1+factor, projPoint, -factor, newVerts[i]);
			}
			ilsf.setVertexCoordinates(newVerts);
			ilsf.update();
		}
	}
	//		        Scene.executeReader(world, new Runnable() {
//	public void run() {
//	for (int i = 0; i<circles.length; ++i)	{
//		double[] newP = Rn.matrixTimesVector(null, matrix, originalCircles[i].getPlaneEquation());
//		circles[i].setPlaneEquation(newP);
//		circles[i].update();
//	}

//	private void updatePlaneGeometry(final double[] matrix)	{
//	double[][] pp = Rn.matrixTimesVector(null, matrix, spherePoints), spp;
//	spp = new double[pp.length][3];
//	for (int i = 0; i<spherePoints.length; ++i)	{
//		if (i > 0 && 1 - pp[i][2] < 10E-8) {
//			System.arraycopy(spp[i-1], 0, spp[i], 0, 3);
//			continue;
//		}
//		spp[i] = CircleFactory.stereoProj(spp[i],pp[i]);
//		spp[i][2] = 0.0;
//	}
//	texturedDisk.setVertexAttributes(Attribute.COORDINATES, 
//			StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(spp));
//}

//	int samples = 30;
//	CircleFactory[] circles = new CircleFactory[2*samples],
//		originalCircles = new CircleFactory[2*samples];
//	planeCircles.getAppearance().setAttribute("lineShader.diffuseColor", new Color(255,255,100));
//	sphereCircles.getAppearance().setAttribute("lineShader.diffuseColor", new Color(100,0,100));
//	planeCircles.getAppearance().setAttribute(PICKABLE, false);
//	sphereCircles.getAppearance().setAttribute(PICKABLE, false);
//	world.addChildren(sphereCircles, planeCircles);
//	MatrixBuilder.euclidean().translate(0,0,-1).scale(2).assignTo(planeCircles);
//	planeCircles.setVisible(false);
//	sphereCircles.setVisible(false);
//
}
