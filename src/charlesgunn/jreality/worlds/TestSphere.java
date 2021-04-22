/*
 * Created on Sep 15, 2011
 *
 */
package charlesgunn.jreality.worlds;

import java.awt.Color;

import javax.swing.JMenuBar;

import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import de.jreality.geometry.SphereUtility;
import de.jreality.scene.SceneGraphComponent;

public class TestSphere extends LoadableScene {

	@Override
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = new SceneGraphComponent();
		world.setGeometry(SphereUtility.tessellatedIcosahedronSphere(5));
		return world;
	}

	@Override
	public void customize(JMenuBar menuBar, PluginSceneLoader psl) {
		psl.getViewer().getSceneRoot().getAppearance().setAttribute("backgroundColor", Color.white);
	}

}
