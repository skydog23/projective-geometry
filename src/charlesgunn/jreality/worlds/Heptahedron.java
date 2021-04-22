/*
 * Created on Dec 8, 2010
 *
 */
package charlesgunn.jreality.worlds;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

public class Heptahedron extends LoadableScene {

	double[][] verts = new double[7][4];
	int[][] indices = {{0,1,2},{0,1,3},{1,2,4},{2,0,5},{0,3,6,5},{1,4,6,3},{2,5,6,4}};
	IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
	double time = 0.0;
	Matrix rot120 = new Matrix(), rot240 = new Matrix(), rotateTriangle = new Matrix();
	double[] tip = new double[3];
	@Override
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		verts[0] = new double[]{Math.sqrt(3.0)/3.0, 0, 0, 1};
		MatrixBuilder.euclidean().rotateZ(Math.PI*2.0/3.0).assignTo(rot120);
		MatrixBuilder.euclidean().rotateZ(Math.PI*4.0/3.0).assignTo(rot240);
		Rn.matrixTimesVector(verts[1], rot120.getArray(), verts[0]);
		Rn.matrixTimesVector(verts[2], rot240.getArray(), verts[0]);
		tip = new double[]{-Math.sqrt(3.0)/6,0, Math.sqrt(3.0)/2.0, 1.0};
		ifsf.setVertexCount(7);
		ifsf.setFaceCount(7);
		ifsf.setFaceIndices(indices);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.WHITE);
		world.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		update();
		world.setGeometry(ifsf.getGeometry());
		return world;
	}
	double[][] planes = new double[3][4];
	private void update()		{
		P3.makeRotationMatrix(rotateTriangle.getArray(), verts[1], verts[2], Math.PI*time, Pn.EUCLIDEAN);
		Rn.matrixTimesVector(verts[4], rotateTriangle.getArray(), tip);
		Rn.matrixTimesVector(verts[5], rot120.getArray(), verts[4]);
		Rn.matrixTimesVector(verts[3], rot240.getArray(), verts[4]);
		for (int i = 4; i<7; ++i)	{
			int[] j = indices[i];
			planes[i-4] = P3.planeFromPoints(planes[i-4], verts[j[0]], verts[j[1]], verts[j[3]]);
		}
		P3.pointFromPlanes(verts[6], planes[0], planes[1], planes[2]);
		Pn.dehomogenize(verts[6], verts[6]);
		ifsf.setVertexCoordinates(verts);
		ifsf.update();
		double d1 = Pn.distanceBetween(verts[4], verts[5], Pn.EUCLIDEAN);
		double d2 = Pn.distanceBetween(verts[0], verts[6], Pn.EUCLIDEAN);
		double qarea = .5 * d1 * d2;
		double diagratio = d2/d1;
		double tarea = Math.sqrt(3.0)/4.0;
		System.err.println("d1 = "+d1+" ARatio = "+qarea/tarea + " angle = "+(90+ 180*time));
		
	}

	public boolean hasInspector() {return true; }
	public Component getInspector(final Viewer v) {	
		Box inspectionPanel =  Box.createVerticalBox();
		final TextSlider timeSlider = new TextSlider.Double("t",SwingConstants.HORIZONTAL, -1.0, 1.0, time);
		timeSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				time = timeSlider.getValue().doubleValue();
				update();
			}
		});
		inspectionPanel.add(timeSlider);
		return inspectionPanel;
	}
}
