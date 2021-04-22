package charlesgunn.jreality.geometry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;

public class EvolverUtility {
	
	public static void writeToFile(IndexedFaceSet ifs, String name, String comment)	{
		PrintWriter w = null;
		Hashtable<Edge, Integer> edgeTable = new Hashtable<Edge,Integer>();
		
        try {
            w =new PrintWriter(new FileWriter(new File(name)));
           
       } catch (IOException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
       w.println(comment);
       w.println("vertices");
       double[][] verts = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
       int[] fixed = ifs.getVertexAttributes(Attribute.attributeForName("fixed")).toIntArray(null);
       if (verts[0].length == 4) 
    	   verts = Pn.dehomogenize(verts, verts);
       for (int i = 0; i<verts.length; ++i)	{
    	   w.print((i+1)+"\t"+verts[i][0]+"\t"+verts[i][1]+"\t"+verts[i][2]);
    	   if (fixed[i] != 0) w.print("\tfixed");
    	   w.println();
       }
       w.println();
       w.println("edges");
       int[][] faces = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
       int[][] edges = IndexedFaceSetUtility.edgesFromFaces(faces).toIntArrayArray(null);
       for (int i = 0; i<edges.length; ++i)	{
    	   int lo = Math.min(edges[i][0], edges[i][1])+1;
    	   int hi = Math.max(edges[i][0], edges[i][1])+1;
    	   w.println((i+1)+"\t"+lo+"\t"+hi);
    	   edgeTable.put(new Edge(lo, hi), new Integer(i+1));
       }
       w.println();
       w.println("faces");
       for (int i = 0; i<faces.length; ++i)	{
    	   int n = faces[i].length;
		   w.print((i+1)+"\t");
		   for (int j = 0; j<n; ++j)	{
    		   int v0 = faces[i][j]+1;
    		   int v1 = faces[i][(j+1)%n]+1;
    		   Integer ei = edgeTable.get(new Edge(v0,v1));
    		   if (ei == null) 
    			   throw new IllegalStateException("No edge");
    		   int e = ei.intValue();
    		   w.print( ((v0 < v1) ? e : -e)+"\t");
    	   }
		   w.println();
       }
       w.println("bodies");
       w.print("1\t");
       for (int i = 1; i<=faces.length; ++i)	{
    	   w.print(i+" ");
       }
       w.println();
       w.close();

	}
   	private static final class Edge {
    	  final int l, h;
    	  Edge(int a, int b) {
    	    if(a<=b) { l=a; h=b; }
    	    else     { h=a; l=b; }
    	  }
    	  public boolean equals(Object obj) {
    	    if(this==obj) return true;
    	    try {
    	      final Edge p=(Edge)obj;
    	      return l == p.l && h == p.h;
    	    } catch(ClassCastException ex) {
    	      return false;
    	    }
    	  }
    	  public int hashCode() {
    	    return (l<<16)^h;
    	  }
    	}
   	private static final class Center {
  	  double[] center;
  	  Center(double[] c) {
  		  center = c;
  	  }
  	  public boolean equals(Object obj) {
  	    if(this==obj) return true;
  	    try {
  	      final Center p=(Center)obj;
  	      return Math.abs(Rn.euclideanNorm(Rn.subtract(null, p.center, center))) < 10E-4;
  	    } catch(ClassCastException ex) {
  	      return false;
  	    }
  	  }
   	}
	public static void moveOneFaceThrough(IndexedFaceSet ifs, double d) {
		int[][] ind = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		double[][] verts = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		int[] fixed = new int[verts.length];
		int n = ind[0].length;
		double[][] fv = new double[n][];
		for (int i = 0; i<n; ++i) {
			fv[i] = verts[ind[0][i]];
			fixed[ind[0][i]] = 1;
		}
		double[] center = Rn.average(null, fv);
		double[] moved = Rn.times(null, d, center);
		for (int i = 0; i<n; ++i) Rn.add(verts[ind[0][i]], verts[ind[0][i]], moved);
		ifs.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(verts[0].length).createReadOnly(verts));

		double[] center2 = null;
		int oppositeIndex = -1;
		for (int j=0; j<ind.length; ++j)	{
			n = ind[j].length;
			fv = new double[n][];
			for (int i = 0; i<n; ++i) fv[i] = verts[ind[j][i]];
			center2 = Rn.average(null, fv);
			if (Math.abs(Rn.euclideanNorm(Rn.add(null, center2, center))) < 10E-4) {
				oppositeIndex = j;
				break;
			}
		}
		if (oppositeIndex == -1) 
			throw new IllegalStateException("No opposite face!");
		for (int i = 0; i<ind[oppositeIndex].length; ++i)	
			fixed[ind[oppositeIndex][i]] = 1;
		ifs.setVertexAttributes(Attribute.attributeForName("fixed"),StorageModel.INT_ARRAY.createReadOnly(fixed));
		
	}

}
