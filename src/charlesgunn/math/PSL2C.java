/*
 * Author	gunn
 * Created on Feb 6, 2006
 *
 */
package charlesgunn.math;

import charlesgunn.jreality.AbstractDeformation;
import de.jreality.math.Pn;

public class PSL2C {
	public Complex[] m;
	String word = "";
	public Complex[] tmp = new Complex[2];
	private static boolean debug = false;
	public final static PSL2C IDENTITY = new PSL2C(new Complex(1,0), new Complex(0,0), new Complex(0,0), new Complex(1,0));
	
	public PSL2C(PSL2C src) {
		this(src.m, src.getWord());
	}
	
	public PSL2C(Complex[] mm) {
		this(mm,null);
	}
	
	public PSL2C(Complex a, Complex b, Complex c, Complex d) {
		super();
		m = new Complex[4];
		m[0]=Complex.copy(null,a);
		m[1]=Complex.copy(null,b);
		m[2]=Complex.copy(null,c);
		m[3]=Complex.copy(null,d);
	}
	
	public PSL2C()	{
		super();
		m = new Complex[4];
		m[0]=new Complex(1,0);
		m[1]=new Complex(0,0);
		m[2]=new Complex(0,0);
		m[3]=new Complex(1,0);
	}

	public PSL2C(Complex[] aa, String string) {
		super();
		m = Complex.copy(null, aa);
		if (string != null) word = string;
	}

	public void setWord(String w)	{
		if (w!=null) word = w;
	}
	
	public String getWord()	{
		return word;
	}
	
	public String toString()	{
		return "Matrix:\t"+m[0].toString()+"\t"+m[1].toString()+"\n"+m[2].toString()+"\t"+m[3].toString()+"\nWord: "+word;
	}

	public static PSL2C[] copy(PSL2C[] dst, PSL2C[] src) {
		if (dst == null || dst.length != src.length)	dst = new PSL2C[src.length];
		for (int i = 0; i<src.length; ++i)	
			dst[i] = new PSL2C(src[i]);
		return dst;
	}

	public static PSL2C times(PSL2C dst, PSL2C src1, PSL2C src2)		{
		if (dst == null) dst = new PSL2C();
		Cn.times(dst.m, src1.m, src2.m);
		dst.word = simplifyWord(src1.word+src2.word);

		return dst;
	}
		
	public static PSL2C conjugateBy(PSL2C dst, PSL2C g, PSL2C h) {
		PSL2C ic = invert(null, h);
		return times(dst, h, times(null, g, ic));
	}
	
	public static PSL2C invert(PSL2C dst, PSL2C src)	{
		if (dst == null) dst = new PSL2C();
		Complex[] result = Cn.invert(dst.m, src.m);
		if (dst.m != result) dst.m = result;
		dst.setWord(invertWord(src.getWord()));		
		return dst;
	}
	
	public static AbstractDeformation  deformationFor(final PSL2C tform)	{
		return new AbstractDeformation() {
			
			@Override
			public double[] valueAt(double[] input, double[] output) {
				Complex in = new Complex(input[0], input[1]);
				Complex out = CP1.times(null, tform.m, in);
				if (output == null) output = new double[2];
				output[0] = out.re;  output[1] = out.im;
				return output;
			}
		};
	}
	public static Complex[] attractiveFixedPoints(Complex[] dst, PSL2C[] src)	{
		if (dst == null) dst = new Complex[src.length];
		for (int i = 0; i<src.length; ++i)	{
			dst[i] = Cn.attractiveFixedPoint(null, src[i].m);
		}
		return dst;
//		commutatorFixedPoints[i][1] = Cn.attractiveFixedPoint(null, commutators[i][0].m);
//		commutatorFixedPoints[i][0] = Cn.attractiveFixedPoint(null, commutators[i][1].m);
//		Complex test = CP1.times(null, els[i][0].m, commutatorFixedPoints[i][0]);
//		if (debug) System.err.println("beginComm error: "+Complex.subtract(null, test, commutatorFixedPoints[i][0]));
//		test = CP1.times(null, els[i][1].m, commutatorFixedPoints[i][1]);
//		if (debug) System.err.println("endComm error: "+Complex.subtract(null, test, commutatorFixedPoints[i][1]));

	}
	public static Complex[][] attractiveFixedPoints(Complex[][] dst, PSL2C[][] src) {
		if (dst == null || dst.length!= src.length)	dst = new Complex[src.length][];
		for (int i = 0; i<src.length; ++i) 
			dst[i] = attractiveFixedPoints(dst[i], src[i]);
		return dst;
	}

