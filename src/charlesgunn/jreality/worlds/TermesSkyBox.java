/*
 * Created on Nov 21, 2011
 *
 */
package charlesgunn.jreality.worlds;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JMenuBar;

import charlesgunn.jreality.plugin.TermesSpherePlugin;
import charlesgunn.jreality.plugin.TermesSphereSPP;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.TextureUtility;
import de.jreality.util.SceneGraphUtility;

public class TermesSkyBox extends Assignment {

	CubeMap cubemap;
//	String filebase = "http://www.math.tu-berlin.de/~gunn/Pictures/textures/";
	String filebase = "/Users/gunn/Pictures/textures/";
	String scenes[] = {"berlinerdom", "gendarmemarkt", "woods"};
	int which = 1;
	private SceneGraphComponent world;
	private TermesSpherePlugin tsp;
	@Override
	public SceneGraphComponent getContent() {
		world = SceneGraphUtility.createFullSceneGraphComponent();
		return world;
	}
	protected void updateCubemap() {
		Appearance ap = new Appearance();
		try {
	  			cubemap = TextureUtility.createReflectionMap(
                  ap,
                  "polygonShader",
	                  filebase+"/jonaspfeil/"+scenes[which]+"/tile_", //"textures/desertstorm/desertstorm_",
//			          new String[]{"rt","lf","up", "dn","bk","ft"},
//			          ".png");                 
//	  			"http://www.math.tu-berlin.de/~gunn/Pictures/textures/jms/jms_",
                  new String[]{"rt","lf","up", "dn","ft","bk"},
                  ".jpg");
				tsp.getTermes().setCubeMap(cubemap);
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	}
	@Override
	public void setupJRViewer(JRViewer v) {
		// TODO Auto-generated method stub
		super.setupJRViewer(v);
		tsp = new TermesSpherePlugin(false);
		tsp.setFromStills(true);
		jrviewer.registerPlugin(tsp);
		v.registerPlugin(tsp);
		TermesSphereSPP tsspp = new TermesSphereSPP();
		jrviewer.registerPlugin(tsspp);
		jrviewer.setPropertiesFile("TermesSkyBox.xml");
		jrviewer.setPropertiesResource(this.getClass(), "TermesSkyBox.xml");
	}
	@Override
	public void display() {
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(0,0,0,0));
		updateCubemap();
		tsp.getTermes().update();
		tsp.getTermes().setVisible(true);
		tsp.getTermes().setCameraPosition(1);
	}
	@Override
	public Component getInspector() {
		Box vbox = Box.createVerticalBox();
		JComboBox scenesCB = new JComboBox(scenes);
		scenesCB.setSelectedIndex(which);
		vbox.add(scenesCB);
		scenesCB.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				which = ((JComboBox)arg0.getSource()).getSelectedIndex();
				updateCubemap();
				tsp.getTermes().update();
			}
		});
		return vbox;
	}

	public static void main(String[] args) {
		new TermesSkyBox().display();
	}
}
