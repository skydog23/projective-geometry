package charlesgunn.pathcurve;

import charlesgunn.math.Complex;

/**
 * This class appears to be devoted to the semi-imaginary case: 
 * one pair of conjugate imaginary eigenvalues and two real ones.
 * @author gunn
 *
 */
public class SemiImagPathCurveFactory extends PathCurveFactory {

		double lambda = 1.0, epsilon = 1.0, speed = 1.0;
		
		public void update()	{
			// TODO figure out the correct normalization for k1 and k2, if there is one
			double k1 = -lambda;
			double k2 = 1;
//			if (Math.abs(lambda) > 1.0) {
//				k1 /= lambda;
//				k2 /= lambda;
//			}
			if (Math.abs(k1) > Math.abs(k2)) {k2 = k2/Math.abs(k1);  k1 = k1/Math.abs(k1); }
			else {k1 = k1/Math.abs(k2); k2 = k2/Math.abs(k2); }
			double k0 = speed;
			k1 = speed*k1;
			k2 = speed*k2;
			if (Math.abs(epsilon) > 1) k0 = k0/epsilon;
			else {k1 = epsilon*k1; k2 = epsilon*k2; }
			System.err.println("k0, k1, k2: "+k0+" "+k1+" "+k2);
			eigenvalues[0] = new Complex(0,k0); 
			eigenvalues[1] = Complex.conjugate(null, eigenvalues[0]);
			eigenvalues[2] = new Complex(k1,0);
			eigenvalues[3] = new Complex(k2,0);
			super.update();
		}

		public double getLambda() {
			return lambda;
		}

		public void setLambda(double lambda) {
			this.lambda = lambda;
		}

		public double getEpsilon() {
			return epsilon;
		}

		public void setEpsilon(double pitch) {
			this.epsilon = pitch;
		}

		public double getSpeed() {
			return speed;
		}

		public void setSpeed(double speed) {
			this.speed = speed;
		}
}
