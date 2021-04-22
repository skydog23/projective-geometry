/*
 * Created on May 1, 2007
 *
 */
package charlesgunn.jreality.worlds;

import java.awt.Color;
import java.util.Random;

import javax.swing.JMenuBar;

import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class VelocityField extends LoadableScene {
	int ucount = 10, vcount = 10, ncircs = 49; 
	int metric = Pn.EUCLIDEAN;
	int bivtype = 0; // the square of the bivector, only positive allowed in hyperbolic case
	double a = .05, b = .1, c = .2, e2 = metric; //a = 0, b = 0, c = 1, e2 = metric; //
	boolean showVectors = false;
	@Override
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent universe = SceneGraphUtility.createFullSceneGraphComponent("universe");
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		if (bivtype == 0 && metric == -1) {a=1; b=0; c=1;}
		if (bivtype == 1) {a = .5; b = 1; c = .5;}
		IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
		double[][] vv = {{-1,0,0},{1,0,0},{0,-1,0},{0,1,0}, {0,0,0}};
		int[][] edges = {{0,1},{2,3}};
		ilsf.setVertexCount(5);
		ilsf.setVertexCoordinates(vv);
		ilsf.setVertexRelativeRadii(new double[]{0,0,0,0,10});
		ilsf.setEdgeCount(2);
		ilsf.setEdgeIndices(edges);
		ilsf.update();
		SceneGraphComponent cross = SceneGraphUtility.createFullSceneGraphComponent("cross");
		cross.setGeometry(ilsf.getGeometry());
		cross.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		cross.getAppearance().setAttribute(CommonAttributes.LINE_WIDTH, 2.0);
		cross.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.blue);
		cross.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		cross.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.blue);
		if (bivtype == 0 && metric == Pn.EUCLIDEAN)
			MatrixBuilder.elliptic().translate(new double[]{a,b,0,0}).scale(.04).assignTo(cross);
		else MatrixBuilder.euclidean().translate(new double[]{a,b,0,0}).scale(.04).assignTo(cross);		
		universe.addChild(cross);
		
		double[] flip = Rn.diagonalMatrix(null, new double[]{-1,-1,-1,-1});
		Matrix t = new Matrix(flip);
		IndexedLineSet standardCircle = IndexedLineSetUtility.circle(100);
