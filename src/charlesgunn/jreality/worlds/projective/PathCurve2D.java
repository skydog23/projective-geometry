/*
 * Created on Dec 1, 2010
 *
 */
package charlesgunn.jreality.worlds.projective;

import static de.jreality.geometry.GeometryUtility.BOUNDING_BOX;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.LIGHTING_ENABLED;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import android.view.animation.Transformation;
import charlesgunn.anim.util.AnimationUtility.InterpolationTypes;
import charlesgunn.jreality.GeometryCollector;
import charlesgunn.jreality.geometry.projective.LinePencilFactory;
import charlesgunn.jreality.geometry.projective.LineUtility;
import charlesgunn.jreality.geometry.projective.PointCollector;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import charlesgunn.math.Biquaternion;
import charlesgunn.math.p5.PlueckerLineGeometry;
import charlesgunn.util.TextSlider;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class PathCurve2D extends Assignment {

	private double[] tform = {1,.7,0,1, -1,.5,0,1, 0,0,1,0,  .15,-.5,0,1};
//	private double[] tform = {0,.5,0,1, 0,-.5,0,1, 0,0,1,0,  1,0,0,0};
	private SceneGraphComponent world,
		cubicCurveSGC,		
		coordSysSGC;
	int numPoints = 500;
	PointCollector pc = new PointCollector(numPoints, 4);
	Color[] colors1 = {Color.red, Color.yellow, Color.blue, Color.green, Color.magenta, Color.cyan};
	Color[] colors2 = {Color.green, Color.magenta, Color.cyan, Color.red, Color.yellow, Color.blue};
	@Override
	public SceneGraphComponent getContent() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		cubicCurveSGC = SceneGraphUtility.createFullSceneGraphComponent("euc");
		coordSysSGC = SceneGraphUtility.createFullSceneGraphComponent("polar");
		Appearance ap = cubicCurveSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", new Color(200, 0,50));
		ap.setAttribute(VERTEX_DRAW, false);
		ap = world.getAppearance();
		ap.setAttribute("lineShader.lineWidth", 1.0);
		ap.setAttribute(LIGHTING_ENABLED, false);
		ap.setAttribute(VERTEX_DRAW, false);
		// tubes are broken due to the fact we're doing a lot at the line at infinity
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(BOUNDING_BOX, Rectangle3D.unitCube);
		ap.setAttribute("lineShader.diffuseColor", new Color(0, 50,50));
		world.addChildren(cubicCurveSGC, coordSysSGC);
		Matrix mm = new Matrix(tform);
		mm.transpose();
		mm.assignTo(world.getTransformation());;
		
		generateGeometry();
		return world;
	}
	double[][] basis = {{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}};
	
	private void generateGeometry() {
		IndexedLineSet ils = PointRangeFactory.line( basis[0], basis[1]);
		SceneGraphComponent xySGC = SceneGraphUtility.createFullSceneGraphComponent("xy");
		xySGC.setGeometry(ils);
		ils = PointRangeFactory.line( basis[1], basis[3]);
		SceneGraphComponent ywSGC = SceneGraphUtility.createFullSceneGraphComponent("yw");
		ywSGC.setGeometry(ils);
		ils = PointRangeFactory.line( basis[3], basis[0]);
		SceneGraphComponent wxSGC = SceneGraphUtility.createFullSceneGraphComponent("wx");
		wxSGC.setGeometry(ils);
		// add some  graph paper
		LinePencilFactory lpfx = new LinePencilFactory(),
				lpfy = new LinePencilFactory(); 
		int lineCount = 60;
		double[][] plines = new double[lineCount][];
		double xscale = 1.4, yscale = 1.27, xinit[] = {-.02,.02}, yinit[] = {-.02,.02};
		
		for (int i = 0; i<lineCount; ++i)	{
			double tmp = i %(lineCount/2); //(i-lineCount)/2.0;
			int which = (2*i)/lineCount;
			double yval = xinit[which] * Math.pow(xscale, tmp);
			plines[i] = PlueckerLineGeometry.lineFromPoints(null, basis[0],
					new double[]{0, yval, 0, 1});
		}
		lpfx.setPoint(basis[0]);
		lpfx.setPlane(basis[2]);
		lpfx.setNumberJoints(12);
		lpfx.setPluckerLines(plines);
		lpfx.setFan(true);
		lpfx.setFiniteSphere(false);
		lpfx.setSphereRadius(50);
		lpfx.setNumLines(lineCount);
		lpfx.update();
		SceneGraphComponent xyLinesSGC = SceneGraphUtility.createFullSceneGraphComponent("xyLines");
		xyLinesSGC.addChild(lpfx.getPencil());
		for (int i = 0; i<lineCount; ++i)	{
			double tmp = i % (lineCount/2);
			int which = (2*i)/lineCount;
			double xval = yinit[which] * Math.pow(yscale, tmp);
			plines[i] = PlueckerLineGeometry.lineFromPoints(null, basis[1],
					new double[]{xval, 0, 0, 1});
		}
		lpfy.setPoint(basis[1]);
		lpfy.setPlane(basis[2]);
		lpfy.setPluckerLines(plines);
		lpfy.setNumberJoints(12);
		lpfy.setFan(true);
		lpfy.setFiniteSphere(false);
		lpfy.setSphereRadius(50);
		lpfy.setNumLines(lineCount);
		lpfy.update();
		SceneGraphComponent yLinesSGC = SceneGraphUtility.createFullSceneGraphComponent("yLines");
		yLinesSGC.addChild(lpfy.getPencil());
		xyLinesSGC.getAppearance().setAttribute("lineShader.diffuseColor", new Color(220, 220, 220));
		xyLinesSGC.getAppearance().setAttribute("lineShader.lineWidth",.75);
		yLinesSGC.setAppearance(xyLinesSGC.getAppearance());
		coordSysSGC.addChildren(xySGC, ywSGC, wxSGC, xyLinesSGC, yLinesSGC);
		MatrixBuilder.euclidean().translate(0, 0, -.01).assignTo(xyLinesSGC);
		
//		for (int i = 0; i<numPoints; ++i)	{
//			double angle = (2* Math.PI *i)/ (numPoints),
//					c = Math.cos(angle),
//					s = Math.sin(angle);
//			double[] curvepoint = {c*s*s, c*c*c, 0, s*s*s};
//			pc.addPoint(curvepoint);
//		}
//		cubicCurveSGC.setGeometry(pc.getCurve());
//		MatrixBuilder.euclidean().translate(0, 0, .01).assignTo(cubicCurveSGC);
	}
	@Override
	public void display() {
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.white);		
		Camera cam = CameraUtility.getCamera(jrviewer.getViewer());
		cam.setFieldOfView(1.25 * cam.getFieldOfView());
		cam.setPerspective(false);
	}

	

