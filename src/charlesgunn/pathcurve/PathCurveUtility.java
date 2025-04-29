package charlesgunn.pathcurve;
import java.awt.Color;

import charlesgunn.jreality.geometry.ClipBox;
import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.math.Complex;
import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

/*
 * Created on Dec 18, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PathCurveUtility {
	
	public static double[][] pathCurveOrbit(Complex[] eigenvalues, double[] coordinateSystem, double[] p, double[] tvals) {
		return pathCurveOrbit(null, eigenvalues, coordinateSystem, p, tvals);
	}
		
	public static double[][] pathCurveOrbit(double[][] vals, Complex[] eigenvalues, double[] coordinateSystem, double[] p, double[] tvals) {
		int n = tvals.length;
		if (vals == null) vals = new double[tvals.length][p.length];
		// check eigenvalues 
		if (eigenvalues.length != 4)	{
			throw new IllegalArgumentException("Invalid number of eigenvalues");
		}
		if (eigenvalues[0].im != 0)	{
			if (!Complex.equals(eigenvalues[0], Complex.conjugate(null, eigenvalues[1]), 10E-8))	{
				throw new IllegalArgumentException("complex eigenvalues must come in conjugate pairs");
			}
		}
		if (eigenvalues[2].im != 0)	{
			if (!Complex.equals(eigenvalues[2], Complex.conjugate(null, eigenvalues[3]), 10E-8))	{
				throw new IllegalArgumentException("complex eigenvalues must come in conjugate pairs");
			}
		}
		for (int i = 0; i<n ; ++i)	{
			double[] m = exponentialMatrixAtTime(eigenvalues, tvals[i]);
			double[] mm = Rn.conjugateByMatrix(null, m, coordinateSystem);
			Rn.matrixTimesVector(vals[i], mm, p);
		}
		return vals;
	}
	
	public static IndexedFaceSet pathCurveMesh(Complex[] eigenvalues, double[] coordinateSystem, double[][] initialVals, double[] tvals) {
		int n = initialVals.length;
		int vl = initialVals[0].length;
		int k = tvals.length;
		QuadMeshFactory qmsf = new QuadMeshFactory();

		qmsf.setULineCount(n);
		qmsf.setVLineCount(k);
		qmsf.setGenerateVertexNormals(true);
		qmsf.setGenerateEdgesFromFaces(true);
		double[][] vals = new double[n*k][vl];
		for (int j = 0; j < k; ++j) {
			double[] m = exponentialMatrixAtTime(eigenvalues, tvals[j]);
			double[] mm = Rn.conjugateByMatrix(null, m, coordinateSystem);
			for (int i = 0; i < n; ++i) {
				Rn.matrixTimesVector(vals[i + j * n], mm, initialVals[i]);
			}
		}
		//Pn.dehomogenize(vals, vals);
		qmsf.setVertexCoordinates(vals);
		qmsf.update();
		
		return qmsf.getIndexedFaceSet();
	}
	/**
	 * @param eigenvalues
	 * @param d
	 * @return
	 */
	static double[] exponentialMatrixAtTime(Complex[] eigenvalues, double d) {
		int n = eigenvalues.length;
		Complex[] exp = new Complex[eigenvalues.length];
		for (int i = 0; i<n; ++i)	{
			exp[i] = Complex.exp(null, Complex.times(null, d, eigenvalues[i]));
		}
		double[] m = Rn.identityMatrix(4);
		for (int i = 0; i<4; ++i)	{
			if (exp[i].im != 0)	{
				m[i*4+i] = m[(i+1)*4+i+1] = exp[i].re;
				m[i*4+i+1] = -exp[i].im;
				m[(i+1)*4+i] = exp[i].im;
				i++;
			} else
				m[i*4+i] = exp[i].re;
		}
		return m;
	}

	/**
	 * @param ev
	 * @param coordinateSystem
	 * @param orbit
	 * @param tmin
	 * @param tmax
	 * @param n
	 * @return
	 */
	public static IndexedFaceSet pathCurveMesh(Complex[] ev, double[] coordinateSystem, double[][] orbit, double tmin, double tmax, int n) {
		double[] tvals = new double[n];
		for (int j = 0; j<n; ++j)	{
			tvals[j] = tmin + (tmax-tmin) * j /(n-1.0);
		}
		return pathCurveMesh(ev, coordinateSystem, orbit, tvals);
	}

	public static double[][] pathCurveOrbit(Complex[] ev, double[] coordinateSystem, double[] p, double tmin, double tmax, int n) {
		return pathCurveOrbit(null, ev, coordinateSystem, p, tmin, tmax, n);
	}
	/**
	 * @param ev
	 * @param coordinateSystem
	 * @param p
	 * @param d
	 * @param e
	 * @param i
	 * @return
	 */
	public static double[][] pathCurveOrbit(double[][] vals, Complex[] ev, double[] coordinateSystem, double[] p, double tmin, double tmax, int n) {
		double[] tvals = new double[n];
		for (int j = 0; j<n; ++j)	{
			tvals[j] = tmin + (tmax-tmin) * j /(n-1.0);
		}
		return pathCurveOrbit(vals, ev, coordinateSystem, p, tvals);
	}

	public static SceneGraphComponent makeSemiImWorld(int which) {
		double d = Math.log(1.4);
		double e = Math.log(.8);
		double alpha = Math.PI/8;
		double radius = .3;
		double[] coordinateSystem = Rn.identityMatrix(4);
		coordinateSystem[14] = 1.0;
		double[] p = {radius,0,.5,1};
		
		double tmin = -3*Math.PI;
		double tmax = 3*Math.PI;
		if (which == 1)	{
			d = e = 0.0;
			p[0] = 0.4;
			p[2] = .1;
		}
		int steps = 40; //80;
		
		// a circle as seed form
		Complex[] ev = new Complex[4];
		ev[0] = new Complex(0, alpha);
		ev[1] = Complex.conjugate(null, ev[0]);
		ev[2] = new Complex(d,0);
		ev[3] = new Complex(e,0);
		PathCurveFactory pcf = new PathCurveFactory();
		pcf.setCoordinateSystem(coordinateSystem);
		pcf.setEigenvalues(ev);
		pcf.setTmin(tmin);
		pcf.setTmax(tmax);
		pcf.setNumberSteps(steps);
		pcf.setInitialPoint(p);
		pcf.update();
//		double[][] orbit = pathCurveOrbit(ev, coordinateSystem, p, tmin, tmax, steps);
		tmin = -Math.PI;
		tmax = Math.PI;
		steps = 20; //50;
		if (which == 1)	{
			d = Math.log(1.5);
			e = Math.log(1.25);
			tmin = 0 * Math.PI;
			tmax = 6 * Math.PI;
			alpha = Math.PI/10.0;
		} else
			alpha = -1.56*alpha;
		
		ev = new Complex[4];
		ev[0] = new Complex(0,alpha);
		ev[1] = Complex.conjugate(null, ev[0]);
		ev[2] = new Complex(0,0);
		ev[3] = new Complex(0,0);
		ev[2] = new Complex(d,0);
		ev[3] = new Complex(e,0);
		PathCurveParametricSurfaceFactory pcpsf = new PathCurveParametricSurfaceFactory();
		pcpsf.setCoordinateSystem(coordinateSystem);
		pcpsf.setEigenvalues(ev);
		pcpsf.setInitialCurveFactory(pcf);
		pcpsf.setTmin(tmin);
		pcpsf.setTmax(tmax);
		pcpsf.setNumberSteps(steps);
		pcpsf.update();
		
		IndexedFaceSet qms = pcpsf.getIndexedFaceSet();
//		IndexedFaceSet qms = pathCurveMesh(ev, coordinateSystem, orbit, tmin, tmax, steps);
//		qms = GeometryUtilityOverflow.diamondize(qms, .38);
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent();
		world.setGeometry(qms);
		
		//SceneGraphComponent tetra = SceneGraphUtility.createFullSceneGraphComponent("tetra");
		//IndexedFaceSet tet = Primitives.tetrahedron();
		//double[][] verts = {{1,1,1},{1,-1,-1},{-1,1,-1},{-1,-1,1}};
		//double[][] tverts = basisFromMatrix(coordinateSystem);
		//DataList dl = StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(tverts);
		//tet.setVertexCountAndAttributes(Attribute.COORDINATES, dl);
		//world.addChild(TubeUtility.ballAndStick(tet,.02,.01, Color.GRAY, Color.RED, Pn.EUCLIDEAN));
		
		double[][] verts = {{0,0,0},{0,0,1}};
		int[][] indices = {{0,1}};
		DataList dl = StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(verts);
		IndexedLineSet axs = new IndexedLineSet(2,1);
		axs.setVertexAttributes(Attribute.COORDINATES, dl);
		axs.setEdgeAttributes(Attribute.INDICES, StorageModel.INT_ARRAY.array().createReadOnly(indices));
//		SceneGraphComponent thickAxes = TubeUtility.ballAndStick(axs, .003, .003, Color.RED, Color.RED, Pn.EUCLIDEAN);
		BallAndStickFactory basf = new BallAndStickFactory(axs);
		basf.setBallColor(Color.RED);
		basf.setBallRadius(.003);
		basf.setStickColor(Color.RED);
		basf.setStickRadius(.003);
		basf.setMetric(Pn.EUCLIDEAN);
		basf.update();
		SceneGraphComponent thickAxes = basf.getSceneGraphComponent();
		thickAxes.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER, "default");
//		world.addChild(thickAxes);
		
		return world;
	}
	
	static ImaginaryPathCurveSurface ipcs = new ImaginaryPathCurveSurface();
	public static SceneGraphComponent makeImaginaryWorld() {
		ipcs = new ImaginaryPathCurveSurface();
		return ipcs.getSGC();
	}
/**
	 * @param coordinateSystem
	 * @return
	 */
	public static double[][] basisFromMatrix(double[] coordinateSystem) {
		double[][] basis = new double[4][4];
		for (int i = 0; i<4; ++i)	{
			for (int j = 0; j<4; ++j)	{
				basis[i][j] = coordinateSystem[j*4+i];
			}
		}
		return basis;
	}
	

}
