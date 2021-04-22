/*
 * Created on Jan 29, 2004
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import charlesgunn.jreality.geometry.projective.LineCongruenceFactory;
import charlesgunn.jreality.geometry.projective.LinePencilFactory;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.geometry.projective.RegulusFactory;
import charlesgunn.jreality.newtools.SelectLineTool;
import charlesgunn.jreality.newtools.SelectLineTool.LineSelectionEvent;
import charlesgunn.jreality.tools.ToolManager;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.math.p5.P5;
import charlesgunn.math.p5.PlueckerLineGeometry;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.GeometryUtility;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.PickResult;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class LineComplexDemo extends LoadableScene implements SelectLineTool.LineSelectionListener {
	SceneGraphComponent nullPencil, 
		redComplexLine, 
		whiteComplexLine, 
		theWorld,
		congruence,
		selectedLineSGC,
		conjugatePairSGC,
		randomLineSGC,
		randomLineConjSGC;
	double sphereRadius = 10.0;
	int numberPencils = 12;
	double tubeRadius = .006;
	Color axisColor = new Color(50, 50, 255);
	// these two lines are the generators of the congruence 
	// determining the line complex pencil
	double[] line0 = {0,0,0,0,1,0},			// vertical line
				line1 = 
				PlueckerLineGeometry.lineFromPoints(null, 
//						new double[]{1,0,0,0}, new double[]{0,0,1,0}); //{0,1,0,0,0,0};	// P3.lineFromPoints(null, new double[]{2,0,0,1}, new double[]{2,1,0,1}); 
						new double[]{3,0,0,1}, new double[]{3,-1,1,1}); //{0,1,0,0,0,0};	// P3.lineFromPoints(null, new double[]{2,0,0,1}, new double[]{2,1,0,1}); 
	
	double[] selectedPoint = {1,0,1,1};
	// the following line determines the line complex
	double[] complexLine = PlueckerLineGeometry.lineFromPoints(null, 
			new double[]{1,0,0,1}, new double[]{1, 1,1,1});
	// this line is NOT in the line complex, so is not its own conjugate
	double[] randomLine = PlueckerLineGeometry.lineFromPoints(null, 
			new double[]{.5,0,0,1}, new double[]{.4, .6, .8,1});
	double[] randomLineConjugate;
	LinePencilFactory nullPencilFactory = new LinePencilFactory();
	RegulusFactory congruenceRegulus = RegulusFactory.getRegulusFactory(),
		lineComplexPencilRegulus = RegulusFactory.getRegulusFactory();
	LineCongruenceFactory congruenceFactory;
	PointRangeFactory selectedLineFactory = new PointRangeFactory(),
		redFactory = new PointRangeFactory(),
		whiteFactory = new PointRangeFactory(),
		randomLineFactory = new PointRangeFactory(),
		randomLineConjFactory = new PointRangeFactory();
	
	public SceneGraphComponent makeWorld()	{
		theWorld = SceneGraphUtility.createFullSceneGraphComponent("world");
//		theWorld.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", Color.white);
		theWorld.getAppearance().setAttribute("lineShader.polygonShader.ambientColor", Color.white);
		theWorld.getAppearance().setAttribute("lineShader.polygonShader.ambientCoefficient", .02);
		theWorld.getAppearance().setAttribute("lineShader.diffuseColor", Color.white);
		theWorld.getAppearance().setAttribute("lineShader.tubeRadius", tubeRadius);
		theWorld.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, true);
		redComplexLine = SceneGraphUtility.createFullSceneGraphComponent("red line");
		redComplexLine.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", new Color(255, 0, 0));
		redComplexLine.setPickable( false);
		theWorld.addChild(redComplexLine);
		whiteComplexLine = SceneGraphUtility.createFullSceneGraphComponent("white line");
		whiteComplexLine.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", Color.white);
		whiteComplexLine.setPickable( false);
		theWorld.addChild(whiteComplexLine);

		// initialize factories for lines
		redFactory.setSphereRadius(sphereRadius);
		redFactory.setFiniteSphere(true);
		whiteFactory.setSphereRadius(sphereRadius);
		whiteFactory.setFiniteSphere(true);

		// initialize the null pencil
		nullPencilFactory.setNumberJoints(30);
		nullPencilFactory.setNumLines(10);
		nullPencilFactory.setSphereRadius(.25);
		nullPencilFactory.setMetric(Pn.EUCLIDEAN);
		nullPencilFactory.setFiniteSphere(true);

		nullPencil = nullPencilFactory.getPencil();
		nullPencil.getAppearance().setAttribute("lineShader.diffuseColor", new Color(255, 0, 0));
		nullPencil.getAppearance().setAttribute("lineShader.tubeRadius", .5*tubeRadius);
		nullPencil.setPickable(false);
		theWorld.addChild(nullPencil);
		
		conjugatePairSGC = SceneGraphUtility.createFullSceneGraphComponent("conj pair");
		randomLineSGC = SceneGraphUtility.createFullSceneGraphComponent("randomLine");
		randomLineSGC.getAppearance().setAttribute("lineShader.diffuseColor", new Color(0, 255, 255));
		randomLineSGC.getAppearance().setAttribute("lineShader.tubeRadius", tubeRadius);
		randomLineConjSGC = SceneGraphUtility.createFullSceneGraphComponent("randomLine conjugate");
		randomLineConjSGC.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", new Color(255, 0, 255));
		randomLineConjSGC.getAppearance().setAttribute("lineShader.tubeRadius", tubeRadius);
		conjugatePairSGC.addChildren(randomLineSGC, randomLineConjSGC);
		randomLineFactory.setSphereRadius(sphereRadius);
		randomLineFactory.setFiniteSphere(true);
		randomLineConjFactory.setSphereRadius(sphereRadius);
		randomLineConjFactory.setFiniteSphere(true);
		theWorld.addChild(conjugatePairSGC);
		
		SceneGraphComponent bounder = SceneGraphUtility.createFullSceneGraphComponent("sphere");
		theWorld.addChild(bounder);
//		bounder.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, 
//				new Color(1f, 1f,1f,0f));
		bounder.setPickable( false);
		Geometry foo = new Geometry("false geometry") { 
		};
		double[][] bnds = {{-1,-1,-1},{1,1,1}};
		foo.setGeometryAttributes(GeometryUtility.BOUNDING_BOX, new Rectangle3D(bnds));
		bounder.setGeometry(foo);
		congruenceRegulus.setNumberOfSamples(50);

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
		congruenceFactory.setMetric(Pn.EUCLIDEAN);
		congruenceFactory.update();
		SceneGraphComponent sgc = congruenceFactory.getSceneGraphComponent();
		congruence.addChild(sgc);	
		for (int i = 0; i<2; ++i)	{
			PointRangeFactory lf = new PointRangeFactory();
			lf.setNumberOfSamples(numberPencils);
			lf.setFiniteSphere(finiteSphere);
			lf.setSphereRadius(sphereRadius);
			lf.setPluckerLine(i == 0 ? line0 : line1);
			lf.update();
			IndexedLineSet axis = lf.getLine();
			axis.setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
			SceneGraphComponent axissgc = SceneGraphUtility.createFullSceneGraphComponent("axis"+i);
			axissgc.setGeometry(axis);
			ap = axissgc.getAppearance();
			ap.setAttribute("lineShader.diffuseColor", axisColor);
			ap.setAttribute("lineShader.polygonShader.ambientColor", Color.white);
			ap.setAttribute("lineShader.polygonShader.ambientCoefficient", .2);
			ap.setAttribute("lineShader.diffuseColor", axisColor);
			ap.setAttribute("lineShader.tubeRadius", 2*tubeRadius);
			ap.setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, true);
			theWorld.addChild(axissgc);			
		}
		
		selectedLineSGC = SceneGraphUtility.createFullSceneGraphComponent();
		selectedLineSGC.setOwner(this);
		selectedLineSGC.setVisible(true);
		selectedLineSGC.setPickable( false);
		theWorld.addChild(selectedLineSGC);
		ap = selectedLineSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", Color.green);
		ap.setAttribute("lineShader.tubeRadius", tubeRadius);

		theWorld.addChild(congruenceRegulus.getRegulus());
		congruenceRegulus.getRegulus().setVisible(false);
		congruenceRegulus.getRegulus().getAppearance().setAttribute("lineShader.diffuseColor", new Color(255,255,0));
		lineComplexPencilRegulus = congruenceRegulus.getLeitScharFactory();
		theWorld.addChild(lineComplexPencilRegulus.getRegulus());
		lineComplexPencilRegulus.getRegulus().setVisible(false);
		lineComplexPencilRegulus.getRegulus().getAppearance().setAttribute("lineShader.diffuseColor", new Color(0,128,0));
		
		return theWorld;
	}

	public boolean isEncompass() {
			return false;
		}


	public void customize(JMenuBar menuBar, final Viewer viewer) {
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, 
				new Color(20,20,20)); //Color.black);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.FOG_DENSITY, .14);
//		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.FOG_ENABLED, true);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.USE_OLD_TRANSPARENCY, true);
		SceneGraphPath toworld = new SceneGraphPath(viewer.getSceneRoot(),theWorld);
		SelectLineTool slt = new SelectLineTool(toworld);
		slt.addLineSelectionListener(this);
		slt.setSphereRadius(2*tubeRadius);
		ToolManager.toolManagerForViewer(viewer).addUserTool(slt, null, "select line");

		CameraUtility.encompass(viewer);
		CameraUtility.getCamera(viewer).setNear(.05);
		updateSelectedLine();
		updateCongruenceRegulus();
		updateRandomLine();
		updateNullPencil();
		
		JMenu actions = new JMenu("Actions");
		JMenuItem jmi = new JMenuItem("Toggle congruence");
		jmi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0));
		jmi.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				congruence.setVisible(!congruence.isVisible());
				viewer.renderAsync();
			}
		});
		actions.add(jmi);
		
		jmi = new JMenuItem("Toggle congruence regulus");
		jmi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0));
		jmi.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				congruenceRegulus.getRegulus().setVisible(!congruenceRegulus.getRegulus().isVisible());
				viewer.renderAsync();
			}
		});
		actions.add(jmi);
		
		jmi = new JMenuItem("Toggle pencil regulus");
		jmi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0));
		jmi.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				lineComplexPencilRegulus.getRegulus().setVisible(!lineComplexPencilRegulus.getRegulus().isVisible());
				viewer.renderAsync();
			}
		});
		actions.add(jmi);
		
		menuBar.add(actions);
		
		
	}

	public void selectionChanged(LineSelectionEvent ev) {
		boolean pointDirty = false;
		if (ev.getType() == PickResult.PICK_TYPE_LINE)	{
			complexLine = ev.getSelectLineTool().getLine();
			updateSelectedLine();		
			pointDirty = true;
		}
		if (pointDirty || ev.getType() == PickResult.PICK_TYPE_POINT) {
			selectedPoint = ev.getSelectLineTool().getPoint();
			if (selectedPoint == null) return;
			updateNullPencil();
//				nullPencils.add(nullPencilFactory);
		}
		
	}

	double[] complexCoords = new double[6];
	void updateSelectedLine() {
		selectedLineFactory = new PointRangeFactory();
		selectedLineFactory.setFiniteSphere(true);
		selectedLineFactory.setSphereRadius(10E2);
		selectedLineFactory.setNumberOfSamples(2);
		selectedLineFactory.setPluckerLine(complexLine);
		selectedLineFactory.update();				
		selectedLineSGC.setGeometry(selectedLineFactory.getLine());
		// calculate the plucker coords of the line complex
		double alpha = P5.innerProduct(line0, complexLine, P5.LINE_SPACE);
		double beta = P5.innerProduct(line1, complexLine, P5.LINE_SPACE);
		Rn.linearCombination(complexCoords, -beta, line0, alpha, line1);
		System.err.println("line complex = "+Rn.toString(complexCoords));
//		System.err.println("line selection changed: "+Rn.toString(selectedLine));
	}

	// only call this when the selected line has been moved off the regulus:
	// since it changes the parametrization of the leitschar dramatically
	void updateCongruenceRegulus()	{
		congruenceRegulus.setElement0(line0);
		congruenceRegulus.setElement1(complexLine);
		congruenceRegulus.setElement2(line1);
		congruenceRegulus.update();		
	}
	
	void updateRandomLine()	{
		randomLineFactory.setPluckerLine(randomLine);
		randomLineFactory.update();
		double[] randomLineConjCoords = PlueckerLineGeometry.conjugateWithRespectToComplex(null, complexCoords, randomLine);
		randomLineConjFactory.setPluckerLine(randomLineConjCoords);
		randomLineConjFactory.update();
		randomLineSGC.setGeometry(randomLineFactory.getLine());
		randomLineConjSGC.setGeometry(randomLineConjFactory.getLine());
		double ff = (complexCoords[0] - randomLine[0])/randomLineConjCoords[0];
		System.err.println("line A = "+Rn.toString(randomLine));
		System.err.println("line B = "+Rn.toString(
				Rn.times(null, ff, randomLineConjCoords)));
	}
	
	void updateNullPencil() {
		double[] g = complexLine;
		double[] P = selectedPoint;
		if (g==null || P==null) return;
		Pn.dehomogenize(P, P);
		// find the plane joining the selected line and selected point
		double[] pi = PlueckerLineGeometry.lineJoinPoint(null, g, P);
		// find the intersection points of the congruence generators with this plane
		double[] s0 = PlueckerLineGeometry.lineIntersectPlane(null, line0, pi);
		double[] s1 = PlueckerLineGeometry.lineIntersectPlane(null, line1, pi);
		double[] h = PlueckerLineGeometry.lineFromPoints(null, s0, s1);
		// S is the point where the selected line passes from front to back of regulus
		// when the eye is at P
		double[] S = PlueckerLineGeometry.intersectionPoint(null, g, h);
		redFactory.setElement0(P);
		redFactory.setElement1(S);
		redFactory.update();
		// also need the congruence line through P
		double[] phi = PlueckerLineGeometry.lineJoinPoint(null, line0, P);
		double[] mi = PlueckerLineGeometry.lineJoinPoint(null, line1, P);
		double[] c = PlueckerLineGeometry.lineFromPlanes(null, phi, mi);
		whiteFactory.setPluckerLine(c);
		whiteFactory.update();
		redComplexLine.setGeometry(redFactory.getLine());
		whiteComplexLine.setGeometry(whiteFactory.getLine());
		// gi is the plane of the null pencil at P
		double[] gi = PlueckerLineGeometry.lineJoinPoint(null, c, S);
		nullPencilFactory.setPoint(P);
		nullPencilFactory.setPlane(gi);
		nullPencilFactory.update();
	}
	public boolean hasInspector() {return true; }
	public Component getInspector(final Viewer viewer) {	
		Box inspectionPanel =  Box.createVerticalBox();
		final TextSlider timeSlider = new TextSlider.Double("t",SwingConstants.HORIZONTAL,0.0,1.0,0.75);
		timeSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				double t = timeSlider.getValue().doubleValue();
				complexLine = lineComplexPencilRegulus.getValueAtTime(t);
				selectedLineFactory.setPluckerLine(complexLine);
				selectedLineFactory.update();
//				System.err.println(t+" line = "+Rn.toString(selectedLine));
				updateSelectedLine();
				updateRandomLine();
				updateNullPencil();
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(timeSlider);
		inspectionPanel.setName("parameters");
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
}
