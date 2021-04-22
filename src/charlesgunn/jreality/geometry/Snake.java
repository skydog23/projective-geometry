/*
 * Created on Mar 8, 2005
 *
 */
package charlesgunn.jreality.geometry;


import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;

public class Snake extends IndexedLineSet {

	int[][] indices;
	int[] vindices;
	int fiber;		// in case it's inlined
	int[] info;		// #beginning point and # of points
	public static Attribute SNAKE_INFO = Attribute.attributeForName("snakeInfo");
	boolean active = false;
	
	public Snake(double[][] p)	{
		super(p.length, 1);
		info = new int[3];
		info[0] = 0; info[1] = p.length; info[2] = -1;
		vindices = new int[p.length];
		vertexAttributes.addWritable(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(p[0].length),p);
		vertexAttributes.addWritable(Attribute.INDICES, StorageModel.INT_ARRAY, vindices);
		update();
	}
	
	public Snake(double[] p, int f)	{
		super(p.length, 1);
		fiber = f;
		info = new int[3];
		info[0] = 0; info[1] = p.length; info[2] = -1;
		vertexAttributes.addWritable(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.inlined(fiber),p);
		update();
	}
	
	int[][] nullindices = {{0}};
	public void update()	{
			int begin = info[0];
			int length = info[1];
			int oldlength = info[2];
			if (length != oldlength) indices = new int[1][length];
			int n = getNumPoints();
			for (int i = 0; i<n; ++i)	{
				vindices[i] = 0;
			}
//			System.err.println("length = "+length);
			for (int i = 0; i<length; ++i)	{
				indices[0][i] = (i+begin)%n;
				vindices[(i+begin)%n] = 1;
			} 
			if (true)	{
				  startWriter();
				  try 
				  {
						edgeAttributes.remove(Attribute.INDICES);
						if (length > 0) edgeAttributes.addWritable(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY, indices);
				  } finally {
				  finishWriter(); 
				  }
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
}
