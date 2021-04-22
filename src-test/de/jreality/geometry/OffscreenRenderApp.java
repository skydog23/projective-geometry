package de.jreality.geometry;
import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import de.jreality.io.JrSceneFactory;

public class OffscreenRenderApp {

   public static void main(String[] args) throws InterruptedException {
      
      final de.jreality.jogl.JOGLViewer v = new de.jreality.jogl.JOGLViewer();
      v.setSceneRoot(Primitives.wireframeSphere()); 
      v.getSceneRoot().getAppearance().setAttribute("backgroundColor", Color.red);
      v.setCameraPath(JrSceneFactory.getDefaultDesktopScene().getPath("cameraPath"));
   
//      JFrame frame = new JFrame();
//      frame.getContentPane().add((Component) v.getViewingComponent());   
//      frame.setSize(100,100);  
//      frame.setTitle("jreality");
//      frame.setVisible(true);
//      v.render();
      final javax.swing.JFrame jf = new javax.swing.JFrame();
      jf.setTitle("image");
//      java.awt.EventQueue.invokeLater(new Runnable() {public void run(){
         BufferedImage bi = v.renderOffscreen(1000,1000); 
                        // we have a buffered image to do with what we want.
                        // to write to a file, serve in a servlet, send to image processing, etc...
                        // but we'll just display here to show that we have the image.
         jf.getContentPane().add(bi==null ? new JLabel("NULL") : new JLabel(new ImageIcon(bi)));
         jf.setVisible(true);
         jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         jf.pack();
//      }});
   }
}