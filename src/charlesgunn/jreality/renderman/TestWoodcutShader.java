package charlesgunn.jreality.renderman;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.RMAN_SURFACE_SHADER;

import java.awt.Color;

import javax.swing.JMenuBar;

import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import de.jreality.geometry.BezierPatchMesh;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.renderman.shader.SLShader;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class TestWoodcutShader extends LoadableScene {

	SceneGraphComponent world;
	double r = .4, R = 1.0;
	public SceneGraphComponent makeWorld() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		SceneGraphComponent child = SceneGraphUtility.createFullSceneGraphComponent("child");
		Appearance ap = child.getAppearance();
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.red);
		SLShader woodcutsl = new SLShader("woodcut");
		ap.setAttribute(RMAN_SURFACE_SHADER, woodcutsl);
		child.setGeometry(Primitives.torus(R, r, 40, 40));
		MatrixBuilder.euclidean().translate(0,r,0).assignTo(child);
		world.addChild(child);
		child = SceneGraphUtility.createFullSceneGraphComponent("child");
		 ap = child.getAppearance();
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.yellow);
		ap.setAttribute(RMAN_SURFACE_SHADER, woodcutsl);
		child.setGeometry(new Sphere());
		MatrixBuilder.euclidean().translate(2.5,1,0).rotateX(Math.PI/2).assignTo(child);
		world.addChild(child);
		child = SceneGraphUtility.createFullSceneGraphComponent("child");
		ap = child.getAppearance();
		double[][][] verts = {{{0,0,0},{1,0,0}},{{0,1,0},{1,1,0}}};
		BezierPatchMesh thing = new BezierPatchMesh(1, 1, verts);
		IndexedFaceSet ifs = BezierPatchMesh.representBezierPatchMeshAsQuadMesh(thing);
		ifs.setVertexAttributes(Attribute.NORMALS, null);
		ifs.setFaceAttributes(Attribute.NORMALS, null);
	    String rmanproxy = String.format("Patch \"bilinear\" \n"+
	    		" \"P\" [0 0 0  1 0 0  0 1 0  1 1 0]\n");

		child.setGeometry(ifs);
		MatrixBuilder.euclidean().scale(20).rotateX(Math.PI/2).translate(-.5,-.5,0).assignTo(child);
		world.addChild(child);
		ap = world.getAppearance();
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.white);
		ap.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, java.awt.Color.WHITE);
		ap.setAttribute(EDGE_DRAW, false);
//		woodcutsl.addParameter("float frequency", 20.0);
//		woodcutsl.addParameter("float duty", .5);
//		woodcutsl.addParameter("float multiplier", 4);

		return world;
	}
	@Override
	public void customize(JMenuBar menuBar, PluginSceneLoader psl) {
		 Viewer viewer = psl.getViewer();
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.RMAN_GLOBAL_INCLUDE_FILE,"quality.rib"); 
		MatrixBuilder.euclidean().translate(1,2,4).rotateX(-Math.PI/6).assignTo(CameraUtility.getCameraNode(viewer));
	}

	@Override
	public boolean isEncompass() { return false; }
}
