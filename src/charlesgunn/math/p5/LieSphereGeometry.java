package charlesgunn.math.p5;

import de.jreality.math.Pn;
import de.jreality.math.Rn;

public class LieSphereGeometry {
	final public static int LINE_SPACE = 0;		// sig (3,3)
	final public static int SPHERE_SPACE = 1;	// sig (4,2)
	final public static int HYPERBOLIC = 2;		// sig (5,1)
	final public static int ELLIPTIC = 3;		// sig (6,0)

	final public static double[] LIE_POINT_AT_INFINITY = {0,0,0,0,1,0};
	final private static double tolerance = 10E-8;
	
	/*
	 * Here begin methods applicable only to Lie sphere geometry.
	 * These methods expect that all 6-tuples representing generalized spheres, have been 
	 * properly normalized, i.e., a projective representative chosen that satisfies following:
	 * proper spheres are represented as 6-tuples (x,y,z,r,|c|^2-r^2,1) 
	 * 		where c=(x,y,z) is the center, r is radius
	 * proper spheres are represented as proper spheres with radius r=0
	 * proper planes are represented as 6-tuples (x,y,z,1,2h,0) 
	 * 		where n=(x,y,z) is the normal direction (unit length), and h is the signed distance to origin
	 * the infinite element is represented as (0,0,0,0,1,0)
	 */
	
	protected static double lieInnerProduct(double[] p0, double[] p1)	{
		return p0[0]*p1[0]+p0[1]*p1[1]+p0[2]*p1[2]-p0[3]*p1[3]-.5*p0[4]*p1[5]-.5*p0[5]*p1[4];
	}

	/**
	 * The set of generalized (oriented) spheres includes planes and points.
	 */
	public static boolean isGeneralizedSphere(double[] p0)	{
		return P5.isOnQuadric(p0, SPHERE_SPACE);
	}
	
	public static boolean isGeneralizedSphere(double[] p0, boolean adaptTol)	{
		return P5.isOnQuadric(p0, SPHERE_SPACE, adaptTol);
	}
	
	/**
	 * Here is also checked whether p0 is not a point and not a plane
	 * @param p0
	 * @return
	 */
	public static boolean isProperSphere(double[] p0)	{
		return (isGeneralizedSphere(p0) && !_isPlane(p0) && !_isPoint(p0));
	}
	
	public static boolean isProperSphere(double[] p0, boolean adaptTol)	{
		return (isGeneralizedSphere(p0, adaptTol) && !_isPlane(p0, adaptTol) && !_isPoint(p0, adaptTol));
	}
	
	/**
	 * Checks to see if p0 is a generalized sphere and in particular a plane.
	 * @param p0
	 * @return
	 */
	public static boolean isPlane(double[] p0)	{
		return isGeneralizedSphere(p0) && _isPlane(p0)  && !_isPoint(p0);
	}
	
	public static boolean isPlane(double[] p0, boolean adaptTol)	{
		return isGeneralizedSphere(p0, adaptTol) && _isPlane(p0, adaptTol)  && !_isPoint(p0, adaptTol);
	}
	
	/**
	 * Local method to be used only when isSphere() is known to be satisfied
	 * @param p0
	 * @return
	 */
	protected static boolean _isPlane(double[] p0)	{
		return ( Math.abs(p0[5]) < tolerance);
	}
	
	protected static boolean _isPlane(double[] p0, boolean adaptTol)	{
		double tol;
		if (adaptTol)
			 tol = P5.getTolerance(p0);
		else tol = tolerance;
		return ( Math.abs(p0[5]) < tol);
	}
	
	/**
	 * Checks to see if the "sphere" is in particular a point.
	 * @param p0
	 * @return
	 */
	public static boolean isPoint(double[] p0)	{
		if (isInfinity(p0)) return true; 
		else return isGeneralizedSphere(p0) && _isPoint(p0) && !_isPlane(p0);
	}
	
	public static boolean isPoint(double[] p0, boolean adaptTol)	{
		if (isInfinity(p0, adaptTol)) return true; 
		else return isGeneralizedSphere(p0, adaptTol) && _isPoint(p0, adaptTol) && !_isPlane(p0, adaptTol);
	}
	
