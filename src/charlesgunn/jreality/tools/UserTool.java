/*
 * Created on Nov 9, 2004
 *
  */
package charlesgunn.jreality.tools;

import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;

import de.jreality.jogl.plugin.HelpOverlay;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;

/**
 * @author gunn
 *
 */
public class UserTool extends MouseTool {

	protected double[] pointNDC = new double[4];
	protected PickResult pickResult;
	public UserTool(InputSlot... activationSlots) {
		super(activationSlots);
		listeners = new Vector();	
		System.err.println("Adding pointndc");
//		addCurrentSlot(InputSlot.getDevice("PointerNDC"));
	}
	
	public UserTool()	{
		super();
		listeners = new Vector();		
	}
	
	public void activate(ToolContext tc) {
		super.activate(tc);
		pointNDC = new double[] {currentNDC[0], currentNDC[1], 0.0, 1.0};
		pickResult = tc.getCurrentPick();
		if (!listeners.isEmpty())	{
			for (int i = 0; i<listeners.size(); ++i)	{
				UserToolInterface l = (UserToolInterface) listeners.get(i);
				l.startTracking(this);
			}
		}
	}
	
	public void perform(ToolContext tc) {
		super.perform(tc);			
		pointNDC = new double[] {currentNDC[0], currentNDC[1], 0.0, 1.0};
		pickResult = tc.getCurrentPick();
		//JOGLConfiguration.theLog.log(Level.FINE,"Object coordinates: "+Rn.toString(newPickPoint.getPointObject()));
		if (!listeners.isEmpty())	{
			for (int i = 0; i<listeners.size(); ++i)	{
				UserToolInterface l = (UserToolInterface) listeners.get(i);
				l.track(this);
			}
		}
	}
	
	public void deactivate(ToolContext tc)		{
		pickResult =  tc.getCurrentPick();
		if (!listeners.isEmpty())	{
			for (int i = 0; i<listeners.size(); ++i)	{
				UserToolInterface l = (UserToolInterface) listeners.get(i);
				l.endTracking(this);
			}
		}
	}
	
	public PickResult getPickPoint()	{
		return pickResult;
	}
	
	public double[] getPointNDC()	{
		//if (newPickPoint == null) 
			return  pointNDC;
		//return newPickPoint.getPointNDC();
	}
	public int getButton() {
		return button;
	}
	
	List listeners;
	public void addListener(UserToolInterface l)	{
		listeners.add(l);
	}
	
	public void removeListener(UserToolInterface l)	{
		listeners.remove(l);
	}
	
	public void registerHelp(HelpOverlay overlay) 	{	
		if (!listeners.isEmpty())	{
			for (int i = 0; i<listeners.size(); ++i)	{
				overlay.registerInfoString("User tool ", ""+i);
				UserToolInterface l = (UserToolInterface) listeners.get(i);
				l.registerHelp(overlay);
			}
		}
	}
	public String getName() {
		return "user tool";
	}
	public ImageIcon getIcon(int size) {
		return null;
	}



}
