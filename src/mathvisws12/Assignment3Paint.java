package mathvisws12;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.jreality.geometry.Primitives;
import de.jreality.jogl.shader.ShadedSphereImage;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.SceneGraphUtility;

/**
 * This class demonstrates the difference between the three classical planar geometries
 * (euclidean, elliptic, and hyperbolic).  A translation in the x-direction is calculated
 * and repeated in both directions. The length of the translation is controlled by a slider.
 * To simplify the example, the results are rendered without lighting. The user can click
 * and drag the pattern.  The dragging respects the metric of the pattern.  
 * 
 * @author Charles Gunn
 *
 */
public class Assignment3Paint extends Assignment3Complete {
	private SceneGraphComponent canvasSGC = SceneGraphUtility.createFullSceneGraphComponent("canvas");
;
	private Graphics2D g2d;
	
	public static void main(String[] args)		{
		Assignment3Paint theProgram = new Assignment3Paint();
		theProgram.display();
	}

	public SceneGraphComponent getContent() {
		super.getContent();
		elSGC.setVisible(false);
		elSGC.setPickable(false);
		setupPainting();
		return worldSGC;
	}

	// this method sets up the scene graph but doesn't set the matrices
	@Override
	protected void initCopies() {
		super.initCopies();
		for (int i = 0; i<3; ++i)	{
			fundamentalDomain[i].addChild(canvasSGC);
			MatrixBuilder.euclidean().scale(1.0).translate(0,-.5,-.02).assignTo(canvasSGC);
		}
	}
	
	@Override
	protected void updateCopies()	{
		super.updateCopies();
		boolean doubled = (whichGroup == 4 || whichGroup == 6);
		MatrixBuilder.euclidean().scale(1.0).translate(0,doubled ? 0 : -.5,-.02).assignTo(canvasSGC);

	}

	private void setupPainting()	{
		IndexedFaceSet canvas = Primitives.texturedQuadrilateral();
		canvasSGC.setGeometry(canvas);
		canvasSGC.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, new Color(255, 255,255));
		canvasSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		canvasSGC.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		
		// set up the texture image and Graphics2D objects for the tool
		final int imageSize = 512;
		BufferedImage bi = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		ImageData id = new ImageData(bi);
		// this is not pretty: the original bi was trashed to get a different byte order.
		bi = (BufferedImage) id.getImage();
		g2d = bi.createGraphics();
		// make a grey background to start with
		final Color initialColor = new Color(1f, 1f, 1f, 1f);//.5f,.5f,.5f,1f);
		g2d.setColor(initialColor);
		g2d.fillRect(0, 0, imageSize, imageSize);
		
		// set up the texture object
		final Texture2D tex2d = TextureUtility.createTexture(canvasSGC.getAppearance(), POLYGON_SHADER,id);
		tex2d.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
		tex2d.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
 		tex2d.setAnimated(true);
 		// not a good idea to set false: no picture shows up!
 		tex2d.setMipmapMode(true);
 		tex2d.setApplyMode(Texture2D.GL_MODULATE);
// 		tex2d.setPixelFormat(Texture2D.GL_BGRA);
  		final int brushSize = 16;
  		// make a transparent brush using utility method in jogl backend
 		Color brushColor = new Color(0,0,255,70);
 		ImageData bid = ShadedSphereImage.shadedSphereImage(
 				Rn.setToLength(null, new double[]{0,1,1}, 1.0),
 				brushColor, 
 				Color.white, 
 				10.0, 
 				brushSize, 
 				true,
 				new int[]{1,0,3,2});
 		final BufferedImage brush1 = (BufferedImage) bid.getImage();
		final BufferedImage brush2 = new BufferedImage(brushSize, brushSize, BufferedImage.TYPE_INT_ARGB);
		Graphics2D tmpG = (Graphics2D) brush2.getGraphics();
		tmpG.setColor(initialColor);
		tmpG.fillRect(0, 0, brushSize, brushSize);

	    Tool paintTool = new AbstractTool(
	    		InputSlot.LEFT_BUTTON,
	    		InputSlot.SHIFT_LEFT_BUTTON) {	// left button and shift left button

	    	BufferedImage brush;
			public void activate(ToolContext tc) {
				// express interest in mouse moves (so perform() gets called)
		   		addCurrentSlot(InputSlot.getDevice("PointerTransformation"));
		   		if (tc.getSource()== InputSlot.LEFT_BUTTON) {
		   			System.err.println("left mouse activate");
		   			brush = brush1;
		   		} else brush = brush2;
			}

			public void perform(ToolContext tc) {
				PickResult currentPick = tc.getCurrentPick();
				if (currentPick == null) return;
				double[] texCoords = currentPick.getTextureCoordinates();
				// that the following can happen is ... odd
				if (texCoords == null  || texCoords.length < 2) return;
				int ix = (int) (texCoords[0] * imageSize);
				int iy = (int) (texCoords[1] * imageSize);
				g2d.drawImage(brush, ix - brushSize/2, iy - brushSize/2, null);
				// we have to explicitly trigger render since the scene graph isn't changed by the painting
				jrviewer.getViewer().renderAsync();
			}

			public String getDescription(InputSlot slot) {
				return null;
			}

			public String getDescription() {
				return "A tool which paints on a 3D surface";
			}

			@Override
			public void deactivate(ToolContext tc) {
		   		removeCurrentSlot(InputSlot.getDevice("PointerTransformation"));
			}
	    	
	    };
		canvasSGC.addTool(paintTool);		

	}
}
