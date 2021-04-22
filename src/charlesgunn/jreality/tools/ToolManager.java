/*
 *
 */
package charlesgunn.jreality.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.logging.Level;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import charlesgunn.jreality.viewer.GlobalProperties;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.toolsystem.ToolSystem;
import de.jreality.ui.viewerapp.SelectionEvent;
import de.jreality.ui.viewerapp.SelectionListener;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.SelectionManagerImpl;
import de.jreality.util.LoggingSystem;


/**
 * @author Charles Gunn
 *
 */
public class ToolManager implements SelectionListener {
//	InteractiveViewer viewer;
	protected MouseTool currentTool, currentShapeTool;
	protected SelectionTool selTool;
	protected CameraTool zoomTool;
	protected CameraFlyTool pointTool;
	protected RotateShapeTool rotTool;
	protected ScaleShapeTool scaleTool;
	protected TranslateShapeTool transTool;
	protected CameraRotateTool camRotTool;
	protected StereoCameraTool stereoTool;
	protected HelpTool helpTool;
	protected int iconSize = SMALL;
	boolean active = true;
	SelectionManager sm;
	final public static int LARGE = 3;
	final public static int MEDIUM = 2;
	final public static int SMALL = 1;
	
	final public static int SELECTION_TOOL = 1;
	final public static int ROTATION_TOOL = 10;
	final public static int STRETCH_TOOL = 11;
	final public static int TRANSLATION_TOOL = 12;
	final public static int CAMERA_ZOOM_TOOL = 20;
	final public static int CAMERA_FLY_TOOL = 21;
	final public static int CAMERA_STEREO_TOOL = 22;
	final public static int CAMERA_ROTATE_TOOL = 23;
	final public static int USER_TOOL = 30;
	Viewer viewer = null;
	static WeakHashMap globalTable = new WeakHashMap();
	public static ToolManager toolManagerForViewer(Viewer v)	{
		ToolManager tm = (ToolManager) globalTable.get(v);
		if (tm != null) return tm;
		tm = new ToolManager(v);
		globalTable.put(v,tm);
		return tm;
	}
	
	private ToolManager(Viewer v) {
		super();
		viewer = v;
		selTool = new SelectionTool();
		rotTool = new RotateShapeTool();
		scaleTool = new ScaleShapeTool();
		transTool = new TranslateShapeTool();
		zoomTool = new CameraTool();
		pointTool = new CameraFlyTool();
		stereoTool = new StereoCameraTool();
		camRotTool = new CameraRotateTool();
		sm = SelectionManagerImpl.selectionManagerForViewer(viewer);
		sm.addSelectionListener(this);
		activateTool(rotTool);
		if (helpTool == null) helpTool = new HelpTool();
//		if (!viewer.getSceneRoot().getTools().contains(helpTool))
//			viewer.getSceneRoot().addTool(helpTool);
		ava = new SceneGraphPath(viewer.getCameraPath());
		while (ava.getLength() != 0)	{
			ava.pop();
			if (ava.getLastElement().getName().indexOf("avatar") != -1) {
				break;
			}
		}
		if (ava.getLength() == 0) ava = null;

	}

	/**
	 * 
	 */
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
		if (toolSystem != null && ava != null) {
//			toolSystem.setAvatarPath(ava);
			System.err.println("Setting avatar path "+ava);
		}
	}
	
	public void updateCurrentTool()	{
		activateTool(currentTool);
	}

	public void activateTool(int which)	{
		MouseTool whichTool = null;
		switch(which)	{
		case SELECTION_TOOL:	whichTool = selTool; break;
		case ROTATION_TOOL:	whichTool = rotTool; break;
		case STRETCH_TOOL:	whichTool = scaleTool; break;
		case TRANSLATION_TOOL:	whichTool = transTool; break;
		case CAMERA_ZOOM_TOOL:	whichTool = zoomTool; break;
		case CAMERA_FLY_TOOL: 	whichTool = pointTool; break;
		case CAMERA_STEREO_TOOL:	whichTool = stereoTool; break;
		case CAMERA_ROTATE_TOOL:	whichTool = camRotTool; break;
		default:					whichTool = selTool; break;
		}
		activateTool(whichTool);
	}
	
	int userToolPosition = 1;
	JToolBar tb;
	ButtonGroup bg = new ButtonGroup();
	public JToolBar getToolbar()	{
		if (tb != null) return tb;
		tb = new JToolBar(SwingConstants.VERTICAL);
	
		//else tb.removeAll();
		tb.add(buttonForTool(selTool));
		//tb.add(buttonForTool("U",userTool,"User tool"));
		// new user tools go here
		tb.addSeparator();
//		tb.addSeparator();
		tb.add(buttonForTool(rotTool));
		tb.add(buttonForTool(scaleTool));
		tb.add(buttonForTool(transTool));
		tb.addSeparator();
//		tb.addSeparator();
	//	tb.add(buttonForTool(camRotTool));
		tb.add(buttonForTool(zoomTool));
		tb.add(buttonForTool(pointTool));
		tb.add(buttonForTool(stereoTool));
//		tb.add(actions[6] = buttonForTool((String) null,stereoTool,"Stereo camera tool"));
		//tb.add(buttonForTool("F",flyTool,"Camera fly tool"));
//		tb.addSeparator();
		tb.addSeparator();
//		activateTool(rotTool);
//		actions[1].setSelected(true);
		return tb;
	}
	JToggleButton currentSelected = null;
