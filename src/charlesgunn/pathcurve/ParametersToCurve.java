/*
 * Created on 27.10.2016
 *
 */
package charlesgunn.pathcurve;

import java.util.Locale;

import charlesgunn.math.Complex;
import de.jreality.math.Pn;
import de.jreality.math.Rn;

public class ParametersToCurve {

	static double[] coordinateSystem = {1,0,0,0,   0,1,0,0,  0,0,-.5,.5,  0,0,1,1};	
	{
		Locale.setDefault(Locale.US);
	}

	static public double[][] parametersToCurve(
			int numberSteps, 		// # of points on curve
			double[] initialPoint,  // 4-tuple
			double dt,				// step size
			double speed	,			// strength of motion
			double lambda,			// shape of vertical cross-section
			double epsilon) 			// amount of rotation around vertical
	{
		double[][] curve = new double[numberSteps][4];
		
		double[][] eigenvalues = parametersToEigenvalues(speed, lambda, epsilon);
		
		System.err.println("eigenvalues are "+Rn.toString(eigenvalues));
		// remap parameters to +/- infinity
		// probably belongs in the calling program
		double xlambda = lambda;
		if (lambda < -1) xlambda = -1.0/(lambda+2);
		if (lambda > 1) xlambda =  1.0/(2-lambda);
		double xepsilon = epsilon;
		if (epsilon < -1) xepsilon = -1.0/(epsilon+2);
		if (epsilon > 1) xepsilon =  1.0/(2-epsilon);
		if (xepsilon != 0) xepsilon = 1.0/xepsilon;
		else xepsilon = 10E8;

		// the time parameter runs from -dt to +dt
		double increment = dt * 2.0/(numberSteps-1);
		for (int i = 0; i<numberSteps; ++i)	{
			double t = -dt + i* increment;
			double[] m = exponentialMatrixAtTime(eigenvalues, t);
			System.err.println("exponential matrix is "+Rn.matrixToString(m));
			double[] mm = Rn.conjugateByMatrix(null, m, coordinateSystem);
			Rn.matrixTimesVector(curve[i], mm, initialPoint);
		}

		return curve;
	}
	
	static private double[][] parametersToEigenvalues(double speed, double lambda, double epsilon)	{
		double k1 = -lambda;
		double k2 = 1;
		if (Math.abs(k1) > Math.abs(k2)) {k2 = k2/Math.abs(k1);  k1 = k1/Math.abs(k1); }
		else {k1 = k1/Math.abs(k2); k2 = k2/Math.abs(k2); }
		double k0 = speed;
		k1 = speed*k1;
		k2 = speed*k2;
		if (Math.abs(epsilon) > 1) k0 = k0/epsilon;
		else {k1 = epsilon*k1; k2 = epsilon*k2; }
//		System.err.println("k0, k1, k2: "+k0+" "+k1+" "+k2);
		double[][] eigenvalues = new double[4][2];
		eigenvalues[0] = new double[]{0,k0}; 
		eigenvalues[1] = new double[]{0,-k0};
		eigenvalues[2] = new double[]{k1,0};
		eigenvalues[3] = new double[]{k2,0};
		return eigenvalues;
	}
			
	static double[] exponentialMatrixAtTime(double[][] eigenvalues, double d) {
		int n = eigenvalues.length;
		double[][] exp = new double[eigenvalues.length][2];
		for (int i = 0; i<n; ++i)	{
			exp[i][0] = d * eigenvalues[i][0];  // real part
			exp[i][1] = d * eigenvalues[i][1];  // imag part
			double rr = Math.exp(exp[i][0]);
			double c = Math.cos(exp[i][1]);
			double s = Math.sin(exp[i][1]);
			exp[i][0] = rr * c;
			exp[i][1] = rr * s;
		}
		double[] m = Rn.identityMatrix(4);
		for (int i = 0; i<4; ++i)	{
			if (exp[i][1] != 0)	{
				m[i*4+i] = m[(i+1)*4+i+1] = exp[i][0];
				m[i*4+i+1] = -exp[i][1];
				m[(i+1)*4+i] = exp[i][1];
				i++;
			} else
				m[i*4+i] = exp[i][0];
		}
		return m;
	}

	public static void main(String[] args) {
		ParametersToCurve ptc = new ParametersToCurve();
		
		double[][] curve = ParametersToCurve.parametersToCurve(
				21, 
				new double[]{.5,0,0,1}, 
				1.0, 
				1.0, 
				1.0, 
				1.0);
//		Pn.dehomogenize(curve, curve);
		System.err.println(Rn.toString(curve));
	}
}
