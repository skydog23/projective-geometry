/*
 * Created on Mar 14, 2007
 *
 */
package charlesgunn.jreality.geometry.projective;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Rectangle3D;
import de.jtem.projgeom.PlueckerLineGeometry;

public class LineUtility {

	public static double[][] twoPlanesOnLine(double[][] pts, double[] pluckerLine)	{
		if (pts == null) pts = new double[2][4];
		double[] m = PlueckerLineGeometry.lineToSkewMatrix(null, pluckerLine);
		Matrix mx = new Matrix(m);
		double[] row = null;
		int i = 0;
		double size = Rn.euclideanNormSquared(pluckerLine);
		for (; i<4; ++i)	{
			row = mx.getColumn(i);
			if (Rn.euclideanNormSquared(row) > size*10E-4) break;		
		}
		if (i==4) throw new IllegalStateException("Degenerate plucker line "+Rn.toString(pluckerLine));
		pts[0] = row;
		i++;
		for ( ; i<4; ++i)	{
			row = mx.getColumn(i);
			double[] pl = PlueckerLineGeometry.lineFromPoints(null, row, pts[0]);
			double ll = Rn.innerProduct(pl, pl);
			if (ll > size*10E-16) 
				if (Rn.euclideanNormSquared(row) > size*10E-4) break;	
		}
		if (i==4) 
			throw new IllegalStateException("Degenerate plucker line "+Rn.toString(pluckerLine));
		pts[1] = row;
		return pts;
	}
	public static double lineCoordOffset = 0.0;

	public static double[][] twoPointsOnLine(double[][] pts, double[] pluckerLine)	{
		return twoPlanesOnLine(pts, PlueckerLineGeometry.dualizeLine(null, pluckerLine));
	}

	public static double[][] ellipticSegment(double[][] verts, double[] pt0, double[] pt1, int num ) {
		if (verts == null) verts = new double[num][pt0.length];
		for (int i =0; i<num; ++i)		{
			double d = i/(num-1.0);
			Pn.linearInterpolation(verts[i], pt0, pt1, d, Pn.ELLIPTIC);
		}	
		return verts;
	}
	
	public static double[][] samplesOn1DExtent(double[][] verts, 
			int offset, 
			int numSegs, 
			double[] pt0, 
			double[] pt1, 
			boolean doubled) {
		boolean d3 = (pt0.length == 3);
		double[] pt04 = pt0, pt14 = pt1;
		if (d3) {
			pt04 = Pn.homogenize(null, pt0);
			pt14 = Pn.homogenize(null, pt1);
		}
		if (doubled)
			if ( (numSegs % 2) != 0)	{
				throw new IllegalArgumentException("Number of segments must be even");
			}
		if (verts == null) verts = new double[numSegs][4];
		int lim = doubled ? numSegs/2 : numSegs;
		double angle = (doubled ? 2 : 1) * Math.PI/numSegs;
		double begin = (-angle * (lim/2 + lineCoordOffset));
		double[] p0 =  Pn.normalize(null, pt04, Pn.ELLIPTIC);
		double[] p1 = Pn.normalize(null, pt14, Pn.ELLIPTIC);
		for (int i =0; i<lim; ++i)		{
			Pn.dragTowards(verts[offset+i], p0, p1, begin + i * angle, Pn.ELLIPTIC);
			if (doubled) Rn.times(verts[offset+lim+i],  -1.0, verts[offset+i]);
		}
		return verts; 
	}

	public static double[][] samplesOnLine(double[][] verts, 
			int numSegs,
			double[] line,
			boolean doubled) {
		double[][] points = twoPointsOnLine(null, line);
		return samplesOn1DExtent(verts, 0, numSegs, points[0], points[1], doubled);
	}
	
	public static double[] valueAtTime(double t, double[] p0, double[] p1)	{
		double rads = t * Math.PI;
		return Pn.dragTowards(null, p0, p1, rads, Pn.ELLIPTIC);
	}
	
	public static double[][] coordinatesFor1DExtent(double[][] verts, int offset, int numSegs, double[] pt0, double[] pt1) {
		return samplesOn1DExtent(verts, offset, numSegs, pt0, pt1, true);
	}
	
	public static SceneGraphComponent sceneGraphForPlane(SceneGraphComponent exists, double[] plane, double[] point, double scale)	{
		if (exists == null) exists = new SceneGraphComponent();
		double[] N = {plane[0], plane[1], plane[2]};
		if (Math.abs(Rn.innerProduct(plane, point)) > 10E-6 ) {
			throw new IllegalStateException("Point must lie on plane");
		}
		MatrixBuilder.euclidean().translate(point).rotateFromTo(new double[]{0,0,1}, plane).scale(scale).assignTo(exists);
		exists.setGeometry(Primitives.regularPolygon(20));
		return exists;
	}

