/*
 * Created on Mar 23, 2004
 *
 */
package charlesgunn.jreality.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.swing.ImageIcon;

import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.plugin.HelpOverlay;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;

/**
 * @author Charles Gunn
 *
 */
public class TranslateShapeTool extends AbstractShapeTool {
	double[] 		zDirectionObject = new double[4];

	private double[] zDir = {0,0,1,0};
	public TranslateShapeTool(InputSlot... activationSlots) {
		super(activationSlots);
	}

	public TranslateShapeTool() {
		super();
	}
	public void activate(ToolContext  tc) {
		super.activate(tc);
		if (!isTracking) return;
		Rn.matrixTimesVector(zDirectionObject,theProjector.getCameraToObject(), zDir );
	}
	
	public void perform(ToolContext tc) {
		if (!isTracking) return;
		super.perform(tc);
//		if ( this.getClass() != TranslateShapeTool.class) return;
		final double[] mat ;
		if (button == 1)	{
			double f = 0.0;
			if (toolStrength <= 1.0) f = 1-toolStrength;
			if (metric == Pn.EUCLIDEAN)	{ //true) { //
				Rn.linearCombination(scaledV,f, anchorV, toolStrength , currentV);				
			} else 
				Pn.dragTowards(scaledV, anchorV, currentV, toolStrength*mouseDisplacement, metric);
			mat = P3.makeTranslationMatrix(null, anchorV, scaledV, metric);
		} else {
			double[] oV = new double[4];
			double[] zV = new double[4];
			double d= Pn.distanceBetween(anchorV, currentV, metric);
			if (currentNDC[1] < anchorNDC[1]) d = -d;
			double ed = theProjector.getDistanceToPlane();
			theProjector.getObjectPosition(anchorNDC, oV);
			theProjector.setDistanceToPlane(ed+toolStrength*d);
			theProjector.getObjectPosition(anchorNDC, zV);
			theProjector.setDistanceToPlane(ed);
			mat = P3.makeTranslationMatrix(null, oV, zV, metric);
		}
		adjust(mat);
		double[] comp = Rn.times(null, origM, mat);
		theEditedTransform.setMatrix(comp);
		viewer.renderAsync();
	}

	protected void adjust(double[] mat) {
	}

	public void deactivate(ToolContext tc)	{
		super.deactivate(tc);
		
		isTracking = false;
		if (theEditedTransform == null) return;
		long dt = currentTime - lastTime;
		if (dt == 0 || mouseDisplacement < cutoff) return;
		if (button != 1)		{
			JOGLConfiguration.theLog.log(Level.FINE,"Continued motion not yet implemented here");
			return;
		}
		theProjector.getObjectPosition(lastNDC, anchorV);
		theProjector.getObjectPosition(currentNDC, currentV);
		double size = .03* toolStrength * mouseDisplacement * (1000.0 /(currentTime-lastTime));
		//JOGLConfiguration.theLog.log(Level.FINE,"dt: "+dt+"Strength: "+strength+"size: "+size);
		Pn.linearInterpolation(currentV, anchorV, currentV, size, metric);
		final double[] mat = P3.makeTranslationMatrix(null, anchorV, currentV,metric);
		//SJOGLConfiguration.theLog.log(Level.FINE,"Translation: "+Rn.matrixToString(mat));
		continuedMotion = new ContinuedMotion(20, (SceneGraphComponent) theEditedNode, new ActionListener()	{
			final Transformation tt = theEditedTransform;
			final double[] repeater = mat;
			public void actionPerformed(ActionEvent e) {
				tt.multiplyOnRight(repeater);
				viewer.renderAsync();
			}

		} );
		if (mm!= null) mm.addMotion(continuedMotion);
}
	
	public void registerHelp(HelpOverlay overlay) 	{	
		overlay.registerInfoString("Translate tool", 
		"Translate currently selected scene graph component");
		overlay.registerInfoString("","a distance proportional to distance of mouse movement.");
		overlay.registerInfoString("Mouse button1 dragged", 
		"In direction of mouse motion");
		overlay.registerInfoString("Mouse button2/3 dragged", 
		"In direction perpendicular to plane of screen ");
	}
	public String getName() {
		return "translate";
	}
	
	public ImageIcon getIcon(int size) {
		return ToolManager.createImageIcon(size == ToolManager.LARGE ? "translateToolIcon.png" : "translateToolIcon-24.png");
	}


	public String getToolTip() {
		return "translate tool";
	}

}
