/*
 * Created on Mar 23, 2004
 *
 */
package charlesgunn.jreality.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.jogl.plugin.HelpOverlay;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Pn;
import de.jreality.math.Quaternion;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.util.Rectangle3D;

/**
 * @author Charles Gunn
 *
 */
public class RotateShapeTool extends AbstractShapeTool  {
	protected Rotator 	theRotator = new Rotator();
	protected double	theAngle;
	public boolean subclassHandlesDeactivate = false;
	/**
	 * @param activationSlots
	 */
	public RotateShapeTool(InputSlot... activationSlots) {
		super(activationSlots);
	}


	public RotateShapeTool() {
		super();
	}
	 

	public void activate(ToolContext   tc) {
		super.activate(tc);
		if (!isTracking) return;
		theRotator = new Rotator(viewer);
		theRotator.setScaleFactor(toolStrength);
		theRotator.setAnchor(anchorNDC);
		double[] objectToWorld = selection.getMatrix(null);
		theRotator.setObjectToCamera(Rn.times(null, worldToCamera, objectToWorld));
		// TODO figure out a better strategy for setting the center of the rotation:
		// current version makes an effort to determine if it's sensible or not
		// but you want to have finer control over this.  E.g., you may want to use
		// the current pick point as the center, etc.
		//if ((button == 1 || (button == 3 && e.isShiftDown())) && theEditedTransform.getMetric() != Pn.ELLIPTIC)	{
		if (shift) centerOnBoundingBox = !centerOnBoundingBox;
		if (centerOnBoundingBox && button == MouseEvent.BUTTON1   && metric != Pn.ELLIPTIC)	{
			Rectangle3D bbox = BoundingBoxUtility.calculateChildrenBoundingBox(theEditedNode);
			//System.out.println("rotate bbox is "+bbox.toString());
			double dd = Rn.euclideanNorm(bbox.getExtent());
			if (dd > 10E-8 && dd < 10E8 ) myTransform.setCenter(bbox.getCenter()); 
			else 
				myTransform.setCenter(null);
		} else 
			myTransform.setCenter(null);
 
		return;
	}
	
	public void perform(ToolContext tc) {
		super.perform(tc);
		if (!isTracking) return;
		theRotator.setScaleFactor(toolStrength);
		Quaternion q = null;
		//JOGLConfiguration.theLog.log(Level.FINE,"Mouse is "+e.toString());
		if (button == 1) q = theRotator.getRotationXY(currentNDC);
		else q = theRotator.getRotationZ(currentNDC);
		myTransform.setRotation(q);
		double[]composite = Rn.times(null, origM, myTransform.getArray());
		theEditedTransform.setMatrix(composite);
//		System.err.println("rotate tool. ");
		viewer.renderAsync();
	}

	protected double finalAngle;
	protected double[] finalAxis;
	public void deactivate(ToolContext tc) {
		super.deactivate(tc);
		if (!isTracking) return;
		if (theEditedTransform == null) {isTracking = false; return;}
//		if (!keepsMoving) return true;
//		System.err.println("Matrix = "+Rn.matrixToJavaString(theEditedTransform.getMatrix()));
		long dt = currentTime - lastTime;
//		System.err.println("Mouse displacement is "+mouseDisplacement);
		mm.pauseMotionsFor(theEditedNode);
		if (dt == 0 || mouseDisplacement < cutoff) {
			isTracking = false;
			return;
		}

		Quaternion q = null;
		if (button == 1) q = theRotator.getRotationXY(lastNDC, currentNDC);
		else	q = theRotator.getRotationZ(lastNDC, currentNDC);
		theAngle = 2* Math.acos(q.re);
		if (theAngle > Math.PI) theAngle -= Math.PI * 2;
		Quaternion.IJK(theAxis, q);
		Rn.normalize(theAxis, theAxis);
		finalAngle = .3* mouseDisplacement * theAngle * (1000.0 /(currentTime-lastTime));
		finalAxis = (double[] ) theAxis.clone();
		if (subclassHandlesDeactivate) return;
		
			final FactoredMatrix repeater = new FactoredMatrix();
			repeater.setCenter(myTransform.getCenter());
			repeater.setRotation(finalAngle, finalAxis);

			continuedMotion = new ContinuedMotion(20, (SceneGraphComponent) theEditedNode, new ActionListener()	{
				final Transformation tt = theEditedTransform;
				final double[] mm = repeater.getArray();
				public void actionPerformed(ActionEvent e) {
					tt.multiplyOnRight( mm);
					viewer.renderAsync();
				}

			} );
		if (mm!= null) mm.addMotion(continuedMotion);	
		isTracking = false;
		return;
	}
	
	public void registerHelp(HelpOverlay overlay) 	{	
		overlay.registerInfoString("Rotate tool", 
		"Rotate currently selected scene graph component");
		overlay.registerInfoString("","through an angle proportional to distance of mouse movement.");
		overlay.registerInfoString("Mouse button1 dragged", 
		"Rotate around axis perpendicular to mouse motion passing through center of bounding box in object space.");
		overlay.registerInfoString("Mouse button2 dragged", 
		"Rotate around axis perpendicular to plane of screen through center of bounding box in object space.");
		overlay.registerInfoString("Shift-key depressed", 
		"Rotation is centered on object coordinate system origin.");
	}

	public String getName() {
		return "rotate";
	}

	public ImageIcon getIcon(int size) {
		return ToolManager.createImageIcon(size ==  ToolManager.LARGE? 
				"rotateToolIcon.png" : "rotateToolIcon-24.png");
	}


}
