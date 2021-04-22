/*
 * Created on Jan 29, 2004
 *
 */
package charlesgunn.jreality.worlds;

import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.RMAN_SHADOWS_ENABLED;
import static de.jreality.shader.CommonAttributes.RMAN_SURFACE_SHADER;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;

import javax.swing.JMenuBar;

import charlesgunn.jreality.geometry.BezierCurve;
import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.renderman.shader.SLShader;
import de.jreality.scene.Cylinder;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.SceneGraphUtility;


/**
 * @author Charles Gunn
 *
 */
public class SimpleShapes extends LoadableScene {
	SceneGraphComponent icokit;
	boolean tryFlatten = true;

	
		public SceneGraphComponent makeWorld()	{
			SceneGraphComponent theWorld = SceneGraphUtility.createFullSceneGraphComponent("world");
			MatrixBuilder.euclidean().rotateX(Math.PI/4).assignTo(theWorld);
			SLShader sls = new SLShader("ambientOcclusion2");
			sls.addParameter("maxvariation", new Float(.05));
			theWorld.getAppearance().setAttribute(RMAN_SURFACE_SHADER, sls);
			theWorld.getAppearance().setAttribute(VERTEX_DRAW, true);
			theWorld.getAppearance().setAttribute(POINT_SHADER+"."+POINT_RADIUS, .03);
			theWorld.getAppearance().setAttribute(POINT_SHADER+"."+POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.yellow);
			theWorld.getAppearance().setAttribute(LINE_SHADER+"."+POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.gray);
			SceneGraphComponent cubesgc = SceneGraphUtility.createFullSceneGraphComponent("cube");
			SceneGraphComponent pyrsgc = SceneGraphUtility.createFullSceneGraphComponent("sphere");
			SceneGraphComponent cylsgc = SceneGraphUtility.createFullSceneGraphComponent("cylinder");
			SceneGraphComponent floorsgc = SceneGraphUtility.createFullSceneGraphComponent("floor");
			theWorld.addChild(cubesgc);
			theWorld.addChild(pyrsgc);
			theWorld.addChild(cylsgc);
			theWorld.addChild(floorsgc);
			double yscale = .5, xscale = 1, y1 = .75, y2 = .35;
			BezierCurve bc = new BezierCurve(3, 
					new double[][] {{0,0,0},{0,yscale*1,0}, {xscale*.5, y1, 0},{0,y1,0},
					{-xscale*.5,y1,0},{-.25,y2-.2,0},{.25,y2,0},{.75,y2+.2,0},{1,yscale*1,0},{1,0,0}});
			for (int i = 0; i<3; ++i) bc.refine();
			IndexedLineSet curve = IndexedLineSetUtility.createCurveFromPoints(bc.getControlPoints(), false);
			IndexedFaceSet ifs = GeometryUtilityOverflow.surfaceOfRevolutionAsIFS(bc.getControlPoints(), 32, 2*Math.PI);
			cubesgc.setGeometry(ifs); //Primitives.coloredCube()); // curve); //
//			double[] radii = {.2, .1, .3, .3, .2, .1, .2, .1};
//			((IndexedFaceSet) cubesgc.getGeometry()).setVertexAttributes(Attribute.RELATIVE_RADII, 
//					StorageModel.DOUBLE_ARRAY.createReadOnly(radii));
//			radii = new double[]{.2, .1, .3, .3, .2, .1, .2, .1, .01, .01, .01, .01};
//			((IndexedFaceSet) cubesgc.getGeometry()).setEdgeAttributes(Attribute.RELATIVE_RADII, 
//					StorageModel.DOUBLE_ARRAY.createReadOnly(radii));
			MatrixBuilder.euclidean().translate(2,0,0).rotateX(Math.PI/2).assignTo(cylsgc);
			cylsgc.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, 
					new Color(1f, .6f, 0f));
			cylsgc.getAppearance().setAttribute(TRANSPARENCY_ENABLED, true);
			cylsgc.getAppearance().setAttribute(TRANSPARENCY, .5);
			cylsgc.setGeometry(new Cylinder());
			MatrixBuilder.euclidean().translate(-2,0,1).assignTo(pyrsgc);
			pyrsgc.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, 
					new Color(1f, 1f,1f,1f));
			pyrsgc.setGeometry(new Sphere());
			
			floorsgc.setGeometry(Primitives.regularPolygon(8));
			MatrixBuilder.euclidean().translate(0,-1,0).scale(4).rotateX(Math.PI/2).assignTo(floorsgc);
			floorsgc.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, 
					new Color(1f, 1f, .7f));

