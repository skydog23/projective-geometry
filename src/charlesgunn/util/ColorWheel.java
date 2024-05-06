/*
 * Created on 22 Apr 2024
 *
 */
package charlesgunn.util;

import java.awt.Color;

import charlesgunn.anim.util.AnimationUtility;
import de.jreality.math.Rn;

public class ColorWheel {

	static double[] dkeyframes, acc;
	static int k = 200, m = 255, n = 60;
	static Color[] values = {
			new Color(n, k, k),
			new Color(n, m, n),
			new Color(k, k, n),
			new Color(m, n, n), 
			new Color(k, n, k),
			new Color(n, n, m),
			new Color(n, k, k)
//			Color.red,
//			Color.magenta,
//			Color.blue,
//			Color.green,
//			Color.yellow,
//			Color.orange,
//			Color.red
	};
	{
		initKeyFrames();
	}
	private static void initKeyFrames() {
//		dkeyframes = new double[]{.1,.1,.1,.1,.1,.1};
		acc = new double[values.length];
		for (int i = 0; i<values.length; ++i)	{
			acc[i] = i /(values.length-1.0);
		}
		System.err.println("acc = "+Rn.toString(acc));
	}
	public static Color getColorForPoint(double[] verts, double phi, boolean zColoring) {
		
		Color c;
		if (zColoring){
			double r = 2*phi/Math.PI;
			c = ColorWheel.getColorForParameter(r);
//			System.err.println("r = "+r);
		} else {
			float[] fc = new float[3];
			for (int i = 0; i<3; ++i)	{
				fc[i] = (float) (.5 + .5 * verts[i]);
				if (fc[i] > 1f) fc[i] = 1.0f;
			}			
			c = new Color(fc[0], fc[1], fc[2]);
		}
		return c;
	}

	static public Color getColorForParameter(double d) {
		Color c;
		if (acc == null) initKeyFrames();
		for (int i = 0; i<acc.length-1; ++i)	{
			if (d >= acc[i] && d <= acc[i+1]) {
				c = AnimationUtility.linearInterpolation(
						d, acc[i], acc[i+1], values[i],values[i+1]);
				return c;
			} 
		};
		return values[acc.length-1];
	}

	public static Color applyGamma(Color c, double gamma) {
		float[] tmp = new float[4];
		float[] cmp = c.getComponents(tmp);
		for (int j = 0; j<3; ++j)	{
			cmp[j] = (float) (Math.pow(((double) cmp[j]), 1.0/gamma));
		}
		c = new Color(cmp[0], cmp[1], cmp[2]);
		return c;
	}

}
