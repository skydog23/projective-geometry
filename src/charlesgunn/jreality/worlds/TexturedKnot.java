/*
 * Created on Jan 29, 2004
 *
 */
package charlesgunn.jreality.worlds;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JMenuBar;

import charlesgunn.anim.sets.AnimatedDoubleArraySet;
import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.texture.RopeTextureFactory;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.math.HomotopyFactory;
import de.jreality.geometry.PolygonalTubeFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.DataList;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.util.CameraUtility;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;


/**
 * @author Charles Gunn
 *
 */
public class TexturedKnot extends LoadableScene {
	SceneGraphComponent icokit;
	boolean tryFlatten = true;
	/**
	 * 
	 */
	public TexturedKnot() {
		super();
	}
		double uStretch = 7, vStretch = 60;
	    Color band1color = new Color(1f, 1f, .8f),
    	shadowColor = new Color(0,0,0,255), 
		gapColor = new Color(0,0,0,0);
		final Color borrColors3 = new Color(.85f, .1f, .1f); // new Color(1f, .8f, 0f), new Color(0f, .4f, .1f)};
		final double bandWidth = .5,
		shadowWidth = .18,
		blendFactor = 0.0;
		double tubeRadius = .35, multiplier = 1.05;
	
		public SceneGraphComponent makeWorld()	{
			SceneGraphComponent theWorld = SceneGraphUtility.createFullSceneGraphComponent("world");
			Appearance ap  = theWorld.getAppearance();
			ap.setAttribute(CommonAttributes.SPECULAR_COEFFICIENT, .2);
			ap.setAttribute(CommonAttributes.SPECULAR_EXPONENT, 125);
			ap.setAttribute(CommonAttributes.AMBIENT_COEFFICIENT, 0.05);
			ap.setAttribute(CommonAttributes.AMBIENT_COLOR, new Color(.8f, .4f, .4f));
			MatrixBuilder.euclidean().rotateX(-Math.PI/90).rotateZ(-Math.PI/10).assignTo(theWorld);
			knot = SceneGraphUtility.createFullSceneGraphComponent("knot");
			ap = knot.getAppearance();
			SceneGraphComponent tmp = null;
			try {
				tmp = (Readers.read(Input.getInput("http://www.math.tu-berlin.de/~gunn/Documents/Models/geomview/8_18.vect")));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Texture2D   tex2d = (Texture2D) AttributeEntityUtility
		       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d", ap, true);		
		  		try {
		  			ImageData id = ImageData.load(Input.getInput("/Users/gunn/Pictures/textures/azalea.jpg")); //weaveRGBABright.png"));
		  			tex2d.setImage(id);
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		   tex2d.setTextureMatrix( new Matrix(P3.makeStretchMatrix(null, new double[]{2,20,0})));
		   tex2d.setBlendColor(new Color(0f,0f,0f,.5f));
			IndexedLineSet knotcurve = (IndexedLineSet) tmp.getGeometry();
			//colorByLength(knotcurve, new double[] {1,0,0}, new double[] {1,1,1});
			double[] dkeys = {0,.25, .25, .25, .25};
			double[][] dvalues = {{0,1,.4},{1,1,1},{1,0,0},{1,1,1},{0,1,.4}};
			AnimatedDoubleArraySet adas = new AnimatedDoubleArraySet(dkeys, dvalues, 
					AnimationUtility.InterpolationTypes.LINEAR);
			knotVertexColors = colorByLength(knotcurve, adas);
//			System.err.println("Length of color[0] is "+knotVertexColors[0].length);
//			for (int i = 0; i<knotVertexColors.length; ++i)	{
//				if (knotVertexColors[i].length != 3)
//						throw new IllegalStateException("bad color");
//			}
//			knotcurve.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(knotVertexColors));
			DataList vcolors = knotcurve.getVertexAttributes(Attribute.COLORS);
			System.err.println("Length of color[0] is "+vcolors.item(0).size());
			 
			int size = 32;
			double scale = 1;
		    double[][] mysection = new double[size][3];
		    for (int i = 0; i<size; ++i)	{
		   		double angle = (i/(size-1.0)) * Math.PI * 2;
		   		double d = 1; //(1+.05*((Math.cos(8*angle))));
				mysection[i][0] = scale * Math.cos(angle)  *d;
		   		mysection[i][1] = scale *  Math.sin(angle)  *d;
//		   		mysection[i][0] = scale * Math.cos(angle); 
//		   		mysection[i][1] = scale *  Math.sin(angle); 
		   		mysection[i][2] = 0.0;
		    }
			
		    ptf = new PolygonalTubeFactory(knotcurve, 0);
		    ptf.setClosed(true);
		   ptf.setRadius(.35);
		   ptf.setCrossSection(mysection);
//		   ptf.setTwists(8);
//		   ptf.setVertexColorsEnabled(true);
		   ptf.setRemoveDuplicates(true);
//		   ptf.setVertexColors(knotVertexColors);
		   ptf.setGenerateTextureCoordinates(true);
		   ptf.setArcLengthTextureCoordinates(true);
		   ptf.update();
		   IndexedFaceSet torus1Tubes = ptf.getTube();
		   theWorld.setGeometry(knotcurve); //torus1Tubes); //ils);
		   knot.setGeometry(torus1Tubes);
			theWorld.addChild(knot);
		   
			ap.setAttribute("polygonShader.diffuseColor", Color.white);
			ap = new Appearance();
			RopeTextureFactory tu = new RopeTextureFactory(ap);
			tu = new RopeTextureFactory(ap);
			tu.setN(Math.sqrt(2)*uStretch);
			tu.setM(Math.sqrt(2)*vStretch);
			tu.setAngle(Math.PI/4);
			tu.setBand1color(band1color);
			tu.setBand2color(borrColors3);
			tu.setShadowcolor(shadowColor);
			tu.setGapcolor(gapColor);
			tu.setBlendcolor(Color.white);
			tu.setBandwidth(bandWidth);
			tu.setShadowwidth(shadowWidth);
			tu.setBlendfactor(blendFactor);
			tu.update();
			return theWorld;
		}
	
		public boolean isEncompass() {
			return true;
		}

		final Color URBackground = new Color(.8f, .85f, .68f); //new Color(215, 215, 190);
		final Color ULBackground  = new Color(1f, .98f, .8f); //new Color(255, 255, 200);  // bg[1];
		final Color LLBackground  = new Color(.1f, .3f, .15f); //new Color(20,20,60);
		final Color LRBackground  = new Color(0.05f, .35f, .15f); //new Color(25, 25, 100);  //bg[2];
		private PolygonalTubeFactory ptf;
		private SceneGraphComponent knot;
		private static double[][] knotVertexColors;

		public void customize(JMenuBar menuBar, final Viewer viewer) {
			viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.RMAN_SHADOWS_ENABLED, true);
			Color[] bg = new Color[4];
			bg[0] = URBackground;
			bg[1] = ULBackground;// bg[1];
			bg[2] = LLBackground;
			bg[3] = LRBackground;  //bg[2];
			//viewer.getSceneRoot().getAppearance().setAttribute("backgroundColors", bg);
			viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", Color.white);
			CameraUtility.getCamera(viewer).setFieldOfView(20);
			((Component) viewer.getViewingComponent()).addKeyListener( new KeyAdapter()	{
				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_H:
						System.out.println("	2/shift-2:  increase/decrease tube radius");
						break;
		
					case KeyEvent.VK_2:
						if (e.isShiftDown()) tubeRadius /= multiplier;
						else tubeRadius *= multiplier;
						ptf.setRadius(tubeRadius);
						ptf.update();
						knot.setGeometry(ptf.getTube());
						viewer.render();
						break;
						
				}
	
				}
			});
			}
