/*
 * Created on Feb 23, 2013
 *
 */
package charlesgunn.math.clifford;

import de.jreality.math.Pn;
import de.jreality.math.Rn;

public class ThreeSpace {
	private int metric = Pn.EUCLIDEAN;
	
	
	public ThreeSpace()	{
		this(Pn.EUCLIDEAN);
	}
	
	public ThreeSpace(int m)	{
		metric = m;
		System.err.println("metric is "+metric);
	}
	
	/**
	 * for non-metric operations see static methods in {@link MultiVectorP3}.
	 * TODO: optimize!
	 */
	public  MultiVectorP3 gp(MultiVectorP3 dst, MultiVectorP3 sa, MultiVectorP3 sb)	{
		if (dst == null) dst = new MultiVectorP3();
		int s = metric;
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
		dst.vals[0] = 	a*b - A0*B0 + a1*b1 - a12*b12 + a2*b2 - a23*b23 + a3*b3 - a31*b31 + 
	    A*B*s + a0*b0*s - a01*b01*s - a02*b02*s - a03*b03*s - A1*B1*s - 
	    A2*B2*s - A3*B3*s;
		dst.vals[1] = 	a0*b + A0*B + a*b0 - A*B0 - a1*b01 - a2*b02 - 
	    a3*b03 + a01*b1 + a23*B1 + A3*b12 + a02*b2 + a31*B2 + A1*b23 + 
	    a03*b3 + a12*B3 + A2*b31;
		dst.vals[2] = 	a1*b - a23*B0 + a*b1 - a2*b12 + a12*b2 - 
	    A0*b23 - a31*b3 + a3*b31 + A1*B*s - a01*b0*s + a0*b01*s - A3*b02*s + 
	    A2*b03*s - A*B1*s + a03*B2*s - a02*B3*s;
		dst.vals[3] = 	a2*b - a31*B0 - a12*b1 + a1*b12 + a*b2 - a3*b23 + a23*b3 - A0*b31 + 
	    A2*B*s - a02*b0*s + A3*b01*s + a0*b02*s - A1*b03*s - a03*B1*s - 
	    A*B2*s + a01*B3*s;
		dst.vals[4] = a3*b - a12*B0 + a31*b1 - A0*b12 - a23*b2 + 
	    a2*b23 + a*b3 - a1*b31 + A3*B*s - a03*b0*s - A2*b01*s + A1*b02*s + 
	    a0*b03*s + a02*B1*s - a01*B2*s - A*B3*s;
		dst.vals[5] = a01*b - a23*B - a1*b0 + A1*B0 + a*b01 + a12*b02 - a31*b03 + a0*b1 - 
	    A0*B1 - a02*b12 - A3*b2 + a3*B2 - A*b23 + A2*b3 - a2*B3 + a03*b31;
	    dst.vals[6] = a02*b - a31*B - a2*b0 + A2*B0 - a12*b01 + a*b02 + a23*b03 + A3*b1 - 
	    a3*B1 + a01*b12 + a0*b2 - A0*B2 - a03*b23 - A1*b3 + a1*B3 - A*b31;
	    dst.vals[7] = a03*b - a12*B - a3*b0 + A3*B0 + a31*b01 - a23*b02 + a*b03 - A2*b1 + 
	    a2*B1 - A*b12 + A1*b2 - a1*B2 + a02*b23 + a0*b3 - A0*B3 - a01*b31;
	    dst.vals[8] = a12*b + a3*B0 - a2*b1 + a*b12 + a1*b2 + a31*b23 + A0*b3 - a23*b31 - 
	    a03*B*s - A3*b0*s + a02*b01*s - a01*b02*s - A*b03*s + A2*B1*s - 
	    A1*B2*s - a0*B3*s;
	    dst.vals[9] = a31*b + a2*B0 + a3*b1 + a23*b12 + A0*b2 - 
	    a12*b23 - a1*b3 + a*b31 - a02*B*s - A2*b0*s - a03*b01*s - A*b02*s + 
	    a01*b03*s - A3*B1*s - a0*B2*s + A1*B3*s;
	    dst.vals[10] = a23*b + a1*B0 + A0*b1 - a31*b12 - a3*b2 + a*b23 + a2*b3 + a12*b31 - 
	    a01*B*s - A1*b0*s - A*b01*s + a03*b02*s - a02*b03*s - a0*B1*s + 
	    A3*B2*s - A2*B3*s;
	    dst.vals[11] = A0*b + a*B0 + a23*b1 + a3*b12 + a31*b2 + a1*b23 + 
	    a12*b3 + a2*b31 + a0*B*s - A*b0*s - A1*b01*s - A2*b02*s - A3*b03*s + 
	    a01*B1*s + a02*B2*s + a03*B3*s;
	    dst.vals[12] = A1*b + a1*B - a23*b0 - a01*B0 + A0*b01 - a3*b02 + a2*b03 - A*b1 + 
	    a*B1 - A2*b12 + a03*b2 + a12*B2 - a0*b23 - a02*b3 - a31*B3 + A3*b31;
	    dst.vals[13] = A2*b + a2*B - a31*b0 - a02*B0 + a3*b01 + A0*b02 - a1*b03 - a03*b1 - 
	    a12*B1 + A1*b12 - A*b2 + a*B2 - A3*b23 + a01*b3 + a23*B3 - a0*b31;
	    dst.vals[14] = A3*b + a3*B - a12*b0 - a03*B0 - a2*b01 + a1*b02 + A0*b03 + a02*b1 + 
	    a31*B1 - a0*b12 - a01*b2 - a23*B2 + A2*b23 - A*b3 + a*B3 - A1*b31;
	    dst.vals[15] =  A*b + a*B - A0*b0 + a0*B0 + a23*b01 + a31*b02 + a12*b03 - A1*b1 + 
	    a1*B1 + a03*b12 - A2*b2 + a2*B2 + a01*b23 - A3*b3 + a3*B3 + a02*b31;
		return dst;
	}
	
