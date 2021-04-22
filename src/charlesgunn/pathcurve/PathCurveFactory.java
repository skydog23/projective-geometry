package charlesgunn.pathcurve;

import charlesgunn.math.Complex;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;

public class PathCurveFactory {
	Complex[] eigenvalues = new Complex[4];
	double[] coordinateSystem = Rn.identityMatrix(4),
			dt = Rn.identityMatrix(4), 
			ddt = Rn.identityMatrix(4),
			dtD = Rn.identityMatrix(4),
			ddtD = Rn.identityMatrix(4);
	double[] initialPoint = {0,0,0,1};
	double tmin = 0, tmax = 1;
	int numberSteps = 2;
	double[][] curvePoints = null;
	Curve curve;
	
	public void update()	{
		// calculate the derivative of the motion at t = 0
		// this is just a diagonal matrix consisting of the eigenvalues
		dt = Rn.identityMatrix(4);
		for (int i = 0; i<eigenvalues.length; ++i)	{
			int index = 4*i+i;
			if (eigenvalues[i].im != 0)	{ // conj pair
				dt[index] = eigenvalues[i].re;
				dt[index+1] = eigenvalues[i].im;
				dt[index+4] = eigenvalues[i+1].im;
				dt[index+5] = eigenvalues[i+1].re;
				i++;
			}
			else
				dt[index] = eigenvalues[i].re;
		}
		dt = Rn.conjugateByMatrix(null, dt, coordinateSystem);
		// the second derivative is obtained by squaring the first
		// that's how exponentials behave
		// d(e^(tk)) = k e^(tk) dt
		ddt = Rn.times(null, dt, dt);
		dtD = Rn.transpose(null, Rn.inverse(null, dt));
		ddtD = Rn.transpose(null, Rn.inverse(null, ddt));
		curve = new Curve()	{
			public void evaluate(double u, double[] xyz, int index) {
				double[] m = PathCurveUtility.exponentialMatrixAtTime(eigenvalues,u);
				double[] mm = Rn.conjugateByMatrix(null, m, coordinateSystem);
				Rn.matrixTimesVector(xyz, mm, initialPoint);
				Pn.dehomogenize(xyz,  xyz);
//				System.err.println("Got point "+Rn.toString(xyz));
			}

			public int getDimensionOfAmbientSpace() {
				return 4;
			}

			public boolean isImmutable() {
				return true;
			}
			
		};
		curvePoints = new double[numberSteps][4];
		double dt = (tmax - tmin)/(numberSteps-1);
		for (int i = 0; i<numberSteps; ++i)	{
			double t = tmin + i* dt;
			curve.evaluate(t, curvePoints[i], 0);
		}
	}
	
//	public double[] getOsculatingPlane
	public double[] getOsculatingPlane(double[] res, double[] pt)	{
		return P3.planeFromPoints(res, pt, 
				Rn.matrixTimesVector(null, dt, pt), 
				Rn.matrixTimesVector(null, ddt, pt));
	}
	
	public double[] getPivotPoint(double[] res, double[] pl)	{
		return P3.pointFromPlanes(res, pl, 
				Rn.matrixTimesVector(null, dtD, pl), 
				Rn.matrixTimesVector(null, ddtD, pl));
	}
	
	public double[] getCoordinateSystem() {
		return coordinateSystem;
	}
	
	public void setCoordinateSystem(double[] coordinateSystem) {
		this.coordinateSystem = coordinateSystem;
	}
	
	public Complex[] getEigenvalues() {
		return eigenvalues;
	}
	
	public void setEigenvalues(Complex[] eigenvalues) {
		this.eigenvalues = eigenvalues;
		update();
	}
	
	public double[] getInitialPoint() {
		return initialPoint;
	}
	
	public void setInitialPoint(double[] initialPoint) {
		this.initialPoint = initialPoint;
	}
	
	public double[][] getCurvePoints() {
		return curvePoints;
	}

	public Curve getCurve() {
		return curve;
	}

	public int getNumberSteps() {
		return numberSteps;
	}

	public void setNumberSteps(int numberSteps) {
		this.numberSteps = numberSteps;
	}

	public double getTmax() {
		return tmax;
	}

	public void setTmax(double tmax) {
		this.tmax = tmax;
	}

	public double getTmin() {
		return tmin;
	}

	public void setTmin(double tmin) {
		this.tmin = tmin;
	}
	
}
