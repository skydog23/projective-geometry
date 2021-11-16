/*
 * Created on Mar 22, 2021
 *
 */
package charlesgunn.jreality.worlds.projective;

import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import charlesgunn.jreality.geometry.projective.LinePencilFactory;
import charlesgunn.jreality.geometry.projective.LineUtility;
import charlesgunn.jreality.geometry.projective.PlanePencilFactoryOld;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.math.p5.PlueckerLineGeometry;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

public class PointLine2D extends AbstractDualGeometry {

	{
		dim = 2;
//		tt3 = new double[]{1.9,.1};
	}
	SceneGraphComponent 
	    elements1D,
			pointRange,
			linePencil,
		tricycles,
			trilateral,
			triangle;
	
	double[][] basis = {{0,0,0,1},{1,0,0,0},{0,1,0,0},{0,0,1,0}};
	boolean showPoint = true;
	SceneGraphComponent point = SceneGraphUtility.createFullSceneGraphComponent("point");
	
	@Override
	public SceneGraphComponent getContent() {
		super.getContent();
		pointRange = SceneGraphUtility.createFullSceneGraphComponent("point range");
		linePencil = SceneGraphUtility.createFullSceneGraphComponent("line pencil");
		trilateral = SceneGraphUtility.createFullSceneGraphComponent("trilateral");
		triangle = SceneGraphUtility.createFullSceneGraphComponent("triangle");
		Appearance ap;
		
		PointRangeFactory prf = new PointRangeFactory();
		prf.setElement0(basis[0]);
		prf.setElement1(basis[2]);
		prf.setFiniteSphere(false);
		prf.update();
		IndexedLineSet ils = prf.getLine();
		ap = pointRange.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.black);
		ap.setAttribute("lineShader.tubeRadius", .005);
		pointRange.setGeometry(ils);

		
		MatrixBuilder.euclidean().rotateY(-Math.PI/6).rotateX(-Math.PI/2).assignTo(worldSpear);
		MatrixBuilder.euclidean().translate(0, 0, -4).rotateX(Math.PI/2).scale(1,.02,1).assignTo(world);
		world.getAppearance().setAttribute("showLabels", false);
		world.getAppearance().setAttribute("lightingEnabled", false);
		
//		elements1D.addChildren(pointRange, linePencil);
//		tricycles.addChildren(trilateral, triangle);
//		world.addChildren(elements1D, tricycles);
		return world;
	}


	@Override
	protected void constructPencil(SceneGraphComponent sgc) {
		super.constructPencil(sgc);
		double[] line0 = PlueckerLineGeometry.lineFromPoints(null,basis[0],basis[1]),
				line1 = PlueckerLineGeometry.lineFromPoints(null, basis[0], basis[3]);
		LinePencilFactory lpf = LinePencilFactory.linePencilFactoryForIntersectingLines(
				null, line0, line1);
		lpf.setNumLines(nEls);
		lpf.setFiniteSphere(false);
		lpf.update();
		sgc.addChild(lpf.getPencil());
		
		constructPoint(point); 
		sgc.addChild(point);

	}

	
	@Override
	protected void constructPoint(SceneGraphComponent sgc) {
		sgc.setGeometry(Primitives.point(new double[]{0,0,0,1})); //, "P"));
		Appearance ap = sgc.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute("pointShader.polygonShader.diffuseColor", Color.black);
		ap.setAttribute("pointShader.pointRadius", .03);
		ap.setAttribute("showLabels", true);
		ap.setAttribute("pointShader.textShader."+CommonAttributes.TEXT_SCALE, .005);
		ap.setAttribute("pointShader.textShader.offset", offset);
		ap.setAttribute("pointShader.textShader.alignment",SwingConstants.EAST);
		MatrixBuilder.euclidean().translate(0, 0.2, 0).assignTo(sgc);
	}

//	@Override
//	protected void constructAbstractAxisOrientation(SceneGraphComponent sgc) {
//		SceneGraphComponent rot1 = new SceneGraphComponent();
//		rot1.addChild(circularArrow3);
//		MatrixBuilder.euclidean().scale(1.5).assignTo(rot1);
//		sgc.addChildren(rot1);
//	}


	@Override
	protected void constructAxisOrientationLabels(SceneGraphComponent sgc) {
		super.constructAxisOrientationLabels(sgc);
	}

	@Override
	protected void constructAxisOrientation(SceneGraphComponent sgc) {
//		super.constructAxisOrientation(sgc);
		Appearance ap;
		sgc.addChildren(circularArrow1, circularArrow2);
		ap = sgc.getAppearance();
		// ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		ap.setAttribute("polygonShader.diffuseColor", Color.white);
		ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.gray);
		ap.setAttribute("pointShader.polygonShader.diffuseColor", Color.gray);
		double[] line0 = PlueckerLineGeometry.lineFromPoints(null,basis[0],basis[1]),
				line1 = PlueckerLineGeometry.lineFromPoints(null, basis[0], basis[3]);
		double[][] twoLines = new double[2][];
		for (int i = 0; i<tt.length; ++i)	{
			twoLines[i] = LineUtility.valueAtTime(tt[i], line0, line1);
		}
		LinePencilFactory lpf = LinePencilFactory.linePencilFactoryForIntersectingLines(
				null, line0, line1);
		lpf.setNumLines(2);
		lpf.setPluckerLines(twoLines);
		lpf.setFiniteSphere(false);
		lpf.update();
		SceneGraphComponent pp2 = SceneGraphUtility.createFullSceneGraphComponent("pp2");
		ap = pp2.getAppearance();
		ap.setAttribute(VERTEX_DRAW, false);
		ap.setAttribute(EDGE_DRAW, true);
		ap.setAttribute(TUBES_DRAW, true);
		ap.setAttribute("lineShader.lineWidth", 2.0);
		ap.setAttribute("lineShader.tubeRadius", .01);
		pp2.addChild(lpf.getPencil());
		MatrixBuilder.euclidean().translate(0,0.02,0).scale(1.25).assignTo(pp2);
		sgc.addChild(pp2);
	}

	@Override
	public Component getInspector() {
		super.getInspector();
		Box hbox = Box.createHorizontalBox();
		inspector.add(hbox);
		JCheckBox lB = new JCheckBox("show point");
		lB.setSelected(showPoint);
		lB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showPoint = ((JCheckBox)arg0.getSource()).isSelected();
				point.setVisible(showPoint);
			}
		});
		hbox.add(lB);
		return inspector;
	}

	public static void main(String[] args) {
		new PointLine2D().display();
	}

}
