/*
 * Created on Sep 9, 2008
 *
 */
package de.jreality.geometry;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import de.jreality.jogl.JOGLViewer;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.ClickWheelCameraZoomTool;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.FlyTool;
import de.jreality.tools.RotateTool;
import de.jreality.toolsystem.ToolSystem;

public class HyperbolicExample {
  public static void main(String[] args) {
    SceneGraphComponent rootNode = new SceneGraphComponent();
    SceneGraphComponent cameraNode = new SceneGraphComponent();
    SceneGraphComponent geometryNode = new SceneGraphComponent();
    SceneGraphComponent lightNode = new SceneGraphComponent();
    
	Appearance rootApp = new Appearance();
	rootApp.setAttribute(CommonAttributes.METRIC, Pn.HYPERBOLIC);
    rootNode.setAppearance(rootApp);
    // convenient to be able to change fov using click wheel on mouse
    rootNode.addTool(new ClickWheelCameraZoomTool());
    
    rootNode.addChild(geometryNode);
    rootNode.addChild(cameraNode);
    cameraNode.addChild(lightNode);
    
    IndexedLineSet torus = Primitives.discreteTorusKnot(.55, .1, 2, 9, 250);
    geometryNode.setGeometry(torus);
    
    Camera camera = new Camera();
    // set near and far for hyperbolic space
    camera.setNear(.01);
    camera.setFar(2.0);
    cameraNode.setCamera(camera);
    // fly tool needs to be damped down a bit
    FlyTool flyTool = new FlyTool();
    flyTool.setGain(0.2);
	cameraNode.addTool(flyTool);
    MatrixBuilder.hyperbolic().translate(0, 0, .9).assignTo(cameraNode);
    
    // For real shading, this needs to be replaced with a genuine hyperbolic light
    Light dl=new DirectionalLight();
    lightNode.setLight(dl);
    
    RotateTool rotateTool = new RotateTool();
    DraggingTool dragTool = new DraggingTool();
    rotateTool.setFixOrigin(true);
    geometryNode.addTool(rotateTool);
    geometryNode.addTool(dragTool);

    SceneGraphPath camPath = new SceneGraphPath();
    camPath.push(rootNode);
    camPath.push(cameraNode);
    camPath.push(camera);
    
    JOGLViewer viewer = new JOGLViewer();
    viewer.setSceneRoot(rootNode);
    viewer.setCameraPath(camPath);
    ToolSystem toolSystem = ToolSystem.toolSystemForViewer(viewer);
    toolSystem.initializeSceneTools();
    
    JFrame frame = new JFrame();
    frame.setVisible(true);
    frame.setSize(640, 480);
    frame.getContentPane().add((Component) viewer.getViewingComponent());
    frame.validate();
    System.out.println(viewer.getViewingComponentSize());
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent arg0) {
        System.exit(0);
      }
    });
    
    while (true) {
      viewer.render();
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
