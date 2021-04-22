/*
 * Created on 28.01.2017
 *
 */
package charlesgunn.math;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jtem.projgeom.PlueckerLineGeometry;

public class Utility {

	public static double[] promote(double[] p4, double[] p3)	{
		if (p4 == null) p4 = new double[4];
		p4[0] = p3[0]; p4[1] = p3[1];  p4[2] = 0.0; p4[3] = p3[2];
		return p4;
	}
	public static double[] demote(double[] p3, double[] p4)	{
		if (p3 == null) p3 = new double[3];
		p3[0] = p4[0]; p3[1] = p4[1];  p3[2] = p4[3];
		return p3;
	}
	public static double[][] promote(double[][] p4, double[][] p3)	{
		if (p4 == null) p4 = new double[p3.length][4];
		for (int i = 0; i<p3.length; ++i)	{
			p4[i] = promote(p4[i], p3[i]);
		}
		return p4;
	}
	public static double[][] demote(double[][] p3, double[][] p4)	{
		if (p3 == null) p3 = new double[p4.length][3];
		for (int i = 0; i<p4.length; ++i)	{
			p3[i] = demote(p3[i], p4[i]);
		}
		return p3;
	}

	 public static double[] pointWithCoordinate(double[] dst, double[] q0, double[] q1, double v, double[] pi) {
		if (dst == null) dst = new double[4];
		// find the "point at infinity" on the line between ds and ds2
		double[] line = PlueckerLineGeometry.lineFromPoints(null, q0, q1);
		double[] ds3 = PlueckerLineGeometry.lineIntersectPlane(null, line, pi);
		return pointWithCoordinates(dst, q0, q1, ds3, v);
	}
	
	/**
     * given the three points q0, q1, and qinf find the point with coordinate v
	 * assuming the coordinates q0->(0,1), q1->(1,1), v->(1,0)
    */
	public static double[] pointWithCoordinates(double[] dst, double[] q0, double[] q1, double[] qinf, double v) {
		// choose two coordinates, where the most action is, to focus on
		int[] indices = bestCoords(q0, q1);
		double d01 = det(q0, q1, indices),
				di1 = det(qinf, q1, indices),
				k = v*d01/di1;
		dst = Rn.subtract(null, q0, Rn.times(null,  k, qinf));
		dehomogenizePreserveWSign(dst, dst); 
//		System.err.println("input points:"+Rn.toString(new double[][]{q0,q1,qinf}));
//		System.err.println("using coords:"+i0+":"+i1);
//		System.err.println("output point:"+Rn.toString(dst));
		return dst;
	}

	private static double det(double[] q0, double[] q1, int[] ii) {
		return q0[ii[0]]*q1[ii[1]] - q0[ii[1]]*q1[ii[0]];
	}
//	public static double[] pointWithCoordinates(double[] dst, double[] q0, double[] q1, double[] qinf, double v) {
//		double[] p0 = Pn.dehomogenize(null, q0);
//		double[] p1 = Pn.dehomogenize(null, q1);
//		double[] pinfinity = Pn.dehomogenize(null, qinf);
//		// if pinfinity is really "at infinity" then just take normal linear combo
//		if (pinfinity[3] == 0 || Rn.innerProduct(pinfinity, pinfinity, 3) > 10000)  
//			return Rn.linearCombination(null, v, q0, 1.0-v, q1);
//		double[] v0 = Rn.subtract(null, p0, pinfinity),
//				v1 = Rn.subtract(null, p1, pinfinity);
//		// find r such that v1 = r v0
//		double r = ratio(v0,v1);
//		
//		dst = Rn.add(dst, 
//				Rn.times(null, r, p0),
//				Rn.times(null, v * (1-r), pinfinity));
////		Pn.dehomogenize(dst, dst);
////		System.err.println("input points:"+Rn.toString(new double[][]{q0,q1,qinf}));
////		System.err.println("output point:"+Rn.toString(dst));
//		return dst;
//	}

