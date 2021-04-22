package charlesgunn.jreality.test;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import charlesgunn.jreality.geometry.TermesSphere;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.worlds.DebugLattice;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class TestTermes extends LoadableScene {

	private TermesSphere termes;
	private static Color[] colors = {Color.red, Color.green, Color.blue, Color.orange, Color.yellow, new Color(255,0,255)};

	@Override
	public SceneGraphComponent makeWorld() {
		DebugLattice dl = new DebugLattice();
		SceneGraphComponent sgc = dl.makeWorld();
		SceneGraphComponent frontSGC = new SceneGraphComponent();
		MatrixBuilder.euclidean().translate(0,0,-10).scale(5).assignTo(frontSGC);
		frontSGC.setGeometry(Primitives.regularPolygon(20));
		for (int i = 0; i< 6; ++i)	{
			SceneGraphComponent child = SceneGraphUtility.createFullSceneGraphComponent("termes face "+i);
			Transformation tform = new Transformation(de.jreality.jogl.JOGLViewer.cubeMapMatrices[i].getArray());
			child.setTransformation(tform);
			child.addChild(frontSGC);
			child.getAppearance().setAttribute("diffuseColor", colors[i]);
			sgc.addChild(child);
		}
		return sgc;
	}

	@Override
	public SceneGraphComponent makeLights() {
		return new SceneGraphComponent();
	}

	@Override
	public void customize(JMenuBar menuBar, final Viewer viewer) {
		termes = new TermesSphere(viewer);
		viewer.getSceneRoot().addChild(termes.getSceneGraphComponent());
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", new Color(200, 200, 200));
		DirectionalLight light = new DirectionalLight();
		light.setIntensity(0.8);
		SceneGraphComponent lightNode1, lightNode2, lightNode3, lightNode4, lights;
		lightNode1 = new SceneGraphComponent("1");
		lightNode2 = new SceneGraphComponent("2");
		lightNode3 = new SceneGraphComponent("3");
		lightNode4 = new SceneGraphComponent("4");
		lights = new SceneGraphComponent("lights");
		lightNode1.setLight(light);
		MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{-1,1,-1}).assignTo(lightNode1);
		lights.addChild(lightNode1);

		lightNode2.setLight(light);
		MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{1,-1,-1}).assignTo(lightNode2);
		lights.addChild(lightNode2);

		lightNode3.setLight(light);
		MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{1,1,1}).assignTo(lightNode3);
		lights.addChild(lightNode3);

		lightNode4.setLight(light);
		MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{-1,-1,1}).assignTo(lightNode3);
		lights.addChild(lightNode4);
		CameraUtility.getCameraNode(viewer).addChild(lights);
//		termes.setVisible(true);
//		CameraUtility.encompass(viewer);
		Component comp = ((Component) viewer.getViewingComponent());
//		comp.addKeyListener(termes.getKeyListener());
  
	}

	public boolean hasInspector() {return true;}
	
	@Override
	public Component getInspector(Viewer v) {
		JPanel mypanel = new JPanel();
		mypanel.setName("ReadMe");
		JTextArea textarea = new JTextArea(10,20);
		textarea.setEditable(false);
		textarea.append("This demo shows how to generate a\n"+
				"so-called Termes sphere for a given scene. \n"+
				"\n"+
				"The demo scene is a 3D lattice.\n"+
				"Type '2' to generate Termes sphere,\n"+
				"then '1' to display it.\n" +
				"\nThe labels have been added to\n"+
				"show how the sphere is composed of\n"+
				"six different views.\n\n"+
				"Typing '1' again toggles back to\n" +
				"standard view.  You can use arrow keys\n"+
				"(use shift modifier, too) to change view\n" +
				"of the lattice.\n" +
				"Typing '2' again creates Termes sphere\n"+
				"from this point of view, '1' shows it, etc\n\n"+
				"Typing '3' flips orientation of the sphere.\n\n"+
				"Shift-cntl-f:  toggles fullscreen mode.\n"+
				"Scroll wheel zooms in and out\n"+
				"\nClick on 'Scene Graph' tab to explore structure\n"+
				"\nAuthor: Charles Gunn\n"+
				"    gunn at math.tu-berlin.de\n");
		mypanel.add(textarea);
		return mypanel;
	}



}
