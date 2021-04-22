/*
 * Created on Nov 11, 2004
 *
 */
package charlesgunn.jreality.worlds.projective;

import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.METRIC;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.OceanTheme;

import charlesgunn.anim.jreality.SceneGraphAnimator;
import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.geometry.SphereUtility;
import de.jreality.geometry.TubeFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.ImplodePolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.tools.RotateTool;
import de.jreality.tutorial.util.FlyTool;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.util.WingedEdge;

/**
 * @author gunn
 *
 */
public class HopfFibrationAssg extends Assignment {
	Hashtable<Appearance, Color> colors = new Hashtable<Appearance, Color>();
	int numLevels = 5;
	protected boolean 
			showTubes = true, 
			showLevel[] = { true, false, false, false, false }, 
			showXYZ = false,
			showHemisphere = false,
			concentricHyps = true,
			zColoring = true,
			bigCore = true,
			mirror = false,
			isInSceneFlight = false,
			show24Cell = false,
			showInSceneGeom = false;
	double[] radii = { .02, .01, .005, .0025, .00125 };
	double globalRadius = .015,
			thetaPhiRatio = 0.6,
			coreRadius = 2.0,
			gamma = 1.5,
			octaScale = .4,
			implodeFactor = .15;
	int numSegs = 12;
	int profileSize = 16;

	SceneGraphComponent  
		theUniverse,
			theUniverse2,
		    		levels[][],
		    			theWorld,
		    			// ...
		    				urCurve,
		    				urTube[],
			    	cores,
		    			coreV,
		    			coreH,
		    		the24cell,
		    			octaDD,
			    inSceneFlight,
	    				inSceneCamera,
	    					inSceneCameraContainer,
		    	mirrorSGC,
	    colorSphere,
	    		colorSphereKids[];
	Color[] threec = { Color.red, Color.blue, Color.yellow };
	Color coreColor = new Color(255, 255, 255);
	int maxLevel = -1;
	Timer rotate, inSceneFlightTimer;
	double dangle1 = .002;
	double[] cliffordTlate = null,
			positionCamera = null,
			inSceneFlightTform = null;

	double cameraAngle = -Math.PI/8;
//	final double[] inSceneFlightTimerM = Rn.identityMatrix(4);
	SceneGraphPath inSceneCamSGP,
			standardCamSGP;
	
	public SceneGraphComponent getContent() {
		theUniverse = SceneGraphUtility.createFullSceneGraphComponent("universe");
		theUniverse2 = SceneGraphUtility.createFullSceneGraphComponent("universe2");
		the24cell = SceneGraphUtility.createFullSceneGraphComponent("24 cell");
		octaDD = SceneGraphUtility.createFullSceneGraphComponent("24 cell FD");
		inSceneFlight= SceneGraphUtility.createFullSceneGraphComponent("in scene flight");
		inSceneCamera= SceneGraphUtility.createFullSceneGraphComponent("in scene camera");
		inSceneCameraContainer = SceneGraphUtility.createFullSceneGraphComponent("in scene camera container");
		mirrorSGC= SceneGraphUtility.createFullSceneGraphComponent("mirror");
		theWorld = SceneGraphUtility.createFullSceneGraphComponent("world");
		colorSphere = SceneGraphUtility.createFullSceneGraphComponent("color sphere");
		urCurve = SceneGraphUtility.createFullSceneGraphComponent("ur curve");
		cores = SceneGraphUtility.createFullSceneGraphComponent("cores");
		coreV = SceneGraphUtility.createFullSceneGraphComponent("coreV");
		coreH = SceneGraphUtility.createFullSceneGraphComponent("coreH");
		levels = new SceneGraphComponent[numLevels][2];
		urTube = new SceneGraphComponent[levels.length];
		colorSphereKids = new SceneGraphComponent[levels.length];
		for (int i = 0; i<colorSphereKids.length; ++i)	{
			colorSphereKids[i] = SceneGraphUtility.createFullSceneGraphComponent("ico sphere");			
			colorSphere.addChild(colorSphereKids[i]);
			urTube[i] = SceneGraphUtility.createFullSceneGraphComponent("ur tube "+i);
		}
		the24cell.setVisible(show24Cell);
		inSceneCameraContainer.setVisible(showInSceneGeom);
		Appearance ap = colorSphere.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.SPHERES_DRAW, true);
		ap.setAttribute(CommonAttributes.POINT_RADIUS, .02);
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		
		ap = theUniverse.getAppearance();
		ap.setAttribute(SceneGraphAnimator.ANIMATED, false);
		
		ap = cores.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", Color.black);
		cores.addChildren(coreV, coreH);
		
		coreColor = getColorForParameter(1.0);
		
