package charlesgunn.jreality.geometry;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EventObject;

import charlesgunn.anim.core.FramedCurve;
import charlesgunn.anim.jreality.SceneGraphAnimator;
import charlesgunn.jreality.newtools.RotateTool;
import charlesgunn.jreality.tools.RotateShapeTool;
import charlesgunn.jreality.viewer.GlobalProperties;
import de.jreality.backends.label.LabelUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.jogl.AbstractViewer;
import de.jreality.jogl.JOGLFBO;
import de.jreality.jogl.JOGLViewer;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.GlslProgram;
import de.jreality.shader.ImageData;
import de.jreality.shader.RootAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.CameraUtility;
import de.jreality.util.ImageUtility;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;

public class TermesSphere {

	Viewer sourceViewer;
	SceneGraphComponent 
		termesSphereSGC, 
		children[], 
		origCamRoot, 
		origCamRootExtended, 
		termesCamRoot, 
		onePanel;
	Texture2D 
		cubeTextures[], 
		letterTextures[];
	ImageData letterImages[];
	SceneGraphPath 
		termesCamPath, 
		origCamPath, 
		origCamPathExtended;
	Camera termesCamera;
	boolean flipped = false,
		showLabels = false,
		useSkyBox =  true,
		doFullDome = false,
		updateOnRender = false,
		separateViewer = true, 
		fastFBO = false,
		allowFog = false,
		origFogEnabled;
	boolean visible = false;
	
	RootAppearance rootAp;


	int size = 1024,
			panelRes = 75;
	boolean illustrate = false;
	Color labelColor = illustrate ? new Color(255, 180, 0) :  new Color(255,255,255,128);
	
	public TermesSphere(Viewer v)	{
		sourceViewer = v;
		if ( !(sourceViewer instanceof de.jreality.jogl.JOGLViewer)) {
			throw new UnsupportedOperationException("Only works with jogl viewer now");
		}
		initialize();
		Appearance ap = sourceViewer.getSceneRoot().getAppearance();
		rootAp = ShaderUtility.createRootAppearance(ap);
		final de.jreality.jogl.JOGLViewer jv = (JOGLViewer) sourceViewer;
		jv.addRenderListener(new AbstractViewer.RenderListener() {
			
			public void renderPerformed(EventObject e) {
				if (!visible && !jv.getCameraPath().equals(origCamPath) ) {
					updateOriginalCameraPath();
				}
				if (!updateOnRender) return;
				update();
				System.err.println("updating termes");
			}
		});
	}
	
