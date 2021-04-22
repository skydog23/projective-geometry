package charlesgunn.pathcurve;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.LINE_WIDTH;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.anim.core.Animated;
import charlesgunn.anim.core.KeyFrameAnimatedBean;
import charlesgunn.anim.core.KeyFrameAnimatedColor;
import charlesgunn.anim.core.KeyFrameAnimatedDelegate;
import charlesgunn.anim.core.KeyFrameAnimatedTransformation;
import charlesgunn.anim.util.AnimationUtility.InterpolationTypes;
import charlesgunn.gui.TransformationInspector;
import charlesgunn.jreality.geometry.Snake;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.Complex;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class PathCurveDemo extends Assignment  {
	{
		Locale.setDefault(Locale.US);
	}

	private int steps;
	final double[] initialcoord = {1,0,0,0,   0,1,0,0,  0,0,-.5,.5,  0,0,1,1};
	Transformation coordinateSystem = new Transformation(initialcoord);
	double dT = 1.0, minT = 0.0;
	int numCurves = 24;
	double lambda = 1.4, 
		epsilon = .4, 
		speed = 1.0, 
		totalAngle = Math.PI*2;
//		wvZtlate = 0.27,
//		wateryVortexLambda = 1.43,
//		wvInit = .65,
//		wvTmin = -1,
//		wvTmax = 2;
	double radius = .5;
	double tmin = -3*Math.PI;
	double tmax = 3*Math.PI;
	double dNumCurves = numCurves, 
		sectorAngle = Math.PI*2/numCurves;
	transient double[] beginningPoint = {radius, 0, 0, 1};
	transient int[] snakeinfo = null;
	transient private Snake snake;
	transient private static SceneGraphComponent world,
			curves[],
			curveHolder,
			pivotForm;
	transient private SemiImagPathCurveFactory pcf;
	transient private double[][] curveOrbit;
//	double[] coordinateSystem = Rn.identityMatrix(4);
	transient Complex[] ev = new Complex[4];
	transient private IndexedLineSetFactory tetrafactory;
	transient private double[][] tetraVerts;
	transient private SceneGraphComponent tetra;
	transient static Dimension dim = new Dimension(1064, 798);
	transient private TextSlider<Double>  minTSlider,
		dTSlider,
		lambdaSlider,
		epsilonSlider,
		radiusSlider,
		speedSlider,
		taSlider;
	transient private TextSlider<Integer>  dNumCurvesSlider;
	transient WateryVortex 	wateryVortex = new WateryVortex(this);

	
	@Override
	public SceneGraphComponent getContent() {
		world = SceneGraphUtility.createFullSceneGraphComponent();
		curveHolder = SceneGraphUtility.createFullSceneGraphComponent("curve holder");
		pivotForm = SceneGraphUtility.createFullSceneGraphComponent("pivot");
		pivotForm.setVisible(false);
		double d = Math.log(1.4);
		double e = Math.log(.8);
		double alpha = 1; //Math.PI/8;
		steps = 500;
		ev[0] = new Complex(0, alpha);
		ev[1] = Complex.conjugate(null, ev[0]);
		ev[2] = new Complex(d,0);
		ev[3] = new Complex(e,0);
		pcf = new SemiImagPathCurveFactory();
		pcf.setEigenvalues(ev);
		pcf.setTmin(tmin);
		pcf.setTmax(tmax);
		pcf.setNumberSteps(steps);
		initialCurves();
//		pivotForm.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);

				
		tetraVerts = new double[][]{{0,0,0,1},{0,0,1,0},{0,1,0,0},{1,0,0,0}};
		int[][] indices = {{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}};
		Color[] tetraColors = {Color.red, Color.blue, Color.green, Color.yellow, Color.cyan, new Color(255,0,255)};
		tetrafactory = new IndexedLineSetFactory();
		tetrafactory.setVertexCount(4);
		tetrafactory.setVertexCoordinates(tetraVerts);
		tetrafactory.setEdgeCount(6);
		tetrafactory.setEdgeIndices(indices);
		tetrafactory.setEdgeColors(tetraColors);
		update();
		IndexedLineSet axs = tetrafactory.getIndexedLineSet(); //new IndexedLineSet(2,1);
		tetra = SceneGraphUtility.createFullSceneGraphComponent();
		tetra.setVisible(false);
		tetra.setGeometry(axs);
		axs.setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		world.addChildren(curveHolder, pivotForm, wateryVortex.getSGC(), tetra);
		Appearance ap = world.getAppearance();
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.YELLOW);
		ap.setAttribute(FACE_DRAW,true);
		ap.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, Color.WHITE);
		ap.setAttribute(LINE_SHADER+"."+POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.WHITE);
		ap.setAttribute(POINT_SHADER+"."+DIFFUSE_COLOR, new Color(10,150,10));