	public static SceneGraphComponent sceneGraphForLine(SceneGraphComponent exists, double[] line, double[] point, double scale)	{
		return sceneGraphForLine(exists, line, point, scale, false);
	}
	public static SceneGraphComponent sceneGraphForLine(SceneGraphComponent exists, double[] line, double[] point, double scale, boolean finite)	{
		if (exists == null) exists = new SceneGraphComponent();
		PointRangeFactory prf = new PointRangeFactory();
		prf.setPluckerLine(line);
		if (point == null) point = new double[4];
		prf.setCenter(point);
		prf.setSphereRadius(scale);
		prf.setFiniteSphere(finite);
		prf.update();
		exists.setGeometry(prf.getLine());
		prf.getLine().setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		return exists;
	}
	static SceneGraphComponent hack1 = new SceneGraphComponent(),
		hack2 = new SceneGraphComponent(),
		hack3 = new SceneGraphComponent();
	public static double[][] lineIntersectSphere(SceneGraphComponent hack, double[] line, double[] center, double rad) {
//		double[][] q = LineUtility.twoPointsOnLine(null, line);
		if (center.length == 3) center = Pn.homogenize(null, center);
		double[] ct = Rn.subtract(null, center, new double[]{0,0,0,1});
		// intersect line with sphere
		// p0 + t(p1-p0)  intersect |P| = r
		double[][] q = new double[2][];
		double[] plane = LineUtility.directionOfLine(null, line);
		plane[3] = 0;
		q[0] = PlueckerLineGeometry.lineIntersectPlane(null, line, plane);
		plane[3] = 1;
		q[1] = PlueckerLineGeometry.lineIntersectPlane(null, line, plane);
		Pn.dehomogenize(q[0], q[0]);
		Pn.dehomogenize(q[1], q[1]);
		double[] v0 = q[0];
		double[] v = null;
		if (q[0][3] == 0.0) {
			v0 = q[1];
			v =  q[0];
		} else if (q[1][3] == 0.0) {
			v = q[1];
		}
		else v = Rn.subtract(null, q[1], q[0]);
		Rn.subtract(v0, v0, ct);
		// solve for solutions of q1 + tq0 intersect sphere with radius r1
		// that is, t^2<q0,q0>+2t<q0,q1>+<q1,q1>-r1*r1=0
		double a = Rn.innerProduct(v, v);
		double b = 2 * Pn.innerProduct(v0, v, Pn.EUCLIDEAN);
		double c = Pn.innerProduct(v0, v0, Pn.EUCLIDEAN) - rad*rad;
		double d = b*b-4*a*c;
		double[] p1, p2;
		double t1, t2;
		if (d < 0)	{
			// return closest point on sphere to line
			// first find plane through center perpendicular to direction of line
			p1 = LineUtility.directionOfLine(null, line);
			double[] plane0 = {p1[0], p1[1], p1[2], -Rn.innerProduct(p1, center,3)};
			double[] plane1 = PlueckerLineGeometry.lineJoinPoint(null, line, center);
			double[] linem = PlueckerLineGeometry.lineFromPlanes(null, plane0, plane1);
			if (hack != null && hack.getChildComponentCount() == 0)	
				hack.addChildren(hack1, hack2, hack3);
			sceneGraphForLine(hack1, linem, null, 4.0);
			sceneGraphForPlane(hack2, plane0, center, 1.0);
			sceneGraphForPlane(hack3, plane1, center, 1.0);
			p1 = LineUtility.directionOfLine(null, linem);
			Rn.setToLength(p1, p1, rad);
			p2 = Rn.times(null, -1, p1);
//			Rn.add(p1, p1, ct);
			Rn.add(p2, p2, ct);
			p1 = p2;
		} else {
			double disc = Math.sqrt(d);
			t1 = (-b + disc)/(2*a);
			t2 = (-b - disc)/(2*a);
			p1 = Rn.add(null, Rn.linearCombination(null, 1, v0, t1, v), ct);
			p2 = Rn.add(null,Rn.linearCombination(null, 1, v0, t2, v), ct);
		}
		return new double[][]{p1,p2};
	}

	private static double[] directionOfLine(double[] dst, double[] line) {
		if (dst == null) dst = new double[4];
		dst[0] = line[2];
		dst[1] = -line[4];
		dst[2] = line[5];
		return dst;
	}
}
