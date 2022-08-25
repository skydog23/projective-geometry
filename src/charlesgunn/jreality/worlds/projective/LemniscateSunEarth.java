/*
 * Created on Dec 1, 2010
 *
 */
package charlesgunn.jreality.worlds.projective;

import static de.jreality.geometry.GeometryUtility.BOUNDING_BOX;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.Component;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import charlesgunn.anim.jreality.SceneGraphAnimator;
import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.anim.util.TextSlider;
import charlesgunn.jreality.GeometryCollector;
import charlesgunn.jreality.geometry.projective.PointCollector;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphPathObserver;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.CameraUtility;
import de.jreality.util.Input;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

/** TODO
 * add earth texture map and daily rotation
 * add transformation to cubemap
 * @author gunn
 *
 */
public class LemniscateSunEarth extends Assignment {

	private transient SceneGraphComponent 
	    fixSGC,
	    world,
		lemniscate1SGC,	
		lemniscate2SGC,
		lem1FramesSGC,
		linesSGC,
		surfaceSGC,
		lineGeomSGC,
		stickGeomSGC,
		celestialSphereSGC;
	private transient int numPoints = 500;		// damn texture map jaggies! Why?
	private transient GeometryCollector geomCollector = new GeometryCollector(numPoints);
	private transient QuadMeshFactory surface = new QuadMeshFactory();
	private transient PointRangeFactory lineGeom = new PointRangeFactory();
	private IndexedFaceSet sphericalPatch;
	private transient IndexedLineSetFactory stick;
	
	private transient PointCollector pc1 = new PointCollector(numPoints, 4),
			pc2 = new PointCollector(numPoints, 4);
	private transient boolean debug = false;
	private transient Color[] stickColors = new Color[2];
	private CubeMap constellationMap;
	private transient boolean fixSun = true, fixEarth=false, showSurface = false;
	@Override
	public SceneGraphComponent getContent() {
		fixSGC = SceneGraphUtility.createFullSceneGraphComponent("superworld");
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		lemniscate1SGC = SceneGraphUtility.createFullSceneGraphComponent("lem1");
		lem1FramesSGC = SceneGraphUtility.createFullSceneGraphComponent("lem1");
		lemniscate2SGC = SceneGraphUtility.createFullSceneGraphComponent("lem2");
		surfaceSGC = SceneGraphUtility.createFullSceneGraphComponent("surface");
		linesSGC = SceneGraphUtility.createFullSceneGraphComponent("lines");
		lineGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("lineGeom");
		stickGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("stickGeom");
		celestialSphereSGC = SceneGraphUtility.createFullSceneGraphComponent("celestialSphere");
		Appearance ap = lemniscate1SGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", stickColors[0] = new Color(55, 255, 50)); // earth
		ap.setAttribute(LINE_SHADER+"."+TUBES_DRAW, true);
		double tubeR = .01;
		ap.setAttribute(LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, tubeR); 
		ap.setAttribute(VERTEX_DRAW, false);
		ap = lemniscate2SGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", stickColors[1] = new Color(255, 55,50));
		ap.setAttribute(LINE_SHADER+"."+TUBES_DRAW, true);
		ap.setAttribute(LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, tubeR); 
		ap.setAttribute(VERTEX_DRAW, false);
		linesSGC.setVisible(false);
		ap = linesSGC.getAppearance();
		ap.setAttribute(LINE_SHADER+"."+TUBES_DRAW, false);
		ap.setAttribute(LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, tubeR); 
		ap.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, 
				new Color(255, 255, 0));
//		velocityCurveSGC.setGeometry(velMesh.getMesh());
		ap.setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		linesSGC.addChild(geomCollector);
		ap = surfaceSGC.getAppearance();
		ap.setAttribute(VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute("polygonShader.diffuseColor", Color.white);
		ap.setAttribute("polygonShader.ambientCoefficient", .2);
		surfaceSGC.setGeometry(surface.getGeometry());
		surfaceSGC.setVisible(showSurface);
		
		Texture2D tex2d = null;
		tex2d = (Texture2D) AttributeEntityUtility.createAttributeEntity(
				Texture2D.class, "polygonShader.texture2d", ap, true);
		tex2d.setMinFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
		tex2d.setMagFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
		tex2d.setMipmapMode(true);
		// the following doesn't work -- rgba switches to agbr.  But it doesn't seem to matter
//		tex2d.setAnimated(true);
		Color transpblack = new Color(0,0,0,0),
				yellow = new Color(255, 255, 72);
		SimpleTextureFactory oloidTextureFactory = new SimpleTextureFactory();

		oloidTextureFactory.setType(SimpleTextureFactory.TextureType.GRAPH_PAPER); // LINE); //
		oloidTextureFactory.setColor(0, new Color(255, 255, 255, 0));
		oloidTextureFactory.setColor(0, transpblack);
		oloidTextureFactory.setColor(1, yellow);
		oloidTextureFactory.setColor(2, new Color(155,255,255));
		oloidTextureFactory.setColor(3,  yellow);
		oloidTextureFactory.setSize(512);
		oloidTextureFactory.setAppearance(ap);
		oloidTextureFactory.update();
		tex2d.setImage(oloidTextureFactory.getImageData());
		Matrix m = new Matrix();
		MatrixBuilder.euclidean().scale(5, 25, 1).assignTo(m);
		tex2d.setTextureMatrix(m);

		ap = stickGeomSGC.getAppearance();
		ap.setAttribute(LINE_SHADER+"."+
				POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.yellow);
		ap.setAttribute(VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.SPHERES_DRAW, true);
		ap.setAttribute(CommonAttributes.POINT_RADIUS, .04);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);

