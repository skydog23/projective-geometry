/*
 * Created on Feb 23, 2013
 *
 */
package charlesgunn.math.clifford;

import de.jreality.math.Pn;
import de.jreality.math.Rn;

public class MultiVectorP3 {

	double[] vals = new double[8];
	int metric = Pn.EUCLIDEAN;
	public final static MultiVectorP3 One = MultiVectorP3.scalar(1),
			e0 = MultiVectorP3.plane(new double[]{0,0,0,1}),
			e1 = MultiVectorP3.plane(new double[]{1,0,0,0}),
			e2 = MultiVectorP3.plane(new double[] { 0, 1, 0, 0 }),
			e3 = MultiVectorP3.plane(new double[] { 0, 0, 1, 0 }),
			e01 = MultiVectorP3.line(new double[] { 1, 0, 0, 0, 0, 0 }),
			e02 = MultiVectorP3.line(new double[] { 0, 1, 0, 0, 0, 0 }),
			e03 = MultiVectorP3.line(new double[] { 0, 0, 1, 0, 0, 0 }),
			e12 = MultiVectorP3.line(new double[] { 0, 0, 0, 1, 0, 0 }),
			e31 = MultiVectorP3.line(new double[] { 0, 0, 0, 0, 1, 0 }),
			e23 = MultiVectorP3.line(new double[] { 0, 0, 0, 0, 0, 1 }),
			E0 = MultiVectorP3.point(new double[] { 0, 0, 0, 1 }),
			E1 = MultiVectorP3.point(new double[] { 1, 0, 0, 0 }),
			E2 = MultiVectorP3.point(new double[] { 0, 1, 0, 0 }),
			E3 = MultiVectorP3.point(new double[]{0,0,1,0}),
			I = MultiVectorP3.pseudoscalar(1.0);
	public final static MultiVectorP3[] planeBasis = {e0, e1, e2, e3},
			lineBasis = {e01, e02, e03, e12, e31, e23},
			pointBasis = {E0, E1, E2, E3};
	
	public MultiVectorP3()	{
		this((double[]) null);
	}
	
	public MultiVectorP3(MultiVectorP3 m)	{
		vals = m.vals.clone();
	}
	
	public MultiVectorP3(double[] v)	{
		if (v == null || v.length != 16) vals = new double[16];
		else vals = v;
	}
	
	public double[] getVals() {
		return vals;
	}

	public void setVals(double[] vals) {
		this.vals = vals;
	}
	
	static final int[] scalarI = {0},
		planeI = {1,2,3,4},
		lineI = {5,6,7,8,9,10},
		pointI = {11,12,13,14},
		pscalarI = {15};
	static final int[][] indices = {scalarI, planeI, lineI, pointI, pscalarI};
	
	static double tolerance = 10E-8;
	
	public static boolean isScalar(MultiVectorP3 v)	{
		return isOtherwiseZero(v, scalarI);
	}
	
	public static boolean isPlane(MultiVectorP3 v)	{
		return isOtherwiseZero(v, planeI);
	}
	
	public static boolean isLine(MultiVectorP3 v)	{
		return isOtherwiseZero(v, lineI);
	}
	
	public static boolean isPoint(MultiVectorP3 v)	{
		return isOtherwiseZero(v, pointI);
	}
	
	public static boolean isPseudoscalar(MultiVectorP3 v)	{
		return isOtherwiseZero(v, pscalarI);
	}
	
	public static boolean isOtherwiseZero(MultiVectorP3 v, int[] comp)	{
		return isOtherwiseZero(v, comp, tolerance);
	}
	
	public static double[] gradeD(MultiVectorP3 v, int grade)	{
		switch(grade)	{
		case 0:
			return new double[]{v.vals[0]};
		case 1:
			return new double[]{v.vals[2], v.vals[3], v.vals[4], v.vals[1]};
		case 2:
			return new double[]{v.vals[5], v.vals[6], v.vals[7], v.vals[8], v.vals[9], v.vals[10]};
		case 3:
			return new double[]{v.vals[12], v.vals[13], v.vals[14], v.vals[11]};
		case 4:
			return new double[]{v.vals[15]};
		default:
			throw new IllegalStateException("invalid grade "+grade);
		}
	}
	
	public static double scalarFrom(MultiVectorP3 v)	{
		return gradeD(v,0)[0];
	}
	
