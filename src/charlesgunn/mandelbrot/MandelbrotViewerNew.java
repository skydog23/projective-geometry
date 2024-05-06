//
//  MandelbrotViewer.java
//
//  Created by Charles Gunn on Sun Mar 23 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
//	For information on setting Java configuration information, including 
//	setting Java properties, refer to the documentation at
//		http://developer.apple.com/techpubs/java/java.html
//
/*
 * mandelbrot set:
-1.4738386622435602 -9.048214909402885E-4 8.24764856410809E-9 8.24764856410809E-9 -1.4738386581197358 -9.048173671160065E-4 8192
-1.9072471417712222 -1.0257199392071925E-6 1.241460267920047E-8 1.241460267920047E-8 -1.907247135563921 -1.0195126378675923E-6 4096
-1.9072471354525586 -1.0205214214051989E-6 8.154447169589352E-12 8.154447169589352E-12 -1.9072471354484815 -1.0205173441954919E-6 8192
-0.18184727071885193 -1.0191361080021666 7.496691939733888E-15 7.496691939733888E-15 4096
-0.18184727071884796 -1.019136108002163 2.9827722858003684E-21 2.9827722858003684E-21 4096
-0.18184727071884796 -1.019136108002163 3.315589463052756E-23 3.315589463052756E-23 4096


TODO:
ability to create colormaps
save/archive favorite locations (use XML?)
    when saving, have to save all info: saving as double is not  enough precision
direct type-ins for center point and viewport size, number of iterations, other parameters
ability to draw the orbit of 0 (in a separate frame)
different tools for mouse interaction:
    choose new viewport,
    center on point,
    draw julia set,
    info mode: print coordinates of position, value there
save images: jpeg library?
supersampling
off-screen calculation of big images
gallery:
    loading a big gallery grinds machine to halt: all the drawing threads at once
    fix alignment so spaces between are constant, not stretching (layout manager?)
    don't overwrite the gallery entry when you make it the current selection.
    how to show which pictures in the gallery are selected?
any way to script java using something like tcl?

DONE
"DoubleLong" support when more precision is needed
ability to interrupt the program: using threads?
history: each new image generates thumbnail that gets added to an "album";
    ability to go back to any state,
progressive calculation of image: display rough resolution, etc (fix)
ability to generate a julia set (in separate frame)
draw the rectangle as you drag out new viewport

*/

package charlesgunn.mandelbrot;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.IndexColorModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.icon.ImageHook;
import de.jreality.plugin.scene.ShrinkPanelAggregator;
import de.jreality.util.NativePathUtility;
import de.jreality.util.Secure;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.jtem.jrworkspace.plugin.simplecontroller.SimpleController;

