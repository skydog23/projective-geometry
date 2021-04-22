package de.jreality.geometry;

import java.awt.Color;

import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.SceneGraphUtility;

public class TestVRML2Color {

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
		Appearance ap  = child1.getAppearance();
		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
		DefaultPolygonShader dps = (DefaultPolygonShader) dgs.createPolygonShader("default");
		dps.setDiffuseColor(c1);
		
		ap  = child2.getAppearance();
		dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
		dps = (DefaultPolygonShader) dgs.createPolygonShader("default");
		dps.setDiffuseColor(c2);
		
	  	MatrixBuilder.euclidean().translate(1.2,0,0).assignTo(child2);
	  	
	  	JRViewer.display(world);

	}

}