		for (int i = 0; i < 3; ++i) {
			SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent();
			sgc.getAppearance().setAttribute("polygonShader.diffuseColor", threec[i]);
			sgc.getAppearance().setAttribute("lineShader.diffuseColor", threec[i]);
//			sgc.getAppearance().setAttribute("lineShader.diffuseColor", Color.black);
			sgc.addChild(theWorld);
			MatrixBuilder.euclidean().rotate(i * Math.PI * 2 / 3.0, new double[] { 1, 1, 1 }).assignTo(sgc);
			theUniverse2.addChild(sgc);
		}
		
		initKeyFrames();
		initWorld();
		updateVisibility();
		
		ap = theUniverse2.getAppearance();
		ap.setAttribute(CommonAttributes.METRIC, Pn.ELLIPTIC);
		ap.setAttribute("pointShader."+CommonAttributes.SPHERES_DRAW,false);
		ap.setAttribute("pointShader."+CommonAttributes.DIFFUSE_COLOR, new Color(255,255,150));
		ap.setAttribute("pointShader."+CommonAttributes.POINT_SIZE, 15);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute("useGLSL", true); //!coloredFace);

		MatrixBuilder.euclidean().scale(-1).assignTo(mirrorSGC);
		mirrorSGC.addChild(theUniverse2);
		
		DiscreteGroup my24cell = discreteGroup.demo.Cell24.get24CellGroup();
		DiscreteGroupSceneGraphRepresentation dgsgr24 = new DiscreteGroupSceneGraphRepresentation(my24cell);
		MatrixBuilder.elliptic().
			rotateZ(Math.PI/4).
			translate( new double[] {0,0,0,1}, new double[] {0,0,1,1}).
			assignTo(dgsgr24.getChangeOfBasisNode());
		dgsgr24.update();
//		dgsgr24.getChangeOfBasisNode().setTransformation(tf );

		DirichletDomain dirdom = new DirichletDomain(my24cell);
		IndexedFaceSet dirichletDomain = dirdom.getDirichletDomain();

		((WingedEdge) dirichletDomain).setColoredFaces(false);
		((WingedEdge) dirichletDomain).setMetric(Pn.ELLIPTIC);
		dirdom.update();
		System.err.println("dirdom " + dirichletDomain.getNumFaces());
		double[][] normals = dirichletDomain.getFaceAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
		System.err.println("DD normals =\n"+Rn.toString(normals));
		octaDD.setGeometry(dirichletDomain);
		dgsgr24.setWorldNode(octaDD);
		dgsgr24.update();
		the24cell.addChild(dgsgr24.getRepresentationRoot());
		ap = the24cell.getAppearance();
		ap.setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute("polygonShader."+CommonAttributes.SMOOTH_SHADING, false);
		ap.setAttribute("lineShader.diffuseColor", Color.green);
		ap.setAttribute("polygonShader.diffuseColor", Color.white);
		update24Cell();
//		ap.setAttribute(CommonAttributes.METRIC, Pn.ELLIPTIC);
		
		inSceneFlight.addChild(inSceneCamera);
		ap = inSceneCamera.getAppearance();
		ap.setAttribute(CommonAttributes.RENDER_S3, true);
		ap.setAttribute("lineShader.diffuseColor", Color.green);
		
		SceneGraphComponent axes = TubeFactory.getXYZAxes();
		MatrixBuilder.elliptic().scale(.1).assignTo(axes);
		inSceneCameraContainer.addChildren(axes, urTube[0]);
		inSceneCamera.addChild(inSceneCameraContainer);
		double[][] segment = {P3.originP3, {Math.sin(Math.PI/4),0,0,Math.cos(Math.PI/4)}};
		IndexedLineSet ils = IndexedLineSetUtility.createCurveFromPoints(segment, false);
		SceneGraphComponent segSGC = new SceneGraphComponent("seg");
		segSGC.setGeometry(ils);
		inSceneCamera.addChild(segSGC);
		
		theUniverse2.addChildren(inSceneFlight, the24cell, cores);
		mirrorSGC.setVisible(mirror);
		theUniverse.addChildren(theUniverse2,  colorSphere, mirrorSGC);
		
