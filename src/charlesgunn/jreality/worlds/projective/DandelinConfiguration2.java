/*
 * Created on Dec 1, 2010
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
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import charlesgunn.jreality.geometry.projective.PlanePencilFactory;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.geometry.projective.RegulusFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.Utility;
import charlesgunn.math.clifford.ConicSection;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P2;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.projgeom.PlueckerLineGeometry;

public class DandelinConfiguration2 extends Assignment {

	private double[][] 
	           points3, 
	           lines3 = new double[6][3], 
	           points4, 
	           regLines = new double[6][6],
	    	       regLinesW = new double[6][6],
	           conicPoints3,
	           conicPoints4;

	int numPoints = 200,
			numRulings = 100;
	double parameter = 0.3,
		depth = .625,
		sphereRadius = 20,
		epsilon = 5.0;
	boolean show3D = false,
			clip = false;
	private SceneGraphComponent 
	world,
	    clip1SGC,
	    clip2SGC,
	world2,
		regulusSGC,
			rotateSGC,
				bothSGC,
			        regFacSGC,
			        leitSharSGC,
		theRestSGC,
			pointsSGC,
			    onConicSGC,
			    onRegSGC,
			linesSGC,
			pascalTriSGC,
			conicSGC;
		;
	SceneGraphPath pathToRegulus;
	private PointSetFactory pointsFactory;
	private IndexedFaceSetFactory pascalTriFac;
	private PointRangeFactory[] lineFactories = new PointRangeFactory[10],
		regLineFactories = new PointRangeFactory[6];
	private PlanePencilFactory[] planeFactories = new PlanePencilFactory[6];
	RegulusFactory regFac = RegulusFactory.getRegulusFactory();
	Color y = Color.yellow, g = Color.green, m = new Color(200,0,50), 
			c = Color.cyan, bl = Color.black, vi = new Color(100,0,180);
	Color bl2 = new Color(40,40,40), bl3 = new Color(20,20,20), gr = new Color(0,135,50);
	ConicSection conic = new ConicSection();
	Appearance regAp, leitAp;

	@Override
	public SceneGraphComponent getContent() {
		if (world !=null) return world;
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		clip1SGC = SceneGraphUtility.createFullSceneGraphComponent("clip1");
		clip2SGC = SceneGraphUtility.createFullSceneGraphComponent("clip2");
		world2 = SceneGraphUtility.createFullSceneGraphComponent("world2");
		regulusSGC = SceneGraphUtility.createFullSceneGraphComponent("regulus");
		rotateSGC = SceneGraphUtility.createFullSceneGraphComponent("rotate regulus");
		bothSGC = SceneGraphUtility.createFullSceneGraphComponent("both rulings of regulus");
		theRestSGC = SceneGraphUtility.createFullSceneGraphComponent("the rest");
		pointsSGC = SceneGraphUtility.createFullSceneGraphComponent("points");
		onConicSGC = SceneGraphUtility.createFullSceneGraphComponent("on conic");
		onRegSGC = SceneGraphUtility.createFullSceneGraphComponent("on regulus");
		linesSGC = SceneGraphUtility.createFullSceneGraphComponent("lines");
		linesSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
//		linesSGC.getAppearance().setAttribute(CommonAttributes.LINE_WIDTH, 1.5);
		linesSGC.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.black);
		linesSGC.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		MatrixBuilder.euclidean().translate(0,0,.01).assignTo(linesSGC);
		pascalTriSGC = SceneGraphUtility.createFullSceneGraphComponent("pascalTri");
		Appearance ap = pascalTriSGC.getAppearance();
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
//		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		ap.setAttribute(CommonAttributes.TRANSPARENCY, 0.6);
		
		pointsSGC.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.yellow);
		conicSGC = SceneGraphUtility.createFullSceneGraphComponent("conic");
		conicSGC.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		conicSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		conicSGC.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.green);
		MatrixBuilder.euclidean().translate(0,0,.01).assignTo(conicSGC);

		theRestSGC.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.unitCube);
		theRestSGC.getAppearance().setAttribute("lineShader.lineWidth", 3.0);

		world.addChildren(world2, clip1SGC, clip2SGC);
		world2.addChildren(regulusSGC, theRestSGC);
		pointsSGC.addChildren(onConicSGC, onRegSGC);
		theRestSGC.addChildren(pointsSGC, linesSGC, conicSGC, pascalTriSGC);
		regulusSGC.addChildren(rotateSGC);
		rotateSGC.addChildren(bothSGC);
		regulusSGC.setVisible(show3D);
		
		regAp = new Appearance();
		leitAp = new Appearance();
		regAp.setAttribute("lineShader."+"diffuseColor", new Color(255,50,50));
		leitAp.setAttribute("lineShader."+"diffuseColor",new Color(50,150,255));	
		regFac.setNumberOfSamples(numRulings);
		regFacSGC = regFac.getRegulus();
		leitSharSGC = regFac.getLeitSchar();
		regFacSGC.setAppearance(regAp);
		leitSharSGC.setAppearance(leitAp);
		bothSGC.addChildren(regFacSGC, leitSharSGC);
		ap = bothSGC.getAppearance();
		ap.setAttribute(CommonAttributes.TUBES_DRAW,false);
		ap.setAttribute(CommonAttributes.LINE_WIDTH, 1.0);
		ap.setAttribute(CommonAttributes.TUBE_RADIUS, .02);
		bothSGC.setPickable(false);
		bothSGC.setVisible(false);

		
		regAp = new Appearance();
		leitAp = new Appearance();
		regAp.setAttribute("diffuseColor", new Color(255,50,50));
		leitAp.setAttribute("diffuseColor",new Color(50,150,255));	
		regulusSGC.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
		pathToRegulus = SceneGraphUtility.getPathsBetween(regulusSGC, rotateSGC).get(0);
		rotateSGC.addTool(new de.jreality.tools.RotateTool());
		MatrixBuilder.euclidean().translate(0,0,depth).assignTo(regulusSGC);
		rotateSGC.getTransformation().addTransformationListener(new TransformationListener() {
			
			@Override
			public void transformationMatrixChanged(TransformationEvent ev) {
				update();
			}
		});
		init();

		pointsSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		pointsSGC.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS, 0.03);
		pointsSGC.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.black);
		pointsSGC.getAppearance().setAttribute(CommonAttributes.TEXT_SHADER+"."+CommonAttributes.TEXT_SCALE, .003);
		pointsSGC.getAppearance().setAttribute(CommonAttributes.TEXT_SCALE, .003);

		conicSGC.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.unitCube);

		world.getAppearance().setAttribute(CommonAttributes.SMOOTH_SHADING, false);
		world.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
//		world.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		world.getAppearance().setAttribute(CommonAttributes.DIFFUSE_COLOR	, Color.white);
//		world.getAppearance().setAttribute("lineShader.lineWidth", 2.5);
		update();
		
		Light dl = new DirectionalLight();
		dl.setIntensity(1.0);
		world2.setLight(dl);
		
		clip1SGC.setGeometry(new ClippingPlane());
		clip2SGC.setGeometry(new ClippingPlane());
		updateClippingPlanes();
		
		MatrixBuilder.euclidean().translate(0,0,-8).assignTo(world2);


		return world;
	}
	private void updateClippingPlanes() {
		double amt = (epsilon > 1 ? 10E8 : epsilon);
		MatrixBuilder.euclidean().translate(0,0,-8-amt).scale(-1).assignTo(clip1SGC);
		MatrixBuilder.euclidean().translate(0,0,-8+amt).assignTo(clip2SGC);
	}
	/** build the regulus first, then slice it to get a conic
	 * Start with a 2x3 set of lines, the middle pair is the x-, resp., y-axis
	 * Then on either side of the x-axis, a pair of lines lying on a
	 * helix that includes the x-axis, and similarly for the y-axis.
	 * Each of x- group meets each of the y-group, so form the
	 * two guide-lines of the regulus.  
	*/
	protected void init()	{
		points4 = new double[18][4];
		points3 = Utility.demote(points3, points4);
		
		pointsFactory = new PointSetFactory();
		pointsFactory.setVertexCount(points4.length);
		pointsFactory.setVertexCoordinates(points4);
		pointsFactory.setVertexLabels(new String[]{
				"A","B","C","A'","B'","C'",
				"A''","B''","C''","0''","1''","2''",
				"0","1","2","0'","1'","2'"
		});
		Color[] pointColors = new Color[]{y,y,y,y,y,y,c,c,c,c,c,c,m,gr,vi,m,gr,vi};

		pointsFactory.setVertexColors(pointColors);
		pointsFactory.update();
		pointsSGC.setGeometry(pointsFactory.getPointSet());

		updateReglines();

		Color[] colors = new Color[]{m,gr,vi,m,gr,vi, Color.blue, m, gr, vi};
		Boolean[] stipple = {true, true, true, true, true, true, false, false, false, false};
		int[] stippleVals = {127, 127, 127, 127, 127, 127, 15*257,165*257, 234*257, 15*257, 165*257, 234*257,0, 0, 0, 0};
		for (int i = 0; i<10; ++i)	{
			lineFactories[i] = new PointRangeFactory(); 
			lineFactories[i].setFiniteSphere(true);
			lineFactories[i].setSphereRadius(sphereRadius);
			lineFactories[i].setElement0(points4[i]);
			lineFactories[i].setElement1(points4[(i+1)%6]);
			lineFactories[i].update();
			SceneGraphComponent child = new SceneGraphComponent("line"+i);
			linesSGC.addChild(child);
			child.setAppearance(new Appearance());
			child.getAppearance().setAttribute("lineShader.diffuseColor", colors[i]);
			child.getAppearance().setAttribute(CommonAttributes.LINE_STIPPLE, stipple[i]);
			child.getAppearance().setAttribute(CommonAttributes.LINE_STIPPLE_PATTERN, stippleVals[i]);
			child.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.unitCube);
			child.setGeometry(lineFactories[i].getLine());
		}
		for (int i = 0; i<6; ++i)	{
			regLineFactories[i] = new PointRangeFactory(); 
			regLineFactories[i].setFiniteSphere(true);
			regLineFactories[i].setSphereRadius(sphereRadius);
			regLineFactories[i].setPluckerLine(regLines[i]);
			regLineFactories[i].update();
			SceneGraphComponent child = new SceneGraphComponent("line"+i);
			child.setAppearance(new Appearance());
			child.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.unitCube);
			child.setAppearance( ((i%2) == 0) ? regAp : leitAp);
			rotateSGC.addChild(child);
			child.setGeometry(regLineFactories[i].getLine());
		}
	}

	double[] xaxis = {1,0,0,0}, yaxis = {0,1,0,0};
	double[] fixedPlane = {0,0,1,0};  // z=0 plane
	double pitch = Math.PI/4, lean = 0;
	int[] hexagoner = {0,1,2,3,4,5},
			doubleDiamonds = {0,1,2,5,4,3},
			atomMull = {0,1,2,4,5,3};

	int pascalTris[][] = {{0,1,2},{0,2,1},{1,0,2},{1,2,0},{2,0,1},{2,1,0}},
			pascalTri[] = pascalTris[0];
	int[][] hexagon = new int[6][2];
	boolean[] showFaces = {false, false, false, false, false, false};
	int[][] pascalIndices = {
			{12, 13, 17},
			{15, 16, 14},
			{13, 14, 12},
			{16, 17, 15},
			{14, 15, 13},
			{17, 12, 16}
	};
	Color[] pascalColors = { gr, gr, vi, vi, m, m};
	
	protected void update()	{
		updateReglines();
		
		points4 = new double[show3D ? 18 : 9][4];
		// find intersections with fixed z=0 plane
		for (int i = 0; i<6; ++i)	{
			points4[i] = PlueckerLineGeometry.lineIntersectPlane(null, regLinesW[i], fixedPlane);
			Pn.dehomogenize(points4[i], points4[i]);
		}
		points3 = Utility.demote(points3, points4);
		for (int i = 0; i<6; ++i)	{
			lineFactories[i].setElement0(points4[i]);
			lineFactories[i].setElement1(points4[(i+1)%6]);
			lineFactories[i].update();
			// we need five 2D lines depending on the first five points in a pentagram arrangement
			// to generate the conic
			if (i < 5) lines3[i] = P2.lineFromPoints(null, points3[i%5], points3[(i+1)%5]);
		}
		
		for (int i = 0; i<3; ++i)	{
			int index = (i+1)%3;
			points4[6+i] = PlueckerLineGeometry.intersectionPoint(null, 
					lineFactories[index].getPluckerLine(), 
					lineFactories[index+3].getPluckerLine());
		}
		pointsFactory.setVertexCount(points4.length);
		if (show3D) {
			// points[9-11] are the three points of the three "unused" planes of the regulus
			for (int i = 0; i<3; ++i)	{
				points4[i+9] = PlueckerLineGeometry.intersectionPoint(null, regLinesW[i], regLinesW[i+3]);
			}
			for (int i = 0; i<6; ++i)	{
				points4[i+12] = PlueckerLineGeometry.intersectionPoint(null, regLinesW[(i+1)%6], regLinesW[(i+2)%6]);
			}			
			pointsFactory.setVertexLabels(new String[]{
					"A","B","C","A'","B'","C'",
					"A''","B''","C''","0''","1''","2''",
					"0","1","2","0'","1'","2'"
			});
			Color[] pointColors = new Color[]{y,y,y,y,y,y,c,c,c,c,c,c,m,gr,vi,m,gr,vi};
			pointsFactory.setVertexColors(pointColors);
		} else {
			pointsFactory.setVertexLabels(new String[]{
					"A","B","C","A'","B'","C'",
					"A''","B''","C''"
			});
			Color[] pointColors = new Color[]{y,y,y,y,y,y,c,c,c};
			pointsFactory.setVertexColors(pointColors);
		}
		Pn.dehomogenize(points4, points4);
		pointsFactory.setVertexCoordinates(points4);

		pointsFactory.update();

		// get the pascal line
		lineFactories[6].setElement0(points4[6]);
		lineFactories[6].setElement1(points4[7]);
		lineFactories[6].update();

		// these are the lines bounding the pascal triangle in 3-space
		if (show3D)	{
			for (int i = 0; i<3; ++i)	{
				lineFactories[i+7].setElement0(points4[i+9]);
				lineFactories[i+7].setElement1(points4[9+((i+1)%3)]);
				lineFactories[i+7].update();
			}			
		}
		
		if (show3D) updatePascalTriangle();

		updateConic();
		
		updateVisibility();
	}
	private void updateVisibility() {
		pascalTriSGC.setVisible(show3D);
		regulusSGC.setVisible(show3D);
		clip1SGC.setVisible(show3D);
		clip2SGC.setVisible(show3D);
		if (show3D)	{
			world.addChildren(clip1SGC, clip2SGC);
		} else {
			world.removeChild(clip1SGC);
			world.removeChild(clip2SGC);
		}
		for (int i = 0; i<3; ++i)	{
			linesSGC.getChildComponent(i+7).setVisible(show3D);
		}
	}
	private void updateConic() {
		conic.setInitialPoints(points3);
		conicPoints3 = new double[numPoints][];
		for (int i = 0; i<numPoints; ++i)	{
			double t = i *(1.0/(numPoints));
			conicPoints3[i] = conic.getValueAtTime(null, t);
		}
		conicPoints4 = Utility.promote(conicPoints4, conicPoints3);
		IndexedLineSet conicILS = IndexedLineSetUtility.createCurveFromPoints(conicPoints4, true);
		conicSGC.setGeometry(conicILS);
	}
	private void updatePascalTriangle() {
		// update the pascal triangle geometry
		if (pascalTriFac == null)	{
			pascalTriFac = new IndexedFaceSetFactory();
			pascalTriFac.setVertexCount(points4.length);
			pascalTriFac.setFaceCount(6);
			pascalTriFac.setFaceIndices(new int[][] {
				{12, 10, 11},
				{15, 11, 10},
				{13, 11, 9},
				{16, 9, 11},
				{14, 9, 10},
				{17, 10, 9}
			});
			pascalTriFac.setFaceColors(new Color[]{ gr, gr, vi, vi, m, m});
			pascalTriFac.setGenerateFaceNormals(true);
		}
		pascalTriFac.setVertexCoordinates(points4);
		int count = 0;
		for (int i = 0; i<6; ++i) {
			if (showFaces[i]) count++;
		}
		int[][] inds = new int[count][];
		Color[] fc = new Color[count];
		count = 0;
		for (int i = 0; i<6; ++i) {
			if (showFaces[i]) {
				inds[count] = pascalIndices[i];
				fc[count] = pascalColors[i];
				count++;
			}
		}
		pascalTriFac.setFaceCount(count);
		pascalTriFac.setFaceIndices(inds);
		pascalTriFac.setFaceColors(fc);
		
		pascalTriFac.update();
		pascalTriSGC.setGeometry(pascalTriFac.getGeometry());
	}

	private void updateReglines() {
		double[][][] rawLines = {
				{regLineAt(null, Math.PI*2/3.0),
				regLineAt(null, 0),
				regLineAt(null, -Math.PI*2/3.0)},
				{leitLineAt(null,  Math.PI*2/3.0),
				leitLineAt(null,0),
				leitLineAt(null, -Math.PI*2/3.0)}};
	
		hexagon =  hexagonForPascalTriangle(hexagon, pascalTri);
		for (int i = 0; i<6; ++i)	{
			int j = hexagon[i][0], k = hexagon[i][1];
			regLines[i] = ((i%2)==0) ? rawLines[0][j] : rawLines[1][k]; 
		}

		// create the regulus
		regFac.setElement0(rawLines[0][0]);
		regFac.setElement1(rawLines[0][1]);
		regFac.setElement2(rawLines[0][2]);
		regFac.setNumberOfSamples(numRulings);
		regFac.setSphereRadius(sphereRadius);
		regFac.update();			
		
		// transform according to the scene graph
		Matrix regM = new Matrix();
		regM.assignFrom(pathToRegulus.getMatrix(null));
		double[] lineTform = PlueckerLineGeometry.inducedP5ProjFromP3Proj(null, regM.getArray());
//		System.err.println("tform = \n"+Rn.toString(lineTform));
		// these lines (in world coords) are used to determine the conic in the fixed world plane z=1
		for (int i =0; i<6; ++i)	{
			regLinesW[i] = Rn.matrixTimesVector(regLinesW[i], lineTform, regLines[i]);
		}
		if (regLineFactories[0] == null) return;
		for (int i =0; i<6; ++i)	{
			regLineFactories[i].setPluckerLine(regLines[i]);
			regLineFactories[i].update();	
		}
	}
	
	private double[] regLineAt(double[] ret, double position) {
		return regLineAt(ret, position, pitch, lean);
	}
	
	private double[] leitLineAt(double[] ret, double position) {
		return regLineAt(ret, position, -pitch, -lean);
	}

	private double[] regLineAt(double[] ret, double position, double p, double l) {
		double c = Math.cos(position), s = Math.sin(position),
				c2 = Math.cos(p), s2 = Math.sin(p);
		double[] point = {c,s,0,1},
				direction = {l,c2,s2,0};
		Matrix m = new Matrix();
		MatrixBuilder.euclidean().rotate(position, 0, 0, 1).assignTo(m);
		direction = m.multiplyVector(direction);
		ret = PlueckerLineGeometry.lineFromPoints(ret,  direction, point);
		return ret;
	}
	
	private int[][] hexagonForPascalTriangle(int[][] is, int[] p) {
		if (is == null) is = new int[6][2];
		int[][] used = new int[3][3];
		for (int i = 0; i<3; ++i)	{
			used[i][p[i]] = 1;
		}
		System.err.println("in hexagonForPT");
//		m[0][(p[0]+1)%3] = 1;
		boolean onRow = true;
		int row=0, column = 0;
		for (int i = 0; i<6; ++i)	{
			if (onRow) {
				while(used[row][column] == 1)
					column = (column+1)%3;
				used[row][column] = 1;
				is[i][0] = row;
				is[i][1] = column;
			} else {
				while(used[row][column] == 1)
					row = (row+1)%3;
				used[row][column] = 1;
				is[i][0] = row;
				is[i][1] = column;
			}
//			System.err.println("got entry "+is[i][0]+":"+is[i][1]);
			onRow = !onRow;
		}
		return is;
	}
	int counter = 0;
	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.white);
		Camera cam = CameraUtility.getCamera(jrviewer.getViewer());
		cam.setFocus(8.0);
		cam.setEyeSeparation(0.5);
		SceneGraphComponent camNode = CameraUtility.getCameraNode(jrviewer.getViewer());
