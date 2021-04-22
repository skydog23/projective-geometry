/*
 * Created on Dec 5, 2014
 *
 */
package charlesgunn.math.clifford;

import charlesgunn.math.Biquaternion;
import de.jreality.math.Rn;

public class ConstructRotor {

	static double angle = Math.PI/4, // 0.0
			c = Math.cos(angle), s = Math.sin(angle),
			d = 2;
	static double[] M = {1., 0., 0., d, 
			0., c, -s, 0.,
			0., s, c, 0.0,
			0., 0., 0., 1.};
	static double[][] points = {{0, 0, 0, 1}, {1, 0, 0, 1}, {0, 1, 0, 1}, {0, 0, 1, 1}, {1, 
		   1, 1, 1}};
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Biquaternion bq = Biquaternion.biquaternionFromDirectIsometry(null, M, Biquaternion.Metric.EUCLIDEAN);
		System.err.println("biq = "+bq);
		Biquaternion axis = Biquaternion.axisForBivector(null, 
				Biquaternion.bivector(null, bq));
		System.err.println("axis = "+axis);
		double[] MM = Biquaternion.matrixFromBiquaternion(null, bq);
		System.err.println("M = "+Rn.matrixToString(MM));
//		bq = new Biquaternion(new double[]{, s)
	}

}