	/**
	 * Local method to be used only when isSphere() is known to be satisfied
	 * @param p0
	 * @return
	 */
	protected static boolean _isPoint(double[] p0)	{
		return ( Math.abs(p0[3]) < tolerance);
	}
	
	protected static boolean _isPoint(double[] p0, boolean adaptTol)	{
		double tol;
		if (adaptTol)
			 tol = P5.getTolerance(p0);
		else tol = tolerance;
		return ( Math.abs(p0[3]) < tol);
	}
	
	/**
	 * Infinity corresponds to the (normalized) vector (0,0,0,0,1,0).
	 * @param p0
	 * @return
	 */
	public static boolean isInfinity(double[] p0)	{
		if (p0[4] == 0) return false;
		else {
			double[] np0 = Rn.times(null, p0[4], p0);
			return Rn.equals(np0, LIE_POINT_AT_INFINITY, tolerance);
		}
	}
	
	public static boolean isInfinity(double[] p0, boolean adaptTol)	{
		if (p0[4] == 0) return false;
		else {
			double tol;
			if (adaptTol)
				 tol = P5.getTolerance(p0);
			else tol = tolerance;
			double[] np0 = Rn.times(null, p0[4], p0);
			return Rn.equals(np0, LIE_POINT_AT_INFINITY, tol);
		}
	}
	
	/**
	 * Normalize the "sphere" coordinates as follows:
	 * If it's a proper sphere, dehomogenize to make last coordinate = 1
	 * If it's not (last coordinate is 0), then:
	 *  If it's point at infinity, return a copy of (0,0,0,0,1,0)
	 *  If it's a plane, normalize the direction vector to have length 1.
	 *   This is done by making p0[3] = 1 (not -1).
	 *   (Then, p0[4] is twice the oriented distance of the plane to the origin)
	 * @param dst
	 * @param p0
	 * @return
	 */
	public static double[] normalizeLieSphereCoordinates(double[] dst, double[] p0)	{
		if (dst == null) dst = new double[6];
		Pn.dehomogenize(dst, p0);
		if (!isGeneralizedSphere(dst,true))
			throw new IllegalStateException("Non isotropic point "+Rn.toString(dst));
		if (_isPlane(dst,true))	{
			if (_isPoint(dst,true))	{
				// infitiy is the only point on the Lie quadric with p0[5] = p0[3] = 0,
				// i.e. "_isPlane" and "_isPoint" are true 
				System.arraycopy(LIE_POINT_AT_INFINITY, 0, dst, 0, 6);
				return dst;
			}
			Rn.times(dst, 1.0/dst[3], dst);
		}
		return dst;
	}
	
	/**
	 * <i>p0</i> and <i>p1</i> are expected to be points on the Lie quadric which are also polar to each other.  
	 * Calculate the elements on their joining line in P5 which correspond to a point (e3=0), and to a plane (e5=0).
	 * The combination of these two elements is called a <i>contact element</i>.
	 * <i>dst</i> will be returned where <i>dst</i>[0] corresponds to the point and <i>dst</i>[1] corresponds to the plane.
	 * @param dst
	 * @param p0
	 * @param p1
	 * @return
	 */
	public static double[][] normalizeContactElement(double[][] dst, double[] p0, double[] p1)	{
		if (!isGeneralizedSphere(p0,true))
			throw new IllegalStateException("Non isotropic point "+Rn.toString(p0));
		if (!isGeneralizedSphere(p1,true))
			throw new IllegalStateException("Non isotropic point "+Rn.toString(p1));
		if (!P5.arePolarPoints(p0,p1,SPHERE_SPACE,true) )
			throw new IllegalStateException("Non polar points "+Rn.toString(p0)+" "+Rn.toString(p1) + " are not in oriented contact");
		if (dst == null) dst = new double[2][6];
		if (dst[0] == null) dst[0] = new double[6];
		if (dst[1] == null) dst[1] = new double[6];
		// this only can happen if the two planes are identical
		if (isPlane(p0,true) && isPlane(p1,true))	{
			System.arraycopy(LIE_POINT_AT_INFINITY, 0, dst[0], 0, 6);
			System.arraycopy(p0,0,dst[1],0,6);
			dst[1][4] = 0.0;		// choose the plane through the origin
//			throw new IllegalStateException("Two planes can't have contact point"+Rn.toString(p0)+" "+Rn.toString(p1));
		} else {
			// find the contact point: linear combinatino where the "radius" coordinate is 0
			Rn.linearCombination(dst[0], -p1[3], p0, p0[3], p1);
			// find the contact plane: linear combination where the homogeneous coordinate is 0
			Rn.linearCombination(dst[1], -p1[5], p0, p0[5], p1);
		}
		normalizeLieSphereCoordinates(dst[0], dst[0]);
		normalizeLieSphereCoordinates(dst[1], dst[1]);
		return dst;
	}
	