//		ap.setAttribute(POINT_SHADER+"."+POLYGON_SHADER+"."+DIFFUSE_COLOR, new Color(10,150,10));
		ap.setAttribute(LINE_SHADER+"."+LINE_WIDTH, 1.5);
		ap.setAttribute(POINT_SHADER+"."+POINT_RADIUS, .01);
		ap.setAttribute(LINE_SHADER+"."+TUBE_RADIUS, .005);
//		MatrixBuilder.euclidean().rotateX(-Math.PI/2).assignTo(world);
		
		
		return world;
	}

	private void initialCurves() {
		SceneGraphUtility.removeChildren(curveHolder);
		curves = new SceneGraphComponent[numCurves];
		for (int i = 0; i< numCurves; ++i)	{
			curves[i] = new SceneGraphComponent();
			curveHolder.addChild(curves[i]);
		}
	}
	protected void update() {
		double xlambda = lambda;
		if (lambda < -1) xlambda = -1.0/(lambda+2);
		if (lambda > 1) xlambda =  1.0/(2-lambda);
		double xepsilon = epsilon;
		if (epsilon < -1) xepsilon = -1.0/(epsilon+2);
		if (epsilon > 1) xepsilon =  1.0/(2-epsilon);
		if (xepsilon != 0) xepsilon = 1.0/xepsilon;
		else xepsilon = 10E8;
		pcf.setSpeed(speed);
		pcf.setLambda(xlambda);
		pcf.setEpsilon(xepsilon);
		pcf.setInitialPoint(new double[]{radius,0,0,1});
		pcf.setCoordinateSystem(coordinateSystem.getMatrix());
		pcf.update();
		curveOrbit = pcf.getCurvePoints(); 
		double[] radii = new double[curveOrbit.length];
		for (int i = 1; i<curveOrbit.length; ++i)	{
			radii[i] = 100*Pn.distanceBetween(curveOrbit[i], curveOrbit[i-1], Pn.EUCLIDEAN);
		}
		radii[0] = radii[1];
		if (snake == null)	{
			snake = new Snake(curveOrbit);
			snakeinfo = snake.getInfo();
			snakeinfo[0] = 0;
		} 
		snakeinfo[0] = (int) ((minT)*steps );
		double dTtmp = (minT+dT > 1.0) ? 1.0 - minT : dT;
		snakeinfo[1] = (int) ((dTtmp)*steps );			
		snake.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(4).createWritableDataList(curveOrbit));
		snake.update();
		snake.setVertexAttributes(Attribute.RELATIVE_RADII, StorageModel.DOUBLE_ARRAY.createReadOnly(radii));
		sectorAngle = Math.PI*2.0/dNumCurves;
		int newNumCurves = (int) dNumCurves;
		if (newNumCurves != numCurves) {
			numCurves = newNumCurves;
			initialCurves();
		}
		for (int i = 0; i< numCurves; ++i)	{
			double[] m = MatrixBuilder.euclidean().rotateZ(i*totalAngle/(numCurves)).getArray();
			double[] mm = Rn.conjugateByMatrix(null, m, coordinateSystem.getMatrix()); 
			MatrixBuilder.euclidean(new Matrix(mm)).assignTo(curves[i]);
			curves[i].setGeometry(snake);
		}
		tetrafactory.setVertexCoordinates(Rn.matrixTimesVector(null, coordinateSystem.getMatrix(), tetraVerts));
		tetrafactory.update();
		
		// update pivot
		double[][][] planeForm = wateryVortex.getPlaneForm();
		int dimu = planeForm.length,
				dimv = planeForm[0].length;
		double[][][] points = new double[dimv][dimu][];
		for (int i = 0; i<dimv; ++i)	{
			for (int j = 0; j<dimu; ++j)	{
				points[i][j] = pcf.getPivotPoint(null, planeForm[i][j]);
			}
			Pn.dehomogenize(points[i], points[i]);
		}
