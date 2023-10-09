/*
 * Created on Apr 30, 2021
 *
 */
package charlesgunn.jreality.worlds.projective;

import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import charlesgunn.jreality.geometry.projective.RegulusFactory;
import charlesgunn.jreality.geometry.projective.SkewLines2x3;
import charlesgunn.jreality.newtools.FlyTool;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class ReyesConfigurationDemo extends Assignment {

	SceneGraphComponent world, cube  =SceneGraphUtility.createFullSceneGraphComponent("cube");
	SkewLines2x3 skewer = new SkewLines2x3();
	@Override
	public SceneGraphComponent getContent() {
		world =SceneGraphUtility.createFullSceneGraphComponent("world");
//		cube =SceneGraphUtility.createFullSceneGraphComponent("cube");
		cube.setGeometry(Primitives.cube4(false));
		cube.setPickable(false);
		Appearance ap = cube.getAppearance();
		// has to be turned off otherwise looks very dark
//		ap.setAttribute(LIGHTING_ENABLED, false);
		ap.setAttribute("lineShader.diffuseColor", Color.WHITE);
		ap.setAttribute(FACE_DRAW,false);
		ap.setAttribute(TUBES_DRAW,true);
		ap.setAttribute(CommonAttributes.TUBE_RADIUS, .02);
		RegulusFactory regFac = RegulusFactory.getRegulusFactory();
		double[][] rl = skewer.getRLines();
		System.err.println("rl = "+rl);
		regFac.setElement0(rl[0]);
		regFac.setElement1(rl[1]);
		regFac.setElement2(rl[2]);
		regFac.update();
		ap = regFac.getRegulus().getAppearance();
		ap.setAttribute("lineShader.diffuseColor", new Color(255, 0, 0));
		double tuberadius = .0015;
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, tuberadius);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.LINE_WIDTH, 3.0);
		regFac.getLeitScharFactory().update();
		regFac.getLeitSchar().setVisible(true);
		ap = regFac.getLeitSchar().getAppearance();
		ap.setAttribute("lineShader.diffuseColor", new Color(0, 0, 255));
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, tuberadius);
		world.addChildren(regFac.getRegulus(), regFac.getLeitSchar(), cube, skewer.getSgcRepn());
		ap = world.getAppearance();
		ap.setAttribute("useGLSL", true); //!coloredFace);
		ap.setAttribute("metric", Pn.ELLIPTIC); //!coloredFace);
		ap.setAttribute("renderS3", true); //!coloredFace);
		ap.setAttribute(CommonAttributes.SMOOTH_SHADING, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.SPHERES_DRAW, false);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.POINT_RADIUS, .01);
 		ap.setAttribute(CommonAttributes.LINE_WIDTH, 3.0);
		ap.setAttribute(CommonAttributes.TUBE_RADIUS, .01);
		MatrixBuilder.elliptic().translate(new double[]{-1,-1,-1,.8}).
			rotateFromTo(new double[]{1,0,0,0} , new double[]{1,1,1,1}).assignTo(world);
		return world;
	}

	@Override
	public Component getInspector() {
		Box hbox = Box.createHorizontalBox();
		inspector.add(hbox);
		
		final JCheckBox visCB = new JCheckBox("show cube");
		visCB.setSelected(cube.isVisible());
		hbox.add(visCB);
		visCB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean boo = visCB.isSelected();
				cube.setVisible(boo);
			}
		});
		
		
		skewer.getInspector(inspector);
		return inspector;
	}


	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.METRIC, Pn.ELLIPTIC);
		viewer.getSceneRoot().getAppearance().setAttribute(
				CommonAttributes.BACKGROUND_COLOR, new Color(120,120,120));
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.RENDER_S3, true);;
//		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		Camera cam = CameraUtility.getCamera(viewer);
		cam.setNear(.02);
		cam.setFar(-.05);
		de.jreality.plugin.basic.Scene scene = 
				jrviewer.getPlugin(de.jreality.plugin.basic.Scene.class);
		SceneGraphPath avatarPath = scene.getAvatarPath();
		FlyTool flytool = new FlyTool();
		flytool.setGain(.1);
		avatarPath.getLastComponent().addTool(flytool);
	}


	public static void main(String[] args) {
		new ReyesConfigurationDemo().display();
	}
}
