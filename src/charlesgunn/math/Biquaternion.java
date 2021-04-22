package charlesgunn.math;

import java.util.logging.Level;
import java.util.logging.Logger;

import charlesgunn.math.p5.PlueckerLineGeometry;
import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Quaternion;
import de.jreality.math.Rn;

/**
 * @author gunn
 *
 */
public class Biquaternion {

	private static final double tolerance = 10E-8;
	private static Level loglevel = Level.FINE;
	protected static Logger logger = Logger.getLogger("charlesgunn.math");;
	Quaternion qr, qd;
	Metric metric;
	static public enum Metric {
		EUCLIDEAN(0),
		ELLIPTIC(1),
		HYPERBOLIC(-1);
		private int sig;
		Metric(int sig) {
			this.sig = sig;
		};
		
		public int getInteger()	{
			return sig;
		}
		
		public static Metric metricForCurvature(int s)	{
			switch(s)	{
				case 0: return Metric.EUCLIDEAN;
				case 1: return Metric.ELLIPTIC;
				case -1: return Metric.HYPERBOLIC;
				default: throw new IllegalArgumentException("invalid curvature "+s);
			}
		}
		
	}
	
	public Biquaternion(double[] d, Metric s)	{
		this( (d.length != 6 ? 
				new Quaternion(d[0], d[1], d[2], d[3]) : new Quaternion(0, d[0], d[1], d[2])), 
			(d.length != 6 ? 
				new Quaternion(d[4], d[5], d[6], d[7]) : new Quaternion(0, d[5], d[4], d[3])),
				s);
	}
	
	public Biquaternion(Quaternion q1, Quaternion q2, Metric s)	{
		qr = q1;
		qd = q2;
		metric = s;
	}
	
	public Biquaternion(Metric s)	{
		this (new Quaternion(), new Quaternion(), s);
	}
	
	public Biquaternion() {
		this (Metric.EUCLIDEAN);
	}
	
	public Biquaternion(double r, double d, Metric s)	{
		this (new Quaternion(r,0,0,0), new Quaternion(d,0,0,0), s);
	}

	public Biquaternion(Biquaternion src)	{
		this(new Quaternion(src.qr), new Quaternion(src.qd), src.metric);
	}
	
	public Quaternion getRealPart()	{
		return qr;
	}

	public Quaternion getDualPart()	{
		return qd;
	}

	public Metric getMetric()	{
		return metric;
	}

	public String toString()	{
		if (isBiscalar(this)) {
			return "Real = "+String.format("%8.6f\t",qr.re)+
					"Dual = "+String.format("%8.6f\t",qd.re);
		}
		return "Real = "+qr.toString()+"\n\tDual= "+qd.toString()+"\tSig="+metric;
	}
	
	public static Biquaternion copy(Biquaternion dst, Biquaternion src)	{
		if (dst == null) dst = new Biquaternion(src);
		else {
			Quaternion.copy(dst.qr, src.qr);
			Quaternion.copy(dst.qd, src.qd);
			dst.metric = src.metric;
		}
		return dst;
	}
	/**
	 * Multiply by epsilon: polarize with respect to metric quadric
	 */
	public void polarize() {
		Quaternion tmp = qd;
		qd = qr;
		if (metric == Metric.EUCLIDEAN) qr = new Quaternion(0,0,0,0);
		else if (metric == Metric.HYPERBOLIC) qr = Quaternion.negate(null, tmp);
		else qr = tmp;
	}
	
	/**
	 * Convert coordinates from point-based to plane-based (or vice versa)
	 */
	public void dualize()	{
		Quaternion tmp = qd;
		qd = qr;
		qr = tmp;
	}
	
	/**
	 * Multiply by the pseudoscalar. 
	 * @param dst
	 * @param src
	 * @return
	 */
	public static Biquaternion polarize(Biquaternion dst, Biquaternion src)	{
		if (dst == null) {
			dst = new Biquaternion(src);
			dst.polarize();
			return dst;
		}
		Quaternion tmp = src.qd;
		dst.qd = src.qr;
		if (src.metric == Metric.EUCLIDEAN) dst.qr = new Quaternion(0,0,0,0);
		else if (src.metric == Metric.HYPERBOLIC) dst.qr = Quaternion.negate(null, tmp);
		else dst.qr = tmp;
		return dst;
	}
	
	public static Biquaternion bivector(Biquaternion dst, Biquaternion src)	{
		if (dst == null) {
			dst = new Biquaternion(src);
			dst.qr.re = 0.0;
			dst.qd.re = 0.0;
		}
		else {
			dst.qr.setValue(0, src.qr.x, src.qr.y, src.qr.z);
			dst.qd.setValue(0, src.qd.x, src.qd.y, src.qd.z);
		}
		return dst;
	}
	
	public static Biquaternion biscalar(Biquaternion dst, Biquaternion src)	{
		if (dst == null) return new Biquaternion(src.qd.re, src.qd.re, src.getMetric());
		dst.qr.setValue(dst.qr.re,0,0,0);
		dst.qd.setValue(dst.qd.re,0,0,0);
		return dst;
	}

	public static Biquaternion inverse(Biquaternion dst, Biquaternion src)	{
		if (!isBiscalar(src)) 
			throw new IllegalStateException("only inverse of biscalars");
		if (dst == null) dst = new Biquaternion(src.getMetric());
		// inverse of a+eb is (a-eb)/(a*a-e*e*b*b).  Proof: multiply it out
		double re = src.getRealPart().re;
		double du = src.getDualPart().re;
		double norm = re*re - src.getMetric().getInteger()*du*du;
		if (norm == 0) 
			throw new IllegalArgumentException("norm = 0, no inverse.  Re="+re+" Du="+du);
		dst.qr.setValue(re/norm, 0, 0, 0);
		dst.qd.setValue(-du/norm, 0, 0, 0);
//		logger.log(loglevel,"x*xinverse="+times(null,dst,src));
		return dst;
	}
	
