/*
 * Created on Dec 1, 2010
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.jreality.geometry.projective.PlanePencilFactory;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.Utility;
import charlesgunn.math.clifford.ConicSection;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P2;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.projgeom.PlueckerLineGeometry;

public class DandelinConfiguration extends Assignment {

	private double[][] 
	           points3 = new double[6][3], 
	           lines3 = new double[6][3], 
	           points4, 
	           regLines = new double[6][6],
	           conicPoints3,
	           conicPoints4,
	           planes = new double[6][4];
	private double[] perpLine = new double[3], middlePoint;
	private double[] cut1, join1, cut2, join2;
	int numPoints = 100;
	double parameter = .2,
		pitch =1.0/3.0,
		shear = .5,
		sphereRadius = 3;
	private SceneGraphComponent world,
		pointsSGC,
		linesSGC,
		planesSGCParent,
			planesSGC[],
		basePlaneSGC,
		conicSGC;
	private PointSetFactory pointsFactory;
	private PointRangeFactory[] lineFactories = new PointRangeFactory[6],
		regLineFactories = new PointRangeFactory[6];
	private PlanePencilFactory[] planeFactories = new PlanePencilFactory[6];
	ConicSection conic = new ConicSection();

	@Override
	public SceneGraphComponent getContent() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		pointsSGC = SceneGraphUtility.createFullSceneGraphComponent("points");
		linesSGC = SceneGraphUtility.createFullSceneGraphComponent("lines");
		linesSGC.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		linesSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		linesSGC.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.black);
		MatrixBuilder.euclidean().translate(0,0,.01).assignTo(linesSGC);
		planesSGCParent = SceneGraphUtility.createFullSceneGraphComponent("planesSGC");
		planesSGCParent.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		planesSGCParent.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		planesSGCParent.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.black);
		pointsSGC.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.yellow);
		conicSGC = SceneGraphUtility.createFullSceneGraphComponent("conic");
		conicSGC.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		conicSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		conicSGC.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.red);
		MatrixBuilder.euclidean().translate(0,0,.01).assignTo(conicSGC);
		basePlaneSGC = SceneGraphUtility.createFullSceneGraphComponent("basePlane");
		planesSGCParent = SceneGraphUtility.createFullSceneGraphComponent("planes");
		world.addChildren(pointsSGC, linesSGC, planesSGCParent, conicSGC, basePlaneSGC, planesSGCParent);

		init();

		pointsFactory = new PointSetFactory();
		pointsFactory.setVertexCount(points4.length);
		pointsFactory.setVertexCoordinates(points4);
		pointsFactory.update();
		pointsSGC.setGeometry(pointsFactory.getPointSet());
		IndexedLineSet conic = IndexedLineSetUtility.createCurveFromPoints(conicPoints4, true);
		conicSGC.setGeometry(conic);
		double s = 2;
		basePlaneSGC.setGeometry(Primitives.texturedQuadrilateral(new double[]{-s,-s,0, s, -s,0,  s,s,0,  -s,s,0}));
		world.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		world.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		world.getAppearance().setAttribute(CommonAttributes.DIFFUSE_COLOR	, Color.white);
		update();
		
		MatrixBuilder.euclidean().translate(0,0,-4).assignTo(world);
		return world;
	}
	double[] fixedPlane = {0,0,1,0};  // z=0 plane
	int[] perm = {0,2,4,1,3,5}, steps = {0,1,2,3,5,4};
	protected void init()	{
		for (int i = 0; i<6; ++i)	{
			double angle = steps[i]*Math.PI * 2.0/6.0;
			int index = perm[i];
			points3[index][0] = Math.cos(angle);
			points3[index][1] = Math.sin(angle);
			points3[index][2] = 1.0;
		}
		conic.setInitialPoints(points3);
		points4 = Utility.promote(points4, points3);
		for (int i = 0; i<6; ++i)	{
			lines3[i] = P2.lineFromPoints(null, points3[i], points3[(i+1)%6]);
		}
		conicPoints3 = new double[numPoints][];
		for (int i = 0; i<numPoints; ++i)	{
			double t = i *(1.0/(numPoints));
			conicPoints3[i] = conic.getValueAtTime(null, t);
		}
		conicPoints4 = Utility.promote(conicPoints4, conicPoints3);
		// initialize planes and 5 of the six regulus lines
		// create a very regular regulus
		regLines[0] = regLineAt(null, Math.PI*2*(0.0/6.0));
		regLines[2] = regLineAt(null, Math.PI*2*(4.0/6.0));
		regLines[4] = regLineAt(null, Math.PI*2*(5.0/6.0));
		regLines[1] = leitLineAt(null, Math.PI*2*(2.0/6.0));
		regLines[3] = leitLineAt(null, Math.PI*2*(1.0/6.0));
		regLines[5] = leitLineAt(null, Math.PI*2*(3.0/6.0));
//		double[] tmp = points4[0].clone();
//		tmp[0] += .5; tmp[1] = .3; tmp[2] = 1;
//		PlueckerLineGeometry.lineFromPoints(regLines[0], points4[0], tmp);
//		tmp = points4[4].clone();
//		tmp[0] -= .5; tmp[1] = -.3; tmp[2] = 1;
//		PlueckerLineGeometry.lineFromPoints(regLines[4], points4[4], tmp);
//		planes[0] = PlueckerLineGeometry.lineJoinPoint(planes[0], regLines[0], points4[1]);
//		planes[3] = PlueckerLineGeometry.lineJoinPoint(planes[3], regLines[4], points4[3]);
//		double[] tplane = PlueckerLineGeometry.lineJoinPoint(null, regLines[0], points4[3]);
//		PlueckerLineGeometry.lineFromPlanes(regLines[3], planes[3], tplane);
//		tplane = PlueckerLineGeometry.lineJoinPoint(null, regLines[4], points4[1]);
//		PlueckerLineGeometry.lineFromPlanes(regLines[1], planes[0], tplane);
//		planes[1] = PlueckerLineGeometry.lineJoinPoint(planes[1], regLines[1], points4[2]);
//		planes[2] = PlueckerLineGeometry.lineJoinPoint(planes[2], regLines[3], points4[2]);
//		PlueckerLineGeometry.lineFromPlanes(regLines[2], planes[1],planes[2]);
//		regLines[5] = regLines[0].clone();
//		System.err.println("points = \n"+Rn.toString(points4));
//		System.err.println("reglines = \n"+Rn.toString(regLines));
		planesSGC = new SceneGraphComponent[6];
		Appearance regAp, leitAp;
		regAp = new Appearance();
		leitAp = new Appearance();
		regAp.setAttribute("diffuseColor", Color.red);
		leitAp.setAttribute("diffuseColor", Color.blue);
		for (int i = 0; i<6; ++i)	{
			lineFactories[i] = new PointRangeFactory(); 
			lineFactories[i].setFiniteSphere(true);
			lineFactories[i].setSphereRadius(3);
			lineFactories[i].setElement0(points4[i]);
			lineFactories[i].setElement1(points4[(i+1)%6]);
			lineFactories[i].update();
			SceneGraphComponent child = new SceneGraphComponent("line"+i);
			linesSGC.addChild(child);
			child.setGeometry(lineFactories[i].getLine());
			regLineFactories[i] = new PointRangeFactory(); 
			regLineFactories[i].setFiniteSphere(true);
			regLineFactories[i].setSphereRadius(sphereRadius);
			regLineFactories[i].setPluckerLine(regLines[i]);
			regLineFactories[i].update();
//			regLineFactories[i].update();
			child = new SceneGraphComponent("line"+i);
			child.setAppearance( ((i%2) == 0) ? regAp : leitAp);
			planesSGCParent.addChild(child);
			child.setGeometry(regLineFactories[i].getLine());
		}
	}
	
	private double[] regLineAt(double[] ret, double position) {
		return regLineAt(ret, position, pitch, parameter);
	}
	private double[] leitLineAt(double[] ret, double position) {
		return regLineAt(ret, position, -pitch, -parameter);
	}

	private double[] regLineAt(double[] ret, double position, double p, double l) {
		double c = Math.cos(position), s = Math.sin(position),
				c2 = Math.cos(p), s2 = Math.sin(p);
		double[] point = {c,s,0,1},
				direction = {l,c2,s2,0};
		Matrix m = new Matrix();
		MatrixBuilder.euclidean().rotate(position, 0, 0, 1).assignTo(m);
		direction = m.multiplyVector(direction);
		direction = shearM.multiplyVector(direction);
		ret = PlueckerLineGeometry.lineFromPoints(ret, point, direction);
		return ret;
	}

	Matrix shearM = new Matrix();
	protected void update()	{
		shearM = new Matrix(new double[]{
				1,0,-shear,0,
				0,1,0,0,
				0,0,1,0,
				0,0,0,1
		});
		regLines[0] = regLineAt(null, Math.PI*2*(0.0/6.0));
		regLines[2] = regLineAt(null, Math.PI*2*(4.0/6.0));
		regLines[4] = regLineAt(null, Math.PI*2*(5.0/6.0));
		regLines[1] = leitLineAt(null, Math.PI*2*(2.0/6.0));
		regLines[3] = leitLineAt(null, Math.PI*2*(1.0/6.0));
		regLines[5] = leitLineAt(null, Math.PI*2*(3.0/6.0));
		for (int i = 0; i<6; ++i)	{
			regLineFactories[i].setPluckerLine(regLines[i]);
			regLineFactories[i].update();	
		}

//		points4[5] = Utility.promote(points4[5], conic.getValueAtTime(null, parameter));
//		pointsFactory.setVertexCoordinates(points4);
//		pointsFactory.update();
//		lineFactories[4].setElement1(points4[5]);
//		lineFactories[5].setElement0(points4[5]);
//		lineFactories[4].update();
//		lineFactories[5].update();
//		// update the planes
//		planes[4] = PlueckerLineGeometry.lineJoinPoint(planes[4], regLines[4], points4[5]);
//		planes[5] = PlueckerLineGeometry.lineJoinPoint(planes[5], regLines[0], points4[5]);
//		PlueckerLineGeometry.lineFromPlanes(regLines[5], planes[4], planes[5]);
//		if (PlueckerLineGeometry.isValidLine(regLines[5])) {
//			regLineFactories[5].setPluckerLine(regLines[5]);
//			regLineFactories[5].update();
//		}
	}
	
//	protected double[] pointOnConicAtTime(double[] dst, double t)	{
//		if (dst == null) dst = new double[3];
//		double angle = Math.PI*t;
//		double[] lineFromPencil = Rn.linearCombination(null, Math.cos(angle), lines3[0], Math.sin(angle), perpLine);
//		cut1 = P2.pointFromLines(cut1,lines3[2], lineFromPencil);
//		join1 = P2.lineFromPoints(join1, cut1, middlePoint);
//		cut2 = P2.pointFromLines(cut2, lines3[1], join1);
//		join2 = P2.lineFromPoints(join2, cut2, points3[4]);
//		P2.pointFromLines(dst, lineFromPencil, join2);
//		return Pn.dehomogenize(dst, dst);
//	}
	

	private double[] leitLineAt(Object object, double d, double e, double parameter2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.white);
	}

	@Override
	public Component getInspector() {
		Box inspectionPanel = inspector;
		final TextSlider timeSlider = new TextSlider.Double("t",SwingConstants.HORIZONTAL, -1.0, 1.0, parameter);
		timeSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				parameter = timeSlider.getValue().doubleValue();
				update();
			}
		});
		inspectionPanel.add(timeSlider);
		final TextSlider rotateSlider = new TextSlider.Double("pitch",SwingConstants.HORIZONTAL, -2.0, 2.0, pitch);
		rotateSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				pitch = rotateSlider.getValue().doubleValue();
				update();
			}
		});
		inspectionPanel.add(rotateSlider);
		final TextSlider shearSlider = new TextSlider.Double("shear",SwingConstants.HORIZONTAL, -2.0, 2.0, shear);
		shearSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				shear = shearSlider.getValue().doubleValue();
				update();
			}
		});
		inspectionPanel.add(shearSlider);
		final TextSlider radiusSlider = new TextSlider.Double("radius",SwingConstants.HORIZONTAL, 2, 100, sphereRadius);
		radiusSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				sphereRadius = radiusSlider.getValue().doubleValue();
				System.err.println("changed radius");
				for (int i = 0; i<6; ++i)	{
					regLineFactories[i].setSphereRadius(sphereRadius);
					regLineFactories[i].update();
				}
			}
		});
		inspectionPanel.add(radiusSlider);
		return inspectionPanel;
	}
	public static void main(String[] args) {
		new DandelinConfiguration().display();
	}
}
