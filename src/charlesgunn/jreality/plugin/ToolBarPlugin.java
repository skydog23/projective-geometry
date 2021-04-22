package charlesgunn.jreality.plugin;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import charlesgunn.jreality.tools.ToolManager;
import de.jreality.plugin.basic.View;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.flavor.PerspectiveFlavor;
import de.jtem.jrworkspace.plugin.flavor.ToolBarFlavor;
public class ToolBarPlugin extends Plugin implements ToolBarFlavor {

	JToolBar toolbar;
	private View view;

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		view = c.getPlugin(View.class);
		toolbar = ToolManager.toolManagerForViewer(view.getViewer().getCurrentViewer()).getToolbar();
		toolbar.setOrientation(SwingConstants.HORIZONTAL);
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo pi = new PluginInfo("my toolbar");
		return pi;
	}

	public Component getToolBarComponent() {
		if (toolbar == null) {
			return new JPanel();
		} else {
			return toolbar;
		}
	}

	public double getToolBarPriority() {
		return 0;
	}

	public Class<? extends PerspectiveFlavor> getPerspective() {
		return view.getClass();
	}

}
