/*
 * Author	gunn
 * Created on Mar 20, 2006
 *
 */
package charlesgunn.jreality.newtools;

import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.ui.viewerapp.Selection;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.SelectionManagerImpl;
import de.jreality.ui.viewerapp.SelectionRenderer;
import de.jreality.util.DefaultMatrixSupport;


public class AllroundTool extends AbstractTool {
	
	static InputSlot rotateActivate = InputSlot.LEFT_BUTTON;
	static InputSlot dragActivate = InputSlot.MIDDLE_BUTTON;
	static InputSlot resetActivate = InputSlot.RIGHT_BUTTON;
	static InputSlot pointerTform = InputSlot.POINTER_TRANSFORMATION;
	static InputSlot timer = InputSlot.SYSTEM_TIME;
	static InputSlot evolutionSlot = InputSlot.getDevice("TrackballTransformation");
	
	SelectionManager		selectionManager;
	SelectionRenderer selectionRenderer;
	SceneGraphPath	previousFullSelection, truncatedSelection, newSelection;
	boolean rotating = false, dragging = false, firstTime = true, active = true;
	PickResult newPickPoint;
	DraggingTool dragtool = new DraggingTool();
	RotateTool rotatetool = new RotateTool();
	
	public AllroundTool() {
		addCurrentSlot(rotateActivate);
		addCurrentSlot(dragActivate);
		addCurrentSlot(resetActivate);
		addCurrentSlot(pointerTform);
	}
	 
	int count = 0;
	public void perform(ToolContext tc) {
		if (selectionManager == null) {
			selectionManager = SelectionManagerImpl.selectionManagerForViewer(tc.getViewer());
			firstTime = true;
		}
//		System.err.println("tc source = "+tc.getSource().getName());
	  if (active)	{
		if (tc.getSource() == rotateActivate)	{
			if (tc.getAxisState(rotateActivate).isReleased()) {
				rotating = false;
				rotatetool.deactivate(tc);
				addCurrentSlot(pointerTform);
				if (getCurrentSlots().contains(evolutionSlot)) removeCurrentSlot(evolutionSlot);
				tc.getRootToToolComponent().getLastComponent().setPickable( true );
			}
			else if (tc.getAxisState(rotateActivate).isPressed()) {
				if (rotating == false)	{
					rotating = true;	
					rotatetool.activate(tc);
					if (getCurrentSlots().contains(pointerTform))  removeCurrentSlot(pointerTform);
					addCurrentSlot(evolutionSlot);
					tc.getRootToToolComponent().getLastComponent().setPickable(false);					
				}
			}
		}
		if (tc.getSource() == dragActivate)	{
			if (tc.getAxisState(dragActivate).isReleased()) {
				dragging = false;
				dragtool.deactivate(tc);
				tc.getRootToToolComponent().getLastComponent().setPickable( true );
			} else if (tc.getAxisState(dragActivate).isPressed()) {
				if (dragging == false)	{
					dragging = true;			
					dragtool.activate(tc);
					tc.getRootToToolComponent().getLastComponent().setPickable( false);					
				}
			}
		}
//		System.err.println("rotate = "+rotating+" drag = "+dragging);
		if (rotating)	{
			rotatetool.perform(tc);
			return;
		}
		if (dragging)	{
			dragtool.perform(tc);
			return;
		}
	  }
		if (tc.getSource() == resetActivate)	{
			count++;
			// TODO figure out why  we always get two events instead of one 
			// and so we have to skip over the redundant event
			if (count%2 == 0) return;
			System.err.println("Toggling active");
			setActive(!getCurrentSlots().contains(pointerTform), 
					tc.getRootToToolComponent().getLastComponent(), 
					tc.getViewer());
			if ( selectionRenderer != null) {
				selectionRenderer.setVisible(isActive());
			}
			return;
		}
//		System.err.println("In art perform, drag + rotate = "+dragging+rotating);
		// else continue to select
		newPickPoint = tc.getCurrentPick();
		SceneGraphPath oldSelection = newSelection;
//		System.err.println("In art: select ");
		if (newPickPoint != null)	{
//			System.err.println("In art: select "+newPickPoint.getPickPath());
			if (!newPickPoint.getPickPath().startsWith(tc.getRootToToolComponent())) return;
			newSelection = newPickPoint.getPickPath();
//			System.err.println("Pick path is "+newSelection);
			newSelection = new SceneGraphPath(newSelection);
			if (!(newSelection.getLastElement() instanceof SceneGraphComponent)) newSelection.pop();
			while (newSelection.getLength() != 0) {
				SceneGraphComponent sgc = newSelection.getLastComponent();
				Transformation t = sgc.getTransformation();
				if (!( t == null || t.isReadOnly())) break;
				newSelection.pop();
			}
			if (newSelection.getLength() != 0 && !newSelection.isEqual(oldSelection)) {
				System.err.println("Setting selection to "+newSelection);
				selectionManager.setSelection(new Selection(newSelection));
//				if (sr != null) sr.dispose();
//				System.err.println("Viewer is of class "+tc.getViewer().getClass().getName());
				if (selectionRenderer == null) selectionRenderer = new SelectionRenderer(selectionManager, tc.getViewer());
				selectionRenderer.setSelectionPath(newSelection);
				broadcastChange();
			}
		}
	}
	public boolean isActive()	{
		return active;
	}
	
	public void setActive(boolean b, SceneGraphComponent where, Viewer v)	{
		if (!b)	{
			removeCurrentSlot(pointerTform);
			removeCurrentSlot(rotateActivate);
			removeCurrentSlot(dragActivate);
			if (newSelection != null)	{
				SceneGraphComponent sgc = newSelection.getLastComponent();
				DefaultMatrixSupport.getSharedInstance().restoreDefaultMatrices(sgc, true);					
			}
		} else {
			addCurrentSlot(pointerTform);
			addCurrentSlot(rotateActivate);
			addCurrentSlot(dragActivate);
		}
		active = b;
		where.setPickable( b );
		if (selectionRenderer != null) selectionRenderer.setVisible(active);
//		if (v != null) SelectionManager.selectionManagerForViewer(v).setRenderSelection(b);
		broadcastChange();
		System.err.println("Setting active to: "+active);
	}
	
	public SceneGraphPath getSelection()	{
		return newSelection;
	}
	
	static Vector<ChangeListener> listeners = new Vector<ChangeListener>();
	

	public  void addChangeListener(ChangeListener l)	{
		if (listeners.contains(l)) return;
		listeners.add(l);
	}
	
	public  void removeChangeListener(ActionListener l)	{
		listeners.remove(l);
	}
	public  void broadcastChange()	{
		if (listeners == null) return;
		ChangeEvent e = new ChangeEvent(this);
		//SyJOGLConfiguration.theLog.log(Level.INFO,"SelectionManager: broadcasting"+listeners.size()+" listeners");
		if (!listeners.isEmpty())	{
			//JOGLConfiguration.theLog.log(Level.INFO,"SelectionManager: broadcasting"+listeners.size()+" listeners");
			for (ChangeListener l : listeners)	{
				l.stateChanged(e);
			}
		}
	}

}
