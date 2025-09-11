package charlesgunn.jreality.geometry.projective;

import static de.jreality.plugin.icon.ImageHook.getIcon;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.View;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class ConicSectionAlgebraic {

	transient protected double[] conic = Rn.diagonalMatrix(null, new double[] {1,1,-1}), dconic;
	transient protected double a,b,cc,f,g,h, A,B,C,F,G,H;
	transient protected double[] center = null, 
			dcenter = null, 
			pointOnConic = null,
			lineOnConic = null;
	transient double[][] curve = null;
	transient protected int numSamples = 100;
	static double[] origin = {0,0,1};
	public ConicSectionAlgebraic(double[] c)	{
		// should check symmetry!
		conic = c.clone();
		dconic = Rn.adjoint(null, conic);
		// polar partner of line at infinity, resp., the origin
		center= getPolarPoint(origin);
		// we want that the center has negative z-coordinate
		if (center[2] < 0) Rn.times(center, -1, center);
		dcenter= getPolarLine(origin);
		if (dcenter[2] < 0) Rn.times(dcenter, -1, dcenter);
		a = conic[0];
		b = conic[4];
		cc = conic[8];
		h = conic[1];
		g = conic[2];
		f = conic[3];
		A = dconic[0];
		B = dconic[4];
		C = dconic[8];
		H = dconic[1];
		G = dconic[2];
		F = dconic[3];
	
		pointOnConic = getPointOnConic();
		lineOnConic = getPolarLine(pointOnConic);
		getConicCurve();
	}

	public double[] getConic() {
		return conic;
	}
	
	public double[][] getConicCurve()	{
		curve = new double[numSamples][];
		
		for (int i = 0; i<numSamples; ++i)	{
			double angle = ( Math.PI * i)/(numSamples*1.0),
					c = Math.cos(angle),
					s = Math.sin(angle);
			double[] poc = pointOnConic;
			double[] V = {c,s,0};  // direction of line
			double  B = aQb(V, conic, poc),
					A = aQa(conic, V), 
					t = (A == 0) ? 0 : -2*B/A;
			curve[i] = Rn.add(null, poc, Rn.times(null, t, V));
		}
		
		return curve;
	}
	public double[] getPointOnConic() {
		if (pointOnConic != null) return pointOnConic;
		double[] pt = new double[3];
		for (int i = 0; i<numSamples; ++i)	{
			double angle = (Math.PI * i)/(numSamples*1.0),
					c = Math.cos(angle),
					s = Math.sin(angle);
			double[] ln = {s, -c, c*center[1] - s*center[0]}, 
					V = {c,s,0};  // direction of line
			double C = aQa(conic, center),
					B = aQb(V, conic, center),
					A = aQa(conic, V);
			double d = B*B-A*C;
			if (d < 0) continue;  // no intersection
			if (A == 0 && B == 0) continue;
			double t = 0;
			if (A == 0) t = -C/(2*B);
			else {
				d = Math.sqrt(d);
			    t = (-B + d)/A;	
			}
			pt = Rn.add(null, center, Rn.times(null, t, V));
			System.err.println(i+"=i. Point found ln.pt = "+Rn.innerProduct(ln, pt));
			return pt;
		}
		return null;
	}
	
	public double[] getCenterPoint()	{
		return getPolarPoint(origin);
	}
	
	public double[] getPolarPoint(double[] ln)	{
		return Rn.matrixTimesVector(null, dconic, ln);
	}
	
	public double[] getPolarLine(double[] pt)	{
		return Rn.matrixTimesVector(null, conic, pt);
	}
	
	public double[][] intersectLineWithConic(double[] ln) {
		return null;		
	}
	
	public static double aQa(double[] conic, double[] el)	{
		return aQb(el, conic, el);
	}
	
	public static double aQb(double[] el1, double[] conic,  double[] el2)	{
		return Rn.innerProduct(el1, Rn.matrixTimesVector(null, conic, el2));
	}
	
	// take a curve of points and break it into segments where ever it crosses the line at infinity
	protected double[][] mkCurveProj(double[][] arr, boolean closed)	{
		int n = arr.length;
		return arr;
	}
//	let mkcurveproj = (arr, closed=true)=>{let n = arr.length,
//			segs = arr.map((x,i)=> [x,arr[(i+1)%n]]);
//			if (!closed) segs.pop();
//			let finsegs = segs.filter(([p0,p1])=>p0.e12 * p1.e12 > 0);
//		if (arr.length == finsegs.length) return finsegs;
//		let infinsegs = segs.filter(([p0,p1])=>p0.e12 * p1.e12 <= 0),
//			infinarr = [];
//		infinsegs.map(([p0, p1])=> {
//			let np0 = p0.Normalized, np1 = p1.Normalized;
//			let V0 =(1.0001*np0 + np1), 
//				V1 = (np0 + 1.0001*np1);
//			infinarr.push([p0, V0]);
//			infinarr.push([V1, p1]);
//		});
//		return finsegs.concat(infinarr);
//	}
	static double[] Q = {
			0,.5,0,.5,0,0,0,0,-3
	};
	
	public static void main(String[] args) {
		ConicSectionAlgebraic csa =  new ConicSectionAlgebraic(Q);
//		double[][] cuts = csa.intersectLineWithConic(new double[] {1,0,0});
//		System.err.println("point on conic = "+Rn.toString(csa.getPointOnConic()));
		double[][] cv = csa.getConicCurve();
		System.err.println("curve = "+Rn.toString(cv));

		
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent();
		world.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.unitCube);
		world.setGeometry(IndexedLineSetUtility.createCurveFromPoints(cv, true));
		View.setTitle("The Conic");
		JRViewer v = JRViewer.createJRViewer(world);
		
		v.startup();

	}
}
