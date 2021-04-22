/*
 * Created on Jan 15, 2010
 *
 */
package de.jreality.geometry;

import de.jreality.plugin.JRViewer;
import de.jreality.scene.IndexedFaceSet;

public class DataListTest {
	public static IndexedFaceSet createSurface( int x, int y ) {
		// generate the coordinates for the surface as a 2D array of 3-vectors
		// QuadMeshFactory is the only factory which accepts such a data structure
		// as the argument of its setVertexCoordinates() method!

		    double [][][] coords = new double [x][y][3];
		    for( int i=0; i<x; i++) {
		        double v = -.4 + .8*(i/(y-1.0));
		        for (int j = 0; j<y; ++j) {
		            double u = -.3 + .6*(j/(x-1.0));
		    coords[i][j][0] = 10*(u-v*v);
		    coords[i][j][1]= 10*u*v;
		    coords[i][j][2]= 10*(u*u-4*u*v*v);
		        }
		     }
		      
		    // QuadMeshFactory knows how to build an IndexedFaceSet from a rectangular array
		    // of vectors.  
		    QuadMeshFactory factory = new QuadMeshFactory();
		    factory.setVLineCount(x);      // important: the v-direction is the left-most index
		    factory.setULineCount(y);      // and the u-direction the next-left-most index
		    factory.setClosedInUDirection(false);   
		    factory.setClosedInVDirection(false);   
		    factory.setVertexCoordinates(coords);   
		    factory.setGenerateFaceNormals(true);
		    factory.setGenerateTextureCoordinates(true);
		    factory.setGenerateEdgesFromFaces(true);
		    factory.setEdgeFromQuadMesh(true);   // generate "long" edges: one for each u-, v- parameter curve      
		    factory.update();
//		    x = x-1;
		    // now swap x and y around
		    coords = new double [y][x][3];
		    for( int i=0; i<y; i++) {
		        double v = -.4 + .8*(i/(y-1.0));
		        for (int j = 0; j<x; ++j)   {
		            double u = -.3 + .6*(j/(x-1.0));
		    coords[i][j][0] = 10*(u-v*v);
		    coords[i][j][1]= 10*u*v;
		    coords[i][j][2]= 10*(u*u-4*u*v*v);
		        }
		     }
		     factory.setVLineCount(y);      // important: the v-direction is the left-most index
		     factory.setULineCount(x);      // and the u-direction the next-left-most index
		     factory.setClosedInUDirection(false);   
		     factory.setClosedInVDirection(false); 
		     factory.setVertexCoordinates(coords);   
		     factory.setGenerateFaceNormals(true);
		     factory.setGenerateTextureCoordinates(true);
		     factory.setGenerateEdgesFromFaces(true);
		     factory.setEdgeFromQuadMesh(true);   // generate "long" edges: one for each u-, v- parameter curve      
		     factory.update(); // <--- CRASH ON THIS LINE
		     return factory.getIndexedFaceSet();
		}
	
	public static void main(String[] argc)	{
		DataListTest dlt = new DataListTest();
		JRViewer.display(dlt.createSurface(10, 15));
	}
}
