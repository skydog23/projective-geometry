/*
 * Created on 17 May 2024
 *
 */
package charlesgunn.jreality.worlds;

import static de.jreality.shader.CommonAttributes.ATTENUATE_POINT_SIZE;

import java.awt.Color;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.geometry.projective.CurveCollector;
import charlesgunn.jreality.geometry.projective.PointCollector;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

public class CassiniCurves extends Assignment {

	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	SceneGraphComponent fdsym = SceneGraphUtility.createFullSceneGraphComponent("fdsym");
	SceneGraphComponent[] symsgc =  new SceneGraphComponent[4];
	double xmin = -2, xmax = 2, ymin = -2, ymax = 2, a = 1, a4 = Math.pow(a, 4);
	int numc = 200, numv = 50;
	CurveCollector cc = new CurveCollector(numc, numv, 4);
	
	@Override
	public SceneGraphComponent getContent() {
		Matrix mat = new Matrix();
		for (int i = 0; i<4; ++i)	{
			SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("sym"+i);
			sgc.addChild(fdsym);
			switch(i)	{
			case 0: 
				break;
			case 1:
				mat = MatrixBuilder.euclidean().reflect(new double[] {1,0,0,0}).getMatrix();
				break;
			case 2:
				mat = MatrixBuilder.euclidean().reflect(new double[] {0,1,0,0}).getMatrix();
				break;
			case 3:
				mat = MatrixBuilder.euclidean().reflect(new double[] {1,0,0,0}).reflect(new double[] {0,1,0,0}).getMatrix();
				break;
			}
			mat.assignTo(sgc);
			world.addChild(sgc);
		}
		Appearance ap = fdsym.getAppearance();
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.SPHERES_DRAW, false);
		ap.setAttribute(ATTENUATE_POINT_SIZE, false);
//		ap.setAttribute(CommonAttributes.LINE_WIDTH,1.0);
		ap.setAttribute("lineShader.diffuseColor", new Color(1,1,1));
		ap.setAttribute("pointShader.diffuseColor", Color.red);
//		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
//		ap.setAttribute(CommonAttributes.TRANSPARENCY, .9);
		update();
		return world;
	}


	@Override
	public void display() {
		super.display();
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(0,0,0,0));
	}


	private void update() {
		fdsym.removeAllChildren();
		for (int i = 0; i<numc; ++i) {
			double tt = AnimationUtility.linearInterpolation(i, 0, numc, 0.0, 1);
			double b = getBForC(tt),
					//b = Math.sqrt(a*a + yy*yy),
					b4 = Math.pow(b, 4.0),
					a4 = Math.pow(a, 4.0);
			PointCollector pc = new PointCollector(2*numv, 4);
			double tmax;
			tmax = (a > b) ?  (Math.asin((b*b)/(a*a))): Math.PI/2.0;
			for (int j = 0; j<numv; ++j)	{
				double t = tmax * (j/(numv-2.0));
					double c = Math.cos(t),
					s = Math.sin(t),
					c2 = Math.cos(2*t),
					s2 = Math.sin(2*t),
					tmp = b4/a4 - s2*s2;
				
					if (tmp < 0 )  {
						//System.err.println("imaginary");
						continue;  // imaginary
					}
					double tmp2 =  c2 + Math.sqrt(tmp);
					if (tmp2 < 0) continue;
					double r1 = a * Math.sqrt(tmp2);
					double[] pt = {r1*c, r1*s, 0, 1};
					pc.addPoint(pt);
//					System.err.println("pt "+j+" "+Rn.toString(pt));
			}
			if (a>b) {
				for (int j = numv-1; j>= -1; j--)	{
					double t = tmax * (j/(numv-1.0));
						double c = Math.cos(t),
						s = Math.sin(t),
						c2 = Math.cos(2*t),
						s2 = Math.sin(2*t),
						tmp = b4/a4 - s2*s2;
					
						if (tmp < 0 )  {
							//System.err.println("imaginary");
							continue;  // imaginary
						}
						double tmp2 =  c2 - Math.sqrt(tmp);
						if (tmp2 < 0) continue;
						double r1 = a * Math.sqrt(tmp2);
						double[] pt = {r1*c, r1*s, 0, 1};
						pc.addPoint(pt);
//						System.err.println("pt "+j+" "+Rn.toString(pt));
				}	
			}
//			System.err.println("curve "+i+" "+pc.getCount());
			SceneGraphComponent sgc = new SceneGraphComponent("child"+i);
			fdsym.addChild(sgc);
			sgc.setGeometry(pc.getCurve());
		}
		
	}

	protected double getBForC(double t)	{
//		a = .5 + Math.sin(0+2.5*Math.PI*t);
//		return t*2 + .32*Math.sin(.61*Math.PI*t);
		return t*2;
	}
	public static void main(String[] args) {
		new CassiniCurves().display();
	}

}
