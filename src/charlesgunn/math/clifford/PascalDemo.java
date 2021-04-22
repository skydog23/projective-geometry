package charlesgunn.math.clifford;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.SwingConstants;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.Utility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.PointSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P2;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.tutorial.app.AnimatedTextureExample;
import de.jreality.util.CameraUtility;
import de.jreality.util.PickUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.util.TextSlider;

/**
 * I adapted the tutorial program {@link AnimatedTextureExample} in  the following way:
 * 	I added a key listener to allow the user to:
 * 		restart the game of life simulation ('1' ), and
 * 		choose between a square or torus as the display geometry ('2')
 * 		display on-line documentation in browser ('3')
 * 
 * Assignment2:
 * @author Charles Gunn
 *
 */
public class PascalDemo extends Assignment {
	
	transient SceneGraphComponent worldSGC, 
		editablePointsSGC, 
		squareSGC, 
		conicSGC, 
		pascalSGC, 
		pascalPointsSGC,
		polePolarSGC,
		    pointSGC,
		    polarLineSGC;
	transient IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
	transient ConicSection conicSection = new ConicSection(),
			conicSection2 = new ConicSection();
	transient int np = 200;
	transient IndexedLineSetFactory connie = null;
	transient PointSetFactory pascalConfig = null;
	transient double time = 0.5277;
	transient double[][] fiveCurvePoints, sixCurvePoints = new double[6][];
	transient int[] permutation = {0,2,4,1,5,3};
	transient double gscale = 2.0;
	transient double[][] points2 = {{.3, .4, 1}, {-.2, -.4,1}};
	
	public static void main(String[] args)		{
		PascalDemo theProgram = new PascalDemo();
		theProgram.display();
	}

