/*
 * Created on Mar 14, 2007
 *
 */
package charlesgunn.jreality.geometry.projective;

import charlesgunn.math.p5.PlueckerLineGeometry;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;

public class RegulusFactory extends Abstract1DExtentFactory {

	double[][] pluckerLines, basisPlanes;
	double[] element2;
	double tolerance = 10E-8;
	boolean finiteSphere = false;
	double sphereRadius = 100;
	boolean isLeitSchar = false;
	transient boolean isDegenerate = false;
	int[] intersectors = new int[2];
	PointRangeFactory[] factories;

	SceneGraphComponent regulus = SceneGraphUtility.createFullSceneGraphComponent("Regulus");
	RegulusFactory leitSchar = null;
	protected double[][] generators;
	static {
		dimension = 6;
	}
	
	protected RegulusFactory() {
		super();
	}
	
	public static RegulusFactory getRegulusFactory() {
		RegulusFactory rf = new RegulusFactory();
		rf.leitSchar = new RegulusFactory();
		rf.leitSchar.isLeitSchar = true;
		return rf;
	}

	
	@Override
	public void setElement1(double[] el1)	{
		super.setElement1(el1);
		basisPlanes = LineUtility.twoPlanesOnLine(null, element1);
	}

	public double[] getElement2() {
		return element2;
	}

	public void setElement2(double[] el) {
		element2 = el;
		if (element2.length == dimension-1)
			element2 = Pn.homogenize(null, element2);
	}

	public double[][] getPluckerLines() {
		return pluckerLines;
	}

	
	public SceneGraphComponent getRegulus() {
		return regulus;
	}
	
	public SceneGraphComponent getLeitSchar() {
		if (isLeitSchar) 
			throw new IllegalStateException("can't attach leitschar to leitschar");
		return getLeitScharFactory().getRegulus();
	}
	
	public RegulusFactory getLeitScharFactory()	{
		if (leitSchar == null) {
			leitSchar = new RegulusFactory();
			leitSchar.isLeitSchar = true;
		}
		return leitSchar;
	}
	
	@Override
	public void update() {
//		System.err.println("Updating regelschar");
		// check if the regelschar is degenerate
		if (!isLeitSchar) {
			checkDegeneracy();			
		}
		computeLines();

		if (pluckerLines.length != regulus.getChildComponentCount())	{
			SceneGraphUtility.removeChildren(regulus);
			factories = new PointRangeFactory[pluckerLines.length];
			for (int i = 0; i<pluckerLines.length; ++i)	{
				SceneGraphComponent child = new SceneGraphComponent();
				regulus.addChild(child);
				factories[i] = new PointRangeFactory();
			}
		}
		for (int i = 0; i<pluckerLines.length; ++i)	{
			if (!PlueckerLineGeometry.isValidLine(pluckerLines[i])) {
				System.err.println("Invalid line");
				continue;
			}
			PointRangeFactory lf =  factories[i];
			lf.setFiniteSphere(finiteSphere);
			lf.setSphereRadius(sphereRadius);
			lf.setPluckerLine(pluckerLines[i]);
			lf.update();
			SceneGraphComponent child = regulus.getChildComponent(i);
			child.setGeometry(lf.getLine());
			// test whether the generated line cuts the three elements
//			double[] plucker = pluckerLines[i];
//			double[] results = new double[3];
//			results[0] = P5.pluckerInnerProduct(plucker, element0);
//			results[1] = P5.pluckerInnerProduct(plucker, element1);
//			results[2] = P5.pluckerInnerProduct(plucker, element2);
//			System.err.println("Results are "+Rn.toString(results));
		}
		if (leitSchar != null)	{
			updateLeitSchar();
		}
	}

	protected void checkDegeneracy() {
		double inpro[] = new double[3];
		isDegenerate = leitSchar.isDegenerate = false;
		generators = new double[][] {element0, element1, element2};
		for (int i = 0; i<3; ++i)	{
			inpro[i] = PlueckerLineGeometry.innerProduct(generators[i], generators[(i+1)%3]);
			if (Math.abs(inpro[i]) < tolerance)	{
				System.err.println("degenerate regulus");
				isDegenerate = leitSchar.isDegenerate = true;
				intersectors[0] = i;
				intersectors[1] = (i+1)%3;
			}
		}
	}

	protected void computeLines() {
		if (pluckerLines == null || pluckerLines.length != numSegs)
			pluckerLines = new double[numSegs][6];
		double[][] pls = LineUtility.samplesOn1DExtent(null, 0, numSegs, basisPlanes[0], basisPlanes[1], false);
		double[] pol1 = PlueckerLineGeometry.lineToSkewMatrix(null, PlueckerLineGeometry.dualizeLine(null, element0));
		double[] pol2 = PlueckerLineGeometry.lineToSkewMatrix(null, PlueckerLineGeometry.dualizeLine(null, element2));
		for (int i = 0; i<numSegs; ++i)	{
			double[] plane = pls[i];
			double[] cut1 = Rn.matrixTimesVector(null, pol1, plane);
			double[] cut2 = Rn.matrixTimesVector(null, pol2, plane);
			pluckerLines[i] = PlueckerLineGeometry.lineFromPoints(null, cut1, cut2);
		}
	}

