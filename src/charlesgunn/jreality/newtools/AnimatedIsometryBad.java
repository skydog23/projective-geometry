/*
 * Created on 16 Apr 2023
 *
 */
package charlesgunn.jreality.newtools;

import java.util.List;

import charlesgunn.math.Biquaternion;
import charlesgunn.math.Biquaternion.Metric;
import charlesgunn.math.IsometryAxis;
import de.jreality.jogl.Viewer;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.tools.AnimatorTask;
import de.jreality.tools.AnimatorTool;

public class AnimatedIsometryBad implements AnimatorTask {

	double[] isom, 
		orig = Rn.identityMatrix(4),
		target = Rn.identityMatrix(4);
	int metric = Pn.EUCLIDEAN;
	IsometryAxis ia;
	SceneGraphComponent targetSGC;
	Transformation origT;
	Viewer viewer = null;
	String name = null;
	static int count = 0;
	double totalAnimation = 3.0;
	WrapperTool wrapperTool;

	public AnimatedIsometryBad(double[] isom, int metric, 
			SceneGraphComponent sgc, SceneGraphComponent world)	{
		this.isom = isom;
		this.metric = metric;
		wrapperTool = new WrapperTool(world);
		Biquaternion biq = Biquaternion.biquaternionFromDirectIsometry(null, 
				isom, Metric.metricForCurvature(metric));
		System.err.println("targetsgc ="+sgc.getName());
		System.err.println("isom = "+isom);
		System.err.println("biq = "+biq);
		ia = new IsometryAxis(biq);
		targetSGC = sgc;
		origT = targetSGC.getTransformation();
		if (origT == null) 
			targetSGC.setTransformation(new Transformation());
		orig = origT.getMatrix();
//		targetSGC.addTool(wrapperTool);
		List<Tool> toollist = sgc.getTools();
		System.err.println(sgc.getName()+"  tool list "+toollist.size());
		count++;
	}
	
	public void reset() {
		origT.setMatrix(orig);
	}
	
	public void go() {
//		targetSGC.addTool(wrapperTool);
//		System.err.println(targetSGC.getName()+"  tool list "+targetSGC.getTools().size());
		wrapperTool.addCurrentSlot(timeslot);
	}
	
	double t = 0;
	public boolean run(double time, double dt) {
		double ddt = 0.001 * dt / totalAnimation;
		t += ddt;
		count++;
		Biquaternion bq = ia.exp(t);
		double[] mat = Biquaternion.matrixFromBiquaternion(null, bq);
		Rn.times(target, orig, mat); // newFM.getArray()));
		origT.setMatrix(target);
		System.err.println("setting tform " + Rn.matrixToString(target));
		if (viewer != null)
			viewer.renderAsync();
		return t >= 1.0 ? false : true;
	}

	public Tool getWrapperTool() {
		return wrapperTool;
	}
	static InputSlot timeslot = InputSlot.getDevice("SystemTime");
	private class WrapperTool extends AbstractTool	{
		SceneGraphComponent world;
		WrapperTool(SceneGraphComponent w){
			super(InputSlot.LEFT_BUTTON);
			world = w;
		}
		
		@Override
		public void activate(ToolContext tc) {
			super.activate(tc);
			System.err.println("in wrapper activate");
			addCurrentSlot(timeslot);
			System.err.println("Wrapper tool activated");
		}

		@Override
		public void deactivate(ToolContext tc) {
			super.deactivate(tc);
			removeCurrentSlot(timeslot);
			System.err.println("in wrapper deactivate");
			AnimatorTool.getInstance(tc).schedule(world, AnimatedIsometryBad.this);
		}

	};
}
