/*
 * Created on Sep 15, 2004
 *
*/
package charlesgunn.jreality.test;

import java.awt.Color;
import java.io.IOException;

import javax.swing.JMenuBar;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.ParametricSurfaceFactory.Immersion;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.GlslProgram;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;

/**
 * @author gunn
 *
 */
public class TestHyperbolicShader extends LoadableScene {

	boolean useLOD = true;
	Viewer viewer = null;
	public void customize(JMenuBar menuBar, Viewer viewer) {
		this.viewer = viewer;
	}
	/* (non-Javadoc)
	 * @see de.jreality.jogl.InteractiveViewerDemo#makeWorld()
	 */
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		world.addChild(Primitives.wireframeSphere());
		
		Appearance ap = world.getAppearance();
		ap.setAttribute(CommonAttributes.EDGE_DRAW,false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW,false);
			ap.setAttribute("polygonShader.diffuseColor",java.awt.Color.white);
//			ap.setAttribute("polygonShader.specularCoefficient",0.0);
			
			ParametricSurfaceFactory psf = new ParametricSurfaceFactory();
			psf.setMetric(Pn.HYPERBOLIC);
			psf.setULineCount(100);
			psf.setVLineCount(100);
		    psf.setUMin(0.0);
		    psf.setUMax(Math.PI * 2);
		    psf.setVMin(0.0);
		    psf.setVMax(.99);
		    psf.setGenerateFaceNormals(true);
		    psf.setGenerateEdgesFromFaces(true);
		    psf.setGenerateVertexNormals(true);
			psf.setImmersion( new Immersion()		{

				public boolean isImmutable() {
					// TODO Auto-generated method stub
					return false;
				}

				public int getDimensionOfAmbientSpace() {
					return 4;
				}

				public void evaluate(double u, double v, double[] xyz, int index) {
					xyz[0] = Math.cos(u) * v;
					xyz[1] = Math.sin(u) * v;
					xyz[2] = 0.0;
					xyz[3] = 1.0;
				}
				
			});
			psf.update();
			IndexedFaceSet ifs = psf.getIndexedFaceSet();
//			GlslPolygonShader.correctNormalLength(ifs);
//			BezierPatchMesh bpm = new BezierPatchMesh(1, 1, square);
//		   	for (int j =0; j<= 4; ++j)	bpm.refine();
//		   	IndexedFaceSet qmpatch = BezierPatchMesh.representBezierPatchMeshAsQuadMesh(null, bpm, Pn.HYPERBOLIC);	 
		   	SceneGraphComponent theFoog2 = SceneGraphUtility.createFullSceneGraphComponent("both");
		   	SceneGraphComponent theFoog = SceneGraphUtility.createFullSceneGraphComponent("hyperbolic shader");
		   	theFoog.setGeometry(ifs);
//		   	MatrixBuilder.hyperbolic().translate(0,0,-.5).assignTo(theFoog);
		   	theFoog2.addChild(theFoog);
		   	ap = theFoog.getAppearance();
			GlslProgram hyperbolicShader = null;
			try {
				hyperbolicShader = new GlslProgram(ap, "polygonShader",   Input.getInput("de/jreality/jogl/shader/resources/noneuclidean.vert"), null);
//				hyperbolicShader = new GlslProgram(ap, "polygonShader",   Input.getInput("de/jreality/jogl/shader/resources/standard3dlabs.vert"), 
//						Input.getInput("de/jreality/jogl/shader/resources/standard3dlabs.frag"));
			} catch (IOException e) {
				e.printStackTrace();
			}
//			ap.setAttribute("polygonShadername", "glsl");
			ap.setAttribute("useGlsl", true);
			hyperbolicShader.setUniform("hyperbolic", true);
//			hyperbolicShader.setUniform("doTexture", 0);
			theFoog = SceneGraphUtility.createFullSceneGraphComponent("normal shader");
			theFoog.setGeometry(ifs);
		   	MatrixBuilder.hyperbolic().translate(0,0,-.5).assignTo(theFoog);
			theFoog2.addChild(theFoog);
			world.addChild(theFoog2);
		   	world.addChild(mymakeLights());
//		   	world.addChild(GeometryUtilityOverflow.displayFaceNormals(ifs, .1, Pn.HYPERBOLIC));
		   	MatrixBuilder.hyperbolic().translate(0,0,-.75).assignTo(world);
			return world;
	}
	

	public int getMetric() {
		return Pn.HYPERBOLIC;
	}
	public boolean addBackPlane() {
		
		return false;
	}
	public boolean isEncompass() {
		
		return false;
	}
	
	public SceneGraphComponent makeLights()	{ return new SceneGraphComponent(); }
	
	public SceneGraphComponent mymakeLights()	{
		SceneGraphComponent lights =  new SceneGraphComponent();
 		SceneGraphComponent spot = SceneGraphUtility.createFullSceneGraphComponent("l1");
		spot.addChild(Primitives.sphere(.05, 0,0,0));
		spot.getAppearance().setAttribute("polygonShader.diffuseColor",Color.YELLOW);
 		PointLight sl = new PointLight();
  		sl.setColor(Color.YELLOW);
 		sl.setIntensity(1.0);
   		spot.setLight(sl);
  		lights.addChild(spot);
		MatrixBuilder.hyperbolic().translate(0,0,.5).assignTo(spot);
		sl.setFalloffA2(.5);
		spot = SceneGraphUtility.createFullSceneGraphComponent("l2");
		spot.addChild(Primitives.sphere(.05, 0,0,0));
		spot.getAppearance().setAttribute("polygonShader.diffuseColor",Color.RED);
 		sl = new PointLight();
  		sl.setColor(Color.RED);
 		sl.setIntensity(1.0);
		Transformation r = spot.getTransformation();
		MatrixBuilder.hyperbolic().translate(.5,0,0).assignTo(spot);
  		spot.setLight(sl);
  		//lights.addChild(spot);
		return lights;
 	}

}