	// TODO this is double-valued: explore what effects choosing one branch makes
	public static Biquaternion sqrt(Biquaternion dst, Biquaternion src)	{
		if (!isBiscalar(src)) 
			throw new IllegalStateException("only sqrt of biscalars");
		if (dst == null) dst = new Biquaternion(src.getMetric());
		double a = src.qr.re;
		double b = src.qd.re;
		double theta, r, x=0, y=0;
		if (a == 0 && b == 0) {
			times(dst, 0, src);
			return dst;
		}
		  switch (src.getMetric()) {
			case EUCLIDEAN :
				x = Math.sqrt(a);
				y = b/(2*x);
				break;
			case ELLIPTIC:
//				if (b == 0) {
//					x = Math.sqrt(a);
//					y = 0;
//				} else  {
				if (a*a < b*b) throw new IllegalArgumentException("no sqrt exists");
					theta = Pn.atanh(b/a);
					r = Math.sqrt(a*a-b*b);
					r = Math.sqrt(r);
					x = r * Pn.cosh(theta/2);
					y = r * Pn.sinh(theta/2);					
//				}
				break;
			case HYPERBOLIC:
				theta = Math.atan2(b,a);
				r = Math.sqrt(a*a+b*b);
				r = Math.sqrt(r);
				x = r * Math.cos(theta/2);
				y = r * Math.sin(theta/2);
		  }
		dst.qr.setValue(x, 0, 0, 0);
		dst.qd.setValue(y, 0, 0, 0);
		return dst;
		
	}
	
	/**
	 * Scale <i>src</i> by a dual number so that the result has norm 1.  This also means that
	 * the inner product of the real and dual parts is 0 (since that gives the
	 * dual part of the norm, which is now 0).  For the case of bivectors, this produces a line
	 * @param dst
	 * @param src
	 * @return
	 */
	public static Biquaternion normalize(Biquaternion dst, Biquaternion src)	{
		if (dst == null) dst = new Biquaternion(src.metric);
		if (!isInvertible(src)) return times(dst, 1, src);
		Biquaternion invSqrt = biscalarForNormalization(null, src);
		times(dst, invSqrt, src);
//		System.err.println("Normalize factor = "+invSqrt.qr.re+":"+invSqrt.qd.re);
//		logger.log(loglevel,"norm="+times(null,dst,conjugate(null, dst)));
		return dst;
	}

	/**
	 * Factor <i>src</i> into a biscalar times a special normed Biquaternion.  
	 * <i>scalar</i> cannot be null. Return the scalar part in the <i>scalar</i> argument.
	 * @param normed
	 * @param scalar
	 * @param src
	 * @return
	 */
	public static Biquaternion normalize(Biquaternion normed, Biquaternion scalar, Biquaternion src) {
		Biquaternion norm = norm(null, src);
		if (Math.abs(norm.qr.re) < tolerance && Math.abs(norm.qd.re) < tolerance) {
			logger.log(loglevel,"Can't normalize"+src);
			scalar.qd.setValue(0, 0, 0, 0);
			scalar.qr.setValue(1, 0, 0, 0);
			return times(normed, 1, src);
		}
		// don't remove this!  scalar is the multiplication factor needed to get to the normalized form
		scalar = sqrt(scalar, norm);
		return normalize(normed, src);
	}

	private static Biquaternion biscalarForNormalization(Biquaternion dst, Biquaternion src) {
		if (dst == null) dst = new Biquaternion(src.metric);
		Biquaternion biscalar = norm(null, src);
		Biquaternion inverse = inverse(null, biscalar);
		dst = sqrt(dst, inverse);
		return dst;
	}
	
	/**
	 * Scale <i>src</i> by a dual number so that its norm is real, which implies it's
	 * a line.  There are two choices
	 * for this line, I'm not sure which one is calculated (they are polar with respect to
	 * metric quadric).
	 * @param dst
	 * @param src
	 * @return
	 */
	public static Biquaternion specialize(Biquaternion dst, Biquaternion src)	{
		Biquaternion biscalar = norm(null, src);
		if (Math.abs(biscalar.qd.re) < tolerance) {
			if (dst == null) dst = new Biquaternion(src);
			else times(dst, 1, src);
			return dst;
		}
		dst = normalize(dst, src);
		// the rest of the method undoes the change of norm ... 
		// TODO reorganize code so that I don't have to do that here
		double norm = norm(null,norm(null, src)).qr.re;
//		logger.log(loglevel,"Norm = "+norm);
//		if (norm <= 0) 
//			throw new IllegalArgumentException("no way jose norm has to positive");
		times(dst, Math.sqrt(Math.abs(norm)), dst);
		return dst;
	}
		
	/**
	 * This method scales the biquaternion so that the inner product of the real part with itself is d^2
	 * @param dst
	 * @param d
	 * @param src
	 * @return
	 */
	public static Biquaternion setToLength(Biquaternion dst, double d, Biquaternion src)	{
		// TODO check this method to see if it makes sense for all metric choices
		if (dst == null) dst = new Biquaternion(src.metric);
		double length = Quaternion.length(src.qr);
//		logger.log(loglevel,"Normalize length = "+length);
		if (length == 0) 
			throw new IllegalStateException("Can't normalize this biquaternion "+src.toString());
		times(dst, d/length, src);
		return dst;
	}
	
	public static double[] asDouble(double[] d, Biquaternion src)	{
		if (d == null) d = new double[8];
		double[] d1 = Quaternion.asDouble(null, src.qr);
		double[] d2 = Quaternion.asDouble(null, src.qd);
		System.arraycopy(d1, 0, d, 0, 4);
		System.arraycopy(d2, 0, d, 4, 4);
		return d;
	}
	
	public static Biquaternion norm(Biquaternion product, Biquaternion src) 	{
		product = times(product,src, conjugate(null, src));
		if (!isBiscalar(product)) 
			throw new IllegalStateException("Bad norm calculation");
		return product;
	}

	public static Biquaternion times(Biquaternion dst, double d, Biquaternion src)	{
		if (dst == null) dst = new Biquaternion(src.metric);
		Quaternion.times(dst.qr, d, src.qr);
		Quaternion.times(dst.qd, d, src.qd);
		return dst;
	}
	
