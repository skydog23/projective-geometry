/*
 * Created on Apr 17, 2009
 *
 */
package charlesgunn.math;

import java.util.logging.Level;

import charlesgunn.math.Biquaternion.Metric;
import de.jreality.math.Pn;
import de.jreality.math.Quaternion;
import de.jreality.math.Rn;

public class IsometryAxis2 {

	Biquaternion 
		isometry, 	// the isometry, normalized to have norm 1
		axis,		// the axis of the isometry: a bivector
		angle;		// the "angle": a biscalar satisfying exp(angle*axis) = isometry.  
					// thus, the logarithm of isometry is the product angle*axis
	Metric metric;
	
	boolean isTranslation = false, 	
		isRightTranslation = false,
		isIdentity = false,
		fromLog = false;
	
	double[] matrix = new double[16];

	private static Level logLevel = Level.FINE;
	
	public IsometryAxis2(double[] matrix, Metric m)	{
		this(Biquaternion.biquaternionFromDirectIsometry(null, matrix, m));
	}
	
	public IsometryAxis2(Biquaternion isom)	{
		isometry = isom;
		metric = isometry.metric;
		init();
	}
	
	public IsometryAxis2(Biquaternion axis, Biquaternion angle, Metric m) {
		this.axis = axis;
		this.angle = angle;
		this.metric = m;
		fromLog = true;
	}
	
	public Biquaternion getIsometry()	{
		return isometry;
	}
	
	public Biquaternion getAxis()	{
		return axis;
	}
	
	public Biquaternion getAngle() {
		return angle;
	}
	
	public double[] getAngles()	{
		double[] angles = new double[2];
		switch (metric)	{
		case EUCLIDEAN:
			break;
		case HYPERBOLIC:
			break;
		case ELLIPTIC:
			break;
		}
		return angles;
	}
	public static Biquaternion kplus = new Biquaternion(.5, .5, Metric.ELLIPTIC),
	kminus = new Biquaternion(.5, -.5, Metric.ELLIPTIC);

	private boolean isInvalidVersor(Biquaternion biq)	{
		Biquaternion nm = Biquaternion.norm(null, isometry);
		return (!Biquaternion.isBiscalar(nm) || (Biquaternion.isInvertible(nm) && 
				Math.abs(nm.getRealPart().re) > 10E-8 && 
				Math.abs(nm.getDualPart().re) > 10E-8) );
	}
		