	@Override
	public SceneGraphComponent getContent() {
		if (worldSGC != null) return worldSGC;
		worldSGC = new SceneGraphComponent("Pascal Theorem Demo");
		editablePointsSGC = new SceneGraphComponent("curve");
		squareSGC = new SceneGraphComponent("square");
		conicSGC = new SceneGraphComponent("conic");
		pascalSGC = new SceneGraphComponent("pascal");
		pascalPointsSGC = new SceneGraphComponent("pascal points");
		pointSGC = SceneGraphUtility.createFullSceneGraphComponent("point");
		polePolarSGC = SceneGraphUtility.createFullSceneGraphComponent("polePolar");
		polarLineSGC = SceneGraphUtility.createFullSceneGraphComponent("polar line");
		for (int i = 0; i<7; ++i)	{
			SceneGraphComponent sgc = new SceneGraphComponent("child"+i);
			pascalSGC.addChild(sgc);
			if (i == 6) {
				Appearance app = new Appearance();
				app.setAttribute("lineShader.diffuseColor", new Color(170, 0, 0));
				sgc.setAppearance(app);
			}
		}
		pascalSGC.addChild(pascalPointsSGC);
		worldSGC.addChildren(editablePointsSGC, squareSGC, conicSGC, pascalSGC, polePolarSGC);
		polePolarSGC.addChildren(pointSGC, polarLineSGC);
		
		Appearance ap = new Appearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute("lineShader."+CommonAttributes.LINE_WIDTH	, 2.0 *gscale);
		ap.setAttribute("lineShader.diffuseColor", new Color(140,40,250));
		conicSGC.setAppearance(ap);
		ap = new Appearance();
		ap.setAttribute("pointShader.diffuseColor", new Color(0,170,0));
		ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS,.01*gscale);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		pascalSGC.setAppearance(ap);
		ap = new Appearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		pascalPointsSGC.setAppearance(ap);
		ap = new Appearance();
		worldSGC.setAppearance(ap);
		ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, .01*gscale);
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
		dgs.setShowLines(true);
		dgs.setShowPoints(true);
		DefaultPolygonShader dps = (DefaultPolygonShader) dgs.createPolygonShader("default");
		dps.setDiffuseColor(Color.white);
		DefaultLineShader dls = (DefaultLineShader) dgs.createLineShader("default");
		dls.setTubeDraw(false);
		dls.setLineWidth(gscale);
		
		ap = new Appearance();
		squareSGC.setAppearance(ap);
		dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
		dgs.setShowLines(false);
		dgs.setShowPoints(false);

		double[][] fivePoints = {
				 {1.14896,	0.505858	,1.00000	},	
				 {0.309017,	0.951057	,1.00000	},	
				 {-0.809017,	0.587785	,1.00000	},	
				 {-0.871933,	-0.423778,	1.00000	},	
				 {0.812364,	-0.685881,	1.00000}
				
		};
		 		
		ilsf = IndexedLineSetUtility.createCurveFactoryFromPoints(fivePoints, true);
		ilsf = IndexedLineSetUtility.circleFactory(5, 0, 0, 1.0);
		 
		fiveCurvePoints = Utility.demote(null, ilsf.getIndexedLineSet().
				getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null));
		conicSection.setInitialPoints(fiveCurvePoints);
		
		editablePointsSGC.setGeometry(ilsf.getGeometry());

		MatrixBuilder.euclidean().translate(0,0,.01).assignTo(editablePointsSGC);
		updateConic();
		
		editablePointsSGC.addTool(editTool);
		
		PointSet ps = Primitives.points(points2, null);
		pointSGC.setGeometry(ps);
		ap = polePolarSGC.getAppearance();
		ap.setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, .02*gscale);
		ap.setAttribute("pointShader.diffuseColor", Color.green);
		ap.setAttribute("lineShader.diffuseColor", Color.green);
		pointSGC.addTool(dragTool);
		polePolarSGC.setVisible(false);
		
		return worldSGC;
	}

	public static IndexedLineSetFactory circleFactory(int n, double cx, double cy, double r) {
		double[][] verts = new double[n][3];
		double angle = 0, delta = Math.PI * 2 / (n);
		for (int i = 0; i<n; ++i) {
			angle = i * delta;
			verts[i][0] = cx+r*Math.cos(angle);
			verts[i][1] = cy+r*Math.sin(angle);
			verts[i][2] = 1.0;
		}
		return IndexedLineSetUtility.createCurveFactoryFromPoints(verts, true);
	}

	protected void updatePascal() {
		for (int i = 0; i<5; ++i)	{
			sixCurvePoints[permutation[i]] =  fiveCurvePoints[i];
		}
		sixCurvePoints[permutation[5]] =  conicSection.getValueAtTime(null, time);
		System.err.println("five points = "+Rn.toString(fiveCurvePoints));
		double[][] pascalpoints = new double[9][];
		double[][] sixLines = new double[6][];
		for (int i = 0; i<6; ++i)	{
			SceneGraphComponent child = pascalSGC.getChildComponent(i);
			sixLines[i] = P2.lineFromPoints(null, sixCurvePoints[i], sixCurvePoints[(i+1)%6]);
			IndexedLineSet ils = PointRangeFactory.line(sixCurvePoints[i], sixCurvePoints[(i+1)%6]);
			child.setGeometry(ils);
			pascalpoints[i] = sixCurvePoints[i];
		}
		// find the three intersection points of opposite sides
		for (int i = 0; i<3; ++i)	{
			pascalpoints[6+i] = P2.pointFromLines(null, sixLines[i], sixLines[3+i]);
		}
		Pn.dehomogenize(pascalpoints, pascalpoints);
		IndexedLineSet ils = PointRangeFactory.line(pascalpoints[6], pascalpoints[7]);
		SceneGraphComponent child = pascalSGC.getChildComponent(6);
		child.setGeometry(ils);
		if (pascalConfig	 == null) {
			pascalConfig = new PointSetFactory();
			pascalConfig.setVertexCount(9);
			pascalConfig.setVertexCoordinates(Utility.promote(null, pascalpoints));
			Color gr = new Color(0, 150, 0), bl = Color.black;
			Color[] colors = {gr,gr,gr,gr,gr,gr,bl,bl,bl};
			pascalConfig.setVertexColors(colors);
			pascalPointsSGC.setGeometry(pascalConfig.getGeometry());
		}
		else 
			pascalConfig.setVertexAttribute(Attribute.COORDINATES, Utility.promote(null, pascalpoints));
		pascalConfig.update();
		
	}



	private void updatePolar() {
		double[] polarline = conicSection.polarizeSame(null, points2[0]);
		System.err.println("polar line = "+Rn.toString(polarline));
		PointRangeFactory prf = new PointRangeFactory();
		prf.set2DLine(polarline);
		prf.setFiniteSphere(false);
		prf.update();
		polarLineSGC.setGeometry(prf.getLine());
		
		
	}

	protected void updateConic() {
		
		double  conic[][] = new double[np][];
		for (int i = 0; i<np; ++i)	{
			conic[i] = Utility.promote(null, conicSection.getValueAtTime(conic[i], (.023 + i)/np));
//			System.err.println("proj: "+Rn.toString(new double[][]{pencil[i],Pa,aM,Mb,bQ,PQ}));
		}
//		System.err.println("pencil = "+Rn.toString(pencil));
//		System.err.println("conic = "+Rn.toString(conic));
//		conic = Utility.promote(null, conic);
		if (connie == null) {
			connie = IndexedLineSetUtility.createCurveFactoryFromPoints(conic, true);
			conicSGC.setGeometry(connie.getGeometry());	
		}
		else 
			connie.setVertexAttribute(Attribute.COORDINATES, conic);
		connie.update();
		updatePascal();
		updatePolar();
	}

	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