	public static Biquaternion times(Biquaternion dst, Biquaternion bq1, Biquaternion bq2)	{
		if (bq1.metric != bq2.metric)	{
			throw new IllegalArgumentException("Biquaternions must have same metric");
		}
		if (dst == null) dst = new Biquaternion(bq1.metric);
		Biquaternion tmp = dst;
		if (dst == bq1 || dst == bq2)	{
			tmp = new Biquaternion(bq1.metric);
		}
		Quaternion.add(tmp.qr,
				Quaternion.times(null, bq1.qr, bq2.qr), 
				Quaternion.times(null, 
						bq1.getMetric().getInteger(), 
						Quaternion.times(null, bq1.qd, bq2.qd)));
		
		Quaternion.add(tmp.qd,
				Quaternion.times(null, bq1.qr, bq2.qd), 
				Quaternion.times(null, bq1.qd, bq2.qr));

		if (tmp != dst)	{
			copy(dst, tmp);
		}
		return dst;
	}
	/**
	 * This is the symmetric part of the product src1 * src2 = (s1*s2+s2*s1)/2
	 * @param dst
	 * @param src1
	 * @param src2
	 * @return
	 */
	public static Biquaternion innerProduct(Biquaternion dst, Biquaternion src1, Biquaternion src2)	{
		if (dst == null) dst = new Biquaternion(src1.getMetric());
		if (src1.metric != src2.metric)	{
			throw new IllegalArgumentException("Biquaternions must have same metric");
		}
		Biquaternion q1 = times(null, src1, src2);
		Biquaternion q2 = times(null, src2, src1);
		add(dst, q1, q2);
		times(dst, .5, dst);
		return dst;
		
	}
	/**
	 * This is the anti-symmetric part of the product src1 * src2  = (s1*s2-s2*s1)/2
	 * @param dst
	 * @param src1
	 * @param src2
	 * @return
	 */
	public static Biquaternion commutator(Biquaternion dst, Biquaternion src1, Biquaternion src2)	{
		if (dst == null) dst = new Biquaternion(src1.getMetric());
		if (src1.metric != src2.metric)	{
			throw new IllegalArgumentException("Biquaternions must have same metric");
		}
		Biquaternion q1 = times(null, src1, src2);
		Biquaternion q2 = times(null, src2, src1);
		add(dst, q1, times(null, -1, q2));
		times(dst, .5, dst);
		return dst;
	}
	
	public static Biquaternion lieBracket(Biquaternion dst, Biquaternion src1, Biquaternion src2)	{
		dst = commutator(dst, src1, src2);
		times(dst, 2.0, dst);
		return dst;
	}
	
	public static Biquaternion add(Biquaternion dst, Biquaternion bq1, Biquaternion bq2)	{
		if (bq1.metric != bq2.metric)	{
			throw new IllegalArgumentException("Biquaternions must have same metric");
		}
		if (dst == null) dst = new Biquaternion(bq1.metric);
		Quaternion.add(dst.qr,bq1.qr, bq2.qr);
		Quaternion.add(dst.qd,bq1.qd, bq2.qd);

		return dst;
	}
	
	public static Biquaternion subtract(Biquaternion dst, Biquaternion bq1, Biquaternion bq2)	{
		if (bq1.metric != bq2.metric)	{
			throw new IllegalArgumentException("Biquaternions must have same metric");
		}
		if (dst == null) dst = new Biquaternion(bq1.metric);
		Quaternion.subtract(dst.qr,bq1.qr, bq2.qr);
		Quaternion.subtract(dst.qd,bq1.qd, bq2.qd);

		return dst;
	}
	
	public static Biquaternion conjugateBy(Biquaternion dst, Biquaternion middle, Biquaternion actor)	{
		return times(dst, actor, times(null, middle, conjugate(null, actor)));
	}
	
	public static Biquaternion conjugate(Biquaternion dst, Biquaternion src)	{
		if (dst == null) dst = new Biquaternion(src.metric);
		if (dst.metric != src.metric)	{
			throw new IllegalArgumentException("Biquaternions must have same metric");
		}
		Quaternion.conjugate(dst.qr, src.qr);
		Quaternion.conjugate(dst.qd, src.qd);
		return dst;
	}
	
	/**
	 * negate the dual part of the biquaternion:  v0+ev1 -> v0-ev1
	 * @param dst
	 * @param src
	 * @return
	 */
	public static Biquaternion dualConjugate(Biquaternion dst, Biquaternion src)	{
		if (dst == null) {
			dst = new Biquaternion(src.qr, Quaternion.negate(null, src.qd), src.metric);
			return dst;
		}
		if (dst.metric != src.metric)	{
			throw new IllegalArgumentException("Biquaternions must have same metric");
		}
		dst.qr = new Quaternion(src.qr);
		Quaternion.negate(dst.qd, src.qd);
		return dst;
	}
	/**
	 * Warning: this expects  plucker coordinates to be wrt points of the form (w,x,y,z)
	 * NOT (x,y,z,w)!
	 * @param plucker	double[6] array containing the Plucker coordinates
	 * @param sig
	 * @return
	 */
	public static Biquaternion bivectorFromLineComplex(double[] plucker, Metric sig)	{
		return new Biquaternion(plucker, sig);
	}

