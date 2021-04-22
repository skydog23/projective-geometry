/*
 * Created on Aug 10, 2009
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;

import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.jreality.texture.SimpleTextureFactory.TextureType;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.PickUtility;
import de.jreality.util.SceneGraphUtility;

public class PlanarProjectivity extends LoadableScene {

	SceneGraphComponent world, planeContain, plane, points, pickingPlane;
	double w = 8.0;
	double[][] tri = {{w,0,0,1},{0,w,0,1},{0,0,1,0},{0,0,0,1},{1,1,0,1}};
	double[] barycentricUnit;
	int[][] edges = {{0,1},{0,3},{1,3}};
	private IndexedLineSetFactory lineSetFactory;
	@Override
	public SceneGraphComponent makeWorld() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		plane = SceneGraphUtility.createFullSceneGraphComponent("plane");
		planeContain = SceneGraphUtility.createFullSceneGraphComponent("planeContain");
		world.addChild(planeContain);
		planeContain.addChild(plane);
		plane.setGeometry(Primitives.texturedQuadrilateral(
				new double[]{-w/2,-w/2,0, w/2,-w/2,0,  w/2,w/2,0,  -w/2,w/2,0}));
		
		pickingPlane = SceneGraphUtility.createFullSceneGraphComponent("pickingPlane");
		w *= 4;
		pickingPlane.setGeometry(Primitives.texturedQuadrilateral(
				new double[]{-w/2,-w/2,0, w/2,-w/2,0,  w/2,w/2,0,  -w/2,w/2,0}));
		Appearance ap = pickingPlane.getAppearance();
		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		ap.setAttribute(CommonAttributes.TRANSPARENCY, 1.0);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.FACE_DRAW, true);
		
		points = SceneGraphUtility.createFullSceneGraphComponent("points");
		world.addChild(points);
		points.addChild(pickingPlane);
		ap = points.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, Color.RED);
		ap.setAttribute(CommonAttributes.POINT_SIZE, 2.5);
		ap.setAttribute(CommonAttributes.POINT_RADIUS, 0.1);
		lineSetFactory = new IndexedLineSetFactory();
		lineSetFactory.setVertexCount(5);
		lineSetFactory.setVertexCoordinates(tri);
		lineSetFactory.setEdgeCount(edges.length);
		lineSetFactory.setEdgeIndices(edges);
		lineSetFactory.update();
		points.setGeometry(lineSetFactory.getGeometry());
		PickUtility.setPickable(points, false, false, false);
		PickUtility.setPickable(pickingPlane, false, false, true);
		MatrixBuilder.euclidean().translate(0,0,.01).assignTo(points);

		barycentricUnit = Pn.barycentricCoordinates(null, tri, tri[4]);
		
		pickingPlane.addTool(new AbstractTool(InputSlot.LEFT_BUTTON) {

			@Override
			public void activate(ToolContext tc) {
				//System.err.println("Activated");
				super.activate(tc);
				addCurrentSlot(InputSlot.POINTER_TRANSFORMATION);
			}

			@Override
			public void deactivate(ToolContext tc) {
				super.deactivate(tc);
				removeCurrentSlot(InputSlot.POINTER_TRANSFORMATION);
			}

			@Override
			public void perform(ToolContext tc) {
				//System.err.println("Perform");
				super.perform(tc);
				PickResult pick = tc.getCurrentPick();
				if (pick == null) return;
				double[] oc = pick.getObjectCoordinates();
				int which = getClosestPoint(oc);
				System.arraycopy(oc, 0, tri[which], 0, 4);
				tri[which][2] = 0.0;
				lineSetFactory.setVertexCoordinates(tri);
				lineSetFactory.update();
				if (which != 4)	{
					tri[4] = coordinatesFromBarycentric(null, tri, barycentricUnit);
				} else {
					Pn.barycentricCoordinates(barycentricUnit, tri, tri[4]);
				}
				double[] proj = projectivityFromTriAndUnit(null, tri, tri[4]);
				new Matrix(proj).assignTo(planeContain);
			}

			private int getClosestPoint(double[] oc) {
				double[] distances = new double[tri.length];
				int min = 0;
				for (int i = 0; i<tri.length; ++i)	{
					distances[i] = Pn.distanceBetween(oc, tri[i], Pn.EUCLIDEAN);
					if (distances[i] < distances[min]) min = i;
				}
				return min;
			}
			
		});
		ap = plane.getAppearance();
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, Color.white);
		SimpleTextureFactory stf = new SimpleTextureFactory();
		stf.setType(TextureType.CHECKERBOARD);
		stf.update();
		Texture2D tex2d = TextureUtility.createTexture(ap, "polygonShader", stf.getImageData());
		Matrix tm = new Matrix();
		tex2d.setTextureMatrix(tm);
		MatrixBuilder.euclidean().scale(4).assignTo(tm);
		double[] proj = projectivityFromTriAndUnit(null, tri, tri[4]);
		new Matrix(proj).assignTo(planeContain);
		return world;
	}
	
	public static double[] coordinatesFromBarycentric(double[] dst,
			double[][] tri, double[] unit) {
		if (dst == null) dst = new double[unit.length];
		
		for (int i = 0; i<unit.length; ++i)	{
			Rn.add(dst, dst, Rn.times(null, unit[i], tri[i]));
		}
		return dst;
	}

	public static double[] projectivityFromTriAndUnit(double[] dst, double[][] tri, double[] unit)	{
		int n = tri[0].length;
		double[] mat = new double[n*n];
//		for (int i = 0; i<n; ++i)	System.arraycopy(tri[i], 0, mat, n*i, n);
//		double[] imat = Rn.inverse(null, Rn.transpose(null, mat));
//		double[] weights = Rn.matrixTimesVector(null, imat, unit);
		double[] weights = Pn.barycentricCoordinates(null, tri, unit);
		for (int i = 0; i<n; ++i)	System.arraycopy(Rn.times(null, weights[i], tri[i]), 0, mat, n*i, n);
		return Rn.transpose(dst, mat);
	}

}
