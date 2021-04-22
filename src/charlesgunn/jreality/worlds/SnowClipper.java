/*
 * Created on Jan 18, 2005
 *
 */
package charlesgunn.jreality.worlds;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JMenuBar;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.jogl.plugin.InfoOverlay;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.reader.ReaderOOGL;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.SelectionManagerImpl;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;
import de.jtem.discretegroup.util.WingedEdge;

/**
 * @author gunn
 *
 */
public class SnowClipper extends LoadableScene {

	public SnowClipper()	{
		super();
	}
	SceneGraphPath toClipPlane, toClipPlane2, toSculpture;
	SceneGraphComponent snowSculpture, clipPlaneJiggler, clipPlaneJiggler2, whiteframe, redframe ;
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("snowClipperWorld");
	SceneGraphComponent manipulator = SceneGraphUtility.createFullSceneGraphComponent("manipulator");
	InfoOverlay iolay;
	Vector infoStrings;
	double[] clippingPlane, clippingPlane2, pickPoint;
	static String fileToLoad = "/homes/geometer/gunn/Documents/Models/snowSculpture/taperedscu.off";
	static double snowSculptureScale = 25.0;
	static double snowSculptureUpdateRate = 30;
	static {
		String foo = Secure.getProperty("snowSculptureFile");
		if (foo != null) fileToLoad = foo;
		foo = Secure.getProperty("snowSculptureScale");
		if (foo != null) snowSculptureScale = Double.parseDouble(foo);
		foo = Secure.getProperty("snowSculptureUpdateRate");
		if (foo != null) snowSculptureUpdateRate = Double.parseDouble(foo);
	}
	public SceneGraphComponent makeWorld() {
		world.addChild(manipulator);
		
		ReaderOOGL or = new ReaderOOGL();
		try {
      snowSculpture = or.read(new File(fileToLoad));
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
		snowSculpture.setName("snowSculptureModel");
		MatrixBuilder.euclidean().translate(0,0,72).scale(snowSculptureScale).assignTo(snowSculpture);
//		snowSculpture.setTransformation(new Transformation());
//		snowSculpture.getTransformation().setStretch(snowSculptureScale);		// (-.2,.2) -> (-60, 60)" 
//		snowSculpture.getTransformation().setTranslation(0,0,72);
		snowSculpture.getTransformation().setReadOnly(false);
		manipulator.addChild(snowSculpture);
		
		if (snowSculpture.getAppearance() == null)  snowSculpture.setAppearance(new Appearance());
		//world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING, false);
		world.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		
		 clipPlaneJiggler =  SceneGraphUtility.createFullSceneGraphComponent("Plane1");
		Appearance ap1 = clipPlaneJiggler.getAppearance();
		ap1.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap1.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap1.setAttribute(CommonAttributes.FACE_DRAW, true);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
		double[][] vv = {{-1,-1,0},{1,-1,0},{1,1,0},{-1,1,0}};
		IndexedFaceSet square = IndexedFaceSetUtility.constructPolygon(vv);
		clipPlaneJiggler.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, 0.75);
		clipPlaneJiggler.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		MatrixBuilder.euclidean().translate(0,0,120).assignTo(clipPlaneJiggler);
		
		//boolean 
//		SceneGraphComponent cp  =  SceneGraphUtility.createFullSceneGraphComponent("fineAdjustment1");
		clipPlaneJiggler.setGeometry(square);
//		cp.addChild(clipPlaneJiggler);
//		clipPlaneJiggler.getTransformation().setIsEditable(false);
		world.addChild(clipPlaneJiggler);
		//world.getTransformation().setRotation(-Math.PI/2, 1,0,0);

		clipPlaneJiggler2 =  SceneGraphUtility.createFullSceneGraphComponent("Plane2");
		ap1 = clipPlaneJiggler2.getAppearance();
		ap1.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap1.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap1.setAttribute(CommonAttributes.FACE_DRAW, true);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.RED);
		clipPlaneJiggler2.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, 0.75);
		clipPlaneJiggler2.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		MatrixBuilder.euclidean().translate(0,0,60).assignTo(clipPlaneJiggler2);
//	    clipPlaneJiggler2 =  SceneGraphUtility.createFullSceneGraphComponent("fineAdjustment2");
		clipPlaneJiggler2.setGeometry(square);
//		clipPlaneJiggler2.addChild(cp);
//		clipPlaneJiggler2.getTransformation().setIsEditable(false);
		world.addChild(clipPlaneJiggler2);

//		world.getTransformation().setCenter(new double[] {0,0,72});
//		manipulator.getTransformation().setCenter(new double[] {0,0,72});
		

		
		whiteframe = SceneGraphUtility.createFullSceneGraphComponent("whiteframe");
		whiteframe.setAppearance(clipPlaneJiggler.getAppearance());
		Appearance ap = whiteframe.getAppearance();
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.WHITE);
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TRANSPARENCY, 0.75);
		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);

		redframe = SceneGraphUtility.createFullSceneGraphComponent("redframe");
		redframe.setAppearance(clipPlaneJiggler2.getAppearance());
		ap = redframe.getAppearance();
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.RED);
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TRANSPARENCY, 0.75);
		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		MatrixBuilder.euclidean().scale(.99).assignTo(redframe);