//		SceneGraphComponent horo = new SceneGraphComponent();
//		horo.setGeometry(IndexedLineSetUtility.circle(100, 0.0, 0.0, .5));
//		MatrixBuilder.euclidean().translate(.5,0,0).scale(1,Math.sqrt(2.0),1).assignTo(horo);

		if (showVectors)	{
			SceneGraphComponent vectors = SceneGraphUtility.createFullSceneGraphComponent("vectors");
			universe.addChild(vectors);
			PointSetFactory psf = new PointSetFactory();
			int numpts = 300;
			double[][] pts = new double[numpts][], vecs = new double[numpts][];
			Random rnd = new Random(System.currentTimeMillis());
			for (int i = 0; i<numpts; ++i)	{
				boolean found = true;
				double[] tmp = null;
				int count = 0;
				do {
					double r =  3*rnd.nextDouble()+.05;
					double a = 2*Math.PI*rnd.nextDouble();
					tmp = new double[]{r*Math.cos(a), r*Math.sin(a),0, 1};
					for (int j = 0; j<i; ++j)	{
						if (Rn.euclideanDistance(tmp, pts[j]) < (.03*r)) {
							found = false;
							break;
						}
					}
					count++;
				} while (count < 50 && !found);
				pts[i] = tmp;
				vecs[i] = new double[]{-c*pts[i][1] +b*pts[i][3], c*pts[i][0]-a*pts[i][3],0, e2*(-b*pts[i][0]+a*pts[i][1])};
				//vecs[i] = new double[]{-c*pts[i][1], c*pts[i][0],0, 0};
				
			}
			psf.setVertexCount(numpts);
			psf.setVertexCoordinates(pts);
			psf.update();
			SceneGraphComponent points = SceneGraphUtility.createFullSceneGraphComponent("points");
			points.setGeometry(psf.getPointSet());
			points.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS, .01);
			vectors.addChild(points);
			
			points.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.black);
			IndexedLineSet vfield = GeometryUtilityOverflow.attachVectorField(psf.getPointSet(), vecs);
			SceneGraphComponent sgc = new SceneGraphComponent();
			BallAndStickFactory basf = new BallAndStickFactory(vfield);
			basf.setShowArrows(true);
			basf.setShowBalls(false);
			basf.setShowSticks(true);
			basf.setArrowPosition(1.0);
			basf.setArrowSlope(3.0);
			basf.setArrowScale(.0075);
			basf.setBallColor(Color.black);
			basf.setStickColor(new Color(175, 175, 175));
			basf.setArrowColor(new Color(175, 175, 175));
			basf.setStickRadius(.005);
			basf.update();
			//sgc.setGeometry(basf.get);
			vectors.addChild(basf.getSceneGraphComponent());
			
		} else {
			SceneGraphComponent uebercircles = SceneGraphUtility.createFullSceneGraphComponent("uebercircles");
			world.addChild(uebercircles);
			SceneGraphComponent circles = SceneGraphUtility.createFullSceneGraphComponent("circles");
			uebercircles.addChild(circles);
			Appearance ap = circles.getAppearance();
			ap.setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.unitCube);

			ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
			ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
			ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(60,60,60));
			ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH,1);
			SceneGraphComponent reflcircles = SceneGraphUtility.createFullSceneGraphComponent("refl circles");
			reflcircles.addChild(circles);
			//MatrixBuilder.euclidean().reflect(new double[]{1,0,0,-1}).assignTo(reflcircles);
			t.assignTo(reflcircles);
			uebercircles.addChild(reflcircles);					
			if (bivtype == 0)	{
				if (metric == Pn.HYPERBOLIC)	{
					double[] matrix = {-1.00000,	-2.00000,	0.00000,	2.00000,
							2.00000,	1.00000,	0.00000,	-2.00000,
							0.00000,	0.00000,	1.00000,	0.00000,
							-2.00000,	-2.00000,	0.00000,	3.00000
							};
					//MatrixBuilder.euclidean().translate(1, 0, 0).scale(9,Math.sqrt(9.0),1).translate(-1,0,0).assignTo(circles);
					//new Matrix(matrix).assignTo(circles);
					for (int i = 0; i< ncircs; ++i) {
						double s = Math.sinh(8*(i-ncircs/2.0)/(ncircs)); //, y = Math.sqrt(x); //Math.pow(1.2,i-(ncircs/2.0))
						double[] mm = {2 - s, 0, 0, s, 0, 2, 0, 0, 0, 0, 2, 0,-s, 0, 0, 2 + s};
						SceneGraphComponent sgc = new SceneGraphComponent();
						sgc.setGeometry(standardCircle);
						//MatrixBuilder.euclidean().translate(1-x,0,0).scale(x,y,0).assignTo(sgc);
						new Matrix(mm).assignTo(sgc);
						circles.addChild(sgc);
					}
				} else // euclidean
				{
					SceneGraphComponent pcircles = SceneGraphUtility.createFullSceneGraphComponent("perp circles");
					circles.addChild(pcircles);
					ap = pcircles.getAppearance();
					ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(180,180,180));
					double[] P = {a,b,0,0}, PP = {-b,a,0,0};
					for (int i = 0; i< ncircs; ++i) {
						double angle = 60*( (i-ncircs/2.0)/(ncircs-1.0));
						double[] PP2 = {-b + angle*a, a + angle*b, 0, 1};
						PointRangeFactory prf = new PointRangeFactory();
						prf.setElement0(PP);
						prf.setElement1(PP2);
						prf.setFiniteSphere(false);
						prf.update();
						SceneGraphComponent sgc = new SceneGraphComponent();
						sgc.setGeometry(prf.getLine());
						circles.addChild(sgc);
						// draw the perpendicular set of lines going through the center of the translation
						double[] P2 = {a + angle*(-b), b + angle*a, 0, 1};
						prf = new PointRangeFactory();
						prf.setElement0(P);
						prf.setElement1(P2);
						prf.setFiniteSphere(false);
						prf.update();
						sgc = new SceneGraphComponent();
						sgc.setGeometry(prf.getLine());
						pcircles.addChild(sgc);
					}
					// apply a projectivity to bring the line at infinity into the finite realm
					double mangle = -Math.PI/4.0;
					double[] floop = {1,0,0,0,    0,Math.cos(mangle), 0, Math.sin(mangle),
							0,0,1,0,   0, -Math.sin(mangle), 0, Math.cos(mangle)};
					new Matrix(floop).assignTo(universe);
				}
			} else
				if (bivtype == 1)	{
					double[] center = {a,b,0,c}, axis = {a,b,0,-c};
					for (int i = 0; i< ncircs; ++i) {
						double s = 8*(i-ncircs/2.0)/(ncircs); //, y = Math.sqrt(x); //Math.pow(1.2,i-(ncircs/2.0))
						double[] mm = Pn.makeGeneralizedProjection(null, center, axis, s);
						SceneGraphComponent sgc = new SceneGraphComponent();
						sgc.setGeometry(standardCircle);
						//MatrixBuilder.euclidean().translate(1-x,0,0).scale(x,y,0).assignTo(sgc);
						new Matrix(mm).assignTo(sgc);
						circles.addChild(sgc);
					}
					// figure out the integral curves in the other angle
					// to do so have to find the two fixed points on the axis of the translation and
					// create an elliptic involution that swaps them
					double d = a * Math.sqrt(a*a+b*b-c*c);
					double y1 = (b*c -d)/(a*a+b*b), y2 = (b*c+d)/(a*a+b*b),
						x1 = (c-b*y1)/a, x2 = (c-b*y2)/a;
					Matrix frame = new Matrix();
					frame.setColumn(0, new double[]{x2, y2, 0, 1});
					frame.setColumn(1, new double[]{x1, y1, 0, 1});
					frame.setColumn(2, new double[]{0,0,1,0});
					frame.setColumn(3, new double[]{a,b,0,c});
					Matrix frame2 = new Matrix(frame);
					frame2.setColumn(1, Rn.times(null, -1, frame.getColumn(0)));
					frame2.setColumn(0, Rn.times(null, 1, frame.getColumn(1)));
					Matrix outerM = new Matrix(Rn.times(null, frame2.getArray(), Rn.inverse(null, frame.getArray())));
					System.err.println("F1 = "+Rn.matrixToString(frame.getArray()));
					System.err.println("outer m . F1 = "+Rn.matrixToString(Rn.times(null, outerM.getArray(), frame.getArray())));
					SceneGraphComponent outercircles = SceneGraphUtility.createFullSceneGraphComponent("circles");
					world.addChild(outercircles);
					outercircles.addChild(uebercircles);
					outerM.assignTo(outercircles);
					outercircles.setAppearance(circles.getAppearance());
				} else

			for (int i = 0; i< ncircs; ++i) {
				double radius = Math.tan((i*Math.PI)/(ncircs));
				IndexedLineSet circle = IndexedLineSetUtility.circle(100, 0.0, 0.0, radius);
				SceneGraphComponent sgc = new SceneGraphComponent();
				if (i>ncircs/2)
					t.assignTo(sgc);
				sgc.setGeometry(circle);
				circles.addChild(sgc);
			}
		}
		
		world.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		universe.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
