/*
 * Created on 13 Jan 2024
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;

import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.util.SceneGraphUtility;

public class QuadricSurfaceFromBundles extends Assignment {

	@Override
	public SceneGraphComponent getContent() {
		SceneGraphComponent world = makeSurface(4);
		return world;
	}

	private SceneGraphComponent makeSurface(int level) {
		if (level < 0)
			level = 0;
		if (level > 4)
			level = 4;
		double[][] verts;
		
		IndexedFaceSet ico = SphereUtility.tessellatedIcosahedronSphere(level, true);
		verts = ico.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		
		Color[]	vc = new Color[verts.length];
		int numVerts = verts.length;

		System.err.println("level = "+level+" size = "+numVerts);
		SceneGraphComponent node = SceneGraphUtility.createFullSceneGraphComponent("Surface " + level);
		SceneGraphComponent curves = SceneGraphUtility.createFullSceneGraphComponent("Curves");
		SceneGraphComponent tubes =  SceneGraphUtility.createFullSceneGraphComponent("Tubes");
		tubes.getAppearance().setAttribute("lineShader.diffuseColor", Color.black);
		node.addChildren(curves, tubes);

		for (int i = 0; i < numVerts; ++i) {
			
			// find phi and then halve it
			double xy = Math.sqrt(verts[i][0] * verts[i][0] + verts[i][1] * verts[i][1]);
			double phi = Math.atan2(verts[i][2], xy);
			phi = (phi + Math.PI/2)/2;  // halve it
			double x = Math.cos(phi) * verts[i][0] / xy;
			double y = Math.cos(phi) * verts[i][1] / xy;
			double z = Math.sin(phi);
			double[] pq = new double[]{x, y, z, 0};
			double[] clifT = leftCliffordTlateFor(null, P3.originP3, pq);
			Matrix clifiso = new Matrix(clifT);

			Color c  = getColorForPoint(verts[i], phi);
			vc[i] = c;
//			if (level < 2 && showLevel[level]) 
//				System.err.println("Vertex "+i+" is "+q.toString()+"\ttheta = "+360.0*theta/(2*Math.PI)+" rgb = "+c.toString());
			
			SceneGraphComponent coreSGC = new SceneGraphComponent("curve "+i);
			coreSGC.setAppearance(new Appearance());
			coreSGC.getAppearance().setName("coreSGC"+i);
			coreSGC.addChild(urCurve);
			curves.addChild(coreSGC);
			colors.put(coreSGC.getAppearance(), c);

			SceneGraphComponent tubeSGC = new SceneGraphComponent("tube "+i);
			tubeSGC.setAppearance(new Appearance());
			tubeSGC.getAppearance().setName("tubeSGC"+colors.size());
			colors.put(tubeSGC.getAppearance(), c);
			tubeSGC.addChild(urTube[level]);
			tubes.addChild(tubeSGC);
			
			clifiso.assignTo(coreSGC);
			clifiso.assignTo(tubeSGC);
		}
		PointSetFactory psf = new PointSetFactory();
		psf.setVertexCount(verts.length);
		psf.setVertexCoordinates(verts);
		psf.setVertexColors(vc);
		psf.update();
		colorSphereKids[level].setGeometry(psf.getGeometry());

		return node;
	}

	public static void main(String[] args) {
		new QuadricSurfaceFromBundles().display();
	}

}
