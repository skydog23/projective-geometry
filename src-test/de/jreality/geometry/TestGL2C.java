/*
 * Author	gunn
 * Created on Feb 6, 2006
 *
 */
package de.jreality.geometry;

import junit.framework.TestCase;
import charlesgunn.math.CP1;
import charlesgunn.math.Cn;
import charlesgunn.math.Complex;
import charlesgunn.math.ComplexPolynomial;
import charlesgunn.math.PSL2C;
import de.jreality.math.Rn;

public class TestGL2C extends TestCase {

	public void testGL2C()	{
		Complex a = new Complex(1,0);
		Complex b = new Complex(0,1);
		Complex c = new Complex(1,0);
		Complex d = new Complex(0,-1);
		PSL2C aa = new PSL2C();
		PSL2C bb = new PSL2C(a,b,c,d);
		PSL2C cc = PSL2C.invert(null, bb);
		System.err.println("A: "+aa.toString());
		System.err.println("A*A: "+PSL2C.times(null, aa, aa).toString());
		System.err.println("A*B: "+PSL2C.times(null, aa, bb).toString());
		System.err.println("B^-1: "+cc.toString());
		PSL2C result = PSL2C.times(null, cc, bb);
		Cn.normalize(result.m,result.m);
		System.err.println("B*B^-1: "+result.toString());
	}
	
	public void testHIFLFT()	{
		Complex a = new Complex(3,1);
		Complex b = new Complex(3,5);
		Complex c = new Complex(1,0);
		Complex d = new Complex(6,1);
		PSL2C aa = new PSL2C();
		PSL2C bb = new PSL2C(a,b,c,d);
		double[] m = CP1.convertPSL2CToSO31(null, bb.m);
		System.err.println("Result:\n"+Rn.matrixToString(m));
	}
	
	public void testStandardMoebius()	{
		Complex a = new Complex(3,1);
		Complex b = new Complex(3,5);
		Complex c = new Complex(1,0);
		Complex[] m = CP1.standardProjectivity(null, a,b,c);
		PSL2C g = new PSL2C(m);
		Complex x,y,z;
		x = CP1.times(null, g.m, a);
		y = CP1.times(null, g.m, b);
		z = CP1.times(null, g.m, c);
		System.err.println("a goes to "+x.toString());
		System.err.println("b goes to "+y.toString());
		System.err.println("c goes to "+z.toString());
	}
	
	public void testGeneralMoebius()	{
		Complex a = new Complex(3,1);
		Complex b = new Complex(3,5);
		Complex c = new Complex(1,0);
		Complex d = new Complex(-2,1);
		Complex e = new Complex(6,0);
		Complex f = new Complex(0,0);
		Complex[] m = CP1.projectivity(null, a,b,c, d,e,f);
		PSL2C g = new PSL2C(m);
		Complex x,y,z;
		x = Complex.subtract(null, d, CP1.times(null, g.m, a));
		y = Complex.subtract(null, e, CP1.times(null, g.m, b));
		z = Complex.subtract(null, f, CP1.times(null, g.m, c));
		System.err.println("a error "+x.toString());
		System.err.println("b error "+y.toString());
		System.err.println("c error "+z.toString());
	}
	public void testGeneralMoebius2()	{
		Complex a = new Complex(0,0);
		Complex b = new Complex(1,0);
		Complex c = new Complex(-1,0);
		Complex e = new Complex(6,0);
		Complex f = new Complex(0,0);
		Complex d = CP1.completeCrossRatio(null, a,b,c,c);
		Complex[] m = CP1.projectivity(null, a,b,c, c,d,a);
		PSL2C g = new PSL2C(m);
		Complex x,y,z;
		x = Complex.subtract(null, c, CP1.times(null, g.m, a));
		y = Complex.subtract(null, d, CP1.times(null, g.m, b));
		z = Complex.subtract(null, a, CP1.times(null, g.m, c));
		System.err.println("a error "+x.toString());
		System.err.println("b error "+y.toString());
		System.err.println("c error "+z.toString());
	}

	public void testCrossRatio()	{
		Complex a = new Complex(0,0);
		Complex b = new Complex(1,0);
		Complex c = new Complex(1,1);
		Complex d = new Complex(1,-1);
		Complex xr = CP1.crossRatio(null, a,b,c,d);
		System.err.println("xratio error "+xr.toString());
	}
	public void testCompleteXRatio()	{
		Complex a = new Complex(-1,0);
		Complex b = new Complex(0,0);
		Complex c = new Complex(1,0);
		Complex x = new Complex(-1,0);
		Complex d = CP1.completeCrossRatio(null, a,b,c,x);
		Complex xx = CP1.crossRatio(null, a,b,c,d);
		System.err.println("xratio "+Complex.subtract(null, xx,x).toString());
		
	}

	public void testCompleteXRatio2()	{
		Complex a = new Complex(3,1);
		Complex b = new Complex(3,5);
		Complex c = new Complex(1,0);
		Complex x = new Complex(2,0);
		Complex d = CP1.completeCrossRatio(null, a,b,c,x);
		Complex xx = CP1.crossRatio(null, a,b,c,d);
		System.err.println("xratio error "+Complex.subtract(null, xx,x).toString());
		
	}

	public void testQuadraticPolynomial()	{
		Complex a = new Complex(0,0);
		Complex b = new Complex(1,0);
		Complex c = new Complex(1,1);
		ComplexPolynomial qp = new ComplexPolynomial(b,b,b);
		System.err.println("Value at "+c+"is "+qp.valueAt(c));
		
	}
	public void testNewtonsMethods()	{
		Complex a = new Complex(0,0);
		Complex b = new Complex(1,0);
		Complex c = new Complex(1,1);
		ComplexPolynomial qp = new ComplexPolynomial(new Complex(1.0,0),b,new Complex(1,0));
		Complex root = Complex.newtonsMethod(qp, a); //new Complex(.35, .87));
		System.err.println("Root is "+root);
		System.err.println("Value is "+qp.valueAt(root));
	}
	
}
