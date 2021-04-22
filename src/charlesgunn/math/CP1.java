/*
 * Author	gunn
 * Created on Feb 8, 2006
 *
 */
package charlesgunn.math;

import de.jreality.math.Rn;

public class CP1 {

	public CP1() {
		super();
	}

	protected static double[] sphereToParaboloid = {
		1, 0, 0, 1,
		0, 1, 0, 0, 
		0, 0, 1, 0, 
		-1, 0, 0, 1};
	
	protected static double[] paraboloidToSphere = {
		1, 0, 0, -1,
		0, 2, 0, 0, 
		0, 0, 2, 0, 
		1, 0, 0, 1};
	
	protected static double[] zxyToxyz = {
		0,1,0,0,
		0,0,1,0,
		1,0,0,0,
		0,0,0,1};
	
	public static double[] convertPSL2CToSO31(double[] dst, Complex[] lft)	{
		if (dst == null) dst = new double[16];
		
		double ax = lft[0].re;
		double ay = lft[0].im;
		double bx = lft[1].re;
		double by = lft[1].im;
		double cx = lft[2].re;
		double cy = lft[2].im;
		double dx = lft[3].re;
		double dy = lft[3].im;
		double[] tmp = new double[16];
		tmp[0] = ax*ax + ay*ay;
		tmp[1] = 2*(ax*bx+ay*by);
		tmp[2] = 2*(ax*by-ay*bx);
		tmp[3] = bx*bx+by*by;
		tmp[4] = ax*cx+ay*cy;
		tmp[5] = bx*cx + by*cy + ax*dx + ay*dy;
		tmp[6] = by*cx - bx*cy - ay*dx + ax*dy;
		tmp[7] = bx*dx + by*dy;
		tmp[8] = ay*cx - ax*cy;
		tmp[9] = by*cx - bx*cy + ay*dx - ax*dy;
		tmp[10]= -(bx*cx) - by*cy + ax*dx + ay*dy;
		tmp[11]= by*dx - bx*dy;
		tmp[12]= cx*cx+cy*cy;
		tmp[13]= 2*(cx*dx + cy*dy);
		tmp[14]= 2*(-cy*dx + cx*dy);
		tmp[15]= dx*dx+dy*dy;
		Rn.conjugateByMatrix(dst, tmp, Rn.times(null, zxyToxyz, paraboloidToSphere));
		return dst;
	}
	
	public static Complex[] projectivity(Complex[] dst, Complex z1, Complex z2, Complex z3, Complex w1, Complex w2, Complex w3)	{
		//TODO make sure inputs are valid
		Complex[] m1 = standardProjectivity(null, z1, z2, z3);
		Complex[] m2 = standardProjectivity(null, w1, w2, w3);
		Complex[] im2 = Cn.invert(null, m2);
		dst = Cn.times(dst, im2, m1);
		return dst;
	}
	
	/**
	 * Generate the Moebius tform taking (z1,z2,z3) to (0, 1, infinity)
	 * @param m
	 * @param z1
	 * @param z2
	 * @param z3
	 * @return
	 */
	public static Complex[] standardProjectivity(Complex[] m, Complex z1, Complex z2, Complex z3)	{
			//TODO make sure inputs are valid
			if (m == null || m.length != 4) m = new Complex[4];
			for (int i = 0; i<4; ++i) if (m[i] == null) m[i] = new Complex();
			if (z1.isInfinite())	{
				m[0].setValue(0,0);
				Complex.subtract(m[1], z2, z3);
				m[2].setValue(1,0);
				Complex.negate(m[3], z3);
			} else if (z2.isInfinite())	{
				m[0].setValue(1,0);
				Complex.negate(m[1], z1);
				m[2].setValue(1,0);
				Complex.negate(m[3], z3);
			} else if (z3.isInfinite())	{
				m[0].setValue(1,0);
				Complex.negate(m[1], z1);
				m[2].setValue(0,0);
				Complex.subtract(m[3], z2, z1);
			} else {
				Complex.subtract(m[0], z2,z3);
				Complex.subtract(m[2], z2,z1);
				Complex.times(m[1], Complex.negate(null, z1), m[0]);
				Complex.times(m[3], Complex.negate(null, z3), m[2]);				
			}
			Cn.normalize(m,m);
			return m;
	}
	
