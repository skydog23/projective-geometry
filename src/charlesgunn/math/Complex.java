/*
 * Created on Dec 18, 2004
 *
 */
package charlesgunn.math;


/**
 * @author gunn
 *
 */
public class Complex {
	private static boolean debug = false;
		public double re, im;
		private static final Complex INFINITE_COMPLEX = new Complex(Double.POSITIVE_INFINITY, 0.0);
		private static final double[] INFINITE_COMPLEX_POLAR = new double[]{Double.POSITIVE_INFINITY, 0.0};
		public static final Complex ONE = new Complex(1,0);
		public static final Complex TWO = new Complex(2,0);
		public static final Complex I = new Complex(0,1);
		public static final Complex ZERO = new Complex(0,0);
		/**
		 * No transforms allowed
		 */
		public Complex() {
			this(0d, 0d);
		}
		
		public Complex(Complex nq) {
			this(nq.re, nq.im);
		}
		
		public Complex(double r, double  i)	{
			super();
			re = r;
			im = i;
		}
		
		public String toString()	{
			if (this == INFINITE_COMPLEX) return "Infinity";
			return Double.toString(re)+" + "+Double.toString(im)+" i";
		}
		
		public boolean isInfinite()	{
			return re==Double.POSITIVE_INFINITY || im==Double.POSITIVE_INFINITY || re==Double.NEGATIVE_INFINITY || im==Double.NEGATIVE_INFINITY;
		}
		
		public double abs() {
			return length(this);
		}

		public static double abs(Complex c)	{
			return c.abs();
		}
		
		public void setValue(double r, double i)	{
			re = r;
			im = i;
		}
		
		public void zero()	{
			re = im = 0.0;
		}
		
		public void infinity()	{
			re = im = Double.POSITIVE_INFINITY;
		}
		
		// static methods start here
		public static Complex copy(Complex dst, Complex src)	{
			if (dst == null) return new Complex(src);
			dst.re = src.re;
			dst.im = src.im;
			return dst;
		}
		

		public static Complex[] copy(Complex[] dst, Complex[] src) {
			if (dst == null || dst.length != src.length) dst = new Complex[src.length];
			for (int i = 0; i<src.length; ++i)
				dst[i] = copy(dst[i], src[i]);
			return dst;
		}
		
		public static boolean equals(Complex a, Complex b)	{
			return equals(a,b,0.0);
		}

		public static boolean equals(Complex a, Complex b, double tol)	{
			if (a.isInfinite() || b.isInfinite())	{
				if (a.isInfinite() && b.isInfinite()) return true;
				return false;
			}
			Complex tmp = new Complex();
			subtract(tmp, a, b);
			double ll = length(tmp);
			return ll < tol;
		}

		public static Complex add(Complex dst, Complex a, Complex b)	{
			if (dst == null) dst = new Complex();
			dst.re = a.re + b.re;
			dst.im = a.im + b.im;
			return dst;
		}
		
		public static Complex negate(Complex dst, Complex src)	{
			if (src == INFINITE_COMPLEX) return src;
			if (dst == null) dst = new Complex();
			if (src == null) {
				return dst;} 
			dst.re = -src.re;
			dst.im = -src.im;
			return dst;
		}
		
		public static Complex conjugate(Complex dst, Complex src)	{
			if (dst == null) dst = new Complex();
			if (src == null) {
				return dst;} 
			dst.re = src.re;
			dst.im = -src.im;
			return dst;
		}
		
		public static Complex subtract(Complex dst, Complex a, Complex b)	{
			if (dst == null) dst = new Complex();
			if (a == null || b == null) {
				return dst;} 
			dst.re = a.re - b.re;
			dst.im = a.im - b.im;
			return dst;
		}

		public static Complex times(Complex dst, double s, Complex src)	{
			if (dst == null) dst = new Complex();
			dst.re = s * src.re;
			dst.im = s * src.im;
			return dst;
		}

		public static Complex times(Complex dst, Complex a, Complex b)	{
			if (dst == null) dst = new Complex();
			Complex target = dst;
			if (a.isInfinite() || b.isInfinite()) {
				dst.infinity();
				return dst;
			}
			if (a == dst || b == dst)	{
				target = new Complex();
			}
			target.re = a.re * b.re - a.im*b.im ;
			target.im =  a.re * b.im + b.re*a.im;
			if (target != dst) copy(dst, target);
			return dst;
		}
		
		public static double innerProduct(Complex a, Complex b)	{
			return (a.re*b.re + a.im*b.im);
		}
		
		public static double outerProduct(Complex a, Complex b)	{
			return (a.re*b.im - b.re*a.im);
		}
		public static double lengthSquared(Complex q)	{
			return innerProduct(q,q);
		}

		public static double length(Complex q)	{
			return Math.sqrt(lengthSquared(q));
		}
		
		public static Complex invert(Complex dst, Complex src)	{
			Complex tmp = new Complex();
			if (dst == null) dst = new Complex();
			if (src.isInfinite()) {
				dst.zero();
				return dst;
			}
			double ll = lengthSquared(src);
			if (ll == 0.0)	{
				dst.infinity();
			} else {		// q^-1 = q * (q bar)/<q,q>
				ll = 1.0/ll;
				conjugate(tmp, src);
				times(dst, ll, tmp);
			}
			return dst;
		}
		
