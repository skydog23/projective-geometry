package charlesgunn.jreality.geometry.projective;

import charlesgunn.math.p5.P5;
import charlesgunn.math.p5.PlueckerLineGeometry;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class LinePencilFactory {

	boolean finiteSphere,
		fan = false;
	double sphereRadius = 10E2;
	double[] center = {0,0,0,1};
	double[] point, plane;
	// alternate way to define the pencil: two intersecting lines
	double[][] intersectingLines = new double[2][];
	int numberJoints = 12, numLines, metric = Pn.ELLIPTIC;
	double  	tolerance = 10E-8;
	double[] times;

	transient double[] helpingLine, line0, line1;
	transient SceneGraphComponent pencilSGC = SceneGraphUtility.createFullSceneGraphComponent("linePencil");
	transient double[][] pluckerLines, pluckerLinesSet;
	
	public int getNumLines() {
		return numLines;
	}
	public void setNumLines(int numLines) {
		this.numLines = numLines;
	}
	public int getNumberJoints() {
		return numberJoints;
	}
	public void setNumberJoints(int numSegs) {
		this.numberJoints = numSegs;
	}
	public double[] getPlane() {
		return plane;
	}
	public void setPlane(double[] plane) {
		this.plane = plane;
	}
	public double[] getPoint() {
		return point;
	}
	public void setPoint(double[] point) {
		this.point = point;
	}
	public SceneGraphComponent getPencil() {
		return pencilSGC;
	}
	public int getMetric() {
		return metric;
	}
	public void setMetric(int metric) {
		this.metric = metric;
	}
	public  boolean isFiniteSphere() {
		return finiteSphere;
	}
	public  void setFiniteSphere(boolean finiteSphere) {
		this.finiteSphere = finiteSphere;
		if (finiteSphere) setNumberJoints(2);
	}
	public double getSphereRadius() {
		return sphereRadius;
	}
	public void setSphereRadius(double sphereRadius) {
		this.sphereRadius = sphereRadius;
	}
	public double[] getCenter() {
		return center;
	}
	public void setCenter(double[] center) {
		this.center = center;
	}
	public double[] getTimes() {
		return times;
	}
	public void setTimes(double[] times) {
		this.times = times;
	}
	public double[] getLine() {
		return helpingLine;
	}
	public void setLine(double[] line) {
		this.helpingLine = line;
		plane = PlueckerLineGeometry.lineJoinPoint(null, line, point);
	}
	public void update()	{
		if ((pluckerLinesSet==null) && Rn.innerProduct(point, plane) >= tolerance)	{
			throw new IllegalStateException("Point and plane must be incident");
		}
		int[][] indices = new int[numLines][numberJoints+(finiteSphere ? 0 : 1)];
		double[][] verts = new double[numLines*numberJoints][4];
		pluckerLines = new double[numLines][];
		if (pluckerLinesSet == null) computeLines();
		else pluckerLines = pluckerLinesSet;
		int foo = 0;
		for (int i = 0; i<numLines; ++i)	{
			foo = i*(numberJoints);
			PointRangeFactory lf = new PointRangeFactory();
			lf.setNumberOfSamples(numberJoints);
			lf.setOffset(foo);
			lf.setVertices(verts);
			// TODO figure out why I do the following: looks like I should set the pluecker line OR the two elements.
			lf.setPluckerLine(pluckerLines[i]);
//			System.err.println(i+" pluecker line ="+Rn.toString(pluckerLines[i]));
//			lf.setElement0(point);
//			lf.setElement1(samples[i]);
			lf.setSphereRadius(sphereRadius);
			lf.setCenter(point);
			lf.setDoubled(!finiteSphere);
			lf.setFiniteSphere(finiteSphere);
			lf.update();
			for (int j = 0; j<=numberJoints; ++j) {
				if (j < numberJoints || !finiteSphere) indices[i][j] = foo+(j%numberJoints);				
			}
		}
//		ils = IndexedLineSetUtility.setIndexedLineSetFrom(ils, indices, verts, null, null);
		IndexedLineSetFactory ifsf = new IndexedLineSetFactory();
		ifsf.setVertexCount(verts.length);
		ifsf.setVertexCoordinates(verts);
		ifsf.setEdgeCount(indices.length);
		ifsf.setEdgeIndices(indices);
		ifsf.update();
		IndexedLineSet ils = ifsf.getIndexedLineSet();
		ils.setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		pencilSGC.setGeometry(ils);
	}
	
	// calculate a set of lines lying in this pencil via a "helping line"
	// which lies in the plane of the pencil. (The helping line is then sampled
	// and the samples joined to the center to obtain the lines of the pencil.)
	// This helping line is obtained
	// as the intersection of the plane of the pencil with a second plane,
	// which generically is the elliptic polar plane of the point.  Important
	// of course is that this helping line does not contain the point of the pencil.
	// A better alternative would be to find two elliptically perpendicular lines
	// in the pencil and use them directly to generate the elements of the pencil.
	private void computeLines() {
		if (times != null)	{
			setNumLines(times.length);
		}

		if (fan && line0 != null && line1 != null)	{
			pluckerLines = new double[numLines][6];
			if (times != null) {
				for (int i = 0; i<numLines; ++i)	{
					pluckerLines[i] = LineUtility.valueAtTime(times[i], line0, line1);
				}
				return;
			}
			LineUtility.ellipticSegment(pluckerLines, line0, line1, numLines);
			return;
		}		
		double[][] samples = new double[numLines][4];
		// HACK  we need a second plane which is different from plane
		// generic case: choose the ideal plane, then the intersection is the ideal line of the plane
		double[] planex = P3.originP3.clone();
		if (Math.abs(point[3]) < 10E-8)  { // point is "at infinity"
			planex = point; 
		}
		helpingLine = PlueckerLineGeometry.lineFromPlanes(null, plane, planex);
		LineUtility.samplesOnLine(samples, numLines, helpingLine, false);
		for (int i = 0; i<numLines; ++i)	{
			pluckerLines[i] = PlueckerLineGeometry.lineFromPoints(null, point, samples[i]);
		}
	}
	
	public static LinePencilFactory linePencilFactoryForIntersectingLines(
			LinePencilFactory lpf, double[] line0, double[] line1) {
		if (lpf == null) lpf = new LinePencilFactory();
		if (Math.abs(P5.innerProduct(line0, line1, P5.LINE_SPACE)) > 10E-8 ) {
			throw new IllegalStateException("not intersecting");
		}
		lpf.point = PlueckerLineGeometry.intersectionPoint(lpf.point, line0, line1);
		lpf.plane = PlueckerLineGeometry.intersectionPlane(lpf.plane, line0, line1);
		lpf.line0 = line0;
		lpf.line1 = line1;
//		lpf.update();
		return lpf;
	}
	
	public double[][] getPluckerLines() {
		return pluckerLines;
	}

	public void setPluckerLines(double[][] pl) {
		pluckerLinesSet = pl;
	}
	
	public void setIntersectingLines(double[] line0, double[] line1) {
		linePencilFactoryForIntersectingLines(this, line0, line1);
	}
	public boolean isFan() {
		return fan;
	}
	public void setFan(boolean fan) {
		this.fan = fan;
	}
	
	public static double[][] intersectionPoints(double[][] pts, LinePencilFactory lpf, PointRangeFactory prf)	{
		double[] line = prf.getPluckerLine();
		if (pts == null || pts.length != lpf.getNumLines()) pts = new double[lpf.getNumLines()][];
		for (int i = 0; i<pts.length; ++i)	{
			pts[i] = PlueckerLineGeometry.intersectionPoint(null, line, lpf.pluckerLines[i]);
			Pn.dehomogenize(pts[i], pts[i]);
			System.err.println("intersection = "+Rn.toString(pts[i]));
		}
		return pts;
	}
}
