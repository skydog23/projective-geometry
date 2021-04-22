//
//  MandelbrotPane.java
//  MandelbrotViewer
//
//  Created by Charles Gunn on Tue Jul 01 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package charlesgunn.mandelbrot;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

import charlesgunn.util.Rectangle2D_DL;

public class MandelbrotPane extends JPanel implements Cloneable  {

    protected Rectangle2D_DL viewportDL = null; 
//    protected JPanel thePanel = null;
    protected Mandelbrot currMb = null;
    protected byte[] valArray = null;
    protected MemoryImageSource mis = null;
    protected IndexColorModel cm = null;
    protected Thread calcThread = null;
    protected Image  ci = null;
    protected AffineTransform at = new AffineTransform();
    protected java.util.Timer myTimer = new java.util.Timer();
    protected TimerTask myTimerTask = null, updateImageTask = null;
    private int cycleCount = 1;
    public int highRepliLimit = 4, lowRepliLimit = 1;
    public boolean isDirty = true,
        isFinished = false,
        doCycleColormap = false;
    private boolean isCalculating = false;
    static boolean isDebug = true;
    static byte[] bb, gg, rr;
    static Dimension defaultSize = new Dimension(256, 256);
    Dimension imageSize = null;

    protected int downX, downY, upX, upY;
    static IndexColorModel defaultCM = null;

    static {
        int i, j, k;
        bb = new byte[256];
        gg = new byte[256];
        rr = new byte[256];
        for (i=0;i<256;++i)	{
            bb[i] = gg[i] = rr[i] = (byte) i;
        }
        defaultCM = new IndexColorModel(8,256,rr,gg,bb);
    }
    int ixmin, iymin, ixmax, iymax;
    boolean dragging  = false;
    Rectangle dragged;
    JFrame parent;

    public MandelbrotPane(JFrame p)	{
        this(new Mandelbrot(new Rectangle2D_DL(-3.0,-2.0,4.0,4.0), 256), p);
    }

    public MandelbrotPane(Mandelbrot mb, JFrame p) {
        super();
        parent = p;
        cm = defaultCM;
		calcThread = new Thread(new Runnable()	{
			public void run()	{
				doOneImage();
			}
		});
       
        updateImageTask = new TimerTask()	{
            public void run()	{
                updateImage();
            }
        };
        myTimer.schedule(updateImageTask, 20, 500);
        currMb = mb;
//        thePanel = new JPanel();
        setSize(defaultSize);
        setPreferredSize(defaultSize);
        imageSize = (Dimension) defaultSize.clone();
               
        addMouseMotionListener(new MouseMotionAdapter() {
            // guts of this needs to be moved to Mandelbrot class
            public void mouseDragged(MouseEvent e)	{
                upX = e.getX();
                upY = e.getY();
//                if (isDebug)	{
//                    System.out.println(upX);
//                    System.out.println(upY);
//                }
                ixmin = Math.min(downX, upX);
                ixmax = Math.max(downX, upX);
                iymin = Math.min(downY, upY);
                iymax = Math.max(downY, upY);
                int iwidth, iheight;
                 iwidth = ixmax - ixmin;
                iheight = iymax - iymin;
                dragged = new Rectangle(ixmin, iymin, iwidth, iheight);
                dragging = true;
               repaint();
             }
        });

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)	{
                downX = e.getX();
                downY = e.getY();
//                if (isDebug) System.out.println(downX);
//                if (isDebug) System.out.println(downY);
                //getParent().requestFocus();
         
                dragged = null;
                //System.out.println("Color is: "+bi.getRGB(downX,downY));
            }
            
             // guts of this needs to be moved to Mandelbrot class
            public void mouseReleased(MouseEvent e)	{
                if ( getParent() instanceof MandelbrotViewer)
                	 if (((MandelbrotViewer) getParent()).isLogging) ((MandelbrotViewer) getParent()).pushCurrMb();
                if (dragged == null) dragged = new Rectangle(downX-getSize().width/2, downY-getSize().height/2, getSize().width, getSize().height);
                currMb.subViewport(imageSize, dragged);
                repaint();
                //getParent().requestFocus();
                System.out.println("Parent is: "+getParent());
                dragging = false;
            }
        });
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e)	{
                System.out.println("Pane being resized from "+e.getComponent().getSize());     
                setImageSize(e.getComponent().getSize());
    			repaint();
                parent.requestFocus();
            }
        });
    }

    protected Object clone()	{
        //try {
            MandelbrotPane copy = (MandelbrotPane) new MandelbrotPane((Mandelbrot) currMb.clone(), parent);
            copy.cm = cm;
            copy.calcThread = null;
            copy.isFinished = false;
            return copy;
        //} catch (CloneNotSupportedException e)	{
            //throw new Error("This should never happen.");
        //}
    }
    
        
    public Mandelbrot getMandelbrot()	{
        return currMb;
    }

    public void setMandelbrot(Mandelbrot xx)	{
        currMb = xx;
        currMb.setDirty(true);
        repaint();
    }

    public boolean isFinished()	{
        return isFinished;
    }
    
    public void setFinished(boolean b) {
        isFinished = b;
    }

