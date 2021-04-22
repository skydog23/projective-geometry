package charlesgunn.math;

import java.util.logging.Level;

import junit.framework.TestCase;
import charlesgunn.math.Biquaternion.Metric;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Quaternion;
import de.jreality.math.Rn;
import de.jtem.projgeom.P5;
import de.jtem.projgeom.PlueckerLineGeometry;
import jdk.nashorn.internal.runtime.BitVector;

public class TestBiquaternion extends TestCase {
	Level logLevel = Level.INFO;
	public void testBiquatFromMatrix()	{
		P5.logger.log(logLevel,"\nBiquat from matrix");
		double[][] axes = {{1,0,0},{0,1,0},{0,0,1}};
		double[] rot = new double[16];
		for (Metric m : Metric.values()) {
//			for (int i = 0; i<3; ++i)	{
//				MatrixBuilder.init(null, m.getInteger()).translate(new double[]{0,0,0,1},new double[]{.5,0,0,1}).rotate(Math.PI/4, axes[i]).assignTo(rot);
				MatrixBuilder.init(null, m.getInteger()).translate(new double[]{0,0,0,1},new double[]{.5,0,0,1}).assignTo(rot);
				Biquaternion biq = Biquaternion.biquaternionFromDirectIsometry(null, rot, m);
				double[] rot2 = Biquaternion.matrixFromBiquaternion(null, biq);
				P5.logger.log(logLevel,"rot = "+Rn.matrixToString(rot));
				P5.logger.log(logLevel,"rot2 = "+Rn.matrixToString(rot2));
//				P5.logger.log(logLevel,"error = "+Rn.matrixToString(Rn.subtract(null, rot2, rot)));
				IsometryAxis ia = new IsometryAxis(biq);
//			}
		}
	}
	
	public void testBiquaternionFromMatrix2()	{
		double[] keytimes = new double[7];		
		double step = 1.0;
		double[] pt1 = {1,1,1,1}, pt3 = {1,-1,-1,-1};
		double[] line = PlueckerLineGeometry.lineFromPoints(null, pt1, pt3);
		double[] pline = PlueckerLineGeometry.polarize(null, line, Pn.ELLIPTIC);
		Rn.normalize(pline, pline);
		System.err.println("screw motion: "+Rn.toString(pline));
		Biquaternion angleScrew = new Biquaternion(Metric.ELLIPTIC);
		angleScrew.getRealPart().re =  -1.0;
		angleScrew.getDualPart().re = 1.0/3.0;
		Biquaternion axis = new Biquaternion(pline, Metric.ELLIPTIC);
		Biquaternion foo = Biquaternion.times(null, axis, axis);
		System.err.println("axis^2= "+foo.toString());
		Biquaternion onestep = Biquaternion.exp(null, axis, angleScrew, Math.PI/4);
		double[] onestepM = Biquaternion.matrixFromBiquaternion(null, onestep);
		double[] acc = Rn.identityMatrix(4);
		{
			for (int i = 0; i<7; ++i)	{
				keytimes[i] = i*step;
//				Biquaternion val = Biquaternion.exp(null, axis, angleScrew, 2*Math.PI* keytimes[i]/6.0);	
				Biquaternion val = Biquaternion.biquaternionFromDirectIsometry(null, acc,Metric.ELLIPTIC);	
				double[] mat = Biquaternion.matrixFromBiquaternion(null, val);	
				Biquaternion val2 = Biquaternion.biquaternionFromDirectIsometry(null, mat,Metric.ELLIPTIC);	
				System.err.println("Biq = "+val.toString());
				System.err.println("Biq2 = "+val2.toString());
				Rn.times(acc, acc, onestepM);
			}
		}

	}
	
	public void testEllipticTlate()	{
		Biquaternion biv = new Biquaternion(Metric.ELLIPTIC);
		biv.qr = new Quaternion(0,1,1,1);
		biv.qd = new Quaternion(0,1,1,1);
		Biquaternion bivn = Biquaternion.normalize(null,biv);
		System.err.println("normalized biq = "+bivn.toString());
		biv.qr.re = 1.0;
		biv.qd.re = .5;
		bivn = Biquaternion.normalize(null,biv);
		System.err.println("normalized biq = "+bivn.toString());
		biv.qr.re =  .5;
		biv.qd.re = .5;
		bivn = Biquaternion.normalize(null,biv);
		System.err.println("normalized biq = "+bivn.toString());
	}
	
