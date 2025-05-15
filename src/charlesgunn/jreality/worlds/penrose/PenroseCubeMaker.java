package charlesgunn.jreality.worlds.penrose;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class PenroseCubeMaker extends Assignment {

	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	SceneGraphComponent cube = SceneGraphUtility.createFullSceneGraphComponent("cube");
	SceneGraphComponent prism = SceneGraphUtility.createFullSceneGraphComponent("prism");

	protected boolean trunc = true, doGem = true;
	protected double f = .2;
	int n = 5;
	@Override
	public void display() {
		// TODO Auto-generated method stub
		setAddCameraLight(true);
		super.display();
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", new java.awt.Color(255,255,255,0));
		CameraUtility.getCamera(viewer).setPerspective(false);
	}

	@Override
	public SceneGraphComponent getContent() {
		cube.setGeometry(truncateEdges(f, Primitives.cube()));
		Appearance ap = cube.getAppearance();
//		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute("polygonShader.diffuseColor",new Color(255,204,204));
		ap.setAttribute("lineShader.diffuseColor", Color.black);
		ap.setAttribute("lineShader.tubeRadius", .01); //36);
		ap.setAttribute("pointShader.diffuseColor", Color.BLACK);
		ap.setAttribute("pointShader.pointRadius", .12);
		
		updateGeometry();
//		trunc = true;
//		world.addChildren(stackedTriPrisms(2, trunc));
//		trunc = false;
//		world.addChildren(stackedTriPrisms(2, trunc));
		world.addChildren(cube);
		return world;
	}

	private void updateGeometry() {
		cube.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, !trunc);
		cube.setGeometry(truncateEdges(f,doGem ? getGem(n) : Primitives.cube()));
	}

	@Override
	public Component getInspector()	{
		Box vbox = Box.createVerticalBox();
		inspector.add(vbox);
		final TextSlider<Double> eSlider = new TextSlider.Double("factor",  SwingConstants.HORIZONTAL, 0, 1, f);
		eSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				f = eSlider.getValue().doubleValue();
				updateGeometry();
		}});
		vbox.add(eSlider);
		final TextSlider<Integer> nSlider = new TextSlider.Integer("n",  SwingConstants.HORIZONTAL, 1,10,n);
		nSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				n = nSlider.getValue().intValue();
				updateGeometry();
			}});
		vbox.add(nSlider);
		final JRadioButton truncB = new JRadioButton("trunc");
		truncB.setSelected(trunc);
		truncB.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				trunc = truncB.isSelected();
				updateGeometry();
			}});
		vbox.add(truncB);
		return inspector;
	}


	static double[][] apv = {{1,1,-1},{1,-1,1},{-1,1,1},{-1,-1,1},{-1,1,-1},{1,-1,-1},{1,1,1},{-1,-1,-1}};