	/**
	 * Convenience method which picks out the point of contact from the contact element.
	 * @param dst
	 * @param p0
	 * @param p1
	 * @return
	 */
	public static double[] getContactPoint(double[] dst, double[] p0, double[] p1) {
		double[][] target = new double[2][];
		target[0] = dst;
		return normalizeContactElement(target, p0, p1)[0];
	}
	
	/**
	 * Convenience method which picks out the plane of contact from the contact element.
	 * @param dst
	 * @param p0
	 * @param p1
	 * @return
	 */
	public static double[] getContactPlane(double[] dst, double[] p0, double[] p1) {
		double[][] target = new double[2][];
		target[1] = dst;
		return normalizeContactElement(target, p0, p1)[1];
	}

    /**
     * Checks whether the given contact elements coincide.
     * If <i>adaptTol</i> equals <i>true</i>, an adaptive tolerance is used.
     * If it is <i>false</i>, the method uses the pre-defined tolerance (10E-8).
     * Requires normalized representation of contact elements
     * @param c1
     * @param c2
     * @param adaptTol
     * @return
     */
	//TODO: makes adaptive tolerance sense in this context? Implement independent of normalization 
    public static boolean contactElementsCoincide(double[][] c1, double[][] c2, boolean adaptTol) {
    	double tol1, tol2;
		if (adaptTol) {
			 tol1 = Math.max(P5.getTolerance(c1[0]),P5.getTolerance(c2[0]));
			 tol2 = Math.max(P5.getTolerance(c1[1]),P5.getTolerance(c2[1]));
		}
		else {
			tol1 = tolerance;
			tol2 = tolerance;
		}
    	if (Rn.equals(c1[0], c2[0], tol1) && Rn.equals(c1[1], c2[1], tol2)) return true;
    	else return false;
    }
    
    /**
     * Checks whether a given array <i>c</i> really is a contact element
     * in the sense that the first entry <i>c</i>[0] represent a point
     * and the second one <i>c</i>[1] represents a plane.
     * @param c
     * @return
     */
    public static boolean isContactElement(double[][] c) {
    	return ( P5.arePolarPoints(c[0], c[1], SPHERE_SPACE) && isPoint(c[0]) && isPlane(c[1]) );
    }
    
    public static boolean isContactElement(double[][] c, boolean adaptTol) {
    	return ( P5.arePolarPoints(c[0], c[1], SPHERE_SPACE, adaptTol) && isPoint(c[0], adaptTol) && isPlane(c[1], adaptTol) );
    }
   
