/*
 * Created on Aug 23, 2004
 *
 */
package charlesgunn.jreality.test;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JMenuBar;
import javax.swing.Timer;

import charlesgunn.anim.sets.AnimatedRectangle2DSet;
import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.util.CameraUtility;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;



/**
 * @author gunn
 *
 */
public class TestCubeTexture extends LoadableScene {
	public TestCubeTexture() {
		super();
	}
	static String resourceDir = "http://www.math.tu-berlin.de/~gunn/Pictures/borromeanRings/"; //"/net/MathVis/data/testData3D/textures/";
	static {
		String foo = Secure.getProperty("resourceDir");
		if (foo != null)	resourceDir  = foo;
	}
	private double[][][] cubeVerts3 = 
	{{{1,1,1}, {1,1,-1}, {1, -1, -1}, {1, -1, 1}},		// right
	 { {-1, 1, -1}, {-1, 1, 1},{-1,-1,1}, {-1,-1,-1}}, // left
	 { {-1, 1,-1},  {1, 1,-1},{1, 1,1}, {-1, 1,1}},		// 	up
	 {  {-1,-1,1},{1,-1,1},{1,-1,-1}, {-1,-1,-1}},		// down
	 {{-1,1,1}, {1,1,1}, {1,-1,1},{-1,-1,1}},		// back
	 {   {1,1,-1},{-1,1,-1}, {-1,-1,-1},{1,-1,-1}}			// front
 };	
	private double aspectRatio = 4/3.0;
	static double[][] square = {{0,-1,0},{1,-1,0},{1,1,0},{0,1,0}};
	static double[][] texc = {{0,0},{1,0},{1,1},{0,1}};
	private SceneGraphComponent theComponent;
	private SceneGraphComponent[] theFaces = new SceneGraphComponent[6];
	final SceneGraphComponent root = SceneGraphUtility.createFullSceneGraphComponent();
	private Appearance withTex;
	int numImages = 2;
	  Texture2D[] tex2d = new Texture2D[numImages];
	float[] bcolor = {1f, 1f, 1f, .5f};
	boolean useWeave = true;
	double u = 5, v = 3;
	double dangle = .01;
	String[] imageNames = {"s03s-3096.png", "temple11.jpg", "temple08.jpg", "cremona.jpg", "dcp00498.jpeg","dcp00499.jpeg"};
	public SceneGraphComponent makeWorld() {
		root.setName("world");
		int[] indices = {4,0, 1, 2, 3, 5};
		for (int q = 0; q<numImages; ++q)	{
			withTex = new Appearance();
			new Appearance();
			withTex.setAttribute(CommonAttributes.VERTEX_DRAW, false);
			withTex.setAttribute(CommonAttributes.EDGE_DRAW, false);
			withTex.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
			theComponent = SceneGraphUtility.createFullSceneGraphComponent("love");
			theComponent.setAppearance(withTex);
			withTex.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.WHITE);
			withTex.setAttribute(CommonAttributes.DIFFUSE_COEFFICIENT, 1.0);
			withTex.setAttribute(CommonAttributes.TRANSPARENCY, 0.0);
			//ap1.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
			withTex.setAttribute(CommonAttributes.SPECULAR_COEFFICIENT, 0.6);
			tex2d[q] = (Texture2D) AttributeEntityUtility
			       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d", withTex, true);		
			  try {
			      imageData = ImageData.load(Input.getInput(resourceDir+imageNames[q]));
//			      BufferedImage bi = (BufferedImage) imageData.getImage();
//			      bi = ImageUtility.resizeToPowerOfTwo(bi);
//			      imageData = new ImageData(bi);
			      tex2d[q].setImage(imageData);
//				    BufferedImage bi = (BufferedImage) id.getImage();
//				    TextureData td = new TextureData(0,0,true,bi);
//				    Texture texture = TextureIO.newTexture(td);
//				    tex2d[q].setJOGLTexture(texture);
			    } catch (IOException e) {
			      e.printStackTrace();
			    }
			    //tex2d[q].setAnimated(true);
			    tex2d[q].setMipmapMode(true);
			    BufferedImage bi = (BufferedImage) imageData.getImage();
			    System.err.println("Image channels: "+ bi.getColorModel().getNumComponents());
				tex2d[q].setRepeatS(Texture2D.GL_CLAMP);
				tex2d[q].setRepeatT(Texture2D.GL_CLAMP);
				tex2d[q].setApplyMode(Texture2D.GL_COMBINE);
				tex2d[q].setCombineModeColor(Texture2D.GL_MODULATE); //MODULATE); //
				tex2d[q].setBlendColor(new Color(0,0,0, 128));				
//				}

			double[][] texc = {{0,0},{1,0},{1,1} ,{0,1}};
			IndexedFaceSet square = IndexedFaceSetUtility.constructPolygon(cubeVerts3[indices[q]]);
			square.setVertexAttributes(Attribute.TEXTURE_COORDINATES,StorageModel.DOUBLE_ARRAY.array(2).createReadOnly(texc));
			MatrixBuilder.euclidean().scale(aspectRatio, 1, aspectRatio).assignTo(theComponent);
			theComponent.setGeometry(square);
			theFaces[q] = theComponent;
			root.addChild(theComponent);
		
		}
		return root;
	}
	
	double cx1 = 500/725.0, cy1 = 280/580.0;
	double[] t1 = P3.makeTranslationMatrix(null, new double[]{cx1, cy1,0}, Pn.EUCLIDEAN);
		
	public boolean addBackPlane()	{return false;}
 	
	public boolean isEncompass() {
		return true;
	}
	double w = .1;
	double[] dkeys = {0, 2, 4};
	Rectangle2D[] values = {
			new Rectangle2D.Double(0,0,1,1),
			new Rectangle2D.Double(cx1-w/2, cy1-w/2,w,w),
			new Rectangle2D.Double(0,0,1,1),
	};
	double[][] dks = {dkeys};
	Rectangle2D[][] vs = {values};
	AnimatedRectangle2DSet animRec = new AnimatedRectangle2DSet(dks, vs);
	Timer rotate, stretch;
	@Override
	public void customize(JMenuBar menuBar, final Viewer viewer) {
		animRec.setWrapType(AnimationUtility.BoundaryModes.REPEAT);
		aspectRatio = CameraUtility.getAspectRatio(viewer);
		System.err.println("aspect ratio is "+aspectRatio);
//		MatrixBuilder.euclidean().scale(aspectRatio, 1, aspectRatio).assignTo(theComponent);
		((Component) viewer.getViewingComponent()).addKeyListener(getKeyAdapter());
		rotate = new Timer(20, new ActionListener()	{
			final double[] m = P3.makeRotationMatrixY(null, -dangle);
			public void actionPerformed(ActionEvent e) {
				root.getTransformation().multiplyOnRight(m);
				viewer.renderAsync();
			}
			
		});
		stretch = new Timer(20, new ActionListener()	{
			 Matrix m = new Matrix(
					Rn.conjugateByMatrix(null, P3.makeStretchMatrix(null, .995), t1));
			 double t = 0, dt = .01;
			public void actionPerformed(ActionEvent e) {
				Matrix mm = new Matrix();
//				MatrixBuilder.euclidean(tex2d.getTextureMatrix()).times(m).assignTo(mm);
				Rectangle2D[] ar = animRec.getValuesAtTime(t, null);
				System.err.println("AR is "+ar[0].toString());
				tex2d[1].setTextureMatrix(new Matrix(AnimationUtility.matrixFromRectangle(null, ar[0])) );
				System.err.println("tex matrix is "+Rn.matrixToString(tex2d[1].getTextureMatrix().getArray()));
				viewer.renderAsync();
				t += dt;
			}
			
		});
	}

	KeyAdapter ka = null;
	private ImageData imageData;
	public KeyAdapter getKeyAdapter() {
		if (ka == null)	{
			ka = new KeyAdapter()	{
				boolean rotating = false;
				boolean stretching = false;
				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_H:
						System.out.println("	1: toggle rotate");
						System.out.println("	2: toggle labels");
						break;
		
					case KeyEvent.VK_1:
						rotating = !rotating;
						if (rotating) rotate.start();
						else rotate.stop();
						break;
						
					case KeyEvent.VK_2:
						stretching = !stretching;
						if (stretching) stretch.start();
						else stretch.stop();

				}
		
				}
			};
		}
		return ka;
	}
		

}
 
