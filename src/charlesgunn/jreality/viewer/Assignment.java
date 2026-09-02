/*
 * Created on Oct 26, 2012
 *
 */
package charlesgunn.jreality.viewer;


import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.anim.core.Animated;
import charlesgunn.anim.gui.AnimationPanel;
import charlesgunn.anim.gui.AnimationPanelEvent;
import charlesgunn.anim.gui.AnimationPanelListener;
import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.jreality.plugin.TermesSpherePlugin;
import charlesgunn.util.TextSlider;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.Shell;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewPreferences;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.content.ContentTools;
import de.jreality.plugin.content.DirectContent;
import de.jreality.plugin.experimental.ViewerKeyListenerPlugin;
import de.jreality.plugin.scene.ShrinkPanelAggregator;
import de.jreality.scene.Appearance;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.tutorial.util.FlyTool;
import de.jreality.util.CameraUtility;
import de.jreality.util.Input;
import de.jreality.util.Secure;
import de.jtem.beans.InspectorPanel;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.jtem.jrworkspace.plugin.simplecontroller.SimpleController.PropertiesMode;
/**
 * This class provides infrastructure for 
 * 		setting up jReality,
 * 		browsing of on-line documentation,  	
 * 		GUI for inspection of parameters, and
 * 		animation.
 *  
 * @author gunn
 */
public abstract class Assignment extends Plugin implements Animated {

		// for examples of use of these methods, see gunn.Assignment2
		transient protected boolean isAnimating = false;
		transient protected Scene scene = null;
		transient protected JRViewer jrviewer = new JRViewer();
		transient protected Viewer viewer = null;
		transient protected Content contentPlugin = new DirectContent();	// give subclasses chance to change this instance
		transient protected AnimationPlugin animationPlugin;
		transient protected Box inspector = Box.createVerticalBox();
		transient protected Vector<Plugin> pluginsToLoad = new Vector<Plugin>();
		{
			Locale.setDefault(Locale.US);
		}
		transient protected ShrinkPanelAggregator shrinkPanelPlugin = new ShrinkPanelAggregator() {
		@Override
		public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
			return View.class;
		}

		@Override
		public String getHelpTitle() {
			return Assignment.this.getClass().getSimpleName();
		}
		
		String pathFromFile = "";
		@Override
		public String getHelpDocument() {
			System.err.println("get help doc called ");
			String file =  getDocumentationFile();
			if (file == null) return null;
			if (file.startsWith("http://") ) return file;
			int lastDot = file.lastIndexOf("/");
			if (lastDot != -1) {
				pathFromFile = file.substring(0, lastDot+1);
				file = file.substring(lastDot+1, file.length());
				System.out.println("help document: " + file);
				return file;
			} else {
				System.out.println("help document: " + file);
				return file;
			}
			
		}

		@Override
		public String getHelpPath() {
			String file =  getHelpDocument();
			if (file == null || file.startsWith("http://")) return "";
			String hp = Assignment.this.getClass().getCanonicalName();
			int lastDot = hp.lastIndexOf(".");
			if (lastDot > 0) hp = hp.substring(0, lastDot+1);
			hp = hp.replace('.', '/');
			String result = "/"+hp+pathFromFile;
			System.out.println("help path: " + result);
			return result;
		}