public class MandelbrotViewerNew extends Plugin  {
	
	
	static {
		String lnfClass = UIManager.getSystemLookAndFeelClassName();
		System.err.println("LaF class = "+lnfClass);
		if (lnfClass.contains("Aqua") || lnfClass.contains("Windows")) {
			if (lnfClass.contains("Aqua")) {
//				System.setProperty("com.apple.mrj.application.apple.menu.about.name", "jReality");
				Secure.setProperty("apple.laf.useScreenMenuBar", "true");
			}
			try {
				UIManager.setLookAndFeel(lnfClass);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	

    //protected ResourceBundle resbundle;
    protected AboutBox aboutBox;
    protected Preferences prefs;
//    private Application fApplication = new Application();
    protected Action newAction;
	
    // Declarations for menus
    static final MenuBar mainMenuBar = new MenuBar();
	
    protected Menu fileMenu;
    protected MenuItem miNew;

    protected int cmap = 0;
    
    protected MandelbrotPaneNew mbPane = new MandelbrotPaneNew();
    protected MandelbrotNew currMb = mbPane.currMb; //mbPane.getMandelbrot();
    protected LinkedList collectionList = new LinkedList();
    protected Hashtable nameTable = new Hashtable();
    protected LinkedList mbList = new LinkedList();
    protected int whichMb = 0;
    public boolean isDebug = true,
        isStartingRepli = true,
        isLogging = true; 
    protected JFrame myFrame = null,
    		gallery = null;
    protected JTabbedPane tp = null;
    protected LinkedList galList = new LinkedList();
    
    SimpleController con = new SimpleController("MandelbrotViewer");
    
	transient protected ShrinkPanelAggregator shrinkPanelPlugin = new ShrinkPanelAggregator() {
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public String getHelpTitle() {
		return MandelbrotViewerNew.this.getClass().getSimpleName();
	}
	

	};

	transient protected ShrinkPanel shrinkPanel = shrinkPanelPlugin.getShrinkPanel();

    static String directory = "/Users/skydog/Developer/MandelbrotViewer/resources";
    static IndexColorModel[] cm = new IndexColorModel[5];
    static int numCmaps = 5;
    static int rooms = 0;
    
    static {
        // add color maps
        int i, j, k;
        byte[] bb = new byte[256];
        byte[] gg = new byte[256];
        byte[] rr = new byte[256];
        for (i=0;i<256;++i)	{
            bb[i] = gg[i] = rr[i] = (byte) i;
        }
        cm[0] = new IndexColorModel(8,256,rr,gg,bb);
        for (i=0;i<256;++i)	{
            bb[i] = gg[i] = rr[i] = (byte) (255 - i);
        }
        cm[1] = new IndexColorModel(8,256,rr,gg,bb);
        for (i=0;i<6;++i)	{
            for (j=0;j<6;++j)	{
                for (k=0;k<7;++k)	{
                    bb[42*i+7*j+k] = (byte) (42 * i);
                    gg[42*i+7*j+k] = (byte) (42 * j);
                    rr[42*i+7*j+k] = (byte) (36 * k);
                }
            }
        }
        for (i=252;i<256;++i)	bb[i] = gg[i] = rr[i] = (byte) 255;
        cm[2] = new IndexColorModel(8,256,rr,gg,bb);
        for (i=0;i<256;++i)	{
            bb[i] = gg[i] = rr[i] = (byte) ((8*(255 - i))%256);
        }
        cm[3] = new IndexColorModel(8,256,rr,gg,bb);
        for (i=0;i<256;++i)	{
            bb[i] = (byte) ((9 * i) % 256);
            gg[i] = (byte) ((255 - 8 * i) % 256);
            rr[i] = (byte) ((255 - 8 * i) % 256);
        }
        bb[255] = gg[255] = rr[255] = (byte) 255;
        cm[4] = new IndexColorModel(8,256,rr,gg,bb);
    }

    class namedLinkedList extends LinkedList {
        protected String name = null;

        public namedLinkedList(String nn)	{
            name = nn;
        }
    }
    public MandelbrotViewerNew() {
//        super("");
//        WindowAdpt WAdapter = new WindowAdpt();
//        this.addWindowListener(WAdapter);
        aboutBox = new AboutBox();
        prefs = new Preferences();
        Toolkit.getDefaultToolkit();

       // set up collections w/ default
        nameTable.put(mbList, "default");
        System.out.println(nameTable.toString());
        myFrame = new JFrame();
        myFrame.getContentPane().setSize(256, 256);
        myFrame.setTitle ("MandelbrotViewer");
        myFrame.setLayout(new GridLayout());
        myFrame.getContentPane().add(mbPane, BorderLayout.CENTER);
        addMenus();
        //createActions();
        con.registerPlugin(shrinkPanelPlugin);
        con.registerPlugin(this);
        con.startupLocal();
        myFrame.setVisible(true);

        myFrame.validate();
        myFrame.pack();
//        show();
        myFrame.requestFocus();
        
        myFrame.addKeyListener(getMyKeyAdapter());
        myFrame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e)	{
                System.out.println("Viewer being resized.");     
                myFrame.requestFocus();
                }
        });

    }


	@Override
	public void install(Controller con) throws Exception {
		super.install(con);
		shrinkPanel.setTitle(this.getClass().getSimpleName());
		Component insp = mbPane.getInspector();
		shrinkPanel.removeAll();
		shrinkPanel.setLayout(new ShrinkPanel.MinSizeGridBagLayout());
		Insets insets = new Insets(1,5,1,5);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = insets;
		c.weighty = 0.0;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.CENTER;
		if (insp != null) 
			shrinkPanel.add(insp, c);
		myFrame.getContentPane().add(shrinkPanel, BorderLayout.EAST);
	}
	
