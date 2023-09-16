/*
 * Created on Dec 1, 2010
 *
 */
package charlesgunn.jreality.worlds.projective;

import static de.jreality.geometry.GeometryUtility.BOUNDING_BOX;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.LIGHTING_ENABLED;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.geometry.BezierCurve;
import charlesgunn.jreality.geometry.projective.LinePencilFactory;
import charlesgunn.jreality.geometry.projective.LineUtility;
import charlesgunn.jreality.geometry.projective.PointCollector;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.p5.PlueckerLineGeometry;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.LineDragEvent;
import de.jreality.tools.LineDragListener;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class IsotropicPlaneParabola extends Assignment {

	private static final Color gColor1 = new Color(200,180,50);
	private static final Color gBaseColor = new Color(255, 230, 50);
	private static final Color gFanColor = gBaseColor;
	private static final Color fColor1 = new Color(100,255,255);
	private static final Color baseColor = new Color(200,125,255);
	private static final Color fsegColor = new Color(80, 200, 200);
	private static final Color fbaseColor = new Color(100, 250, 250);
	private static final Color isoColor = new Color(100, 255, 100);
	private static final Color curveColor = new Color(200, 0,50);
	private static final Color legcurveColor = new Color(255, 100, 100);
	private double[] tform = {
			 1,0,0,0, 
			0, 1, 0, 0, 
			0,  0,1,0,  
			0, 1,0, 1},
		id = Rn.identityMatrix(4);
	private SceneGraphComponent world,
	    perspWorld,
	    curvesSGC,
			curveSGC,
			legendreSGC,
		pointsSGC,
			pointrangeSGC,
			pointSGC,
			basePointSGC,
			isoLineSGC,
			distSegSGC,
		linesSGC,
			pencilSGC,
			tangentSGC,
			isoPointSGC,
			distFanSGC,
			baseLineSGC,
		coordSysSGC,
			graphpaperSGC;
	int numPoints = 200;
	double domainSize = 10.0;
	PointCollector pc = new PointCollector(numPoints, 4),
			legendreTfPC = new PointCollector(numPoints, 4);
	IndexedLineSetFactory distSegF;
	Color[] colors1 = {Color.red, Color.yellow, Color.blue, Color.green, Color.magenta, fColor1};
	Color[] colors2 = {Color.green, Color.magenta, fColor1, Color.red, Color.yellow, Color.blue};
	
	double yscale = .5, xscale = 1, y1 = .75, y2 = .35, yt = .4;
	double tick = .03, currentTime = .6;
	BezierCurve bc2 = new BezierCurve(2, 
			new double[][] {
			{-1, .8+yt,0,1},
			{-.9,.5+yt,0,1}, 
			{-.05, .2+yt,0,1 },
			{.8, -.1+yt, 0, 1},
			{1,y1+yt,0, 1}});
	BezierCurve bc = new BezierCurve(2, 
			new double[][] {
			{-2, 4+yt,0,1},
			{-1, yt, 0,1}, 
			{0, yt, 0,1 },
			{1, yt, 0, 1},
			{2,4+yt,0, 1}});
	Timer goPersp = null;
	boolean isopenciltubed = true;
@Override
	public SceneGraphComponent getContent() {
		perspWorld = SceneGraphUtility.createFullSceneGraphComponent("perspWorld");
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		curvesSGC = SceneGraphUtility.createFullSceneGraphComponent("curves");
		curveSGC = SceneGraphUtility.createFullSceneGraphComponent("curve");
		legendreSGC = SceneGraphUtility.createFullSceneGraphComponent("leg curve");
		coordSysSGC = SceneGraphUtility.createFullSceneGraphComponent("coordsys");
		graphpaperSGC = SceneGraphUtility.createFullSceneGraphComponent("graphpaper");
		pointsSGC = SceneGraphUtility.createFullSceneGraphComponent("points");
		linesSGC = SceneGraphUtility.createFullSceneGraphComponent("lines");
		pencilSGC = SceneGraphUtility.createFullSceneGraphComponent("pencil");
		tangentSGC = SceneGraphUtility.createFullSceneGraphComponent("tangent");
		baseLineSGC = SceneGraphUtility.createFullSceneGraphComponent("base line");
		isoPointSGC = SceneGraphUtility.createFullSceneGraphComponent("iso point");
		distFanSGC = SceneGraphUtility.createFullSceneGraphComponent("dist fan");
		pointSGC = SceneGraphUtility.createFullSceneGraphComponent("point");
		pointrangeSGC = SceneGraphUtility.createFullSceneGraphComponent("point range");
		basePointSGC = SceneGraphUtility.createFullSceneGraphComponent("base point");
		isoLineSGC = SceneGraphUtility.createFullSceneGraphComponent("iso line");
		distSegSGC = SceneGraphUtility.createFullSceneGraphComponent("dist seg");
		linesSGC.addChildren(tangentSGC, baseLineSGC, isoPointSGC, distFanSGC, pencilSGC);
		pointsSGC.addChildren(pointSGC, basePointSGC, isoLineSGC, distSegSGC, pointrangeSGC);
		LinePencilFactory lcp= new LinePencilFactory();
		lcp.setCenter(new double[] {0,0,0,1});
		lcp.setNumLines(15);
		lcp.setFiniteSphere(false);
		lcp.setPlane(new double[] {0,0,1,0});
		lcp.setPoint(new double[] {0,0,0,1});
		lcp.update();
		pencilSGC.addChild(lcp.getPencil());
		MatrixBuilder.euclidean().rotateZ(Math.PI/30).translate(0,0, -.001).assignTo(pencilSGC);
		
//		LinePencilFactory isolpf= new LinePencilFactory();
//		isolpf.setCenter(new double[] {0,1,0,0});
//		isolpf.setNumLines(15);
//		isolpf.setFiniteSphere(false);
//		isolpf.setPlane(new double[] {0,0,1,0});
//		isolpf.setPoint(new double[] {0,1,0,0});
//		isolpf.update();
//		graphpaperSGC.addChild(isolpf.getPencil());

		Appearance ap = pencilSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", baseColor);
		ap.setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, true);
		ap.setAttribute("pointShader."+CommonAttributes.LINE_WIDTH, .08);
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .001);
		ap = curveSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", curveColor);
		ap = curvesSGC.getAppearance();
		ap.setAttribute(VERTEX_DRAW, false);
		ap.setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, true);
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .01);
		ap.setAttribute("lineShader."+CommonAttributes.LINE_WIDTH, 3.5);
		ap = legendreSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", legcurveColor);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		legendreSGC.setVisible(false);
		ap = pointsSGC.getAppearance();
		ap.setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, true);
		ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, .02);
		ap.setAttribute("pointShader.diffuseColor",fColor1);
		ap.setAttribute(VERTEX_DRAW, true);
		IndexedLineSet ils = PointRangeFactory.line( basis[3], basis[0]);
		pointrangeSGC.setGeometry(ils);
		ap = pointrangeSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor",fbaseColor);
		ap.setAttribute("lineShader."+CommonAttributes.LINE_WIDTH,3.0);
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .003);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, true);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);

		ap = basePointSGC.getAppearance();
		ap.setAttribute("pointShader.diffuseColor",fbaseColor);
		ap = isoLineSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", isoColor);
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .004);
		ap.setAttribute("lineShader.lineWidth", 2.0);
		ap.setAttribute(EDGE_DRAW, true);
		ap.setAttribute(VERTEX_DRAW, false);
		ap = distSegSGC.getAppearance();
		ap.setAttribute(EDGE_DRAW, false);
		ap.setAttribute(VERTEX_DRAW, true);
		ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, .015);
		ap.setAttribute("pointShader.diffuseColor",fsegColor);
		ap = linesSGC.getAppearance();
		ap.setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, true);
		ap.setAttribute("lineShader.diffuseColor", gColor1);
		ap.setAttribute("lineShader."+CommonAttributes.LINE_WIDTH, .5);
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .004);
		ap.setAttribute(EDGE_DRAW, true);
		ap.setAttribute(VERTEX_DRAW, false);
		ap = baseLineSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor",baseColor);
		ap = isoPointSGC.getAppearance();
		ap.setAttribute("pointShader.diffuseColor", isoColor);
		ap.setAttribute("pointShader."+CommonAttributes.SPHERES_DRAW, false);
		ap.setAttribute("pointShader."+CommonAttributes.ATTENUATE_POINT_SIZE, false);
		ap.setAttribute("pointShader.pointSize", 12.0);
		ap.setAttribute(VERTEX_DRAW, true);
		ap = distFanSGC.getAppearance();
		ap.setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, true);
		ap.setAttribute("lineShader.diffuseColor", gFanColor);
		ap.setAttribute("lineShader."+CommonAttributes.LINE_WIDTH, .5);
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .002);
		ap = world.getAppearance();
		ap.setAttribute("lineShader.lineWidth", 2.0);
		ap.setAttribute(LIGHTING_ENABLED, false);
		ap.setAttribute(EDGE_DRAW, true);
		ap.setAttribute(VERTEX_DRAW, false);
		ap.setAttribute(BOUNDING_BOX, Rectangle3D.unitCube);
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .003);
		world.addChildren(curvesSGC, pointsSGC, linesSGC, coordSysSGC);
		curvesSGC.addChildren(curveSGC, legendreSGC);
		DragEventTool t = new DragEventTool();
		t.addLineDragListener(new LineDragListener() {
			
			private IndexedLineSet lineSet;
			private double[][] points;
			
			public void lineDragStart(LineDragEvent e) {
				System.out.println("start dragging line "+e.getIndex());
				
				lineSet = e.getIndexedLineSet();
				points=new double[lineSet.getNumPoints()][];
				lineSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(points);
			}

			public void lineDragged(LineDragEvent e) {
				System.err.println("IN drag perform");
				double[][] newPoints=(double[][])points.clone();
				Matrix trafo=new Matrix();
				MatrixBuilder.euclidean().translate(e.getTranslation()).assignTo(trafo);
				curveSGC.getTransformation().multiplyOnLeft(trafo.getArray());
			}

			public void lineDragEnd(LineDragEvent e) {
				// transform the original control points and reset tform to id
				Matrix trafo = new Matrix(curveSGC.getTransformation());
				double[][] newbc = Rn.matrixTimesVector(null, trafo.getArray(), bc.getControlPoints());
				bc = new BezierCurve(2, newbc);
				new Matrix().assignTo(curveSGC.getTransformation());
				initializeCurves();
				setValueAtTime(currentTime);
			}			
		});

		curveSGC.addTool(t);
		
		ap = coordSysSGC.getAppearance();
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		perspWorld.addChild(world);
		MatrixBuilder.euclidean().translate(0,-1,0).assignTo(world);
		generateGeometry();	
		initializeCurves();
		setValueAtTime(currentTime);
		
		goPersp = new Timer(20, new ActionListener()	{
			double step = .006;
			final double[] xRotateM = P3.makeRotationMatrixX(null, -step);
			public void actionPerformed(ActionEvent e) {
				System.err.println("persp timer");
				world.getTransformation().multiplyOnRight(xRotateM); 
				viewer.renderAsync();
			}
		});

		
		return perspWorld;
	}
	double[][] basis = {{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}};
	
	private void generateGeometry() {
		IndexedLineSet ils = PointRangeFactory.line( basis[0], basis[1]);
		SceneGraphComponent xyzSGC = SceneGraphUtility.createFullSceneGraphComponent("xy");
		SceneGraphComponent xySGC = SceneGraphUtility.createFullSceneGraphComponent("xy");
		xySGC.setGeometry(ils);
		SceneGraphComponent ZSGC = SceneGraphUtility.createFullSceneGraphComponent();
		xySGC.addChild(ZSGC);
		ZSGC.setGeometry(Primitives.point(basis[1]));
		Appearance ap = ZSGC.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.SPHERES_DRAW, false);
		ap.setAttribute("pointShader.diffuseColor", Color.white);
		ap.setAttribute("pointShader."+CommonAttributes.POINT_SIZE,12.0);
		ap.setAttribute("pointShader."+CommonAttributes.ATTENUATE_POINT_SIZE, false);
		MatrixBuilder.euclidean().translate(0,0,-.01).assignTo(ZSGC);
		
		SceneGraphComponent ywSGC = SceneGraphUtility.createFullSceneGraphComponent("yw");
		ils = PointRangeFactory.line( basis[1], basis[3]);
		ywSGC.setGeometry(ils);
