/*
 * Created on Mar 14, 2007
 *
 */
package charlesgunn.jreality.geometry.projective;

import de.jreality.math.P3;
import de.jreality.math.Pn;

abstract public class Abstract1DExtentFactory {
	
	protected int offset, 
			numSegs = 12;
	protected double[][] samples;
	protected double[] element0 = P3.originP3,
			element1 = {1,0,0,1};
	protected double[] times = null;
	static protected double dimension = 4;		// default dimension of underlying vector space
	
	public abstract void update();
	
	public int getNumberOfSamples() {
		return numSegs;
	}

	public void setNumberOfSamples(int numSegs) {
//		if ( (numSegs % 2) != 0)	{
//			//throw new IllegalArgumentException("Number of segments must be even");
//			numSegs += 1;
//		}
		this.numSegs = numSegs;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public double[] getElement0() {
		return element0;
	}

	public void setElement0(double[] point0) {
		this.element0 = point0.clone();
		if (element0.length == 3)
			element0 = Pn.homogenize(null, element0);
	}

	public double[] getElement1() {
		return element1;
	}

	public void setElement1(double[] point1) {
		this.element1 = point1.clone();
		if (element1.length == 3)
			element1 = Pn.homogenize(null, element1);
	}

	public void setVertices(double[][] v)	{
		samples = v;
	}

	public double[][] getVertices()	{
		return samples;
	}

	public double[] getValueAtTime(double t) {
	//		return LineUtility.valueAtTime(t, samples[0], samples[samples.length-1]);
			return LineUtility.valueAtTime(t, element0, element1);
		}

	public double[][] getSamples() {
		return samples;
	}

	public void setTimes(double[] tt) {
		times = tt;
	}
}