    void printUsage()	{
    		Logger theLog = Logger.getLogger("charlesgunn.mandelbrot");
    		message(theLog, "A","push current settings");
    		message(theLog, "B", "toggle high-precision");
    		message(theLog,"C","change colormap");
    		message(theLog, "Shift-C","cycle colormap");
    		message(theLog,"D","double number of iterations");
    		message(theLog,"Shift-D","halve number of iterations");
    		message(theLog,"F","refresh display");
       	message(theLog,"G","show gallery");
       	message(theLog,"I","interrupt");
       	message(theLog,"J","toggle Julia/Mandelbrot set");
       	message(theLog,"L", "toggle logging of program settings");
       	message(theLog,"M","double lower automatic resolution");
       	message(theLog,"Shift-M","halve lower automatic resolution");
       	message(theLog,"N","double higher automatic resolution");
       	message(theLog,"Shift-N","halve higher automatic resolution");
       	message(theLog,"O","open saved gallery file");
       	message(theLog,"P","print current viewport");
       	message(theLog,"R","reset viewport to default");
       	message(theLog,"S","save current gallery to file");
       	message(theLog,"X","double resolution");
       	message(theLog,"Shift-X","halve resolution");
       	message(theLog,"Z","zoom in");
       	message(theLog,"Shift-Z","zoom out");
       	message(theLog,"left arrow","move to previous settings");
       	message(theLog,"right arrow", "move to next settings");
    		
    }
    
    private void message(Logger l, String key, String mes)	{
		//l.log(Level.INFO,"\t"+key+":\t"+mes);
		System.out.println("\t"+key+":\t"+mes);
		     }
//    public void about(ApplicationEvent e) {
//        aboutBox.setResizable(false);
//        aboutBox.setVisible(true);
//        aboutBox.setSize(300,150);
//        aboutBox.show();
//    }
//
//    public void preferences(ApplicationEvent e) {
//        prefs.setResizable(true);
//        prefs.setVisible(true);
//        prefs.show();
//    }
//
//    public void quit(ApplicationEvent e) {
//        System.exit(0);
//    }
//
    
