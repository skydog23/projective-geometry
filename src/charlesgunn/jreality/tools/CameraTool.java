/*
 * Author	gunn
 * Created on Feb 28, 2006
 *
 */
package charlesgunn.jreality.tools;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.swing.ImageIcon;

import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.plugin.HelpOverlay;
import de.jreality.math.Rn;
import de.jreality.scene.Camera;
import de.jreality.scene.tool.ToolContext;
import de.jreality.util.CameraUtility;

public class CameraTool extends MouseTool {

	Camera camera;
	double[] centerPointNDC;
	boolean originalIsOnAxis;
	double originalFOV, aspectRatio;
	Rectangle2D originalViewport, newViewport;
	ArrayList al = new ArrayList();  
	ArrayList al2 = new ArrayList();
	double[] lowerLeft = new double[2], upperRight = new double[2];
	double[] centerVwpt = new double[2];
	double originalNear = 0;
	public CameraTool() {
		super();
	}
	 
	public void activate(ToolContext tc) {
		super.activate(tc);
		camera = CameraUtility.getCamera(tc.getViewer());
		if (button == 1 || wheel != 0) {
			aspectRatio = CameraUtility.getAspectRatio(tc.getViewer());
			originalFOV = camera.getFieldOfView();
			originalViewport = CameraUtility.getViewport(camera, aspectRatio);
			centerVwpt[0] = (anchorNDC[0] + 1)*.5 * originalViewport.getWidth() + originalViewport.getMinX();
			centerVwpt[1] = (anchorNDC[1] + 1)*.5 * originalViewport.getHeight() + originalViewport.getMinY();
			lowerLeft[0] = originalViewport.getMinX();
			lowerLeft[1] = originalViewport.getMinY();
			upperRight[0] = originalViewport.getMaxX();
			upperRight[1] = originalViewport.getMaxY();
			JOGLConfiguration.theLog.log(Level.FINE,"old newViewport is "+originalViewport.toString());
			JOGLConfiguration.theLog.log(Level.FINE,"pick point in viewport coords is"+Rn.toString(centerVwpt));
			newViewport = new Rectangle2D.Double();			
		} else {
			originalNear = camera.getNear();			
		}
		if (wheel != 0) perform(tc);
	}

	public void perform(ToolContext tc) {
		super.perform(tc);
		if (wheel != 0) {
			double fov = camera.getFieldOfView();
			camera.setFieldOfView( fov * ((wheel > 0) ? 1.05 : 1/1.05));	
		}
		else if (button == 1) {
			if (!shift)	{
				double fov = Math.exp(-toolStrength * diffNDC[1]) * originalFOV;
				camera.setFieldOfView(fov > 179.0 ? 179.0 : fov);	
				System.err.println("Setting fov to "+camera.getFieldOfView());
			} else {
				double factor = Rn.euclideanNorm(diffNDC);
				if (factor > .99) factor = .99;
				double[] lowerLeftT = Rn.linearCombination(null, 1-factor, lowerLeft, factor, centerVwpt);
				double[] upperRightT = Rn.linearCombination(null, 1-factor, upperRight, factor, centerVwpt);
				newViewport.setFrameFromDiagonal(lowerLeftT[0], lowerLeftT[1], upperRightT[0], upperRightT[1]);
				JOGLConfiguration.theLog.log(Level.FINE,"new newViewport center is "+newViewport.getCenterX()+":"+newViewport.getCenterY());
				camera.setOnAxis(false);
				camera.setViewPort(newViewport);		
			}			
		} else if (button == 2)	{
			// near clipping plane
			double near = Math.exp(toolStrength * diffNDC[1]) * originalNear;
			camera.setNear(near);
			System.err.println("Near is "+near);
		}
		viewer.renderAsync();
	}

	public void deactivate(ToolContext tc) {
		super.deactivate(tc);
		if (button == 1) {
			if (shift) {
				camera.setOnAxis(originalIsOnAxis);
				if (!camera.isOnAxis()) camera.setViewPort(originalViewport);
			}			
		} else if (button == 2) 
			if (!shift) camera.setNear(originalNear);
		
		viewer.renderAsync();
	}

	public void registerHelp(HelpOverlay overlay) {
		overlay.registerInfoString("Camera tool", 
		"Manipulate camera settings (field of view and near clip plane");
		overlay.registerInfoString("Mouse button1 dragged", 
		"Increase/decrease on-axis field of view proportional to y-displacement of drag motion");
		overlay.registerInfoString("Shift-Mouse button1 dragged", 
		"Increase/decrease field of view centered on mouse position (reverts to original on release");
		overlay.registerInfoString("Mouse button2 dragged", 
		"Increase/decrease near clipping plane proportional to y-displacement of drag motion (reverts on release).");
		overlay.registerInfoString("Shift-Mouse button2 depressed", 
		"As previous, but doesn't revert on release");
	}

	public String getName() {
		return "Camera zoom";
	}

	public ImageIcon getIcon(int size) {
		return ToolManager.createImageIcon(size == ToolManager.LARGE ? "cameraZoomToolIcon.png" : "cameraZoomToolIcon-24.png",this.getClass());
	}
	
}
