package charlesgunn.jreality.geometry;

import charlesgunn.anim.util.AnimationUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.data.Attribute;

public class InterpolatedILS {

	IndexedLineSet ils;
	IndexedLineSetFactory ilsf;
	double[][] arclength, segmentLengths;
	double[] curvelengths;
	double[][] verts;
	int[][] indices;
	double totalLength = 0;
	public InterpolatedILS(IndexedLineSet ils)	{
		this.ils = ils;
		init();
	}
	
	private  void init()	{
		verts = ils.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		indices = ils.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null);
		int fiber = verts[0].length;
		// right now only euclidean allowed
		int n = verts.length;
		int m = indices.length;
		curvelengths = new double[m];
		arclength = new double[m][];
		segmentLengths = new double[m][];
		totalLength = 0;
		for (int i = 0; i<m; ++i)	{
			n = indices[i].length;
			arclength[i] = new double[indices[i].length];
			segmentLengths[i] = new double[indices[i].length-1];
			arclength[i][0] = totalLength;
			for (int j = 1; j<n; ++j)	{
				double step;
				int k = indices[i][j-1], k2 = indices[i][j];
				if (fiber == 4) 
					step = Pn.distanceBetween(verts[k], verts[k2], Pn.EUCLIDEAN);
				else
					step = Rn.euclideanDistance(verts[k], verts[k2]);
				totalLength += step;
				segmentLengths[i][j-1] = step;	
				arclength[i][j] = totalLength;	
			}
			curvelengths[i] = totalLength;
		}
		System.err.println("total length = "+totalLength);
		double factor = 1.0/totalLength;
		// normalize so the total length of all components is 1
		for (int i = 0; i<m; ++i)	{
			n = indices[i].length;
			curvelengths[i] *= factor;
			for (int j = 0; j<n; ++j)	{
				arclength[i][j] *= factor;
			}
		}
		
		ilsf = new IndexedLineSetFactory();
		ilsf.setVertexCount(verts.length);
		ilsf.setVertexCoordinates(verts);
		ilsf.setEdgeCount(indices.length);
		ilsf.setEdgeIndices(indices);
		ilsf.update();
	}
	
	public void setVerts(double[][] verts) {
		this.verts = verts;
	}


	int whichComp = -1, whichEdge = -1;
	double[] newvert;
	public void setValueAtTime(double t)	{
		getInterpolatedVertex(t);
//		System.err.println("INterpolating dt/edgelength: "+dt+" "+edgelength);
		double[][] newverts = new double[verts.length+1][];
		System.arraycopy(verts, 0, newverts, 0, verts.length);
		newverts[verts.length]= newvert; 
		int[][] newindices = new int[whichComp+1][];
		for (int i = 0;  i<=whichComp; ++i)	{
			if (i < whichComp) newindices[i] = indices[i];
			else {
				newindices[i] = new int[whichEdge+1];
				System.arraycopy(indices[whichComp], 0, newindices[whichComp], 0, whichEdge);
				newindices[whichComp][whichEdge] = verts.length;
			}
		}
		ilsf.setVertexCount(newverts.length);
		ilsf.setVertexCoordinates(newverts);
		ilsf.setEdgeCount(newindices.length);
		ilsf.setEdgeIndices(newindices);
		ilsf.update();
	}

	public double[][] getUnitSpeedParametrizedCurve(int num)	{
		double[][] verts = new double[num][];
		for (int i = 0; i<num; ++i)	{
			double t = i/(num-1.0);
			verts[i] = getInterpolatedVertex(t);
		}
		return verts;
	}
	
	public double[] getInterpolatedVertex(double t) {
		double dt = -1;
		if (t < 0) t = 0;
		if (t > 1) t = 1;
		if (t == 0) {
			t = 0.000001;
		}
		// find the edge in which the fraction t of the total length occurs
		for (int i = 0; i<curvelengths.length; ++i)	{
			if (t <= curvelengths[i]) {
				whichComp = i;
				break;
			}
		}
		if (whichComp < 0 ) 
			throw new IllegalStateException("invalid time");
		
		// in this "edge" find the segment where the fraction t of total length occurs
		int m = indices[whichComp].length;
		for (int i = 0; i<m; ++i)	{
			if (t <= arclength[whichComp][i]) {
				whichEdge = i;
				break;
			}			
		}
		if (whichEdge <= 0) 
			throw new IllegalStateException("invalid time");
			
		dt = 0;
		double edgelength = 1.0;
		if (whichEdge > 0) {
			dt = t - arclength[whichComp][whichEdge-1];
			edgelength = arclength[whichComp][whichEdge] - arclength[whichComp][whichEdge-1];
		}
		int k1 = indices[whichComp][whichEdge-1], k2 = indices[whichComp][whichEdge];
		newvert = AnimationUtility.linearInterpolation(null, dt, 0, edgelength, verts[k1], verts[k2]);
		return newvert.clone();
	}
	
	public IndexedLineSet getInterpolatedILS()	{
		return ilsf.getIndexedLineSet();
	}
	
