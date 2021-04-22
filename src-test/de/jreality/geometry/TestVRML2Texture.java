package de.jreality.geometry;

import java.awt.Color;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.Texture2D;
import de.jreality.tutorial.util.SimpleTextureFactory;
import de.jreality.util.SceneGraphUtility;

public class TestVRML2Texture {

	public static void main(String[] args) {
		IndexedFaceSet quad = Primitives.texturedQuadrilateral();
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent();
		world.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		world.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		SceneGraphComponent child1 = SceneGraphUtility.createFullSceneGraphComponent("child1");
		SceneGraphComponent child2 = SceneGraphUtility.createFullSceneGraphComponent("child2");
		world.addChildren(child1, child2);
		child1.setGeometry(quad);
		child2.setGeometry(quad);
		Color c1 = new Color(1f, .8f, 0f),  c2 = new Color(.4f, 1f, 0f);
		SimpleTextureFactory sft = new SimpleTextureFactory();
		sft.setType(SimpleTextureFactory.TextureType.GRADIENT);
		sft.setColor(0, c1);
		sft.setColor(1, c2);
		sft.update();
		Appearance ap  = child1.getAppearance();
		Texture2D tex2d = (Texture2D) AttributeEntityUtility
	       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d", ap, true);	
	  	tex2d.setImage(sft.getImageData());
	  	tex2d.setApplyMode(Texture2D.GL_REPLACE);
	  	Matrix foo = new Matrix();
	  	tex2d.setTextureMatrix(foo);
	  	
		ap  = child2.getAppearance();
		tex2d = (Texture2D) AttributeEntityUtility
	       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d", ap, true);	
	  	tex2d.setImage(sft.getImageData());
	  	tex2d.setApplyMode(Texture2D.GL_REPLACE);
	  	foo = new Matrix();
	  	MatrixBuilder.euclidean().translate(0,1,0).scale(1,-1,1).assignTo(foo);
	  	tex2d.setTextureMatrix(foo);
	  	
	  	MatrixBuilder.euclidean().translate(1.2,0,0).assignTo(child2);
	  	
	  	JRViewer.display(world);

	}

}