		MatrixBuilder.elliptic().
			translate(0,0,-Math.PI/10).
//			rotateY(Math.PI/2).
			rotateX(-Math.PI/2).
			assignTo(theUniverse);
		theUniverse.addTool(new RotateTool());
		return theUniverse;
	}
	
	private void updateVisibility() {
		colorSphere.setVisible(showHemisphere);
		theUniverse2.setVisible(!showHemisphere);
		theUniverse2.getChildComponent(1).setVisible(showXYZ);
		theUniverse2.getChildComponent(2).setVisible(showXYZ);
	}

	private void initWorld() {
		colors.clear();
		SceneGraphUtility.removeChildren(theWorld);
		
		// create lists of different levels of resolution
		for (int i = 0; i < levels.length; ++i) {
			SceneGraphComponent tmp = hopfFibration(i);
			theWorld.addChild(tmp);
			levels[i][0] = tmp.getChildComponent(0);
			System.err.println("Adding " + tmp.getName() + levels[i][0].getName());
			levels[i][1] = tmp.getChildComponent(1);
			System.err.println("Adding " + tmp.getName() + levels[i][1].getName());
		}
		// initialize the two cores
		theWorld.addChild(cores);
		new Matrix().assignTo(coreV);
		new Matrix(
				leftCliffordTlateFor(null, P3.originP3, new double[]{1,0,0,0})).assignTo(coreH);
		updateTubes();
		updateSceneGraph();
		updateColors();
	}

	private void updateCores() {
		IndexedFaceSet tube = constructTubeWithRadius(
				numSegs, profileSize, globalRadius * coreRadius);
		coreV.setGeometry(tube);
		coreH.setGeometry(tube);
		Appearance ap = cores.getAppearance();
		ap.setAttribute("polygonShader.diffuseColor", coreColor);
	}

	private void updateTubes() {
		double[][] curveV = new double[numSegs][];
		final IndexedLineSet curve = constructCurve(curveV, numSegs);
		final IndexedFaceSet[] tubes = new IndexedFaceSet[numLevels];
		// set up the basic tubes
		for (int i = 0; i < levels.length; ++i) {
			tubes[i] = constructTubeWithRadius(curveV, profileSize, radii[i]);
		}
		Scene.executeWriter(theUniverse, new Runnable() {
					@Override
					public void run() {
						urCurve.setGeometry(curve);
						for (int i = 0; i < levels.length; ++i) {
							urTube[i].setGeometry(tubes[i]);
						}
					}
			});
		updateCores();
	}

	protected void updateSceneGraph() {
		for (int i = 0; i < levels.length; ++i) {
			if (showLevel[i]) {
				levels[i][0].setVisible(!showTubes);
				levels[i][1].setVisible(showTubes); //(showXYZ);
			} else {
				levels[i][0].setVisible(false);
				levels[i][1].setVisible(false);
			}
		}
		for (int i = 0; i<showLevel.length; ++i)	{
			if (showLevel[i]) maxLevel = i;
		}
		for (int i = 0; i < showLevel.length; ++i) {
			colorSphereKids[i].setVisible(i == maxLevel);
		}

	}


	/**
	 * @param theWorld
	 * @return
	 */
	private SceneGraphComponent hopfFibration(int level) {
		if (level < 0)
			level = 0;
		if (level > 4)
			level = 4;
		double[][] verts;
		
		if (concentricHyps) 
			verts = getConcentricHyps(level, thetaPhiRatio);
		else {
			IndexedFaceSet ico = SphereUtility.tessellatedIcosahedronSphere(level, true);
			verts = ico.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		}
		
		Color[]	vc = new Color[verts.length];
		int numVerts = verts.length;

		System.err.println("level = "+level+" size = "+numVerts);
		SceneGraphComponent node = SceneGraphUtility.createFullSceneGraphComponent("Hopf fibration level " + level);
		SceneGraphComponent curves = SceneGraphUtility.createFullSceneGraphComponent("Curves");
		SceneGraphComponent tubes =  SceneGraphUtility.createFullSceneGraphComponent("Tubes");
		tubes.getAppearance().setAttribute("lineShader.diffuseColor", Color.black);
		node.addChildren(curves, tubes);

		for (int i = 0; i < numVerts; ++i) {
			// find phi and then halve it
			double xy = Math.sqrt(verts[i][0] * verts[i][0] + verts[i][1] * verts[i][1]);
			double phi = Math.atan2(verts[i][2], xy);
			phi = (phi + Math.PI/2)/2;  // halve it
			double x = Math.cos(phi) * verts[i][0] / xy;
			double y = Math.cos(phi) * verts[i][1] / xy;
			double z = Math.sin(phi);
			double[] pq = new double[]{x, y, z, 0};
			double[] clifT = leftCliffordTlateFor(null, P3.originP3, pq);
			Matrix clifiso = new Matrix(clifT);

			Color c  = getColorForPoint(verts[i], phi);
			vc[i] = c;
//			if (level < 2 && showLevel[level]) 
//				System.err.println("Vertex "+i+" is "+q.toString()+"\ttheta = "+360.0*theta/(2*Math.PI)+" rgb = "+c.toString());
			
			SceneGraphComponent coreSGC = new SceneGraphComponent("curve "+i);
			coreSGC.setAppearance(new Appearance());
			coreSGC.getAppearance().setName("coreSGC"+i);
			coreSGC.addChild(urCurve);
			curves.addChild(coreSGC);
			colors.put(coreSGC.getAppearance(), c);

			SceneGraphComponent tubeSGC = new SceneGraphComponent("tube "+i);
			tubeSGC.setAppearance(new Appearance());
			tubeSGC.getAppearance().setName("tubeSGC"+colors.size());
			colors.put(tubeSGC.getAppearance(), c);
			tubeSGC.addChild(urTube[level]);
			tubes.addChild(tubeSGC);
			
			clifiso.assignTo(coreSGC);
			clifiso.assignTo(tubeSGC);
		}
		PointSetFactory psf = new PointSetFactory();
		psf.setVertexCount(verts.length);
		psf.setVertexCoordinates(verts);
		psf.setVertexColors(vc);
		psf.update();
		colorSphereKids[level].setGeometry(psf.getGeometry());

		return node;
	}

	private IndexedLineSet constructCurve(double[][] curveV, int numSegs) {
		curveV = constructCurveVertices(curveV, numSegs);
		IndexedLineSet dsc = IndexedLineSetUtility.createCurveFromPoints(curveV, false);
		return dsc;
	}

	private static double[][] constructCurveVertices(double[][] curveV, int numSegs) {
		if (curveV == null) curveV = new double[numSegs][];
		double totalAngle = Math.PI * 2;
		double da = totalAngle / (numSegs - 1.0);
		for (int j = 0; j < numSegs; ++j) {
			double angle = (j) * da;
			curveV[j] = new double[]{0.0, 0.0, Math.sin(angle), Math.cos(angle)};
		}
		return curveV;
	}

	private static IndexedFaceSet constructTubeWithRadius(
			int ns, 
			int ps, 
			double radius) {
		double[][] cv = constructCurveVertices(null, ns);
		return constructTubeWithRadius(cv, ps, radius);
	}

	private static IndexedFaceSet constructTubeWithRadius(
			double[][] curveV, 
			int profileSize,
			double radius) { 
		double[][][] tubeV = new double[curveV.length][profileSize+1][];
		double scale = radius,
				cs = Math.sin(scale); //scale;
		for (int j = 0; j < curveV.length; ++j) {
			double[] clifTr = rightCliffordTlateFor(null, P3.originP3, curveV[j] );
			for (int k = 0; k <= profileSize; ++k) {
				double a = k * 2*Math.PI / (profileSize);
				double[] smallCircleYZ = new double[]{ 
						Math.cos(a) *  cs,
						Math.sin(a) *  cs, 
						0.0,
						1.0};
				tubeV[j][k] = Rn.matrixTimesVector(null, clifTr, smallCircleYZ);
			}
		}
		QuadMeshFactory qmf = new QuadMeshFactory();
		qmf.setMetric(Pn.ELLIPTIC);
		qmf.setULineCount(tubeV[0].length);
		qmf.setVLineCount(tubeV.length);
		qmf.setClosedInUDirection(false);
		qmf.setClosedInVDirection(false);
		qmf.setVertexCoordinates(tubeV);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateVertexNormals(true);
		qmf.update();
		IndexedFaceSet qms = qmf.getIndexedFaceSet();
		return qms;
	}
	
	private static double[] leftCliffordTlateFor(double[] dst, double[] P, double[] Q) {
		double dist = Pn.distanceBetween(P, Q, Pn.ELLIPTIC);
		return P3.makeScrewMotionMatrix(dst, P, Q, dist, Pn.ELLIPTIC);
	}
	private static double[] rightCliffordTlateFor(double[] dst, double[] P, double[] Q) {
		double dist = Pn.distanceBetween(P, Q, Pn.ELLIPTIC);
		return P3.makeScrewMotionMatrix(dst, P, Q, -dist, Pn.ELLIPTIC);
	}

	private static double[] leftCliffordTlateFor(double[] dst, double[] P) {
		double xy = Math.sqrt(P[0] * P[0] + P[1] * P[1]);
		double phi = Math.atan2(P[2], xy);
		phi = (phi + Math.PI/2)/2;
		double x = Math.cos(phi) * P[0] / xy;
		double y = Math.cos(phi) * P[1] / xy;
		double z = Math.sin(phi);
		double[] pq = new double[]{x, y, z, 0};
		double[] clifT = leftCliffordTlateFor(null, P3.originP3, pq);
		return clifT;
	}
	
	private Color getColorForPoint(double[] verts, double phi) {
		Color c;
		if (zColoring){
			double r = 2*phi/Math.PI;
			c = getColorForParameter(r);
//			System.err.println("r = "+r);
		} else {
			float[] fc = new float[3];
			for (int i = 0; i<3; ++i)	{
				fc[i] = (float) (.5 + .5 * verts[i]);
				if (fc[i] > 1f) fc[i] = 1.0f;
			}			
			c = new Color(fc[0], fc[1], fc[2]);
		}
		return c;
	}

	static int[] numHyps = {3, 5, 10, 20, 40}; //{5, 8, 13, 21, 34};
	private static double[][] getConcentricHyps(int level, double tpr) {
		int num = numHyps[level];
		double dphi = Math.PI/(num-1.0);
		int total = 0;
		int[] numI = new int[num];
		for (int i = 0; i<num; ++i)	{
			double phi = dphi*i - Math.PI/2;
			double c = Math.cos(phi);
			numI[i] = (int) (tpr * c * 2.0 * num + 1);
			total += numI[i];
//			System.err.println(i+" # verts = "+numI[i]);
		}
//		System.err.println("# verts = "+total);
		double[][] hverts = new double[total][];
		int count = 0;
		for (int i = 0; i<num; ++i)	{
			double phi = dphi*i - Math.PI/2;
			double sp = Math.sin(phi),
					cp = Math.cos(phi);
			for (int j = 0; j<numI[i]; ++j)  {
				double theta = 2 * Math.PI * (j/(numI[i]*1.0));
				hverts[count] = new double[]
					   {cp * Math.cos(theta), 
						cp * Math.sin(theta), 
						sp, 1};
				count++;
			}
		}
//		if (level <= 1) 
//			System.err.println("hverts =\n"+Rn.toString(hverts));
	return hverts;
}

	public void update24Cell()	{
		MatrixBuilder.euclidean().scale(octaScale).assignTo(octaDD);
		Appearance ap = octaDD.getAppearance();
//		ap.setAttribute(LINE_SHADER+"."+POLYGON_SHADER+"."+DIFFUSE_COLOR, new Color(255,200,200));
			DefaultGeometryShader dgs = (DefaultGeometryShader) 
		   			ShaderUtility.createDefaultGeometryShader(ap, true);
			ImplodePolygonShader dps = (ImplodePolygonShader) dgs.createPolygonShader("implode");
			ap.setAttribute("polygonShader.implodeFactor", implodeFactor);
	}
	
	public void updateColors()	{
		Enumeration<Appearance> aps = colors.keys();
//		System.err.println("table # = "+colors.size());
		while (aps.hasMoreElements()) {
			Appearance ap = aps.nextElement();
			String name = ap.getName();
			boolean isTube = name.contains("tube");
			Color c = colors.get(ap);
			c = applyGamma(c, gamma);
			if (!isTube) ap.setAttribute("lineShader.diffuseColor", showXYZ ? Appearance.INHERITED : c);
//			ap.setAttribute("lineShader.polygonShader.diffuseColor", showXYZ ? Appearance.INHERITED : c);
			if (isTube) ap.setAttribute("polygonShader.diffuseColor", showXYZ ? Appearance.INHERITED : c);
		}
	}

	Transformation avatarT, camT;
	boolean rotating = false;
	@Override
	public void display() {
		super.display();
		FileLoaderDialog.setLastDir(new File("/Users/gunn/Pictures/fromJReality/hopfFibration/"));
		viewer.getSceneRoot().getAppearance().setAttribute(BACKGROUND_COLOR, Color.black);
		viewer.getSceneRoot().getAppearance().setAttribute(METRIC, Pn.ELLIPTIC);

		// following seems to do more harm than good
		animationPlugin.setAnimateCamera(true);
		animationPlugin.setAnimateSceneGraph(true);
		animationPlugin.getAnimationPanel().setResourceDir("src/charlesgunn/jreality/worlds/projective/");
		animationPlugin.getAnimationPanel().getRecordPrefs().setCurrentDirectoryPath("/gunn_local/Movies/hopfFibration/");
//		viewer.getSceneRoot().getAppearance().setAttribute(RENDER_S3, true);
		Camera cam = CameraUtility.getCamera(viewer);
		cam.setNear(.02);
		cam.setFar(-.05);
		inSceneCamSGP = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), inSceneCamera).get(0);
		inSceneCamera.setCamera(cam);
		inSceneCamera.getAppearance().setAttribute(SceneGraphAnimator.LOCAL_ANIMATED, false);
		inSceneCamSGP.push(cam);
		standardCamSGP = viewer.getCameraPath();
		
		camT = CameraUtility.getCameraNode(viewer).getTransformation();
