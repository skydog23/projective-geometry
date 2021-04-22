/*
 * Created on May 12, 2004
 *
 */
package charlesgunn.jreality.worlds;
import java.awt.Color;
import java.io.IOException;

import javax.swing.JMenuBar;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.examples.CatenoidHelicoid;
import de.jreality.geometry.FrameFieldType;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.PolygonalTubeFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.CameraUtility;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;

/**
 * @author Charles Gunn
 *
 */
public class JOGLSkyBox extends LoadableScene {

	static double[][] square = {{-1,-1,0},{1,-1,0},{1,1,0},{-1,1,0}};
	static double[][] texc = {{0,0},{1,0},{1,1},{0,1}};

	public boolean encompass()	{ return false; }
	
	public boolean addBackPlane()	{ return false; }
  CubeMap rm = null;
  public SceneGraphComponent makeWorld() {
    SceneGraphComponent root = makeScene();
//    //CubeMap rm = CubeMap.CubeMapFactory(
//        "textures/desertstorm/desertstorm_",
//        new String[]{"rt","lf","up", "dn","bk","ft"},
//        "JPG");
//    SkyBox sb = new SkyBox(rm.getFaceTextures());
     return root;

  }
  
	public SceneGraphComponent makeScene() {

		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("theWorld");
		root.setTransformation(new Transformation());	
		double[][] pos = new double[6][3];
		for (int i = 0; i<6; ++i)	{
			double angle = i*Math.PI * 2.0/(6.0);
			pos[i][0] = 4 * Math.cos(angle);
			pos[i][1] = 0.0;
			pos[i][2] = 4 * Math.sin(angle);
		}
		
		CatenoidHelicoid globeSet=new CatenoidHelicoid(40);
		IndexedFaceSetUtility.calculateAndSetFaceNormals(globeSet);
		globeSet.setName("ReflectingHelicoid");
		globeSet.setAlpha(Math.PI/4);
		SceneGraphComponent globeNode1= new SceneGraphComponent();
		globeNode1.setName("Comp1");
		MatrixBuilder.euclidean().translate(pos[0]).scale(.3).assignTo(globeNode1);
	   Appearance ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.DIFFUSE_COLOR, java.awt.Color.RED);
	   //ap1.setAttribute(CommonAttributes.SMOOTH_SHADING,false);
	   ap1.setAttribute(CommonAttributes.EDGE_DRAW,false);
	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
//	   polygonShader.setDiffuseColor( Color.WHITE);
//	   polygonShader.setSmoothShading(false);
	   double[] vec = {1d, 1.5d, 1d};
	   Texture2D tex2d = null;
	   tex2d = (Texture2D) AttributeEntityUtility
	       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d", ap1, true);		
	  		try {
	  			ImageData id = ImageData.load(Input.getInput("http://www.math.tu-berlin.de/~gunn/Pictures/textures/grid256rgba.png")); //weaveRGBABright.png"));
	  			tex2d.setImage(id);
	  			rm = TextureUtility.createReflectionMap(
                  ap1,
                  "polygonShader",
//                  "/Users/gunn/Pictures/grabs/cubeMapTest_", //"textures/desertstorm/desertstorm_",
//		          new String[]{"rt","lf","up", "dn","bk","ft"},
//		          ".png");                 
	  			"http://www.math.tu-berlin.de/~gunn/Pictures/textures/desertstorm/desertstorm_",
                  new String[]{"rt","lf","up", "dn","bk","ft"},
                  ".JPG");
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	   tex2d.setTextureMatrix( new Matrix(P3.makeStretchMatrix(null, vec)));
	   tex2d.setBlendColor(new Color(0f,0f,0f,.5f));
	   ap1.setAttribute(CommonAttributes.RMAN_REFLECTIONMAP_FILE,"desertstorm-refl.tex");
	   globeNode1.setAppearance(ap1);
	   globeNode1.setGeometry(globeSet);
  
  
		// 2.
		CatenoidHelicoid catHel = new CatenoidHelicoid(20);
		IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(catHel);
		catHel.setAlpha(Math.PI / 2);
		catHel.setName("Catenoid");
		IndexedFaceSetUtility.calculateAndSetFaceNormals(globeSet);
		SceneGraphComponent globeNode2 = new SceneGraphComponent();
		globeNode2.setName("Comp1");
		MatrixBuilder.euclidean().translate(pos[1]).scale(.3).assignTo(
				globeNode2);
		globeNode2.setGeometry(catHel);
		ap1 = new Appearance();
		ap1.setAttribute(CommonAttributes.LINE_SHADER + "."
				+ CommonAttributes.DIFFUSE_COLOR, new Color(255, 0, 255));
		ap1.setAttribute(CommonAttributes.LINE_WIDTH, 2.0);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER + "."
				+ CommonAttributes.DIFFUSE_COLOR, new java.awt.Color(.2f, .5f,
				.5f, 1f));
		ap1.setAttribute(CommonAttributes.FACE_DRAW, true);
		ap1.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		ap1.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap1.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		ap1.setAttribute(CommonAttributes.TRANSPARENCY, 0.5d);
		ap1.setAttribute(CommonAttributes.SMOOTH_SHADING, true);
		globeNode2.setAppearance(ap1);
  
 
	   	catHel = new CatenoidHelicoid(20);
		catHel.setName("Helicoid");
		IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(catHel);
		IndexedFaceSetUtility.calculateAndSetFaceNormals(globeSet);
		SceneGraphComponent globeNode3 = new SceneGraphComponent();
		MatrixBuilder.euclidean().translate(pos[2]).scale(.3).assignTo(globeNode3);
		globeNode3.setGeometry(catHel);
		double[][] verts = catHel.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		double[] mat = MatrixBuilder.euclidean().scale(.5).translate(.5,.5,.5).getMatrix().getArray();
		double[][] foo = Rn.matrixTimesVector(null, mat, verts);
		catHel.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(foo));
		ap1 = new Appearance();
		ap1.setAttribute(CommonAttributes.LINE_SHADER + "."
				+ CommonAttributes.DIFFUSE_COLOR, new Color(200, 150, 0));
		ap1.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap1.setAttribute(CommonAttributes.LINE_SHADER + "."
				+ CommonAttributes.TUBES_DRAW, true);
		ap1.setAttribute(CommonAttributes.POINT_SHADER + "."
				+ CommonAttributes.DIFFUSE_COLOR, Color.WHITE); //new java.awt.Color(.2f, .9f,.2f, 1f));
		ap1.setAttribute(CommonAttributes.POINT_SHADER + "."+CommonAttributes.SPHERES_DRAW,false);
		// ap1.setAttribute(CommonAttributes.LINE_STIPPLE,true);
		// ap1.setAttribute(CommonAttributes.LINE_STIPPLE_PATTERN,0x1c47);
		// ap1.setAttribute(CommonAttributes.ANTIALIASING_ENABLED,true);
		ap1.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap1.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, .07);
		ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_SIZE, 10.0);
		globeNode3.setAppearance(ap1);

		IndexedLineSet torus1 = Primitives.discreteTorusKnot(1.0,.5, 3,4, 400);
		double[][] pts = torus1.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		//IndexedFaceSet tube = TubeUtility.makeTubeAsIFS(pts, .07d, null,TubeUtility.PARALLEL, true, Pn.EUCLIDEAN, 0);
		PolygonalTubeFactory ptf = new PolygonalTubeFactory(pts);
		ptf.setRadius(.07);
		ptf.setFrameFieldType(FrameFieldType.PARALLEL);
		ptf.setClosed(true);
		ptf.setMetric(Pn.EUCLIDEAN);
		ptf.update();
		IndexedFaceSet tube = ptf.getTube();
		SceneGraphComponent globeNode4 = new SceneGraphComponent();
		MatrixBuilder.euclidean().translate(pos[3]).rotateY(Math.PI).assignTo(globeNode4);
		globeNode4.setGeometry(tube);
		ap1 = new Appearance();
		try {
			CubeMap rm2 = TextureUtility.createReflectionMap(ap1, "polygonShader",
					TextureUtility.getCubeMapImages(rm));
			rm2.setBlendColor(new java.awt.Color(1f, 1f, 1f, .6f));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER + "."
				+ CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLUE);
		ap1.setAttribute(CommonAttributes.FACE_DRAW, true);
		ap1.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap1.setAttribute(CommonAttributes.SMOOTH_SHADING,true);
		ap1.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap1.setAttribute(CommonAttributes.RMAN_REFLECTIONMAP_FILE,"desertstorm-refl.tex");

		globeNode4.setAppearance(ap1);
 
	   	IndexedFaceSet torus = Primitives.torus(2.3, 1.5, 40, 60);
		SceneGraphComponent globeNode5 = new SceneGraphComponent();
		globeNode5.setName("TorusWithCubeMap");
		MatrixBuilder.euclidean().translate(pos[4]).scale(.3).assignTo(globeNode5);
		globeNode5.setGeometry(torus); // SphereHelper.spheres[4]); //torus);

		ap1 = new Appearance();
		try {
			CubeMap rm3 = TextureUtility.createReflectionMap(ap1, "polygonShader",
					TextureUtility.getCubeMapImages(rm));
			rm3.setBlendColor(new Color(1f, 1f, 1f, .6f));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER + "."
				+ CommonAttributes.DIFFUSE_COLOR, java.awt.Color.YELLOW);
		ap1.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap1.setAttribute(CommonAttributes.RMAN_REFLECTIONMAP_FILE,"desertstorm-refl.tex");
		globeNode5.setAppearance(ap1);

		torus = Primitives.torus(2.3, 1.5, 20, 20);
		SceneGraphComponent globeNode6 = new SceneGraphComponent();
		MatrixBuilder.euclidean().translate(pos[5]).rotate(Math.PI / 2.0, 1.0,0.0, 0.0).scale(0.3).assignTo(globeNode6);
		globeNode6.setGeometry(IndexedFaceSetUtility.implode(torus, -.65));

		sbkit = SceneGraphUtility.createFullSceneGraphComponent("skybox");
		ap1 = sbkit.getAppearance();
		ap1.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		ap1.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER + "."
				+ CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
		sides = TextureUtility.getCubeMapImages(rm);
//		sb = new SkyBox(sides);
//		sb.getTransformation().setMatrix(P3.makeStretchMatrix(null, 100.0));
		root.addChild(globeNode1);
		root.addChild(globeNode2);
		root.addChild(globeNode3);
		root.addChild(globeNode4);
		root.addChild(globeNode5);
		root.addChild(globeNode6);	

		
		return root;
	}
	
	public boolean isEncompass() {return false;}
 
	SceneGraphComponent sbkit;
	private ImageData[] sides;
	public void customize(JMenuBar menuBar, Viewer viewer) {
		//sb.getTransformation().setTranslation(0.0d, 0.0d, -4.0d);
		CameraUtility.getCamera(viewer).setFar(2000.0);
		TextureUtility.createSkyBox(viewer.getSceneRoot().getAppearance(), sides);
		//viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.SKY_BOX, rm);
	}
}