    /**
     * Computes the Lie sphere coordinates of the unique common sphere in
     * <i>l1</i> and <i>l2</i>.
     * It is used that for any isotropic projective line l and an
     * isotropic projective point x not on l, there is another unique isotropic projective line
     * through x intersecting l.
     * Exeptions are thrown if <i>l1</i> and <i>l2</i> do not intersect or the intersection is not unique, i.e.
     * <i>l1</i> equals <i>l2</i> and also if they are not isotropic lines.
     * @param dst
     * @param l1
     * @param l2
     * @return
     */
    public static double[] getCommonGeneralizedSphere(double[] dst, double[][] l1, double[][] l2) {
    	double[][] c1;
    	double[][] c2;
    	   	
    	// it easier to handle the normalized case, therefore c1 and c2 are assured to represent contact elements
    	// within the call of "normalizeContactElement" it is also checked whether the lines 
    	// l1 and l2 are isotropic at all.
    	if (isContactElement(l1)) c1 = l1;
    	else c1 = normalizeContactElement(null, l1[0], l1[1]);
    	if (isContactElement(l2)) c2 = l2;
    	else c2 = normalizeContactElement(null, l2[0], l2[1]);
    	
    	double[] x1 = c1[0];
    	double[] p1 = c1[1];
    	double[] x2 = c2[0];
    	double[] p2 = c2[1];
    	
    	if (dst == null) dst = new double[6];
    	
    	// check whether Euclidean points coincide
    	if (Rn.equals(x1, x2, tolerance)) {
    		// if also planes coincide the contact elements are identical
    		if (Rn.equals(p1, p2, tolerance)) 
    			throw new IllegalStateException("Contact elements coincide "
    					+ Rn.toString(c1) + " and " + Rn.toString(c2));
    		// else x1 = x2 is intersection point
    		System.arraycopy(x1,0,dst,0,6);
    	}
    	// otherwise we know that x2 is not contained in c1 and we can compute the unique contact element c
    	// containing x2 and intersecting c1
    	else {
    		// calculate the intersection point of c with c1
    		Rn.linearCombination(dst,
    				- P5.innerProduct(p1, x2, SPHERE_SPACE), x1,
    				P5.innerProduct(x1, x2, SPHERE_SPACE), p1);
    		// which gives the contact element 
    		double[][] c = normalizeContactElement(null, x2, dst);
    		// c1 and c2 intersect if and only if c equals c2
    		if (!contactElementsCoincide(c, c2, false))
    			throw new IllegalStateException("Contact elements do not intersect "
    					+ Rn.toString(c1) + " and " + Rn.toString(c2));
    	}
    	normalizeLieSphereCoordinates(dst, dst);
    	return dst;
    }
		
	/*
	 * The following methods are concerned with converting to and from the two sets
	 * {generalized oriented spheres in R3} and {points on the Lie quadric}.
	 */
	/**
	 * return plane coordinates in R3 for the plane represented by <i>p0</i>
	 * @param dst
	 * @param p0 - Lie sphere coordinates for a Euclidean plane
	 * @return
	 */
	public static double[] getPlaneCoordinates(double[] dst, double[] p0)	{
		if (!isPlane(p0,true))
			throw new IllegalStateException("Not a plane "+p0);
		if (dst == null) dst = new double[4];
		double[] np0 = normalizeLieSphereCoordinates(null, p0);
		for (int i = 0; i<3; ++i) dst[i] = np0[i];
		dst[3] = -np0[4]/2;
		return dst;
	}
	
	/**
	 * Get sphere coordinates (x,y,z,r) = (center,radius) for a sphere in R^3.
	 * If r=0 the sphere is a point. 
	 * @param dst
	 * @param p0 - Lie sphere coordinates for a Euclidean point or a proper sphere
	 * @return	4-vector
	 */
	public static double[] getSphereCoordinates(double[] dst, double[] p0)	{
		if (!isGeneralizedSphere(p0,true))
			throw new IllegalStateException("Non isotropic point "+Rn.toString(p0));
		if (isPlane(p0))
			throw new IllegalStateException("Plane, not sphere: "+p0);
		if (dst == null) dst = new double[4];
		double[] np0 = normalizeLieSphereCoordinates(null, p0);
		for (int i = 0; i<4; ++i) dst[i] = np0[i];
		return dst;
	}
	
	/**
	 * If not a plane, returns the center of the sphere.
	 * In the case of a point representative this is the point itself.
	 * @param dst
	 * @param p0 - Lie sphere coordinates for a Euclidean point or a proper sphere
	 * @return
	 */
	public static double[] getSphereCenter(double[] dst, double[] p0)	{
		if (dst == null) dst = new double[3];
		double[] sphereCoordinates = getSphereCoordinates(null, p0);
		for (int i = 0; i<3; ++i) dst[i] = sphereCoordinates[i];
		if (dst.length == 4) dst[3] = 1.0;
		return dst;
	}
	
