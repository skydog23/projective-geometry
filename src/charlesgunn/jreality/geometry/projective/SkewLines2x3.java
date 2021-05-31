/*
 * Created on Apr 29, 2021
 *
 */
package charlesgunn.jreality.geometry.projective;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.math.clifford.ThreeSpace;
import charlesgunn.math.p5.PlueckerLineGeometry;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class SkewLines2x3  {

	protected double[][] rLines = null,
			lLines = null;
	protected double[][][] 
			pt9Lines = null,
			pl9Lines = null;
	protected static double[][]	defRL = null, defLL = null;
	protected ZigZag[] zigzags = new ZigZag[perms.length];
	protected ReyesConfiguration[] reyesConfig = new ReyesConfiguration[2];
	protected boolean pascal = false;  // true: add pascal planes
	static double[][] cubeZigZag = {
			{1,1,1,1},
			{1,-1,1,1},
			{1,-1,-1,1},
			{-1,-1,-1,1},
			{-1,1,-1,1},
			{-1,1,1,1}
	};
	{
		System.err.println("Constructing default");
		double [][] ptr = cubeZigZag;
		defRL = new double[6][];
		defLL = new double[6][];
		for (int i = 0; i<3; ++i)	{
			defRL[i] = PlueckerLineGeometry.lineFromPoints(null, ptr[2*i], ptr[2*i+1]);
			defLL[i] = PlueckerLineGeometry.lineFromPoints(null, ptr[2*i+1], ptr[(2*i+2)%6]);
		}
	}
	protected double[][][] points = new double[3][3][],
			planes = new double[3][3][];
	protected SceneGraphComponent sgcRepn;
	double implodeFactor =  0;

	public SkewLines2x3() {
		this( defRL, defLL);
	}
	
	public SkewLines2x3(double[][] rl, double[][] ll) {
		super();
		if (rl == null) rl = defRL;
		if (ll == null) ll = defLL;
		setLines(rl, ll);
		sgcRepn = SceneGraphUtility.createFullSceneGraphComponent("skewlines2x3");
		Appearance  ap = sgcRepn.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.SPHERES_DRAW, true);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, true);
		ap.setAttribute(CommonAttributes.POINT_RADIUS, .01);
		ap.setAttribute(CommonAttributes.TUBE_RADIUS, .015);
