/*
 * Created on Oct 27, 2021
 *
 */
package charlesgunn.jreality.worlds.rugr2d;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Camera;
import de.jreality.util.CameraUtility;

public class DPLCCircle extends DualPointLineCurve {

	{		
			scale = 1.0; 
			tscale = .15; 
			pointRadius = .055;
			lineRadius = .008;
			encompass = false;
	}

	
	


	@Override
	protected Matrix getWorldTform() {
		Matrix m = super.getWorldTform();
		m.multiplyOnRight(MatrixBuilder.euclidean().translate(1,0,0).getMatrix());
		return m;
	}


	@Override
	protected void getParms(double[] dparms, double t) {
		dparms[0] = tscale ;
		dparms[1] = 2*Math.PI/(numberOfSegments);
	}


	@Override
	public void display() {
		super.display();
//		runAnimationFile("RuGR2D-anim-circleboth.xml");
	}


	public static void main(String[] args) {
		new DPLCCircle().display();

	}

}
