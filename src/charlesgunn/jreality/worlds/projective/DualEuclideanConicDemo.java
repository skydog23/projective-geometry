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

import charlesgunn.anim.util.AnimationUtility.InterpolationTypes;
import charlesgunn.jreality.GeometryCollector;
import charlesgunn.jreality.geometry.projective.LinePencilFactory;
import charlesgunn.jreality.geometry.projective.PointCollector;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import charlesgunn.math.Biquaternion;
import charlesgunn.util.TextSlider;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class DualEuclideanConicDemo extends Assignment {

	
	int numLines = 10;
	private SceneGraphComponent world,
		eucSGC,
			pickCurveSGC,
		polarSGC,
			pivotPointSGC;
	int numPoints = 500;
	PointCollector pc = new PointCollector(numPoints, 4), polarPivot = new PointCollector(numPoints, 4);
	GeometryCollector gc = new GeometryCollector(numPoints), polarGC = new GeometryCollector(numPoints);
	Biquaternion lastPolarBQ = null;
	double[] lastPolar = null;
	// draw an ellipse and its polar
	double[] oldPt = null, oldpolarline = null;
	double a = 1.5, b = 1.5, c = 0;
	int num = 50;
	Color[] colors1 = {Color.red, Color.yellow, Color.blue, Color.green, Color.magenta, Color.cyan};
	Color[] colors2 = {Color.green, Color.magenta, Color.cyan, Color.red, Color.yellow, Color.blue};
	double[] oldcc = null;
	// this version isn't finished: apparently I began working on transitioning from
	// point-range/line-fan to conics.
	LinePencilFactory polarBFanFact[];
	PointRangeFactory polarBLineFact[], polarDotsFact[];
	@Override
	public SceneGraphComponent getContent() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		eucSGC = SceneGraphUtility.createFullSceneGraphComponent("euc");
		polarSGC = SceneGraphUtility.createFullSceneGraphComponent("polar");
		SceneGraphUtility.createFullSceneGraphComponent("star");
		polarSGC.addChildren(polarGC);
		eucSGC.addChildren(gc);
		polarGC.setAppearance(new Appearance());
		Appearance ap = polarGC.getAppearance();
		Color linecolor = new Color(42, 74, 154);
		ap.setAttribute("lineShader.diffuseColor", linecolor); //Color(200, 0,50));
		ap.setAttribute(VERTEX_DRAW, false);
		ap = world.getAppearance();
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, new Color(255,255,255,0));
//		ap.setAttribute("lineShader.lineWidth", 1.7);
		ap.setAttribute(LIGHTING_ENABLED, false);
		gc.setAppearance(new Appearance());
		ap = gc.getAppearance();
//		ap.setAttribute(EDGE_DRAW, true);
		ap.setAttribute(VERTEX_DRAW, false);
		ap.setAttribute(BOUNDING_BOX, Rectangle3D.unitCube);
		Color pointColor = new Color(211, 15, 58);
		ap.setAttribute("lineShader.diffuseColor", pointColor);
		ap = polarSGC.getAppearance();
		ap.setAttribute(BOUNDING_BOX, Rectangle3D.unitCube);
		pickCurveSGC = SceneGraphUtility.createFullSceneGraphComponent("curve");
		ap = pickCurveSGC.getAppearance();
		ap.setAttribute(EDGE_DRAW, false);
//		ap.setAttribute(VERTEX_DRAW, true);
		ap.setAttribute("pointShader.diffuseColor", linecolor);
		pivotPointSGC = SceneGraphUtility.createFullSceneGraphComponent("pivot");
		pivotPointSGC.setGeometry(polarPivot.getCurve());
		ap = pivotPointSGC.getAppearance();
		ap.setAttribute(EDGE_DRAW, false);
