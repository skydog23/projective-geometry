package charlesgunn.jreality.geometry.projective;

import java.security.InvalidParameterException;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.math.p5.PlueckerLineGeometry;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.LoggingSystem;

public class PointRangeFactory extends Abstract1DExtentFactory{

	boolean ilsDirty = true;
	IndexedLineSet theLine;
	public IndexedLineSetFactory lineFactory = new IndexedLineSetFactory();
	boolean finiteSphere = true;
	double sphereRadius = 10; //10E5;
	boolean	doubled = true;
	transient boolean firstTime = true;
	double[] center = {0,0,0,1};
	double[] oldPoint = null,
			plueckerLine = null;
	
	public boolean isFiniteSphere() {
		return finiteSphere;
	}

	public void setFiniteSphere(boolean finiteSphere) {
		this.finiteSphere = finiteSphere;
	}

	public double getSphereRadius() {
		return sphereRadius;
	}

	public void setSphereRadius(double sphereRadius) {
		this.sphereRadius = sphereRadius;
	}
	
	public boolean isDoubled() {
		return doubled;
	}

	public void setDoubled(boolean doubled) {
		this.doubled = doubled;
	}


	public double[] getCenter() {
		return center;
	}

	public void setCenter(double[] center) {
		this.center = center.clone();
//		System.err.println("Center = "+Rn.toString(center));
	}

	public double[] getOldPoint() {
		return oldPoint;
	}

	public void setOldPoint(double[] oldPoint) {
		this.oldPoint = oldPoint;
	}

	public IndexedLineSet getLine() {
//		if (ilsDirty)	{
//			update();
//		}
		return lineFactory.getIndexedLineSet();
	}
	public IndexedLineSetFactory getLineFactory() {
		return lineFactory;
	}
	private double[][] cutpoints = new double[2][4];
	public void update()	{
//		boolean hit = theLine == null ? false : theLine.getGeometry().getName().equals("3rd line");
		boolean isValid = true;
		if (finiteSphere)	{
//			numSegs=2;			// questionable
			if (samples == null) 
				samples = new double[numSegs][4];
			isValid = intersectLineWithSphere(cutpoints, 0, element0, element1, oldPoint, sphereRadius);
			if (isValid)	{
//				LoggingSystem.getLogger(this).fine("v0: "+Rn.toString(samples[offset]));
//				LoggingSystem.getLogger(this).fine("v1: "+Rn.toString(samples[offset+1]));	
				if (numSegs >= 2) {
					for (int i = 0; i<numSegs; ++i)	{
						double t = i/(numSegs-1.0);
						AnimationUtility.linearInterpolation(samples[i+offset], t, 0,1, cutpoints[0],  cutpoints[1]);
					}
//					System.err.println("points = "+Rn.toString(new double[][]{element0, element1}));
//					System.err.println("cut points = \n"+Rn.toString(cutpoints));
				}
			} else  {
				LoggingSystem.getLogger(this).info("Lies outside sphere.");
			}
		}
		else 
			samples = LineUtility.samplesOn1DExtent(samples, offset, numSegs, element0, element1, doubled);
//		System.err.println("samples = \n"+Rn.toString(samples));
		//ilsDirty = true;
//		System.err.println("num segs = "+samples.length);
		if (isValid && numSegs > 0) {
			if (firstTime) {
				lineFactory = IndexedLineSetUtility.createCurveFactoryFromPoints(lineFactory, samples, !finiteSphere);
				theLine = lineFactory.getIndexedLineSet();
				firstTime = false;
			} 
			else {
				lineFactory.setVertexCoordinates(samples);
				lineFactory.update();
			}
			//IndexedLineSetUtility.createCurveFromPoints(theLine, samples, !finiteSphere);	
		}
//		else theLine = null;
		ilsDirty = false;
	}

