package charlesgunn.jreality.viewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.jreality.CameraUtilityOverflow;
import charlesgunn.jreality.newtools.FlyTool;
import charlesgunn.jreality.plugin.BackgroundColorsTool;
import charlesgunn.jreality.plugin.LoadableSceneMenu;
import charlesgunn.jreality.plugin.TermesSpherePlugin;
import charlesgunn.jreality.plugin.ToolBarPlugin;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewerUtility;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.InfoOverlayPlugin;
import de.jreality.plugin.basic.Inspector;
import de.jreality.plugin.basic.PropertiesMenu;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.Shell;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.experimental.ViewerKeyListenerPlugin;
import de.jreality.plugin.menu.BackgroundColor;
import de.jreality.plugin.menu.CameraMenu;
import de.jreality.plugin.menu.DisplayOptions;
import de.jreality.plugin.menu.ExportMenu;
import de.jreality.plugin.scene.SceneShrinkPanel;
import de.jreality.plugin.scene.ShrinkPanelAggregator;
import de.jreality.plugin.scene.Sky;
import de.jreality.plugin.scene.VRPanel;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.SelectionManagerImpl;
import de.jreality.util.CameraUtility;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;

public class PluginSceneLoader extends Plugin {

	JRViewer pva;
	LoadableSceneInterface currentLoadedScene = null;
	SceneGraphComponent world = null;
	SceneGraphComponent lights = null;
	Viewer viewer;
	boolean hasBackPlane = false;
	boolean doingVR = true;
	boolean sceneMenu = false;
	private Component lastInspectionPanel;
	private SelectionManager selectionManager;
	Content content;
	Controller controller;
	protected Sky sky;
	InfoOverlayPlugin infoOverlay;
	ViewerKeyListenerPlugin viewerKeyListener;
	AnimationPlugin animated;
	ToolBarPlugin toolbar;
	LoadableSceneMenu scenemenu;
	ShrinkPanelAggregator appInspector;
	protected VRPanel vrpanel;
	private JMenuBar currentApplicationMenus;
	Vector<JMenu> menus = new Vector<JMenu>();
	
	public PluginSceneLoader()	{
		this(null, false);
	}
	public PluginSceneLoader(InputStream properties) {
		this(properties, false);
	}
	
	public PluginSceneLoader(InputStream properties, boolean b) {
		sceneMenu = b;
		pva = new JRViewer(false);
		Secure.setProperty("apple.laf.useScreenMenuBar", "false");
		pva.setShowPanelSlots(true, false, false, false);
		pva.registerPlugin(new Inspector());
		pva.registerPlugin(new Shell());
		
		pva.registerPlugin(new DisplayOptions());
		pva.registerPlugin(new ViewMenuBar());
		pva.registerPlugin(new ViewToolBar());
		
//		pva.registerPlugin(new ContentAppearance());
		
		pva.registerPlugin(new ExportMenu());
		bcplugin = new BackgroundColor();
		pva.registerPlugin(bcplugin);
		pva.registerPlugin(new CameraMenu());
		
		pva.registerPlugin(new PropertiesMenu());
		pva.addContentSupport(ContentType.Raw);
		
		vrpanel = new VRPanel();
		pva.registerPlugin(vrpanel);
		
		infoOverlay = new InfoOverlayPlugin();
		pva.registerPlugin(infoOverlay);
		
		viewerKeyListener = new ViewerKeyListenerPlugin();
		pva.registerPlugin(viewerKeyListener);
		
		animated = new AnimationPlugin();
		pva.registerPlugin(animated);
		
		toolbar = new ToolBarPlugin();
		pva.registerPlugin(toolbar);
		
		if (sceneMenu)	{
			System.err.println("adding scene menu");
			scenemenu = new LoadableSceneMenu(this);
			pva.registerPlugin(scenemenu);			
		}
		
		TermesSpherePlugin tsp = new TermesSpherePlugin();
//		pva.registerPlugin(tsp);
		
		appInspector = new ShrinkPanelAggregator() {
			
			@Override
			public String getHelpTitle() {
				return docTitle;
			}
			@Override
			public String getHelpDocument() {
				System.err.println("Returning "+htmlDoc);
				return htmlDoc;
			}
			
			@Override
			public String getHelpPath() {
				return helpPath;
			}
			
			@Override
			public Class<?> getHelpHandle() {
				return docClass;
			}
		};
		
		//if (!doingVR) 
			appInspector.setTriggerComponent(null);
		pva.registerPlugin(appInspector);
		
		// I'm a plugin too so I can inherit store/restore and pass it on to loaded scene
		pva.registerPlugin(this);
		if (properties != null) {
			System.err.println("input stream = "+properties);
			pva.setPropertiesInputStream(properties);
		}
		pva.startup();
//		vrpanel.setShowPanel(false);

		viewer = pva.getPlugin(View.class).getViewer().getCurrentViewer();
//		System.err.println("current root is "+viewer.getSceneRoot().getName());

		SceneGraphUtility.removeLights(viewer);
		controller = pva.getController();
		SceneGraphComponent ava = controller.getPlugin(Scene.class).getAvatarPath().getLastComponent();
		MatrixBuilder.euclidean().assignTo(ava);
		content = JRViewerUtility.getContentPlugin(controller);
		FlyTool ft = new FlyTool();
		ft.setGain(.1);
		CameraUtility.getCameraNode(viewer).addTool(ft);
//		BackgroundColorsTool.addBackgroundColorsTool(viewer);
//		bcplugin.addChoice("special", new Color(100, 120, 80));
//		bcplugin.setColor("special");
	}

	
	public JRViewer getJRViewer()	{
		return pva;
	}
	