	/**
	 * If a not a plane, returns the radius.
	 * For a point this is 0.
	 * @param p0 - Lie sphere coordinates for a Euclidean point or a proper sphere
	 * @return
	 */
	public static double getSphereRadius(double[] p0)	{
		return getSphereCoordinates(null, p0)[3];
	}
	
	/**
	 * Get Euclidean sphere coordinates (x,y,z,r) = (center,radius) for a proper sphere in R^3.
	 * r is not equal zero.
	 * @param dst
	 * @param p0 - Lie sphere coordinates of a proper sphere
	 * @return
	 */
	public static double[] getProperSphereCoordinates(double[] dst, double[] p0)	{
		if (!isProperSphere(p0,true))
			throw new IllegalStateException("Not a proper sphere "+Rn.toString(p0));
		if (dst == null) dst = new double[4];
		double[] np0 = normalizeLieSphereCoordinates(null, p0);
		for (int i = 0; i<4; ++i) dst[i] = np0[i];
		return dst;
	}
	
	/**
	 * In the case of a proper sphere its center is returned, otherwise a exception is thrown.
	 * @param dst
	 * @param p0 - Lie sphere coordinates for a Euclidean point or a proper sphere
	 * @return
	 */
	public static double[] getProperSphereCenter(double[] dst, double[] p0)	{
		if (dst == null) dst = new double[3];
		double[] sphereCoordinates = getProperSphereCoordinates(null, p0);
		for (int i = 0; i<3; ++i) dst[i] = sphereCoordinates[i];
		if (dst.length == 4) dst[3] = 1.0;
		return dst;
	}
	
	/**
	 * Get Euclidean point coordinates (x,y,z) of a point in R^3 other than infinity.
	 * @param dst
	 * @param p0 - Lie sphere coordinates of a Euclidean point
	 * @return
	 */
	public static double[] getPointCoordinates(double[] dst, double[] p0)	{
		
		if (!isPoint(p0,true))
			throw new IllegalStateException("Not a point "+Rn.toString(p0));
		if (dst == null) dst = new double[3];
		double[] np0 = normalizeLieSphereCoordinates(null, p0);
		for (int i = 0; i<3; ++i) dst[i] = np0[i];
		return dst;
	}
		
	/**
	 * Create normalized Lie sphere coordinates for a sphere with center <i>center</i> (non-zero last coordinate) and 
	 * signed radius <i>r</i>.
	 * Can be used for generating Lie sphere coordinates of points with <i>r</i>=0
	 * @param dst
	 * @param center
	 * @param r
	 * @return
	 */
	public static double[] getLieCoordinatesForSphere(double[] dst, double[] center, double r)	{
		double[] hcenter = null;
		// ideally center is a homogeneous vector with last coordinate 1; make it so
		if (center.length == 3) hcenter = Pn.homogenize(null, center);
		else if (center.length == 4) hcenter = Pn.dehomogenize(null, center);
		else throw new IllegalStateException("No valid center coordinates "+Rn.toString(center));
		if (dst == null) dst = new double[6];
		if (hcenter[3] != 1.0)
			throw new IllegalStateException("No valid homogeneous center coordinates "+Rn.toString(center));
		for (int i = 0; i<3; ++i) dst[i] = hcenter[i];
		dst[3] = r;
		dst[4] = Rn.innerProduct(hcenter, hcenter, 3) - r*r;
		dst[5] = 1.0;
		return dst;
	}
	
