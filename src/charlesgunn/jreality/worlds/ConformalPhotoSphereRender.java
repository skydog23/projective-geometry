/*
 * Created on May 12, 2013
 *
 */
package charlesgunn.jreality.worlds;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.jreality.plugin.TermesSpherePlugin;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.Shell;
import de.jreality.plugin.basic.ViewPreferences;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.content.ContentTools;
import de.jreality.plugin.experimental.ViewerKeyListenerPlugin;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;

public class ConformalPhotoSphereRender extends Assignment {

	private TermesSpherePlugin termes;
//	protected String searchDir = "/Volumes/SamsungSSD1T/gunn_local/TUB-HomepageOct19/WWW/Pictures/textures/", 
//			imageName = "Horeshoe-Bend-PS.jpg";
	protected String searchDir = "/Users/gunn/Downloads/", 
			imageName = "mars-perseverence-2.png";
	@Override
	public SceneGraphComponent getContent() {
		String filename = searchDir+imageName;
		Input input = null;
		try {
			input = Input.getInput(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return loadPhotoSphere(input);
	}

	public SceneGraphComponent loadPhotoSphere(Input input)	{
		ImageData id = null;
		try {
			id = ImageData.load(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //purpleAster-02.jpg")); //
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		world.setGeometry(SphereUtility.sphericalPatch(0, 0, 360, 180, 40, 20, 1.0));
		Appearance ap = world.getAppearance();
		ap.setAttribute("polygonShader.diffuseColor", Color.white);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		Texture2D tex = TextureUtility.createTexture(ap, "polygonShader", id);
		Matrix m = new Matrix();
		MatrixBuilder.euclidean().scale(-1,1,1).assignTo(m);
		tex.setTextureMatrix(m);
		return world;
	}
	@Override
	public void setupJRViewer(JRViewer v) {
		// TODO Auto-generated method stub
		super.setupJRViewer(v);
		jrviewer.setPropertiesFile("TermesFromPhotoSphere.xml");
		jrviewer.setPropertiesResource(this.getClass(), "TermesFromPhotoSphere.xml");
//		termes = new TermesSpherePlugin();
		termes.setReducedGUI(false);
		v.registerPlugin(termes);
		v.registerPlugin(new ViewerKeyListenerPlugin());
	}

	@Override
	public List<Plugin> getPluginsToRegister() {
			pluginsToLoad.add(new Shell());
			pluginsToLoad.add(contentPlugin);
			pluginsToLoad.add(new ContentTools());
			pluginsToLoad.add(new ContentLoader());
			pluginsToLoad.add(new ViewPreferences());
			animationPlugin = new AnimationPlugin();
			pluginsToLoad.add(animationPlugin);
			pluginsToLoad.add(new ViewerKeyListenerPlugin());
			pluginsToLoad.add(shrinkPanelPlugin);
			pluginsToLoad.add(termes = new TermesSpherePlugin());
			return pluginsToLoad;
		}


	public static void main(String[] args) {
		ConformalPhotoSphereRender tfsi = new ConformalPhotoSphereRender();
		tfsi.display();
	}

	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		Scene scene = jrviewer.getPlugin(Scene.class);
		MatrixBuilder.euclidean().assignTo(scene.getAvatarComponent());
		termes.getTermes().update();
		termes.getTermes().setVisible(true);
		termes.getTermes().setCameraPosition(0);
		termes.toggleViewB.setSelected(true);
		// for some reason it doesn't want to display the scene right away.  Have to ask several times ...
		jrviewer.getViewer().render();
		jrviewer.setShowPanelSlots(false, false, false, false);
		jrviewer.getViewer().render();
		jrviewer.setShowPanelSlots(true, false, false, false);
		jrviewer.getViewer().renderAsync();
	}

	@Override
	public Component getInspector() {
		Box box = Box.createVerticalBox();
		Box hbox = Box.createHorizontalBox();
		box.add(hbox);
		JButton loadB = new JButton("Load photosphere");
		hbox.add(Box.createHorizontalGlue());
		hbox.add(loadB);
		hbox.add(Box.createHorizontalGlue());
		
		loadB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser filechoose = new JFileChooser(searchDir);
				int returnVal = filechoose.showOpenDialog(shrinkPanel);
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			        File files = filechoose.getSelectedFile();
					Input input = null;
					try {
						input = new Input(files);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					contentPlugin.setContent(loadPhotoSphere(input));
					termes.getTermes().update();
					String foo = files.getPath();
					searchDir = foo.substring(0, foo.lastIndexOf('/')+1);
					imageName = files.getName();
					System.err.println("searchDir = " + searchDir);
				}
			}
		});
		return box;
	}

	@Override
	public void restoreStates(Controller c) throws Exception {
		// TODO Auto-generated method stub
		super.restoreStates(c);
		searchDir = c.getProperty(getClass(), "searchDir", searchDir);
		imageName = c.getProperty(getClass(), "imageName", imageName);
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		// TODO Auto-generated method stub
		super.storeStates(c);
		c.storeProperty(getClass(), "searchDir", searchDir);
		c.storeProperty(getClass(), "imageName", imageName);
	}

	public String getSearchDir() {
		return searchDir;
	}

	public void setSearchDir(String searchDir) {
		this.searchDir = searchDir;
	}

	public String getName() {
		return imageName;
	}

	public void setName(String name) {
		this.imageName = name;
	}

	@Override
	public String getDocumentationFile() {
		// TODO Auto-generated method stub
		return "http://page.math.tu-berlin.de/~gunn/Files/ConformalPhotoSphereRender.html"; //"ConformalPhotoSphereRender.html"; //
	}
}
