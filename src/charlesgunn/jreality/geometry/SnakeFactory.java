package charlesgunn.jreality.geometry;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.scene.IndexedLineSet;

public class SnakeFactory {

	double[][] verts;
	int[][] indices;
	IndexedLineSetFactory ilsf, emptyILSF = new IndexedLineSetFactory();
	int length = -1, fiber = -1, currentPointer;
	boolean wasReset = true;
	
	public SnakeFactory(int length, int fiber)	{
		setFiber(fiber);
		setLength(length);
		emptyILSF.setVertexCount(1);
		emptyILSF.update();
	}
	
	public void setFiber(int f)	{
		if (f == fiber) return;
		fiber = f;
		setLength(length);
	}
	
	// TODO: rewrite this to preserve the existing list of points, either truncating or 
	// extending with duplicates.
	public void setLength(int nn)	{
		if (length == nn) return;
		length = nn;
		ilsf = new IndexedLineSetFactory();
		ilsf.setVertexCount(length);
		ilsf.setEdgeCount(1);
		verts = new double[length][fiber];
		indices = new int[1][length];
		currentPointer = 0;
	}
	
	public void addPoint(double[] pp)	{
		System.arraycopy(pp, 0, verts[currentPointer], 0,fiber);
		currentPointer = (currentPointer+1)%length;
	}
	
	public void update()	{
		if (wasReset)	{
			int lastIndex = (currentPointer + length -1) % length;
			for (int i = 1; i<length; ++i)	{
				System.arraycopy(verts[lastIndex], 0, verts[(lastIndex+i)%length], 0,fiber);			
			}
			wasReset = false;
		}
		for (int i = 0; i<length; ++i)	{
			indices[0][i] = (currentPointer+i)%length;
		}
		ilsf.setVertexCoordinates(verts);
		ilsf.setEdgeIndices(indices);
		ilsf.update();
	}

	public IndexedLineSet getSnake()	{
		return ilsf.getIndexedLineSet();
	}

	public void reset() {
		int lastIndex = (currentPointer + length -1) % length;
		for (int i = 0; i<length; ++i)	{
			System.arraycopy(verts[lastIndex], 0, verts[i], 0,fiber);			
		}
		ilsf.setVertexCoordinates(verts);
		ilsf.update();
		wasReset = true;
	}
}
