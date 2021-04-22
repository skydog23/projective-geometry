package charlesgunn.jreality.texture;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import charlesgunn.anim.util.AnimationUtility;
import de.jreality.scene.Appearance;
import de.jreality.shader.ImageData;

public class SimpleTextureFactory {
	public enum TextureType {
		WEAVE,
		GRAPH_PAPER,
		DISK,
		ANTI_DISK,
		SPHERE,
		CHECKERBOARD,
		STRIPES,
		LINE,
		GRADIENT,
		RING
	};
	TextureType type = TextureType.WEAVE;
	int size = 64;
	int[] channels = {0,1,2,3};
	transient Appearance appearance;
	transient int textureID = 0;
	transient ImageData imageData;
	transient boolean opaqueTexture = false;
	
	public SimpleTextureFactory() {
		super();
		updatebcolors();
	}
	
	public void setType(TextureType foo)	{
		type = foo;
	}
	
	public ImageData getImageData()	{
		return imageData;
	}
	
	Color[] colors = {Color.white, Color.LIGHT_GRAY, Color.yellow, new Color(0,0,0,0)};
	byte[][] bcolors = { {(byte)255,(byte)255,(byte)255,(byte)255}, //{(byte)0x0,(byte)0x0,(byte)0x0,(byte)0x0},
			{(byte)200,(byte)200,(byte)200,(byte)0xff},
			{(byte)255,(byte)255,(byte)255,(byte)255},
			{(byte)0,(byte)0, (byte) 0, (byte) 255}};

	public void setColors(Color[] colors) {
		this.colors = colors;
	}

	protected Color color0, color1, color2, color3;
	
	public Color getColor0() {
		return colors[0];
	}

	public void setColor0(Color color0) {
		setColor(0, color0);
	}

	public Color getColor1() {
		return colors[1];
	}

	public void setColor1(Color color1) {
		setColor(1, color1);
	}

	public Color getColor2() {
		return colors[2];
	}

	public void setColor2(Color color2) {
		setColor(2, color2);
	}

	public Color getColor3() {
		return colors[3];
	}

	public void setColor3(Color color3) {
		setColor(3, color3);
	}

	public void setColor(int i, Color c)	{
		colors[i] = c;
//		System.err.println("setting color.a "+i+" to "+c.getAlpha());
		updatebcolors();
	}
	
	double[] params = {.5,1};
	public void setParams(double[] p)	{
		params = p;
	}
	
	int[] indices = {0,1};
	public void setIndices(int[] i)	{
		indices = i;
	}
	private void updatebcolors() {
		float[] cc = new float[4];
		for (int i = 0; i<colors.length; ++i)	{
			cc = colors[i].getRGBComponents(cc);
			for (int j = 0; j<4; ++j)	bcolors[i][j] = (byte) (cc[channels[j]] * 255.0);
		}
	}

