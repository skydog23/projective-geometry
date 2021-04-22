/*
 *
 */
package charlesgunn.jreality.newtools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import charlesgunn.jreality.tools.AbstractShapeTool;
import charlesgunn.jreality.viewer.GlobalProperties;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.scene.tool.Tool;
import de.jreality.toolsystem.ToolSystem;
import de.jreality.ui.viewerapp.SelectionEvent;
import de.jreality.ui.viewerapp.SelectionListener;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.util.LoggingSystem;


/**
 * @author Charles Gunn
 *
 */
public class ToolManager implements SelectionListener {
	protected int iconSize = SMALL;
	SelectionManager sm;
	final public static int LARGE = 3;
	final public static int MEDIUM = 2;
	final public static int SMALL = 1;	
	Tool currentTool = null;
	Viewer viewer = null;
	static WeakHashMap<Viewer, ToolManager> globalTable = new WeakHashMap<Viewer, ToolManager>();
	Vector<Tool> tools = new Vector<Tool>();
	static HashMap<Class, ImageIcon> iconTable = new HashMap<Class, ImageIcon>();
	static {
		iconTable.put(RotateTool.class, new ImageIcon(ToolManager.class.getResource("rotateToolIcon-24.png")));
		iconTable.put(DraggingTool.class, new ImageIcon(ToolManager.class.getResource("translateToolIcon-24.png")));
		iconTable.put(AllroundTool.class, new ImageIcon(ToolManager.class.getResource("selectToolIcon-24.png")));
	}
	public static ToolManager toolManagerForViewer(Viewer v)	{
		ToolManager tm = (ToolManager) globalTable.get(v);
		if (tm != null) return tm;
		tm = new ToolManager(v);
		globalTable.put(v,tm);
		return tm;
	}
	
	public ToolManager(Viewer v) {
		viewer = v;
		sm = de.jreality.ui.viewerapp.SelectionManagerImpl.selectionManagerForViewer(viewer);
		sm.addSelectionListener(this);
	}

	public void dispose()	{
		sm.removeSelectionListener(this);
	}
	
	public void addTool(Tool t)	{
		tools.add(t);
		updateToolBar();
	}
	
	public void removeTool(Tool t)	{
		if (tools.contains(t)) tools.remove(t);
		updateToolBar();
	}
	
	private ToolSystem toolSystem;
	SceneGraphPath emptyPickPath = null;
	public void initializeToolSystem(boolean b) {
		if (toolSystem == null)	{
			if (b) {
				toolSystem = ToolSystem.toolSystemForViewer(viewer);
				toolSystem.setPickSystem(new AABBPickSystem());			
				toolSystem.initializeSceneTools();
			}
			else toolSystem = ToolSystem.getToolSystemForViewer(viewer);
		}
		System.err.println("ToolManager: initialize is "+b);
	}
	
	public ToolSystem getToolSystem()	{
		if (toolSystem == null) initializeToolSystem(GlobalProperties.doOwnTools);
		return toolSystem;
	}
		
	int userToolPosition = 1;
	JToolBar tb;
	ButtonGroup bg = new ButtonGroup();
	public JToolBar getToolbar()	{
		if (tb != null) return tb;
		
		tb = new JToolBar(SwingConstants.VERTICAL);
		updateToolBar();
		return tb;
	}

	private void updateToolBar() {
		getToolbar();
		tb.removeAll();
		for (Tool t : tools)	{
			tb.add(buttonForTool(t));
		}
	}
	
	JToggleButton currentSelected = null;
	
	
	public JToggleButton buttonForTool( final Tool t){
		ImageIcon icon = iconTable.get(t.getClass());
		if (icon == null) icon=createImageIcon("myicon-03-24.png");
		return buttonForTool(icon, t, null);
	}

	public JToggleButton buttonForTool(ImageIcon icon, final Tool t, String tooltip){
		if (icon == null) 	icon=createImageIcon("myicon-03-24.png");
		final JToggleButton theButt = new JToggleButton( icon);
//		theButt.setText(name);

		if (tooltip != null) theButt.setToolTipText(tooltip);
		theButt.addActionListener( new ActionListener()	{
			final Tool tool = t;
			public void actionPerformed(ActionEvent e)	{
				System.err.println("tool size is "+tools.size());
				if (tools.size() == 1)	{
					if (currentTool != null) {
						deactivateTool(currentTool);
						if (currentSelected != null) currentSelected.setSelected(false);
						currentSelected = null;
						return;
					}
					
				}
				if (currentTool == tool) return;
				if (currentSelected != null) currentSelected.setSelected(false);
				currentSelected = theButt;
				currentSelected.setSelected(true);
				activateTool(tool);
//				System.err.println("Selecting tool "+tool.getName());
			}
		});
		bg.add(theButt);
		return theButt;
	}
	
