/*
 * Author	gunn
 * Created on Aug 15, 2005
 *
 */
package charlesgunn.jreality.texture;

import java.awt.Color;

import charlesgunn.anim.util.AnimationUtility;
import de.jreality.scene.Appearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;

public class RopeTextureFactory extends TextureFactory {

	double bandwidth = .6,
	shadowwidth = .18,
	blendfactor = 0.0;
	Color band1color = new Color(1f, 1f, 1f),
		band2color = new Color(.7f, .2f, .5f),
		blendcolor = Color.white,
		shadowcolor = new Color(0,0,0,255), 
		gapcolor = new Color(0,0,0,0);
	
	public RopeTextureFactory(Appearance ap)	{
		super(ap);
	}
	
	public Color getBand1color() {
		return band1color;
	}
	public void setBand1color(Color band1color) {
		this.band1color = band1color;
	}
	public Color getBand2color() {
		return band2color;
	}
	public void setBand2color(Color band2color) {
		this.band2color = band2color;
	}
	public double getBandwidth() {
		return bandwidth;
	}
	public void setBandwidth(double bandwidth) {
		this.bandwidth = bandwidth;
	}
	public Color getBlendcolor() {
		return blendcolor;
	}
	public void setBlendcolor(Color blendcolor) {
		this.blendcolor = blendcolor;
	}
	public double getBlendfactor() {
		return blendfactor;
	}
	public void setBlendfactor(double blendfactor) {
		this.blendfactor = blendfactor;
	}
	public Color getGapcolor() {
		return gapcolor;
	}
	public void setGapcolor(Color gapcolor) {
		this.gapcolor = gapcolor;
	}
	public Color getShadowcolor() {
		return shadowcolor;
	}
	public void setShadowcolor(Color shadowcolor) {
		this.shadowcolor = shadowcolor;
	}
	public double getShadowwidth() {
		return shadowwidth;
	}
	public void setShadowwidth(double shadowwidth) {
		this.shadowwidth = shadowwidth;
	}
	public void update()	{
		super.update();
		ImageData it = createImageData();
        tex2d.setImage(it);
        tex2d.setRepeatS(Texture2D.GL_REPEAT);
        tex2d.setRepeatT(Texture2D.GL_REPEAT);
        tex2d.setApplyMode(Texture2D.GL_COMBINE);
        tex2d.setCombineModeColor(Texture2D.GL_MODULATE); //INTERPOLATE);
        // a blendfactor of 1 gives pure texture; of 0, pure  diffuse color
//        tex2d.setBlendColor(new Color(0f,0f,0f,(float) blendfactor));
//        return tex2d;
     }
	private ImageData createImageData() {
		int size = 256;
		int ibandwidth = (int) ((size * bandwidth)/2);
		int shwd = (int) ((size/2)*shadowwidth);
		int onewidth = size/2;
		int iband, jband, imod, jmod;
		int which = 0;
		byte[] im = new byte[size*size* 4];
		byte[][] colors = {{(byte)0x0,(byte)0x0,(byte)0x0,(byte)0x0},	// gap
		{(byte)200,(byte)200,(byte)200,(byte)0xff},						// band1
		{(byte)255,(byte)255,(byte)255,(byte)255},						// band2
		{(byte)0,(byte)0, (byte) 0, (byte) 255}};						// shadow
		colors[0] = convertToByte(gapcolor, blendcolor, blendfactor);
		colors[1] = convertToByte(band1color, blendcolor, blendfactor);
		colors[2] = convertToByte(band2color, blendcolor, blendfactor);
		colors[3] = convertToByte(shadowcolor, blendcolor, blendfactor);
		Color result;
		double mixfactor = 0.0;
		for (int i = 0; i<size; ++i)	{
	        iband = i/onewidth;
	        imod = i%onewidth;
	        for (int j = 0; j< size; ++j)	{
	        	int where = 4*(i*size+j);
	        	jband = j /onewidth;
	        	jmod = j%onewidth;
	        	int q = 2*(iband)+jband;
	        	if (imod >= ibandwidth && jmod >= ibandwidth) which = 0;
	        	else {
	        		if (imod <= ibandwidth && jmod <= ibandwidth)	{
//	        		if (imod <= (onewidth - shwd) && jmod <= (onewidth - shwd))	{
	        			if (q == 0 || q == 3) which = 1;
	        			else which = 2;
	        		} else if (jmod > ibandwidth) {
//	        		} else if (jmod > (onewidth - shwd)) {
						which = 1;
						if ((q == 0 || q == 3) && jmod > (onewidth - shwd))	{
							which = 3;
							mixfactor = AnimationUtility.hermiteInterpolation(jmod, onewidth-shwd, onewidth, 0, 1);
							result = AnimationUtility.linearInterpolation(band1color, shadowcolor, mixfactor);
							colors[which] = convertToByte(result, blendcolor, blendfactor);
						}
						else if ((q == 1 || q == 2) && jmod < (ibandwidth + shwd)){
							which = 3;
							mixfactor = AnimationUtility.hermiteInterpolation(jmod, ibandwidth, ibandwidth+shwd, 0, 1);
							result = AnimationUtility.linearInterpolation( shadowcolor,band1color, mixfactor);
							colors[which] = convertToByte(result, blendcolor, blendfactor);
						}
					} else if (imod > ibandwidth) {
//					} else if (imod > (onewidth - shwd)) {
						which = 2;
						if ((q == 1 || q == 2) && imod > (onewidth - shwd)){
							which = 3;
							mixfactor = AnimationUtility.hermiteInterpolation(imod, onewidth-shwd, onewidth, 0, 1);
							result = AnimationUtility.linearInterpolation(band2color, shadowcolor, mixfactor);
							colors[which] = convertToByte(result, blendcolor, blendfactor);
						}
						else if ((q == 0 || q == 3) && imod < (ibandwidth + shwd))
						{
							which = 3;
							mixfactor = AnimationUtility.hermiteInterpolation(imod, ibandwidth, ibandwidth+shwd, 0, 1);
							result = AnimationUtility.linearInterpolation(shadowcolor, band2color, mixfactor);
							colors[which] = convertToByte(result, blendcolor, blendfactor);
						}					}
		}
		System.arraycopy(colors[which],0,im,where,4);
		}
	    }
	    ImageData it = new ImageData(im,size,size);
		return it;
	}
	public static byte[] convertToByte(Color c, Color bl, double d)	{
		float[] x = new float[4];
		c.getRGBComponents(x);
		float[] y = new float[4];
		bl.getRGBComponents(y);
		int n = x.length;
		byte[] ret = new byte[n];
		for (int i = 0; i<n; ++i) ret[i] = (byte) ( ((1-d)* x[i] + d*y[i])*255.0);
		return ret;
	}

}