	public void loadScene(String loadableScene)	{
		LoadableSceneInterface ls = null;
		if (loadableScene != null)	{
		       try {
	            ls = (LoadableSceneInterface) Class.forName(loadableScene).newInstance();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
		loadScene(ls);
	}

	public void loadScene(LoadableSceneInterface ls) {
		System.err.println("Loading "+ls.getClass().getName());
		currentLoadedScene = ls;
		if (ls instanceof LoadableScene) {
			((LoadableScene) ls).psl = this;
		}
		SceneGraphComponent root = viewer.getSceneRoot();
		setRootAppearance(root);

		int metric = currentLoadedScene.getMetric();
		boolean isEncompass =  currentLoadedScene.isEncompass();
		boolean perspective = GlobalProperties.isPortal ? true : currentLoadedScene.isPerspective();
	    world = currentLoadedScene.makeWorld();
		lights = currentLoadedScene.makeLights();
		if (lights == null) lights = GlobalProperties.makeLightsS();
		CameraUtility.getCameraNode(viewer).addChild(lights);
		
		if (world != null) {		// sometimes the subclass has already added the world
//			pva.setContent(world);
			content.setContent(world);
//			if (doingVR) 
//				appInspector.setTriggerComponent(world);
			System.err.println("Loading world "+world.getName());
			if (world.getTransformation() == null) world.setTransformation(new Transformation());
		}			
		CameraUtility.getCamera(viewer).setPerspective(perspective);
		CameraUtilityOverflow.reset(CameraUtility.getCamera(viewer), metric);		
		if (isEncompass)	{
			CameraUtility.encompass(viewer);
		} 

		SceneGraphUtility.setMetric(root, metric);
		System.err.println("Setting metric to "+metric);
 
//		if (addBackPlane) {
//			Color[] corners = { new Color(.5f,.5f,1f), new Color(.5f,.5f,.5f),new Color(1f,.5f,.5f),new Color(.5f,1f,.5f) };
//			viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLORS, corners);
//			hasBackPlane = true;
//		}
		
		selectionManager = SelectionManagerImpl.selectionManagerForViewer(viewer);
		System.err.println("PSL: sm = "+selectionManager);
		List<SceneGraphPath> l = SceneGraphUtility.getPathsBetween(root, world);
		SceneGraphPath ds = (SceneGraphPath) l.get(0);
		selectionManager.setDefaultSelectionPath(ds);
		selectionManager.setSelectionPath(ds);	
		System.err.println("Def sel is "+ds);

		//add inspector of loadableScene
		appInspector.getShrinkPanel().setTitle(ls.getClass().getSimpleName());
		if (ls.hasInspector()) {
			JComponent c = (JComponent) ls.getInspector(viewer);
			if (c.getName() == "") c.setName("App");
			GridLayout gl = new GridLayout();
//			gl.setRows(1);
			ShrinkPanel shrink = new ShrinkPanel("Inspector");
			shrink.setLayout(gl);
			shrink.add(c);
			appInspector.addComponent(PluginSceneLoader.class, shrink, 1.0, "Default");
			lastInspectionPanel = shrink;
//			appInspector.setHelpResourceChecked(false);
			htmlDoc = ls.getClass().getSimpleName()+".html";
			docClass = ls.getClass();
			helpPath = ls.getHelpPath();
			docTitle = ls.getClass().getSimpleName();
			if (helpPath == null)	{	// construct default path: in subpackage help of the LS
				String hp = ls.getClass().getCanonicalName();
				int lastDot = hp.lastIndexOf(".");
				if (lastDot > 0) hp = hp.substring(0, lastDot+1);
				hp = hp.replace('.', '/');
				helpPath = "/"+hp+"help/";				
			}
			System.err.println("help path = "+helpPath);
			// this is required to get a clean 
			appInspector.getShrinkPanel().updateUI();
		} 

		//edit help menu to menu bar

		currentApplicationMenus = new JMenuBar();
		ls.customize(currentApplicationMenus, this);
		ViewMenuBar viewerMenu = pva.getPlugin(ViewMenuBar.class);
		menus.clear();
		for (int i = 0; i<currentApplicationMenus.getMenuCount(); ++i)	{
			JMenu m = currentApplicationMenus.getMenu(i);
			menus.add(m);
			System.err.println("adding menu "+m.getLabel());
			viewerMenu.addMenu(
					getClass(),
					1000.0,
					m
			);
			
		}
		DefaultMatrixSupport.getSharedInstance().storeDefaultMatrices(root);
		if (restore) {
			currentLoadedScene.restoreStates(controller);
			restore = false;
		}

	}

	public void unloadScene() {
		if (currentLoadedScene == null)return;
		currentLoadedScene.dispose();
		
		if (lastInspectionPanel != null) {
			appInspector.removeComponent(getClass(), (JComponent) lastInspectionPanel);
			lastInspectionPanel = null;
			System.err.println("removing panel");
		}
//		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLORS, Appearance.INHERITED);
//		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_TEXTURE2D, Appearance.INHERITED);
		CameraUtility.getCameraNode(viewer).getTransformation().setMatrix(Rn.identityMatrix(4));
//		viewer.getSceneRoot().setAppearance(null);
		SceneGraphPath newsel = new SceneGraphPath();
		newsel.push(viewer.getSceneRoot());
		selectionManager.setDefaultSelectionPath(newsel);
		selectionManager.setSelectionPath(newsel);
		selectionManager.clearSelections();
		if (lights != null && CameraUtility.getCameraNode(viewer).isDirectAncestor(lights))	
			CameraUtility.getCameraNode(viewer).removeChild(lights);
		lights = null;
		if (currentApplicationMenus != null)	{
			ViewMenuBar viewerMenu = pva.getPlugin(ViewMenuBar.class);
			System.err.println("menu count = "+menus.size());
			for (JMenu m : menus) {
				viewerMenu.removeMenu(getClass(), m);
				System.err.println("removing menu "+m);
			}
		}
	}


	private void setRootAppearance(SceneGraphComponent root) {
		if (root.getAppearance() == null) root.setAppearance(new Appearance());
		ShaderUtility.createRootAppearance( root.getAppearance());

		root.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
//		root.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
		root.getAppearance().setAttribute("polygonShader.diffuseColor", Color.WHITE);
		root.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
		root.getAppearance().setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, true);
		root.getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLORS, Appearance.INHERITED);
		root.getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(0,0,0,0));
		root.getAppearance().setAttribute(CommonAttributes.RMAN_GLOBAL_INCLUDE_FILE,"quality.rib");
	}

	private static String helpPath = "/charlesgunn/jreality/plugin/help/";
	private static String htmlDoc = "PluginSceneLoader.html";
	private static Class docClass = PluginSceneLoader.class;
	private static String docTitle = "PluginSceneLoader";
	public static SceneShrinkPanel createSceneShrinkPanel( final String title) {
		SceneShrinkPanel p = new SceneShrinkPanel() {
			
			{
				GridLayout gl = new GridLayout();
				gl.setRows(1);
				setInitialPosition(SHRINKER_LEFT);
				shrinkPanel.setName(title);
				shrinkPanel.setLayout(gl);
//				shrinkPanel.add(c);
			}
			
			@Override
			public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
				return View.class;
			}

			@Override
			public PluginInfo getPluginInfo() {
				return new PluginInfo(title);
			}
			
			@Override
			public String getHelpDocument() {
				System.err.println("Returning "+htmlDoc);
				return htmlDoc;
			}
			
			@Override
			public String getHelpPath() {
				return helpPath;
			}
			
			@Override
			public Class<?> getHelpHandle() {
				return docClass;
			}

		};
		return p;

	}
	static boolean debugCP = false,
			doTermes = false;
    public static void main(String[] args) {
		Set<String> params = new HashSet<String>();
		for (String param : args) params.add(param);
		if (params.contains("-termes"))
			doTermes = true;
		if (params.contains("-debugCP"))
			debugCP = true;
    	if (debugCP)	{
       	 String cp = System.getProperty("java.class.path").replace(':', '\n');
         System.err.println("cp = "+cp);
    		
    	}
		if (args != null && args.length != 0) System.err.println("args 0 is "+args[0]);
		String lsname = "charlesgunn.jreality.worlds.SimpleShapes";
		String propfile = null;
		if (args != null && args.length > 0) {
			lsname = args[0];
			if (args.length > 1 && !args[1].startsWith("-")) {
				propfile = args[1];
			}
		}
		if (propfile != null) System.err.println("propfile = "+propfile);
		try {
	            LoadableSceneInterface ls = (LoadableSceneInterface) Class.forName(lsname).newInstance();
	    		InputStream is = null;
	            if (propfile != null)  is = ls.getClass().getResourceAsStream(propfile);
	    		final PluginSceneLoader psl = new PluginSceneLoader(is);
	            psl.loadScene(ls);
		} catch (Exception e) {
	            e.printStackTrace();
		}

	}

    boolean restore = true;
	private BackgroundColor bcplugin;
