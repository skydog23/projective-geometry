/*
 * Author	gunn
 * Created on Aug 3, 2005
 *
 */
package charlesgunn.jreality;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import de.jreality.scene.pick.Graphics3D;
import de.jreality.util.CameraUtility;

public class LevelOfDetailComponentFactory {
	protected LevelOfDetailComponent theComp;
	double levelOfDetail = 1.0;
	protected Graphics3D context;
	int updateInterval = 100;
	Timer theTimer;
	
	private LevelOfDetailComponentFactory(LevelOfDetailComponent c)	{
		super();
		theComp = c;
	}
	
	public static LevelOfDetailComponentFactory getLevelOfDetailComponentFactory(LevelOfDetailComponent c) {
		if (c == null)
			throw new IllegalArgumentException("null arguments");
		
		LevelOfDetailComponentFactory lodf = new LevelOfDetailComponentFactory(c);
		return lodf;
	}
	
	public void activate()	{
		if (theTimer == null) {
			theTimer = new Timer(updateInterval, new ActionListener() {
				public void actionPerformed(ActionEvent ev)	{
					update();
				}
			});
		}
		theTimer.start();
	}
	
	public void deactivate()	{
		if (theTimer != null) theTimer.stop();
	}
	
	protected void update()	{
		if (context == null)	{
			throw new IllegalStateException("No context set");
		}
		double d = CameraUtility.getNDCExtent(context.getObjectToNDC());
		theComp.setScreenExtent(d * levelOfDetail);
	}
		
	public Graphics3D getContext() {
		return context;
	}
	public void setContext(Graphics3D context) {
		if (context == null) throw new IllegalArgumentException("null context");
		this.context = context;
	}
	public double getLevelOfDetail() {
		return levelOfDetail;
	}
	public void setLevelOfDetail(double levelOfDetail) {
		this.levelOfDetail = levelOfDetail;
	}
	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}



}
