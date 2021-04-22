/*
 * Created on Jan 2, 2013
 *
 */
package charlesgunn.jreality;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class TestDrawImage {

	public static void main(String[] args) {
		BufferedImage bi1, bi2;
		int width = 256, height = width;
		int count = 10000;
		bi1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		bi2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) bi1.getGraphics();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
		for (int j = 0; j<20; ++j)	{
			long time1 = System.currentTimeMillis();
			for (int i = 0; i<count; ++i)	{
				g2.drawImage(bi2, 0, 0, null);
			}			
			long time2 = System.currentTimeMillis();
			long diff = time2-time1;
			double dt = diff/1000.0;
			System.err.println("Time elapsed = "+dt);
		}
	}
}
