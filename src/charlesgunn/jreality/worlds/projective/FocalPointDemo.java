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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.anim.util.AnimationUtility.InterpolationTypes;
import charlesgunn.jreality.GeometryCollector;
import charlesgunn.jreality.geometry.projective.LinePencilFactory;
import charlesgunn.jreality.geometry.projective.LineUtility;
import charlesgunn.jreality.geometry.projective.PointCollector;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.Biquaternion;
import charlesgunn.math.Utility;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P2;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class FocalPointDemo extends Assignment {

	boolean showApollonian = true,
			traceOneKind = true;
	private double[][] 
	           basis = {{1,0,0}, {0,1,0}, {0,0,1}};
	private SceneGraphComponent world,
		VPencilSGC,
		vRangeSGC,
		UPencilSGC,
		conicSGC,
		axesSGC,
			xRangeSGC,
			yRangeSGC,
		circlesSGC,		// apollonian circle families determined by focal points
			oneKindSGC,
				oneKindRSGC,
			otherKindSGC,
				otherKindRSGC;
	int numPoints = 100, num = 50, numLines = 1, numCircles = 10;
	PointCollector pc = new PointCollector(numPoints, 3);
	IndexedLineSetFactory ellipse;
	GeometryCollector gc = new GeometryCollector(numPoints), polarGC = new GeometryCollector(numPoints);
	double a = 1.0, b = 1.5, A = 1.0/a, B = 1.0/b;
	double vangle = Math.PI/6,
			pencilRange = .220,
			gscale = 1.0;
	
	@Override
	public SceneGraphComponent getContent() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		vRangeSGC = SceneGraphUtility.createFullSceneGraphComponent("v");
		VPencilSGC = SceneGraphUtility.createFullSceneGraphComponent("U");
		UPencilSGC = SceneGraphUtility.createFullSceneGraphComponent("V");
		axesSGC = SceneGraphUtility.createFullSceneGraphComponent("axes");
		xRangeSGC = SceneGraphUtility.createFullSceneGraphComponent("x");
		yRangeSGC = SceneGraphUtility.createFullSceneGraphComponent("y");
		circlesSGC = SceneGraphUtility.createFullSceneGraphComponent("circles");
		oneKindSGC = SceneGraphUtility.createFullSceneGraphComponent("one kind");
		otherKindSGC = SceneGraphUtility.createFullSceneGraphComponent("other kind");
		oneKindRSGC = SceneGraphUtility.createFullSceneGraphComponent("one kind");
		otherKindRSGC = SceneGraphUtility.createFullSceneGraphComponent("other kind");
		conicSGC = SceneGraphUtility.createFullSceneGraphComponent("curve");
		
		Appearance ap = world.getAppearance();
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, new Color(255,255,255,0));
		ap.setAttribute(CommonAttributes.LINE_WIDTH, 2*gscale);
		ap.setAttribute(LIGHTING_ENABLED, false);
		ap.setAttribute(TUBES_DRAW, false);
		gc.setAppearance(new Appearance());
		ap = gc.getAppearance();
