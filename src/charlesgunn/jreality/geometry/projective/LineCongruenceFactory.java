package charlesgunn.jreality.geometry.projective;

import java.awt.Color;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;
import de.jtem.projgeom.PlueckerLineGeometry;

/**
 * For now, all lines are represented by 6-vector Plucker coordinates and are assumed to represent 
 * rays, not axes (Strahlen, not Axen ).
 * @author Charles Gunn
 *
 */public class LineCongruenceFactory {
	double[] line0, line1;
	int numberPencils0 = 15, numberPencils1 = 15, numberJoints = 12;
	boolean finiteSphere = false;
	double sphereRadius = 10E5;
	int metric = Pn.ELLIPTIC;
	SceneGraphComponent sgc = null;
	
	public double[] getLine0() {
		return line0;
	}
	public void setLine0(double[] line0) {
		this.line0 = line0;
	}
	public double[] getLine1() {
		return line1;
	}
	public void setLine1(double[] line1) {
		this.line1 = line1;
	}
	public int getNumberPencils0() {
		return numberPencils0;
	}
	public void setNumberPencils0(int numberPencils0) {
		this.numberPencils0 = numberPencils0;
	}
	public int getNumberPencils1() {
		return numberPencils1;
	}
	public void setNumberPencils1(int numberPencils1) {
		this.numberPencils1 = numberPencils1;
	}
	public  boolean isFiniteSphere() {
		return finiteSphere;
	}
	public  void setFiniteSphere(boolean finiteSphere) {
		this.finiteSphere = finiteSphere;
		if (isFiniteSphere()) setNumberJoints(2);
	}
	public double getSphereRadius() {
		return sphereRadius;
	}
	public void setSphereRadius(double sphereRadius) {
		this.sphereRadius = sphereRadius;
	}
	
	public void update()	{
		if (sgc == null)	{
			sgc = SceneGraphUtility.createFullSceneGraphComponent("lineCongruenceFactory");
			sgc.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", Color.white);
			sgc.getAppearance().setAttribute("lineShader.polygonShader.ambientColor", Color.white);
			sgc.getAppearance().setAttribute("lineShader.polygonShader.ambientCoefficient", .2);
			sgc.getAppearance().setAttribute("lineShader.diffuseColor", Color.white);
		} else 
			SceneGraphUtility.removeChildren(sgc);
		// the columns of polarizer are the intersection of the line with the coordinate planes
//		double[][] points = LineUtility.twoPointsOnLine(null, line0);
//		double[] p0 = points[0];
//		double[] p1 = points[1];
		double[][] verts = new double[numberPencils0][4];
		LineUtility.samplesOnLine(verts,numberPencils0,line0, false);
		double[][] planes = new double[numberPencils0][4];
		double[] polarizer = PlueckerLineGeometry.lineToSkewMatrix(null, line1);//PlueckerLineGeometry.dualizePlueckerLine(null, line1));
		Rn.matrixTimesVector(planes, polarizer, verts);
		// verts now contains numberPencils0 equally spaced (elliptic measure) points on line0
		for (int i = 0; i<numberPencils0; ++i)	{
			LinePencilFactory lpf = new LinePencilFactory();
			lpf.setNumLines(numberPencils1);
			lpf.setNumberJoints(numberJoints);
			lpf.setFiniteSphere(finiteSphere);
			lpf.setSphereRadius(sphereRadius);
			lpf.setPoint(verts[i]);
			lpf.setPlane(planes[i]);
			lpf.setLine(line1);
			lpf.setMetric(metric);
			lpf.update();
			sgc.addChild(lpf.getPencil());
		}
		
	}
	
	public SceneGraphComponent getSceneGraphComponent()	{
		if (sgc == null) update();
		return sgc;
	}
	public int getNumberJoints() {
		return numberJoints;
	}
	public void setNumberJoints(int numberJoints) {
		this.numberJoints = numberJoints;
	}
	
	public double[] getLineThroughPoint(double[] P)	{
		double[] phi = PlueckerLineGeometry.lineJoinPoint(null, line0, P);
		double[] mi = PlueckerLineGeometry.lineJoinPoint(null, line1, P);
		return PlueckerLineGeometry.lineFromPlanes(null, phi, mi);
	}
	
	public double[] getLineInPlane(double[] P)	{
		double[] phi = PlueckerLineGeometry.lineIntersectPlane(null, line0, P);
		double[] mi = PlueckerLineGeometry.lineIntersectPlane(null, line1, P);
		return PlueckerLineGeometry.lineFromPlanes(null, phi, mi);
	}
	public void setMetric(int s) {
		metric = s;
	}
}