//	class ToolAction extends AbstractAction {
//		MouseTool tool;
//		public ToolAction(String name, MouseTool t, String tooltip)	{
//			super(name);
//			tool = t;
//			putValue(Action.SHORT_DESCRIPTION,tooltip);
//		}
//		public void actionPerformed(ActionEvent e)	{
//			if (currentTool == tool) return;
//			currentAction = this;
//			activateTool(tool);
//		}
//	}
	
	public void addTool(MouseTool dt)	{
		getToolbar();
		tb.add(buttonForTool(dt));
	}
	
	public JToggleButton buttonForTool( final MouseTool t){
		String tooltip = t.getName() + " tool";
		ImageIcon icon = t.getIcon(iconSize);
		if (icon == null) icon=createImageIcon("myicon-03-24.png");
		return buttonForTool(icon, t, tooltip);
	}

	public JToggleButton buttonForTool(ImageIcon icon, final MouseTool t, String tooltip){
		if (icon == null) 	icon=createImageIcon("myicon-03-24.png");
		final JToggleButton theButt = new JToggleButton( icon);
//		theButt.setText(name);

		theButt.setToolTipText(tooltip);
		theButt.addActionListener( new ActionListener()	{
			final MouseTool tool = t;
			public void actionPerformed(ActionEvent e)	{
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
		if (currentTool != null) activateTool(currentTool);
	}

	SceneGraphPath currentToolPath = null;
	boolean attached = false;
	public void activateTool(MouseTool tool) {
		if (toolSystem == null) initializeToolSystem(GlobalProperties.doOwnTools);
//		System.err.println("Activating tool:"+tool);
		if (!active) return;
//		if (currentTool != null) System.err.println("old tool "+currentTool.getName());
		if (currentTool != null) {
			if (currentToolPath != null) {
				if (currentToolPath.getLastComponent().getTools().contains(currentTool))
					currentToolPath.getLastComponent().removeTool(currentTool);			
				else LoggingSystem.getLogger(this).warning("Could not remove old tool from path "+currentToolPath.toString());
			}
			currentTool.detachFromViewer();			
		}
		currentTool = tool;
		SceneGraphPath attachmentPath = currentTool.getAttachmentPath();
		if ( attachmentPath == null)	{
			currentToolPath = new SceneGraphPath();
			currentToolPath.push(viewer.getSceneRoot());
		} else if (attachmentPath == AbstractShapeTool.CURRENT_SELECTION) {
			attachmentPath = sm.getSelectionPath();
			if ((attachmentPath.get(0) != viewer.getSceneRoot())){
				sm.setSelectionPath(new SceneGraphPath(viewer.getSceneRoot()));
				attachmentPath = sm.getSelectionPath();
			}
			if (attachmentPath == null) attachmentPath = currentToolPath;
		}
		if (attachmentPath != null) {
			currentToolPath = attachmentPath;
		}
		if (viewer.getSceneRoot() == null || currentToolPath == null ){
			currentToolPath = attachmentPath = null;
			return; //throw new IllegalStateException("Bad tool state");
		}
		LoggingSystem.getLogger(this).fine("current tool path is "+currentToolPath.toString());
		boolean alreadyThere = currentToolPath.getLastComponent().getTools().contains(currentTool);
		if (!alreadyThere && toolSystem != null) {
			currentToolPath.getLastComponent().addTool(currentTool);
			toolSystem.setEmptyPickPath(currentToolPath);
		}
		currentTool.attachToViewer(viewer);

//		System.err.println("tool path is "+currentToolPath);;
		broadcastChange();
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
	HashMap usertools = null;
	private SceneGraphPath ava; 
	public void addUserTool(final UserTool tool, ImageIcon ic, final String name)	{
		if (usertools == null) usertools = new HashMap();
		final ImageIcon icon = tool.getIcon(iconSize);
		if (tb == null) getToolbar();
		TimerTask addToolTask = new TimerTask()	{
			public void run()	{
				JToggleButton jb = buttonForTool(icon, tool, name);
				tb.add(jb);
				usertools.put(tool, jb);
			}
		};
		Timer doIt = new Timer();
		doIt.schedule(addToolTask, 10);
		
	}
	
	public void removeUserTool(final UserTool tool)	{
		if (usertools == null)	{
			JOGLConfiguration.theLog.log(Level.WARNING, "Removing usertool before any have been added");
			return;
		}
		Object obj = usertools.get(tool);
		if (obj != null && obj instanceof JToggleButton)	{
			tb.remove(((JToggleButton) obj));
			usertools.remove(tool);
		}
	}
	
	/**
	 * @return
	 */
	public DocumentedTool getCurrentTool() {
		return currentTool;
	}

	public int getIconSize() {
		return iconSize;
	}

	public void setIconSize(int iconSize) {
		this.iconSize = iconSize;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
		System.err.println("Setting active to "+active);
		if (!this.active)	{
			if (currentTool != null) {
				if (currentToolPath != null) {
					if (currentToolPath.getLastComponent().getTools().contains(currentTool))
						currentToolPath.getLastComponent().removeTool(currentTool);			
					else LoggingSystem.getLogger(this).warning("Could not remove old tool from path "+currentToolPath.toString());
				}
				currentTool.detachFromViewer();			
			}

		}
	}

}
