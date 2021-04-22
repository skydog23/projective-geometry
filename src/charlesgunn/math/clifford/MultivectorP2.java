/*
 * Created on Feb 23, 2013
 * Author: Charles Gunn
 *
 */
package charlesgunn.math.clifford;

import de.jreality.math.Pn;
import de.jreality.math.Rn;

/**
 * This class models a multivector in a dual Grassmann algebra over RP(2).
 * That is, 1-vectors model lines, 2-vectors model points.
 * A multivector is then 8D (1+3+3+1).
 * The fact that it's dual isn't essential, but the names used reflect this assumption.
 * Note that to make the 3D Pluecker coordinates be compatible with the literature,
 * we set the 0th coordinate to be the homogeneous coordinate (whereas in computer
 * graphics practice, one usually uses the last coordinate for this purpose.)
 * This accounts for the strange indices which appear when converting from double[]
 * representations (following CG practice) and multivectors.
 * For example, the line ax+by+cz=0 is represented by the multivector (0,c,a,b,0,0,0,0).
 * The point (x,y,z) as (0,0,0,0,z,x,y,0)
 * The code is not optimized to handle blades. 
 * @author Charles Gunn
 *
 */
public class MultivectorP2 {
	double[] vals = new double[8];
	int metric = Pn.EUCLIDEAN;
	public final static MultivectorP2 One = MultivectorP2.scalar(1),
			e0 = MultivectorP2.line(new double[]{0,0,1}),
			e1 = MultivectorP2.line(new double[]{1,0,0}),
			e2 = MultivectorP2.line(new double[]{0,1,0}),
			E0 = MultivectorP2.point(new double[]{0,0,1}),
			E1 = MultivectorP2.point(new double[]{1,0,0}),
			E2 = MultivectorP2.point(new double[]{0,1,0}),
			I = MultivectorP2.pseudoscalar(1.0);
	public final static MultivectorP2[] lineBasis = {e0, e1, e2},
			pointBasis = {E0, E1, E2};
	
	public MultivectorP2()	{
		this((double[]) null);
	}
	
	public MultivectorP2(MultivectorP2 m)	{
		vals = m.vals.clone();
	}
	
	public MultivectorP2(double[] v)	{
		if (v == null || v.length != 8) vals = new double[8];
		else vals = v;
	}
	
	public double[] getVals() {
		return vals;
	}

	public void setVals(double[] vals) {
		this.vals = vals;
	}
	
	static final int[] scalarI = {0},
		lineI = {1,2,3},
		pointI = {4,5,6},
		pscalarI = {7};
	static final int[][] indices = {scalarI, lineI, pointI, pscalarI};
	
	static double tolerance = 10E-8;
	
	// various static boolean operators to determine whether a multivector is a k-vector
	public static boolean isScalar(MultivectorP2 v)	{
		return isZeroComplement(v, scalarI);
	}
	
	public static boolean isLine(MultivectorP2 v)	{
		return isZeroComplement(v, lineI);
	}
	
	public static boolean isPoint(MultivectorP2 v)	{
		return isZeroComplement(v, pointI);
	}
	
	public static boolean isPseudoscalar(MultivectorP2 v)	{
		return isZeroComplement(v, pscalarI);
	}
	
	public static boolean isZeroComplement(MultivectorP2 v, int[] comp)	{
		return isZeroComplement(v, comp, tolerance);
	}
	
	/**
	 * Check whether there are non-zero entries NOT contained in the index list <i>ignore</i> are all zero
	 * @param v
	 * @param ignore list of indices to ignore
	 * @param tol    allowed tolerance to zero
	 * @return
	 */
	public static boolean isZeroComplement(MultivectorP2 v, int[] ignore, double tol)	{
		for (int count = 0, i = 0; i<8; ++i)	{
			if (ignore != null && count < ignore.length && ignore[count] == i)	{
				count++;
				continue;
			}
			if (Math.abs(v.vals[i]) > tol) return false;
		}
		return true;
	}
	
	/**
	 * Strip out the grade <i>k</i> part of the multivector and return as double[]
	 * @param v
	 * @param k
	 * @return
	 */
	public static double[] gradeD(MultivectorP2 v, int k)	{
		switch(k)	{
		case 0:
			return new double[]{v.vals[0]};
		case 1:
			return new double[]{v.vals[2], v.vals[3], v.vals[1]};
		case 2:
			return new double[]{v.vals[5], v.vals[6], v.vals[4]};
		case 3:
			return new double[]{v.vals[7]};
		default:
			throw new IllegalStateException("invalid grade "+k);
		}
	}
	
	public static double scalarFrom(MultivectorP2 v)	{
		return gradeD(v,0)[0];
	}
	
	public static double pseudoscalarFrom(MultivectorP2 v)	{
		return gradeD(v,3)[0];
	}
	
	/**
	 * Fill multivector the multivector <i>dst</i> with the grade-k part of <i>v</i>.
	 * If <i>dst</i> is null, create a new instance.
	 * @param dst
	 * @param v
	 * @param k
	 * @return
	 */
	public static MultivectorP2 grade(MultivectorP2 dst, MultivectorP2 v, int k)	{
		if (dst == null) dst = new MultivectorP2();
		int[] comp = indices[k];
		for (int count = 0, i = 0; i<8; ++i)	{
			if (count < comp.length && comp[count] == i)	{
				count++;
				dst.vals[i] = v.vals[i];
			} else 
				dst.vals[i] = 0;
		}
		return dst;
	}
	// create multivectors from a single grade information

