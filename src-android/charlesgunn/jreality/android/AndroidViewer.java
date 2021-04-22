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

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.PixelFormat;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
public class AndroidViewer extends AbstractViewer {
//	protected GLCanvas canvas;

	public AndroidViewer(SceneGraphPath camPath, SceneGraphComponent root, Context ct) {
		super(camPath, root, ct);
	}

	@Override
	protected void initializeFrom(SceneGraphComponent r, SceneGraphPath p)	{
		setSceneRoot(r);
		setCameraPath(p);
		EGLConfig[] configs = new EGLConfig[1];
//		GLCapabilities caps = new GLCapabilities();
//		caps.setAlphaBits(8);
//		caps.setStereo(AndroidConfiguration.quadBufferedStereo);
//		caps.setDoubleBuffered(true);
		Context sharedContext = firstOne.get();
//		if (AndroidConfiguration.multiSample)	{
//			GLCapabilitiesChooser chooser = new MultisampleChooser();
//			caps.setSampleBuffers(true);
//			caps.setNumSamples(4);
//			caps.setStereo(AndroidConfiguration.quadBufferedStereo);
//			canvas = new GLCanvas(caps, chooser, sharedContext,  GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
//		} else {
//			canvas = new GLCanvas(caps);
//		}
//        AndroidConfiguration.getLogger().log(Level.INFO, "Caps is "+caps.toString());
// 		surfaceView.addGLEventListener(this);
// 		if (AndroidConfiguration.quadBufferedStereo) setStereoType(HARDWARE_BUFFER_STEREO);
//		canvas.requestFocus();
		surfaceView = new JRealityGLSurfaceView(context);
		surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		
		if (AndroidConfiguration.sharedContexts && sharedContext == null) {
			firstOne = new WeakReference<Context>(surfaceView.getContext());
		}
	}

	public void dispose() {
		super.dispose();
//		if (surfaceView != null) surfaceView.removeGLEventListener(this);
		surfaceView = null;
//		canvas=null;
	}

	public void onDrawFrame(GL10 arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onSurfaceChanged(GL10 arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		// TODO Auto-generated method stub
		
	}
	
}