	public static double pseudoscalarFrom(MultiVectorP3 v)	{
		return gradeD(v,4)[0];
	}
	
	public static MultiVectorP3 grade(MultiVectorP3 dst, MultiVectorP3 v, int grade)	{
		if (dst == null) dst = new MultiVectorP3();
		int[] comp = indices[grade];
		for (int count = 0, i = 0; i<16; ++i)	{
			if (count < comp.length && comp[count] == i)	{
				count++;
				dst.vals[i] = v.vals[i];
			} else 
				dst.vals[i] = 0;
		}
		return dst;
	}
	/**
	 * Check whether the entries NOT contained in the index list <i>comp</i> are all zero
	 * @param v
	 * @param ignore
	 * @return
	 */
	public static boolean isOtherwiseZero(MultiVectorP3 v, int[] ignore, double tol)	{
		for (int count = 0, i = 0; i<16; ++i)	{
			if (ignore != null && count < ignore.length && ignore[count] == i)	{
				count++;
				continue;
			}
			if (Math.abs(v.vals[i]) > tol) return false;
		}
		return true;
	}
	
	// create multivectors from a single grade information
	public static MultiVectorP3 scalar(double s)	{
		MultiVectorP3 dst = new MultiVectorP3();
		dst.vals[0] = s;
		return dst;
	}
	
	public static MultiVectorP3 plane(double[] abcd)	{
		MultiVectorP3 dst = new MultiVectorP3();
		dst.vals[1] = abcd[3];
		dst.vals[2] = abcd[0];
		dst.vals[3] = abcd[1];
		dst.vals[4] = abcd[2];
		return dst;
	}
	
	public static MultiVectorP3 line(double[] pluecker)	{
		MultiVectorP3 dst = new MultiVectorP3();
		for (int i = 0; i<6; ++i)
			dst.vals[5+i] = pluecker[i];
		return dst;
	}
	
	public static MultiVectorP3 point(double[] xyzw)	{
		MultiVectorP3 dst = new MultiVectorP3();
		dst.vals[11] = xyzw[3];
		dst.vals[12] = xyzw[0];
		dst.vals[13] = xyzw[1];
		dst.vals[14] = xyzw[2];
		return dst;
	}
	
	public static MultiVectorP3 pseudoscalar(double s)	{
		MultiVectorP3 dst = new MultiVectorP3();
		dst.vals[15] = s;
		return dst;
	}

	@Override
	public String toString() {
 		return Rn.toString(vals);
	}
	
	/*******
	 * Here are the methods associated to the associated Grassmann algebra
	 */
	public static MultiVectorP3 plus(MultiVectorP3 dst,  MultiVectorP3 sa, MultiVectorP3 sb)	{
		if (dst == null) dst = new MultiVectorP3();
		for (int i = 0; i<16; ++i)	{
			dst.vals[i] =sa.vals[i] + sb.vals[i];
		}
		return dst;
	}
	
	public static MultiVectorP3 minus(MultiVectorP3 dst,  MultiVectorP3 sa, MultiVectorP3 sb)	{
		if (dst == null) dst = new MultiVectorP3();
		for (int i = 0; i<16; ++i)	{
			dst.vals[i] =sa.vals[i] - sb.vals[i];
		}
		return dst;
	}
	
	public static MultiVectorP3 times(MultiVectorP3 dst, double s, MultiVectorP3 src)	{
		if (dst == null) dst = new MultiVectorP3();
		for (int i = 0; i<16; ++i)	{
			dst.vals[i] = s * src.vals[i];
		}
		return dst;
	}

