/*
 * Created on Mar 23, 2004
 *
 */
package charlesgunn.jreality.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

import de.jreality.jogl.plugin.HelpOverlay;
import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.ToolContext;
import de.jreality.util.CameraUtility;

/**
 * @author Charles Gunn
 *
 */
public class CameraRotateTool extends AbstractShapeTool  {
	double	theAngle;
    double[] camera2world = null;
    double[] world2object = null;
    double[] camera2object = null;
    double[] wirbel2camera = null;
    double[] wirbel2object = null;
    double[] rotationAxis = new double[4];
    double translateDistance = 1.0;
    int metric = Pn.EUCLIDEAN;
	public CameraRotateTool() {
		super();
	}
	 

	double[] origCam;
	private Transformation camTransform;
	public void activate(ToolContext   tc) {
		super.activate(tc);
		camTransform = CameraUtility.getCameraNode(tc.getViewer()).getTransformation();
		origCam = camTransform.getMatrix();
	    world2object = new Matrix(tc.getRootToToolComponent().getInverseMatrix(null)).getArray(); 
	    camera2world = tc.getViewer().getCameraPath().getMatrix(null);
	    camera2object = Rn.times(null, world2object, camera2world);
	    Matrix m = new Matrix(camera2object);
	    rotationAxis = m.getColumn(1);		// image of y-axis
	    wirbel2camera = P3.makeTranslationMatrix(null, new double[]{0,0,translateDistance}, metric);
	    wirbel2object = Rn.times(null, world2object, Rn.times(null, camera2world,wirbel2camera ));
		return;
	}
	
	public void perform(ToolContext tc) {
		super.perform(tc);
		if (!isTracking) return;
		theAngle = diffNDC[0] * toolStrength;
//		double[] rot = P3.makeRotationMatrix(null, rotationAxis, theAngle);
		double[] rot = P3.makeRotationMatrixY(null, theAngle);
		double[]composite = Rn.conjugateByMatrix(null, rot, wirbel2camera);
//		theEditedTransform.setMatrix(Rn.times(null,origM, composite));
		camTransform.setMatrix(Rn.times(null, composite,origCam));
//		System.err.println("rotate tool. ");
		viewer.renderAsync();
	}

	public void deactivate(ToolContext tc) {
		super.deactivate(tc);
		if (!isTracking) return;
		if (camTransform == null) return;
//		if (!keepsMoving) return true;
		long dt = currentTime - lastTime;
		if (dt == 0 || diffNDC[0] == 0.0) {
			mm.pauseMotionsFor(theEditedNode);
			return;
		}
		double angle = .003* diffNDC[0] *  (1000.0 /(currentTime-lastTime));
		final double[] rot = P3.makeRotationMatrixY(null, angle);
		final double[]composite = Rn.conjugateByMatrix(null, rot, wirbel2camera);
		continuedMotion = new ContinuedMotion(20, theEditedNode, new ActionListener()	{
			public void actionPerformed(ActionEvent e) {
				camTransform.multiplyOnLeft(composite);
				viewer.renderAsync();
			}

		} );
		if (mm!= null) mm.addMotion(continuedMotion);
		return;
	}
	
	public void registerHelp(HelpOverlay overlay) 	{	
		overlay.registerInfoString("Camera Rotate tool", 
		"Rotate currently selected scene graph component in camera coordinate system around y-axis at distance z");
		overlay.registerInfoString("","through an angle proportional to distance of mouse movement.");
//		overlay.registerInfoString("Mouse button1 dragged", 
//		"Rotate around axis perpendicular to mouse motion passing through center of bounding box in object space.");
//		overlay.registerInfoString("Mouse button2 dragged", 
//		"Rotate around axis perpendicular to plane of screen through center of bounding box in object space.");
//		overlay.registerInfoString("Shift-key depressed", 
//		"Rotation is centered on object coordinate system origin.");
	}

	public String getName() {
		return "camera rotate";
	}

	public ImageIcon getIcon(int size) { return null; }
//	public ImageIcon getIcon(int size) {
//		return ToolManager.createImageIcon(size ==  ToolManager.LARGE? 
//				"rotateToolIcon.png" : "rotateToolIcon-24.png");
//	}


}