	/**
	 * returns plucker coordinates for the biquaternion (better be a bivector!)
	 * @param dst
	 * @param src
	 * @return
	 */
	public static double[] lineComplexFromBivector(double[] dst, Biquaternion src) {
		if (dst == null) dst = new double[6];
//		if (!isBivector(src))
//			throw new IllegalStateException("Invalid biquaternion");
		double[] foo = asDouble(null, src);
		dst[0] = foo[1]; dst[1] = foo[2]; dst[2] = foo[3];
		dst[3] = foo[7]; dst[4] = foo[6]; dst[5] = foo[5];
		return dst;
	}

	
	public static Biquaternion axisForBivector(Biquaternion dst, Biquaternion src)	{
		if (!isBivector(src))
			throw new IllegalArgumentException("must be a bivector");
		if (isLine(src))	{
			if (dst == null) {dst = src; return dst;}
			dst.metric = src.metric;
			dst.qd = src.qd;
			dst.qr = src.qr;
			return dst;
		}
		dst = specialize(dst, src); 
		// make sure the result is a proper line
		if (dst.metric == Metric.HYPERBOLIC) {
			Biquaternion norm = Biquaternion.norm(null, dst);
			if (norm.getRealPart().re < 0) {
				polarize(dst,dst);
//				System.err.println("polarizing the axis");
			}
		}

//		logger.log(loglevel,"axis before: "+src.toString());
//		logger.log(loglevel,"axis after: "+dst.toString());
		return dst;
	}
	
	
	public static boolean isBivector(Biquaternion src)	{
		return isBivector(src, tolerance);
	}
	
	private static boolean isBivector(Biquaternion src, double d) {
		return (Math.abs(src.qr.re) < d  && Math.abs(src.qd.re) < d && !(Quaternion.length(src.qr) < d && Quaternion.length(src.qd) < d) );
	}

	public static boolean isLine(Biquaternion src)	{
		if (!isBivector(src)) return false;
		Biquaternion norm2 = norm(null, src);
		return (Math.abs(norm2.qd.re) < tolerance);
	}
	
	public static boolean isBiscalar(Biquaternion src)	{
		return isBiscalar(src, tolerance);
	}
	
	private static boolean isBiscalar(Biquaternion src, double d) {
		double[] vr = Quaternion.IJK(null, src.qr);
		double[] vd = Quaternion.IJK(null, src.qd);
		return (Rn.euclideanNorm(vr) < d  && Rn.euclideanNorm(vd) < d);
	}

	public static boolean isNormZero(Biquaternion src)	{
		Biquaternion norm = norm(null, src);
		return Math.abs(norm.qr.re) < tolerance && Math.abs(norm.qd.re) < tolerance;
	}

	public static boolean isInvertible(Biquaternion src)	{
		Biquaternion norm = norm(null, src);
		double normnorm = norm.qr.re*norm.qr.re - src.getMetric().getInteger()*norm.qd.re*norm.qd.re;
		return Math.abs(normnorm)  > (tolerance*tolerance);
	}

	private static Biquaternion kplus = new Biquaternion(.5, .5, Metric.ELLIPTIC),
			kminus = new Biquaternion(.5, -.5, Metric.ELLIPTIC);
	
	/**
	 * @param dst
	 * @param bivector
	 * @param angle
	 * @param t
	 * @return
	 */
	public static Biquaternion exp(Biquaternion dst, Biquaternion bivector, Biquaternion angle, double t) {
		if (dst == null) dst = new Biquaternion(bivector.metric);
		Metric metric = bivector.getMetric();
		if (!Biquaternion.isInvertible(bivector))	{
			if (metric == Metric.ELLIPTIC)	{
				boolean isRightTranslation = Quaternion.innerProduct(bivector.qr, bivector.qd) < 0;
				dst = Biquaternion.times(null, isRightTranslation ? kminus : kplus,
						new Biquaternion(Quaternion.exp(null, t*angle.qr.re, bivector.qr ), new Quaternion(0,0,0,0), Metric.ELLIPTIC));
			} else if (metric == Metric.EUCLIDEAN)	{
				dst = Biquaternion.times(null, t, bivector);
				dst.getRealPart().re = 1.0;
			}
		} else {
			// in general the result is (cs0 + cs1 I + (cs2 + cs3 I) axis).
			// build that result up
			double[] cossin = cossin(null, t*angle.qr.re, t*angle.qd.re, metric);
			Biquaternion scalar2 = new Biquaternion(cossin[2], cossin[3], metric);
			dst = Biquaternion.times(null, scalar2, bivector);
			dst.qr.re = cossin[0];
			dst.qd.re = cossin[1];			
		}
		return dst;
	}
	
	// returns re/du parts of cos and sin resp in a 4-vector
	public static double[] cossin(double[] ret, double theta1, double theta2, Metric metric)	{
		if (ret == null) ret = new double[4];
		double c1 = Math.cos(theta1), s1 = Math.sin(theta1), c2 = 0, s2 = 0;
		switch(metric)	{
			case EUCLIDEAN:
				c2 = 1;
				s2 = theta2;
				break;
			case ELLIPTIC:
				c2 = Math.cos(theta2);
				s2 = Math.sin(theta2);
				break;
			case HYPERBOLIC:
				c2 = Pn.cosh(theta2);
				s2 = Pn.sinh(theta2);
				break;
		}
		// dual analysis expansion of cos(theta1 + theta2 I) and sin(theta1 + theta2 I) yields 4-vector
		ret[0] = c1*c2;
		ret[1] = -s1*s2;
		ret[2] = s1*c2;
		ret[3] = c1*s2;
		return ret;             
	}

