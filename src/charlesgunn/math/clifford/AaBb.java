/*
 * Created on Mar 24, 2014
 *
 */
package charlesgunn.math.clifford;

import java.awt.Color;

import charlesgunn.jreality.geometry.projective.CircleFactory;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.Matrix;
import de.jreality.math.P2;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class AaBb extends Assignment {
	// start with a triangle
	// the third coordinate of pts3 is the w-coordinate, not the z-coordinate!
	double[][] pts3 = {{1,0,1}, {.2, Math.sqrt(3.0),1},  {-1, 0,1}},
			pts4 = new double[3][4]; 	
	int metric = Pn.EUCLIDEAN;
	
	SceneGraphComponent world,  points,
			triangle, movedTriangles,  movedTriangle[], mirrors, mirror[], symmetryLine, eulerLine;
	IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory(),
			mirrorIfsf[] = new IndexedFaceSetFactory[3];
	PointRangeFactory lineFac[] = new PointRangeFactory[3], 
			symmetryFac = new PointRangeFactory(),
			eulerFac = new PointRangeFactory();
	Color[] colors = {Color.red, Color.blue, Color.green};
	String[][] labels = {{"cab"}, {"abc"}, {"bca"}};
	private MultivectorP2 A, B, C, a, b, c;
	TwoSpace ts = new TwoSpace(metric);
	private MultivectorP2 glideReflection;
	int numSteps = 20;
	
	@Override
	public SceneGraphComponent getContent() {
		if (world == null) init();
		update();
		return world;
	}


	protected void init() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		symmetryLine = SceneGraphUtility.createFullSceneGraphComponent("symmetryLine");
		eulerLine = SceneGraphUtility.createFullSceneGraphComponent("eulerLine");
		Appearance ap = world.getAppearance();
		ap.setAttribute("polygonShader."+CommonAttributes.DIFFUSE_COLOR, Color.white);
		ap.setAttribute("lineShader."+CommonAttributes.DIFFUSE_COLOR, Color.white);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, .05);
		ap.setAttribute("pointShader."+CommonAttributes.VERTEX_COLORS_ENABLED, true);
		triangle = SceneGraphUtility.createFullSceneGraphComponent("triangle");
		movedTriangles = SceneGraphUtility.createFullSceneGraphComponent("movedTriangle");
		movedTriangle = new SceneGraphComponent[3];
		mirrors = SceneGraphUtility.createFullSceneGraphComponent("movedTriangle");
		mirrors.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		mirrors.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		mirror = new SceneGraphComponent[3];
		for (int i = 0; i<3; ++i)	{
			mirror[i] = SceneGraphUtility.createFullSceneGraphComponent("mirror"+i);
			mirrors.addChild(mirror[i]);
			mirror[i].getAppearance().setAttribute("lineShader.diffuseColor", colors[(i+1)%3]);
			lineFac[i] = new PointRangeFactory(); 
			lineFac[i].setNumberOfSamples(2);
			lineFac[i].setSphereRadius(50);
			lineFac[i].update(); 
			mirror[i].setGeometry(lineFac[i].getLine());
		}

		pts4 = P2.imbedP2InP3(null, pts3);
		ifsf = IndexedFaceSetUtility.constructPolygonFactory(ifsf, pts4, 0);
		ifsf.setFaceLabels(new String[]{"1"});
		ifsf.setVertexCount(3);
		ifsf.setVertexColors(colors);
		ifsf.update();
		triangle.setGeometry(ifsf.getGeometry());

		for (int i = 0; i<3; ++i)	{
			movedTriangle[i] = SceneGraphUtility.createFullSceneGraphComponent("movedTriangle"+i);
			movedTriangles.addChild(movedTriangle[i]);
			mirrorIfsf[i] = IndexedFaceSetUtility.constructPolygonFactory(null, pts4, 0);
			movedTriangle[i].setGeometry(mirrorIfsf[i].getGeometry());
			mirrorIfsf[i].setFaceLabels(labels[i]);
			mirrorIfsf[i].setVertexCount(3);
			mirrorIfsf[i].setVertexColors(colors);
			mirrorIfsf[i].update();
		}

		DragEventTool t = new DragEventTool();
		t.addPointDragListener(new PointDragListener() {

			public void pointDragStart(PointDragEvent e) {
//				System.out.println("drag start of vertex no "+e.getIndex());				
			}

			public void pointDragged(PointDragEvent e) {
				for (int i = 0; i<2; ++i) pts3[e.getIndex()][i] = e.getPosition()[i];
		        update();
			}

			public void pointDragEnd(PointDragEvent e) {
			}
			
		});
		
		triangle.addTool(t);

		movedTriangles.setPickable(false);

		symmetryFac = new PointRangeFactory(); 
		symmetryFac.setNumberOfSamples(2);
		symmetryFac.setSphereRadius(50);
