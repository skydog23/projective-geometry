package mathvisws12;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.util.SceneGraphUtility;

public class SpringSystem extends Assignment{

	double[][] points = {{1,1,1},{1,-1,-1},{0,1,-1},{-1,-1,1}};
	int[][] pairs = {{0,1},{2,3}};
	int[][] edge;
	int[] fixedPoints;
	IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
	public SceneGraphComponent getContent()	{
		sgc = SceneGraphUtility.createFullSceneGraphComponent("world");
		//build the initial geometry
		double[][] verts = new double[6][];
		for (int i = 0; i<4; ++i)	{
			Rn.normalize(points[i], points[i]);
			verts[i] = points[i];
		}
		verts[4] = Rn.average(null, new double[][]{points[pairs[0][0]], points[pairs[0][1]]});
		verts[5] = Rn.average(null, new double[][]{points[pairs[1][0]], points[pairs[1][1]]});
		int[][] edges = {{0,4},{1,4},{2,5},{3,5},{4,5}};
		edge = new int[6][6];
		fixedPoints = new int[6];
		fixedPoints[0] = fixedPoints[1] = fixedPoints[2] = fixedPoints[3] = 1;
		
		// calculate the edge list for the ils
		int count = 0;
		for (int i = 0; i<6; ++i)	{
			if (fixedPoints[i] == 1) continue;
			for (int j = 0;j<6; ++j) {
				for (int k = 0; k<edges.length;++k)	{
					if ((edges[k][0] == i && edges[k][1] == j) || (edges[k][1] == i && edges[k][0] == j))
						edge[i][j] = 1;
					else edge[i][j]  = 0;
				}
			}
		}
		
		ilsf.setVertexCount(verts.length);
		ilsf.setVertexCoordinates(verts);
		ilsf.setEdgeCount(edges.length);
		ilsf.setEdgeIndices(edges);
		ilsf.update();
		sgc.setGeometry(ilsf.getGeometry());
		
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		world.addChildren(sgc, Primitives.wireframeSphere());
		return world;
	}
	double k = .01, k2 = .1;
	private SceneGraphComponent sgc;
	public void update()	{
		double[][] verts = ilsf.getIndexedLineSet().getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		int n = edge.length;
		double[][] forces = new double[verts.length][];
		double max = 0.0;
		for (int i = 0; i<n; ++i)	{
			forces[i] = new double[3];
			for (int j = 0; j<n; ++j)	{
				if (edge[i][j] == 1)	{
					double[] diff = Rn.subtract(null, verts[j], verts[i]);
					Rn.add(forces[i], forces[i], diff);
					double d = Rn.euclideanNorm(diff);
					Rn.add(forces[i], forces[i], Rn.times(null, -k2/(d*d), diff));
				}
			}
		}
		for (int i = 0; i<n; ++i)	{
			Rn.add(verts[i], verts[i], Rn.times(null, .01, forces[i]));
			double d = Rn.euclideanNorm(forces[i]);
			if (d > max) max = d;
		}
		// signal convergence with change of color
		sgc.getAppearance().setAttribute("lineShader.diffuseColor",(max < 10E-6) ? Color.yellow : Color.blue);
		ilsf.setVertexCoordinates(verts);
		ilsf.update();
	}
	public static void main(String[] args) {
		SpringSystem ss = new SpringSystem();
		ss.display();
	}
	@Override
	public void display() {
		super.display();
		Timer timer = new Timer(50, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				update();
			}
		});
		timer.start();
	}
}
