/*
 * Author	gunn
 * Created on Nov 14, 2005
 *
 */
package charlesgunn.jreality.geometry;

import de.jreality.geometry.BezierPatchMesh;
import de.jreality.geometry.TubeFactory;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;

public class BezierPatchMeshTubeFactory extends TubeFactory {
	BezierPatchMesh theTube;
	boolean coreCurveIsCubicBezier = false;
	BezierCurve crossCurve = null;
//	public BezierPatchMeshTubeFactory(IndexedLineSet ils) {
//		super(ils);
//	}

	public BezierPatchMeshTubeFactory(double[][] curve) {
		super(curve);
	}

	public boolean isCoreCurveIsCubicBezier() {
		return coreCurveIsCubicBezier;
	}

	public void setCoreCurveIsCubicBezier(boolean coreCurveIsCubicBezier) {
		this.coreCurveIsCubicBezier = coreCurveIsCubicBezier;
	}

	public BezierCurve getCrossCurve() {
		return crossCurve;
	}

	public void setCrossSection(BezierCurve crossCurve) {
		this.crossCurve = crossCurve;
		crossSection = crossCurve.controlPoints;
	}

	public BezierPatchMesh getTube()	{
		return theTube;
	}
	
	public void update() {
		int vl = crossSection[0].length;
		
		if (vl != 3 && vl != 4)	{
			throw new IllegalArgumentException("Points must be dimension 3 or 4");
		}
		int n = theCurve.length;
		updateFrames();
		//System.err.println("Created "+frames.length+" frames");
		int  numBezierNodes;
		if (coreCurveIsCubicBezier) numBezierNodes = n;
		else numBezierNodes = closedCurve ? 3*n + 1 : 
			3*((extendAtEnds ? n: n-2)) + 1;
		double[][][] vals = new double[numBezierNodes][crossSection.length][vl];
		
//		LoggingSystem.getLogger().log(Level.FINE,"Input curve has "+n+" segments");
//		LoggingSystem.getLogger().log(Level.FINE,"Curve handed over to routines "+P.length+" segments");
//		LoggingSystem.getLogger().log(Level.FINE,"Frame field has "+frames.length+" frames");
//		LoggingSystem.getLogger().log(Level.FINE,"Bezier patch mesh has "+vals.length+" nodes");
		
		double[] scaleMatrix = Rn.identityMatrix(4);
		double radcoord = radius;
		if (metric == Pn.ELLIPTIC)		radcoord = Math.tan(radius);
		else if (metric == Pn.HYPERBOLIC) 	radcoord = Pn.tanh(radius);
		int m = crossSection.length;
		double[] rotateNBMatrix = new double[16];
		//n = closed ? 2 * usedVerts - 1  :  2 * usedVerts - 3;
		int nn = closedCurve? frames.length-2 : frames.length;
		for (int i = 0,outCount = 0; i<nn; ++i)	{
			// TODO extend following to respect metric as above
			if (radiiField != null) radcoord = radiiField[i+1];
			//System.err.println("Radius is "+radcoord+" and theta is "+frames[i].theta);
			int index = i;
			double sangle = Math.sin(frames[index].theta/2.0);
			scaleMatrix[0] = radcoord * ((sangle == 0) ? 1.0 : 1.0/sangle);
			scaleMatrix[5] = radcoord;
			P3.makeRotationMatrixZ(rotateNBMatrix, frames[index].phi);
			double[] scaledFrame = Rn.times(null, frames[index].frame, Rn.times(null,scaleMatrix, rotateNBMatrix));
			for (int j = 0; j<m; ++j)	{
				int p = j; //m - j - 1;
				Rn.matrixTimesVector(vals[outCount][j], scaledFrame, crossSection[p]);
			}
			if (i%2 == 1 && !coreCurveIsCubicBezier)	{		
				outCount++;
			}
			outCount++;
		}
		if (!coreCurveIsCubicBezier)	{
			int k = vals.length;
			int ai = 0;
			for (int i = 0; i<k; ++i)		{
				if ((i%3) != 1) continue;		// midpoints
				int next = i+2;
				int prev = i-1;
				double a = Math.abs(frames[ai].theta/Math.PI);
				// This quadratic function is chosen so that the resulting spline surface
				// is a cylinder when angle = pi,
				// is close to a circular arc for angle = pi/2
				double p = .4379 * a*a - .1046 * a + .3333;
				//p = p * Math.sin(angles[ai]/2);
				p *= .8;
				//p = 1.0;
				double ip = 1.0 - p;
				ai += 2;
				
				for (int j = 0; j<m; ++j) {
					Rn.linearCombination(vals[i+1][j], ip, vals[next][j], p, vals[i][j]);
					Rn.linearCombination(vals[i][j], ip, vals[prev][j], p, vals[i][j]);
				}
			}
			
		}
		theTube = new BezierPatchMesh(crossCurve != null ? crossCurve.degree : 2, 3, vals);
	}