//		symmetryFac.setFiniteSphere(false);
		symmetryLine.setGeometry(symmetryFac.getLine());
		eulerFac = new PointRangeFactory(); 
		eulerFac.setNumberOfSamples(2);
		eulerFac.setSphereRadius(50);
//		symmetryFac.setFiniteSphere(false);
		eulerLine.setGeometry(eulerFac.getLine());
		mirrors.addChildren(symmetryLine, eulerLine);
		world.addChildren(triangle, movedTriangles, mirrors);
		
	}
	
	private void update()	{
		pts4 = P2.imbedP2InP3(pts4, pts3);
		ifsf.setVertexCoordinates(pts4);
		ifsf.update();
		
		System.err.println("points = "+Rn.toString(pts3));
		A= MultivectorP2.point(pts3[0]);
		B = MultivectorP2.point(pts3[1]);
		C = MultivectorP2.point(pts3[2]);
		a = MultivectorP2.join(null, B, C);
		b = MultivectorP2.join(null, C, A);
		c = MultivectorP2.join(null, A, B);
		a = ts.normalize(null, a);
		b = ts.normalize(null, b);
		c = ts.normalize(null, c);
	
		MultivectorP2 abc[] = new MultivectorP2[]{a,b,c},
				mirrorLines[] = new MultivectorP2[3];

		for (int i = 0; i<3; ++i)	{
			mirrorIfsf[i].setVertexCoordinates(pts4);
			mirrorIfsf[i].update();
			int ii = i, jj = (i+1)%3, kk = (i+2)%3;
			glideReflection = ts.gp(null, abc[kk], ts.gp(null, abc[jj], abc[ii]));
			mirrorLines[i] = new MultivectorP2(glideReflection);
			
			ts.normalize(glideReflection, glideReflection);
//			glideReflection = new MultivectorP2(new double[]{0,-1,1,0,0,0,0,0});
//			glideReflection.getVals()[7] = 0.0;
			lineFac[i].set2DLine(MultivectorP2.gradeD(glideReflection, 1));  
			lineFac[i].update(); 
			MultivectorP2 test = ts.gp(null, glideReflection, MultivectorP2.reverse(null, glideReflection));
			
			System.err.println("glide reflection = "+glideReflection);
			System.err.println("norm = "+test);
			double[] mat = ts.matrixForRotor(glideReflection);
			double[] mat4 = P2.imbedMatrixP2InP3(null, mat);
			Matrix m = new Matrix(mat4);
			if (mat4[15] < 0){
				//mat = Rn.times(null, -1, mat);
//				m.setColumn(0, Rn.times(null, -1, m.getColumn(0)));
//				m.setColumn(3, Rn.times(null, -1, m.getColumn(3)));
			}
//			m.setColumn(0, Rn.times(null, -1, m.getColumn(0)));
//			m.setColumn(1, Rn.times(null, -1, m.getColumn(1)));
			System.err.println("matrix = "+Rn.matrixToString(m.getArray()));
			m.assignTo(movedTriangle[i]);
		}

		// calculate symmetry line
		MultivectorP2 sumMirrorLines = MultivectorP2.plus(null, mirrorLines[0], 
						MultivectorP2.plus(null, mirrorLines[1], mirrorLines[2]));
		sumMirrorLines = MultivectorP2.grade(null, sumMirrorLines, 1);
		ts.normalize(sumMirrorLines, sumMirrorLines);
		symmetryFac.set2DLine(MultivectorP2.gradeD(sumMirrorLines, 1));  
		symmetryFac.update(); 
		
		// update euler line
		MultivectorP2 centroid = MultivectorP2.plus(null, A, MultivectorP2.plus(null, B, C));
		ts.normalize(centroid, centroid);
		// orthocenter is product of two altitudes
		MultivectorP2 altA = MultivectorP2.grade(null, ts.gp(null, a, A), 1);
		MultivectorP2 altB = MultivectorP2.grade(null, ts.gp(null, b, B), 1);
		MultivectorP2 orthocenter = MultivectorP2.grade(null, ts.gp(null, altA, altB), 2);
		MultivectorP2 perpA = MultivectorP2.grade(null, ts.gp(null, a, MultivectorP2.plus(null, B, C)), 1);
		MultivectorP2 perpB = MultivectorP2.grade(null, ts.gp(null, b, MultivectorP2.plus(null, C, A)), 1);
		MultivectorP2 circumcenter = MultivectorP2.grade(null, ts.gp(null, perpA, perpB), 2);
		MultivectorP2 el = MultivectorP2.join(null, centroid, orthocenter);
		eulerFac.set2DLine(MultivectorP2.gradeD(el, 1));
		MultivectorP2 xpoint = MultivectorP2.wedge(null, sumMirrorLines, el);
		ts.normalize(orthocenter, orthocenter);
		ts.normalize(circumcenter, circumcenter);
		ts.normalize(xpoint, xpoint);
		double xratio = calculateCrossRatio(centroid, orthocenter, circumcenter, xpoint);
		System.err.println("Cross ratio = "+xratio);
		
		MultivectorP2 polarSymPoint = polarizeWRTTriangle(null, sumMirrorLines, a, b, c, A, B, C);
		System.err.println("polar sym line = "+polarSymPoint);
		System.err.println("centroid = "+centroid);
		System.err.println("orthocenter = "+orthocenter);
		System.err.println("circumcenter = "+circumcenter);
		eulerFac.update();
	}

	private MultivectorP2 polarizeWRTTriangle(Object object, MultivectorP2 sumMirrorLines, MultivectorP2 a2,
			MultivectorP2 b2, MultivectorP2 c2, MultivectorP2 A2, MultivectorP2 B2, MultivectorP2 C2) {
		// first find the intersection of the line with the sides of the triangle
		MultivectorP2 xA = MultivectorP2.wedge(null, sumMirrorLines, a2), // (0-11)
				xB = MultivectorP2.wedge(null, sumMirrorLines, b2),  // (10-1)
				xC = MultivectorP2.wedge(null, sumMirrorLines, c2),  // (-110)
				// then join these points to the corresponding corners of the triangle
				ja = MultivectorP2.join(null, xA, A2),  // [011]
				jb = MultivectorP2.join(null, xB, B2),  // [101]
				jc = MultivectorP2.join(null, xC, C2),  // [110]
				// then find the intersections pairwise of the new lines
				yA = MultivectorP2.wedge(null, jb, jc), // (-111)
				yB = MultivectorP2.wedge(null, jc, ja), // (1-11)
				yC = MultivectorP2.wedge(null, ja, jb), // (11-1)
				// join with the corresponding vertices (again)
				ja2 = MultivectorP2.join(null, yA, A2),  // [011]
				jb2 = MultivectorP2.join(null, yB, B2),  // [101]
				jc2 = MultivectorP2.join(null, yC, C2),  // [110]
				// finally join two of these to get polar point
				pP = MultivectorP2.wedge(null, ja2, jb2);
				
				ts.normalize(pP, pP);
		return pP;
	}


	private double calculateCrossRatio(MultivectorP2 centroid, MultivectorP2 orthocenter, MultivectorP2 circumcenter,
			MultivectorP2 xpoint) {
		double[][] pts = new double[4][];
		pts[0] = MultivectorP2.gradeD(centroid, 2);
		pts[1] = MultivectorP2.gradeD(orthocenter, 2);
		pts[2] = MultivectorP2.gradeD(circumcenter, 2);
		pts[3] = MultivectorP2.gradeD(xpoint, 2);
		double
				ac = Pn.distanceBetween(pts[0], pts[2], 0),
				ad = Pn.distanceBetween(pts[0], pts[3], 0),
				bc = Pn.distanceBetween(pts[1], pts[2], 0),
				bd = Pn.distanceBetween(pts[1], pts[3], 0);
		
		return (ac * bd)/(ad * bc);
	}


	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		Camera foo = CameraUtility.getCamera(jrviewer.getViewer());
		foo.setFar(20);
		foo.setNear(1);
	}


	public static void main(String[] args) {
		new AaBb().display();
	}
}
