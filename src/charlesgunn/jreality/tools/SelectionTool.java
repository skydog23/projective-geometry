/*
 * Author	gunn
 * Created on Mar 20, 2006
 *
 */
package charlesgunn.jreality.tools;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import de.jreality.jogl.plugin.HelpOverlay;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.ToolContext;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.SelectionManagerImpl;
import de.jreality.ui.viewerapp.SelectionRenderer;

public class SelectionTool extends MouseTool {
	
	int			depth; 
	SelectionManager		selectionManager;
	SceneGraphPath	previousFullSelection, truncatedSelection, newSelection;
	boolean mouseMoved, firstTime;
	PickResult newPickPoint;
	double[] previousNDC = null;
	SelectionRenderer sr;
	boolean wasShift = false;
	ArrayList al = new ArrayList();  
	ArrayList al2 = new ArrayList();

	public SelectionTool() {
		super();
	}
	 

	public void activate(ToolContext tc) {
		super.activate(tc);
		if (selectionManager == null) {
			// TODO selection manager should depend on the viewer, so other classes can access it
			selectionManager =  SelectionManagerImpl.selectionManagerForViewer(tc.getViewer());
			System.err.println("ST: sm = "+selectionManager);
			sr = new SelectionRenderer(selectionManager, tc.getViewer());
			sr.setVisible(false);
			firstTime = true;
		}
		newSelection = null;
		mouseMoved=false;		

		newPickPoint = tc.getCurrentPick();
		if (newPickPoint != null)	{
			newSelection = newPickPoint.getPickPath();
//			if (selectionManager instanceof SelectionManager)
//				((SelectionManager) selectionManager).setPickPoint(newPickPoint);
		}
		if (shift)	{
			sr.setVisible(!sr.isVisible());
		}
		wasShift = shift;
		
	}

	public void perform(ToolContext tc) {
//		mouseMoved=true;
		super.perform(tc);
		if (wasShift) return;
		newPickPoint = tc.getCurrentPick();
		if (newPickPoint != null)	{
			newSelection = newPickPoint.getPickPath();
//			if (selectionManager instanceof SelectionManager)
//				((SelectionManager) selectionManager).setPickPoint(newPickPoint);
		}
	}

	public void deactivate(ToolContext tc) {
		super.deactivate(tc);
		if (wasShift) return;
		if (newSelection == null)		{		// nothing under the cursor
			previousFullSelection = null;
			truncatedSelection = null;
			selectionManager.setSelectionPath(null);
			return;
		} 
		if (Rn.euclideanDistance(anchorNDC, currentNDC) > 10E-6 ) {
			previousNDC = Rn.copy(null, anchorNDC);
			return;	
		}
		previousNDC = Rn.copy(null, anchorNDC);
		if (previousFullSelection != null &&  previousFullSelection.isEqual(newSelection) ) {
			selectionManager.cycleSelectionPath();
			System.err.println("Cycling selection");
			return;	
		} // else {    // renew selection
		// To be here means we have selected a different full path than the previous one
		previousFullSelection = newSelection;
		// notify the selection manager
//		selectionManager.setPickPoint(newPickPoint);
		selectionManager.setSelectionPath(newSelection);
		if (firstTime)	{
//			selectionManager.setRenderSelection(true);
			sr.setVisible(true);
			firstTime = false;
		}
		newPickPoint = null;
		newSelection = null;
	}



	public ImageIcon getIcon(int size) {
		return ToolManager.createImageIcon(size == ToolManager.LARGE ? "selectToolIcon.png" : "selectToolIcon-24.png");
	}


	public String getName() {
		return "selection";
	}


	public Component getInspector() {
		return null;
	}


	@Override
	public void registerHelp(HelpOverlay overlay) {
		// TODO Auto-generated method stub
		
	}

}
