//
//  Mandelbrot.java
//  testawt
//
//  Created by Charles Gunn on Wed May 28 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package charlesgunn.mandelbrot;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.MemoryImageSource;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.util.Rectangle2D_DL;
import charlesgunn.util.TextSlider;
import charlesgunn.util.doubleLong;

public class MandelbrotNew extends Object implements Cloneable {
    protected Rectangle2D_DL viewport = null ;
    protected doubleLong jcx, jcy;		// coordinates of Julia-set value
    protected int numIters = 256;
    protected boolean isDirty = true,
        doInterrupt = false,
        isJulia = false,
        useDL = false;
    static boolean isDebug = false;
    static doubleLong zeroDL = new doubleLong(0.0);
    public MandelbrotNew()	{
        this(new Rectangle2D_DL(-3.0, -2.0, 4.0, 4.0), 256);
    }

    public MandelbrotNew(double x, double y, double w, double h, int n)	{
        this(new Rectangle2D_DL(x,y, w, h), n);
    }

    public MandelbrotNew(Rectangle2D_DL vp, int n)	{
        //this(sz.width, sz.height, defaultCM, valArray = new byte[size.width * size.height], 0, sz.width);
        viewport = vp;
        numIters = n;
        setupInspector();
    }
/*
    class myTimerTask extends TimerTask	{
        public void run()	{
            threadCalcImage(mpR);
        }
    }
*/    
    protected Object clone()	{
        try {
        MandelbrotNew copy = (MandelbrotNew) super.clone();
        copy.viewport = (Rectangle2D_DL) viewport.clone();
        copy.isDirty = true;	// no data left, needs to be regenerated
        return copy;
        } catch (CloneNotSupportedException e)	{
            throw new Error("This should never happen.");
        }
    }

    Box inspector = Box.createHorizontalBox();
    Component getInspector() {
    	return inspector;
    }

