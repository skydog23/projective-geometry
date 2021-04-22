/*
 * Created on 28.01.2017
 *
 */
package charlesgunn.math.clifford;

import charlesgunn.math.Utility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.P2;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.data.Attribute;

public class ConicSection {
	
	double[][] initialPoints, initialPoints3;
	double[][] pentagram = new double[5][], 
			PaMbQ = new double[5][];
//	double[] P, Q, M, a, b;
	boolean dim4 = false;
	int numberOfPoints = 200;
	IndexedLineSetFactory connie = null;
	IndexedLineSetFactory ilsf = IndexedLineSetUtility.circleFactory(5, 0, 0, 1);

	public ConicSection()	{
		double[][] circlePoints = ilsf.getIndexedLineSet().getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		setInitialPoints(circlePoints);		
	}
	
	public void setFivePerspectivities(double[][] PaMbQIn)	{
		for (int i = 0; i<5; ++i)	{
			PaMbQ[i] = PaMbQIn[i].clone();
		}
	}
	public void setInitialPoints(double[][] init)	{
		initialPoints = init.clone();
		if (initialPoints[0].length == 4) {
			 dim4 = true;
			 initialPoints3 = Utility.demote(null, initialPoints);
		}
		else {
			dim4 = false;
			initialPoints3 = initialPoints;
		}
		for (int i = 0; i<5; ++i)	{
			pentagram[i] = P2.lineFromPoints(null, initialPoints3[(2*i)%5], initialPoints3[(2*i+2)%5]);
			pentagram[i] = P2.normalizeLine(null, pentagram[i]);
		}
//		System.err.println("pentagram = "+Rn.toString(pentagram));
		PaMbQ[0] = initialPoints3[0]; 
		PaMbQ[4] = initialPoints3[3]; 
		PaMbQ[2] = P2.pointFromLines(null, pentagram[0], pentagram[3]);
		PaMbQ[1] = pentagram[2];
		PaMbQ[3] = pentagram[1];
		Pn.dehomogenize(PaMbQ[2], PaMbQ[2]);
		P2.normalizeLine(PaMbQ[1],PaMbQ[1]);
		P2.normalizeLine(PaMbQ[3], PaMbQ[3]);
		
	}
	
	public double[] getValueAtTime(double[] ret, double t)	{		
		double angle = t * Math.PI;
		double[] p2 = Rn.add(null, PaMbQ[0], new double[]{Math.cos(angle), Math.sin(angle), 0});
		double[] Ppencil = P2.lineFromPoints(null, PaMbQ[0], p2);
		double[] bQ = getValueOfProjectivityAt(null, Ppencil);
		double[] PQ = P2.pointFromLines(null, Ppencil, bQ);
		ret = Pn.dehomogenize(ret, PQ);
//		System.err.println("proj: "+Rn.toString(new double[][]{pencil[i],Pa,aM,Mb,bQ,PQ}));
//		return dim4 ? Utility.promote(null, ret) : ret;
		return ret;
	}
	
	private double[] getValueOfProjectivityAt(double[] value, double[] lineAtA) {
		double[] Pa = P2.pointFromLines(null, lineAtA, PaMbQ[1]),
				aM = P2.lineFromPoints(null, Pa, PaMbQ[2]),
				Mb = P2.pointFromLines(null, aM, PaMbQ[3]);
		value = P2.lineFromPoints(value, Mb, PaMbQ[4]);
		return value;
	}
	private double[] getValueOfInverseProjectivityAt(double[] value, double[] lineAtC) {
		double[] Pa = P2.pointFromLines(null, lineAtC, PaMbQ[3]),
				aM = P2.lineFromPoints(null, Pa, PaMbQ[2]),
				Mb = P2.pointFromLines(null, aM, PaMbQ[1]);
		value = P2.lineFromPoints(value, Mb, PaMbQ[0]);
		return value;
	}

	public double[] polarizeSame(double[] output, double[] input)	{
		if (input == null) output = new double[3];
		double[] ax = P2.lineFromPoints(null, input, PaMbQ[0]),
				cx = P2.lineFromPoints(null, PaMbQ[4], input),
				axp = getValueOfProjectivityAt(null, ax),
				cxp = getValueOfInverseProjectivityAt(null, cx),
				b = P2.pointFromLines(null, ax, axp),
				d = P2.pointFromLines(null, cx, cxp),
				R = P2.pointFromLines(null, axp, cxp),
				ac = P2.lineFromPoints(null, PaMbQ[0], PaMbQ[4]),
				bd = P2.lineFromPoints(null, b, d),
				Q = P2.pointFromLines(null, ac, bd);
		output = P2.lineFromPoints(output, R, Q);	
		Pn.dehomogenize(R, R);
		Pn.dehomogenize(Q, Q);
		System.err.println("Polarize variables = "+Rn.toString(new double[][]{ax,cx,axp,cxp,b,d,R,ac,bd,Q,output}));
		return output;
	}

}
