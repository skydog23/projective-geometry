/*
 * Created on Oct 28, 2022
 *
 */
package charlesgunn.jreality.worlds.misc;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.tutorial.util.FlyTool;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.groups.TriangleGroup;

public class PerspCubeOcta extends Assignment {

	protected double bigN = 10000.0, eps = .06;
	protected transient boolean wireframe = false;
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	SceneGraphComponent both = SceneGraphUtility.createFullSceneGraphComponent("geom");
	SceneGraphComponent cube = SceneGraphUtility.createFullSceneGraphComponent("geom");
	SceneGraphComponent cubeedge = SceneGraphUtility.createFullSceneGraphComponent("cube edge");
	SceneGraphComponent halfcline = SceneGraphUtility.createFullSceneGraphComponent("half line");
	SceneGraphComponent octa = SceneGraphUtility.createFullSceneGraphComponent("geom");
	SceneGraphComponent octaedge = SceneGraphUtility.createFullSceneGraphComponent("cube edge");
	SceneGraphComponent halfoline = SceneGraphUtility.createFullSceneGraphComponent("half line");
	@Override
	public SceneGraphComponent getContent() {
		DiscreteGroup dg = TriangleGroup.instanceOfGroup("234");
		dg.setDimension(3);
		DiscreteGroupElement dge[] = new DiscreteGroupElement[2];
		dge[0] = new DiscreteGroupElement(Pn.EUCLIDEAN,
				P3.makeRotationMatrix(null, new double[]{0, 0,1}, Math.PI/2),
				"z");
		dge[1] = new DiscreteGroupElement(Pn.EUCLIDEAN,
				P3.makeRotationMatrix(null, new double[]{1, 1,1}, 2*Math.PI/3),
				"u");
		dg.setGenerators(dge);
		dg.update();
		DiscreteGroupSceneGraphRepresentation dgsgr = new DiscreteGroupSceneGraphRepresentation(dg);
		both.addChildren(cube, octa);
		cube.addChildren(cubeedge, halfcline);
		octa.addChildren(octaedge, halfoline);
		cube.getAppearance().setAttribute("lineShader."+CommonAttributes.DIFFUSE_COLOR, new Color(120,0,0));//1,5,21,85,341,1365,5461
		octa.getAppearance().setAttribute("lineShader."+CommonAttributes.DIFFUSE_COLOR, new Color(0,0,120));//1,5,21,85,341,1365,5461
		Appearance ap = halfcline.getAppearance();
		setlineattr(ap);
		ap.setAttribute("lineShader."+CommonAttributes.LINE_STIPPLE_PATTERN, 0b101010101010101);//1,5,21,85,341,1365,5461
		setlineattr(halfoline.getAppearance());
		halfoline.getAppearance().setAttribute("lineShader."+CommonAttributes.LINE_STIPPLE_PATTERN, 0b0011111100100010);//1,5,21,85,341,1365,5461
		halfoline.getAppearance().setAttribute("lineShader."+CommonAttributes.LINE_STIPPLE_FACTOR, 2.0);//1,5,21,85,341,1365,5461
		ap = cubeedge.getAppearance();
		setedgeattr(ap);
		setedgeattr(octaedge.getAppearance());;

		IndexedLineSet halfcG = IndexedLineSetUtility.createCurveFromPoints(new double[][]{{1,1,0,1},{0,0,1,0}},false);
		IndexedLineSet halfoG = IndexedLineSetUtility.createCurveFromPoints(new double[][]{{1,1,0,1},{-1,1,0,0}},false);
		halfcline.setGeometry(halfcG);
		halfoline.setGeometry(halfoG);
		IndexedLineSet cubeedgeG = IndexedLineSetUtility.createCurveFromPoints(new double[][]{{1,1,0,1},{1,1,1+eps,1}},false);
		IndexedLineSet octaedgeG = IndexedLineSetUtility.createCurveFromPoints(new double[][]{{1,1,0,1},{-eps,2+eps,0,1}},false);
		cubeedge.setGeometry(cubeedgeG);
		octaedge.setGeometry(octaedgeG);
		dgsgr.setWorldNode(both);
		dgsgr.update();
//		SceneGraphComponent cube = SceneGraphUtility.createFullSceneGraphComponent("cube");
//		cube.setGeometry(Primitives.cube4(true));
		SceneGraphComponent far = SceneGraphUtility.createFullSceneGraphComponent("far");
		SceneGraphComponent farP = SceneGraphUtility.createFullSceneGraphComponent("farP");
		SceneGraphComponent farL = SceneGraphUtility.createFullSceneGraphComponent("farL");
		PointSetFactory psf = new PointSetFactory();
		double[][] farPts = new double[][] {
			{1,0,0,0},{0,1,0,0},{0,0,1,0}};
		psf.setVertexCount(farPts.length);
		psf.setVertexCoordinates(farPts);
		psf.update();
		farP.setGeometry(psf.getGeometry());
		for (int i = 0; i<3; ++i)	{ // the 3 lines
			PointRangeFactory lf = new PointRangeFactory();
			lf.setElement0(farPts[i]);
			lf.setElement1(farPts[(i+1)%3]);
			lf.setFiniteSphere(false);
			lf.update();
			SceneGraphComponent child = SceneGraphUtility.createFullSceneGraphComponent("child"+i);
			child.setGeometry(lf.getLine());
			farL.addChild(child);
		}
		far.addChildren(farP, farL);
		far.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		ap = farP.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute("pointShader."+CommonAttributes.SPHERES_DRAW, false);
		ap.setAttribute("pointShader."+CommonAttributes.POINT_SIZE, 6.0);
		ap.setAttribute("pointShader."+CommonAttributes.ATTENUATE_POINT_SIZE, false);
		ap.setAttribute("pointShader."+CommonAttributes.DIFFUSE_COLOR, Color.black);
		world.addChildren(dgsgr.getRepresentationRoot(),far);
		MatrixBuilder.euclidean().translate(new double[]{0,0,-2.5}).assignTo(world);
		ap = world.getAppearance();
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		return world;
	}

	private void setedgeattr(Appearance ap) {
		ap.setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, !wireframe);
		ap.setAttribute("lineShader."+CommonAttributes.LINE_STIPPLE, false);
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .01);
		ap.setAttribute("lineShader."+CommonAttributes.LINE_WIDTH, 5.0);
	}

	private void setlineattr(Appearance ap) {
		ap.setAttribute("lineShader."+CommonAttributes.LINE_STIPPLE, true);
		ap.setAttribute("lineShader."+CommonAttributes.LINE_STIPPLE_FACTOR, 3);
		ap.setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .01);
		ap.setAttribute("lineShader."+CommonAttributes.LINE_WIDTH, 3.6);
	}

	@Override
	public void display() {
		super.display();
		Viewer v = jrviewer.getViewer();
		v.getSceneRoot().getAppearance().
			setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.white);
		Camera c = CameraUtility.getCamera(v);
		c.setFar(-1);
		c.setFieldOfView(140);
	    CameraUtility.getCameraNode(v).addTool(new FlyTool());
	    
		Component comp = ((Component) viewer.getViewingComponent());
		comp.addKeyListener(new KeyAdapter() {
 				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_H:
						System.err.println("	1: toggle camera path");
						break;
		
					case KeyEvent.VK_1:
						cube.setVisible(!cube.isVisible());
						break;
					case KeyEvent.VK_2:  // print lengths
						octa.setVisible(!octa.isVisible());
						break;
					}
 				}
		});

	}

	public static void main(String[] args) {
		new PerspCubeOcta().display();
	}

}
