package charlesgunn.jreality.plugin;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.icon.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class LoadableSceneMenu extends Plugin {

	private JMenu menu;
	private ButtonGroup buttonGroup;
	private HashMap<String, ButtonModel> nameToButton = new  HashMap<String, ButtonModel>();
	private HashMap<String, Color[]> nameToColors = new  HashMap<String, Color[]>();
	private String selectedScene;
	private ViewMenuBar viewerMenu;
	PluginSceneLoader loader;
	
	// TODO: figure out way to delay this until it's actually needed (rare!)
	public LoadableSceneMenu(PluginSceneLoader psl) {
		loader = psl;

		menu = new JMenu("Load scene");
//		menu.setIcon(ImageHook.getIcon("color_swatch.png"));
		buttonGroup = new ButtonGroup();
		String[] packages = {"charlesgunn.jreality.worlds","charlesgunn.jreality.worlds.projective",
				"charlesgunn.jreality.test", "discreteGroup", "dragonfly"};
		for (String packagey : packages) {
			JMenu smenu = new JMenu(packagey);
			menu.add(smenu);
//			menu.setIcon(ImageHook.getIcon("color_swatch.png"));
			Class[] loadableSceneClasses = null;
			try {
				loadableSceneClasses = getClasses(packagey);
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
//			for (String scene : loadableScenes)	{
			System.err.println("found "+loadableSceneClasses.length+" classes for package "+packagey);
			for (Class classy : loadableSceneClasses)	{
				Class sc = classy.getSuperclass();
				boolean good = false;
				//for (Class inf : interfaces) {
				if (!(sc == LoadableScene.class)) {continue;}
				String scene = classy.getName();
//				System.err.println("index of $ = "+scene.indexOf('?'));
				if (scene.indexOf(((int) '$')) >= 0) continue;
				final String thisone = scene;
				Action action = new AbstractAction(thisone) {

					public void actionPerformed(ActionEvent e) {
						selectedScene = thisone;
						loader.unloadScene();
						loader.loadScene(thisone);
					}

				};
				JRadioButtonMenuItem item = new JRadioButtonMenuItem(action);
				item.getModel().setActionCommand(thisone);
				nameToButton.put(thisone, item.getModel());
				buttonGroup.add(item);
				smenu.add(item);
				
			}
			
		}
	}

	public String getColor() {
		return buttonGroup.getSelection().getActionCommand();
	}
	
	public JMenu getMenu() {
		return menu;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "LoadableScene menu";
		info.vendorName = "Charles Gunn";
		info.icon = ImageHook.getIcon("arrow.png");
		return info;
	}

	public void install(Scene scene) {
	}

	@Override
	public void install(Controller c) throws Exception {
		install(c.getPlugin(Scene.class));
		viewerMenu = c.getPlugin(ViewMenuBar.class);
		viewerMenu.addMenuItem(
				getClass(),
				10.0,
				getMenu(),
				"File"
		);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		viewerMenu.removeAll(getClass());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		selectedScene = c.getProperty(getClass(), "selectedScene", selectedScene);
		if (true) return;
		if (selectedScene != null && loader != null) loader.loadScene(selectedScene);
		super.restoreStates(c);
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		c.storeProperty(getClass(), "selectedScene", selectedScene);
		super.storeStates(c);
	}
	
    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
