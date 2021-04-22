package charlesgunn.jreality.geometry.projective;

import java.util.List;

import charlesgunn.jreality.geometry.SnakeMesh;
import de.jreality.scene.IndexedFaceSet;

public class CurveCollector {

	SnakeMesh curveMesh;
	int length;
	double[][] points;
	int count;			// points to next position to write into
	// option: check if the curve crosses over the plane at infinity, and attempt to insert
	// some extra segments so it gets drawn correctly.
	boolean projective = true;
	int insertCount = 4;
	int curveSize, fiber;
	int metric;
	public CurveCollector(int l, int c, int f)	{
		length = l;
		points = new double[length*c][f];
		curveMesh = new SnakeMesh(points, l, c, f);
		curveSize = c;
		fiber = f;
	}
	
	public void addCurves(double[][][] p)	{
		for (int i = 0; i<p.length; ++i)	{
			addCurvePrivate(p[i]);
		}
		curveMesh.update();
	}

	public void addCurve(double[][] p)	{
		addCurvePrivate(p);
		curveMesh.update();
	}

	private void addCurvePrivate(double[][] p) {
		if (p.length != curveSize)
			throw new IllegalArgumentException("wrong length curve");
		int index = (count%length);
//		int prev = ((count-1+length)%length);
//		if (p.length == 4) {
//			if (points[prev][3] * p[3] < 0.0)	{ // cross the line at infinity
//				for (int i = 0; i<insertCount; ++i)	{
//					double t = (i)/(insertCount-1.0);
//					Rn.linearCombination(points[index], 1-t, points[prev], t, p);
//					count++;
//					index = (count%length);
//					System.err.println("Inserting intermediate point");
//				}
//			}
//		}
		for (int i = 0; i<curveSize; ++i)	{
			//System.arraycopy(p, 0, points[index], 0, p.length);
			points[index*curveSize+i] = p[i].clone();
		}
		int[] snakeinfo = curveMesh.getInfo();
		snakeinfo[0] = (count < length) ? 0 : (count+1)%length;
		snakeinfo[1] = (count < length) ? count : length;
		count++;
		System.err.println("curve collector count = "+count);
	}
	
	public void addCurves(List<double[][]> curves)	{
		for (double[][] p : curves) addCurvePrivate(p);
		curveMesh.update();
	}
	
	public IndexedFaceSet getMesh() {
		return curveMesh;
	}
	
	public int getCount() {
		return count;
	}
	
	public void reset()	{
//		System.err.println("resetting collector");
		int[] snakeinfo = curveMesh.getInfo();
		snakeinfo[0] = snakeinfo[1] = 0;
		curveMesh.update();
		count = 0;
	}

	public int getMetric() {
		return metric;
	}

	public void setMetric(int metric) {
		this.metric = metric;
		curveMesh.setMetric(metric);
	}
}
