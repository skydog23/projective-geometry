package charlesgunn.jreality.worlds;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JMenuBar;
import javax.swing.Timer;

import bezieranim3d.BezierAnim;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.util.SceneGraphUtility;

public class AnimatedTexture extends LoadableScene {

	ImageData id;
	int width, height;
	Viewer viewer;
	@Override
	public void customize(JMenuBar menuBar, Viewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent();
		Appearance ap = world.getAppearance();
		ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.white);
		double[][] texc = {{0,0},{1,0},{1,1} ,{0,1}};
		IndexedFaceSet square = IndexedFaceSetUtility.constructPolygon(
				new double[][]{   {1,1,-1},{-1,1,-1}, {-1,-1,-1},{1,-1,-1}});
		square.setVertexAttributes(Attribute.TEXTURE_COORDINATES,StorageModel.DOUBLE_ARRAY.array(2).createReadOnly(texc));
		world.setGeometry(square);
		Texture2D tex2d = (Texture2D) AttributeEntityUtility
	       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d", ap, true);
        final BezierAnim bezierAnim = new BezierAnim();
		BufferedImage bi = new BufferedImage(256, 256,BufferedImage.TYPE_4BYTE_ABGR);
		id = new ImageData(bi);
		tex2d.setImage(id);
 		tex2d.setAnimated(true);
 		tex2d.setRunnable(new Runnable() {

			public void run() {
	 			updateTexture(bezierAnim); 
			} 
 		});
 		System.err.println("runnable = "+tex2d.getRunnable());
        width = id.getWidth();
        height = id.getHeight();
        bezierAnim.reset(width, height);
        Graphics2D g2d = ((BufferedImage) id.getImage()).createGraphics();
        bezierAnim.step(width, height);
        bezierAnim.render(width, height, g2d);
        g2d.dispose();
		Timer timer = new Timer(20, new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (viewer == null) return;
//		        updateTexture(bezierAnim);
		        viewer.renderAsync();
			}

		});
		
		timer.start();
		return world;
	}

	private void updateTexture(final BezierAnim bezierAnim) {
		Graphics2D g2d = ((BufferedImage) id.getImage()).createGraphics();
        width = id.getWidth();
        height = id.getHeight();
        bezierAnim.step(width, height);
        bezierAnim.render(width, height, g2d);
        g2d.dispose();
	}
	
	public boolean isEncompass() { return true; }
	}