	public static ImageIcon createImageIcon(String path) {
	    java.net.URL imgURL = ToolManager.class.getResource(path);
	    return new ImageIcon(imgURL);
	}

	public static ImageIcon createImageIcon(String path, Class base) {
	    java.net.URL imgURL = base.getResource(path);
	    return new ImageIcon(imgURL);
	}

	public static class Changed extends java.util.EventObject	{

		/**
		 * @param source
		 */
		public Changed(Object source) {
			super(source);
		}
	}
	
	public void selectionChanged(SelectionEvent e) {
		LoggingSystem.getLogger(this).fine("Current selection is "+e.getSelection().getSGPath().toString());
		//if (currentTool != null) activateTool(currentTool);
	}

	SceneGraphPath currentToolPath = null;
	boolean attached = false;
	public void activateTool(Tool tool) {
		if (toolSystem == null) initializeToolSystem(GlobalProperties.doOwnTools);
//		System.err.println("Activating tool:"+tool);
		emptyPickPath = toolSystem.getEmptyPickPath();
		if (emptyPickPath == null)	{
			emptyPickPath = new SceneGraphPath();
			emptyPickPath.push(viewer.getSceneRoot());
			toolSystem.setEmptyPickPath(emptyPickPath);
		}
		deactivateTool(currentTool);
		currentTool = tool;
		currentToolPath = emptyPickPath;
		if (currentTool instanceof MyTool)	{
			SceneGraphPath attachmentPath = ((MyTool) currentTool).getAttachmentPath();
			if ( attachmentPath == null)	{
				currentToolPath = new SceneGraphPath();
				currentToolPath.push(viewer.getSceneRoot());
			} else if (attachmentPath == AbstractShapeTool.CURRENT_SELECTION) {
				attachmentPath = sm.getSelectionPath();
				if (attachmentPath == null) attachmentPath = currentToolPath;
			}
			if (attachmentPath != null) {
				currentToolPath = attachmentPath;
			}			
		}

		LoggingSystem.getLogger(this).info("current tool path is "+currentToolPath.toString());
		boolean alreadyThere = currentToolPath.getLastComponent().getTools().contains(currentTool);
		if (!alreadyThere) {
			currentToolPath.getLastComponent().addTool(currentTool);
			toolSystem.setEmptyPickPath(currentToolPath);
		}
		broadcastChange();
	}
	
	public void deactivateTool(Tool tool)	{
		if (currentTool != null) {
			if (currentToolPath != null) {
				if (currentToolPath.getLastComponent().getTools().contains(currentTool))
					currentToolPath.getLastComponent().removeTool(currentTool);			
				else LoggingSystem.getLogger(this).warning("Could not remove old tool from path "+currentToolPath.toString());
			}
		}
		currentTool = null;
	}
	Vector listeners;
	
	public interface Listener extends java.util.EventListener	{
		public void toolChanged(ToolManager.Changed e);
	}

	public void addToolListener(ToolManager.Listener l)	{
		if (listeners == null)	listeners = new Vector();
		if (listeners.contains(l)) return;
		listeners.add(l);
		//System.err.println("ToolManager: Adding geometry listener"+l+"to this:"+this);
	}
	
	public void removeToolListener(ToolManager.Listener l)	{
		if (listeners == null)	return;
		listeners.remove(l);
	}

	public void broadcastChange()	{
		if (listeners == null) return;
		//System.err.println("ToolManager: broadcasting"+listeners.size()+" listeners");
		if (!listeners.isEmpty())	{
			ToolManager.Changed e = new ToolManager.Changed(this);
			//System.err.println("ToolManager: broadcasting"+listeners.size()+" listeners");
			for (int i = 0; i<listeners.size(); ++i)	{
				ToolManager.Listener l = (ToolManager.Listener) listeners.get(i);
				l.toolChanged(e);
			}
		}
	}
}
