/*
 * Created on Nov 23, 2011
 *
 */
package charlesgunn.jreality.geometry.projective;

import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.util.ArrayList;

import charlesgunn.math.p5.PlueckerLineGeometry;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Matrix;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointLight;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.util.CopyVisitor;
import de.jreality.util.SceneGraphUtility;

public class DualizeSceneGraph {

	public static final String 
				FAN_RADIUS = "fanRadius",
				FAN_COUNT = "fanCount",
				DO_FANS = "doFans",
				DUALIZE = "dualize",
				DUALIZE_POINTS = "dualizePoints",
				DUALIZE_LINES = "dualizeLines";
	public static final double FAN_RADIUS_DEFAULT = .1;
	public static final int FAN_COUNT_DEFAULT = 12;
	public static final Appearance topAp = new Appearance();
	{
		topAp.setAttribute(FAN_RADIUS,FAN_RADIUS_DEFAULT);
		topAp.setAttribute(FAN_COUNT, FAN_COUNT_DEFAULT);
		topAp.setAttribute(DO_FANS, true);
		topAp.setAttribute(DUALIZE, true);
		topAp.setAttribute(DUALIZE_POINTS, true);
		topAp.setAttribute(DUALIZE_LINES, true);
	}
	// we want to compute the following using appearance attributes with above keys
	// but for now set them globally
	boolean doFan = true;
	double fanRadius = .6;
	int fanCount = 12;
	boolean finiteLines = false,
		renderSphere = true,
		dualize = true;
	CopyVisitor copier = new CopyVisitor();

	SceneGraphComponent root, result;
	DualizeVisitor dv;
	SceneGraphPath sgp;
	
	public DualizeSceneGraph(SceneGraphPath thePath)	{
		sgp = thePath;
		root = thePath.getLastComponent();
		dv = new DualizeVisitor(sgp);
	}
	
	public SceneGraphComponent visit()	{
		dv.updateEAP();
		root.childrenAccept(dv);
		result = SceneGraphUtility.createFullSceneGraphComponent("DualizeSceneGraph result");
		result.addChild(dv.currentDualSGC);
		if (renderSphere) {
			SceneGraphComponent  backBanana = SceneGraphUtility.createFullSceneGraphComponent("back banana");
			double[] mat = Rn.diagonalMatrix(null, new double[]{-1,-1,-1,-1});
			new Matrix(mat).assignTo(backBanana);
			backBanana.addChild(dv.currentDualSGC);
			result.addChild(backBanana);
		}
		return result;
	}

	static public SceneGraphComponent dualize(SceneGraphPath sgp)	{
		DualizeSceneGraph dsg = new DualizeSceneGraph(sgp);
		return dsg.visit();
	}
	
	static public SceneGraphComponent dualize(SceneGraphComponent sgc)	{
		SceneGraphPath sgp = new SceneGraphPath(sgc);
		return dualize(sgp);
	}
	
	protected class DualizeVisitor extends SceneGraphVisitor {
		
		SceneGraphComponent  
			currentSGC, currentDualSGC;
		EffectiveAppearance eap;
		private double[][] dualLines;

		public DualizeVisitor(SceneGraphPath path)	{
			eap = EffectiveAppearance.create(path);
			if (eap == null)
				throw new IllegalStateException("null eap");
			init(null, path.getLastComponent());
		}
		
		private DualizeVisitor(DualizeVisitor parent, SceneGraphComponent c)	{
			init(parent, c);
		}

		private void init(DualizeVisitor parent, SceneGraphComponent c) {
			currentSGC = c;
			copier.visit(currentSGC);
			currentDualSGC = (SceneGraphComponent) copier.getCopy();
			currentDualSGC.setName( c.getName()+" dual");
			if (parent == null) return;
			if (parent.currentSGC != null && parent.currentSGC.isDirectAncestor(currentSGC)) {
				parent.currentDualSGC.addChild(currentDualSGC);
			}
			if (currentSGC.getAppearance() != null)
				eap = parent.eap.create(currentSGC.getAppearance());
			else eap = parent.eap;
		}
		
		@Override
		public void visit(IndexedFaceSet f) {
			visit((IndexedLineSet) f);
		}