//		PointLight dl = new PointLight();
//		dl.setIntensity(.5);
//		camNode.setLight(dl);
		Component comp = ((Component) jrviewer.getViewer().getViewingComponent());
		comp.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e)	{ 
				switch(e.getKeyCode())	{
					
				case KeyEvent.VK_H:
					System.err.println("	1: cycle types");
					break;
	
				case KeyEvent.VK_1:
					counter = (counter+1)%pascalTris.length;
					pascalTri = pascalTris[counter];
					update();
					
				case KeyEvent.VK_2:
					counter = (counter+1)%pascalTris.length;
					pascalTri = pascalTris[counter];
					update();
				}
			}
		});
	}

	@Override
	public Component getInspector() {
		if (bothSGC == null) getContent();
		Box inspectionPanel = inspector;
		Box hbox = Box.createHorizontalBox();
		inspectionPanel.add(hbox);
		for (int i = 0; i<6; ++i)	{
			JCheckBox cb = new JCheckBox(String.format("%1d", i));
			hbox.add(cb);
			cb.setSelected(showFaces[i]);
			final int j = i;
			cb.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					showFaces[j] = ((JCheckBox) e.getSource()).isSelected();
					update();
				}
			});	
		}
		hbox = Box.createHorizontalBox();
		inspectionPanel.add(hbox);
		JCheckBox cb = new JCheckBox("show 3D");
		hbox.add(cb);
		cb.setSelected(regulusSGC.isVisible());
		cb.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				show3D = ((JCheckBox) e.getSource()).isSelected();
				update();
			}
		});	
		cb = new JCheckBox("show regulus");
		hbox.add(cb);
		cb.setSelected(bothSGC.isVisible());
		cb.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean b = ((JCheckBox) e.getSource()).isSelected();
				bothSGC.setVisible(b);
			}
		});	
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
		final TextSlider depthSlider = new TextSlider.Double("depth",SwingConstants.HORIZONTAL, -2.0, 5.0, depth);
		depthSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				depth = depthSlider.getValue().doubleValue();
				MatrixBuilder.euclidean().translate(0,0,depth).assignTo(regulusSGC);

				update();
			}
		});
		inspectionPanel.add(depthSlider);
		final TextSlider clipSlider = new TextSlider.Double("clip amount",SwingConstants.HORIZONTAL, 0.0, 2, epsilon);
		clipSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				epsilon = clipSlider.getValue().doubleValue();
				updateClippingPlanes();
			}
		});
		inspectionPanel.add(clipSlider);
		final TextSlider radiusSlider = new TextSlider.Double("radius",SwingConstants.HORIZONTAL, 2, 100, sphereRadius);
		radiusSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				sphereRadius = radiusSlider.getValue().doubleValue();
				System.err.println("changed radius");
				for (int i = 0; i<6; ++i)	{
					regLineFactories[i].setSphereRadius(sphereRadius);
					regLineFactories[i].update();
					lineFactories[i].setSphereRadius(sphereRadius);
					lineFactories[i].update();
				}
			}
		});
		inspectionPanel.add(radiusSlider);
		final TextSlider numRulingsSlider = new TextSlider.Integer("# rulings",SwingConstants.HORIZONTAL, 1,200, numRulings);
		numRulingsSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				numRulings = numRulingsSlider.getValue().intValue();
				update();
			}
		});
		inspectionPanel.add(numRulingsSlider);
		return inspectionPanel;
	}
	public static void main(String[] args) {
		new DandelinConfiguration2().display();
	}
}

