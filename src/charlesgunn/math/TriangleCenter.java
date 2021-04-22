package charlesgunn.math;

import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jtem.projgeom.PlueckerLineGeometry;

public class TriangleCenter {

	public static double[] triangleCenter(double[] dst, double[] p0, double[] p1, double[] p2, int metric)	{
		if (dst == null) dst = new double[4];
		// calculate two mid points
		double[] m01 = Pn.linearInterpolation(null, p0, p1, .5, metric);
		double[] m12 = Pn.linearInterpolation(null, p1, p2, .5, metric);
		// calculate the joining line
		double[] p01 = PlueckerLineGeometry.lineFromPoints(null, p0, p1);
		double[] p12 = PlueckerLineGeometry.lineFromPoints(null, p1, p2);
		// calculate polar lines
		double[] pp01 = PlueckerLineGeometry.polarize(null, p01, metric);
		double[] pp12 = PlueckerLineGeometry.polarize(null, p12, metric);
		// calculate perpendicular bisector plane for the two sides
		double[] pl1 = PlueckerLineGeometry.lineJoinPoint(null, pp01, m01);
		double[] pl2 = PlueckerLineGeometry.lineJoinPoint(null, pp12, m12);
		// get plane equation for the triangle
		double[] pl3 = P3.planeFromPoints(null, p0, p1, p2);
		// now find intersection of three planes
		dst = P3.pointFromPlanes(dst, pl1, pl2, pl3);
		Pn.dehomogenize(dst, dst);
		double[] dist = new double[3];
			dist[0] = Pn.distanceBetween(dst, p0, metric);
			dist[1] = Pn.distanceBetween(dst, p1, metric);
			dist[2] = Pn.distanceBetween(dst, p2, metric);
			System.err.println("distances = "+Rn.toString(dist));
		return dst;
	}
}