	private static final double[] finiteTetra = {1,0,0,1,  0,1,0,1, 0,0,1,1, 1,1,1,1};
	static double someConstant = 1.0/Math.sqrt(3);
	private static final double[] dualTetra = {1,0,0,1,  0,1,0,1, 0,0,1,1, someConstant,someConstant,someConstant,1};
	private static final double[] pointRefl = {-1,0,0,0, 0,-1,0,0,  0,0,-1,0, 0,0,0,1};
	/**
	 * Returns an array of two Biquaternions: <i>dst[0]</i> represents the pure reflection R; <i>dst[1]</i>, the
	 * direct isometry S such that matrix(RS) = m.
	 * @param dst
	 * @param m
	 * @param metric
	 * @return
	 */
	public static Biquaternion[] biquaternionsFromIndirectIsometry(Biquaternion[] dst, double[] m, Metric metric)	{
		if (dst == null) dst = new Biquaternion[2];
		// look first for invariant plane of the isometry i
		// it should contain the midpoints of x and i(x) for every x.
		// take four standard points: origin (0,0,0,1) and the points in the x,y,z directions
		// one unit away, e.g.,  (1,0,0,1), etc.
		// It can however happen that the four points map to a single point
		double[] imageTetra = Rn.times(null, m, finiteTetra);
		double[] sum = Rn.add(null, finiteTetra, imageTetra);
		double[][] points = new double[4][];
		Matrix sumM = new Matrix(sum);
		int got = 0;
		for (int i = 0; i<4; ++i)	{
			double[] col = sumM.getColumn(i);
			col = Pn.normalize(null, col, metric.getInteger());
			// if a point is mapped onto its own negative, the midpoint is (0,0,0,0): invalid
			// reject such points
			if (Rn.euclideanNormSquared(col) < 10E-8) continue;
			boolean already = false;
			// add the point to the list of points if it's not already there
			for (int j= 0; j<got; ++j)	{
					if (Pn.distanceBetween(col, points[j], metric.getInteger()) < 10E-2) 
						{ already = true; break; }
			}
			if (!already) points[got++] =col;
		}
		boolean samepoint = got == 1;
//		if (got < 3)
//			throw new IllegalStateException("can't find invariant plane");
		boolean validPlane = got >= 3;
		double[] invplane = null;
		double[] invpoint = null;
		// if this didn't work, look for fixed point
		Biquaternion reflBiq = null;
		double[] reflM = null;
		boolean validPoint = true;
		if (validPlane) {
			invplane = P3.planeFromPoints(null, points[0], points[1], points[2]);
			logger.log(loglevel,"inv plane = "+Rn.toString(invplane));
			if (Rn.innerProduct(invplane, invplane, 3) < 10E-8 && metric == Metric.EUCLIDEAN) 
				validPlane = false;
		}
		if (validPlane)	{
			reflBiq = new Biquaternion(new double[]{0,invplane[0], invplane[1], invplane[2], -invplane[3], 0,0,0}, metric);			
			reflM = P3.makeReflectionMatrix(null, invplane, metric.getInteger());
		} else {
			if (!samepoint)	{
				Matrix mM = new Matrix(m);
				if (Rn.euclideanNormSquared(Rn.subtract(null,P3.originP3, mM.getColumn(3))) < 10E-8) {
					dst[0] = new Biquaternion(1,0,metric);
					dst[1] = new Biquaternion(1,0,metric);
					return dst;
				}
				double[] invtransp = Rn.transpose(null, Rn.inverse(null, m));
				logger.log(loglevel,"inv transp= "+Rn.matrixToString(invtransp));
				imageTetra = Rn.times(null, invtransp, dualTetra);
				logger.log(loglevel,"image tetra = "+Rn.matrixToString(imageTetra));
				sum = Rn.add(null, dualTetra, imageTetra);
				logger.log(loglevel,"sum tetra = "+Rn.matrixToString(sum));
				double[][] planes = new double[4][];
				sumM = new Matrix(sum);
				got = 0;
				for (int i = 0; i<4; ++i)	{
					double[] col = sumM.getColumn(i);
					if (Rn.euclideanNormSquared(col) < 10E-8) continue;
					planes[got++] = col;
				}//				if (got < 3)
//					throw new IllegalStateException("can't find invariant plane");
				validPoint = (got >= 3);
				if (!validPoint)
					throw new IllegalStateException("can't find invariant plane or point");
				invpoint = P3.pointFromPlanes(null, planes[0], planes[1], planes[2]);
			} else 
				invpoint = points[0];

			logger.log(loglevel,"fixed point = "+Rn.toString(invpoint));

//			double[] dualTetra = Rn.transpose(null,finiteTetra);
			reflBiq = new Biquaternion(new double[]{-invpoint[3], 0,0,0, 0, invpoint[0], invpoint[1], invpoint[2]}, metric);			
//			if (metric == Metric.EUCLIDEAN)	{
				double[] tlate = P3.makeTranslationMatrix(null, invpoint, metric.getInteger());
				reflM = Rn.conjugateByMatrix(null, pointRefl, tlate);
//			} else {
//				invplane = Pn.polarizePoint(null, invpoint, metric.getInteger());
//				reflM = P3.makeReflectionMatrix(null, invplane, metric.getInteger());
//			}
		}
		Biquaternion.normalize(reflBiq, reflBiq);
		double[] rest = Rn.times(null, reflM, m);
		logger.log(loglevel,"rest = "+Rn.matrixToString(rest));
		Biquaternion restBiq = biquaternionFromDirectIsometry(null, rest, metric);
		Biquaternion.normalize(restBiq, restBiq);
		logger.log(loglevel,"refl biq = "+reflBiq);
		logger.log(loglevel,"rest biq = "+restBiq);
		dst[0] = reflBiq;
		dst[1] = restBiq;
		return dst;		
	}
	public static Biquaternion biquaternionFromIndirectIsometry(Biquaternion dst, double[] m, Metric metric)	{
		if (dst == null) dst  = new Biquaternion();
		Biquaternion[] pieces = biquaternionsFromIndirectIsometry(null, m, metric);
		Biquaternion.times(dst, pieces[0], pieces[1]);
		Biquaternion.normalize(dst, dst);
		return dst;
	}

