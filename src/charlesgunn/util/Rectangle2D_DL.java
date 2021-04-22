package charlesgunn.util;
//
//  Rectangle2D_DL.java
//  testawt
//
//  Created by Charles Gunn on Sun Jun 01 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

public class Rectangle2D_DL implements Cloneable {
    public doubleLong x,y,w,h, cx, cy;
    private static doubleLong t1, t2, t3, t4;

    static {		// temporary objects: avoid allocating often
        t1 = new doubleLong();
        t2 = new doubleLong();
        t3 = new doubleLong();
        t4 = new doubleLong();
    }

    public Rectangle2D_DL()  {
        x = new doubleLong(0.0);
        y = new doubleLong(0.0);
        w = new doubleLong(1.0);
        h = new doubleLong(1.0);
        cx = new doubleLong();
        cy = new doubleLong();
        calculateCenter();
    }

    public Rectangle2D_DL(double dx, double dy, double dw, double dh)	{
        x = new doubleLong(dx);
        y = new doubleLong(dy);
        w = new doubleLong(dw);
        h = new doubleLong(dh);
        cx = new doubleLong();
        cy = new doubleLong();
        calculateCenter();
    }

    public Object clone()	{
        try {
            Rectangle2D_DL copy = (Rectangle2D_DL) super.clone();
            copy.x = (doubleLong) x.clone();
            copy.y = (doubleLong) y.clone();
            copy.w = (doubleLong) w.clone();
            copy.h = (doubleLong) h.clone();
            copy.cx = (doubleLong) cx.clone();
            copy.cy = (doubleLong) cy.clone();
            return copy;
        } catch (CloneNotSupportedException e)	{
            throw new Error("This should never happen.");
        }
    }

    public void setRect(double dx, double dy, double dw, double dh)	{
        x.setValue(dx);
        y.setValue(dy);
        w.setValue(dw);
        h.setValue(dh);
        calculateCenter();
    }
    
    public String toString()	{
        return (Double.toString(x.doubleValue())+" "+Double.toString(y.doubleValue())+" "+Double.toString(w.doubleValue())+" "+Double.toString(h.doubleValue()));
    }
    
    public void calculateCenter()	{
        w.multiply(.5,t1);
        h.multiply(.5,t2);
        x.add(t1,cx);
        y.add(t2,cy);
    }
}
