/*
 * Author	gunn
 * Created on Mar 20, 2006
 *
 */
package charlesgunn.jreality.tools;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import charlesgunn.util.TextSlider;
import de.jreality.jogl.plugin.HelpOverlay;
import de.jreality.math.Matrix;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.SelectionManagerImpl;
import de.jreality.util.CameraUtility;

public abstract class MouseTool extends AbstractTool implements DocumentedTool {
	public double[] anchorNDC, currentNDC, lastNDC, diffNDC;
	public double mouseDisplacement;
	SelectionManager sm;
	protected Viewer viewer;
	protected double toolStrength = 1.0;
	long currentTime, lastTime;
	protected int button, wheel;
	protected boolean shift = false;
	protected ContinuedMotion continuedMotion;
	protected int metric;
	protected MotionManager mm = null;
	Font defaultFont = new Font("Helvetica", Font.PLAIN, 12);
	Box inspectionPanel = null;
	protected boolean isTracking = true;

	public MouseTool(InputSlot... activationSlots) {
		super(activationSlots);
		anchorNDC = new double[2];
		currentNDC = new double[2];
		lastNDC = new double[2];
		diffNDC = new double[2];
	}

	public MouseTool()	{
		this(	InputSlot.getDevice("PrimaryAction"),
				InputSlot.getDevice("PrimaryMenu"),
				InputSlot.getDevice("SecondaryAction"),
				InputSlot.getDevice("SecondaryMenu"),
				InputSlot.getDevice("SecondarySelection"),
				InputSlot.getDevice("PrimarySelection"),
				InputSlot.getDevice("PrimaryUp"),
				InputSlot.getDevice("PrimaryDown")
				);
		  //addCurrentSlot( InputSlot.getDevice("PointerTransformation"));
	}
	
	public void activate(ToolContext tc) {
		shift = false;
		wheel = 0;
		button = 0;
		if (tc.getSource()== InputSlot.getDevice("PrimaryAction")) {
			button = MouseEvent.BUTTON1;
		}
		else if (tc.getSource()== InputSlot.getDevice("SecondaryAction")) {
			button = MouseEvent.BUTTON1;
			shift = true;
		}
		else if (tc.getSource()== InputSlot.getDevice("PrimaryMenu")) {
			button = MouseEvent.BUTTON2;
		}
		else if (tc.getSource()== InputSlot.getDevice("SecondaryMenu")) {
			button = MouseEvent.BUTTON2;
			shift = true;
		}
		else if (tc.getSource()== InputSlot.getDevice("PrimarySelecion")) {
			button = MouseEvent.BUTTON3;
		}
		else if (tc.getSource()== InputSlot.getDevice("SecondarySelection")) {
			button = MouseEvent.BUTTON3;
			shift = true;
		}
//		System.err.println("Time is "+tc.getTime());
		viewer = tc.getViewer();
		sm=SelectionManagerImpl.selectionManagerForViewer(viewer);
//		Matrix m = new Matrix(tc.getTransformationMatrix(InputSlot.getDevice("PointerNDC")));
		double[] cc = getNDCFromPointer(tc);
		currentNDC[0] = anchorNDC[0] = cc[0];
		currentNDC[1] = anchorNDC[1] = cc[1];
		mouseDisplacement = 0.0;
//		addCurrentSlot(InputSlot.getDevice("PointerNDC"));
		addCurrentSlot(InputSlot.getDevice("PointerTransformation"));
	}
	
	public void deactivate(ToolContext tc) {
		diffNDC = Rn.subtract(diffNDC, currentNDC, anchorNDC);
		mouseDisplacement = Rn.euclideanNorm(diffNDC);
//		removeCurrentSlot(InputSlot.getDevice("PointerNDC"));
		removeCurrentSlot(InputSlot.getDevice("PointerTransformation"));
	}
	static double tol = 10E-8;
	public void perform(ToolContext tc) {
//		System.err.println("Time is "+tc.getTime());
		// try getting same info from pointer transformation (more general)
		// the last two columns represent the direction and position of the pointer
		// the position is located on the near clipping plane.  
//		Matrix m = new Matrix(tc.getTransformationMatrix(InputSlot.getDevice("PointerNDC")));
		double[] cc = getNDCFromPointer(tc);
		lastTime = currentTime;
		currentTime = tc.getTime();
		if ( !Rn.equals(currentNDC,cc,tol)) {
//			System.out.println("Copying new into current");
			Rn.copy(lastNDC, currentNDC);
			Rn.copy(currentNDC, cc);
			diffNDC = Rn.subtract(diffNDC, currentNDC, anchorNDC);
			mouseDisplacement = Rn.euclideanNorm(diffNDC);
		}
	}

	static protected double[] getNDCFromPointer(ToolContext tc)	{
		Matrix pointerMatrix = new Matrix(tc.getTransformationMatrix(InputSlot
				.getDevice("PointerTransformation")));
		double[] cam2ndc = CameraUtility.getCameraToNDC(tc.getViewer());
		double[] cam2world = tc.getTransformationMatrix(InputSlot.getDevice("CameraToWorld")).toDoubleArray(null);
		double[] world2cam = Rn.inverse(null, cam2world);
		double[] pointer2ndc = Rn.times(null, cam2ndc, Rn.times(null, world2cam, pointerMatrix.getArray()));
		Matrix m = new Matrix(pointer2ndc);
		double[] pos = m.getColumn(3);
		Pn.dehomogenize(pos, pos);
//		System.err.println("pointer = "+pos[0]+" "+pos[1]);
		return pos;
	}
	public List getOutputSlots() {
		return Collections.EMPTY_LIST;
	}
	
	public SceneGraphPath getAttachmentPath() {
		return null;
	}
	
	public void attachToViewer(Viewer v)	{
		
	}
	
	public void detachFromViewer()	{
		
	}
	abstract public void registerHelp(HelpOverlay overlay);
	abstract public String getName();
	abstract public ImageIcon getIcon(int size);

	public Component getInspector()	{
		if (inspectionPanel == null) initializeInspectionPanel();
		return inspectionPanel;
	}

	protected void initializeInspectionPanel()	{
		System.err.println("In super");
		TitledBorder title = BorderFactory.createTitledBorder(
	                BorderFactory.createRaisedBevelBorder(), getName()+" tool");
		inspectionPanel =  Box.createVerticalBox();
//		inspectionPanel.setPreferredSize(new Dimension(600,200));
		inspectionPanel.setBorder(title);
//		inspectionPanel.addComponentListener(new ComponentAdapter() {
//				public void componentShown(ComponentEvent e)	{
//					updateFromInspector();
//				}
//		});			
		final TextSlider strengthSlider = new TextSlider.Double("strength",SwingConstants.HORIZONTAL,0.0,10.0,toolStrength);
		strengthSlider.setFont(defaultFont);
		strengthSlider.setFont(defaultFont);
		inspectionPanel.add(strengthSlider);
		strengthSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				toolStrength = strengthSlider.getValue().doubleValue();
				if (viewer!= null) viewer.renderAsync();
			}
		});
	}
}
