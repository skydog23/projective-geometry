package charlesgunn.pathcurve;

import charlesgunn.math.Complex;

public class SimplePathCurveParametricSurfaceFactory extends PathCurveParametricSurfaceFactory {

		double lambda = 1.0, pitch = 1.0;
		
		public void update()	{
			double exc = Math.sqrt(lambda);
			eigenvalues[0] = new Complex(0, 1); //alpha);
			eigenvalues[1] = Complex.conjugate(null, eigenvalues[0]);
			eigenvalues[2] = new Complex(lambda,0);
			eigenvalues[3] = new Complex(pitch,0);
			super.update();
		}

		public double getLambda() {
			return lambda;
		}

		public void setLambda(double lambda) {
			this.lambda = lambda;
		}

		public double getPitch() {
			return pitch;
		}

		public void setPitch(double pitch) {
			this.pitch = pitch;
		}
}
