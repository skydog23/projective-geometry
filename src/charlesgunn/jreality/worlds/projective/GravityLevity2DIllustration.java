/*
 * Created on Dec 1, 2010
 *
 */
package charlesgunn.jreality.worlds.projective;

import static de.jreality.geometry.GeometryUtility.BOUNDING_BOX;
import static de.jreality.shader.CommonAttributes.ATTENUATE_POINT_SIZE;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LIGHTING_ENABLED;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.LINE_WIDTH;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;

import javax.swing.JMenuBar;

import charlesgunn.anim.util.AnimationUtility.InterpolationTypes;
import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.geometry.projective.DualizeSceneGraph;
import charlesgunn.jreality.geometry.projective.LinePencilFactory;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.tools.TranslateShapeTool;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import charlesgunn.math.p5.PlueckerLineGeometry;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class GravityLevity2DIllustration extends Assignment {

	private static final double K1 = Math.sqrt(3)/2.0;
	private double[][] 
	           vertices = {{0,1,0,1}, {-K1,-.5, 0,1}, {K1,-.5,0,1}};
	int numLines = 10;
	private SceneGraphComponent world,
		starSGC,
		eucSGC,
			faceSGC,
			dotsSGC,
			centerSGC,
			fallingSGC,
		polarSGC;
//	GeometryCollector gc = new GeometryCollector(numPoints), polarGC = new GeometryCollector(numPoints);
	double fanRadius = .35,
		pictureSize = 4.0;
	int numSides = 6, numPlops = 6;
	double[][] randomPoints;
	int numRandomPoints = 100;
	Color[] colors1 = {Color.red, Color.yellow, Color.blue, Color.green, Color.magenta, Color.cyan};
	Color[] colors2 = {Color.green, Color.magenta, Color.cyan, Color.red, Color.yellow, Color.blue};
	double[] oldcc = null;
	private double[][] polygonSides, lines;
	LinePencilFactory polarBFanFact[];
	PointRangeFactory polarBLineFact[], polarDotsFact[];
	boolean euclideanFans = true;
	double xshift = 0; //.1;
	@Override
	public SceneGraphComponent getContent() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		eucSGC = SceneGraphUtility.createFullSceneGraphComponent("euc");
		faceSGC = SceneGraphUtility.createFullSceneGraphComponent("face");
		dotsSGC = SceneGraphUtility.createFullSceneGraphComponent("dots");
		fallingSGC = SceneGraphUtility.createFullSceneGraphComponent("falling");
		centerSGC = SceneGraphUtility.createFullSceneGraphComponent("center");
		starSGC = SceneGraphUtility.createFullSceneGraphComponent("star");
		
		eucSGC.addChildren(faceSGC, dotsSGC);
		Appearance ap = world.getAppearance();
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, new Color(255,255,255,0));
		ap.setAttribute("lineShader.lineWidth", 1.7);
		ap.setAttribute(LIGHTING_ENABLED, false);
		ap.setAttribute(TUBES_DRAW, false);
		world.addChildren(eucSGC); //, polarSGC, polarSGC2);
		
		ap = faceSGC.getAppearance();
		ap.setAttribute(FACE_DRAW, true);
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, new Color(255,255,255,0));
		ap.setAttribute(SMOOTH_SHADING, false);
		ap.setAttribute(EDGE_DRAW, false);
		ap.setAttribute(VERTEX_DRAW, false);
//		ap.setAttribute(DualizeSceneGraph.DUALIZE, false);
		IndexedFaceSetFactory regularPolygonFactory = Primitives.regularPolygonFactory(numSides, .0); //IndexedFaceSetUtility.constructPolygon(triangle);
		// funny bug with coloring; when edge and vertex colors are set in the factory, 
		// the jogl backend doesn't read the color for the face from the appearance, so I set it here ...
		regularPolygonFactory.setFaceColors(new double[][]{{1,1,1,0}});
		regularPolygonFactory.update();
		faceSGC.setGeometry(regularPolygonFactory.getIndexedFaceSet());

		IndexedFaceSet ifs = (IndexedFaceSet) faceSGC.getGeometry();
		vertices = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		Matrix m = new Matrix(MatrixBuilder.euclidean().scale(.9).translate(xshift,0,0).getArray());
		Rn.matrixTimesVector(vertices, m.getArray(), vertices);
		regularPolygonFactory.setVertexCoordinates( vertices);
		regularPolygonFactory.update();
		if (vertices[0].length == 3) vertices = Pn.homogenize(null, vertices);

		randomPoints = new double[numRandomPoints][4];
		for (int i = 0; i<numRandomPoints; ++i)	{
			double angle = 2*Math.PI* ((double)i)/(numRandomPoints-1.0); //Math.random();
			double factor = Math.random();
			factor = Math.pow(factor, .125);
			factor = 1.0;
			randomPoints[i] = new double[]{Math.cos(angle)*factor + xshift, Math.sin(angle)*factor,0,1};
		}
		PointSetFactory psf1 = new PointSetFactory();
		psf1.setVertexCount(numRandomPoints);
		psf1.setVertexCoordinates(randomPoints);
		if (dotColors != null) psf1.setVertexColors(dotColors);
		psf1.update();
		dotsSGC.setGeometry(psf1.getPointSet());
		Appearance ap1 = dotsSGC.getAppearance();
		ap1.setAttribute(VERTEX_DRAW, true);
		ap1.setAttribute("pointShader.diffuseColor", dotColor);
		ap1.setAttribute("pointShader.pointRadius", .02);
		//		}
		ap = eucSGC.getAppearance();
		ap.setAttribute(CommonAttributes.METRIC,Pn.EUCLIDEAN);
		ap.setAttribute(BOUNDING_BOX, Rectangle3D.unitCube);
		eucSGC.addTool(new TranslateShapeTool());
		eucSGC.getTransformation().addTransformationListener(new TransformationListener() {
			
			public void transformationMatrixChanged(TransformationEvent ev) {
				double[] mat = eucSGC.getTransformation().getMatrix();
				mat = Rn.transpose(null,Rn.inverse(null, mat));
				new Matrix(mat).assignTo(polarSGC);
			}
		});
		PointSetFactory psf = new PointSetFactory();
		psf.setVertexCount(1);
		psf.setVertexCoordinates(new double[][]{{xshift,0,0,1}});
		psf.update();
		centerSGC.setGeometry(psf.getGeometry());
		ap = centerSGC.getAppearance();
		ap.setAttribute("pointShader.diffuseColor", new Color(128, 0, 255));
		ap.setAttribute(ATTENUATE_POINT_SIZE, false);
		ap.setAttribute("pointShader.pointSize", 12.0);
		ap.setAttribute("pointShader.pointRadius", .02);