//		PointLight pl = new PointLight();
//		pl.setIntensity(.5);
//		pl.setColor(new Color(255,200,200));
//		CameraUtility.getCameraNode(viewer).setLight(pl);
		de.jreality.plugin.basic.Scene scene = 
				jrviewer.getPlugin(de.jreality.plugin.basic.Scene.class);
		avatarPath = scene.getAvatarPath();
		avatarT = avatarPath.getLastComponent().getTransformation();
		flytool = new FlyTool();
		flytool.setGain(.1);
		avatarPath.getLastComponent().addTool(flytool);
		updateInSceneCamera();
		
	
		((Component) viewer.getViewingComponent()).addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e)	{ 
				switch(e.getKeyCode())	{
					
				case KeyEvent.VK_H:
					System.out.println("	1: toggle clifford translate");
					System.out.println("	2: toggle in scene camera flight");
					System.out.println("	3: toggle vertex draw");
//					System.out.println("	4: toggle tool");
//					System.out.println("	5: toggle labels");
//					System.out.println("	6: reset content tform");
//					System.out.println("	7: reset tlate/rotate tforms");
//					System.out.println("	7: toggle x-rotate");
					break;
	
				case KeyEvent.VK_1:
					rotating = !rotating;
					if (rotating) rotate.start();
					else rotate.stop();
					break;
					
				case KeyEvent.VK_2:
					isInSceneFlight = !isInSceneFlight;
					if (isInSceneFlight) inSceneFlightTimer.start();
					else inSceneFlightTimer.stop();
					break;
				case KeyEvent.VK_3:
					break;
				}
			}
		
		
		});

		cliffordTlate = rightCliffordTlateFor(
				null, P3.originP3, new double[]{0, 0, Math.sin(dangle1), Math.cos(dangle1)});
		rotate = new Timer(20, new ActionListener()	{
			public void actionPerformed(ActionEvent e) {
				System.err.println("rotate timer action");
				theUniverse2.getTransformation().multiplyOnRight(cliffordTlate); //zRotateM);
				viewer.renderAsync();
			}
			
		});

