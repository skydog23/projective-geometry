package charlesgunn.jreality.worlds;
/*
 * Created on Nov 11, 2004
 *
 */


import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;

import charlesgunn.jreality.SelectionComponent;
import charlesgunn.jreality.geometry.projective.NullPlaneFactory;
import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.jreality.texture.SimpleTextureFactory.TextureType;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.tools.DraggingTool;
import de.jreality.util.SceneGraphUtility;

/**
 * @author gunn
 *
 */
public class PerspectiveDemo extends Assignment {
		SceneGraphComponent worldSGC, 
			topWorldSGC,
			tableSGC, 
			vertSGC,
			screenSGC, 
			centerSGC,
			parallelPlaneSGC,
			vanishingLineSGC,
			geomSGC,
			vGeomSGC,
			edgeSGC,
			hEdgeSGC, 
			vEdgeSGC, 
			screenEdgeSGC, 
			pointSGC,
			hPointSGC,
			clippingPlanesSGC;
		SceneGraphComponent[] clipSGC = new SceneGraphComponent[4];
		SelectionComponent hGeomSGC;
		IndexedFaceSetFactory tableFac,
			screenFac;
		double eyeHeight = 2.0,
			tableWidth = 4.0,
			tableLength = 6.5,
			tableThickness = .5,
			screenHeight = 2.25,
			distToScreen = 2.0,
			defaultRadius = .01;
		boolean shadowCasting = true,
			clipping = false,
			rotating;
		Timer rotate, dirty;
		double dangle = .0075, angle = 0.0;
		private static double[] verts = {-1,-1,0,  1,-1,0, 1,1,0,  -1,1,0};
		
		public SceneGraphComponent getContent()	{
			
			topWorldSGC = SceneGraphUtility.createFullSceneGraphComponent("topWorld");
			worldSGC = SceneGraphUtility.createFullSceneGraphComponent("theWorld");
			Appearance ap  = worldSGC.getAppearance();
//			ap.setAttribute(CommonAttributes.RADII_WORLD_COORDINATES, true);
			ap.setAttribute(CommonAttributes.POINT_RADIUS, defaultRadius);
			ap.setAttribute(CommonAttributes.TUBE_RADIUS, defaultRadius);
			ap.setAttribute("polygonShader.diffuseColor", Color.white);
			ap.setAttribute("ambientCoefficient",.2);
			ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.black);
			ap.setAttribute("pointShader.polygonShader.diffuseColor", Color.black);
			ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
			
			tableSGC = SceneGraphUtility.createFullSceneGraphComponent("table");
			tableSGC.setGeometry(Primitives.box(1,1,1,false));
			tableSGC.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
			tableSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
			tableSGC.getAppearance().setAttribute("polygonShader.diffuseColor", new Color(255, 225, 225));
			
			vertSGC = SceneGraphUtility.createFullSceneGraphComponent("vertical");
			screenSGC = SceneGraphUtility.createFullSceneGraphComponent("screen");
			screenFac = Primitives.texturedQuadrilateralFactory(verts);
			screenSGC.setGeometry(screenFac.getGeometry());
			vertSGC.addChild(screenSGC);
			
			centerSGC = SceneGraphUtility.createFullSceneGraphComponent("center");
			ap = centerSGC.getAppearance();
			ap.setAttribute("ambientCoefficient",.05);
			parallelPlaneSGC = SceneGraphUtility.createFullSceneGraphComponent("parallel plane");
			vanishingLineSGC = SceneGraphUtility.createFullSceneGraphComponent("vanishing line");
			geomSGC = SceneGraphUtility.createFullSceneGraphComponent("geom");
			//hGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("horizontal geom");
			hGeomSGC = new SelectionComponent();
			hGeomSGC.setName("horizontal geom");
			hGeomSGC.setAppearance(new Appearance());
			vGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("vertical geom");
			edgeSGC = SceneGraphUtility.createFullSceneGraphComponent("lines");
			hEdgeSGC = SceneGraphUtility.createFullSceneGraphComponent("horizontal edges");
			vEdgeSGC = SceneGraphUtility.createFullSceneGraphComponent("vertical edges");
			screenEdgeSGC = SceneGraphUtility.createFullSceneGraphComponent("screen edges");
			pointSGC = SceneGraphUtility.createFullSceneGraphComponent("points");
			hPointSGC = SceneGraphUtility.createFullSceneGraphComponent("horizontal points");
			clippingPlanesSGC = SceneGraphUtility.createFullSceneGraphComponent("clipping planes");
			
