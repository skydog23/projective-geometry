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
import java.io.IOException;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.anim.jreality.SceneGraphAnimator;
import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.geometry.projective.DualizeSceneGraph;
import charlesgunn.jreality.geometry.projective.PointCollector;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.p5.PlueckerLineGeometry;
import charlesgunn.util.ColorWheel;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.BoundingBoxTraversal;
import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.RootAppearance;
import de.jreality.util.CameraUtility;
import de.jreality.util.Input;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class DualPLLCCircleSimple extends Assignment {

	private transient SceneGraphComponent world,
		eucSGC,
		lcopiesSGC,
		pcopiesSGC,
		pointSGC,
		lineSGC,
		fakeLineSGC;
	protected int numberOfSegments = 84;		// -2,-1: degenerate motion 0: generate circle,  1: generate center,  2: show inside, 3: outside
	private boolean 
			showEuc = true,
			showPolar = true,
			encompass = true;
	protected double 
			pointRadius = .0345,
			pointSize = 10,
			lineWidth = 2.5,
			lineRadius = .008,
			maxSaturate = .5;
	protected double[] startPoint = {0,0,0,1}, 
			startLine = {1,0,0,0};
	protected Color pointColor = Color.red, //new Color(255, 196, 29), //new Color(255, 140, 40), //50, 50), //(255,255,50),
//			lineColor =  new Color(255,255,255),
			lineColor =  Color.blue, //new Color(182, 250, 250),
			backgroundColor =  new Color(0,0,0,0);
	boolean hack = true,
			allSame = false,
			colorWheel = true;
	int ncopies = 14;
	PointCollector pc = new PointCollector(2000, 4),
			lc = new PointCollector(2000,4);

	@Override
	public SceneGraphComponent getContent() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		eucSGC = SceneGraphUtility.createFullSceneGraphComponent("euc");
		pcopiesSGC = SceneGraphUtility.createFullSceneGraphComponent("copies");
		lcopiesSGC = SceneGraphUtility.createFullSceneGraphComponent("copies");
		pointSGC = SceneGraphUtility.createFullSceneGraphComponent("point");
		lineSGC = SceneGraphUtility.createFullSceneGraphComponent("line");
		fakeLineSGC = SceneGraphUtility.createFullSceneGraphComponent("fakeLine");
		world.addChildren(eucSGC);
		eucSGC.addChildren(pointSGC, lineSGC, lcopiesSGC, pcopiesSGC);
		
		Appearance ap = pointSGC.getAppearance();
		ap.setAttribute("pointShader.diffuseColor", pointColor);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, pointRadius);
		ap.setAttribute("pointShader."+CommonAttributes.POINT_SIZE, pointSize);
		ap.setAttribute("pointShader."+CommonAttributes.ATTENUATE_POINT_SIZE, false);
		
		ap = lineSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", lineColor);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, lineRadius);
		ap.setAttribute("lineShader."+CommonAttributes.LINE_WIDTH, lineWidth);
		ap.setAttribute(CommonAttributes.FLIP_NORMALS_ENABLED, true);
		ap = world.getAppearance();
//		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
//		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
//		ap.setAttribute(CommonAttributes.SPHERES_DRAW, true);
		ap.setAttribute(SceneGraphAnimator.ANIMATED, false);
		update();
		MatrixBuilder.euclidean().translate(0,0,.01).assignTo(pointSGC);
		update();
		lineSGC.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, 
				Rectangle3D.unitCube);

		return world;
	}

	@Override
	public String getPropertyFileName() {
		// TODO Auto-generated method stub
		return "RuGR2D.xml";
	}

	protected Matrix getWorldTform() {
		return MatrixBuilder.euclidean().translate(0,0,0).getMatrix();
	}
	
	public boolean isShowEuc() {
		return showEuc;
	}

	public void setShowEuc(boolean showEuc) {
		this.showEuc = showEuc;
	}

	public boolean isShowPolar() {
		return showPolar;
	}

	public void setShowPolar(boolean showPolar) {
		this.showPolar = showPolar;
	}

	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR,backgroundColor);
		animationPlugin.getAnimationPanel().setResourceDir("src/charlesgunn/jreality/worlds/rugr2d");
		animationPlugin.getAnimationPanel().getRecordPrefs().setCurrentDirectoryPath("/Volumes/SamsungSSD1T/gunn_local/Movies/RuGR/");
		animationPlugin.setAnimateCamera(true);
		animationPlugin.setAnimateSceneGraph(true);
	
		Appearance ap = viewer.getSceneRoot().getAppearance();
		ap.setAttribute(CommonAttributes.RENDER_S3, true);
		Camera cam = CameraUtility.getCamera(jrviewer.getViewer());
