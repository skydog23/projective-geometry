/*
 * Created on Jan 15, 2012
 *
 */
package charlesgunn.jreality.worlds;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.jreality.texture.SimpleTextureFactory.TextureType;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.SceneGraphUtility;

public class AmesRoom extends LoadableScene {

	double[] abc = {0,0,.3};
	double eyeHeight = -.2503;
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	SceneGraphComponent room = SceneGraphUtility.createFullSceneGraphComponent("room");
	SceneGraphComponent eucroom = SceneGraphUtility.createFullSceneGraphComponent("eucroom");
	SceneGraphComponent bothroom = SceneGraphUtility.createFullSceneGraphComponent("bothroom");
	SceneGraphComponent frontwallSGC = SceneGraphUtility.createFullSceneGraphComponent("frontwall");
	SceneGraphComponent floorSGC = SceneGraphUtility.createFullSceneGraphComponent("floor");
	SceneGraphComponent eye = SceneGraphUtility.createFullSceneGraphComponent("eye");
	SceneGraphComponent sphere = SceneGraphUtility.createFullSceneGraphComponent("sphere");
	SceneGraphComponent rays = SceneGraphUtility.createFullSceneGraphComponent("rays");
	SceneGraphComponent cameraSGC = SceneGraphUtility.createFullSceneGraphComponent("camera2");
	SceneGraphComponent platform = SceneGraphUtility.createFullSceneGraphComponent("platform");
	IndexedFaceSetFactory platformFactory = new IndexedFaceSetFactory();
	IndexedFaceSet theAmesRoom, theRoom;
	double[][] origVerts, scaledVerts;
	double pLength = 4,
	pWidth = 3,
	pHeight = .86,
	pAngle = Math.PI/6.0,
	pScale = 1.0,
	platRotAngle = Math.PI/2,
	globalScale = 1.298,
	lengthScale = 1.503,
	heightScale = .7105;		
	int texU = 4, texV = 3, texM= 6;
	boolean showEdgeLengths = false;
	