			ap  = geomSGC.getAppearance();
			ap.setAttribute("polygonShader.diffuseColor", Color.cyan);

			ap  = screenSGC.getAppearance();
			ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
			ap.setAttribute(CommonAttributes.TRANSPARENCY, .6);
			ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.blue);
			ap.setAttribute("pointShader.polygonShader.diffuseColor", Color.blue);
			
			ap  = parallelPlaneSGC.getAppearance();
			ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
			ap.setAttribute(CommonAttributes.TRANSPARENCY, .8);
			ap.setAttribute("ambientCoefficient",.05);
			ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.red);
			ap.setAttribute("pointShader.polygonShader.diffuseColor", Color.red);
			ap.setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES	,true);
			ap.setAttribute("pointShader.pointRadius", .006);
			ap.setAttribute("lineShader.tubeRadius", .006);
			nullPlaneRepresentation = NullPlaneFactory.nullPlaneRepresentation(20);
			nullPlaneRepresentation.setAppearance(null);
			parallelPlaneSGC.addChild(nullPlaneRepresentation);
			parallelPlaneSGC.addChild(vanishingLineSGC);
			nullPlaneRepresentation.setVisible(false);
			vanishingLineSGC.setVisible(false);
			
			ap = vEdgeSGC.getAppearance();
			ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.pink);
			ap.setAttribute("pointShader.polygonShader.diffuseColor", Color.pink);
			
			ap = vGeomSGC.getAppearance();
			ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
			
			ap = centerSGC.getAppearance();
			ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.black);
			ap.setAttribute("pointShader.polygonShader.diffuseColor", Color.yellow);
			ap.setAttribute("pointShader.pointRadius", .04);
			ap.setAttribute("lineShader.tubeRadius", .02);
			worldSGC.addChildren(tableSGC, centerSGC,
					geomSGC, edgeSGC, pointSGC, vertSGC, parallelPlaneSGC);
			if (clipping) worldSGC.addChild(clippingPlanesSGC);
			//centerSGC.addChild(parallelPlaneSGC);
			geomSGC.addChildren(hGeomSGC, vGeomSGC);
			edgeSGC.addChildren(hEdgeSGC, vEdgeSGC, screenEdgeSGC);
			pointSGC.addChild(hPointSGC);
			
			rotate = new Timer(20, new ActionListener()	{
				final double[] m = P3.makeRotationMatrixZ(null, dangle);
				public void actionPerformed(ActionEvent e) {
					angle += dangle;
					MatrixBuilder.euclidean().translate(tableWidth/2,0,0).rotateZ(angle).translate(-tableWidth/2,0,0).assignTo(worldSGC);
					worldSGC.getTransformation().multiplyOnRight(m);
				}
				
			});
			setupGeometry();
			updateScene();
			topWorldSGC.addChild(worldSGC);
			MatrixBuilder.euclidean().
				translate(0,1,-7.5).
				rotateX(Math.PI/6).
				rotateY(-Math.PI/2).
				rotateX(-Math.PI/2).assignTo(topWorldSGC);
			return topWorldSGC; //clipSGC[0];
		}

		@Override
		public void setupJRViewer(JRViewer v) {
			// TODO Auto-generated method stub
			super.setupJRViewer(v);
			jrviewer.setPropertiesFile("PerspectiveDemo.xml");
			jrviewer.setPropertiesResource(this.getClass(),"PerspectiveDemo.xml");
		}

		@Override
		public void display() {
			super.display();
			
			Viewer viewer = jrviewer.getViewer();
			Appearance ap = viewer.getSceneRoot().getAppearance();
			ap.setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(0,0,50));
			Color[] backgroundArray = new Color[4];
			float scale = .85f;
			final Color URBackground = new Color(scale*.8f, scale*.85f, scale*.68f); //new Color(215, 215, 190);
			final Color ULBackground  = new Color(scale*.8f, scale*.98f, scale*.88f); //new Color(255, 255, 200);  // bg[1];
			final Color LLBackground  = new Color(scale*.1f, scale*.1f, scale*.45f); //new Color(20,20,60);
			final Color LRBackground  = new Color(scale*.05f, scale*.15f, scale*.45f); //new Color(25, 25, 100);  //bg[2];
			backgroundArray[0] = URBackground;
			backgroundArray[1] = ULBackground;// bg[1];
			backgroundArray[2] = LLBackground;
			backgroundArray[3] = LRBackground;  //bg[2];
			viewer.getSceneRoot().getAppearance().setAttribute("backgroundColors", backgroundArray);
			viewer.renderAsync();
			Component comp = ((Component) viewer.getViewingComponent());
			comp.addKeyListener(new KeyAdapter() {
	 				public void keyPressed(KeyEvent e)	{ 
						switch(e.getKeyCode())	{
							
						case KeyEvent.VK_H:
							System.err.println("	1: move center up/down");
							System.out.println("	2: toggle edge drawing");
							break;
			
						case KeyEvent.VK_1:
							if (e.isShiftDown()) eyeHeight /= 1.05;
							else eyeHeight *= 1.05;
							updateScene();
							break;

						case KeyEvent.VK_2:
							if (e.isShiftDown()) tableLength /= 1.1;
							else tableLength *= 1.1;
							updateScene();
							break;

						case KeyEvent.VK_3:
							if (e.isShiftDown())
								edgeSGC.setVisible(!edgeSGC.isVisible());
							else {
								vertSGC.setVisible(!vertSGC.isVisible());
								vGeomSGC.setVisible(!vGeomSGC.isVisible());
							}
							break;

						case KeyEvent.VK_4:
							whichGeom = (whichGeom+1)%geomList.size();
							updateScene();
							break;		

						case KeyEvent.VK_5:
							rotating = !rotating;
							if (rotating) rotate.start();
							else rotate.stop();
							break;

						case KeyEvent.VK_6:
							nullPlaneRepresentation.setVisible(!nullPlaneRepresentation.isVisible());
							break;		

						case KeyEvent.VK_7:
							vanishingLineSGC.setVisible(!vanishingLineSGC.isVisible());
							break;		

						case KeyEvent.VK_8:
							clipping = !clipping;
							if (clipping) worldSGC.addChild(clippingPlanesSGC);
							else worldSGC.removeChild(clippingPlanesSGC);
							break;		
							
							

							}
			
					}
				});
		}

		@Override
		public Component getInspector() {
			JPanel mypanel = new JPanel();
			mypanel.setName("ReadMe");
			JTextArea textarea = new JTextArea(10,20);
			textarea.setEditable(false);
			textarea.append("This is a demo of central projection.\n"+
					"There are a number of keystrokes to control\n"+
					"what is displayed (Click in the graphics \n"+
					"window to make sure you have focus):\n"+
					"    '1':    move center up (w/ shift moves down).\n"+
					"    '2':    extend table length (w/ shift contract)\n"+
					"    '3':    toggle visibility of screen\n"+
					"    'shift 3':    toggle visibility of rays\n"+
					"    '4':    cycle through geometry list\n"+
					"    '5':    toggle rotation of world\n"+
					"    '6':    toggle parallel line pencil\n"+
					"    '7':    toggle horizon line\n"+
					"    '8':    toggle clipping planes\n"+
					"    'h':    display help menu for viewer\n"+
					"Cmd-f:  toggles fullscreen mode.\n"+
					"Current geometry can be dragged using\n"+
					"middle mouse (when over the geometry).\n"+
					"\nAuthor: Charles Gunn\n"+
					"    gunn at math.tu-berlin.de\n");
			mypanel.add(textarea);
			return mypanel;

		}

		private void updateScene() {
			
			hGeomSGC.setSelectedChild(whichGeom);
			System.err.println("setting selected child to "+whichGeom);

			// clip planes
			clippingPlanesSGC.setVisible(clipping);
			MatrixBuilder.euclidean().translate(0,.01+tableWidth/2,0).rotateX(-Math.PI/2).assignTo(clipSGC[0]);
			MatrixBuilder.euclidean().translate(0,-.01-tableWidth/2,0).rotateX(Math.PI/2).assignTo(clipSGC[1]);
			MatrixBuilder.euclidean().translate(-.01,0,0).rotateY(-Math.PI/2).assignTo(clipSGC[2]);
			MatrixBuilder.euclidean().translate(.01+tableLength,0,0).rotateY(Math.PI/2).assignTo(clipSGC[3]);

			// update the long stripgs (hack)
			updateGeometry();

//			double[] verts = {0,-tableWidth/2,0,  
//					tableLength,-tableWidth/2, 0, 
//					tableLength, tableWidth/2,0,  
//					0, tableWidth/2,0};
//			tableFac.setVertexCoordinates(verts);
//			tableFac.update();
			MatrixBuilder.euclidean().translate(tableLength/2, 0, -tableThickness/2).scale(tableLength, tableWidth, tableThickness).assignTo(tableSGC);
			double[] sverts = {0,-tableWidth/2,0,  
					screenHeight,-tableWidth/2, 0, 
					screenHeight, tableWidth/2,0,  
					0, tableWidth/2,0};
			screenFac.setVertexCoordinates(sverts);
			screenFac.update();
//			MatrixBuilder.euclidean().assignTo(screenSGC);
			MatrixBuilder.euclidean().translate(distToScreen,0,0).rotateY(-Math.PI/2).assignTo(screenSGC);
			double[][] centerPoints = new double[][]{{0,0,0,1},{0,0,eyeHeight,1}};
			IndexedLineSet ils = IndexedLineSetUtility.createCurveFromPoints(
					centerPoints, false);
			centerSGC.setGeometry(ils);
			
			double[][] endPoints = new double[][]{{distToScreen,tableWidth/2,eyeHeight,1},{distToScreen,-tableWidth/2,eyeHeight,1}};
			ils = IndexedLineSetUtility.createCurveFromPoints(endPoints, false);
			vanishingLineSGC.setGeometry(ils);
			
			MatrixBuilder.euclidean().translate(0,0,eyeHeight).scale(2.5).assignTo(nullPlaneRepresentation);
			SceneGraphComponent flatHGeom = SceneGraphUtility.flatten(hGeomSGC, true);
			Geometry geom = SceneGraphUtility.getFirstGeometry(flatHGeom);
			if ( !(geom instanceof IndexedLineSet)) 
				throw new IllegalArgumentException("Must be an indexed face set");
			IndexedLineSet hgeom = (IndexedLineSet) geom;
			IndexedLineSet hEdge, vEdge, screenEdge;
			double[][] points = hgeom.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
			vEdge = coneOverPointSet(points, centerPoints[1]);
			vEdgeSGC.setGeometry(vEdge);
			
			// create the flattened version of the geometry on the ground plane
			{
			double[] projectionPlane = {0,0,1,0};
			double[] center = {0,0,1,0};
			double[] flattener = Pn.makeGeneralizedProjection(null, center, projectionPlane, .01);
			double[][] hpoints = Rn.matrixTimesVector(null, flattener, points);
			hEdge = coneOverPointSet(hpoints, centerPoints[0]);
			hEdgeSGC.setGeometry(hEdge);
			}
			
			// create the flattened version of the geometry on the screen
			{
			double[] projectionPlane = {1,0,0,-distToScreen+.01};
			double[] center = {0,0,eyeHeight,1};
			double[] flattener = Pn.makeGeneralizedProjection(null, center, projectionPlane, .01);
			MatrixBuilder.euclidean(new Matrix(flattener)).assignTo(vGeomSGC);
			vGeomSGC.setGeometry(hgeom);
			vGeomSGC.setAppearance(hGeomSGC.getSelectedChildAsSGC().getAppearance());
			}
			
		}

		private IndexedLineSet coneOverPointSet(double[][] points,
				double[] center) {
			int fiber = points[0].length;
			double[][] verts = new double[points.length+1][fiber];
			for (int i= 0; i<points.length; ++i) verts[i] = points[i];
			int n = points.length;
			if (center.length == fiber) verts[n] = center;
			else {
				if (fiber == 4) verts[n][3] = 1.0;
				for (int i =0; i<fiber; ++i)  verts[n][i] = center[i];
			}
			int[][] edges = new int[n][2];
			for (int i = 0; i<n; ++i)	{
				edges[i][0] = n;
				edges[i][1] = i;
			}
			IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
			ilsf.setVertexCount(verts.length);
			ilsf.setVertexCoordinates(verts);
			ilsf.setEdgeCount(edges.length);
			ilsf.setEdgeIndices(edges);
			ilsf.update();
			return ilsf.getIndexedLineSet();
		}

		double offset = 3.5;
		PointSet[] geometries = new PointSet[3];
		List<PointSet> geomList = new ArrayList<PointSet>();
		int whichGeom = 0;
		private Tool dragTool;
