/*
 * Created on Jun 2, 2010
 *
 */
package charlesgunn.jreality.worlds.projective;

import static de.jreality.geometry.GeometryUtility.BOUNDING_BOX;

import java.awt.Color;
import java.util.List;

import javax.swing.JMenuBar;

import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.geometry.projective.LineUtility;
import charlesgunn.jreality.geometry.projective.PlanePencilFactoryOld;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.plugin.ToolBarPlugin;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.basic.Shell;
import de.jreality.plugin.basic.ViewPreferences;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.content.ContentTools;
import de.jreality.plugin.experimental.ViewerKeyListenerPlugin;
import de.jreality.scene.Appearance;
import de.jreality.scene.Cylinder;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.jrworkspace.plugin.Plugin;

public class ProductTwoLines extends Assignment {

	double alpha = Math.PI/10, d = 1.0, farAway = 4;
	transient double[][] pts = {
			{0,0,0,1},
			{0,1,0,1},
			{0,1,0,0},
			{1,0,0,0},
			{Math.cos(alpha), 0, -Math.sin(alpha), 0},
			{0, .5, -farAway, 1}
	};
	@Override
	public SceneGraphComponent getContent() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		SceneGraphComponent mSGC = SceneGraphUtility.createFullSceneGraphComponent("m");
		SceneGraphComponent mtSGC = SceneGraphUtility.createFullSceneGraphComponent("mt");
		SceneGraphComponent nSGC = SceneGraphUtility.createFullSceneGraphComponent("n");
		SceneGraphComponent normalSGC = SceneGraphUtility.createFullSceneGraphComponent("normal");
		SceneGraphComponent normalPSGC = SceneGraphUtility.createFullSceneGraphComponent("normalPerp");
		SceneGraphComponent helixSGC = SceneGraphUtility.createFullSceneGraphComponent("cyl");
		
		MatrixBuilder.euclidean().translate(0,1,0).assignTo(mtSGC);
		world.addChildren(mSGC, nSGC, normalSGC, normalPSGC, helixSGC, mtSGC);
				
		Color color1 = new Color(50,150,250);
		Color color2 = new Color(200,0,255);
		Color color3 = new Color(200, 200, 200);
		Color[] colorList = {color1, Color.black, color2, color3, color3};
		SceneGraphComponent[] list = {mSGC, mtSGC, nSGC, normalSGC, normalPSGC};
		int[][] indices = {{3,0},{3,0},{1,4},{2,0},{3,5}};
		Appearance ap;
		for (int i = 0; i<indices.length; ++i)	{
			PointRangeFactory prf = new PointRangeFactory();
			prf.setElement0(pts[indices[i][0]]);
			prf.setElement1(pts[indices[i][1]]);
			prf.setFiniteSphere(true);
			prf.setSphereRadius(100);
			prf.setNumberOfSamples(2);
			prf.update();
			list[i].setGeometry(prf.getLine());
			ap = list[i].getAppearance();
			ap.setAttribute("lineShader.polygonShader.diffuseColor", colorList[i]);
		}
		int numSteps = 50;
		for (int i = 0; i<numSteps; ++i)	{
			SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("helix"+i);
			double t = (i/(numSteps-1.0));
			Color col = AnimationUtility.linearInterpolation(color1, color2, t);
			System.err.println("color is "+col);
			sgc.getAppearance().setAttribute("diffuseColor", col);
			sgc.setGeometry(new Cylinder());
			MatrixBuilder.euclidean().translate(0,t,0).rotateY(t*alpha+Math.PI/2).scale(.15).scale(.015, .015, 2).translate(0,0,0).assignTo(sgc);
			helixSGC.addChild(sgc);
		}
		helixSGC.setVisible(false);
		
		ap = world.getAppearance();
		ap.setAttribute(BOUNDING_BOX, Rectangle3D.unitCube);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.AMBIENT_COEFFICIENT, .1);
		ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.black);
		ap.setAttribute("lineShader.tubeRadius", .02);
//		ap.setAttribute("lineShader.tubeRadius", .005);
		
		ap = mtSGC.getAppearance();
		ap.setAttribute("lineShader.lineWidth", 4);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		ap = normalPSGC.getAppearance();
		ap.setAttribute("lineShader.lineWidth", 4);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		ap.setAttribute(CommonAttributes.LINE_STIPPLE, true);
//		ap.setAttribute(CommonAttributes.TRANSPARENCY, .6);
//		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
//		ap.setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, false);
//		ap.setAttribute("lineShader.tubeRadius", .01);

		MatrixBuilder.euclidean().translate(0, -.4, -.5).rotateX(-Math.PI/60).rotateY(-Math.PI/200).assignTo(world);
		return world;
	}

	
	@Override
	public List<Plugin> getPluginsToRegister() {
		pluginsToLoad.add(new Shell());
		pluginsToLoad.add(contentPlugin);
		pluginsToLoad.add(new ContentLoader());
		pluginsToLoad.add(new ViewPreferences());
		animationPlugin = new AnimationPlugin();
		pluginsToLoad.add(animationPlugin);
		pluginsToLoad.add(new ViewerKeyListenerPlugin());
		pluginsToLoad.add(shrinkPanelPlugin);
		pluginsToLoad.add(new ToolBarPlugin());
		return pluginsToLoad;
	}


	@Override
	public void display(){
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute("backgroundColor", Color.white);
		CameraUtility.getCamera(jrviewer.getViewer()).setFieldOfView(110);
	}

	public static void main(String[] args) {
		new ProductTwoLines().display();
	}
}
