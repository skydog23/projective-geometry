/*
 * Created on Oct 27, 2021
 *
 */
package charlesgunn.jreality.worlds.rugr2d;

public class DPLCLinePencil extends DPLCCircle {

	{
		numberOfSegments = 40;
		tscale = 0.0;
	}

	@Override
	protected void getParms(double[] dparms, double t) {
		dparms[0] = 0;
		dparms[1] = Math.PI/(numberOfSegments);
	}


	public static void main(String[] args) {
		new DPLCLinePencil().display();

	}

}