//			MatrixBuilder.euclidean().rotateX(Math.PI/3).assignTo(theWorld);
			
			//			 SLShader whitted = new SLShader("whitted");
//			 whitted.addParameter("Kd",new Double(1.0));
//			 whitted.addParameter("eta", new Double(1.3));
//			 theWorld.getAppearance().setAttribute(POLYGON_SHADER,"free");
//			 theWorld.getAppearance().setAttribute(RMAN_SL_SHADER, whitted);
//			SceneGraphComponent boxedSignFromString = 
//				GeometryUtilityOverflow.boxedSignFromString("A.01", .3, .1,new Font("Serif",Font.PLAIN,128));
////		  	theWorld.addChild(boxedSignFromString);
//		  	SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent();
//		  	Appearance ap = theWorld.getAppearance();
//			Texture2D tex2d2 = (Texture2D) AttributeEntityUtility.createAttributeEntity(
//					Texture2D.class, "polygonShader.texture2d", ap, true);
//			try {
//				ImageData id = ImageData.load(Input.getInput(
//						"/Users/gunn/Pictures/screenshots/starSolids.png")); // weaveRGBABright.png"));
//				tex2d2.setImage(id);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		  	Image id = tex2d2.getImage().getImage();
//		  	BufferedImage bi = null;
//			try {
//				bi = (BufferedImage) 
//					ImageData.load(Input.getInput("/Users/gunn/Pictures/screenshots/starSolids.png")).getImage();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		  	tex2d2.setApplyMode(Texture2D.GL_MODULATE);
//		  	theWorld.addChild(GeometryUtilityOverflow.boxedTerrainFromImage(bi, .1, .1, false));

		  	//			MatrixBuilder.euclidean().rotateX(Math.PI).assignTo(theWorld);
			return theWorld;
		}
	
		public boolean isEncompass() {
			return true;
		}


		public void customize(JMenuBar menuBar, final Viewer viewer) {
			viewer.getSceneRoot().getAppearance().setAttribute(RMAN_SHADOWS_ENABLED, true);
			viewer.getSceneRoot().getAppearance().setAttribute(BACKGROUND_COLOR, Color.black);
//			viewer.getSceneRoot().addTool(new PickShowTool());
		}
		public SceneGraphComponent makeLights() {
			SceneGraphComponent lightNode = new SceneGraphComponent();
			lightNode.setName("lights");
			SceneGraphComponent l0 = SceneGraphUtility.createFullSceneGraphComponent("light0");
			DirectionalLight dl = new DirectionalLight();
			dl.setColor(new Color(250, 250, 00));
			dl.setIntensity(.5);
			double[] zaxis = {0,0,1};
			double[] other = {0,1,1};
			l0.getTransformation().setMatrix( P3.makeRotationMatrix(null, zaxis, other));
			System.err.println("Light0 position is "+Rn.toString(Rn.matrixTimesVector(null,l0.getTransformation().getMatrix(), zaxis)));
			l0.setLight(dl);
			lightNode.addChild(l0);
					
			dl = new DirectionalLight();
			dl.setColor(new Color(250,0, 225));
			dl.setIntensity(.5);
			l0 = SceneGraphUtility.createFullSceneGraphComponent("light1");
			double[] other2 = {-.6,-.2,.5};
			l0.getTransformation().setMatrix( P3.makeRotationMatrix(null, zaxis, other2));
			l0.setLight(dl);
			lightNode.addChild(l0);
			l0.getAppearance().setAttribute(RMAN_SHADOWS_ENABLED, false);
			System.err.println("Light1 position is "+Rn.toString(Rn.matrixTimesVector(null,l0.getTransformation().getMatrix(), zaxis)));
			
			
			return lightNode;
		}
			
	
}