	/**
	 * @return
	 */
	public static String invertWord(String word) {
		char[] thisWord = word.toCharArray();
		int n = thisWord.length;
		char[] invWord = new char[n];
		for (int i = 0; i<n; ++i)	{
			char tc = thisWord[i];
			if (Character.isLowerCase(tc))	invWord[n-i-1] = Character.toUpperCase(tc);
			else							invWord[n-i-1] = Character.toLowerCase(tc);
		}
		return new String(invWord);
	}
	public static Complex derivativeAt(Complex der, Complex[] m, Complex z0)	{
		Complex denom = Complex.add(null, Complex.times(null, m[2], z0), m[3]);
		denom = Complex.times(null, denom, denom);
		return Complex.divide(der, Cn.determinant(m), denom );
	}
	public static boolean isNearlyParabolic(PSL2C g, double tol) {
		return isNearlyParabolic(g, tol, .0001);
	}
	
	public static boolean isNearlyParabolic(PSL2C g, double tol1, double tol2) {
		Complex trace = Cn.trace(g.m);
		// reject elements that are nearly elliptic (but not nearly hyperbolic)
		//if (Math.abs(trace.re) < 2.0 && Math.abs(trace.im) < tol2)	return false;
		double diff = Complex.abs(new Complex(trace.re-2,trace.im));
		if (diff < tol1) return true;
		if (debug) 
			System.err.println("trace"+diff);
		diff = Complex.abs(new Complex(trace.re+2,trace.im));		
		if (diff < tol1)		return true;
		return false;
	}

	public static boolean isConjugate(String w)	{
		char f = w.charAt(0);
		char l = w.charAt(w.length()-1);
		if (f==l) return false;
		if (f == Character.toUpperCase(l) || f == Character.toLowerCase(l)) return true;
		return false;
	}

	public static boolean isPower(String word2) {
		int n = word2.length();
		if (n == 1) return false;
		for (int wordSize = 1; wordSize< n; ++wordSize)	{
			if (n%wordSize != 0) continue;		// has to divide into length evenly
			String sub = word2.substring(0,wordSize);
			int subIndex = wordSize;
			while(word2.indexOf(sub, subIndex) == subIndex && subIndex < n)
					subIndex += wordSize;
			if (subIndex >= n) return true;
		}
		return false;
	}

	public static boolean isPossiblyPQ(String w) {
		boolean doubleA = w.indexOf("aa") != -1 || w.indexOf("AA") != -1;
		boolean doubleB = w.indexOf("bb") != -1 || w.indexOf("BB") != -1;
		return !(doubleA && doubleB);
	}
	
	private static String simplifyWord(String string) {
		int n = string.length();
		if (n <= 1) return string;
		char l = string.charAt(0);
		char r;
		StringBuffer sb = new StringBuffer();
		int i;
		for (i = 1; i<n; ++i)	{
			r = string.charAt(i);
			// look for cancellations in the word
			if (l != r && (l == Character.toLowerCase(r) || l == Character.toUpperCase(r))) {
				i++;
				if (i<n) r = string.charAt(i);
			}
			else sb.append(l);
			l = r;
		}
		if (i <= n) sb.append(l);
		return sb.toString();
	}

	public static PSL2C elementForWord(String w, PSL2C g1, PSL2C g2)	{
		int n = w.length();
		PSL2C result = new PSL2C();
		for (int i = 0; i<n; ++i)	{
			String foo = w.substring(i,i+1);
			if (foo.equals(g1.getWord())) times(result, result, g1);
			else if (foo.equals( g2.getWord())) times(result, result, g2);
		}
		return result;
	}

	/**
	 * Create a dehn twist that inserts a cylinder of length alpha and the parallel translation of phi
	 * @param alpha
	 * @return
	 */public static PSL2C dehnTwist(double alpha, double phi) {
		 Complex ch = new Complex(Pn.cosh(phi/2), 0.0);
		 Complex sh = new Complex(-Pn.sinh(phi/2),0.0);
		PSL2C opening = new PSL2C(ch, sh, sh, ch);
		 ch = new Complex(Math.cos(alpha/2), 0.0);
		 sh = new Complex(0.0, -Math.sin(alpha/2));
		 PSL2C twist = new PSL2C(ch, sh, sh, ch);
		 return times(null, opening, twist);
	}


}