//		ap.setAttribute(EDGE_DRAW, true);
		ap.setAttribute(VERTEX_DRAW, false);
		ap.setAttribute("lineShader.diffuseColor", new Color(0, 50,50));
		ap = UPencilSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", new Color(50, 100, 200));
		ap = VPencilSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", new Color(0,200,100));
		ap = vRangeSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", new Color(100, 50, 200));
		ap = conicSGC.getAppearance();
		ap.setAttribute(VERTEX_DRAW, false);
		ap.setAttribute("lineShader.diffuseColor", new Color(160, 0, 80));
		ap = circlesSGC.getAppearance();
		ap.setAttribute(CommonAttributes.LINE_WIDTH, gscale);
		ap = oneKindSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", new Color(255,125, 120));
		ap = otherKindSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", new Color(100,200,200));
		ap = axesSGC.getAppearance();
		ap.setAttribute(VERTEX_DRAW, false);
		ap.setAttribute("lineShader.diffuseColor", new Color(0, 0, 0));
		
		double[][] fudged = {{1,0,.1}, {-.1,0,1}, {0,1,.1},{0,-.1,1}};
		xRangeSGC.setGeometry(PointRangeFactory.line(fudged[0], fudged[1]));
		yRangeSGC.setGeometry(PointRangeFactory.line(fudged[2], fudged[3]));
		MatrixBuilder.euclidean().translate(0,0,-5).assignTo(world);
		world.addChildren(UPencilSGC, vRangeSGC, VPencilSGC, conicSGC, axesSGC, circlesSGC);
		axesSGC.addChildren(xRangeSGC, yRangeSGC);
		circlesSGC.addChildren(oneKindSGC, oneKindRSGC, otherKindSGC, otherKindRSGC);
		oneKindRSGC.addChild(oneKindSGC);
		MatrixBuilder.euclidean().reflect(new double[]{1,0,0,0}).assignTo(oneKindRSGC);
		otherKindRSGC.addChild(otherKindSGC);
		MatrixBuilder.euclidean().reflect(new double[]{0,1,0,0}).assignTo(otherKindRSGC);

		init();
		updateConics();
		updatePencils();
		return world;
	}
	
	
	transient PointRangeFactory prf;
	private void init() {
		// set up pencils
		VPencilSGC.removeAllChildren();
		UPencilSGC.removeAllChildren();
		for (int i = 0; i<numLines; ++i)	{
			SceneGraphComponent child = new SceneGraphComponent("v child");
			VPencilSGC.addChild(child);
			child = new SceneGraphComponent("u child");
			UPencilSGC.addChild(child);
		}
		oneKindSGC.removeAllChildren();
		otherKindSGC.removeAllChildren();
		for (int i = 0; i<numCircles; ++i)	{
			SceneGraphComponent child = new SceneGraphComponent("one child");
			oneKindSGC.addChild(child);
			child = new SceneGraphComponent("other child");
			otherKindSGC.addChild(child);
		}
		
	}

	double[] lastP = null;
	double oldRange = 0.0, oldAngle = 0.0;
	private void updatePencils() {
		double[] V = {Math.cos(vangle), -Math.sin(vangle), 0}, // direction of pencil
				U = {Math.sin(vangle), Math.cos(vangle), 0},   // metric polar direction
				v = P2.lineFromPoints(null, basis[2], V),      // element of pencil through origin
				vPole = polarPoint(v);
		vRangeSGC.setGeometry(PointRangeFactory.line(basis[2], vPole));		
		int n = VPencilSGC.getChildComponentCount();
		for (int i = 0; i<1; ++i)	{
			// draw a line in the pencil at V
			SceneGraphComponent vchild = VPencilSGC.getChildComponent(i);
			double dist = pencilRange; //-2*(i-n/2)*pencilRange/(n);
			double[] V1;
			if (lastP != null && oldAngle != vangle && oldRange == pencilRange)	{
				V1 =  lastP;
//				System.err.println("using old point "+Rn.toString(V1));
			} else {
				V1 =  new double[]{dist*Math.sin(vangle), dist*Math.cos(vangle), 1};
				double[] Vpencilline = P2.lineFromPoints(null, V, V1);
				lastP = P2.pointFromLines(null, Vpencilline, basis[traceOneKind ? 1 : 0]); 
			}
			oldAngle = vangle;
			oldRange = pencilRange;
			vchild.setGeometry(PointRangeFactory.line(V, V1));
			// draw the conjugate line in the pencil at U
			SceneGraphComponent uchild = UPencilSGC.getChildComponent(i);
			double[] Vpencilline = P2.lineFromPoints(null, V, V1),
					polarPoint3 = polarPoint(Vpencilline);
			uchild.setGeometry(PointRangeFactory.line(U, polarPoint3));
		}
		// do the apollonian circles
		if (!showApollonian) return;
		double f = Math.sqrt(Math.abs(B*B-A*A));
		System.err.println("focal distance = "+f);
		double[] fp = {f,0,1};
		int minCount = 100;
		for (int i=0; i<numCircles; ++i)	{
			double t = (i*f)/(numCircles),
					it = f*f/t;
			double[] C = {(t+it)/2,0,1};
			double radius1 = Math.abs(t-it)/2.0;
			int nsides = (int)(radius1 > 10 ? 500 : (radius1*minCount));
			if (nsides < minCount) nsides = minCount;
			oneKindSGC.getChildComponent(i).setGeometry(
					IndexedLineSetUtility.circle(nsides, C[0], C[1], radius1));	
		}
		for (int i=0; i<numCircles; ++i)	{
			double angle = -.2 + i*(Math.PI/2)/numCircles,
					c = Math.cos(angle),
					s = Math.sin(angle);
			double[] line1 = P2.lineFromPoints(null, fp, new double[]{c,s,0}),
					p1 = P2.pointFromLines(null, line1, basis[0]),
					line2 = P2.lineFromPoints(null, fp, new double[]{-s,c,0}),
					p2 = P2.pointFromLines(null, line2, basis[0]);
			Pn.dehomogenize(p1, p1);
			Pn.dehomogenize(p2, p2);
			double r = Math.abs(p1[1] - p2[1])/2;
			double[] C = Pn.dehomogenize(null, Rn.add(null, p1, p2));
			int nsides = (int)(r > 10 ? 500 : (r*minCount));
			if (nsides < minCount) nsides = minCount;
			otherKindSGC.getChildComponent(i).setGeometry(
					IndexedLineSetUtility.circle(nsides, C[0], C[1], r));	
		}

	}
	@Override
	public void display() {
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.white);
		animationPlugin.setAnimateCamera(true);
		animationPlugin.setAnimateSceneGraph(true);
		animationPlugin.setDefaultInterp(InterpolationTypes.CUBIC_HERMITE);
		Camera cam = CameraUtility.getCamera(jrviewer.getViewer());
		cam.setFieldOfView(1.7 * cam.getFieldOfView());
		cam.setPerspective(false);
		Component comp = ((Component) jrviewer.getViewer().getViewingComponent());
		comp.addKeyListener(new KeyAdapter() {
 				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_H:
						System.err.println("	1: toggle display circles");
						System.err.println("	2: toggle family of circles to trace");
						break;
		
					case KeyEvent.VK_1:
						circlesSGC.setVisible(!circlesSGC.isVisible());
						break;
						
					case KeyEvent.VK_2:
						traceOneKind = !traceOneKind;
						lastP = null;
						break;
					}
				}
			});
	}
	
	private void updateConics() {
		A = 1/a; 
		B = 1/b;
		double[][] points = new double[numPoints][];
		for (int i = 0; i<numPoints; ++i)	{
			double angle = Math.PI * 2  *(i)/((double)num);
			// following point lies on the conic section above
			points[i] = new double[]{Math.cos(angle)/a, Math.sin(angle)/b, 1};	
		}
		ellipse = IndexedLineSetUtility.createCurveFactoryFromPoints(ellipse, points, true);
		if (conicSGC.getGeometry() == null)
			conicSGC.setGeometry(ellipse.getGeometry());
	}	

	private double[] polarLine( double[] pt) {
		double[] polarpoint = {a * a*(pt[0]), b*b*pt[1],  -pt[2]};
		P2.normalizeLine(polarpoint, polarpoint);
		return polarpoint;
	}

	private double[] polarPoint( double[] ln) {
		double[] polarline = {A*A*(ln[0]), B*B*ln[1],  -ln[2]};
		Pn.dehomogenize(polarline, polarline);
		return polarline;
	}

	@Override
	public Component getInspector() {
		Box inspectionPanel =  inspector;
		final TextSlider nSlider = new TextSlider.Integer("num",SwingConstants.HORIZONTAL, 1, 200, numPoints);
		nSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				numPoints = nSlider.getValue().intValue();
				updateConics();
			}
		});
		inspectionPanel.add(nSlider);
		final TextSlider mSlider = new TextSlider.Integer("count",SwingConstants.HORIZONTAL, 1, 40, numLines);
		mSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				numLines = mSlider.getValue().intValue();
				init();
				updatePencils();
			}
		});
		inspectionPanel.add(mSlider);
		final TextSlider aSlider = new TextSlider.Double("a",SwingConstants.HORIZONTAL, -1.0, 1.0, a);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				a = aSlider.getValue().doubleValue();
				updateConics();
			}
		});
		inspectionPanel.add(aSlider);
		final TextSlider bSlider = new TextSlider.Double("angle",SwingConstants.HORIZONTAL, -3.0, 3.0, vangle);
		bSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				vangle = bSlider.getValue().doubleValue();
				updatePencils();
			}
		});
		inspectionPanel.add(bSlider);
		final TextSlider rSlider = new TextSlider.Double("range",SwingConstants.HORIZONTAL, 0.0, 1.0, pencilRange);
		rSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				pencilRange = rSlider.getValue().doubleValue();
				updatePencils();
			}
		});
		inspectionPanel.add(rSlider);
		return inspectionPanel;
	}

	public static void main(String[] args) {
		new FocalPointDemo().display();
	}
}
