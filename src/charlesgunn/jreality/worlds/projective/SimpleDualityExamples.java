/*
 * Created on Mar 24, 2014
 *
 */
package charlesgunn.jreality.worlds.projective;

import static de.jreality.geometry.GeometryUtility.BOUNDING_BOX;

import java.awt.Color;

import charlesgunn.jreality.geometry.projective.DualizeSceneGraph;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.PolygonalTubeFactory;
import de.jreality.math.Pn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class SimpleDualityExamples extends Assignment {

	private SceneGraphComponent world,
	eucSGC,
		lineSGC,
		segmentSGC,
		debugSGC,
	polarSGC,
		pointSGC,
		fanSGC;
	@Override
	public SceneGraphComponent getContent() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		eucSGC = SceneGraphUtility.createFullSceneGraphComponent("euc");
		lineSGC = SceneGraphUtility.createFullSceneGraphComponent("line");
		segmentSGC = SceneGraphUtility.createFullSceneGraphComponent("segment");
		polarSGC = SceneGraphUtility.createFullSceneGraphComponent("polar");
		pointSGC = SceneGraphUtility.createFullSceneGraphComponent("point");
		fanSGC = SceneGraphUtility.createFullSceneGraphComponent("fan");
		debugSGC = SceneGraphUtility.createFullSceneGraphComponent("debug");
		world.addChildren(eucSGC);
		eucSGC.addChildren(lineSGC, segmentSGC, debugSGC);
		double[] p1 = {-1, 0, 0}, p2 = {1,0, 0};
//		PointSetFactory segment = DualizeSceneGraph.segmentFactory(p1, p2, 10, Pn.EUCLIDEAN);
//		segmentSGC.setGeometry(segment.getPointSet());
		PointRangeFactory prf = new PointRangeFactory();
		double[] p14 = {-1.01, 1,  0,1}, p24 = {.98,1,0, 1};
		prf.setElement0(p1);
		prf.setElement1(p2);
		prf.setFiniteSphere(false);
		prf.update();
		String[] labels = new String[prf.getLineFactory().getVertexCount()];
		for (int i = 0;i<labels.length; ++i)	{
			labels[i] = ""+i;
		}
		prf.getLineFactory().setVertexLabels(labels);
		prf.getLineFactory().update();
		lineSGC.setGeometry(prf.getLine());
		PolygonalTubeFactory ptf = new PolygonalTubeFactory(prf.getLine(),0);
		ptf.update();
		debugSGC.getAppearance().setAttribute(BOUNDING_BOX, Rectangle3D.unitCube);
		debugSGC.addChild(ptf.getFramesSceneGraphRepresentation(.2));
		lineSGC.getAppearance().setAttribute(BOUNDING_BOX, Rectangle3D.unitCube);
		lineSGC.getAppearance().setAttribute("pointShader.textShader.diffuseColor", Color.red);
		lineSGC.getAppearance().setAttribute("pointShader.textShader.scale", 0.01);
		lineSGC.getAppearance().setAttribute("lineShader.textShader.diffuseColor", Color.blue);
		lineSGC.getAppearance().setAttribute("lineShader.textShader.scale", 0.01);
		eucSGC.getAppearance().setAttribute("lineShader.diffuseColor", Color.cyan);
		eucSGC.getAppearance().setAttribute("pointShader.diffuseColor", Color.red);
		lineSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		segmentSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);

//		polarSGC = DualizeSceneGraph.dualize(eucSGC);
		world.getAppearance().setAttribute(BOUNDING_BOX, Rectangle3D.unitCube);
		world.addChild(polarSGC);
		polarSGC.setVisible(false);
		return world;
	}

	public static void main(String[] args) {
		new SimpleDualityExamples().display();
	}
}
