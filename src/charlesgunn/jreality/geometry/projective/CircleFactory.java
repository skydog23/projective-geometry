package charlesgunn.jreality.geometry.projective;

import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

/**
 * This factory handles representation of circles in moebius geometry, in particular, under
 * stereographic projection of z=0 onto the unit sphere from the north pole.  You can provide either
 * the circle coordinates in the plane z==0, as the vector 
 * 			(cx, cy, m-r*r, 1.0) for real circles with center (cx, cy) and radius r (m:=cx*cx+cy*cy)  OR
 * 			(a, b, -2*c, 0) for lines ax+by+c = 0 
 * Then under stereographic projection these circle coordinates are mapped to circles on the unit sphere;
 * each such circle is uniquely identified by the plane it lies in [p, q, r, s] (i.e. the points satisfying px+qy+rz+s=0).
 * This transformation to plane coordinates is linear in the circle coordinates above.
*/
public class CircleFactory {

	double[] pE = {1,0,0,-.5};
	double[] cc = new double[4];
	boolean definedOnSphere = true,
		changed  = true,
		updateSphereGeometry = true;
	static double tolerance = 10E-10;
	static double[] northPole = {0,0,1,1}, np3 = {0,0,1};
	static int numSamples = 100;
	SceneGraphComponent both, sphereSGC, planeSGC;
	static IndexedLineSet urCircle = IndexedLineSetUtility.circle(numSamples),
		hiresCircle = IndexedLineSetUtility.circle(numSamples*numSamples);		// TODO: resolution dependent
	static {
		urCircle.setVertexAttributes(Attribute.NORMALS, urCircle.getVertexAttributes(Attribute.COORDINATES));
		hiresCircle.setVertexAttributes(Attribute.NORMALS, hiresCircle.getVertexAttributes(Attribute.COORDINATES));
	}
	
	public CircleFactory()	{
		super();
		both = SceneGraphUtility.createFullSceneGraphComponent("both");
		sphereSGC = SceneGraphUtility.createFullSceneGraphComponent("sphere");
		planeSGC = SceneGraphUtility.createFullSceneGraphComponent("plane");
		both.addChildren(sphereSGC, planeSGC);
		sphereSGC.setGeometry(urCircle);
		planeSGC.setGeometry(urCircle);
		update();
	}
	
	public static CircleFactory circleFactoryForPlane(double[] p)	{
		CircleFactory ret = new CircleFactory();
		ret.setPlaneEquation(p);
		ret.update();
		return ret;
	}

	public static CircleFactory circleFactoryForCircle(double[] c)	{
		CircleFactory ret = new CircleFactory();
		ret.setCircleCoordinates(c);
		ret.update();
		return ret;
	}
	
	public void setPlaneEquation(double[] p)	{
		if (p.length != 4)	
			throw new IllegalArgumentException("bad length");
		pE = p.clone();
		double d = Rn.innerProduct(pE, pE, 3);
		if (d!=0)	Rn.times(pE, 1.0/Math.sqrt(d), pE);
		definedOnSphere = true;
		changed = true;
	}
	
	public double[] getPlaneEquation()	{ return pE; }
	public double[] getCircleCoordinates() { return cc; }
	
	public void setCircleCoordinates(double[] c)	{
		if (c.length != 4)	
			throw new IllegalArgumentException("bad length");
		cc = c.clone();
		definedOnSphere = false;
		changed = true;
	}
	
	public SceneGraphComponent getSceneGraphComponent()	{
		return both;
	}
	public SceneGraphComponent getSphereSGC()	{
		return sphereSGC;
	}
	public SceneGraphComponent getPlaneSGC()	{
		return planeSGC;
	}
	