	/**
	 * @param n
	 * @return
	 */
	public void updateFrames() {
		if (!framesDirty) return;
		int n = theCurve.length;
		int usedVerts = (!coreCurveIsCubicBezier && closedCurve) ? n+3 : 
				(extendAtEnds ? n+2 : n);
		double[][] polygon2 = new double[usedVerts][];
		double[] radii2 = null;
		double[][] tangents = null;
		if (radii != null) radii2 = new double[usedVerts];
		if (coreCurveIsCubicBezier)	{
//			for (int i = 0; i<usedVerts; ++i)	{
//				polygon2[i] = theCurve[i];
//				if (radii2 != null) radii2[i] = radii[i];
//			}		
			// calculate the tangent vectors
			tangents = new double[n][4];
			for (int i = 0; i<n; ++i)	{
				double t = 0.0;
				if ( (i%4) == 0)	
					Rn.times(null, 3.0, Rn.subtract(tangents[i], theCurve[i+1], theCurve[i]));
				else if ((i%4) == 3)	
					Rn.times(null, 3.0, Rn.subtract(tangents[i], theCurve[i], theCurve[i-1]));
				// the intermediate points evaluated at t = 1/3 and t=2/3
				// give derivatives: a'(1/3) = -4/3 P0 + P2 + 1/3 P3, similar for a'(2/3)
				else if ((i%4) == 1)	{
					Rn.add(tangents[i], 
						Rn.times(null, -4/3.0, theCurve[i-1]),
						Rn.add(null, theCurve[i+1], Rn.times(null, 1/3.0, theCurve[i+2])));
				}
				else if ((i%4) == 2)	{
					Rn.add(tangents[i], 
						Rn.times(null, -1/3.0, theCurve[i-2]),
						Rn.subtract(null, Rn.times(null, 4/3.0, theCurve[i+1]),  theCurve[i-1]));
				}
				tangents[i][3] = 0;
				//System.err.println("Tangent "+i+" "+Rn.toString(tangents[i]));
			}
			setTangents(tangents);
		} //else {
			if (closedCurve)	{
				for (int i = 0; i<n; ++i)	{
					polygon2[i+1] = theCurve[i];
					if (radii2 != null) radii2[i+1] = radii[i];
				}
				polygon2[0] = theCurve[n-1];
				polygon2[n+1] = theCurve[0];
				if (radii2 != null) {
					radii2[0] = radii[n-1];
					radii2[n+1] = radii[0];
				}
				polygon2[n+2] = theCurve[1];
				if (radii2 != null) radii2[n+2] = radii2[1];
			} else if (extendAtEnds) {
				for (int i = 0; i<n; ++i)	{
					polygon2[i+1] = theCurve[i];
					if (radii2 != null) radii2[i+1] = radii[i];
				}
				polygon2[0] = Rn.add(null, theCurve[0],  Rn.subtract(null, theCurve[0], theCurve[1]));
				polygon2[n+1] = Rn.add(null, theCurve[n-1], Rn.subtract(null, theCurve[n-1], theCurve[n-2]));
				if (radii2 != null)	{
					radii2[0] = radii[0];
					radii2[n+1] = radii[n-1];
				}
			}	else {
				for (int i = 0; i<n; ++i)	{
					polygon2[i] = theCurve[i];
					if (radii2 != null) radii2[i] = radii[i];
				}				
			}
		//}
		
		double[][] P = null;
		if (coreCurveIsCubicBezier)	{
			P = polygon2;
			radiiField = radii2;
		} else {
			P = new double[2*usedVerts-1][theCurve[0].length];
			if (radii != null) radiiField = new double[2*usedVerts-1];
			for (int i = 0; i<usedVerts; ++i)	{
				Rn.copy(P[2*i], polygon2[i]);
				if (radii2 != null) radiiField[2*i] = radii2[i];
				if (i< (usedVerts - 1)) {
					Rn.times(P[2*i+1], .5, Rn.add(null, polygon2[i], polygon2[i+1]));
					if (radiiField != null) radiiField[2*i+1] = radii2[i]*.5 + radii2[i+1]*.5;
				}
			}			
		}
		frames = makeFrameField(P, frameFieldType, metric);
	}
	
}
