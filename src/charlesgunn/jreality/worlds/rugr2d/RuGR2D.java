/*
 * Created on 25.09.2018
 *
 */
package charlesgunn.jreality.worlds.rugr2d;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import charlesgunn.anim.core.Animated;
import charlesgunn.anim.core.KeyFrameAnimatedDelegate;
import charlesgunn.anim.core.KeyFrameAnimatedDouble;
import charlesgunn.anim.jreality.SceneGraphAnimator;
import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.geometry.InterpolatedILS;
import charlesgunn.jreality.geometry.projective.DualizeSceneGraph;
import charlesgunn.jreality.geometry.projective.LineUtility;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.p5.PlueckerLineGeometry;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class RuGR2D extends Assignment {

	private transient SceneGraphComponent world,
	eucSGC,
		pointCircleSGC,
			triangleSGC,
		    onepointCircleSGC,
		insideSGC,
		    inside2SGC,
		diameterSGC,
		centerSGC,
		diskSGC,
	polarSGC,
		lineCircleSGC,
		polarTriangleSGC,
		pinsideSGC,
		pcenterSGC,
		pdiameterSGC,
		    rrtracksSGC,
	starSGC,
	extraStuff,
	arrowSGC;
	private transient int num = 100,
			numrr = 400,
			phase = -2;		// -2,-1: degenerate motion 
							//	 0: generate circle,  1: generate center,  2: show inside, 3: outside
							//   4: contraction/expansion	
	private transient double time = 0, radius = 1.0;
	private transient boolean showCenter = false,
			showPCenter = false,
			showPointCircle = false,
			showLineCircle = true,
			showInside = false,
			showPInside = true,
			showStar = false,
			isPerspective = false,
			showRRTracks = false,
			showSurfaces = false,
			completeCircle = false, // keep the two circles together, the point circle is the "boss"
			doTriangle = false;
	private transient Color diskColor = new Color(192,192,192);

	private transient PointSetFactory psf = new PointSetFactory(), psf2 = new PointSetFactory();
	private transient InterpolatedILS triangleILS;
    private transient double[] point = new double[]{1,0,0,1};
	private transient double[] line = PlueckerLineGeometry.lineFromPoints(null, point, new double[]{1,0,0,0});
	private transient double weirdZClipForLaI = 10000.0;
	private transient double scale = Math.sqrt(2.0);
	private transient double[][] triVerts = 
			{{scale*Math.cos(-Math.PI/6), scale*Math.sin(-Math.PI/6), 0,1},
			{scale*Math.cos(Math.PI/2), scale*Math.sin(Math.PI/2), 0,1},
			{scale*Math.cos(7*Math.PI/6), scale*Math.sin(7*Math.PI/6), 0,1}};
	private transient Color[] colors = {Color.orange, Color.cyan, Color.magenta, Color.orange};
	private transient Color[] colors3 = {Color.green, Color.red, Color.blue}; //{Color.orange, Color.cyan, Color.magenta};
	private transient TextSlider scalerSlider;

	@Override
	public SceneGraphComponent getContent() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		eucSGC = SceneGraphUtility.createFullSceneGraphComponent("euc");
		diameterSGC = SceneGraphUtility.createFullSceneGraphComponent("diameter");
		centerSGC = SceneGraphUtility.createFullSceneGraphComponent("center");
		pcenterSGC = SceneGraphUtility.createFullSceneGraphComponent("pcenter");
		pdiameterSGC = SceneGraphUtility.createFullSceneGraphComponent("pdiameter");
		rrtracksSGC = SceneGraphUtility.createFullSceneGraphComponent("rrtracks");
		polarSGC = SceneGraphUtility.createFullSceneGraphComponent("polar");
		pointCircleSGC = SceneGraphUtility.createFullSceneGraphComponent("point");
		triangleSGC = SceneGraphUtility.createFullSceneGraphComponent("triangle");
		polarTriangleSGC = SceneGraphUtility.createFullSceneGraphComponent("polar triangle");
		onepointCircleSGC = SceneGraphUtility.createFullSceneGraphComponent("onepoint");
		insideSGC = SceneGraphUtility.createFullSceneGraphComponent("inside");
		inside2SGC = SceneGraphUtility.createFullSceneGraphComponent("inside2");
		diskSGC = SceneGraphUtility.createFullSceneGraphComponent("disk");
		pinsideSGC = SceneGraphUtility.createFullSceneGraphComponent("pinside");
		lineCircleSGC = SceneGraphUtility.createFullSceneGraphComponent("ppoint");
		starSGC = SceneGraphUtility.createFullSceneGraphComponent("star");
		extraStuff = SceneGraphUtility.createFullSceneGraphComponent("extra stuff");

		world.addChildren(eucSGC, polarSGC, extraStuff);
		eucSGC.addChildren(pointCircleSGC, insideSGC, diameterSGC, centerSGC);
//		insideSGC.addChild(centerSGC);
//		insideSGC.setAppearance(pointCircleSGC.getAppearance());
//		pinsideSGC.setAppearance(lineCircleSGC.getAppearance());
		polarSGC.addChildren(lineCircleSGC, pinsideSGC, pdiameterSGC, pcenterSGC,rrtracksSGC, starSGC);
		
		Appearance ap = pointCircleSGC.getAppearance();
		ap.setAttribute("pointShader.diffuseColor", new Color(255,255,204));
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
//		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .006);
		if (doTriangle) {
			ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, .03);
			ap.setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.unitCube);			
		}
		if (phase == -2) {
			ap = onepointCircleSGC.getAppearance();
			ap.setAttribute("lineShader.diffuseColor", new Color(255,255,204));
			ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
			ap.setAttribute(CommonAttributes.EDGE_DRAW, true);		
		} else onepointCircleSGC.setAppearance(pointCircleSGC.getAppearance());
		
		ap = insideSGC.getAppearance();
		inside2SGC.setAppearance(ap);
		ap.setAttribute("pointShader.diffuseColor", new Color(255,255,204));
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		pointCircleSGC.setGeometry(psf.getGeometry());
		
		double tradius = Math.exp(radius);
		insideSGC.setGeometry(IndexedLineSetUtility.circle(num, 0, 0, tradius));
		insideSGC.getAppearance().setAttribute(DualizeSceneGraph.DUALIZE_LINES, false);
		if (phase != 1)	{ // hack to force all lines at once
			SceneGraphComponent poop = new SceneGraphComponent();
			poop.setGeometry(IndexedLineSetUtility.circle(num, 0, 0, 1.0));
			poop.setAppearance(pointCircleSGC.getAppearance());
			poop.getAppearance().setAttribute(DualizeSceneGraph.DUALIZE_LINES, false);
			SceneGraphComponent dualize = DualizeSceneGraph.dualize(poop);
			dualize.getChildComponent(0).setVisible(true);
			lineCircleSGC.addChild(dualize);
		}
		generateRRTracks(rrtracksSGC, numrr);

		double[][] dverts = {{1,0,0,1},{-1,0,0,1}};
		IndexedLineSetFactory diamfac = IndexedLineSetUtility.createCurveFactoryFromPoints(dverts, false);
		diameterSGC.setGeometry(diamfac.getGeometry());
		ap = diameterSGC.getAppearance();
		ap.setAttribute("pointShader.diffuseColor", Color.red);
		ap.setAttribute(SceneGraphAnimator.ANIMATED, false);

		ap.setAttribute("lineShader.diffuseColor", Color.cyan);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, isPerspective ? .016 : .01);
		ap.setAttribute("lineShader.polygonShader."+CommonAttributes.AMBIENT_COEFFICIENT, .2);
		ap.setAttribute("lineShader.polygonShader."+CommonAttributes.AMBIENT_COLOR, Color.red);
