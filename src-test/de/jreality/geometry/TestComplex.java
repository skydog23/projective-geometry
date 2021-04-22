/*
 * Author	gunn
 * Created on Feb 6, 2006
 *
 */
package de.jreality.geometry;

import junit.framework.TestCase;
import charlesgunn.math.Complex;
import charlesgunn.math.ComplexPolynomial;


public class TestComplex extends TestCase {
	Complex a = new Complex(1,0);
	Complex b = new Complex(0,1);
	Complex c = new Complex(0,0);
	Complex f = new Complex(3,2);
	Complex d = new Complex(Double.POSITIVE_INFINITY,0);
	Complex e = new Complex(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY);
	Complex g = new Complex(-1,0);

	public void testComplex()	{
		Complex a = new Complex(1,0);
		Complex b = new Complex(0,1);
		Complex c = new Complex(1,1);
		System.err.println("a*b: "+Complex.times(null, a, b));
		System.err.println("b^-1: "+Complex.invert(null, b));
	}
	
	public void testInfinity()	{
		System.err.println("-d: "+Complex.negate(null, d));
		System.err.println("1+inf: "+Complex.add(null, a, d));
		System.err.println("1*inf: "+Complex.times(null, a, d));
		System.err.println("0*inf: "+Complex.times(null, c, d));
		System.err.println("1/inf: "+Complex.invert(null,  d));
		System.err.println("inf-inf: "+Complex.subtract(null, e, d));
		System.err.println("length(inf): "+Complex.length(d));
	}
	
	public void testQuadraticSolve()	{
		Complex[] solns = Complex.quadraticSolve(null, a, c, a);
		System.err.println("Solutions:"+solns[0]+":"+solns[1]);
		solns = Complex.quadraticSolve(null, a, f, b);
		System.err.println("Solutions:"+solns[0]+":"+solns[1]);
	}
	
	public void testComplexPolynomialTimes()	{
		Complex[] p1 = {new Complex(0,1), new Complex(1,0)};	// z + i
		Complex[] p2 = {new Complex(1,0), new Complex(2,0)};	// 2z + 1
		ComplexPolynomial cp1 = new ComplexPolynomial(p1);
		ComplexPolynomial cp2 = new ComplexPolynomial(p2);
		ComplexPolynomial cp3 = ComplexPolynomial.times(cp1, cp2);  // should be 2z^2+(1+2i)z+i
		System.err.println("Result is "+cp3.toString());
	}
	public void testRootSet()	{
		ComplexPolynomial p1 = new ComplexPolynomial(new Complex[]{g,c,c,c,c,c,c,c,a});  //z^8=1
		Complex[] roots = Complex.rootSet(p1, 8, new  Complex(Math.random(), Math.random()));
		System.err.println("Found "+roots.length+" roots");
		for (int i = 0; i<roots.length; ++i)	{
			System.err.println(roots[i].toString());
		}
	}
	public void testRootSet2()	{
		ComplexPolynomial p1 = new ComplexPolynomial(new Complex[]{a,new Complex(3,0), new Complex(3,0),a});  //z^3=0
		Complex[] roots = Complex.rootSet(p1, 3, new  Complex());
		System.err.println("Found "+roots.length+" roots");
		for (int i = 0; i<roots.length; ++i)	{
			System.err.println(roots[i].toString());
		}
	}
}
