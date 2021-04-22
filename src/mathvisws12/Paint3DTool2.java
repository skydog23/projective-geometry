package mathvisws12;

import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.JColorChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import charlesgunn.anim.util.AnimationUtility;
import de.jreality.jogl.shader.ShadedSphereImage;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jtem.beans.InspectorPanel;

public class Paint3DTool2 extends AbstractTool {

	protected int imageSize = 1024;
	protected int brushSize = 32;
	protected int n = 3, m = 5;
	protected double alpha = .3;
	final Color initialColor = new Color(1f, 1f, 1f, 1f);//.5f,.5f,.5f,1f);
	transient boolean lighting = false;
	transient Graphics2D g2d ;
	transient JRViewer jrv;
	public Paint3DTool2(Appearance ap, JRViewer jrviewer)	{
    		super(InputSlot.LEFT_BUTTON,InputSlot.SHIFT_LEFT_BUTTON);
    		jrv=jrviewer;
		textureImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		ImageData id = new ImageData(textureImage);
		// this is not pretty: the original bi was trashed to get a different byte order.
		textureImage = (BufferedImage) id.getImage();
		initTexture();
		
		// set up the texture object
		final Texture2D tex2d = TextureUtility.createTexture(ap, POLYGON_SHADER,id);
		tex2d.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
		tex2d.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
		tex2d.setAnimated(true);
 		// not a good idea to set false: no picture shows up!
 		tex2d.setMipmapMode(true);
 		tex2d.setApplyMode(Texture2D.GL_MODULATE);
 		brushColor = new Color(0,0,255,((int) (alpha*255)));
 		updateBrush();
    }


	private void initTexture() {
		g2d = textureImage.createGraphics();
		// make a grey background to start with
		g2d.setColor(initialColor);
		g2d.fillRect(0, 0, imageSize, imageSize);
		g2d.setColor(lineColor);
		g2d.drawLine(0, 0, imageSize-1, 0);
		g2d.drawLine(0,0,0,imageSize-1);
	}


	private void updateBrush() {
		brushColor = new Color(brushColor.getRed(), brushColor.getGreen(), brushColor.getBlue(), ((int) (255*alpha)));
		ImageData bid = ShadedSphereImage.shadedSphereImage(
 				Rn.setToLength(null, new double[]{0,1,1}, 1.0),
 				brushColor, 
 				Color.white, 
 				10.0, 
 				brushSize, 
 				lighting,
 				new int[]{1,0,3,2});
 		brush1 = (BufferedImage) bid.getImage();
		brush2 = new BufferedImage(brushSize, brushSize, BufferedImage.TYPE_INT_ARGB);
		Graphics2D tmpG = (Graphics2D) brush2.getGraphics();
		tmpG.setColor(initialColor);
		tmpG.fillRect(0, 0, brushSize, brushSize);
	}
    
  		private BufferedImage brush;
		private BufferedImage brush1;
		private BufferedImage brush2;
		private double[] startP;
		public void activate(ToolContext tc) {
			// express interest in mouse moves (so perform() gets called)
	   		addCurrentSlot(InputSlot.getDevice("PointerTransformation"));
	   		if (tc.getSource()== InputSlot.LEFT_BUTTON) {
	   			System.err.println("left mouse activate");
	   			brush = brush1;
	   		} else brush = brush2;
			PickResult currentPick = tc.getCurrentPick();
			if (currentPick == null) return;
			startP = currentP = currentPick.getTextureCoordinates().clone();
		}

		double[] currentP;
		public void perform(ToolContext tc) {
			PickResult currentPick = tc.getCurrentPick();
			if (currentPick == null) return;
			currentP = currentPick.getTextureCoordinates();
			// that the following can happen is ... odd
//			if (currentP == null  || currentP.length < 2) return;
//			int ix = (int) (currentP[0] * imageSize);
//			int iy = (int) (currentP[1] * imageSize);
//			g2d.drawImage(brush, ix - brushSize/2, iy - brushSize/2, null);
			// we have to explicitly trigger render since the scene graph isn't changed by the painting
//			jrv.getViewer().renderAsync();
		}

		double density = .5;
		private Color lineColor = Color.black;
		private BufferedImage textureImage;
		private Color brushColor;
		@Override
		public void deactivate(ToolContext tc) {
	   		removeCurrentSlot(InputSlot.getDevice("PointerTransformation"));
	   		// if shift was down, reset the texture
	   		if (brush == brush2)	initTexture();
	   		// draw a bresenham line from downP to currentP
	   		else myDrawLine(startP, currentP);
			// we have to explicitly trigger render since the scene graph isn't changed by the painting
			jrv.getViewer().renderAsync();

		}
    	

