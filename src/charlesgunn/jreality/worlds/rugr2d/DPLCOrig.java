/*
 * Created on Oct 27, 2021
 *
 */
package charlesgunn.jreality.worlds.rugr2d;

public class DPLCOrig extends DualPointLineCurve {

	{		scale = 1.0; 
			tscale = .07; 
			ascale = .1510; 
			phase = -.17; 
			ascale2 = .92;
			ascale3 = .321; 
			ascale4 = 1.0;
			pointRadius = .03;
			lineRadius = .006;
	}

	
	@Override
	protected void getParms(double[] dparms, double t) {
		dparms[0] = scale * tscale;
		dparms[1] = scale * ascale * Math.cos(phase + ascale2*Math.PI*2*t);
	}


	public static void main(String[] args) {
		new DPLCOrig().display();

	}

}
