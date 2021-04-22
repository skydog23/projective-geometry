/*
 * Created on 03.10.2016
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;

import android.R.color;
import charlesgunn.jreality.geometry.projective.LinePencilFactory;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.p5.PlueckerLineGeometry;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class Elation2 extends Assignment {

	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	SceneGraphComponent tworld = SceneGraphUtility.createFullSceneGraphComponent("world");
	@Override
	public SceneGraphComponent getContent() {
		
		double[] yaxis = new double[]{1,0,0},
				xaxis = new double[]{0,1,0};
		double[] origin = {0,0,0,1}, xdir = {1,0,0,0}, ydir = {0,1,0,0};
		PointRangeFactory vertline = new PointRangeFactory();
//		vertline.set2DLine(yaxis);
		vertline.setElement0(origin);
		vertline.setElement1(ydir);
		vertline.setFiniteSphere(false);
		vertline.setSphereRadius(50);
		vertline.update();
		vertline.getLine().setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		PointRangeFactory horline = new PointRangeFactory();
//		horline.set2DLine(xaxis);
		vertline.setElement0(origin);
		vertline.setElement1(xdir);
		horline.setFiniteSphere(false);
		horline.setSphereRadius(50);
		horline.update();
		horline.getLine().setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);

		
		IndexedFaceSet cube = Primitives.coloredCube();
		for (int i = 0; i<4; ++i)	{
			SceneGraphComponent child = SceneGraphUtility.createFullSceneGraphComponent("childy"+i);
			world.addChild(child);
			MatrixBuilder.euclidean().translate(i+3, 0, 0).getMatrix().assignTo(child);
			child.setGeometry(vertline.getLine());
			child = SceneGraphUtility.createFullSceneGraphComponent("childx"+i);
			tworld.addChild(child);
			MatrixBuilder.euclidean().translate(0, i+3, 0).getMatrix().assignTo(child);
			child.setGeometry(horline.getLine());
		}
		world.getAppearance().setAttribute("lineShader.diffuseColor", color.black);
		tworld.getAppearance().setAttribute("lineShader.diffuseColor", Color.blue);
		world.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, false);
		double[] skew = {
				1,1,0,0,
				0,1,0,0,
				0,0,1,0,
				0,0,0,1
		};
		Matrix mskew = new Matrix();
		mskew.assignFrom(skew);
//		mskew.assignTo(tworld);
		double angle = Math.PI/4;
		double[] rot = {
				1,0, 0, 0,
				0, Math.cos(angle),0, -Math.sin(angle),
				0, 0, 1, 0,
				0, Math.sin(angle), 0, Math.cos(angle)
		};
		Matrix mrot = new Matrix(rot);
//		mrot.assignTo(world);
//		MatrixBuilder.elliptic().rotate(new double[]{0,1,0,0}, new double[]{0,0,1,0}, Math.PI/4).assignTo(world);
		world.addChild(tworld);
		return world;
	}

	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.white);
		SceneGraphComponent cameranode = CameraUtility.getCameraNode(jrviewer.getViewer());
		MatrixBuilder.euclidean().translate(new double[]{5,5,15}).assignTo(cameranode);
		
	}

	public static void main(String[] args) {
		new Elation2().display();
	}

}
