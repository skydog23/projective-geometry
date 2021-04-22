package charlesgunn.bicycle;

import java.io.IOException;
import java.util.prefs.InvalidPreferencesFormatException;

import javax.swing.JTabbedPane;

import charlesgunn.jreality.viewer.GlobalProperties;
import de.jreality.math.Matrix;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.swing.jrwindows.JRWindow;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;
import de.jreality.vr.ViewerVR;

public class BicycleVR {

	static ViewerVR vr;
	static JRWindow win;
	static JTabbedPane tabs = new JTabbedPane();

	private ViewerApp va;

	public BicycleVR() {
		vr = ViewerVR.createDefaultViewerVR(null);
		vr.setDoAlign(false);
		va = vr.initialize();
		try {
			vr.importPreferences(BicycleVR.class
					.getResourceAsStream("vrprefsBicycle.xml"));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidPreferencesFormatException e) {
			e.printStackTrace();
		}
		va.update();
		va.display();

		BicycleFactory bf = new BicycleFactory();

		SceneGraphComponent sgc = bf.getBicycle();
		vr.setContent(sgc);
		vr.setContentMatrix(new Matrix());

		win = vr.getWindowManager().createFrame();
		win.getFrame().setTitle("Bicycle Simulation");
		bf.insertTabs(tabs);

		win.resize(1);
		sgc.addTool(win.getPanelTool());

		win.getFrame().getContentPane().add(tabs);
		win.getFrame().pack();
		vr.setAvatarPosition(0, 0, 5);
	}

	public static void main(String[] args) {
		try {
			remoteMain(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ViewerApp remoteMain(String[] args) throws IOException {
		Secure.setProperty(SystemProperties.VIEWER,
				GlobalProperties.DEFAULT_VIEWER); 
		Secure.setProperty("doOwnTools", "false"); 
		BicycleVR nbv = new BicycleVR();
		return nbv.va;
	}
}
