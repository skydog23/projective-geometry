/*
 * Created on Mar 14, 2007
 *
 */
package charlesgunn.jreality.geometry.projective;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.projgeom.PlueckerLineGeometry;

public class PlanePencilFactory extends Abstract1DExtentFactory {

	SceneGraphComponent planePencil = new SceneGraphComponent("Plane pencil");
	IndexedFaceSet ifs;
	
	double width = .375;
	double[][] fverts = {{1,-width,0,1}, {0,-width,0,1}, {0, width,0,1}, {1,width,0,1}};
	int[][] finds = {{0,1,2,3}};
	double extent = 5;
	double[] line = new double[6];
	boolean autoScale = false;
	double scale = 3.0;
	{
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(4);
		ifsf.setVertexCoordinates(fverts);
		ifsf.setFaceCount(1);
		ifsf.setFaceIndices(finds);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.update();
		ifs = ifsf.getIndexedFaceSet();
	}
	
	public void update() {
		PlueckerLineGeometry.lineFromPlanes(line, element0, element1);
		double[] vec = {line[2], -line[4], line[5]};
		System.err.println("plane pencil: direction vector is "+Rn.toString(vec));
		double length = Rn.euclideanNorm(vec);
		vec = Rn.setToLength(null, vec, 1.0);
		MatrixBuilder.euclidean().
			translate(element0).
			rotateFromTo(new double[]{1,0,0,0}, vec).
			scale(autoScale? length : scale).assignTo(planePencil);
		// create fan for plane pencil
		if (planePencil.getChildComponentCount() != numSegs) {
			for (int i = 0; i<numSegs; ++i)	{
				double angle = (i/((double)numSegs));
				SceneGraphComponent child = getPlaneAtTime(null, angle);
				planePencil.addChild(child);
			}			
		}
	}

	public void setPluckerLine(double[] pc)	{
		double[][] pts = LineUtility.twoPlanesOnLine(null, pc);
//		System.err.println("points = "+Rn.toString(pts));
		setElement0(pts[0]);
		setElement1(pts[1]);
		return;
	}
	
	public double[] getPluckerLine() {
		if (element0 == null || element1 == null)	
			return null;
		return PlueckerLineGeometry.lineFromPlanes(null, element0, element1);
	}
	public SceneGraphComponent getPlaneAtTime(SceneGraphComponent sgc, double t) {
		double angle = t * Math.PI;
		if (sgc == null) {
			sgc = new SceneGraphComponent("child");
			sgc.setGeometry(ifs);
		}
		MatrixBuilder.euclidean().rotateX(angle).assignTo(sgc);
		return sgc;
	}
	
	public SceneGraphComponent getPlanePencil() {
		return planePencil;
	}
	
	
}