	public void testNorm() {
		P5.logger.log(logLevel,"\ntestNorm");
		Quaternion q1 = new Quaternion(1, 2, 3, 4);
		Quaternion q2 = new Quaternion(1, -3, -1.5, 2);
		Biquaternion bq = new Biquaternion(q1, q2, Metric.ELLIPTIC);
		Biquaternion bs = Biquaternion.norm(null, bq);
		P5.logger.log(logLevel,"bs = " + bs.toString());
		Biquaternion normed = Biquaternion.normalize(null, bq);
		P5.logger.log(logLevel,"normed = " + normed.toString());
	}

	public void testConjugate() {
		P5.logger.log(logLevel,"\ntestConjugate");
		Quaternion q1 = new Quaternion(1, 2, 3, 4);
		Quaternion q2 = new Quaternion(1, 0, 1, 0);
		Biquaternion bq = new Biquaternion(q1, q2, Metric.ELLIPTIC);

		P5.logger.log(logLevel,"conj = "
				+ Biquaternion.conjugate(null, bq).toString());
		P5.logger.log(logLevel,"dualconj = "
				+ Biquaternion.dualConjugate(null, bq).toString());
	}

	public void testTimes() {
		P5.logger.log(logLevel,"\ntestTimes");
		Quaternion q1 = new Quaternion(1, 2, 3, 4);
		Quaternion q2 = new Quaternion(1, 0, 1, 0);
		Biquaternion bq = new Biquaternion(q1, q2, Metric.ELLIPTIC);
		Quaternion p1 = new Quaternion(0, 0, 3, 4);
		Quaternion p2 = new Quaternion(0, 1, 1, 0);
		Biquaternion bp = new Biquaternion(p1, p2, Metric.ELLIPTIC);
		Biquaternion dst = Biquaternion.times(null, bq, bp);
		P5.logger.log(logLevel,"dst = " + dst.toString());
	}

	public void testInverse() {
		P5.logger.log(logLevel,"\nInverse test");
		for (Metric m : Metric.values()) {
			Biquaternion bq = new Biquaternion(1,2, m);
			Biquaternion bi = Biquaternion.inverse(null, bq);
			P5.logger.log(logLevel,"inverse = "+bi.getRealPart().re+" "+bi.getDualPart().re);
			Biquaternion pro = Biquaternion.times(null, bi, bq);
			P5.logger.log(logLevel,"product = "+pro.getRealPart().re+" "+pro.getDualPart().re);
		}
	}

	public void testSqrt() {
		P5.logger.log(logLevel,"\nSqrt test");
		for (Metric m : Metric.values()) {
			Biquaternion bq = new Biquaternion(2,1, m);
			Biquaternion bi = Biquaternion.sqrt(null, bq);
			P5.logger.log(logLevel,"sqrt = "+bi.getRealPart().re+" "+bi.getDualPart().re);
			Biquaternion pro = Biquaternion.times(null, bi, bi);
			Biquaternion.add(pro, Biquaternion.times(null, -1, bq), pro);
			P5.logger.log(logLevel,"x - sq^2 = "+pro.getRealPart().re+" "+pro.getDualPart().re);
		}
	}

	public void testSpecialize() {
		P5.logger.log(logLevel,"\nspecialize test");
		double[][] complexes = {{1,0,0,0,0,.5},{1,0,0,.5,0,0}};
		for (Metric m : Metric.values()) {
			for (double[] complex : complexes) {
				Biquaternion bq = new Biquaternion(complex, m);
				Biquaternion bi = Biquaternion.specialize(null, bq);
				P5.logger.log(logLevel,"before = "+bq);				
				P5.logger.log(logLevel,"after = "+bi);				
			}
		}
	}

	public void testLine()	{
		P5.logger.log(logLevel,"\nisLine test");
		double[] point = {1,2,3,4};
		for (Metric m : Metric.values()) {
			Quaternion q1 = new Quaternion(0, 0, 0, 0);
			Quaternion q2 = new Quaternion(0, 1, 0, 0);
			Biquaternion bq = new Biquaternion(q1, q2, m);	
			P5.logger.log(logLevel,"it's a line: "+Biquaternion.isLine(bq));
		}
	}
	public void testPoint() {
		P5.logger.log(logLevel,"\npoint test");
		double[] p = { 1, 0, 0, 1 };
		Opposition point = Opposition.point(p, Metric.ELLIPTIC);
		Quaternion q1 = new Quaternion(1, 2, 3, 4);
		Quaternion q2 = new Quaternion(2, 0, 1, 0);
		Biquaternion bq = new Biquaternion(q1, q2, Metric.ELLIPTIC);
		Opposition plane = Opposition.times(null, point, bq);
		P5.logger.log(logLevel,"plane = " + plane.toString());
		Biquaternion bq1 = Biquaternion.conjugate(null, bq);
		Opposition point1 = Opposition.times(null, bq1, plane);
		P5.logger.log(logLevel,"point1 = " + point1.toString());
	}

