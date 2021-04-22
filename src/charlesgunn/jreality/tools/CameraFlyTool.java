/*
 * Created on May 27, 2004
 *
 */
package charlesgunn.jreality.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import charlesgunn.util.TextSlider;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.plugin.HelpOverlay;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.ToolContext;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

/**
 * @author Charles Gunn
 *
 */
public class CameraFlyTool extends MouseTool {
	SceneGraphComponent cameraNode;
	Transformation cameraTrans;
//	CameraDirectionKeyListener cdkl;
	double camFactor = 0.5;
	double[] transM;
	double[] xyTransM;
	double[] origCM;
	double[] rotM;
	double[] toVec = {0.0, 0.0, 0.0, 1d};
	double fixedFactor = 0.1;
	boolean isZTrans = true;
	boolean flyBackwards = false;
	boolean activated = false;
	boolean coMo = true;
	Camera theCamera;
	/**
	 * 
	 */
	public CameraFlyTool() {
		super();
		transM = Rn.identityMatrix(4);
		xyTransM = Rn.identityMatrix(4);
		origCM = Rn.identityMatrix(4);
		rotM = Rn.identityMatrix(4);
//		cdkl = new CameraDirectionKeyListener();
	}

	public void activate(ToolContext tc) {
		super.activate(tc);
		cameraNode = CameraUtility.getCameraNode(viewer);
		cameraTrans = cameraNode.getTransformation();
		if ( cameraTrans == null)	{
			JOGLConfiguration.theLog.log(Level.FINE,"No transform in CameraDirectionTool");
			return;
		}
//		viewer.getViewingComponent().addKeyListener(cdkl);
		theCamera = CameraUtility.getCamera(viewer);
		metric = SceneGraphUtility.getMetric(viewer.getCameraPath()); //cameraNode.getTransformation().getMetric();
		System.out.println("fly tool: metric is "+metric);
		cameraTrans.getMatrix(origCM);
		Rn.setIdentityMatrix(transM);
		Rn.setIdentityMatrix(rotM);
		if (continuedMotion != null)	{
			continuedMotion.stop();
		}
		System.err.println("viewer is "+viewer);
		continuedMotion = new ContinuedMotion(20, cameraNode, new ActionListener()	{
			final Transformation tt = cameraTrans;
			final double[] repeater1 = transM;
			public void actionPerformed(ActionEvent e) {
				if (tt == null || viewer == null) return;
				tt.multiplyOnRight(repeater1);
				//System.out.println("Camera tform is: \n"+Rn.matrixToString(tt.getMatrix()));
				viewer.renderAsync();
			}

		} );
		if (coMo &&  (button == 1) ) continuedMotion.start();
		activated = true;
	}

	static final double[] zaxis = {0,0,1};
	private double[] deltas;
	public void perform(ToolContext tc)	{
		super.perform(tc);
		//JOGLConfiguration.theLog.log(Level.FINE,"Mouse event"+e.toString());
		
		deltas = Rn.subtract(null, currentNDC, lastNDC);
		double s = Rn.euclideanNorm(deltas);
		double[] axis = new double[3];
		axis[0] = deltas[1];
		axis[1] = -deltas[0];
		axis[2] = 0.0;
		if (button == 1) {
			if (shift)	{
				isZTrans = false;				
			} else {
				isZTrans = true;
				setTranslation();
			}
			// change flight direction based on most recent two mouse positions
			cameraTrans.multiplyOnRight(P3.makeRotationMatrix(null, axis, s * camFactor));
			Rn.setIdentityMatrix(rotM);
		}	
		else if (button == 2) {
			
			if (shift)	{
				double[] zrot = P3.makeRotationMatrix(null, zaxis, toolStrength * -.5 * deltas[1] );
				cameraNode.getTransformation().multiplyOnRight(zrot);				
			} else {
				double[] absChange = new double[4];
				absChange[0] = currentNDC[0] - anchorNDC[0];
				absChange[1] = currentNDC[1] - anchorNDC[1];
				double[] tvec = Rn.times(null, fixedFactor*toolStrength, absChange);
				tvec[3] = 1.0;
				P3.makeTranslationMatrix(xyTransM, tvec, metric);
				cameraTrans.setMatrix(Rn.times(null, origCM, xyTransM));				
			}
		}
		viewer.renderAsync();
	}

