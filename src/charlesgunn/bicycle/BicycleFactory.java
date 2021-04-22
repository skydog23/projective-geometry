package charlesgunn.bicycle;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import charlesgunn.jreality.geometry.projective.LineUtility;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.projgeom.PlueckerLineGeometry;

public class BicycleFactory {
	double tx, 
		ty, 
		xyAngle, 
		roll, 
		frontTurn, 
		correction;
	double tireR = .27, tirer = .03,
		tireSeparation = 1.0;
	static double R = .8, L = 0, G = .1, sigma;
	{
		L = Math.sqrt(1-(R+G)*(R+G));
		sigma = Math.asin(L);
	}
	double[] frontAxis = {0,0,1};
	SceneGraphComponent finalSGC = SceneGraphUtility.createFullSceneGraphComponent("final");
	SceneGraphComponent bikeSGC = SceneGraphUtility.createFullSceneGraphComponent("bike");
	SceneGraphComponent backSGC =SceneGraphUtility.createFullSceneGraphComponent("back");
	SceneGraphComponent backBikeSGC =SceneGraphUtility.createFullSceneGraphComponent("backBike");
	SceneGraphComponent front1SGC =SceneGraphUtility.createFullSceneGraphComponent("front1");
	SceneGraphComponent front2SGC =SceneGraphUtility.createFullSceneGraphComponent("front2");
	SceneGraphComponent front3SGC =SceneGraphUtility.createFullSceneGraphComponent("front3");
	SceneGraphComponent front4SGC =SceneGraphUtility.createFullSceneGraphComponent("front4");
	SceneGraphComponent tireSGC =SceneGraphUtility.createFullSceneGraphComponent("front");
	ParametricSurfaceFactory tireFactory = makeTorusFactory();
	SceneGraphPath worldToFrontWheel;
	