		@Override
		public void visit(IndexedLineSet p) {
			visit((PointSet) p);
			if (!eap.getAttribute(DUALIZE_LINES, true) || p.getEdgeAttributes(Attribute.INDICES) == null) return;
			double[][] verts = p.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
			int[][] edges = p.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null);
			double[][] ecolors = null;
			if (p.getEdgeAttributes(Attribute.COLORS) != null)
				ecolors = p.getEdgeAttributes(Attribute.COLORS).toDoubleArrayArray(null);
			double[][] vcolors = null;
			if (p.getVertexAttributes(Attribute.COLORS) != null)
				vcolors = p.getVertexAttributes(Attribute.COLORS).toDoubleArrayArray(null);
			int numEdges = edges.length;
			if (verts[0].length == 3) 
				verts = Pn.homogenize(null, verts);
			ArrayList<double[]> dualPoints = new ArrayList<double[]>();
			SceneGraphComponent dualPointsSGC = new SceneGraphComponent("dual points");
			currentDualSGC.addChild(dualPointsSGC);
			
			for (int i = 0; i<numEdges; ++i)		{
				int[] edge = edges[i];
				int numLines = edge.length-1;
//				boolean closed = false;
//				if (Rn.euclideanDistance(verts[edge[0]], verts[edge[numLines]]) < 10E-10)
//					closed = true;
				int numFans = numLines ; //- (closed ? 0 : 1);
				
				SceneGraphComponent edgeSGC = new SceneGraphComponent("edge "+i);
				edgeSGC.setAppearance(new Appearance());
				currentDualSGC.addChild(edgeSGC);
				edgeSGC.getAppearance().setAttribute(VERTEX_DRAW, false);
				if (ecolors != null)	{
					Color foo = new Color((float) ecolors[i][0], (float) ecolors[i][1], (float) ecolors[i][2]);
					int j = i; //(i+1)%ecolors.length;
					foo = new Color((float) ecolors[j][0], (float) ecolors[j][1], (float) ecolors[j][2]);
					edgeSGC.getAppearance().setAttribute("lineShader.diffuseColor", foo);
					edgeSGC.getAppearance().setAttribute("pointShader.diffuseColor", foo);
				}
				boolean doFans = eap.getAttribute(DO_FANS, true);
				System.err.println("fan radius = "+fanRadius);
				for (int j = 0; j<numFans; ++j) 	{
					// generate the points dual to the edges
					// we find them as the intersection points of pairs of dual lines
					dualPoints.add(PlueckerLineGeometry.intersectionPoint(
							null, dualLines[edge[j]], dualLines[edge[(j+1)%edge.length]]));
					if (!doFans) continue;
					// generate fans at the joints of the
					LinePencilFactory lpf = null;
					lpf = LinePencilFactory.linePencilFactoryForIntersectingLines(
							lpf, dualLines[edge[j]], dualLines[edge[(j+1)%edge.length]]);
					lpf.setFan(true);
					lpf.setFiniteSphere(true);
					lpf.setSphereRadius(fanRadius);
					lpf.setNumLines(fanCount);
					lpf.update();
					edgeSGC.addChild(lpf.getPencil());
//					if (vcolors != null)	{
//						lpf.getPencil().setAppearance(new Appearance());
//						int index = (j+1)%vcolors.length;
//						Color foo = new Color((float) vcolors[index][0], (float) vcolors[index][1], (float) vcolors[index][2]);
//						lpf.getPencil().getAppearance().setAttribute("lineShader.diffuseColor", foo);
//					}
				}
			}
			if (dualPoints.size() == 0) return;
			PointSetFactory psf = new PointSetFactory();
			double[][] dualPointA = dualPoints.toArray(new double[dualPoints.size()][]);
			psf.setVertexCount(dualPointA.length);
			psf.setVertexCoordinates(dualPointA);
			if (ecolors != null && ecolors.length == dualPointA.length) psf.setVertexColors(ecolors);
			psf.update();
			dualPointsSGC.setGeometry(psf.getPointSet());
		}

