/*
 * Created on Jun 2, 2010
 *
 */
package charlesgunn.jreality.worlds.projective;

import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
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

import charlesgunn.jreality.geometry.projective.PlanePencilFactoryOld;
import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class StrahlUndAchse extends AbstractDualGeometry {

	double[] offset = new double[]{.05,0,0.1};
	SceneGraphComponent line = SceneGraphUtility.createFullSceneGraphComponent("line");
	SceneGraphComponent strahl = SceneGraphUtility.createFullSceneGraphComponent("strahl");
	SceneGraphComponent achse = SceneGraphUtility.createFullSceneGraphComponent("achse");
	SceneGraphComponent strahlOrientation = SceneGraphUtility.createFullSceneGraphComponent("spear orientation");
	SceneGraphComponent achseOrientation = SceneGraphUtility.createFullSceneGraphComponent("axis arrow");
	SceneGraphComponent achseAbstractOrientation = SceneGraphUtility.createFullSceneGraphComponent("achseAO");
	SceneGraphComponent strahlAbstractOrientation = SceneGraphUtility.createFullSceneGraphComponent("strahlAO");
	SceneGraphComponent strahlArrow = SceneGraphUtility.createFullSceneGraphComponent("spear arrow");
	SceneGraphComponent circularArrow1, circularArrow2, circularArrow3;
	@Override
	public SceneGraphComponent getContent() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		
		constructLine(line);
		world.addChild(line);

		constructPointRange(strahl);
		world.addChild(strahl);

		Appearance ap;
		PlanePencilFactoryOld ppf;
		constructPlanePencil(achse);
		world.addChild(achse);

		DefaultGeometryShader dgs;
		DefaultTextShader pts1;
		constructStraightArrowWLabels(strahlOrientation);
		strahl.addChild(strahlOrientation);

		constructAbstractStraightOrientation(strahlAbstractOrientation);
		line.addChild(strahlAbstractOrientation);

		constructCircularArrows();
		
		double[][] axisPoints = new double[][] { axisCurve[0], axisCurve[n - 1] };
		constructAxisOrientation(achseOrientation);
		achse.addChildren(achseOrientation);

		constructAbstractAxisOrientation(achseAbstractOrientation);
		line.addChild(achseAbstractOrientation);

		ppf = new PlanePencilFactoryOld();
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
		achseOrientation.addChild(pp2);

		SceneGraphComponent axisLabels = SceneGraphUtility.createFullSceneGraphComponent("axis labels");
		IndexedLineSetFactory twoPoints = IndexedLineSetUtility.createCurveFactoryFromPoints(axisPoints, false);
		twoPoints.setVertexLabels(new String[] { "a", "b" });
		twoPoints.update();
		axisLabels.setGeometry(twoPoints.getGeometry());
		ap = axisLabels.getAppearance();
		ap.setAttribute("showLabels", true);
		dgs = ShaderUtility.createDefaultGeometryShader(ap, false);
		dgs.setShowLines(false);
		dgs.setShowPoints(true);
		pts1 = (DefaultTextShader) ((DefaultPointShader) dgs.getPointShader()).getTextShader();
		pts1.setScale(.006);
		pts1.setOffset(offset);
		pts1.setAlignment(SwingConstants.EAST);
		MatrixBuilder.euclidean().translate(0, 1.0, 0).scale(.75).assignTo(axisLabels);
		pp2.addChild(axisLabels);

		MatrixBuilder.euclidean().translate(0, 0, -4).assignTo(world);
		world.getAppearance().setAttribute("showLabels", false);
		return world;
	}


	private void constructPlanePencil(SceneGraphComponent sgc) {
		super.constructPencil(sgc);
		PlanePencilFactoryOld ppf = new PlanePencilFactoryOld();
		ppf.setNumberOfSamples(10);
		ppf.setElement0(verts[0]);
		ppf.setElement1(verts[1]);
		ppf.update();
		sgc.addChild(ppf.getPlanePencil());
	}

	@Override
	public Component getInspector() {
		Box hbox = Box.createHorizontalBox();
		inspector.add(hbox);
		JCheckBox sB = new JCheckBox("show spear");
		sB.setSelected(showSpear);
		sB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showSpear = ((JCheckBox)arg0.getSource()).isSelected();
				strahl.setVisible(showSpear);
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
			}
		});
		hbox.add(aB);

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
				achseAbstractOrientation.setVisible(showAbstractOrientation);
				strahlAbstractOrientation.setVisible(showAbstractOrientation);
			}
		});
		hbox.add(aoB);

		return inspector;
	}

	@Override
	public void display() {
		super.display();
//		SceneGraphUtility.removeLights(viewer);
		SceneGraphComponent camSGC = CameraUtility.getCameraNode(viewer);
		SceneGraphComponent l1sgc = new SceneGraphComponent();
		SceneGraphComponent l2sgc =new SceneGraphComponent();
		SceneGraphComponent l3sgc =new SceneGraphComponent();
		camSGC.addChildren(l1sgc, l2sgc);
		DirectionalLight dl = new DirectionalLight();
		dl.setIntensity(.75);
		dl.setColor(new Color(225, 225, 255));
		MatrixBuilder.euclidean().rotateY(.1-Math.PI/2).assignTo(l1sgc);
		l1sgc.setLight(dl);
		DirectionalLight d2 = new DirectionalLight();
		d2.setIntensity(.75);
		d2.setColor(new Color(255,225,225));
		MatrixBuilder.euclidean().rotateY(Math.PI/4).assignTo(l2sgc);
		l2sgc.setLight(d2);
		DirectionalLight d3 = new DirectionalLight();
		d2.setIntensity(.75);
		d2.setColor(new Color(225,255,225));
		MatrixBuilder.euclidean().rotateX(-Math.PI/2).assignTo(l3sgc);
		l3sgc.setLight(d3);
	}

	public static void main(String[] args) {
		new StrahlUndAchse().display();
	}
}