//		private TranslateShapeTool dragTool;
		private TransformationListener listener;
		private SceneGraphComponent nullPlaneRepresentation;
		boolean afterStart = false;
		private IndexedFaceSetFactory longStripsFactory;
		boolean dirtyTform = true;
		private void setupGeometry()	{
			// aarg: deadlock problems in updateScene()
//			dragTool = new TranslateShapeTool() {
//
//				@Override
//				protected void adjust(double[] mat) {
//					mat[11] = 0;
//				}
//
//				
//			};
			dragTool = new DraggingTool(); // {

//				@Override
//				public void perform(ToolContext tc) {
//				     comp.getTransformation().getMatrix(result.getArray());
//				     evolution.setEntry(2, 3, 0);
//				     result.multiplyOnRight(evolution);
//				     comp.getTransformation().setMatrix(result.getArray());
//					
//				}
//
//				
//			};
			listener = new TransformationListener() {
				
				public void transformationMatrixChanged(TransformationEvent ev) {
					//if (afterStart) updateScene();
					if (afterStart)
						Scene.executeWriter(worldSGC, new Runnable() {
						
						public void run() {
							updateScene();
						}
					});
				}
			};
			// prepare the clip planes
			for (int i = 0; i<4; ++i)	{
				clipSGC[i] = new SceneGraphComponent("clip"+i);
				ClippingPlane clipPlane = new ClippingPlane();
				clipSGC[i].setGeometry(clipPlane);
				clippingPlanesSGC.addChild(clipSGC[i]);
			}

			double[][] tri = {{-1,-1,0,1},{+0,1,0,1},{1,0,0,1}};
			IndexedFaceSet triangle = IndexedFaceSetUtility.constructPolygon(tri);
			SceneGraphComponent child = addGeometry(triangle);
			MatrixBuilder.euclidean().translate(offset,0,.01).assignTo(child);

			IndexedFaceSet cube = Primitives.coloredCube();
			child = addGeometry(cube);
			MatrixBuilder.euclidean().translate(offset+1, 0,.5).rotateZ(Math.PI/6).scale(.5).assignTo(child);
			
			IndexedFaceSetFactory quad = Primitives.texturedQuadrilateralFactory(verts);
			child = addGeometry(quad.getPointSet());
			child.setAppearance(new Appearance());
			SimpleTextureFactory stf = new SimpleTextureFactory();
			stf.setType(TextureType.CHECKERBOARD);
			stf.setColor(0, Color.blue);
			stf.setColor(1, Color.yellow);
			stf.update();
			Texture2D tex2d = TextureUtility.createTexture(child.getAppearance(), "polygonShader", stf.getImageData());
			Matrix foo = new Matrix();
			MatrixBuilder.euclidean().scale(4,4,1).assignTo(foo);
			tex2d.setTextureMatrix(foo);
			MatrixBuilder.euclidean().translate(offset+1, 0, 0).rotateZ(Math.PI/6).scale(1.2).assignTo(child);
			child.getAppearance().setAttribute("polygonShader.diffuseColor", Color.white);
			child.getAppearance().setAttribute("ambientCoefficient", .05);
			child.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		
			longStripsFactory = new IndexedFaceSetFactory();
			updateGeometry();
			child = addGeometry(longStripsFactory.getIndexedFaceSet());
			child.setAppearance(new Appearance());
			child.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", Color.black);
			child.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);

			geometries = geomList.toArray(new PointSet[geomList.size()]);
			hGeomSGC.setSelectedChild(whichGeom);
			MatrixBuilder.euclidean().translate(0, 0, 0.005).assignTo(hGeomSGC);
			afterStart = true;
		}

		private void updateGeometry() {
			int numLines = 4;
			double[][] vs = new double[numLines*2][3];
			int[][] edges = new int[numLines-1][4];
			Color[] fcolors = {Color.red, Color.blue, Color.green};
			for (int i=0; i<numLines; ++i)	{
				double y = tableWidth/4 * (-1 + 2 * (i/(numLines-1.0)));
				vs[i][0] = distToScreen;
				vs[i][1] = vs[i+numLines][1] = y;
				vs[i+numLines][0] = tableLength;
				if (i < numLines-1)	{
					edges[i][0] = i;
					edges[i][1] = i+numLines;					
					edges[i][2] = i+numLines+1;					
					edges[i][3] = i+1;					
				}
			}
			longStripsFactory.setVertexCount(vs.length);
			longStripsFactory.setVertexCoordinates(vs);
			longStripsFactory.setFaceCount(edges.length);
			longStripsFactory.setFaceIndices(edges);
			longStripsFactory.setFaceColors(fcolors);
			longStripsFactory.setGenerateEdgesFromFaces(true);
			longStripsFactory.update();
		}

		private SceneGraphComponent addGeometry(PointSet triangle) {
			geomList.add(triangle);
			SceneGraphComponent child = new SceneGraphComponent("child"+geomList.size());
			child.setTransformation(new Transformation());
			child.setGeometry(triangle);
			hGeomSGC.addChild(child);
			child.getTransformation().addTransformationListener(listener);
			child.addTool(dragTool);
			return child;
		}
		
		public static void main(String[] args) {
			PerspectiveDemo pd = new PerspectiveDemo();
			pd.display();
		}
	}