	protected static String[] names = {"RT","LF","UP","DN","FT","BK"};
	protected static Color[] colors = {Color.red, Color.green, Color.blue, Color.orange, Color.yellow, new Color(255,0,255)};
	protected void initialize()	{
		termesSphereSGC = SceneGraphUtility.createFullSceneGraphComponent("termes sphere");
		Appearance ap = termesSphereSGC.getAppearance();
		ap.setAttribute(CommonAttributes.FACE_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, Color.white);
		termesShader = null;
		try {
			termesShader = new GlslProgram(ap, "polygonShader",   
					Input.getInput("charlesgunn/jreality/resources/termes.vert"),
					null
			    );
		} catch (IOException e) {
			e.printStackTrace();
		}
		updateTermesShader();

		IndexedFaceSet panel = oneSixthSphere(panelRes);
		onePanel = new SceneGraphComponent("one panel");
		onePanel.setGeometry(panel);
		if (illustrate)	{
			SceneGraphComponent cubesgc = new SceneGraphComponent("wireframe");
			cubesgc.setGeometry(Primitives.cube());
			cubesgc.setAppearance(new Appearance());
			ap  = cubesgc.getAppearance();
			ap.setAttribute(CommonAttributes.FACE_DRAW, false);
			ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
			ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
			ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, true);
			onePanel.addChild(cubesgc);
			ap.setAttribute("lineShader.diffuseColor", Color.yellow);
			ap.setAttribute("pointShader.diffuseColor", Color.red);
			ap.setAttribute(CommonAttributes.TUBE_RADIUS, .02);
			ap.setAttribute(CommonAttributes.POINT_RADIUS, .03);			
		}
		children = new SceneGraphComponent[6];
		cubeTextures = new Texture2D[6];
		letterTextures = new Texture2D[6];
		letterImages = new ImageData[6];
		// create a "dummy" image for now
//		SimpleTextureFactory stf = new SimpleTextureFactory();
//		stf.setType(TextureType.WEAVE);
//		stf.update();
//		ImageData id = stf.getImageData();
		for (int i = 0; i< 6; ++i)	{
			children[i] = SceneGraphUtility.createFullSceneGraphComponent("termes face "+i);
			Transformation tform = new Transformation(de.jreality.jogl.JOGLViewer.cubeMapMatrices[i].getArray());
			children[i].setTransformation(tform);
			tform.setReadOnly(true);
			children[i].addChild(onePanel);
			termesSphereSGC.addChild(children[i]);
			letterImages[i] = new ImageData(LabelUtility.createImageFromString(names[i],  
					new Font("Sans Serif",Font.PLAIN,192), labelColor));
			cubeTextures[i] = TextureUtility.createTexture(children[i].getAppearance(), "polygonShader", 0, letterImages[i]); //images[i]);
			if (!fastFBO)	{
				cubeTextures[i].setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
				cubeTextures[i].setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
				cubeTextures[i].setMinFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
				cubeTextures[i].setMagFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);				
			}
//			cubeTextures[i].setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
//			cubeTextures[i].setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);		
			letterTextures[i] = TextureUtility.createTexture(children[i].getAppearance(), "polygonShader",1,letterImages[i]); //images[i]);
			letterTextures[i].setApplyMode(Texture2D.GL_DECAL);
			letterTextures[i].setBlendColor(new Color(1f, 1f, 1f, 0f));		
			letterTextures[i].setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
			letterTextures[i].setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);	
		}
		updateOriginalCameraPath();

		termesCamPath = GlobalProperties.createDefaultCameraPath(sourceViewer.getSceneRoot(), "Termes");
		termesCamPath.getLastComponent().setAppearance(new Appearance());
		termesCamPath.getLastComponent().getAppearance().setAttribute(SceneGraphAnimator.ANIMATED, false);
		termesCamRoot = ((SceneGraphComponent) termesCamPath.get(1));
		termesCamRoot.setVisible(visible);
//		MatrixBuilder.euclidean().translate(0, 0, 2.5).assignTo(termesCamPath.getLastComponent());
		termesSphereSGC.setVisible(visible);
		termesSphereSGC.addTool(new RotateShapeTool());
		termesCamera = (Camera) termesCamPath.getLastElement();
		termesCamera.setNear(.005);
		termesCamera.setFar(4);
//		RenderTrigger trigger = new RenderTrigger();
//		trigger.addSceneGraphComponent(termesCamRoot);
//		trigger.addViewer(viewer);
		
		setShowLabels(showLabels);
