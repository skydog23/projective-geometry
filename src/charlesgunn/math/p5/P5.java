package charlesgunn.math.p5;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;

/**
 * This class deals with real projective 5-space and the four possible non-singular metrics
 * See {@link Pn}, {@link P3} and {@link Rn}.
 * @author Charles Gunn
 *
 */
public class P5 {
	
	final public static int LINE_SPACE = 0;		// sig (3,3)
	final public static int SPHERE_SPACE = 1;	// sig (4,2)
	final public static int HYPERBOLIC = 2;		// sig (5,1)
	final public static int ELLIPTIC = 3;		// sig (6,0)
	
	final public static double[] LIE_POINT_AT_INFINITY = {0,0,0,0,1,0};
	// define the four different quadrics which correspond to the above metrics
	// we don't need this any more...
//	final private static double[][] Q = {
//			{0,0,0,0,0,1,   0,0,0,0,1,0,    0,0,0,1,0,0,   0,0,1,0,0,0,   0,1,0,0,0,0,   1,0,0,0,0,0},
//			{1,0,0,0,0,0,	0,1,0,0,0,0,	0,0,1,0,0,0,	0,0,0,-1,0,0, 0,0,0,0,0,-.5,   0,0,0,0,-.5,0},
//			//Rn.diagonalMatrix(null, new double[]{1,1,1,1,-1,-1}),
//			Rn.diagonalMatrix(null, new double[]{1,1,1,1,1,-1}),
//			Rn.diagonalMatrix(null, new double[]{1,1,1,1,1,1})};
	final private static double tolerance = 10E-8;
	
	public static Logger logger = Logger.getLogger("de.jtem.projgeom");
	{
		logger.setLevel(Level.INFO);
	}

	/**
	 * Calculates the sum of absolute values for the first <i>m</i> entries of <i>po</i>.
	 * @param p0
	 * @param m
	 * @return
	 */
	public static double manhattenMetric(double[] p0, int m) {
		if (m < 0) throw new IllegalStateException("m must be non-negative");
		if (m > p0.length) throw new IllegalStateException("m exceeds the length of p0");
		double sum = 0;
		for (int i=0; i<m; ++i)
			sum = sum + Math.abs(p0[i]);
		return sum;
	}
	
	/**
	 * Calculates an adaptive tolerance depending on the scale of the first four coordinates.
	 * @param p0
	 * @return
	 */
	//TODO: makes sense for other metrics than LIE?
	public static double getTolerance(double[] p0) {
		double m = manhattenMetric(p0,4);
		m = m*m;
		return (m > 1 ? m : 1) * tolerance;
	}
	
	/**
	 * The following method has been optimized to avoid the matrix multiplications, 
	 * but should be thought of abstractly as p0.Q[sig].p1 where Q is the symmetric
	 * form from the above list.
	 * @param p0
	 * @param p1
	 * @param metric
	 * @return
	 */
	public static double innerProduct(double[] p0, double[]p1, int metric)	{
		switch(metric)	{
			case LINE_SPACE: return PlueckerLineGeometry.innerProduct(p0, p1);
			case SPHERE_SPACE: return LieSphereGeometry.lieInnerProduct(p0, p1);
			case HYPERBOLIC: return Pn.innerProduct(p0, p1, Pn.HYPERBOLIC);
			case ELLIPTIC: return Pn.innerProduct(p0, p1, Pn.ELLIPTIC);
		default:
			throw new IllegalStateException("No such metric"+metric);
		}
	}
	
	public static boolean isOnQuadric(double[] p0, int metric)	{
		return arePolarPoints(p0, p0, metric);
	}
	
	public static boolean isOnQuadric(double[] p0, int metric, boolean adaptTol)	{
		return arePolarPoints(p0, p0, metric, adaptTol);
	}
	
	public static boolean arePolarPoints(double[] p0, double[]p1, int metric)	{
		return Math.abs(innerProduct(p0, p1, metric)) < tolerance ;	
	}
	
	public static boolean arePolarPoints(double[] p0, double[]p1, int metric, boolean adaptTol)	{
		double tol;
		if (adaptTol)
			 tol = Math.max(getTolerance(p0),getTolerance(p1));
		else tol = tolerance;
		return Math.abs(innerProduct(p0, p1, metric)) < tol;	
	}
	
	public static double[] normalize(double[] np, double[] p, int metric)	{
		if (np == null) np = new double[p.length];
		double d = innerProduct(p, p, metric);
		if (Math.abs(d) < tolerance) {
			np = p; return np;
		}
		return Rn.times(np, 1.0/Math.sqrt(d), p);
	}
	
	/**
	 * Checks whether two points define an isotropic line, i.e. a line lying completly on
	 * the absolute quadric determined by given metric.
	 * @param p0
	 * @param p1
	 * @param metric
	 * @return
	 */
	private static boolean isIsotropicLine(double[] p0, double[] p1, int metric)	{
		return ( isOnQuadric(p0, metric) && isOnQuadric(p1, metric) && arePolarPoints(p0, p1, metric) );
	}
	
	private static boolean isIsotropicLine(double[] p0, double[] p1, int metric, boolean adaptTol)	{
		return ( isOnQuadric(p0, metric, adaptTol) && isOnQuadric(p1, metric, adaptTol) && arePolarPoints(p0, p1, metric, adaptTol) );
	}
			
	/**
	 * If the line determined by the points <i>p0</i> and <i>p1</i> is not completely contained
	 * in the polar of <i>q</i>, then there is a unique intersection point of the line with the
	 * polar (hyperplane).
	 * @param p0
	 * @param p1
	 * @param q
	 * @param metric
	 * @return Intersection of the line through <i>p0</i> and <i>p1</i> with the polar of <i>q</i>, if unique.
	 */
	//TODO: implement with adaptive tolerance?
	public static double[] getIntersectionOfLineWithPolar(double[] dst, double[] p0, double[] p1, double[] q, int metric) {
		if (dst == null)
			dst = new double[6];
		if (arePolarPoints(p0, q, metric)) {
			if (arePolarPoints(p1, q, metric))
				throw new IllegalStateException("Line through " + Rn.toString(p0) + " and " + Rn.toString(p1)
						+ " completely contained in the polar of " + Rn.toString(q));
			System.arraycopy(p0, 0, dst, 0, 6);
		}
		else { 
			Rn.linearCombination(dst, - innerProduct(p1, q, metric), p0, innerProduct(p0, q, metric), p1);
		}
		return dst;
	}
	
}