//		SceneGraphComponent frame = SceneGraphUtility.createFullSceneGraphComponent("frame");
//		frame.getTransformation().setStretch(59,59,71);
//		frame.getTransformation().setTranslation(0,0,72);
//		IndexedFaceSet tet = Primitives.cube();
//		frame.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.WHITE);
//		frame.addChild(TubeUtility.ballAndStick(tet,.01,.01, null, null));
		manipulator.addChild(whiteframe);
		manipulator.addChild(redframe);
		
		return world;
	}
	Viewer viewer;
		@Override
	public void customize(JMenuBar menuBar, final Viewer viewer) {
			this.viewer = viewer;
			SceneGraphPath toWorld = new SceneGraphPath();
			toWorld.push(viewer.getSceneRoot());
			toWorld.push(world);

			toSculpture = new SceneGraphPath();
			toSculpture.push(viewer.getSceneRoot());
			toSculpture.push(world);
			toSculpture.push(manipulator);

			toClipPlane = new SceneGraphPath();
			toClipPlane.push(viewer.getSceneRoot());
			toClipPlane.push(world);
			toClipPlane.push(clipPlaneJiggler);
			
			toClipPlane2 = new SceneGraphPath();
			toClipPlane2.push(viewer.getSceneRoot());
			toClipPlane2.push(world);
			toClipPlane2.push(clipPlaneJiggler2);
			
			SelectionManager sm = SelectionManagerImpl.selectionManagerForViewer(viewer);
			sm.addSelection(toWorld);
			sm.addSelection(toSculpture);
			sm.addSelection(toClipPlane);
			sm.addSelection(toClipPlane2);
			calculateClippingPlanes();
			updateFrames();
			viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new java.awt.Color(60, 60, 60));
//			viewer.getSelectionManager().getPickPointAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, .005);
			//viewer.getSelectionManager().setRenderSelection(true);
			((Component) viewer.getViewingComponent()).addKeyListener(new KeyAdapter()	{
				
			    double scaleFactor = .05;
			    int selection = 0;
				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_H:
						System.out.println("	6:  toggle info display");
						System.out.println("	7:  increase plane movement increment");
						System.out.println("shift-7:  decrease plane movement increment");
						System.out.println("	8:  dump plane info to stdout");
						System.out.println("	9:  dump pickpoint info to stdout");
						System.out.println("	k:  cycle selection path");
						System.out.println("	up/down arrows: move white plane ");
						System.out.println("	left/right arrows: move red plane ");
						break;
						
					case KeyEvent.VK_6:
						iolay.setVisible(!iolay.isVisible());
				        break;
						
					case KeyEvent.VK_7:
						if (e.isShiftDown()) scaleFactor *= .75;
						else scaleFactor *= 1.3333;
					    if (scaleFactor > 10) scaleFactor = 10.0;
						System.out.println("Plane movement increment is "+scaleFactor);
				        break;
						
					case KeyEvent.VK_8:
						System.out.println("White plane coordinates: "+Rn.toString(clippingPlane));
						System.out.println("Red   plane coordinates: "+Rn.toString(clippingPlane2));
						break;
						
					case KeyEvent.VK_9:
						if (pickPoint != null) System.out.println("Pick Point coordinates: "+Rn.toString(pickPoint));
						break;

					
					case KeyEvent.VK_UP:
			    			moveZPlane(1, clipPlaneJiggler);
			    			break;
		    			
				    case KeyEvent.VK_DOWN:
			    			moveZPlane(-1, clipPlaneJiggler);
			    			break;
				   
				    case KeyEvent.VK_LEFT:
		    				moveZPlane(-1, clipPlaneJiggler2);
		    				break;
		    			
			    
				    case KeyEvent.VK_RIGHT:
		    				moveZPlane(1, clipPlaneJiggler2);
		    				break;
						}
				}
				/**
				 * @param shift
				 */
				private void moveZPlane(double scale, SceneGraphComponent jiggler) {
					//System.out.println("Moving the plane");
		    			double[] shift = {0,0,scale*scaleFactor};
					double[] movePlaneInZ = P3.makeTranslationMatrix(null, shift, Pn.EUCLIDEAN);
					//jiggler.getTransformation().setIsEditable(true);
					jiggler.getTransformation().multiplyOnRight(movePlaneInZ);
					//jiggler.getTransformation().setIsEditable(false);
					viewer.renderAsync();
				}

			});
//			iolay = new InfoOverlay(viewer);
//			iolay.setVisible(true);
//			if ((viewer.getViewingComponent() instanceof GLCanvas))
//				((GLAutoDrawable) viewer.getViewingComponent()).addGLEventListener(iolay);	 		
//	 		infoStrings = new Vector();
//	 		iolay.setInfoStrings(infoStrings);
		
	 		int milli = (int) (1000/snowSculptureUpdateRate);
			javax.swing.Timer followTimer = new javax.swing.Timer(milli, new ActionListener()	{
				public void actionPerformed(ActionEvent e) {
//					if (iolay.isVisible()) updateInfoOverlay(); 
					updateFrames();
				}
			} ) ;
			followTimer.start();
			
	}
