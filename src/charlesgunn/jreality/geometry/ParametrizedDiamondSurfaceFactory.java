package charlesgunn.jreality.geometry;

import java.awt.Dimension;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.geometry.ParametricSurfaceFactory.Immersion;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;

public class ParametrizedDiamondSurfaceFactory  {

	double diamondFactor = .5;
	IndexedFaceSetFactory ifsfactory = new IndexedFaceSetFactory();
	ParametricSurfaceFactory parametricSurfaceFactory = null;
	IndexedFaceSet ifs = null;

	public ParametrizedDiamondSurfaceFactory( ParametricSurfaceFactory psf )	{
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
    	int numv = h*(w-1) + w*(h-1);
    	double[][] newv = new double[numv][fiberlength];
    	System.err.println(numv+" new vertices");
    	double[][] oldv = parametricSurfaceFactory.getDomainVertices(null); 
    	//)ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
    	int horizontalEdges = h*(w-1);
    	int verticalEdges = w*(h-1);
    	double[] uv = new double[2];
    	for (int i = 0; i<h; ++i)	{
    		// midpoints of horizontal edges
    		for (int j = 0; j<w-1; ++j)	{
    			double s = ((i+j)%2==0)? diamondFactor : 1-diamondFactor;
    			double t = 1-s;
    			Rn.linearCombination(uv, s, oldv[i*w+j], t, oldv[i*w+j+1]);
    			immersion.evaluate(uv[0], uv[1], newv[i*(w-1)+j], 0 );
    		}
    		// midpoints of vertical edges
        	if (i != (h-1))
        		for (int j = 0; j<w; ++j)	{
        			double s = ((i+j)%2==1)? diamondFactor : 1-diamondFactor;
        			double t = 1-s;
        			Rn.linearCombination(uv, s, oldv[i*w+j], t, oldv[(i+1)*w+j]);
        			immersion.evaluate(uv[0], uv[1], newv[horizontalEdges+i*w+j], 0 );
        		}
    	}
		int numf = numOldFaces + (w-2)*(h-2); // faces are centered on old faces and old vertices
    	if (uclosed) numf += h-2;
    	if (vclosed) numf += w-2;
    	if (uclosed && vclosed) numf++;
    	int[][] newf = new int[numf][];
    	System.err.println(numf+" new faces");
    	int foffset = numOldFaces;
    	// create the faces which are centered on the old faces
       	int imod = vclosed ? (h-1) : h;
    	int jmod = uclosed ? (w-1) : w;
    	for (int i = 0; i<h-1; ++i)	{
    		for (int j = 0; j<(w-1); ++j)	{
    			int[] pp = newf[i*(w-1)+j] = new int[4];
    			pp[0] = i*(w-1)+j;
    			pp[1] = horizontalEdges + i*(w) + j;
    			pp[2] = ((i+1) % imod) *(w-1)+j;
    			pp[3] = horizontalEdges + i*w + (j+1)%jmod;
    		}
    	}
    	// now create the faces centered on the old vertices
    	int ilim = vclosed ? (h-1) : (h-2);
    	int jlim = uclosed ? (w-1) : (w-2);
     	for (int i = 0; i<ilim; ++i)	{
    		for (int j = 0; j<jlim; ++j)	{
    			int[] pp = newf[foffset+i*(jlim)+j] = new int[4];
    			pp[0] = horizontalEdges + i*(w)+ (j+1) % (jmod);
    			pp[1] = ((i+1)%imod)*(w-1) + j;
    			pp[2] = horizontalEdges + (((i+1)%imod)*(w)+ (j+1)%jmod)%verticalEdges;
    			pp[3] = ((i+1)%imod)*(w-1) + (j + 1)%jmod;
    		}
    	}
    	if (uclosed && vclosed)	{
			int[] pp = newf[newf.length - 1] = new int[4];
			pp[0] = 0;
			pp[1] = numv - w;
			pp[2] = w-2;
			pp[3] = horizontalEdges;
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