//		ywSGC.setVisible(false);
//		SceneGraphComponent wxSGC = SceneGraphUtility.createFullSceneGraphComponent("wx");
		// add some graph paper
		LinePencilFactory lpf = new LinePencilFactory(); 
		int lineCount = 200;
		double[][] plines = new double[lineCount][];
		for (int i = 0; i<lineCount; ++i)	{
			double xval = .05*(i-lineCount/2.0);
			plines[i] = PlueckerLineGeometry.lineFromPoints(null, basis[1],
					new double[]{xval, 0, 0, 1});
		}
		lpf.setPoint(basis[0]);
		lpf.setPlane(basis[2]);
		lpf.setPluckerLines(plines);
		lpf.setFan(true);
		lpf.setFiniteSphere(true);
		lpf.setSphereRadius(500);
		lpf.setNumLines(lineCount);
		lpf.update();
		graphpaperSGC.addChild(lpf.getPencil());
//		IndexedFaceSetFactory quad = Primitives.texturedQuadrilateralFactory();
//		graphpaperSGC.setGeometry(quad.getIndexedFaceSet());
//		graphpaperSGC.setAppearance(new Appearance());
//		SimpleTextureFactory stf = new SimpleTextureFactory();
//		stf.setType(TextureType.LINE);
//		stf.setColor(0, Color.white);
//		stf.setColor(2, Color.white);
//		stf.setColor(1, new Color(0,0,0,0));
//		stf.setColor(3, new Color(0,0,0,0));
//		stf.setSize(192);
//		stf.update();
//		Texture2D tex2d = TextureUtility.createTexture(graphpaperSGC.getAppearance(), "polygonShader", stf.getImageData());
//		Matrix foo = new Matrix();
//		MatrixBuilder.euclidean().scale(400,400,1).assignTo(foo);
//		tex2d.setTextureMatrix(foo);
//		MatrixBuilder.euclidean().translate(-25,-25, -.01).scale(50).assignTo(graphpaperSGC);
//		graphpaperSGC.getAppearance().setAttribute("polygonShader.diffuseColor", Color.white);
//		graphpaperSGC.getAppearance().setAttribute("ambientCoefficient", .05);
		graphpaperSGC.getAppearance().setAttribute("lineShader.diffuseColor",new Color(150,150,150));
		graphpaperSGC.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, isopenciltubed);
		graphpaperSGC.getAppearance().setAttribute("lineShader."+CommonAttributes.LINE_WIDTH, 1.0);
		graphpaperSGC.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .004);
		
		
		ap = xyzSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", Color.white);
		ap.setAttribute("lineShader.lineWidth",3.0);