	public void testMatrixFrom() {
		P5.logger.log(logLevel,"\ntestMatrixFrom");
		Quaternion q1 = new Quaternion(0, 0, 0, 0);
		Quaternion q2 = new Quaternion(1, 0, 0, 0);
		q1 = Quaternion.normalize(q1, q1);
		q2 = Quaternion.normalize(q2, q2);
		for (Metric sig : Metric.values()) {
			int sigi = sig.getInteger();
			Biquaternion bq = new Biquaternion(q1, q2, sig);
			double[] mat = Biquaternion.matrixFromBiquaternion(null, bq);
			// P3.orthonormalizeMatrix(null, mat, 10E-8, sigi);
			P5.logger.log(logLevel,sigi + " mat = " + Rn.matrixToString(mat));
		}
	}

	public void testWedge() {
		P5.logger.log(logLevel,"\ntestWedge");
		Quaternion q1 = new Quaternion(0, 2, -3, 4);
		Quaternion q2 = new Quaternion(0, 0, 1, 0);
		Biquaternion bq = new Biquaternion(q1, q2, Metric.HYPERBOLIC);
		Quaternion p1 = new Quaternion(0, 0, 3, 4);
		Quaternion p2 = new Quaternion(0, -1, 0, 1);
		Biquaternion bp = new Biquaternion(p1, p2, Metric.HYPERBOLIC);
		Biquaternion dst = Biquaternion.commutator(null, bq, bp);
		P5.logger.log(logLevel,"wedge = " + dst.toString());
		Biquaternion bq1 = Biquaternion.innerProduct(null, bq, dst), 
				bp1 = Biquaternion.innerProduct(null, bp, dst);
		P5.logger.log(logLevel,"q.wedge = " + bq1.toString());
		P5.logger.log(logLevel,"p.wedge = " + bp1.toString());
	}

	public void testAxis() {
		P5.logger.log(logLevel,"\ntestAxis");
		for (Metric m : Metric.values()) {
			{
				Quaternion q1 = new Quaternion(0, 0, 0, 1); // 2,3);
				Quaternion q2 = new Quaternion(0, 0, 0, .5); // 4,5,4);
				Biquaternion bq = new Biquaternion(q1, q2, m);
				// Biquaternion.normalize(bq, bq);
				Biquaternion dst = Biquaternion.axisForBivector(null, bq);
				P5.logger.log(logLevel,"axis = " + dst.toString());
			}
			{
				Quaternion q1 = new Quaternion(0, 1, 2, 3); // 2,3);
				Quaternion q2 = new Quaternion(0, 4, 5, 4); // 4,5,4);
				Biquaternion bq = new Biquaternion(q1, q2, m);
				// Biquaternion.normalize(bq, bq);
				Biquaternion dst = Biquaternion.axisForBivector(null, bq);
				P5.logger.log(logLevel,"axis = " + dst.toString());
			}
		}

	}
		

	public void testBiqFromMatrix()	{
		P5.logger.log(logLevel,"\ntestBiqFromMatrix");
		double[][] axes = {{1,0,0},{0,1,0},{0,0,1}};
		double[] rot = new double[16];
		//Metric m = Metric.ELLIPTIC;
		for (Metric m : Metric.values()) {
			for (int i = 0; i<3; ++i)	{
				MatrixBuilder.init(null, m.getInteger()).
					translate(new double[]{0,0,0,1},new double[]{.5,0,0,1}).rotate(Math.PI/4, axes[i]).assignTo(rot);
				Biquaternion biq = Biquaternion.biquaternionFromDirectIsometry(null, rot, m);
				double[] rot2 = Biquaternion.matrixFromBiquaternion(null, biq);
				P5.logger.log(logLevel,"biq = "+biq);
//				P5.logger.log(logLevel,"rot = "+Rn.matrixToString(rot));
//				P5.logger.log(logLevel,"rot2 = "+Rn.matrixToString(rot2));
				P5.logger.log(logLevel,"error = "+Rn.matrixToString(Rn.subtract(null, rot2, rot)));
			}
		}
	}
	public void testExp()	{
		P5.logger.log(logLevel,"\ntestExp");
		Matrix foo = new Matrix();
		MatrixBuilder.init(null, Pn.ELLIPTIC).translate(new double[]{0,0,0,1},new double[]{.5,0,0,1}).rotateX(Math.PI/2).assignTo(foo);
		Biquaternion biq = Biquaternion.biquaternionFromDirectIsometry(null, foo.getArray(), Metric.ELLIPTIC);
		IsometryAxis isom = new IsometryAxis(biq);
		for (double t = 0; t <= 1.0; t+= .25)	{
			biq = isom.exp(t);
			P5.logger.log(logLevel,"t = "+t+"\n"+biq);
		}
	}
	