    private KeyAdapter getMyKeyAdapter() {
    	return new KeyAdapter()	{
        public void keyPressed(KeyEvent e)	{
            System.out.println("key event = "+e.getKeyChar());
            switch(e.getKeyCode())	{

                case KeyEvent.VK_A:		// add current value to list
                    pushCurrMb();
                    break;

                case KeyEvent.VK_B:		// toggle BigDecimals
                    currMb.setUseDL( !currMb.getUseDL());
                    mbPane.repaint();
                    break;

                case KeyEvent.VK_C:		// change colormap
                    if (e.isShiftDown())	{
                        mbPane.toggleCycleColormap();
                        mbPane.repaint();
                        break;
                    }

                    cmap = (cmap+1) % numCmaps;
                    mbPane.setColorModel(cm[cmap]);
                    mbPane.repaint();
                    break;

                case KeyEvent.VK_D:		// double # of iterations
                    if (isLogging) pushCurrMb();
                    if (e.isShiftDown()) currMb.setNumIterations(currMb.getNumIterations() >> 1);                                                			
                    else currMb.setNumIterations(2 * currMb.getNumIterations());                        			
                    mbPane.repaint();
                    break;

                case KeyEvent.VK_F:		// reFresh display
                    currMb.setDirty(true);
                    mbPane.repaint();
                    break;

                case KeyEvent.VK_G:		// show gallery
                    makeGallery(mbList);
                    break;
                    
                case KeyEvent.VK_H:		// help menu
                    printUsage();
                    break;

                case KeyEvent.VK_I:		// interrupt Mandelbrot thread
                    currMb.doInterrupt();
                    break;

                case KeyEvent.VK_J:		// toggle Mandelbrot/Julia
                    currMb.setIsJulia(!currMb.getIsJulia());
                    mbPane.repaint();
                    break;

                case KeyEvent.VK_L:		
                    isLogging = !isLogging;
                    if (isDebug) System.out.println("Logging is: "+isLogging);
                    break;

                case KeyEvent.VK_M:		// change multi-resolution parameters
                    if (!e.isShiftDown())	{
                        mbPane.lowRepliLimit *= 2;
                    }
                    else			{	// halve it
                        mbPane.lowRepliLimit /= 2;
                        if (mbPane.lowRepliLimit < 1)	mbPane.lowRepliLimit = 1;
                    }
                    //mbPane.setReplifactor(lowRepliLimit);
                    //currMb.setDirty(true);
                    //mbPane.repaint();
                    break;

                case KeyEvent.VK_N:		// change multi-resolution parameters
                    if (!e.isShiftDown())	{	// use direct writes to save time
                        mbPane.highRepliLimit *= 2;
                    }
                    else			{	// halve it
                        mbPane.highRepliLimit /= 2;
                        if (mbPane.highRepliLimit < mbPane.lowRepliLimit)
                            mbPane.lowRepliLimit = mbPane.lowRepliLimit;
                    }
                    //mbPane.setReplifactor(lowRepliLimit);
                    //currMb.setDirty(true);
                    //mbPane.repaint();
                    break;

                case KeyEvent.VK_O:		// open file containing locations
                    handleOpen();
                    break;

                case KeyEvent.VK_P:		// print viewport
                    System.out.println(currMb.getViewport().toString()+currMb.getNumIterations());
                    break;

                case KeyEvent.VK_R:		// reset viewport to default
                    if (isLogging) pushCurrMb();
                    currMb.resetViewport(mbPane.getSize());
                    mbPane.repaint();
                    break;

                case KeyEvent.VK_S:		// save current list to file
                    handleSave();
                    break;

                case KeyEvent.VK_X:		// double the size of the window
                    if (isLogging) pushCurrMb();
                    currMb.doInterrupt();

                    Dimension dim = mbPane.getSize();
                    if (isDebug) System.out.println("Current size is: "+dim.toString());
                    if (!e.isShiftDown())	{
                        dim.width *= 2;
                        dim.height *= 2;
                        mbPane.setSize(dim);
                    }
                    else			{	// halve it
                        dim.width /= 2;
                        dim.height /= 2;
                        mbPane.setSize(dim);
                    }
                    mbPane.invalidate();
                    myFrame.pack();
//                    show();
                    mbPane.repaint();
                    currMb.setDirty(true);
                    mbPane.repaint();
                    break;

                case KeyEvent.VK_Z:		// zoom out/in
                    if (isDebug) System.out.println("Zooming");
                    if (isLogging) pushCurrMb();
                    if (e.isShiftDown())	currMb.zoomOut();
                    else			currMb.zoomIn();
                    mbPane.repaint();
                    break;

                case KeyEvent.VK_DELETE:		
                        if (mbList.size() <= 1) break;
                        int index = mbList.indexOf(mbPane);
                        if (index < 0) break;
                        makeCurrent(index - 1, mbList);
                        mbList.remove(index);
                        break;

                case KeyEvent.VK_SLASH:		// toggle debugging
                    isDebug = !isDebug;
                    MandelbrotPane.isDebug = !MandelbrotPane.isDebug;
                    Mandelbrot.isDebug = !Mandelbrot.isDebug;
                    break;

                case KeyEvent.VK_ESCAPE:
                	    System.exit(-1);
                	    break;
                	    
                case KeyEvent.VK_RIGHT:		// move to next location in list
                    if (isLogging)
                        if (!mbList.contains(mbPane)) pushCurrMb();
                    whichMb++;
                    makeCurrent(whichMb, mbList);
                    break;

                case KeyEvent.VK_LEFT:		// move to previous locations
                    if (isDebug) System.out.println("Length is: "+mbList.size());
                    if (isLogging)	{
                        if (!mbList.contains(mbPane)) pushCurrMb();
                        //whichMb--;
                    }
                    whichMb--;
                    makeCurrent(whichMb, mbList);
                    break;

            }
        }};
    }
    public void createActions() {
        System.out.println("Creating actions");
        int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        //Create actions that can be used by menus, buttons, toolbars, etc.
        try {
            newAction = new newActionClass("New",
                    KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcutKeyMask) );
        }
        catch (java.util.MissingResourceException ev)	{
            System.out.println("MissingResourceException:"+ev.getMessage());
        }
    } 

    public class newActionClass extends AbstractAction {
        public newActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }
        public void actionPerformed(ActionEvent e) {
            System.out.println("New...");
        }
    }

    public void addFileMenuItems() {
        miNew = new MenuItem ("New");
        miNew.setShortcut(new MenuShortcut(KeyEvent.VK_N, false));
        fileMenu.add(miNew).setEnabled(true);
        miNew.addActionListener(newAction);
		
		
        mainMenuBar.add(fileMenu);
    }
	
    public void addMenus() {
        fileMenu = new Menu("File");
        addFileMenuItems();
        myFrame.setMenuBar (mainMenuBar);
    }

    public void handleAbout()
    {
        aboutBox.setResizable(false);
        aboutBox.setVisible(true);
        aboutBox.show();
    }
	
    public void handleQuit()
    {	
        // If the application needs to save document/state before exiting, do so here
        System.exit(0);
    }

    /*
    public void paint (Graphics g)	{
        if (isDirty)	{
            remove(mbPane);
            
        }
    }
     */
    
    public void pushCurrMb()	{
        int index = mbList.indexOf(mbPane);
        // if the pane already exists, replace it with the copy
        if (index != -1) 	{
            mbList.set(index, mbPane.clone());
            mbList.add(mbPane);
            if (isDebug) System.out.println("Panel exists at position: "+index);
        }
        else 	               { 	// add a copy to the end of  list
            mbList.add(mbPane.clone());		// put current pane at end of list
            if (isDebug) System.out.println("Panel does not exist in list");
        }
        // always go to end of list
        whichMb = mbList.size() - 1;
        if (isDebug) System.out.println("Pushing "+mbList.size());
    }

    private void makeCurrent(int index, LinkedList xxList)	{
        if (xxList.size() <= 1) return;
        if (index < 0) index += xxList.size();
        index = index % xxList.size();
        whichMb = index;
        mbList = xxList;
        makeCurrent((MandelbrotPaneNew) xxList.get(index));
   }
    
    public void makeCurrent(MandelbrotPaneNew nmp)		{
        //mbPane.deactivate();        
    	 myFrame.remove(mbPane);
        mbPane = nmp;
        currMb = mbPane.getMandelbrot();
        //mbPane.activate();
        myFrame.getContentPane().add(mbPane);
        mbPane.repaint();
        myFrame.validate();
        myFrame.pack();
        myFrame.show();
    }
    
    public void handleOpen()	{
        String shortname, filename, ss;
        File file;
        FileDialog fd = new FileDialog(myFrame, "Open file", FileDialog.LOAD);
        
        //StreamTokenizer st;
        java.util.StringTokenizer st;
        double [] vals = new double[5];
        int nt, i;
        collectionList.add(mbList);
        mbList = new LinkedList();
        fd.setDirectory(directory);
        fd.show();
        directory = fd.getDirectory();
        shortname = fd.getFile();
        filename = directory+shortname;

        try {
            file = new File(filename);
            BufferedReader fin = new BufferedReader(new FileReader(file));
            while ( (ss = fin.readLine()) != null)	{
                st = new java.util.StringTokenizer(ss);
                for (i=0; i<5; ++i)	{
                    vals[i] = Double.parseDouble(st.nextToken());
                    System.out.println(vals[i]);
                }
                mbList.add(new MandelbrotPaneNew(new MandelbrotNew(vals[0], vals[1], vals[2], vals[3], ((int) vals[4]))));
            }
            fin.close();
        }
        catch (java.io.IOException ev)	{
            System.out.println("IOException:"+ev.getMessage());
        }
        nameTable.put(mbList,shortname);
        System.out.println(nameTable.toString());
    }


    public void handleSave()	{
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String filename, ss;
        File file;
        FileDialog fd = new FileDialog(myFrame, "Open file", FileDialog.SAVE);
        ListIterator ls;
        Mandelbrot mb;
        MandelbrotPane mbp;
        //StreamTokenizer st;
        java.util.StringTokenizer st;
        double [] vals = new double[5];
        int nt, i;
        //System.out.print("Input file name: ");
        fd.setDirectory(directory);
        fd.show();
        directory = fd.getDirectory();
        filename = directory+fd.getFile();

        try {
            //filename = in.readLine();
            file = new File(filename);
            PrintWriter pw = new PrintWriter(new FileWriter(file));
            
            //st = new StreamTokenizer(fr);
            for( ls = mbList.listIterator(); ls.hasNext();)	{
                mbp = (MandelbrotPane) (ls.next());
                mb = mbp.getMandelbrot();
                pw.print(mb.getViewport().toString());
                pw.print(" ");
                pw.println(mb.getNumIterations());
            }
            pw.close();
            //viewportDL.setRect(vals[0], vals[1], vals[2], vals[3]);
            //currMb.setDirty(true);
            //mbPane.repaint();
        }
        catch (java.io.IOException ev)	{
            System.out.println("IOException:"+ev.getMessage());
        }

    }

   
    public void makeGallery(final LinkedList xxlist)	{
        ListIterator ls;
        Mandelbrot mb;
        MandelbrotPane mbp;
        if (gallery == null)	{
            gallery = new JFrame("Gallery"); //Window(this);
            gallery.setSize(600,600);

            Border bb = new EmptyBorder(5,5,5,5);
            //gallery.getContentPane().setBorder(bb);
            tp = new JTabbedPane();

            gallery.getContentPane().add(tp);
        }
        JPanel pp = new JPanel();
        pp.setLayout(new GridLayout((xxlist.size()/4 + 1),4,5,5));
        final Dimension thumbnail = new Dimension(128,128);
        for( ls = xxlist.listIterator(); ls.hasNext();)	{
            final MandelbrotPane fmbp;
            mbp = (MandelbrotPane) (ls.next());
            fmbp = mbp;
            class scaledImage extends Panel	{
                Dimension tnail = thumbnail;
                MandelbrotPane mp = fmbp;
                AffineTransform at = new AffineTransform();
                public Dimension preferredSize()	{
                   return thumbnail;
                }
                public void paint(Graphics g)	{
                    mp.updateImage();
                    Image ci = mp.getImage();
                    if (ci == null) {
                        return;
                    };
                    double sx = ((double) tnail.getWidth())/fmbp.getSize().getWidth();
                    double sy = ((double) tnail.getHeight())/fmbp.getSize().getHeight();
                    double ss = Math.min(sx, sy);
                    at.setToScale(ss,ss);
                    ((Graphics2D) g).drawImage(ci, at, this);
                    if (isDebug) System.out.print("$");
                }
            }
            final scaledImage ip = new scaledImage();
            ip.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e)	{
                    System.out.println("IP "+e.getComponent().toString());
                    int index = xxlist.indexOf(ip.mp);
                    makeCurrent(index, xxlist);
                }
            } );
            
            pp.add(ip);
        }
        pp.setSize(580,140 * (xxlist.size()/4 + 1));
        pp.repaint();
        ScrollPane sp = new ScrollPane();

        sp.setSize(580,600);
        sp.add(pp);
        sp.repaint();
        String collectionName = (String) nameTable.get(xxlist);
        if (collectionName != null) 	tp.addTab(collectionName, sp);
        else				tp.addTab("testlist"+rooms,sp);	       
        gallery.validate();
        gallery.pack();
        gallery.show();
        rooms++;
        
    }
    
    class WindowAdpt extends java.awt.event.WindowAdapter {
        public void windowClosing(java.awt.event.WindowEvent event) {
            handleQuit();
        }
    }
    
    public static void main(String args[]) {
        new MandelbrotViewerNew();
    }


}