		public void myDrawLine(double[] startP, double[] currentP) {
			currentP = closestRepresentative(startP, currentP);
			currentP[0] += n;
			currentP[1] += m;
	   		double[] diff = Rn.subtract(null, currentP, startP);
	   		double ll = Rn.euclideanNorm(diff);
	   		int steps = (int) (density* imageSize* ll);
	   		double delta = steps < 1 ? 1.0: 1.0/(steps);
	   		g2d.setColor(lineColor);
	   		for (int i = 0; i<=steps; ++i)	{
	   			double f = i*delta;
	   			double[] interpP = AnimationUtility.linearInterpolation(null, f, 0, 1, startP, currentP);
	   			interpP = intoFundamentalDomain(interpP);
				int ix = (int) (interpP[0] * imageSize);
				int iy = (int) (interpP[1] * imageSize);
				boolean left = ix < brushSize/2, 
						right = ix > imageSize - brushSize/2, 
						down = iy < brushSize/2,
						up = iy > imageSize - brushSize/2;
				g2d.drawImage(brush, ix - brushSize/2, iy - brushSize/2, null);
				if (left)	{				
					if (down)
						g2d.drawImage(brush, ix + imageSize - brushSize/2, iy  + imageSize - brushSize/2, null);
					else if (up) 
						g2d.drawImage(brush, ix + imageSize - brushSize/2, iy  - imageSize - brushSize/2, null);
					else
						g2d.drawImage(brush, ix + imageSize - brushSize/2, iy - brushSize/2, null);
				}
				else if (right)	{				
					if (down)
						g2d.drawImage(brush, ix - imageSize - brushSize/2, iy  + imageSize - brushSize/2, null);
					else if (up) 
						g2d.drawImage(brush, ix - imageSize - brushSize/2, iy  - imageSize - brushSize/2, null);
					else
						g2d.drawImage(brush, ix - imageSize - brushSize/2, iy - brushSize/2, null);
				}
				if (down)					
					g2d.drawImage(brush, ix - brushSize/2, iy  + imageSize - brushSize/2, null);
				else if (up)					
					g2d.drawImage(brush, ix - brushSize/2, iy - imageSize - brushSize/2, null);

	   		}
		}


		private double[] intoFundamentalDomain(double[] interpP) {
			double[] ret = interpP.clone();
			while (ret[0] < 0) ret[0] += 1;
			while (ret[0] >= 1) ret[0] -= 1;
			while (ret[1] < 0) ret[1] += 1;
			while (ret[1] >= 1) ret[1] -= 1;
			return ret;
		}


		private double[] closestRepresentative(double[] P, double[] Q) {
			double dist = 100.0;
			double[] QQ = new double[P.length], min = Q.clone();
			for (int i = -1; i<=1; ++i)	{
				for (int j = -1; j<= 1; ++j)	{
					QQ[0] = Q[0] + i;
					QQ[1] = Q[1] + j;
					double d = Rn.euclideanDistance(QQ, P);
					if (d < dist) {
						dist = d;
						min = QQ.clone();
					}
				}
			}
			return min;
		}


		public String getDescription(InputSlot slot) {
			return null;
		}

		public String getDescription() {
			return "A tool which paints on a 3D surface";
		}

		public Component getInspector()	{
			Box vbox = Box.createVerticalBox();
			final JColorChooser jcc = new JColorChooser(lineColor);
			jcc.setPreviewPanel(null);
			jcc.getSelectionModel().addChangeListener( new ChangeListener() {

				public void stateChanged(ChangeEvent arg0) {
					brushColor = jcc.getColor();
					updateBrush();
				}

			});
			vbox.add(jcc);
			InspectorPanel ip = new InspectorPanel(true);
			ip.setObject(this);
			vbox.add(ip);
			return vbox;
		}


		public int getBrushSize() {
			return brushSize;
		}


		public void setBrushSize(int brushSize) {
			this.brushSize = brushSize;
			updateBrush();
		}


		public int getN() {
			return n;
		}


		public void setN(int n) {
			this.n = n;
		}


		public int getM() {
			return m;
		}


		public void setM(int m) {
			this.m = m;
		}


		public double getDensity() {
			return density;
		}


		public void setDensity(double density) {
			this.density = density;
		}


		public double getAlpha() {
			return alpha;
		}


		public void setAlpha(double alpha) {
			this.alpha = alpha;
		}

}
