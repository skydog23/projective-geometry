/*
 * Created on Mar 8, 2005
 *
 */
package charlesgunn.jreality.geometry;


import java.awt.Dimension;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.Pn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;

public class SnakeMesh extends IndexedFaceSet {

	int[][] indices;
	int[] vindices;
	int fiber;		// in case it's inlined
	int numCurves;
	int curveSize;
	int[] info;		// #beginning point and # of points
//	public static Attribute SNAKE_INFO = Attribute.attributeForName("snakeInfo");
	boolean active = false;
	int metric = Pn.EUCLIDEAN;
	double[][] tc;
	public SnakeMesh(double[][] p, int n, int c, int f)	{
		if (p.length != (n*c))
			throw new IllegalArgumentException("wrong dimension");
		numCurves = n;
		curveSize = c;
		fiber = f;
		info = new int[3];
		info[0] = 0; info[1] = p.length; info[2] = -1;
		vindices = new int[p.length];
		setNumPoints(p.length);
		tc = new double[p.length][2];
		vertexAttributes.addWritable(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(fiber),p);
		vertexAttributes.addWritable(Attribute.TEXTURE_COORDINATES, StorageModel.DOUBLE_ARRAY.array(2),tc);
		vertexAttributes.addWritable(Attribute.INDICES, StorageModel.INT_ARRAY, vindices);
		setGeometryAttributes(GeometryUtility.QUAD_MESH_SHAPE, new Dimension(n, c));
		update();
	}
	
	
	int[][] nullindices = {{0}};
	public void update()	{
			int begin = info[0];
			int length = info[1];
			int oldlength = info[2];
			if (length > 1) {
				if (length != oldlength) {
					indices = new int[(length-1)*(curveSize-1)][4];
					setNumFaces((length-1)*(curveSize-1));
				}
				int n = getNumPoints();
				for (int i = 0; i<n; ++i)	{
					vindices[i] = 0;
				}
//				System.err.println("length = "+length);
				int[] offsets = {0, 1, curveSize+1, curveSize};
				for (int i = 0; i<length-1; ++i)	{
					for (int j = 0; j<curveSize-1; ++j)	{
						for (int k = 0; k<4; ++k)	{
							int k2 = (((i+begin)%numCurves)*curveSize+j+offsets[k])%n;
							indices[i*(curveSize-1)+j][k] = k2;
							vindices[k2] = 1;	
							tc[k2][0] = j/(curveSize-1.0);
							tc[k2][1] = i/(length-1.0);
						}
					}
				} 				
			}
				  startWriter();
				  try 
				  {
						faceAttributes.remove(Attribute.INDICES);
						if (length > 0) {
							faceAttributes.addWritable(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY, indices);
							IndexedFaceSetUtility.calculateAndSetFaceNormals(this, metric);
//							IndexedFaceSetUtility.calculateAndSetVertexNormals(this);
						} else setNumFaces(0);
				  } finally {
				  finishWriter(); 
				  }
			info[2] = length;
			fireChange();
	}
	
	public void fireChange()	{
		fireGeometryChanged(null, null, null, null);
	}
	public int[] getInfo() {
		return info;
	}

	public int getMetric() {
		return metric;
	}

	public void setMetric(int metric) {
		this.metric = metric;
	}
}
