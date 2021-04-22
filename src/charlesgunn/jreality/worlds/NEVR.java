/*
 * Created on Jan 29, 2004
 *
 */
package charlesgunn.jreality.worlds;

import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.JMenuBar;

import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.backends.label.LabelUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.SceneGraphUtility;


/**
 * @author Charles Gunn
 *
 */
public class NEVR extends LoadableScene {
		
	SceneGraphComponent theWorld = SceneGraphUtility.createFullSceneGraphComponent("world"),
		lightNode = SceneGraphUtility.createFullSceneGraphComponent("child2");
	double scale = .57;
		public SceneGraphComponent makeWorld()	{
			String[] names = {"Hyperbolic", "Euclidean", "Elliptic"};
			theWorld.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS, (scale/.45)*.01);
			theWorld.getAppearance().setAttribute("lineShader."+CommonAttributes.LINE_WIDTH, 4.0);
			theWorld.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS, (scale/.45)*.01);
			theWorld.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
			theWorld.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
			theWorld.getAppearance().setAttribute(CommonAttributes.DIFFUSE_COLOR, Color.white);
			SceneGraphComponent child;
			IndexedFaceSet ifs = Primitives.texturedQuadrilateral(
					new double[]{
							-scale, -scale, -scale, 
							scale,-scale, -scale  , 
							scale,scale, -scale ,
							-scale,scale, -scale});
//			new double[]{
//					-scale, -scale, 0, 
//					scale,-scale, 0  , 
//					scale,scale, 0 ,
//					-scale,scale, 0});
			for (int i = 0; i<3; ++i)	{
				child = SceneGraphUtility.createFullSceneGraphComponent("child2");
				theWorld.addChild(child);
				child.setGeometry(ifs);
				child.getAppearance().setAttribute("metric", i-1);
				BufferedImage bi = LabelUtility.createImageFromStrings(new String[]{" ",names[i]," "}, null, Color.black, new Color(0,0,0,0));
				ImageData id= new ImageData(bi);
				Texture2D tex2d = TextureUtility.createTexture(child.getAppearance(), "polygonShader", 1, id);
				tex2d.setApplyMode(Texture2D.GL_DECAL);
				tex2d.setTextureMatrix(new Matrix(MatrixBuilder.euclidean().scale(1,-3,1).getArray()));
				SimpleTextureFactory stf = new SimpleTextureFactory();
				stf.setType(SimpleTextureFactory.TextureType.LINE);
				stf.update();
				Texture2D grid = TextureUtility.createTexture(child.getAppearance(), "polygonShader", 0, stf.getImageData());
				//gridTexture2d[i].setImage(stf.getImageData());
				grid.setTextureMatrix(new Matrix(P3.makeStretchMatrix(null,
						new double[]{10,-10,1})));
				grid.setApplyMode(Texture2D.GL_DECAL);
				grid.setBlendColor(new Color(1f, 1f, 1f, 1f));							
				MatrixBuilder.init(null, i-1).translate(0,0,-scale).scale(1).assignTo(child);
				child.setVisible(i == current);
			}
			child = SceneGraphUtility.createFullSceneGraphComponent("child2");
			theWorld.addChild(child);
			child.setGeometry(ifs);
			child.getAppearance().setAttribute("metric", 0);
			MatrixBuilder.euclidean().translate(0,0,-scale+scale*.005).scale(1).assignTo(child);
			child.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
			child.getAppearance().setAttribute(CommonAttributes.DIFFUSE_COLOR, Color.red);
			child.setVisible(true);
			return theWorld;
		}
	
		public boolean isEncompass() {
			return false;
		}

		int current = 1;
		Viewer viewer;
		double distance = .8;
		double[] unitD = {Math.tanh(distance), distance, Math.tan(distance)};
		double[][] falloffs =   {{1.5,.5,0},{.5,.25,0},{.5, .25, 0}};
		private PointLight pointLight;
		@Override
		public void customize(JMenuBar menuBar, Viewer v) {
			viewer = v;
			System.err.println("in customize");
			viewer.getSceneRoot().getAppearance().setAttribute(BACKGROUND_COLOR, Color.black);
			viewer.getSceneRoot().getAppearance().setAttribute("metric", current-1);
//			MatrixBuilder.init(null, current-1).translate(0,0,unitD[current]).assignTo(lightNode);
			((Component) viewer.getViewingComponent()).addKeyListener( new KeyAdapter()	{
				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_1:
						System.err.println("Got 1");
						theWorld.getChildComponent(current).setVisible(false);
						current = (current+1)%3;
						theWorld.getChildComponent(current).setVisible(true);
						viewer.renderAsync();
//						viewer.getSceneRoot().getAppearance().setAttribute("metric", current-1);
//						MatrixBuilder.init(null, current-1).translate(0,0,unitD[current]).assignTo(lightNode);
//						MatrixBuilder.init(null, current-1).translate(0,0,unitD[current]).
//							assignTo(CameraUtility.getCameraNode(viewer));
//						pointLight.setFalloff(falloffs[current]);
						break;
				}

				}
			});
		}
			
	
}
