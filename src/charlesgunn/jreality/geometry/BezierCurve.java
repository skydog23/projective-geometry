/*
 * Created on Jun 23, 2004
 *
 */
package charlesgunn.jreality.geometry;

import charlesgunn.math.p5.PlueckerLineGeometry;
import de.jreality.math.Rn;

/**
 * This class implements Bezier curves of arbitrary dimension.  T
 * 
 * Instances are immutable.  The only available constructor (see {@link #BezierCurve(int, double[][])}
 * specifies the degree and an array of control points.  The control points
 * can be n-dimensional. The
 * control points must be consistent with the degree specification.  That is, the number of 
 * control points must be 1 more than a multiple of the degree.   
 * The simplest case is a single arc; for a cubic Bezier, this is a 4-array of
 * control points.
 * 
 * Currently the only operations supported on the mesh is binary refinement: {@link #refine()}.  
 * 
 * @author Charles Gunn
 *
 */
public class BezierCurve {
	public double[][] controlPoints;
	int degree;
	double[] u0Split, u1Split;	// matrices expressing subdivision step 
	
	public BezierCurve(int d,  double[][] cp)	{
		super();
		this.degree = d;
		if (degree > 1 && cp.length % (degree) != 1)	{
			throw new IllegalArgumentException("Array length must be for form degree*n + 1");
		}
		controlPoints = cp;
		u0Split = Rn.identityMatrix(degree+1);
		u1Split = Rn.identityMatrix(degree+1);
		
		double[][] ptri = {{1},{1,1},{1,2,1},{1,3,3,1},{1,4,6,4,1},{1,5,10,10,5,1}};
		
		double factor = 1.0;
		int size = (degree+1)*(degree+1);
		for (int i = 0; i<= degree; ++i)	{
			for (int j = 0; j<=i; ++j)	{
				u0Split[i*(degree+1) + j] = factor * ptri[i][j];
				u1Split[size - i*(degree+1) - j - 1] = factor * ptri[i][j];
			}
			factor *= .5;
		}
		System.err.println("u0split = "+Rn.matrixToString(u0Split));
		System.err.println("u1split = "+Rn.matrixToString(u1Split));

	}
	
	
	public  void refine()	{
		int dim = controlPoints.length;
		
		int vectorLength = controlPoints[0].length;
		double[][] vals = new double[2*dim-1][vectorLength];
		double[] icp = new double[degree+1];
		double[] ocp = new double[degree+1];
		for (int i = 0; i<vectorLength; ++i)	{
			int outCount = 0;
			for (int inCount = 0; inCount < dim-1; inCount += degree)	{
				for (int j = 0; j<=degree; ++j)		icp[j] = controlPoints[inCount+j][i];
				Rn.matrixTimesVector(ocp, u0Split, icp);
				for (int j = 0; j<=degree; ++j)		vals[outCount+j][i] = ocp[j];
				outCount += degree;
				Rn.matrixTimesVector(ocp, u1Split, icp);
				for (int j = 0; j<=degree; ++j)		vals[outCount+j][i] = ocp[j];
				outCount += degree;
			}
		} 
		controlPoints = vals;		
	}
	
	/**
	 * @return Returns the controlPoints.
	 */
	public double[][] getControlPoints() {
		return controlPoints;
	}
	/**
	 * @return Returns the uDegree.
	 */
	public int getDegree() {
		return degree;
	}

    public double[] getValueAtTime(double at)	{
    	if (degree != 2) {
    		System.err.println("only allowed for degree = 2");
    		return null;
    	}
    	if (at < 0) at = 0;
    	if (at > 1) at = 1;
    	int numsegs = (controlPoints.length-1)/2;
    	if (at == 1) return controlPoints[controlPoints.length-1];
    	
    	int segment = (int) (at * numsegs);
    	double t = numsegs * at - segment;
    	int i0 = 2*segment, i1 = i0+1, i2 = i0+2;
    	double[] p0 = Rn.times(null, (1-t)*(1-t), controlPoints[i0]),
    			p1 = Rn.times(null, (1-t)*t*2, controlPoints[i1]),
    			p2 = Rn.times(null, t*t, controlPoints[i2]);
    	double[] ret = Rn.add(null, Rn.add(null, p0, p1), p2);
//    	System.err.println("value at time "+at+"= "+Rn.toString(ret));
    	return ret;
    }
    
    public double[] getTangentAtTime(double at)	{
    	if (degree != 2) {
    		System.err.println("only allowed for degree = 2");
    		return null;
    	}
    	int numsegs = (controlPoints.length-1)/2;
//    	if (at == 1) return controlPoints[controlPoints.length-1];
    	
    	int segment = (int) (at * numsegs);
    	double t = numsegs * at - segment;
    	if (t==0 && segment > 0) { t = 1; segment = segment-1;}
    	int i0 = 2*segment, i1 = i0+1, i2 = i0+2;
    	double[] p0 = Rn.add(null,
    				Rn.times(null, (1-t), controlPoints[i0]),
    				Rn.times(null, t, controlPoints[i1])),
    			 p1 = Rn.add(null,
        				Rn.times(null, (1-t), controlPoints[i1]),
        				Rn.times(null, t, controlPoints[i2])),
    			 line = PlueckerLineGeometry.lineFromPoints(null, p0, p1);
     	return line;
    }

	public double[][] getPolygonPoints() {
		int ifoo = 1+(controlPoints.length-1)/(degree+1);
		double[][] polygonPoints = new double[ifoo][controlPoints[0].length];
		for (int i =0;i<ifoo; ++i) {
			polygonPoints[i] = controlPoints[i*(degree+1)];
		}
		return polygonPoints;
	}

//	public static void main(String[] args) {
//		BezierCurv
//	}
}
