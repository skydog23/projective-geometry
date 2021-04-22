package de.jreality.tutorial;

import java.awt.Component;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import de.jreality.geometry.QuadMeshFactory;
import de.jreality.jogl.plugin.InfoOverlay;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;


public class MeshFrame implements KeyListener {
   
   private SceneGraphComponent dispGraph = null;
   private SceneGraphComponent root;
   
   private IndexedFaceSet createSurface( int N, int mode ) {
      double [][][] coords = new double [N][N][3];
      double [][][] colors = new double [N][N][3];
      for( int i=0; i<N; i++) {
         double v = -.4 + .8*(i/(N-1.0));
         for (int j = 0; j<N; ++j)   {
            double u = -.3 + .6*(j/(N-1.0));
            coords[i][j][0] = 10.0f - (20.0f/(float)N) * (float)(i);
            coords[i][j][1]= 10.0f - (20.0f/(float)N) * (float)(j);
            if (mode == 0)
               coords[i][j][2]= Math.sin((i+j)*0.1);
            else
               coords[i][j][2]= Math.cos((i+j)*0.01);

            colors[i][j][2]= Math.max(0.5 + 0.5 * coords[i][j][2],0.0);
               
         }
      }
      
      QuadMeshFactory factory = new QuadMeshFactory();
      factory.setVLineCount(N);      
      factory.setULineCount(N);      
      factory.setClosedInUDirection(false);   
      factory.setClosedInVDirection(false);   
      factory.setVertexCoordinates(coords);   
      factory.setVertexColors(colors);
      factory.setGenerateFaceNormals(true);
      factory.setGenerateTextureCoordinates(true);
      factory.setGenerateEdgesFromFaces(true);
      factory.setEdgeFromQuadMesh(true);
      factory.update();
      
      return factory.getIndexedFaceSet();
   }
   
   private SceneGraphComponent buildNewGraph(int mode,
                               char renderMode)
   {
      SceneGraphComponent graph =
            SceneGraphUtility.createFullSceneGraphComponent("graph");
      graph.setGeometry(createSurface(512,mode));
      Appearance ap = graph.getAppearance();
      ap.setAttribute(CommonAttributes.LIGHTING_ENABLED,false);
      //ap.setAttribute(CommonAttributes.ANY_DISPLAY_LISTS, false);
      DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
      if (renderMode == 'f' ||
          renderMode == 'F')
      {
         dgs.setShowFaces(true);
         dgs.setShowLines(false);
         dgs.setShowPoints(false);
      } else {
         dgs.setShowFaces(false);
         dgs.setShowLines(true);
         dgs.setShowPoints(false);
      }
      DefaultLineShader dls = (DefaultLineShader) dgs.createLineShader("default");
      dls.setLineLighting(false);
      dls.setTubeDraw(false);
      dls.setLineWidth(1.0);
      dls.setVertexColors(true);
      return graph;
   }
   
   public MeshFrame()
   {
      root = SceneGraphUtility.createFullSceneGraphComponent("world");
      JFrame f = new JFrame();
//      ViewerApp va = new ViewerApp(root);
      dispGraph = buildNewGraph(0,'F');
      root.addChild(dispGraph);
      Camera sceneCamera =
         CameraUtility.getCamera( va.getCurrentViewer());
		if (va.getCurrentViewer() instanceof de.jreality.jogl.InstrumentedViewer) {
			InfoOverlay io =InfoOverlay.perfInfoOverlayFor();
			io.setInstrumentedViewer((de.jreality.jogl.InstrumentedViewer)va.getCurrentViewer());
			io.setVisible(true);
		}

      sceneCamera.setFieldOfView(60);
      sceneCamera.setNear(1.0);
      sceneCamera.setFar(100);
      CameraUtility.encompass( va.getCurrentViewer());      
      Component comp = (Component)va.getViewingComponent();
      comp.addKeyListener(this);
      f.getContentPane().add(comp);
      f.setSize(512, 512);
      f.validate();
      f.setVisible(true);
   }
   
   public void keyPressed(java.awt.event.KeyEvent e)
   {
   }
   
   public void keyReleased(java.awt.event.KeyEvent e)
   {
   }
   
   public void keyTyped(java.awt.event.KeyEvent e)
   {
      if (e.getKeyChar() == '0' ||
          e.getKeyChar() == '1')
      {
         root.removeChild(dispGraph);
         dispGraph = null;
         System.gc();         
         if (e.getKeyChar() == '0')
            dispGraph = buildNewGraph(0,'F');
         else
            dispGraph = buildNewGraph(1,'F');
         root.addChild(dispGraph);
      }
   }   
   
	   public static void main(String[] args) {
	      MeshFrame f = new MeshFrame();
	   }

}
