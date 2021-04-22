/*
 * Created on Jan 29, 2004
 *
 */
package charlesgunn.jreality.worlds;

import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.RMAN_SHADOWS_ENABLED;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JMenuBar;

import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.jreality.texture.SimpleTextureFactory.TextureType;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PolygonalTubeFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;


/**
 * @author Charles Gunn
 *
 */
public class TubeExample extends LoadableScene {
		
	SceneGraphComponent theWorld = SceneGraphUtility.createFullSceneGraphComponent("world"),
		lightNode = SceneGraphUtility.createFullSceneGraphComponent("child2");

		public SceneGraphComponent makeWorld()	{
			double[][] crosssection = new double[32][3];
			double start = 0.0;
			for (int  i =0; i<32; ++i)	{
				double angle = start+i * 2.0*Math.PI/31.0;
				crosssection[i][0] = Math.cos(angle);
				crosssection[i][1] = Math.sin(angle);
				crosssection[i][2] = 0.0;
			}
			double d = 1.0;
			double[] lim = {Pn.tanh(d), d, Math.tan(d)};
			int[][] edges = new int[1][50];
			for (int j = 0; j<50; ++j)	{
				edges[0][j] = j;
			}
			for (int i = 0; i<3; ++i)	{
				double[] left = {0,0,0,1}, right = {lim[i],0,0,1};
				IndexedLineSetFactory seg = new IndexedLineSetFactory();
				seg.setVertexCount(50);
				int metric = i-1;
				double[] midpoint = Pn.linearInterpolation(null, left, right, .5, metric);
				double[][] verts = new double[50][4];
				for (int j = 0; j<50; ++j)	{
					Pn.linearInterpolation(verts[j], left, right, (j)/49.0,metric);
//					Rn.matrixTimesVector(verts[j], tlate, verts[j]);
				}
				seg.setVertexCoordinates(verts);
				seg.setEdgeCount(1);
				seg.setEdgeIndices(edges);
				seg.update();
				PolygonalTubeFactory ptf = new PolygonalTubeFactory(seg.getIndexedLineSet(),0);
				ptf.setArcLengthTextureCoordinates(true);
				ptf.setMetric(metric);
				ptf.setRadius(.13);
				ptf.setClosed(false);
				ptf.setCrossSection(crosssection);
				ptf.setGenerateTextureCoordinates(true);
				ptf.update();
				SceneGraphComponent child = SceneGraphUtility.createFullSceneGraphComponent("child"+i);
				SceneGraphComponent child2 = new SceneGraphComponent("tlate");
				SceneGraphComponent child3 = new SceneGraphComponent("core");
				child3.setAppearance(new Appearance());
				child3.getAppearance().setAttribute(LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
				child3.getAppearance().setAttribute(LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, 2);
				child3.getAppearance().setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, Color.red);
				child3.setGeometry(seg.getIndexedLineSet());
				child2.setGeometry(ptf.getTube());
				child2.addChild(child3);
				child.addChildren(child2);
				new Matrix(Rn.inverse(null, MatrixBuilder.init(null, metric).translate(midpoint).getArray())).assignTo(child2);
//				MatrixBuilder.init(null, 0).translate(0,.2*(i-1),0).assignTo(child);
				child.getAppearance().setAttribute(CommonAttributes.METRIC, metric);
				child.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.white);
//				child.getAppearance().setAttribute(LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
//				child.getAppearance().setAttribute(LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .08);
				child.getAppearance().setAttribute("useGLSL", true);
				theWorld.addChild(child);
				if (i != current) child.setVisible(false);
			}
			theWorld.getAppearance().setAttribute("crossSection", crosssection);
			SimpleTextureFactory stf = new SimpleTextureFactory();
//			stf.setColor(0, new Color(0,0,0,0));	// gap color in weave pattern is totally transparent
			stf.setType(TextureType.GRAPH_PAPER);
			stf.update();
			Texture2D tex2d = TextureUtility.createTexture(theWorld.getAppearance(), "polygonShader", stf.getImageData());
			tex2d.setTextureMatrix(new Matrix(MatrixBuilder.euclidean().scale(8,10,1).getArray()));
			return theWorld;
		}
	
		public boolean isEncompass() {
			return false;
		}

		int current = 0;
		Viewer viewer;
		double distance = .8;
		double[] unitD = {Math.tanh(distance), distance, Math.tan(distance)};
		double[][] falloffs =   {{1.5,.5,0},{.5,.25,0},{.5, .25, 0}};
		private PointLight pointLight;
		@Override
		public void customize(JMenuBar menuBar, Viewer v) {
			viewer = v;
			System.err.println("in customize");
			viewer.getSceneRoot().getAppearance().setAttribute(RMAN_SHADOWS_ENABLED, true);
			viewer.getSceneRoot().getAppearance().setAttribute(BACKGROUND_COLOR, Color.black);
			viewer.getSceneRoot().getAppearance().setAttribute("metric", current-1);
//			MatrixBuilder.init(null, current-1).translate(0,0,unitD[current]).assignTo(lightNode);
			SceneGraphUtility.removeLights(viewer);
			pointLight = new PointLight();
			pointLight.setIntensity(1);
			pointLight.setColor(new Color(250, 250, 250));
			lightNode.setLight(pointLight);
			pointLight.setFalloff(falloffs[current]);
			CameraUtility.getCameraNode(viewer).addChild(lightNode);
			((Component) viewer.getViewingComponent()).addKeyListener( new KeyAdapter()	{
				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_1:
						System.err.println("Got 1");
						theWorld.getChildComponent(current).setVisible(false);
						current = (current+1)%3;
						theWorld.getChildComponent(current).setVisible(true);
						viewer.getSceneRoot().getAppearance().setAttribute("metric", current-1);
//						MatrixBuilder.init(null, current-1).translate(0,0,unitD[current]).assignTo(lightNode);
//						MatrixBuilder.init(null, current-1).translate(0,0,unitD[current]).
//							assignTo(CameraUtility.getCameraNode(viewer));
						pointLight.setFalloff(falloffs[current]);
						break;
				}

				}
			});
		}
			
	
}
