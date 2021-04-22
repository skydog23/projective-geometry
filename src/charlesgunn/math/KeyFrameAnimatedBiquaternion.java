package charlesgunn.math;

import java.util.HashMap;

import charlesgunn.anim.core.KeyFrame;
import charlesgunn.anim.core.KeyFrameAnimatedDelegate;
import charlesgunn.anim.core.SimpleKeyFrameAnimated;
import charlesgunn.anim.util.AnimationUtility;
import de.jreality.math.Rn;

public class KeyFrameAnimatedBiquaternion extends SimpleKeyFrameAnimated<Biquaternion> {

	HashMap<KeyFrame<Biquaternion>, IsometryAxis> isoax = new HashMap<KeyFrame<Biquaternion>, IsometryAxis>();
	
	public KeyFrameAnimatedBiquaternion(Biquaternion t) {
		super(t);
	}
	
	public KeyFrameAnimatedBiquaternion(KeyFrameAnimatedDelegate<Biquaternion> animatedDelegate) {
		super(animatedDelegate);
	}
	
	@Override
	public Biquaternion copyFromTo(Biquaternion from, Biquaternion to) {
		return Biquaternion.copy(to, from);
	}

	@Override
	protected Biquaternion getNewInstance() {
		if (target != null) return new Biquaternion(target);
		return new Biquaternion();
	}

	private void updateIsometryAxes()	{
		for (int i = 0; i<keyFrames.size(); ++i)	{
			Biquaternion value = keyFrames.get(i).getValue();
			int j = (i == keyFrames.size()-1 ? i : i+1);
			Biquaternion value2 = keyFrames.get(j).getValue();
//			Biquaternion bq = Biquaternion.times(null, value2, Biquaternion.dualConjugate(null, value));
			double[] mat1 =   Biquaternion.matrixFromBiquaternion(null, value);
			double[] mat2 =   Biquaternion.matrixFromBiquaternion(null, value2);
			double[] mat = Rn.times(null,  mat2, Rn.inverse(null, mat1));
			Biquaternion bq = Biquaternion.biquaternionFromDirectIsometry(null, mat, value.getMetric());
			IsometryAxis ia = new IsometryAxis(bq);
			System.err.println("i = "+i+" axis = "+ia.getAxis().toString()+" angles ="+ia.getAngle().qr.re+":"+ia.getAngle().qd.re);
			isoax.put(keyFrames.get(i), ia);
		}
	}
	@Override
	public void setValueAtTime(double t) {
		if (getKeyFrames().size() == 0) return;
   		if (isoax.size() != getKeyFrames().size() || (getKeyFrames().size() > 1 && 
   			keyFramesChanged()))	{
   				updateIsometryAxes();
	   			keyFramesChanged = false;
   			}
		super.setValueAtTime(t);
		t = remappedTime;
		if (!valueIsSet)   {
			IsometryAxis ia = isoax.get(previous);
			double  thistime = AnimationUtility.hermiteInterpolation(t, previous.getTime(), next.getTime(),0.0, 1.0);
			Biquaternion exp = ia.exp(thistime);
//			currentValue = Biquaternion.conjugateBy(null, previous.getValue(), exp);
			// TODO calculate with biquaternions w/o using matrices
			double[] expm = Biquaternion.matrixFromBiquaternion(null, exp);
			double[] cv =  Rn.times(null, expm, Biquaternion.matrixFromBiquaternion(null, previous.getValue()));
			currentValue = Biquaternion.biquaternionFromDirectIsometry(null, cv, ia.getIsometry().getMetric());//conjugateBy(null, exp, previous.getValue());
			double[] cv2 = Biquaternion.matrixFromBiquaternion(null, currentValue);
			boolean same = Rn.equals(cv, cv2, 10E-8);
			if (!same)	{
				System.err.println("M->B->M not identity ");
			}
			System.err.println("setValueAtTime: keyframe "+previous.getTime()+" t "+t);
		}
		if (target != null) {
			copyFromTo(currentValue, target);
		}
		delegate.propagateCurrentValue(currentValue);
	}

	@Override
	public boolean equalValues(Biquaternion t1, Biquaternion t2) {
		// TODO Auto-generated method stub
		return Rn.equals(Biquaternion.asDouble(null, t1), Biquaternion.asDouble(null, t2));
	}

}