	public static Biquaternion biquaternionFromDirectIsometry(Biquaternion dst, double[] m, Metric metric)	{
		if (dst == null) dst = new Biquaternion(metric);
		if (Rn.isIdentityMatrix(m, 10E-8)) {
			dst.qr = new Quaternion(1,0,0,0);
			dst.qd = new Quaternion(0,0,0,0);
			return dst;
		}
		double[] perm = Rn.permutationMatrix(null, new int[]{1,2,3,0});
		double[] wxyzM = Rn.conjugateByMatrix(null, m, perm);
		double[] induced = PlueckerLineGeometry.inducedP5ProjFromP3Proj(null, wxyzM);
//			logger.log(loglevel,"induced = \n"+Rn.matrixToString(induced));
		// construct two 3x3 matrices representing the action on the "study" sphere
		double[] cr = new double[9], cd = new double[9];
		for (int i = 0; i<3; ++i)	{
			for (int j = 0; j<3; ++j)	{
				cr[i*3+j] = induced[6*i+j];
				cd[i*3+j] = induced[6*(5-i)+(j)];
			}
		}
//			logger.log(loglevel,"cr = \n"+Rn.matrixToString(cr));
//			logger.log(loglevel,"CCt = \n"+Rn.matrixToString(CCt));
		Biquaternion ret = null;
		// handle special case of euclidean translation
		if (Rn.isIdentityMatrix(cr, 10E-8)) {
			if (metric == Metric.EUCLIDEAN)	{
				double[] tlate = new Matrix(m).getColumn(3);
				tlate = Rn.times(tlate, .5, tlate);
				//ret = new Biquaternion(metric);
				dst.getRealPart().setValue(1, 0, 0, 0);
				dst.getDualPart().setValue(0, tlate[0], tlate[1], tlate[2]);
				logger.log(loglevel,"euc tlate = "+dst);				
			} else {
				dst.getRealPart().setValue(1, 0, 0, 0);
				dst.getDualPart().setValue(1, 0, 0, 0);
			}
			return dst;
		}
		Biquaternion E[] = {
				new Biquaternion(new double[]{1,0,0,0,0,0}, metric),
				new Biquaternion(new double[]{0,1,0,0,0,0}, metric),
				new Biquaternion(new double[]{0,0,1,0,0,0}, metric)
		};
		// create three bivectors that are the image of the standard three
		Biquaternion R[] = new Biquaternion[3];
		for (int i = 0; i<3; ++i)	{
			R[i] = new Biquaternion(
				new double[]{cr[i], cr[3+i], cr[6+i], cd[6+i],cd[3+i],cd[i]}, metric);
		}
		// construct the axis biquaternion h such that he(i) = r(i)h
		// h := e(i)r(j) - e(j)r(i) - r(k) - e(k)   for (i,j,k) cyclic permutation of (0,1,2)
		// use the first one that isn't too small
		// see blaschke "Non-euclidean geometry and mechanics" (1942) for original idea
		for (int i = 0; i<3; ++i)	{
			int index0 = (i+1)%3, index1 = (i+2)%3, index2 = (i+3)%3;
			// e2r3-e3r2-e1-r1
			ret = subtract(null, 
				subtract(null, 
					times(null, E[index1], R[index2]),
					times(null, E[index2], R[index1])),
				add(null, E[index0], R[index0]));
			// not sure why I have to conjugate here!
			ret = conjugate(ret, ret);
//			logger.log(loglevel,"ret = "+ret.toString());
			// TODO document the special case euclidean and non-invertible: when does that arise?
			boolean acceptable = (!isBiscalar(ret, 10E-4)) && (
					(metric == Metric.EUCLIDEAN && isInvertible(ret)) ||
					(metric != Metric.EUCLIDEAN));
			if (acceptable) {
				ret = normalize(null, ret);
				logger.log(loglevel,"ret = "+ret.toString());
				System.err.println("found biq, i = "+index0);
				break;
			}
		}
//		dst.qd=ret.qd;
//		dst.qr=ret.qr;
		normalize(dst, ret);
		return dst;
	}

	public static boolean isEllipticTranslation(Biquaternion src)	{
		return (src.getMetric() == Metric.ELLIPTIC && !isInvertible(src));
	}
	
	public static boolean isEuclideanTranslation(Biquaternion src)	{
		if (src.getMetric() != Metric.EUCLIDEAN) return false;
		Biquaternion biv = bivector(null, src);
		if (!isInvertible(biv)) return true;
		return false;
	}
	
	public static boolean isTranslation(Biquaternion src)	{
		return isEllipticTranslation(src) || isEuclideanTranslation(src);
	}
	
