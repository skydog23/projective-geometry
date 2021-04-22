 /*
  * Created on May 12, 2004
  *
  */
 package charlesgunn.jreality.worlds;
 import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import charlesgunn.jreality.geometry.BezierPatchMeshTubeFactory;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.BezierPatchMesh;
import de.jreality.geometry.FrameFieldType;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.util.CameraUtility;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;

 /**
  * @author Charles Gunn
  *
  */
 public class MakeWeave extends LoadableScene {

 	static double x = 1;
 	static double[][] circle =  {{x, 0, 0, 1},	 {x, 1,0, 1}, {0,2, 0, 2}, {-x, 1,0, 1}, {-x, 0, 0, 1},{-x, -1,0, 1},{0,-2, 0, 2},{x, -1,0, 1},{x, 0, 0, 1}};	
     static double yval = .5;
 	static double[][] form = {{0,yval,0.0},{1,yval,1.5},{2,yval,0}};
  	SceneGraphComponent geometryHome = null;
 	IndexedFaceSet qmpatch = null;
 	QuadMeshFactory qmf;
 	int refineLevel = 3;
 	double radius = 0.4;
	private BezierPatchMeshTubeFactory bpmtf;
 	public SceneGraphComponent makeWorld() {
  	   SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
  	   SceneGraphComponent copies5 = SceneGraphUtility.createFullSceneGraphComponent("copies5");
	   SceneGraphComponent order4 = SceneGraphUtility.createFullSceneGraphComponent("order4");
	   SceneGraphComponent order2 = SceneGraphUtility.createFullSceneGraphComponent("order20");
	   SceneGraphComponent order20 = SceneGraphUtility.createFullSceneGraphComponent("order20");
	   SceneGraphComponent order21 = SceneGraphUtility.createFullSceneGraphComponent("order21");
	   geometryHome = SceneGraphUtility.createFullSceneGraphComponent("geometryHome ");
	   MatrixBuilder.euclidean().translate(-.5,0,-.75).assignTo(geometryHome.getTransformation());
	   order20.addChild(geometryHome);
 	   order21.addChild(geometryHome);
	   MatrixBuilder.euclidean().rotateY(Math.PI).assignTo(order21.getTransformation());
 	   order2.addChild(order20);
	   order2.addChild(order21);
	   for (int i = 0; i<4; ++i)	{
	 	   SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("order4"+i);
		   MatrixBuilder.euclidean().rotateZ(i*Math.PI/2).assignTo(sgc.getTransformation());
		   sgc.addChild(order2);
		   order4.addChild(sgc);
	   }
	   double[][] tlates = {{0,0,0},{2,0,0},{0,2,0},{-2,0,0},{0,-2,0}};
	   for (int j = 0; j<5; ++j)	{
	 	   SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("tlatexy"+j);
		   MatrixBuilder.euclidean().translate(tlates[j]).assignTo(sgc.getTransformation());
		   sgc.addChild(order4);
		   copies5.addChild(sgc);
	   }
	   world.addChild(copies5);

	   Appearance ap1 = world.getAppearance();
	   Texture2D tex2d = null;
	   tex2d = (Texture2D) AttributeEntityUtility
	       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d", ap1, true);		
	   try {
	      ImageData id = ImageData.load(Input.getInput("textures/weaveRGBABright.png"));
	      tex2d.setImage(id);
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
		tex2d.setTextureMatrix(MatrixBuilder.euclidean().rotate(Math.PI/4.0,0,0,1).scale(8,8,1).getMatrix());
   
  	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.WHITE);
 	   ap1.setAttribute(CommonAttributes.SPECULAR_EXPONENT, 100.0);
 	   ap1.setAttribute(CommonAttributes.SPECULAR_COEFFICIENT, 0.1);
 	   ap1.setAttribute(CommonAttributes.DIFFUSE_COEFFICIENT, 1.0);
  	   ap1.setAttribute(CommonAttributes.EDGE_DRAW, false);
 	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH,1.0);

 	   bpmtf = new BezierPatchMeshTubeFactory(form);
  	   bpmtf.setCrossSection(circle);
 	   bpmtf.setFrameFieldType(FrameFieldType.PARALLEL);
 	   bpmtf.setMetric(Pn.EUCLIDEAN);
 	   bpmtf.setClosed(false);
 	   bpmtf.setRadius(radius);
	   bpmtf.update();
 	   //tubePoints = bpmtf.getTube();
 	   BezierPatchMesh bpm = bpmtf.getTube(); //new BezierPatchMesh(2, 3, tubePoints);
 	   	for (int j = 1; j<= refineLevel; ++j)	bpm.refine();
 		qmf =  BezierPatchMesh.representBezierPatchMeshAsQuadMeshFactory(qmf, bpm, 0);
 		qmpatch = qmf.getIndexedFaceSet();
// 		GeometryUtility.calculateAndSetTextureCoordinates(qmpatch);
  		geometryHome.setGeometry(qmpatch);	   
  	  return world;
 	}
 	
 	public void setConfiguration(ConfigurationAttributes config) {
 	}

 	public int getMetric() {
 		return Pn.EUCLIDEAN;
 	}

 	public boolean addBackPlane() {
 		return false;
 	}
 	public boolean isEncompass() {
 		return true;
 	}

	public boolean hasInspector() {return true; }
	public Component getInspector(Viewer viewer) {
		Box container = Box.createVerticalBox();
		TextSlider ballCount = new TextSlider.Double("radius", SwingConstants.HORIZONTAL, 0.0, 1.0, 0.5);
	    ballCount.addActionListener( new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
		            double value = ((TextSlider) e.getSource()).getValue().doubleValue();
		                setRadius(value);
		    }	       	
	       });
	    //ballCount.setAlignmentX(1.0f);
		container.add(ballCount);
//		return super.getInspector();
		return container;
	}

	/**
	 * @param d
	 */
	protected void setRadius(double d) {
			radius = d;
	 	   	bpmtf.setRadius(radius);
	 	   	bpmtf.update();
	 	    BezierPatchMesh bpm = bpmtf.getTube(); 
	 	    for (int j = 1; j<= refineLevel; ++j)	bpm.refine();
	 		qmf =  BezierPatchMesh.representBezierPatchMeshAsQuadMeshFactory(qmf, bpm, 0);
	 		qmpatch = qmf.getIndexedFaceSet();
 		   	geometryHome.setGeometry(qmpatch);
	}
	
	public void customize(JMenuBar menuBar, PluginSceneLoader psl) {
		Viewer viewer = psl.getViewer();
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor",java.awt.Color.BLACK);
		CameraUtility.getCamera(viewer).setPerspective(false);
	}
 }