//		private void updateInfoOverlay() {
//			infoStrings.clear();
//			//infoStrings.add("Realtime data");
//			calculateClippingPlanes();
//			if (clippingPlane != null) infoStrings.add("White clipping plane:    "+Rn.toString(clippingPlane));
//			if (clippingPlane2 != null) infoStrings.add("Red clipping plane:    "+Rn.toString(clippingPlane2));
//			PickPoint pickPoint2 = viewer.getSelectionManager().getPickPoint();
//			if (pickPoint2 != null)  {
//				SceneGraphPath sgp = pickPoint2.getPickPath();
//				pickPoint = pickPoint2.getPointObject();
//				if (sgp.getLastComponent() == snowSculpture) {
//					if (pickPoint != null )  {
//						pickPoint = Rn.matrixTimesVector(null, snowSculpture.getTransformation().getMatrix(), pickPoint);
//						infoStrings.add("Sculpture pick point:    "+Rn.toString(pickPoint));
//					}					
//				} //else infoStrings.add("Pick point:    "+Rn.toString(pickPoint, 7));
//			}
//			infoStrings.add("Framerate: "+Double.toString(viewer.getRenderer().getFramerate()));
//			infoStrings.add("Time: "+System.currentTimeMillis());
//			viewer.renderAsync();
//		}
		
		public void updateFrames()	{
			WingedEdge whiteFrameG = new WingedEdge(-60, 60, -60, 60, -4, 144);
			whiteFrameG.cutWithPlane(clippingPlane, 23);
			int foo = whiteFrameG.getFirstFaceWithTag(23);
			double[][] cutPlane = whiteFrameG.getFaceWithIndex(foo);
			whiteframe.setGeometry(IndexedFaceSetUtility.constructPolygon(cutPlane));
			SceneGraphUtility.removeChildren(whiteframe);
			BallAndStickFactory basf = new BallAndStickFactory(whiteFrameG);
			basf.setBallColor(java.awt.Color.WHITE);
			basf.setBallRadius(1.0);
			basf.setStickColor(java.awt.Color.WHITE);
			basf.setStickRadius(.5);
			basf.setMetric(Pn.EUCLIDEAN);
			basf.update();
			SceneGraphComponent ballAndStick = basf.getSceneGraphComponent();
			ballAndStick.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
			whiteframe.addChild(ballAndStick);
			WingedEdge redFrameG = new WingedEdge(-60, 60, -60, 60, -4, 144);
			redFrameG.cutWithPlane(clippingPlane2, 24);
			foo = redFrameG.getFirstFaceWithTag(23);
			cutPlane = redFrameG.getFaceWithIndex(foo);
			redframe.setGeometry(IndexedFaceSetUtility.constructPolygon(cutPlane));
			basf = new BallAndStickFactory(redFrameG);
			basf.setBallColor(java.awt.Color.RED);
			basf.setBallRadius(1.0);
			basf.setStickColor(java.awt.Color.RED);
			basf.setStickRadius(.5);
			basf.setMetric(Pn.EUCLIDEAN);
			basf.update();
			SceneGraphComponent ballAndStick2 = basf.getSceneGraphComponent();
			ballAndStick2.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
			SceneGraphUtility.removeChildren(redframe);
			redframe.addChild(ballAndStick2);
			viewer.renderAsync();
		}
		/**
		 * @param zdir
		 */
		private void calculateClippingPlanes() {
			clippingPlane = calculateClippingPlane(toClipPlane);
			clippingPlane2 = calculateClippingPlane(toClipPlane2);
		}
		
	/**
		 * 
		 */
		private double[] calculateClippingPlane(SceneGraphPath clipPlanePath) {
			double[] zdir = {0,0,1,0};
			
			double[] toS = toSculpture.getInverseMatrix(null);
			double[] fromC = clipPlanePath.getMatrix(null);
			double[] cToS = Rn.times(null, toS, fromC);
			double[] cToSN = Rn.transpose(null, Rn.inverse(null, cToS));
//			double tmp = cToSN[1]; cToSN[1] = cToSN[4]; cToSN[4] = tmp;
//			tmp = cToSN[2]; cToSN[2] = cToSN[8]; cToSN[8] = tmp;
//			tmp = cToSN[6]; cToSN[6] = cToSN[9]; cToSN[9] = tmp;
//			System.out.println("CtoS\n"+Rn.matrixToString(cToSN));
			double[] clippingPlane = Rn.matrixTimesVector(null, cToSN, zdir);
			Pn.normalizePlane(clippingPlane, clippingPlane, Pn.EUCLIDEAN);
			return clippingPlane;
		}
		public boolean addBackPlane() { return false; }
		
		public boolean isEncompass() { return true; }
	
//	public static void main(String[] args) {
//		SnowClipper test = new SnowClipper();
//		test.begin();
//	}

}
