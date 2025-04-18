/*
 * Created on Oct 8, 2014
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.geometry.projective.CircleFactory;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.newtools.RotateTool;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.ContinuedFraction;
import de.jreality.geometry.BoundingBoxTraversal;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class CounterSpacePlanets extends Assignment {

	Color[] colors = {
			new Color(180, 180, 180), 
			new Color(125, 255,0), 
			Color.blue,
			new Color(255, 100, 100), 
			new Color(155, 0, 255), 
			Color.cyan, 
			Color.gray, 
			new Color(0, 255, 125)};
	static double[] eccentricity = {
		    0.20563069, // Mercury
		    0.00677672, // Venus
		    0.01671022, // Earth
		    0.09339410, // Mars
		    0.04839266, // Jupiter
		    0.05415060, // Saturn
		    0.04716771, // Uranus
		    0.00858587, // Neptune
	},
		perihelion = {
			.307,
			.718,
			.983,
			1.382,
			4.95,
			9.04,
			18.324,
			29.709
	},
		longPerihelion = {
			77.45,
			131.53,
			102.95,
			336.04,
			14.75,
			92.43,
			170.96,
			44.97
	};
	double scaler = 1.0/perihelion[7];
	
	static String[] names = {"mercury","venus","earth","mars","jupiter","saturn","uranus","neptune"};
	
	boolean stPr = false;
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("planet");
	SceneGraphComponent worldFlat = SceneGraphUtility.createFullSceneGraphComponent("planetFlat");
	SceneGraphComponent worldSphere = SceneGraphUtility.createFullSceneGraphComponent("planetSphere");
	SceneGraphComponent starSGC = SceneGraphUtility.createFullSceneGraphComponent("star");
	
	@Override
	public SceneGraphComponent getContent() {
		world.addChildren(worldFlat, worldSphere);
		worldFlat.setVisible(!stPr);
		worldSphere.setVisible(stPr);
		for (int j = 0; j<2; ++j)	{
			for (int i = 0; i<eccentricity.length; ++i)	{
				SceneGraphComponent child = planetaryPath(
						eccentricity[i], scaler*perihelion[i], (Math.PI/180.0)*longPerihelion[i], j==1, names[i]);
				child.setName(names[i]);
				child.getAppearance().setAttribute("lineShader.diffuseColor", colors[i]);
				child.getAppearance().setAttribute("pointShader.diffuseColor", colors[i]);
				(j == 0 ? worldFlat : worldSphere).addChild(child);
			}			
		}
		starSGC.setGeometry(GeometryUtilityOverflow.starPoint(16, .01, .03));
		MatrixBuilder.euclidean().translate(0,0,stPr ? -1 : 0).scale(.1).assignTo(starSGC);
		Appearance ap = starSGC.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", Color.yellow);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute("lineShader.lineWidth", 2.0);
		world.addChild(starSGC);
		
		ap = world.getAppearance();
		ap.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .001);
		ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, .002);
		ap.setAttribute(CommonAttributes.TEXT_SHADER+"."+CommonAttributes.TEXT_SCALE, .0001);
		ap.setAttribute(CommonAttributes.TEXT_SCALE, .0001);
		
		SceneGraphComponent sphereSGC = SceneGraphUtility.createFullSceneGraphComponent("sphere");
		IndexedFaceSet sphere = SphereUtility.tessellatedIcosahedronSphere(5);
		sphereSGC.setGeometry(sphere);
		sphereSGC.getAppearance().setAttribute("polygonShader.diffuseColor", Color.white);
		sphereSGC.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		sphereSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		MatrixBuilder.euclidean().scale(.99).assignTo(sphereSGC);
		worldSphere.addChild(sphereSGC);
//		world.addTool(new RotateTool());
		return world;
	}

	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		getDualRadii();
		Component comp = ((Component) jrviewer.getViewer().getViewingComponent());
		comp.addKeyListener(new KeyAdapter() {
 				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_H:
						System.err.println("	1: toggle flat/round");
						break;
		
					case KeyEvent.VK_1:
						stPr = !stPr;
						worldFlat.setVisible(!stPr);
						worldSphere.setVisible(stPr);
						MatrixBuilder.euclidean().translate(0,0,stPr ? -1 : 0).assignTo(starSGC);
						break;
					}
				}
			});
	}

	
	public static double[] getDualRadii()	{
		double[] dualRadii = new double[names.length];
		
		for (int i = 0; i<names.length; ++i)	{
			double f = eccentricity[i] * perihelion[i]/ (1-eccentricity[i]),
					major = f + perihelion[i];
			dualRadii[i] = 1/(major-f) + 1/(major+f);
		}
		double scale = 1.0/dualRadii[2];
		double[] ratio = new double[names.length*names.length];
		for (int i = 0; i<names.length; ++i)	{
			for (int j = 0; j<names.length; ++j)	{
				double r = dualRadii[i]/dualRadii[j];
				ratio[names.length * i + j] = r;
			}
		}
		System.err.println(Rn.toString(dualRadii));
		
		return dualRadii;
	}
	public static SceneGraphComponent planetaryPath(double ecc, double peri, double lp, boolean stPr, String name)	{
		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("planet");
		SceneGraphComponent bahnSGC = SceneGraphUtility.createFullSceneGraphComponent("bahn");
		SceneGraphComponent f2SGC = SceneGraphUtility.createFullSceneGraphComponent("focus2");
		SceneGraphComponent midlineSGC = SceneGraphUtility.createFullSceneGraphComponent("middle line");
		double f = ecc * peri/ (1-ecc),
				a = f + peri,
				b = Math.sqrt(a*a-f*f);
		System.err.println("f a b = "+f+" "+a+" "+b);
		System.err.println("dual radius "+(1/(a-f)+1/(a+f)));
		
		double[][] points = new double[100][4];
		for (int i = 0; i<100; ++i)	{
			double angle = (i*Math.PI*2.0)/100;
			points[i][0] = a * Math.cos(angle)-f;
			points[i][1] = b * Math.sin(angle);
			points[i][2] = 0.0;
			points[i][3] = 1.0;
			if (stPr) 
				CircleFactory.inverseStereoProj(points[i], points[i][0], points[i][1]);
		}
		Pn.dehomogenize(points, points);
		IndexedLineSet bahn = IndexedLineSetUtility.createCurveFromPoints(points, true);
		bahnSGC.setGeometry(bahn);
		// add the second focus (where the sun isn't)
		double[] focus2 = {-2*f,0,0,1};
		if (stPr)	{
			focus2 = CircleFactory.inverseStereoProj(focus2, focus2[0], focus2[1]);
		}
		PointSet f2 = Primitives.point(focus2, stPr ? null : name);
		f2SGC.setGeometry(f2);
		f2SGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		if (stPr)	{
			CircleFactory cf = CircleFactory.circleFactoryForCircle(new double[]{1, 0, 2*a*a/f, 0});
			cf.update();
			SceneGraphComponent flattened = SceneGraphUtility.flatten(cf.getSphereSGC());
			midlineSGC.addChild(flattened);
		} else {
			// add the "middle line" of the counter-space circle
			double[] p1 = {0,1,0,0};		// y-direction
			double[] p2 = {a*a/f,0,0,1};
			PointRangeFactory midline = new PointRangeFactory();
			midline.setElement0(p1);
			midline.setElement1(p2);
			midline.setFiniteSphere(false);
			midline.update();
			midlineSGC.setGeometry(midline.getLine());			
		}
		
		midlineSGC.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, stPr); 
		midlineSGC.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, 
				Rectangle3D.EMPTY_BOX);
		sgc.addChildren(bahnSGC, f2SGC, midlineSGC);
		MatrixBuilder.euclidean().rotateZ(lp).assignTo(sgc);
		return sgc;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new CounterSpacePlanets().display();
	}

	
}
