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

import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.microedition.khronos.opengles.GL11;

import charlesgunn.jreality.android.shader.RenderingHintsInfo;
import charlesgunn.jreality.android.shader.Texture2DLoaderAndroid;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.util.CameraUtility;
import de.jreality.util.LoggingSystem;
import de.jreality.util.SceneGraphUtility;

public class AndroidRenderer   {

	public GL11 globalGL;
	protected SceneGraphComponent theRoot, auxiliaryRoot;

	// peer objects 
	transient protected AndroidPeerComponent thePeerRoot = null,
		thePeerAuxilliaryRoot = null;
	// helper objects
	transient public AndroidRenderingState renderingState;
	transient protected AndroidLightHelper lightHelper;
	transient protected AndroidTopLevelAppearance topAp;
//	transient protected AndroidFBOViewer fboViewer;
	transient protected AndroidPerformanceMeter perfMeter;
	transient protected GeometryGoBetween geometryGB;

	transient protected int width, height;		// GLDrawable.getSize() isnt' implemented for GLPBuffer!
	transient protected int whichEye = CameraUtility.MIDDLE_EYE;
	transient protected int[] currentViewport = new int[4];

	transient private final  Logger theLog = LoggingSystem.getLogger(this);
	// software matrix stack
	transient protected final static int MAX_STACK_DEPTH = 14;	// hardware supported
	transient protected Matrix[] matrixStack = new Matrix[128];
	transient protected int stackCounter,
		stackDepth;
	transient protected Stack<RenderingHintsInfo> rhStack = new Stack<RenderingHintsInfo>();

	transient protected int numberTries = 0;		// how many times we have tried to make textures resident
	// miscellaneous fields and methods
	transient protected int clearColorBits;
	// an exotic mode: render the back hemisphere of the 3-sphere (currently disabled)
	transient public static double[] frontZBuffer = new double[16], backZBuffer = new double[16];

	transient protected boolean 
		lightListDirty = true, 
		lightsChanged = true, 
		clippingPlanesDirty = true,
		disposed = false,
		frontBanana = false,
		texResident = true,
		offscreenMode = false;
	protected Viewer theViewer;
	protected Camera theCamera;

	static {
		MatrixBuilder.euclidean().translate(0,0,-.5).scale(1,1,.5).assignTo(frontZBuffer);
		MatrixBuilder.euclidean().translate(0,0,.5).scale(1,1,.5).assignTo(backZBuffer);
		Rn.times(backZBuffer, -1, backZBuffer);
	}
	public AndroidRenderer(Viewer viewer) {
		theViewer=viewer;
		//TODO figure out I do this here
		perfMeter = new AndroidPerformanceMeter(this);
		geometryGB = new GeometryGoBetween(this);
		renderingState = new AndroidRenderingState(this);
		setAuxiliaryRoot(viewer.getAuxiliaryRoot());	
	}

	public void dispose() {
		disposed = true;
		lightHelper.disposeLights();
		setSceneRoot(null);
		setAuxiliaryRoot(null);
		Texture2DLoaderAndroid.deleteAllTextures(globalGL);
		if (topAp != null) topAp.dispose();
		LoggingSystem.getLogger(this).info("gobetween table has "+GoBetween.rendererTable.get(this).size());
		LoggingSystem.getLogger(this).info("geom table has "+geometryGB.geometries.size());
		geometryGB.dispose();
	}

	public int getStereoType() {
		return renderingState.stereoType;
	}

	public void setStereoType(int stereoType) {
		renderingState.stereoType = stereoType;
	}

	public Viewer getViewer() {
		return theViewer;
	}

	private void setSceneRoot(SceneGraphComponent sgc) {
		if (topAp != null) {
			topAp.dispose();
		}
		theRoot = sgc;
		if (theRoot != null && theRoot.getAppearance() != null)  {
			topAp = new AndroidTopLevelAppearance(theRoot.getAppearance());
		} else {
			topAp = new AndroidTopLevelAppearance(new Appearance("dummy root appearance"));
		}
		
		if (thePeerRoot != null) {
			thePeerRoot.dispose();
			thePeerRoot = null;
		}
		else return;

		theLog.fine("setSceneRoot");
	}

