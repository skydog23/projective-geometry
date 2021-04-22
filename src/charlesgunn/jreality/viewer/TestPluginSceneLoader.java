package charlesgunn.jreality.viewer;

import java.io.InputStream;

public class TestPluginSceneLoader {

    public static void main(String[] args) {
    	String cp = ((String)System.getProperty("java.class.path")).replace(':', '\n'); //split(":");
    	System.err.println("cp = "+cp);
    	// first argument is name of loadablescene class, second is optionally a property file name
		if (args != null && args.length != 0) System.err.println("args 0 is "+args[0]);
		if (args == null || args.length == 0)  {
    			final PluginSceneLoader psl = new PluginSceneLoader(null, true );
            psl.loadScene("charlesgunn.jreality.worlds.SimpleShapes" );
            return;
		}
		String lsname = null; //"charlesgunn.jreality.worlds.SimpleShapes";
		String propfile = null;
		if (args != null && args.length > 0) {
			lsname = args[0];
			if (args.length > 1) {
				propfile = args[1];
			}
		}
		if (propfile != null) System.err.println("propfile = "+propfile);
		try {
	            LoadableSceneInterface ls = (LoadableSceneInterface) Class.forName(lsname).newInstance();
	    		InputStream is = null;
	            if (propfile != null)  is = ls.getClass().getResourceAsStream(propfile);
	    		final PluginSceneLoader psl = new PluginSceneLoader(is);
	            psl.loadScene(lsname == null ? "charlesgunn.jreality.worlds.SimpleShapes" : lsname);
//	            Window w = SwingUtilities.getWindowAncestor((Panel)psl.getJRViewer().getViewer().getViewingComponent());
//	            SwingUtilities.updateComponentTreeUI(w);
		} catch (Exception e) {
	            e.printStackTrace();
	        }
		}

}
