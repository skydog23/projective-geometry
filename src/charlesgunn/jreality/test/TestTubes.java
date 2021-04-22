 /*
 * Created on May 12, 2004
 *
 */
package charlesgunn.jreality.test;
import java.awt.Color;
import java.util.List;

import javax.swing.JMenuBar;

import charlesgunn.jreality.LevelOfDetailComponent;
import charlesgunn.jreality.LevelOfDetailComponentFactory;
import charlesgunn.jreality.geometry.BezierPatchMeshTubeFactory;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.BezierPatchMesh;
import de.jreality.geometry.FrameFieldType;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetUtility;
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
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

/**
 * @author Charles Gunn
 *
 */
public class TestTubes extends LoadableScene {

	static double x = 1;
	static double[][] circle =  
		{{x, 0, 0, 1},	 
		{x, 1,0, 1}, 
		{0,2, 0, 2}, 
		{-x, 1,0, 1}, 
		{-x, 0, 0, 1},
		{-x, -1,0, 1},
		{0,-2, 0, 2},
		{x, -1,0, 1},
		{x, 0, 0, 1}};
	
	static double octant[][][] = {
			{{0, 0, 1, 1},		{0, 1, 1, 1},		{0, 2, 0, 2}}, 
			{{1, 0, 1, 1},		{1, 1, 1, 1},		{2, 2, 0, 2}}, 
			{{2, 0, 0, 2},		{2 ,0, 0, 2},		{4, 0, 0, 4}}};

	static double[][] form = {{1,1,1}, {1,-1,1},{1,-1,-1},{1,1,-1},{-1,1,-1},{-1,-1,-1},{-1,-1,1},{-1,1,1}};
	static double[][] form2 = {{1,1,1}, {1,0,1}, {1,-1,1},{1,-1,0},{1,-1,-1},{1,0,-1},{1,1,-1},{0,1,-1},
			{-1,1,-1},{-1,0,-1},{-1,-1,-1},{-1,-1,0},{-1,-1,1},{-1,0,1},{-1,1,1}};

	static double[][] otherDirection	= {{1,0,0,0},{0,0,1,0},{0,0,2,0},{1,0,3,0}};
	
	static double[][][] patch;
	static double[] lodLevels = {.1,.2, .4, .8};
	static 
	{
		patch = new double[circle.length][otherDirection.length][4];
		for (int i = 0; i<circle.length; ++i)	{
			for (int j = 0; j<otherDirection.length; ++j)	{
				for (int k = 0; k<4; ++k)	
					patch[i][j][k] = circle[i][k] + otherDirection[j][k];
			}
		}
	}
	LevelOfDetailComponent lodc;
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent root = new SceneGraphComponent();
		root.setTransformation(new Transformation());
		root.setAppearance(new Appearance());
		root.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);

		double[][] rod = new double[8][3];
		for (int i = 0; i<8; ++i)	{
			rod[i][0] = .1*i;
			rod[i][1] = rod[i][2] = 0.0;
		}
		boolean doIco = true, doKnot = true, doBez = true, doBox = true, doHyp = false;
		
//		if (doHyp)	{
//			TubeUtility.makeTubeAsIFS(rod, .04,null,TubeUtility.FRENET,false, Pn.HYPERBOLIC, 0);
//		}
		
//		double[][] square = {{1,1,0},{-1,1,0},{-1,-1,0},{1,-1,0}};		
//		double[][] profile = {{0,0,0}, {0,.1,0},{1,.1,0},{1,.2,0},{1.4,0,0}};
//		double[][] profile2 = {{1,.2,0}, {.2, .2,0}, {0,.4,0}, {-.2, .2, 0},{-1,.2,0}, {-1,-.2,0},{-.2, -.2,0}, {0,-.4,0}, {.2, -.2, 0},{1,-.2,0}};
//		  IndexedFaceSet arrow = Primitives.surfaceOfRevolutionAsIFS(profile, 24, Math.PI * 2);
	   //torus1.addGeometryListener(torus1);
	   //pts = square;
