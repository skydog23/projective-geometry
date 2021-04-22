/*
 * Created on Jan 19, 2011
 *
 */
package charlesgunn.math;

import java.util.logging.Level;

import charlesgunn.math.Biquaternion.Metric;
import charlesgunn.math.p5.P5;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P2;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import junit.framework.Assert;
import junit.framework.TestCase;

public class TestForStefan extends TestCase {
	Level logLevel = Level.INFO;
	
	public void testHyperbolic()	{
		// create an isometry which translates and rotates around the x-axis
		double[] p0 = {.5,0,0,1};
		double[] tlate = MatrixBuilder.hyperbolic().translate(p0).rotateX(Math.PI/4).getArray();
		double d = Pn.distanceBetween(P3.originP3, p0, Pn.HYPERBOLIC);
		P5.logger.log(logLevel,"angle = "+Math.PI/4+"dist = "+d);
		IsometryAxis ia = new IsometryAxis(tlate,Metric.HYPERBOLIC);
		Biquaternion angle = ia.getAngle();
		P5.logger.log(logLevel,"angle = "+2*angle.qr.re+"translation= "+2*angle.qd.re);
		Assert.assertEquals(d, 2*angle.qd.re, 10E-10);
		Assert.assertEquals(Math.PI/4, 2*angle.qr.re, 10E-10);
	}

	public void testMakeFrame()	{
		double[] p0 = {0,0,1};
		double[] p1 = {1,0,1};
		double[] q0 = {0,1,1};
		double[] q1 = {1,1,1};
		double[] a = P2.makeDirectIsometryFromFrames(null, p0, p1, q0, q1,
		Pn.EUCLIDEAN);
		System.out.println("det: " + Rn.determinant(a));
		double[] checkQ0 = Rn.matrixTimesVector(null, a, p0);
		double[] checkQ1 = Rn.matrixTimesVector(null, a, p1);
		System.out.println(Rn.toString(checkQ0) + " == " + Rn.toString(q0));
		System.out.println(Rn.toString(checkQ1) + " == " + Rn.toString(q1));

	}
}
