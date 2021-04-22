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
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
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
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;



/**
 * @author gunn
 *
 */
public class TestTexture extends LoadableScene {
	public TestTexture() {
		super();
	}
	static String resourceDir = "/net/MathVis/data/testData3D/textures/";
	static {
		String foo = Secure.getProperty("resourceDir");
		if (foo != null)	resourceDir  = foo;
	}
	static double[][] square = {{0,-1,0},{1,-1,0},{1,1,0},{0,1,0}};
	double phi = .5*(Math.sqrt(5)-1);
	double s = Math.sqrt((1+phi*phi*phi*phi)/3.0);
	double[][] pentagon = {{phi*phi,0,1},{s,s,s},{0,1,phi*phi},{-s,s,s},{-phi*phi,0,1}};
	double[][] texc = {{0,0},{1,0},{1,1},{0,1}};
	double[][] texcpent = {{0,1},{1,1},{1,.5},{1,0},{0,0}};
	private SceneGraphComponent theComponent;
	private Appearance withTex;
	private Appearance noTex;
	  Texture2D tex2d = null;
	float[] bcolor = {1f, 1f, 1f, .5f};
	boolean useWeave = true;
	double u = 5, v = 3;
	boolean hasTex = true;
	public SceneGraphComponent makeWorld() {
		boolean testImageCreation = true;
		byte[] im = new byte[128 * 128 * 4];
		if (testImageCreation)	{
			for (int i = 0; i<128; ++i)	{
				for (int j = 0; j< 128; ++j)	{
					int I = 4*(i*128+j);
					int sq = (i-64)*(i-64) + (j-64)*(j-64);
					if (sq < 4096)	
						{ int foo = (int) (255- Math.floor(Math.abs(sq/16.0)));
						if (foo == 0) foo = 1;
						im[I] =  im[I+1] = im[I+2] = im[I+3] = (byte) foo; }
					else
						{im[I] =  im[I+1] = im[I+2] = im[I+3]  = 1;  }
				}
			}
		}
		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("world");
		withTex = new Appearance();
		noTex = new Appearance();
		
			theComponent = SceneGraphUtility.createFullSceneGraphComponent("theClipIcon");
			theComponent.setAppearance(withTex);
//			withTex.setAttribute(CommonAttributes.VERTEX_DRAW, true);
			withTex.setAttribute(CommonAttributes.EDGE_DRAW, false);
			withTex.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
			withTex.setAttribute(CommonAttributes.METRIC, Pn.HYPERBOLIC);
//			withTex.setAttribute(CommonAttributes.FACE_DRAW, true);
//			withTex.setAttribute(CommonAttributes.POLYGON_SHADER,"twoSide");
//			withTex.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.WHITE);
//			withTex.setAttribute(CommonAttributes.DIFFUSE_COEFFICIENT, 1.0);
//			withTex.setAttribute(CommonAttributes.TRANSPARENCY, 0.0);
			//ap1.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
//			withTex.setAttribute(CommonAttributes.SPECULAR_COEFFICIENT, 0.6);
//			if (useWeave)	{
//				tex2d = (new RopeTextureFactory()).makeTextureAppearance(withTex, null, u, v, Math.PI/4,
//			    		.7, .12, 0.0, Color.GREEN, Color.RED, Color.BLACK, Color.BLUE, Color.WHITE);
//				
//			} else {
//			  if (testImageCreation)	{
//			  		ImageData it = new ImageData(im, 128, 128);
//			  		tex2d.setImage(it);
//			  }
//			  	else 
			// set up a "light map"
			boolean testLightMap = true;
			if (testLightMap)	{
				tex2d = (Texture2D) AttributeEntityUtility
			       .createAttributeEntity(Texture2D.class, "polygonShader.lightMap", withTex, true);	
			  	ImageData it = new ImageData(im, 128, 128);
			  	tex2d.setImage(it);
//				try {
//					ImageData id = ImageData.load(Input.getInput(
//							"/homes/geometer/gunn/Pictures/textures/weave.png")); // weaveRGBABright.png"));
//					tex2d.setImage(id);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
			  	
				tex2d.setApplyMode(Texture2D.GL_REPLACE);
//				tex2d.setCombineModeColor(Texture2D.GL_MODULATE); //MODULATE); //
				tex2d.setTextureMatrix(new Matrix(P3.makeStretchMatrix(null,new double[]{5,5,1})));
				//tex2d.setRepeatS(Texture2D.GL_MIRRORED_REPEAT);
				//tex2d.setRepeatT(Texture2D.GL_MIRRORED_REPEAT);				
			}

			Texture2D tex2d2 = (Texture2D) AttributeEntityUtility.createAttributeEntity(
					Texture2D.class, "polygonShader.texture2d", withTex, true);
			try {
				ImageData id = ImageData.load(Input.getInput(
						"/homes/geometer/gunn/Pictures/textures/butterfly-bw-alpha.png")); // weaveRGBABright.png"));
				tex2d2.setImage(id);
			} catch (IOException e) {
				e.printStackTrace();
			}
			tex2d2.setApplyMode(Texture2D.GL_COMBINE);
			tex2d2.setCombineModeColor(Texture2D.GL_INTERPOLATE); // MODULATE); //
			tex2d2.setOperand2Color(Texture2D.GL_SRC_ALPHA);
			tex2d2.setBlendColor(new Color(.5f, .5f, 0f, .5f));				
			double[][] vv = {{-1,-1,0},{-1,1,0},{1,1,0},{1,-1,0}};
			double[][] texc = {{0,0},{1,0},{1,1} ,{0,1}};
			IndexedFaceSet square = IndexedFaceSetUtility.constructPolygon(pentagon);
			square.setVertexAttributes(Attribute.TEXTURE_COORDINATES,StorageModel.DOUBLE_ARRAY.array(2).createReadOnly(texcpent));
			theComponent.setGeometry(square);
			SceneGraphComponent[] level1 = new SceneGraphComponent[3];
			level1[0] = new SceneGraphComponent();
			level1[0].addChild(theComponent);
			level1[1] = new SceneGraphComponent();
			MatrixBuilder.euclidean().reflect(new double[]{0,1,0,0}).assignTo(level1[1]);
			level1[1].addChild(theComponent);
			level1[2] = new SceneGraphComponent();
			MatrixBuilder.euclidean().translate(0,0,1).scale(-1.0, .5, .5).assignTo(level1[2]);
			level1[2].setAppearance(new Appearance());
			level1[2].getAppearance().setAttribute(CommonAttributes.POINT_RADIUS, 1.0);
			level1[2].getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
			double scalef = (phi*phi);
			double[][] points = {
					{scalef-.1, 0, 0},
					{scalef/3+.07, 0, 0},
					{scalef/6+.05, 0, 0},
					{-scalef/6+.05, 0, 0},
					{-scalef/2+.05, 0, 0},
					{-scalef+.15, 0, 0}
			};
			double[] rad = {.1, .14, .15, .135, .12, .1};
			PointSetFactory psf = new PointSetFactory();
			psf.setMetric(Pn.HYPERBOLIC);
			psf.setVertexCount(points.length);
			psf.setVertexCoordinates(points);
			psf.setVertexAttribute(Attribute.RELATIVE_RADII, StorageModel.DOUBLE_ARRAY.createReadOnly(rad));
			psf.update();
			level1[2].setGeometry(psf.getPointSet());
//			IndexedLineSetFactory psf = new IndexedLineSetFactory();
//			psf.setMetric(Pn.HYPERBOLIC);
//			psf.setVertexCount(points.length);
//			psf.setVertexCoordinates(points);
//			psf.setVertexAttribute(Attribute.RADII, StorageModel.DOUBLE_ARRAY.createReadOnly(rad));
			SceneGraphComponent level1P = new SceneGraphComponent();
			level1P.addChild(level1[0]);
			level1P.addChild(level1[1]);
			level1P.addChild(level1[2]);
			SceneGraphComponent[] level2 = new SceneGraphComponent[2];
			level2[0] = new SceneGraphComponent();
			level2[0].addChild(level1P);
			level2[1] = new SceneGraphComponent();
			MatrixBuilder.euclidean().scale(-1).assignTo(level2[1]);
			level2[1].addChild(level1P);
			SceneGraphComponent level2P = new SceneGraphComponent();
			level2P.addChild(level2[0]);
			level2P.addChild(level2[1]);
			
			SceneGraphComponent[] level3 = new SceneGraphComponent[3];
			SceneGraphComponent level3P = new SceneGraphComponent();
			Color[] colors = new Color[]{
			new Color(.3f, .5f, 1f),
			new Color(.3f, 1f, .5f),
			new Color(1f, .2f,.2f)};
			for (int i = 0; i<3; ++i)	{
				level3[i] = new SceneGraphComponent();
				MatrixBuilder.euclidean().rotate(2*i*Math.PI/3.0, new double[]{1,1,1}).assignTo(level3[i]);
				Appearance ap = new Appearance();
				ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, colors[i]);
				ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, colors[i]);
				level3[i].setAppearance(ap);
				level3P.addChild(level3[i]);
				level3[i].addChild(level2P);
			}
			root.addChild(level3P);
		return root;
	}
	
	@Override
	public void customize(JMenuBar menuBar, final Viewer viewer) {
		((Component) viewer.getViewingComponent()).addKeyListener( new KeyAdapter()	{
			public void keyPressed(KeyEvent e)	{ 
				switch(e.getKeyCode())	{
					
				case KeyEvent.VK_H:
					System.out.println("	2:  activate/deactivate texture");
					break;
	
				case KeyEvent.VK_2:
					hasTex = !hasTex;
					if (hasTex) theComponent.setAppearance(withTex);
					else theComponent.setAppearance(noTex);
					viewer.render();
					break;
					
			}

			}
		});
	}

	public boolean hasInspector() {return true; }
	public Component getInspector(Viewer viewer) {
		Box container = Box.createVerticalBox();
		final TextSlider aSlider = new TextSlider.Double("texture blendfactor",  SwingConstants.HORIZONTAL, 0.0, 1.0, .5);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				bcolor[3] = (float) aSlider.getValue().doubleValue();
				tex2d.setBlendColor(new Color(bcolor[0], bcolor[1], bcolor[2], bcolor[3]));
			}
		});
		container.add(aSlider);

		final TextSlider bSlider = new TextSlider.Double("weave blendfactor",  SwingConstants.HORIZONTAL, 0.0, 1.0, 0);
		bSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				double x = (float) bSlider.getValue().doubleValue();
//				tex2d = (new RopeTextureFactory()).makeTextureAppearance(withTex, null,u,v, Math.PI/4,
//			    		.7, .12, x, Color.GREEN, Color.RED, Color.BLACK, Color.BLUE, Color.WHITE);
				
			}
		});
		container.add(bSlider);

		container.add(Box.createVerticalGlue());
		return container;
	}

 	public boolean addBackPlane()	{return false;}
 	
	public boolean isEncompass() {
		return true;
	}
 }
