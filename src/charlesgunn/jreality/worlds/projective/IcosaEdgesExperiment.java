/*
 * Created on 03.10.2016
 *
 */
package charlesgunn.jreality.worlds.projective;

import static de.jreality.geometry.GeometryUtility.BOUNDING_BOX;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.SwingConstants;

import android.R.color;
import charlesgunn.jreality.geometry.projective.LinePencilFactory;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.p5.PlueckerLineGeometry;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class IcosaEdgesExperiment extends Assignment {

	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	SceneGraphComponent sphereSGC = SceneGraphUtility.createFullSceneGraphComponent("sphere");
	SceneGraphComponent tworld = SceneGraphUtility.createFullSceneGraphComponent("lines");
	double scale = 1.0;
	@Override
	public SceneGraphComponent getContent() {
		
		IndexedFaceSet icosa = Primitives.icosahedron();
		double[][] icov = icosa.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		int [][] edges = icosa.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null);
		
		for (int i = 0; i<edges.length; ++i)	{
			int i1 = edges[i][0], i2 = edges[i][1];
			System.err.println("edge "+i+" "+i1+":"+i2);
			PointRangeFactory prf = new PointRangeFactory();
			prf.setElement0(icov[i1]);
			prf.setElement1(icov[i2]);
			prf.setFiniteSphere(false);
			prf.setSphereRadius(50);
			prf.update();
			prf.getLine().setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
			SceneGraphComponent child = SceneGraphUtility.createFullSceneGraphComponent("child"+i);
			child.setGeometry(prf.getLine());
			tworld.addChild(child);
		}
		Appearance ap = world.getAppearance();
		ap.setAttribute(BOUNDING_BOX, Rectangle3D.unitCube);
		world.getAppearance().setAttribute("lineShader.diffuseColor", color.white);
		tworld.getAppearance().setAttribute("lineShader.diffuseColor", Color.white);
//		world.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, false);
		world.addChild(tworld);
		
		IndexedFaceSet sphere = SphereUtility.tessellatedIcosahedronSphere(5);
		sphereSGC.setGeometry(sphere);
		ap = sphereSGC.getAppearance();
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute("polygonShader.diffuseColor", Color.red);
		world.addChild(sphereSGC);
		
		return world;
	}
	

	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
//		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.white);
		SceneGraphComponent cameranode = CameraUtility.getCameraNode(jrviewer.getViewer());
//		MatrixBuilder.euclidean().translate(new double[]{5,5,15}).assignTo(cameranode);
		
	}
	
	@Override
	public Component getInspector() {
		super.getInspector();
		final TextSlider<Double> tsl = new TextSlider.Double("scale", SwingConstants.HORIZONTAL, 0, 5.0, scale);
		tsl.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				scale = tsl.getValue().doubleValue();
				update();
			}

			private void update() {
				// TODO Auto-generated method stub
				
			}
		});
		inspector.add(tsl);
		return inspector;
	}


	public static void main(String[] args) {
		new IcosaEdgesExperiment().display();
	}

}