//		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, (isPerspective && showSurfaces) ? false : true);

		ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, .02);
				
		// scene graph for center point and possible arrows 
		SceneGraphComponent pcenterTurn = new SceneGraphComponent("pcenterTurn");
		SceneGraphComponent pcenterNoTurn = new SceneGraphComponent("pcenterNoTurn");
		pcenterSGC.addChildren(pcenterTurn, pcenterNoTurn);
		pcenterSGC.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.unitCube);
		MatrixBuilder.euclidean().rotateZ(Math.PI).assignTo(pcenterTurn);
		SceneGraphComponent child = new SceneGraphComponent("child1");
		pcenterNoTurn.addChild(child);
		pcenterTurn.addChild(child);
		child.setGeometry(Primitives.arrow(0,0,0,.8,.2));
		child.setVisible(!isPerspective);
		arrowSGC = child;
		MatrixBuilder.euclidean().translate(0,1.0,0).scale(.3).assignTo(child);
		child = new SceneGraphComponent("child2");
		ap = new Appearance();
		child.setAppearance(ap);
		pcenterNoTurn.addChild(child);
		pcenterTurn.addChild(child);
		// place it at a finite but far away point
		child.setGeometry(Primitives.point(new double[]{0,1,0,1.0/weirdZClipForLaI}));
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED,false);
		ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW, false);
		ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_SIZE, 16.0);
				
		ap = pcenterSGC.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(SceneGraphAnimator.ANIMATED, false);
		ap.setAttribute("lineShader.diffuseColor", Color.cyan);
		ap.setAttribute("pointShader.diffuseColor", Color.cyan);
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .03);
		ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, .03);
		ap.setAttribute("pointShader."+CommonAttributes.POINT_SIZE, 12.0);

		centerSGC.setGeometry(Primitives.point(P3.originP3));
		ap = centerSGC.getAppearance();
		ap.setAttribute("pointShader.diffuseColor", Color.YELLOW);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);

		ap = polarSGC.getAppearance();
		ap.setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		if (doTriangle) {
			ap.setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, true);
			ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .01);
			ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
			IndexedLineSetFactory ilsf = IndexedLineSetUtility.createCurveFactoryFromPoints(triVerts, true);
			triangleILS = new InterpolatedILS(ilsf.getIndexedLineSet());
			triangleSGC.setGeometry(ilsf.getGeometry());
			ilsf.setVertexColors(colors3);
			ilsf.update();
			
			MatrixBuilder.euclidean().rotateZ(Math.PI).assignTo(polarSGC);
			eucSGC.addChild(triangleSGC);
			polarSGC.addChild(polarTriangleSGC);
			ap = triangleSGC.getAppearance();
			ap.setAttribute(DualizeSceneGraph.DUALIZE_LINES, false);
			ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
			ap.setAttribute(CommonAttributes.SPHERES_DRAW, true);
			ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, .03);

			SceneGraphComponent dualize = DualizeSceneGraph.dualize(triangleSGC);
			polarTriangleSGC.addChild(dualize);
		}
		if (phase < 0)	{
			world.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.unitCube);
			if (phase == -1)
				MatrixBuilder.euclidean().rotateZ(Math.PI/2).assignTo(world);
		}
		if (phase >= 2) 	{		// fade out the intensity of the fixed line circle when it's animating
			ap = lineCircleSGC.getAppearance();
			ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
			ap.setAttribute("lineShader.polygonShader."+CommonAttributes.TRANSPARENCY, .65);
			ap = pinsideSGC.getAppearance();
			ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		}
		
		starSGC.setGeometry(GeometryUtilityOverflow.starPoint(32, .01, .03));
		MatrixBuilder.euclidean().scale(1.5).assignTo(starSGC);
		ap = starSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", Color.white);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute("lineShader.lineWidth", 2.0);
		starSGC.setVisible(showStar);
		world.addChild(starSGC);

		ap = world.getAppearance();
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, isPerspective);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS,  isPerspective ? .005 : .005);
		ap.setAttribute("lineShader.polygonShader."+CommonAttributes.AMBIENT_COEFFICIENT, .1);
		ap.setAttribute(CommonAttributes.SPHERES_DRAW, true);
		ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, .02);
		
		extraStuff.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.unitCube);			
		if (!showSurfaces) extraStuff.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, true);
		
		SceneGraphComponent linf = new SceneGraphComponent();
		linf.setAppearance(new Appearance());
		linf.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		linf.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		linf.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, 4.0 * CommonAttributes.LINE_WIDTH_DEFAULT);
		linf.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+"diffuseColor",Color.YELLOW);
		linf.setGeometry(PointRangeFactory.line( new double[]{1,0,0,0}, new double[]{0,1,0,0}));
		extraStuff.addChild(linf);
		linf = new SceneGraphComponent();
		MatrixBuilder.euclidean().scale(2.0, 1, 1).assignTo(linf);
		Appearance newApp = new Appearance();
		linf.setAppearance(newApp);
		linf.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		linf.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
