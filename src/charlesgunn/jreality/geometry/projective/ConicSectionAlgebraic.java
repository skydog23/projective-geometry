package charlesgunn.jreality.geometry.projective;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import de.jreality.math.Rn;

public class ConicSectionAlgebraic {

	transient protected double[] conic = Rn.diagonalMatrix(null, new double[] {1,1,-1}), dconic;
	transient protected double a,b,cc,f,g,h, A,B,C,F,G,H;
	transient protected double[] center = null, dcenter = null;
	
	public ConicSectionAlgebraic(double[] c)	{
		// should check symmetry!
		conic = c.clone();
		dconic = Rn.adjoint(null, conic);
		// polar partner of line at infinity, resp., the origin
		center= Rn.matrixTimesVector(null, conic, new double[] {0,0,1});
		// we want that the center has negative z-coordinate
		if (center[2] > 0) Rn.times(conic, -1, conic);
		dcenter= Rn.matrixTimesVector(null, dconic, new double[] {0,0,1});
		if (dcenter[2] > 0) Rn.times(dconic, -1, dconic);
		a = conic[0];
		b = conic[4];
		cc = conic[8];
		h = conic[1];
		g = conic[2];
		f = conic[3];
		A = dconic[0];
		B = dconic[4];
		C = dconic[8];
		H = dconic[1];
		G = dconic[2];
		F = dconic[3];
	
	}

	public double[] getConic() {
		return conic;
	}
	
	public double[] getPointOnConic(double[] pt) {
		if (pt == null) pt = new double[3];
		// rotate a line around the center and search for intersections.
		return pt;
	}
	
	public double[] getPolarPoint(double[] pt)	{
		return Rn.matrixTimesVector(null, conic, pt);
	}
	
	public double[] getPolarLine(double[] ln)	{
		return Rn.matrixTimesVector(null, dconic, ln);
	}
	
	public double[][] intersectLineWithConic(double[] ln) {
		double R = ln[0], S = ln[1], T = ln[2];
		double d = sqrt(pow(2*f*R*S + 2*g*pow(S,2) - 2*b*R*T - 2*h*S*T,2) - 
			       4*(b*pow(R,2) + 2*h*R*S + a*pow(S,2))*
			        (cc*pow(S,2) - 2*f*S*T + b*pow(T,2))),
				num = -2*f*R*S - 2*g*pow(S,2) + 2*b*R*T + 2*h*S*T,
				den = 2.*(b*pow(R,2) + 2*h*R*S + a*pow(S,2)),
				x1 = (num+d)/den,
				x2 = (num-d)/den;
		System.err.println("d,num,den,x1,x2:"+d+":"+num+":"+den+":"+x1+":"+x2+":");
		return new double[][] {{S*x1, R*x1-T, S}, {S*x2, R*x2-T, S}};
	}
	
	public static void main(String[] args) {
		ConicSectionAlgebraic csa =  new ConicSectionAlgebraic(Rn.diagonalMatrix(null, new double[] {1,1,-1}));
		double[][] cuts = csa.intersectLineWithConic(new double[] {1,0,0});
		System.err.println("cuts = "+Rn.toString(cuts));
	}
}
