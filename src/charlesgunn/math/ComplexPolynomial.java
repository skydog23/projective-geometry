/*
 * Author	gunn
 * Created on Feb 17, 2006
 *
 */
package charlesgunn.math;

public class ComplexPolynomial implements ComplexFunction {
	Complex[] coeffs; 
	public ComplexPolynomial(Complex a, Complex b, Complex c) {
		super();
		coeffs =new Complex[3];
		coeffs[0] = c;
		coeffs[1] = b;
		coeffs[2] = a;
	}
	
	public ComplexPolynomial(Complex[] c)	{
		super();
		coeffs = c;
	}
	
	public Complex valueAt(Complex z) {
		Complex value = new Complex(0,0);
		int n = coeffs.length;
		for (int i =n-1; i>=0; --i)	{
			Complex.times(value, value, z);
			Complex.add(value, value, coeffs[i]);
		}
		return value;
	}
	
	public int getDegree()	{
		return coeffs.length - 1;
	}

	public static ComplexPolynomial times(ComplexPolynomial a, ComplexPolynomial b)	{
		int n = a.getDegree();
		int m = b.getDegree();
		int p = n+m+1;
		Complex[] coeffs = new Complex[p];		
		for (int i = 0; i<p; ++i)	{
			coeffs[i] = new Complex();
			for (int k = 0; k <= i; ++k)	{
				if (k > n || (i-k) > m) continue;
				//System.err.println("k,i-k:"+k+":"+(i-k));
				Complex.add(coeffs[i], coeffs[i], Complex.times(null, a.coeffs[k], b.coeffs[i-k]));
			}
		}
		return new ComplexPolynomial(coeffs);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("ComplexPolynomial: ");
		for (int i = 0; i<coeffs.length; ++i)	sb.append("a"+i+":\t"+coeffs[i].toString());
		return sb.toString();
	}
	
}
