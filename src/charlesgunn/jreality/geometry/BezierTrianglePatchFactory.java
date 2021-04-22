/*
 * Created on Jun 23, 2004
 *
 */
package charlesgunn.jreality.geometry;

import de.jreality.geometry.ParametricTriangularSurfaceFactory;
import de.jreality.scene.IndexedFaceSet;

/**
 * TODO: documentation
 * @author Charles Gunn
 *
 */
public class BezierTrianglePatchFactory {
	double[][] controlPoints;
	int fiberLength = 3;
	int degree = 2;
	MyPTSF ptsf = new MyPTSF();
	public BezierTrianglePatchFactory()	{
		super();
	}
	
	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
		if (degree != 2) 
			throw new IllegalArgumentException("Degree must be 2 now");
	}

	public void setControlPoints(double[][] controlPoints) {
		this.controlPoints = controlPoints;
		fiberLength = controlPoints[0].length;
	}

	public double[][] getControlPoints() {
		return controlPoints;
	}

	public  IndexedFaceSet getIndexedFaceSet()	{
		return ptsf.getIndexedFaceSet();
	}

	public int getSubDivision()	{
		return ptsf.getSubdivision();
	}
	
	public void setSubdivision(int s)	{
		ptsf.setSubdivision(s);
	}
	
	public void update()	{
		ptsf.update();
	}
	
	public ParametricTriangularSurfaceFactory getSurfaceFactory()	{
		return ptsf;
	}
	
	private class MyPTSF extends ParametricTriangularSurfaceFactory {
		MyPTSF() {
			super();
			setImmersion(new Immersion()	{

				public void evaluate(double u, double v, double[] xyz, int index) {
					double w = 1 - u - v;
					double[][] cp = controlPoints;
					for (int i = 0; i<fiberLength; ++i)	{
						xyz[i] = w*w*cp[0][i]+
							2*u*w*cp[1][i]+
							u*u*cp[2][i]+
							2*v*w*cp[3][i]+
							2*u*v*cp[4][i]+
							v*v*cp[5][i];
					}
				}

				public int getDimensionOfAmbientSpace() {
					return fiberLength;
				}

				public boolean isImmutable() {
					return false;
				}
				
			});
		}
	}
//
/////**
// * @deprecated
// */
// public static double[][] extractUParameterCurve(double[][] curve, QuadMeshShape qms, int which)	{
//	return extractParameterCurve(curve, qms, which, 0);
//}
//
//	/**
//	 * @deprecated
//	 */
//public static double[][] extractVParameterCurve(double[][] curve, QuadMeshShape qms, int which)	{
//	return extractParameterCurve(curve, qms, which, 1);
//}
//
///**
// * @deprecated
// */
//public static double[][] extractParameterCurve(double[][] curve, QuadMeshShape qms, int which, int type)	{
//	return extractParameterCurve(curve, qms, qms.getMaxU(), qms.getMaxV(), which, type);
//}

}