	public static double[] northpole = {1,0,0,1};		// it's the x-axis!
	public static double[] r2ToUnitSphere(double[] dst, Complex src)	{
		if (src.isInfinite()) return northpole;
		return r2ToUnitSphere(dst, new double[]{src.re, src.im});
	}

	public static double[] r2ToUnitSphere3(double[] dst, Complex src)	{
		return r2ToUnitSphere3(dst, new double[]{src.re, src.im});
	}

	public static double[] r2ToUnitSphere(double[] dst, double[] ds) {
		if (dst == null) dst = new double[4];
		
		double factor = ds[0]*ds[0] + ds[1]*ds[1];
		dst[2] = factor-1;
		dst[0] = 2*ds[0];
		dst[1] = 2*ds[1];
		dst[3] = factor+1;
		return dst; //Pn.dehomogenize(dst,dst);
	}
	
	public static double[] r2ToUnitSphere3(double[] dst, double[] ds)	{
		if (dst == null) dst = new double[3];
		
		double factor = ds[0]*ds[0] + ds[1]*ds[1];
		double factor2 = 1.0/(factor+1);
		dst[2] = factor2*(factor-1);
		dst[0] = factor2*2*ds[0];
		dst[1] = factor2*2*ds[1];
		return dst;
	}

	public static double[][] r2ToUnitSphere(double[][] dst, Complex[] src) {
		if (dst == null || dst.length != src.length)	{
			dst = new double[src.length][4];
		}
		int n = src.length;
		for (int i = 0; i<n; ++i)	{
			r2ToUnitSphere(dst[i], src[i]);
		}
		return dst;
	}
	
	public static double[][] r2ToUnitSphere3(double[][] dst, Complex[] src) {
		if (dst == null || dst.length != src.length)	{
			dst = new double[src.length][3];
		}
		int n = src.length;
		for (int i = 0; i<n; ++i)	{
			r2ToUnitSphere3(dst[i], src[i]);
		}
		return dst;
	}
	
	public static double[][][] r2ToUnitSphere(double[][][] dst, Complex[][] src) {
		if (dst == null || dst.length != src.length)	{
			dst = new double[src.length][][];
		}
		int n = src.length;
		for (int i = 0; i<n; ++i)	{
			dst[i] = r2ToUnitSphere(dst[i], src[i]);
		}
		return dst;
	}
	
	public static double[][][] r2ToUnitSphere3(double[][][] dst, Complex[][] src) {
		if (dst == null || dst.length != src.length)	{
			dst = new double[src.length][][];
		}
		int n = src.length;
		for (int i = 0; i<n; ++i)	{
			dst[i] = r2ToUnitSphere3(dst[i], src[i]);
		}
		return dst;
	}
	
	public static Complex crossRatio(Complex dst, Complex z1, Complex z2, Complex z3, Complex z4)	{
		return Complex.divide(dst,
				Complex.times(null, Complex.subtract(null, z4, z1), Complex.subtract(null,z2,z3)),
				Complex.times(null, Complex.subtract(null, z4, z3), Complex.subtract(null,z2,z1)));	
	}
	/**
	 * Calculate a fourth point so cr(z1,z2,z3,z4) = x.  When (z1,z2,z3)=(0,1,Infinity), z4 = x
	 * @param dst
	 * @param z1
	 * @param z2
	 * @param z3
	 * @param xratio
	 * @return
	 */
	public static Complex completeCrossRatio(Complex dst, Complex z1, Complex z2, Complex z3, Complex xratio)	{
		if (dst == null) dst = new Complex();
		Complex[] m = standardProjectivity(null, z1, z2, z3);
		m = Cn.invert(null, m);
		CP1.times(dst, m, xratio);
		return dst;
	}

	public static Complex times(Complex dst, final Complex[] m, final Complex src)	{
		if (dst == null) dst = new Complex();
		if (src.isInfinite()) {
			return Complex.divide(null, m[0], m[2]);
		}
		final Complex z0 = new Complex(), z1 = new Complex();
		Complex.add(z0, Complex.times(z0, m[0], src), m[1]);
		Complex.add(z1, Complex.times(z1, m[2], src), m[3]);
		return Complex.divide(dst, z0, z1);
	}
}
