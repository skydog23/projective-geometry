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
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import discreteGroup.tools.MidiSoundEffects;

public class AnimatedIsometry {

	double[] isom, 
		orig = Rn.identityMatrix(4),
		target = Rn.identityMatrix(4);
	int metric = Pn.EUCLIDEAN;
	IsometryAxis ia;
	SceneGraphComponent targetSGC;
	Transformation origT;
	String name = null;
	double turn = 1.0;   // this is a half-turn
	MidiSoundEffects mse = new MidiSoundEffects();

 
	public AnimatedIsometry(double[] isom, int metric, 
			SceneGraphComponent sgc)	{
		this.isom = isom;
		this.metric = metric;
		Biquaternion biq = Biquaternion.biquaternionFromDirectIsometry(null, 
				isom, Metric.metricForCurvature(metric));
//		System.err.println("targetsgc ="+sgc.getName());
//		System.err.println("isom = "+isom);
//		System.err.println("biq = "+biq);
		ia = new IsometryAxis(biq);
		targetSGC = sgc;
		origT = targetSGC.getTransformation();
		if (origT == null) 
			targetSGC.setTransformation(new Transformation());
		orig = origT.getMatrix();
		mse.setDoSound(true);
	}
	
	public void reset() {
		origT.setMatrix(orig);
	}
	
	public void startAnimation() {
		mse.initMoving();
	}
	public void setValueAtTime(double xt) {
		double t = xt*(turn);
		Biquaternion bq = ia.exp(t);
		double[] mat = Biquaternion.matrixFromBiquaternion(null, bq);
		Rn.times(target, orig, mat); // newFM.getArray()));
		origT.setMatrix(target);
		mse.playMoving(xt);
//		System.err.println("setting tform " + Rn.matrixToString(target));
	}
	
	public void endAnimation() {
		mse.playEnd();
	}
	
}
