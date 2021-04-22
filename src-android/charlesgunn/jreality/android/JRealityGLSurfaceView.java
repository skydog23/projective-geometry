/*
 * Created on Nov 10, 2010
 *
 */
package charlesgunn.jreality.android;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

public class JRealityGLSurfaceView extends GLSurfaceView {

	public JRealityGLSurfaceView(Context context) {
		super(context);
		System.err.println("JRealityGLSurfaceView: constructor");
	}

	public JRealityGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		System.err.println("JRealityGLSurfaceView: constructor");
	}

	@Override
	public int getDebugFlags() {
		System.err.println("JRealityGLSurfaceView: getDebugFlags()");
		return super.getDebugFlags();
	}

	@Override
	public int getRenderMode() {
		System.err.println("JRealityGLSurfaceView: getRenderMode()");
		return super.getRenderMode();
	}

	@Override
	protected void onDetachedFromWindow() {
		System.err.println("JRealityGLSurfaceView: onDetachedFromWindow()");
		super.onDetachedFromWindow();
	}

	@Override
	public void onPause() {
		super.onPause();
		System.err.println("JRealityGLSurfaceView: onPause()");
	}

	@Override
	public void onResume() {
		super.onResume();
		System.err.println("JRealityGLSurfaceView: onResume()");
	}

	@Override
	public void queueEvent(Runnable r) {
		System.err.println("JRealityGLSurfaceView: queueEvent()");
		super.queueEvent(r);
	}

	@Override
	public void requestRender() {
		System.err.println("JRealityGLSurfaceView: requestRender()");
		super.requestRender();
	}

	@Override
	public void setDebugFlags(int debugFlags) {
		System.err.println("JRealityGLSurfaceView: setDebugFlags()");
		super.setDebugFlags(debugFlags);
	}

	@Override
	public void setEGLConfigChooser(boolean needDepth) {
		super.setEGLConfigChooser(needDepth);
	}

	@Override
	public void setEGLConfigChooser(EGLConfigChooser configChooser) {
		super.setEGLConfigChooser(configChooser);
	}

	@Override
	public void setEGLConfigChooser(int redSize, int greenSize, int blueSize,
			int alphaSize, int depthSize, int stencilSize) {
		super.setEGLConfigChooser(redSize, greenSize, blueSize, alphaSize, depthSize,
				stencilSize);
	}

	@Override
	public void setEGLContextClientVersion(int version) {
		super.setEGLContextClientVersion(version);
	}

	@Override
	public void setEGLContextFactory(EGLContextFactory factory) {
		super.setEGLContextFactory(factory);
	}

	@Override
	public void setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory factory) {
		super.setEGLWindowSurfaceFactory(factory);
	}

	@Override
	public void setGLWrapper(GLWrapper glWrapper) {
		super.setGLWrapper(glWrapper);
	}

	@Override
	public void setRenderer(Renderer renderer) {
		System.err.println("JRealityGLSurfaceView: setRenderer()");
		super.setRenderer(renderer);
	}

	@Override
	public void setRenderMode(int renderMode) {
		super.setRenderMode(renderMode);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		System.err.println("JRealityGLSurfaceView: surfaceChanged()");
		super.surfaceChanged(holder, format, w, h);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		System.err.println("JRealityGLSurfaceView: surfaceCreated()");
		super.surfaceCreated(holder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		System.err.println("JRealityGLSurfaceView: surfaceDestroyed()");
		super.surfaceDestroyed(holder);
	}

}