//		BackgroundColorsTool.addBackgroundColorsTool(sourceViewer, termesCamPath);
//		trigger.
	}

	private void updateOriginalCameraPath() {
		origCamPath = new SceneGraphPath(sourceViewer.getCameraPath());
		origCamRoot = (SceneGraphComponent) origCamPath.get(1);
		origCamPathExtended = new SceneGraphPath(origCamPath);
		origCamPathExtended.pop();		// remove camera
		origCamRootExtended = new SceneGraphComponent("Termes extended camera");
		Camera origCam = CameraUtility.getCamera(sourceViewer);
		origCamRootExtended.setCamera(origCam);
		origCamPathExtended.push(origCamRootExtended);
		origCamPathExtended.push(origCam);
		System.err.println("ocpe = "+origCamPathExtended);
	}

	public void setVisible(boolean b)	{
		// every time we're asked to display Termes,
		// check to see if the camera path on the source viewer has changed
//		if (b && !visible) {
//			updateOriginalCameraPath();
//		}
		visible = b;
		// turn on/off fog
		if (!allowFog)	{
			if (visible)	{
				origFogEnabled = rootAp.getFogEnabled();
				rootAp.setFogEnabled(false);
			} else {
				rootAp.setFogEnabled(origFogEnabled);
			}
		}
		updateVisibility();
	}

	protected void updateVisibility()	{
		SceneGraphComponent root = sourceViewer.getSceneRoot();
		for (SceneGraphComponent child : root.getChildComponents()) {
//			if (child == origCamRoot) continue;
			boolean vis = (!visible) ^ (child == termesSphereSGC || child == termesCamRoot);
			child.setVisible(vis);
		}
		sourceViewer.setCameraPath( visible ? termesCamPath : origCamPath);
	}
	

	public boolean getVisible() {
		return visible;
	}

	public void update()		{
		if (cubemap == null) {
			if (!visible) {
				origFogEnabled = rootAp.getFogEnabled();
			}
			boolean ov = visible;
			setVisible(false);
			if (!allowFog) rootAp.setFogEnabled(origFogEnabled);
			sourceViewer.setCameraPath(origCamPathExtended);
			MatrixBuilder.euclidean().assignTo(CameraUtility.getCameraNode(sourceViewer));
			((de.jreality.jogl.JOGLViewer) sourceViewer).renderCubeMap(cubeTextures, size, fastFBO);
			sourceViewer.setCameraPath(origCamPath);		
			setVisible(ov);
		} else {
			// use existing cube map
			ImageData[] sides =  TextureUtility.getCubeMapImages(cubemap);
			for (int i = 0; i<6; ++i)	{
				cubeTextures[i].setImage(sides[i]);
			}
		}
	}
	


	protected void updateTermesShader() {
		// rFOV is half the FOV in radians
		double rFOV = Math.PI*FOV/360.0;
		termesShader.setUniform("FOV", rFOV);
		termesShader.setUniform("cosFOV", Math.cos(rFOV));
		termesShader.setUniform("sinFOV", Math.sin(rFOV));
		Appearance ap = termesSphereSGC.getAppearance();
		ap.setAttribute("useGLSL", doFullDome && whichPlace == 0);
		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, doFullDome && whichPlace == 0);
		ap.setAttribute(CommonAttributes.FACE_DRAW, true);
	}
	
	public SceneGraphComponent getSceneGraphComponent() {
		return termesSphereSGC;
	}
	
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setFlipped(boolean b)	{
		flipped = b;
		MatrixBuilder.euclidean().scale(b ? -1 : 1).assignTo(onePanel);
	}
	
	public boolean isFlipped() {
		return flipped;
	}
	
	public boolean isShowLabels() {
		return showLabels;
	}

	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
		for (int i = 0; i<6; ++i)	{
			if (!showLabels)	children[i].getAppearance().setAttribute("polygonShader.texture2d[1]", Appearance.INHERITED);
			else TextureUtility.createTexture(children[i].getAppearance(), "polygonShader",1,letterImages[i]);
		}
	}

	private CubeMap cubemap = null;
	public void setCubeMap(CubeMap cm)	{
		cubemap = cm;
	}
	boolean sweetSpot = false;
	int whichPlace = 0;
	double oldFOV = 90;
	double FOV = 125;
	Matrix oldM = new Matrix();
	private GlslProgram termesShader;
	public void cycleCameraPosition()	{
//		sweetSpot = !sweetSpot;
		System.err.println("cycling camera: "+whichPlace);
		whichPlace = (whichPlace+1)%3;
		setCameraPosition(whichPlace);
	}
	public void setCameraPosition(int which) {
		whichPlace = which;
		Camera camera = CameraUtility.getCamera(sourceViewer);
		switch (which) {
		case 0:
			oldM.assignFrom(termesCamPath.getLastComponent().getTransformation().getMatrix());
			MatrixBuilder.euclidean().translate(0, 0, 1).assignTo(termesCamPath.getLastComponent());
//			oldFOV = CameraUtility.getCamera(sourceViewer).getFieldOfView();
			camera.setPerspective(true);
			camera.setFieldOfView(FOV);	
			break;
		case 1:
			MatrixBuilder.euclidean().translate(0, 0, 2.5).assignTo(termesCamPath.getLastComponent());
			//oldM.assignTo(termesCamPath.getLastComponent());
			double exactlyThisFOV = 2*(180.0/Math.PI)*Math.atan2(1.1, 2.5);
			camera.setPerspective(false);
			if (!illustrate) camera.setFieldOfView(exactlyThisFOV);
			break;
		case 2:
			camera.setPerspective(true);
			MatrixBuilder.euclidean().translate(0, 0, 0).assignTo(termesCamPath.getLastComponent());
			camera.setFieldOfView(FOV);	
			break;
		default:
			break;
		}
		updateTermesShader();

//		if (sweetSpot)	{
//			oldM.assignFrom(termesCamPath.getLastComponent().getTransformation().getMatrix());
//			MatrixBuilder.euclidean().translate(0, 0, 1).assignTo(termesCamPath.getLastComponent());
//			oldFOV = CameraUtility.getCamera(viewer).getFieldOfView();
//			CameraUtility.getCamera(viewer).setFieldOfView(FOV);	
//		} else {
//			oldM.assignTo(termesCamPath.getLastComponent());
//			CameraUtility.getCamera(viewer).setFieldOfView(oldFOV);				
//		}
	}

	IndexedFaceSet oneSixthSphere( int n)	{
		QuadMeshFactory qmf = new QuadMeshFactory();
		qmf.setULineCount(n);
		qmf.setVLineCount(n);
		qmf.setClosedInUDirection(false);
		qmf.setClosedInVDirection(false);
		double[][] verts = new double[n*n][3];
		for (int i = 0; i<n; ++i)	{
			double y = 1.0 - 2 * (i/(n-1.0));
			for (int j = 0 ; j<n ; ++j)	{
				double x = -1.0 + 2 * (j/(n-1.0));
				double[] v = {x,y,-1.0};
//				if (!illustrate) 
					Rn.normalize(v,v);
				System.arraycopy(v,0,verts[i*n+j], 0, 3);
			}
		}
		qmf.setVertexCoordinates(verts);
		qmf.setVertexNormals(verts);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateTextureCoordinates(true);
		qmf.update();
		return qmf.getIndexedFaceSet();
	}

	public double getFOV() {
		return FOV;
	}

	public void setFOV(double fOV) {
		FOV = fOV;
		CameraUtility.getCamera(sourceViewer).setFieldOfView(FOV);	
		updateTermesShader();
	}

	public boolean isDoFullDome() {
		return doFullDome;
	}

	public void setDoFullDome(boolean doFullDome) {
		this.doFullDome = doFullDome;
		updateTermesShader();
	}

	public boolean isUpdateOnRender() {
		return updateOnRender;
	}

	public void setUpdateOnRender(boolean updateOnRender) {
		this.updateOnRender = updateOnRender;
	}

	public Camera getTermesCamera() {
		return termesCamera;
	}

	public void setTermesCamera(Camera termesCamera) {
		this.termesCamera = termesCamera;
	}
	String[] suffixes = {"rt","lf","up","dn","bk","ft"}; //{"bk","ft","up","dn","lf","rt"};
	public void writeCubeMap(String stem)	{
		BufferedImage[] images = ((de.jreality.jogl.JOGLViewer) sourceViewer).renderCubeMap(size);
		for (int i = 0; i<6; i++)	{
			File outfile = new File(stem+"."+suffixes[i]+".png");
			ImageUtility.writeBufferedImage(outfile, images[i]);
		}
	}

	public boolean isFastFBO() {
		return fastFBO;
	}

	public void setFastFBO(boolean fastFBO) {
		this.fastFBO = fastFBO;
	}
	private final double[] standard = {0,0,-1,1};
	public  SceneGraphComponent getIllustrativeSGC(double d)	{
		
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("termes illustr"),
				cameraRepn = FramedCurve.cameraIcon(1),
				frustrumRepn = SceneGraphUtility.createFullSceneGraphComponent("frustum"),
				clippedSphereSGC = SceneGraphUtility.createFullSceneGraphComponent("clipped sphere"),
				screenSGC = SceneGraphUtility.createFullSceneGraphComponent("screen"),
				wireframeSGC = Primitives.wireframeSphere();
		
		world.getAppearance().setAttribute("polygonShader.diffuseColor", Color.white);
		world.getAppearance().setAttribute("lineShader.diffuseColor", Color.darkGray);
		world.getAppearance().setAttribute("lineShader.tubeRadius", .01);
		world.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		world.addChildren(cameraRepn, frustrumRepn, clippedSphereSGC, screenSGC,wireframeSGC  );
		wireframeSGC.getAppearance().setAttribute("lineShader.diffuseColor", Color.gray);
		MatrixBuilder.euclidean().scale(.98).assignTo(wireframeSGC);
		// set up the camera representation
		cameraRepn.getAppearance().setAttribute("polygonShader.diffuseColor", Color.LIGHT_GRAY);
		MatrixBuilder.euclidean().translate(0,0,1).scale(.1).scale(3,1,.2).assignTo(cameraRepn);

		
		double fov = termesCamera.getFieldOfView(),
				aspectRatio = CameraUtility.getAspectRatio(sourceViewer);
		fov = fov*Math.PI/180.0;
		System.err.println("fov = "+fov);
		double fov2 = 2*Math.atan(Math.tan(fov/2.0) * aspectRatio);
		// set up the screen
		double factor = Math.tan(fov/2)*(d+1);
		double[] scos = {
				-aspectRatio*factor, factor, -d,
				aspectRatio*factor, factor, -d,
				aspectRatio*factor, -factor, -d,
				-aspectRatio*factor, -factor, -d,
		};
		screenSGC.setGeometry(Primitives.texturedQuadrilateral(scos));
		// apply one of the texture maps from the cube map as a test
		JOGLFBO fbo = new JOGLFBO((int) (aspectRatio*800), 800);
		fbo.setAsTexture(true);
		Texture2D ricky = TextureUtility.createTexture(screenSGC.getAppearance(), "polygonShader", 0, letterImages[0]); //images[i]);
		((de.jreality.jogl.JOGLViewer) sourceViewer).getRenderer().getOffscreenRenderer().renderOffscreen(fbo, ricky, 
				(int) (aspectRatio*800), 800);
		screenSGC.getAppearance().setAttribute("lightingEnabled", false);
		// set up the edges of the frustum
		// these three points determine two lines which we need to rotate the clipping planes from z=1 to the correct position on the frustum
		double[] center = {0,0,1,1}, fred = {1,0,0,0}, ethel = {0,1,0,0};
		double[][] verts  = {
				center,
				{aspectRatio*factor, factor, -d, 1},
				{-aspectRatio*factor, factor, -d, 1},
				{-aspectRatio*factor, -factor, -d, 1},
				{aspectRatio*factor, -factor, -d, 1}		
		};
		int[][] inds = {{0,1},{0,2},{0,3},{0,4}};
		IndexedLineSetFactory frustumFactory = new IndexedLineSetFactory();
		frustumFactory.setVertexCount(verts.length);
		frustumFactory.setVertexCoordinates(verts);
		frustumFactory.setEdgeCount(inds.length);
		frustumFactory.setEdgeIndices(inds);
		frustumFactory.update();
		frustrumRepn.setGeometry(frustumFactory.getIndexedLineSet());
//		frustrumRepn.getAppearance().setAttribute("lineShader.diffuseColor", Color.DARK_GRAY);
		frustrumRepn.getAppearance().setAttribute("lineShader.tubeRadius", .003);
		
		// create clipping planes for the sphere determined by the frustum
		SceneGraphComponent[] clipSGC = new SceneGraphComponent[4];
		double[][] clipPlanes = new double[4][];
		Matrix mm = new Matrix();
		MatrixBuilder.euclidean().rotate(center, fred, Math.PI/2-fov/2).assignTo(mm);
		mm = new Matrix(Rn.inverse(null, Rn.transpose(null, mm.getArray())));
		clipPlanes[0] = mm.multiplyVector(standard);
		MatrixBuilder.euclidean().rotate(center, fred, -(Math.PI/2-fov/2)).assignTo(mm);
		mm = new Matrix(Rn.inverse(null, Rn.transpose(null, mm.getArray())));
		clipPlanes[1] = mm.multiplyVector(standard);
		MatrixBuilder.euclidean().rotate(center, ethel, Math.PI/2-fov2/2).assignTo(mm);
		mm = new Matrix(Rn.inverse(null, Rn.transpose(null, mm.getArray())));
		clipPlanes[2] = mm.multiplyVector(standard);
		MatrixBuilder.euclidean().rotate(center, ethel, -(Math.PI/2-fov2/2)).assignTo(mm);
		mm = new Matrix(Rn.inverse(null, Rn.transpose(null, mm.getArray())));
		clipPlanes[3] = mm.multiplyVector(standard);
		// sigh ... this is ugly
		for (int i = 0; i<4; ++i)	{
			clipSGC[i] = new SceneGraphComponent("clip"+i);
			ClippingPlane cp = new ClippingPlane();
			cp.setLocal(true);
			cp.setPlane(clipPlanes[i]);
			clipSGC[i].setGeometry(cp);
			if (i > 0) clipSGC[i-1].addChild(clipSGC[i]);
		}
		clippedSphereSGC.addChild(clipSGC[0]);
		clipSGC[3].addChild(termesSphereSGC);
		
		return world;
		
	}
}