public Image getImage()	{
    return ci;
}
	Object readLock = new Object();
	/**
	 * @param size
	 */
	private void setImageSize(Dimension size) {
		currMb.doInterrupt();
		try {
		Thread.sleep(20);
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
		synchronized(readLock)	{
			currMb.alignViewport(size);
			imageSize = size;
	        valArray = new byte[size.width * size.height];
	        mis = new MemoryImageSource(size.width, size.height, cm, valArray, 0, size.width);            	
	        if (isDebug) System.out.println("MandelbrotPane size is: "+size.toString());
	        isDirty = true;
		}
 	}

	public Dimension getImageSize() {
		// TODO Auto-generated method stub
		return imageSize;
	}


    //public IndexColorModel getColorModel()	{
      //  return cm;
    //}

    public void setColorModel(IndexColorModel xcm) {
        cm = xcm;
        mis.newPixels(valArray, cm, 0, imageSize.width);
        mis.newPixels(0,0,imageSize.width, imageSize.height);
        isFinished = true;	// trigger redraw
        updateImage();
    }

 public void toggleCycleColormap()	{
     doCycleColormap = !doCycleColormap;
     if (doCycleColormap)	{
         
         myTimerTask = new TimerTask()	{
             public void run()	{
                 doOneCycleStep();
             }
         };
         myTimer.schedule(myTimerTask, 0, 20);
     }
     else {
         if (myTimerTask != null) {
             myTimerTask.cancel();
             myTimerTask = null;
             mis.newPixels(valArray, cm, 0, imageSize.width);
             mis.newPixels(0,0,imageSize.width, imageSize.height);
             isFinished = true;	// trigger redraw  
             updateImage();
         }         
     }
 }

private void doOneCycleStep()	{
     int i, j;
     byte[][][] ruff = new byte[2][3][256];
     cm.getReds(ruff[0][0]);
     cm.getGreens(ruff[0][1]);
     cm.getBlues(ruff[0][2]);

     for (i=0; i<256; ++i)	{
         for (j=0;j<3;++j)	{
             ruff[1][j][i] = ruff[0][j][(i+cycleCount)%256];
         }
     }
     mis.newPixels(valArray, new IndexColorModel(8,256,ruff[1][0], ruff[1][1], ruff[1][2]), 0, imageSize.width);
     mis.newPixels(0,0,imageSize.width,imageSize.height);
     isFinished = true;	// trigger redraw
     if (isDebug) System.out.print("@");
     cycleCount = (cycleCount + 1)%256;
     updateImage();
}

    public void update(Graphics g)	{
        paint(g);
    }

    public void paint(Graphics og) {
        Graphics2D g = (Graphics2D) og;
        //if (isDebug) System.out.print("&");
        calculateImage();
        if (ci != null) {
        	g.drawImage(ci, 0,0,this);
        }
        if (dragging)	{
            g.setColor(java.awt.Color.WHITE);
            g.drawLine(ixmin, iymin, ixmax, iymin);
            g.drawLine(ixmax, iymin, ixmax, iymax);
            g.drawLine(ixmax, iymax, ixmin, iymax);
            g.drawLine(ixmin, iymax, ixmin, iymin);
       	
        }
    }

public void updateImage()	{
		if (mis != null) ci = createImage(mis);	
		repaint();
}

public void calculateImage()	{
    if ( isDirty || currMb.isDirty() ) { //|| isFinished))	{
        	if (!isCalculating) {
        		if (calcThread != null) calcThread.stop();
        		calcThread = new Thread(new Runnable()	{
        			public void run()	{
        				doOneImage();
        			}
        		});
        		calcThread.start();
        	    System.out.println("In updateImage, this, other thread: "+Thread.currentThread().getName()+" "+calcThread.getName());
        	}
         }
}
	   private void doOneImage() {
			// if (isDebug) System.out.print("#");
			if ((isDirty || currMb.isDirty)) {
				isCalculating = true;
				boolean normalCompletion = false;
				synchronized (readLock) {
					normalCompletion = currMb.threadCalcImage(this); // loR, hiR, dimR, valR,
														// misR);
				}
				isCalculating = false;
				isDirty = false;
				if (normalCompletion) {
					updateImage();
					System.out.println("Calculated image");
				} else
					System.out.println("interrupted");
			}
	}
	
	
	
// public void calcImage() {
//	    
// if (!pendingUpdate) {
// pendingUpdate = true;
// //EventQueue.invokeLater(this);
// if (calcThread == null) calcThread = new Thread(this);
// calcThread.run();
// }
// // if (calcThread == null) {
////	        calcThread = new Thread( new Runnable()	{
////	            public void run()	{
////	                doThread();
////	            }
////	        });
////	        calcThread.start();
////	    }
//	    //if (isDebug) System.out.print("?");
//	        //System.out.println("In calcImage: Mis hash is: "+mis.hashCode());
//	}
//
//	    private void doThread()	{
//	        while (true)	{
//	            try {
//	            //if (isDebug) System.out.print("#");
//	            if (isDirty || currMb.isDirty)	{
//	                isCalculating = true;
//	                currMb.threadCalcImage(this); //loR, hiR, dimR, valR, misR);
//	                isFinished = true;
//	                isCalculating = false;
//	                System.out.println("Calculated image");
//	                isDirty = false;
//	                //ci = createImage(mis);
//	                   //calcThread.suspend();
//	                }
//	            Thread.sleep(20);
//	            }
//	            catch (java.lang.InterruptedException ev)	{
//	                System.out.println("InterruptedException:"+ev.getMessage());
//	            }
//	        }   
//	    }
//
//	    public void deactivate()	{
//	        if (calcThread == null) return;
//	            //try {
//	            //doInterrupt = true;		// try to interrupt
//	            //calcThread.sleep(1);
//	        System.out.println("Suspending "+calcThread.getName());
//	        calcThread.suspend();
//	            //}
//	            //catch (java.lang.InterruptedException ev)	{
//	            //System.out.println("InterruptedException:"+ev.getMessage());
//	            //}
//	    
//	            //if (calcTask != null)	calcTask.cancel();
//	    }
//	    
//	    public void activate()	{
//	        if (calcThread != null) {
//	            System.out.println("Resuming "+calcThread.getName());
//	            calcThread.resume();
//	        }
//	    }
	    


}
