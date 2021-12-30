package charlesgunn.jreality.worlds.projective;

import java.awt.Color;

import javax.swing.JMenuBar;

import charlesgunn.jreality.geometry.projective.LinePencilFactory;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class LinePencilPointRangeIllustration extends Assignment {
	SceneGraphComponent  
		theRealWorld,
			pointRangeSGC,
			linePencilSGC,
				centerSGC;

	@Override
	public SceneGraphComponent getContent() {
		theRealWorld = SceneGraphUtility.createFullSceneGraphComponent("theWorld");
		pointRangeSGC = SceneGraphUtility.createFullSceneGraphComponent("pointRange");
		linePencilSGC = SceneGraphUtility.createFullSceneGraphComponent("linePencil");
		centerSGC = SceneGraphUtility.createFullSceneGraphComponent("center");
		Color[] colors = { Color.red, Color.blue};
		LinePencilFactory lpf = new LinePencilFactory();
		lpf.setPoint(new double[]{0,1,0,1});
		lpf.setPlane(new double[]{0,0,1,0});
		lpf.setNumLines(20);
		lpf.setFiniteSphere(true);
		lpf.setSphereRadius(5.0);
		lpf.update();
		linePencilSGC.addChild(lpf.getPencil());
		linePencilSGC.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+"diffuseColor", colors[1]);
		linePencilSGC.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+"diffuseColor", colors[0]);
		PointRangeFactory prf = new PointRangeFactory();
		prf.setElement0(P3.originP3);
		prf.setElement1(new double[]{1,0,0,1});
		prf.setFiniteSphere(true);
		prf.setSphereRadius(5.0);
		prf.setNumberOfSamples(20);
		prf.update();
		pointRangeSGC.setGeometry(prf.getLine());
		double[][] xpoints = LinePencilFactory.intersectionPoints(null, lpf, prf);
		prf.getLine().setVertexAttributes(Attribute.COORDINATES, 
				StorageModel.DOUBLE_ARRAY_ARRAY.createReadOnly(xpoints));
		pointRangeSGC.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+"diffuseColor", colors[0]);
		pointRangeSGC.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+"diffuseColor", colors[1]);
		centerSGC.setGeometry(Primitives.point(lpf.getPoint()));
		linePencilSGC.addChild(centerSGC);
//		centerSGC.getAppearance().setAttribute(CommonAttributes.POINT_SIZE, 5.0);
		theRealWorld.addChildren(pointRangeSGC, linePencilSGC);
		theRealWorld.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		theRealWorld.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW, false);
		theRealWorld.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
		theRealWorld.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, 2.5 * CommonAttributes.LINE_WIDTH_DEFAULT);
		MatrixBuilder.euclidean().translate(0,-.5,0).assignTo(theRealWorld);
		return theRealWorld;
	}

	@Override
	public void display() {
		super.display();
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", new Color(200,200,200));
		MatrixBuilder.euclidean().translate(0,0,2).assignTo(CameraUtility.getCameraNode(viewer));
	}

	public static void main(String[] args) {
		new LinePencilPointRangeIllustration().display();
	}
}