//		CameraUtility.encompass(jrviewer.getViewer());
		SceneGraphComponent cam = CameraUtility.getCameraNode(jrviewer.getViewer());
		CameraUtility.getCamera(jrviewer.getViewer()).setPerspective(false);
		CameraUtility.getCamera(jrviewer.getViewer()).setFieldOfView(60.0);
		MatrixBuilder.euclidean().translate(0, 0, 6).assignTo(cam);
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.white);
		((Component) jrviewer.getViewer().getViewingComponent()).addKeyListener(getKeyAdapter());
	}

	@Override
	public Component getInspector() {
		TextSlider delaySlider = new TextSlider.Double("time",SwingConstants.HORIZONTAL, 0.0, 1.0, time);
		delaySlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				time =  ((TextSlider) arg0.getSource()).getValue().doubleValue();
				updatePascal();
			}
		});
		delaySlider.setToolTipText("Set the time");
		inspector.add(delaySlider);
		return inspector;
	}


	KeyAdapter ka = null;
	private PointSetFactory psf;
	public KeyAdapter getKeyAdapter() {
		if (ka == null)	{
			ka = new KeyAdapter()	{
				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_H:
						System.out.println("	1: toggle edit points");
						break;
		
					case KeyEvent.VK_1:
						editablePointsSGC.setVisible(!editablePointsSGC.isVisible());
						break;

				}
		
				}
			};
		}
		return ka;
	}

	
	transient Tool editTool = new AbstractTool(InputSlot.LEFT_BUTTON) {
		
		boolean active = false,		// a security flag to avoid editing in an inconsistent state
				deleting = false;
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
					pr.getPickPath().getLastElement() != ilsf.getGeometry()) return;
			
			// we have a valid hit; proceed with the algorithm
			active = true;
			// to receive events as the mouse moves need to add following input slot
			// then perform() will be called
			addCurrentSlot(InputSlot.POINTER_TRANSFORMATION);	
			perform(tc);
			PickUtility.setPickable(editablePointsSGC, true, false, false);
		}

		@Override
		public void perform(ToolContext tc) {
			if (!active || deleting) return;
			super.perform(tc);			// could be omitted since it's an empty method, but perhaps in the future ...
			PickResult pr = tc.getCurrentPick();
			// check that we have a valid pick
			if (pr == null || pr.getPickPath() == null || pr.getPickPath().getLastElement() != ilsf.getGeometry()) return;
			// just to be safe, check that it's a point pick
			if (pr.getPickType() == PickResult.PICK_TYPE_POINT) {
				System.err.println("Picked vertex "+pr.getIndex());
				double[] newPoint = pr.getObjectCoordinates();
				final double[][] verts4 = ilsf.getIndexedLineSet().getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
				verts4[pr.getIndex()] = newPoint;
				Scene.executeWriter(editablePointsSGC, new Runnable() {
					
					@Override
					public void run() {
						ilsf.getIndexedLineSet().setVertexCountAndAttributes(Attribute.COORDINATES, 
								StorageModel.DOUBLE_ARRAY.array(verts4[0].length).
								createReadOnly(verts4));
					}
				});
				fiveCurvePoints[pr.getIndex()] = Utility.demote(null, newPoint);
				conicSection.setInitialPoints(fiveCurvePoints);
				updateConic();
			}
		}

		@Override
		public void deactivate(ToolContext tc) {
			super.deactivate(tc);
			// restore default states
			removeCurrentSlot(InputSlot.POINTER_TRANSFORMATION);
			PickUtility.setPickable(editablePointsSGC, true, true, false);
			active = false;
		}
		

	};

	transient Tool dragTool = new AbstractTool(InputSlot.LEFT_BUTTON) {
		
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
					pr.getPickPath().getLastElement() != pointSGC.getGeometry()) return;
			
			addCurrentSlot(InputSlot.POINTER_TRANSFORMATION);	

			double[] newPoint = pr.getObjectCoordinates();
			// have to pay attention to whether we are working with homogeneous coordinates or not
			// *** The pick system always uses homogeneous coordinates to report pick coordinates ***
			Pn.dehomogenize(newPoint, newPoint);
			perform(tc);
		}

		@Override
		public void perform(ToolContext tc) {
			super.perform(tc);			// could be omitted since it's an empty method, but perhaps in the future ...
			PickResult pr = tc.getCurrentPick();
			// check that we have a valid pick
			if (pr == null || pr.getPickPath() == null || pr.getPickPath().getLastElement() != pointSGC.getGeometry()) return;
			// just to be safe, check that it's a point pick
			if (pr.getPickType() == PickResult.PICK_TYPE_POINT) {
				System.err.println("Picked vertex "+pr.getIndex());
				double[] newPoint = pr.getObjectCoordinates();
				Pn.dehomogenize(newPoint, newPoint);
				newPoint = new double[]{newPoint[0], newPoint[1], newPoint[2]};
				newPoint[2] = 1.0;	
				int j = pr.getIndex();
				for (int i = 0; i<3; ++i)	{
					points2[j][i] = newPoint[i];
				}
				PointSet ps = Primitives.points(points2, null);
				pointSGC.setGeometry(ps);
				updatePolar();
			}
		}

		@Override
		public void deactivate(ToolContext tc) {
			super.deactivate(tc);
			// restore default states
			removeCurrentSlot(InputSlot.POINTER_TRANSFORMATION);
		}
		

	};


}
