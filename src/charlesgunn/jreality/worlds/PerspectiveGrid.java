/*
 * Created on Nov 11, 2004
 *
 */
package charlesgunn.jreality.worlds;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;

import charlesgunn.jreality.geometry.projective.LineUtility;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.plugin.TermesSpherePlugin;
import charlesgunn.jreality.tools.RotateShapeTool;
import charlesgunn.jreality.tools.ToolManager;
import charlesgunn.jreality.tools.TranslateShapeTool;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

/**
 * @author gunn
 *
 */
public class PerspectiveGrid extends Assignment {
		SceneGraphComponent 
		contentWorld,
			theRealWorldTranslate, 
				theRealWorldRotate,
					theWorld,
						kids[], 
							lines, 
						quad,
						pinf;
		protected boolean showTubes = true;
		int lineCount = 60;
		Timer rotate = null, ytranslate = null, xtranslate = null, goPersp = null;
		boolean labels = false,
			tlating = true;
		double w = .0001,
				linewidth = 2.4;
		double f = Math.sqrt(2.0);
		Tool rotTool = new RotateShapeTool(), transTool = new TranslateShapeTool();
		final double[][] ptsAtInfy = {{1,-1,0,w},{0,-f,0,w},{-1,-1,0,w},{-f,0,0,w},{-1,1,0,w},{0,f,0,w},{1,1,0,w},{f,0,0,w}};
		public SceneGraphComponent getContent()	{
			
			theWorld = SceneGraphUtility.createFullSceneGraphComponent("theWorld");
			contentWorld = SceneGraphUtility.createFullSceneGraphComponent("contentWorld");
			theRealWorldRotate = SceneGraphUtility.createFullSceneGraphComponent("theWorldRotate");
			theRealWorldRotate.addChild(theWorld);
			theRealWorldRotate.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
			theRealWorldRotate.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, linewidth * CommonAttributes.LINE_WIDTH_DEFAULT);
			theRealWorldTranslate = SceneGraphUtility.createFullSceneGraphComponent("theWorldTranslate");
			theRealWorldTranslate.addChild(theRealWorldRotate);
			theRealWorldTranslate.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
			theRealWorldTranslate.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, linewidth * CommonAttributes.LINE_WIDTH_DEFAULT);
			lines = SceneGraphUtility.createFullSceneGraphComponent("lines");
			lines.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
			lines.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
			lines.setGeometry(calculateLines());
			kids = new SceneGraphComponent[4];
			
			float a = .6f;
			float b = 1.0f;
			Color[] colors = { new Color(a, a, b), new Color(b,b,a), new Color(a, a, b), new Color(b,b,a)};
			for (int i=0; i<4; ++i)	{
				kids[i] = SceneGraphUtility.createFullSceneGraphComponent("kid"+i);
				kids[i].addChild(lines);
				kids[i].getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+"diffuseColor", colors[i]);
				MatrixBuilder.euclidean().rotateZ(Math.PI*i/4.0).scale( i%2 == 0 ? 1.0 : Math.sqrt(2.0)).assignTo(kids[i]);
				theWorld.addChild(kids[i]);
			}
			
			SceneGraphComponent linf = new SceneGraphComponent();
			linf.setAppearance(new Appearance());
			linf.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
			linf.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, linewidth * CommonAttributes.LINE_WIDTH_DEFAULT);
			linf.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+"diffuseColor",Color.WHITE);
			linf.setGeometry(PointRangeFactory.line( new double[]{1,0,0,0}, new double[]{0,1,0,0}));
			theWorld.addChild(linf);

			quad = new SceneGraphComponent();
			quad.setAppearance(new Appearance());
			quad.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