//	@Override
//	public Component getInspector(Viewer v) {
//		Box inspectionPanel =  Box.createVerticalBox();
//		final TextSlider nSlider = new TextSlider.Integer("num",SwingConstants.HORIZONTAL, 1, 200, num);
//		nSlider.addActionListener(new ActionListener()	{
//			public void actionPerformed(ActionEvent e)	{
//				num = nSlider.getValue().intValue();
//				updateConics();
//			}
//		});
//		inspectionPanel.add(nSlider);
//		final TextSlider aSlider = new TextSlider.Double("a",SwingConstants.HORIZONTAL, -1.0, 1.0, a);
//		aSlider.addActionListener(new ActionListener()	{
//			public void actionPerformed(ActionEvent e)	{
//				a = aSlider.getValue().doubleValue();
//				updateConics();
//			}
//		});
//		inspectionPanel.add(aSlider);
//		final TextSlider bSlider = new TextSlider.Double("b",SwingConstants.HORIZONTAL, -1.0, 1.0, b);
//		bSlider.addActionListener(new ActionListener()	{
//			public void actionPerformed(ActionEvent e)	{
//				b = bSlider.getValue().doubleValue();
//				updateConics();
//			}
//		});
//		inspectionPanel.add(bSlider);
//		final TextSlider cSlider = new TextSlider.Double("t",SwingConstants.HORIZONTAL, -1.0, 1.0, c);
//		cSlider.addActionListener(new ActionListener()	{
//			public void actionPerformed(ActionEvent e)	{
//				c = cSlider.getValue().doubleValue();
//				updateConics();
//			}
//		});
//		inspectionPanel.add(cSlider);
//		return inspectionPanel;
//	}

	public static void main(String[] args) {
		new PathCurve2D().display();
	}
}