//		System.err.println("points = \n"+Rn.toString(points));

		QuadMeshFactory qmf = new QuadMeshFactory();
		qmf.setULineCount(dimu);
		qmf.setVLineCount(dimv);
		qmf.setClosedInUDirection(true);
		qmf.setClosedInVDirection(false);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setGenerateFaceNormals(true);
		qmf.setVertexCoordinates(points);
		qmf.update();
		pivotForm.setGeometry(qmf.getGeometry());
		
//		psf.getGeometry().setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
	}

	Viewer viewer;
	@Override
	public void display() {
		super.display();
		this.viewer = jrviewer.getViewer();
//		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.FOG_ENABLED, true);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.FOG_DENSITY, .15);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", new Color(20,20,20,0));
		CameraUtility.encompass(viewer);
		CameraUtility.getCamera(viewer).setPerspective(false);
		setupAnimation();
		
//		CameraUtility.getCamera(viewer).setFieldOfView(20.0);
		((Component) viewer.getViewingComponent()).addKeyListener(new KeyAdapter()	{
			
		    
			public void keyPressed(KeyEvent e)	{ 
				switch(e.getKeyCode())	{
					
				case KeyEvent.VK_H:
					break;
				case KeyEvent.VK_1:
					tetra.setVisible(!tetra.isVisible());
					break;
				case KeyEvent.VK_2:
					wateryVortex.getSGC().setVisible(!wateryVortex.getSGC().isVisible());
					break;
				case KeyEvent.VK_3:
					pivotForm.setVisible(!pivotForm.isVisible());
					break;
					
				case KeyEvent.VK_4:
					wateryVortex.getSGC().getTransformation().setMatrix(Rn.identityMatrix(4));
					break;
					
					}
			}
		});
	}

	private void setupAnimation()	{
		PathCurveDemoBean pcdb = new PathCurveDemoBean();
		KeyFrameAnimatedBean<PathCurveDemoBean> adb = new KeyFrameAnimatedBean<PathCurveDemoBean>(pcdb);
		adb.setUpdateRunnable(new Runnable() {

			public void run() {
				update();
				viewer.renderAsync();
			}
			
		});
		adb.setName("PathCurveDemo");
		
		animationPlugin.getAnimated().add(adb);
		Camera c = CameraUtility.getCamera(viewer);
		KeyFrameAnimatedBean<Camera> ac = new KeyFrameAnimatedBean<Camera>(c);
		animationPlugin.getAnimated().add(ac);
	

		final List<Animated> animated = new Vector<Animated>();
		Transformation ct = CameraUtility.getCameraNode(viewer).getTransformation();
		if (ct == null) { 
			ct = new Transformation();
			CameraUtility.getCameraNode(viewer).setTransformation(ct);
		}
		KeyFrameAnimatedTransformation camT = new KeyFrameAnimatedTransformation(ct);
		camT.setInterpolationType(InterpolationTypes.CUBIC_HERMITE);
		animated.add(camT);
		KeyFrameAnimatedTransformation worldT = new KeyFrameAnimatedTransformation(world.getTransformation());
		worldT.setInterpolationType(InterpolationTypes.CUBIC_HERMITE);
		animated.add(worldT);
		
		KeyFrameAnimatedDelegate<Color> kfad = new KeyFrameAnimatedDelegate<Color> () {
			Appearance ap = world.getAppearance();
			public void propagateCurrentValue(Color t) {
				ap.setAttribute("lineShader.diffuseColor", t);
			}

			public Color gatherCurrentValue(Color t) {
				Object foo = ap.getAttribute("lineShader.diffuseColor", Color.class);
				if (foo instanceof Color)	
					return ((Color) foo);
				return Color.white;  // signal error!
			}
			
		};
		KeyFrameAnimatedColor lineCA = new KeyFrameAnimatedColor( kfad);
		animated.add(lineCA);
		
		
//		AnimationPanelListenerImpl apl = new AnimationPanelListenerImpl(viewer,"path curve");
//		apl.setAnimated(animated);
//		apl.setViewer(viewer);
//		ap.addAnimationPanelListener(apl);
	}
	
	public class PathCurveDemoBean	{
		
		public double getMinT() {
			return minT;
		}

		public void setMinT(double d) {
			minT = d;
			minTSlider.setValue(minT);
		}

		public double getDTB() {
			return dT;
		}

		public void setDT(double d) {
			dT = d;
			dTSlider.setValue(d);
		}

		public double getEpsilon() {
			return epsilon;
		}

		public void setEpsilon(double d) {
			epsilon = d;
			epsilonSlider.setValue(d);
		}

		public double getLambda() {
			return lambda;
		}

		public void setLambda(double d) {
			lambda = d;
			lambdaSlider.setValue(d);
		}

		public double getTotalAngle() {
			return totalAngle;
		}

		public void setTotalAngle(double d) {
			totalAngle = d;
			taSlider.setValue(d);
		}

		public double getDNumCurves() {
			return dNumCurves;
		}

		public void setDNumCurves(double d) {
			dNumCurves = d;
			dNumCurvesSlider.setValue(((int) d));
		}

		public double getRadius() {
			return radius;
		}

		public void setRadius(double d) {
			radius = d;
			radiusSlider.setValue(d);
		}

		public double getSpeed() {
			return speed;
		}

		public void setSpeed(double d) {
			speed = d;
			speedSlider.setValue(d);
		}
	}
	

