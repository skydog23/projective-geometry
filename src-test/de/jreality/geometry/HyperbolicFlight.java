package de.jreality.geometry;

import java.awt.Color;
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
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.CommonAttributes;
import de.jreality.toolsystem.ToolSystem;

public class HyperbolicFlight {
  public static void main(String[] args) {
    SceneGraphComponent rootNode = new SceneGraphComponent();
    SceneGraphComponent cameraNode = new SceneGraphComponent();
    SceneGraphComponent lightNode = new SceneGraphComponent();
   
    //The component that causes the trouble.
    // Rodion: The problem was that you overwrote the transformation of the returned SceneGraphComponent
    // If you look at the source code for Primitives.sphere() you'll see that that t'form is already used
    // to scale the sphere.
    SceneGraphComponent earth = Primitives.sphere(.1,new double[] {.1,.2,.1},Pn.HYPERBOLIC);
    // add a set of translated copies of the sphere to the root
    for (int i = 0; i<10; ++i)	{
    	   SceneGraphComponent earthHolder = new SceneGraphComponent("Holder"+i);
    	    earthHolder.addChild(earth);
     	    MatrixBuilder.init(null,Pn.HYPERBOLIC).translate(i/10.0,0,0).assignTo(earthHolder);
    	    rootNode.addChild(earthHolder);
    }
     
	Appearance rootApp = new Appearance();
	rootApp.setAttribute(CommonAttributes.METRIC, Pn.HYPERBOLIC);
	rootApp.setAttribute(CommonAttributes.BACKGROUND_COLOR ,Color.WHITE);
    rootNode.setAppearance(rootApp);
    
    rootNode.addChild(cameraNode);
    cameraNode.addChild(lightNode);
    
    Camera camera = new Camera();
    cameraNode.setCamera(camera);
    MatrixBuilder.hyperbolic().translate(0, 0, .9).assignTo(cameraNode);
    MatrixBuilder.hyperbolic().translate(0, 0, .9).assignTo(lightNode);
    
    Light dl=new DirectionalLight();
    lightNode.setLight(dl);

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
