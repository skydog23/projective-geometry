/*
 * Created on Dec 1, 2010
 *
 */
package charlesgunn.jreality.worlds.projective;

import static de.jreality.geometry.GeometryUtility.BOUNDING_BOX;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LIGHTING_ENABLED;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;

import javax.swing.JMenuBar;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.anim.util.AnimationUtility.InterpolationTypes;
import charlesgunn.jreality.GeometryCollector;
import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.geometry.projective.DualizeSceneGraph;
import charlesgunn.jreality.geometry.projective.LinePencilFactory;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.tools.TranslateShapeTool;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P2;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.projgeom.PlueckerLineGeometry;

public class CompletePolygonDemo extends LoadableScene {

	private static final double K1 = Math.sqrt(3)/2.0;
	private double[][] 
	           vertices = {{0,1,0,1}, {-K1,-.5, 0,1}, {K1,-.5,0,1}};
	int numLines = 10;
	private SceneGraphComponent world,
		starSGC,
		eucSGC,
			faceSGC,
			dotsSGC,
		polarSGC,
			bLinesSGC,
			linesSGC,
			polarSGC2;
	int numPoints = 500;
//	PointCollector pc = new PointCollector(numPoints, 4), polarPivot = new PointCollector(numPoints, 4);
	GeometryCollector gc = new GeometryCollector(numPoints), 
			polarGC = new GeometryCollector(numPoints);
//	Biquaternion lastPolarBQ = null;
//	double[] lastPolar = null;
//	// draw an ellipse and its polar
//	double[] oldPt = null, oldpolarline = null;
//	double a = 1.5, b = .75, c = 2.0;
	double fanRadius = .65,
		pictureSize = 4.0;
	int numSides = 6, numPlops = 6;
	double[][] randomPoints;
	int numRandomPoints = 100;
	Color yellow = new Color(255, 255, 0);
	Color[] colors1 = {yellow, Color.green, Color.cyan, Color.blue,  Color.magenta, Color.red};
	Color[] colors2 = {Color.red, yellow, Color.green, Color.cyan, Color.blue,  Color.magenta};//colors1; //{Color.green, Color.magenta, Color.cyan, Color.red, Color.yellow, Color.blue};
	double[] oldcc = null;
	private double[][] polygonSides, lines;
	LinePencilFactory polarBFanFact[];
	PointRangeFactory polarBLineFact[], polarDotsFact[];
	boolean euclideanFans = true;
	@Override
	public SceneGraphComponent makeWorld() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		eucSGC = SceneGraphUtility.createFullSceneGraphComponent("euc");
		faceSGC = SceneGraphUtility.createFullSceneGraphComponent("face");
		dotsSGC = SceneGraphUtility.createFullSceneGraphComponent("dots");
		linesSGC = SceneGraphUtility.createFullSceneGraphComponent("lines");
		polarSGC = SceneGraphUtility.createFullSceneGraphComponent("polar");
		polarSGC2 = SceneGraphUtility.createFullSceneGraphComponent("polar2");
		bLinesSGC = SceneGraphUtility.createFullSceneGraphComponent("boundary line");
		starSGC = SceneGraphUtility.createFullSceneGraphComponent("star");
		polarSGC.addChildren(bLinesSGC, linesSGC, polarGC);
		double[] mat = Rn.diagonalMatrix(null, new double[]{-1,-1,-1,-1});
		new Matrix(mat).assignTo(polarSGC2);
		
