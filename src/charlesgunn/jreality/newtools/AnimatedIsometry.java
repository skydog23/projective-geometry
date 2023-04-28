/*
 * Created on 16 Apr 2023
 *
 */
package charlesgunn.jreality.newtools;

import charlesgunn.math.Biquaternion;
import charlesgunn.math.Biquaternion.Metric;
import charlesgunn.math.IsometryAxis;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Transformation;
import discreteGroup.parser.ComplexListParserTokenTypes;

public class AnimatedIsometry {

	double[] isom = Rn.identityMatrix(4),
		src = Rn.identityMatrix(4),
		target = Rn.identityMatrix(4);
	int metric = Pn.EUCLIDEAN;
	private IsometryAxis ia;
	Transformation origT;
	String name = null;
	double turn = 1.0;   // this is a half-turn
	boolean overwriteOriginal = false;
	boolean clipTime = true;
 
	// interpolate between the two isometries src and target
	// animation runs between t-values of 0 and 1
	public AnimatedIsometry(double[] src, double[] target, int metric)	{
		this.metric = metric;
		this.src = src;
		this.target = target;
		
		isom = Rn.times(null, target,  Rn.inverse(null, src));
		Biquaternion biq = Biquaternion.biquaternionFromDirectIsometry(null, 
				isom, Metric.metricForCurvature(metric));
		ia = new IsometryAxis(biq);
//		System.err.println("ia.axis = "+ia.getAxis()+" angle = "+ia.getAngle());
	}
	
	public void setClipTime(boolean b) {
		clipTime = b;
	}
	public void setIsometryAxis(IsometryAxis ia) {
		this.ia = ia;
	}
	// t should be in the range [0,1]
	public double[] getValueAtTime(double t) {
		if (clipTime) {
			if (t < 0.0) t = 0.0;
			if (t > 1.0) t = 1.0;			
		}
//		System.err.println("AsimIsom t = "+t);
		Biquaternion bq = ia.exp(t);
		double[] mat = Biquaternion.matrixFromBiquaternion(null, bq);
		isom = Rn.times(null, mat, src); // newFM.getArray()));
		return isom ;
	}
	
}
