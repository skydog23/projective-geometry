/*
 * Created on May 11, 2021
 *
 */
package charlesgunn.math;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.newtools.FlyTool;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.p5.PlueckerLineGeometry;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.GeometryUtility;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class TestEllipticTubes extends Assignment {

	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	SceneGraphComponent axisSGC = SceneGraphUtility.createFullSceneGraphComponent("world");
	private double angle = .05;
	int num = 16;
	double[] axis = PlueckerLineGeometry.lineFromPoints(null, P3.originP3, new double[]{0,1,0,0});
	
	@Override
	public SceneGraphComponent getContent() {

//		for (int i = 0; i<num; ++i)	{
//			SceneGraphComponent child = new SceneGraphComponent("child"+i);
//			world.addChild(child);
//		}
		updateGeometry();
		
		PointRangeFactory prf = new PointRangeFactory();
		prf.setPluckerLine(axis);
		prf.setFiniteSphere(false);
		prf.update();
		axisSGC.setGeometry(prf.getLine());
		axisSGC.getAppearance().setAttribute("lineShader.diffuseColor", Color.red	);	
		world.addChild(axisSGC);
		Appearance ap = world.getAppearance();
		ap.setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.unitCube);
		ap.setAttribute("metric", Pn.ELLIPTIC);
		ap.setAttribute("lineShader.diffuseColor", Color.white);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute("lineShader."+CommonAttributes.LINE_WIDTH, 2.5);
		
		return world;
	}

	protected void updateGeometry() {
//		double[][] cps = PlueckerLineGeometry.cliffordTorus(null, axis, angle, num);
//		for (int i = 0; i<cps.length; ++i)	{
//			PointRangeFactory prf = new PointRangeFactory();
//			prf.setPluckerLine(cps[i]);
//			prf.setFiniteSphere(false);
//			prf.update();
//			SceneGraphComponent child =  world.getChildComponent(i);
//			child.setGeometry(prf.getLine());
//		}
		IndexedFaceSet eds = PlueckerLineGeometry.equidistantSurface(axis, angle, num);
		world.setGeometry(eds);
	}
	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		Appearance ap = viewer.getSceneRoot().getAppearance();
		ap.setAttribute(CommonAttributes.RENDER_S3, true);
		ap.setAttribute(CommonAttributes.METRIC, Pn.ELLIPTIC);
		ap.setAttribute(CommonAttributes.USE_GLSL, true);
		Camera cam = CameraUtility.getCamera(viewer);
		cam.setNear(.02);
		cam.setFar(-.05);
		FlyTool flytool = new FlyTool();
		flytool.setGain(.1);
		CameraUtility.getCameraNode(viewer).addTool(flytool);
	}

	@Override
	public Component getInspector() {
		Box hbox = Box.createHorizontalBox();
		inspector.add(hbox);
		
		final TextSlider aSlider = new TextSlider.Double("radius",  SwingConstants.HORIZONTAL,
				-Math.PI/2, Math.PI/2, angle);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				angle = aSlider.getValue().doubleValue();
				updateGeometry();
			}
		});
		inspector.add(aSlider);
		
		return inspector;
	}

	public static void main(String[] args) {
		new TestEllipticTubes().display();
	}
}
