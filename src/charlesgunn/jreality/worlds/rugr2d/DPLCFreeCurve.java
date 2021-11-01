/*
 * Created on Oct 27, 2021
 *
 */
package charlesgunn.jreality.worlds.rugr2d;

import java.awt.Color;

public class DPLCFreeCurve extends DualPointLineCurve {

	{		scale = 1.0; 
			tscale = .2; 
			ascale = .2165; 
			phase = .7949; 
			ascale2 = .5367;
			ascale3 = .2757; 
			ascale4 = .6862;
			pointRadius = .04;
			lineRadius = .006;
			numberOfSegments = 200;
			lineColor = new Color(87,136,136);
	}

	

	@Override
	protected void getParms(double[] dparms, double t) {
		dparms[0] = scale * tscale * Math.sin(phase + ascale4*Math.PI*2*t);
		dparms[1] = scale * ascale * Math.cos(phase + ascale2*Math.PI*2*t);
	}


	public static void main(String[] args) {
		new DPLCFreeCurve().display();

	}

}
