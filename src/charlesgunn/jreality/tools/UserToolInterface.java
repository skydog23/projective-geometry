/*
 * Created on Nov 9, 2004
 *
 */
package charlesgunn.jreality.tools;

import de.jreality.jogl.plugin.HelpOverlay;



/**
 * @author gunn
 *
 */
public interface UserToolInterface {
	public void startTracking(UserTool t);
	public void track(UserTool t);
	public void endTracking(UserTool t);
	public void registerHelp(HelpOverlay ho);
}
