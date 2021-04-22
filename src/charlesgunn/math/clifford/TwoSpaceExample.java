package charlesgunn.math.clifford;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P2;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.util.TextSlider;

public class TwoSpaceExample extends Assignment {

	// start with two points, two lines, and their intersection
	double[] P0 = {-.5, -.2,1}, P1 = {.4,.3,1}, Q  = {0, 1.3,1}; 	
	int metric = Pn.EUCLIDEAN;
	double time = 0;
	
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world"), 
			lines, points,
			P0sgc, P0nolabelssgc, P1sgc, P01sgc, 
			m0sgc, m1sgc, 
			arrow0sgc, arrow1sgc, 
			Msgc, 
			pointBissgc, 
			lineBissgc, 
			Amsgc, 
			centersgc, 
			cp0sgc, 
			unitCirclesgc, 
			animsgc, 
			lotsgc, 
			interpsgc,
			m0Labelsgc, 
			m1Labelsgc, 
			pointBisLabelsgc, 
			lineBisLabelsgc, 
			P0Csgc;
	PointRangeFactory m0fac, m1fac, pointBisfac, lineBisfac, cp0fac, p01fac;
	private MultivectorP2 Mv, Pv0, Pv1, Midpoint, lineBis, pointBis, cp0, Center, mv0, mv1;
	TwoSpace ts = new TwoSpace(metric);
	private MultivectorP2 rotor;
	boolean flipm0 = true, flipm1 = false, showInterpolatedIsometry = false;
	int numSteps = 20;
	
	@Override
	public SceneGraphComponent getContent() {
		init();

		initSceneGraph();
		
		update();
		MatrixBuilder.euclidean().translate(0, -.5, -12).assignTo(world);
		return world;
	}


	protected void init() {
		double[] m0 = P2.lineFromPoints(null, Q, P0), // m0 and m1 meet at Q
				m1 = P2.lineFromPoints(null, Q, P1); // m0 and m1 meet at Q
		Q = P2.pointFromLines(null, m0, m1);		// just to be safe
		Mv = MultivectorP2.point(Q);
		Pv0 = MultivectorP2.point(P0);
		Pv1 = MultivectorP2.point(P1);
		mv0 = MultivectorP2.line(m0);
		mv1 = MultivectorP2.line(m1);
	}

