/*
 * Author	gunn
 * Created on Apr 28, 2005
 *
 */
package charlesgunn.jreality.viewer;

import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;

import charlesgunn.anim.core.Animated;
import charlesgunn.anim.plugin.AnimationPlugin;
import de.jreality.math.Pn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.util.SceneGraphUtility;
import de.jtem.jrworkspace.plugin.Controller;


/**
 * See {@link LoadableSceneInterface}.
 * @author gunn
 *
 */
public abstract class LoadableScene  implements LoadableSceneInterface, Animated, Serializable {
	
	private static final long serialVersionUID = 1L;
	transient protected PluginSceneLoader psl;
	transient protected AnimationPlugin animationPlugin;
	transient protected SceneGraphComponent lsWorld = SceneGraphUtility.createFullSceneGraphComponent("helen");
	public abstract SceneGraphComponent makeWorld();
	protected Vector<String> exclude;
	{
		exclude = new Vector<String>();
		String[] excludo = {"helpSet","perspective","encompass","name","metric","propertiesFile","helpPath","valueAtTime"};
		for (String s: excludo) exclude.add(s);
	}
	/**
	 * @deprecated Use {@link #customize(JMenuBar, JRViewer)}.
	 */
	public void customize(JMenuBar menuBar, Viewer viewer) { return; }
	public void customize(JMenuBar menuBar, PluginSceneLoader psl) {
		customize(menuBar, psl.viewer); 
		this.psl = psl;
		animationPlugin = psl.getAnimationPlugin();
		animationPlugin.getAnimated().add(this);
	}

	public int getMetric() {
		return Pn.EUCLIDEAN;
	}

	public boolean isPerspective() {
		return true;
	}

	public boolean hasInspector()	{ return false; }
	public Component getInspector(Viewer v)	{ return null; }

	public void insertTabs(JTabbedPane tabs, Viewer v, Object va) {
		if (hasInspector())	{
			Component c = getInspector(v);
			tabs.add("Application", c);
		}
//		va.setFirstAccessory(c);
	}

	public boolean addBackPlane() {
		return false;
	}
	
	public boolean isEncompass() {
		return false;
	}

	public void dispose() {}
	
	public SceneGraphComponent makeLights()	{
		return null;
	}

	public boolean hasHelpset() {
		return false;
	}
	public String getHelpSet() {
		return null;
	}
	
	public String getHelpPath()	{
		return null;
	}

	public String getPropertiesFile() {
		return null;
	}

	public void restoreStates(Controller c) {
	}

	public void storeStates(Controller c) {
	}

	List<String> excludedProperties = new ArrayList<String>();
	public List<String> getExcludedProperties() {
		excludedProperties.add("metric");
		excludedProperties.add("encompass");
		return excludedProperties;
	}

	@Override
	public void setValueAtTime(double t) {
	}
	@Override
	public void setName(String name) {
	}
	@Override
	public String getName() {
		return this.getClass().getName();
	}
	@Override
	public void startAnimation() {
	}
	@Override
	public void endAnimation() {
	}
	@Override
	public void printState() {
	}
	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		LoadableScene ls = new 
//		LoadableScene mc = new ();
//		PluginSceneLoader psl = new PluginSceneLoader();
//		psl.loadScene(mc);
//	}


}
