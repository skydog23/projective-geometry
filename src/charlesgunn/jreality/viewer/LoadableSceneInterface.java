/*
 * Author	gunn
 * Created on Apr 28, 2005
 *
 */
package charlesgunn.jreality.viewer;

import java.awt.Component;

import javax.help.HelpSet;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;

import de.jreality.math.Pn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jtem.jrworkspace.plugin.Controller;

public interface LoadableSceneInterface {
	/**
	 * Create the scene graph and return its root; it will be added to the root of the associated viewer
	 * @return
	 */
	public SceneGraphComponent makeWorld();
	/**
	 * Create a scene graph containing lights; usually the default provided is adequate.
	 * @return
	 */
	public SceneGraphComponent makeLights();
	/**
	 * If {@link #hasInspector()} returns true, then the component returned by {@link #getInspector(Viewer)}
	 * is added to the inspection panel associated to the viewer.
	 */
	public boolean hasInspector();
	public Component getInspector(Viewer viewer);
	public void insertTabs(JTabbedPane tabs, Viewer v, Object va);

	/**
	 * return the metric metric of the ambient space {@link Pn}. Default: Pn.EUCLIDEAN
	 */
	public int getMetric();
	/**
	 * Whether to encompass the world on start-up.  Default: false
	 */
	public boolean isEncompass();
	/**
	 * Whether the camera should be perspective or not.  Default: false
	 */
	public boolean isPerspective();
	/**
	 * Add a colored backplane with different colored corners to the scene. Default: false
	 */
	public boolean addBackPlane();
	/**
	 * Called when the scene is unloaded; get rid of any instances no longer needed, etc.
	 *
	 */
	public void dispose();		
	/**
	 * Classes can provide an instance of {@link HelpSet} 
	 * which is found in the jar file with the name given by the String returned by {@link #getHelpSet()}.
	 * @return
	 */
	@Deprecated
	public boolean hasHelpset();
	
	@Deprecated
	public String getHelpSet();
	
	public String getHelpPath();
	/**
	 * After all the above methods have been called, this is called:
	 * one can add entries to the menu bar and also perform other customizations which require knowledge
	 * of the viewer (e.g., everything related to the camera, to the scene root, etc).
	 */
	public void customize(JMenuBar theMenuBar, Viewer viewer);
	public void customize(JMenuBar theMenuBar, PluginSceneLoader psl);
	
	public String getPropertiesFile();
	public void storeStates(Controller c);
	public void restoreStates(Controller c);

}
