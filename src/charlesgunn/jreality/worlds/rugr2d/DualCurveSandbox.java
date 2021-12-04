/*
 * Created on Nov 16, 2021
 *
 */
package charlesgunn.jreality.worlds.rugr2d;

import charlesgunn.jreality.geometry.projective.DualCurve;
import charlesgunn.jreality.geometry.projective.LineElement;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;

public class DualCurveSandbox extends Assignment {

	LineElement begin = new LineElement(
			new double[]{0,0,1}, new double[]{0,1,0}),
			end = new LineElement(
					new double[]{1,1,1}, new double[]{-1,0,1});
	DualCurve dc = new DualCurve(begin, end, 10);
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	@Override
	public SceneGraphComponent getContent() {
		SceneGraphComponent sgc = dc.getSGC();
		world.addChild(sgc);
		return world;
	}

	
	@Override
	public void setValueAtTime(double d) {
		dc.iterate();
		viewer.renderAsync();
	}


	@Override
	public void startAnimation() {
		// TODO Auto-generated method stub
		super.startAnimation();
		world.removeAllChildren();
		dc = new DualCurve(begin, end, 10);
		world.addChild(dc.getSGC());
	}


	@Override
	public void display() {
		super.display();
		
	}

	public static void main(String[] args) {
		new DualCurveSandbox().display();
	}

}
