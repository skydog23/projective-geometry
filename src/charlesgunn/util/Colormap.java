/*
 * Created on Aug 5, 2010
 *
 */
package charlesgunn.util;

import java.awt.Color;

import charlesgunn.anim.core.KeyFrameAnimatedColor;
import charlesgunn.anim.core.KeyFrameAnimatedDelegate;
import charlesgunn.anim.core.TimeDescriptor;

public class Colormap extends KeyFrameAnimatedColor {

	Color currentColor;
	
	public Colormap(KeyFrameAnimatedDelegate<Color> ad) {
		super(ad);
	}

	public  Colormap()  {
		super(Color.white);
		setDelegate(new KeyFrameAnimatedDelegate<Color>() {

			public Color gatherCurrentValue(Color t) {
				return currentColor;
			}

			public void propagateCurrentValue(Color t) {
				currentColor = t;
			}
			
		});
	}
	
	public void addKeyFrame(double t, Color c)	{
		setCurrentValue(c);
		currentColor = c;
		addKeyFrame(new TimeDescriptor(t));
	}
	
	public Color getValueAtTime(double t)	{
		setValueAtTime(t);
		return currentValue;
	}
	
	public Color getCurrentValue() { return currentValue; }
}
