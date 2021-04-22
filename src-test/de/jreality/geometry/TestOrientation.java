package de.jreality.geometry;

import java.awt.Color;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.TwoSidePolygonShader;
import de.jreality.util.SceneGraphUtility;

public class TestOrientation extends LoadableScene {

	@Override
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent();
		DefaultGeometryShader dgs = (DefaultGeometryShader) 
			ShaderUtility.createDefaultGeometryShader(world.getAppearance(), true);
		dgs.setShowLines(false);
		dgs.setShowPoints(false);
		TwoSidePolygonShader tsps = (TwoSidePolygonShader) dgs.createPolygonShader("twoSide");
		DefaultPolygonShader dps = (DefaultPolygonShader) tsps.createFront("default");
		DefaultPolygonShader dps2 = (DefaultPolygonShader) tsps.createBack("default");
		dps.setDiffuseColor(new Color(0,204,204));
		dps2.setDiffuseColor(new Color(204,204,0));
		DefaultLineShader dls = (DefaultLineShader) dgs.createLineShader("default");
		tsps = (TwoSidePolygonShader) dls.createPolygonShader("twoSide");
		dps = (DefaultPolygonShader) tsps.createFront("default");
		dps2 = (DefaultPolygonShader) tsps.createBack("default");
		dps.setDiffuseColor(new Color(0,204,204));
		dps2.setDiffuseColor(new Color(204,204,0));
		DefaultPointShader dvs = (DefaultPointShader) dgs.createPointShader("default");
		tsps = (TwoSidePolygonShader) dvs.createPolygonShader("twoSide");
		dps = (DefaultPolygonShader) tsps.createFront("default");
		dps2 = (DefaultPolygonShader) tsps.createBack("default");
		dps.setDiffuseColor(new Color(0,204,204));
		dps2.setDiffuseColor(new Color(204,204,0));
		
		
		SceneGraphComponent sphere = new SceneGraphComponent(),
			cylinder = new SceneGraphComponent(),
			knot = new SceneGraphComponent();
		IndexedFaceSet ifs = SphereUtility.tessellatedIcosahedronSphere(3);
//		sphere.addChild(GeometryUtilityOverflow.displayFaceNormals(ifs, .1));
		sphere.setGeometry(ifs);
		cylinder.setGeometry(ifs = Primitives.cylinder(20)); // new Cylinder());
//		cylinder.addChild(GeometryUtilityOverflow.displayFaceNormals(ifs, .1));
		IndexedLineSet discreteTorusKnot = Primitives.discreteTorusKnot(.5, .2,3, 2, 20);
		PolygonalTubeFactory ptf  = new PolygonalTubeFactory(discreteTorusKnot, 0);
		ptf.setClosed(true);
		ptf.update();
		knot.setGeometry(ptf.getTube());
//		world.addChild(GeometryUtilityOverflow.displayFaceNormals(ptf.getTube(), .1));
		world.addChildren(sphere, cylinder, knot);
		MatrixBuilder.euclidean().translate(2,0,0).assignTo(cylinder);
		MatrixBuilder.euclidean().translate(4,0,0).assignTo(knot);
		return world;
	}

	@Override
	public boolean isEncompass() {
		return true;
	}

}
