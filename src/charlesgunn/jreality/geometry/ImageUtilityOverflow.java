package charlesgunn.jreality.geometry;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

public class ImageUtilityOverflow {

	public static BufferedImage convertToRGBA(BufferedImage image) {
	    java.awt.image.Raster raster = image.getRaster();
	    int width = image.getWidth();
	    int height = image.getHeight();
	    java.awt.image.ColorModel colorModel = image.getColorModel();
	    byte[] convertedImage = null;
	    BufferedImage ret;
	    if (colorModel.hasAlpha())
	    {
		  ret = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		  WritableRaster outraster = ret.getRaster();
		  convertedImage = ((DataBufferByte) outraster.getDataBuffer()).getData();
	      int index = 0;
	      for (int y = 0; y < height; y++)
	      {
	        for (int x = 0; x < width; x++)
	        {
	          Object pixel = raster.getDataElements(x, y, null);
	          byte red = (byte)colorModel.getRed(pixel);
	          byte green = (byte)colorModel.getGreen(pixel);
	          byte blue = (byte)colorModel.getBlue(pixel);
	          byte alpha = (byte)colorModel.getAlpha(pixel);
	          convertedImage[index++] = red; //blue;
	          convertedImage[index++] = alpha; //green;
	          convertedImage[index++] = blue; //red;
	          convertedImage[index++] = green; //alpha;
	        }
	      }
	    }
	    else
	    {
	    	ret = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
			WritableRaster outraster = ret.getRaster();
			convertedImage = ((DataBufferByte) outraster.getDataBuffer()).getData();
	      int index = 0;
	      for (int y = 0; y < height; y++)
	      {
	        for (int x = 0; x < width; x++)
	        {
	          Object pixel = raster.getDataElements(x, y, null);
	          byte red = (byte)colorModel.getRed(pixel);
	          byte green = (byte)colorModel.getGreen(pixel);
	          byte blue = (byte)colorModel.getBlue(pixel);
	          convertedImage[index++] = blue;
	          convertedImage[index++] = green;
	          convertedImage[index++] = red;
	        }
	      }
	    }
	    return ret;
	}

}
