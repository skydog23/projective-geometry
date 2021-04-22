/*
 * Created on 25.01.2018
 *
 */
package charlesgunn.jreality.worlds.iscador;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.geometry.projective.PointCollector;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.Utility;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class ProjectiveConicsDemo extends Assignment {

	double param = 0;
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	double[][] verts;
	int num = 1000;
	IndexedLineSet circle = IndexedLineSetUtility.circle(num);
	PointCollector pc = new PointCollector(3*num, 4);
	@Override
	public SceneGraphComponent getContent() {
		SceneGraphComponent w1 = SceneGraphUtility.createFullSceneGraphComponent("world1");
		SceneGraphComponent w2 = SceneGraphUtility.createFullSceneGraphComponent("world2");
		world.addChildren(w1, w2);
		verts = circle.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		update();

		w1.setGeometry(pc.getCurve());
		w2.setGeometry(pc.getCurve());
//		w2.addChild(w1);
		double[] m = Rn.identityMatrix(4);
		Rn.times(m, -1, m);
		w2.getTransformation().setMatrix(m);
		Appearance ap = w1.getAppearance();
		w2.setAppearance(ap);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.SPHERES_DRAW, false);
//		ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.white);
//		ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.white);
//		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.white);
//		
//		ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, Color.white);
		Color pathcolor = Color.red;
		ap.setAttribute("pointShader.diffuseColor", pathcolor);
		ap.setAttribute("lineShader.diffuseColor", pathcolor);
		ap.setAttribute("lineShader.lineWidth", 2.5);
		ap.setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute("polygonShader.diffuseColor", pathcolor);
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
//		System.err.println("ap = "+ap.getAttributes());
		return world;
	}

	
	@Override
	public void display() {
	// TODO Auto-generated method stub
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(0,0,0,0));
//		CameraUtility.getCamera(jrviewer.getViewer()).setFieldOfView(9.3);
		animationPlugin.getAnimationPanel().setResourceDir("src/charlesgunn/jreality/worlds/iscador/");

	}


	@Override
	public void setValueAtTime(double d) {
		param = AnimationUtility.linearInterpolation(d, 0, 1, .6, 2.0);
		update();
	}


	public Component getInspector() {
		Box inspectionPanel =  inspector;
		final TextSlider nSlider = new TextSlider.Double("param",SwingConstants.HORIZONTAL, 0, 2, param);
		nSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				param = nSlider.getValue().doubleValue();
				update();
			}
		});
		inspectionPanel.add(nSlider);
		return inspector;
	}
	protected void update() {
		double realP = (param < 1) ? param : (1.0/(2.0001-param));
		double[] m = Rn.identityMatrix(4);
		m[13] = -realP;
		double[][] tverts = Rn.matrixTimesVector(null, m, verts);
		Utility.dehomogenizePreserveWSign( tverts);
		pc.reset();
		for (int i = 0; i<tverts.length; ++i) {
			pc.addPoint(tverts[i]);
		}
		pc.addPoint(tverts[0]);
		pc.addPoint(tverts[1]);
//		System.err.println("tverts=\n"+Rn.toString(tverts));
//		circle.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY_ARRAY.createReadOnly(tverts));
//		world.getTransformation().setMatrix(m);
	}

	public static void main(String[] args) {
		new ProjectiveConicsDemo().display();
	}

}
