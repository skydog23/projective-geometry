package charlesgunn.jreality.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuBar;
import javax.swing.Timer;

import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.Primitives;
import de.jreality.jogl.AbstractViewer;
import de.jreality.jogl.JOGLFBOViewer;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.tools.RotateTool;
import de.jreality.ui.viewerapp.ViewerSwitch;
import de.jreality.util.RenderTrigger;
import de.jreality.util.SceneGraphUtility;

public class TestFBOTexture extends LoadableScene  {

	Matrix tm = new Matrix();
	double t = 0.0, dt =.001;
	@Override
	public SceneGraphComponent makeWorld() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		IndexedFaceSet square = Primitives.texturedQuadrilateral();
		world.setGeometry(square);
		world.getAppearance().setAttribute("polygonShader.diffuseColor", Color.white);
		world.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		world.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ImageData img = null;
		SimpleTextureFactory stf = new SimpleTextureFactory();
		stf.update();
		img = stf.getImageData(); 
		tex2d = TextureUtility.createTexture(world.getAppearance(), "polygonShader", img);
		tex2d.setTextureMatrix(tm);
		
		return world;
	}
	
	JOGLFBOViewer joglFBOViewer;
	private void setupJOGLViewer() {
	    SceneGraphComponent rootNode = new SceneGraphComponent("root");
	    SceneGraphComponent cameraNode = new SceneGraphComponent("camera");
	    final SceneGraphComponent geometryNode = new SceneGraphComponent("geometry");
	    SceneGraphComponent lightNode = new SceneGraphComponent("light");
	    
	    rootNode.addChild(geometryNode);
	    rootNode.addChild(cameraNode);
	    cameraNode.addChild(lightNode);
	    
	    Light dl=new DirectionalLight();
	    lightNode.setLight(dl);
	    
	    IndexedFaceSet ifs = Primitives.icosahedron(); 
	    geometryNode.setGeometry(ifs);
	    
	    RotateTool rotateTool = new RotateTool();
	    geometryNode.addTool(rotateTool);

	    MatrixBuilder.euclidean().translate(0, 0, 3).assignTo(cameraNode);

		Appearance rootApp= new Appearance();
	    rootApp.setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(0f, .1f, .1f));
	    rootApp.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(1f, 0f, 0f));
	    rootNode.setAppearance(rootApp);
	        
	    Camera camera = new Camera();
	    cameraNode.setCamera(camera);
	    SceneGraphPath camPath = new SceneGraphPath(rootNode, cameraNode);
	    camPath.push(camera);
	    
		joglFBOViewer = new JOGLFBOViewer(camPath, rootNode);
		joglFBOViewer.setSize(new Dimension(512,512));
		joglFBOViewer.setTexture2D(tex2d);

	    Timer timer = new Timer(20, new ActionListener() {
	    	double t = 0;
			public void actionPerformed(ActionEvent e) {
				double[] mat = MatrixBuilder.euclidean().rotateY(t).getArray();
				new Matrix(mat).assignTo(geometryNode);
				t += .01;
				MatrixBuilder.euclidean().assignTo(world);
			}
	    	
	    });
	    timer.start();
	}
	
	Viewer viewer;
	private Texture2D tex2d;
	private SceneGraphComponent world;
	@Override
	public void customize(JMenuBar menuBar, Viewer viewer) {
		this.viewer = viewer;
		setupJOGLViewer();
	    if (viewer instanceof AbstractViewer) 
	    	((AbstractViewer) viewer).fbo = joglFBOViewer;
	    RenderTrigger rt = new RenderTrigger();
	    rt.addSceneGraphComponent(world);
	    rt.addViewer(viewer);
	}

	@Override
	public boolean isEncompass() {
		return true;
	}

	public static void main(String args[])	{
		TestFBOTexture tfbot = new TestFBOTexture();
		tfbot.makeWorld();
		Viewer jrv = JRViewer.display(tfbot.world);
		tfbot.setupJOGLViewer();
		if (jrv instanceof ViewerSwitch)	jrv = ((ViewerSwitch)jrv).getCurrentViewer();
		if (jrv instanceof AbstractViewer) ((AbstractViewer) jrv).fbo = tfbot.joglFBOViewer;
	    RenderTrigger rt = new RenderTrigger();
	    rt.addSceneGraphComponent(tfbot.world);
	    rt.addViewer(jrv);

	}
}