//		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		Color[] threec = {y, g, c};
		for (int i = 0; i<6; ++i)	{
			zigzags[i] = new ZigZag(i);
			SceneGraphComponent sgc = zigzags[i].getSgcRepn();
			sgcRepn.addChild(sgc);
			sgc.setVisible(i <= 2);
			sgc.getAppearance().setAttribute("polygonShader.diffuseColor", threec[i%3]);
			sgc.getAppearance().setAttribute("lineShader.diffuseColor",
					AnimationUtility.linearInterpolation(threec[i%3],Color.BLACK, .2));
		}
		update();
		
		// generate the two reyes configurations
		double[][] rpts = new double[12][],
				rlns = new double[16][],
				rplns = new double[12][];
		for (int  i = 0; i<9; ++i)	{
			rpts[i] = points[i/3][i%3];
			rplns[i] = planes[i/3][i%3];
		}
		double[][] lines6 = zigzags[0].getLines();
		for (int i = 0; i<6; ++i)	{
			rlns[i] = lines6[i];
		}
		
		for (int j = 0; j<2; ++j)	{
			// "point-based" features two more cube verts and center
			// "brianchon" points are the two "other" cube verts and center of cube
			if (j == 0)	{   
				for (int i = 0; i < 3; ++i) {
					rpts[i + 9] = zigzags[i].getBrianchonPoint();
				}
				// these are the planes meeting at the centerpoint
				for (int i = 0; i < 3; ++i) {
					rplns[i + 9] = getPlaneForPerm(perms[i+3]);
				}
				double[][] bp = zigzags[0].getBrianchonPlanes();
				for (int i = 0; i<3; ++i)	{
					// three space diagonals of the cube
					rlns[i+6] = PlueckerLineGeometry.lineFromPlanes(null, 
							bp[(i+1)%3], 
							bp[(i+2)%3]);
				}
				// the next six lines are joining lines of the pascal points with
				// the vertices of the zigzag
				double[][] pascalPoints = zigzags[0].getPascalPoints();
				double[][] cubeVerts = {zigzags[1].getBrianchonPoint(),
						zigzags[2].getBrianchonPoint()};
				for (int i = 0; i<3; ++i)	{
					rlns[i+9] = PlueckerLineGeometry.lineFromPoints(null, 
							pascalPoints[i], 
							cubeVerts[0]);
					rlns[i+12] = PlueckerLineGeometry.lineFromPoints(null, 
							pascalPoints[i], 
							cubeVerts[1]);
				}
				rlns[15] = PlueckerLineGeometry.lineFromPoints(null, cubeVerts[0], cubeVerts[1]);
			}
			else if (j == 1)	{   
				for (int i = 0; i < 3; ++i) {
					rplns[i + 9] = zigzags[i].getPascalPlane();
				}
				for (int i = 0; i < 3; ++i) {
					rpts[i + 9] = getPointForPerm(perms[i+3]);
				}
				double[][] pp = zigzags[0].getPascalPoints();
				for (int i = 0; i < 3; ++i) {
					rlns[i+6] = PlueckerLineGeometry.lineFromPoints(null, 
							pp[(i+1)%3], 
							pp[(i+2)%3]);
				}
				double[][] brianchonPlanes = zigzags[0].getBrianchonPlanes();
				double[][] octFaces = {zigzags[1].getPascalPlane(),
						zigzags[2].getPascalPlane()};
				for (int i = 0; i<3; ++i)	{
					rlns[i+9] = PlueckerLineGeometry.lineFromPlanes(null, 
							brianchonPlanes[i], 
							octFaces[0]);
					rlns[i+12] = PlueckerLineGeometry.lineFromPlanes(null, 
							brianchonPlanes[i], 
							octFaces[1]);
				}
				rlns[15] = PlueckerLineGeometry.lineFromPlanes(null, octFaces[0], octFaces[1]);
			}
			System.err.println("points: " + Rn.toString(rpts));
			System.err.println("planes: " + Rn.toString(rplns));
			System.err.println("lines 1: " + Rn.toString(rlns));
			reyesConfig[j] = new ReyesConfiguration(rpts, rlns, rplns);
			reyesConfig[j].getSceneGraphRepn().setVisible(j==0);
			sgcRepn.addChildren(reyesConfig[j].getSceneGraphRepn());
		} 
		
		for (int i = 0; i<6; ++i)	{
			SceneGraphComponent sgc = zigzags[i].getSgcRepn();
			sgcRepn.addChild(sgc);
		}
	}
	

	private void setLines(double[][] rl, double[][] ll) {
		rLines = rl;
		lLines = ll;
		updatePtsPlnsLns();

	}

	static int[][] perms = {{0,1,2},{1,2,0},{2,0,1},{0,2,1},{1,0,2},{2,1,0}}; // maybe don't need others?

	public double[] getPlaneForPerm(int[] perm)	{
		double[][] pts = new double[3][];
		for (int i = 0; i<3; ++i)	{
			pts[i] = points[i][perm[i]];
		}
		return join3Points(null, pts);
	}
	public double[] getPointForPerm(int[] perm)	{
		double[][] pts = new double[3][];
		for (int i = 0; i<3; ++i)	{
			pts[i] = planes[i][perm[i]];
		}
		return meet3Planes(null, pts);
	}
	
	private void updatePtsPlnsLns() {
		for (int i = 0; i<3; ++i)	{
			for (int j = 0; j<3; ++j)	{
				points[i][j] = Pn.normalize(null, 
						PlueckerLineGeometry.intersectionPoint(null, rLines[i], lLines[j]),
						Pn.ELLIPTIC);
				if (points[i][j][3]<0) Rn.times(points[i][j], -1, points[i][j]);
				planes[i][j] = Pn.normalize(null, 
						PlueckerLineGeometry.intersectionPlane(null, rLines[i], lLines[j]),
						Pn.ELLIPTIC);
			}
		}
		pt9Lines = new double[3][3][];
		pl9Lines = new double[3][3][];
		
		for (int i = 0; i<3; ++i)	{
			for (int j = 0; j<3; ++j)	{
				int j1 = (j+1)%3;
				pt9Lines[i][j] = PlueckerLineGeometry.lineFromPoints(null, points[j][perms[i][j]], points[j1][perms[i][j1]]);
				pl9Lines[i][j] = PlueckerLineGeometry.lineFromPlanes(null,  planes[j][perms[i][j]], planes[j1][perms[i][j1]]);
				
			}
		}
		System.err.println("skewlines: pt 9 lines = \n"+Rn.toString(pt9Lines));
	}

	public void update() {
		
		updatePtsPlnsLns();
		
		for (int i = 0; i<6; ++i)	{
			zigzags[i].update();
		}
	}

	public SceneGraphComponent getSgcRepn() {
		return sgcRepn;
	}

	public double[][] getRLines() {
		return rLines;
	}

	public void setrLines(double[][] rLines) {
		this.rLines = rLines;
	}

	public Box getInspector(Box inspector) {
		Box hbox = Box.createHorizontalBox();
		inspector.add(hbox);

		for (int i = 0; i < 6; ++i) {
			final JCheckBox visCB = new JCheckBox("show " + i);
			final SceneGraphComponent foo = getSgcRepn().getChildComponent(i);
			visCB.setSelected(foo.isVisible());
			hbox.add(visCB);
			final int j = i;
			visCB.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					boolean boo = visCB.isSelected();
					foo.setVisible(boo);
				}
			});
		}
		hbox = Box.createHorizontalBox();
		inspector.add(hbox);
		
		for (int i = 0; i < 2; ++i) {
			final JCheckBox visCB = new JCheckBox("show RC " + i);
			final SceneGraphComponent foo = reyesConfig[i].getSceneGraphRepn();
			visCB.setSelected(foo.isVisible());
			hbox.add(visCB);
			final int j = i;
			visCB.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					boolean boo = visCB.isSelected();
					foo.setVisible(boo);
				}
			});
		}

		final JButton flipB = new JButton("flip Reyes Config ");
		hbox.add(flipB);
		flipB.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				for (int j = 0; j<2; ++j)	
					reyesConfig[j].getSceneGraphRepn().setVisible(
							!reyesConfig[j].getSceneGraphRepn().isVisible());
			}
		});

		final TextSlider aSlider = new TextSlider.Double("transp",  SwingConstants.HORIZONTAL,
				0, 1, implodeFactor);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				implodeFactor = aSlider.getValue().doubleValue();
				for (int i = 0; i<6; ++i)	{
					zigzags[i].getSgcRepn().getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, implodeFactor > 0.0);
					zigzags[i].getSgcRepn().getAppearance().setAttribute("polygonShader.transparency", implodeFactor);
				}

			}
		});
		inspector.add(aSlider);

		return inspector;
	}
	
	static public double[] meet3Planes(double[] dst, double[][] pls) {
		double[] pt = PlueckerLineGeometry.lineIntersectPlane(dst,
				PlueckerLineGeometry.lineFromPlanes(null, pls[0], pls[1]), 
				pls[2]);
		return Pn.normalize(pt, pt, Pn.ELLIPTIC);
	}
	static public double[] join3Points(double[] dst, double[][] pls) {
		double[] pt = PlueckerLineGeometry.lineJoinPoint(dst,
				PlueckerLineGeometry.lineFromPoints(null, pls[0], pls[1]), 
				pls[2]);
		return Pn.normalize(pt, pt, Pn.ELLIPTIC);
	}
	// this class provides access into the 3x3 point and 3x3 plane arrays
	// corresponding to choosing a zig-zag path of 6 vertices in these arrays.
	protected class ZigZag {
		int[] perm = {0,1,2};
		int[][] zigzag = new int[6][2],      // indices for the 6 included elements
				paschon = new int[3][2];     // indices for the 3 omitted elements
		double[][] lines = new double[6][];  // line connecing the 6 included elements
		double[][] zzvertices = new double[6][],
				pascalPoints = new double[3][],
				zzplanes = new double[6][],
				brianchonPlanes = new double[3][];
		double[] pascalPlane, brianchonPoint;
		
//		double[][] zzverts = new double[8][], 
//				pverts = new double[3][],
//				bverts = new double[4][],
//				bplanes = new double[3][];
		SceneGraphComponent 
			worldSGC,
			    zigzagSGC,
			    octaSGC,
			    cubeSGC,
			    pascalSGC,
			    brianchonSGC;
		IndexedLineSetFactory zigzagFac = new IndexedLineSetFactory(),
				brianchonFac = new IndexedLineSetFactory();
		IndexedFaceSetFactory pascalFac = new IndexedFaceSetFactory(),
				octaFac = new IndexedFaceSetFactory(),
				cubeFac = new IndexedFaceSetFactory();
		 
		protected ZigZag(int which) {
			super();
			perm = perms[which];
			worldSGC = SceneGraphUtility.createFullSceneGraphComponent("zigzag"+which);
			zigzagSGC = SceneGraphUtility.createFullSceneGraphComponent("zigzag"+which);
			octaSGC = SceneGraphUtility.createFullSceneGraphComponent("octa"+which);
			cubeSGC = SceneGraphUtility.createFullSceneGraphComponent("cube"+which);
			pascalSGC = SceneGraphUtility.createFullSceneGraphComponent("pascal"+which);
			brianchonSGC = SceneGraphUtility.createFullSceneGraphComponent("brianchon"+which);
			worldSGC.addChildren(zigzagSGC, octaSGC, cubeSGC, brianchonSGC, pascalSGC);
			Appearance ap=worldSGC.getAppearance();
			ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
			// surprise! implode shader shits in its pants in elliptic space
//			DefaultGeometryShader dgs = (DefaultGeometryShader) 
//		   			ShaderUtility.createDefaultGeometryShader(ap, true);
//			ImplodePolygonShader dps = (ImplodePolygonShader) dgs.createPolygonShader("implode");
			ap.setAttribute("polygonShader.implodeFactor", implodeFactor);
			ap = pascalSGC.getAppearance();
			ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
			ap.setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, true);
			ap.setAttribute(CommonAttributes.TRANSPARENCY, .6);
			ap = octaSGC.getAppearance();
			ap.setAttribute("lineShader.tubeRadius",.01);
			// construct the zigzag by permuting the indices of the left lines
			System.err.println("zigzag "+which);
			for (int i = 0; i<6; ++i) {
			    zigzag[i][0] = i/2;
			    zigzag[i][1] = perm[((i+1)/2) %3];
			    // the rule is, move first on r-line, then on left-line
			    lines[i] = (i%2 == 0) ? rLines[i/2] : lLines[ perm[((i+1)/2) %3]]; 
			    zzvertices[i] = points[zigzag[i][0]][zigzag[i][1]].clone();
			    if (which == 1 && zzvertices[i][3] == 0.0)
			    		Rn.times(zzvertices[i], -1, zzvertices[i]);
			    System.err.println("zz[i]"+i+"="+zigzag[i][0]+":"+zigzag[i][1]);
			}
			for (int i = 0; i<3; ++i) {
				paschon[i][0] = i;
				paschon[i][1] = perm[(i+2)%3];
				// these are the points not included in the zigzag
				pascalPoints[i] = points[i][perm[(i+2)%3]];
				// these are the planes not included in the zigzag
				brianchonPlanes[i] = planes[i][perm[(i+2)%3]];
			}
			brianchonPoint = meet3Planes(null, brianchonPlanes);
			pascalPlane = join3Points(null, pascalPoints);
		}