		eucSGC.addChildren(faceSGC);
		faceSGC.addChild(dotsSGC);
		Appearance ap = world.getAppearance();
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, new Color(255,255,255,0));
//		ap.setAttribute("lineShader.lineWidth", 1.7);
		ap.setAttribute(LIGHTING_ENABLED, false);
		ap = polarSGC.getAppearance();
		ap.setAttribute(BOUNDING_BOX, Rectangle3D.unitCube);
		ap = world.getAppearance();
		ap.setAttribute(TUBES_DRAW, false);
		world.addChildren(eucSGC); //, polarSGC, polarSGC2);
		
		ap = faceSGC.getAppearance();
		ap.setAttribute(FACE_DRAW, true);
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, new Color(255,255,255,0));
		ap.setAttribute(SMOOTH_SHADING, false);
		ap.setAttribute(EDGE_DRAW, true);
		ap.setAttribute(VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.VERTEX_COLORS_ENABLED, true);
		//ap.setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.unitCube);
		//ap.setAttribute("lineShader.diffuseColor", new Color(0, 200,50));
		//ap.setAttribute(VERTEX_COLORS_ENABLED, false);
		IndexedFaceSetFactory regularPolygonFactory = Primitives.regularPolygonFactory(numSides, .0); //IndexedFaceSetUtility.constructPolygon(triangle);
//		regularPolygonFactory.setGenerateEdgesFromFaces(true);
		double[][] hexverts = regularPolygonFactory.getIndexedFaceSet().getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		lines = new double[hexverts.length][];
		for (int i = 0; i< lines.length; ++i)	{
			lines[i] = P2.lineFromPoints(null, hexverts[i], hexverts[(i+1)%lines.length]);
		}
		regularPolygonFactory.setEdgeCount(numSides);
		int[][] inds = new int[numSides][2];
		for (int i = 0; i<numSides; ++i) {inds[i][0] = i; inds[i][1] = (i+1)%numSides;}
		regularPolygonFactory.setEdgeIndices(inds);
		darken(colors1);
		darken(colors2);
		
		regularPolygonFactory.setEdgeColors(colors1);
		regularPolygonFactory.setVertexColors(colors2);
		// funny bug with coloring; when edge and vertex colors are set in the factory, 
		// the jogl backend doesn't read the color for the face from the appearance, so I set it here ...
		regularPolygonFactory.setFaceColors(new double[][]{{1,1,1,0}});
		regularPolygonFactory.update();
		faceSGC.setGeometry(regularPolygonFactory.getIndexedFaceSet());

		IndexedFaceSet ifs = (IndexedFaceSet) faceSGC.getGeometry();
		vertices = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		Matrix m = new Matrix(MatrixBuilder.euclidean().scale(.9).getArray());
		Rn.matrixTimesVector(vertices, m.getArray(), vertices);
		regularPolygonFactory.setVertexCoordinates( vertices);
		regularPolygonFactory.update();
		if (vertices[0].length == 3) vertices = Pn.homogenize(null, vertices);

