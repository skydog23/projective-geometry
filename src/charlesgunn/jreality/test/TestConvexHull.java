package charlesgunn.jreality.test;

import quickhull3d.QuickHull3D;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

public class TestConvexHull extends LoadableScene {

	@Override
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world)");

		int numPoints = 1000;
		double[] points = new double[numPoints*3];
		double[] tmp = new double[3];
		for (int i = 0; i<numPoints; ++i) {
			for (int j = 0; j<3; ++j) {
				tmp[j] =  Math.random()-.5;
			}
			Rn.normalize(tmp, tmp);
			System.arraycopy(tmp, 0, points, 3*i, 3);
		}
		SceneGraphComponent ps = new SceneGraphComponent();
		ps.setAppearance(new Appearance());
		ps.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		PointSetFactory psf = new PointSetFactory();
		psf.setVertexCount(numPoints);
		psf.setVertexCoordinates(points);
		psf.update();
		ps.setGeometry(psf.getGeometry());
		world.addChild(ps);
		
		QuickHull3D hull = new QuickHull3D();
		hull.build(points);
		hull.triangulate();
		int np = hull.getNumVertices();
		int nf = hull.getNumFaces();
		double[] newpoints = new double[np*3];
		hull.getVertices(newpoints);
		int[][] faces = hull.getFaces();
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(np);
		ifsf.setVertexCoordinates(newpoints);
		ifsf.setFaceCount(nf);
		ifsf.setFaceIndices(faces);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);

		ifsf.update();
		world.setGeometry(ifsf.getIndexedFaceSet());
		return world;
	}
	@Override
	public boolean isEncompass() { return true; }
}
