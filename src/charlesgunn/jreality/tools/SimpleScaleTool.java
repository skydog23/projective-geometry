package charlesgunn.jreality.tools;

import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;

public class SimpleScaleTool extends AbstractTool {

	private transient double scale = 1.0;
	private transient double gain = 1.05;
	
	  static InputSlot increaseSlot = InputSlot.getDevice("IncreaseScale");
	  static InputSlot decreaseSlot = InputSlot.getDevice("DecreaseScale");

	  public SimpleScaleTool() {
		super(null);
	    addCurrentSlot(increaseSlot, "scales up");
	    addCurrentSlot(decreaseSlot, "scales down");
	    getDescription();
	  }

	  transient Matrix centerTranslation = new Matrix();
	  transient boolean bigger;

	  public void perform(ToolContext tc) {
	    if (tc.getAxisState(increaseSlot).isPressed()) bigger = true;
	    else if (tc.getAxisState(decreaseSlot).isPressed()) bigger = false;
	   SceneGraphComponent sgc = tc.getRootToToolComponent().getLastComponent();
	    	if (sgc.getTransformation() == null) sgc.setTransformation(new Transformation());
	    	sgc.getTransformation().multiplyOnRight(P3.makeStretchMatrix(null, bigger ? gain : 1.0/gain));
	    	scale *= bigger ? gain : 1.0/gain;
	    	System.err.println("in simple scale tool, scale is "+scale);
	    	tc.getViewer().renderAsync();
	  }

	public double getGain() {
		return gain;
	}

	public void setGain(double gain) {
		this.gain = gain;
	}
	 

}