//			quad.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, true);
			quad.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+"diffuseColor", Color.black); //new Color(.1f, .2f, .1f));
			quad.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SPECULAR_COEFFICIENT,0.1);
			MatrixBuilder.euclidean().translate(0,0,-.02).scale(1000).assignTo(quad);
			quad.setGeometry(Primitives.regularPolygon(4));
			theWorld.addChild(quad);

			psf = new PointSetFactory();
			psf.setVertexCount(8);
			psf.setVertexCoordinates(ptsAtInfy);
			if (labels) psf.setVertexLabels(new String[]{"H","O","R","I","Z","O","N",""});
			psf.update();
			pinf = new SceneGraphComponent("pinf");
			pinf.setGeometry(psf.getPointSet());
			Appearance ap = new Appearance();
			ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
			ap.setAttribute(CommonAttributes.POINT_RADIUS, 1.0);
			ap.setAttribute(CommonAttributes.SPHERES_DRAW, true);
		    DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
		    DefaultTextShader pts = (DefaultTextShader) ((DefaultPointShader)dgs.getPointShader()).getTextShader();
		    pts.setDiffuseColor(Color.WHITE);
		    Double scale = new Double(40.0);
		    pts.setScale(scale);
		    double[] offset = new double[]{-1,.15,0};
		    pts.setOffset(offset);
		    pts.setFont(new Font("Sans Serif",Font.PLAIN, 48));
			pinf.setAppearance(ap);
			theWorld.addChild(pinf);
			
			theWorld.getAppearance().setAttribute(CommonAttributes.CENTER_ON_BOUNDING_BOX, true);
			contentWorld.addTool(transTool);
			contentWorld.addChild(theRealWorldTranslate);
			//MatrixBuilder.euclidean().translate(0,0,-20.0).assignTo(theWorld));
			return contentWorld;
		}

		private IndexedLineSet calculateLines() {
			int numSegs = 12;
			IndexedLineSet ils = new IndexedLineSet();
			int[][] indices = new int[lineCount][numSegs+1];
			double[][] verts = new double[lineCount*numSegs][4];
			double[] p0 = {0,0,0,1}, p1 = {0,1,0,1};
			int foo = 0;
			for (int i = 0; i<lineCount; ++i)	{
				foo = i*numSegs;
				p1[0] = p0[0] = i-lineCount/2;
				LineUtility.coordinatesFor1DExtent(verts, foo, numSegs, p0, p1);
				for (int j = 0; j<=numSegs; ++j) indices[i][j] = foo+(j%numSegs);
			}
			IndexedLineSetFactory ifsf = new IndexedLineSetFactory();
			ifsf.setVertexCount(verts.length);
			ifsf.setVertexCoordinates(verts);
			ifsf.setEdgeCount(indices.length);
			ifsf.setEdgeIndices(indices);
			ifsf.update();
			ils = ifsf.getIndexedLineSet();
			return ils;
		}
		@Override
		public void setupJRViewer(JRViewer v) {
			// TODO Auto-generated method stub
			super.setupJRViewer(v);
			v.registerPlugin(new TermesSpherePlugin());
			jrviewer.setPropertiesFile("PerspectiveGrid.xml");
			jrviewer.setPropertiesResource(this.getClass(), "PerspectiveGrid.xml");
		}

		public void display() {
			super.display();
			viewer = jrviewer.getViewer();
			MatrixBuilder.euclidean().translate(0,0,6.0).assignTo(CameraUtility.getCameraNode(viewer));
			viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor",java.awt.Color.BLACK);
			CameraUtility.getCamera(viewer).setNear(1.0);
			CameraUtility.getCamera(viewer).setFar(-10000.0);
			CameraUtility.getCamera(viewer).setFieldOfView(100.0);
//			ToolManager.toolManagerForViewer(viewer).activateTool(ToolManager.TRANSLATION_TOOL);
			((Component) viewer.getViewingComponent()).addKeyListener(getKeyAdapter());
			rotate = new Timer(20, new ActionListener()	{
				double dangle = .005;
				final double[] zRotateM = P3.makeRotationMatrix(
						null, P3.originP3, new double[]{0,0,1,0}, .005, Pn.ELLIPTIC);
//				final double[] zRotateM = P3.makeScrewMotionMatrix(
//						null, P3.originP3, new double[]{1,0,0,1}, .01, Pn.ELLIPTIC);

				public void actionPerformed(ActionEvent e) {
					theRealWorldTranslate.getTransformation().multiplyOnRight(zRotateM); //zRotateM);
					viewer.renderAsync();
				}
				
			});
			xtranslate = new Timer(20, new ActionListener()	{
				double step = .015;
//				final double[] zRotateM = P3.makeRotationMatrixZ(null, dangle);
				final double[] xtrans = P3.makeTranslationMatrix(null, new double[]{step, 0, 0}, 0);
				public void actionPerformed(ActionEvent e) {
					theRealWorldTranslate.getTransformation().multiplyOnRight(xtrans); //zRotateM);
					viewer.renderAsync();
				}
				
			});
			ytranslate = new Timer(20, new ActionListener()	{
				double step = .015;
//				final double[] zRotateM = P3.makeRotationMatrixZ(null, dangle);
				final double[] ytrans = P3.makeTranslationMatrix(null, new double[]{0,-step, 0}, 0);
				public void actionPerformed(ActionEvent e) {
					theRealWorldTranslate.getTransformation().multiplyOnRight(ytrans); //zRotateM);
					viewer.renderAsync();
				}
				
			});
			goPersp = new Timer(20, new ActionListener()	{
				double step = .003;
				final double[] xRotateM = P3.makeRotationMatrixX(null, -step);
				public void actionPerformed(ActionEvent e) {
					theRealWorldTranslate.getTransformation().multiplyOnRight(xRotateM); //zRotateM);
					viewer.renderAsync();
				}
				
			});
		}

		private PointSetFactory psf;
		KeyAdapter ka = null;
		public KeyAdapter getKeyAdapter() {
			if (ka == null)	{
				ka = new KeyAdapter()	{
					boolean rotating = false, xtranslating = false, ytranslating = false, xroting = false;
					boolean beyondInfinity = false;
					public void keyPressed(KeyEvent e)	{ 
						switch(e.getKeyCode())	{
							
						case KeyEvent.VK_H:
							System.out.println("	1: toggle rotate");
							System.out.println("	2: toggle translate");
							System.out.println("	3: toggle x-rotate");
							System.out.println("	4: toggle beyond infinity");
							System.out.println("	5: toggle labels");
							System.out.println("	6: reset content tform");
							System.out.println("	7: reset tlate/rotate tforms");
							break;
			
						case KeyEvent.VK_1:
							rotating = !rotating;
							if (rotating) rotate.start();
							else rotate.stop();
							break;

						case KeyEvent.VK_2:
							xtranslating = !xtranslating;
							if (xtranslating) xtranslate.start();
							else xtranslate.stop();
							break;
							
						case KeyEvent.VK_8:
							ytranslating = !ytranslating;
							if (ytranslating) ytranslate.start();
							else ytranslate.stop();
							break;
							
						case KeyEvent.VK_3:
							xroting = !xroting;
							if (xroting) goPersp.start();
							else goPersp.stop();
							break;
							
						case KeyEvent.VK_4:
							beyondInfinity = !beyondInfinity;
							if (beyondInfinity) CameraUtility.getCamera(viewer).setFar(-1.0);
							else CameraUtility.getCamera(viewer).setFar(-10000);
							viewer.renderAsync();
							break;

//						case KeyEvent.VK_4:
//							tlating = !tlating;
//							contentWorld.removeTool(tlating ? rotTool : transTool);
//							contentWorld.addTool(tlating ? transTool : rotTool);
////							ToolManager.toolManagerForViewer(viewer).activateTool(
////									tlating? ToolManager.TRANSLATION_TOOL : ToolManager.ROTATION_TOOL);
//							viewer.renderAsync();
//							break;

						case KeyEvent.VK_5:
							labels = !labels;
							psf.setVertexLabels(labels ? new String[]{"H","O","R","I","Z","O","N","!"} : null);
							psf.update();
							System.err.println("Setting point set with labels "+labels);
							viewer.renderAsync();
							break;
							
						case KeyEvent.VK_6:
//							tlating = true;
//							contentWorld.removeTool(tlating ? rotTool : transTool);
//							contentWorld.addTool(tlating ? transTool : rotTool);
							contentWorld.getTransformation().setMatrix(Rn.identityMatrix(4));
////							ToolManager.toolManagerForViewer(viewer).activateTool(
////									tlating? ToolManager.TRANSLATION_TOOL : ToolManager.ROTATION_TOOL);
							viewer.renderAsync();
							break;
							
						case KeyEvent.VK_7:
							rotate.stop();
							xtranslate.stop();
							theRealWorldTranslate.getTransformation().setMatrix(Rn.identityMatrix(4));
							theRealWorldRotate.getTransformation().setMatrix(Rn.identityMatrix(4));
							break;



					}
			
					}
				};
			}
			return ka;
		}
			
		@Override
		public Component getInspector() {
			JPanel mypanel = new JPanel();
			mypanel.setName("ReadMe");
			JTextArea textarea = new JTextArea(10,20);
			textarea.setEditable(false);
			textarea.append("This application shows a \n" +
					"euclidean grid in perspective.  \n"+
					"It starts with a bird's eye view \n"+
					"Use the rotate tool to rotate the \n"+
					"grid until the horizon can be seen. \n\n"+
					"Type 'h' to display other keyboard shortcuts.\n"+
					"Shift-cntl-f:  toggles fullscreen mode.\n"+
					"\nAuthor: Charles Gunn\n");
			mypanel.add(textarea);
			return mypanel;
		}
		
		public static void main(String[] args) {
			new PerspectiveGrid().display();
		}
	}