		@Override
		public Class<?> getHelpHandle() {
			return Assignment.this.getClass();
		}

	};

	transient protected ShrinkPanel shrinkPanel = shrinkPanelPlugin.getShrinkPanel();

	public Assignment()	{
    		String cp = ((String)System.getProperty("java.class.path")).replace(':', '\n'); //split(":");
    		System.err.println("cp = "+cp);
		Scene.defaultZTranslation = 0.0;
		jrviewer = new JRViewer();
		setupJRViewer(jrviewer);
		jrviewer.registerPlugin(this);
		shrinkPanel.setTitle(this.getClass().getSimpleName());
	}

	@Override
	public void install(Controller con) throws Exception {
		super.install(con);
		scene = con.getPlugin(Scene.class);
		animationPlugin = con.getPlugin(AnimationPlugin.class);
		animationPlugin.setAnimateSceneGraph(false);
		animationPlugin.getAnimated().add(this);
		Component insp = getInspector();
		shrinkPanel.removeAll();
		shrinkPanel.setLayout(new ShrinkPanel.MinSizeGridBagLayout());
		Insets insets = new Insets(1,5,1,5);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = insets;
		c.weighty = 0.0;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.CENTER;
		if (insp != null) 
			shrinkPanel.add(insp, c);
	}
	
	/** 
	 * the returned SGC is used to set the content of the JRViewer
	 * @return
	 */
	public abstract SceneGraphComponent getContent();
	
	/**
	 * this can be used by the Assignment to inspect its state.
	 * See also {@link Assignment#getBeansInspector()}.
	 * @return
	 */
	public Component getInspector()	{
		if (addCameraLight ) {
			final TextSlider<Double> eSlider = new TextSlider.Double("headlight",  SwingConstants.HORIZONTAL, 0, 1, hlIntensity);
			eSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					hlIntensity = eSlider.getValue().doubleValue();
					headlight.setIntensity(hlIntensity);
				}});
			Box hbox = Box.createHorizontalBox();
			hbox.add(eSlider);
			inspector.add(hbox);
		}
		return inspector;
	}

	/**
	 * Automatically generated beans inspector.
	 * @return
	 */
	public Component getBeansInspector()	{
		InspectorPanel ip = new InspectorPanel(true);
		Vector<String> exclude = new Vector<String>();
		exclude.add("documentationFile");
		exclude.add("name");
		exclude.add("valueAtTime");
		exclude.add("content");
		ip.setObject(this, exclude);
		return ip;
	}
	

	/**
	 * replace this to customize the JRViewer as you wish
	 * @param v
	 */
	public void setupJRViewer(JRViewer v)	{
		v.getController().setPropertiesMode(PropertiesMode.StaticPropertiesFile);
		// if a property file is provided, use it.
		// Otherwise, look for a xml file with same name as this class (except
		// starts with a lower case letter) in the same folder as this class
		// advantage of this approach is that it also works with webstarts,
		// whereas a fixed property file apparently can't be found in the jar file
		File pf = getPropertyFile();
		if (pf != null) 
			v.getController().setStaticPropertiesFile(pf);
		else {
			String prn = getPropertyFileName();
			if (prn == null)	{
				String defaultPropName = this.getClass().getSimpleName()+".xml";
				String begin = defaultPropName.substring(0, 1),
						lcbegin = begin.toLowerCase();
				defaultPropName = defaultPropName.replaceFirst(begin, lcbegin);
				prn = defaultPropName;
			}
			System.err.println("name = "+prn);
			InputStream is = this.getClass().getResourceAsStream(prn);
            if (is != null) {
            		v.getController().setPropertiesMode(PropertiesMode.UserPropertiesFile);
//            		v.getController().setPropertiesInputStream(is);
            		System.err.println("prop file read");
            }
		}
		v.addBasicUI();
		for (Plugin pl : getPluginsToRegister())	{
			v.registerPlugin(pl);
		}
	}
	
	public List<Plugin> getPluginsToRegister()	{
		pluginsToLoad.add(new Shell());
		pluginsToLoad.add(contentPlugin);
		pluginsToLoad.add(new ContentTools());
		pluginsToLoad.add(new ContentLoader());
		pluginsToLoad.add(new ViewPreferences());
		animationPlugin = new AnimationPlugin();
		pluginsToLoad.add(animationPlugin);
		pluginsToLoad.add(new ViewerKeyListenerPlugin());
		pluginsToLoad.add(shrinkPanelPlugin);
		pluginsToLoad.add(new TermesSpherePlugin());
		return pluginsToLoad;
	}
	public  File getPropertyFile() {
		return null;
	}
	
	public String getPropertyFileName() {
		return null;
	}
	/**
	 * the returned String should be a html file in the same directory as your main java class
	 * @return
	 */
	public String getDocumentationFile() { return null; }
	
	private boolean finished = false;
	private void setFinished(boolean b) {
		finished = b;
	}
	public void runAnimationFile(String file) {
		AnimationPanel ap = animationPlugin.getAnimationPanel();
		ap.addAnimationPanelListener(new AnimationPanelListener() {
			@Override
			public void actionPerformed(AnimationPanelEvent e) {
				switch (e.type)	{
				case PLAYBACK_COMPLETED:
					setFinished(true);
				}
			}
			@Override
			public String getName() {
				return null;
			}
			@Override
			public void printState() {
			}
			@Override
			public Object getState() {
				return null;
			}
			@Override
			public void setState(Object o) {
			}
		});
		
		ap.setPaused(true);
		try {
			ap.read(
				new Input(this.getClass().getResource(file)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ap.setRecording(true);
		System.err.println("starting playback");
		finished = false;
		ap.startPlayback();
		do {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (!finished);
		System.err.println(this.getClass().getCanonicalName()+" animation finished");
	}
	/**
	 * This is called once to activate the application
	 */
	protected boolean useContent = true;
	public void display()	{
		jrviewer.startup();
		viewer = jrviewer.getViewer();
		if (addCameraLight) addCameraLight(hlIntensity);
		// comment out the following to get transparent black background
		Appearance ap = jrviewer.getViewer().getSceneRoot().getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute("backgroundColors", Appearance.INHERITED);
		ap.setAttribute("backgroundColor", new Color(0,0,0,0));
		if (useContent) {
			SceneGraphComponent content = getContent();
			contentPlugin.setContent(content);			
		}
		// add keyboard listener
		Component comp = ((Component) jrviewer.getViewer().getViewingComponent());
		comp.addKeyListener(new KeyAdapter() {
 				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_H:
						Assignment.this.browseDocumentation();
						break;
		
				}
 				}
			});
	}
	
	boolean addCameraLight = false;
	protected double hlIntensity = .6;
    PointLight headlight = new PointLight();
	public boolean isAddCameraLight() {
		return addCameraLight;
	}

	// this has to  be called before display() is called!
	public void setAddCameraLight(boolean addCameraLight) {
		this.addCameraLight = addCameraLight;
	}

	protected void addCameraLight(double intensity) {
	    SceneGraphComponent cnode = CameraUtility.getCameraNode(viewer);
	    headlight.setColor(Color.white);
	    headlight.setGlobal(true);
	    headlight.setIntensity(intensity);
	    headlight.setFalloff(new double[] {1,0,0});
	    cnode.setLight(headlight);
	}
	/**
	 * handle display of on-line documentation using the Desktop object of Java 6
	 */
	transient boolean browserActivated = false;
    transient Desktop desktop;
	transient private String externalHttpLocation = "http://page.math.tu-berlin.de/~gunn/Files/mvws13html/"; //"http://www3.math.tu-berlin.de/geometrie/Lehre/WS13/MathVis/resources/projects/html/";
	public void browseDocumentation()	{
		String foo = Secure.getProperty("jnlp.webstart");
		boolean isWebstart =  (foo != null && (foo.startsWith("true"))) ? true : false;
		System.err.println("is webstart = "+isWebstart);
		String docName = getDocumentationFile();
		if (docName == null) return;
		if (browserActivated) {
			System.err.println("URL already open in browser");
			return;
		}
		System.err.println("doc file ="+docName);
        if (Desktop.isDesktopSupported()) {
        		desktop = Desktop.getDesktop();
            if (desktop != null && desktop.isSupported(Action.BROWSE)) {
            	URI uri = null;
            	// this all needs to be checked over by a URL guru who knows how to handle the different cases
			try {
				if (docName.startsWith("http") || docName.startsWith("file")) uri = new URI(docName);
				else {
					URL url = null;
					if (isWebstart) {
						// this is a hack.  I use it to create a version for use with webstart, that
						// reads the html doc from an external location where I have dumped all the
						// html docs and accessory files for all the student projects
						if (docName.startsWith("html/")) docName = docName.substring(5);
						uri = new URI(externalHttpLocation+docName);	
					} else {
						url = this.getClass().getResource(docName);
						try {
						    uri = new URI(url.toString()); 
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} 
            	try {
            		System.err.println("opening uri "+uri);
					desktop.browse(uri);
			        browserActivated = true;
				} catch (IOException e) {
				}
            }
        }
	}

	// The rest of the methods are the implementation of Animated.
	@Override
	public void startAnimation() {
		isAnimating = true;
	}
	@Override
	public void endAnimation() {
		isAnimating = false;
	}
	@Override
	public void printState() {
	}
	@Override
	public String getName() {
		return "Assignment";
	}
	@Override
	public void setName(String s) {
	}
	@Override
	public void setValueAtTime(double d) {
	}

	public JRViewer getJrviewer() {
		return jrviewer;
	}


}