//	   double[][] pts = form2;
//	   
	   //QuadMeshShape torust = TubeUtility.makeTubeAsIFS(tpts, .2,  null, TubeUtility.PARALLEL, false);
	   //GeometryUtility.calculateAndSetNormals(torust);
	   if (doKnot)	{
		   SceneGraphComponent torussgc = SceneGraphUtility.createFullSceneGraphComponent("torus knot");
		   torussgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, 
		   		new Color(120,0,  120));
		   //torussgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		   torussgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.SMOOTH_SHADING, true);
		   torussgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .06);
		   //torussgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING, false);
		   IndexedLineSet torus1 = Primitives.discreteTorusKnot(1,.25, 2, 9, 250);//		   double[][] verts = new double[250][3];
		   int size = 16;
		   double scale = 1;
		   double[][] mysection = new double[size][3];
		   for (int i = 0; i<size; ++i)	{
		   		double angle = (i/(size-1.0)) * Math.PI * 2;
		   		mysection[i][0] = scale * Math.cos(angle)  *(1.5+Math.cos(4*angle));
		   		mysection[i][1] = scale *  Math.sin(angle)  *(1.5+Math.cos(4*angle));
		   		mysection[i][2] = 0.0;
		   }
		   IndexedLineSet ils = torus1;
		   colorByAngle(ils, new double[] {1,0,0}, new double[] {0,1,0});
//		   double[][] tpts = torus1.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		   //QuadMeshShape torus1Tubes = TubeUtility.makeTubeAsIFS(tpts, .04, null, TubeUtility.PARALLEL, true, Pn.EUCLIDEAN);

