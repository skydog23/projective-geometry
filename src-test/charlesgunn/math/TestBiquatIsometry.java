/*
 * Created on Apr 18, 2009
 *
 */
package charlesgunn.math;

import java.util.logging.Level;

import charlesgunn.math.Biquaternion.Metric;
import charlesgunn.math.p5.P5;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import junit.framework.Assert;
import junit.framework.TestCase;


public class TestBiquatIsometry extends TestCase {
	Level logLevel = Level.INFO;
	
	public void testHyperbolic()	{
		double[] p0 = {.5,0,0,1};
		double[] tlate = MatrixBuilder.hyperbolic().translate(p0).rotateX(Math.PI/4).getArray();
		double d = Pn.distanceBetween(P3.originP3, p0, Pn.HYPERBOLIC);
		P5.logger.log(logLevel,"angle = "+Math.PI/4+"dist = "+d);
		IsometryAxis ia = new IsometryAxis(tlate,Metric.HYPERBOLIC);
		Biquaternion angle = ia.getAngle();
		P5.logger.log(logLevel,"angle = "+2*angle.qr.re+"translation= "+2*angle.qd.re);
		double[] tlate2 = Biquaternion.matrixFromBiquaternion(null, ia.getIsometry());
		P5.logger.log(logLevel,"matrix = "+Rn.matrixToString(tlate2));
		Assert.assertEquals(d, 2*angle.qd.re, 10E-10);
		Assert.assertEquals(Math.PI/4, 2*angle.qr.re, 10E-10);
		double diff = Rn.manhattanNorm(Rn.subtract(null, tlate, tlate2));
		Assert.assertEquals(diff, 0.0, 10E-10);
	}
	public void testEuclidean()	{
		double[] tlate = MatrixBuilder.euclidean().translate(1,0,0).getArray();
		Biquaternion axis = Biquaternion.biquaternionFromDirectIsometry(null, tlate, Biquaternion.Metric.EUCLIDEAN);
		P5.logger.log(logLevel,"biq = "+axis.toString());
		IsometryAxis ia = new IsometryAxis(axis);
		double[] tlate2 = Biquaternion.matrixFromBiquaternion(null, axis);
		P5.logger.log(logLevel,"matrix = "+Rn.matrixToString(tlate2));
		
		tlate = MatrixBuilder.euclidean().translate(1,0,0).rotateX(1).getArray();
		axis = Biquaternion.biquaternionFromDirectIsometry(null, tlate, Biquaternion.Metric.EUCLIDEAN);
		P5.logger.log(logLevel,"biq = "+axis.toString());
		ia = new IsometryAxis(axis);
		tlate2 = Biquaternion.matrixFromBiquaternion(null, axis);
		P5.logger.log(logLevel,"matrix = "+Rn.matrixToString(tlate2));
		
		tlate = MatrixBuilder.euclidean().translate(-1,-1,-1).rotate(2*Math.PI/3.0, new double[]{1,1,1}).getArray();
		 axis = Biquaternion.biquaternionFromDirectIsometry(null, tlate, Biquaternion.Metric.EUCLIDEAN);
		P5.logger.log(logLevel,"biq = "+axis.toString());
		 ia = new IsometryAxis(axis);
		tlate2 = Biquaternion.matrixFromBiquaternion(null, axis);
		P5.logger.log(logLevel,"matrix = "+Rn.matrixToString(tlate2));
	}
//		P5.logger.log(logLevel,"testEucTlate");
////		Biquaternion euct = new Biquaternion(new double[]{1,0,0,0,0,1,0,0},Metric.EUCLIDEAN);
//		Matrix foo = new Matrix();
////		Metric m = Metric.ELLIPTIC;
//		for (Metric m : Metric.values()) {
//			for (int i = 0; i<20; ++i)	{
//				double[] axis = {Math.random()-.5, Math.random()-.5, Math.random()-.5};
//				double[] tlate = {Math.random()-.5, Math.random()-.5, Math.random()-.5};
//				MatrixBuilder.init(null, m.getInteger()).translate(tlate).rotate(Math.random(), axis).assignTo(foo);
//	//			P5.logger.log(logLevel,"matrix before = \n"+Rn.matrixToString(foo.getArray()));
//				Biquaternion biq = Biquaternion.biquaternionFromIsometry(null, foo.getArray(), m);
//				double[] mat = Biquaternion.matrixFromBiquaternion(null, biq);
//				if (Rn.manhattanNorm( Rn.subtract(null, foo.getArray(), mat)) > 10E-8)
//					throw new IllegalStateException("not equal!");
//	//			P5.logger.log(logLevel,"matrix after = \n"+Rn.matrixToString(mat));
//				IsometryAxis isom = new IsometryAxis(biq);	
//			}
//		}
//	}
	double c222[] =  {-1.00000,	0,0,0,
			0,	-1.00000,	0,	2.00000,
			0,	0,	1.00000,	2.00000,
			0.00000,	0.00000,	0.00000,	1.00000
			};
	double c22[] =  {1.00000,	2.85591e-31,	1.44329e-15,	-2.00000,
			1.08839e-31,	-1.00000,	1.22465e-16,	1.20980e-15,
			1.44329e-15,	-1.22465e-16,	-1.00000,	2.00000,
			0.00000,	0.00000,	0.00000,	1.00000
			};
	public void testc22()	{
		P5.logger.log(logLevel,"testc22");
		
		Biquaternion biq = Biquaternion.biquaternionFromDirectIsometry(null, c22, Metric.EUCLIDEAN);
		IsometryAxis isom = new IsometryAxis(biq);
		for (double t = 0; t <= 1.0; t+= 1)	{
			biq = isom.exp(t);
			P5.logger.log(logLevel,"t = "+t+"\n"+biq);
			P5.logger.log(logLevel,"mat = "+Rn.matrixToString(Biquaternion.matrixFromBiquaternion(null, biq)));
		}
		
	}
	// following matrix is similar to a euclidean transformation: has no well-defined axis,
	// a whole set of clifford parallels are mapped to themselves by the transformation
	double[] el1202 = {0.809017,	-0.309017,	-0.500000,	0,
			0.309017,	0.809017,	0,	0.500000,
			0.500000,	0.00000,	0.809017,	-0.309017,
			0,	-0.500000,	0.309017,	0.809017};
	double[] el120 =  {0.592374,	-0.632976,	0.476375,	-0.146635,
			0.632976,	0.445738,	0.00000,	0.632976,
			-0.476375,	0.00000,	0.739009,	0.476375,
			-0.146635,	-0.632976,	-0.476375,	0.592374
			};
	public void test120cell()	{
		P5.logger.log(logLevel,"test120Cell");
		
		Biquaternion biq = Biquaternion.biquaternionFromDirectIsometry(null, el1202, Metric.ELLIPTIC);
		IsometryAxis isom = new IsometryAxis(biq);
		for (double t = 0; t <= 1.0; t+= .25)	{
			biq = isom.exp(t);
			P5.logger.log(logLevel,"t = "+t+"\n"+biq);
			P5.logger.log(logLevel,"mat = "+Rn.matrixToString(Biquaternion.matrixFromBiquaternion(null, biq)));
		}
		
	}
	
	public void testScrewMotion() {
		double[] p0 = {0,0,0,1}, p1 = {0,0,1,1};
		double[] screw = P3.makeScrewMotionMatrix(null, p0, p1, Math.PI/4, Pn.EUCLIDEAN);
		Biquaternion bq = Biquaternion.biquaternionFromDirectIsometry(null, screw, Biquaternion.Metric.EUCLIDEAN);
		IsometryAxis ia = new IsometryAxis(bq);
		P5.logger.log(logLevel,"axis = "+ia.axis.toString());
	}
	
	public void testHypPara()	{
		Biquaternion rotor = new Biquaternion(new double[]{0,0,1,0,-1,0}, Metric.HYPERBOLIC);
		rotor.qr.re = 1.0; 
		double[] mm = Biquaternion.matrixFromBiquaternion(null, rotor);
		P5.logger.log(logLevel,"matrix = "+Rn.matrixToJavaString(mm));
	
	}

}
