/*
 * Created on Mar 22, 2009
 *
 */
package charlesgunn.jreality.test;

import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.Timer;

import charlesgunn.jreality.geometry.SpherePoints;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.util.WingedEdge;

public class TestSpherePoints extends Assignment {

	int count = 10;
	private SpherePoints sp = new SpherePoints(count);;
	private SceneGraphComponent world, pworld;
	

	@Override
	public SceneGraphComponent getContent() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		world.setGeometry(sp.getWingedEdge());
		pworld = SceneGraphUtility.createFullSceneGraphComponent("pworld");
		pworld.getAppearance().setAttribute("lineShader.diffuseColor", new Color(200, 200, 250));
		pworld.getAppearance().setAttribute("pointShader.diffuseColor", new Color(200, 200, 250));
		world.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		world.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, .4);
		world.getAppearance().setAttribute("lineShader.diffuseColor", new Color(255, 200, 200));
		world.getAppearance().setAttribute("pointShader.diffuseColor", new Color(255, 200, 200));
		Timer timer = new Timer(50, new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				sp.update();
				WingedEdge we = sp.getWingedEdge();
				world.setGeometry(we);
				pworld.setGeometry(we.polarize(1.0));
			}
			
		});
		timer.start();
		SceneGraphComponent cont = new SceneGraphComponent("container");
		cont.setAppearance(new Appearance());
		Appearance ap = cont.getAppearance();
		cont.addChildren(pworld,world);
		ap.setAttribute(VERTEX_DRAW, true);
		return cont;
	}

	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute("backgroundColor", new Color(0,20,40));
	}

	@Override
	public Component getInspector() {
		Component container = sp.getInspector();
		JPanel panel = new JPanel();
		panel.setName("Parameters");
		panel.add(container);
		panel.add(Box.createVerticalGlue());
		return panel;
	}

	public static void main(String[] args) {
		new TestSpherePoints().display();
	}
}