		public static Complex divide(Complex dst, Complex a, Complex b) {
			if (a.isInfinite() && b.isInfinite()) 	{
				if (dst == null) dst = new Complex();
				dst.setValue(1.0,0.0);
				return dst;
			}
			Complex tmp = new Complex();
			invert(tmp, b);
			return times(dst, a, tmp);
		}

		public static Complex star(Complex dst, Complex src)	{
			Complex tmp = new Complex();
			return conjugate(dst, invert(tmp, src));
		}
		
		public static Complex normalize(Complex dst, Complex src)	{
			double ll = length(src);
			if (dst == null) dst = new Complex();
			if (src.isInfinite()) {
				dst.infinity();
				return dst;
			}
			if (ll == 0) copy(dst, src);
			else {
				ll = 1.0/ll;
				times(dst, ll, src);
			}
			return dst;
		}
		
		public static Complex sqrt(Complex dst, Complex src) {
			if (dst == null) dst = new Complex();
			if (src.isInfinite()) {
				dst.infinity(); return dst;
			}
			double[] pol = src.polarCoordinates();
			pol[0] = Math.sqrt(pol[0]);
			pol[1] = pol[1]/2.0;
			dst.re = pol[0] * Math.cos(pol[1]);
			dst.im = pol[0] * Math.sin(pol[1]);
			return dst;
		}
		
		public static Complex log(Complex dst, Complex src)	{
			double[] foo = src.polarCoordinates();
			return new Complex(Math.log(foo[0]), foo[1]);
		}
		/**
		 * exp(a+bi) = exp(a)(cos(b) + isin(b))
		 * @param result
		 * @param src
		 * @return
		 */
		public static Complex exp(Complex result, Complex src)	{
			if (result == null) result = new Complex();
			if (src.isInfinite()) {
				result.infinity(); return result;
			}
			double rr = Math.exp(src.re);
			double c = Math.cos(src.im);
			double s = Math.sin(src.im);
			result.setValue(rr*c, rr*s);
			return result;
		}
			
		public double[] polarCoordinates()	{
			return polarCoordinates(null);
		}
		
		public double[] polarCoordinates(double[] result)	{
			if (result == null) result = new double[2];
			if (isInfinite()) {
				result[0] = Double.POSITIVE_INFINITY;
				return result;
			}
			result[0] = Math.sqrt(re*re+im*im);
			result[1] = Math.atan2(im,re);
			return result;
		}
		public double[] toDouble() { return toDouble(null); }
		
		/**
		 * @return
		 */
		public double[] toDouble(double[] val) {
			if (val == null) val = new double[2];
			val[0] = re;
			val[1] = im;
			return val;
		}

		public static double[][] toDouble(double[][] dst, Complex[] src) {
			if (dst == null) dst = new double[src.length][2];
			for (int i = 0; i<src.length; ++i)	{
				src[i].toDouble(dst[i]);
			}
			return dst;
		}

		public static Complex[] quadraticSolve(Complex[] result, Complex a, Complex b, Complex c) {
			if (lengthSquared(a)==0.0)	{
					
				Complex z = Complex.divide(null, Complex.negate(null, c), b);
				if (result == null) {
					result = new Complex[2]; 
					result[0] = new Complex(z);
					result[1] = new Complex(z);
				}
				return result;
			}
			if (result == null) {
				result = new Complex[2]; 
				result[0] = new Complex();
				result[1] = new Complex();
			}
			Complex mb = Complex.negate(null, b);
			Complex sqrt = Complex.sqrt(null, Complex.subtract( null,
					Complex.times(null, b,b), 
					Complex.times(null, 
							4.0, 
							Complex.times(null, a,c))));
			Complex denom = Complex.times(null, 2, a);
			Complex.divide(result[0], Complex.add(null, mb, sqrt), denom);
			Complex.divide(result[1], Complex.subtract(null, mb, sqrt), denom);
			Complex test = Complex.add(null, 
					Complex.add(null, 
							Complex.times(null, a, Complex.times(null, result[0],result[0])),
							Complex.times(null, b, result[0])),
					c);
			if (debug) System.err.println("Test: "+test);
			return result;
		}

		protected static double EPSILON1 = 10E-5, EPSILON2 = 10E-12;
		protected static Complex hsmidge = new Complex(10E-1*EPSILON1, 0),
								vsmidge = new Complex(0,10E-1*EPSILON1);
		private static int maxIts = 60;
		
		public static Complex dfApprox(ComplexFunction f, Complex z0)	{

			Complex hrate = Complex.divide(null,
								Complex.subtract(null, 
									f.valueAt(Complex.add(null, z0, hsmidge)), 
									f.valueAt(Complex.subtract(null, z0, hsmidge))),
							    Complex.times(null, 2, hsmidge));
			Complex vrate = Complex.divide(null,
								Complex.subtract(null,
									f.valueAt(Complex.add(null, z0, vsmidge)), 
									f.valueAt(Complex.subtract(null, z0, vsmidge))),
								Complex.times(null, 2, vsmidge));
			return Complex.times(null, .5, Complex.add(null, hrate, vrate));
		}
		