		lineGeomSGC.setAppearance(linesSGC.getAppearance());
		lineGeomSGC.setGeometry(lineGeom.getLine());
		lineGeom.setFiniteSphere(true);
		lineGeom.setSphereRadius(50);
		lineGeom.setNumberOfSamples(2);	
		lineGeom.getLine().setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		
		// create two different resolutions of (theta,phi) patches convering whole sphere
		sphericalPatch = SphereUtility.sphericalPatch(0.0, 0.0, 360.0, 179.999, 180, 90, 1.0);
		celestialSphereSGC.setGeometry(sphericalPatch);
		
		ap = celestialSphereSGC.getAppearance();
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		MatrixBuilder.euclidean().scale(100).assignTo(celestialSphereSGC);
		sphericalPatch.setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		
		ap.setAttribute("polygonShader.diffuseColor",Color.white );

		ap = world.getAppearance();
		ap.setAttribute("lineShader.lineWidth", 1.0);
		ap.setAttribute("lineShader.polygonShader."+CommonAttributes.AMBIENT_COEFFICIENT, .1);
//		ap.setAttribute(LIGHTING_ENABLED, false);
		ap.setAttribute(VERTEX_DRAW, false);
		// tubes are broken due to the fact we're doing a lot at the line at infinity
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(BOUNDING_BOX, Rectangle3D.unitCube);
		ap.setAttribute("lineShader.diffuseColor", new Color(0, 50,50));
		SceneGraphComponent coordSys = SceneGraphUtility.createFullSceneGraphComponent("coord sys");
		MatrixBuilder.euclidean().rotateX(-Math.toRadians(23.5)).rotateZ(Math.PI/2).assignTo(coordSys);
		lemniscate1SGC.addChild(lem1FramesSGC);
		coordSys.addChild(fixSGC);
		fixSGC.getAppearance().setAttribute(SceneGraphAnimator.LOCAL_ANIMATED, false);
		fixSGC.addChildren(lemniscate1SGC, lemniscate2SGC, linesSGC, surfaceSGC, lineGeomSGC, stickGeomSGC);
		world.addChild(coordSys);
	if (debug)	{
		lemniscate2SGC.setVisible(false);
		surfaceSGC.setVisible(false);
		
	}
		setupStick();
		update();
		return world;
	}
	
	private transient double margin = .05, repeat = 2.0;
	@Override
	public void startAnimation() {
		// TODO Auto-generated method stub
		super.startAnimation();
	}
	
	public void setupStick()	{
		// set up labels 
		Appearance ap = stickGeomSGC.getAppearance();
	    DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, false);
	    DefaultTextShader pts = (DefaultTextShader) ((DefaultPointShader)dgs.getPointShader()).getTextShader();
	    
	    pts.setDiffuseColor(Color.white);
	    Double scale = new Double(0.002);
	    pts.setScale(scale);
	    double[] offset = new double[]{0,0,0.0};
	    pts.setOffset(offset);
	    pts.setAlignment(SwingConstants.NORTH_WEST);
	    
	    // setup geometry
		double[] P1 = {0, 0,0,1 };
		double[] P2 = {1,0,0,1 };
		stick = IndexedLineSetUtility.createCurveFactoryFromPoints(
				null, new double[][]{P1, P2}, false);
		stickGeomSGC.setGeometry(stick.getGeometry());
		stick.setVertexColors(stickColors);
		stick.setVertexLabels(new String[]{"E","S"});
		stick.update();
		setValueAtTime(0.0);
