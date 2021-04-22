package charlesgunn.math;

import charlesgunn.math.Biquaternion.Metric;
import charlesgunn.math.p5.PlueckerLineGeometry;
import de.jreality.math.Rn;
import junit.framework.TestCase;

public class TestBiquaternionUtility extends TestCase {

	public void testProjectPointOntoLine() {
		double[] point = {1,2,3,4}, pt0 = {1,1,0,0}, pt1 = {0,1,0,0};
//		double[] point = {1,0,0,0}, pt0 = {1,1,0,0}, pt1 = {0,1,0,0};
		double[] plucker = PlueckerLineGeometry.lineFromPoints(null, pt0, pt1);
		for (Metric m : Metric.values()) {
			Biquaternion bq = new Biquaternion(plucker, m);	
			double[] point2 = BiquaternionUtility.projectPointOntoLine(null, point, bq);
			System.err.println("Point2 = "+Rn.toString(point2));
		}
	}

	public void testLineIntersectLine()	{
		Metric metric = Metric.ELLIPTIC;
		double[] p0 = {1,2,3,4}, p1 = {2,2,3,4}, p2 = {1,3,3,4};
		double[] l0pl = PlueckerLineGeometry.lineFromPoints(null, p0, p1);
		double[] l1pl = PlueckerLineGeometry.lineFromPoints(null, p0, p2);
		l1pl = PlueckerLineGeometry.dualizeLine(null, l1pl);
		Biquaternion l0 = new Biquaternion(l0pl, metric);
//		l0 = Biquaternion.polarize(null, l0);
		Biquaternion l1 = new Biquaternion(l1pl, metric);
		Biquaternion l2 = Biquaternion.commutator(null, l0, l1);
		System.err.println("l2 = "+l2);
	}
	
	public void testLineSegmentCenteredOnPoint()	{
		Metric metric = Metric.ELLIPTIC;
		double[] p0 = {1,2,3,4}, p1 = {2,2,3,4}, p2 = {1,3,3,4};
		double[] l0pl = PlueckerLineGeometry.lineFromPoints(null, p0, p1);
		double[] l1pl = PlueckerLineGeometry.lineFromPoints(null, p0, p2);
		l1pl = PlueckerLineGeometry.dualizeLine(null, l1pl);
		Biquaternion l0 = new Biquaternion(l0pl, metric);
//		l0 = Biquaternion.polarize(null, l0);
		Biquaternion l1 = new Biquaternion(l1pl, metric);
		Biquaternion l2 = Biquaternion.commutator(null, l0, l1);
		System.err.println("l2 = "+l2);
	}

}
