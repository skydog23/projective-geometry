package charlesgunn.jreality.worlds;

import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.Primitives;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;

public class DiamondDemo extends LoadableScene {

	@Override
	public SceneGraphComponent makeWorld() {
		IndexedFaceSet ifs = GeometryUtilityOverflow.plainQuadMesh(1, 1, 3, 3);
		ifs = Primitives.torus(1.5, .7, 20, 10);
		ifs = GeometryUtilityOverflow.diamondize(ifs);
		SceneGraphComponent world = new SceneGraphComponent();
		world.setGeometry(ifs);
		return world;
	}

}