	public static MultivectorP2 scalar(double s)	{
		MultivectorP2 dst = new MultivectorP2();
		dst.vals[0] = s;
		return dst;
	}
	
	public static MultivectorP2 line(double[] abc)	{
		MultivectorP2 dst = new MultivectorP2();
		dst.vals[1] = abc[2];
		dst.vals[2] = abc[0];
		dst.vals[3] = abc[1];
		return dst;
	}
	
	public static MultivectorP2 point(double[] xyw)	{
		MultivectorP2 dst = new MultivectorP2();
		dst.vals[4] = xyw[2];
		dst.vals[5] = xyw[0];
		dst.vals[6] = xyw[1];
		return dst;
	}
	
	public static MultivectorP2 pseudoscalar(double s)	{
		MultivectorP2 dst = new MultivectorP2();
		dst.vals[7] = s;
		return dst;
	}

	@Override
	public String toString() {
 		return Rn.toString(vals);
	}
	
	/*******
	 * Here are static methods for operation in the associated Grassmann algebra
	 */
	public static MultivectorP2 plus(MultivectorP2 dst,  MultivectorP2 sa, MultivectorP2 sb)	{
		if (dst == null) dst = new MultivectorP2();
		for (int i = 0; i<8; ++i)	{
			dst.vals[i] =sa.vals[i] + sb.vals[i];
		}
		return dst;
	}
	
	public static MultivectorP2 minus(MultivectorP2 dst,  MultivectorP2 sa, MultivectorP2 sb)	{
		if (dst == null) dst = new MultivectorP2();
		for (int i = 0; i<8; ++i)	{
			dst.vals[i] =sa.vals[i] - sb.vals[i];
		}
		return dst;
	}
	
	public static MultivectorP2 times(MultivectorP2 dst, double s, MultivectorP2 src)	{
		if (dst == null) dst = new MultivectorP2();
		for (int i = 0; i<8; ++i)	{
			dst.vals[i] = s * src.vals[i];
		}
		return dst;
	}

	public  static MultivectorP2 wedge(MultivectorP2 dst, MultivectorP2 sa, MultivectorP2 sb)	{
		if (dst == null) dst = new MultivectorP2();
		double a = sa.vals[0], 
			a0 = sa.vals[1],
			a1 = sa.vals[2],
			a2 = sa.vals[3],
			A0 = sa.vals[4],
			A1 = sa.vals[5],
			A2 = sa.vals[6],
			A = sa.vals[7],
			b = sb.vals[0],
			b0 = sb.vals[1],
			b1 = sb.vals[2],
			b2 = sb.vals[3],
			B0 = sb.vals[4],
			B1 = sb.vals[5],
			B2 = sb.vals[6],
			B = sb.vals[7];
		dst.vals[0] = a*b;
		dst.vals[1] = a0*b + a*b0;
		dst.vals[2] = a1*b + a*b1;
		dst.vals[3] = a2*b + a*b2;
		dst.vals[4] = A0*b + a*B0 - a2*b1 + a1*b2;
		dst.vals[5] = A1*b + a2*b0 + a*B1 - a0*b2;
		dst.vals[6] = A2*b - a1*b0 + a0*b1 + a*B2;
		dst.vals[7] = A*b + a*B + A0*b0 + a0*B0 + A1*b1 + a1*B1 + A2*b2 + a2*B2;
		return dst;
	}
	
	/** Return the dual coordinates for a multivector.  These are how the geometric elements
	 * contained in the multivector are represented in the standard Grassmann algebra 
	 * (where points are 1-vectors, etc.).
	 * @param dst
	 * @param src
	 * @return
	 */
	public static MultivectorP2 dual(MultivectorP2 dst, MultivectorP2 src)	{
		if (dst == null) dst = new MultivectorP2();
		double[] tmp = dst.vals;
		if (dst == src)	{tmp = new double[8];}
		tmp[0] = src.vals[7];
		tmp[1] = src.vals[4];
		tmp[2] = src.vals[5];
		tmp[3] = src.vals[6];
		tmp[4] = src.vals[1];
		tmp[5] = src.vals[2];
		tmp[6] = src.vals[3];
		tmp[7] = src.vals[0];
		if (dst == src) System.arraycopy(tmp, 0, dst.vals, 0, 8);
		return dst;
	}

	/** The wedge product is the meet operator; to find the join we move the arguments to
	 * the standard algebra using dual, perform the wedge there, and return them to this algebra.
	 * @param dst
	 * @param sa
	 * @param sb
	 * @return
	 */
	public static MultivectorP2 join(MultivectorP2 dst, MultivectorP2 sa, MultivectorP2 sb)	{
		if (dst == null) dst = new MultivectorP2();
		MultivectorP2 dsa = dual(null, sa),
			dsb = dual(null, sb),
			wdab = wedge(null, dsa, dsb);
		return dual(dst, wdab);
	}
	
	final static double[] reverseSigns = {1,1,1,1,-1,-1,-1,-1};
	
	/** Reverse the order of all products; this flips the sign of grades 2 and 3.
	 * @param dst
	 * @param src
	 * @return
	 */
	public static MultivectorP2 reverse(MultivectorP2 dst, MultivectorP2 src)	{
		if (dst == null) dst = new MultivectorP2();
		for (int i = 0; i<8; ++i)	{
			dst.vals[i] = src.vals[i] * reverseSigns[i];
		}
		return dst;
	}
	


}
