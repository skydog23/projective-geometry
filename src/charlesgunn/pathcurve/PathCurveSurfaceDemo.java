/*
 * Created on Nov 11, 2011
 *
 */
package charlesgunn.pathcurve;

import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.Box;

import charlesgunn.jreality.geometry.ClipBox;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImplodePolygonShader;
import de.jreality.shader.TwoSidePolygonShader;
import de.jreality.util.SceneGraphUtility;

public class PathCurveSurfaceDemo extends Assignment {

	double clipSize = 3.0,
			shrink = -.8;
	ImaginaryPathCurveSurface ipcs = new ImaginaryPathCurveSurface();
	SceneGraphComponent sgc = ipcs.getSGC();
	List<SceneGraphPath> list = SceneGraphUtility.getPathsToNamedNodes(sgc, "mesh");
	SceneGraphComponent mesh = list.get(0).getLastComponent();
	@Override
	public SceneGraphComponent getContent() {
		Appearance ap1 = mesh.getAppearance();
		TwoSidePolygonShader tsps = 
			(TwoSidePolygonShader) AttributeEntityUtility.createAttributeEntity(
				TwoSidePolygonShader.class, CommonAttributes.POLYGON_SHADER, ap1, true);
		ImplodePolygonShader ips = 
			(ImplodePolygonShader) AttributeEntityUtility.createAttributeEntity(
				ImplodePolygonShader.class, CommonAttributes.POLYGON_SHADER+".front", ap1, true);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+".front."+CommonAttributes.DIFFUSE_COLOR, new Color(0,204,204));
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+".back."+CommonAttributes.DIFFUSE_COLOR, new Color(204,204,0));
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+".implodeFactor", shrink);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+".vertexShader", "simple");
		sgc.setPickable(false);
		return sgc;
	}

	
	@Override
	public void display() {
		// TODO Auto-generated method stub
		setAddCameraLight(true);
		super.display();
		viewer.getSceneRoot().getAppearance().setAttribute(BACKGROUND_COLOR, new Color(93,14,98));

	}


	@Override
	public Component getInspector() {	
		Box inspectionPanel =  inspector;
		inspector.add(ipcs.getInspector());
		return inspectionPanel;
	}

	public static void main(String[] args) {
		new PathCurveSurfaceDemo().display();
	}
}