	// this matrix acts on circle coordinates in the plane to give plane equations 
	// in R3 identifying the circle on the sphere
	private static double[] stereoProjMatrix = {
		1,0,0,0,
		0,1,0,0,
		0,0,.5,-.5,
		0,0,-.5,-.5
	}, // and this matrix acts on plane coordinates to give circles in the plane
	stereoProjInverseMatrix = Rn.inverse(null, stereoProjMatrix);
	public void update()	{
		if (!changed) return;
		if (definedOnSphere)	{
			double diff = -(pE[2]+pE[3]);
			Rn.matrixTimesVector(cc, stereoProjInverseMatrix, pE);
			if (Math.abs(diff) < tolerance)	{		// line
				double e = Rn.innerProduct(pE, pE, 2);
				if (e!=0) {	
					e = 1.0/Math.sqrt(e);
					// normalize the coordinates so the (x,y) pair has length 1
					cc[0] *= e;
					cc[1] *= e;
					cc[2] *= e*e;	
					cc[3] = 0.0;
			} else 
					throw new IllegalArgumentException("north pole isn't a circle");
			} else {		// a real circle
				Pn.dehomogenize(cc, cc);
			}
		} else {
				Rn.matrixTimesVector(pE, stereoProjMatrix, cc);
//				System.err.println("circle "+Rn.toString(cc));
//				System.err.println("line "+Rn.toString(pE));
		}
		// now update the transformations
		setSphereCircleGeometry();
		setPlaneCircleGeometry();
		changed = false;
	}
	private static final double[] xyPlane = {0,0,1,0};
	private void setSphereCircleGeometry() {
		// normalize the plane coordinates so last coord is the distance from origin
		double scale = Math.sqrt(Rn.innerProduct(pE, pE, 3));
		Rn.times(pE, 1.0/scale, pE);
		System.err.println("Plane equation = "+Rn.toString(pE));
		double r = Math.sqrt((1-pE[3]*pE[3]));
		double[] n = new double[]{pE[0],pE[1],pE[2]};
		System.err.println("r = "+r);
		MatrixBuilder.euclidean().rotateFromTo(np3, n).translate(0,0,-pE[3]).scale(r).assignTo(sphereSGC);
//		double[] rotLine = PlueckerLineGeometry.plueckerLineFromPlanes(null, pE, xyPlane);
//		double[] polarLine = PlueckerLineGeometry.polarize(null, rotLine, Pn.HYPERBOLIC);
//		double[] p1 = PlueckerLineGeometry.plueckerLineIntersectPlane(null, polarLine, xyPlane);
//		double[] p2 = PlueckerLineGeometry.plueckerLineIntersectPlane(null, polarLine, pE);
//		
//		MatrixBuilder.hyperbolic().translate(p1, p2).assignTo(sphereSGC);
//		sphereSGC.setGeometry(IndexedLineSetUtility.circle(numSamples, 0,0,r));
	}

	private void setPlaneCircleGeometry() {
		double r;
		if (Math.abs(cc[3]) < tolerance)	{ // line
			double[] plucker = new double[]{cc[2]*.5, 0, cc[1], 0, -cc[0], 0};
			double[][] pts = LineUtility.lineIntersectSphere(null, plucker, P3.originP3, 50.0);
			IndexedLineSet seg = IndexedLineSetUtility.createCurveFromPoints(pts, false);
			MatrixBuilder.euclidean().assignTo(planeSGC);
			planeSGC.setGeometry(seg);
		} else {
			double[] center = new double[]{cc[0],cc[1],0};
			r = Math.sqrt(-cc[2] + cc[0]*cc[0] + cc[1]*cc[1]);
			int samples = (r > 20 ) ? 20 : ((int) r) +1;
			Geometry resoCircle = IndexedLineSetUtility.circle(samples *numSamples, cc[0], cc[1], r); //(r > 25) ? hiresCircle : urCircle;
			//if (planeSGC.getGeometry() != resoCircle) 
			planeSGC.setGeometry(resoCircle);
			//MatrixBuilder.euclidean().translate(center).scale(r).assignTo(planeSGC);			
//			//planeSGC.setGeometry(IndexedLineSetUtility.circle(numSamples, cc[0], cc[1], r));
		}
	}

