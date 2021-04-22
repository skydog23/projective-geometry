/*
 * Created on Feb 23, 2013
 * Author:  Charles Gunn
 *
 */
package charlesgunn.math.clifford;

import com.sun.org.apache.xpath.internal.operations.Mult;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import junit.framework.Assert;

/**
 * This class features a the class variable <i>metric</i> which can take the three values 1,0,-1,
 * yielding elliptic, euclidean, and hyperbolic planar geometry.
 * The geometry is represented by the class {@link MultivectorP2}.
 * For non-metric operations on multivectors use the static methods in {@link MultivectorP2}.
 * For mathematical background, consult http://arxiv.org/abs/1501.06511.
 * 
 * 16.05.2016: should be updated to the state of https://github.com/embeepea/pga2d, a ClojureScript
 * project (collaborator Mark Phillips) -- the main improvement to make is to refine the
 * use of the ideal norm for ideal points -- helps give correct answers for such elements.
 * This only applies to the euclidean metric.
 * @author Charles Gunn
 *
 */
public class TwoSpace {
	private int metric = Pn.EUCLIDEAN;
	
	public TwoSpace()	{
		this(Pn.EUCLIDEAN);
	}
	
	public TwoSpace(int m)	{
		metric = m;
		System.err.println("metric is "+metric);
	}
	
	public MultivectorP2 gp3(MultivectorP2 a, MultivectorP2 b, MultivectorP2 c) {
		return gp(null, a, gp(null, b, c));
	}
	public MultivectorP2 gp4(MultivectorP2 a, MultivectorP2 b, MultivectorP2 c, MultivectorP2 d) {
		return gp(null, a, gp3(b,c,d));
	}
	/**
	 * Calculate the geometric product of two multivectors.
	 * TODO: optimize!
	 */
	public  MultivectorP2 gp(MultivectorP2 dst, MultivectorP2 sa, MultivectorP2 sb)	{
		if (dst == null) dst = new MultivectorP2();
		int s = metric;
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
		dst.vals[0] = 	a*b - A0*B0 + a1*b1 + a2*b2 - A*B*s + a0*b0*s - A1*B1*s - A2*B2*s;
		dst.vals[1] =    a0*b - A0*B + a*b0 - A*B0 + A2*b1 + a2*B1 - A1*b2 - a1*B2;
		dst.vals[2] =    a1*b - a2*B0 + a*b1 + A0*b2 - A1*B*s - A2*b0*s - A*B1*s + a0*B2*s;
		dst.vals[3] =    a2*b + a1*B0 - A0*b1 + a*b2 - A2*B*s + A1*b0*s - a0*B1*s - A*B2*s;
		dst.vals[4] =    A0*b + a*B0 - a2*b1 + a1*b2 + a0*B*s + A*b0*s + A2*B1*s - A1*B2*s;
		dst.vals[5] =    A1*b + a1*B + a2*b0 - A2*B0 + A*b1 + a*B1 - a0*b2 + A0*B2;
		dst.vals[6] =    A2*b + a2*B - a1*b0 + A1*B0 + a0*b1 - A0*B1 + A*b2 + a*B2;
		dst.vals[7] =    A*b + a*B + A0*b0 + a0*B0 + A1*b1 + a1*B1 + A2*b2 + a2*B2;
		return dst;
	}
	
	/** 
	 * The polarity on the metric quadric is represented by multiplication by the pseudoscalar.
	 * @param dst
	 * @param src
	 * @return
	 */
	public MultivectorP2 polarize(MultivectorP2 dst, MultivectorP2 src)	{
		return gp(dst, src, MultivectorP2.I);
	}
	
	/**
	 * Produce a multivector whose square is +/-1.
	 * Is only designed for blades, but doesn't check for this.
	 * Also, if it's an ideal element (square is 0), 
	 * it should implement the ideal norm, but doesn't.
	 * @param dst
	 * @param src
	 * @return
	 */
	public MultivectorP2 normalize(MultivectorP2 dst, MultivectorP2 src)	{
		if (dst == null) dst = new MultivectorP2();
		MultivectorP2 sq = gp(null, src, MultivectorP2.reverse(null, src));
		if (MultivectorP2.isScalar(sq))	{
			double scalar = sq.vals[0];
//			System.err.println("norm squared = "+scalar);
			if (scalar == 0) {
				System.err.println("Can't normalize 0-norm");
				return src;
			}
			scalar = Math.sqrt(Math.abs(scalar));
			dst = MultivectorP2.times(dst, 1.0/scalar, src);
		} else {
			System.err.println("Square of this element not a scalar");
		}
		// this appears necessary to get rotors to work properly
		// ugh always the same problems!
		if (MultivectorP2.scalarFrom(dst) < 0)
			MultivectorP2.times(dst, (double) -1, dst);
		return dst;
	}

	/**
	 * Produce a multivector which multiplies by <i>src</i> to give +/-1.
	 * Is only designed for blades, but doesn't check for this.
	 * @param dst
	 * @param src
	 * @return
	 */
	public MultivectorP2 inverse(MultivectorP2 dst, MultivectorP2 src)	{
		if (dst == null) dst = new MultivectorP2();
		MultivectorP2 sq = gp(null, src, src);
		if (MultivectorP2.isScalar(sq))	{
			double scalar = sq.vals[0];
			if (scalar == 0) {
				System.err.println("Can't invert 0-norm");
				return src;
			}
			dst = MultivectorP2.times(dst, 1.0/scalar, src);
		} else {
			System.err.println("Square of this element not a scalar");
		}
		return dst;
	}
	
