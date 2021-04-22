package charlesgunn.jreality.worlds;

import java.awt.Color;

import javax.swing.JMenuBar;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;

public class MatheonLogo extends LoadableScene {

	@Override
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent matheonLogo = new SceneGraphComponent();
		MatrixBuilder.euclidean().scale(1.25).rotateFromTo(new double[]{1.6180, 1, 0}, new double[]{0,1,0}).assignTo(matheonLogo);
		IndexedFaceSet slab = Primitives.box(1.6180, 1, .1, false, Pn.EUCLIDEAN);
		matheonLogo.setAppearance(new Appearance());
		matheonLogo.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		Color[] colors = {new Color(0, 93, 201), new Color(135, 191, 240), new Color(238, 249, 255)};
		for (int i = 0; i<3; ++i)	{
			SceneGraphComponent sgc = new SceneGraphComponent();
			sgc.setGeometry(slab);
			MatrixBuilder.euclidean().rotate(i*2*Math.PI/3, 1, 1, 1).assignTo(sgc);
			sgc.setAppearance(new Appearance());
			sgc.getAppearance().setAttribute("polygonShader.diffuseColor", colors[i]);
			matheonLogo.addChild(sgc);
		}
		SceneGraphComponent matheonLogo2 = new SceneGraphComponent();
		matheonLogo2.addChild(matheonLogo);
		return matheonLogo2;
	}

	@Override
	public void customize(JMenuBar menuBar, Viewer viewer) {
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColors", Appearance.INHERITED);
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", new Color(0,0,0,0));
	}

}