//		world.setGeometry(bar);
		
		IndexedLineSet circ = IndexedLineSetUtility.circle(100);
		SceneGraphComponent sgc2 = SceneGraphUtility.createFullSceneGraphComponent();
		Appearance ap = sgc2.getAppearance();
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.blue);
		ap.setAttribute(CommonAttributes.LINE_WIDTH,1.5);
		sgc2.setGeometry(circ);
		universe.addChild(sgc2);
		//sgc2.setVisible(e2 == -1);
		
		/* add the polar line of the point (a,b,c) */
		double scaleroo = 1;
		double polarLine[] = {metric*c, 0, -b*scaleroo, 0, -a*scaleroo, 0}; //{ e2 * c, 0, -b, 0, -a, 0};
		//double polarLine[] = {-scaleroo, 0, 0, 0, -scaleroo, 0}; //{ e2 * c, 0, -b, 0, -a, 0};
		if (metric==Pn.EUCLIDEAN) polarLine = new double[]{1,0,0,0,0,0};
		PointRangeFactory prf = new PointRangeFactory();
		prf.setPluckerLine(polarLine);
		prf.setFiniteSphere(false);
		prf.update();
		sgc2 = SceneGraphUtility.createFullSceneGraphComponent("polar line");
		sgc2.setGeometry(prf.getLine());
		ap = sgc2.getAppearance();
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute(CommonAttributes.LINE_WIDTH,1.5);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		prf.getLine().setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.blue);
		//sgc2.setVisible(metric != Pn.EUCLIDEAN);
		//sgc2.setTransformation(t);
		universe.addChild(sgc2);
		Matrix m = new Matrix(P3.makeTranslationMatrix(null, new double[]{a/c,b/c,0}, metric));
		if (bivtype == -1) m.assignTo(world);
		universe.addChild(world);
		return universe;
	}

	@Override
	public void customize(JMenuBar menuBar, Viewer viewer) {
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(255, 255, 255));
	}

	@Override
	public boolean isEncompass() {
		return false;
	}
	
}
