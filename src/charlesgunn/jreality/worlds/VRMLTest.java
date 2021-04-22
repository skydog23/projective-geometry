package charlesgunn.jreality.worlds;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;

public class VRMLTest extends LoadableScene {

	@Override
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent();
		SceneGraphComponent sgc1 = SceneGraphUtility.createFullSceneGraphComponent();
		SceneGraphComponent sgc2 = SceneGraphUtility.createFullSceneGraphComponent();
		world.addChildren(sgc1, sgc2);
		sgc1.setGeometry(Primitives.regularPolygon(4));
		sgc2.setGeometry(sgc1.getGeometry());
		sgc1.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.red);
		sgc2.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.blue);
		MatrixBuilder.euclidean().translate(0,0,1).assignTo(sgc2);
		MatrixBuilder.euclidean().rotateX(Math.PI/2).assignTo(world);
		world.getAppearance().setAttribute(EDGE_DRAW, false);
		world.getAppearance().setAttribute(VERTEX_DRAW, false);
		return world;
	}
	
	@Override
	public boolean isEncompass() { return true; }

}