//regLines[0] = PlueckerLineGeometry.lineFromPoints(null, 
//new double[]{1,0,0,1}, 
//new double[]{0,c,s,0});
//regLines[1] = PlueckerLineGeometry.lineFromPoints(null, P3.originP3, yaxis);
//regLines[2] = PlueckerLineGeometry.lineFromPoints(null, 
//new double[]{-asymmetry,0,0,1}, 
//new double[]{0,c,-s,0});
//regLines[4] = PlueckerLineGeometry.lineFromPoints(null, P3.originP3, xaxis);
//double[] planeThruLine1 = new double[]{-s,0,c,0},
//pointOnLine0 = PlueckerLineGeometry.lineIntersectPlane(null, regLines[0], planeThruLine1),
//pointOnLine2 = PlueckerLineGeometry.lineIntersectPlane(null, regLines[2], planeThruLine1);
//regLines[3] = PlueckerLineGeometry.lineFromPoints(null, pointOnLine0, pointOnLine2);
//planeThruLine1 = new double[]{s,0,c,0};
//pointOnLine0 = PlueckerLineGeometry.lineIntersectPlane(null, regLines[0], planeThruLine1);
//pointOnLine2 = PlueckerLineGeometry.lineIntersectPlane(null, regLines[2], planeThruLine1);
//regLines[5] = PlueckerLineGeometry.lineFromPoints(null, pointOnLine0, pointOnLine2);

//double[][][]
//regpts3x3 = new double[3][3][],	// array of 3x3 points on regulus
//regpln3x3 = new double[3][3][];
//
//// find the intersection points and joining planes of the two sets of lines
//for (int i = 0; i<3; ++i)	{
//for (int j = 0; j<3; ++j)	{
//regpts3x3[i][j] = PlueckerLineGeometry.intersectionPoint(null, rawLines[0][i], rawLines[1][j]);
//regpln3x3[i][j] = PlueckerLineGeometry.intersectionPlane(null, rawLines[0][i], rawLines[1][j]);
//}
//regpts3x3[i] = Rn.matrixTimesVector(regpts3x3[i], regM.getArray(), regpts3x3[i]);
//}

