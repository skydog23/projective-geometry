/*
 * Created on Oct 27, 2021
 *
 */
package charlesgunn.jreality.worlds.rugr2d;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.util.CameraUtility;

public class DPLCPointRange extends DPLCCircle {

	{
			numberOfSegments = 30;
	}



	@Override
	protected void getParms(double[] dparms, double t) {
		dparms[0] = tscale;
		dparms[1] = 0;
	}


	public static void main(String[] args) {
		new DPLCPointRange().display();

	}

}