//		public SceneGraphComponent makeLights() {
//			SceneGraphComponent lightNode = new SceneGraphComponent();
//			lightNode.setName("lights");
//			SceneGraphComponent l0 = SceneGraphUtility.createFullSceneGraphComponent("light0");
//			DirectionalLight dl = new DirectionalLight();
//			dl.setColor(new Color(250, 250, 00));
//			dl.setIntensity(.5);
//			double[] zaxis = {0,0,1};
//			double[] other = {0,1,1};
//			l0.getTransformation().setMatrix( P3.makeRotationMatrix(null, zaxis, other));
//			System.err.println("Light0 position is "+Rn.toString(Rn.matrixTimesVector(null,l0.getTransformation().getMatrix(), zaxis)));
//			l0.setLight(dl);
//			lightNode.addChild(l0);
//					
//			dl = new DirectionalLight();
//			dl.setColor(new Color(250,0, 225));
//			dl.setIntensity(.5);
//			l0 = SceneGraphUtility.createFullSceneGraphComponent("light1");
//			double[] other2 = {-.6,-.2,.5};
//			l0.getTransformation().setMatrix( P3.makeRotationMatrix(null, zaxis, other2));
//			l0.setLight(dl);
//			lightNode.addChild(l0);
//			l0.getAppearance().setAttribute(CommonAttributes.RMAN_SHADOWS_ENABLED, false);
//			System.err.println("Light1 position is "+Rn.toString(Rn.matrixTimesVector(null,l0.getTransformation().getMatrix(), zaxis)));
//			
//			
//			return lightNode;
//		}
		public static double[][] colorByLength(IndexedLineSet ils, AnimatedDoubleArraySet adas)	{
			int nPts = ils.getNumPoints();
			int chans = 4;
			knotVertexColors = new double[nPts][chans];
			double[][] vertices = ils.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
			double length = HomotopyFactory.getLength(vertices, true);
			double[] vals= new double[3];
			double running = 0.0;
			for (int i = 1; i<nPts-1; ++i)	{
				running += Rn.euclideanDistance(vertices[i], vertices[i-1]);
				//double t = .5 + .5*Math.cos(-Math.PI/4+Math.PI*4*running/length);
				double t = .25+4*running/length;
				adas.getValuesAtTime(t, vals);
				System.err.println("time "+t+" value: "+Rn.toString(vals));
				System.arraycopy(vals, 0, knotVertexColors[i], 0, 3);
				//Rn.linearCombination(colors[i],t, color2, 1-t, color1);
				if (chans > 3) knotVertexColors[i][3] = 1.0;
			}
			System.arraycopy(knotVertexColors[1], 0, knotVertexColors[0], 0, chans);
			System.arraycopy(knotVertexColors[nPts-2], 0, knotVertexColors[nPts-1], 0, chans);
			return knotVertexColors;
		}

		@Override
		public Component getInspector(Viewer v) {
			// TODO Auto-generated method stub
			return super.getInspector(v);
		}

		@Override
		public boolean hasInspector() {
			// TODO Auto-generated method stub
			return super.hasInspector();
		}
			
	
}
