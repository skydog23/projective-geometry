package charlesgunn.jreality.geometry;

import java.awt.Color;
import java.io.BufferedReader;
import java.net.URL;
import java.util.StringTokenizer;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;

public class TestStarMap extends LoadableScene {

	@Override
	public SceneGraphComponent makeWorld() {
		PointSet stars = starMap(true);
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("starWorld");
		SceneGraphComponent starC = SceneGraphUtility.createFullSceneGraphComponent("stars");
		starC.setGeometry(stars);
//		starC.getTransformation().setMatrix(P3.makeStretchMatrix(null, 10*MarsUtility.marsRadius));
		MatrixBuilder.euclidean().rotateX(-Math.PI/2).assignTo(starC);
		starC.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW, false);
		starC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		starC.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_SIZE, 2.0);
		starC.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.ATTENUATE_POINT_SIZE, false);
		starC.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.white);
		world.addChild(starC);
//		world.addChild(Primitives.wireframeSphere());
		return world;
	}
	
	protected static class StarSize	{
		double size;
		int base, count, color;
	}

	public static PointSet starMap(boolean realSizes)		{
		PointSet stars = null;
		try {
//			File file = new File("../../mars_earth.rsc/"+"stars.illi");
			String ss;
			StringTokenizer st;
			URL url = TestStarMap.class.getResource("stars.illi");
			Input input = Input.getInput(url);//"http://www.math.tu-berlin.de/~gunn/Documents/Models/geomview/stars.illi");
//     File file = input.toFile();
			BufferedReader fin = new BufferedReader(input.getReader());
	        
	        ss = fin.readLine();
	        st = new java.util.StringTokenizer(ss);
	       int numStars = Integer.parseInt(st.nextToken());
	       int numSizes = Integer.parseInt(st.nextToken());
	       int numColors = Integer.parseInt(st.nextToken());
	       double[][] verts = new double[numStars][3];
	       double[][] vcolors = new double[numStars][3];
	       double[] radii = new double[numStars];
	       StarSize[] sizes = new StarSize[numSizes];
	       double[][] colors = new double[numColors][3];
	       
	       for (int i = 0; i<numStars; ++i)	{
	       	ss = fin.readLine();
	      	st = new java.util.StringTokenizer(ss);
	       	for (int j = 0; j<3; ++j)	
	       		verts[i][j] = Double.parseDouble(st.nextToken());
//	       	System.err.println(i+"point="+Rn.toString(verts[i]));
	       }
	       for (int i = 0; i<numSizes; ++i)	{
	       	ss = fin.readLine();
	     	st = new java.util.StringTokenizer(ss);
	       	sizes[i] = new StarSize();	
	      	sizes[i].size = Double.parseDouble(st.nextToken());
	     	sizes[i].base = Integer.parseInt(st.nextToken());
	     	sizes[i].count = Integer.parseInt(st.nextToken());
	     	sizes[i].color = Integer.parseInt(st.nextToken());
	      	}
	       
	       for (int i = 0; i<numColors; ++i)		{
	       	ss = fin.readLine();
	       	st = new java.util.StringTokenizer(ss);
	       	for (int j = 0; j<3; ++j)	
	       		colors[i][j] = Double.parseDouble(st.nextToken());
	       }
	        fin.close();
			stars = new PointSet(numStars);
			int sizeCount = 0;
			for (int i = 0; i<numStars; ++i)	{
				if (i >= sizes[sizeCount].base + sizes[sizeCount].count)	{
					sizeCount++;
				}
				radii[i] = sizes[sizeCount].size;
				System.arraycopy(colors[sizes[sizeCount].color], 0, vcolors[i],0,3);
			}
			stars.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(verts));
			stars.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(vcolors));
			//Attribute size = new Attribute("pointSize", StorageModel.DOUBLE_ARRAY.inlined(1));
			if (realSizes) stars.setVertexAttributes(Attribute.RELATIVE_RADII, StorageModel.DOUBLE_ARRAY.inlined(1).createReadOnly(radii));
			}
	    catch (java.io.IOException ev)	{
	    		ev.printStackTrace();
	    }
		
		return stars;
	}



	public static void main(String[] args) {
		TestStarMap tsm = new TestStarMap();
		JRViewer.display(tsm.makeWorld());
	}

}