		static int numTries = 3;
		static double incrJiggle = 10;
		static double initJiggle = .001;
		public static Complex newtonsMethod(ComplexFunction f, Complex w)	{
			Complex root = _newtonsMethod(f, w);
			if (root != null) return root;
			double jiggle = initJiggle;
			for (int i = 0; i< numTries; ++i)	{
				root = _newtonsMethod(f, Complex.add(null, w, new Complex( Math.random()*jiggle, Math.random()*jiggle)));
				if (root != null) return root;
				jiggle *= incrJiggle;
			}
			return w;
					//throw new IllegalStateException("newtons Method doesn't converge");
		}

		private static Complex _newtonsMethod(ComplexFunction f, Complex w)	{
			Complex z, z0 = w , fval;
			for (int i = 0; i< maxIts; ++i)	{
				Complex df = dfApprox(f, z0 );
				fval = f.valueAt(z0);
//				z = Complex.subtract(null, z0, Complex.times(null, .8, Complex.divide(null, fval, df)));
				z = Complex.subtract(null, z0, Complex.divide(null, fval, df));
				if (Complex.abs(fval) <= EPSILON2 && Complex.abs(Complex.subtract(null, z, z0)) < EPSILON1)
					return z;
				if (debug) System.err.println("i "+i+"z: "+z0+" fval: "+fval);
				z0 = z;
			}
		    return null;
		}
		
		public static ComplexFunction deflate(final ComplexFunction f, final ComplexPolynomial p)	{
			return new ComplexFunction()	{
				public Complex valueAt(Complex z) {
					return Complex.divide(null, f.valueAt(z), p.valueAt(z));
				}
			};
		}
		
		public static ComplexFunction deflate(final ComplexFunction f, final Complex root)	{
			return deflate(f, new ComplexPolynomial(new Complex[]{Complex.negate(null, root), new Complex(1,0)}));
		}
		
		public static Complex[] rootSet(ComplexFunction f, int numRoots, Complex w)	{
			Complex[] roots = new Complex[numRoots]; 
			ComplexPolynomial alreadyFound = new ComplexPolynomial(new Complex[]{new Complex(1,0)});
			ComplexFunction deflated = f;
			for (int i = 0; i<numRoots; ++i)	{
				roots[i] = newtonsMethod(deflated, w);
				alreadyFound = ComplexPolynomial.times(alreadyFound, 
					new ComplexPolynomial(new Complex[]{Complex.negate(null, roots[i]), new Complex(1,0)}));
				deflated = deflate(f, alreadyFound);
			}
			return roots;
		}

		public static Complex[] rootSet(ComplexFunction cf, int q) {
			return rootSet(cf, q, new Complex(Math.random(), Math.random()));
		}

		public static Complex linearCombination(Complex dst, double d, Complex c1, double delta, Complex c2) {
			return add(dst, Complex.times(null, d, c1), Complex.times(null, delta, c2));
		}

		public static Complex sinh(Complex dst, Complex src)	{
			if (dst == null) dst = new Complex();
	        double sinIm = Math.sin(src.im);
	        double cosIm = Math.cos(src.im);
	        double expRe = Math.exp(src.re);
	        dst.re = (cosIm * (expRe - 1.0D / expRe)) / 2D;
	        dst.im = (sinIm * (expRe + 1.0D / expRe)) / 2D;
	        return dst;
		}
		
	    public  static Complex cosh(Complex dst, Complex src)	{
			if (dst == null) dst = new Complex();
	        double sinIm = Math.sin(src.im);
	        double cosIm = Math.cos(src.im);
	        double expRe = Math.exp(src.re);
	        dst.re = (cosIm * (expRe + 1.0D / expRe)) / 2D;
	        dst.im = (sinIm * (expRe - 1.0D / expRe)) / 2D;
	        return dst;
	    }

		public static Complex sin(Complex dst, Complex src)	{
			if (dst == null) dst = new Complex();
	        double sinRe = Math.sin(src.re);
	        double cosRe = Math.cos(src.re);
	        double expIm = Math.exp(src.im);
	        dst.re = (sinRe * (expIm + 1.0D / expIm)) / 2D;
	        dst.im = (cosRe * (expIm - 1.0D / expIm)) / 2D;
	        return dst;
		}
		
	    public  static Complex cos(Complex dst, Complex src)	{
			if (dst == null) dst = new Complex();
	        double sinRe = Math.sin(src.re);
	        double cosRe = Math.cos(src.re);
	        double expIm = Math.exp(src.im);
	        dst.re = (cosRe * (expIm + 1.0D / expIm)) / 2D;
	        dst.im = (-sinRe * (expIm - 1.0D / expIm)) / 2D;
	        return dst;
	    }

		public static Complex linearInterpolate(Complex dst, double a,
				Complex c1, double b, Complex c2) {
			return add(null, Complex.times(null, a, c1), Complex.times(null, b, c2));
		}

}
