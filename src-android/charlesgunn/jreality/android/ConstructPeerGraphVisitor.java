/*
 * Created on Jan 14, 2007
 *
 */
package charlesgunn.jreality.android;

import java.lang.reflect.InvocationTargetException;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.util.LoggingSystem;

public class ConstructPeerGraphVisitor extends SceneGraphVisitor	{
	SceneGraphComponent myRoot;
	AndroidPeerComponent thePeerRoot, myParent;
	SceneGraphPath sgp;
	boolean topLevel = true;
	AndroidRenderer jr;
	boolean singlePeer = false;
	static Class<? extends AndroidPeerComponent> peerClass = AndroidPeerComponent.class;
	public static void setPeerClass(Class<? extends AndroidPeerComponent> c)	{
		peerClass = c; 
	}
	
	public static void setPeerClass(String name) throws ClassNotFoundException	{
		peerClass = (Class<? extends AndroidPeerComponent>) Class.forName(name); 
	}

	public ConstructPeerGraphVisitor(SceneGraphComponent r, AndroidPeerComponent p, AndroidRenderer jr)	{
		super();
		myRoot = r;
		sgp = new SceneGraphPath();
		myParent = p;
		this.jr = jr;
	}

	private ConstructPeerGraphVisitor(ConstructPeerGraphVisitor pv, AndroidPeerComponent p)	{
		super();
		sgp = new SceneGraphPath(pv.sgp);
		myParent = p;
		topLevel = false;
		this.jr = pv.jr;
	}

	static protected AndroidPeerComponent constructPeerForSceneGraphComponent(
			final SceneGraphComponent sgc, final AndroidPeerComponent p, AndroidRenderer jr) {
		if (sgc == null) return null;
		ConstructPeerGraphVisitor constructPeer = new ConstructPeerGraphVisitor( sgc, p, jr);
		final AndroidPeerComponent peer = (AndroidPeerComponent) constructPeer.visit();
		return peer;
	}

	public void visit(SceneGraphComponent c) {
		// check the appearance to see if single peer is indicated
		boolean oldSinglePeer = singlePeer;
		if (c.getAppearance() != null)	{
			Object foo = c.getAppearance().getAttribute("singlePeer",Boolean.class);
			if (foo != null && foo instanceof Boolean) {
				singlePeer = ((Boolean)foo).booleanValue();
			} 			
		}
//		System.err.println("OSP: "+oldSinglePeer+" "+c.getName()+" NSP: "+singlePeer);
		sgp.push(c);
		GoBetween gb = GoBetween.goBetweenFor(jr, c, singlePeer);
		AndroidPeerComponent peer = null;
		boolean alreadySinglePeer = false;
		if (gb.isSinglePeer() && (gb.getSinglePeer()) != null) {
			peer = gb.getSinglePeer();
			alreadySinglePeer = true;
		} else {
			try {
				try {
					peer = peerClass.getConstructor().newInstance();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch(SecurityException se)	{
				LoggingSystem.getLogger(this).warning("Security exception in setting configuration options");
			}
			peer.init(gb, sgp, myParent, jr);
		}
//			System.err.println("Got sgc of class "+peer.getClass().getName());
//			System.err.println("peerClass is "+AndroidConfiguration.peerClass.getName());
		if (topLevel) thePeerRoot = peer;
		else if (myParent != null) {
			int n = myParent.children.size();
			//String space = (new char[2*sgp.getLength()]).toString();
			myParent.children.add(peer);
			peer.childIndex = n;
			myParent.childCount = n+1;
		}
		if (!alreadySinglePeer)	{
			c.childrenAccept(new ConstructPeerGraphVisitor(this, peer));
			sgp.pop();
			singlePeer = oldSinglePeer;
		}
	}

	public Object visit()	{
		visit(myRoot);
//		System.err.println("Peer count is "+AndroidPeerComponent.count);
		return thePeerRoot;
	}

}