//	    Font f = new Font("Arial Bold", Font.ITALIC, 24);
//	    pts.setFont(f);

	}
	@Override
	public void setValueAtTime(double d) {
		double angle = AnimationUtility.linearInterpolation(d, margin, 1.0-margin, 0, repeat*2*Math.PI),
		
		c = Math.cos(angle-Math.PI/2),
		s = Math.sin(angle-Math.PI/2),
		s2 = .5 * Math.sin(2*angle);
		double[] curvepoint = {0, s,s2,1 };
		double[] curvepoint2 = {c, 0, s2,1 };
		stick.setVertexCoordinates(new double[][]{curvepoint,curvepoint2});
		stick.update();
		
		lineGeom.setElement0(curvepoint);
		lineGeom.setElement1(curvepoint2);
		lineGeom.update();
		
		if (fixSun || fixEarth)	{
			Matrix m = MatrixBuilder.euclidean().translate(fixSun ? curvepoint : curvepoint2).getMatrix();
			m.invert();
			m.assignTo(fixSGC);
		}

	}


	private transient double[][] basis = {{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}};
	
	private void update() {
		
		geomCollector.reset();
		pc1.reset();
		pc2.reset();
		double[][] lem1 = new double[numPoints][], lem2 = new double[numPoints][];
		double[][][] surfP = new double[numPoints][2][];
		for (int i = 0; i<numPoints; ++i)	{
			double angle = (2* Math.PI *i)/ (numPoints-1.0),
					c = Math.cos(angle),
					s = Math.sin(angle),
					s2 = .5 * Math.sin(2*angle);
			double[] curvepoint = {0, s,s2,1 };
			lem1[i] = curvepoint;
			double[] curvepoint2 = {c, 0, s2,1 };
			lem2[i] = (curvepoint2);
			surfP[i][0] = curvepoint;
			surfP[i][1] = curvepoint2;
			// add line joining these two points
			PointRangeFactory localVelFactory = new PointRangeFactory();
			localVelFactory.setFiniteSphere(false);
			localVelFactory.setSphereRadius(20);
			localVelFactory.setNumberOfSamples(12);	
			localVelFactory.getLine().setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);			
			localVelFactory.setElement0(curvepoint);
			localVelFactory.setElement1(curvepoint2);
			localVelFactory.update();
			geomCollector.addGeometry(localVelFactory.getLine());

		}
		surface.setClosedInUDirection(false);
		surface.setClosedInVDirection(true);
		surface.setULineCount(2);
		surface.setVLineCount(numPoints);
		surface.setVertexCoordinates(surfP);
		surface.setGenerateFaceNormals(true);
		surface.setGenerateTextureCoordinates(true);
		surface.update();
