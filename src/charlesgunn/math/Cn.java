/*
 * Author	gunn
 * Created on Feb 8, 2006
 *
 */
package charlesgunn.math;

public class Cn {

	private Cn() {
		super();
	}
	
	public static Complex attractiveFixedPoint(Complex dst, Complex[] aa)	{
		if (dst == null) dst = new Complex();
		Complex[] fp = fixedPoints(null, aa);
		for (int i = 0; i<2; ++i)	{
			Complex deriv = PSL2C.derivativeAt(null, aa, fp[i]);
			if (deriv.abs() < 1.0) return fp[i];
		}
		return fp[0];	
	}

	public static Complex[] conjugateBy(Complex[] dst, Complex[] m, Complex[] c)	{
		Complex[] ic = Cn.invert(null, c);
		return times(dst, c, times(null, m, ic));
	}

	public static Complex[] copy(Complex[] dst, Complex[] src)	{
		int n = src.length;
		if (dst == null) dst = new Complex[n];
		for (int i = 0; i<n; ++i)  dst[i] = Complex.copy(dst[i], src[i]);
		return dst;
	}
	
	public static Complex[][] copy(Complex[][] dst, Complex[][] src)	{
		if (dst == null || dst.length != src.length) dst = new Complex[src.length][];
		for (int i = 0; i<src.length; ++i)  dst[i] = Cn.copy(dst[i], src[i]);
		return dst;
	}

	public static Complex determinant(Complex[] m)	{
		return Complex.subtract(null, Complex.times(null, m[0],m[3]), Complex.times(null, m[1],m[2]));
	}

	public static Complex[] fixedPoints(Complex[] dst, Complex[] aa) {
		return Complex.quadraticSolve(null, aa[2], Complex.subtract(null, aa[3], aa[0]), Complex.negate(null, aa[1]));
	}

	public static Complex[] invert(Complex[] dst, Complex[] src)	{
		if (dst == null) {
			dst = new Complex[4];
			for (int i =0; i<4; ++i) dst[i] = new Complex();
		}
		Complex[] target = dst;
		if (src == dst)		{
			target = new Complex[4];
			for (int i =0; i<4; ++i) target[i] = new Complex();
		}
		Complex.copy(target[0], src[3]);
		Complex.copy(target[3], src[0]);
		Complex.negate(target[1], src[1]);
		Complex.negate(target[2], src[2]);
		if (target != dst)	for (int i = 0; i<4; ++i)  Complex.copy(dst[i], target[i]);
		return dst;
	}

	public static Complex[] normalize(Complex[] dst, Complex[] src)	{
		Complex d = Cn.determinant(src);
		if (Complex.lengthSquared(d)==0.0) {
			// TODO print a warning
			return dst;
		}
		d = Complex.invert(d,d);
		d = Complex.sqrt(d,d);
		dst = times(dst, d, src);
		return dst;
	}

	public static Complex[] times(Complex[] dst, Complex d, Complex[] src) {
		if (dst == null) {
			dst = new Complex[4];
			for (int i =0; i<4; ++i) dst[i] = new Complex();
		}
		int n= src.length;
		for (int i = 0; i<n; ++i)	{
			Complex.times(dst[i], d, src[i]);
		}
		return dst;
	}

	public static Complex[] times(Complex[] dst, final Complex[] src1, final Complex[] src2)		{
		if (dst == null) {
			dst = new Complex[4];
			for (int i =0; i<4; ++i) dst[i] = new Complex();
		}
		Complex[] target = dst;
		if (src1 == dst || src2 == dst)		{
			target = new Complex[4];
			for (int i =0; i<4; ++i) target[i] = new Complex();
		}
		Complex tmp = new Complex();
		for (int i = 0; i<2; ++i)	{
			for (int j = 0; j<2; ++j)	{
				target[2*i+j].zero();
				for (int k = 0; k<2; ++k)
					Complex.add(target[i*2+j], target[i*2+j], Complex.times(tmp,src1[i*2+k],src2[2*k+j]));
			}
		}
		if (target != dst) for (int i = 0; i<4; ++i)  Complex.copy(dst[i], target[i]);
		return dst;
	}

	public static Complex trace(Complex[] m)	{
		return Complex.add(null, m[0],m[3]);
	}


}
