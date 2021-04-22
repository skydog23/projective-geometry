/*
 * Created on 25.09.2018
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.geometry.InterpolatedILS;
import charlesgunn.jreality.geometry.projective.DualizeSceneGraph;
import charlesgunn.jreality.geometry.projective.LinePencilFactory;
import charlesgunn.jreality.geometry.projective.LineUtility;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.util.TextSlider;
import charlesgunn.util.doubleLong;
import de.jreality.geometry.GeometryAttributeListSet;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.PointSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.projgeom.PlueckerLineGeometry;

public class DualPointLineCurve extends Assignment {

	private transient SceneGraphComponent world,
	eucSGC,
		pointSGC,
		lineSGC,
		fakeLineSGC;
	int num = 100;		// -2,-1: degenerate motion 0: generate circle,  1: generate center,  2: show inside, 3: outside
	double time = 0;
	boolean 
			showEuc = true,
			showPolar = true;
	private transient PointSetFactory psf = new PointSetFactory(), lsf = new PointSetFactory();
    private double[] point = new double[]{1,0,0,1};
	private double[] line = PlueckerLineGeometry.lineFromPoints(null, point, new double[]{1,0,0,0});
	double scale = 1.0, tscale = .07, ascale = .1510, phase = -.17, ascale2 = .92;
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
		ap.setAttribute("pointShader.diffuseColor", new Color(255,255,204));
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
//		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .006);
		
		ap = lineSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", new Color(255,255,204));
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		// ??
		MatrixBuilder.euclidean().rotateZ(Math.PI).assignTo(lineSGC);
		
		ap = world.getAppearance();
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, true);
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .005);
		ap.setAttribute(CommonAttributes.SPHERES_DRAW, true);
		ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, .02);
				
		time = 1.0;
		update();

		update();

		return world;
	}

	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(0,0,0,0));
		animationPlugin.getAnimationPanel().setResourceDir("src/charlesgunn/jreality/worlds/projective/");
		animationPlugin.getAnimationPanel().getRecordPrefs().setCurrentDirectoryPath("/gunn_local/Movies/RuGR/");

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
		
		SceneGraphComponent dualize = DualizeSceneGraph.dualize(fakeLineSGC);
		dualize.getChildComponent(0).setVisible(showPolar);	
		
		lineSGC.addChild(dualize);
		
		updateVisibility();
	}

	double oldtime = -1.0;
	private boolean generateCurve() {
		double localtime = time;
//		if (oldtime == localtime) return false;
		int num2 =  num;
		int limit = ((int) (localtime * num2))+1;
		double fraction = (1+localtime*num2) - limit;
		double[][] points = new double[limit+((time == 1.0) ? 0:1)][4];
		double[][] lines = new double[points.length][4];
		double[] dparms = new double[2];
		Matrix M = new Matrix(), dM = new Matrix(), iM = new Matrix();
		points[0] = new double[]{0,0,0,1}; // origin
		lines[0] = new double[]{1,0,0,0};	// x = 0
		double delta = 1.0/num2;
		for (int i = 1; i<limit; ++i) {
			getParms(dparms, delta*i);
			MatrixBuilder.euclidean().rotateZ(dparms[1]).translate(0, dparms[0], 0).assignTo(dM);
			M.multiplyOnRight(dM);
			points[i] = M.multiplyVector(points[0]);
			iM = M.getInverse();
			iM.transpose();
			lines[i] = iM.multiplyVector(lines[0]);
		}
		if (localtime != 1.0)  {
			getParms(dparms, localtime);
			MatrixBuilder.euclidean().rotateZ(fraction*dparms[1]).translate(0, fraction* dparms[0], 0).assignTo(dM);
			M.multiplyOnRight(dM);
			points[limit] = M.multiplyVector(points[0]);
			iM = M.getInverse();
			iM.transpose();
			lines[limit] = iM.multiplyVector(lines[0]);
			System.err.println("n-1 Line "+Rn.toString(lines[limit-1]));
			System.err.println("n   Line "+Rn.toString(lines[limit]));
		}

		psf.setVertexCount(points.length);
		psf.setVertexCoordinates(points);
		psf.update();
		lsf.setVertexCount(lines.length);
		lsf.setVertexCoordinates(lines);
		lsf.update();
		oldtime = localtime;
		return true;
	}

	private void getParms(double[] dparms, double t) {
		dparms[0] = scale * tscale;
		dparms[1] = scale * ascale * Math.cos(phase + ascale2*Math.PI*2*t);
	}
	
	@Override
	public Component getInspector() {
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
		final TextSlider aSlider = new TextSlider.Double("angle scale",  SwingConstants.HORIZONTAL, 0, 1, ascale2);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				ascale2 = aSlider.getValue().doubleValue();
				update();
			}
		});
		container.add(aSlider);
		}
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
		inspector.add(container);
		return super.getInspector();
	}

	public static void main(String[] args) {
		new DualPointLineCurve().display();
	}

}
