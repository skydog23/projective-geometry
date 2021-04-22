/*
 * Created on Nov 10, 2010
 *
 */
package charlesgunn.jreality.android;

public class Util {

	public static float[] getTransposedFloatMatrix(float[] dst, double[] mat)	{
		if (dst == null) dst = new float[16];
		
		for (int i = 0; i<4; ++i)	{
			for (int j = 0; j<4; ++j)	{
				dst[4*j+i] = (float) mat[4*i+j];
			}
		}
		return dst;
	}
}