	byte[] im;
	BufferedImage image = null;
	public void update()	{
		if (im == null || im.length != size*size*4) {
			im = new byte[size*size* 4];
			imageData = new ImageData(im, size, size);
//			if (type == TextureType.SPHERE)  return;
		    image = (BufferedImage) imageData.getImage();
		}
		im = new byte[size*size* 4];
//		System.err.println("in stf update");
		switch(type)	{
		case ANTI_DISK:
//		    for (int i = 0; i<size; ++i)	{
//		        for (int j = 0; j< size; ++j)	{
//					int I = 4*(i*size+j);
//					int sq = (i-(size/2))*(i-(size/2)) + (j-(size/2))*(j-(size/2));
//					sq = i*i + j*j;
//					if (sq <= size*size)	
//						{im[I] =  im[I+1] = im[I+2] = im[I+3] = (byte) 255; }
//					else
//						{im[I] =  im[I+1] = im[I+2] = im[I+3]  = 0;  }
//			    }
//			}
//			break;
		case DISK:
		{
			int index1 = type == TextureType.DISK ? 0 : 3;
			int index2 = type == TextureType.DISK ? 3 : 0;
			int shrunk = size/2-1; //size/2; //
			int size2 = (size*size)/4;
			int shrunk2 = shrunk*shrunk;
			byte[] fringe = new byte[4];
		    for (int i = 0; i<size; ++i)	{
		        for (int j = 0; j< size; ++j)	{
					int I = 4*(i*size+j);
					int i2 = i - (size/2), j2 = j - size/2;
					int sq = i2*i2 + j2*j2;
					//sq = i*i + j*j;
					if (sq > shrunk2)	{
						if (sq <= size2) {
							double f = (Math.sqrt(sq)-shrunk)/(size/2.0 - shrunk);
//							System.err.println("f = "+f);
							for (int k = 0; k<4; ++k) fringe[k] = (byte) (f * bcolors[3][k]);
							System.arraycopy(fringe,0,im,I,4);
						} else 
							System.arraycopy(bcolors[index2],0,im,I,4);
					}
						//{im[I] =  im[I+1] = im[I+2] = im[I+3] = (byte) 255; }
					else
						System.arraycopy(bcolors[index1],0,im,I,4);
//					im[I+3] = (byte) (255 - im[I+3]);
			    }
			}
		}
			break;
		case RING:
		{
			double repeat = 2.8;
			int size2 = (size*size)/4;
			float[] bcf = new float[4];
			byte[] fringe = new byte[4];
		    for (int i = 0; i<size; ++i)	{
		        for (int j = 0; j< size; ++j)	{
					int I = 4*(i*size+j);
					int i2 = i - (size/2), j2 = j - size/2;
					int sq = i2*i2 + j2*j2;
					for (int k=0; k<4; ++k) im[I+k] = (byte) (255 * bcf[k]);
						if (sq < size2) {
							double f = Math.sqrt(sq)/(size/2.0);
							double intensity = Math.sin(Math.PI*2*f*repeat);
							intensity = AnimationUtility.linearInterpolation(intensity, -.5, .5, 0, 1);
							Color bc = AnimationUtility.linearInterpolation(colors[0], colors[3], 1-intensity);
							bc.getRGBComponents(bcf);
							for (int k = 0; k<4; ++k) fringe[k] = (byte) (255 * bcf[k]);
							System.arraycopy(fringe,0,im,I,4);
						} else 
							System.arraycopy(bcolors[3],0,im,I,4);
			    }
			}
		}
			break;
		case WEAVE:
			int bandwidth = 16;
			int shwd = 2;
			int onewidth = 32;
			int iband, jband, imod, jmod;
			int which = 0;
		    for (int i = 0; i<size; ++i)	{
		        iband = i/onewidth;
		        imod = i%onewidth;
		        for (int j = 0; j< size; ++j)	{
		        	int where = 4*(i*size+j);
					jband = j /onewidth;
					jmod = j%onewidth;
					int q = 2*(iband)+jband;
					if (imod > bandwidth && jmod > bandwidth) which = 0;
					else {
					    if (imod <= bandwidth && jmod <= bandwidth)	{
					        if (q == 0 || q == 3) which = 1;
					        else which = 2;
					    } else if (jmod > bandwidth) {
					        which = 1;
					        if ((q == 0 || q == 3)&& jmod > (onewidth - shwd)) which = 3;
					        if ((q == 1 || q == 2) && jmod < (bandwidth + shwd)) which = 3;
					    } else if (imod > bandwidth) {
				 	        which = 2;
					        if ((q == 1 || q == 2)&& imod > (onewidth - shwd)) which = 3;
					        if ((q == 0 || q ==3) && imod < (bandwidth + shwd)) which = 3;
					    }
					}
					System.arraycopy(bcolors[which],0,im,where,4);
				}
		    }
			break;
		case GRAPH_PAPER:
			int bands = 4;
			int factor = size/64;
			int[] widths = {factor*4,factor*2,factor*2,factor*2};
			int halfwidth = widths[0]/2;
			onewidth = size/bands;
		    for (int i = 0; i<size; ++i)	{
		        iband = i/onewidth;
		        imod = i%onewidth;
		        for (int j = 0; j< size; ++j)	{
//					int where = 4*(((i+size-halfwidth)%size)*size+((j+size-halfwidth)%size));
					int where = 4*(((i+size)%size)*size+((j+size-halfwidth)%size));
					jband = j /onewidth;
					jmod = j%onewidth;
					which = 0;
					if (jmod <= widths[jband]) {
					    if (jband == 0) which = 1;
					    else which = 2;
					} 
					if (which != 1 && imod <=widths[iband]) {
					    if (iband == 0) which = 1;
					    else which = 2;
					}
					System.arraycopy(bcolors[which],0,im,where,4);
				}
		    }			
		    break;
		case SPHERE:
			imageData = de.jreality.jogl.shader.ShadedSphereImage.shadedSphereImage(new double[]{.05, .15,1}, colors[0], colors[1], 60.0, size, true, channels);
			break;
//	
		case CHECKERBOARD:
			for (int i = 0; i<size; ++i)	{
				for (int j = 0; j<size; ++j)	{
					int where = 4*(i*size+j);
					which = ((i<size/2 && j < size/2) || (i>=size/2 && j >= size/2)) ? 0 : 1;
					System.arraycopy(bcolors[which],0,im,where,4);
				}
			}
			break;
		case STRIPES:
			for (int i = 0; i<size; ++i)	{
				which = 0;
				for (int k = 0; k<params.length; ++k)	{
					if ( i < ((int) (params[k]*size))) {
						which = k;
						break;
					}
				}
				for (int j = 0; j<size; ++j)	{
					int where = 4*(i*size+j);
					System.arraycopy(bcolors[indices[which]],0,im,where,4);
				}
			}
			break;
		case LINE:
			int width = 5; //7;//size/8;
			int[] alphas = {100, 200, 255, 200, 100}; //{100,200,255,255,255,200,100};
			float alpha0 = (float) (colors[0].getAlpha()/255.0),
				alpha2 = (float) (colors[2].getAlpha()/255.0);
			int thicker = colors[0].getAlpha() > colors[2].getAlpha() ? 0 : 2;
			Color blendc = AnimationUtility.linearInterpolation(colors[2-thicker], colors[thicker], colors[thicker].getAlpha()/255.0);
			float ohmygod[] = blendc.getRGBComponents(new float[4]);
			byte[] blendcb = new byte[4];
			for (int j = 0; j<4; ++j)	blendcb[j] = (byte) (ohmygod[channels[j]] * 255.0);
			for (int i = 0; i<size; ++i)	{
		    		if (i < width) {
		    			bcolors[0][3] = (byte) ( alpha0 * alphas[i]);
		    		}
		    		int index = (i+size-2) % size;
		        for (int j = 0; j< size; ++j)	{
		        		int jndex = (j+size-2)%size;
		        		int where = 4*(index*size+jndex);
					if (i < width &&  j < width)	{
						int k = thicker == 2 ? j : i;
						blendcb[3] = (byte) ((colors[thicker].getAlpha()/255.0) * alphas[k]);
						System.arraycopy(blendcb,0,im,where,4);
					}
					else if (i < width) {
						System.arraycopy(bcolors[0],0,im,where,4);
					}
					else if (j < width) {
						bcolors[2][3] = (byte) (alpha2 * alphas[j]);
						System.arraycopy(bcolors[2],0,im,where,4);
					}
					else 
						System.arraycopy(bcolors[3],0,im,where,4);
//					im[where+3] = (byte) 255;
		        }
		    }			
		    break;
		case GRADIENT:
			double blend = 0.0;
			int k1 = 0, k2 = size-k1;
			float[] bcf = new float[4];
			for (int i = 0; i<size; ++i)	{
				for (int j = 0; j< size; ++j)	{
					int I = 4*(j*size+i);
					if (j <= k1 ) { blend = 1.0; }
					else if (j >= k2) { blend = 0.0; }
					else {
						blend = 1.0-(1.0*(j-k1))/(k2-k1);
					}
					Color bc = AnimationUtility.linearInterpolation(colors[0], colors[1], blend);
//					System.err.println("color = "+bc);
					bc.getRGBComponents(bcf);
				for (int k=0; k<4; ++k) im[I+k] = (byte) (255 * bcf[k]);
				}
			}
			break;
		}
		
		if (type != TextureType.SPHERE)  {
			if (opaqueTexture)	{
				for (int i = 0; i<size*size; ++i)	{
					im[4*i+3] = (byte) 255;
				}
			}
			imageData = new ImageData(im, size, size);
		}
 		if (appearance != null)	{
 			if (textureID == 0) appearance.setAttribute("polygonShader.texture2d:image", imageData);
 			else appearance.setAttribute("polygonShader.texture2d["+textureID+"]:image", imageData);
 			System.err.println("stf: setting new image");
 		}
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int[] getChannels() {
		return channels;
	}

	public void setChannels(int[] channels) {
		this.channels = channels;
		updatebcolors();
	}

	public static Image getScaledImage(Image srcImg, int w, int h){
	    BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	    Graphics2D g2 = resizedImg.createGraphics();
	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2.drawImage(srcImg, 0, 0, w, h, null);
	    g2.dispose();
	    return resizedImg;
	}

	public Appearance getAppearance() {
		return appearance;
	}

	public void setAppearance(Appearance appearance) {
		this.appearance = appearance;
	}

	public int getTextureID() {
		return textureID;
	}

	public void setTextureID(int textureID) {
		this.textureID = textureID;
	}

	public boolean isOpaqueTexture() {
		return opaqueTexture;
	}

	public void setOpaqueTexture(boolean opaqueTexture) {
		this.opaqueTexture = opaqueTexture;
	}
}
