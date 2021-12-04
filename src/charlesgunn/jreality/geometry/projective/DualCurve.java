/*
 * Created on Nov 16, 2021
 *
 */
package charlesgunn.jreality.geometry.projective;

import java.awt.Color;

import charlesgunn.anim.util.AnimationUtility;
import de.jreality.geometry.GeometryUtility;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class DualCurve {

	protected LineElement[] curve;
	protected int num = 2;
	protected double stepsize = .05,
			pointWeight = 1.0,
			lineWeight = 1.0;
	
	SceneGraphComponent sgc;
	
	public DualCurve(LineElement begin, LineElement end, int n) {
		num = n;
		curve = new LineElement[num];
		curve[0] = begin;
		curve[0].normalize();
		curve[num-1] = end;
		curve[num-1].normalize();
		for (int i = 1; i<num-1; ++i)	{
			double t = i/(num - 1.0);
			double[] pt = AnimationUtility.linearInterpolation(
					t, 0, 1, begin.point, end.point);
			double[] ln = AnimationUtility.linearInterpolation(
					t, 0, 1, begin.line, end.line);
			ln[2] = -(ln[0]*pt[0] + ln[1]*pt[1]);
			curve[i] = new LineElement(pt, ln);
			curve[i].normalize();
		}
	}

	public void iterate()	{
		DualCurve tcurve = new DualCurve(curve[0], curve[num-1], num);
		for (int i = 1; i<num-1; ++i)	{
			tcurve.curve[i].line = LineElement.wedge(curve[i-1].point, curve[i].point);
			tcurve.curve[i].point = LineElement.wedge(curve[i].line, curve[i+1].line);
			tcurve.curve[i].normalize();
		}
		// iterate
		for (int i = 1; i<num-1; ++i)	{
			curve[i].point = Rn.add(null, curve[i].point,
						Rn.times(null, stepsize * pointWeight, tcurve.curve[i].point));
			curve[i].line = Rn.add(null, curve[i].line,
					Rn.times(null, stepsize * lineWeight, tcurve.curve[i].line));
		}
		getSGC();
	}
	
	public SceneGraphComponent getSGC() {
		if (sgc == null)  {
			sgc = SceneGraphUtility.createFullSceneGraphComponent("dual curve");
			Appearance ap = sgc.getAppearance();
			ap.setAttribute("lineShader.diffuseColor", Color.cyan);
			ap.setAttribute("lineShader.tubeRadius", .01);
			ap.setAttribute("pointShader.diffuseColor", Color.yellow);
			ap.setAttribute("pointShader.pointRadius", .02);
			sgc.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX	, Rectangle3D.unitCube);
		}
		else sgc.removeAllChildren();
		for (int i = 0; i<num; ++i)	{
			sgc.addChild(curve[i].getSGC());
		}
		return sgc;
		
	}
}