	/**
	 * Get Lie sphere coordinates for the plane consisting of all points 
	 * satisfying <i><normal,x> = offset.</i>. 
	 * Note: Positive <i>offset</i> means the plane is translated from the origin in the direction of <i>normal</i>;
	 * negative moves it in the opposite direction.
	 * <i>normal</i> can also be given as a direction in homogeneous coordinates,
	 * i.e. as an array of length 4 with last coordinate equal 0. 
	 * @param dst
	 * @param normal
	 * @param offset
	 * @return
	 */
	public static double[] getLieCoordinatesForPlane(double[] dst, double[] normal, double offset)	{
		double[] hnormal = null;
		// ideally normal is a homogeneous vector with last coordinate 0; make it so
		if (normal.length == 3) {
			hnormal = Pn.homogenize(null, normal);
			hnormal[3] = 0;
		}
		else if (normal.length == 4) hnormal = Pn.dehomogenize(null, normal);
		else throw new IllegalStateException("No valid normal direction "+Rn.toString(normal));
		if (hnormal[3] != 0.0)
			throw new IllegalStateException("No valid homogeneous normal direction "+Rn.toString(normal));
		if (dst == null) dst = new double[6];
		System.arraycopy(Rn.normalize(null, hnormal), 0, dst, 0, 3);
		dst[3] = 1.0;
		dst[4] = 2*offset;
		dst[5] = 0.0;
		return dst;
	}
	
	/**
	 * A contact element is uniquely determined by a point and a normal in this point.
	 * Both, <i>point</i> and <i>normal</i>, may be given in homogeneous coordinates. 
	 * @param dst
	 * @param point
	 * @param normal
	 * @return
	 */
	public static double[][] getContactElementForPointAndNormal(double[][] dst, double[] point, double[] normal) {
		double[] hpoint = null;
		double[] hnormal = null;
		
		// ideally point is a homogeneous vector with last coordinate 1; make it so
		if (point.length == 3) hpoint = Pn.homogenize(null, point);
		else if (point.length == 4) hpoint = Pn.dehomogenize(null, point);
		else throw new IllegalStateException("No valid point coordinates "+Rn.toString(point));	
		if (hpoint[3] != 1.0)
			throw new IllegalStateException("No valid homogeneous point coordinates "+Rn.toString(point));
		
		// ideally normal is a homogeneous vector with last coordinate 0; make it so
		if (normal.length == 3) {
			hnormal = Pn.homogenize(null, normal);
			hnormal[3] = 0;
		}
		else if (normal.length == 4) hnormal = Pn.dehomogenize(null, normal);
		else throw new IllegalStateException("No valid normal direction "+Rn.toString(normal));	
		if (hnormal[3] != 0.0)
			throw new IllegalStateException("No valid homogeneous normal direction "+Rn.toString(normal));
				
		if (dst == null) dst = new double[2][6];
		if (dst[0] == null) dst[0] = new double[6];
		if (dst[1] == null) dst[1] = new double[6];
		// For Lie coordinates of the point we have a method
		getLieCoordinatesForSphere(dst[0], hpoint, 0);
		// For the plane we need the offset
		double offset = Rn.innerProduct(hpoint, Rn.normalize(null, hnormal), 3);
		getLieCoordinatesForPlane(dst[1], hnormal, offset);
		return dst;
	}
	
	/**
	 * A contact element is uniquely determined by a proper sphere and a point on it.
	 * @param dst
	 * @param point - 3-vector (x,y,z)
	 * @param center - 3-vector (x,y,z)
	 * @param radius - absolute value has to be greater than 10E-8
	 * @return
	 */
	public static double[][] getContactElementForPointAndSphere(double[][] dst,
			double[] point, double[] center, double radius) {
		//TODO: reasonable adaption to homogeneous coordinates... -> sphere?
		//Therefore I did it strictly Euclidean
		if (point.length != 3)
			throw new IllegalStateException("No valid point "+Rn.toString(point));
		if (center.length != 3)
			throw new IllegalStateException("No valid center "+Rn.toString(center));
		if (Math.abs(radius) < tolerance)
			throw new IllegalStateException("radius smaller than tolerance, defines no proper sphere");
		if (dst == null) dst = new double[2][6];
		if (dst[0] == null) dst[0] = new double[6];
		if (dst[1] == null) dst[1] = new double[6];
		// We can apply our methods directly.
		getLieCoordinatesForSphere(dst[0], point, 0);
		getLieCoordinatesForSphere(dst[1], center, radius);
		return dst;
	}
	
}