//	Color[] bcolors = null;
//	@Override
//	public void restoreStates(Controller c) throws Exception {
//		System.err.println("PSL: restoring");
//		bcolors = c.getProperty(getClass(), "backgroundColors", null);
//		if (bcolors != null)
//			viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLORS, bcolors);
//		if (currentLoadedScene != null) 
//			currentLoadedScene.restoreStates(c);
//		else restore = true;
//		super.restoreStates(c);
//	}
//
//
//	@Override
//	public void storeStates(Controller c) throws Exception {
//		System.err.println("PSL: storing");
//		if (viewer != null) {
//			Object bgo = viewer.getSceneRoot().getAppearance().getAttribute(CommonAttributes.BACKGROUND_COLORS);
//			if (bgo != null && bgo instanceof Color[]) {
//				bcolors = (Color[]) bgo;
//				c.storeProperty(getClass(), "backgroundColors", bcolors);
//			}	
//		}
//		if (currentLoadedScene != null) 
//			currentLoadedScene.storeStates(c);
//		super.storeStates(c);
//	}
	@Override
	public PluginInfo getPluginInfo() {
			return new PluginInfo("Scene Loader, Plugin version", "Charles Gunn");	
	}
	public Controller getController() {
		return controller;
	}
	public void setController(Controller controller) {
		this.controller = controller;
	}
	public Sky getSky() {
		return sky;
	}
	public AnimationPlugin getAnimationPlugin() {
		return animated;
	}
	public Viewer getViewer() {
		return viewer;
	}
	public VRPanel getVRPanel() {
		return vrpanel;
	}
	public void setVRPanel(VRPanel vrpanel) {
		this.vrpanel = vrpanel;
	}

	

}