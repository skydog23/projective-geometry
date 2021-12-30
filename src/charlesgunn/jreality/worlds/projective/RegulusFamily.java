package charlesgunn.jreality.worlds.projective;

import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.PICKABLE;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import charlesgunn.anim.core.Animated;
import charlesgunn.anim.core.KeyFrameAnimatedDelegate;
import charlesgunn.anim.core.KeyFrameAnimatedDouble;
import charlesgunn.anim.gui.AnimationPanel;
import charlesgunn.anim.gui.AnimationPanelListenerImpl;
import charlesgunn.anim.jreality.SceneGraphAnimator;
import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.anim.util.AnimationUtility.InterpolationTypes;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.geometry.projective.SkewQuad;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import charlesgunn.math.p5.PlueckerLineGeometry;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class RegulusFamily extends Assignment {

	double[][] points = {{1,1,1,1},{1,-1,-1,1},{-1,1,-1,1},{-1,-1,1,1}};
	double[][] parameterLines = {PlueckerLineGeometry.lineFromPoints(null, points[0], points[1]),
			PlueckerLineGeometry.lineFromPoints(null, points[2], points[3])};
	double[][] pointsl3 = new double[2][];
	PointSet[] pointsets = new PointSet[2];
	Color leftColor = Color.red, rightColor = Color.blue;
	double[][] rPlucker  = new double[3][], lPlucker = new double[3][];
	PointRangeFactory[] generatorFactories = new PointRangeFactory[5];
	int lineCount = 64;
	double sphereRadius = 10.0, 
		globalSphereRadius = 200.0;
	double time = 0.25;
	boolean showTetra = true,
		showSurface = false,
		showSkewQuad = true,
		movePointsTogether = false;
	private PointRangeFactory[] parameterLineF = new PointRangeFactory[2];
	private SkewQuad regulusFactory;
	private SceneGraphComponent 
		world,
			rSGC,		// the regelschar
			lSGC,		// the leitschar
			fourLines,	// the four skew lines of a skew quadrilateral
			l3p1SGC, 	// holds first movable point determining the third leit line
			l3p2SGC, 	// holds second movable point determining the third leit line
			thirdLeitLine, 
			tetraSGC,
			surfaceRep;
	@Override
	public SceneGraphComponent getContent() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		lSGC  = SceneGraphUtility.createFullSceneGraphComponent("leitschar");
		rSGC = SceneGraphUtility.createFullSceneGraphComponent("regelschar");
		fourLines = SceneGraphUtility.createFullSceneGraphComponent("four lines");
		world.addChildren(rSGC, lSGC, fourLines);
		thirdLeitLine = SceneGraphUtility.createFullSceneGraphComponent("third leit line");
		l3p1SGC = SceneGraphUtility.createFullSceneGraphComponent("p1");
		l3p2SGC = SceneGraphUtility.createFullSceneGraphComponent("p2");
		l3p1SGC.getAppearance().setAttribute(PICKABLE, false);
		l3p2SGC.getAppearance().setAttribute(PICKABLE, false);
		l3p1SGC.getAppearance().setAttribute(POINT_SHADER+"."+POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.yellow);
		l3p2SGC.getAppearance().setAttribute(POINT_SHADER+"."+POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.yellow);
		l3p1SGC.setGeometry(pointsets[0] = Primitives.point(pointsl3[0] = Rn.add(null, points[0],points[1])));
		l3p2SGC.setGeometry(pointsets[1] = Primitives.point(pointsl3[1] = Rn.add(null, points[2],points[3])));
		l3p2SGC.addChild(thirdLeitLine);
		Appearance leftAp = lSGC.getAppearance(), rightAp = rSGC.getAppearance();
		leftAp.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, leftColor);
		leftAp.setAttribute(LINE_SHADER+"."+POLYGON_SHADER+"."+DIFFUSE_COLOR, leftColor);
		rightAp.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, rightColor);
		rightAp.setAttribute(LINE_SHADER+"."+POLYGON_SHADER+"."+DIFFUSE_COLOR, rightColor);
		Appearance gap = new Appearance(), tap = new Appearance();
		gap.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, Color.cyan);
		gap.setAttribute(LINE_SHADER+"."+POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.cyan);
		tap.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, Color.pink);
		tap.setAttribute(LINE_SHADER+"."+POLYGON_SHADER+"."+DIFFUSE_COLOR,Color.pink);
		gap.setAttribute(LINE_SHADER+"."+TUBE_RADIUS, .04);
		tap.setAttribute(LINE_SHADER+"."+TUBE_RADIUS, .04);
		gap.setAttribute(LINE_SHADER+"."+TUBES_DRAW, true);
		tap.setAttribute(LINE_SHADER+"."+TUBES_DRAW, true);
		gap.setAttribute(POINT_SHADER+"."+POINT_RADIUS, .06);
		tap.setAttribute(POINT_SHADER+"."+POINT_RADIUS, .06);
		gap.setAttribute(VERTEX_DRAW, true);
		tap.setAttribute(VERTEX_DRAW, true);
