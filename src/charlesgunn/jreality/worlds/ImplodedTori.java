/*
 * Created on May 12, 2004
 *
 */
package charlesgunn.jreality.worlds;
import java.awt.Color;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

/**
 * @author Charles Gunn
 *
 */
public class ImplodedTori extends LoadableScene {


	public SceneGraphComponent makeWorld() {
		SceneGraphComponent root = SceneGraphUtility.createFullSceneGraphComponent("theWorld");
		root.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.RED);
		root.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		double r1 = 1.0, r2 = 0.2, r3 = 0.5;
		IndexedFaceSet torus= Primitives.torus(r1, r2, 40, 60);
		torus.setName("torus");
		SceneGraphComponent mainOne = SceneGraphUtility.createFullSceneGraphComponent("SGC");
		MatrixBuilder.euclidean().rotateX(Math.PI/2).assignTo(mainOne.getTransformation());
		IndexedFaceSetUtility.calculateAndSetNormals(torus);
		mainOne.setGeometry(torus);
		mainOne.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		root.addChild(mainOne);
		for (int i = 0; i< 6; ++i)	{
			double angle = (Math.PI*2.0*i)/6.0;
			torus= Primitives.torus(r3, r3-r2, 20, 30);
			torus.setName("torus"+i);
			IndexedFaceSetUtility.calculateAndSetNormals(torus);
			SceneGraphComponent globeNode = SceneGraphUtility.createFullSceneGraphComponent("SGC"+i);
			MatrixBuilder.euclidean().translate(Math.cos(angle), Math.sin(angle),0).rotateZ(angle).assignTo(globeNode.getTransformation());
			if (i!=0) globeNode.setGeometry(IndexedFaceSetUtility.implode(torus, -.9 + .35 * i));
			else globeNode.setGeometry(IndexedFaceSetUtility.truncate(torus));
			root.addChild(globeNode);
		}
		return root;
	}
	
	public boolean addBackPlane() {
		return false;
	}
	public boolean isEncompass() {
		return true;
	}

}