    final TextSlider<Integer> ts = new TextSlider.Integer("num",SwingConstants.HORIZONTAL,1,2048, numIters );
;
    public void setupInspector()	{
		ts.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				int foo = ts.getValue().intValue();
				setNumIterations(foo);
			}
		});
		inspector.add(ts);

    }
        
    public int getNumIterations()	{
        return numIters;
    }

    public void setNumIterations(int n) {
        numIters = n;
        if (numIters < 1) numIters = 1;
        System.out.println("Number of its is "+numIters);
        isDirty = true;

    }

    public boolean isDirty()	{
        return isDirty;
    }

    public void setDirty(boolean b) {
        isDirty = b;
    }

    public void doInterrupt()	{
        doInterrupt = true;
    }

    public void setViewport(Rectangle2D_DL xx) {
        viewport = xx;
        isDirty = true;
    }

    public Rectangle2D_DL getViewport()	{
        return viewport;
    }

    public boolean getUseDL()	{
        return useDL;
    }

    public void setUseDL(boolean b) {
        useDL = b;
        isDirty = true;
    }

    public boolean getIsJulia()	{
        return isJulia;
    }

    Rectangle2D_DL mandelbrotVP = null;
    public void setIsJulia(boolean b) {
        isJulia = b;
        if (isJulia)	{
            mandelbrotVP = (Rectangle2D_DL) viewport.clone();
            jcx = (doubleLong) viewport.cx.clone();
            jcy = (doubleLong) viewport.cy.clone();
            viewport.setRect(-2.0, -2.0, 4.0, 4.0);
        } 
        else {
        	if (mandelbrotVP != null) viewport = mandelbrotVP;
        }
        isDirty = true;
    }

    public void zoomIn()	{
        double cx, cy;
        doubleLong tx = new doubleLong();
        doubleLong ty = new doubleLong();
        if (isDebug) System.out.println("preZoomIN:"+viewport.toString());
        viewport.w.multiply(.25,tx);
        viewport.h.multiply(.25,ty);
        viewport.cx.subtract(tx, viewport.x);
        viewport.cy.subtract(ty, viewport.y);
        viewport.w.multiply(.5);
        viewport.h.multiply(.5);
        if (isDebug) System.out.println("postZoomIN:"+viewport.toString());
        //viewport.calculateCenter();
        isDirty = true;
    }

    public void zoomOut()	{
        double cx, cy;
        viewport.cx.subtract(viewport.w, viewport.x);
        viewport.cy.subtract(viewport.h, viewport.y);
        viewport.w.multiply(2.0);
        viewport.h.multiply(2.0);
        // viewport.calculateCenter();
        isDirty = true;
    }

    public void resetViewport(Dimension size)	{
        if (isJulia) viewport.setRect(-2.0, -2.0, 4.0, 4.0);
        else viewport.setRect(-3.0, -2.0, 4.0, 4.0);
        viewport.calculateCenter();
        alignViewport(size);
        isDirty = true;
        if (isDebug) System.out.println("Resetting viewport.");
    }

    public void alignViewport(Dimension size)	{
        double vasr, fasr,rr;	// view and frame aspect ratios
        double cx, cy;
        fasr = ((double) size.width)/size.height;
        vasr = viewport.w.doubleValue()/viewport.h.doubleValue();
        if (vasr == fasr) return;
        //otherwise adjust the viewport to produce square pixels
        // should keep the center fixed
        rr = fasr/vasr;
        doubleLong tnx = new doubleLong();
        viewport.w.multiply((1-rr)*.5, tnx);
        viewport.x.add(tnx);
        viewport.w.multiply(rr);
        viewport.calculateCenter();
        isDirty = true;
    }

    // calculate a subViewport 
    public void subViewport(Dimension size, Rectangle subR)	{
        // calculate center of dragged rect in NDC
        doubleLong xf = new doubleLong(((double) (subR.getX()+(subR.getWidth()*.5)))/size.width);
        doubleLong yf = new doubleLong(((double) (subR.getY()+(subR.getHeight()*.5)))/size.height);
        // calculate the center offset in "world coordinates"
        xf.multiply(viewport.w);
        yf.multiply(viewport.h);
        // calculate the center of the new viewport in Mandelbrot "world" coordinates
        viewport.x.add(xf,viewport.cx);
        viewport.y.add(yf,viewport.cy);
        // if the rectangle is too small, just move the center
        if (Math.abs(subR.getWidth())< 3 || Math.abs(subR.getHeight()) < 3)	{
            viewport.w.multiply(.5,xf);
            viewport.h.multiply(.5,yf);
            viewport.cx.subtract(xf,viewport.x);
            viewport.cy.subtract(yf,viewport.y);
        } else {
            // shrink the whole rectangle by a factor determined by the x-data
            double dx = ((double) subR.getWidth())/size.width;
            doubleLong tdx = new doubleLong(dx);
            // find the new width and height
            viewport.w.multiply(tdx);
            viewport.h.multiply(tdx);
            viewport.w.multiply(.5,xf);
            viewport.h.multiply(.5,yf);
            // determine the new corner by subtacting half width, height from center
            viewport.cx.subtract(xf,viewport.x);
            viewport.cy.subtract(yf,viewport.y);
        }
        setDirty(true);
    }
    
    public boolean threadCalcImage(MandelbrotPaneNew mp)  { 
    	System.err.println("rectangle = "+viewport.toString());
        int tlo, thi, currRf;
        Dimension tsize;
        byte valArray[];
        MemoryImageSource mis;
//        synchronized(mp.readLock)	{
            tlo = mp.lowRepliLimit;
            thi = mp.highRepliLimit;
            tsize = mp.getImageSize();
            valArray = mp.valArray;
            mis = mp.mis;
            System.out.println("threadCalcImage "+tsize);
//        }
        //currRf = thi = tlo = 1;
        for (currRf = thi; currRf >= tlo; currRf = currRf/2)	{
            
        int i,j,k,m, start;
        int val;
        double xmin, xmax, ymin, ymax, dx, dy, x,y, jcxd=0, jcyd=0;
        doubleLong dlx, dly, dldx, dldy, dldxs, dldys, bx, by;
        int tval;
        //if (!isDirty)	return;
        
        dx = viewport.w.doubleValue()/tsize.width;
        dy = viewport.h.doubleValue()/tsize.height;
        xmin = viewport.x.doubleValue();
        ymin = viewport.y.doubleValue();

        if (isJulia)	{
            jcxd = jcx.doubleValue();
            jcyd = jcy.doubleValue();
        }
        dlx = new doubleLong();
        dly = new doubleLong();
        dldx = new doubleLong();
        dldy = new doubleLong();
        dldxs = new doubleLong();
        dldys = new doubleLong();
        bx = new doubleLong();
        by = new doubleLong();
        dldx.setValue(dx);
        dldy.setValue(dy);
        dly.setValue(viewport.y);
        dldys.setValue(0.0);
        for (i=0;i<tsize.height;i++)	{
            dlx.setValue(viewport.x);
            dldxs.setValue(0.0);
            doubleLong.add(dly, dldys, by);
            for (j=0;j<tsize.width;j++)	{
                if ((j % currRf == 0) & (i % currRf == 0))	{
                    if (doInterrupt)	{
                        doInterrupt = false;
                        // resizing doesn't work correctly if I un-comment out the following
                        // but interrupt doesn't work with it commented out.
                        //isDirty = false;
                        System.out.println("Interrupt received");
                        return false;
                    }
                    doubleLong.add(dlx, dldxs, bx);
                    if (isJulia)
                        if (useDL)		tval = getValueDL(bx, by, jcx, jcy );
                        else			tval = getValue(xmin+dx*j, ymin+dy*i,jcxd, jcyd);
                    else
                        if (useDL)		tval = getValueDL(zeroDL, zeroDL, bx, by );
                    else			tval = getValue(0.0, 0.0, xmin+dx*j, ymin+dy*i);
                    
                    start = i*tsize.width + j;
                    for (k=0; k < currRf; k++)
                        for (m=0;m<currRf; ++m)	{
                           if ( (i+k) >= tsize.height || (j+m) >= tsize.width) continue;
                        	valArray[i*tsize.width + j + k * tsize.width + m] = (byte) tval; 	
                        }
                 }
                dldxs.add(dldx);
            }
            if (i% currRf == 0)  {
                if (isDebug) System.out.print(".");
                mis.newPixels(0,i,tsize.width, currRf);
            }
            //Thread.yield();
            dldys.add(dldy);
        }
        }
        isDirty = false;
        return true;
   }

    public int getValue(double sx, double sy, double cx, double cy)	{
        int i, rr, n;
        double tx, ty, otx, oty, ff;
        otx = sx; oty =  sy;
        n = numIters;
        ff = 255.0/n;
        for (i=0; i<n; ++i)	{
            if (doInterrupt)	{
                return 0;
            }
            tx = otx*otx - oty*oty + cx;
            ty = 2 * otx*oty  + cy;
            if (tx*tx + ty*ty > 10000) break;
            otx = tx;
            oty = ty;
        }
        rr =((int) (i * ff));
        rr = (rr > 255) ? 255 : rr;
        return rr;
    }
    public int getValueDL(doubleLong sx, doubleLong sy, doubleLong cx, doubleLong cy)	{
        int i, rr, sc, n;
        double ff;
        doubleLong btx, bty, botx, boty, bff, t1, t2, t3, bnx, bny;
        t1  = new doubleLong();
        t2  = new doubleLong();
        t3  = new doubleLong();
        bnx  = new doubleLong();
        bny = new doubleLong();
        botx = (doubleLong) sx.clone();		// copy!
        boty = (doubleLong) sy.clone();
        n = numIters;
        ff = 255.0/n;
        for (i=0; i<n; ++i)	{
            botx.multiply(botx, t1);		// re*re
            boty.multiply(boty, t2);
            t1.subtract(t2, t3);		// - im*im
            t3.add(cx, bnx);			// + re0
            botx.multiply(boty, t1);
            t1.add(t1,t2);
            //t1.shiftLeft(1);			// 2 re*im
            t2.add(cy, bny);			// + im0
                               //bnx.multiply(bnx,t1);
                               //bny.multiply(bny,t2);
            if (Math.abs(bnx.hi) + Math.abs(bny.hi) > 0x7ffffffffffffffL) break;
            bnx.copyTo(botx);		// copy!
            bny.copyTo(boty);
        }
        rr =((int) (i * ff));
        rr = (rr > 255) ? 255 : rr;
        //System.out.println("Value: "+rr);
        return rr;

    }
    
    
}
