/*
 * Created on 03.10.2016
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;

import charlesgunn.jreality.geometry.projective.LinePencilFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.p5.PlueckerLineGeometry;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class Elation extends Assignment {

	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	SceneGraphComponent tworld = SceneGraphUtility.createFullSceneGraphComponent("tworld");
	SceneGraphComponent tworld2 = SceneGraphUtility.createFullSceneGraphComponent("tworld2");
	@Override
	public SceneGraphComponent getContent() {
		LinePencilFactory lpf1 = new LinePencilFactory(),
				lpf2 = new LinePencilFactory();
		// create the line pencil in the plane z=1 in the point (0,0,1,1)
		lpf1.setPoint(new double[]{0,0,1,1});
		lpf1.setPlane(new double[]{0,0,1,-1});
		lpf1.setFan(false);
		lpf1.setFiniteSphere(true);
		lpf1.setNumberJoints(10);
		lpf1.setSphereRadius(10);
		lpf1.setNumLines(10);
		lpf1.update();
		tworld.getAppearance().setAttribute("lineShader.diffuseColor", Color.black);
		tworld.addChild(lpf1.getPencil());
		// create the line pencil in the plane z=1 in the ideal point in the x-direction
		lpf2.setPoint(new double[]{1,0,0,0});
		lpf2.setPlane(new double[]{0,0,1,-1});
		lpf2.setFan(false);
		lpf2.setFiniteSphere(false);
		lpf2.setNumberJoints(12);
		lpf2.setNumLines(10);
		lpf2.update();
		// put the second pencil into the world
		tworld.addChild(lpf2.getPencil());
		tworld.getAppearance().setAttribute("lineShader.diffuseColor", Color.blue);
		world.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, false);
		// put both pencils into the sgc tworld
		tworld2.addChildren(lpf1.getPencil(), lpf2.getPencil());
		setupSkew(1);
		
		world.addChildren(tworld, tworld2);
		return world;
	}

	private void setupSkew(double t) {
		double[] skew = {
				1,t,0,0,
				0,1,0,0,
				0,0,1,0,
				0,0,0,1
		};
		Matrix mskew = new Matrix();
		mskew.assignFrom(skew);
		// transform tworld by (x,y)->(x+y,y): skew to the right
		mskew.assignTo(tworld2);
	}

	@Override
	public void setValueAtTime(double d) {
		super.setValueAtTime(d);
		setupSkew(d);
		
		
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
