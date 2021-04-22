package de.jreality.geometry;

import java.awt.Color;

import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.RenderingHintsShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.SceneGraphUtility;

public class ShadedLinesExample {

	public static void main(String[] argv)	{
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent();
		SceneGraphComponent curveSGC = SceneGraphUtility.createFullSceneGraphComponent();
		Appearance ap = curveSGC.getAppearance();
		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
		dgs.setShowPoints(false);
		dgs.setShowLines(true);
		RenderingHintsShader rhs = ShaderUtility.createDefaultRenderingHintsShader(ap, true);
		DefaultLineShader dls = (DefaultLineShader) dgs.createLineShader("default");
		// this polygon shader controls how the tubes are rendered
		DefaultPolygonShader dpls = (DefaultPolygonShader) dls.createPolygonShader("default");
		dls.setTubeDraw(false);
		dls.setLineWidth(4.0);
		dls.setDiffuseColor(Color.white);
		dls.setLineLighting(true);
		IndexedLineSet curve = Primitives.discreteTorusKnot(1.0, .4, 23, 31, 25000);
		TubeUtility.calculateAndSetNormalVectorsForCurve(curve);
		curveSGC.setGeometry(curve);
		world.addChildren(curveSGC); //,PointSetUtility.displayVertexNormals(curve, .05, Pn.EUCLIDEAN));
		JRViewer.display(world);

	}
}