	protected void updateLeitSchar() {
		// in the degenerate case, we just don't draw a leitschar.
		// theoretically it would be the union of a line bundle and a line field
		if (isLeitSchar)	 
			throw new IllegalStateException("Am leitschar, don't have another one.");
		if (isDegenerate) {
			SceneGraphUtility.removeChildren(leitSchar.regulus);
			return;
		}
		int interval = numSegs/3;
		leitSchar.setElement0(pluckerLines[0]);
		leitSchar.setElement1(pluckerLines[interval]);
		leitSchar.setElement2(pluckerLines[2*interval]);
//			System.err.println("Leitschar 0 = "+Rn.toString(pluckerLines[0]));
//			System.err.println("Leitschar 1 = "+Rn.toString(pluckerLines[interval]));
//			System.err.println("Leitschar 2 = "+Rn.toString(pluckerLines[2*interval]));
		leitSchar.setNumberOfSamples(getNumberOfSamples());
		leitSchar.setFiniteSphere(finiteSphere);
		leitSchar.setSphereRadius(sphereRadius);
		leitSchar.update();			
//		System.err.println("Updating leitschar");
	}

	public double[] getValueAtTime(double t)	{
		double[] cutplane = LineUtility.valueAtTime(t, basisPlanes[0], basisPlanes[1]);
		double[] pol1 = PlueckerLineGeometry.lineToSkewMatrix(null, PlueckerLineGeometry.dualizeLine(null, element0));
		double[] pol2 = PlueckerLineGeometry.lineToSkewMatrix(null, PlueckerLineGeometry.dualizeLine(null, element2));
		double[] cut1 = Rn.matrixTimesVector(null, pol1, cutplane);
		double[] cut2 = Rn.matrixTimesVector(null, pol2, cutplane);
		double[] line = PlueckerLineGeometry.lineFromPoints(null, cut1, cut2);
//		double ff = Rn.euclideanNorm(line);
//		Rn.times(line, 1.0/ff, line);
		return line;
	}

	public boolean isFiniteSphere() {
		return finiteSphere;
	}

	public void setFiniteSphere(boolean finiteSphere) {
		this.finiteSphere = finiteSphere;
		if (!isLeitSchar) leitSchar.finiteSphere = finiteSphere;
	}

	public double getSphereRadius() {
		return sphereRadius;
	}

	public void setSphereRadius(double sphereRadius) {
		this.sphereRadius = sphereRadius;
		if (!isLeitSchar) leitSchar.sphereRadius = sphereRadius;
	}
	
	public double[][] getBasisPlanes() {
		return basisPlanes;
	}

	public void setBasisPlanes(double[][] basisPlanes) {
		this.basisPlanes = basisPlanes;
	}

	public SceneGraphComponent getSurfaceRepresentation(SceneGraphComponent sgc)	{
		if (sgc == null) sgc = SceneGraphUtility.createFullSceneGraphComponent("regulus surface");
		// find range of valid lines
		int start1, end1, start2 = -1, end2=-1;
		int[] validIndices = null;
		if (isFiniteSphere())	{
			int i = 0;
			while (i < factories.length && factories[i].getLine() == null) i++;
			start1 = i;
			while (i < factories.length && factories[i].getLine() != null) i++;
			end1 = i+1;
		} else {
			start1 = 0; end1 = factories.length;
		}
		if (end1 > factories.length) end1 = factories.length;
		validIndices = new int[end1-start1 + end2 - start2];
		for (int i = 0, j = 0; i<factories.length; ++i)	{
				if ((i >= start1 && i<end1) || (i>= start2 && i < end2))
				validIndices[j++] = i;	
		}
		double[][][] quadmesh = new double[validIndices.length][][];
		double d0 = 0.0, d1 = 0.0;
		for (int j = 0; j<validIndices.length; ++j)	{
			int i = validIndices[j];
			quadmesh[i] = factories[i].getVertices();
			if (isFiniteSphere() && i>0)	{
				d0 = Pn.distanceBetween(quadmesh[i][0], quadmesh[i-1][0], Pn.EUCLIDEAN);
				d1 = Pn.distanceBetween(quadmesh[i][1], quadmesh[i-1][0], Pn.EUCLIDEAN);
				if (d0 > d1) { // swap
					double[] tmp = quadmesh[i][0];
					quadmesh[i][0] = quadmesh[i][1];
					quadmesh[i][1] = tmp;
				}
			}
		}
		QuadMeshFactory qmf = new QuadMeshFactory();
		qmf.setULineCount(quadmesh[0].length);
		qmf.setVLineCount(quadmesh.length);
//		qmf.setClosedInUDirection(true);
		qmf.setVertexCoordinates(quadmesh);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateVertexNormals(true);
		qmf.update();
		sgc.setGeometry(qmf.getIndexedFaceSet());
		return sgc;
	}

	public boolean isLinePencil() {
		return isDegenerate;
	}

}
