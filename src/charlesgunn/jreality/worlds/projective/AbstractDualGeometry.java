/*
 * Created on Mar 22, 2021
 *
 */
package charlesgunn.jreality.worlds.projective;

import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.geometry.projective.PlanePencilFactory;
import charlesgunn.jreality.geometry.projective.PlanePencilFactoryOld;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.p5.PlueckerLineGeometry;
import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.PolygonalTubeFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.SceneGraphUtility;

public abstract class AbstractDualGeometry extends Assignment {

	protected boolean showSpear = true;
	protected boolean showLine = true;
	protected boolean showAxis = true;
	protected boolean showOrientation = true;
	protected boolean showAbstractOrientation = true;
	protected double stickRadius = .02;
	protected double arrowScale = .1;
	protected double arrowSlope = 1.5;
	protected double arrowPosition = .83;
	protected Color ballC = new Color(80,80,80);
	protected Color arrowC = new Color(60,60,255);
	protected Color stickC = arrowC;
	protected int n = 50;
	double globalScale = 2.25;
	protected double[][] axisCurve = new double[n][];
	protected double[][] verts = {
			{0,1,0,1}, 
			{0,-1,0,1}, 
			{.5,-1,0,1}, 
			{.5,1,0,1}
			};
	protected double extent = 5;
	double[] tt = new double[] { .3, .6 };
	double[] tt2 = new double[] { 1.3, .6 };
	double[] tt3 = new double[] { 0, 1 };
	double[] offset = new double[]{.05,0,0.02};
	int dim = -1;
	SceneGraphComponent line = SceneGraphUtility.createFullSceneGraphComponent("line");
	SceneGraphComponent strahl = SceneGraphUtility.createFullSceneGraphComponent("strahl");
	SceneGraphComponent achse = SceneGraphUtility.createFullSceneGraphComponent("achse");
	SceneGraphComponent strahlOrientation = SceneGraphUtility.createFullSceneGraphComponent("spear orientation");
	SceneGraphComponent achseOrientation = SceneGraphUtility.createFullSceneGraphComponent("axis arrow");
	SceneGraphComponent axisLabels = SceneGraphUtility.createFullSceneGraphComponent("axis labels");
	SceneGraphComponent achseAbstractOrientation = SceneGraphUtility.createFullSceneGraphComponent("achseAO");
	SceneGraphComponent strahlAbstractOrientation = SceneGraphUtility.createFullSceneGraphComponent("strahlAO");
	SceneGraphComponent strahlArrow = SceneGraphUtility.createFullSceneGraphComponent("spear arrow");
	SceneGraphComponent circularArrow1, circularArrow2, circularArrow3;
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	SceneGraphComponent worldSpear = SceneGraphUtility.createFullSceneGraphComponent("world spear");
	SceneGraphComponent worldAxis = SceneGraphUtility.createFullSceneGraphComponent("world axis");
	protected int nEls = 10;