	public MultiVectorP3 polarize(MultiVectorP3 dst, MultiVectorP3 src)	{
		return gp(dst, src, MultiVectorP3.I);
	}
	
	public MultiVectorP3 normalize(MultiVectorP3 dst, MultiVectorP3 src)	{
		if (dst == null) dst = new MultiVectorP3();
		MultiVectorP3 sq = gp(null, src, MultiVectorP3.reverse(null, src));
		if (MultiVectorP3.isScalar(sq))	{
			double scalar = sq.vals[0];
//			System.err.println("norm squared = "+scalar);
			if (scalar == 0) {
				System.err.println("Can't normalize 0-norm");
				return src;
			}
			scalar = Math.sqrt(Math.abs(scalar));
			dst = MultiVectorP3.times(dst, 1.0/scalar, src);
		} else {
			System.err.println("Square of this element not a scalar");
		}
		// this appears necessary to get rotors to work properly
		// ugh always the same problems!
		if (MultiVectorP3.scalarFrom(dst) < 0)
			MultiVectorP3.times(dst, (double) -1, dst);
		return dst;
	}

	public MultiVectorP3 inverse(MultiVectorP3 dst, MultiVectorP3 src)	{
		if (dst == null) dst = new MultiVectorP3();
		MultiVectorP3 sq = gp(null, src, src);
		if (MultiVectorP3.isScalar(sq))	{
			double scalar = sq.vals[0];
			if (scalar == 0) {
				System.err.println("Can't invert 0-norm");
				return src;
			}
			dst = MultiVectorP3.times(dst, 1.0/scalar, src);
		} else {
			System.err.println("Square of this element not a scalar");
		}
		return dst;
	}
	
	public MultiVectorP3 sandwichProduct(MultiVectorP3 bread, MultiVectorP3 meat)	{
		return gp(null, bread, gp(null, meat, MultiVectorP3.reverse(null, bread)));
	}
	