	protected void initSceneGraph() {
		animsgc = SceneGraphUtility.createFullSceneGraphComponent("world"); 
		interpsgc = SceneGraphUtility.createFullSceneGraphComponent("interp"); 
		lotsgc = SceneGraphUtility.createFullSceneGraphComponent("lot"); 
		lines = SceneGraphUtility.createFullSceneGraphComponent("lines"); 
		points = SceneGraphUtility.createFullSceneGraphComponent("points"); 
		P0sgc = SceneGraphUtility.createFullSceneGraphComponent("A"); 
		P0nolabelssgc = SceneGraphUtility.createFullSceneGraphComponent("A"); 
		P1sgc = SceneGraphUtility.createFullSceneGraphComponent("A'"); 
		Amsgc = SceneGraphUtility.createFullSceneGraphComponent("Am"); 
		P01sgc = SceneGraphUtility.createFullSceneGraphComponent("a"); 
		Msgc = SceneGraphUtility.createFullSceneGraphComponent("M"); 
		m0sgc  = SceneGraphUtility.createFullSceneGraphComponent("m"); 
		m1sgc = SceneGraphUtility.createFullSceneGraphComponent("m'");
		arrow0sgc  = SceneGraphUtility.createFullSceneGraphComponent("arrow"); 
		arrow1sgc = SceneGraphUtility.createFullSceneGraphComponent("arrow'");
		m0sgc.addChild(arrow0sgc);
		m1sgc.addChild(arrow1sgc);
		P0sgc.addTool(dragToolForPoint(Pv0));
		P1sgc.addTool(dragToolForPoint(Pv1));
		m0sgc.addTool(dragToolForPoint(mv0));
		m1sgc.addTool(dragToolForPoint(mv1));
		Msgc.addTool(dragToolForPoint(Mv));
		pointBissgc = SceneGraphUtility.createFullSceneGraphComponent("pointBis"); 
		pointBissgc.getAppearance().setAttribute("lineShader.diffuseColor", Color.cyan);
		lineBissgc = SceneGraphUtility.createFullSceneGraphComponent("world"); 
		lineBissgc.getAppearance().setAttribute("lineShader.diffuseColor", Color.green);
		centersgc = SceneGraphUtility.createFullSceneGraphComponent("center"); 
		cp0sgc = SceneGraphUtility.createFullSceneGraphComponent("world");	
		cp0sgc.getAppearance().setAttribute("lineShader.diffuseColor", Color.red);
		unitCirclesgc = SceneGraphUtility.createFullSceneGraphComponent("unit circle"); 
		m0fac = new PointRangeFactory(); 
		m0fac.setNumberOfSamples(2);
		m0fac.update(); 
		m0sgc.setGeometry(m0fac.getLine());
		m0sgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		m1fac = new PointRangeFactory();  
		m1fac.setNumberOfSamples(2);
		m1fac.update(); m1sgc.setGeometry(m1fac.getLine());
		p01fac = new PointRangeFactory(); 
		p01fac.update(); 
		P01sgc.setGeometry(p01fac.getLine());
		pointBisfac = new PointRangeFactory(); 
		pointBisfac.update(); 
		pointBissgc.setGeometry(pointBisfac.getLine());
		lineBisfac = new PointRangeFactory(); 
		lineBisfac.update(); 
		lineBissgc.setGeometry(lineBisfac.getLine());
		cp0fac = new PointRangeFactory(); 
		cp0fac.update(); 
		cp0sgc.setGeometry(cp0fac.getLine());
		lines.addChildren(m0sgc, m1sgc, pointBissgc, lineBissgc, cp0sgc, P01sgc, animsgc);
		points.addChildren(P0sgc, P1sgc,  Amsgc, Msgc, centersgc);
		for (int i = 0; i<20; ++i)	{
			SceneGraphComponent sgc = new SceneGraphComponent("child"+i);
			interpsgc.addChild(sgc);
			sgc.addChildren(m0sgc, lotsgc, P0nolabelssgc);
		}
		interpsgc.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		interpsgc.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, .5);
		interpsgc.setPickable(false);
		world.addChildren(lines, points, unitCirclesgc, interpsgc);
		unitCirclesgc.setGeometry(IndexedLineSetUtility.circle(100));
		unitCirclesgc.getAppearance().setAttribute("lineShader.diffuseColor", Color.white);
		unitCirclesgc.getAppearance().setAttribute("lineShader.tubeRadius", .01);
		unitCirclesgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		lines.getAppearance().setAttribute("lineShader.tubeRadius", .01);
		lines.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		lines.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		points.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		world.getAppearance().setAttribute("pointShader.diffuseColor", Color.white);
		world.getAppearance().setAttribute("pointShader.pointRadius", .02);
//		world.getAppearance().setAttribute(CommonAttributes.TEXT_COLOR, Color.black);
		world.getAppearance().setAttribute(CommonAttributes.TEXT_SCALE, .002);
		world.getAppearance().setAttribute(CommonAttributes.TEXT_OFFSET, new double[]{.01,.01,.01});
		world.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, false);
		world.getAppearance().setAttribute("lineShader.lineWidth", 3.0);
		world.getAppearance().setAttribute("lineShader.diffuseColor", new Color(0,120, 255));
		world.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		world.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		
		animsgc.addChildren(P0sgc, m0sgc, lotsgc);
		animsgc.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		animsgc.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		animsgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		animsgc.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW, false);
		lotsgc.getAppearance().setAttribute("lineShader.diffuseColor", Color.white);
		lotsgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
	}

	protected void update() {
		ts.normalize(Mv, Mv);
		ts.normalize(Pv0, Pv0);
		ts.normalize(Pv1, Pv1);
		MultivectorP2.join(mv0, Mv, Pv0);
		MultivectorP2.join(mv1, Mv, Pv1);
		ts.normalize(mv0, mv0);
		ts.normalize(mv1, mv1);
		if (flipm0)
			MultivectorP2.times(mv0, (double) -1, mv0);
		if (flipm1)
			MultivectorP2.times(mv1, (double) -1, mv1);
		m0sgc.getAppearance().setAttribute("lineShader.diffuseColor", flipm0 ? Color.yellow : Color.yellow);
		m1sgc.getAppearance().setAttribute("lineShader.diffuseColor",  flipm1 ? Color.yellow : Color.yellow);
		lineBis = MultivectorP2.minus(null, mv1, mv0);
		Midpoint = MultivectorP2.plus(null, Pv0, Pv1);
		ts.normalize(Midpoint, Midpoint);
		Amsgc.setGeometry(Primitives.point(P2.imbedP2InP3(null, MultivectorP2.gradeD(Midpoint, 2)), "Am"));
		MultivectorP2 joiningline = MultivectorP2.join(null, Pv0, Pv1);
		pointBis = ts.gp(null, Midpoint, joiningline);
		Center = MultivectorP2.wedge(null, lineBis, pointBis);
		ts.normalize(Center, Center);
		if (Center.getVals()[4] < 0) Rn.times(Center.getVals(), -1, Center.getVals());
		// if the center is non-zero ...
		cp0 = MultivectorP2.join(null, (MultivectorP2.isZeroComplement(Center, null) ? Mv : Center), Pv0);
		rotor = ts.gp(null,  pointBis, cp0);
		MultivectorP2 imageP0 = ts.sandwichProduct(rotor, Pv0), 
				imagem0 = ts.sandwichProduct(rotor, mv0);
		ts.normalize(rotor, rotor);
		ts.normalize(imageP0, imageP0);
		ts.normalize(imagem0, imagem0);
//		System.err.println("center is "+Rn.toString(Center.getVals()));
//		System.err.println("real point is "+Rn.toString(Pv1.getVals()));
//		System.err.println("calculated point is "+Rn.toString(imageP0.getVals()));
//		System.err.println("real line is "+Rn.toString(mv1.getVals()));
//		System.err.println("calculated line is "+Rn.toString(imagem0.getVals()));
		m0fac.set2DLine(MultivectorP2.gradeD(mv0, 1));  m0fac.update(); 
		m1fac.set2DLine(MultivectorP2.gradeD(mv1, 1)); m1fac.update();
		pointBisfac.set2DLine(MultivectorP2.gradeD(pointBis, 1)); pointBisfac.update(); 
		lineBisfac.set2DLine(MultivectorP2.gradeD(lineBis, 1)); lineBisfac.update();
		cp0fac.set2DLine(MultivectorP2.gradeD(cp0, 1)); cp0fac.update();
		unitCirclesgc.setVisible(metric == Pn.HYPERBOLIC);
		P0sgc.setGeometry(Primitives.point(P2.imbedP2InP3(null, MultivectorP2.gradeD(Pv0, 2)), "A"));
		P0nolabelssgc.setGeometry(Primitives.point(P2.imbedP2InP3(null, MultivectorP2.gradeD(Pv0, 2))));
		P1sgc.setGeometry(Primitives.point(P2.imbedP2InP3(null, MultivectorP2.gradeD(Pv1, 2)), "A'"));
		Msgc.setGeometry(Primitives.point(P2.imbedP2InP3(null, MultivectorP2.gradeD(Mv, 2)), "M"));
		centersgc.setGeometry(Primitives.point(P2.imbedP2InP3(null, MultivectorP2.gradeD(Center, 2)), "C"));
		double[][] pts = {P2.imbedP2InP3(null,MultivectorP2.gradeD(Pv0, 2)), 
				P2.imbedP2InP3(null,MultivectorP2.gradeD(Pv1, 2))};
		p01fac.setElement0(pts[0]);
		p01fac.setElement1(pts[1]); p01fac.update();
		MultivectorP2 lot = MultivectorP2.grade(null, ts.gp(null, Center, mv0), 1);
		MultivectorP2 Closest = MultivectorP2.wedge(null, lot, mv0);
		ts.normalize(Closest, Closest);
		pts = new double[][]{P2.imbedP2InP3(null,MultivectorP2.gradeD(Center, 2)), 
				P2.imbedP2InP3(null,MultivectorP2.gradeD(Closest, 2))};
		lotsgc.setGeometry(IndexedLineSetUtility.createCurveFromPoints(pts, false));
		updateLabels();
		updateArrows();
		updateTime();
		interpsgc.setVisible(showInterpolatedIsometry);
		if (showInterpolatedIsometry)	{
			for (int i = 0; i<numSteps; ++i)	{
				SceneGraphComponent child = interpsgc.getChildComponent(i);
				double[] mat = calculateInterpolatedIsometry( (i/(numSteps-1.0)));
				new Matrix(mat).assignTo(child);
			}
		}
	}


	private void updateLabels() {
		if (m0Labelsgc == null)	{
			m0Labelsgc = SceneGraphUtility.createFullSceneGraphComponent("m0label");
			m1Labelsgc = SceneGraphUtility.createFullSceneGraphComponent("m1label");
			pointBisLabelsgc = SceneGraphUtility.createFullSceneGraphComponent("pointBisLabel");
			lineBisLabelsgc = SceneGraphUtility.createFullSceneGraphComponent("lineBisLabel");
			P0Csgc = SceneGraphUtility.createFullSceneGraphComponent("P0C");
			points.addChildren(m0Labelsgc, m1Labelsgc,pointBisLabelsgc, lineBisLabelsgc, P0Csgc);
		}
		MultivectorP2 tmp = MultivectorP2.plus(null, Mv, Pv0);
		double[] p = MultivectorP2.gradeD(tmp, 2);
		p = P2.imbedP2InP3(null, p);
		m0Labelsgc =  Primitives.labelPoint(m0Labelsgc, p, "m");

		tmp = MultivectorP2.plus(null, Mv, Pv1);
		p = MultivectorP2.gradeD(tmp, 2);
		p = P2.imbedP2InP3(null, p);
		m1Labelsgc =  Primitives.labelPoint(m1Labelsgc, p, "m'");

		tmp = MultivectorP2.plus(null, Center, Midpoint);
		p = MultivectorP2.gradeD(tmp, 2);
		p = P2.imbedP2InP3(null, p);
		pointBisLabelsgc =  Primitives.labelPoint(pointBisLabelsgc, p, "r");

		tmp = MultivectorP2.plus(null, Mv, Center);
		p = MultivectorP2.gradeD(tmp, 2);
		p = P2.imbedP2InP3(null, p);
		lineBisLabelsgc =  Primitives.labelPoint(lineBisLabelsgc, p, "c");		

		tmp = MultivectorP2.plus(null, Pv0, Center);
		p = MultivectorP2.gradeD(tmp, 2);
		p = P2.imbedP2InP3(null, p);
		P0Csgc =  Primitives.labelPoint(P0Csgc, p, "s");		
}

	double arrowScale1 = .2, arrowScale2 = .25;
	protected void updateArrows()	{
		arrow0sgc.setGeometry(arrowForLine(Mv, mv0, arrowScale1, arrowScale2));
		arrow1sgc.setGeometry(arrowForLine(Mv, mv1, arrowScale1, arrowScale2));
	}
	
	protected IndexedLineSet arrowForLine(MultivectorP2 startP, MultivectorP2 line, double size, double size2)	{
		double[] xyz = MultivectorP2.gradeD(startP, 2);
		// following is important to get correct coordinates in the non-euclidean case, i.e., convert to affine coords
		xyz = Pn.dehomogenize(null, xyz);
		double[] abc = MultivectorP2.gradeD(line, 1);
		IndexedLineSet ils = Primitives.arrow(xyz[0], xyz[1], 
				xyz[0]-size*abc[1], xyz[1]+size*abc[0], size2, false);
		double[][] verts = ils.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		verts = Pn.homogenize(null, verts);
		Pn.normalize(verts, verts, metric);
		ils.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(verts));
		return ils;
	}
	
	protected void updateTime() {
		double[] mat4 = calculateInterpolatedIsometry(time);
		animsgc.getTransformation().setMatrix(mat4);
		animsgc.setVisible(time != 0);
	}

	protected double[] calculateInterpolatedIsometry(double t) {
		MultivectorP2 log = ts.logarithmForRotor(rotor);
		MultivectorP2 halfRotor = ts.exp(null, 
				MultivectorP2.grade(null, log, 2), 
				t*MultivectorP2.scalarFrom(log));
		double[] mat = ts.matrixForRotor(halfRotor);
		double[] mat4 = P2.imbedMatrixP2InP3(null, mat);
		return mat4;
	}

	public static void main(String[] args) {
		TwoSpaceExample tse = new TwoSpaceExample();
//		tse.getContent();
		tse.display();
	}
	
	@Override
	public void display() {
		super.display();
		CameraUtility.getCamera(jrviewer.getViewer()).setFieldOfView(11.0);
	}


	public Tool dragToolForPoint(final MultivectorP2 point)	{
		
	Tool dragTool = 		new AbstractTool(InputSlot.LEFT_BUTTON, InputSlot.SHIFT_LEFT_BUTTON) {
		
		boolean active = false;		// a security flag to avoid editing in an inconsistent state
		double[][] curveVerts;
		@Override
		public void activate(ToolContext tc) {
			super.activate(tc);
			// usually it's sufficient to get the closest hit using tc.getCurrentPick()
			// but in this case, we always want to choose the closest hit on a vertex if there is such a hit;
			// otherwise we accept the hit on the closest edge
			List<PickResult> hitlist = tc.getCurrentPicks();
			if (hitlist == null || hitlist.size() == 0) return;
			PickResult pr = hitlist.get(0);
			
			if (tc.getSource() == InputSlot.LEFT_BUTTON)
				System.err.println("source is left button");
			if (tc.getSource() == InputSlot.SHIFT_LEFT_BUTTON) 
				System.err.println("source is shift-left button");
			// we have a valid hit; proceed with the algorithm
			active = true;
			// to receive events as the mouse moves need to add following input slot
			// then perform() will be called
			addCurrentSlot(InputSlot.POINTER_TRANSFORMATION);	

			if (pr.getPickType() == PickResult.PICK_TYPE_POINT) {
				perform(tc);
			} 
		}

		@Override
		public void perform(ToolContext tc) {
			if (!active) return;
			super.perform(tc);			// could be omitted since it's an empty method, but perhaps in the future ...
			PickResult pr = tc.getCurrentPick();
			// check that we have a valid pick
			if (pr == null || pr.getPickPath() == null) return;
			// just to be safe, check that it's a point pick
			if (pr.getPickType() == PickResult.PICK_TYPE_POINT) {
//				System.err.println("Picked vertex "+pr.getIndex());
				double[] newPoint = pr.getObjectCoordinates();
				newPoint[2] = 0.0;	
				double[] vals = point.getVals();
				vals[4] = newPoint[3];
				vals[5] = newPoint[0];
				vals[6] = newPoint[1];
				update();
			}
		}

		@Override
		public void deactivate(ToolContext tc) {
			super.deactivate(tc);
			// restore default states
			removeCurrentSlot(InputSlot.POINTER_TRANSFORMATION);
			active = false;
			PickResult pr = tc.getCurrentPick();
			if (pr == null || pr.getPickPath() == null) return;
			if (pr.getPickType() == PickResult.PICK_TYPE_LINE) {
				if (point == mv0) flipm0 = !flipm0;
				else if (point == mv1) flipm1 = !flipm1;
				System.err.println("Deactivating line"+flipm0);
			update();
			}
		}
		

	};
	return dragTool;
	}


	@Override
	public Component getInspector() {
		Box hbox = Box.createHorizontalBox();
		JComboBox jcb = new JComboBox(new String[]{"Euclidean", "Hyperbolic", "Elliptic"});
	    hbox.setBorder(new EmptyBorder(5,10,5,10));
	    int height = (int)(jcb.getPreferredSize().getHeight());
	    hbox.setMaximumSize(new Dimension(1000, height));   
	    jcb.setMaximumSize(new Dimension(1000, height));   
	    jcb.setAlignmentX(Component.LEFT_ALIGNMENT);
		jcb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				switch(((JComboBox)e.getSource()).getSelectedIndex()) {
				case 0:
					metric = Pn.EUCLIDEAN;
					break;
				case 1:
					metric = Pn.HYPERBOLIC;
					break;
				case 2:
					metric = Pn.ELLIPTIC;
					break;
				}
				
				ts.setMetric(metric);
				update();
			}
			
		});
		hbox.add(jcb);
		JCheckBox showIntCB = new JCheckBox("show interp");
		showIntCB.setSelected(showInterpolatedIsometry);
		showIntCB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showInterpolatedIsometry = ((JCheckBox) arg0.getSource()).isSelected();
				update();
			}
		});
		hbox.add(showIntCB);
		Box panel = Box.createVerticalBox();
		panel.add(hbox);
		TextSlider delaySlider = new TextSlider.Double("time",SwingConstants.HORIZONTAL, 0.0, 1.0, time);
		delaySlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				time =  ((TextSlider) arg0.getSource()).getValue().doubleValue();
				updateTime();
			}
		});
		delaySlider.setToolTipText("Set the time");
		panel.add(delaySlider);
		return panel;
	}


	@Override
	public String getDocumentationFile() {
		// TODO Auto-generated method stub
		return "http://dgd.service.tu-berlin.de/wordpress/vismathws12/2014/02/20/829/";
	}
	
