package de.jreality.geometry;

import junit.framework.TestCase;

public class TestILSF extends TestCase {

	public void testILSF()	{
		double[][] verts = new double[3][3];
		int[][] ind = new int[1][2];
		IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
		ilsf.setVertexCount(3);
		ilsf.setVertexCoordinates(verts);
		ilsf.setEdgeCount(1);
		ilsf.setEdgeIndices(ind);
		ind[0] = new int[4];
		ilsf.setEdgeIndices(ind);
		ilsf.update();
	}
}
