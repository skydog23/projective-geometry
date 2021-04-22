/*
 * Created on Feb 18, 2020
 *
 */
package charlesgunn.jreality.worlds;

import charlesgunn.jreality.viewer.Assignment;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.discretegroup.groups.ArchimedeanSolids;

public class Dodecahedron extends Assignment {

	@Override
	public SceneGraphComponent getContent() {
		SceneGraphComponent sgc = new SceneGraphComponent();
		sgc.setGeometry(ArchimedeanSolids.archimedeanSolid("5.3"));
		return sgc;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Dodecahedron().display();
	}

}
