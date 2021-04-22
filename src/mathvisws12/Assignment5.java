package mathvisws12;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;
import java.awt.Component;

import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.scene.Lights;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.jrworkspace.plugin.Controller;

/**
 * This class demonstrates the difference between the three classical planar geometries
 * (euclidean, elliptic, and hyperbolic).  A translation in the x-direction is calculated
 * and repeated in both directions. The length of the translation is controlled by a slider.
 * To simplify the example, the results are rendered without lighting. The user can click
 * and drag the pattern.  The dragging respects the metric of the pattern.  
 * 
 * @author Charles Gunn
 *
 */
public class Assignment5 extends Assignment {
	private SceneGraphComponent torusSGC = SceneGraphUtility.createFullSceneGraphComponent("torus");
	IndexedFaceSet torus = Primitives.torus(1.0, .5, 50, 50) ;
	private Paint3DTool2 paint3dTool;

	public static void main(String[] args)		{
		Assignment5 theProgram = new Assignment5();
		theProgram.display();
	}

	public SceneGraphComponent getContent() {
		torusSGC.setGeometry(torus);
		torusSGC.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, new Color(255, 255,255));
		torusSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		torusSGC.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		paint3dTool = new Paint3DTool2(torusSGC.getAppearance(), jrviewer);
		torusSGC.addTool(paint3dTool);
		MatrixBuilder.euclidean().rotateX(Math.PI/2).assignTo(torusSGC);
		return torusSGC;
	}
	@Override
	public Component getInspector() {
		// TODO Auto-generated method stub
		return paint3dTool.getInspector();
	}

	@Override
	public void install(Controller con) throws Exception {
		// TODO Auto-generated method stub
		super.install(con);
		Lights lights = con.getPlugin(Lights.class);
		lights.setSunLightIntensity(.6);
	}



}