		@Override
		public void visit(PointSet p) {
			DataList vertexAttributes = p.getVertexAttributes(Attribute.COORDINATES);
			if (vertexAttributes == null) return;
			double[][] verts = vertexAttributes.toDoubleArrayArray(null);
			double[][] colors = null;
			if (p.getVertexAttributes(Attribute.COLORS) != null)
				colors = p.getVertexAttributes(Attribute.COLORS).toDoubleArrayArray(null);
			int numVerts = verts.length;
			dualLines = new double[numVerts][];
			for (int i = 0; i<numVerts; ++i)	{
				dualLines[i] = dualizePoint2Line(dualLines[i], verts[i]); //new double[]{ theVerts[i][3], 0, -theVerts[i][1], 0, -theVerts[i][0], 0};
			}
//			System.err.println("dual lines = \n"+Rn.toString(dualLines));
			if (!eap.getAttribute(DUALIZE_POINTS, true)) return;
			for (int i = 0; i<numVerts; ++i)	{
				PointRangeFactory prf = new PointRangeFactory();
				prf.setPluckerLine(dualLines[i]);
				prf.setFiniteSphere(finiteLines);
				prf.setSphereRadius(500.0);
				prf.update();
				prf.getLine().setName(p.getName()+" dual line "+i);
				SceneGraphComponent child = new SceneGraphComponent(p.getName()+" dual line "+i);
				child.setGeometry(prf.getLine());
				child.setAppearance(new Appearance());
				// TODO this could be controversial: how to control this?
				child.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
				if (colors != null)	{
					Color foo = new Color((float) colors[i][0], (float) colors[i][1], (float) colors[i][2]);
					child.getAppearance().setAttribute("lineShader.diffuseColor", foo);
				}
				currentDualSGC.addChild(child);
			}
		}

		@Override
		public void visit(SceneGraphComponent c) {
			updateEAP();
			if (!dualize) return;
			c.childrenAccept(new DualizeVisitor(this, c));
		}

		private void updateEAP() {
			if (eap != null) {
				dualize = (eap.getAttribute(DualizeSceneGraph.DUALIZE, true));
				fanRadius = (eap.getAttribute(DualizeSceneGraph.FAN_RADIUS, FAN_RADIUS_DEFAULT));
				fanCount = (eap.getAttribute(DualizeSceneGraph.FAN_COUNT, FAN_COUNT_DEFAULT));
			}
		}

		@Override
		public void visit(Transformation t) {
			double[] mat = t.getMatrix();
			mat = Rn.transpose(null, Rn.inverse(null, mat));
			currentDualSGC.setTransformation(new Transformation(mat));
		}

		@Override
		public void visit(Appearance a) {
			copier.visit(a);
			Appearance copied = (Appearance) copier.getCopy();
			// TODO handle other dual properties here.
			Object foo = copied.getAttribute("pointShader.diffuseColor");
			Object bar = copied.getAttribute("lineShader.diffuseColor");
			if (foo instanceof Color){
				copied.setAttribute("lineShader.diffuseColor", foo);
				copied.setAttribute("pointShader.diffuseColor", Appearance.INHERITED);
			}
			if (bar instanceof Color){
				copied.setAttribute("pointShader.diffuseColor", bar);
				if (foo == null) copied.setAttribute("lineShader.diffuseColor", Appearance.INHERITED);
			}
			foo = copied.getAttribute(CommonAttributes.VERTEX_DRAW);
			bar = copied.getAttribute(CommonAttributes.EDGE_DRAW);
			if (foo instanceof Boolean){
				copied.setAttribute(CommonAttributes.EDGE_DRAW, foo);
				if (bar == null) copied.setAttribute(CommonAttributes.VERTEX_DRAW, Appearance.INHERITED);
			}
			if (bar instanceof Boolean){
				copied.setAttribute(CommonAttributes.VERTEX_DRAW, bar);
				if (foo == null) copied.setAttribute("lineShader.diffuseColor", Appearance.INHERITED);
			}
			
			currentDualSGC.setAppearance(copied);
		}

		@Override
		public void visit(Camera c) {
			copier.visit(c);
			Camera copied = (Camera) copier.getCopy();
			currentDualSGC.setCamera(copied);
		}

		@Override
		public void visit(DirectionalLight l) {
			copier.visit(l);
			DirectionalLight copied = (DirectionalLight) copier.getCopy();
			currentDualSGC.setLight(copied);
		}

		@Override
		public void visit(PointLight l) {
			copier.visit(l);
			PointLight copied = (PointLight) copier.getCopy();
			currentDualSGC.setLight(copied);
		}

