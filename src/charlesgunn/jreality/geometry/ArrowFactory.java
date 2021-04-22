package charlesgunn.jreality.geometry;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.Pn;
import de.jreality.scene.IndexedLineSet;

public class ArrowFactory {

	IndexedLineSet theArrow;
	double[][] verts = new double[3][4];
	double[] base = {0,0,1}, direction = {1,0,0};
	int metric = Pn.EUCLIDEAN;
	double length = 1.0, scale = .1, angle = Math.PI/4.0;
	
	IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
	{
		verts[0][3] = verts[1][3] = verts[2][3] = 1.0;
		update();
		ilsf.update();
	}
	
	public void update()	{
		
	}
	
	
	public IndexedLineSet getTheArrow() {
		return theArrow;
	}


	public double getScale() {
		return scale;
	}


	public void setScale(double scale) {
		this.scale = scale;
	}


	public double getAngle() {
		return angle;
	}


	public void setAngle(double angle) {
		this.angle = angle;
	}


	public int getMetric() {
		return metric;
	}


	public void setMetric(int metric) {
		this.metric = metric;
	}

}