//		linf.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, true);
		linf.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		linf.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+"diffuseColor",new Color(153,130,226));
		// place the sky slightly behind the line at infinity
		double w = -.5/weirdZClipForLaI;
		Geometry foo = Primitives.texturedQuadrilateral(new double[]{
				1,0,0,w,
				0,1,0,w,
				0,1,100,w,
				1,0,100,w
		});
		// have to repeat it to get the full extent covered
		linf.setGeometry(foo);
		linf.setVisible(!!showSurfaces);
		extraStuff.addChild(linf);
		linf = new SceneGraphComponent();
		linf.setGeometry(foo);
		linf.setAppearance(newApp);
		linf.setVisible(showSurfaces);
		MatrixBuilder.euclidean().rotateZ(Math.PI/2).scale(2.0, 1, 1).assignTo(linf);
		extraStuff.addChild(linf);
//			linf = new SceneGraphComponent();
		diskSGC.setGeometry(Primitives.regularPolygon(num));
		diskSGC.setVisible(showSurfaces);
		newApp = diskSGC.getAppearance();
		newApp.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"diffuseColor",diskColor);
		newApp.setAttribute(CommonAttributes.EDGE_DRAW, false);
		newApp.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		extraStuff.addChild(diskSGC);	

		time = 1.0;
		update();

		update();
		world.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.unitCube);
		world.getAppearance().setAttribute(SceneGraphAnimator.ANIMATED, false);

		pointCircleSGC.setVisible(showPointCircle);
		lineCircleSGC.setVisible(showLineCircle);
		centerSGC.setVisible(showCenter);
		diameterSGC.setVisible(showCenter);
		pcenterSGC.setVisible(showPCenter);
		pdiameterSGC.setVisible(showPCenter);
		arrowSGC.setVisible(!isPerspective);
		insideSGC.setVisible(showInside);
		pinsideSGC.setVisible(showPInside);
		starSGC.setVisible(showStar);
		rrtracksSGC.setVisible(showRRTracks);
		

		return world;
	}


	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(0,0,0,0));
		animationPlugin.getAnimationPanel().setResourceDir("src/charlesgunn/jreality/worlds/rugr2d/");
		animationPlugin.getAnimationPanel().getRecordPrefs().setCurrentDirectoryPath("/Volumes/SamsungSSD1T/gunn_local/Movies/RuGR/");
		animationPlugin.setAnimateSceneGraph(!doTriangle);
		animationPlugin.setAnimateCamera(true);
		List<Animated> animated = animationPlugin.getAnimated();
		
		KeyFrameAnimatedDelegate<Double> dd = new KeyFrameAnimatedDelegate<Double> () {

			public void propagateCurrentValue(Double t) {
				radius = (t);
				scalerSlider.setValue(t);
				update();
				jrviewer.getViewer().renderAsync();
			}

			public Double gatherCurrentValue(Double t) {
				return (radius);
			}
			
		};
		KeyFrameAnimatedDouble animAlpha = new KeyFrameAnimatedDouble(dd );
		animAlpha.setName("animScaler");
		animated.add(animAlpha);
		
		Component comp = ((Component) jrviewer.getViewer().getViewingComponent());
		comp.addKeyListener(new KeyAdapter() {
 				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
						
					case KeyEvent.VK_8:
						CameraUtility.getCamera(jrviewer.getViewer()).setFar(-weirdZClipForLaI);

						break;
		
 				}
 				}
		});

	}	

	@Override
	public void setValueAtTime(double d) {
		// TODO Auto-generated method stub
		super.setValueAtTime(d);
		time = d;
		update();
	}

	@Override
	public void startAnimation() {
		// TODO Auto-generated method stub
		super.startAnimation();
		
	}

	private void update() {
		boolean newSG = doTriangle ? generateTriangle() : generateCircles(time, psf); 
		double dtime = time;
		if (phase == 1)	{  // center animation
			dtime = AnimationUtility.linearInterpolation(time, 0.5, 1.5, 0.0, 2.0);
			MatrixBuilder.euclidean().rotateZ(Math.PI  * dtime).assignTo(diameterSGC);
//			MatrixBuilder.euclidean().rotateZ(Math.PI  * dtime).assignTo(pdiameterSGC);
			MatrixBuilder.euclidean().rotateZ(Math.PI  * dtime).assignTo(pcenterSGC);
		}
		else if (phase > 1)	{   // inside/outside
//			double radius = time;
//			if (phase == 3)	{   // outside the circle
//				radius = AnimationUtility.linearInterpolation(time, 0.05, .95, 1, scaler);
//				radius = 1.0/radius;
//			}
			double tradius = Math.exp(radius);
			insideSGC.setGeometry(IndexedLineSetUtility.circle(num, 0, 0, tradius));
			MatrixBuilder.euclidean().scale(tradius).assignTo(diskSGC);
			insideSGC.getAppearance().setAttribute(DualizeSceneGraph.DUALIZE_LINES, false);
			pinsideSGC.removeAllChildren();
			SceneGraphComponent dualize = null;
			if (completeCircle) {
				inside2SGC.setGeometry(IndexedLineSetUtility.circle(num, 0, 0, 1.0/tradius));
				dualize = DualizeSceneGraph.dualize(inside2SGC);
			}
			else
				dualize = DualizeSceneGraph.dualize(insideSGC);			
			pinsideSGC.addChild(dualize);
			dualize.getChildComponent(0).setVisible(true);	
//			}
		} 
//		if (!newSG) {
//			updateVisibility();
//			return;
//		}
//		diameterSGC.setVisible(time >= 0);
		if (phase <= 1) {
			lineCircleSGC.removeAllChildren();
			pdiameterSGC.removeAllChildren();		
		}
		if (phase == -2)	{
			LineUtility.sceneGraphForLine(onepointCircleSGC, line, point, 50);
			lineCircleSGC.addChild(onepointCircleSGC);
		} else if (phase == -1)	{
			onepointCircleSGC.setGeometry(Primitives.point(new double[]{0,0,0,1}));
			SceneGraphComponent dualize = DualizeSceneGraph.dualize(pointCircleSGC);
			lineCircleSGC.addChildren(dualize, onepointCircleSGC);
		} else if (phase == 1 || phase == 0){    // running through the circle
			SceneGraphComponent dualize = DualizeSceneGraph.dualize(pointCircleSGC);
			lineCircleSGC.addChild(dualize);
			dualize.getChildComponent(0).setVisible(true);
			boolean foo = diameterSGC.isVisible();
			diameterSGC.setVisible(true);
			pdiameterSGC.addChild(DualizeSceneGraph.dualize(diameterSGC));
			diameterSGC.setVisible(foo);
			pdiameterSGC.setVisible(showPCenter);			
		}
//		updateVisibility();
	}

	private transient double oldtime = -1.0;
	// generate the geometry
	private boolean generateCircles(double time, PointSetFactory psf) {
		double localtime = ( phase < 1) ? time : 1.0;
		if (oldtime == localtime) return false;
		int num2 = (int) ((phase == -1 ? .5 : 1) * num);
		int limit = 1+((int) (localtime * num2));
		double[][] verts = new double[limit+((time == 1.0) ? 0 : 1)][4];
		double angle = 0, delta = Math.PI * (phase == -1 ? 1 : 2) / (num2);
		for (int i = 0; i<limit; ++i) {
			angle = i * delta;
			if (phase == -2)	{
				verts[i][0] = angle;
				verts[i][1] = 0;
			}  else {
				verts[i][0] = Math.cos(angle);
				verts[i][1] = Math.sin(angle);
			}
			verts[i][2] = 0.0;
			verts[i][3] = phase == -1 ? 0.0 : 1.0;
		}
		angle = Math.PI * (phase == -1 ? 1 : 2) * time;
		if (time != 1.0) 
			if (phase == -2)	{
				verts[limit] = new double[]{angle, 0, 0, 1};
			} else {
				verts[limit] = new double[]{Math.cos(angle), Math.sin(angle), 0, phase == -1 ? 0.0 : 1.0};				
			}
		psf.setVertexCount(verts.length);
		psf.setVertexCoordinates(verts);
		psf.update();
		oldtime = localtime;
		return true;
	}

	
	private boolean generateTriangle() {
		double localtime = phase == 0 ? time : 1.0;
		if (oldtime == localtime) return false;
		int limit = (int) (localtime * num);
		double[][] verts = new double[limit+((time == 1.0) ? 0 : 1)][4];
		Color[] cv = new Color[verts.length];
		double angle = 0, delta = 1.0 / (num-1.0);
		for (int i = 0; i<limit; ++i) {
			angle = i * delta;
			verts[i] = triangleILS.getInterpolatedVertex(angle);
			int j = ((angle < 1.0/3.0) ? 0 : (angle < 2.0/3.0) ? 1 : 2);
			double p = 3 * (angle - j/3.0);
			if (p > 1) p = 1;
			if (p < 0) p = 0;
			cv[i] = AnimationUtility.linearInterpolation(colors[j], colors[j+1], p);
//			verts[i][0] = Math.cos(angle);
//			verts[i][1] = Math.sin(angle);
//			verts[i][2] = 0.0;
//			verts[i][3] = 1.0;
		}
		angle = time;
		if (time != 1.0) {
			verts[limit] = triangleILS.getInterpolatedVertex(angle);
			if (limit > 0) cv[limit] = cv[limit-1];
			else cv[limit] = colors[0];
		}
		psf.setVertexCount(verts.length);
		psf.setVertexCoordinates(verts);
		psf.setVertexColors(cv);
		psf.update();
		oldtime = localtime;
		return true;
	}

	private SceneGraphComponent generateRRTracks(SceneGraphComponent sgc, int num) {
		SceneGraphComponent leftTrack = SceneGraphUtility.createFullSceneGraphComponent("left"), 
				rightTrack = SceneGraphUtility.createFullSceneGraphComponent("right"), 
				crossTie = SceneGraphUtility.createFullSceneGraphComponent("crosstie");
		Geometry box = Primitives.box(.1, .4, 1.0, false);
		leftTrack.setGeometry(box);
		rightTrack.setGeometry(box);
		Appearance ap = leftTrack.getAppearance();
		rightTrack.setAppearance(ap);
		ap.setAttribute("polygonShader.diffuseColor", Color.red);
		ap.setAttribute("polygonShader.ambientColor", Color.red);
		ap.setAttribute("polygonShader.ambientCoefficient", .1);
		MatrixBuilder.euclidean().translate(-1, .2, 0).assignTo(leftTrack);
		MatrixBuilder.euclidean().translate(1, .2, 0).assignTo(rightTrack);
		crossTie.setGeometry(Primitives.box(2.2, .05, .3, false));
		MatrixBuilder.euclidean().translate(0, 0,0).assignTo(crossTie);
		ap = crossTie.getAppearance();
		Color crossTieColor = new Color(180,120,60);
		Color crossTieEdgeColor = new Color(153,153,153);
		ap.setAttribute("polygonShader.diffuseColor", crossTieColor);
		ap.setAttribute("lineShader.diffuseColor", crossTieEdgeColor);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, true);
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .005);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		SceneGraphComponent oneCopy = SceneGraphUtility.createFullSceneGraphComponent("oneCopy");
		oneCopy.addChildren(leftTrack, rightTrack, crossTie);
		for (int i = 0; i<numrr; ++i) {
			SceneGraphComponent child = new SceneGraphComponent("child"+i);
			child.addChild(oneCopy);
			MatrixBuilder.euclidean().translate(0, 0, 10-i).assignTo(child);
			sgc.addChild(child);
		}
		
		MatrixBuilder.euclidean().rotateX(Math.PI/2).translate(0, .025, 0).assignTo(sgc);
		rrtracksSGC.setVisible(showRRTracks);
		ap = rrtracksSGC.getAppearance();
		ap.setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		
		return sgc;
	}

	@Override
	public Component getInspector() {
		// TODO Auto-generated method stub
		Box hbox = Box.createHorizontalBox();
		inspector.add(hbox);
		JCheckBox c1 = new JCheckBox("point O");
		c1.setSelected(showPointCircle);
		hbox.add(c1);

		c1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showPointCircle = !showPointCircle;
				pointCircleSGC.setVisible(showPointCircle);
			}
		});
		c1 = new JCheckBox("line O");
		hbox.add(c1);
		c1.setSelected(showLineCircle);
		c1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showLineCircle = !showLineCircle;
				lineCircleSGC.setVisible(showLineCircle);
			}
		});
		 c1 = new JCheckBox("point ctr");
		c1.setSelected(showCenter);
		hbox.add(c1);
		c1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showCenter = !showCenter;
				centerSGC.setVisible(showCenter);
			}
		});
		 c1 = new JCheckBox("point diam");
		c1.setSelected(true);
		hbox.add(c1);
		c1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				diameterSGC.setVisible(!diameterSGC.isVisible());
			}
		});
		c1 = new JCheckBox("line ctr");
		hbox.add(c1);
		c1.setSelected(showPCenter);
		c1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showPCenter = !showPCenter;
				pcenterSGC.setVisible(showPCenter);
			}
		});
		 c1 = new JCheckBox("line diam");
		c1.setSelected(true); //pdiameterSGC.isVisible());
		hbox.add(c1);
		c1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				pdiameterSGC.setVisible(!pdiameterSGC.isVisible());
			}
		});
		c1 = new JCheckBox("perspective");
		hbox.add(c1);
		c1.setSelected(isPerspective);
		c1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				isPerspective = !isPerspective;
				arrowSGC.setVisible(!isPerspective);
			}
		});
		hbox = Box.createHorizontalBox();
		inspector.add(hbox);
		
		c1 = new JCheckBox("showInside");
		hbox.add(c1);
		c1.setSelected(showInside);
		c1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showInside = !showInside;
				insideSGC.setVisible(showInside);
			}
		});
		 c1 = new JCheckBox("showPInside");
		c1.setSelected(showPInside);
		hbox.add(c1);
		c1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showPInside = !showPInside;
				pinsideSGC.setVisible(showPInside);
			}
		});
		c1 = new JCheckBox("showStar");
		hbox.add(c1);
		c1.setSelected(showStar);
		c1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showStar = !showStar;
				starSGC.setVisible(showStar);
			}
		});
		 c1 = new JCheckBox("show Surfaces");
		c1.setSelected(showSurfaces);
		hbox.add(c1);
		c1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showSurfaces = !showSurfaces;
				extraStuff.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, showSurfaces);
				update();	
			}
		});
		
		 c1 = new JCheckBox("complete circle");
		c1.setSelected(completeCircle);
		hbox.add(c1);
		c1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				completeCircle = !completeCircle;
				update();	
			}
		});
		c1 = new JCheckBox("rr");
		hbox.add(c1);
		c1.setSelected(showRRTracks);
		c1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showRRTracks = !showRRTracks;
				rrtracksSGC.setVisible(showRRTracks);
			}
		});
		
		final TextSlider rotateSlider  = new TextSlider.Integer("phase",SwingConstants.HORIZONTAL,-2, 6, phase);
		rotateSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				phase = rotateSlider.getValue().intValue();
				update();
			}
		});
		inspector.add(rotateSlider);
		scalerSlider= new TextSlider.Double("scale",SwingConstants.HORIZONTAL, Math.log(.01), Math.log(100.0), Math.log(radius));
		scalerSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				radius = scalerSlider.getValue().doubleValue();
				update();
			}
		});
		inspector.add(scalerSlider);

		return inspector;
	}

	public static void main(String[] args) {
		new RuGR2D().display();
	}

}