	public  static MultiVectorP3 wedge(MultiVectorP3 dst, MultiVectorP3 sa, MultiVectorP3 sb)	{
		if (dst == null) dst = new MultiVectorP3();
		double a = sa.vals[0], 
				a0 = sa.vals[1],
				a1 = sa.vals[2],
				a2 = sa.vals[3],
				a3 = sa.vals[4],
				a01 = sa.vals[5],
				a02 = sa.vals[6],
				a03 = sa.vals[7],
				a12 = sa.vals[8],
				a31 = sa.vals[9],
				a23 = sa.vals[10],
				A0 = sa.vals[11],
				A1 = sa.vals[12],
				A2 = sa.vals[13],
				A3 = sa.vals[14],
				A = sa.vals[15],
				b = sb.vals[0], 
				b0 = sb.vals[1],
				b1 = sb.vals[2],
				b2 = sb.vals[3],
				b3 = sb.vals[4],
				b01 = sb.vals[5],
				b02 = sb.vals[6],
				b03 = sb.vals[7],
				b12 = sb.vals[8],
				b31 = sb.vals[9],
				b23 = sb.vals[10],
				B0 = sb.vals[11],
				B1 = sb.vals[12],
				B2 = sb.vals[13],
				B3 = sb.vals[14],
				B = sb.vals[15];
		dst.vals[0] = a*b;
		dst.vals[1] = a0*b + a*b0;
		dst.vals[2] = a1*b + a*b1;
		dst.vals[3] = a2*b + a*b2;
		dst.vals[4] = a3*b + a*b3;
		dst.vals[5] = a01 * b - a1 * b0 + a * b01 + a0 * b1;
		dst.vals[6] = a02 * b - a2 * b0 + a * b02 + a0 * b2;
		dst.vals[7] = a03 * b - a3 * b0 + a * b03 + a0 * b3;
		dst.vals[8] = a12 * b - a2 * b1 + a * b12 + a1 * b2;
		dst.vals[9] = a31 * b + a3 * b1 - a1 * b3 + a * b31;
		dst.vals[10] = a23 * b - a3 * b2 + a * b23 + a2 * b3;
		dst.vals[11] = A0 * b + a * B0 + a23 * b1 + a3 * b12 + a31 * b2 + a1
				* b23 + a12 * b3 + a2 * b31;
		dst.vals[12] = A1 * b - a23 * b0 - a3 * b02 + a2 * b03 + a * B1 + a03
				* b2 - a0 * b23 - a02 * b3;
		dst.vals[13] = A2 * b - a31 * b0 + a3 * b01 - a1 * b03 - a03 * b1 + a
				* B2 + a01 * b3 - a0 * b31;
		dst.vals[14] = A3 * b - a12 * b0 - a2 * b01 + a1 * b02 + a02 * b1 - a0
				* b12 - a01 * b2 + a * B3;
		dst.vals[15] = A * b + a * B - A0 * b0 + a0 * B0 + a23 * b01 + a31
				* b02 + a12 * b03 - A1 * b1 + a1 * B1 + a03 * b12 - A2 * b2
				+ a2 * B2 + a01 * b23 - A3 * b3 + a3 * B3 + a02 * b31;
		return dst;
	}
	
	public static MultiVectorP3 dual(MultiVectorP3 dst, MultiVectorP3 src)	{
		if (dst == null) dst = new MultiVectorP3();
		double[] tmp = dst.vals;
		if (dst == src)	{tmp = new double[16];}
		tmp[0] = src.vals[15];
		tmp[1] = src.vals[11];
		tmp[2] = src.vals[12];
		tmp[3] = src.vals[13];
		tmp[4] = src.vals[14];
		tmp[5] = src.vals[10];
		tmp[6] = src.vals[9];
		tmp[7] = src.vals[8];
		tmp[8] = src.vals[7];
		tmp[9] = src.vals[6];
		tmp[10] = src.vals[5];
		tmp[11] = src.vals[1];
		tmp[12] = src.vals[2];
		tmp[13] = src.vals[3];
		tmp[14] = src.vals[4];
		tmp[15] = src.vals[0];
		if (dst == src) System.arraycopy(tmp, 0, dst.vals, 0, 16);
		return dst;
	}

	public static MultiVectorP3 join(MultiVectorP3 dst, MultiVectorP3 sa, MultiVectorP3 sb)	{
		if (dst == null) dst = new MultiVectorP3();
		MultiVectorP3 dsa = dual(null, sa),
			dsb = dual(null, sb),
			wdab = wedge(null, dsa, dsb);
		return dual(dst, wdab);
	}
	
	final static double[] reverseSigns = {1,1,1,1,1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,1};
	
	public static MultiVectorP3 reverse(MultiVectorP3 dst, MultiVectorP3 src)	{
		if (dst == null) dst = new MultiVectorP3();
		for (int i = 0; i<16; ++i)	{
			dst.vals[i] = src.vals[i] * reverseSigns[i];
		}
		return dst;
	}
	


}
