package charlesgunn.jreality.plugin;

import de.jreality.plugin.basic.View;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class TermesSphereSPP extends ShrinkPanelPlugin {

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Six-point perspective ", "Charles Gunn");
		info.isDynamic = false;
		return info;
	}
	@Override
	public String getHelpDocument() {
		return "http://page.math.tu-berlin.de/~gunn/Files/SixPointPerspectivePlugin.html";
	}
	
	@Override
	public String getHelpPath() {
		return null; //"/charlesgunn/jreality/plugin/help/";
	}
	
	@Override
	public Class<?> getHelpHandle() {
		return getClass();
	}

}