//		ap.setAttribute(VERTEX_DRAW, true);
		ap.setAttribute("pointShader.diffuseColor", pointColor); //(0, 50,50));
		ap = world.getAppearance();
		ap.setAttribute(TUBES_DRAW, false);
		pickCurveSGC.setGeometry(pc.getCurve());
		world.addChildren(eucSGC, polarSGC);
		eucSGC.addChild(pickCurveSGC);
		polarSGC.addChild(pivotPointSGC);

		PointRangeFactory prf = new PointRangeFactory();
		prf.setFiniteSphere(true);
		prf.setElement0(new double[]{0,0,0,1});
		prf.setElement1(new double[]{0,1,0,1});
		prf.setNumberOfSamples(50);
		prf.setSphereRadius(2);
		prf.setCenter(P3.originP3);
		prf.update();
		SceneGraphComponent prsgc = SceneGraphUtility.createFullSceneGraphComponent("point range");
		ap = prsgc.getAppearance();
		ap.setAttribute("pointShader.diffuseColor", pointColor);
		ap.setAttribute("lineShader.diffuseColor", linecolor);
		prsgc.setGeometry(prf.getLine());
		
		polarSGC.addChild(prsgc);
		polarSGC.setVisible(false);
		// draw conic section C: a^2(x-c)^2 + b^2y^2 -z^2=0
		updateConics();
		return world;
	}
	@Override
	public void display() {
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.white);
		animationPlugin.setAnimateCamera(true);
		animationPlugin.setAnimateSceneGraph(true);
		animationPlugin.setDefaultInterp(InterpolationTypes.CUBIC_HERMITE);
		Camera cam = CameraUtility.getCamera(jrviewer.getViewer());
		cam.setFieldOfView(1.25 * cam.getFieldOfView());
		cam.setPerspective(false);
	}
	private void updateConics() {
		reset();
		for (int i = 0; i<=num; ++i)	{
			double angle = Math.PI * 2  *(i)/((double)num);
			// following point lies on the conic section above
			double[] pt = {Math.cos(angle)/a + c, Math.sin(angle)/b, 0, 1};
			pc.addPoint(pt);
//			// polar line at this point is given by gradient of C evaluated at this point
			double[] polarline = {a * a*(pt[0]-c), b*b*pt[1], 0, a*a*c*(c - pt[0]) -1};
			double[] polarline6 = { polarline[3], 0, -polarline[1], 0, -polarline[0], 0};
			PointRangeFactory prf = new PointRangeFactory();
			prf.setPluckerLine(polarline6);
			prf.setFiniteSphere(false);
			prf.update();
			gc.addGeometry(prf.getLine());
			generatePolar(pt, oldPt, polarline, oldpolarline);
			oldPt = pt;  oldpolarline = polarline;
		}
	}
	double[] fixedPlane = {0,0,1,0};  // z=0 plane

	

	private void generatePolar(double[] point, double[] lastObj, double[] tangentLine, double[] oldtangentline) {
		Pn.dehomogenize(tangentLine, tangentLine);
		polarPivot.addPoint(tangentLine);
		// convert the point into 3D line coordinates
		double[] polarLine = new double[]{ point[3], 0, -point[1], 0, -point[0], 0};
		PointRangeFactory prf = new PointRangeFactory();
		prf.setPluckerLine(polarLine);
		prf.setFiniteSphere(false);
		prf.setCenter(tangentLine);
		double d = .1;
		if (oldtangentline != null) d = Pn.distanceBetween(tangentLine, oldtangentline, Pn.EUCLIDEAN);
		prf.setSphereRadius(3.0 * d);
//						prf.setNumberOfSamples(12);
		prf.update();
		polarGC.addGeometry(prf.getLine());
	}

	@Override
	public Component getInspector() {
		Box inspectionPanel =  inspector;
		final TextSlider nSlider = new TextSlider.Integer("num",SwingConstants.HORIZONTAL, 1, 200, num);
		nSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				num = nSlider.getValue().intValue();
				updateConics();
			}
		});
		inspectionPanel.add(nSlider);
		final TextSlider aSlider = new TextSlider.Double("a",SwingConstants.HORIZONTAL, -1.0, 1.0, a);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				a = aSlider.getValue().doubleValue();
				updateConics();
			}
		});
		inspectionPanel.add(aSlider);
		final TextSlider bSlider = new TextSlider.Double("b",SwingConstants.HORIZONTAL, -1.0, 1.0, b);
		bSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				b = bSlider.getValue().doubleValue();
				updateConics();
			}
		});
		inspectionPanel.add(bSlider);
		final TextSlider cSlider = new TextSlider.Double("t",SwingConstants.HORIZONTAL, -1.0, 1.0, c);
		cSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				c = cSlider.getValue().doubleValue();
				updateConics();
			}
		});
		inspectionPanel.add(cSlider);
		return inspectionPanel;
	}

	private void reset() {
		pc.reset();
		gc.reset();
		polarPivot.reset();
		polarGC.reset();
		lastPolarBQ = null;
		lastPolar = null;
	}
	public static void main(String[] args) {
		new DualEuclideanConicDemo().display();
	}
}
