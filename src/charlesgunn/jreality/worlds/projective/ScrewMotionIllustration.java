/*
 * Created on Nov 11, 2011
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;

import javax.swing.JMenuBar;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.geometry.projective.PointCollector;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import charlesgunn.math.Biquaternion;
import charlesgunn.math.IsometryAxis;
import charlesgunn.math.Biquaternion.Metric;
import de.jreality.geometry.FrameFieldType;
import de.jreality.geometry.PolygonalTubeFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class ScrewMotionIllustration extends Assignment {

	double a = Math.PI/2, pitch = .5, b = pitch*a;
	double ca = Math.cos(a), sa = Math.sin(a), cb = Math.cos(b), sb = Math.sin(b);
	double[][] mat = {{ca, -sa, 0, 0,
					sa, ca, 0, 0,
					0, 0, cb = Math.cosh(b), sb = Math.sinh(b),
					0, 0, sb, cb},
					{ca, sa, 0, 0,
						sa, ca, 0, 0,
						0,0,1,pitch,
						0,0,0,1},
					{ca, sa, 0, 0,
						sa, ca, 0, 0,
						0, 0, cb = Math.cos(b), sb = Math.sin(b),
						0, 0, -sb, cb}};
					
	int orbitSize = 50;
	double endT = 3.0,
		maxR = .5;
	int numPoints = 15;
	int numCircles = 1;
	int metric = Pn.EUCLIDEAN;
	boolean debug = false;
	@Override
	public SceneGraphComponent getContent() {
		double[] xx = {1,0,0,0}, yy = {0,1,0,0};
		Matrix mm = new Matrix();
		if (metric != 0) MatrixBuilder.init(mm, metric).rotateZ(a).rotate(xx,yy,b);
		else MatrixBuilder.euclidean().rotateZ(a).translate(0,0,pitch).assignTo(mm);
		SceneGraphComponent sgc =  SceneGraphUtility.createFullSceneGraphComponent("world");
		IsometryAxis screwMo = new IsometryAxis(mm.getArray(), Metric.metricForCurvature(metric));
		double[][] matrices = new double[orbitSize][];
		Color[] c2 = {Color.red, Color.blue};
		for (int i = 0; i<orbitSize; ++i)	{
			double x = (i)/(orbitSize-1.0);
			double t = endT*(x-.5);
			Biquaternion bq = screwMo.exp(t);
			matrices[i] = Biquaternion.matrixFromBiquaternion(null, bq);
		}
		for (int i = 0; i< numCircles; ++i)	{
			double x = numCircles == 1 ? 0 : (i)/(numCircles-1.0);
			Color interp = AnimationUtility.linearInterpolation(c2[0], c2[1], x);
			SceneGraphComponent circle = new SceneGraphComponent("circle"+(i));
			circle.setAppearance(new Appearance());
			circle.getAppearance().setAttribute("diffuseColor", interp);
			sgc.addChild(circle);
			double r = ((i+1)*maxR)/(numCircles);
			for (int j = 0; j<numPoints; ++j)	{
				double angle =  2 * (Math.PI *j)/(numPoints);
				double[] point = {r * Math.cos(angle), r* Math.sin(angle), 0, 1};
				PointCollector pc = new PointCollector(orbitSize, 4);
				for (int k = 0; k< orbitSize; ++k)	{
					double[] tpoint = Rn.matrixTimesVector(null, matrices[k], point);
					pc.addPoint(tpoint);
				}
				SceneGraphComponent child = new SceneGraphComponent("curve"+(i*numPoints+j));
				if (!debug)	{
					circle.addChild(child);
					child.setGeometry(pc.getCurve());					
				} else {
					IndexedLineSet ils = pc.getCurve();
					PolygonalTubeFactory ptf = new PolygonalTubeFactory(ils, 0);
					ptf.update();
					SceneGraphComponent dbg = ptf.getFramesSceneGraphRepresentation();
					circle.addChild(dbg);					
				}
			}
		}
		sgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		
		PointRangeFactory prf = new PointRangeFactory();
		prf.setElement0(new double[]{0,0,1,0});
		prf.setElement1(new double[]{0,0,0,1});
		prf.setFiniteSphere(false);
		prf.update();
		SceneGraphComponent axis1 = new SceneGraphComponent("axis1");
		axis1.setGeometry(prf.getLine());
		
		prf = new PointRangeFactory();
		prf.setElement0(new double[]{1,0,0,0});
		prf.setElement1(new double[]{0,1,0,0});
		prf.setFiniteSphere(false);
		prf.update();
		SceneGraphComponent axis2 = new SceneGraphComponent("axis12");
		axis2.setGeometry(prf.getLine());
//		sgc.addChildren(axis1, axis2);
		
		sgc.getAppearance().setAttribute(CommonAttributes.DIFFUSE_COLOR, Color.white);
		sgc.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS, .005);
		sgc.getAppearance().setAttribute("useGLSL", true);
		sgc.getAppearance().setAttribute(
				CommonAttributes.TUBE_STYLE, FrameFieldType.FRENET);
		MatrixBuilder.elliptic().translate(0,0,-1).rotateX(-Math.PI/2).assignTo(sgc);
		return sgc;
	}
	@Override
	public void display() {
		super.display();
		viewer.getSceneRoot().getAppearance().setAttribute("metric", metric);
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", Color.white);
		CameraUtility.getCamera(viewer).setNear(.01);
		CameraUtility.getCamera(viewer).setFar(metric == Pn.ELLIPTIC ? -.05 : 10.0);
		CameraUtility.getCamera(viewer).setFieldOfView(90);
	}
	
	public int getMetric() {
		// TODO Auto-generated method stub
		return metric;
	}

	public static void main(String[] args) {
		new ScrewMotionIllustration().display();
	}
}
