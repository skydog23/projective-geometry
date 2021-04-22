/*
 * Created on Dec 5, 2009
 *
 */
package de.jreality.geometry;

import java.awt.Color;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.tutorial.util.SimpleTextureFactory;
import de.jreality.tutorial.util.SimpleTextureFactory.TextureType;
import de.jreality.util.SceneGraphUtility;

public class TestCube extends LoadableScene {

	@Override
	public SceneGraphComponent makeWorld() {
		final double[][] vertices = new double[][] { { 0, 0, 0 }, { 1, 0, 0 },
				{ 1, 1, 0 }, { 0, 1, 0 }, { 0, 0, 1 }, { 1, 0, 1 },
				{ 1, 1, 1 }, { 0, 1, 1 } };

		final int[][] indices = new int[][] { { 0, 1, 2, 3 }, { 7, 6, 5, 4 },
				{ 0, 1, 5, 4 }, { 1, 2, 6, 5 }, { 2, 3, 7, 6 }, { 3, 0, 4, 7 } };

		final double[][] TextureCoordinates = new double[][] { { 0,0},
				{ 1,0 }, { 1,1 }, {0,1 }};

		SceneGraphComponent sceneCompParent = SceneGraphUtility.createFullSceneGraphComponent();
		//		sceneCompParent.setGeometry(ifsf.getGeometry());
		sceneCompParent.getAppearance().setAttribute("diffuseColor", Color.white);
		TextureType[] tts = {TextureType.ANTI_DISK, TextureType.GRAPH_PAPER,
				TextureType.WEAVE,TextureType.GRAPH_PAPER, TextureType.DISK, TextureType.GRADIENT
		};
		for (int i = 0; i<6; ++i)	{
			IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
	
			ifsf.setVertexCount(4);
	
			double[][] verts = {vertices[indices[i][0]],
					vertices[indices[i][1]],
					vertices[indices[i][2]],
					vertices[indices[i][3]]
			};
			ifsf.setVertexCoordinates(verts);
	
			ifsf.setVertexTextureCoordinates(TextureCoordinates);
	
			ifsf.setFaceCount(1);
			ifsf.setFaceIndices(new int[][]{{0,1,2,3}});
	
			ifsf.setGenerateVertexNormals(true);
			ifsf.setGenerateEdgesFromFaces(true);
	
			ifsf.update();
			
			IndexedFaceSet face = ifsf.getIndexedFaceSet();
			SceneGraphComponent child = SceneGraphUtility.createFullSceneGraphComponent("child"+i);
			sceneCompParent.addChild(child);
			child.setGeometry(face);
			SimpleTextureFactory stf = new SimpleTextureFactory();
			stf.setType(tts[i]);
			stf.update();
			Texture2D tex = TextureUtility.createTexture(child
					.getAppearance(), "polygonShader", stf.getImageData(), false);
			tex.setTextureMatrix(MatrixBuilder.euclidean().scale(i+2).getMatrix());
		}
		return sceneCompParent;
	}

}