//	public  IndexedLineSet resample(double maxStep)	{
//		return resample(maxStep, null);
//	}
//	
//	public  IndexedLineSet resample(double maxStep, boolean[][] segSpecs)	{
//		IndexedLineSetFactory resamplingFactory = new IndexedLineSetFactory();
//		// right now only euclidean allowed
//		int n = verts.length;
//		int m = indices.length;
//		int totalNewVerts = 0, newSegsPerEdge[] = new int[m],
//				newSegsPerSeg[][] = new int[m][],
//				newIndices[][] = new int[m][];
//		for (int i = 0; i<m; ++i)	{
//			n = indices[i].length;
//			newSegsPerSeg[i] = new int[n];
//			newSegsPerEdge[i] = 0;
//			for (int j = 0; j<n-1; ++j)	{
//				if (segSpecs == null || segSpecs[i][j]) {
//					double realLength = (segmentLengths[i][j]);
//					int count = (int) (1+(realLength / maxStep));
//					newSegsPerSeg[i][j] = count;
//					newSegsPerEdge[i] += count;			
//					System.err.println(realLength+" inserting "+count+" vertices");
//				} else {
//					newSegsPerSeg[i][j] = 1;
//					newSegsPerEdge[i] += newSegsPerSeg[i][j];										
//				}
//			}
//			totalNewVerts += newSegsPerEdge[i];	
//		}
	public  IndexedLineSet resample(int[][] newSegsPerSeg)	{
		IndexedLineSetFactory resamplingFactory = new IndexedLineSetFactory();
		// right now only euclidean allowed
		int n = verts.length;
		int m = indices.length;
		int totalNewVerts = 0, newSegsPerEdge[] = new int[m],
//				newSegsPerSeg[][] = new int[m][],
				newIndices[][] = new int[m][];
		for (int i = 0; i<m; ++i)	{
			n = indices[i].length;
//			newSegsPerSeg[i] = new int[n];
			newSegsPerEdge[i] = 0;
			for (int j = 0; j<n-1; ++j)	{
				int count = newSegsPerSeg[i][j];
//				System.err.println("numSegsPerSeg = "+count);
				newSegsPerEdge[i] += count;			
			}
			totalNewVerts += newSegsPerEdge[i];	
		}
		double[][] newVerts = new double[totalNewVerts][];
		int vcount = 0;
		for (int i = 0; i<m; ++i)	{
			newIndices[i] = new int[newSegsPerEdge[i]+1];
			n = indices[i].length;
			int indexCount = 0;
			for (int j = 0; j<n-1; ++j)	{
				int k = indices[i][j], k2 = indices[i][j+1];
				int segCount = newSegsPerSeg[i][j];
				for (int w = 0; w<segCount; ++w) {
					double t = (w)/((double) segCount);
					newVerts[vcount] = AnimationUtility.linearInterpolation(t, 0, 1, verts[k], verts[k2]);
					newIndices[i][indexCount++] = vcount;
					vcount++;
				}
				newIndices[i][indexCount] = newIndices[i][0];
			}
		}
		resamplingFactory.setVertexCount(newVerts.length);
		resamplingFactory.setVertexCoordinates(newVerts);
		resamplingFactory.setEdgeCount(newIndices.length);
		resamplingFactory.setEdgeIndices(newIndices);
		resamplingFactory.update();
		
		return resamplingFactory.getIndexedLineSet();
	}
	
	public static void main(String[] args) {
		IndexedLineSet circle = IndexedLineSetUtility.circle(20);
//		circle = Primitives.torus(1.0, .5, 6, 6);//texturedQuadrilateral();
		InterpolatedILS iils = new InterpolatedILS(circle);
//		iils.setValueAtTime(0.5);
//		JRViewer.display(iils.resample(.1)); //iils.getInterpolatedILS());
	}
}
