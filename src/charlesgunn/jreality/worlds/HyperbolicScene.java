/*
 * Created on Sep 15, 2004
 *
*/
package charlesgunn.jreality.worlds;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.LIGHTING_ENABLED;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.METRIC;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.RMAN_GLOBAL_INCLUDE_FILE;
import static de.jreality.shader.CommonAttributes.SPECULAR_EXPONENT;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JMenuBar;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.SphereUtility;
import de.jreality.geometry.ParametricSurfaceFactory.Immersion;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.GlslProgram;
import de.jreality.util.CameraUtility;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;

public class HyperbolicScene extends LoadableScene {

	boolean programOnly = true, useVertexArrays = false;
	GlslProgram hyperbolicShader = null;
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	double[][] falloffs = {{1,.25,0},{.5, .5,0},{.5, .5, 0}};
	double[][] cameraClips = {{.001,2},{.01, 1000},{.01,-.05}};
	double distance = .5;
	double[] unitD = {Math.tanh(distance), distance, Math.tan(distance)};
	double ballRadius = .015, stickRadius = .01;
	int metric = Pn.EUCLIDEAN;
	private ParametricSurfaceFactory psf;
	private SceneGraphComponent[] sigs = new SceneGraphComponent[3];
	private IndexedFaceSet ifs;
	private PointLight pointLight;
	private SceneGraphComponent lightNode;
	private SceneGraphComponent wireframeSphere;
	Viewer viewer;
	public SceneGraphComponent makeWorld() {
		
		Appearance ap = world.getAppearance();
		try {
			hyperbolicShader = new GlslProgram(ap, POLYGON_SHADER,   Input.getInput("de/jreality/jogl/shader/resources/noneuclidean.vert"), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (programOnly) ap.setAttribute("useGLSL",metric != Pn.EUCLIDEAN);
		ap.setAttribute("useVertexArrays", useVertexArrays);
		
		hyperbolicShader.setUniform("Nw", 0.00001);
		hyperbolicShader.setUniform("useNormals4", !programOnly && useVertexArrays);
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR,new Color(250, 250, 0));
		ap.setAttribute(LINE_SHADER+"."+POLYGON_SHADER+"."+DIFFUSE_COLOR,new Color(250, 0, 250));
		ap.setAttribute(SPECULAR_EXPONENT, 50.0);
		ap.setAttribute(TUBE_RADIUS, .003);
		lightNode = SceneGraphUtility.createFullSceneGraphComponent("child2");
		lightNode.getAppearance().setAttribute(LIGHTING_ENABLED, false);
		lightNode.addChild(Primitives.sphere(.05, new double[]{0,0,0}, Pn.EUCLIDEAN));
		pointLight = new PointLight();
		pointLight.setFalloff(falloffs[metric+1]);
		pointLight.setIntensity(1);
		pointLight.setColor(new Color(250, 250, 250));
		lightNode.setLight(pointLight);
		world.addChild(lightNode);
		
		for (int i = 0; i<3; ++i)	{
			sigs[i] = SceneGraphUtility.createFullSceneGraphComponent("sig"+i);
			SceneGraphComponent disk = SceneGraphUtility.createFullSceneGraphComponent("disk"+i);
			ifs = getDisk(i-1);
			disk.setGeometry(ifs);
			sigs[i].addChild(disk);
			SceneGraphComponent normals = SceneGraphUtility.createFullSceneGraphComponent("normals");
			normals.addChild(IndexedFaceSetUtility.displayFaceNormals(ifs, .1, i-1));
			sigs[i].addChild(normals);
			sigs[i].getAppearance().setAttribute(VERTEX_DRAW, false);
			sigs[i].getAppearance().setAttribute(EDGE_DRAW, false);
			BallAndStickFactory basf = new BallAndStickFactory(ifs);
			basf.setStickColor(Color.red);
			basf.setStickRadius(stickRadius);
			basf.setBallColor(Color.blue);
			basf.setBallRadius(ballRadius);
			basf.setMetric(i-1);
			basf.update();
			sigs[i].addChild(basf.getSceneGraphComponent());
			world.addChild(sigs[i]);
			sigs[i].setVisible(false);
		}
		sigs[metric+1].setVisible(true);
		wireframeSphere = Primitives.wireframeSphere();
		wireframeSphere.getAppearance().setAttribute(METRIC, Pn.EUCLIDEAN);
		wireframeSphere.getAppearance().setAttribute(LINE_SHADER+"."+TUBE_RADIUS,.0015);
		wireframeSphere.getAppearance().setAttribute(LIGHTING_ENABLED, false);
		sigs[0].addChild(wireframeSphere);
		
//		double[][] square = {{-.5,-.5,0},{.5,-.5,0},{.5,.5,0},{-.5,.5,0}};
//		IndexedFaceSet ifs2 = IndexedFaceSetUtility.constructPolygon(null,square, Pn.HYPERBOLIC);
//		if (simple) child1.setGeometry(ifs2);
		SceneGraphComponent cube = SceneGraphUtility.createFullSceneGraphComponent();
		IndexedFaceSet ccc = Primitives.coloredCube();
		double[][] verts = ccc.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		Rn.matrixTimesVector(verts, P3.makeScaleMatrix(null, .2), verts);
		ccc.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(verts));
		
		cube.addChild(SphereUtility.tessellatedCubeSphere(7));  //setGeometry(ccc); //
		cube.getAppearance().setAttribute(EDGE_DRAW, false);
		MatrixBuilder.euclidean().scale(.2).assignTo(cube);
		world.addChild(cube);
		wireframeSphere.setVisible(metric == Pn.HYPERBOLIC);
		return world;
	}


