package charlesgunn.jreality.geometry.projective;

import java.util.List;

import charlesgunn.jreality.geometry.Snake;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedLineSet;

public class PointCollector {

	Snake curve;
	int length;
	double[][] points;
	int count;			// points to next position to write into
	// option: check if the curve crosses over the plane at infinity, and attempt to insert
	// some extra segments so it gets drawn correctly.
	boolean projective = true;
	int insertCount = 40;
	boolean checkInfinity = false;
	public PointCollector(int l, int f)	{
		length = l;
		points = new double[length][f];
		curve = new Snake(points);
		curve.setName("PCSnake");
//		System.err.println("PC constructor, class = "+curve.getClass().getName());
	}
	
	public boolean isCheckInfinity() {
		return checkInfinity;
	}

	public void setCheckInfinity(boolean checkInfinity) {
		this.checkInfinity = checkInfinity;
	}

	public void setDefaultValue(double[] p)	{
		for (int i = 0; i<length; ++i) points[i] = p.clone();
	}
	public void addPoint(double[] p)	{
		addPointPrivate(p);
		curve.update();
	}

	private void addPointPrivate(double[] p) {
		if (points == null) {
			points = new double[length][p.length];
			for (int i = 0; i<length; ++i) points[i][3] = 1.0;
			curve = new Snake(points);
		}
		if (p.length != points[0].length)
			throw new IllegalArgumentException("wrong length vector");
		int index = (count%length);
		int prev = ((count-1+length)%length);
		if (p.length == 4) {
			if (checkInfinity && crossLAI(points[prev], p) )	{
//			if (points[prev][3] * p[3] < 0.0)	{ // cross the line at infinity
				for (int i = 0; i<insertCount; ++i)	{
					double t = (i)/(insertCount-1.0);
					Rn.linearCombination(points[index], 1-t, points[prev], t, p);
					count++;
					index = (count%length);
					System.err.println("Inserting intermediate point");
				}
			}
		}
		System.arraycopy(p, 0, points[index], 0, p.length);
		int[] snakeinfo = curve.getInfo();
		snakeinfo[0] = (count < length) ? 0 : (count+1)%length;
		snakeinfo[1] = (count < length) ? count : length;
		count++;
	}
	
	private boolean oppWCoord(double[] prev, double[] p) {
		return prev[3] * p[3] < 0;
	}
	private boolean crossLAI(double[] prev, double[] p) {
		boolean ret = false;
		if (oppWCoord(prev, p))	{
			// make sure the other coordinates match
			if (prev[0] * p[0] >= 0 && prev[1] * p[1] >=0 & prev[2]*p[2] >= 0) ret = true;
			else {
				if (p[3] < 0) Rn.times(p, -1, p);
			}
//			System.err.println("Ret = "+ret);
//			System.err.println("prev = "+Rn.toString(prev));
//			System.err.println("p = "+Rn.toString(p));
		}
		return ret;
	}

	public void addPoints(List<double[]> points)	{
		for (double[] p : points) addPointPrivate(p);
		curve.update();
	}
	
	public IndexedLineSet getCurve() {
		return curve;
	}
	
	public int getCount() {
		return count;
	}
	
	public void reset()	{
//		System.err.println("resetting collector");
		int[] snakeinfo = curve.getInfo();
		snakeinfo[0] = snakeinfo[1] = 0;
		curve.update();
		count = 0;
	}
}
