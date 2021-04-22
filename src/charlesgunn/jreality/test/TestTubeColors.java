/*
 * Author	gunn
 * Created on May 4, 2005
 *
 */
package charlesgunn.jreality.test;

import java.awt.Color;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.SceneGraphUtility;

/**
 * @author gunn
 *
 */
public class TestTubeColors extends LoadableScene {
	SceneGraphComponent icokit;
		/*	
		*/
	    
		public SceneGraphComponent makeWorld()	{
			SceneGraphComponent theWorld = SceneGraphUtility.createFullSceneGraphComponent();
			double[][] verts = new double[5][3];
			int order = 5;
			for (int  i =0; i<order; ++i)	{
				double angle = 2 * Math.PI * i/order;
				verts[i][0] = Math.cos(angle);
				verts[i][1] = Math.sin(angle);
				verts[i][2] = 0.0;
			}
			double[][] colors = {{1,1,1},{1,1,0},{0,1,1},{1,0,1},{0,1,0}};
			//int[][] indices = {{0,1},{1,2},{2,3},{3,4},{4,0}};
			int[][] indices = {{0,1,2,3,4,0}};
			IndexedLineSet ils = new IndexedLineSet(5,1);
			ils.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(verts));
			ils.setEdgeCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY.createReadOnly(indices));
			//ils.setEdgeAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(colors));
			//ils.setEdgeAttributes(Attribute.COLORS, null);
			double[][] edgecolors={{1,0,0},{0,1,0},{1,0,0},{0,1,0}};
			double[][] points = {{1,1,0},{-1,1,0},{-1,-1,0},{1,-1,0}}; //new double[4][3];
			int[][] eindices = {{0,1},{1,2},{2,3},{3,0}};
			int[][] xindices = {{0,1,2,3}};
//			ifs.setVertexAttributes(Attribute.COORDINATES,  StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(points));
//			ifs.setEdgeCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY.createReadOnly(eindices));
//			ifs.setEdgeAttributes(Attribute.COLORS,  StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(edgecolors));
//			ifs.setFaceAttributes(Attribute.INDICES,  StorageModel.INT_ARRAY_ARRAY.createReadOnly(xindices));
//			GeometryUtility.calculateAndSetNormals(ifs);
			IndexedLineSetFactory ifsf = new IndexedLineSetFactory();
			ifsf.setVertexCount(4);
			ifsf.setEdgeCount(1);
//			ifsf.setFaceCount(1);
			ifsf.setVertexCoordinates(points);
//			ifsf.setFaceIndices(xindices);
			ifsf.setEdgeIndices(xindices);
			ifsf.setGenerateVertexLabels(true);
			ifsf.update();
			IndexedLineSet ifs = ifsf.getIndexedLineSet();
			
			DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(theWorld.getAppearance(), false);
		    DefaultTextShader pts = (DefaultTextShader) ((DefaultPointShader)dgs.getPointShader()).getTextShader();
		    pts.setScale(new Double(.01));
		    pts.setDiffuseColor(Color.blue);
		    pts.setOffset(new double[]{.05,0,.021});
//		    pts.setFont()
			theWorld.setGeometry( ils);

			BallAndStickFactory bsf = new BallAndStickFactory(ifs);
			bsf.setBallRadius(.03);
			bsf.setStickRadius(.03);
			bsf.update();
			theWorld.addChild(bsf.getSceneGraphComponent());
			
			return theWorld;
		}
	
		public boolean isEncompass() {
			return true;
		}
			

}