	private void init()	{
		if (fromLog) return;
		if (isInvalidVersor(isometry))
			throw new IllegalStateException("Not an isometry: norm is invertible but has nonzero dual part");
		double theta1 = 0, theta2 = 0;
		if (isometry.getMetric() == Metric.ELLIPTIC) 
			if (isometry.qr.re < 0) Biquaternion.times(isometry, -1, isometry);
		Biquaternion.matrixFromBiquaternion(matrix, isometry);
		// pull off the bivector part and find its axis
		Biquaternion bivector = Biquaternion.bivector(null, isometry);
		axis = new Biquaternion(isometry.getMetric());
		angle = new Biquaternion(isometry.getMetric());
		axis = Biquaternion.normalize(axis, angle, bivector);
		if (Biquaternion.isBiscalar(isometry))	{
			isIdentity = true;
			return;
		}
		if (!Biquaternion.isBiscalar(angle))
			throw new IllegalStateException("not a biscalar!"+angle);
		Biquaternion nm = Biquaternion.norm(null, axis);
		nm = Biquaternion.norm(null, nm);
		if (!Biquaternion.isInvertible(isometry))  {
			// vanishing norm means its a translation 
			Biquaternion.logger.log(logLevel, "It's a translation ");
			isTranslation = true;
			switch (metric)	{
			case ELLIPTIC:
				// a clifford translation has no well-defined axis
				// (a euclidean translation does however! -- the line at infinity carrying the translation)
				if (Biquaternion.isLine(axis))
					throw new IllegalStateException("elliptic translation has no axis");
				isRightTranslation = Quaternion.innerProduct(bivector.qr, bivector.qd) < 0;
				axis = new Biquaternion(Quaternion.normalize(axis.qr,axis.qr), new Quaternion(0,0,0,0), Metric.ELLIPTIC);
				Biquaternion.logger.log(logLevel,"axis  = "+axis);
				
				Biquaternion.logger.log(logLevel,"translation is right = "+isRightTranslation);
				double euclideanNorm = Rn.euclideanNorm(Quaternion.IJK(null, isometry.qr));
				double length = Quaternion.length(isometry.qr);
				Biquaternion.logger.log(logLevel,"length = "+length);
				theta1 = Math.atan2( euclideanNorm, isometry.qr.re);
				Biquaternion.times(isometry, 1.0/length, isometry);
				break;
			case EUCLIDEAN:
				Biquaternion.logger.log(logLevel,"euclidean translation");
				break;
			case HYPERBOLIC:
				throw new IllegalStateException("There are no hyperbolic translations");
			}
		}
		else {
			// check that the factorization is correct
			Biquaternion.logger.log(logLevel,"axis = "+axis);
			Biquaternion.logger.log(logLevel,"angle = "+angle);
			Biquaternion.logger.log(logLevel,"axis*angle - input = "+
					Biquaternion.subtract(null, Biquaternion.times(null,angle, axis), bivector));
			double c = angle.qr.re,
				d = angle.qd.re;
			double sr = isometry.qr.re,
				sd = isometry.qd.re;
			// we have to find the "dual" angle (theta1 + theta2 I) such that exp(theta axis) = g
			// there are usually two ways to find theta1 and theta2, choose the numerically more stable one
			if (Math.abs(sr) > Math.abs(d)) theta1 = Math.atan2(c, sr);
			else theta1 = Math.atan2(-sd, d);
			if (isometry.getMetric() == Metric.HYPERBOLIC)	{
				if (theta1 > Math.PI/2)
					theta1 = theta1 - Math.PI;
				else if (theta1 < -Math.PI/2)
					theta1 = -theta1 + Math.PI;
			}
			double c1= Math.cos(theta1),
				s1 = Math.sin(theta1);
			switch(isometry.getMetric())	{
				case EUCLIDEAN:
					if (Math.abs(c1) > Math.abs(s1)) theta2 = d/c1;
					else theta2 = (-sd/s1);
					break;
				case ELLIPTIC:
					if (Math.abs(c) > Math.abs(sr))	theta2 = Math.atan2(-sd, c);
					else theta2 = Math.atan2(d, sr);
					break;
				case HYPERBOLIC:
					if (Math.abs(c) > Math.abs(sr))	theta2 =Pn.atanh(-sd/c);
					else theta2 = Pn.atanh(d/sr);
					break;
			}
		}
		angle.qr.re = theta1;
		angle.qd.re = theta2;
		Biquaternion.logger.log(logLevel,"thetas = "+theta1+" "+theta2);
		Biquaternion.logger.log(logLevel,"input = "+isometry);
		Biquaternion.logger.log(logLevel,"exp(gt) = "+exp(1.0));
		
	}
	public Biquaternion exp(double t)	{
		Biquaternion axis2 = null;
		if (isIdentity) return isometry;
		if (isTranslation)	{
			if (metric == Metric.ELLIPTIC)	{
				axis2 = Biquaternion.times(null, isRightTranslation ? kminus : kplus,
						new Biquaternion(Quaternion.exp(null, t*angle.qr.re, axis.qr ), new Quaternion(0,0,0,0), Metric.ELLIPTIC));
			} else if (metric == Metric.EUCLIDEAN)	{
				axis2 = Biquaternion.times(null, t, axis);
				axis2.getRealPart().re = 1.0;
			}
		} else {
			// in general the result is (cs0 + cs1 I + (cs2 + cs3 I) axis).
			// build that result up
			double[] cossin = Biquaternion.cossin(null, t*angle.qr.re, t*angle.qd.re, metric);
			Biquaternion scalar2 = new Biquaternion(cossin[2], cossin[3], metric);
			axis2 = Biquaternion.times(null, scalar2, axis);
			axis2.qr.re = cossin[0];
			axis2.qd.re = cossin[1];			
		}
		return axis2;
	}

	public double[] getMatrix() {
		return matrix;
	}

	public void setMatrix(double[] matrix) {
		this.matrix = matrix;
	}
}