	enum ProductType {NULL, CONJ, DUAL, CONJDUAL};
	public static double[] matrixFromBiquaternion(double[] m, Biquaternion src)	{
		if (m == null) m = new double[16];
		Quaternion re = src.qr;
		Quaternion du = src.qd;
		int sig = src.getMetric().getInteger();
		double a = re.re, b = re.x, c = re.y, d = re.z, e = du.re, f = du.x, g = du.y, h = du.z;
		double a2 = a*a, b2= b*b, c2=c*c, d2=d*d, e2=e*e, f2=f*f, g2=g*g, h2=h*h;
		double[] mm = null;
		// check to see if its an elliptic translation: these can't be written in the standard form
		if (Biquaternion.isEllipticTranslation(src)) {
			if ( Rn.equals(Quaternion.IJK(null, src.qr), Quaternion.IJK(null, src.qd), 10E-8))  {  // left tlate
				mm = new double[]{a, -d, c, b,
					  d, a, -b, c,
					  -c, b, a, d,
					  -b, -c, -d, a};
				} else {		// use -conjugate
				mm = new double[]{-a, d, -c, b,
					-d, -a, b, c,
					c, -b, -a, d,
					-b, -c, -d, -a};
			}
			if (mm[15] >= 0) System.arraycopy(mm, 0, m, 0, 16);
			else Rn.times(m, -1, mm);
			return m;
		}
		ProductType type = ProductType.NULL;
        switch(type) {
            case NULL:
			// the nonstandard version (q x conjugate(dual(q)))
			mm = new double[]{a2 + b2 - c2 - d2 - 
				    e2*sig - sig*f2 + sig*g2 + 
				    sig*h2,
				    2*b*c - 2*a*d - 2*sig*f*g + 2*e*sig*h,
				   2*a*c + 2*b*d - 2*e*sig*g - 2*sig*f*h,
				   -2*b*e + 2*a*f - 2*d*g + 2*c*h,
				   2*b*c + 2*a*d - 2*sig*f*g - 2*e*sig*h,
				   a2 - b2 + c2 - d2 - 
				    e2*sig + sig*f2 - sig*g2 + 
				    sig*h2,
				    -2*a*b + 2*c*d + 2*e*sig*f - 2*sig*g*h,
				   -2*c*e + 2*d*f + 2*a*g - 2*b*h,
				   -2*a*c + 2*b*d + 2*e*sig*g - 2*sig*f*h,
				   2*a*b + 2*c*d - 2*e*sig*f - 2*sig*g*h,
				   a2 - b2 - c2 + d2 - 
				    e2*sig + sig*f2 + sig*g2 - 
				    sig*h2,
				    -2*d*e - 2*c*f + 2*b*g + 2*a*h,
				   2*b*e*sig - 2*a*sig*f - 2*d*sig*g + 2*c*sig*h,
				   2*c*e*sig + 2*d*sig*f - 2*a*sig*g - 2*b*sig*h,
				   2*d*e*sig - 2*c*sig*f + 2*b*sig*g - 2*a*sig*h,
				   a2 + b2 + c2 + d2 - 
				    e2*sig - sig*f2 - sig*g2 - 
				    sig*h2};			
            break;
            case CONJ:  // standard
			// this is the  "theoretical" version  (conjugate(q) x dual(q))
            	// in euclidean case makes logarithmic spirals
            	mm = new double[]{a2 + b2 - c2 - d2 - 
			    e2*sig - sig*f2 + sig*g2 + 
			    sig*h2,
			    2*b*c + 2*a*d - 2*sig*f*g - 2*e*sig*h,
			   -2*a*c + 2*b*d + 2*e*sig*g - 2*sig*f*h,
			   2*b*e - 2*a*f - 2*d*g + 2*c*h,
			   2*b*c - 2*a*d - 2*sig*f*g + 2*e*sig*h,
			   a2 - b2 + c2 - d2 - 
			    e2*sig + sig*f2 - sig*g2 + 
			    sig*h2,2*a*b + 2*c*d - 2*e*sig*f - 2*sig*g*h,
			   2*c*e + 2*d*f - 2*a*g - 2*b*h,
			   2*a*c + 2*b*d - 2*e*sig*g - 2*sig*f*h,
			   -2*a*b + 2*c*d + 2*e*sig*f - 2*sig*g*h,
			   a2 - b2 - c2 + d2 - 
			    e2*sig + sig*f2 + sig*g2 - 
			    sig*h2,2*d*e - 2*c*f + 2*b*g - 2*a*h,
			   -2*b*e*sig + 2*a*sig*f - 2*d*sig*g + 2*c*sig*h,
			   -2*c*e*sig + 2*d*sig*f + 2*a*sig*g - 2*b*sig*h,
			   -2*d*e*sig - 2*c*sig*f + 2*b*sig*g + 2*a*sig*h,
			   a2 + b2 + c2 + d2 - 
			    e2*sig - sig*f2 - sig*g2 - 
			    sig*h2};
            	break;
            case DUAL:	// euclidean case all right except rotation is backwards
            	mm = new double[]{a2 + b2 - c2 - d2 - 
			    e2*sig - sig*f2 + sig*g2 + 
			    sig*h2,2*b*c - 2*a*d - 2*sig*f*g + 2*e*sig*h,
			   2*a*c + 2*b*d - 2*e*sig*g - 2*sig*f*h,
			   2*b*e - 2*a*f + 2*d*g - 2*c*h,
			   2*b*c + 2*a*d - 2*sig*f*g - 2*e*sig*h,
			   a2 - b2 + c2 - d2 - 
			    e2*sig + sig*f2 - sig*g2 + 
			    sig*h2,-2*a*b + 2*c*d + 2*e*sig*f - 2*sig*g*h,
			   2*c*e - 2*d*f - 2*a*g + 2*b*h,
			   -2*a*c + 2*b*d + 2*e*sig*g - 2*sig*f*h,
			   2*a*b + 2*c*d - 2*e*sig*f - 2*sig*g*h,
			   a2 - b2 - c2 + d2 - 
			    e2*sig + sig*f2 + sig*g2 - 
			    sig*h2,2*d*e + 2*c*f - 2*b*g - 2*a*h,
			   -2*b*e*sig + 2*a*sig*f + 2*d*sig*g - 2*c*sig*h,
			   -2*c*e*sig - 2*d*sig*f + 2*a*sig*g + 2*b*sig*h,
			   -2*d*e*sig + 2*c*sig*f - 2*b*sig*g + 2*a*sig*h,
			   a2 + b2 + c2 + d2 - 
			    e2*sig - sig*f2 - sig*g2 - 
			    sig*h2  };       
            	break;
            case CONJDUAL:	// euclidean case makes logarithmic spirals
            	mm = new double[]{a2 + b2 - c2 - d2 - 
        			e2*sig - sig*f2 + sig*g2 + 
        			sig*h2,
        			2*b*c + 2*a*d - 2*sig*f*g - 2*e*sig*h,
        			-2*a*c + 2*b*d + 2*e*sig*g - 2*sig*f*h,
        			-2*b*e + 2*a*f + 2*d*g - 2*c*h,
        			2*b*c - 2*a*d - 2*sig*f*g + 2*e*sig*h,
        			a2 - b2 + c2 - d2 - 
        			e2*sig + sig*f2 - sig*g2 + 
        			sig*h2,2*a*b + 2*c*d - 2*e*sig*f - 2*sig*g*h,
        			-2*c*e - 2*d*f + 2*a*g + 2*b*h,
        			2*a*c + 2*b*d - 2*e*sig*g - 2*sig*f*h,
        			-2*a*b + 2*c*d + 2*e*sig*f - 2*sig*g*h,
        			a2 - b2 - c2 + d2 - 
        			e2*sig + sig*f2 + sig*g2 - 
        			sig*h2,-2*d*e + 2*c*f - 2*b*g + 2*a*h,
        			2*b*e*sig - 2*a*sig*f + 2*d*sig*g - 2*c*sig*h,
        			2*c*e*sig - 2*d*sig*f - 2*a*sig*g + 2*b*sig*h,
        			2*d*e*sig + 2*c*sig*f - 2*b*sig*g - 2*a*sig*h,
        			a2 + b2 + c2 + d2 - 
        			e2*sig - sig*f2 - sig*g2 - 
        			sig*h2};
            break;
        }
//		P3.orthonormalizeMatrix(null, m, 10E-10, sig);
//		printDots(mm, sig);
//		if (true || mm[15] >= 0) 
		System.arraycopy(mm, 0, m, 0, 16);
//		else Rn.times(m, -1, mm);
		return m;
	}

//	private static void printDots(double[] m, int sig)	{
//		double[] dots = new double[16];
//		Matrix mat = new Matrix(m);
//		int colms = sig == Pn.EUCLIDEAN ? 3 : 4;
//		for (int i = 0; i<colms; ++i)	{
//			for (int j = i; j<colms; ++j)	{
//				double[] c1 = mat.getColumn(i);
//				double[] c2 = mat.getColumn(j);
//				dots[4*i+j] = Pn.innerProduct(c1, c2, sig);
//			}
//		}
//		logger.log(loglevel,"sig = "+sig+"\nDots = \n"+Rn.matrixToString(dots));
//	}

}

