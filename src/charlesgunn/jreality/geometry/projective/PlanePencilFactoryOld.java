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

public class PlanePencilFactoryOld extends Abstract1DExtentFactory {

	SceneGraphComponent planePencil;
	IndexedFaceSet ifs;
	double width = .375;
	double[][] fverts = {{1,-width,0,1}, {0,-width,0,1}, {0, width,0,1}, {1,width,0,1}};
	int[][] finds = {{0,1,2,3}};
	double extent = 5;
	String[] labels = {"", "","c",""};
	{
	IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
	ifsf.setVertexCount(4);
	ifsf.setVertexCoordinates(fverts);
	ifsf.setFaceCount(1);
	ifsf.setFaceIndices(finds);
	ifsf.setVertexLabels(labels);
	ifsf.setGenerateFaceNormals(true);
	ifsf.setGenerateEdgesFromFaces(true);
	ifsf.update();
	ifs = ifsf.getIndexedFaceSet();
	}
	
	public void update() {
		//samples = LineUtility.samplesOn1DExtent(samples, offset, numSegs, element0, element1, false);
		if (planePencil == null)	{
			planePencil = new SceneGraphComponent();
		}
		double[] vec = Rn.subtract(null, element1, element0);
		double scale = Rn.euclideanNorm(vec);
		vec = Rn.setToLength(null, vec, 1.0);
		MatrixBuilder.euclidean().translate(element0).rotateFromTo(new double[]{1,0,0,0}, vec).scale(scale).assignTo(planePencil);
		// create fan for plane pencil
		if (planePencil.getChildComponentCount() != numSegs) {
			for (int i = 0; i<numSegs; ++i)	{
				double angle = (times == null) ? (i/((double)numSegs)) : times[i];
				SceneGraphComponent child = getPlaneAtTime(null, angle);
				planePencil.addChild(child);
			}			
		}
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