	public SceneGraphComponent getAuxiliaryRoot() {
		return auxiliaryRoot;
	}
	public void setAuxiliaryRoot(SceneGraphComponent auxiliaryRoot) {
		this.auxiliaryRoot = auxiliaryRoot;
		if (thePeerAuxilliaryRoot != null) thePeerAuxilliaryRoot.dispose();
		if (auxiliaryRoot != null) {
			thePeerAuxilliaryRoot = ConstructPeerGraphVisitor.constructPeerForSceneGraphComponent(
					auxiliaryRoot, null, this);
		}
	}
	public void render() {
		if (disposed) return;
		Texture2DLoaderAndroid.clearAnimatedTextureTable(globalGL);
		if (thePeerRoot == null || theViewer.getSceneRoot() != thePeerRoot.getOriginalComponent())	{
			setSceneRoot(theViewer.getSceneRoot());
			thePeerRoot = ConstructPeerGraphVisitor.constructPeerForSceneGraphComponent(theRoot, null, this); 
		}
		if (auxiliaryRoot != null && thePeerAuxilliaryRoot == null)
			thePeerAuxilliaryRoot = ConstructPeerGraphVisitor.constructPeerForSceneGraphComponent(
					auxiliaryRoot, null, this);

		renderingState.oneTexture2DPerImage = topAp.isOneTexture2DPerImage();
		renderingState.currentPath.clear();
		renderingState.context  = new Graphics3D(theViewer.getCameraPath(), renderingState.currentPath, CameraUtility.getAspectRatio(theViewer));
		globalGL.glMatrixMode(GL11.GL_PROJECTION);
		globalGL.glLoadIdentity();

		AndroidRendererHelper.handleBackground(this, width, height, theRoot.getAppearance());

		frontBanana = true;
		renderOnePass();
		if (topAp.isRenderSpherical())	{
			frontBanana = false;
			renderOnePass();
		}
		if (topAp.isForceResidentTextures()) forceResidentTextures();

		lightListDirty = false;
		
	}


	private void renderOnePass() {
		if (theCamera == null) return;
		double aspectRatio = getAspectRatio();
		// for pick mode the aspect ratio has to be set to that of the viewer component
		globalGL.glMatrixMode(GL11.GL_PROJECTION);
		globalGL.glLoadIdentity();
		if (topAp.isRenderSpherical())	
		{
			globalGL.glMultMatrixf(Util.getTransposedFloatMatrix(null,frontBanana ? frontZBuffer : backZBuffer), 0);
//			System.err.println("c2ndc = "+Rn.matrixToString(
//					Rn.times(null, frontBanana ? frontZBuffer : backZBuffer, c2ndc)));
		}
		double[] c2ndc = CameraUtility.getCameraToNDC(theCamera, 
				aspectRatio,
				whichEye);
		globalGL.glMultMatrixf(Util.getTransposedFloatMatrix(null,c2ndc), 0);

		// prepare for rendering the geometry
		globalGL.glMatrixMode(GL11.GL_MODELVIEW);
		globalGL.glLoadIdentity();

		renderingState.cameraToWorld = renderingState.context.getCameraToWorld();
		renderingState.worldToCamera = Rn.inverse(null, renderingState.cameraToWorld);
		renderingState.cameraToNDC = c2ndc;
		globalGL.glMultMatrixf(Util.getTransposedFloatMatrix(null,renderingState.worldToCamera), 0);
		if (topAp.getSkyboxCubemap() != null) 
			AndroidSkyBox.render(globalGL, 
					renderingState.worldToCamera, 
					topAp.getSkyboxCubemap(), 
					CameraUtility.getCamera(theViewer));

		processLights();

		processClippingPlanes();

		rhStack.clear();
		rhStack.push(RenderingHintsInfo.defaultRHInfo);
		RenderingHintsInfo.defaultRHInfo.render(renderingState, null);
		renderingState.flipped = (Rn.determinant(renderingState.worldToCamera) < 0.0);
		globalGL.glFrontFace(renderingState.flipped ? GL11.GL_CW : GL11.GL_CCW);

		texResident=true;
		thePeerRoot.render();		
		if (thePeerAuxilliaryRoot != null) thePeerAuxilliaryRoot.render();
		if (topAp.isRenderSpherical() && !frontBanana) globalGL.glPopMatrix();
		globalGL.glLoadIdentity();
	}

	List clipPlanes = null;
	private void processClippingPlanes() {
		if (clipPlanes == null  || clippingPlanesDirty) {
			clipPlanes = SceneGraphUtility.collectClippingPlanes(theRoot);
		}
		AndroidRendererHelper.processClippingPlanes(this, clipPlanes);
		clippingPlanesDirty = false;
	}

