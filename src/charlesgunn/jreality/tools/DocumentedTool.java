/*
 * Author	gunn
 * Created on Mar 21, 2006
 *
 */
package charlesgunn.jreality.tools;

import java.awt.Component;

import javax.swing.ImageIcon;

import de.jreality.jogl.plugin.HelpOverlay;

public interface DocumentedTool {

	abstract public void registerHelp(HelpOverlay overlay);
	abstract public String getName();
	abstract public Component getInspector();
	abstract public ImageIcon getIcon(int size);
}