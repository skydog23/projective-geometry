/*
 * Created on May 12, 2021
 *
 */
package charlesgunn.jreality.geometry.projective;

import java.awt.Color;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;

public class SurfaceElementFactory {

	protected double[][] points, planes;
	
	protected int numEls, metric;
	protected double diskRadius;
	protected Color pointColor, planeColor;
	protected Color[] pointColors, planeColors;
	protected SceneGraphComponent sgcRepn = SceneGraphUtility.createFullSceneGraphComponent("surface element factory");
	private SurfaceElement[] ses;

	
	public double[][] getPoints() {
		return points;
	}
	public void setPoints(double[][] points) {
		this.points = points;
	}
	public double[][] getPlanes() {
		return planes;
	}
	public void setPlanes(double[][] planes) {
		this.planes = planes;
	}
	public int getNumEls() {
		return numEls;
	}
	public void setNumEls(int numEls) {
		this.numEls = numEls;
	}
	public int getMetric() {
		return metric;
	}
	public void setMetric(int metric) {
		this.metric = metric;
	}
	public double getDiskRadius() {
		return diskRadius;
	}
	public void setDiskRadius(double diskRadius) {
		this.diskRadius = diskRadius;
	}
	public Color getPointColor() {
		return pointColor;
	}
	public void setPointColor(Color pointColor) {
		this.pointColor = pointColor;
	}
	public Color getPlaneColor() {
		return planeColor;
	}
	public void setPlaneColor(Color planeColor) {
		this.planeColor = planeColor;
	}
	public SceneGraphComponent getSgcRepn() {
		return sgcRepn;
	}
	
	public void update()	{
		if (points == null || planes == null) {
			System.err.println("SurfaceElementFactory.update(): Invalid state");
			return;
		}
		boolean hasSGC = true;
		if (sgcRepn.getChildComponentCount() != numEls) {
			sgcRepn.removeAllChildren();
			hasSGC = false;
		}
		ses = new SurfaceElement[numEls];
		for (int i = 0; i<numEls; ++i)	{
			Color ptC = (pointColors != null) ? pointColors[i%numEls] : pointColor;
			Color plC = (planeColors != null) ? planeColors[i%numEls] : planeColor;
			SceneGraphComponent foo = SurfaceElement.surfaceElement(
					hasSGC ? sgcRepn.getChildComponent(i) : null, 
					points[i%numEls], planes[i%numEls], diskRadius, metric);
			if (!hasSGC) sgcRepn.addChild(foo);
		}
	}
	
	
	
}