//		IndexedLineSet lem1Curve = IndexedLineSetUtility.createCurveFromPoints(lem1, true);
//		TubeFactory tb = new TubeFactory(lem1);
//		tb.setClosed(true);
//		tb.setFrameFieldType(FrameFieldType.PARALLEL);
//		tb.setMetric(Pn.EUCLIDEAN);
//		lem1FramesSGC.removeAllChildren();
//		lem1FramesSGC.addChild(TubeFactory.getSceneGraphRepresentation(
//				tb.makeFrameField(lem1, FrameFieldType.PARALLEL, Pn.EUCLIDEAN), .1));
//		lemniscate1SGC.setGeometry(lem1Curve);
		lemniscate1SGC.setGeometry(IndexedLineSetUtility.createCurveFromPoints(lem1, true));
		lemniscate2SGC.setGeometry(IndexedLineSetUtility.createCurveFromPoints(lem2, true));
		linesSGC.setGeometry(geomCollector.getGeometry());
	}
	
	Transformation constellT=new Transformation();
	@Override
	public void display() {
		super.display();
//		setup the constellation map
		ImageData[] cm = new ImageData[6];
		String[] faces = {"rt","lf","up","dn","bk","ft"};
		for (int i = 0; i<6; ++i)	{
			try {
				cm[i] =ImageData.load(Input.getInput(
						"src/charlesgunn/jreality/resources/cubemapConstell2/foo."+faces[i]+".png"));			
				System.err.println("star map face "+i+" loaded "+cm[i].getWidth());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		constellationMap = TextureUtility.createSkyBox(jrviewer.getViewer().getSceneRoot().getAppearance(), cm);
		constellationMap.setTransformation(constellT);
		List ll = SceneGraphUtility.getPathsToNamedNodes(jrviewer.getViewer().getSceneRoot(), "world");
		final SceneGraphPath sgp = (SceneGraphPath) (ll.get(0));
		SceneGraphPathObserver sgpo = new SceneGraphPathObserver();
		sgpo.setPath(sgp);
		final double[] tform = new double[16];
		// force the star map to mimic the rotational part of the root to world tform 
		sgpo.addTransformationListener(new TransformationListener() {
			
			@Override
			public void transformationMatrixChanged(TransformationEvent ev) {
				sgp.getMatrix(tform);
				tform[3]=tform[7]=tform[11] = 0.0;	// extract rotation part
				constellT.setMatrix(tform);
			}
		});
		// Point the build in camera to look in the right direction
		ll = SceneGraphUtility.getPathsToNamedNodes(jrviewer.getViewer().getSceneRoot(), "avatar trafo");
		SceneGraphPath sgp2 = (SceneGraphPath) (ll.get(0));
		System.err.println("setting trafo at node "+sgp2.getLastComponent().getName());
		MatrixBuilder.euclidean().rotateZ(-Math.PI/2).rotateX(-Math.PI/2).assignTo(sgp2.getLastComponent());
		CameraUtility.encompass(jrviewer.getViewer());
		jrviewer.getViewer().renderAsync();
		Camera cam = CameraUtility.getCamera(jrviewer.getViewer());
		cam.setFar(100.0);
		
		animationPlugin.getAnimationPanel().setResourceDir("src/charlesgunn/jreality/worlds/projective/");
		animationPlugin.setAnimateSceneGraph(true);
//		final Color URBackground = new Color(.8f, .85f, .68f); //new Color(215, 215, 190);
//		final Color ULBackground  = new Color(1f, .98f, .8f); //new Color(255, 255, 200);  // bg[1];
//		final Color LLBackground  = new Color(.1f, .1f, .25f); //new Color(20,20,60);
//		final Color LRBackground  = new Color(0.05f, .15f, .35f); //new Color(25, 25, 100);  //bg[2];
//		Color[] backgroundArray = new Color[4];
//		backgroundArray[0] = URBackground;
//		backgroundArray[1] = ULBackground;// bg[1];
//		backgroundArray[2] = LLBackground;
//		backgroundArray[3] = LRBackground;  //bg[2];
//		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute("backgroundColors", backgroundArray);
////	jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.white);		
//		Camera cam = CameraUtility.getCamera(jrviewer.getViewer());
//		cam.setFieldOfView(1.25 * cam.getFieldOfView());
//		cam.setPerspective(false);
		((Component) jrviewer.getViewer().getViewingComponent()).addKeyListener( new KeyAdapter()	{
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode())	{
				
				case KeyEvent.VK_H:
					System.err.println(" 1: toggle animate");
					break;
				case KeyEvent.VK_1:
					surfaceSGC.setVisible(!surfaceSGC.isVisible());
					break;
				case KeyEvent.VK_2:
					linesSGC.setVisible(!linesSGC.isVisible());
					break;
				}
			}	
			
		});	
		
	}

	

	@Override
	public Component getInspector() {
		Box inspectionPanel =  Box.createVerticalBox();
		Box hbox = Box.createHorizontalBox();
		inspectionPanel.add(hbox);
		
		final JCheckBox fixSunB = new JCheckBox("Fix Sun");
		hbox.add(fixSunB);
		fixSunB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				fixSun =  fixSunB.isSelected();
				update();
			}
		});
		
		final JCheckBox fixEarthB = new JCheckBox("Fix Earth");
		hbox.add(fixEarthB);
		fixEarthB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				fixEarth =  fixEarthB.isSelected();
				update();
			}
		});
		
		final JCheckBox showSurfaceB = new JCheckBox("Show surface");
		hbox.add(showSurfaceB);
		showSurfaceB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showSurface =  showSurfaceB.isSelected();
			}
		});
		
		final TextSlider<Integer> nSlider = new TextSlider.Integer("num",SwingConstants.HORIZONTAL, 1, 500, numPoints);
		nSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e)	{
				numPoints = nSlider.getValue().intValue();
				update();
			}
		});
		inspectionPanel.add(nSlider);
//		final TextSlider aSlider = new TextSlider.Double("a",SwingConstants.HORIZONTAL, -1.0, 1.0, a);
//		aSlider.addActionListener(new ActionListener()	{
//			public void actionPerformed(ActionEvent e)	{
//				a = aSlider.getValue().doubleValue();
//				updateConics();
//			}
//		});
//		inspectionPanel.add(aSlider);
//		final TextSlider bSlider = new TextSlider.Double("b",SwingConstants.HORIZONTAL, -1.0, 1.0, b);
//		bSlider.addActionListener(new ActionListener()	{
//			public void actionPerformed(ActionEvent e)	{
//				b = bSlider.getValue().doubleValue();
//				updateConics();
//			}
//		});
//		inspectionPanel.add(bSlider);
//		final TextSlider cSlider = new TextSlider.Double("t",SwingConstants.HORIZONTAL, -1.0, 1.0, c);
//		cSlider.addActionListener(new ActionListener()	{
//			public void actionPerformed(ActionEvent e)	{
//				c = cSlider.getValue().doubleValue();
//				updateConics();
//			}
//		});
		inspectionPanel.add(nSlider);
		inspector.add(inspectionPanel);
		return inspector;
	}

	public static void main(String[] args) {
		new LemniscateSunEarth().display();
	}
}
