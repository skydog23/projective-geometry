/*
 * Created on Jun 2, 2010
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;

import charlesgunn.jreality.geometry.projective.PlanePencilFactoryOld;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.CameraUtility;

public class DualityPlanePencilPointRange extends AbstractDualGeometry {

	{
		dim = 3;
	}
	
	@Override
	public SceneGraphComponent getContent() {
				
		super.getContent();
		MatrixBuilder.euclidean().translate(0, 0, -4).assignTo(world);
		world.getAppearance().setAttribute("showLabels", false);
		return world;
	}

	@Override
	protected void constructPencil(SceneGraphComponent sgc) {
		super.constructPencil(sgc);
		PlanePencilFactoryOld ppf = new PlanePencilFactoryOld();
		ppf.setNumberOfSamples(nEls);
		ppf.setElement0(verts[0]);
		ppf.setElement1(verts[1]);
		ppf.update();
		sgc.addChild(ppf.getPlanePencil());
	}

	

	@Override
	public void display() {
		super.display();
//		SceneGraphUtility.removeLights(viewer);
		SceneGraphComponent camSGC = CameraUtility.getCameraNode(viewer);
		SceneGraphComponent l1sgc = new SceneGraphComponent();
		SceneGraphComponent l2sgc =new SceneGraphComponent();
		SceneGraphComponent l3sgc =new SceneGraphComponent();
		camSGC.addChildren(l1sgc, l2sgc);
		DirectionalLight dl = new DirectionalLight();
		dl.setIntensity(.75);
		dl.setColor(new Color(225, 225, 255));
		MatrixBuilder.euclidean().rotateY(.1-Math.PI/2).assignTo(l1sgc);
		l1sgc.setLight(dl);
		DirectionalLight d2 = new DirectionalLight();
		d2.setIntensity(.75);
		d2.setColor(new Color(255,225,225));
		MatrixBuilder.euclidean().rotateY(Math.PI/4).assignTo(l2sgc);
		l2sgc.setLight(d2);
		DirectionalLight d3 = new DirectionalLight();
		d2.setIntensity(.75);
		d2.setColor(new Color(225,255,225));
		MatrixBuilder.euclidean().rotateX(-Math.PI/2).assignTo(l3sgc);
		l3sgc.setLight(d3);
	}

	public static void main(String[] args) {
		new DualityPlanePencilPointRange().display();
	}
}