//		final double[] insceneflighttform = P3.makeTranslationMatrix(
//				null, P3.originP3, new double[]{0, 0, Math.sin(dangle2), Math.cos(dangle2)}, Pn.ELLIPTIC),
		inSceneFlightTform = Rn.inverse(null, rightCliffordTlateFor(
				null, P3.originP3, new double[]{0, 0, Math.sin(dangle1), Math.cos(dangle1)}));


		inSceneFlightTimer = new Timer(30, new ActionListener()	{
			public void actionPerformed(ActionEvent e) {
				inSceneCamera.getTransformation().multiplyOnRight(inSceneFlightTform); //zRotateM);
				viewer.renderAsync();
			}
			
		});

		
//		SceneGraphUtility.removeLights(viewer);
//		viewer.getSceneRoot().addChild(makeLights(.15));

	}
	
	protected void updateInSceneCamera() {
		// set up the position and orientation of the in scene camera
		positionCamera = 
					leftCliffordTlateFor(null, 
					P3.originP3, 
					new double[]{Math.sin(cameraAngle),0,0,Math.cos(cameraAngle)});
		new Matrix(positionCamera).assignTo(inSceneCamera);
		System.err.println("position camera = \n"+Rn.matrixToString(positionCamera, "%8.4f"));
		if (isInSceneFlight) {
			inSceneFlight.addTool(flytool);
			avatarPath.getLastComponent().removeTool(flytool);			
		} else {
			inSceneFlight.removeTool(flytool);
			avatarPath.getLastComponent().addTool(flytool);			
		}
		inSceneCameraContainer.setVisible(showInSceneGeom && !isInSceneFlight);
		viewer.setCameraPath( isInSceneFlight ? inSceneCamSGP : standardCamSGP);
	}

	@Override
	public void setValueAtTime(double d) {
		super.setValueAtTime(d);
		if (isInSceneFlight)	{
			System.err.println("writing iscam");
			inSceneCamera.getTransformation().multiplyOnRight(inSceneFlightTform); //zRotateM);			
		}
		else theUniverse2.getTransformation().multiplyOnRight(cliffordTlate); //zRotateM);
	}

	public SceneGraphComponent makeLights(double strength) {
		SceneGraphComponent lightNode = new SceneGraphComponent();
		SceneGraphComponent lightNode1 = new SceneGraphComponent();
		SceneGraphComponent lightNode2 = new SceneGraphComponent();
		lightNode.setName("lights");
		double[][] lightlocations = {
				{1,1,1,1},
				{1,-1,-1,1},
				{-1,-1,1,1},
				{-1,1,-1,1},
				{1,1,1,-1},
				{1,-1,-1,-1},
				{-1,-1,1,-1},
				{-1,1,-1,-1},
		};
		double[][] mats = new double[lightlocations.length][];
		mats[0] = P3.makeRotationMatrix(null, new double[]{0,0,1}, new double[]{1,1,1});
		mats[1] = P3.makeRotationMatrixX(null, Math.PI);
		mats[2] = P3.makeRotationMatrixY(null, Math.PI);
		mats[3] = P3.makeRotationMatrixZ(null, Math.PI);
		
		for( int i = 0; i < lightlocations.length; ++i )	{
			if (i > 0) mats[i] = Rn.times(null, mats[i%4], mats[0]);
//			double[] axis = {-1.2,-1.6,-2,1};
//			if( i > 0 )
//				axis[i-1] = 1; 
//			else 
//				axis = new double[]{1.8,1.3,1,1};
			SceneGraphComponent l0 = SceneGraphUtility.createFullSceneGraphComponent("light0");
			DirectionalLight pointLight = new DirectionalLight();
			int c[] = {255, 255, 255,255};
			c[i%4] = 200;
			pointLight.setColor(new Color(c[0], c[1], c[2]));
			pointLight.setIntensity(strength);
			l0.getTransformation().setMatrix( P3.makeTranslationMatrix(null, lightlocations[i], Pn.EUCLIDEAN));		
			l0.setLight(pointLight);
			lightNode1.addChild(l0);
			//			dl.setFalloff(falloffs[metric+1]);
		}
		lightNode2.addChild(lightNode1);
		lightNode2.setTransformation(new Transformation(
				Rn.times(null, -1, Rn.identityMatrix(4))));
		
		lightNode.addChildren(lightNode1, lightNode2);
		return lightNode;
	}



	@Override
	public Component getInspector() {
		Box hbox = Box.createHorizontalBox();
		inspector.add(hbox);
		JButton dhB = new JButton("reset camera");
		dhB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				inSceneFlight.getTransformation().setMatrix(Rn.identityMatrix(4));
				updateInSceneCamera();
				camT.setMatrix(Rn.identityMatrix(4));
				avatarT.setMatrix(Rn.identityMatrix(4));
			}
		});
		hbox.add(dhB);
		
		final JCheckBox chB = new JCheckBox("Concentric Hyps");
		chB.setSelected(concentricHyps);
		hbox.add(chB);
		chB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				concentricHyps = chB.isSelected();
				initWorld();
			}
		});
		
		final JCheckBox zcolB = new JCheckBox("Color by z");
		zcolB.setSelected(zColoring);
		hbox.add(zcolB);
		zcolB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				zColoring = zcolB.isSelected();
				initWorld();
				// initWorld();
			}
		});
		
		final JCheckBox mB = new JCheckBox("Mirror");
		mB.setSelected(mirror);
		hbox.add(mB);
		mB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mirror = mB.isSelected();
				mirrorSGC.setVisible(mirror);
			}
		});
		
		hbox = Box.createHorizontalBox();
		inspector.add(hbox);
		
		final JCheckBox hsB = new JCheckBox("Show hemisphere");
		hsB.setSelected(showHemisphere);
		hbox.add(hsB);
		hsB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showHemisphere = hsB.isSelected();
				colorSphere.setVisible(showHemisphere);
				theUniverse2.setVisible(!showHemisphere);
			}
		});

		final JCheckBox stB = new JCheckBox("Show tubes");
		stB.setSelected(showTubes);
		hbox.add(stB);
		stB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showTubes = stB.isSelected();
				System.out.println("Show tubes is " + showTubes);
				updateSceneGraph();
				viewer.renderAsync();
			}
		});
	    
		final JCheckBox jcperp = new JCheckBox("Show xyz");
		jcperp.setSelected(showXYZ);
		hbox.add(jcperp);
		jcperp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showXYZ = jcperp.isSelected();
				System.out.println("Show xyz is " + showXYZ);
