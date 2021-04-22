/*
 * Created on Jan 14, 2007
 *
 */
package charlesgunn.jreality.android;

import java.util.logging.Logger;

import de.jreality.util.LoggingSystem;

public class AndroidPeerNode	{
	String name;
	AndroidRenderer jr;
	static Logger theLog = LoggingSystem.getLogger(AndroidPeerNode.class);

	public AndroidPeerNode()	{
		super();
	}
	public AndroidPeerNode(AndroidRenderer jr)	{
		super();
		this.jr =  jr;
	}
	public String getName()	{
		return name;
	}

	public void setName(String n)	{
		name = n;
	}
}

