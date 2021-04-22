/*
 * Created on 26.04.2017
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayoutInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.SwingConstants;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.geometry.projective.PointCollector;
import charlesgunn.jreality.plugin.TermesSpherePlugin;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.Utility;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.projgeom.PlueckerLineGeometry;

public class SchatzSquare extends Assignment {

	SceneGraphComponent world,
		reflectedCopy,
		originalCopy,
		quadrangleSGC,
		quadGridSGC,
		inverseQuadSGC,
		orbitsSGC,
		draggableLine,
		secondFPSGC;
	IndexedFaceSetFactory quadFactory;
	double angle = Math.PI/4;
	double[] amesT = Rn.identityMatrix(4);
	boolean invert = true;
	double[] planeAtinfinity = P3.originP3, centerPlane;
	IndexedLineSetFactory squareGridFactory = new IndexedLineSetFactory();
	static private double[][] origQuadVerts =  
	{{1,0,0,1},{0,1,0,1},{-1,0,0,1},{0,-1,0,1},{0,0,1,0}}, quadVerts = new double[5][];
	static private int[][] find = {{1,2,3}}, //,{3,0,1}}, 
			eind = {{1,2,3,1}}; //{{0,1,2,3,0}};
	int numTris = find.length;
	boolean useMatrix = true,
			showOrbits = true;
	double width = 3, height = .5;
	private double[][] dragpoints = new double[][]{{-width, height,0,1},{width,height,0,1}};

	@Override
	public SceneGraphComponent getContent() {
		world = SceneGraphUtility.createFullSceneGraphComponent("lines");
		reflectedCopy = SceneGraphUtility.createFullSceneGraphComponent("oneCopy");
		originalCopy = SceneGraphUtility.createFullSceneGraphComponent("ames");
		quadrangleSGC = SceneGraphUtility.createFullSceneGraphComponent("quad");
		quadGridSGC = SceneGraphUtility.createFullSceneGraphComponent("quad grid");
		inverseQuadSGC = SceneGraphUtility.createFullSceneGraphComponent("inverse quad");
		orbitsSGC = SceneGraphUtility.createFullSceneGraphComponent("orbits");
		secondFPSGC = SceneGraphUtility.createFullSceneGraphComponent("second FP");
		Appearance ap = secondFPSGC.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, .04);
		ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.red);
		orbitsSGC.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		new Matrix(Rn.times(null, -1, Rn.identityMatrix(4))).assignTo(inverseQuadSGC);
		quadrangleSGC.addChild(quadGridSGC);
		inverseQuadSGC.addChild(quadrangleSGC);
		world.addChildren(originalCopy, reflectedCopy, secondFPSGC);
		originalCopy.addChildren(quadrangleSGC, inverseQuadSGC);
		reflectedCopy.addChild(originalCopy);
		MatrixBuilder.euclidean().reflect(new double[]{1,0,0,0}).assignTo(reflectedCopy);
		update();
		quadrangleSGC.setGeometry(quadFactory.getGeometry());
		
		gridIndices = new int[numTris*3*gridCount][2];
		for (int i = 0; i< numTris;  ++i)	{
			for (int j = 0; j< 3;  ++j)	{
				int start = (3*i+j) * gridCount,
					end = 3*i*gridCount+(((j+2) * gridCount - 1)%(3*gridCount));
				for (int k = 0; k<gridCount; ++k)	{
					gridIndices[start+k][0] = start+k;
					gridIndices[start+k][1] = end - k;
				}
			}
		}
		updateTriangleGrid(quadVerts);
		squareGridFactory.setEdgeCount(gridIndices.length);
		squareGridFactory.setEdgeIndices(gridIndices);
		squareGridFactory.update();
		quadGridSGC.setGeometry(squareGridFactory.getGeometry());

		world.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW	, false);
		world.getAppearance().setAttribute("lineShader."+CommonAttributes.DIFFUSE_COLOR, Color.black);
		world.getAppearance().setAttribute("lineShader."+CommonAttributes.LINE_WIDTH, 2.0);
		world.getAppearance().setAttribute("polygonShader."+CommonAttributes.DIFFUSE_COLOR, Color.red);
		world.getAppearance().setAttribute("polygonShader."+CommonAttributes.DIFFUSE_COLOR, Color.red);
		world.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		world.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		world.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, .5);
		draggableLine = draggableLine(dragpoints);
		MatrixBuilder.euclidean().translate(0,0,.01).assignTo(draggableLine);
		world.addChild(draggableLine);
		world.addChild(orbitsSGC);
		return world;
	}
	
	public void update()		{
		if (quadFactory == null)
			quadFactory = constructPolygonFactory(null, origQuadVerts, find, eind);
		
		currentTform = computeMatrixForAngle(angle);
		secondFPSGC.setGeometry(Primitives.point(computeSecondFPForAngle(angle)));
		
		double[] planeForm = Rn.inverse(null, Rn.transpose(null, currentTform));
		planeAtinfinity = Rn.matrixTimesVector(null, planeForm, P3.originP3);
		System.err.println("Plane at infinity = "+Rn.toString(planeAtinfinity));

		quadVerts = Rn.matrixTimesVector(null, currentTform, origQuadVerts);
		System.err.println("my transformed verts = \n"+Rn.toString(quadVerts));
		
		if (invert)	{
			double modo = (angle) % (Math.PI*2);
			if (Math.PI/2 <= modo && modo <= 3*Math.PI/2 )	{
				Rn.times(quadVerts[3], -1, quadVerts[3]);
			}
			if (Math.PI <= modo && modo <= 2*Math.PI )	{
				Rn.times(quadVerts[1], -1, quadVerts[1]);;
			}
		}

		quadFactory.setVertexAttribute(Attribute.COORDINATES, quadVerts);
		if (!useMatrix) quadFactory.update();
		if (gridIndices != null) updateTriangleGrid(quadVerts);
		if (useMatrix) new Matrix(currentTform).assignTo(quadrangleSGC);
		
		if (showOrbits) updateOrbits();
	}

	private double[] computeSecondFPForAngle(double a)	{
		double c = Math.cos(a), s = Math.sin(a);
		double k = s == 0.0 ? 10E8 : c/s;
		double[] sfp = new double[]{0,1-k,0,1+k};
		System.err.println("k = "+k);
		double [] tformed = Rn.matrixTimesVector(null, currentTform, sfp);
		System.err.println("fp1? = "+Rn.toString(Utility.dehomogenizePreserveWSign(null, sfp)));
		System.err.println("fp2? = "+Rn.toString(Utility.dehomogenizePreserveWSign(null, tformed)));
		return new double[]{0,1-k, 0, 1+k};
	}
	private double[] computeMatrixForAngle(double a) {
		double c = Math.cos(a), s = Math.sin(a), t = Math.tan(a);
		// the transformation is determined by the four point pairs given below:
		// fixed points at (-1,0) and (1,0), and points on y-axis
		// move with an elliptic type measure (but the origin is fixed!)
		double[][] v2 = {{1,0,1},{-1,0,1},{0,1,1},{0,-1,1}},
				v2t = {{1,0,1},{-1,0,1},{0,s,c},{0,-c,s}};
		double[] m2 = Pn.projectivity(null, v2, v2t);
//		System.err.println("m2 = \n"+Rn.matrixToString(m2));
		double[] myVersion = Rn.identityMatrix(4);
		int[] indices = {0,1,3,4,5,7,12,13,15};
		for (int i = 0; i<indices.length; ++i)	{
			myVersion[indices[i]] = m2[i];
		}
		// see if I can compute the second fixed point
		// the parameter k in the mma notebook is c/s
		
//		System.err.println("k:k2 = "+k/k2);
//		double[] src = new double[]{0,1-k, 0, 1+k};
		return myVersion;
	}
	
	private void updateOrbits() {
		
		int osize = 100, no = 20;
		double[][] mlist = new double[osize][];
		int n = orbitsSGC.getChildComponentCount();
		boolean hasChildren = (n > 0);
//		if (hasChildren) return;
		
		for (int j = 0; j<osize; ++j)	{
			mlist[j] = computeMatrixForAngle(.001+Math.PI*((j/((double)osize-1.0))));
		}
		for (int i = 0; i<no; ++i)	{
			double s = (i)/(no - 1.0);
			double[] start = AnimationUtility.linearInterpolation(s, 0, 1, dragpoints[0], dragpoints[1]);
			PointCollector pc = new PointCollector(2*osize, 4);
			for (int j = 0; j<osize; ++j)	{
				double[] np = Rn.matrixTimesVector(null, mlist[j], start);
//				if (np[3] < 0) Rn.times(np, -1, np);
				pc.addPoint(np);
			}
			 
			SceneGraphComponent child = hasChildren ? orbitsSGC.getChildComponent(i) : new SceneGraphComponent();
			child.setGeometry(pc.getCurve());
			if (!hasChildren) orbitsSGC.addChild(child);
		}
	}

	int gridCount = 10;
	double[][] gridVerts = new double[numTris*3*gridCount][];
	Color[] gridColors = new Color[numTris*3*gridCount];

	boolean showGrid = true;
	private int[][] gridIndices;
	private double[] currentTform;

	private void updateTriangleGrid(double[][] verts) {
		if (!showGrid) return;
		boolean[] infiniteSeg = new boolean[3*numTris];
		for (int i = 0; i<numTris; ++i)	{
			for (int j = 0; j<3; ++j)	{
				int i0 = find[i][j], i1 = find[i][(j+1)%3];
				double[] infinity;
				double[] line = PlueckerLineGeometry.lineFromPoints(null, verts[i0], verts[i1]);
				double[] pi = PlueckerLineGeometry.lineIntersectPlane(null, line, planeAtinfinity);
				double[] pi2 = Utility.pointWithCoordinates(null,
						verts[i0], verts[i1], pi, .5);
				infiniteSeg[3*i+j] = verts[i0][3] * verts[i1][3] < 0;
				infinity = infiniteSeg[3*i+j] ? pi2 : pi;
				
				for (int k = 0; k<gridCount; ++k)	{
					double u = k/(gridCount - 1.0);
					double[] tmpP1 = Utility.pointWithCoordinates(null,
							verts[i0], verts[i1], infinity, u);
//					Pn.dehomogenize(tmpP1, tmpP1);
					gridVerts[(3*i+j)*gridCount + (k)] = tmpP1;
					gridColors[(3*i+j)*gridCount + k] = tmpP1[3] < 0 ? Color.blue : Color.red;
				}
			}	
		}
		System.err.println("inf seg: ");
		for (int i = 0; i<infiniteSeg.length;++i)	{
			System.err.print(infiniteSeg[i]+" ");
		}
		// adjust signs of coordinates to reflect which segment to draw
//		int counter = 0;
//		for (int i = 0; i<numTris; ++i)	{
//			for (int j = 0; j<3*gridCount; ++j)	{
//				int localSeg = j/gridCount,
//						nextSeg = ((localSeg+1)%3);
//				boolean thisInf = infiniteSeg[3*i+localSeg] ^ infiniteSeg[3*i+nextSeg];
//				double w0 = gridVerts[counter][3];
//				boolean isInfAlready = w0 < 0;
//				if ((thisInf ^ isInfAlready))
//					Rn.times(gridVerts[counter], -1, gridVerts[counter]);
//				counter++;
//			}
//		}
//		double[][] gridVerts2 = Rn.matrixTimesVector(null, projForm.getArray(), gridVerts);
		Utility.dehomogenizePreserveWSign(gridVerts);
		squareGridFactory.setVertexCount(gridVerts.length);
		squareGridFactory.setVertexCoordinates(gridVerts);
		squareGridFactory.setVertexColors(gridColors);
		if (!useMatrix) squareGridFactory.update();
	}


	@Override
	public void setValueAtTime(double d) {
		// TODO Auto-generated method stub
		angle = AnimationUtility.linearInterpolation(d, 0, 1, Math.PI*2+Math.PI/4,  Math.PI/4);
		update();
	}

	@Override
	public Component getInspector() {
		// TODO Auto-generated method stub
		final TextSlider aSlider = new TextSlider.Double("angle",  SwingConstants.HORIZONTAL, Math.PI/4, Math.PI*2+Math.PI/4, angle);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				angle = aSlider.getValue().doubleValue();
				update();
			}
		});
		inspector.add(aSlider);
		final TextSlider xSlider = new TextSlider.Double("width",  SwingConstants.HORIZONTAL, 0, 6, width);
		xSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				width = xSlider.getValue().doubleValue();
				dragpoints[0][0] = -(dragpoints[1][0] = width);
				setPoints((PointSet) draggableLine.getGeometry(), dragpoints);
				updateOrbits();
			}
		});
		inspector.add(xSlider);
		final TextSlider ySlider = new TextSlider.Double("height",  SwingConstants.HORIZONTAL, -2, 2, height);
		ySlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				height = ySlider.getValue().doubleValue();
				dragpoints[0][1] = dragpoints[1][1] = height;
				setPoints((PointSet) draggableLine.getGeometry(), dragpoints);
				updateOrbits();
			}
		});
		inspector.add(ySlider);

		return inspector;
	}

	@Override
	public void display() {
		super.display();
		Viewer v = jrviewer.getViewer();
		CameraUtility.encompass(v);
		setBGC(v);
	}

	@Override
	public List<Plugin> getPluginsToRegister() {
		// TODO Auto-generated method stub
		super.getPluginsToRegister();
		pluginsToLoad.add(new TermesSpherePlugin());
		return pluginsToLoad;
	}

	private void setBGC(Viewer viewer) {
		final Color URBackground = new Color(.8f, .85f, .68f); //new Color(215, 215, 190);
		final Color ULBackground  = new Color(1f, .98f, .8f); //new Color(255, 255, 200);  // bg[1];
		final Color LLBackground  = new Color(.1f, .1f, .25f); //new Color(20,20,60);
		final Color LRBackground  = new Color(0.05f, .15f, .35f); //new Color(25, 25, 100);  //bg[2];
		Color[] backgroundArray = new Color[4];
		backgroundArray[0] = URBackground;
		backgroundArray[1] = ULBackground;// bg[1];
		backgroundArray[2] = LLBackground;
		backgroundArray[3] = LRBackground;  //bg[2];
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColors", backgroundArray);
	}


	public static void main(String[] args) {
		new SchatzSquare().display();
	}

	
	public static IndexedFaceSetFactory constructPolygonFactory(IndexedFaceSetFactory ifsf, 
			double[][] points, int[][] find, int[][] eind)	{
		// TODO replace this code when it's fixed to initialize the factory with the existing ifs.
		if (ifsf == null) ifsf = new IndexedFaceSetFactory();// Pn.EUCLIDEAN, true, false, true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setVertexCount(points.length);
		ifsf.setVertexCoordinates(points);
		ifsf.setFaceCount(find.length);
		ifsf.setFaceIndices(find);
		ifsf.setEdgeCount(eind.length);
		ifsf.setEdgeIndices(eind);
		ifsf.update();
		
		return ifsf;
	}
	
	// not used anymore
	private void updateSquareGrid(double[][] verts) {
		if (!showGrid) return;
		for (int i = 0; i<2; ++i)	{
			int ll, ul, lr, ur;
			if (i == 0) { // edges 01 and 23
				ll = 0; ul = 1; lr = 3; ur = 2;
			} else {		  // edges 12 and 30
				ll = 1; ul = 2; lr = 0; ur = 3;
			}
			double[] leftInf, rightInf;
			double[] line = PlueckerLineGeometry.lineFromPoints(null, verts[ll], verts[ul]);
			double[] pi = PlueckerLineGeometry.lineIntersectPlane(null, line, planeAtinfinity);
			double[] pi2 = Utility.pointWithCoordinates(null,
					verts[ll], verts[ul], pi, .5);
			boolean goesInfL = verts[ll][3] * verts[ul][3] < 0;
			leftInf = goesInfL ? pi2 : pi;
			
			line = PlueckerLineGeometry.lineFromPoints(null, verts[lr], verts[ur]);
			pi = PlueckerLineGeometry.lineIntersectPlane(null, line, planeAtinfinity);
			double[] rpi2 = Utility.pointWithCoordinates(null,
					verts[lr], verts[ur], pi, .5);
			boolean goesInfR = verts[lr][3] * verts[ur][3] < 0;
			rightInf = goesInfR ? rpi2 : pi;
			
			for (int j = 0; j<gridCount; ++j)	{
				double u = j/(gridCount - 1.0);
				double[] tmpP1 = Utility.pointWithCoordinates(null,
						verts[ll], verts[ul], leftInf, u);
				boolean negWL = tmpP1[3] < 0;
				gridVerts[i*gridCount + (j)] = tmpP1;
				double[] tmpP = Utility.pointWithCoordinates(null,
						verts[lr], verts[ur], rightInf, u);
				boolean negWR = tmpP[3] < 0;
				// if exactly one of the segments is at infinity, make sure the 
				// two endpoints have different w-signs, so it goes thru inf als
				if ((goesInfL ^ goesInfR) && !(negWL ^ negWR)) {
					Rn.times(tmpP, -1, tmpP);
				}
				gridVerts[2*gridCount+i*gridCount + (j)] = tmpP;
			}
		}
//		double[][] gridVerts2 = Rn.matrixTimesVector(null, projForm.getArray(), gridVerts);
		Utility.dehomogenizePreserveWSign(gridVerts);
		squareGridFactory.setVertexCount(gridVerts.length);
		squareGridFactory.setVertexCoordinates(gridVerts);
		squareGridFactory.update();
	}


	private SceneGraphComponent draggableLine(final double[][] pts) {
		
		SceneGraphComponent toolSGC = SceneGraphUtility.createFullSceneGraphComponent("tool");
		IndexedLineSetFactory ils = IndexedLineSetUtility.createCurveFactoryFromPoints(pts, false);
		toolSGC.setGeometry(ils.getGeometry());
		Appearance ap = toolSGC.getAppearance(); 
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, .02);
		ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.red);
		DragEventTool t = new DragEventTool();
		t.addPointDragListener(new PointDragListener() {

			public void pointDragStart(PointDragEvent e) {
				System.out.println("drag start of vertex no "+e.getIndex());				
			}

			public void pointDragged(PointDragEvent e) {
				PointSet pointSet = e.getPointSet();
				//double[][] points=new double[pointSet.getNumPoints()][];
		        pts[e.getIndex()]=e.getPosition();  
		        pointSet.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(pts));			
			}

			public void pointDragEnd(PointDragEvent e) {
			}
			
		});
		
		toolSGC.addTool(t);

		return toolSGC;
	}
	
	private void setPoints(PointSet ps, double[][] pts)	{
        ps.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(pts));			
	}

}
