/*
 * Created on Dec 1, 2010
 *
 */
package charlesgunn.jreality.worlds.projective;

import static de.jreality.geometry.GeometryUtility.BOUNDING_BOX;
import static de.jreality.shader.CommonAttributes.LIGHTING_ENABLED;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.SwingConstants;

import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.Utility;
import charlesgunn.math.clifford.ConicSection;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Matrix;
import de.jreality.math.P2;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.basic.Shell;
import de.jreality.plugin.basic.ViewPreferences;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.experimental.ViewerKeyListenerPlugin;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.PickUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.jrworkspace.plugin.Plugin;

public class Complete4Point4Side extends Assignment {

	transient double[] tform = {1,.5,0,1, -1,.5,0,1, 0,0,1,0,  0,-.5,0,1};
	double t = .3333;
	private SceneGraphComponent world,
			fourPtsSGC,
			sixLinesSGC,
				sixLinesLabelsSGC,
			threePtsSGC,
			threeLinesSGC,
				threeLinesLabelsSGC,
			sixPtsSGC,
			fourLinesSGC,
				fourLinesLabelsSGC,
			ptsSGC,
			lnsSGC,
		conicCurveSGC;
	int numPoints = 500;
	Color[] colors1 = {Color.red, Color.yellow, Color.blue, Color.green, Color.magenta, Color.cyan};
	Color[] colors2 = {Color.green, Color.magenta, Color.cyan, Color.red, Color.yellow, Color.blue};
	ConicSection conicSection = new ConicSection();
	@Override
	public SceneGraphComponent getContent() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		ptsSGC = SceneGraphUtility.createFullSceneGraphComponent("pts");
		lnsSGC = SceneGraphUtility.createFullSceneGraphComponent("lns");
		fourPtsSGC = SceneGraphUtility.createFullSceneGraphComponent("4pts");
		sixLinesSGC = SceneGraphUtility.createFullSceneGraphComponent("6lns");
		sixLinesLabelsSGC = SceneGraphUtility.createFullSceneGraphComponent("6lnsLbls");
		threePtsSGC = SceneGraphUtility.createFullSceneGraphComponent("3pts");
		threeLinesSGC = SceneGraphUtility.createFullSceneGraphComponent("3lns");
		threeLinesLabelsSGC = SceneGraphUtility.createFullSceneGraphComponent("3lnsLbls");
		sixPtsSGC = SceneGraphUtility.createFullSceneGraphComponent("6pts");
		fourLinesSGC = SceneGraphUtility.createFullSceneGraphComponent("4lns");
		fourLinesLabelsSGC = SceneGraphUtility.createFullSceneGraphComponent("4lnsLbls");
		conicCurveSGC = SceneGraphUtility.createFullSceneGraphComponent("conic");
		world.addChildren(ptsSGC, lnsSGC, conicCurveSGC);
		ptsSGC.addChildren(fourPtsSGC, sixPtsSGC, threePtsSGC);
		lnsSGC.addChildren(sixLinesSGC, fourLinesSGC, threeLinesSGC);
		fourPtsSGC.addChild(sixLinesLabelsSGC);
		sixPtsSGC.addChild(fourLinesLabelsSGC);
		threePtsSGC.addChild(threeLinesLabelsSGC);
		Appearance ap = world.getAppearance();
		ap = world.getAppearance();
//		ap.setAttribute("lineShader.lineWidth", 1.0);
		ap.setAttribute(LIGHTING_ENABLED, false);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(BOUNDING_BOX, Rectangle3D.unitCube);
		ap.setAttribute(CommonAttributes.TEXT_SHADER+"."+CommonAttributes.TEXT_SCALE, .0015);
		ap.setAttribute(CommonAttributes.TEXT_SCALE, .0015);
		ap = ptsSGC.getAppearance();
		ap.setAttribute(VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.SPHERES_DRAW, true);
		ap.setAttribute(CommonAttributes.POINT_RADIUS, .015);
		ap = lnsSGC.getAppearance();
		lnsSGC.setPickable(false);
		ap.setAttribute(VERTEX_DRAW, false);
//		ap.setAttribute("lineShader.lineWidth", 1.0);
		ap.setAttribute("lineShader.diffuseColor", new Color(50, 50, 50));
		// tubes are broken due to the fact we're doing a lot at the line at infinity
		Matrix mm = new Matrix(tform);
		mm.transpose();
//		mm.assignTo(world.getTransformation());
		fourPtsSGC.addTool(editTool);
		init();
		update();
		return world;
	}
	double[][] basis = {{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}};
	transient int[][] pairs6 = {{0,1},{1,2},{2,3},{3,0},{0,2},{1,3}},  // six lines determined by 4 points
			pairs3 = {{0,2},{1,3},{4,5}};							  // three diag. points determined by six lines
	transient double[] tang1 = {1,0,-1};
	// the whole figure is determined from these points
	transient double[][] points4 = {
			{0.140914,	0.407256	, 1.00000	},	
			{-0.227068,	0.426046	, 1.00000	},	
			{-0.531136,	0.0727796,	1.00000 },		
			{0.342219,	-0.267891,	1.00000	}
	}, 
			//{1,0,1},{0,1,1},{-1,0,1},{0,-1,1}}, 
			points4d,
			lines6 = new double[6][],
			points3 = new double[3][],
			lines3 = new double[3][],
			points6 = new double[6][],
			lines4 = new double[4][];
	
	PointSetFactory pt4Fac = new PointSetFactory(),
			pt3Fac = new PointSetFactory(),
			pt6Fac = new PointSetFactory(),
			lnLbl4Fac = new PointSetFactory(),
			lnLbl6Fac = new PointSetFactory(),
			lnLbl3Fac = new PointSetFactory();
	private void init() {
		// update the points and lines
		pt4Fac.setVertexCount(4);
		pt4Fac.setVertexLabels(new String[]{"A", "B", "C", "D"});
//		pt4Fac.setGenerateVertexLabels(true);
		pt6Fac.setVertexCount(6);
		pt6Fac.setVertexLabels(new String[]{"S", "T", "U", "V","W","X"});
		pt3Fac.setVertexCount(3);
		pt3Fac.setVertexLabels(new String[]{"P", "Q", "R"});
		
		lnLbl4Fac.setVertexCount(4);
		lnLbl4Fac.setVertexLabels(new String[]{"a", "b", "c", "d"}); // tangent lines
		lnLbl6Fac.setVertexCount(6);
		lnLbl6Fac.setVertexLabels(new String[]{"s", "t", "u", "v","w","x"}); // six lines of complete 4-point
		lnLbl3Fac.setVertexCount(3);
		lnLbl3Fac.setVertexLabels(new String[]{"p", "q", "r"}); // diagonal lines
		for (int i = 0; i<4; ++i)	{
			SceneGraphComponent child = new SceneGraphComponent("pts4"+i);
			fourPtsSGC.addChild(child);
			child = new SceneGraphComponent("lines4"+i);
			fourLinesSGC.addChild(child);
		}
		for (int i = 0; i<6; ++i)	{
			SceneGraphComponent child = new SceneGraphComponent("lines6"+i);
			sixLinesSGC.addChild(child);
			child = new SceneGraphComponent("pts6"+i);
			sixPtsSGC.addChild(child);
		}
		for (int i = 0; i<3; ++i)	{
			SceneGraphComponent child = new SceneGraphComponent("points3"+i);
			threePtsSGC.addChild(child);
			child = new SceneGraphComponent("lines3"+i);
			threeLinesSGC.addChild(child);
		}
		Color diagonalColor = new Color(0,0, 250),
				pointsColor = new Color(0,150,0),
				pointsLColor = new Color(0, 75, 0),
				tangentsColor = new Color(130,0,50),
				tangentsPColor = new Color(250,0,70),
				conicColor = new Color(150,50,250);
		threePtsSGC.getAppearance().setAttribute("pointShader.diffuseColor",  diagonalColor);
		threeLinesSGC.getAppearance().setAttribute("lineShader.diffuseColor",  diagonalColor);
		fourPtsSGC.getAppearance().setAttribute("pointShader.diffuseColor", pointsColor);
		sixLinesSGC.getAppearance().setAttribute("lineShader.diffuseColor", pointsLColor);
		sixPtsSGC.getAppearance().setAttribute("pointShader.diffuseColor", tangentsPColor);
		fourLinesSGC.getAppearance().setAttribute("lineShader.diffuseColor", tangentsColor);
		threeLinesSGC.getAppearance().setAttribute(CommonAttributes.LINE_STIPPLE, true);
		threeLinesLabelsSGC.getAppearance().setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, 0.0);
		threeLinesLabelsSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		fourLinesLabelsSGC.setAppearance(threeLinesLabelsSGC.getAppearance());
		sixLinesLabelsSGC.setAppearance(threeLinesLabelsSGC.getAppearance());
		threeLinesSGC.getAppearance().setAttribute(CommonAttributes.LINE_STIPPLE_PATTERN, 341);
		fourLinesSGC.getAppearance().setAttribute(CommonAttributes.LINE_STIPPLE, true);
		conicCurveSGC.getAppearance().setAttribute("lineShader.diffuseColor", conicColor);
	}
	private void update() {
		double[][] points4d1 = Utility.promote(null, points4);
		System.err.println("points = "+Rn.toString(points4));
		pt4Fac.setVertexCoordinates(points4d1);
		pt4Fac.update();
		fourPtsSGC.setGeometry(pt4Fac.getGeometry());
		
		for (int i = 0; i<6; ++i)	{
			int i0 = pairs6[i][0];
			int i1 = pairs6[i][1];
			lines6[i] = P2.lineFromPoints(null, points4[i0], points4[i1]);
			IndexedLineSet ils = PointRangeFactory.line(points4d1[i0], points4d1[i1]);
			sixLinesSGC.getChildComponent(i).setGeometry(ils);
		}
		// figure out where to put the labels
		double[][] pts6tmp = new double[6][];
		double[] ff = {1.8, -.75, -.5, 1.5, 1.3, -.5};
		Pn.dehomogenize(points4, points4);
		for (int i = 0; i<6; ++i)	{
			int i0 = pairs6[i][0];
			int i1 = pairs6[i][1];
			pts6tmp[i] = Rn.linearCombination(null, ff[i], points4[i0], 1-ff[i], points4[i1]);
		}
		lnLbl6Fac.setVertexCoordinates(Utility.promote(null, pts6tmp));
		lnLbl6Fac.update();
		sixLinesLabelsSGC.setGeometry(lnLbl6Fac.getGeometry());

		for (int i = 0; i<3; ++i)	{
			points3[i] = P2.pointFromLines(null, lines6[pairs3[i][0]], lines6[pairs3[i][1]]);
		}
		Pn.dehomogenize(points3, points3);
		points4d1 = Utility.promote(null, points3);
		pt3Fac.setVertexCoordinates(points4d1);
		pt3Fac.update();
		threePtsSGC.setGeometry(pt3Fac.getGeometry());
		for (int i = 0; i<3; ++i)	{
			lines3[i] = P2.lineFromPoints(null, points3[i], points3[(i+1)%3]);
			IndexedLineSet ils = PointRangeFactory.line(points4d1[i], points4d1[(i+1)%3]);
			threeLinesSGC.getChildComponent(i).setGeometry(ils);
		}
		// draw the labels on the diag lines
		double[][] pts3tmp = new double[3][];
		ff = new double[]{-1, .4, -.5};
		for (int i = 0; i<3; ++i)	{
			pts3tmp[i] = Rn.linearCombination(null, ff[i], points3[(i+1)%3], 1-ff[i], points3[(i+2)%3]);
		}
		lnLbl3Fac.setVertexCoordinates(Utility.promote(null, pts3tmp));
		lnLbl3Fac.update();
		threeLinesLabelsSGC.setGeometry(lnLbl3Fac.getGeometry());

		// draw the tangents
		// first tangent is join of first point with linear combination of P and Q (diagonal triangle)
		double[][][] tangentPts = new double[4][2][0];
		Pn.dehomogenize(points3, points3);
		double[] tmp = Rn.linearCombination(null, t, points3[0], 1-t, points3[1]);
		Pn.dehomogenize(tmp, tmp);
		tangentPts[0][0] = tangentPts[2][0] = tmp;
		tangentPts[0][1] = points4[0];
		tangentPts[2][1] = points4[2];
		lines4[0] = P2.lineFromPoints(null, tmp, points4[0]);
		lines4[2] = P2.lineFromPoints(null, tmp, points4[2]);
		double[] tmp2 = Rn.linearCombination(null, -t, points3[0], 1-t, points3[1]);
		Pn.dehomogenize(tmp2, tmp2);
		tangentPts[1][0] = tangentPts[3][0] = tmp2;
		tangentPts[1][1] = points4[1];
		tangentPts[3][1] = points4[3];
		lines4[1] = P2.lineFromPoints(null, tmp2, points4[1]);
		lines4[3] = P2.lineFromPoints(null, tmp2, points4[3]);
		System.err.println("tangents = "+Rn.toString(lines4));
		double[][] fourByTwo = {tmp, points4[0],tmp2, points4[1], tmp, points4[2],tmp2, points4[3]},
				fourByTwo4d = Utility.promote(null, fourByTwo);
		for (int i = 0; i<4; ++i)	{
			IndexedLineSet ils = PointRangeFactory.line(fourByTwo4d[2*i], fourByTwo4d[2*i+1]);
			fourLinesSGC.getChildComponent(i).setGeometry(ils);
		}
		// draw the labels on the tangent lines
		double[][] pts4tmp = new double[4][];
		ff = new double[]{.75, .25, .75, .15};
		for (int i = 0; i<4; ++i)	{
			pts4tmp[i] = Rn.linearCombination(null, ff[i], tangentPts[i][0], 1-ff[i],tangentPts[i][1]);
		}
		lnLbl4Fac.setVertexCoordinates(Utility.promote(null, pts4tmp));
		lnLbl4Fac.update();
		fourLinesLabelsSGC.setGeometry(lnLbl4Fac.getGeometry());

		// determine the six points determined by the tangents
		for (int i = 0; i<6; ++i)	{
			int i0 = pairs6[i][0];
			int i1 = pairs6[i][1];
			points6[i] = P2.pointFromLines(null, lines4[i0], lines4[i1]);
		}
//		Pn.dehomogenize(points6, points6);
		points4d1 = Utility.promote(null, points6);
		pt6Fac.setVertexCoordinates(points4d1);
		pt6Fac.update();
		sixPtsSGC.setGeometry(pt6Fac.getGeometry());
		
		
		// the conic section is determined by
		// P = A
		// a = BD
		// M = P
		// b = tangent at B
		// Q = C
		double[][] PaMbQ = {
				points4[0],	 // A
				lines6[5],   // Pluecker index 13
				points3[0],  // meet of AB and CD
				lines4[1],	// tangent at B
				points4[2]	// C
		};
		conicSection.setFivePerspectivities(PaMbQ);
		updateConic();
	}
	
	int np = 200;
	IndexedLineSetFactory connie = null;
	protected void updateConic() {
		
		double  conic[][] = new double[np][];
		for (int i = 0; i<np; ++i)	{
			conic[i] = conicSection.getValueAtTime(conic[i], (.023 + i)/np);
//			System.err.println("proj: "+Rn.toString(new double[][]{pencil[i],Pa,aM,Mb,bQ,PQ}));
		}
		conic = Utility.promote(null, conic);
//		System.err.println("pencil = "+Rn.toString(pencil));
//		System.err.println("conic = "+Rn.toString(conic));
//		conic = Utility.promote(null, conic);
		if (connie == null) {
			connie = IndexedLineSetUtility.createCurveFactoryFromPoints(conic, true);
			conicCurveSGC.setGeometry(connie.getGeometry());	
			conicCurveSGC.setPickable(false);
		}
		else {
			connie.setVertexAttribute(Attribute.COORDINATES,  conic);
			connie.update();
		}
	}
	
	

	@Override
	public List<Plugin> getPluginsToRegister() {
			pluginsToLoad.add(new Shell());
			pluginsToLoad.add(contentPlugin);
//			pluginsToLoad.add(new ContentTools());
			pluginsToLoad.add(new ContentLoader());
			pluginsToLoad.add(new ViewPreferences());
			animationPlugin = new AnimationPlugin();
			pluginsToLoad.add(animationPlugin);
			pluginsToLoad.add(new ViewerKeyListenerPlugin());
			pluginsToLoad.add(shrinkPanelPlugin);
			return pluginsToLoad;

	}
	@Override
	public void display() {
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.white);		
		Camera cam = CameraUtility.getCamera(jrviewer.getViewer());
		cam.setFieldOfView(1.25 * cam.getFieldOfView());
		cam.setPerspective(false);
		
	}

	

	@Override
	public Component getInspector() {
		final TextSlider cSlider = new TextSlider.Double("t",SwingConstants.HORIZONTAL, 0.0, 1.0, t);
		cSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				t = cSlider.getValue().doubleValue();
				update();
			}
		});
		inspector.add(cSlider);
		return inspector;
	}

	public static void main(String[] args) {
		new Complete4Point4Side().display();
	}
	
	transient Tool editTool = new AbstractTool(InputSlot.LEFT_BUTTON, InputSlot.SHIFT_LEFT_BUTTON) {
		
		boolean active = false,		// a security flag to avoid editing in an inconsistent state
				deleting = false;
		int toDelete;
		@Override
		public void activate(ToolContext tc) {
			super.activate(tc);
			// usually it's sufficient to get the closest hit using tc.getCurrentPick()
			// but in this case, we always want to choose the closest hit on a vertex if there is such a hit;
			// otherwise we accept the hit on the closest edge
			List<PickResult> hitlist = tc.getCurrentPicks();
			if (hitlist == null || hitlist.size() == 0) return;
			PickResult pr = hitlist.get(0);
			// check to see if a vertex was hit; otherwise use the top pick
			for (PickResult foo : hitlist)	{
				if (pr != foo && foo.getPickType() == PickResult.PICK_TYPE_POINT) {
					System.err.println("Using point hit although edge was higher");
					pr = foo;
					break;
				}
			}
			if (pr == null || pr.getPickPath() == null || 
					pr.getPickPath().getLastElement() != pt4Fac.getGeometry()) return;
			
			if (tc.getSource() == InputSlot.LEFT_BUTTON)
				System.err.println("source is left button");
			if (tc.getSource() == InputSlot.SHIFT_LEFT_BUTTON) {
				System.err.println("source is shift-left button");
				if (pr.getPickType() == PickResult.PICK_TYPE_POINT) {
					deleting = true;
					toDelete = pr.getIndex();						
				}
			}
			// we have a valid hit; proceed with the algorithm
			active = true;
			// to receive events as the mouse moves need to add following input slot
			// then perform() will be called
			addCurrentSlot(InputSlot.POINTER_TRANSFORMATION);	

			points4d = pt4Fac.getPointSet().getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
			if (points4d[0].length == 4) points4d = Utility.demote(null, points4d);
			double[] newPoint = pr.getObjectCoordinates();
			// have to pay attention to whether we are working with homogeneous coordinates or not
			// *** The pick system always uses homogeneous coordinates to report pick coordinates ***
			final int fiber = points4d[0].length;
			if (fiber == 3)	{
				Pn.dehomogenize(newPoint, newPoint);
				// furthermore, we want to force the z-coordinate to be 0 since we know our curve lies in the z=0 plane
				// the actual pick point may have non-zero z-coordinate since it comes from the little sphere
				// representing the vertex
				newPoint = new double[]{newPoint[0], newPoint[1], 0};
			} 	else newPoint[2] = 0.0;
			// first handle the case that the user has clicked on an edge
			// here we insert a new vertex into the curve at that point
			// further picks in this activate/deactivate cycle will involve dragging this new point around
			if (pr.getPickType() == PickResult.PICK_TYPE_POINT) {
				perform(tc);
			}
			// for perform() we only allow picking of vertices
			// notice that we turn back on picking of edges in deactivate()
			PickUtility.setPickable(fourPtsSGC, true, false, false);
		}

		@Override
		public void perform(ToolContext tc) {
			if (!active || deleting) return;
			super.perform(tc);			// could be omitted since it's an empty method, but perhaps in the future ...
			PickResult pr = tc.getCurrentPick();
			// check that we have a valid pick
			if (pr == null || pr.getPickPath() == null || pr.getPickPath().getLastElement() != pt4Fac.getGeometry()) return;
			// just to be safe, check that it's a point pick
			if (pr.getPickType() == PickResult.PICK_TYPE_POINT) {
				System.err.println("Picked vertex "+pr.getIndex());
				double[] newPoint = pr.getObjectCoordinates();
				points4d = pt4Fac.getPointSet().getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
				if (points4d[0].length == 3)	{
					Pn.dehomogenize(newPoint, newPoint);
					newPoint = new double[]{newPoint[0], newPoint[1], newPoint[2]};
				} 	
				// force the z-coordinate to be 0
				newPoint[2] = 1.0;	
				// edit the coordinates of the picked point 
				// if there are fixed points first read the value at pr.getIndex() 
				// and return if it's value is within epsilon of a fixed point.
				points4d[pr.getIndex()] = newPoint;
				// and write the new results back into the IndexedLineSet
				points4 = Utility.demote(null, points4d);
//				Scene.executeWriter(fourPtsSGC, new Runnable() {
//					
//					@Override
//					public void run() {
//						pt4Fac.setVertexCoordinates(data);(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(points4d[0].length).createReadOnly(points4d));
//					}
//				});
				update();
			}
		}

		@Override
		public void deactivate(ToolContext tc) {
			super.deactivate(tc);
			// restore default states
			removeCurrentSlot(InputSlot.POINTER_TRANSFORMATION);
			PickUtility.setPickable(fourPtsSGC, true, true, false);
			active = false;
		}
		

	};

}