//		xyLinesSGC.getAppearance().setAttribute("lineShader.diffuseColor", new Color(200, 200, 200));
//		xyLinesSGC.getAppearance().setAttribute("lineShader.lineWidth",1.0);
		xyzSGC.addChildren(xySGC, ywSGC);
		coordSysSGC.addChildren(xyzSGC, graphpaperSGC);
		
	}

	private void initializeCurves() {
		legendreTfPC.reset();
		for (int i = 0; i<numPoints; ++i)   {
			double t = i/(numPoints-1.0);
			double[] base = bc.getValueAtTime(t);
			double[] dbase = legendreTformAtTime(t);
			pc.addPoint(base);
			legendreTfPC.addPoint(dbase);
		}
		curveSGC.setGeometry(pc.getCurve());
		legendreSGC.setGeometry(legendreTfPC.getCurve());
	}
	
	
	@Override
	public void setValueAtTime(double t) {
		super.setValueAtTime(t);
		currentTime = t;
		double[] val = bc.getValueAtTime(t);
		pointSGC.setGeometry(Primitives.point(val));
		double[] base = val.clone();
		base[1] = 0;
		basePointSGC.setGeometry(Primitives.point(base));
		double[] tl = bc.getTangentAtTime(t);
		PlueckerLineGeometry.normalize(tl, tl);
		double[] isoLine = PlueckerLineGeometry.lineFromPoints(null, val, base);
		LineUtility.sceneGraphForLine(isoLineSGC, isoLine, null, 5, false);
		double[][] nb = {base, val};
		if (distSegF == null) {
			distSegF = IndexedLineSetUtility.createCurveFactoryFromPoints(nb, false);
		}
		double d = Pn.distanceBetween(val, base, Pn.EUCLIDEAN);
		int num = (int) (d/tick);
		double max = num*tick;
		nb[1] = nb[1].clone();
		nb[1][1] = max;
		distSegF.setVertexCount(2);
		distSegF.setVertexCoordinates(nb);
		distSegF.update();
		System.err.println("# segs = "+num);
		IndexedLineSet ils = IndexedLineSetUtility.refine(distSegF.getIndexedLineSet(), num);
		distSegSGC.setGeometry(ils);
		
		LineUtility.sceneGraphForLine(tangentSGC, tl, null, 5, false);
		System.err.println("tangent "+Rn.toString(tl));
		double[] btl = tl.clone();
		btl[0] =  0.0;
		LineUtility.sceneGraphForLine(baseLineSGC, btl, null, 5, false);
		
		double[] isoPt = {tl[2],-tl[4],0,0};
		if (isoPt[1] < 0) isoPt = Rn.times(null, -1.0, isoPt);
//		System.err.println("iso pt "+Rn.toString(isoPt));
		isoPointSGC.setGeometry(Primitives.point(isoPt));
		
		// this is a hassle ... generating the fan
		double[] xplane = {1,0,0,0},
				yintercept = PlueckerLineGeometry.lineIntersectPlane(null, tl, xplane);
		Pn.normalize(yintercept, yintercept, Pn.EUCLIDEAN);
		d = yintercept[1];
		num = (int) (d/tick);
		if (Math.abs(num) > 100) num = (int) (100 * Math.signum(num));
		max = num*tick;
		num = Math.abs(num);
		if (num > 1) {
			double[][] fanLines = new double[num][];
			for (int i = 0; i<num; ++i)	{
				t = (max/d)* i/(num-1.0);
				fanLines[i] = Rn.linearCombination(null, 1-t, btl, t, tl);
			}
//			System.err.println("Fan lines = "+Rn.toString(fanLines));
			LinePencilFactory fanLPF = new LinePencilFactory();
			fanLPF.setNumLines(num);
			fanLPF.setPluckerLines(fanLines);
			fanLPF.setPoint(isoPt);
			fanLPF.setFiniteSphere(false);
			fanLPF.update();
			distFanSGC.removeAllChildren();
			distFanSGC.addChild(fanLPF.getPencil());			
		} else 
			distFanSGC.removeAllChildren();

//		double[] legPt = val.clone();
//		legPt[1] = -yintercept[1];
//		legendreTfPC.addPoint(legPt);
	
	}

	private double[] legendreTformAtTime(double t)	{
		double[]  tl = bc.getTangentAtTime(t);
		double[] val = bc.getValueAtTime(t);
		double[] val2d = {val[0], val[1], val[3]},
				tl2d = {tl[4]/tl[2], -1, tl[0]/tl[2]};
		System.err.println("tangent = "+Rn.toString(tl2d));
		// g(U) = <U,x> - f(x),   U = slope
		double[] ret ={tl2d[0], -val2d[0]*tl2d[0] - val2d[1], 0, 1};
		// shortcut: g(p) is -(y-intercept) of the tangent line
//		double[] ret ={tl2d[0], -tl2d[2], 0, 1};
//		System.err.println("legendre = "+Rn.toString(ret));
		return ret;
	}

	@Override
	public void display() {
		super.display();
		
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(0,0,0,0));		
//		CameraUtility.encompass(viewer);
		Camera cam  = CameraUtility.getCamera(jrviewer.getViewer());
		cam.setFar(-1.0);
