package charlesgunn.jreality.geometry;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.event.CameraEvent;
import de.jreality.scene.event.CameraListener;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class FrontWindow {

	static double zval = .99;
	double[][] nearFrustum = {{-1,-1,0},{1,-1,0},{1,1,0}};//,{-1,1,0}};
	IndexedFaceSet geometry;
	{
		 geometry = IndexedFaceSetUtility.constructPolygon(nearFrustum);
	}
	Viewer viewer;
	SceneGraphComponent nearSGC;
	public FrontWindow(Viewer v)	{
		this(v, v.getCameraPath());
	}
	
	public FrontWindow(Viewer v, SceneGraphPath cp) {
		viewer = v;
		cameraPath = cp;
		setup();
	}
	
	public SceneGraphComponent getWindow()	{
		return nearSGC;
	}
	
	public void attachWindow(boolean b)	{
		nearSGC.setVisible(b);
	}
	
	private void setup()	{
		SceneGraphComponent camnode = null;
		if (cameraPath != null)  camnode = cameraPath.getLastComponent();
		else camnode = CameraUtility.getCameraNode(viewer);
		// TODO check that camnode does not already have a child named "frontWindow"
		nearSGC = SceneGraphUtility.createFullSceneGraphComponent("frontWindow");
		nearSGC.setGeometry(geometry);

		Appearance ap = nearSGC.getAppearance();
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		ap.setAttribute(CommonAttributes.SMOOTH_SHADING, false);
//		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute("polygonShader.diffuseColor", Color.white);
//		nearSGC.setPickable( false);
		updateNearRect();
		camnode.addChild(nearSGC);
		Camera cam = CameraUtility.getCamera(viewer);
		cam.addCameraListener(new CameraListener() {

			public void cameraChanged(CameraEvent ev) {
				updateNearRect();
				
			}
			
		});
		
		((Component) viewer.getViewingComponent()).addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				updateNearRect();
			}


		});
	}
	private void updateNearRect() {
		double[] ndcToCam = CameraUtility.getNDCToCamera(viewer);
		double[] zdistT = P3.makeTranslationMatrix(null, new double[]{0,0,-zval}, Pn.EUCLIDEAN);
		double[] cumul = Rn.times(null, ndcToCam, zdistT);
		new Matrix(cumul).assignTo(nearSGC);
//		double[][] camCoords = Rn.matrixTimesVector(null, ndcToCam, nearFrustum);
//		System.err.println(Rn.toString(camCoords));
	}
	public IndexedFaceSet getGeometry() {
		return geometry;
	}
	public void setGeometry(IndexedFaceSet geometry) {
		this.geometry = geometry;
	}

	SceneGraphPath cameraPath = null;
	public SceneGraphPath getCameraPath() {
		return cameraPath;
	}
	public void setCameraPath(SceneGraphPath cameraPath) {
		this.cameraPath = cameraPath;
	}

}