//		   IndexedFaceSet torus1Tubes = TubeUtility.makeTubeAsIFS(torus1, 0, true, .04, mysection, TubeUtility.PARALLEL, true, Pn.EUCLIDEAN, 6);
//		   GeometryUtility.calculateAndSetNormals(torus1Tubes);
		   PolygonalTubeFactory ptf = new PolygonalTubeFactory(ils, 0);
		   ptf.setClosed(true);
		   ptf.setVertexColorsEnabled(true);
		   ptf.setRadius(.04);
		   ptf.setCrossSection(mysection);
		   ptf.setTwists(6);
		   double[][] vcolors = ils.getVertexAttributes(Attribute.COLORS).toDoubleArrayArray(null);
		   ptf.setVertexColors(vcolors);
		   ptf.update();
		   IndexedFaceSet torus1Tubes = ptf.getTube();
   			IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(torus1Tubes);
		   //IndexedFaceSetUtility.simpleTriangulate(torus1Tubes);
		   torussgc.setGeometry(torus1Tubes); //ils);
		   torussgc.getTransformation().setMatrix(P3.makeStretchMatrix(null, .9));
		   root.addChild(torussgc);	   	
	   }
	   
	   SceneGraphComponent globeNode= SceneGraphUtility.createFullSceneGraphComponent("container");
	   if (doBox)	{
		   SceneGraphComponent globeNode2= SceneGraphUtility.createFullSceneGraphComponent("curve");
		   Appearance ap1 = globeNode2.getAppearance();
		   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,new Color(240, 100, 0));
		   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .03);
		   ap1.setAttribute(CommonAttributes.FACE_DRAW,false);
		   ap1.setAttribute(CommonAttributes.EDGE_DRAW,true);
		   ap1.setAttribute(CommonAttributes.VERTEX_DRAW,true);
		   ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW,true);
		   ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS,.06);
		   ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_SIZE, 3.0);
		   ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.RED);
		   //QuadMeshShape qms = TubeUtility.makeTubeAsIFS(form, .04, null, TubeUtility.PARALLEL, true, Pn.EUCLIDEAN);
		   //GeometryUtility.calculateAndSetNormals(qms);	   	
		   IndexedLineSet croxl = IndexedLineSetUtility.createCurveFromPoints(form, true);
		   globeNode2.setGeometry(croxl);
		   globeNode.addChild(globeNode2);
	   }
	   
	   //SceneGraphComponent globeNode4= SceneGraphUtility.createFullSceneGraphComponent("patch");
	   SceneGraphComponent globeNode4 = SceneGraphUtility.createFullSceneGraphComponent("a node ");
	   globeNode4.setTransformation(new Transformation());
	   Appearance ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.BLUE);
	   ap1.setAttribute(CommonAttributes.SPECULAR_EXPONENT, 100.0);
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.BLACK);
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH,1.0);
	   ap1.setAttribute(CommonAttributes.POINT_RADIUS,3.0);
	   globeNode4.setAppearance(ap1);
	   
	   if (doBez)	{
//		   	double[][][] tubePoints = TubeUtility.makeTubeAsBezierPatchMesh(form, .2, circle, TubeUtility.PARALLEL,true, Pn.EUCLIDEAN);
//		   	BezierPatchMesh bpm = new BezierPatchMesh(2, 3, tubePoints);
		   BezierPatchMeshTubeFactory bpmtf = new BezierPatchMeshTubeFactory(form);
		   bpmtf.setFrameFieldType(FrameFieldType.PARALLEL);
		   bpmtf.setRadius(.2);
		   bpmtf.setCrossSection(circle);
		   bpmtf.setClosed(true);
		   bpmtf.update();
		   BezierPatchMesh bpm = bpmtf.getTube();
		   double[][][] tubePoints = bpm.getControlPoints();
		   lodc = new LevelOfDetailComponent(lodLevels);
	   		for (int i = 0; i<4; ++i)	{ 
	   			bpm = new BezierPatchMesh(2, 3, tubePoints);
		   		for (int j = 1; j<= i; ++j)	bpm.refine();
		   		IndexedFaceSet qmpatch = BezierPatchMesh.representBezierPatchMeshAsQuadMesh(bpm);	 
//		   		GeometryUtility.calculateAndSetTextureCoordinates(qmpatch);
		   		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("selection child "+i);
		   		sgc.setAppearance(makeTextureAppearance(5d,35d));
		 	    sgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.WHITE);
		   		sgc.setGeometry(qmpatch);	   
		   		lodc.addChild(sgc);
	   		}
	   		globeNode4.addChild(lodc);
	   }

	   if (doIco)	{
//		   SceneGraphComponent tubie = TubeUtility.ballAndStick(Primitives.sharedIcosahedron, .10, .05, java.awt.Color.YELLOW, java.awt.Color.GREEN, Pn.EUCLIDEAN); //TubeUtility.createTubesOnEdges(Primitives.sharedIcosahedron, .05); //TubeUtility.makeTubeAsIFS(p1, p2, .3, null);
		   BallAndStickFactory basf = new BallAndStickFactory(Primitives.sharedIcosahedron);
		   basf.setBallRadius(.04);
		   basf.setStickRadius(.02);
		   basf.setShowArrows(true);
		   basf.setArrowScale(.1);
		   basf.setArrowSlope(1.5);
		   basf.setArrowPosition(.9);
		   basf.update();
		   SceneGraphComponent tubedIcosa = basf.getSceneGraphComponent();
		   tubedIcosa.setTransformation(new Transformation());
		   tubedIcosa.getTransformation().setMatrix(P3.makeStretchMatrix(null, .5));
		   tubedIcosa.setAppearance(new Appearance());
		   globeNode4.addChild(tubedIcosa);	   	
	   }
		//ReflectionMap refm = ReflectionMap.reflectionMapFactory("/homes/geometer/gunn/Pictures/textures/desertstorm/desertstorm_", texNameSuffixes, "JPG");
		//root.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+"reflectionMap", refm);
		root.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,false);

	   root.addChild(globeNode);
	   globeNode.addChild(globeNode4);
	  return root;
	}

	public int getMetric() {
		return Pn.EUCLIDEAN;
	}

	public boolean addBackPlane() {
		return true;
	}
	public boolean isEncompass() {
		return true;
	}

	public static void colorByAngle(IndexedLineSet ils, double[] color1, double[] color2)	{
		int nPts = ils.getNumPoints();
		double[][] colors = new double[nPts][3];
		double[][] vertices = ils.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		for (int i = 1; i<nPts-1; ++i)	{
			double[] v1 = Rn.subtract(null, vertices[i], vertices[i-1]);
			//System.out.println("Angle "+i+" is "+angle);
			//double t = Math.abs(angle/Math.PI);
			double t = 10 * Math.sqrt(Math.abs( v1[0]*v1[0] + v1[1] * v1[1]));
			t = t  - ((int) t);
			Rn.linearCombination(colors[i],t, color1, 1-t, color2);
		}
		System.arraycopy(colors[1], 0, colors[0], 0, 3);
		System.arraycopy(colors[nPts-2], 0, colors[nPts-1], 0, 3);
		ils.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(colors));
	}

	int size = 256;
	int margin = size/16;
	int bandwidth = size/4;
	int gapwidth =size/4;
	int shwd = size/64;
	int onewidth = size/2;
	int iband, jband, imod, jmod;
	int which;
	byte[] im = new byte[size*size* 4];
	byte[][] colors = {{(byte)0x0,(byte)0x0,(byte)0x0,(byte)0x0},
	{(byte)200,(byte)200,(byte)200,(byte)0xff},
	{(byte)255,(byte)255,(byte)255,(byte)255},
	{(byte)0,(byte)0, (byte) 0, (byte) 255}};
	public Appearance makeTextureAppearance(double n, double m) 	{
	    for (int i = 0; i<size; ++i)	{
	        iband = i/onewidth;
	        imod = i%onewidth;
	        for (int j = 0; j< size; ++j)	{
				int where = 4*(i*size+j);
				jband = j /onewidth;
				jmod = j%onewidth;
				int q = 2*(iband)+jband;
				if (imod > bandwidth && jmod > bandwidth) which = 0;
				else {
				    if (imod <= bandwidth && jmod <= bandwidth)	{
				        if (q == 0 || q == 3) which = 1;
				        else which = 2;
				    } else if (jmod > bandwidth) {
				        which = 1;
				        if ((q == 0 || q == 3)&& jmod > (onewidth - shwd)) which = 3;
				        if ((q == 1 || q == 2) && jmod < (bandwidth + shwd)) which = 3;
				    } else if (imod > bandwidth) {
			 	        which = 2;
				        if ((q == 1 || q == 2)&& imod > (onewidth - shwd)) which = 3;
				        if ((q == 0 || q ==3) && imod < (bandwidth + shwd)) which = 3;
				    }
				}
				System.arraycopy(colors[which],0,im,where,4);
			}
	    }
	    ImageData it = new ImageData(im,size,size);
	    Appearance ap =new Appearance();
	    Texture2D tex2d = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, "polygonShader.texture2d", ap, true);	
	    Matrix mat = new Matrix();
	    MatrixBuilder.euclidean().rotateZ(Math.PI/4.0).scale(n,m,1.0).assignTo(mat);
	    tex2d.setTextureMatrix(mat);
        tex2d.setImage(it);
        tex2d.setRepeatS(Texture2D.GL_REPEAT);
        tex2d.setRepeatT(Texture2D.GL_REPEAT);
        return ap;
     }

	public void customize(JMenuBar menuBar, PluginSceneLoader psl) {
		Viewer viewer = psl.getViewer();
		super.customize(menuBar, viewer);
		if (lodc == null) return;

		List l = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), lodc);
		SceneGraphPath world = (SceneGraphPath) l.get(0);
  		Graphics3D g = new Graphics3D(viewer.getCameraPath(), world, CameraUtility.getAspectRatio(viewer));
   		LevelOfDetailComponentFactory lodf = LevelOfDetailComponentFactory.getLevelOfDetailComponentFactory(lodc);
   		lodf.setContext(g);
   		lodf.setLevelOfDetail(1.0);
   		lodf.setUpdateInterval(100);
   		lodf.activate();
   		viewer.render();
	}


}
