/*
 * Created on 21 Apr 2024
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;

import charlesgunn.jreality.geometry.projective.LineBundleFactory;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;

public class LineBundleTest extends Assignment {

	Color color = new Color(204,204, 55);
	@Override
	public SceneGraphComponent getContent() {
		LineBundleFactory lbf = new LineBundleFactory(0);
		lbf.setR1(.05);
		lbf.update();
		
		SceneGraphComponent sgc = lbf.getBundleSGC();
		Appearance ap = sgc.getAppearance();
		ap.setAttribute("pointShader.diffuseColor", color);
		ap.setAttribute("lineShader.diffuseColor", color);
		ap.setAttribute("lineShader.lineWidth", 3.0);
		ap.setAttribute("lineShader.tubeRadius", .0027);
		return lbf.getBundleSGC();
	}

	
	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(255,255,255,0));
	}


	public static void main(String[] args) {
		new LineBundleTest().display();
	}
}