//		thirdLeitLine.setAppearance(tap);
		Appearance ap = thirdLeitLine.getAppearance();
		ap.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, Color.red);
		ap.setAttribute(LINE_SHADER+"."+POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.red);
		thirdLeitLine.setAppearance(ap);
		thirdLeitLine.setPickable( false);
		
		regulusFactory = SkewQuad.skewQuadForCorners(points);
		double[][] linesInSkewQuad = regulusFactory.getElements();
		// draw the edge lines of the tetrahedron
		for (int i = 0; i<4; ++i)	{
			PointRangeFactory genFac = generatorFactories[i] = new PointRangeFactory();
			genFac.setPluckerLine(linesInSkewQuad[i]);
			genFac.setFiniteSphere(true);
			genFac.setSphereRadius(globalSphereRadius);
			genFac.update();
			SceneGraphComponent child = new SceneGraphComponent(""+i);
			if (i==0)	{
				child.addTool(new DragPointTool(0));
				child.addChild(l3p1SGC);
			}
			if (i == 2)	{
				child.addTool(new DragPointTool(1));
				child.addChild(l3p2SGC);
			}
			child.setGeometry(genFac.getLine());
			child.setAppearance((i%2)==0 ? gap : tap);
//			if ((i%2)==0) rSGC.addChild(child);
//			else lSGC.addChild(child);
			fourLines.addChild(child);
		}
		rPlucker[0] = linesInSkewQuad[0];
		lPlucker[0] = linesInSkewQuad[1];
		rPlucker[1] = linesInSkewQuad[2];
		lPlucker[1] = linesInSkewQuad[3];
		lPlucker[2] = PlueckerLineGeometry.lineFromPoints(null, pointsl3[0], pointsl3[1]);
		// This point range factory is not used for drawing, but for parametrizing the line
		// for use in slider (below) and animation
		int[][] inds = {{0,1},{2,3}};
		for (int i = 0; i<2; ++i)	{
			parameterLineF[i] = new PointRangeFactory();
//			parameterLineF[i].setPluckerLine(parameterLines[i]);
			parameterLineF[i].setElement0(points[inds[i][0]]);
			parameterLineF[i].setElement1(points[inds[i][1]]);
			parameterLineF[i].setFiniteSphere(false);
			parameterLineF[i].setNumberOfSamples(lineCount);
			parameterLineF[i].update();			
		}
//		pointsl3[0] = parameterLineF[0].getValueAtTime(time);

		PointRangeFactory  prf;
		// the "fifth" line: which determines the regulus and its "leitschar"
		prf = generatorFactories[4] = new PointRangeFactory();
		prf.setElement0(pointsl3[0]);
		prf.setElement1(pointsl3[1]);
		prf.setFiniteSphere(true);
		prf.setSphereRadius(globalSphereRadius);
		prf.update();
		prf.getLine().setName("3rd line");
		thirdLeitLine.setGeometry(prf.getLine());
		
		
