package charlesgunn.jreality.geometry;

import java.awt.Dimension;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.geometry.ParametricSurfaceFactory.Immersion;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;

public class ParametrizedTriangleMeshSurfaceFactory  {

	double diamondFactor = .5;
	IndexedFaceSetFactory ifsfactory = new IndexedFaceSetFactory();
	ParametricSurfaceFactory parametricSurfaceFactory = null;
	IndexedFaceSet ifs = null;

	public ParametrizedTriangleMeshSurfaceFactory( ParametricSurfaceFactory psf )	{
		super();
		parametricSurfaceFactory = psf;
	}
	
	public void update()	{
		parametricSurfaceFactory.update();
		IndexedFaceSet qm = parametricSurfaceFactory.getIndexedFaceSet();
	    Object obj = qm.getGeometryAttributes(GeometryUtility.QUAD_MESH_SHAPE);
	    if (obj == null || !(obj instanceof Dimension)) 
	    	throw new IllegalArgumentException("Must be a quad mesh");
	   	Dimension dim = (Dimension) obj;
	   	Immersion immersion = parametricSurfaceFactory.getImmersion();
	    int fiberlength = immersion.getDimensionOfAmbientSpace();
	    boolean uclosed = parametricSurfaceFactory.isClosedInUDirection();
	    boolean vclosed = parametricSurfaceFactory.isClosedInVDirection();
     	int w = dim.width;
    	int h = dim.height;
    	int numOldFaces = qm.getNumFaces();
    	if ( (w-1)*(h-1) != numOldFaces) 
    		throw new IllegalStateException("Bad face count");
    	double[][] domain = parametricSurfaceFactory.getDomainVertices(null, true); 
    	double[][] newv = new double[domain.length][fiberlength];
    	//)ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		int numf = 2 * numOldFaces; // faces are centered on old faces and old vertices
    	int[][] newf = new int[numf][3];
    	System.err.println(numf+" new faces");
    	int[][] oldf = qm.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
    	for (int i = 0; i < h; ++i)	{
    		for (int j = 0; j<w; ++j)	{
    			int ii = j+i*w;
    			immersion.evaluate(domain[ii][0], domain[ii][1], newv[ii], 0);
    		}
    	}
    	int[][][] indices = {{{0,1,w},{1,w+1,w}}, {{0,1,w+1},{w+1,w,0}}};
       	for (int i = 0, fc = 0; i < h-1; ++i)	{
       		int which = i % 2;
    		for (int j = 0; j<w-1; ++j, fc +=2)	{
    			int v0 = j+i*w;
    			for (int k = 0; k<3; ++k)	{
           			newf[fc][k] = v0+indices[which][0][k];
        			newf[fc+1][k] = v0+indices[which][1][k];				    				
    			}
     		}
    	}
     	ifsfactory.setVertexCount(newv.length);
    	ifsfactory.setVertexCoordinates(newv);
    	ifsfactory.setFaceCount(newf.length);
    	ifsfactory.setFaceIndices(newf);
    	ifsfactory.setGenerateEdgesFromFaces(true);
    	ifsfactory.setGenerateFaceNormals(true);
    	
		ifsfactory.update();
		ifs = ifsfactory.getIndexedFaceSet();
	}

	public double getDiamondFactor() {
		return diamondFactor;
	}

	public void setDiamondFactor(double diamondFactor) {
		this.diamondFactor = diamondFactor;
	}

	public ParametricSurfaceFactory getParametricSurfaceFactory() {
		return parametricSurfaceFactory;
	}

	public void setParametricSurfaceFactory(
			ParametricSurfaceFactory parametricSurfaceFactory) {
		this.parametricSurfaceFactory = parametricSurfaceFactory;
	}

	public IndexedFaceSet getIndexedFaceSet() {
		return ifs;
	}


	public void setIndexedFaceSet(IndexedFaceSet indexedFaceSet) {
		this.ifs = indexedFaceSet;
	}

}