//	boolean doEncompass = false;
	
	@Override
	public Component getInspector() {	
		Box inspectionPanel =  inspector;
		minTSlider = new TextSlider.Double("minT",SwingConstants.HORIZONTAL,0.0,1.0,minT);
		minTSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				minT = minTSlider.getValue().doubleValue();
				update();
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(minTSlider);
		dTSlider = new TextSlider.Double("dT",SwingConstants.HORIZONTAL,0.0,1.0,dT);
		dTSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				dT = dTSlider.getValue().doubleValue();
				update();
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(dTSlider);
		lambdaSlider = new TextSlider.Double("lambda",SwingConstants.HORIZONTAL,-2.0, 2.0,lambda);
		lambdaSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				lambda = lambdaSlider.getValue().doubleValue();
				update();
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(lambdaSlider);
		epsilonSlider = new TextSlider.Double("epsilon",SwingConstants.HORIZONTAL,-2.0, 2.0,1.0/epsilon);
		epsilonSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				epsilon = epsilonSlider.getValue().doubleValue();
				update();
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(epsilonSlider);
		speedSlider = new TextSlider.Double("speed",SwingConstants.HORIZONTAL,0.0, 2.0,speed);
		speedSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				speed = speedSlider.getValue().doubleValue();
				update();
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(speedSlider);
		radiusSlider = new TextSlider.Double("initial radius",SwingConstants.HORIZONTAL,0.0, 2.0,radius);
		radiusSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				radius = radiusSlider.getValue().doubleValue();
				update();
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(radiusSlider);
		dNumCurvesSlider = new TextSlider.Integer("number of curves",SwingConstants.HORIZONTAL,0, 50,numCurves);
		dNumCurvesSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				dNumCurves = dNumCurvesSlider.getValue().intValue();
				update();
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(dNumCurvesSlider);
		taSlider = new TextSlider.Double("total angle",SwingConstants.HORIZONTAL,0.0, Math.PI*2, totalAngle);
		taSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				totalAngle = taSlider.getValue().doubleValue();
				update();
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(taSlider);
		inspectionPanel.add(dNumCurvesSlider);
		TransformationInspector tformInsp = new TransformationInspector.Raw(coordinateSystem, new ActionListener()	{
			public void actionPerformed(ActionEvent e) {
				update();
				viewer.render();
			}
		});
		inspectionPanel.add(wateryVortex.getInspector());
		
		tformInsp.addBorderTitle("Fundamental Tetrahedron (columns)");
		inspectionPanel.add(tformInsp);
//		InspectorPanel testpanel = new InspectorPanel();
//		testpanel.setObject(pcf);
//		JScrollPane pane  = new JScrollPane(testpanel);
//		inspectionPanel.add(pane);
		inspectionPanel.setName("Parameters");
		return inspectionPanel;
	}

