package charlesgunn.video;

import java.awt.Dimension;
import java.awt.Frame;

import javax.media.Format;
import javax.media.format.RGBFormat;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLU;
import de.jreality.jogl.JOGLRenderer;

/*
 * Created on 25.02.2006
 *
 * This file is part of the  package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */

public class VideoTexture  extends AbstractVideoProvider {
    int width;
    int height;
    
    byte[] ba;
    long t;
    private boolean newData = false;
    private boolean inited = false;
    private int textureID = -1 ;

    
    public VideoTexture() {
        super(new Frame());
    }
    
    public void init(GL gl) {
        if(inited) return;
//        if(textureID  == -1) {
//            int[] tmp = new int[1]; 
//           gl.glGenTextures(1, tmp);
//           textureID = tmp[0];
//        }
//        gl.glBindTexture(GL.GL_TEXTURE_2D, textureID);
//        gl.glMatrixMode(GL.GL_TEXTURE);
//        //gl.glLoadIdentity();
//        gl.glLoadTransposeMatrixd(flipmatrix);
//        gl.glMatrixMode(GL.GL_MODELVIEW);
        inited = true;
    }
    private double[] flipmatrix = new double[]{-1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};
    private double[] matrix = new double[]{1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};
    public void render(JOGLRenderer jr) {
        GL gl = jr.globalGL;
        if(!inited) {
            init(gl);
        }
        
        if(textureID  == -1) {
            int[] tmp = new int[1]; 
           gl.glGenTextures(1, tmp);
           textureID = tmp[0];
        }
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureID);
        if(newData) {
            GLU glu = jr.getCanvas().getGLU();
            if(true) {
                glu.gluBuild2DMipmaps(GL.GL_TEXTURE_2D, GL.GL_RGBA, width,
                        height, GL.GL_BGR, GL.GL_UNSIGNED_BYTE, ba);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_NEAREST); 
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR_MIPMAP_NEAREST);
            
             
            }else {
                gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, //GL.GL_RGBA,
                        width, height, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, ba);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR); 
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            }
            
            gl.glMatrixMode(GL.GL_TEXTURE);
            //gl.glLoadIdentity();
            gl.glLoadTransposeMatrixd(matrix);
            gl.glMatrixMode(GL.GL_MODELVIEW);
            newData = false;
        }
        gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
        //gl.glBlendFunc(GL.GL_SRC_ALPHA,GL.GL_ONE);
    }
   
    protected final void processBuffer() {
        if(System.currentTimeMillis()-t >1000/5 ) {
        t = System.currentTimeMillis();
        Format format = buffer.getFormat();
        if(format instanceof RGBFormat) {
            RGBFormat rgb = (RGBFormat) format;
            Dimension d = rgb.getSize();
            byte[] b = (byte[]) buffer.getData();
            width = d.width;
            height = d.height;
            ba = b;
            newData  = true;
        }
        }
        
    }
}