//		private double[] meetOf3ZZPlanes(int[] ia) {
//			double[][] ploop = new double[3][];
//			for (int i = 0; i<3; ++i)	{
//				ploop [i ] = planes[zigzag[ia[i]][0]][ zigzag[ia[i]][1]];
//			}
//			return meet3Planes(null, ploop);
//		}
		Color rc = Color.blue, bc = Color.red;
		public void update()	{
//			double[][] zzt = new double[8][];
//			for (int i = 0; i<6; ++i)	zzt[i] = zzvertices[i];
//			double[][] tmp = new double[][]{meetOf3ZZPlanes(new int[]{0,2,4}), meetOf3ZZPlanes(new int[]{1,3,5})};
//			zzt[6] = tmp[0];
//			zzt[7] = tmp[1];
			zigzagFac.setVertexCount(6);
			zigzagFac.setVertexCoordinates(zzvertices);
			zigzagFac.setEdgeCount(6);
			zigzagFac.setEdgeIndices(new int[][]{{0,1},{1,2},{2,3},{3,4},{4,5},{5,0}});
			zigzagFac.setEdgeColors(new Color[]{rc,bc,rc,bc,rc,bc});
			zigzagFac.setMetric(Pn.ELLIPTIC);
		    zigzagFac.update();
			zigzagSGC.setGeometry(zigzagFac.getGeometry());
			octaFac.setVertexCount(6);
			octaFac.setVertexCoordinates(zzvertices);
			octaFac.setFaceCount(8);
			octaFac.setFaceIndices(new int[][]
					{{0,1,2},{1,2,3},{2,3,4},{3,4,5},{4,5,0},{5,0,1},
					{0,2,4}, {1,3,5}});
			octaFac.setGenerateEdgesFromFaces(true);
			octaFac.setGenerateFaceNormals(true);
			octaFac.setMetric(Pn.ELLIPTIC);
			octaFac.update();
			octaSGC.setGeometry(octaFac.getGeometry());
			pascalFac = IndexedFaceSetUtility.constructPolygonFactory(null, pascalPoints, Pn.ELLIPTIC);
			pascalFac.setMetric(Pn.ELLIPTIC);
			pascalFac.update();
			pascalSGC.setGeometry(pascalFac.getGeometry());
//			double[][] bverts = {brianchon
//			brianchonFac.setVertexCount(4);
//			brianchonFac.setVertexCoordinates();
//			brianchonFac.setEdgeCount(3);
//			brianchonFac.setEdgeIndices(new int[][]{{0,3},{1,3},{2,3}});
//			brianchonFac.setMetric(Pn.ELLIPTIC);
//			brianchonFac.update();
//			brianchonSGC.setGeometry(brianchonFac.getGeometry());
		}

		public int[][] getZigzag() {
			return zigzag;
		}

		public int[][] getPaschon() {
			return paschon;
		}

		public double[][] getLines() {
			return lines;
		}

		public double[][] getVertices() {
			return zzvertices;
		}

		public double[][] getPascalPoints() {
			return pascalPoints;
		}

		public double[][] getPlanes() {
			return zzplanes;
		}

		public double[][] getBrianchonPlanes() {
			return brianchonPlanes;
		}

		public double[] getPascalPlane() {
			return pascalPlane;
		}

		public double[] getBrianchonPoint() {
			return brianchonPoint;
		}

		public SceneGraphComponent getSgcRepn() {
			return worldSGC;
		}


	};

	Color y = Color.yellow, g = Color.green, m = new Color(200,0,120), 
			c = Color.cyan, bl = Color.black, vi = new Color(100,0,180),
			red = Color.red, blue = Color.blue,
	bl2 = new Color(40,40,40), bl3 = new Color(20,20,20), gr = new Color(0,135,50);

	protected class ReyesConfiguration	{
		double[][] rpoints, rlines, rplanes;
		SceneGraphComponent rcSGC = SceneGraphUtility.createFullSceneGraphComponent("rc"),
		    rpointsSGC  = SceneGraphUtility.createFullSceneGraphComponent("rc points"),
		    rlinesSGC   = SceneGraphUtility.createFullSceneGraphComponent("rc lines"),
		    rplanesSGC = SceneGraphUtility.createFullSceneGraphComponent("rc planes");

		private double tubeR = .015;
		private int numLines = 8;

		protected ReyesConfiguration(double[][] pts, double[][] lns, double[][] plns) {
			super();
			rpoints = pts.clone();
			rlines = lns.clone();
			rplanes = plns.clone();
			printIncidence();
			Appearance ap = rpointsSGC.getAppearance();
			ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
			ap.setAttribute(CommonAttributes.POINT_RADIUS, .02);
			ap = rlinesSGC.getAppearance();
			ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
			ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
//			ap.setAttribute(CommonAttributes.FACE_DRAW, false);
			
			ap.setAttribute(CommonAttributes.TUBE_RADIUS, tubeR);
			
			Color[] pointC = new Color[]{
					g,g,g,g,g,g,g,g,g,   // 3x3 points
					vi,vi,vi, 	// three "brianchon" points
			};
			
			Color[] lineC = new Color[]{
					blue,red,blue,red,blue,red,   // zig-zag
					y,y,y, 	// three space diagonals of cube
					g,g,g,
					c,c,c,  // lines through two other "Brianchon" pts
					Color.white   // the fourth space diagonal
			};

			PointSetFactory psf = new PointSetFactory();
			psf.setVertexCount(12);
			psf.setVertexCoordinates(pts);
			psf.setVertexColors(pointC);
			psf.update();
			rpointsSGC.setGeometry(psf.getGeometry());
			
//			PointRangeFactory[] lineFactories = new PointRangeFactory[16];
			ap = rlinesSGC.getAppearance();
			ap.setAttribute("lineShader.diffuseColor", Color.black);
//			ap.setAttribute(CommonAttributes.FACE_DRAW, true);
			ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
			ap.setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.unitCube);
			for (int i = 0; i<16; ++i)	{
//				lineFactories[i] = new PointRangeFactory(); 
//				lineFactories[i].setFiniteSphere(false);
//				lineFactories[i].setPluckerLine(rlines[i]);
//				lineFactories[i].update();
				IndexedFaceSet eds = PlueckerLineGeometry.equidistantSurface(rlines[i], tubeR, numLines);
				SceneGraphComponent child = new SceneGraphComponent("line"+i);
				rlinesSGC.addChild(child);
				child.setAppearance(new Appearance());
				child.getAppearance().setAttribute("polygonShader.diffuseColor", lineC[i]);
				child.setGeometry(eds);
			}

			rcSGC.addChildren(rpointsSGC, rlinesSGC, rplanesSGC);
		}
		
		protected SceneGraphComponent getSceneGraphRepn() {
			return rcSGC;
		}
		
		protected void printIncidence()	{
			int[][] inc = new int[12][12];
			String[] str = new String[12];
			for (int i = 0; i<12; ++i)	{
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j<12; ++j)	{
					double d = Rn.innerProduct(rplanes[i], rpoints[j]);
					boolean b = (Math.abs(d) < 10E-4);
					inc[i][j] = b ? 1 : 0;
					sb.append(b ? 'x' : 'o');
				}
				str[i] = sb.toString();
			}
			System.err.println("incidence matrix:\n");
			for (int i = 0; i<12; ++i)	{
				System.err.println(str[i]);
			}
			str = new String[16];
			ThreeSpace ts = new ThreeSpace(Pn.ELLIPTIC);
			for (int i = 0; i<16; ++i)	{
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j<12; ++j)	{
					int[] permo = new int[]{1,2,3,0};
//					double[] newcoords = 
////							PlueckerLineGeometry.dualizeLine(null, 
////									PlueckerLineGeometry.permuteCoordinates(null,rlines[i], permo));
//							PlueckerLineGeometry.dualizeLine(null, rlines[i]);
////					double[] newplane = permutePCoords(rplanes[j], new int[]{3,0,1,2});
//					MultiVectorP3 lineGA = MultiVectorP3.line(newcoords),
//							planeGA = MultiVectorP3.point(rpoints[j]);
//					MultiVectorP3 pointGA = MultiVectorP3.join(null, lineGA, planeGA);
//					double d = MultiVectorP3.scalarFrom(ts.gp(null, pointGA, pointGA));
////					double d = Rn.innerProduct(rlines[i], rpoints[j]);
					double[] plane = PlueckerLineGeometry.lineJoinPoint(null, rlines[i], rpoints[j]);
					double d = Rn.innerProduct(plane, plane);
					boolean b = (Math.abs(d) < 10E-3);
					sb.append(b ? 'x' : 'o');
				}
				str[i] = sb.toString();
			}
			System.err.println("line-plane incidence matrix:\n");
			for (int i = 0; i<16; ++i)	{
				System.err.println(str[i]);
			}
		}
	}

	public double[] permutePCoords(double[] ds, int[] p) {
		return new double[]{ds[p[0]], ds[p[1]], ds[p[2]], ds[p[3]]};
	}
	}
