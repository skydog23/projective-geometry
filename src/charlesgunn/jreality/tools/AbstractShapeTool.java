/*
 * Created on Mar 23, 2004
 *
 */
package charlesgunn.jreality.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import de.jreality.jogl.JOGLRenderer;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.util.LoggingSystem;

/**
 * @author Charles Gunn
 *
 */
public abstract class AbstractShapeTool extends MouseTool {
	double[] 	origM; 		// original object transformation
	protected SceneGraphPath 	selection;	// two selections: altSel is the "center"
	SceneGraphPath alternateSelection;
			// of the motion; sel is the object moved
	public Transformation theEditedTransform;	// the transform that gets edited
	protected FactoredMatrix myTransform;
	protected boolean multipleMovements = true;
	protected double[] 	theAxis;
	protected double[]	worldToCamera;			// get this from the camera in startTrackingAt()
	protected double[] anchorV = new double[4];
	protected double[] currentV = new double[4];
	protected double[] scaledV = new double[4];
	protected PlaneProjector theProjector;
	protected double distance;
	double duration;
	double cutoff = .06;
	public SceneGraphComponent theEditedNode;
	protected boolean centerOnBoundingBox = true;
	public static SceneGraphPath CURRENT_SELECTION = new SceneGraphPath();
	static boolean deactivatePicking = false;
	
	/**
	 * @param activationSlots
	 */
	public AbstractShapeTool(InputSlot... activationSlots) {
		super(activationSlots);
		duration = 400.0;			// make it quasi-infinite
		myTransform =  new FactoredMatrix();
		origM = new double[16];
		theAxis = new double[3];
	}
	public AbstractShapeTool() {
		super();
		duration = 400.0;			// make it quasi-infinite
		myTransform =  new FactoredMatrix();
		origM = new double[16];
		theAxis = new double[3];
	}
	boolean useSelection = false;
	  transient EffectiveAppearance eap;
		public static void setDeactivatePicking(boolean b)	{
			deactivatePicking = b;
		}
	public void activate(ToolContext tc) {
		// the shape tools need a selection path and a shape to
		// act upon
		super.activate(tc);
		isTracking  = false;
		Object	nodeContents;
		if (useSelection)	{
			selection = sm.getSelectionPath();
		} else {
			selection = tc.getRootToToolComponent();
			if (selection.getLength() == 0) return;
		}
		if (selection == null) selection = sm.getDefaultSelectionPath();
		if (selection == null) return;
		nodeContents = selection.getLastComponent();			
		if (nodeContents == null ||
				!(nodeContents instanceof SceneGraphComponent)) return;
		theEditedNode = (SceneGraphComponent) nodeContents;
		theEditedTransform = theEditedNode.getTransformation();
		if (theEditedTransform == null || (theEditedTransform.isReadOnly())) return;

		isTracking = true;
		mm = MotionManager.motionManagerForViewer(viewer);
//		System.err.println("Tracking is true");
		//theEditedTransform.setDoFactor(false);
		Rn.copy(origM,theEditedTransform.getMatrix());
//		metric = theEditedTransform.getMetric();
		myTransform =  new FactoredMatrix();
		//worldToCamera = theCamera.getWorldToCameraMatrix();
		worldToCamera = viewer.getCameraPath().getInverseMatrix(worldToCamera);
		
		if (eap == null || !EffectiveAppearance.matches(eap, tc.getRootToToolComponent())) {
		        eap = EffectiveAppearance.create(tc.getRootToToolComponent());
		}
		metric = eap.getAttribute("metric", Pn.EUCLIDEAN);
	    LoggingSystem.getLogger(JOGLRenderer.class).fine("Path is "+selection.toString());
	    LoggingSystem.getLogger(JOGLRenderer.class).fine("metric is "+metric);
	    
		centerOnBoundingBox = eap.getAttribute(CommonAttributes.CENTER_ON_BOUNDING_BOX, true);
		theProjector = new PlaneProjector(viewer);
		theProjector.setAnchor(anchorNDC);
		double[] objectToWorld = selection.getMatrix(null);
		theProjector.setObjectToCamera(Rn.times(null, worldToCamera, objectToWorld));
		theProjector.setDefaultPlane();
		distance = theProjector.getDistanceToPlane();

		if (continuedMotion!= null && !multipleMovements) {
			mm.removeMotion(continuedMotion);
			continuedMotion = null;
		} else 			
			mm.pauseMotionsFor(theEditedNode);
		isTracking = true;
		
//		((Component) (viewer.getViewingComponent())).addKeyListener( new KeyAdapter()	{
//			public void keyPressed(KeyEvent e) {
//				switch(e.getKeyCode())	{
//				
//				case KeyEvent.VK_5:
//					if (e.isShiftDown()) break;
//					if (theEditedTransform != null)
//						System.err.println("Shape tool matrix is: \n"+Rn.matrixToJavaString(theEditedTransform.getMatrix()));
//					break;
//	
//				}
//			}	
//			
//		});
//
		return;
	}


	public void perform(ToolContext tc) {
		if (!isTracking) return;
		super.perform(tc);
		theProjector.getObjectPosition(anchorNDC, anchorV);
		theProjector.getObjectPosition(currentNDC, currentV);
	}
	boolean pickable = false;
	@Override
	public void attachToViewer(Viewer v) {
		viewer = v;
		if (!deactivatePicking) return;
		pickable = viewer.getSceneRoot().isPickable();
		viewer.getSceneRoot().setPickable( false);
		System.err.println("Setting pickable to false");
	}

	@Override
	public void detachFromViewer() {
		if (viewer == null) return;
		if (!deactivatePicking) return;
		viewer.getSceneRoot().setPickable( pickable);
		System.err.println("Setting pickable to "+pickable);
	}
	@Override
	public SceneGraphPath getAttachmentPath() {
		return CURRENT_SELECTION;
	}

	JCheckBox manyMotionsButton = null;
	protected void initializeInspectionPanel() {
		super.initializeInspectionPanel();
		manyMotionsButton = new JCheckBox("multiple motions", multipleMovements);
		manyMotionsButton.addActionListener( new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				multipleMovements = ((JCheckBox) e.getSource()).isSelected();
				viewer.renderAsync();
			}
		});
		manyMotionsButton.setFont(defaultFont);
		inspectionPanel.add(manyMotionsButton); 
	}


	public boolean isCenterOnBoundingBox() {
		return centerOnBoundingBox;
	}


	public void setCenterOnBoundingBox(boolean centerOnBoundingBox) {
		this.centerOnBoundingBox = centerOnBoundingBox;
	}

}


