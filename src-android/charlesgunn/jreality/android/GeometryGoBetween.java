package charlesgunn.jreality.android;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.logging.Level;

import javax.swing.Timer;

import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;

public class GeometryGoBetween {

	AndroidRenderer jr;
	Timer followTimer;
	boolean geometryRemoved = false;
	boolean checkMemoryLeak = false;
	
	protected GeometryGoBetween(AndroidRenderer jr)	{
		this.jr = jr;
		followTimer = new Timer(1000, new ActionListener()	{
			public void actionPerformed(ActionEvent e) {updateGeometryHashtable(); } } );
		if (checkMemoryLeak) followTimer.start();
	}
	
	public void dispose()	{
		followTimer.setRepeats(false);
		followTimer.stop();
		followTimer = null;
	}
	
	int geomDiff = 0;
	protected HashMap<Geometry, AndroidPeerGeometry> geometries = new HashMap<Geometry, AndroidPeerGeometry>();
	protected void updateGeometryHashtable() {
		if (!geometryRemoved) return;
		final WeakHashMap<Geometry, AndroidPeerGeometry> newG = new WeakHashMap<Geometry, AndroidPeerGeometry>();
		SceneGraphVisitor cleanup = new SceneGraphVisitor()	{
			public void visit(SceneGraphComponent c) {
				if (c.getGeometry() != null) {
					Geometry wawa = c.getGeometry();
					AndroidPeerGeometry peer = geometries.get(wawa);
					newG.put(wawa, peer);
				}
				c.childrenAccept(this);
			}
		};
		cleanup.visit(jr.theRoot);
		geometryRemoved = false;
		//TODO dispose of the peer geomtry nodes which are no longer in the graph
		if (geometries.size() - newG.size() != geomDiff)	{
			AndroidConfiguration.theLog.log(Level.WARNING,"Old, new hash size: "+geometries.size()+" "+newG.size());
			geomDiff = geometries.size() - newG.size() ;
		}
		return;
	}
	public  AndroidPeerGeometry getAndroidPeerGeometryFor(Geometry g)	{
		AndroidPeerGeometry pg;
		synchronized(geometries)	{
			pg = (AndroidPeerGeometry) geometries.get(g);
			if (pg != null) return pg;
			pg = new AndroidPeerGeometry(g, jr);
			geometries.put(g, pg);			
		}
		return pg;
	}


}
