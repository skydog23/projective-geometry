/*
 * Created on Dec 3, 2010
 *
 */
package charlesgunn.pcl4;

public class Multivector {

	protected double[] x;		// the values

	public static Multivector geometricProduct(Multivector dst, Multivector src1, Multivector src2) {
		if (dst == null) dst = new Multivector();
		return dst;
	}
	
}
