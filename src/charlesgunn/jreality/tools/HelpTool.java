/*
 * Author	gunn
 * Created on Mar 21, 2006
 *
 */
package charlesgunn.jreality.tools;

import javax.swing.JFrame;

import de.jreality.math.Matrix;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;

public class HelpTool extends AbstractTool {
	private JFrame toolInspectorFrame;

	public HelpTool() {
		super(InputSlot.getDevice("PrimarySelection"),
				InputSlot.getDevice("SecondarySelection"));
		addCurrentSlot(InputSlot.getDevice("PointerNDC"));
	}


	public void activate(ToolContext tc) {
		if (toolInspectorFrame == null) 	{
			toolInspectorFrame = new JFrame("Mouse tool inspector");
			toolInspectorFrame.setSize(300,200);
			toolInspectorFrame.getContentPane().setSize(300,200);
			toolInspectorFrame.pack();
			Matrix m = new Matrix(tc.getTransformationMatrix(InputSlot.getDevice("PointerNDC")));
			int x = (int) (.5+.5*(m.getEntry(0,3)) * tc.getViewer().getViewingComponentSize().getWidth());
			int y = (int) (.5-.5*(m.getEntry(1,3)) * tc.getViewer().getViewingComponentSize().getHeight());
			toolInspectorFrame.setLocation(x+20, y+20);
		}
		toolInspectorFrame.getContentPane().removeAll();
		DocumentedTool mt = ToolManager.toolManagerForViewer(tc.getViewer()).getCurrentTool();
		if (mt.getInspector() == null) return;
		System.err.println("Help tool activated for "+mt.getName());
		toolInspectorFrame.getContentPane().add(mt.getInspector());
		toolInspectorFrame.pack();
		toolInspectorFrame.setVisible(true);
	}


}
