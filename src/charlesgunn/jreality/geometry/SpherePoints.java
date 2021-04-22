/*
 * Created on Mar 22, 2009
 *
 */
package charlesgunn.jreality.geometry;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import charlesgunn.util.TextSlider;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Rn;
import de.jreality.scene.PointSet;
import de.jtem.discretegroup.util.WingedEdge;

public class SpherePoints		{
	int pointCount = 10;
	PointSetFactory psf = new PointSetFactory();
	WingedEdge cutPlanes;
	double[][] points;
	double scaleFactor = 1.0,
		timeStep = .1, 
		powerLaw = 2;
	boolean animating = true;
	public SpherePoints(int c)	{
		pointCount = c;
		initialize();
	}
	
	public void initialize()	{
		points = new double[pointCount][3];
		for (int i = 0; i<pointCount; ++i)	{
			for (int j = 0; j<3; ++j) {
				points[i][j] =  Math.random()-.5;
			}
			Rn.normalize(points[i], points[i]);
		}
		psf.setVertexCount(pointCount);
		psf.setVertexCoordinates(points);
		psf.update();
	}
	
	public void update()	{
		double[][] forces = new double[pointCount][3];
		double[] vec = new double[3];
		for (int i = 0; i<pointCount; ++i)	{
			for (int j = i+1; j<pointCount; ++j)	{
				double d = Math.acos(Rn.innerProduct(points[i], points[j]));
				d = scaleFactor/d;
				d = Math.pow(d, powerLaw);
				Rn.subtract(vec, points[i], points[j]);
				double[] times = Rn.times(null, d, vec);
				Rn.add(forces[i], forces[i], times);
				Rn.subtract(forces[j], forces[j], times);
			}
		}
		for (int i = 0; i<pointCount; ++i)	{
			Rn.add(points[i], points[i], Rn.times(null, timeStep, forces[i]));
			Rn.setToLength(points[i], points[i], 1.0);
		}
		psf.setVertexCoordinates(points);
		psf.update();
		cutPlanes = null;
	}
	
	public PointSet getPointSet()	{
		return psf.getPointSet();
	}
	
	public WingedEdge getWingedEdge()	{
		if (cutPlanes == null)	{
			cutPlanes = new WingedEdge();
			double[][] planes = new double[pointCount][4];
			for (int i = 0; i<pointCount; ++i)	{
				System.arraycopy(points[i], 0, planes[i], 0, 3);
				planes[i][3] = -1.0;
			}
			cutPlanes.cutWithPlane(planes);
		}
			
		return cutPlanes;
	}	
	
	public Component getInspector() {
		Box container = Box.createVerticalBox();
		container.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder(BorderFactory
						.createEtchedBorder(), "Sphere points parameters")));

		final TextSlider.IntegerLog RSlider = new TextSlider.IntegerLog("count",
				SwingConstants.HORIZONTAL, 2, 2000, pointCount);
		RSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pointCount = RSlider.getValue().intValue();
				System.err.println("point count = "+pointCount);
				initialize();
			}
		});
		container.add(RSlider);
		
		final TextSlider.DoubleLog tSlider = new TextSlider.DoubleLog("timeStep",
				SwingConstants.HORIZONTAL, .00001, 10, timeStep);
		tSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timeStep = tSlider.getValue();
			}
		});
		container.add(tSlider);
		
		final TextSlider.Double  pSlider = new TextSlider.Double("power law",
				SwingConstants.HORIZONTAL, 1, 5, powerLaw);
		pSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				powerLaw = pSlider.getValue();
			}
		});
		container.add(pSlider);
		
		return container;
	}

}