	List<SceneGraphPath> lights = null;
	private void processLights( ) {
//		lightsChanged = false;
		if (lights == null || lights.size() == 0 || lightListDirty) {
			lightHelper.disposeLights();
			lights = SceneGraphUtility.collectLights(theRoot);
			lightHelper.resetLights(globalGL, lights);
			lightListDirty = false;
			renderingState.numLights = lights.size();
			lightsChanged = true;
		}
		lightHelper.enableLights(globalGL, lights.size());
		if (lightsChanged) {
			lightHelper.cacheLightMatrices(lights);
			lightsChanged = false;
		}
		lightHelper.processLights(globalGL, lights);
	}

	private void forceResidentTextures() {
		// Try to force textures to be resident if they're not already
		if (!texResident && numberTries < 3)	{
			final Viewer theV = theViewer;
			TimerTask rerenderTask = new TimerTask()	{
				public void run()	{
					theV.render();
				}
			};
			Timer doIt = new Timer();
			forceNewDisplayLists();
			doIt.schedule(rerenderTask, 10);
			numberTries++;		// don't keep trying indefinitely
			AndroidConfiguration.theLog.log(Level.WARNING,"Textures not resident");
		} else numberTries = 0;
	}


	private void forceNewDisplayLists() {
		if (thePeerRoot != null) thePeerRoot.setDisplayListDirty();
		if (thePeerAuxilliaryRoot != null) thePeerAuxilliaryRoot.setDisplayListDirty();
	}


	public double getFramerate()	{
		return perfMeter.getFramerate();
	}

	public double getClockrate()	{
		return perfMeter.getClockrate();
	}

	public int getPolygonCount()	{
		return renderingState.polygonCount;
	}
	
	protected void myglViewport(int lx, int ly, int rx, int ry)	{
		globalGL.glViewport(lx, ly, rx, ry);
		currentViewport[0] = lx;
		currentViewport[1] = ly;
		currentViewport[2] = rx;
		currentViewport[3] = ry;
	}

	public Graphics3D getContext() {
		return renderingState.context;
	}

	public int[] getCurrentViewport()	{
		return currentViewport;
	}

	public double getAspectRatio() {
		return ((double) currentViewport[2])/currentViewport[3];
	}


	public void init(GL11 gl) {
		System.err.println("initing gl");
		globalGL = gl;
	
		String vv = globalGL.glGetString(GL11.GL_VERSION);
		theLog.log(Level.FINE,"new GL: "+gl);			
		theLog.log(Level.FINE,"version: "+vv);			
//		renderingState = new AndroidRenderingState(this);
		lightHelper = new AndroidLightHelper(this);
		lightsChanged = true;
		Texture2DLoaderAndroid.deleteAllTextures(globalGL);
//		AndroidCylinderUtility.setupCylinderDLists(this);
//		AndroidSphereHelper.setupSphereDLists(this);
		if (thePeerRoot != null) 
			thePeerRoot.propagateGeometryChanged(AndroidPeerComponent.ALL_GEOMETRY_CHANGED);
		if (thePeerAuxilliaryRoot != null) 
			thePeerAuxilliaryRoot.propagateGeometryChanged(AndroidPeerComponent.ALL_GEOMETRY_CHANGED);
	}
	
//	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
//	}

	public void reshape(GL11 arg0,int minx,int miny,int maxx,int maxy) {
		globalGL = arg0;
		width = maxx-minx;
		height = maxy-miny;
		myglViewport(0,0, width, height);
	}

	protected int[] whichTile = new int[2];
	
	public void display()	{
		display(globalGL);
	}
	public void display(GL11 gl) {
//		System.err.println("display "+width+" "+height);
		globalGL=gl;
		perfMeter.beginFrame();
		renderingState.initializeGLState();
		renderingState.currentEye = CameraUtility.MIDDLE_EYE;		
		clearColorBits = (renderingState.clearColorBuffer ? GL11.GL_COLOR_BUFFER_BIT : 0);
		try {
			theCamera = CameraUtility.getCamera(theViewer);
		} catch (IllegalStateException ise) {
			return;
		}
		renderingState.clearBufferBits = clearColorBits | GL11.GL_DEPTH_BUFFER_BIT;
		myglViewport(0,0,width, height);
		whichEye=CameraUtility.MIDDLE_EYE;
		render();			
		perfMeter.endFrame();
	}


}
