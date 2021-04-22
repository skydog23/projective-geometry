package charlesgunn.jreality.viewer;

import java.awt.Color;

import de.jreality.math.P3;
import de.jreality.portal.PortalCoordinateSystem;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.RemotePortalHeadMoveTool;
import de.jreality.tutorial.util.FlyTool;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.SystemProperties;

public class GlobalProperties {

	public final static String DEFAULT_VIEWER = "de.jreality.jogl.JOGLViewer";
	//SelectionTool newSelectionTool = new SelectionTool();
	public static boolean doOwnTools = true;
	public static boolean doOwnKeyListener = true;
	public static String environment = SystemProperties.ENVIRONMENT_DEFAULT;
	public static boolean isPortal = SystemProperties.isPortal;
	public static String resourceDir = null;
	public static String saveResourceDir = null;
	public static String getMemoryUsage() {
		Runtime r = Runtime.getRuntime();
		int block = 1024;
		return "Memory usage: " + ((r.totalMemory() / block) - (r.freeMemory() / block)) + " kB";
	}
	public  static SceneGraphPath createDefaultCameraPath(final SceneGraphComponent sceneRoot)	{
		return createDefaultCameraPath(sceneRoot, "");
	}
	public  static SceneGraphPath createDefaultCameraPath(final SceneGraphComponent sceneRoot, String stem)	{
			SceneGraphComponent avatarNode = new SceneGraphComponent("avatar"+stem);
			avatarNode.setTransformation(new Transformation());
			//sceneRoot.addChild(sgc);
			sceneRoot.addChild(avatarNode);
			SceneGraphComponent cameraNode = new SceneGraphComponent("camera"+stem);
			cameraNode.setTransformation(new Transformation());
			Camera c = new Camera();
			avatarNode.addChild(cameraNode);
			cameraNode.setCamera(c);
			SceneGraphPath p = new SceneGraphPath(); // (SceneGraphPath) l.get(0);
			p.push(sceneRoot);
			p.push(avatarNode);
			p.push(cameraNode);
			p.push(c);
	//		if (environment.equals("desktop")) {
				FlyTool ft = new FlyTool();
				ft.setGain(.1);
				if (environment.equals("portal-remote")) 			
					avatarNode.addTool(ft);	
				else cameraNode.addTool(ft);
	
	//		} else 
			if (environment.equals("portal-remote")) {
				c.setNear(.1);
				c.setFar(50.0);
				c.setOnAxis(false);
				c.setStereo(true);
				c.setEyeSeparation(PortalCoordinateSystem.convertMeters(PortalCoordinateSystem.getEyeSeparationMeters()));		// based on eye separation of 7 cm = .07 meters
	
	//			ft.setGain(.1*PortalCoordinateSystem.portalScale);
				RemotePortalHeadMoveTool rphmt = new RemotePortalHeadMoveTool();
				Object foo = (Object) (sceneRoot.getAppearance().getAttribute("metric"));
				int metric = 0;
				if (foo != null && foo instanceof Integer) metric = ((Integer) foo).intValue();
	//			if (metric != Pn.EUCLIDEAN) 
	//				rphmt.setHeadTracked(false);
				cameraNode.addTool(rphmt);
	//			avatarNode.addTool(new PointerDisplayTool());
			}
	
			// TODO
			//SimpleScaleTool sc =  new SimpleScaleTool();
			//scalerNode.addTool(sc);
			return p;
	
			
		}
	public static SceneGraphComponent makeLightsS()	{
		SceneGraphComponent lightNode = new SceneGraphComponent();
		lightNode.setName("lights");
		SceneGraphComponent l0 = SceneGraphUtility.createFullSceneGraphComponent("light0");
		DirectionalLight dl = new DirectionalLight();
		dl.setColor(new Color(250, 250, 200));
		dl.setIntensity(.5);
		double[] zaxis = {0,0,1};
		double[] other = {0,1,1};
		l0.getTransformation().setMatrix( P3.makeRotationMatrix(null, zaxis, other));
	
		l0.setLight(dl);
		lightNode.addChild(l0);
				
		dl = new DirectionalLight();
		dl.setColor(new Color(250, 200, 250));
		dl.setIntensity(.5);
		l0 = SceneGraphUtility.createFullSceneGraphComponent("light1");
		double[] other2 = {-.6,-.2,1};
		l0.getTransformation().setMatrix( P3.makeRotationMatrix(null, zaxis, other2));
		l0.setLight(dl);
		lightNode.addChild(l0);
		
		
		return lightNode;
	}

	public static void addBackPlane(Viewer v)	{
		Color[] corners = { new Color(.5f,.5f,1f), new Color(.5f,.5f,.5f),new Color(1f,.5f,.5f),new Color(.5f,1f,.5f) };
		addBackPlane(v, corners);
	}
	public static void addBackPlane(Viewer v, Color[] corners)	{
		SceneGraphComponent sceneRoot = v.getSceneRoot();
		if (sceneRoot.getAppearance() == null)	sceneRoot.setAppearance(new Appearance());
		sceneRoot.getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLORS, corners);
	}
	
	public static void removeBackPlane(Viewer v)	{
		SceneGraphComponent sceneRoot = v.getSceneRoot();
		if (sceneRoot.getAppearance() != null) sceneRoot.getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLORS, Appearance.INHERITED);
	}

}
