/*
 * Created on Dec 23, 2010
 *
 */
package charlesgunn.jreality.worlds;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import charlesgunn.anim.core.Animated;
import charlesgunn.anim.core.KeyFrameAnimatedDelegate;
import charlesgunn.anim.core.KeyFrameAnimatedDouble;
import charlesgunn.anim.gui.AnimationPanelListenerImpl;
import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;

public class Christmas2010 extends LoadableScene {

	SceneGraphComponent world, billboardSGC;
	double aspect = 560/477.0;
	Texture2D android, tree;
	@Override
	public SceneGraphComponent makeWorld() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		billboardSGC = SceneGraphUtility.createFullSceneGraphComponent("android");
//		tree = SceneGraphUtility.createFullSceneGraphComponent("tree");
		world.addChildren(billboardSGC);
		IndexedFaceSet billboard = Primitives.texturedQuadrilateral();
		billboardSGC.setGeometry(billboard);
		MatrixBuilder.euclidean().rotateX(Math.PI).scale(aspect,1,1).assignTo(billboardSGC);
		Appearance ap = billboardSGC.getAppearance();
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, java.awt.Color.white);
		ImageData id = null;
		try {
			id = ImageData.load(Input.getInput(
					"/Users/gunn//Pictures/grabs/Asus-Android-phone.png")); // weaveRGBABright.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	  	
		android = TextureUtility.createTexture(ap, "polygonShader", 0, id);
		android.setApplyMode(Texture2D.GL_DECAL);
	  	android.setBlendColor(new Color(128,128,128,128));
	  	
		try {
			id = ImageData.load(Input.getInput(
					"/Users/gunn//Pictures/grabs/Asus-Android-phone-jreality.png")); // weaveRGBABright.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		tree = TextureUtility.createTexture(ap, "polygonShader", 1, id);
		tree.setApplyMode(Texture2D.GL_COMBINE);
	  	update();
		return world;
	}
	 TextSlider timeSlider = null;
	public boolean hasInspector() {return true; }
	public Component getInspector(final Viewer viewer) {
		Box container = Box.createVerticalBox();
		timeSlider = new TextSlider.Double("time",  SwingConstants.HORIZONTAL, 0.0, 1.0, alpha);
	    timeSlider.addActionListener( new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					alpha = timeSlider.getValue().doubleValue();
					update();
				}
 	       });
	    container.add(timeSlider);
	    return container;
	}

	@Override
	public boolean isEncompass() {
		// TODO Auto-generated method stub
		return true;
	}
	
	public void update()	{
		float fscale = (float) alpha;
        tree.setBlendColor(new Color(fscale, fscale, fscale, fscale));
	}
	double alpha = 0.0;
	@Override
	public void customize(JMenuBar menuBar, final PluginSceneLoader psl) {
		AnimationPlugin ap = psl.getAnimationPlugin();
		ArrayList<Animated> animated = new ArrayList<Animated>();
		KeyFrameAnimatedDelegate<Double> dd = new KeyFrameAnimatedDelegate<Double> () {

			public void propagateCurrentValue(Double t) {
				alpha = t;
				timeSlider.setValue(t);
				update();
				psl.getViewer().renderAsync();
			}

			public Double gatherCurrentValue(Double t) {
				return alpha;
			}
			
		};
		KeyFrameAnimatedDouble animAlpha = new KeyFrameAnimatedDouble(dd );
		animAlpha.setName("animAlpha");
		animated.add(animAlpha);
		
		AnimationPanelListenerImpl apl = new AnimationPanelListenerImpl(psl.getViewer(), "christmas demo");
		apl.setAnimated(animated);
		ap.setAnimateSceneGraph(true);
		ap.setAnimateCamera(true);
		ap.getAnimationPanel().addAnimationPanelListener(apl);
	}

}