//	@Override
//	public SceneGraphComponent getContent() {
//		m1 = P2.lineFromPoints(null, P1, new double[]{0,0,1}); // m0 and m1 meet outside
//		Pn.normalize(P0, P0, metric);
//		Pn.normalize(P1, P1, metric);
//		Pn.normalize(m1, m1, metric);
//		double[] M = P2.pointFromLines(null, m0, m1);  // should be (0,0,1)
//		Pn.normalize(M, M, metric);
//		double[] r1 = Pn.midPlane(null, m0, Rn.times(null, -1, m1), metric);
//		double[] plane = new double[]{r1[0], r1[1], 0, r1[2]};
//		double[] refl = P3.makeReflectionMatrix(null, plane, metric);
//		double[] R0 = Rn.matrixTimesVector(null, refl, P0);
//		double[] R1 = Rn.matrixTimesVector(null, refl, P1);
//		double[] cP = P2.perpendicularBisector(null, P0, P1, metric);
//		double[] cR = P2.perpendicularBisector(null, R0, R1, metric);
//		double[] C = P2.pointFromLines(null, cP, cR);
//		Pn.normalize(C, C, metric);
//		double[] cp0 = P2.lineFromPoints(null, C, P0);
//		double[] cp1 = P2.lineFromPoints(null, C, P1);
//		double angle = Pn.angleBetween(cp0, cp1, metric);
//		double[] C4 = P2.imbedP2InP3(null, C);
//		double[] C42 = C4.clone();  C42[2] = 1.0;
//		double[] rot = P3.makeRotationMatrix(null, C4, C42, -angle, metric); 
//		double[] calc = Rn.matrixTimesVector(null, rot, P2.imbedP2InP3(null, P0));
//		Pn.normalize(calc, calc, metric);
//		System.err.println("center is "+Rn.toString(C));
//		System.err.println("angle is "+angle);
//		System.err.println("real point is "+Rn.toString(P1));
//		System.err.println("calculated point is "+Rn.toString(calc));
//		return null;
//	}
//
}