//		doBoundary();
		doDots();
		faceSGC.addTool(new TranslateShapeTool());
		faceSGC.getTransformation().addTransformationListener(new TransformationListener() {
			
			public void transformationMatrixChanged(TransformationEvent ev) {
				double[] mat = faceSGC.getTransformation().getMatrix();
				mat = Rn.transpose(null,Rn.inverse(null, mat));
				new Matrix(mat).assignTo(polarSGC);
			}
		});
		starSGC.setGeometry(GeometryUtilityOverflow.starPoint(16, .015, .05));
		ap = starSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", Color.black);
		ap.setAttribute("lineShader.drawTubes", false);
		ap.setAttribute("lineShader.lineWidth", 1.0);
		MatrixBuilder.euclidean().translate(0,0,.01).assignTo(starSGC);
		world.addChild(starSGC);

		eucSGC.getAppearance().setAttribute(DualizeSceneGraph.FAN_RADIUS, .3);
		polarSGC = DualizeSceneGraph.dualize(new SceneGraphPath(world,eucSGC));
		ap = polarSGC.getAppearance();
		ap.setAttribute(BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
//		ap.setAttribute("lineShader.lineWidth", 1.0);
		polarSGC2.addChild(polarSGC);

		world.addChildren(polarSGC2); //

		return world;
	}
	private void darken(Color[] c) {
		for (int i = 0; i< c.length; ++i)	{
			float[] rgba = c[i].getRGBComponents(null);
			for (int j = 0;j<3;++j)	{
				rgba[j] *= .95;
			}
			c[i] = new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
		}
	}
	int valo = 50;
	Color dotColor = new Color(valo, valo, valo);
	double blandFactor = .2;
	boolean doRandom = false;
	Color[] dotColors;
	private SceneGraphComponent dualize;
	private void doDots()	{
		if (doRandom)	{
			randomPoints = new double[numRandomPoints][4];
			
			for (int i = 0; i<numRandomPoints; ++i)	{
				boolean inside = false;
				double[] tmp = null;
				do {
					tmp = new double[]{Math.random()*2-1, Math.random()*2-1, 0, 1};
					inside = true;
					for (int j = 0; j<lines.length; ++j)	{
						if (Rn.innerProduct(tmp, lines[j]) < 0) {
							inside = false; break;
						}
					}
				} while(!inside);
				randomPoints[i] = tmp;
			}
		} else {
			numRandomPoints = 1+numSides*numPlops*(numPlops-1)/2;
			randomPoints = new double[numRandomPoints][4];
			dotColors = new Color[numRandomPoints];
			int count = 0;
			dotColors[count] = AnimationUtility.linearInterpolation(dotColor, Color.white, blandFactor);
			randomPoints[count] = new double[]{0,0,0,1};
			count++;
			IndexedFaceSet ifs = (IndexedFaceSet) faceSGC.getGeometry();
			double[][] theVerts = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
			if (theVerts[0].length == 3) theVerts = Pn.homogenize(null, theVerts);
			for (int i = 0; i<numSides; ++i)	{
				double[] vs = theVerts[i], ve = theVerts[(i+1)%numSides];
				for (int j = 1 ; j<numPlops; ++j)	{
					double fraction = ((double)(numPlops-j))/(numPlops);
					double[] fvs = Rn.times(null, fraction, vs),
						fve = Rn.times(null, fraction, ve);
					fvs[3] = fve[3] = 1.0;
					for (int k = 0; k<numPlops-j; ++k)	{
						double fraction2 = ((double)k)/(numPlops-j);
						Color ctmp = AnimationUtility.linearInterpolation(dotColor, colors1[i], fraction);
						dotColors[count] = AnimationUtility.linearInterpolation(ctmp, Color.white, blandFactor);
						randomPoints[count] = Rn.linearCombination(null, fraction2, fve, 1-fraction2, fvs);
						count++;
					}
				}
			}
			System.err.println("count = "+count);
			System.err.println("allocated = "+numRandomPoints);
			System.err.println("points = "+Rn.toString(randomPoints));
		}
		PointSetFactory psf = new PointSetFactory();
		psf.setVertexCount(numRandomPoints);
		psf.setVertexCoordinates(randomPoints);
		if (dotColors != null) psf.setVertexColors(dotColors);
		psf.update();
		dotsSGC.setGeometry(psf.getPointSet());
		Appearance ap = dotsSGC.getAppearance();
		ap.setAttribute("pointShader.diffuseColor", dotColor);
		ap.setAttribute("pointShader.pointRadius", .02);
		ap = linesSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", dotColor);
		ap.setAttribute("lineShader.drawTubes", false);
//		ap.setAttribute("lineShader.lineWidth", .5);
//		if (polarDotsFact == null || polarDotsFact.length != numRandomPoints)	{
//			SceneGraphUtility.removeChildren(linesSGC);
			polarDotsFact = new PointRangeFactory[numRandomPoints];
			for (int i = 0; i<numRandomPoints; ++i)	{
				polarDotsFact[i] = new PointRangeFactory();
				SceneGraphComponent child = new SceneGraphComponent("polarDot"+i);
				linesSGC.addChild(child);
				child.setAppearance(new Appearance());
				PointRangeFactory prf = polarDotsFact[i];
				prf.setPluckerLine( DualizeSceneGraph.dualizePoint2Line(null, randomPoints[i])); //new double[]{ randomPoints[i][3], 0, -randomPoints[i][1], 0, -randomPoints[i][0], 0});
				prf.setFiniteSphere(false);
				prf.update();
				if (dotColors != null) child.getAppearance().setAttribute("lineShader.diffuseColor", dotColors[i]);
				child.setGeometry(prf.getLine());
			}
//		}
	}
	
	private void doBoundary() {
//		double[] mat = faceSGC.getTransformation().getMatrix();
		double[][] theVerts = vertices; //Rn.matrixTimesVector(null, mat, vertices);
		polygonSides = new double[numSides][];
		lines = new double[numSides][];
		if (polarBFanFact == null || numSides != polarBFanFact.length)	{
			SceneGraphUtility.removeChildren(polarGC);
			polarGC.addChild(bLinesSGC);
			SceneGraphUtility.removeChildren(bLinesSGC);
			polarBFanFact = new LinePencilFactory[numSides];
			polarBLineFact = new PointRangeFactory[numSides];
			for (int i = 0; i<numSides; ++i)	{
				polarBFanFact[i] = new LinePencilFactory();
				polarBLineFact[i] = new PointRangeFactory();
				polarGC.addChild(polarBFanFact[i].getPencil());
				SceneGraphComponent child = new SceneGraphComponent("fan"+i);
				bLinesSGC.addChild(child);
				child.setAppearance(new Appearance());
				double[] tmp= PlueckerLineGeometry.lineFromPoints(null, theVerts[i], theVerts[(i+1)%numSides]);
				lines[i] = DualizeSceneGraph.dualizeLine2Point(null, tmp);
				polygonSides[i] = DualizeSceneGraph.dualizePoint2Line(polygonSides[i], theVerts[i]); //new double[]{ theVerts[i][3], 0, -theVerts[i][1], 0, -theVerts[i][0], 0};
			}
		}
		// convert the vertices of the polygon into lines: interpret them as line coordinates in 2D,
		// and then express them as 3D lines via Pluecker coordinates, assuming z = 0
		for (int i = 0; i<numSides; ++i)	{
			LinePencilFactory lpf = polarBFanFact[i]; //
			LinePencilFactory.linePencilFactoryForIntersectingLines(lpf,polygonSides[i], polygonSides[(i+1)%numSides]);
			lpf.setFan(true);
			lpf.setFiniteSphere(true);
			lpf.setSphereRadius(fanRadius);
			lpf.setNumLines(numLines);
			lpf.update();
			lpf.getPencil().getAppearance().setAttribute("lineShader.diffuseColor", colors1[i]);
			PointRangeFactory prf = polarBLineFact[i]; //new PointRangeFactory();
			prf.setPluckerLine(polygonSides[i]);
			prf.setFiniteSphere(false);
//			prf.setSphereRadius(pictureSize);
//			prf.setNumberOfSamples(12);
			prf.update();
			SceneGraphComponent child = bLinesSGC.getChildComponent(i); //new SceneGraphComponent("fan"+i);
			child.setGeometry(prf.getLine());
			child.getAppearance().setAttribute("lineShader.diffuseColor", colors2[i]);
//			polarGC.addChild(child);
		}
	}
	
	@Override
	public void customize(JMenuBar menuBar, PluginSceneLoader psl) {
		psl.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.white);
		psl.getAnimationPlugin().setAnimateCamera(true);
		psl.getAnimationPlugin().setAnimateSceneGraph(true);
		psl.getAnimationPlugin().setDefaultInterp(InterpolationTypes.CUBIC_HERMITE);
		Camera cam = CameraUtility.getCamera(psl.getViewer());
		cam.setFieldOfView(1.25 * cam.getFieldOfView());
		cam.setPerspective(false);
	}

	
	@Override
	public boolean isEncompass() {
		return true;
	}
}
