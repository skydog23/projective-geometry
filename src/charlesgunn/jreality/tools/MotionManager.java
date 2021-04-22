/*
 * Created on Mar 24, 2004
 *
 */
package charlesgunn.jreality.tools;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.WeakHashMap;

import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.Viewer;
import de.jreality.ui.viewerapp.ViewerSwitch;
import de.jreality.util.LoggingSystem;


/**
 * @author gunn
 *
 */
public class MotionManager {
	List<ContinuedMotion> motions = new Vector<ContinuedMotion>();			// list of ContinuedMotion instances
	boolean allowMotion = true;
	public boolean oneAtATime = false;
	boolean oneMotionPerTarget=true;
	ContinuedMotion currentMotion = null;
	
	static WeakHashMap globalTable = new WeakHashMap();
	public static MotionManager motionManagerForViewer(Viewer v)	{
		if (v instanceof ViewerSwitch)	{
			v = ((ViewerSwitch)v).getCurrentViewer();
		}
		MotionManager sm = (MotionManager) globalTable.get(v);
		if (sm != null) {
			LoggingSystem.getLogger(MotionManager.class).finer("MM for viewer: "+v+" is "+sm);
			return sm;
		}
		sm = new MotionManager();
		globalTable.put(v,sm);
//		System.err.println("MM for viewer: "+v+" is "+sm);
		return sm;		
	}
	public List getMotions() {
		return motions;
	}

	public void addMotion(ContinuedMotion m)	{
		if (!allowMotion) return;
	  synchronized(motions){
		if (m == null ) return;
		if (oneAtATime) {
			motions.clear();
			if (currentMotion != null && currentMotion.isRunning()) currentMotion.stop();
		}
		//
		if (oneMotionPerTarget)	{
			Object tar = m.target;
			Iterator it = motions.iterator();
			Object dup = null;
			while (it.hasNext()) {
				ContinuedMotion mm = (ContinuedMotion) it.next();
				if (mm.target == tar) {
					mm.stop();
					dup = (mm);
				}
			}
			if (dup != null) motions.remove(dup);
			}
		motions.add(m);
		if (allowMotion)	m.start();
		currentMotion = m;
	  }
	}
	
	public void removeMotion(ContinuedMotion m)	{
		synchronized(motions){
//			System.out.println("Removing motion ");
			if (m.isRunning()) m.stop();
			motions.remove(m);
			if (currentMotion == m) currentMotion = null;
		}
	}
	
	public void pauseMotions()	{
		stopMotions();
	}
	
	public void stopMotions()	{
		Iterator it = motions.iterator();
		while(it.hasNext())	{
			ContinuedMotion m = (ContinuedMotion) it.next();
			if (m.isRunning()) m.stop();
		}	
	}
	
	public void resumeMotions()	{
		if (!allowMotion) return;
		Iterator it = motions.iterator();
		while(it.hasNext())	{
			ContinuedMotion m = (ContinuedMotion) it.next();
			if (!m.isRunning()) m.restart();
		}
	}
	
	public void clearMotions()	{
		synchronized (motions) {
			stopMotions();
			motions.clear();
		}
	}

	public void pauseMotionsFor(SceneGraphNode theEditedNode) {
		Iterator it = motions.iterator();
		while(it.hasNext())	{
			ContinuedMotion m = (ContinuedMotion) it.next();
			if (m.target == theEditedNode) m.stop();
		}	
	}
	
	public void resumeMotionsFor(SceneGraphNode theEditedNode) {
		if (!allowMotion) return;
		Iterator it = motions.iterator();
		while(it.hasNext())	{
			ContinuedMotion m = (ContinuedMotion) it.next();
			if ( !m.isRunning() && m.target == theEditedNode) m.restart();
		}	
	}
	public boolean isAllowMotion() {
		return allowMotion;
	}
	public void setAllowMotion(boolean allowMotion) {
		this.allowMotion = allowMotion;
	}
}