//		System.err.println("Plucker lines: "+Rn.toString(lPlucker));
//		regulusFactory.setElements(skewquad);
//		regulusFactory.setElement0(lPlucker[0]);
//		regulusFactory.setElement2(lPlucker[1]);
		regulusFactory.setElement1(lPlucker[2]);
		regulusFactory.setFiniteSphere(true);
		regulusFactory.setSphereRadius(sphereRadius);
		regulusFactory.setNumberOfSamples(lineCount);
		regulusFactory.update();
		SceneGraphComponent regSGC = regulusFactory.getRegulus();
		regSGC.setPickable(false);
		regSGC.getAppearance().setAttribute("toolListener", false);
		rSGC.addChild(regSGC);
		SceneGraphComponent leitScharSGC = regulusFactory.getLeitSchar();
		leitScharSGC.setPickable( false);
		leitScharSGC.getAppearance().setAttribute("toolListener", false);
		lSGC.addChild(leitScharSGC);
		update();
		MatrixBuilder.euclidean().rotateX(Math.PI/10).assignTo(world);
		world.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		world.addChild(surfaceRep = regulusFactory.getSurfaceRepresentation(null));
		surfaceRep.getAppearance().setAttribute(CommonAttributes.SMOOTH_SHADING, false);
		surfaceRep.setVisible(showSurface);
		
		tetraSGC = SceneGraphUtility.createFullSceneGraphComponent("tetraSGC");
		ap = tetraSGC.getAppearance();
		ap.setAttribute(TRANSPARENCY_ENABLED, true);
		ap.setAttribute(TRANSPARENCY, .6);
		ap.setAttribute(EDGE_DRAW, true);
		ap.setAttribute(LINE_SHADER+"."+TUBES_DRAW, true);
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.white);
		ap.setAttribute(LINE_SHADER+"."+POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.white);
		ap.setAttribute(LINE_SHADER+"."+TUBE_RADIUS, .03);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		tetraSGC.setGeometry(Primitives.tetrahedron());
		world.addChild(tetraSGC);
		return world;
	}
	int count = 0;
	protected void update()	{
//		System.err.println("Plucker lines: "+Rn.toString(lPlucker));
		pointsets[0].setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(4).
				createReadOnly(new double[][]{pointsl3[0]}));
		pointsets[1].setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(4).
				createReadOnly(new double[][]{pointsl3[1]}));
		generatorFactories[4].setElement0(pointsl3[0]);
		generatorFactories[4].setElement1(pointsl3[1]);
		generatorFactories[4].update();
		lPlucker[2] = PlueckerLineGeometry.lineFromPoints(null, pointsl3[0], pointsl3[1]);

		regulusFactory.setElement1(lPlucker[2]);
		regulusFactory.setNumberOfSamples(lineCount);
		regulusFactory.setSphereRadius(sphereRadius);
		regulusFactory.update();
		if (showSurface)
			regulusFactory.getSurfaceRepresentation(surfaceRep);
