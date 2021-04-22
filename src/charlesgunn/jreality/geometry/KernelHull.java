/*
 * Created on May 4, 2010
 *
 */
package charlesgunn.jreality.geometry;

import java.awt.Color;
import java.util.List;

import quickhull3d.QuickHull3D;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.util.WingedEdge;
import de.jtem.discretegroup.util.WingedEdgeUtility;
import de.jtem.discretegroup.util.WingedEdge.Face;
import de.jtem.discretegroup.util.WingedEdge.Vertex;

public class KernelHull {

	WingedEdge polyhedron, polarPoly, kernel, hull;
	boolean normalize = false;
	
	public KernelHull(WingedEdge p)	{
		polyhedron = p;
		update();
	}
	
	public WingedEdge getPolyhedron() {
		return polyhedron;
	}
	public WingedEdge getPolarPoly() {
		return polarPoly;
	}
	public WingedEdge getKernel() {
		return kernel;
	}
	public WingedEdge getHull() {
		return hull;
	}
	public void update()	{
		if (normalize) {
			polyhedron.normalize(1.0);
		}
		polarPoly = polyhedron.polarize();
		// ugly way of getting a copy!
		kernel = WingedEdgeUtility.convertConvexPolyhedronToWingedEdge(polyhedron);
		// create the kernel by slicing the polyhedron with the planes of the polar
		List<Face> fl = polarPoly.faceList;
		for (Face f : fl)	{
			double[] pl = f.plane;
//			System.err.println("Plane = "+Rn.toString(pl));
			int tag = f.tag;
			kernel.cutWithPlane(pl, tag);
		}
		//kernel.update();
		// create the hull by combining all the vertices and constructing the convex hull
		List<Vertex> vo, vp;
		vo = polyhedron.vertexList;
		vp = polarPoly.vertexList;
		int no = vo.size(), np = vp.size();
		double[] pts = new double[(no+np)*3];
		for (int i = 0; i<no; ++i)	{
			double[] vv = vo.get(i).point;
			for (int j = 0; j<3; ++j)	{
				pts[3*i+j] = vv[j];
			}
		}
		for (int i = 0; i<np; ++i)	{
			double[] vv = vp.get(i).point;
			for (int j = 0; j<3; ++j)	{
				pts[3*(no+i)+j] = vv[j];
			}
		}
		QuickHull3D qhull = new QuickHull3D();
		qhull.build(pts);
		qhull.triangulate();
		np = qhull.getNumVertices();
		int nf = qhull.getNumFaces();
		double[] newpoints = new double[np*3];
		qhull.getVertices(newpoints);
		int[][] faces = qhull.getFaces();
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(np);
		ifsf.setVertexCoordinates(newpoints);
		ifsf.setFaceCount(nf);
		ifsf.setFaceIndices(faces);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		hull = WingedEdgeUtility.convertConvexPolyhedronToWingedEdge(ifsf.getIndexedFaceSet());
	}

	public static SceneGraphComponent instrument(KernelHull kh) {
		SceneGraphComponent parent = SceneGraphUtility.createFullSceneGraphComponent("parent");
		SceneGraphComponent c1 = SceneGraphUtility.createFullSceneGraphComponent("poly");
		SceneGraphComponent c2 = SceneGraphUtility.createFullSceneGraphComponent("polar");
		SceneGraphComponent c3 = SceneGraphUtility.createFullSceneGraphComponent("hull");
		SceneGraphComponent c4 = SceneGraphUtility.createFullSceneGraphComponent("kernel");
		kh.update();
		// make a copy
		WingedEdge polyhedron = WingedEdgeUtility.convertConvexPolyhedronToWingedEdge(kh.getPolyhedron());
		c1.setGeometry(polyhedron);
		c2.setGeometry(kh.getPolarPoly());
		c3.setGeometry(kh.getHull());
		c4.setGeometry(kh.getKernel());
		parent.addChildren(c1, c2, c3, c4);
		Color[] clrs = {Color.white, Color.blue, Color.red, Color.green};
		for (int i = 0; i<4; ++i)	{
			SceneGraphComponent c = parent.getChildComponent(i);
//			c.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", clrs[i]);
//			c.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor", clrs[i]);
			if (i > 1) c.setVisible(false);
		}
		return parent;
	}

	public static void setVisibility(SceneGraphComponent pp, boolean[] bools) {
		for (int i = 0; i<4; ++i)	{
			pp.getChildComponent(i).setVisible(bools[i]);
		}
	}

}
