/*
 * Created on Oct 29, 2021
 *
 */
package charlesgunn.jreality.worlds.rugr2d;

public class DPLCCircleLines extends DPLCCircle {

	{
		setShowEuc(false);
		setShowPolar(true);
	}
	public static void main(String[] args) {
		new DPLCCircleLines().display();
	}

}