//		SceneGraphComponent camsgc = CameraUtility.getCameraNode(jrviewer.getViewer());
//		MatrixBuilder.euclidean().translate(0,0,3.0).rotateX(Math.PI/4).assignTo(camsgc);
		((Component) viewer.getViewingComponent()).addKeyListener(getKeyAdapter());

	}

	KeyAdapter ka = null;
	public KeyAdapter getKeyAdapter() {
		if (ka == null)	{
			ka = new KeyAdapter()	{
				boolean xroting = false;
				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_H:
						System.out.println("	1: toggle persp");
						break;
		
						
					case KeyEvent.VK_1:
						Camera cam  = CameraUtility.getCamera(jrviewer.getViewer());
						cam.setFar(-1.0);
						xroting = !xroting;
						if (xroting) goPersp.start();
						else goPersp.stop();
						break;
					
					case KeyEvent.VK_2:
						linesSGC.setVisible(!linesSGC.isVisible());
						break;
						
					case KeyEvent.VK_3:
						pointsSGC.setVisible(!pointsSGC.isVisible());
						break;
						
					case KeyEvent.VK_4:
						legendreSGC.setVisible(!legendreSGC.isVisible());
						break;
						
					case KeyEvent.VK_5:
						graphpaperSGC.setVisible(!graphpaperSGC.isVisible());
						break;
						
					case KeyEvent.VK_6:
						isopenciltubed = !isopenciltubed;
						graphpaperSGC.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, isopenciltubed);
						break;
						
				}
		
				}
			};
		}
		return ka;
	};

	@Override
	public Component getInspector() {
		Box vbox = Box.createVerticalBox();
		final TextSlider<Double> alphas = new TextSlider.Double("t", SwingConstants.HORIZONTAL, 0, 1, currentTime);
		alphas.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				double t = alphas.getValue().doubleValue();
				setValueAtTime(t);
			}
		});
		vbox.add(alphas);
		final TextSlider<Double> persp = new TextSlider.Double("persp", SwingConstants.HORIZONTAL, 0, 1, 0);
		persp.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				double t = persp.getValue().doubleValue();
				double[] atform = new double[16];
				for (int i = 0; i<16; ++i)	{
					atform[i] = AnimationUtility.linearInterpolation( id[i], tform[i], t);
					
				}
				Matrix mm = new Matrix(atform);
				mm.multiplyOnRight(MatrixBuilder.euclidean().translate(0, t, 0).getMatrix());
				mm.assignTo(perspWorld.getTransformation());;

			}
		});
		vbox.add(persp);
		return vbox;
	}
	

	public static void main(String[] args) {
		new IsotropicPlaneParabola().display();
	}
}
