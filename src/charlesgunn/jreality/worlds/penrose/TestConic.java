package charlesgunn.jreality.worlds.penrose;

import charlesgunn.jreality.viewer.Assignment;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;

public class TestConic extends Assignment {

	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	SceneGraphComponent cube = SceneGraphUtility.createFullSceneGraphComponent("cube");
	SceneGraphComponent prism = SceneGraphUtility.createFullSceneGraphComponent("prism");

	@Override
	public SceneGraphComponent getContent() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