	public BicycleFactory()	{
		finalSGC.addChild(bikeSGC);
		finalSGC.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .02);
		finalSGC.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, .02);
		bikeSGC.addChildren(backSGC, front4SGC);
		backSGC.addChildren(backBikeSGC, tireSGC);
		backBikeSGC.setGeometry(getBackBike());
		MatrixBuilder.euclidean().rotateY(sigma).assignTo(backBikeSGC);
		front4SGC.addChild(front3SGC);
		front3SGC.addChild(front2SGC);
		front2SGC.addChild(front1SGC);
		MatrixBuilder.euclidean().translate(-G,0,0).assignTo(front1SGC);
		
		front2SGC.setGeometry(getFrontBike());
		MatrixBuilder.euclidean().translate(-R, 0,-L).assignTo(front3SGC);
		MatrixBuilder.euclidean().rotateY(sigma).assignTo(front4SGC);
		front1SGC.addChild(tireSGC);
		tireSGC.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		tireSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		tireSGC.setGeometry(tireFactory.getGeometry());
		worldToFrontWheel = new SceneGraphPath(bikeSGC, front4SGC, front3SGC, front2SGC, front1SGC, tireSGC);
		update();
	}
	
	public void setTx(double tx) {
		this.tx = tx;
	}

	public void setTy(double ty) {
		this.ty = ty;
	}

	public void setXyAngle(double xyAngle) {
		this.xyAngle = xyAngle;
	}

	public void setLean(double lean) {
		this.roll = lean;
	}

	public void setFrontTurn(double frontTurn) {
		this.frontTurn = frontTurn;
	}

	public void setCorrection(double correction) {
		this.correction = correction;
	}

	public SceneGraphComponent getBicycle()	{
		return finalSGC;
	}
	
	double[] frontWheelPlane = {0,1,0,0};		// y == 0 
	double[] groundPlane = {0,0,1,0};			// z == 0
	double[] front2World = new double[16], 
		front2WorldStar = new double[16], 
		tmp = new double[16];
	double[] M = new double[4],			// front wheel center in world coords
		P = new double[4],				// plane of front wheel in world coords
		l = new double[6];				// line of intersection of P with ground (z=0) plane
	public void update()	{
		MatrixBuilder.euclidean().scale(4).translate(tx+.5,ty,tirer).rotateZ(xyAngle).assignTo(finalSGC);
		MatrixBuilder.euclidean().rotateX(roll).translate(0,0,tireR).assignTo(bikeSGC);
		MatrixBuilder.euclidean().rotateZ(frontTurn).assignTo(front2SGC);
		if (frontTurn == 0) frontTurn = 10E-8;
		// correct position of front wheel above/below ground
		worldToFrontWheel.getMatrix(front2World);
		Rn.transpose(front2WorldStar, Rn.inverse(tmp, front2World));
		Rn.matrixTimesVector(M, front2World, P3.originP3);
		Rn.matrixTimesVector(P, front2WorldStar, frontWheelPlane);
//		System.err.println("Front wheel plane = "+Rn.toString(frontWheelPlane));
		PlueckerLineGeometry.lineFromPlanes(l, groundPlane, P);
		double[][] p12 = new double[2][4];
		LineUtility.twoPointsOnLine(p12, l);
		double[] p1 = new double[3], p2 = new double[3], m3 = new double[3];
		Pn.dehomogenize(p1, p12[0]);
		Pn.dehomogenize(p2, p12[1]);
		Pn.dehomogenize(m3, M);
		double[] dir = Rn.subtract(null, p1, p2);
		double k1 = Rn.innerProduct(dir, dir), 
			k2 = Rn.innerProduct(dir, Rn.subtract(null, m3, p2));
		double t = k2/k1;
		double[] X = Rn.linearCombination(null, 1.0, p2, t, dir);
//		System.err.println("M = "+Rn.toString(m3));3
//		System.err.println("X = "+Rn.toString(X));
		double[] toM = Rn.subtract(null, m3, X);
		double ip = Rn.innerProduct(toM, dir);
//		System.err.println("inpro = "+ip);
		double length = Rn.euclideanNorm(toM);
		double scale = tireR/length;
//		System.err.println("scale = "+scale);
		toM = Rn.times(null, scale, toM);
		X = Rn.subtract(null, m3, toM);
//		System.err.println("X = "+Rn.toString(X));
		// final step: rotate bike around axis through back contact point,
		// perpendicular to plane through back contact point, front contact point, and vertical direction
		double[] axis = Rn.crossProduct(null, X, new double[]{0,0,1});
		double angle = -Math.asin(X[2]/Rn.euclideanNorm(X));
//		System.err.println("Axis, angle = "+Rn.toString(axis)+" "+angle);
		bikeSGC.getTransformation().multiplyOnLeft(P3.makeRotationMatrix(null, axis, angle));
	}
	 ParametricSurfaceFactory makeTorusFactory() {
		ParametricSurfaceFactory foo = new ParametricSurfaceFactory();
		 foo.setImmersion(new ParametricSurfaceFactory.Immersion() {
					double R = .9*tireR;

					double r = .1*tireR;
					double a =1, b=1;
					public int getDimensionOfAmbientSpace() {
						return 3;
					}

					public void evaluate(double u, double v, double[] xyz,
							int offset) {
						xyz[0] = Math.cos(u) * (R + r * Math.cos(v));
						xyz[1] = r * Math.sin(v);
						xyz[2] = Math.sin(u) * (R + r * Math.cos(v));				
					}

					public boolean isImmutable() {
						return true;
					}
				}

		);
		foo.setUMin(0);
		foo.setVMin(0);
		foo.setUMax(2*Math.PI);
		foo.setVMax(2*Math.PI);
		foo.setClosedInUDirection(true);
		foo.setClosedInVDirection(true);
		foo.setULineCount(100);
		foo.setVLineCount(13);
		foo.setGenerateVertexNormals(true);
		foo.setGenerateFaceNormals(true);
		foo.setGenerateEdgesFromFaces(true);
		foo.update();
		return foo;
	}
	 
	private IndexedLineSet getFrontBike()	{
		IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
		double[][] verts = {
				{-G,2*tirer,0},
				{-G,-2*tirer,0},
				{0,2*tirer,.4*L},
				{0,-2*tirer,.4*L},
				{0,2*tirer,.8*L},
				{0,-2*tirer,.8*L},
				{0,0,.8*L},
				{0,0,1.3*L},
				{0,L*.5,1.3*L},
				{0,-L*.5,1.3*L}};
		int[][] edgeIndices = {{0,2},{1,3},{2,4},{3,5},{4,5},{6,7},{8,9}};
		ilsf.setVertexCount(verts.length);
		ilsf.setVertexCoordinates(verts);
		ilsf.setEdgeCount(edgeIndices.length);
		ilsf.setEdgeIndices(edgeIndices);
		ilsf.update();
		return ilsf.getIndexedLineSet();
	}

	private IndexedLineSet getBackBike()	{
		IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
		double[][] verts = new double[][]{
				{0,2*tirer,0},
				{0,-2*tirer,0},
				{-1.2*tireR,2*tirer,0},
				{-1.2*tireR,-2*tirer,0},
				{-1.2*tireR,0,0},
				{-R,0,0}};
		int[][] edgeIndices = {{0,2},{1,3},{2,3},{4,5}};
		ilsf.setVertexCount(verts.length);
		ilsf.setVertexCoordinates(verts);
		ilsf.setEdgeCount(edgeIndices.length);
		ilsf.setEdgeIndices(edgeIndices);
		ilsf.update();
		return ilsf.getIndexedLineSet();
	}
	public void insertTabs(JTabbedPane tabs) {
		JPanel panel = new JPanel();
		tabs.addTab("parameters", panel);
		Box box = Box.createVerticalBox();
		panel.add(box);
		final TextSlider stepSlider = new TextSlider.Double("tx",SwingConstants.HORIZONTAL,-10,10,tx);
		stepSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				tx =stepSlider.getValue().doubleValue();
				update();
			}
		});
		box.add(stepSlider);
		final TextSlider tySlider = new TextSlider.Double("ty",SwingConstants.HORIZONTAL,-10,10,ty);
		tySlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				ty =tySlider.getValue().doubleValue();
				update();
			}
		});
		box.add(tySlider);
		final TextSlider xyAngleSlider = new TextSlider.Double("xyAngle",SwingConstants.HORIZONTAL,-Math.PI,Math.PI, xyAngle);
		xyAngleSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				xyAngle = xyAngleSlider.getValue().doubleValue();
				update();
			}
		});
		box.add(xyAngleSlider);
		final TextSlider rollSlider = new TextSlider.Double("roll",SwingConstants.HORIZONTAL,-Math.PI/2,Math.PI/2, xyAngle);
		rollSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				roll = rollSlider.getValue().doubleValue();
				update();
			}
		});
		box.add(rollSlider);
		final TextSlider turnSlider = new TextSlider.Double("turn",SwingConstants.HORIZONTAL,-Math.PI/2,Math.PI/2, xyAngle);
		turnSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				frontTurn = turnSlider.getValue().doubleValue();
				update();
			}
		});
		box.add(turnSlider);
		final TextSlider blendSlider = new TextSlider.Integer("reflection",SwingConstants.HORIZONTAL,0,255, 0);
		blendSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				int alpha= blendSlider.getValue().intValue();
				System.err.println("alpha = "+alpha);
				tireSGC.getAppearance().setAttribute("polygonShader.reflectionMap:blendColor", new Color(0,0,0,alpha));
			}
		});
		box.add(blendSlider);
	}

	
}
