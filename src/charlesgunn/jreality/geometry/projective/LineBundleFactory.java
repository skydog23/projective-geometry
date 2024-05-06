/*
 * Created on 19 Apr 2024
 *
 */
package charlesgunn.jreality.geometry.projective;

import java.awt.Color;

import charlesgunn.anim.util.AnimationUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.util.SceneGraphUtility;

public class LineBundleFactory {
	
	double[][] points;
	SceneGraphComponent bundleSGC = SceneGraphUtility.createFullSceneGraphComponent("bundle");
	IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
//	Color c1 = new Color(0,0,0), c2 = new Color(100,100,100);
	Color c1 = new Color(235, 143, 96), c2 = new Color(255, 167, 112);
	boolean doSpread = false;
	
	double r1 = .1, r2 = 1.0;
	
	public LineBundleFactory(int res) {
		super();
		points = SphereUtility.tessellatedIcosahedronSphere(res).getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		ilsf = new IndexedLineSetFactory();
		bundleSGC.setGeometry(ilsf.getGeometry());
		update();
	}
	
	public void update() {
		double vpts[][] = new double[2*points.length][];
		int ind[][] = new int[points.length][2];
		Color ca[] = new Color[points.length];
		for (int i = 0; i< points.length; ++i)	{
			vpts[2*i] = Rn.times(null, r1, points[i]);
			vpts[2*i+1] = Rn.times(null, r2, points[i]);
			ind[i] = new int[] {2*i,2*i+1};
			ca[i] = AnimationUtility.linearInterpolation(c1, c2, Math.random());
		}
		ilsf.setVertexCount(vpts.length);
		ilsf.setVertexCoordinates(vpts);
		ilsf.setEdgeCount(ind.length);
		ilsf.setEdgeIndices(ind);
		if (doSpread) ilsf.setEdgeColors(ca);
		ilsf.update();
	}
	
	public double getR1() {
		return r1;
	}

	public void setR1(double r1) {
		this.r1 = r1;
	}

	public double getR2() {
		return r2;
	}

	public void setR2(double r2) {
		this.r2 = r2;
	}

	public SceneGraphComponent getBundleSGC() {
		return bundleSGC;
	}
	
	public void setVertices(double[][] v)	{
		points = v;
	}

	public double[][] getVertices()	{
		return points;
	}

	public static void main(String[] args) {
		LineBundleFactory lbf  = new LineBundleFactory(1);
		lbf.update();
		JRViewer.display(lbf.getBundleSGC());
	}
	
}