//		cam.setPerspective(false);
		cam.setFar(-1);
		if (encompass) {
			CameraUtility.encompass(jrviewer.getViewer());
		}

	
		Component comp = ((Component) jrviewer.getViewer().getViewingComponent());
		comp.addKeyListener(new KeyAdapter() {
 				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_1:
						showEuc = !showEuc;
						updateVisibility();
						break;
		
					case KeyEvent.VK_2:
						showPolar = !showPolar;
						updateVisibility();
						break;
		
					case KeyEvent.VK_3:
						Camera poop = CameraUtility.getCamera(viewer);
//						cam.setPerspective(false);
						poop.setFar(-1);
						break;

					case KeyEvent.VK_4:
						allSame = !allSame;
						update();
						break;

 					case KeyEvent.VK_5:
						colorWheel = !colorWheel;
						update();
						break;

 				}
 				}
		});

	}

	
	@Override
	public void setValueAtTime(double d) {
		// TODO Auto-generated method stub
		super.setValueAtTime(d);
		update();
	}

	private void updateVisibility() {
		
		pointSGC.setVisible(showEuc);
		lineSGC.setVisible(showPolar);
	}
	
	Color pointC[], lineC[];

	private void update() {
		generateCurve(); 
		PointSetFactory psf = pc.getPointSetFactory(),
				lsf = lc.getPointSetFactory();
		psf.setVertexColors(pointC);
		psf.update();
		lsf.setVertexColors(lineC);
		lsf.update();
		lineSGC.removeAllChildren();
		DualizeSceneGraph.setMetric(Pn.HYPERBOLIC);
		pointSGC.setGeometry(psf.getPointSet());
		fakeLineSGC.setGeometry(lsf.getPointSet());
		SceneGraphComponent dualize = DualizeSceneGraph.dualize(fakeLineSGC);		
		lineSGC.addChild(dualize);
		
		updateVisibility();
	}

	private void generateCurve() {
		pc.reset();
		lc.reset();
		int total = 0, counts[] = new int[ncopies+1];
		for (int k = 0; k<=ncopies; ++k) {
			double r = ((ncopies-k)/(ncopies*1.0));
			if (r == 0) r = .0000001;
			int num = allSame ? numberOfSegments :  (int) (r*numberOfSegments);
			if (num == 0) num = 1;
			counts[k] = num;
			for (int i = 0; i<num; ++i)  {
				double angle = (Math.PI*2*(i))/(num);
				pc.addPoint(new double[] {r*Math.cos(angle),r*Math.sin(angle),0,1});
				lc.addPoint(new double[] {(r)*Math.cos(angle),(r)*Math.sin(angle),0,1});
				total++;
			}
		}
		pointC = new Color[total];
		lineC = new Color[total];
		Color clist[] = new Color[total];
		// sigh ... compute the color wheel colors 
		total = 0;
		for (int k = 0; k<=ncopies; ++k) {
			int num = counts[k];
			for (int i = 0; i<num; ++i)  {
				double angle = ((i))/(num*1.0);
				clist[total] = ColorWheel.getColorForParameter(angle);
				total++;
			}
		}
		total = 0;
		boolean inv = maxSaturate < 0;
		double doobie = Math.abs(maxSaturate);
		Object foo = pointSGC.getAppearance().getAttribute("pointShader.diffuseColor", Color.class);
		if (foo instanceof Color) {
			pointColor = (Color) foo;
		}
		foo = lineSGC.getAppearance().getAttribute("lineShader.diffuseColor", Color.class);
		if (foo instanceof Color) {
			lineColor = (Color) foo;
		}
//		System.err.println("Line color = "+lineColor.toString());
		for (int k = 0; k<=ncopies; ++k) {
			int num = counts[k];
			double saturated =  doobie + (1-doobie)*((inv ? ncopies - k : k)/(ncopies*1.0));
			for (int i = 0; i<num; ++i)  {
				Color tc = colorWheel ? clist[total] : pointColor;
				Color pclr = AnimationUtility.linearInterpolation( Color.white, tc, saturated);
				tc = colorWheel ? clist[total] : lineColor;
				Color lclr = AnimationUtility.linearInterpolation( Color.white, tc, saturated);
				pointC[total] = pclr;
				lineC[total] = lclr;
				total++;
			}
		}
	}
	
	
	@Override
	public Component getInspector() {
		System.err.println("In get inspector");
		Box container = Box.createVerticalBox();
		inspector.add(container);
		{
		final TextSlider aSlider = new TextSlider.Double("satur",  SwingConstants.HORIZONTAL, -1.0, 1.0, maxSaturate);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				maxSaturate = aSlider.getValue().doubleValue();
				update();
			}
		});
		container.add(aSlider);
		}
		{
		final TextSlider aSlider = new TextSlider.Integer("num",  SwingConstants.HORIZONTAL, 0, 100, numberOfSegments);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				numberOfSegments = aSlider.getValue().intValue();
				update();
			}
		});
		container.add(aSlider);
		}
		{
		final TextSlider aSlider = new TextSlider.Integer("rings",  SwingConstants.HORIZONTAL, 0, 20, ncopies);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				ncopies = aSlider.getValue().intValue();
				update();
			}
		});
		container.add(aSlider);
		}
		{
		final TextSlider aSlider = new TextSlider.Double("tube radius",  SwingConstants.HORIZONTAL, 0, .1, lineRadius);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				lineRadius = aSlider.getValue().doubleValue();
				lineSGC.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, lineRadius);
			}
		});
		container.add(aSlider);
		}
		{
		final TextSlider aSlider = new TextSlider.Double("line width",  SwingConstants.HORIZONTAL, 0, 100, lineWidth);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				lineWidth = aSlider.getValue().doubleValue();
				lineSGC.getAppearance().setAttribute("lineShader."+CommonAttributes.LINE_WIDTH, lineWidth);
			}
		});
		container.add(aSlider);
		}
		{
		final TextSlider aSlider = new TextSlider.Double("point radius",  SwingConstants.HORIZONTAL, 0, .1, pointRadius);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				pointRadius = aSlider.getValue().doubleValue();
				pointSGC.getAppearance().setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, pointRadius);
			}
		});
		container.add(aSlider);
		}
		{
		final TextSlider aSlider = new TextSlider.Double("point size",  SwingConstants.HORIZONTAL, 0, 100, pointSize);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				pointSize = aSlider.getValue().doubleValue();
				pointSGC.getAppearance().setAttribute("pointShader."+CommonAttributes.POINT_SIZE, pointSize);
			}
		});
		container.add(aSlider);
		}

		return super.getInspector();
	}

	public static void main(String[] args) {
		new DualPLLCCircleSimple().display();
	}

}