//		ap.setAttribute(LINE_SHADER+"."+LINE_WIDTH, 4);
		ap.setAttribute(SPHERES_DRAW, false);
		ap.setAttribute(VERTEX_DRAW, true);
		MatrixBuilder.euclidean().translate(0,0,.01).assignTo(centerSGC);
		eucSGC.addChild(centerSGC);
		eucSGC.addChild(getFalling());
		
		starSGC.setGeometry(GeometryUtilityOverflow.starPoint(16, .015, .05));
		starSGC.setPickable(false);
		ap = starSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", Color.black);
		ap.setAttribute("lineShader.drawTubes", false);
		ap.setAttribute("lineShader.lineWidth", 1.0);
		ap.setAttribute(DualizeSceneGraph.DUALIZE, false);
		MatrixBuilder.euclidean().translate(xshift,0,.01).assignTo(starSGC);
		world.addChild(starSGC);

		polarSGC = DualizeSceneGraph.dualize(new SceneGraphPath(world,eucSGC));
		MatrixBuilder.euclidean().rotateZ(Math.PI/2).assignTo(polarSGC);
		ap = polarSGC.getAppearance();
		ap.setAttribute(CommonAttributes.METRIC,Pn.ELLIPTIC);
		ap.setAttribute(BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		ap.setAttribute("lineShader.lineWidth", 1.0);
		world.addChildren(polarSGC); //

		return world;
	}
	
	double height = 1.5, dt = .3;
	private SceneGraphComponent getFalling() {
		Appearance ap = fallingSGC.getAppearance();
		ap.setAttribute(POINT_SHADER+"."+DIFFUSE_COLOR, Color.RED);
		ap.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, Color.BLUE);
		ap.setAttribute(DualizeSceneGraph.DO_FANS, false);
		double[] p1 = {xshift, 0,0,1}, p2 ={xshift,height,0,1};
		
		double[] fallLine = PlueckerLineGeometry.lineFromPoints(null, p1, p2);
		double[][] pts = {p2, Rn.add(null, p2, Rn.times(null, dt, 
					Rn.subtract(null, p2, p1)))};
		PointSetFactory psf = new PointSetFactory();
		psf.setVertexCount(2);
		psf.setVertexCoordinates(pts);
		psf.update();
//		IndexedLineSet fallSeg = IndexedLineSetUtility.createCurveFromPoints(pts, false);
		SceneGraphComponent sgc =SceneGraphUtility.createFullSceneGraphComponent("child");
		sgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		sgc.setGeometry(psf.getGeometry());
		fallingSGC.addChildren(sgc);
		
		PointRangeFactory prf = new PointRangeFactory();
		prf.setPluckerLine(fallLine);
		prf.setFiniteSphere(false);
		prf.update();
		sgc =SceneGraphUtility.createFullSceneGraphComponent("child2");
		sgc.getAppearance().setAttribute(DualizeSceneGraph.DUALIZE_POINTS, false);
		sgc.setGeometry(prf.getLine());
		fallingSGC.getAppearance().setAttribute(DualizeSceneGraph.DO_FANS, false);
		fallingSGC.addChildren(sgc);
		return fallingSGC;
	}
	private void darken(Color[] c) {
		for (int i = 0; i< c.length; ++i)	{
			float[] rgba = c[i].getRGBComponents(null);
			for (int j = 0;j<3;++j)	{
				rgba[j] *= .75;
			}
			c[i] = new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
		}
	}
	int valo = 50;
	Color dotColor = new Color(valo, valo, valo);
	double blandFactor = .2;
	boolean doRandom = true;
	Color[] dotColors;
	private SceneGraphComponent dualize;
	@Override
	public void display() {
		super.display();
		Viewer v = jrviewer.getViewer();
		v.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.white);
		animationPlugin.setAnimateCamera(true);
		animationPlugin.setAnimateSceneGraph(true);
		animationPlugin.setDefaultInterp(InterpolationTypes.CUBIC_HERMITE);
		CameraUtility.encompass(v);
		Camera cam = CameraUtility.getCamera(v);
		cam.setFieldOfView(1.25 * cam.getFieldOfView());
		cam.setFar(-.05);
//		cam.setPerspective(false);
	}

	public static void main(String[] args) {
		new GravityLevity2DIllustration().display();
	}
}