	public AbstractDualGeometry() {
		super();
	}

	
	@Override
	public SceneGraphComponent getContent() {
		
		constructLine(line);
		worldSpear.addChild(line);

		constructPointRange(strahl);
		worldSpear.addChild(strahl);

		constructPencil(achse);
		worldAxis.addChild(achse);

		constructStraightArrowWLabels(strahlOrientation);
		strahl.addChild(strahlOrientation);
		if (dim == 2) 
			MatrixBuilder.euclidean().translate(0,0,.2).assignTo(strahlOrientation);

		constructAbstractStraightOrientation(strahlAbstractOrientation);
		worldSpear.addChild(strahlAbstractOrientation);

		constructCircularArrows();
		
		constructAxisOrientation(achseOrientation);
		achse.addChildren(achseOrientation);
		constructAxisOrientationLabels(axisLabels);
		achseOrientation.getChildComponent(2).addChild(axisLabels);

		constructAbstractAxisOrientation(achseAbstractOrientation);
		worldAxis.addChild(achseAbstractOrientation);
		
		world.addChildren(worldAxis, worldSpear);
		return world;
	}


	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", new Color(200,255,200));
	}


	protected SceneGraphComponent constructCircularArrow(double[] tt) {
	
		SceneGraphComponent circularArrow = SceneGraphUtility.createFullSceneGraphComponent("circ arrow");
		Appearance ap = circularArrow.getAppearance();
		ap.setAttribute("polygonShader.diffuseColor", arrowC);
		ap.setAttribute("lineShader.diffuseColor", ballC);
		ap.setAttribute(VERTEX_DRAW, false);
		ap.setAttribute(EDGE_DRAW, false);
		circularArrow.setGeometry(
				makeCircularArrow(tt));
		MatrixBuilder.euclidean().translate(0,1.125,0).scale(.65).assignTo(circularArrow);
	
		return circularArrow;
	}

	protected Geometry makeCircularArrow(double[] tt) {
		double[] rad = new double[n];
		for (int i = 0; i<n; ++i)	{
			double t = (i)/(n-1.0);
			double[] foo = getCurvePoint(tt, i, n);
			axisCurve[i] = foo;
			double arrowFactor = t < arrowPosition ? 0.0 : 
				AnimationUtility.linearInterpolation(
					t, arrowPosition, 1.0, stickRadius*2.0, 0.0);
			rad[i] = globalScale * (t < arrowPosition ? stickRadius : arrowFactor);
		}
		rad[0] = 0.0;
		PolygonalTubeFactory ptf = new PolygonalTubeFactory(axisCurve);
		ptf.setRadii(rad);
		ptf.update();
		return ptf.getTube();
	}


	private double[] getCurvePoint(double[] tt, int i, int n) {
		double t = (i)/(n-1.0);
		double angle = Math.PI * AnimationUtility.linearInterpolation(tt[0], tt[1], t);
		double[] foo = new double[]{Math.cos(angle), 0, Math.sin(angle),1};
		return foo;
	}


	protected void constructLine(SceneGraphComponent sgc) {
		PointRangeFactory prf = new PointRangeFactory();
		prf.setElement0(verts[0]);
		prf.setElement1(verts[1]);
		prf.setFiniteSphere(false);
		prf.update();
		IndexedLineSet ils = prf.getLine();
		Appearance ap = sgc.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.black);
		ap.setAttribute("lineShader.tubeRadius", .005);
		sgc.setGeometry(ils);
	}

	protected void constructPoint(SceneGraphComponent sgc) {
	}


	protected void constructPointRange(SceneGraphComponent sgc) {
		Appearance ap = sgc.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute("pointShader.polygonShader.diffuseColor", Color.red);
		ap.setAttribute("pointShader.pointRadius", .02);
		int numSpheres = 250;
		double[][] pts = new double[numSpheres][3];
		for (int i = 0; i < numSpheres; ++i) {
			pts[i][1] = AnimationUtility.linearInterpolation(i / (numSpheres - 1.0), 0, 1, -extent, extent);
			pts[i][0] = pts[i][2] = 0;
		}
		PointSetFactory psf = new PointSetFactory();
		psf.setVertexCount(numSpheres);
		psf.setVertexCoordinates(pts);
		psf.update();
		sgc.setGeometry(psf.getGeometry());
	}
	
	protected void constructPencil(SceneGraphComponent sgc)	{
		Appearance ap = sgc.getAppearance();
		// ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		ap.setAttribute("polygonShader.diffuseColor", Color.white);
		ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.red);
		ap.setAttribute("pointShader.polygonShader.diffuseColor", Color.red);
		ap.setAttribute("pointShader.pointRadius", .005);
		ap.setAttribute("lineShader.tubeRadius", .005);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);

	}

	protected void constructCircularArrows() {
		//  construct 3 circular arrows
		circularArrow1 = constructCircularArrow(tt);
		arrowPosition = .85;
		circularArrow2 = constructCircularArrow(tt2);
		arrowPosition = .75;
		stickRadius = .1;
		circularArrow3 = constructCircularArrow(tt3);
		MatrixBuilder.euclidean().translate(0, 1.14, 0).scale(.2).assignTo(circularArrow3);
	}

	protected void constructAxisOrientation(SceneGraphComponent sgc) {
		Appearance ap;
		sgc.addChildren(circularArrow1, circularArrow2);
		ap = sgc.getAppearance();
		// ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		ap.setAttribute("polygonShader.diffuseColor", Color.white);
		ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.gray);
		ap.setAttribute("pointShader.polygonShader.diffuseColor", Color.gray);
		PlanePencilFactoryOld ppf = new PlanePencilFactoryOld();
		ppf.setNumberOfSamples(2);
		ppf.setElement0(verts[0]);
		ppf.setElement1(verts[1]);
		ppf.setTimes(tt);
		ppf.update();
		SceneGraphComponent pp = ppf.getPlanePencil();
		SceneGraphComponent pp2 = SceneGraphUtility.createFullSceneGraphComponent("pp2");
		ap = pp2.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(TRANSPARENCY_ENABLED, true);
		ap.setAttribute("polygonShader." + TRANSPARENCY_ENABLED, true);
		ap.setAttribute(TRANSPARENCY, .3);
		ap.setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, true);
		ap.setAttribute("lineShader.tubeRadius", .01);
		ap.setAttribute("pointShader.pointRadius", .01);
		pp2.addChild(pp);
		MatrixBuilder.euclidean().scale(1.25).assignTo(pp2);
		sgc.addChild(pp2);
	}
	
	protected void constructAxisOrientationLabels(SceneGraphComponent sgc) {
		double[][] axisPoints = new double[][] { getCurvePoint(tt, 0, n), getCurvePoint(tt, n-1, n) };

		IndexedLineSetFactory twoPoints = IndexedLineSetUtility.createCurveFactoryFromPoints(axisPoints, false);
		twoPoints.setVertexLabels(new String[] { "a", "b" });
		twoPoints.update();
		sgc.setGeometry(twoPoints.getGeometry());
		Appearance ap = sgc.getAppearance();
		ap.setAttribute("showLabels", true);
		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, false);
		dgs.setShowLines(false);
		dgs.setShowPoints(true);
		DefaultTextShader pts1 = (DefaultTextShader) ((DefaultPointShader) dgs.getPointShader()).getTextShader();
		pts1.setScale(.006);
		pts1.setOffset(offset);
		pts1.setAlignment(SwingConstants.EAST);
		MatrixBuilder.euclidean().translate(0, 1.0, 0).scale(.75).assignTo(sgc);
	}

	protected void constructAbstractAxisOrientation(SceneGraphComponent sgc) {
		SceneGraphComponent rot1 = new SceneGraphComponent(), 
				rot2 = new SceneGraphComponent();
		rot1.addChild(circularArrow3);
		rot2.addChild(circularArrow3);
		MatrixBuilder.euclidean().rotateY(Math.PI).assignTo(rot2);
		sgc.addChildren(rot1, rot2);
	}

	protected void constructAbstractStraightOrientation(SceneGraphComponent sgc) {
		Appearance ap;
		strahlAbstractOrientation.setGeometry(Primitives.cone(50, 1.0, true));
		ap = sgc.getAppearance();
		ap.setAttribute("polygonShader.diffuseColor", arrowC);
		ap.setAttribute(VERTEX_DRAW, false);
		ap.setAttribute(EDGE_DRAW, false);
		MatrixBuilder.euclidean().scale(.1, .3, .1).rotateX(-Math.PI / 2).assignTo(sgc);
	}

	protected void constructStraightArrowWLabels(SceneGraphComponent sgc) {
		double a = -.5, b = .5;
		double[][] segment = { { 0, a, 0, 1 }, { 0, b, 0, 1 } };
		IndexedLineSetFactory arrow1 = IndexedLineSetUtility.createCurveFactoryFromPoints(segment, false);
		BallAndStickFactory basf = new BallAndStickFactory(arrow1.getIndexedLineSet());
		basf.setBallRadius(.04);
		basf.setBallColor(ballC);
		basf.setStickRadius(stickRadius);
		basf.setStickColor(stickC);
		basf.setShowArrows(true);
		basf.setArrowScale(arrowScale);
		basf.setArrowSlope(arrowSlope);
		basf.setArrowPosition(arrowPosition);
		basf.setArrowColor(arrowC);
		basf.update();
		strahlArrow.addChild(basf.getSceneGraphComponent());
		Appearance ap = strahlArrow.getAppearance();
		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);

		SceneGraphComponent labels = SceneGraphUtility.createFullSceneGraphComponent("spear labels");
		ap = labels.getAppearance();
		if (dim == 2) {
//			ap.setAttribute("pointShader.polygonShader.pointRadius",0.0001);
			ap.setAttribute("pointShader.pointRadius",0.0);
		}
		ap.setAttribute("showLabels", true);
		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, false);
		dgs.setShowLines(false);
		dgs.setShowPoints(true);
		DefaultTextShader pts1 = (DefaultTextShader) ((DefaultPointShader) dgs.getPointShader()).getTextShader();
		pts1.setScale(.005);
		pts1.setOffset(offset);
		pts1.setAlignment(SwingConstants.EAST);
		labels.setGeometry(arrow1.getIndexedLineSet());
		arrow1.setVertexLabels(new String[] { "A", "B" });
		arrow1.update();
		sgc.addChildren(strahlArrow, labels);
	}
	
	@Override
	public Component getInspector() {
		Box hbox = Box.createHorizontalBox();
		inspector.add(hbox);
		JCheckBox lB = new JCheckBox("show line");
		lB.setSelected(showLine);
		lB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showLine = ((JCheckBox)arg0.getSource()).isSelected();
				line.setVisible(showLine);
			}
		});
		hbox.add(lB);

		JCheckBox sB = new JCheckBox("show spear");
		sB.setSelected(showSpear);
		sB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showSpear = ((JCheckBox)arg0.getSource()).isSelected();
				strahl.setVisible(showSpear);
				strahlAbstractOrientation.setVisible(showSpear && showAbstractOrientation);
			}
		});
		hbox.add(sB);

		JCheckBox aB = new JCheckBox("show axis");
		aB.setSelected(showAxis);
		aB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showAxis = ((JCheckBox)arg0.getSource()).isSelected();
				achse.setVisible(showAxis);
				achseAbstractOrientation.setVisible(showAxis && showAbstractOrientation);
			}
		});
		hbox.add(aB);

		hbox = Box.createHorizontalBox();
		inspector.add(hbox);

		JCheckBox oB = new JCheckBox("show orientation");
		oB.setSelected(showOrientation);
		oB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showOrientation = ((JCheckBox)arg0.getSource()).isSelected();
				achseOrientation.setVisible(showOrientation);
				strahlOrientation.setVisible(showOrientation);
			}
		});
		hbox.add(oB);
		JCheckBox aoB = new JCheckBox("show abstract orientation");
		aoB.setSelected(showOrientation);
		aoB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showAbstractOrientation = ((JCheckBox)arg0.getSource()).isSelected();
				achseAbstractOrientation.setVisible(showAxis && showAbstractOrientation);
				strahlAbstractOrientation.setVisible(showSpear && showAbstractOrientation);
			}
		});
		hbox.add(aoB);

		return inspector;
	}


}