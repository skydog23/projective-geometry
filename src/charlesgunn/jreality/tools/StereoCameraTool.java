/*
 * Created on Jun 4, 2004
 *
 */
package charlesgunn.jreality.tools;

import javax.swing.ImageIcon;

import de.jreality.jogl.plugin.HelpOverlay;
import de.jreality.scene.Camera;
import de.jreality.scene.tool.ToolContext;
import de.jreality.util.CameraUtility;

/**
 * @author Charles Gunn
 *
 */
public class StereoCameraTool extends MouseTool {
	Camera cam;
	double 
		eyeSeparationOrig,
		focusOrig;
	/**
	 * 
	 */
	public StereoCameraTool() {
		super();
	}


	@Override
	public void activate(ToolContext tc) {
		super.activate(tc);
		cam = CameraUtility.getCamera(tc.getViewer());
		eyeSeparationOrig = cam.getEyeSeparation();
		focusOrig = cam.getFocus();
	}


	@Override
	public void perform(ToolContext tc) {
		super.perform(tc);
		double dx = diffNDC[0];
		double dy = diffNDC[1];
		double val = 0.0;
		// decide which axis to consider
		if (Math.abs(dx) > Math.abs(dy))	{val = dx; dy = 0;}
		else {val = dy;  dx = 0;}
		val = Math.pow(3.0, val);
		boolean doEyeSeparation = true, doFocus = true;
		if (button == 2)	{
			if (dx == 0)	{
				doEyeSeparation = false;
			}
			if (dy == 0) 	{
				doFocus = false;
			}
		}
		if (doEyeSeparation)	{
			double es = eyeSeparationOrig * val;
			cam.setEyeSeparation(es);							
			System.err.println("In perform: eye sep: "+es);
		}
		if (doFocus)	{
			double f = focusOrig * val;
			cam.setFocus(f);							
			System.err.println("In perform: focus: "+f);
		}
		tc.getViewer().renderAsync();
	}

	public void registerHelp(HelpOverlay overlay) 	{	
		overlay.registerInfoString("Stereo camera tool", 
		"Adjust stereo settings for camera");
		overlay.registerInfoString("Mouse button1 dragged", 
		"Motion in x-direction adjusts eye-separation (left decreases).");
		overlay.registerInfoString("", 
		"Motion in y-direction adjusts focus (where left/right eye rays meet");
		overlay.registerInfoString("Mouse button2 dragged", 
		"Adjusts both together so ratio eyeSep:focus remains constant");
		overlay.registerInfoString("", 
		"All adjustments are exponential (no negative values arise) ");
	}

	public String getName() {
		return "stereo camera tool";
	}

	public ImageIcon getIcon(int size) {
		return ToolManager.createImageIcon(size == ToolManager.LARGE ? "stereoCameraIcon.png" : "stereoCameraIcon-24.png");

	}

	@Override
	public void deactivate(ToolContext tc) {
		// TODO Auto-generated method stub
		super.deactivate(tc);
	}


}
