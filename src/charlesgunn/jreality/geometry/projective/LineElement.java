/*
 * Created on Nov 16, 2021
 *
 */
package charlesgunn.jreality.geometry.projective;

import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import de.jreality.geometry.Primitives;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

public class LineElement {

	
	protected double[] point = {0,0,1}, line={1,0,0};
	protected boolean isIncident = true;
	final double tol = 10e-8;
	protected SceneGraphComponent sgc;
	
	public LineElement() {
		super();
	}
	public LineElement(double[] p, double[] l)	{
		super();
		point = p.clone();
		line = l.clone();
	}
	
	public boolean isIncident() {
		return (Math.abs(evaluate(point, line)) < tol); 
	}
	
	public void normalize()	{
		normalizePoint(point);
		normalizeLine(line);
	}

	public SceneGraphComponent getSGC()	{
		if (sgc == null)
			sgc = SceneGraphUtility.createFullSceneGraphComponent("line element");
		else sgc.removeAllChildren();
		
		double[] pt4 = GeometryUtilityOverflow.convert3To4(null, point);
		SceneGraphComponent ptsgc = SceneGraphUtility.createFullSceneGraphComponent("pt");
		PointSet f2 = Primitives.point(pt4);
		ptsgc.setGeometry(f2);
		ptsgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		sgc.addChild(ptsgc);
//		sgc.addChild(Primitives.labelPoint(null, pt4, null));
		System.err.println("adding point "+Rn.toString(pt4));
		System.err.println("adding line "+Rn.toString(line));
//		double[] ln3d = GeometryUtilityOverflow.convert2dLineTo3d(null, line);
		double[] ln3d = {line[2], 0, -line[1], 0, -line[0], 0};
//		ln3d = PlueckerLineGeometry.dualizeLine(null, ln3d);
		SceneGraphComponent lsgc = LineUtility.sceneGraphForLine(null, ln3d, null, 1.0, false);
		lsgc.setAppearance(new Appearance());
		sgc.addChild(lsgc);
		Appearance ap = lsgc.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		
		return sgc;
	}
	

	public static double[] wedge(double[] a, double[] b)	{
		return new double[]{a[1]*b[2] - a[2]*b[1],
				a[2]*b[0] - a[0]*b[2],
				a[0]*b[1] - a[1]*b[0]};
	}
	
	public static double evaluate(double[] a, double[] b)	{
		return Rn.innerProduct(a, b);
	}
	
	public static void normalizePoint(double[] a) {
		if (a[2] != 0) Rn.times(a, 1.0/a[2], a);
	}
	
	public static void normalizeLine(double[] a) {
		double d = Math.sqrt(a[0]*a[0] + a[1]*a[1]);
		if (d != 0) Rn.times(a, 1.0/d, a);
	}

	public static void main(String[] args) {
		LineElement le = new LineElement();
		System.err.println("incident = "+le.isIncident());
	}
}