//		System.err.println("In update "+count++);
		if (viewer != null) viewer.renderAsync();
	}
	@Override
	public Component getInspector() {	
		Box inspectionPanel =  inspector;
		timeSlider = new TextSlider.Double("t",SwingConstants.HORIZONTAL, -1.0, 1.0, time);
		timeSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				time = timeSlider.getValue().doubleValue();
				updatePointsFromParameter(time);
				update();
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(timeSlider);

		final TextSlider segSlider = new TextSlider.Integer("lines",SwingConstants.HORIZONTAL,1,300,lineCount);
		segSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				lineCount = segSlider.getValue().intValue();
				update();
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(segSlider);

		final TextSlider radiusSlider = new TextSlider.Double("radius",SwingConstants.HORIZONTAL,0,100,sphereRadius);
		radiusSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				sphereRadius = radiusSlider.getValue().doubleValue();
				update();
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(radiusSlider);
		
		final JCheckBox tetraBox = new JCheckBox("Show tetrahedron");
		tetraBox.setSelected(showTetra);
		tetraBox.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				showTetra = tetraBox.isSelected();
				tetraSGC.setVisible(showTetra);
			}
		});
		inspectionPanel.add(tetraBox);
		
		final JCheckBox sqBox = new JCheckBox("Show skew quad");
		sqBox.setSelected(showSkewQuad);
		sqBox.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				showSkewQuad = sqBox.isSelected();
				fourLines.setVisible(showSkewQuad);
			}
		});
		inspectionPanel.add(sqBox);
		
		final JCheckBox mtBox = new JCheckBox("Move points together");
		mtBox.setSelected(movePointsTogether);
		mtBox.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				movePointsTogether = mtBox.isSelected();
			}
		});
		inspectionPanel.add(mtBox);
		
		inspectionPanel.setName("Parameters");
		return inspectionPanel;
	}

	@Override
	public void display() {
		super.display();
		MatrixBuilder.euclidean().translate(0,0,5).assignTo(CameraUtility.getCameraNode(viewer));
		viewer.getSceneRoot().getAppearance().setAttribute(BACKGROUND_COLOR, new Color(20,20,40));
		AnimationPlugin ap = animationPlugin;
		ap.setAnimateSceneGraph(true);
		ap.setDefaultInterp(InterpolationTypes.CUBIC_HERMITE);
		ap.update();
		ap.setDefaultInterp(InterpolationTypes.CUBIC_HERMITE);
		setupAnimation(ap.getAnimationPanel());
	}
	
	private static final InputSlot pointerSlot = InputSlot.getDevice("PointerTransformation");
	class DragPointTool extends AbstractTool	{
		  	int which = 0;
			public DragPointTool(int w){
				super(InputSlot.getDevice("AllDragActivation"));
				addCurrentSlot(pointerSlot, "triggers drag events");
				which = w;
			}

			@Override
			public void activate(ToolContext tc) {
				if (tc.getCurrentPick() == null) return;
//				System.err.println("Start index "+tc.getCurrentPick().getIndex());
			}

			@Override
			public void deactivate(ToolContext tc) {
				if (tc.getCurrentPick() == null) return;
//				System.err.println("End index "+tc.getCurrentPick().getIndex());
			}

			@Override
			public void perform(ToolContext tc) {
				PickResult currentPick = tc.getCurrentPick();
				if (currentPick == null) return;
//				System.err.println("perform index "+Rn.toString(tc.getCurrentPick().getObjectCoordinates()));
				pointsl3[which] = currentPick.getObjectCoordinates().clone();
				PlueckerLineGeometry.projectPointOntoLine(pointsl3[which], pointsl3[which], parameterLines[which], Pn.EUCLIDEAN);
				update();
			}
			
	}

	List<Animated> animated = new ArrayList<Animated>();
	private TextSlider timeSlider;
	private void setupAnimation(AnimationPanel ap) {
		SceneGraphAnimator sga = new SceneGraphAnimator(viewer.getSceneRoot());
		sga.setAnimateCamera(true);
		sga.setName("sga");
		sga.init();
		animated.add(sga);
		
		KeyFrameAnimatedDelegate<Double> dd = new KeyFrameAnimatedDelegate<Double> () {

			public void propagateCurrentValue(Double t) {
				time=t;
				updatePointsFromParameter(time);
				update();
				viewer.renderAsync();
			}

			public Double gatherCurrentValue(Double t) {
				return time;
			}
			
		};
		KeyFrameAnimatedDouble animAlpha = new KeyFrameAnimatedDouble(dd );
		animAlpha.setName("animAlpha");
		animated.add(animAlpha);
		
		AnimationPanelListenerImpl apl = new AnimationPanelListenerImpl(viewer, "regulus family");
		apl.setAnimated(animated);
		ap.addAnimationPanelListener(apl);
	}
	private void updatePointsFromParameter(double t) {
		pointsl3[0] = parameterLineF[0].getValueAtTime(t);
		if (!movePointsTogether) return;

		double time2 = t % 1.0;
		if (time2 < 0) time2 += 1;
//		time2 = .5 - time2;
//		if (time2 < 0) time2 += 1;
		if (time2 > .5) {
			time2 = 1.0 -time2;
		}
		System.err.println("time, time2: "+t+" : "+time2);
		pointsl3[1] = parameterLineF[1].getValueAtTime(time2);
	}

	public static void main(String[] args) {
		new RegulusFamily().display();
	}
}
