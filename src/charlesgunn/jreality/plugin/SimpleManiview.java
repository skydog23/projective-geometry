/**
 *
 * This package is open source software, made available under a BSD license:
 *
 * Copyright (c) 2009, Charles Gunn
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


package charlesgunn.jreality.plugin;

import java.awt.Color;
import java.awt.Event;
import java.util.Collections;
import java.util.List;

import charlesgunn.anim.io.ImportExport;
import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.anim.util.AnimationUtility.InterpolationTypes;
import de.jreality.io.JrScene;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Inspector;
import de.jreality.plugin.basic.PropertiesMenu;
import de.jreality.plugin.basic.Shell;
import de.jreality.plugin.basic.ToolSystemPlugin;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.content.ContentTools;
import de.jreality.plugin.menu.CameraMenu;
import de.jreality.plugin.menu.ExportMenu;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.EncompassTool;
import de.jreality.tools.RotateTool;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.groups.Platycosm;
import de.jtem.discretegroup.plugin.DirichletDomainSP;
import de.jtem.discretegroup.plugin.DiscreteGroupLoader;
import de.jtem.discretegroup.plugin.FogPlugin;
import de.jtem.discretegroup.plugin.TessellatedContent;
import charlesgunn.jreality.plugin.TermesSpherePlugin;

public class SimpleManiview  {
	private DiscreteGroup dg;
	DirichletDomain dirdom;
	private static Color backgroundColor;
	private JRViewer jrv;

	private static JrScene createDefaultScene() {
		JrScene scene;
		SceneGraphComponent sceneroot = SceneGraphUtility.createFullSceneGraphComponent("root");
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		sceneroot.addChild(world);
		world.addTool(new RotateTool());
		sceneroot.addTool(new EncompassTool());
		Appearance ap = sceneroot.getAppearance();
		backgroundColor = new Color(20,20,40);
		ap.setAttribute(CommonAttributes.BACKGROUND_COLOR, backgroundColor);
		ap.setAttribute(CommonAttributes.BACKGROUND_COLORS, Appearance.INHERITED);
		SceneGraphComponent avatarNode = SceneGraphUtility.createFullSceneGraphComponent("avatar");
		sceneroot.addChild(avatarNode);
		SceneGraphComponent cameraNode = new SceneGraphComponent("camera");
		cameraNode.setTransformation(new Transformation());
		Camera c = new Camera();
		avatarNode.addChild(cameraNode);
		cameraNode.setCamera(c);
		SceneGraphPath p = new SceneGraphPath(); // (SceneGraphPath) l.get(0);
		p.push(sceneroot);
		p.push(avatarNode);
		p.push(cameraNode);
		p.push(c);
		//sceneRoot.addTool(new ClickWheelCameraZoomTool());
		// add lights
//		SceneGraphComponent lights=new SceneGraphComponent("lights");
//		SceneGraphComponent lightNode=new SceneGraphComponent("light 1");
//		lights.addChild(lightNode);
//		DirectionalLight light = new DirectionalLight();
//		double intensity = 1;
//		light.setIntensity(intensity);
//		lightNode.setLight(light);
//		avatarNode.addChild(lights);
		SceneGraphPath cp = p;
		SceneGraphPath avatarPath = cp.popNew(); avatarPath.pop();
		List<SceneGraphPath> paths = SceneGraphUtility.getPathsToNamedNodes(sceneroot, "avatar");
		if (paths != Collections.EMPTY_LIST) avatarPath = paths.get(0);			
		SceneGraphPath contentPath = new SceneGraphPath(sceneroot,world);
		scene = new JrScene(sceneroot);
		scene.addPath("cameraPath", cp);
		scene.addPath("avatarPath", avatarPath);
		scene.addPath("emptyPickPath", contentPath);
		scene.addPath("contentPath", contentPath);
		return scene;
	}
	
	private DiscreteGroup getGroup()	{
		DiscreteGroup group = Platycosm.instanceOfGroup("c6");
//		DiscreteGroup group = BorromeanUtility.borromeanGroupOfOrder(4);
//		dirdom = new DirichletDomain(group);
		group.update();
		return group;
	}
	
	private void doIt() {
		JrScene scene = createDefaultScene();
		jrv = new JRViewer(scene);
		jrv.setPropertiesResource(SimpleManiview.class, "simpleManiview.xml");
		jrv.registerPlugin(new ToolSystemPlugin());
		jrv.registerPlugin(new Inspector());
		jrv.registerPlugin(new Shell());
		
		jrv.registerPlugin(new ViewMenuBar());
		jrv.registerPlugin(new ViewToolBar());
		
		jrv.registerPlugin(new ExportMenu());
		jrv.registerPlugin(new PropertiesMenu());
		jrv.registerPlugin(new ContentAppearance());
		jrv.registerPlugin(new CameraMenu());
		jrv.registerPlugin(new ContentTools());
		AnimationPlugin ap = new AnimationPlugin();
		ap.setDefaultInterp(InterpolationTypes.LINEAR);
		jrv.registerPlugin(ap);
		
		final TessellatedContent tessellatedContent = new TessellatedContent();
		jrv.registerPlugin(tessellatedContent);	
		tessellatedContent.setupJRViewer(jrv);
		
		final DirichletDomainSP dirichletDomainSP = new DirichletDomainSP(tessellatedContent);
		dirichletDomainSP.getShrinkPanel().setShrinked(true);
		jrv.registerPlugin(dirichletDomainSP);
		
		final DiscreteGroupLoader dgl = new DiscreteGroupLoader();
		jrv.registerPlugin(dgl);
		dgl.addGroupLoadedListener(new DiscreteGroupLoader.GroupLoadedListener() {
			public void GroupLoaded(Event e) {
				tessellatedContent.setGroup(dgl.getGroup());
			}	
		});			

		jrv.registerPlugin(new TermesSpherePlugin());
		jrv.registerPlugin(new FogPlugin());
		jrv.registerPlugin(new ContentLoader());
		jrv.startup();	

		ap.setAnimateCamera(true);
		ap.setAnimateSceneGraph(true);
//		ImportExport.readInto(ap.getAnimationPanel(), 
//				this.getClass().getResourceAsStream("simpleManiviewAnim-02.xml"));
		ap.getAnimationPanel().getRecordPrefs().setCurrentDirectoryPath("/Volumes/SamsungSSD1T/gunn_local/Movies/conformal/stereographproj");
		ap.getAnimationPanel().setResourceDir("/Volumes/SamsungSSD1T/gunn_local/workspace/ProjectiveGeometry/src/charlesgunn/jreality/plugin//");

		// following is now optional; without it uses trivial group
		dg = getGroup();
		tessellatedContent.setClipToCamera(true);
		tessellatedContent.setFollowsCamera(true);
		tessellatedContent.setGroup(dg, true);
//		RenderTrigger rt = new RenderTrigger();
//		rt.addSceneGraphComponent(c)
		jrv.getPlugin(View.class).getViewer().renderAsync();
		ap.setAnimateCamera(true);
		ap.setAnimateSceneGraph(true);
	}

	public static void main(String[] args) throws Exception {
		SceneGraphNode.setThreadSafe(false);
		// turn off render trigger since it slows down the tessellation rendering too much
		// has to be done here since it's evaluated once-for=all at startup
//		Secure.setProperty(SystemProperties.AUTO_RENDER, "false");
//		Secure.setProperty(SystemProperties.JOGL_COPY_CAT, "true");
		SimpleManiview sm = new SimpleManiview();
		sm.doIt();
	}


}