	public static SceneGraphComponent getSphereCircles(int numLat, int numLong) {
			SceneGraphComponent circlesSGC = SceneGraphUtility.createFullSceneGraphComponent(" circles");
	//		circlesSGC.getAppearance().setAttribute(CommonAttributes.LINE_WIDTH, 1.0);
			CircleFactory lats[] = new CircleFactory[numLat], longs[] = new CircleFactory[numLong];
			for (int i = 0; i<numLat; ++i)	{
				lats[i] = new CircleFactory();
				double angle = (Math.PI*i)/(numLat);
				lats[i].setCircleCoordinates(new double[]{Math.cos(angle), Math.sin(angle), 0, 0});
				lats[i].update();
				circlesSGC.addChild(lats[i].getSphereSGC());
			}
			for (int i = 0; i<numLong; ++i)	{
				longs[i] = new CircleFactory();
				double angle = (Math.PI*i)/(numLong) - .001+Math.PI/2;
				longs[i].setPlaneEquation(new double[]{0,0,1,-Math.sin(angle)});
				longs[i].update();
				circlesSGC.addChild(longs[i].getSphereSGC());
			}
			return circlesSGC;
		}

	public static SceneGraphComponent getPlaneCircles(int numLat, int numLong) {
		SceneGraphComponent circlesSGC = SceneGraphUtility.createFullSceneGraphComponent(" circles");
//		circlesSGC.getAppearance().setAttribute(CommonAttributes.LINE_WIDTH, 1.0);
		CircleFactory lats[] = new CircleFactory[numLat], longs[] = new CircleFactory[numLong];
		for (int i = 0; i<numLat; ++i)	{
			lats[i] = new CircleFactory();
			double angle = (Math.PI*i)/(numLat);
			lats[i].setCircleCoordinates(new double[]{Math.cos(angle), Math.sin(angle), 0, 0});
			lats[i].update();
			circlesSGC.addChild(lats[i].getPlaneSGC());
		}
		for (int i = 0; i<numLong; ++i)	{
			longs[i] = new CircleFactory();
			double angle = (Math.PI*i)/(numLong) - .001+Math.PI/2;
			longs[i].setPlaneEquation(new double[]{0,0,1,-Math.sin(angle)});
			longs[i].update();
			circlesSGC.addChild(longs[i].getPlaneSGC());
		}
		return circlesSGC;
	}


	public static double[] stereoProj(double[] dst, double[] v)	{
		return stereoProj(dst, v[0], v[1], v[2]);
	}
	/**
	 * Stereo-project from NP of sphere (0,0,1) onto plane z=0.
	 * @param dst
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static double[] stereoProj(double[] dst, double x, double y, double z) 	{
		if (dst == null) dst = new double[2];
		double t1 = 1-z;
		// handle the case that the point is not near the north pole first
		if (t1 > 10E-10) {
			t1 = 1.0/t1;
		} else {
			t1 = 10E10;
			if (x==0 && y == 0) {dst[0] = 10E10; dst[1] = 0; return dst; }
		}
		dst[0] = t1*x;
		dst[1] = t1*y;
		if (dst.length > 2) dst[2] = 0.0;
		return dst;
	}
	
	/**
	 * Generate the inverse stereo projection from the point (0,0,1) mapping the plane z=0 
	 * to the unit sphere.
	 * @param dst
	 * @param x
	 * @param y
	 * @return
	 */
	public static double[] inverseStereoProj(double[] dst, double x, double y) 	{
		double t1 = x*x+y*y+1;
		if (dst == null) dst = new double[3];
		dst[0] = 2*x/t1;
		dst[1] = 2*y/t1;
		dst[2] = (t1-2)/t1;
		return dst;
	}
//	CircleFactory one = null;
//	int count = 0;
//	for (int i = 1; i<=samples; ++i)	{
//		double t = (i-samples/2.)/(.5*samples);
//		originalCircles[count] = CircleFactory.circleFactoryForPlane(new double[]{0,0,1,t});
//		circles[count]= one = CircleFactory.circleFactoryForPlane(new double[]{0,0,1,t});
//		count++;
//		planeCircles.addChild(one.getPlaneSGC());	
//		sphereCircles.addChild(one.getSphereSGC());	
//		double angle = i * (Math.PI/(samples));
//		double c = Math.cos(angle);
//		double s = Math.sin(angle);
//		originalCircles[count] = CircleFactory.circleFactoryForPlane(new double[]{s,c,0,0});
//		circles[count]= one = CircleFactory.circleFactoryForPlane(new double[]{s,c,0,0});
//		count++;
//		planeCircles.addChild(one.getPlaneSGC());	
//		sphereCircles.addChild(one.getSphereSGC());	
//	}

}
