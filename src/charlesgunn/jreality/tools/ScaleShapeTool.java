/*
 * Created on Mar 23, 2004
 *
 */
package charlesgunn.jreality.tools;

import javax.swing.ImageIcon;

import de.jreality.jogl.plugin.HelpOverlay;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.scene.tool.ToolContext;

/**
 * @author Charles Gunn
 *
 */
public class ScaleShapeTool extends AbstractShapeTool {
	double	theScale;
    double fudge = 0.02;

    public void perform(ToolContext  tc)	{
		super.perform(tc);
		
		theScale = Math.exp(toolStrength*.2*diffNDC[1]); //)1.0 + fudge*diff[1];
		myTransform.setStretch(theScale);
		double[] composite;
		// TODO figure out how to do scales in non-euc space, which aren't around the origin
		if (false)	{	//(metric != Pn.EUCLIDEAN)	
			double[] tlate = P3.makeTranslationMatrix(null, anchorV, metric);
			composite = Rn.conjugateByMatrix(null, myTransform.getArray(), tlate);
			Rn.times(composite, origM, composite);
		} else {
			composite = Rn.times(null, origM, myTransform.getArray());
		}
		theEditedTransform.setMatrix(composite);
		viewer.renderAsync();
	}

	public String getName() {
		return "scale";
	}

	public void registerHelp(HelpOverlay overlay) {
		// TODO Auto-generated method stub
		
	}

	public ImageIcon getIcon(int size) {
		return ToolManager.createImageIcon(size == ToolManager.LARGE ? "scaleToolIcon.png" : "scaleToolIcon-24.png");
	}

	
}