	public  boolean intersectLineWithSphere(double[][] result, int offset2, double[] p0, double[] p1, double radius) {
		return intersectLineWithSphere(result, offset2, p0, p1, null, radius);
	}
	public  boolean intersectLineWithSphere(double[][] result, int offset2, double[] p0x, double[] p1x, double[] oldP0, double radius) {
		double[] p0 = p0x.clone(), p1 = p1x.clone();
		double[] ct = center;
		if (center.length == 3) ct = Pn.homogenize(null, center);
		else {
			ct = Pn.dehomogenize(null, center);
		}
		ct[3] = 0.0;
		if (result == null) {
			throw new InvalidParameterException("result can't be null");
		}
		// intersect line with sphere
		// p0 + t(p1-p0)  intersect |P| = r
		if (Math.abs(p0[3]) < Math.abs(p1[3])) {
			double[] tmp = p1;
			p1 = p0;
			p0 = tmp;
		}
		Pn.dehomogenize(p0, p0);
		Pn.dehomogenize(p1, p1);
		double[] v0 = p0;
		double[] v = null;
		if (p0[3] == 0.0) {
			v0 = p1;
			v =  p0;
		} else if (p1[3] == 0.0) {
			v = p1;
		}
		else v = Rn.subtract(null, p1, p0);
		Rn.subtract(v0, v0, ct);
		double a = Rn.innerProduct(v, v, 3);
		double b = 2 * Rn.innerProduct(v0, v, 3);
		double c = Rn.innerProduct(v0, v0, 3) - radius*radius;
		double d = b*b-4*a*c;
		if (d < 0) {
			return false;
		}
		d = Math.sqrt(d);
		double r0 = (-b+d)/(2*a);
		double r1 = (-b-d)/(2*a);
		Rn.add(result[0+offset2], Rn.linearCombination(null, 1, v0, r0, v), ct);
		Rn.add(result[1+offset2], Rn.linearCombination(null, 1, v0, r1, v), ct);
		Pn.dehomogenize(result[0+offset2], result[0+offset2]);
		Pn.dehomogenize(result[1+offset2], result[1+offset2]);
		if (oldP0 != null) {
			double d0 = Pn.distanceBetween(result[0+offset2], oldP0, Pn.EUCLIDEAN);
			double d1 = Pn.distanceBetween(result[1+offset2], oldP0, Pn.EUCLIDEAN);
			if (d0 > d1) { // swap
				double[] tmp = result[0+offset2];
				result[0+offset2] = result[1+offset2];
				result[1+offset2] = tmp;
			}
		}
//		System.err.println("v0 = "+Rn.toString(v0));
//		System.err.println("v = "+Rn.toString(v));
//		System.err.println("Point 0 = "+Rn.toString(result[0+offset]));
//		System.err.println("Point 1 = "+Rn.toString(result[1+offset]));
		return true;
	}

	public static IndexedLineSet line(double[] pt0, double[] pt1)	{
		return line(pt0, pt1, 12);
	}

	public static IndexedLineSet line(double[] pt0, double[] pt1, int numSegs)	{
		double[] pt04 = pt0, pt14 = pt1;
		if (pt0.length == 3 || pt1.length == 3)	{
			pt04 = new double[]{pt0[0], pt0[1], 0, pt0[2]};
			pt14 = new double[]{pt1[0], pt1[1], 0, pt1[2]};
		}
		double[][] verts = LineUtility.coordinatesFor1DExtent(null, 0, numSegs, pt04, pt14);
		return IndexedLineSetUtility.createCurveFromPoints(verts, true);
	}

	public void setPluckerLine(double[] pc)	{
		plueckerLine = pc.clone();
		double[][] pts = LineUtility.twoPointsOnLine(null, pc);
//		System.err.println("setPL: \n"+Rn.toString(pts));
		setElement0(pts[0]);
		setElement1(pts[1]);
		return;
	}
	
	public void set2DLine(double[] abc)	{
		double[] pc = {abc[2], 0, -abc[1], 0, -abc[0], 0, 0};
//		double[] pc = {0,0, abc[2],  0,  abc[1],abc[0]};
		double[][] pts = LineUtility.twoPointsOnLine(null, pc);
//		System.err.println("points = "+Rn.toString(pts));
		setElement0(pts[0]);
		setElement1(pts[1]);
		return;
	}
	

	
	public double[] getPluckerLine() {
		if (plueckerLine != null) return plueckerLine;
		if (element0 == null || element1 == null)	
			return null;
		return PlueckerLineGeometry.lineFromPoints(null, element0, element1);
	}
	static int count = 0;
	public QuadMeshFactory getTubedLine(QuadMeshFactory ifsf, double r, int circleRes)	{
		double[] plueckerLine = getPluckerLine();
		double[] polarLine = PlueckerLineGeometry.polarize(null, plueckerLine, Pn.ELLIPTIC);
		circleRes = 2*(circleRes/2);
		PointRangeFactory polarF = new PointRangeFactory();
		polarF.setFiniteSphere(false);
		polarF.setPluckerLine(polarLine);
		polarF.setNumberOfSamples(circleRes);
		polarF.update();
		double[][] polarSamples = polarF.getSamples();
//		System.err.println("polar samples are: \n"+Rn.toString(polarSamples));
		double[][][] verts = new double[numSegs+1][circleRes+1][];
		for (int i = 0; i<=numSegs; ++i)	{
			for (int j = 0; j<= circleRes; ++j)	{
				verts[(i)][j] = Pn.dragTowards(null, samples[i%numSegs], polarSamples[j%circleRes], r, Pn.ELLIPTIC);
			}
		}
		if (ifsf == null) ifsf = new QuadMeshFactory();
		ifsf.setMetric(Pn.ELLIPTIC);
		ifsf.setClosedInUDirection(true);
		ifsf.setClosedInVDirection(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateVertexNormals(true);
		ifsf.setGenerateTextureCoordinates(true);
		ifsf.setULineCount(circleRes+1);
		ifsf.setVLineCount(numSegs+1);
		ifsf.setVertexCoordinates(verts);
		ifsf.update();
		ifsf.getGeometry().setName("tubedLine"+count++);
		ifsf.getGeometry().setGeometryAttributes(CommonAttributes.METRIC, Pn.ELLIPTIC);
		return ifsf;
	}
}
