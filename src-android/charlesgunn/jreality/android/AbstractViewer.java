/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package charlesgunn.jreality.android;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.View;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.util.SceneGraphUtility;
abstract public class AbstractViewer implements 
		de.jreality.scene.Viewer, 
		android.opengl.GLSurfaceView.Renderer, 
		Runnable {
	protected SceneGraphComponent sceneRoot;
	SceneGraphComponent auxiliaryRoot;
	SceneGraphPath cameraPath;
	SceneGraphComponent cameraNode;
	protected GLSurfaceView surfaceView;
	protected Context context;
	protected AndroidRenderer renderer;
	protected boolean disposed = false;
	static WeakReference<Context> firstOne = new WeakReference<Context>(null);		// for now, all display lists shared with this one
	public static final int 	CROSS_EYED_STEREO = 0;
	public static final int 	RED_BLUE_STEREO = 1;
	public static final int 	RED_GREEN_STEREO = 2;
	public static final int 	RED_CYAN_STEREO =  3;
	public static final int 	HARDWARE_BUFFER_STEREO = 4;
	public static final int   	LEFT_EYE_STEREO = 5; //<-- New
	public static final int   	RIGHT_EYE_STEREO = 6; //<-- New
	public static final int 	STEREO_TYPES = 7;
	public AbstractViewer() {
		this(null, null, null);
	}

	public AbstractViewer(SceneGraphPath camPath, SceneGraphComponent root, Context ct) {
		context = ct;
		setAuxiliaryRoot(SceneGraphUtility.createFullSceneGraphComponent("AuxiliaryRoot"));
		initializeFrom(root, camPath);	
	}

	abstract void initializeFrom(SceneGraphComponent r, SceneGraphPath p);

	public GLSurfaceView getDrawable()	{
		return surfaceView;
	}
	public SceneGraphComponent getSceneRoot() {
		return sceneRoot;
	}

	public void setSceneRoot(SceneGraphComponent r) {
		if (r == null)	{
			AndroidConfiguration.getLogger().log(Level.WARNING,"Null scene root, not setting.");
			return;
		}
		sceneRoot = r;
	}

	public SceneGraphComponent getAuxiliaryRoot() {
		return auxiliaryRoot;
	}
	public void setAuxiliaryRoot(SceneGraphComponent auxiliaryRoot) {
		this.auxiliaryRoot = auxiliaryRoot;
		if (renderer != null) renderer.setAuxiliaryRoot(auxiliaryRoot);
	}

	public SceneGraphPath getCameraPath() {
		return cameraPath;
	}

	public void setCameraPath(SceneGraphPath p) {
		cameraPath = p;
	}

	public void renderAsync() {
		if (disposed) return;
	    synchronized (renderLock) {
				if (!pendingUpdate) {
					EventQueue.invokeLater(this);
					pendingUpdate = true;
				}
			}
	}

	public boolean hasViewingComponent() {
		return true;
	}

	private View component;


  public Object getViewingComponent() {
	  return surfaceView;
  }
  
  	public Dimension getViewingComponentSize() {
  		Dimension dim = new Dimension(surfaceView.getWidth(), surfaceView.getHeight());
	    return (dim);
	  }

	public void initializeFrom(de.jreality.scene.Viewer v) {
		initializeFrom(v.getSceneRoot(), v.getCameraPath());
	}
	
	public AndroidRenderer getRenderer() {
			return renderer;
		}
		
	/****** Convenience methods ************/
	public void addAuxiliaryComponent(SceneGraphComponent aux)	{
		if (auxiliaryRoot == null)	{
			setAuxiliaryRoot(SceneGraphUtility.createFullSceneGraphComponent("AuxiliaryRoot"));
		}
		if (!auxiliaryRoot.isDirectAncestor(aux)) auxiliaryRoot.addChild(aux);
	}
	
	public void removeAuxiliaryComponent(SceneGraphComponent aux)	{
		if (auxiliaryRoot == null)	return;
		if (!auxiliaryRoot.isDirectAncestor(aux) ) return;
		auxiliaryRoot.removeChild(aux);
	}
	private boolean pendingUpdate;
		
//	@Override
	public void onDrawFrame(GL11 arg0) {
		renderer.display(arg0);
	}

//	@Override
	public void onSurfaceChanged(GL11 arg0, int arg1, int arg2) {
		renderer.reshape(arg0, 0, 0, arg1, arg2);

	}

//	@Override
	public void onSurfaceCreated(GL11 arg0, EGLConfig arg1) {
		renderer = new AndroidRenderer(this);
		renderer.init(arg0);  
	}

	protected final Object renderLock=new Object();
	boolean autoSwapBuffers=true;
	
	public boolean isRendering() {
		synchronized(renderLock) {
			return pendingUpdate;
		}
	}
	  
	public void waitForRenderFinish() {
		synchronized(renderLock) {
			while(pendingUpdate) try {
				renderLock.wait();
			} catch(InterruptedException ex) {}
		}
	}
//	public AndroidFBOViewer fbo;
	public void run() {
		if (!EventQueue.isDispatchThread())
			throw new IllegalStateException();
		synchronized (renderLock) {
			pendingUpdate = false;
			renderer.display();
//			AndroidConfiguration.theLog.log(Level.INFO,"rendering "+renderer.frameCount);
//			if (listeners!=null) broadcastChange();
			renderLock.notifyAll();
		}
//		if (debug) AndroidConfiguration.theLog.log(Level.INFO,"Render: calling display");
	}

//	public void setAutoSwapMode(boolean autoSwap) {
//		autoSwapBuffers=autoSwap;
//		drawable.setAutoSwapBufferMode(autoSwap);
//	}
//	
//    final Runnable bufferSwapper = new Runnable() {
//        public void run() {
//            drawable.swapBuffers();
//        }
//    };
//                
//	public void swapBuffers() {
//		if(EventQueue.isDispatchThread()) drawable.swapBuffers();
//		else
//			try {
//				EventQueue.invokeAndWait(bufferSwapper);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				e.printStackTrace();
//			}
//	}

  public boolean canRenderAsync() {
    return true;
  }

  public void render() {
		if (disposed) return;
    if (EventQueue.isDispatchThread()) run();
    else
      try {
        EventQueue.invokeAndWait(this);
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
  }
	
  public void dispose() {
	  disposed = true;
	  cameraPath.clear();
	  cameraNode=null;
	  setSceneRoot(null);
	  setAuxiliaryRoot(null);
	  if (renderer != null) renderer.dispose();
	  renderer = null;
	  
  }
}
