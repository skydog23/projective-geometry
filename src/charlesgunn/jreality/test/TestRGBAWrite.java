/*
 * Created on Jan 29, 2004
 *
 */
package charlesgunn.jreality.test;

import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;

import java.awt.Color;

import javax.swing.JMenuBar;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;


/**
 * @author Charles Gunn
 *
 */
public class TestRGBAWrite extends LoadableScene {
		public SceneGraphComponent makeWorld()	{
			SceneGraphComponent theWorld = SceneGraphUtility.createFullSceneGraphComponent("world");
			theWorld.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
			theWorld.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, .5);
			theWorld.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
			Color[] colors = {Color.red, Color.green, Color.blue};
			IndexedFaceSet face = Primitives.regularPolygon(4, .5);
			for (int i = 0; i<3; ++i)	{
				SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("sgc"+i);
				sgc.getAppearance().setAttribute("polygonShader.diffuseColor", colors[i]);
				sgc.setGeometry(face);
				MatrixBuilder.euclidean().translate(2*i,0,0).assignTo(sgc);
				theWorld.addChild(sgc);
			}
			return theWorld;
		}
	
		public boolean isEncompass() {
			return true;
		}

		public void customize(JMenuBar menuBar, Viewer viewer) {
			viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
			viewer.getSceneRoot().getAppearance().setAttribute(BACKGROUND_COLOR, new Color(200, 200,0,255));
		}
}
