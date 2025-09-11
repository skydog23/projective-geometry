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

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.anim.jreality.SceneGraphAnimator;
import charlesgunn.jreality.geometry.projective.DualizeSceneGraph;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class DualPointLineCurve extends Assignment {

	private transient SceneGraphComponent world,
		eucSGC,
		pointSGC,
		lineSGC,
		fakeLineSGC;
	protected int numberOfSegments = 100;		// -2,-1: degenerate motion 0: generate circle,  1: generate center,  2: show inside, 3: outside
	private double time = 0;
	private boolean 
			showEuc = true,
			showPolar = true;
	protected boolean encompass = true;
	private transient PointSetFactory psf = new PointSetFactory(), lsf = new PointSetFactory();
    protected double 
			scale = 1.0, 
			tscale = .07, 
			ascale = .1510, 
			phase = -.17, 
			ascale3 = .321, 
			ascale2 = .92,
			ascale4 = 1.0,
			pointRadius = .055,
			lineRadius = .008;
	protected double[] startPoint = {0,0,0,1}, 
			startLine = {1,0,0,0};
	protected Color pointColor = new Color(255, 196, 29), //new Color(255, 140, 40), //50, 50), //(255,255,50),
//			lineColor =  new Color(255,255,255),
			lineColor =  new Color(182, 250, 250),
			backgroundColor =  new Color(0,0,0,0);

	@Override
	public SceneGraphComponent getContent() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		eucSGC = SceneGraphUtility.createFullSceneGraphComponent("euc");
		pointSGC = SceneGraphUtility.createFullSceneGraphComponent("point");
		lineSGC = SceneGraphUtility.createFullSceneGraphComponent("line");
		fakeLineSGC = SceneGraphUtility.createFullSceneGraphComponent("fakeLine");
		world.addChildren(eucSGC);
		eucSGC.addChildren(pointSGC, lineSGC);
		
		pointSGC.setGeometry(psf.getGeometry());
		fakeLineSGC.setGeometry(lsf.getGeometry());
		
		Appearance ap = pointSGC.getAppearance();
		ap.setAttribute("pointShader.diffuseColor", pointColor);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, pointRadius);
		
		ap = lineSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", lineColor);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, lineRadius);
		// ??
//		MatrixBuilder.euclidean().rotateZ(Math.PI).assignTo(lineSGC);
		
		ap = world.getAppearance();
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, true);
		ap.setAttribute(CommonAttributes.SPHERES_DRAW, true);
		ap.setAttribute(SceneGraphAnimator.ANIMATED, false);
		getWorldTform().assignTo(eucSGC);		
		time = 1.0;
		update();

//		update();
		// this is a hack to get the bounding boxes to work even when the points aren't
		// being drawn
		lineSGC.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, 
				BoundingBoxUtility.calculateBoundingBox(psf.getPointSet()));

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
	
		Camera cam = CameraUtility.getCamera(jrviewer.getViewer());
		cam.setPerspective(false);
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

	private void updateVisibility() {
		
		pointSGC.setVisible(showEuc);
		lineSGC.setVisible(showPolar);
	}
	private void update() {
		generateCurve(); 
		
		lineSGC.removeAllChildren();
		
		DualizeSceneGraph.setMetric(Pn.ELLIPTIC);
		SceneGraphComponent dualize = DualizeSceneGraph.dualize(fakeLineSGC);
//		dualize.getChildComponent(0).setVisible(showPolar);	
		
		lineSGC.addChild(dualize);
		
		updateVisibility();
	}

	private double oldtime = -1.0;
	private Matrix zrotM = MatrixBuilder.euclidean().rotateZ(Math.PI).getMatrix();
	private boolean generateCurve() {
//		double localtime = time;
		if (oldtime == time) return false;
		int steps = ((int) (time * numberOfSegments));
		double fraction = (time*numberOfSegments) - steps;
		double[][] points = new double[steps+2][4];
		double[][] lines = new double[points.length][4];
		double[] dparms = new double[2];
		Matrix M = new Matrix(), dM = new Matrix(), iM = new Matrix();
		double delta = 1.0/(numberOfSegments);
		points[0] = M.multiplyVector(startPoint);
		lines[0] = iM.multiplyVector(startLine);
		for (int i = 1; i<=steps; ++i) {
			getParms(dparms, delta*i);
//			System.err.println(i +" parms = "+Rn.toString(dparms));
			MatrixBuilder.euclidean().rotateZ(dparms[1]).translate(0, dparms[0], 0).assignTo(dM);
			M.multiplyOnRight(dM);
			points[i] = M.multiplyVector(startPoint);
			iM = M.getInverse();
			iM.transpose();
			lines[i] = iM.multiplyVector(startLine);
//			System.err.println("dot: "+Rn.innerProduct(points[i], lines[i]));
		}
//		if (time != 1.0)  {
			getParms(dparms, time);
			MatrixBuilder.euclidean().rotateZ(fraction*dparms[1]).translate(0, fraction* dparms[0], 0).assignTo(dM);
			M.multiplyOnRight(dM);
			points[steps+1] = M.multiplyVector(startPoint);
			iM = M.getInverse();
			iM.transpose();
			lines[steps+1] = iM.multiplyVector(startLine);
//			System.err.println("n-1 Line "+Rn.toString(lines[limit-1]));
//			System.err.println("n   Line "+Rn.toString(lines[limit]));
//		}

//		System.err.println("Points = "+Rn.toString(points));
//		System.err.println("Lines = "+Rn.toString(lines));
		psf.setVertexCount(points.length);
		psf.setVertexCoordinates(points);
		psf.update();
		lsf.setVertexCount(lines.length);
		lsf.setVertexCoordinates(lines);
		lsf.update();
		oldtime = time;
		return true;
	}

	protected void getParms(double[] dparms, double t) {
		dparms[0] = .1;
		dparms[1] = .1;
	}
	
	
	@Override
	public Component getInspector() {
		System.err.println("In get inspector");
		Box container = Box.createVerticalBox();
		final TextSlider cSlider = new TextSlider.Double("translate",  SwingConstants.HORIZONTAL, 0, .1, tscale);
		cSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				tscale = cSlider.getValue().doubleValue();
				update();
			}
		});
		container.add(cSlider);
		{
		final TextSlider aSlider = new TextSlider.Double("phase",  SwingConstants.HORIZONTAL, 0, 1, phase);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				phase = aSlider.getValue().doubleValue();
				update();
			}
		});
		container.add(aSlider);
		}
		{
		final TextSlider aSlider = new TextSlider.Double("rotate",  SwingConstants.HORIZONTAL, 0, 1, ascale);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				ascale = aSlider.getValue().doubleValue();
				update();
			}
		});
		container.add(aSlider);
		}
		{
		final TextSlider aSlider = new TextSlider.Double("ascale2",  SwingConstants.HORIZONTAL, 0, 1, ascale2);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				ascale2 = aSlider.getValue().doubleValue();
				update();
			}
		});
		container.add(aSlider);
		}
		{
		final TextSlider aSlider = new TextSlider.Double("ascale3",  SwingConstants.HORIZONTAL, 0, 1, ascale3);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				ascale3 = aSlider.getValue().doubleValue();
				update();
			}
		});
		container.add(aSlider);
		}
		{
		final TextSlider aSlider = new TextSlider.Double("ascale4",  SwingConstants.HORIZONTAL, 0, 1, ascale4);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				ascale4 = aSlider.getValue().doubleValue();
				update();
			}
		});
		container.add(aSlider);
		}
		inspector.add(container);
		return super.getInspector();
	}

	public static void main(String[] args) {
		new DualPointLineCurve().display();
	}

}