//	static int[][] apvi = {{0,1,2,0},{3,4,5,3},{0,5,1},{5,1,3},{1,3,2},{3,2,4},{2,4,0},{4,0,5}};
	static int[][] apvi = {{0,5},{5,1},{1,3},{3,2},{2,4},{4,0}};
	static int[][] apfi = {{0,5,1},{5,1,3},{1,3,2},{3,2,4},{2,4,0},{4,0,5}};
	static int[][] capi = {{7,3},{7,4},{7,5}};
	static int[][] capfi = {{7,3,4},{7,4,5},{7,5,3}};
	private IndexedFaceSet triangularAntiPrism()	{
		IndexedFaceSetFactory ilsf = new IndexedFaceSetFactory();
		ilsf.setVertexCount(apv.length);
		ilsf.setVertexCoordinates(apv);
		if (trunc) {
			ilsf.setEdgeCount(apvi.length);
			ilsf.setEdgeIndices(apvi);
		}
		ilsf.setFaceCount(apfi.length);
		ilsf.setFaceIndices(apfi);
		ilsf.setGenerateFaceNormals(true);
		ilsf.setGenerateEdgesFromFaces(!trunc);
		ilsf.update();
		return ilsf.getIndexedFaceSet();
	}
	private IndexedFaceSet prismCap()	{
		IndexedFaceSetFactory ilsf = new IndexedFaceSetFactory();
		ilsf.setVertexCount(apv.length);
		ilsf.setVertexCoordinates(apv);
		if (trunc) {
			ilsf.setEdgeCount(capi.length);
			ilsf.setEdgeIndices(capi);
		}
		ilsf.setFaceCount(capfi.length);
		ilsf.setFaceIndices(capfi);
		ilsf.setGenerateFaceNormals(true);
		ilsf.setGenerateEdgesFromFaces(!trunc);
		ilsf.update();
		return ilsf.getIndexedFaceSet();
	}
	
	private SceneGraphComponent stackedTriPrisms(int n, boolean trunc)	{
		SceneGraphComponent stack = SceneGraphUtility.createFullSceneGraphComponent("stack");
		stack.setAppearance(cube.getAppearance());
		SceneGraphComponent geomSGC = SceneGraphUtility.createFullSceneGraphComponent("onePrism");
		SceneGraphComponent capSGC = SceneGraphUtility.createFullSceneGraphComponent("cap");
		IndexedFaceSet onePrism = triangularAntiPrism();
		geomSGC.setGeometry(trunc ? truncateEdges(f,onePrism) : onePrism);
		capSGC.setGeometry(trunc ? truncateEdges(f,prismCap()) : prismCap());
		SceneGraphComponent childCapSGC = SceneGraphUtility.createFullSceneGraphComponent("cap2");
		childCapSGC.addChild(capSGC);
		double op = 0; //.1;
		double[] acc = MatrixBuilder.euclidean().translate(op,op,op).getArray();
		double a = 2.0/3.0 + op;
		double[] mat = P3.makeScrewMotionMatrix(null, new double[] {0,0,0,1}, new double[] {a,a,a,1}, Math.PI/3.0, Pn.EUCLIDEAN);
	for (int i = 0; i<n ; ++i ) {
			SceneGraphComponent child = SceneGraphUtility.createFullSceneGraphComponent("child");
			new Matrix(acc).assignTo(child);
			child.addChild(geomSGC);
			stack.addChild(child);
			acc = Rn.times(null, acc, mat);
		}
//		new Matrix(acc).assignTo(childCapSGC);
	 	MatrixBuilder.euclidean(new Matrix(acc)).reflect(new double[] {1,1,1,1}).assignTo(childCapSGC);
		stack.addChildren(capSGC, childCapSGC );
		return stack;
	}
	
	private IndexedFaceSet getGem(int n) {
		IndexedFaceSetFactory ilsf = new IndexedFaceSetFactory();
		int[][] ind = new int[2*n][];
		double [][] vv = new double[2*n+2][];
		vv[2*n] = new double[] {0,0,1,1};
		vv[2*n+1] = new double[] {0,0,-1,1};
		double a = Math.PI*2.0/(2*n),
				c = Math.cos(a),
				s = Math.sin(a);
//		double width = (1-Math.cos(a))/(1+Math.cos(a));
		double width = .5*Math.sqrt(s*s - (c-1)*(c-1));
		for (int i = 0; i<2*n; ++i) {
			double angle = i*Math.PI*2.0/(2*n);
			vv[i] = new double[] {Math.cos(angle), Math.sin(angle), ((i%2)==0) ? width : -width};
		}
		for (int i = 0; i<n; ++i) {
			int tn = 2*n;
			ind[i] = new int[] {2*n, 2*i, (2*i+1)%tn, (2*i+2)%tn}; 
			ind[i+n] = new int[] {2*n+1, (2*i+1)%tn, (2*i+2)%tn, (2*i+3)%tn};
		}
		ilsf.setVertexCount(vv.length);
		ilsf.setVertexCoordinates(vv);
		ilsf.setFaceCount(ind.length);
		ilsf.setFaceIndices(ind);
		ilsf.setGenerateEdgesFromFaces(true);
		ilsf.setGenerateFaceNormals(true);
		ilsf.update();
	
		return ilsf.getIndexedFaceSet();
		
	}
	private IndexedFaceSet truncateEdges(double f, IndexedFaceSet ils) {
		if (!trunc) return ils;
		IndexedFaceSetFactory ilsf = new IndexedFaceSetFactory();
		int[][] ind = ils.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null);
		double[][] vv = ils.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		double[][] nv = new double[2*ind.length][];
		int[][] nind = new int[ind.length][2];
		for (int i = 0; i<ind.length; ++i) {
			int j = ind[i][0], k = ind[i][1]; // hack: just do the first segment
			double[] vj = vv[j], vk = vv[k];
			nv[2*i] = AnimationUtility.linearInterpolation(null, f, 0.0, 1.0, vj, vk);
			nv[2*i+1]= AnimationUtility.linearInterpolation(null, f, 0.0, 1.0,  vk, vj);
			nind[i][0] = 2*i;
			nind[i][1] = 2*i+1;
		}
		ilsf.setVertexCount(nv.length);
		ilsf.setVertexCoordinates(nv);
		ilsf.setEdgeCount(nind.length);
		ilsf.setEdgeIndices(nind);
		ilsf.update();
		return ilsf.getIndexedFaceSet();
		
	}
	
	public static void main(String[] args) {
		new PenroseCubeMaker().display();

	}

}
