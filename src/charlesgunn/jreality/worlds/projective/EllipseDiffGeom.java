/*
 * Created on Apr 1, 2015
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.SwingConstants;

import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

public class EllipseDiffGeom extends Assignment {

	int size = 101;
	@Override
	public void display() {
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().
			setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(100,100,100));
		Component comp = ((Component) jrviewer.getViewer().getViewingComponent());
		comp.addKeyListener(new KeyAdapter() {
 				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_H:
						System.err.println("	1: toggle normals");
						break;
		
					case KeyEvent.VK_1:
						normals.setVisible(!normals.isVisible());
						break;
					}
				}
			});
	}

	double b = .5, time = 1.0;
	IndexedLineSetFactory curveFac = null, normFac = null, evoluteFac = null;
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world"),
			points = SceneGraphUtility.createFullSceneGraphComponent("points"),
			evolute = SceneGraphUtility.createFullSceneGraphComponent("evolute"),
			normals = SceneGraphUtility.createFullSceneGraphComponent("normals");
	@Override
	public SceneGraphComponent getContent() {
		normFac = new IndexedLineSetFactory();
		update();
		world.addChildren(points, normals, evolute);
		Appearance ap = world.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", Color.white);
		ap.setAttribute("pointShader.diffuseColor", new Color(255, 150, 150));
		ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, .0075);
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .005);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap = normals.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", new Color(255, 255, 150));
		return world;
	}

	public void update()	{
		double pts[][] = new double[size][4],
				evo[][] = new double[size][4],
				nns[][] = new double[2*size][4];
		int[][] nind = new int[size][2], curveNoCusps = new int[size][2];
		for (int i =0; i<size; ++i)	{
			double angle = (i*Math.PI*2)/(size-1);
			pts[i] = new double[]{Math.cos(angle), b*Math.sin(angle),0,1.0};
			nns[2*i] = pts[i];
			double[] normVec = {-b*Math.cos(angle), -Math.sin(angle), 0, 0};
			Rn.normalize(normVec, normVec);
			nns[2*i+1] = Rn.add(null, pts[i], Rn.times(null, time, normVec ));
			evo[i] = nns[2*i+1];
			nind[i][0] = 2*i;
			nind[i][1] = 2*i+1;
			curveNoCusps[i][0] = i;
			curveNoCusps[i][1] = (i+1)%size;
		}
		curveFac = IndexedLineSetUtility.createCurveFactoryFromPoints(curveFac, pts, false);
		if (points.getGeometry() == null) points.setGeometry(curveFac.getGeometry());
		evoluteFac = IndexedLineSetUtility.createCurveFactoryFromPoints(evoluteFac, evo, false);
		evoluteFac.setEdgeCount(size);
		evoluteFac.setEdgeIndices(curveNoCusps);
		evoluteFac.update();
		if (evolute.getGeometry() == null) evolute.setGeometry(evoluteFac.getGeometry());
		normFac.setVertexCount(2*size);
		normFac.setVertexCoordinates(nns);
		normFac.setEdgeCount(size);
		normFac.setEdgeIndices(nind);
		normFac.update();
		normals.setGeometry(normFac.getGeometry());

	}
	@Override
	public Component getInspector() {
		final TextSlider eSlider = new TextSlider.Double("time",  SwingConstants.HORIZONTAL, -2, 2, time);
		eSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				time = eSlider.getValue().doubleValue();
				update();
			}
		});
		inspector.add(eSlider);
		final TextSlider bSlider = new TextSlider.Double("b",  SwingConstants.HORIZONTAL, 0, 1, b);
		bSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				b = bSlider.getValue().doubleValue();
				update();
			}
		});
		inspector.add(bSlider);

		return inspector;
	}

	public static void main(String[] args) {
		new EllipseDiffGeom().display();
	}
}
