/*
 * Created on Jan 17, 2011
 *
 */
package charlesgunn.jreality.test;

/*
 * Created on Dec 8, 2010
 *
 */

import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;

public class TestFan extends LoadableScene {

		@Override
		public SceneGraphComponent makeWorld() {
			SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
			double[] p0 = {0,0,1},
				p1 = {1,1,0};
			IndexedFaceSet ifs = GeometryUtilityOverflow.fanFromSegment(p0, p1, 30, .5);
			world.setGeometry(ifs);
			return world;
		}
	}
