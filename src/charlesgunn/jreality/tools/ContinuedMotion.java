/*
 * Author	gunn
 * Created on Oct 25, 2005
 *
 */
package charlesgunn.jreality.tools;

import java.awt.event.ActionListener;

import javax.swing.Timer;

import de.jreality.scene.SceneGraphComponent;

public class ContinuedMotion extends Timer {
	public SceneGraphComponent target;

	public ContinuedMotion(int arg0, SceneGraphComponent sgc, ActionListener arg1) {
		super(arg0, arg1);
		target = sgc;
	}

	public SceneGraphComponent getTarget() {
		return target;
	}

}
