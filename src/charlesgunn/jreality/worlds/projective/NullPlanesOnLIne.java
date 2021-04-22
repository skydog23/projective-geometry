/*
 * Created on Jun 2, 2010
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.geometry.projective.PlanePencilFactory;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

public class NullPlanesOnLIne extends Assignment {

	Color[] edgeC = {
			Color.blue,
			Color.red,
			Color.green,
			Color.green,
			Color.red,
			Color.blue,
			Color.cyan,
			Color.pink,
			Color.red,
			Color.orange,
			Color.yellow,
			Color.magenta,
			Color.gray,
			Color.black
	};
	@Override
	public SceneGraphComponent getContent() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		double[][] verts = {{0,1,0,1}, {0,-1,0,1}, {.5,-1,0,1}, {.5,1,0,1}};
		double extent = 5;
		
		PointRangeFactory prf = new PointRangeFactory();
		prf.setElement0(verts[0]);
		prf.setElement1(verts[1]);
		prf.setFiniteSphere(true);
		prf.setSphereRadius(extent);
		prf.setNumberOfSamples(50);
		prf.update();
		IndexedLineSet ils = prf.getLine();
		SceneGraphComponent line = SceneGraphUtility.createFullSceneGraphComponent("line");
		Appearance ap = line.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.black);
		ap.setAttribute("lineShader.tubeRadius", .005);
		line.setGeometry(ils);
		world.addChild(line);

		SceneGraphComponent strahl = SceneGraphUtility.createFullSceneGraphComponent("strahl");
		ap = strahl.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute("pointShader.polygonShader.diffuseColor", Color.red);
		ap.setAttribute("pointShader.pointRadius", .01);
		int numSpheres = 250;
		double[][] pts = new double[numSpheres][3];
		for (int i = 0; i < numSpheres; ++i) {
			pts[i][1] = AnimationUtility.linearInterpolation(i / (numSpheres - 1.0), 0, 1, -extent, extent);
			pts[i][0] = pts[i][2] = 0;
		}
		PointSetFactory psf = new PointSetFactory();
		psf.setVertexCount(numSpheres);
		psf.setVertexCoordinates(pts);
		psf.update();
		strahl.setGeometry(psf.getGeometry());
		world.addChild(strahl);

		SceneGraphComponent achse = SceneGraphUtility.createFullSceneGraphComponent("achse");
		ap = achse.getAppearance();
		// ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		ap.setAttribute("polygonShader.diffuseColor", Color.white);
		ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.black);
		ap.setAttribute("pointShader.polygonShader.diffuseColor", Color.black);
		ap.setAttribute("pointShader.pointRadius", .005);
		ap.setAttribute("lineShader.tubeRadius", .005);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		PlanePencilFactory ppf = new PlanePencilFactory();
		ppf.setNumberOfSamples(10);
		ppf.setElement0(verts[0]);
		ppf.setElement1(verts[1]);
		ppf.update();
		achse.addChild(ppf.getPlanePencil());

		world.addChild(achse);
		MatrixBuilder.euclidean().translate(0, 0, -4).assignTo(world);
		return world;
	}

	@Override
	public void display() {
		super.display();
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", Color.white);
	}

	public static void main(String[] args) {
		new NullPlanesOnLIne().display();
	}
}