	/**
	 * Used to implement isometries (where <i>bread</i> is a versor).
	 * @param bread
	 * @param meat
	 * @return
	 */
	public MultivectorP2 sandwichProduct(MultivectorP2 bread, MultivectorP2 meat)	{
		return gp(null, bread, gp(null, meat, MultivectorP2.reverse(null, bread)));
	}
	
	static int[] oww = {2,0,1};
	public double[] matrixForRotor(MultivectorP2 rotor)	{
		double[] mat = new double[9];
		for (int i = 0; i < 3; ++i) {
			MultivectorP2 e = MultivectorP2.pointBasis[i];
			MultivectorP2 f = sandwichProduct(rotor, e);
			double[] fd = MultivectorP2.gradeD(f, 2);
//			if (fd[2] < 0) 
			System.arraycopy(fd, 0, mat, 3*oww[i], 3);
		}
		mat = Rn.transpose(null, mat);
		if (mat[8] < 0) Rn.times(mat, -1, mat);

//		System.err.println("rotor matrix = "+Rn.matrixToString(mat));
		return mat;
	}
	
	/**
	 * Evaluate the exponential <i>exp(tc)</i>.  Here <i>c</i> is expected to be an even-grade blade.
	 * @param dst
	 * @param c
	 * @param t
	 * @return
	 */
	public  MultivectorP2 exp(MultivectorP2 dst, MultivectorP2 c, double t) {
		boolean scalar = MultivectorP2.isScalar(c);
		boolean point = MultivectorP2.isPoint(c);
		if (!(scalar || point)) {
			throw new IllegalStateException("Cannot exponentiate");
		}
		// don't know if we really need to be able to exponentiate a scalar, but ... here it is
		if (scalar)	
			return MultivectorP2.scalar(Math.exp(MultivectorP2.scalarFrom(c)));
		MultivectorP2 nc = normalize(null, c);
		double normSquared = MultivectorP2.scalarFrom(gp(null, nc, nc));
		if (normSquared < 0)	{
			return MultivectorP2.plus(dst, 
					MultivectorP2.scalar(Math.cos(t)), 
					MultivectorP2.times(null, Math.sin(t), nc));
		} else if (normSquared > 0) {
			return MultivectorP2.plus(dst, 
					MultivectorP2.scalar(Math.cosh(t)), 
					MultivectorP2.times(null, Math.sinh(t), nc));
		} else
			return MultivectorP2.plus(dst, 
					MultivectorP2.scalar(1), 
					MultivectorP2.times(null, t, nc));
	}
	
	/**
	 * Warning: this uses the MultivectorP2 in a slightly misleading way.
	 * A logarithm is a bivector.  But this method returns the norm and the
	 * normalized bivector separately in the multivector 
	 * (as the scalar and grade-2 parts, naturally).
	 * The reason for this is to maintain accuracy when the norm is small.
	 * Note: this method is much simpler than the corresponding 3D method,
	 * since in 2D all bivectors are simple.
	 * Note: it's not clear to me whether this handles euclidean translations
	 * correctly.
	 * @param rotor
	 * @return  
	 */
	public MultivectorP2 logarithmForRotor(MultivectorP2 rotor)	{
		MultivectorP2 axis = MultivectorP2.grade(null, rotor, 2);
		double normSquared = MultivectorP2.scalarFrom(gp(null, axis, axis));
//		if (Math.abs(normSquared) != 1 && Math.abs(normSquared) != 0) {
//			throw new IllegalStateException("not normed rotor");
//		}
		// Calculate the measure of the rotor (distance or angle)
		double a = 0;
		if (normSquared > 0)	{	// hyperbolic
			a = Pn.acosh(MultivectorP2.scalarFrom(rotor));
		} else if (normSquared == 0)	{
			a = 0;
		} else {
			a = Math.acos(MultivectorP2.scalarFrom(rotor));
		}
		// extract normalized axis
		MultivectorP2 ret = MultivectorP2.grade(null, rotor, 2);
		normalize(ret, ret);
//		// check
//		MultivectorP2 exp = exp(null, ret, a);
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
		TwoSpace ts = new TwoSpace(Pn.EUCLIDEAN);
		MultivectorP2 line = MultivectorP2.line(new double[]{1,2,0});
		boolean isLine = MultivectorP2.isLine(line);
		System.err.println("Is line: "+isLine);
		MultivectorP2 point = MultivectorP2.point(new double[]{2,-1,0});
		MultivectorP2 wedge = MultivectorP2.wedge(null, point, line);
		System.err.println("Wedge is zero = "+MultivectorP2.isZeroComplement(wedge, null));
		point = MultivectorP2.point(new double[]{1,0,1});
		MultivectorP2 rpoint = ts.sandwichProduct(line, point);
		ts.normalize(rpoint, rpoint);
		System.err.println("rpoint = "+Rn.toString(MultivectorP2.gradeD(rpoint, 2)));
	}

}