	public void deactivate(ToolContext tc)	{
		super.deactivate(tc);
//		viewer.getViewingComponent().removeKeyListener(cdkl);
		continuedMotion.stop();
	}

	public String getName()	{return "Camera"; }
	
	public ImageIcon getIcon(int size) {
		return ToolManager.createImageIcon(size == ToolManager.LARGE ? "cameraToolIcon.png" : "cameraToolIcon-24.png");
	}
		
//	OneEntry speedEntry = null, nearEntry, farEntry, FOVEntry, eyeSepEntry, focusEntry;
	TextSlider FOVSlider, nearSlider, farSlider, eyeSepSlider, focusSlider;
	JCheckBox directionButton = null;
	protected void initializeInspectionPanel() {
			super.initializeInspectionPanel();
			System.err.println("In camerafly");
			directionButton = new JCheckBox("fly backwards", flyBackwards);
			directionButton.addActionListener( new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					flyBackwards = ((JCheckBox) e.getSource()).isSelected();
					if (viewer!= null) viewer.renderAsync();
				}
			});
			if (!activated) return;
//			directionButton.setFont(defaultFont);
			inspectionPanel.add(directionButton); 
			nearSlider = new TextSlider.Double("near",SwingConstants.HORIZONTAL,0.0,10.0,theCamera.getNear());
			nearSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					theCamera.setNear(nearSlider.getValue().doubleValue());
					if (viewer!= null) viewer.renderAsync();
				}
			});
			inspectionPanel.add(nearSlider);
			double min = theCamera.getNear();
			if (min > 0) min = min/10.0;
			farSlider = new TextSlider.Double("far",SwingConstants.HORIZONTAL,min,2000.0,
					theCamera.getFar() < 0 ? 10000 : theCamera.getFar());
			farSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					theCamera.setFar(farSlider.getValue().doubleValue());
					if (viewer!= null) viewer.renderAsync();
				}
			});
			inspectionPanel.add(farSlider);
			FOVSlider = new TextSlider.Double("FOV",SwingConstants.HORIZONTAL,0.0,179.0,theCamera.getFieldOfView());
			FOVSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					theCamera.setFieldOfView(FOVSlider.getValue().doubleValue());
					if (viewer!= null) viewer.renderAsync();
				}
			});
			inspectionPanel.add(FOVSlider);

			eyeSepSlider = new TextSlider.Double("eyeSep",SwingConstants.HORIZONTAL,0.0,10.0*theCamera.getEyeSeparation(),theCamera.getEyeSeparation());
			eyeSepSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					theCamera.setEyeSeparation(eyeSepSlider.getValue().doubleValue());
					if (viewer!= null) viewer.renderAsync();
				}
			});
			inspectionPanel.add(eyeSepSlider);
				
			focusSlider = new TextSlider.Double("focus",SwingConstants.HORIZONTAL,0.0,10.0*theCamera.getFocus(),theCamera.getFocus());
			focusSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					theCamera.setFocus(focusSlider.getValue().doubleValue());
					viewer.renderAsync();
				}
			});
			inspectionPanel.add(focusSlider);
	}

	private void setTranslation()	{
		toVec[0] = toVec[1] = 0.0; toVec[2] = flyBackwards ? (toolStrength*.001) : (-toolStrength * .001);
		synchronized(transM)	{
			P3.makeTranslationMatrix(transM, toVec, metric);					
		}		
	}

	public void registerHelp(HelpOverlay overlay) 	{	
		overlay.registerInfoString("Mouse button1 dragged", 
		"Fly forward/backward and turn camera in direction of mouse motion.");
		overlay.registerInfoString("Shift-Mouse button1 dragged", 
		"Turn camera in direction of mouse motion.");
		overlay.registerInfoString("Mouse button2 dragged", 
		"Move camera in plane parallel to screen (no forward velocity).");
		//overlay.registerMouseEvent(MouseEvent.MOUSE_DRAGGED, MouseEvent.BUTTON2_DOWN_MASK + MouseEvent.SHIFT_DOWN_MASK, 
		//"Fly ");
		overlay.registerInfoString("Shift-Mouse button2 dragged", 
		"Roll (rotate in (x,y) plane) based on mouse y-displacement");
		overlay.registerInfoString("Left arrow", "Fly backwards");
		overlay.registerInfoString("Right arrow", "Fly forwards");
		overlay.registerInfoString("Up/down arrows", "Increase/decrease speed");
	}
}