	private IndexedFaceSet getDisk(final int metric) {
		psf = new ParametricSurfaceFactory();
		psf.setMetric(metric);
		psf.setULineCount(17);
		psf.setVLineCount(15);
	    psf.setUMin(0.0);
	    psf.setUMax(Math.PI * 2);
	    psf.setVMin(0.01);
	    psf.setVMax(.99);
	    psf.setGenerateFaceNormals(true);
	    psf.setGenerateEdgesFromFaces(true);
	    psf.setGenerateVertexNormals(true);
		psf.setImmersion( new Immersion()		{
			double[] scales = {1.5,1,Math.PI/4};
			double[] foo = {1,0,0,0};
			public boolean isImmutable() {
				// TODO Auto-generated method stub
				return false;
			}

			public int getDimensionOfAmbientSpace() {
				return 4;
			}

			public void evaluate(double u, double v, double[] xyz, int index) {
				double[] p = Pn.dragTowards(null, P3.originP3, foo, v * scales[metric+1], metric); //distanceBetween(Pn.originP3, foo, metric);
				
				xyz[0] = Math.cos(u) * p[0];
				xyz[1] = Math.sin(u) * p[0];
				xyz[2] = 0.0;
				xyz[3] = p[3];
			}
			
		});
		psf.update();
		IndexedFaceSet ifs = psf.getIndexedFaceSet();
		return ifs;
	}
	
	public SceneGraphComponent makeLights()	{
		return new SceneGraphComponent();
	}

	public void update()	{
		viewer.getSceneRoot().getAppearance().setAttribute("metric", metric);
		CameraUtility.getCamera(viewer).setNear(cameraClips[metric+1][0]);
		CameraUtility.getCamera(viewer).setFar(cameraClips[metric+1][1]);
		for (int i = 0; i<3; ++i) sigs[i].setVisible(false);
		sigs[metric+1].setVisible(true);
		if (metric != 0) {
			if (programOnly) world.getAppearance().setAttribute("useGLSL", true);
			else {
				world.getAppearance().setAttribute("polygonShadername", "glsl");
				world.getAppearance().setAttribute("lineShader.polygonShadername", "glsl");							
			}
			hyperbolicShader.setUniform("hyperbolic", Pn.HYPERBOLIC==metric);
		} else {
			if (programOnly) world.getAppearance().setAttribute("useGLSL", false);
			else {
				world.getAppearance().setAttribute("lineShader.polygonShadername", Appearance.INHERITED);
				world.getAppearance().setAttribute("polygonShadername", Appearance.INHERITED);
			}
		}
		hyperbolicShader.setUniform("useNormals4", !programOnly && useVertexArrays);
		pointLight.setFalloff(falloffs[metric+1]);
		MatrixBuilder.init(null, metric).translate(0,0,unitD[metric+1]).assignTo(lightNode);
		MatrixBuilder.init(null, metric).translate(0,0,2*unitD[metric+1]).assignTo(CameraUtility.getCameraNode(viewer));
		viewer.renderAsync();
		wireframeSphere.setVisible(metric == Pn.HYPERBOLIC);
		System.err.println("metric is "+metric);

	}
	@Override
	public void customize(JMenuBar menuBar, final Viewer v) {
		viewer = v;
		viewer.getSceneRoot().getAppearance().setAttribute("metric", metric);
		viewer.getSceneRoot().getAppearance().setAttribute(RMAN_GLOBAL_INCLUDE_FILE, "quality.rib");
		update();
		((Component) viewer.getViewingComponent()).addKeyListener( new KeyAdapter()	{
			public void keyPressed(KeyEvent e)	{ 
				switch(e.getKeyCode())	{
					
				case KeyEvent.VK_H:
					System.out.println("	2/shift-2:  cycle metric");
					break;
	
				case KeyEvent.VK_2:
					metric ++;
					if (metric == 2) metric = -1;
					update();
					break;
					
				case KeyEvent.VK_3:
					useVertexArrays = !useVertexArrays;
					programOnly = !useVertexArrays;
					update();
					break;	
			}

			}
		});
	}

}