	@Override
	public SceneGraphComponent makeWorld() {
		frontwallSGC.addChild(frontWallScene()); //setGeometryPrimitives.texturedQuadrilateral());
		floorSGC.setGeometry(Primitives.texturedQuadrilateral());
		floorSGC.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		floorSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		MatrixBuilder.euclidean().translate(0.1,0,0).assignTo(floorSGC);
		SimpleTextureFactory stf = new SimpleTextureFactory();
		stf.setType(TextureType.CHECKERBOARD);
		stf.setColor(0, Color.white);
		stf.setColor(1, Color.black);
		stf.update();
//		Texture2D tex2d = TextureUtility.createTexture(frontwallSGC.getAppearance(), "polygonShader", stf.getImageData(), true);
//		Matrix texm = new Matrix();
//		MatrixBuilder.euclidean().scale(texU, texV,1).assignTo(texm);
//		tex2d.setTextureMatrix(texm);
		Texture2D tex2d = TextureUtility.createTexture(floorSGC.getAppearance(), "polygonShader", stf.getImageData(), true);
		Matrix texm = new Matrix();
		MatrixBuilder.euclidean().scale(texM, texU,1).assignTo(texm);
		tex2d.setTextureMatrix(texm);
		room.addChild(frontwallSGC);
		room.addChild(floorSGC);
		frontwallSGC.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, true);
		room.getAppearance().setAttribute("transparencyEnabled", true);
		room.getAppearance().setAttribute("transparency", .5);
		room.setGeometry(theAmesRoom = Primitives.cube());
		origVerts = theAmesRoom.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null).clone();
		
		eucroom.setGeometry(theRoom = Primitives.cube());
		eucroom.getAppearance().setAttribute("lineShader.diffuseColor", Color.red);
		eucroom.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		eucroom.addChild(rays);
		update();
		updateRays();
		sphere.setGeometry(SphereUtility.tessellatedIcosahedronSphere(3));
		MatrixBuilder.euclidean().scale(.1).assignTo(sphere);
		sphere.getAppearance().setAttribute("polygonShader.diffuseColor", Color.yellow);
		sphere.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		eye.addChildren(sphere, cameraSGC);
		
		world.getAppearance().setAttribute("diffuseColor", Color.blue);
		bothroom.addChildren(eucroom, room);
		world.addChildren( eye, platform,bothroom);
		platformFactory.setVertexCount(6);
		platformFactory.setFaceCount(4);
		platformFactory.setFaceIndices(faceIndices);
		platformFactory.setGenerateFaceNormals(true);
		updatePlatform();
		platform.setGeometry(platformFactory.getIndexedFaceSet());
		platform.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		platform.getAppearance().setAttribute(CommonAttributes.DIFFUSE_COLOR, Color.gray);
		
		bothroom.getAppearance().setAttribute("lineShader.textShader."+CommonAttributes.TEXT_SCALE, .012);
		bothroom.getAppearance().setAttribute("lineShader.textShader."+CommonAttributes.TEXT_OFFSET, new double[]{0,0,.1});
		bothroom.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		
		MatrixBuilder.euclidean().rotateY(Math.PI/2).assignTo(world);
		return world;
	}

	private void updateRays() {
		Matrix scaler = MatrixBuilder.euclidean().scale(1).getMatrix();
		double[][] oVerts = theRoom.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null).clone();
		double[][] bigverts = new double[oVerts.length+1][origVerts[0].length];
		double[] c = new double[]{-1,eyeHeight,0};
		bigverts[8] = c;
		Rn.matrixTimesVector(bigverts, scaler.getArray(), oVerts);
		for (int i = 0; i<8; ++i)	{
			bigverts[i] = Rn.add(null, c, Rn.times(null, 2, Rn.subtract(null, bigverts[i], c)));
		}
		int[][] edges = {{8,0},{8,1},{8,2},{8,3},{8,4},{8,5},{8,6},{8,7}};
		IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
		ilsf.setVertexCount(9);
		ilsf.setVertexCoordinates(bigverts);
		ilsf.setEdgeCount(8);
		ilsf.setEdgeIndices(edges);
		ilsf.update();
		rays.setGeometry(ilsf.getIndexedLineSet());
		rays.getAppearance().setAttribute("lineShader.diffuseColor", Color.black);
		rays.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		rays.getAppearance().setAttribute("lineShader.lineWidth", 3.0);
	}

	double[][] pVerts = new double[6][3];
	int[][] faceIndices = {{0,2,4},{1,3,5},{1,0,4,5},{4,2,3,5}};
	private void update() {
		scaleLHM = MatrixBuilder.euclidean().translate(-1,-1,0).scale(lengthScale,heightScale,1).translate(1,1,0).getArray();
		scaledVerts = Rn.matrixTimesVector(null, scaleLHM, origVerts);
		DataList vertsDL = StorageModel.DOUBLE_ARRAY_ARRAY.createReadOnly(scaledVerts);
		theAmesRoom.setVertexAttributes(Attribute.COORDINATES, vertsDL);
		theRoom.setVertexAttributes(Attribute.COORDINATES, vertsDL);
		
		theHomology = generateHomology(abc[0], abc[1], abc[2],  1, -eyeHeight, 0);
		double[] fooM = theHomology.getArray();

		double[] tmpM = new double[16];
		MatrixBuilder.euclidean().translate(2*lengthScale-1.001,(heightScale-1.0),0).
			rotateY(Math.PI/2.0).scale(2.0, 2*heightScale, 1.0).translate(-.5,-.5,0).assignTo(tmpM);
		tmpM = Rn.times(null, theHomology.getArray(), tmpM);
		new Matrix(tmpM).assignTo(frontwallSGC);
		MatrixBuilder.euclidean().translate(lengthScale-1.0,-.99,0).rotateX(Math.PI/2).scale(2.0*lengthScale, 2.0, 1.0).translate(-.5,-.5,0).assignTo(tmpM);
		tmpM = Rn.times(null, theHomology.getArray(), tmpM);
		new Matrix(tmpM).assignTo(floorSGC);
		
		scaledVerts = Rn.matrixTimesVector(null, fooM, scaledVerts);
		vertsDL = StorageModel.DOUBLE_ARRAY_ARRAY.createReadOnly(scaledVerts);
		theAmesRoom.setVertexAttributes(Attribute.COORDINATES, vertsDL);
		generateHomology(0,0,0, 1, -eyeHeight, 0).assignTo(eucroom);
		MatrixBuilder.euclidean().scale(globalScale).assignTo(bothroom);
		// calculate the distances of the edges and attach as labels
		double[][] verts = scaledVerts;
		int fiberlength = verts[0].length;
		if (fiberlength == 4) Pn.dehomogenize(verts, verts);
		if (showEdgeLengths)	{
			int[][] edges = theRoom.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null);
			double[] d = new double[edges.length];
			String[] labels = new String[edges.length];
			for (int i = 0; i<edges.length; ++i)	{
				double[] v0 = verts[edges[i][0]], v1 = verts[edges[i][1]];
				d[i] = fiberlength == 3 ? Rn.euclideanDistance(v0, v1) : Pn.distanceBetween(v0, v1, Pn.EUCLIDEAN);
				labels[i] = String.format("%4.3g", globalScale*d[i]);
			}
			DataList dl = StorageModel.STRING_ARRAY.createReadOnly(labels);
			theAmesRoom.setEdgeAttributes(Attribute.LABELS, dl);			
		}
		
		System.err.println("scaled verts = \n"+Rn.toString(Rn.times(null, globalScale,scaledVerts)));
		// check the bottom floor
		double[] floor = P3.planeFromPoints(null, scaledVerts[6], scaledVerts[3], scaledVerts[2]);
		Pn.normalizePlane(floor, floor, Pn.EUCLIDEAN);
		System.err.println("floor plane = "+Rn.toString(floor));
		System.err.println("Cos of angle = "+Math.cos(pAngle));
	}

	@Override
	public boolean isEncompass() {
		return true;
	}

	Matrix generateHomology(double a, double b, double c, double x, double y, double z) {
		double[] mat = {1,0,0,0,  0,1,0,0,  0,0,1,0, a,b,c,1};
		Matrix m = new Matrix(mat);
		Matrix t = new Matrix();
		MatrixBuilder.euclidean().translate(x,y,z).assignTo(t);
		return new Matrix(Rn.times(null, mat, t.getArray()));
	}
	
	private void updatePlatform() {
		pAngle = Math.asin(pHeight/pLength);
		double c = Math.cos(pAngle);
		double left = -1, right = left+pWidth;
		pVerts = new double[][]{
				{left, 0, c*pLength},
				{right, 0,  c*pLength},
				{left,0,0},
				{right, 0, 0},
				{left, pHeight, 0},
				{right, pHeight, 0}
		};
		platformFactory.setVertexCoordinates(pVerts);
		platformFactory.update();
		rotatePlatform();
	}

	private void rotatePlatform() {
		MatrixBuilder.euclidean().translate(2, -pHeight-1-eyeHeight,1).rotateY(platRotAngle+Math.PI/2).assignTo(platform);
	}

	Viewer viewer;
	@Override
	public void customize(JMenuBar menuBar, PluginSceneLoader psl) {
		viewer = psl.getViewer();
		// set up second camera path, ending in the moving point on the curve
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", new Color(255,255,70));
		Camera camera = new Camera();
		camera.setNear(.015);
		camera.setFieldOfView(90);
		cameraSGC.setCamera(camera);
		final SceneGraphPath campath2 = SceneGraphUtility.getPathsBetween(
				viewer.getSceneRoot(), 
				cameraSGC).get(0);
		campath2.push(cameraSGC.getCamera());
		MatrixBuilder.euclidean().rotateY(-Math.PI/2).assignTo(cameraSGC);
		
		Component comp = ((Component) viewer.getViewingComponent());
		final SceneGraphPath campath = viewer.getCameraPath();
		comp.addKeyListener(new KeyAdapter() {
 				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_H:
						System.err.println("	1: toggle camera path");
						break;
		
					case KeyEvent.VK_1:
						final SceneGraphPath cp = viewer.getCameraPath();
						Scene.executeWriter(viewer.getSceneRoot(), new Runnable() {

							public void run() {
								boolean alternative = cp == campath;
								viewer.setCameraPath(alternative ? campath2 : campath);
								rays.setVisible(!alternative);
								sphere.setVisible(!alternative);
							}
							
						});
						break;
//					case KeyEvent.VK_2:  // print lengths
//						printFloorPerimeterMarks();
//						break;
					case KeyEvent.VK_2:  // print lengths
						rays.setVisible(!rays.isVisible());
						break;
					}
 				}
		});

	}


	double[] x = {0,.20, .66, 1.0}, y = {-1, -.84, -.25, .16, .53, .84, 1};
	double width = .2;
	Color border = Color.blue,
		window = new Color(100,80,60);

	protected SceneGraphComponent frontWallScene()	{
		SceneGraphComponent fws = SceneGraphUtility.createFullSceneGraphComponent();
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(16);
		ifsf.setFaceCount(4);
		frontWallVerts = new double[][]{
				{-x[3], y[6], 0},
				{-x[3], y[5], 0},
				{x[3], y[5], 0},
				{x[3], y[6], 0},
				{-x[1],y[4], 0},
				{-x[1], y[2], 0},
				{-x[2], y[2], 0},
				{-x[2], y[4], 0},
				{x[1],y[4], 0},
				{x[1], y[2], 0},
				{x[2], y[2], 0},
				{x[2], y[4], 0},
				{-x[3], y[1], 0},
				{-x[3], y[0], 0},
				{x[3], y[0], 0},
				{x[3], y[1], 0},
		};
		ifsf.setVertexCoordinates(frontWallVerts);
		ifsf.setFaceIndices(new int[][] {
				{0,1,2,3},
				{4,5,6,7},
				{8,9,10,11},
				{12,13,14,15}
		});
		ifsf.setFaceColors(new Color[]{border, Color.red, window,border});
		ifsf.update();
		fws.setGeometry(ifsf.getIndexedFaceSet());
		MatrixBuilder.euclidean().scale(.5).translate(1,1,0).assignTo(fws);
		return fws;
	}
	protected void printFloorPerimeterMarks() {
		int[] foobie = {6,2,2,3,7,3,7,6};
		int[] refinery = {texM, texU, texM, texU};
		totalM = Rn.times(null, 
				P3.makeStretchMatrix(null, globalScale),
				Rn.times(null, theHomology.getArray(), scaleLHM));
		int fiberlength = scaledVerts[0].length;
		for (int i = 0; i<4; ++i)	{
			System.err.println("Segment "+foobie[2*i]+"-"+foobie[2*i+1]);
			double[][] verts = refine(origVerts[foobie[2*i]], origVerts[foobie[2*i+1]], 2* refinery[i]+1);
			double[][] tverts = Rn.matrixTimesVector(null, totalM, verts);
			for (int j = 1; j<verts.length; ++j)	{
				double d =  fiberlength == 3 ? Rn.euclideanDistance(tverts[0], 
						tverts[j]) : Pn.distanceBetween(tverts[0], tverts[j], Pn.EUCLIDEAN);
				System.err.println("Dist "+i+" "+String.format("%4.3g", d));
			}
		}
		// handle the front wall scene also
		if (abc[2] == 0) return;		
		double finitePart = 1.0/abc[2];
		double rangle = Math.atan2(2*lengthScale, finitePart);
		double[] rot = P3.makeRotationMatrixY(null, rangle);
		double[] tlate = P3.makeTranslationMatrix(null, new double[]{0,2*lengthScale,finitePart},Pn.EUCLIDEAN);
		double[] rrot = Rn.times(null, P3.makeStretchMatrix(null, globalScale), Rn.times(null, Rn.conjugateByMatrix(null, rot, tlate), 
				frontwallSGC.getTransformation().getMatrix()));
		double[][] ttverts = Rn.matrixTimesVector(null, Rn.times(null, rrot, MatrixBuilder.euclidean().scale(.5).translate(1,1,0).getArray()), frontWallVerts);
		SceneGraphComponent sgc = new SceneGraphComponent();
		sgc.addChild(frontWallScene());
		new Matrix(rrot).assignTo(sgc);
		world.addChild(sgc);
		System.err.println("front wall verts rotated\n"+Rn.toString(ttverts));
	}
	
	public static double[][] refine(double[] p0, double[] p1, int n)	{
		int veclength = p0.length;
		double[][] newVerts = new double[n][veclength];
			for (int j = 0; j<n; ++j)	{
				double t = (j)/(n-1.0);
				double s = 1.0 - t;
				newVerts[j] = Rn.linearCombination(null, s, p0, t, p1);
		}
		return newVerts;
	}
	@Override
	public boolean hasInspector() {
		return true;
	}

	String[] labels = {"a", "b", "c"};
	private Matrix theHomology;
	private double[] scaleLHM;
	private double[][] frontWallVerts;
	private double[] totalM;
	public Component getInspector(Viewer v) {
		Box container = Box.createVerticalBox();
		Box plane = Box.createVerticalBox();
		container.add(plane);
		for (int i = 0; i<3; ++i)	{
			final int j = i;
			final TextSlider cSlider = new TextSlider.Double(labels[j],  SwingConstants.HORIZONTAL, -1, 1, abc[j]);
			cSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					abc[j] = cSlider.getValue().doubleValue();
					update();
				}
			});
			container.add(cSlider);
		}
		final TextSlider eSlider = new TextSlider.Double("eye height",  SwingConstants.HORIZONTAL, -1, 1, eyeHeight);
		eSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				eyeHeight = eSlider.getValue().doubleValue();
				update();
				updatePlatform();
			}
		});
		container.add(eSlider);
		final TextSlider pSlider = new TextSlider.Double("rotate platform",  SwingConstants.HORIZONTAL, -Math.PI/2, Math.PI/2, platRotAngle);
		pSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				platRotAngle = pSlider.getValue().doubleValue();
				rotatePlatform();
			}
		});
		container.add(pSlider);
		final TextSlider sSlider = new TextSlider.Double("room scale",  SwingConstants.HORIZONTAL, 1,3,globalScale);
		sSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				globalScale = sSlider.getValue().doubleValue();
				update();
			}
		});
		container.add(sSlider);
		final TextSlider lSlider = new TextSlider.Double("room length scale",  SwingConstants.HORIZONTAL, 1,3,lengthScale);
		lSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				lengthScale = lSlider.getValue().doubleValue();
				update();
			}
		});
		container.add(lSlider);
		final TextSlider hSlider = new TextSlider.Double("room height scale",  SwingConstants.HORIZONTAL, .3,3,heightScale);
		hSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				heightScale = hSlider.getValue().doubleValue();
				update();
			}
		});
		container.add(hSlider);
		return container;
	}

}