//	static AnimationPanel ap;

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public double getDT() {
		return dT;
	}

	public void setDT(double dt) {
		dT = dt;
	}

	public double getMinT() {
		return minT;
	}

	public void setMinT(double minT) {
		this.minT = minT;
	}

	public int getNumCurves() {
		return numCurves;
	}

	public void setNumCurves(int numCurves) {
		this.numCurves = numCurves;
	}

	public double getLambda() {
		return lambda;
	}

	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getTotalAngle() {
		return totalAngle;
	}

	public void setTotalAngle(double totalAngle) {
		this.totalAngle = totalAngle;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getTmin() {
		return tmin;
	}

	public void setTmin(double tmin) {
		this.tmin = tmin;
	}

	public double getTmax() {
		return tmax;
	}

	public void setTmax(double tmax) {
		this.tmax = tmax;
	}

	public TextSlider<Double> getMinTSlider() {
		return minTSlider;
	}

	public void setMinTSlider(TextSlider<Double> minTSlider) {
		this.minTSlider = minTSlider;
	}

	public TextSlider<Double> getLambdaSlider() {
		return lambdaSlider;
	}

	public void setLambdaSlider(TextSlider<Double> lambdaSlider) {
		this.lambdaSlider = lambdaSlider;
	}

	public TextSlider<Double> getEpsilonSlider() {
		return epsilonSlider;
	}

	public void setEpsilonSlider(TextSlider<Double> epsilonSlider) {
		this.epsilonSlider = epsilonSlider;
	}

	public TextSlider<Double> getRadiusSlider() {
		return radiusSlider;
	}

	public void setRadiusSlider(TextSlider<Double> radiusSlider) {
		this.radiusSlider = radiusSlider;
	}

	public TextSlider<Double> getSpeedSlider() {
		return speedSlider;
	}

	public void setSpeedSlider(TextSlider<Double> speedSlider) {
		this.speedSlider = speedSlider;
	}

	public TextSlider<Double> getTaSlider() {
		return taSlider;
	}

	public void setTaSlider(TextSlider<Double> taSlider) {
		this.taSlider = taSlider;
	}

	public TextSlider<Double> getDTSlider() {
		return dTSlider;
	}

	public void setDTSlider(TextSlider<Double> slider) {
		dTSlider = slider;
	}

	public double getDNumCurves() {
		return dNumCurves;
	}

	public void setDNumCurves(double numCurves) {
		dNumCurves = numCurves;
	}

	public TextSlider<Integer> getDNumCurvesSlider() {
		return dNumCurvesSlider;
	}

	public void setDNumCurvesSlider(TextSlider<Integer> numCurvesSlider) {
		dNumCurvesSlider = numCurvesSlider;
	}

	public static void main(String[] args) {
		new PathCurveDemo().display();
	}
}
