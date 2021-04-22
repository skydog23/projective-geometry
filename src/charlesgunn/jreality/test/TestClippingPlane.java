/*
 * Created on Aug 23, 2004
 *
 */
package charlesgunn.jreality.test;

import java.awt.Color;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.RotateTool;
import de.jreality.util.SceneGraphUtility;

public class TestClippingPlane extends LoadableScene {
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent root = new SceneGraphComponent("world");	
		SceneGraphComponent clipPlaneSGC2 = SceneGraphUtility.createFullSceneGraphComponent("theClipIcon");

		// this scene graph holds the rotate tool for manipulating the clipping plane
		SceneGraphComponent clipPlaneSGC1 = SceneGraphUtility.createFullSceneGraphComponent("theClipIcon");
		clipPlaneSGC2.addChild(clipPlaneSGC1);
		clipPlaneSGC1.addTool(new RotateTool());
		
		// this node holds a representation of the clipping plane as a rectangle
		// which lies in a plane just slightly on the "unclipped" side of the clipping plane
		clipPlaneSGC1.setGeometry(Primitives.regularPolygon(4, .5));
		clipPlaneSGC1.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.white);
		MatrixBuilder.euclidean().translate(.5,0,.5).scale(1,2,1).assignTo(clipPlaneSGC1);
		
		// finally here is the clipping plane itself.
		SceneGraphComponent clipPlaneItself =  SceneGraphUtility.createFullSceneGraphComponent("theClipPlane");
		// move the clipping plane to be just on the other side of the geometry so we don't clip out 
		// geometry representing the clipping plane
		MatrixBuilder.euclidean().translate(0,0,.01).assignTo(clipPlaneItself);
		clipPlaneItself.setGeometry(new ClippingPlane());
		clipPlaneSGC1.addChild(clipPlaneItself);
		
		SceneGraphComponent sphereSGC = SceneGraphUtility.createFullSceneGraphComponent("sphere");
		sphereSGC.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+
				CommonAttributes.DIFFUSE_COLOR, Color.cyan);
		// we don't want the sphere interfere with moving the clipping plane
		sphereSGC.setPickable(false);
		sphereSGC.addChild(SphereUtility.tessellatedCubeSphere(SphereUtility.SPHERE_SUPERFINE));
		root.addChildren(sphereSGC, clipPlaneSGC1);
		return root;
	}
 }
