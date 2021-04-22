package charlesgunn.math;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jtem.projgeom.PlueckerLineGeometry;

public class BiquaternionUtility {

	public static double[] projectPointOntoLine(double[] dst, double[] point, Biquaternion line)	{
		if (!Biquaternion.isLine(line))
			throw new IllegalArgumentException("Must be a line");
		Biquaternion polar = Biquaternion.polarize(null, line);
		double[] plucker = Biquaternion.lineComplexFromBivector(null, polar);
		double[] plane = PlueckerLineGeometry.lineJoinPoint(null, plucker, point);
//		System.err.println("point = "+Rn.toString(point));
//		System.err.println("polar line = "+Rn.toString( plucker));
//		System.err.println("plane = "+Rn.toString(plane));
		dst = PlueckerLineGeometry.lineIntersectPlane(dst, Biquaternion.lineComplexFromBivector(null, line),  plane);
		return dst;
	}
	
	public static double[][] lineSegmentCenteredOnPoint(
			double[][] dst, 
			Biquaternion line, 
			double[] point, 
			double length) {
		if (dst == null) dst = new double[2][4];
		int metric = line.getMetric().getInteger();
		double[] polarPlane = Pn.polarizePoint(null, point, metric);
		double[] point2 = PlueckerLineGeometry.lineIntersectPlane(null, 
				line.lineComplexFromBivector(null, line), 
				polarPlane);
		Pn.dragTowards(dst[0], point, point2, length, metric);
		Pn.dragTowards(dst[1], point, Rn.times(point2, -1, point2), length, metric);
		return dst;
	}
	
	public static double[] diagonalMatrixTimesVector(double[] dst, double[] m, double[] src)	{
		if (dst == null) dst = new double[src.length];
		int n = m.length;
		if (src.length*src.length != n) 
			throw new IllegalArgumentException("Wrong dimensions.");
		n = dst.length;
		for (int i = 0; i<n; ++i) dst[i] = m[i*(n+1)] * src[i];
		return dst;
	}
}