//				updateSceneGraph();
				updateVisibility();
				updateColors();
			}
		});
		
		hbox = Box.createHorizontalBox();
		inspector.add(hbox);
		final JCheckBox iscCB = new JCheckBox("In-scene cam");
		iscCB.setSelected(isInSceneFlight);
		hbox.add(iscCB);
		iscCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isInSceneFlight = iscCB.isSelected();
				System.out.println("In-scene cam is " + isInSceneFlight);
				updateInSceneCamera();
			}
		});
		
		final JCheckBox showCCB = new JCheckBox("Show cam geom");
		showCCB.setSelected(showInSceneGeom);
		hbox.add(showCCB);
		showCCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showInSceneGeom = showCCB.isSelected();
				updateInSceneCamera();
			}
		});
		
		final JCheckBox s24CCB = new JCheckBox("Show 24-cell");
		s24CCB.setSelected(show24Cell);
		hbox.add(s24CCB);
		s24CCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				show24Cell = s24CCB.isSelected();
				the24cell.setVisible(show24Cell);
			}
		});
		

		hbox = Box.createHorizontalBox();
		inspector.add(hbox);
		JLabel shL = new JLabel("Levels: ");
		hbox.add(shL);

		for (int i = 0; i < showLevel.length; ++i) {
			final JCheckBox jcx = new JCheckBox("level " + i);
			jcx.setSelected(showLevel[i]);
			hbox.add(jcx);
			final int j = i;
			jcx.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showLevel[j] = jcx.isSelected();
					updateSceneGraph();
					viewer.renderAsync();
				}
			});
		}
		
		
		TextSlider theSlider = new TextSlider.Double("tube radius", SwingConstants.HORIZONTAL, 0.0, .1, globalRadius);
		theSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				globalRadius = ((TextSlider) e.getSource()).getValue().doubleValue();
				radii[0] = globalRadius;
				for (int i = 1; i < radii.length; ++i) {
					radii[i] = radii[i - 1] * .5;
				}
				updateTubes();
			}
		});
		inspector.add(theSlider);
		TextSlider tpSlider = new TextSlider.Double("theta:phi", SwingConstants.HORIZONTAL, 0.0, 3.0, thetaPhiRatio);
		tpSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double dval = ((TextSlider) e.getSource()).getValue().doubleValue();
				if (dval != thetaPhiRatio)  {
					thetaPhiRatio = dval; 
					initWorld();
				}
			}
		});
		inspector.add(tpSlider);
		
		
		TextSlider gSlider = new TextSlider.Double("gamma", SwingConstants.HORIZONTAL, 0.0, 3.0, gamma);
		gSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gamma = ((TextSlider) e.getSource()).getValue().doubleValue();
				updateColors();
			}
		});
		inspector.add(gSlider);
		
		
		TextSlider osSlider = new TextSlider.Double("octa scale", SwingConstants.HORIZONTAL, 0.0, 3.0, octaScale);
		osSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				octaScale = ((TextSlider) e.getSource()).getValue().doubleValue();
				update24Cell();
			}
		});
		inspector.add(osSlider);
		
		final TextSlider aSlider = new TextSlider.Double("implode",  SwingConstants.HORIZONTAL,
				-1, 1, implodeFactor);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				implodeFactor = aSlider.getValue().doubleValue();
				update24Cell();

		}
		});
		inspector.add(aSlider);

		TextSlider caSlider = new TextSlider.Double("camera angle", SwingConstants.HORIZONTAL, -Math.PI, Math.PI, cameraAngle);
		caSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cameraAngle = ((TextSlider) e.getSource()).getValue().doubleValue();
				updateInSceneCamera();
			}
		});
		inspector.add(caSlider);
		
		TextSlider daSlider = new TextSlider.Double("delta angle", SwingConstants.HORIZONTAL, 0.0, 0.1, dangle1);
		daSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dangle1 = ((TextSlider) e.getSource()).getValue().doubleValue();
			}
		});
		inspector.add(daSlider);
		
		TextSlider psSlider = new TextSlider.Integer("profile size", SwingConstants.HORIZONTAL, 2, 32, profileSize);
		psSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				profileSize = ((TextSlider) e.getSource()).getValue().intValue();
				updateTubes();
			}
		});
		inspector.add(psSlider);
		
		Box vbox = Box.createVerticalBox();
		inspector.add(vbox);
		hbox = Box.createHorizontalBox();
		vbox.add(hbox);
		vbox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder(BorderFactory
						.createEtchedBorder(), "Cores")));
		
		final JCheckBox coreB = new JCheckBox("show cores");
		coreB.setSelected(bigCore);
		hbox.add(coreB);
		coreB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bigCore = coreB.isSelected();
				cores.setVisible(bigCore);
				// initWorld();
			}
		});
		
		final JButton coreCB = new JButton("color");
		coreCB.setBackground(coreColor);
		coreCB.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				coreColor = JColorChooser.showDialog((Component) viewer.getViewingComponent(), "Select color ",  null);
				coreCB.setBackground(coreColor);
				updateCores();
			}
		});
		hbox.add(coreCB);

		hbox = Box.createHorizontalBox();
		vbox.add(hbox);
		TextSlider cSlider = new TextSlider.Double("core size", SwingConstants.HORIZONTAL, 1.0, 4.0, coreRadius);
		cSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				coreRadius = ((TextSlider) e.getSource()).getValue().doubleValue();
				updateCores();
			}
		});
		hbox.add(cSlider);


		return inspector;
	}

	public static void main(String[] args) {
		new HopfFibrationAssg().display();
	}
	
	double[] dkeyframes, acc;
	int k = 200, m = 255, n = 60;
	Color[] values = {
			new Color(n, k, k),
			new Color(n, m, n),
			new Color(k, k, n),
			new Color(m, n, n), 
			new Color(k, n, k),
			new Color(n, n, m),
			new Color(n, k, k)
//			Color.red,
//			Color.magenta,
//			Color.blue,
//			Color.green,
//			Color.yellow,
//			Color.orange,
//			Color.red
	};
	private FlyTool flytool;
	private SceneGraphPath avatarPath;
	protected void initKeyFrames() {
//		dkeyframes = new double[]{.1,.1,.1,.1,.1,.1};
		acc = new double[values.length];
		for (int i = 0; i<values.length; ++i)	{
			acc[i] = i /(values.length-1.0);
		}
		System.err.println("acc = "+Rn.toString(acc));
	}
	
	public Color getColorForParameter(double d) {
		Color c;
		if (acc == null) initKeyFrames();
		for (int i = 0; i<acc.length-1; ++i)	{
			if (d >= acc[i] && d <= acc[i+1]) {
				c = AnimationUtility.linearInterpolation(
						d, acc[i], acc[i+1], values[i],values[i+1]);
				return c;
			} 
		};
		return values[acc.length-1];
	}

	private static Color applyGamma(Color c, double gamma) {
		float[] tmp = new float[4];
		float[] cmp = c.getComponents(tmp);
		for (int j = 0; j<3; ++j)	{
			cmp[j] = (float) (Math.pow(((double) cmp[j]), 1.0/gamma));
		}
		c = new Color(cmp[0], cmp[1], cmp[2]);
		return c;
	}

}
