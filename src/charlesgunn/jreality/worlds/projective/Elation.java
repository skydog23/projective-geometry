/*
 * Created on 03.10.2016
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;

import android.R.color;
import charlesgunn.jreality.geometry.projective.LinePencilFactory;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.projgeom.PlueckerLineGeometry;

public class Elation extends Assignment {

	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	SceneGraphComponent tworld = SceneGraphUtility.createFullSceneGraphComponent("world");
	@Override
	public SceneGraphComponent getContent() {
		LinePencilFactory lpf1 = new LinePencilFactory(),
				lpf2 = new LinePencilFactory();
		lpf1.setPoint(new double[]{0,0,1,1});
		lpf1.setPlane(new double[]{0,0,1,-1});
		lpf1.setFan(false);
		lpf1.setFiniteSphere(true);
		lpf1.setNumberJoints(10);
		lpf1.setSphereRadius(10);
		lpf1.setNumLines(10);
		lpf1.update();
		world.getAppearance().setAttribute("lineShader.diffuseColor", color.black);
		world.addChild(lpf1.getPencil());
		lpf2.setPoint(new double[]{1,0,0,0});
		lpf2.setPlane(new double[]{0,0,1,-1});
		lpf2.setFan(false);
		lpf2.setFiniteSphere(false);
		lpf2.setNumberJoints(12);
		lpf2.setNumLines(10);
		lpf2.update();
		world.addChild(lpf2.getPencil());
		tworld.getAppearance().setAttribute("lineShader.diffuseColor", Color.blue);
		world.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, false);
		tworld.addChildren(lpf1.getPencil(), lpf2.getPencil());
		double[] skew = {
				1,1,0,0,
				0,1,0,0,
				0,0,1,0,
				0,0,0,1
		};
		Matrix mskew = new Matrix();
		mskew.assignFrom(skew);
		mskew.assignTo(tworld);
		
		world.addChild(tworld);
		return world;
	}

	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.white);
		SceneGraphComponent cameranode = CameraUtility.getCameraNode(jrviewer.getViewer());
		MatrixBuilder.euclidean().translate(new double[]{0,0,5}).assignTo(cameranode);
		
	}

	public static void main(String[] args) {
		new Elation().display();
	}

}
