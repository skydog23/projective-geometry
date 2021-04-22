/*
 * Created on Nov 11, 2011
 *
 */
package charlesgunn.pathcurve;

import java.awt.Color;
import java.util.List;

import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImplodePolygonShader;
import de.jreality.shader.TwoSidePolygonShader;
import de.jreality.util.SceneGraphUtility;

public class PathCurveSurfaceDemo extends Assignment {

	@Override
	public SceneGraphComponent getContent() {
		SceneGraphComponent sgc =  PathCurveUtility.makeImWorld();
		List<SceneGraphPath> list = SceneGraphUtility.getPathsToNamedNodes(sgc, "mesh");
		SceneGraphComponent mesh = list.get(0).getLastComponent();
		Appearance ap1 = mesh.getAppearance();
		TwoSidePolygonShader tsps = 
			(TwoSidePolygonShader) AttributeEntityUtility.createAttributeEntity(
				TwoSidePolygonShader.class, CommonAttributes.POLYGON_SHADER, ap1, true);
		ImplodePolygonShader ips = 
			(ImplodePolygonShader) AttributeEntityUtility.createAttributeEntity(
				ImplodePolygonShader.class, CommonAttributes.POLYGON_SHADER+".front", ap1, true);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+".front."+CommonAttributes.DIFFUSE_COLOR, new Color(0,204,204));
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+".back."+CommonAttributes.DIFFUSE_COLOR, new Color(204,204,0));
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+".implodeFactor", -.8);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+".vertexShader", "simple");
		return sgc;
	}

	public static void main(String[] args) {
		new PathCurveSurfaceDemo().display();
	}
}