// in interesting tale behind the following calculation:  went around my elbow to get to my nose
// used mathematica to extract complicated formula for what amounts to the same thing
// I do above with the normalize() method, essentially finding the square root of a dual number.
//public static Biquaternion axisForBivector(Biquaternion dst, Biquaternion src)	{
//Metric metric = src.getMetric();
//if (dst == null) {
//	throw new IllegalArgumentException("dst argument must be non-null");
//	dst = new Biquaternion(metric);
//}
//Biquaternion n2 = normSquared(src);
//Quaternion qr = src.getRealPart();
//Quaternion qd = src.getDualPart();
//double c00 = Quaternion.innerProduct(qr, qr);
//double c11 = Quaternion.innerProduct(qd, qd);
//double c01 = Quaternion.innerProduct(qr, qd);
//// we seek a biscalar s = (a0, a1) such that s * src has vanishing dual part
//// this leads to a quadratic equation in a0 (or a1, its a homogeneous eqn in the two variables)
//double a = c01, 
//	b = c00 + metric.getInteger()* c11,
//	c = metric.getInteger()*c01;
//double a0, a1;
//if (a == 0)	{
//	if (b == 0) {
//		if (c != 0) throw new IllegalStateException("impossible eqn");
//		else {a0 = a1 = 0;}
//	} else {
//		a0 = -c; a1 = b;	// linear bx+c
//	} 
//} else {
//	if (c == 0) {	// breaks into linear factors x(bx+a)
//		a0 = -b; a1 = a;
//	} else {
//		// genuine quadratic!
//		double d = b*b - 4*a*c;
//		if (d < 0) throw new IllegalStateException("impossible eqn");
//		a0 = -b + Math.sqrt(d);
//		a1 = 2*a;
//		logger.log(loglevel,"a0 = "+a0+" a1 = "+a1);
//	}
//}
//Biquaternion biscalar = new Biquaternion(a0, a1, metric);
//dst = times(dst, biscalar, src);
//Biquaternion prod = times(null, dst, dst);
//logger.log(loglevel,"axis * axis = "+prod);
//return biscalar;
//}
// following code is rejected attempts from biquaternionFromIsometry
//else if (false && metric == Metric.HYPERBOLIC)	{
//	double[] imo = new Matrix(m).getColumn(3);
//	double[] tlate = P3.makeTranslationMatrix(null, imo, metric.sig);
//	double[] itlate = Rn.inverse(null, tlate);
//	double[] rot = Rn.times(null, itlate, m);
//	logger.log(loglevel,"tlate = "+Rn.matrixToString(tlate));
//	logger.log(loglevel,"rot = "+Rn.matrixToString(rot));
//	ql = Quaternion.rotationMatrixToQuaternion(null, rot);
//	qr = new Quaternion(imo[3], imo[0], imo[1], imo[2]);
//	
//	logger.log(loglevel,"ql = "+ql);
//	logger.log(loglevel,"qr = "+qr);
//	dst.qr = ql;
//	dst.qd = qr;
//	//Quaternion.times(dst.qr, .5, Quaternion.add(null, ql, qr));
//	//Quaternion.times(dst.qd, .5, Quaternion.subtract(null, ql, qr));
//} else {
//	// extract columns of m as quaternions
//	Quaternion B[] = new Quaternion[4];
//	for (int i = 0; i<4; ++i) {
//			B[i] = new Quaternion(m[12+i], m[i], m[4+i], m[8+i]);
//	}
//	// construct the left-directions
//	int noid = 3, offset = 0;
//	Quaternion cb0 = B[noid];
//	//if (metric == Metric.ELLIPTIC) 
//		cb0 = Quaternion.conjugate(null, cb0); //0]); //
//	Quaternion rl[] = new Quaternion[3], rr[] = new Quaternion[3];
//	// construct two rotation matrices
//	double rml[] = new double[9], rmr[] = new double[9];
//	for (int i = 0; i<3; ++i)	{
//		rl[i] = Quaternion.times(null, B[i+offset], cb0);
//		logger.log(loglevel,"rli = "+rl[i]);
//		rr[i] = Quaternion.times(null, cb0, B[i+offset]);
//		double[] rdl = Quaternion.asDouble(null, rl[i]);
//		Rn.normalize(rdl, rdl);
//		double[] rdr = Quaternion.asDouble(null, rr[i]);
//		Rn.normalize(rdr, rdr);
//		for (int j = 0; j<3; ++j)	{
//			rml[3*j+i] = rdl[j+1];
//			rmr[3*j+i] = rdr[j+1];
//		}
//	}
//	// extract two quaternions
//	ql = Quaternion.rotationMatrixToQuaternion(null, rml);
//	qr = Quaternion.rotationMatrixToQuaternion(null, rmr);	
//	logger.log(loglevel,"ql = "+ql);
//	logger.log(loglevel,"qr = "+qr);
//	Quaternion.times(dst.qr, .5, Quaternion.add(null, ql, qr));
//	Quaternion.times(dst.qd, .5, Quaternion.subtract(null, ql, qr));
//}
//logger.log(loglevel,"ql = "+ql);
//logger.log(loglevel,"qr = "+qr);