	private static final double[] pointRefl = {-1,0,0,0, 0,-1,0,0,  0,0,-1,0, 0,0,0,1};
	public void testIndirectIsometry()	{
		P5.logger.log(logLevel,"\ntestIndirectIsometry");
		for (Metric metric : Metric.values()) {
//		Metric metric = Metric.EUCLIDEAN;
			double[] m = MatrixBuilder.init(null, metric.getInteger()).translate(0,.5,0).scale(-1,-1,-1).getArray();
			Biquaternion[] biq = Biquaternion.biquaternionsFromIndirectIsometry(null, m, metric);
			double[] rm = Biquaternion.matrixFromBiquaternion(null, biq[0]),
				sm = Biquaternion.matrixFromBiquaternion(null, biq[1]);
			if (rm[15] >0) Rn.times(rm,-1,rm);
			for (int i = 0; i<4; ++i) rm[12+i] = -rm[12+i];
			P5.logger.log(logLevel,"Matrix = "+Rn.matrixToString(m));
			P5.logger.log(logLevel,"Matrix2 = "+Rn.matrixToString(Rn.times(null, rm, sm)));
//			IsometryAxis isom = new IsometryAxis(biq);			
		}
	}
	
	public void testLogarithm() {
		P5.logger.log(logLevel,"\ntestLogarithm");
		Quaternion q1 = new Quaternion(0,0,0,1);
		Quaternion q2 = new Quaternion(0,0,0,1);
		Biquaternion bq = new Biquaternion(q1, q2, Metric.EUCLIDEAN);
		Biquaternion pitch = new Biquaternion( Metric.EUCLIDEAN);
		pitch.qr.re = 1.0; pitch.qd.re = 3.6;
		Biquaternion exp = Biquaternion.exp(null, bq, pitch, 1.0);
		P5.logger.log(logLevel,"exp = " + exp.toString());
		Biquaternion normed = Biquaternion.normalize(null, exp);
		P5.logger.log(logLevel,"normed = " + normed.toString());
	}
	
	public void testNormalize() {
		P5.logger.log(logLevel,"\ntestNormalize");
		
		Quaternion q1 = new Quaternion(1, 2, 3, 1);
		Quaternion q2 = new Quaternion(1, 10,0,0);
		Biquaternion bq = new Biquaternion(q1, q2, Metric.EUCLIDEAN);
		Biquaternion normed = Biquaternion.normalize(null, bq);
		P5.logger.log(logLevel,"normed = " + normed.toString());
	}


//	public void testPrintout()	{
//		double[] isom = 
//		{1.00000,	-3.94299e-16,	-1.31328e-31,	-2.77556e-16,
//				3.33067e-16,	-1.10934e-31,	1.00000,	0.00000,
//				6.12323e-17,	1.00000,	3.33067e-16,	1.11022e-16,
//				0.00000,	0.00000,	0.00000,	1.00000
//				};		
//		{-3.33067e-16,	1.00000,	-9.05391e-32,	0.500000,
//				-1.00000,	-1.10934e-31,	-2.71835e-16,	0.500000,
//				-6.12323e-17,	-3.33067e-16,	-1.00000,	-0.500000,
//				0.00000,	0.00000,	0.00000,	1.00000
//				};
//		Biquaternion[] biq = Biquaternion.biquaternionsFromIndirectIsometry(null, isom, Metric.EUCLIDEAN);
//		double[] rm = Biquaternion.matrixFromBiquaternion(null, biq[0]),
//			sm = Biquaternion.matrixFromBiquaternion(null, biq[1]);
//		if (rm[15] >0) Rn.times(rm,-1,rm);
//		for (int i = 0; i<4; ++i) rm[12+i] = -rm[12+i];
//		P5.logger.log(logLevel,"Matrix = "+Rn.matrixToString(isom));
//		P5.logger.log(logLevel,"Matrix2 = "+Rn.matrixToString(Rn.times(null, rm, sm)));
//	}

}
