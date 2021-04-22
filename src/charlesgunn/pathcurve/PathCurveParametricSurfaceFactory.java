package charlesgunn.pathcurve;

import charlesgunn.math.Complex;
import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.geometry.ParametricSurfaceFactory.Immersion;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;

public class PathCurveParametricSurfaceFactory {
	Complex[] eigenvalues;
	double[] coordinateSystem = Rn.identityMatrix(4);
	double tmin = 0, tmax = 1;
	int numberSteps = 2;
	PathCurveFactory initialCurveFactory = new PathCurveFactory();
	ParametricSurfaceFactory parametricSurfaceFactory = new ParametricSurfaceFactory();
	IndexedFaceSet surface;

	public void update()	{

		Immersion immersion = new Immersion() {
			double[] initialPoint = new double[4];
			public int getDimensionOfAmbientSpace() {
				return 4;
			}
			public void evaluate(double u, double v, double[] xyz, int index) {
				initialCurveFactory.getCurve().evaluate(u, initialPoint, 0);
				double[] m = PathCurveUtility.exponentialMatrixAtTime(eigenvalues,v);
				double[] mm = Rn.conjugateByMatrix(null, m, coordinateSystem);
				Rn.matrixTimesVector(xyz, mm, initialPoint);
//				System.err.println("Got point "+Rn.toString(xyz));
			}
			
			public boolean isImmutable() {
				return true;
			}		
			
		};
		parametricSurfaceFactory.setImmersion(immersion);
		parametricSurfaceFactory.setUMin(initialCurveFactory.getTmin());
		parametricSurfaceFactory.setUMax(initialCurveFactory.getTmax());
		parametricSurfaceFactory.setVMin(tmin);
		parametricSurfaceFactory.setVMax(tmax);
		parametricSurfaceFactory.setULineCount(initialCurveFactory.getNumberSteps());
		parametricSurfaceFactory.setVLineCount(numberSteps);
		parametricSurfaceFactory.setGenerateFaceNormals(true);
		parametricSurfaceFactory.setGenerateEdgesFromFaces(true);
		parametricSurfaceFactory.update();
		surface = parametricSurfaceFactory.getIndexedFaceSet();
	}

	public double[] getCoordinateSystem() {
		return coordinateSystem;
	}

	public void setCoordinateSystem(double[] coordinateSystem) {
		this.coordinateSystem = coordinateSystem;
	}

	public Complex[] getEigenvalues() {
		return eigenvalues;
	}

	public void setEigenvalues(Complex[] eigenvalues) {
		this.eigenvalues = eigenvalues;
	}

	public PathCurveFactory getInitialCurveFactory() {
		return initialCurveFactory;
	}

	public void setInitialCurveFactory(PathCurveFactory initialCurveFactory) {
		this.initialCurveFactory = initialCurveFactory;
	}

	public int getNumberSteps() {
		return numberSteps;
	}

	public void setNumberSteps(int numberSteps) {
		this.numberSteps = numberSteps;
	}

	public ParametricSurfaceFactory getParametricSurfaceFactory() {
		return parametricSurfaceFactory;
	}

	public double getTmax() {
		return tmax;
	}

	public void setTmax(double tmax) {
		this.tmax = tmax;
	}

	public double getTmin() {
		return tmin;
	}

	public void setTmin(double tmin) {
		this.tmin = tmin;
	}

	public IndexedFaceSet getIndexedFaceSet()	{
		return surface;
	}
}