	public static double ratio(double[] v0, double[] v1) {
		int maxi = 0; double max = -10e20;
		for (int i = 0; i<v0.length; ++i)	{
			if (Math.abs(v0[i]) > max) {
				maxi = i;
				max = Math.abs(v0[i]);
			}
		}
		return v1[maxi]/v0[maxi];
	}
	
	public static int[] bestCoords(double[] v0, double[] v1)	{
		double[] diff = Rn.subtract(null, v0, v1);
		int[] maxi = new int[2]; double max = -10e20;
		for (int i = 0; i<v0.length; ++i)	{
			if (Math.abs(v0[i]) > max) {
				maxi[0] = i;
				max = Math.abs(diff[i]);
			}
		}
		max = -10e20;
		for (int i = 0; i<v0.length; ++i)	{
			if (i == maxi[0]) continue;
			if (Math.abs(v0[i]) > max) {
				maxi[1] = i;
				max = Math.abs(diff[i]);
			}
		}

		return maxi;
	}

	public static double[][] dehomogenizePreserveWSign(double[][] vv) {
		for (int i = 0; i<vv.length; ++i)	{
			dehomogenizePreserveWSign(vv[i], vv[i]);
		}
		return vv;
	}
	
	public static double[] dehomogenizePreserveWSign(double[] dst, double[] vv)	{
		boolean negW = vv[3] < 0 ;
		return Rn.times(dst, negW ? -1 : 1, Pn.dehomogenize(dst, vv));
	}

	public static double crossRatio(double x0, double xinf, double x1, double x) {
		return (x0-x)*(xinf-x1)/((x0-x1)*(xinf-x));
	}
	public static void main(String[] args) {
		double[] p0 = {0,0,0,1},
				np0 = {0,0,0,-1},
				p1 = {1,0,0,1},
				np1 = {-1,0,0,-1},
				pi = {1,0,0,0},
				npi = {-1,0,0,0},
				pm = {.6,0,0,1},
				npm = {-.6,0,0,-1};
		double[] p = pointWithCoordinates(null, p0, p1, pi, .6);
		System.err.println("p0, p1, pi, .6 = "+Rn.toString(p));
		p = pointWithCoordinates(null, p0, np1, pi, .6);
		System.err.println("p0, np1, pi, .6 = "+Rn.toString(p));
		for (int i = 0; i<10; ++i)	{
			p = pointWithCoordinates(null, p0, np1, pm, i/10.0);
			System.err.println("p0, np1, pm, "+i/10.0+ " = "+Rn.toString(p));			
		}
	}
}

// it remains to set the sign to agree with the "euclidean"
// interpretation.  The w-coordinate plays a distinguished role here.
// Consider the w-coordinate of q0 and q1.  If they have opposite sign,
// then look at the position of dst wrt q0 and q1.  If the euclidean
// order is dst-q0-q1 or q1-q0-dst, then give dst the same w-sign as q0; if the
// order is q0-q1-dst or dst-q1-q0, then give dst the same w-sign as q1;
// the other two cases shouldn't occur for reasons I don't want to give now.
// Use the cross ratio to determine which of these cases is present
//if (q0[i0] * q1[i0] < 0) {
//	double[] line = PlueckerLineGeometry.lineFromPoints(null, q0, q1);
//	double[] farpoint = PlueckerLineGeometry.lineIntersectPlane(null, line, P3.originP3);
//	double cr0 = crossRatio(dst[i0], q1[i0], q0[i0], farpoint[i0]);
//	if (cr0 < 0)	{
//		if (dst[3] * q0[3] < 0) Rn.times(dst, -1, dst);
//	} else {
//		cr0 = crossRatio(q0[i0], dst[i0], q1[i0], farpoint[i0]);
//		if (cr0 < 0)	{
//			if (dst[3] * q1[3] < 0) Rn.times(dst, -1, dst);
//		}
//		else {
//			System.err.println("Bad case");
//			System.err.println("input points:"+Rn.toString(dehomogenizePreserveWSign(new double[][]{q0,q1,qinf})));
//			System.err.println("using coords:"+i0+":"+i1);
//			System.err.println("output point:"+Rn.toString(dst));
//		}
//	} 			
//}
	