		@Override
		public void visit(SpotLight l) {
			copier.visit(l);
			SpotLight copied = (SpotLight) copier.getCopy();
			currentDualSGC.setLight(copied);
		}

	}

	public static double[] dualizeLine2Point(double[] pt, double[] line)	{
		if (pt == null) return new double[]{-line[4], -line[2],0, metric * line[0]};
		pt[0] = -line[4]; pt[1] = -line[2]; pt[2] = 0; pt[3] = metric * line[0];
		return pt;
	}

	public static void setMetric(int m) {
		metric = m;
	}
	public static int getMetric()	{
		return metric;
	}
	static int metric = Pn.HYPERBOLIC;
	public static double[] dualizePoint2Line(double[] line, double[] pt)	{
		if (line == null) {
			if (pt.length == 4)
				return new double[]{metric * pt[3], 0, -pt[1], 0, -pt[0], 0};
			return new double[]{metric * 1,0,-pt[1], 0, -pt[0], 0}; 
		}
		System.arraycopy(new double[]{metric * pt[3], 0, -pt[1], 0, -pt[0], 0}, 0, line, 0, 6);
		return line;
	}

	public boolean isFiniteLines() {
		return finiteLines;
	}

	public void setFiniteLines(boolean finiteLines) {
		this.finiteLines = finiteLines;
	}

	public static PointSetFactory segmentFactory(double[] p1, double[] p2, int n, int metric) {
		PointSetFactory psf = new PointSetFactory();
		double[][] verts = new double[n][];
		for (int i = 0; i < n; ++i)	{
			double t = (i/(n-1.0));
			verts[i] = Pn.linearInterpolation(null, p1, p2, t, metric);
		}
		psf.setVertexCount(n);
		psf.setVertexCoordinates(verts);
		psf.update();
		return psf;
	}
	
	public static SceneGraphComponent completePolygon(double[][] verts, int fcount, int scount, double rad)		{
		SceneGraphComponent all = SceneGraphUtility.createFullSceneGraphComponent("all"),
				segments = SceneGraphUtility.createFullSceneGraphComponent("segments"), 
				fans = SceneGraphUtility.createFullSceneGraphComponent("fans"),
				trad = SceneGraphUtility.createFullSceneGraphComponent("trad");
		segments.getAppearance().setAttribute("pointShader."+CommonAttributes.DIFFUSE_COLOR, Color.red);
		segments.getAppearance().setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, .01);
		fans.getAppearance().setAttribute("lineShader."+CommonAttributes.DIFFUSE_COLOR, Color.blue);
		fans.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .005);
		fans.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		int n = verts.length;
		all.addChildren(segments, fans, trad);
		double[][] lines = new double[n][];
		for (int i = 0; i<n; ++i)	{
			lines[i] = PlueckerLineGeometry.lineFromPoints(null, verts[i], verts[(i+1)%n]);
			PlueckerLineGeometry.normalize(lines[i], lines[i]);
		}
		for (int i = 0; i<n; ++i)	{
			// get the ith segment
			PointSetFactory psf = segmentFactory(verts[i], verts[(i+1)%n], scount, Pn.EUCLIDEAN);
			SceneGraphComponent seg = new SceneGraphComponent();
			seg.setGeometry(psf.getGeometry());
			segments.addChild(seg);
			// get the ith fan
			LinePencilFactory lpf = LinePencilFactory.linePencilFactoryForIntersectingLines(
					null,lines[i], lines[(i+1)%n]);
			lpf.setFan(true);
			lpf.setFiniteSphere(true);
			lpf.setSphereRadius(rad);
			lpf.setNumLines(fcount);
			lpf.update();
			fans.addChild(lpf.getPencil());
		}
		IndexedLineSet tradils = IndexedLineSetUtility.createCurveFromPoints(null, verts, true);
		trad.setGeometry(tradils);
		trad.getAppearance().setAttribute("lineShader."+CommonAttributes.DIFFUSE_COLOR, new Color(255,0,255));
		trad.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .005);
		trad.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		return all;
	}

	static double[][] verts = {
			{1,.3,0,1},
			{0,1,0,1},
			{-1.2,0,0,1},
			{-.3,-1,0,1},
			{.6,-.8, 0, 1}
	};
	public static void main(String[] args) {
		SceneGraphComponent test = completePolygon(verts, 10, 50, .35);
		JRViewer.display(test);
	}
}
