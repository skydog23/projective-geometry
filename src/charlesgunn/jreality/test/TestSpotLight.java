/*
 * Author	gunn
 * Created on Oct 12, 2005
 *
 */
package charlesgunn.jreality.test;

import java.awt.Color;
import java.util.List;

import javax.swing.JMenuBar;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.BezierPatchMesh;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.SelectionManagerImpl;
import de.jreality.util.SceneGraphUtility;



/**
 * @author gunn
 *
 */
public class TestSpotLight extends LoadableScene {
	Viewer viewer;

	static double[][][] square = new double[2][2][3];
	public SceneGraphComponent makeWorld() {
		theWorld = SceneGraphUtility.createFullSceneGraphComponent("world");
		theShape = SceneGraphUtility.createFullSceneGraphComponent("shape");
		theShape.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
		theShape.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);

		for (int i =0; i<2; ++i)	
			for (int j = 0; j<2; ++j)	{
				square[i][j][0] = 9*(i-.5);
				square[i][j][1] = 9*(j-.5);
			}
		BezierPatchMesh bpm = new BezierPatchMesh(1, 1, square);
	   	for (int j =0; j<= 7; ++j)	bpm.refine();
	   	IndexedFaceSet qmpatch = BezierPatchMesh.representBezierPatchMeshAsQuadMesh(bpm);	 
	   	theShape.setGeometry(qmpatch);
	
	   	theWorld.addChild(theShape);
	   	theWorld.addChild(makeSpotLight());
	   	return theWorld;
	}
	
 	public boolean addBackPlane()	{return false;}
 	
	public boolean isEncompass() {
		return true;
	}
	SceneGraphComponent lightIcon;
	private SceneGraphComponent spot;
	private SceneGraphComponent theShape;
	private SceneGraphComponent theWorld;
 	public SceneGraphComponent makeSpotLight()	{
 		spot = SceneGraphUtility.createFullSceneGraphComponent("Spot");
		SpotLight sl = new SpotLight();
 		//PointLight sl = new PointLight();
 		//DirectionalLight sl = new DirectionalLight();
 		sl.setColor(Color.YELLOW);
		sl.setConeAngle(Math.PI/6.0 );
//		sl.setConeDeltaAngle(Math.PI/6.0);
 		sl.setDistribution(1.0);
 		sl.setIntensity(1.0);
  		sl.setFalloff(1, 0, 0);
 		spot.setLight(sl);
 		MatrixBuilder.euclidean().translate(0,0,2).rotateX(Math.PI).assignTo(spot);
 		lightIcon = SceneGraphUtility.createFullSceneGraphComponent("Light icon");
 		lightIcon.setGeometry(Primitives.cylinder(10));
 		lightIcon.getTransformation().setMatrix(P3.makeStretchMatrix(null, new double[] {.2, .2, .4}));
		lightIcon.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
 		spot.addChild(lightIcon);
 		spot.getAppearance().setAttribute("lighting", false);
 		return spot;
 	}
 	
 	public SceneGraphComponent makeLights() { return new SceneGraphComponent(); }

	public void customize(JMenuBar menuBar, Viewer viewer) {
		List l = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), spot);
		SceneGraphPath sgp = (SceneGraphPath) l.get(0);
//		SceneGraphPath sgp = SceneGraphUtility.findFirstPathBetween(viewer.getSceneRoot(), spot);
		SelectionManagerImpl.selectionManagerForViewer(viewer).setSelectionPath(sgp);
		SelectionManagerImpl.selectionManagerForViewer(viewer).addSelection(sgp);
//		sgp = SceneGraphUtility.findFirstPathBetween(viewer.getSceneRoot(), theShape);
		l = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), theShape);
		sgp = (SceneGraphPath) l.get(0);
		SelectionManagerImpl.selectionManagerForViewer(viewer).addSelection(sgp);
		l = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), theWorld);
		sgp = (SceneGraphPath) l.get(0);
//		sgp = SceneGraphUtility.findFirstPathBetween(viewer.getSceneRoot(), theWorld);
		SelectionManagerImpl.selectionManagerForViewer(viewer).addSelection(sgp);
	}
 }
