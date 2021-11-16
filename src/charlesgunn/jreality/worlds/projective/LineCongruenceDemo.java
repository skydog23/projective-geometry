/*
 * Created on Jan 29, 2004
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import charlesgunn.jreality.geometry.projective.LineCongruenceFactory;
import charlesgunn.jreality.geometry.projective.LinePencilFactory;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.geometry.projective.RegulusFactory;
import charlesgunn.jreality.newtools.SelectLineTool;
import charlesgunn.jreality.newtools.SelectLineTool.LineSelectionEvent;
import charlesgunn.jreality.tools.ToolManager;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.math.p5.PlueckerLineGeometry;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.GeometryUtility;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Sphere;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.PickResult;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;


public class LineCongruenceDemo extends Assignment implements SelectLineTool.LineSelectionListener {
	SceneGraphComponent nullPencil, redComplexLine, whiteComplexLine;
	boolean tryFlatten = true;
	protected boolean showCongruence = true,
		showCongReg = false,
		showComplexReg = false;
	private double sphereRadius = 10.0;
	private int numberPencils = 12;
	private double tubeRadius = .006;
	private Color axisColor = new Color(50, 50, 255);
	private SceneGraphComponent theWorld;
	RegulusFactory congruenceRegulus = RegulusFactory.getRegulusFactory(),
		lineComplexRegulus = RegulusFactory.getRegulusFactory();
	double[] line0 = {0,0,0,0,1,0},			// vertical line
				line1 = {0,1,0,0,0,0};	// P3.lineFromPoints(null, new double[]{2,0,0,1}, new double[]{2,1,0,1}); 
	private LinePencilFactory nullPencilFactory = new LinePencilFactory();
	
	List<LinePencilFactory> nullPencils = new ArrayList<LinePencilFactory>();
	private SceneGraphComponent congruence;
	private SceneGraphComponent selectedLineSGC;
	private PointRangeFactory selectedLineFactory;
	private double[] selectedPoint;
	private double[] selectedLine;
	private LineCongruenceFactory congruenceFactory;
	private PointRangeFactory redFactory = new PointRangeFactory(),
		whiteFactory = new PointRangeFactory();
	
	@Override
	public SceneGraphComponent getContent()	{
		theWorld = SceneGraphUtility.createFullSceneGraphComponent("world");
		theWorld.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", Color.white);
		theWorld.getAppearance().setAttribute("lineShader.polygonShader.ambientColor", Color.white);
		theWorld.getAppearance().setAttribute("lineShader.polygonShader.ambientCoefficient", .02);
		theWorld.getAppearance().setAttribute("lineShader.diffuseColor", Color.white);
		theWorld.getAppearance().setAttribute("lineShader.tubeRadius", tubeRadius);
		theWorld.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, true);
		SceneGraphComponent pyrsgc = SceneGraphUtility.createFullSceneGraphComponent("sphere");
		theWorld.addChild(pyrsgc);
		redComplexLine = SceneGraphUtility.createFullSceneGraphComponent("red line");
		redComplexLine.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", new Color(255, 0, 0));
		redComplexLine.setPickable( false);
		theWorld.addChild(redComplexLine);
		whiteComplexLine = SceneGraphUtility.createFullSceneGraphComponent("white line");
		whiteComplexLine.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", Color.white);
		theWorld.addChild(whiteComplexLine);
		nullPencil = nullPencilFactory.getPencil();
		nullPencil.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", new Color(255, 0, 0));
		nullPencil.getAppearance().setAttribute("lineShader.tubeRadius", .5*tubeRadius);
		nullPencil.setPickable( false);
		theWorld.addChild(nullPencil);
		pyrsgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, 
				new Color(1f, 1f,1f,0f));
		pyrsgc.setPickable( false);
		pyrsgc.setGeometry(new Sphere());
		
		congruence = SceneGraphUtility.createFullSceneGraphComponent("congruence");
		Appearance ap = congruence.getAppearance();
		ap.setAttribute("lineShader.tubeRadius", tubeRadius*.5);
		theWorld.addChild(congruence);
		boolean finiteSphere = true;
		congruenceFactory = new LineCongruenceFactory();
		congruenceFactory.setLine0(line0);
		congruenceFactory.setLine1(line1);
		congruenceFactory.setNumberPencils0(18);
		congruenceFactory.setNumberPencils1(12);
		congruenceFactory.setNumberJoints(12);
		congruenceFactory.setFiniteSphere(finiteSphere);
		congruenceFactory.setSphereRadius(sphereRadius);
		congruenceFactory.update();
		SceneGraphComponent sgc = congruenceFactory.getSceneGraphComponent();
		congruence.addChild(sgc);				
		PointRangeFactory lf = new PointRangeFactory();
		lf.setNumberOfSamples(numberPencils);
		lf.setFiniteSphere(finiteSphere);
		lf.setSphereRadius(sphereRadius);
		lf.setElement0(new double[]{0,0,0,1});
		lf.setElement1(new double[]{0,1,0,1});
		lf.update();
		IndexedLineSet axis = lf.getLine();
		axis.setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		SceneGraphComponent axissgc = SceneGraphUtility.createFullSceneGraphComponent("axis");
		axissgc.setGeometry(axis);
		ap = axissgc.getAppearance();
		ap.setAttribute("lineShader.polygonShader.diffuseColor", axisColor);
		ap.setAttribute("lineShader.polygonShader.ambientColor", Color.white);
		ap.setAttribute("lineShader.polygonShader.ambientCoefficient", .2);
		ap.setAttribute("lineShader.diffuseColor", axisColor);
		ap.setAttribute("lineShader.tubeRadius", 2*tubeRadius);
		ap.setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, true);
		theWorld.addChild(axissgc);
		
		selectedLineSGC = SceneGraphUtility.createFullSceneGraphComponent();
		selectedLineSGC.setOwner(this);
		selectedLineSGC.setVisible(true);
		selectedLineSGC.setPickable(false);
		theWorld.addChild(selectedLineSGC);
		ap = selectedLineSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", Color.green);
		ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.green);
		ap.setAttribute("lineShader.tubeRadius", tubeRadius);
		return theWorld;
	}

	@Override
	public void display() {
		super.display();
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, 
				new Color(20,20,20)); //Color.black);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.FOG_DENSITY, .14);
//		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.FOG_ENABLED, true);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.USE_OLD_TRANSPARENCY, true);
		SceneGraphPath toworld = new SceneGraphPath(viewer.getSceneRoot(), theWorld);
		SelectLineTool slt = new SelectLineTool(toworld);
		slt.addLineSelectionListener(this);
		slt.setSphereRadius(2*tubeRadius);
		theWorld.addChild(congruenceRegulus.getRegulus());
		congruenceRegulus.getRegulus().setVisible(showCongReg);
		congruenceRegulus.getRegulus().getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", new Color(255,255,0));
		lineComplexRegulus = congruenceRegulus.getLeitScharFactory();
		theWorld.addChild(lineComplexRegulus.getRegulus());
		lineComplexRegulus.getRegulus().setVisible(showComplexReg);
		lineComplexRegulus.getRegulus().getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", new Color(0,128,0));
		ToolManager.toolManagerForViewer(viewer).addUserTool(slt, null, "select line");
		CameraUtility.encompass(viewer);
		CameraUtility.getCamera(viewer).setNear(.05);
		
		
	}

	public void selectionChanged(LineSelectionEvent ev) {
		boolean pointDirty = false;
		if (ev.getType() == PickResult.PICK_TYPE_LINE)	{
			selectedLine = ev.getSelectLineTool().getLine();
			selectedLineFactory = new PointRangeFactory();
			selectedLineFactory.setFiniteSphere(true);
			selectedLineFactory.setSphereRadius(10E2);
			selectedLineFactory.setNumberOfSamples(2);
			selectedLineFactory.setPluckerLine(selectedLine);
			selectedLineFactory.update();				
			selectedLineSGC.setGeometry(selectedLineFactory.getLine());
			System.err.println("line selection changed: "+Rn.toString(selectedLine));
			congruenceRegulus.setElement0(line0);
			congruenceRegulus.setElement1(selectedLine);
			congruenceRegulus.setElement2(line1);
			congruenceRegulus.setNumberOfSamples(50);
			congruenceRegulus.update();		
			pointDirty = true;
		}
		if (pointDirty || ev.getType() == PickResult.PICK_TYPE_POINT) {
			selectedPoint = ev.getSelectLineTool().getPoint();
			if (selectedPoint == null) return;
			updateNullPencil(selectedLine, selectedPoint);
//				nullPencils.add(nullPencilFactory);
		}
		
	}

	private void updateNullPencil(double[] g, double[] P) {
		if (g==null || P==null) return;
		Pn.dehomogenize(P, P);
		// find the plane joining the selected line and selected point
		double[] pi = PlueckerLineGeometry.lineJoinPoint(null, g, P);
		// find the intersection points of the congruence generators with this plane
		double[] s0 = PlueckerLineGeometry.lineIntersectPlane(null, line0, pi);
		double[] s1 = PlueckerLineGeometry.lineIntersectPlane(null, line1, pi);
		double[] h = PlueckerLineGeometry.lineFromPoints(null, s0, s1);
		double ip = PlueckerLineGeometry.innerProduct(h, g);
		System.err.println("Plucker inner product: "+ip);
		double[] S = PlueckerLineGeometry.intersectionPoint(null, g, h);
		double[] n = PlueckerLineGeometry.lineFromPoints(null, P, S);
		redFactory.setElement0(P);
		redFactory.setElement1(S);
		redFactory.setSphereRadius(sphereRadius);
		redFactory.setFiniteSphere(true);
		redFactory.update();
		// have to find the congruence line through P
		double[] phi = PlueckerLineGeometry.lineJoinPoint(null, line0, P);
		double[] mi = PlueckerLineGeometry.lineJoinPoint(null, line1, P);
		double[] c = PlueckerLineGeometry.lineFromPlanes(null, phi, mi);
//		double[] c = congruenceFactory.getLineThroughPoint(P);
		double[] gi = PlueckerLineGeometry.lineJoinPoint(null, c, S);
		whiteFactory.setPluckerLine(c);
		whiteFactory.setSphereRadius(sphereRadius);
		whiteFactory.setFiniteSphere(true);
		whiteFactory.update();
		redComplexLine.setGeometry(redFactory.getLine());
		whiteComplexLine.setGeometry(whiteFactory.getLine());
//				nullPencilFactory = new LinePencilFactory();
		nullPencilFactory.setPoint(P);
		nullPencilFactory.setPlane(gi);
		nullPencilFactory.setNumberJoints(30);
		nullPencilFactory.setNumLines(10);
		nullPencilFactory.setSphereRadius(.25);
		nullPencilFactory.setMetric(Pn.EUCLIDEAN);
		nullPencilFactory.setFiniteSphere(true);
		nullPencilFactory.update();
	}
	public boolean hasInspector() {return true; }
	public Component getInspector(final Viewer viewer) {	
		Box inspectionPanel =  Box.createVerticalBox();
		final TextSlider timeSlider = new TextSlider.Double("t",SwingConstants.HORIZONTAL,0.0,1.0,0.75);
		timeSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				double t = timeSlider.getValue().doubleValue();
				selectedLine = lineComplexRegulus.getValueAtTime(t);
				selectedLineFactory.setPluckerLine(selectedLine);
				selectedLineFactory.update();
				updateNullPencil(selectedLine, selectedPoint);
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(timeSlider);
		
//		JMenu actions = new JMenu("Actions");
		Box vbox = Box.createVerticalBox();
		inspectionPanel.add(vbox);
		vbox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder(BorderFactory
						.createEtchedBorder(), "Display options")));

		JCheckBox jmi = new JCheckBox("Display congruence");
		jmi.setSelected(showCongruence);
		jmi.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showCongruence = ((JCheckBox) e.getSource()).isSelected();
				congruence.setVisible(showCongruence);
				viewer.renderAsync();
			}
		});
		vbox.add(jmi);
		
		jmi = new JCheckBox("Display congruence regulus");
		jmi.setSelected(showCongReg);
		jmi.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showCongReg = ((JCheckBox) e.getSource()).isSelected();
				congruenceRegulus.getRegulus().setVisible(showCongReg);
				viewer.renderAsync();
			}
		});
		vbox.add(jmi);
		
		jmi = new JCheckBox("Display line complex regulus");
		jmi.setSelected(showComplexReg);
		jmi.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showComplexReg = ((JCheckBox) e.getSource()).isSelected();
				lineComplexRegulus.getRegulus().setVisible(showComplexReg);
				viewer.renderAsync();
			}
		});
		vbox.add(jmi);
		
		return inspectionPanel;
	}
	public SceneGraphComponent makeLights()	{
		SceneGraphComponent lightNode = new SceneGraphComponent();
		lightNode.setName("lights");
		SceneGraphComponent l0 = SceneGraphUtility.createFullSceneGraphComponent("light0");
		DirectionalLight dl = new DirectionalLight();
		dl.setColor(new Color(250, 250, 225));
		dl.setIntensity(.5);
		double[] zaxis = {0,0,1};
		double[] other = {0,1,1};
		l0.getTransformation().setMatrix( P3.makeRotationMatrix(null, zaxis, other));

		l0.setLight(dl);
		lightNode.addChild(l0);
				
		dl = new DirectionalLight();
		dl.setColor(new Color(250, 225, 250));
		dl.setIntensity(.5);
		l0 = SceneGraphUtility.createFullSceneGraphComponent("light1");
		double[] other2 = {-.6,-.2,.5};
		l0.getTransformation().setMatrix( P3.makeRotationMatrix(null, zaxis, other2));
		l0.setLight(dl);
		lightNode.addChild(l0);
		
		
		return lightNode;
	}
	
	public static void main(String[] args) {
		new LineCongruenceDemo().display();
	}
}