	static int[] oww = {3,0,1,2};
	public double[] matrixForRotor(MultiVectorP3 rotor)	{
		double[] mat = new double[16];
		for (int i = 0; i < 4; ++i) {
			MultiVectorP3 e = MultiVectorP3.pointBasis[i];
			MultiVectorP3 f = sandwichProduct(rotor, e);
			double[] fd = MultiVectorP3.gradeD(f, 3);
			System.arraycopy(fd, 0, mat, 4*oww[i], 4);
		}
		mat = Rn.transpose(null, mat);
//		System.err.println("rotor matrix = "+Rn.matrixToString(mat));
		return mat;
	}
	
	public  MultiVectorP3 exp(MultiVectorP3 dst, MultiVectorP3 c, double angle) {
		boolean scalar = MultiVectorP3.isScalar(c);
		boolean bivector = MultiVectorP3.isLine(c);
		if (!(scalar || bivector)) {
			throw new IllegalStateException("Cannot exponentiate");
		}
		if (scalar)	
			return MultiVectorP3.scalar(Math.exp(MultiVectorP3.scalarFrom(c)));
		MultiVectorP3 nc = normalize(null, c);
		double normSquared = MultiVectorP3.gradeD(gp(null, nc, nc), 0)[0];
		if (normSquared < 0)	{
			return MultiVectorP3.plus(dst, MultiVectorP3.scalar(Math.cos(angle)), MultiVectorP3.times(null, Math.sin(angle), nc));
		} else if (normSquared > 0) {
			return MultiVectorP3.plus(dst, MultiVectorP3.scalar(Math.cosh(angle)), MultiVectorP3.times(null, Math.sinh(angle), nc));
		} else
			return MultiVectorP3.plus(dst, MultiVectorP3.scalar(1), nc);
	}
	
	/**
	 * 
	 * @param rotor
	 * @return  if log is tP, scalar part is t and grade-2 is P (normalized)
	 */
	public MultiVectorP3 logarithmForRotor(MultiVectorP3 rotor)	{
		MultiVectorP3 axis = MultiVectorP3.grade(null, rotor, 2);
		double normSquared = MultiVectorP3.scalarFrom(gp(null, axis, axis));
//		if (Math.abs(normSquared) != 1 && Math.abs(normSquared) != 0) {
//			throw new IllegalStateException("not normed rotor");
//		}
		double a = 0, b = 0;
		if (normSquared > 0)	{	// hyperbolic
			a = Pn.acosh(MultiVectorP3.scalarFrom(rotor));
		} else if (normSquared == 0)	{
			a = 0;
		} else {
			a = Math.acos(MultiVectorP3.scalarFrom(rotor));
		}
		// extract normalized axis
		MultiVectorP3 ret = MultiVectorP3.grade(null, rotor, 2);
		normalize(ret, ret);
		// check
		MultiVectorP3 exp = exp(null, ret, a);
		ret.getVals()[0] = a;
		return ret;
	}
	

	public int getMetric() {
		return metric;
	}

	public void setMetric(int metric) {
		this.metric = metric;
	}

	public static void main(String[] args) {
		ThreeSpace ts = new ThreeSpace(Pn.EUCLIDEAN);
		MultiVectorP3 line = MultiVectorP3.line(new double[]{1,2,3,-1,2,0});
		boolean isLine = MultiVectorP3.isLine(line);
		System.err.println("Is line: "+isLine);
		MultiVectorP3 point = MultiVectorP3.point(new double[]{2,-1,0,1});
		MultiVectorP3 wedge = MultiVectorP3.wedge(null, point, line);
		System.err.println("Wedge is zero = "+MultiVectorP3.isOtherwiseZero(wedge, null));
		point = MultiVectorP3.point(new double[]{1,0,0,1});
		MultiVectorP3 rpoint = ts.sandwichProduct(line, point);
		ts.normalize(rpoint, rpoint);
		System.err.println("rpoint = "+Rn.toString(MultiVectorP3.gradeD(rpoint, 2)));
	}

}
