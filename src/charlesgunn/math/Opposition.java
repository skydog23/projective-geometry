package charlesgunn.math;

import de.jreality.math.Quaternion;

public class Opposition extends Biquaternion {

	public Opposition(Quaternion q1, Quaternion q2, Metric s) {
		super(q1, q2, s);
	}

	public Opposition(Metric s) {
		super(s);
	}

	public Opposition() {
		super();
	}

	public Opposition(Biquaternion src) {
		super(src);
	}

	public static Opposition plane(double[] plane, Metric sig)	{
		return new Opposition(
				new Quaternion(0,plane[0],plane[1],plane[2]), 
				new Quaternion(plane[3],0,0,0),
				sig);
	}

	public static Opposition point(double[] point, Metric sig)	{
		return new Opposition(
				new Quaternion(point[3],0,0,0),
				new Quaternion(0,point[0],point[1],point[2]), 
				sig);
	}

	public static Opposition times(Opposition dst, Opposition bq1, Biquaternion bq2)	{
		if (dst == null) dst = new Opposition(bq1.metric);
		return (Opposition) Biquaternion.times(dst, bq1, bq2);
	}

	public static Opposition times(Opposition dst, Biquaternion bq1, Opposition bq2)	{
		if (dst == null) dst = new Opposition(bq1.metric);
		return (Opposition) Biquaternion.times(dst, Biquaternion.dualConjugate(null, bq1), bq2);
	}

}
