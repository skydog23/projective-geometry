/*
 * Author	gunn
 * Created on Dec 12, 2005
 *
 */
package charlesgunn.jreality.test;

import java.awt.Color;
import java.io.IOException;

import javax.swing.JMenuBar;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.GlslProgram;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;

public class TestStandardGlsl extends LoadableScene {

	boolean testNewGlsl = false;
	private GlslProgram brickProg, standardProg;
		
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		Appearance ap=world.getAppearance();
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING, true);
		ap.setAttribute(CommonAttributes.BACK_FACE_CULLING_ENABLED, true);

		SceneGraphComponent oldWay = SceneGraphUtility.createFullSceneGraphComponent("old");
		SceneGraphComponent newWay = SceneGraphUtility.createFullSceneGraphComponent("new");
		MatrixBuilder.init(null, Pn.EUCLIDEAN).translate(2,0,0).assignTo(newWay);
		world.addChild(oldWay);
		world.addChild(newWay);
		Appearance oldAp= oldWay.getAppearance();
		Appearance newAp = newWay.getAppearance();
//		if (!testNewGlsl)	{
		    ap =  newAp;
			try {
//				brickProg = new GlslProgram(oldAp, "polygonShader",
//								        Input.getInput("de/jreality/jogl/shader/resources/brick.vert"),
//								        Input.getInput("de/jreality/jogl/shader/resources/brick.frag")
//								    );
//				oldAp.setAttribute("polygonShader","glsl");
				standardProg = new GlslProgram(newAp, "polygonShader",
				        Input.getInput("de/jreality/jogl/shader/resources/standardOGL.vert"),
				        null
				    );
				newAp.setAttribute("polygonShader","glsl"); 
			} catch (IOException e) {
				e.printStackTrace();
			}
//		}
		int i = 1;
		IndexedFaceSet ifs = SphereUtility.tessellatedIcosahedronSphere(6, true);
		oldWay.setGeometry(ifs); //new Sphere()); //
		newWay.setGeometry(ifs);
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new java.awt.Color(1.0f,0.0f,1.0f));
		double[] brickSize = new double[2];
		double[] brickPct = new double[2];
		double[] mortarPct = new double[2];
		float r = (float) (i/6.0);
		float b = 1f - r;
		brickSize[0] = .1+r*.5;
		brickSize[1] = .2+r*.3;
		brickPct[0] = .4 + .1*i;
		brickPct[1] = .7 + .05*i;
		mortarPct[0] = 1.0-brickPct[0];
		mortarPct[1] = 1.0-brickPct[1];
		double[] lightPosition = {0,0,4};
		lightPosition[0] = i-2.0;
//		if (testNewGlsl)	{
//			brickProg.setUniform("SpecularContribution", (i*.15));
//			brickProg.setUniform("DiffuseContribution", 1.0);
//			brickProg.setUniform("BrickColor", new double[]{r,0f,b});
//			brickProg.setUniform("MortarColor", new double[] {b,1f,r});
//			brickProg.setUniform("BrickSize", brickSize);
//			System.out.println("Brick size is "+Rn.toString(brickSize));
//			brickProg.setUniform("BrickPct", brickPct);
//			brickProg.setUniform("MortarPct", mortarPct);
//			brickProg.setUniform("LightPosition", lightPosition);			
//		} else {
			ap = oldAp;
			ap.setAttribute("polygonShader.vertexShader.specularExponent", 120.0);
			ap.setAttribute("polygonShader.vertexShader.ambientCoefficient", 0.0);
			ap.setAttribute("polygonShader.vertexShader.ambientColor", new Color(255,255,255,255));
			ap.setAttribute("polygonShader.vertexShader.diffuseCoefficient", 0.7);
			ap.setAttribute("polygonShader.vertexShader.diffuseColor", new Color(255,255,0,255));
			ap.setAttribute("polygonShader.vertexShader.specularCoefficient", 0.7);
			ap.setAttribute("polygonShader.vertexShader.speculartColor", new Color(255,255,255,255));
			ap=newAp;
//			standardProg.setUniform("lightingEnabled",1);
//			standardProg.setUniform("ambientColor", new double[]{1.0,1.0,1.0,1.0});
//			standardProg.setUniform("ambientCoefficient", 0.0);
//			standardProg.setUniform("diffuseColor", new double[]{1.0,1.0,0.0,1.0});
//			standardProg.setUniform("diffuseCoefficient", 0.7);
//			standardProg.setUniform("specularColor", new double[]{1.0, 1.0,1.0,1.0});
//			standardProg.setUniform("specularCoefficient", 0.7);
//		}
		return world;
	}
	

	public boolean addBackPlane() {
		
		return false;
	}
	public boolean isEncompass() {
		
		return true;
	}

	public  SceneGraphComponent makeLights()	{
		SceneGraphComponent lightNode = new SceneGraphComponent();
		lightNode.setName("lights");
		SceneGraphComponent l0 = SceneGraphUtility.createFullSceneGraphComponent("light0");
		PointLight dl = new PointLight();
		dl.setColor(new Color(50, 250, 250));
		dl.setFalloff(1.0, 0.0, 0.0);
		dl.setIntensity(.5);
		double[] zaxis = {0,0,1};
//		MatrixBuilder.euclidean().translate(1,1,0).assignTo(l0);
//		l0.getTransformation().setMatrix( P3.makeRotationMatrix(null, zaxis, other));

		l0.setLight(dl);
		lightNode.addChild(l0);
				
		dl = new PointLight();
		dl.setColor(new Color(250, 50, 250));
		dl.setIntensity(.5);
		dl.setFalloff(1.0, 0.0, 0.0);
		l0 = SceneGraphUtility.createFullSceneGraphComponent("light1");
		double[] other2 = {-.6,-.2,1};
		MatrixBuilder.euclidean().translate(2,2,0).assignTo(l0);
		l0.setLight(dl);
		lightNode.addChild(l0);
		
		
		return lightNode;
	}


	public void customize(JMenuBar menuBar, Viewer viewer) {
//		((de.jreality.jogl.JOGLViewer) viewer).setFlipped(false);
	}


}

