package charlesgunn.util;
//
//  doubleLong.java
//  testawt
//
//  Created by Charles Gunn on Fri May 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

public class doubleLong extends Object implements Cloneable {
    public long hi, lo;
    public static final long LO56,
        HI8,
        LO31,
        LO32,
        HI32,
        LO24,
        LO25,
        BIT56;
	private static doubleLong thus, that;

    static	{
        LO56 = 0xffffffffffffffL;
        HI8 = 0xff00000000000000L;
		LO31 = 0x7fffffffL;
		LO32 = 0xffffffffL;
        HI32 = 0xffffffff00000000L;
		LO24 = 0xffffffL;
		LO25 = 0x1ffffffL;
        BIT56 = 0x100000000000000L;
        thus = new doubleLong();
        that = new doubleLong();
        //System.out.println(Long.toHexString(LO56));
        //System.out.println(Long.toHexString(HI8));
        //System.out.println(Long.toHexString(LO32));
        //System.out.println(Long.toHexString(HI32));
        //System.out.println(Long.toHexString(BIT56));
    }
    public doubleLong ()	{
        this.hi = 0;
        this.lo = 0;
    }

    public doubleLong (long xlo, long xhi)	{
        this.setValue(xlo, xhi);
    }

    public doubleLong (double val)	{
        this.setValue(val);
    }

    public void setValue(long xlo, long xhi)	{
        hi = xhi;
        lo = xlo;
    }

    public void setValue(doubleLong dlval)	{
        hi = dlval.hi;
        lo = dlval.lo;
    }

    public void setValue(double val)	{
        double dhi, dlo;
        boolean flipped = false;
        if (val < 0) {
        	val = -val;
        	flipped = true;
        }
        dhi = BIT56 * val;
        hi = (long) dhi;
        dlo = BIT56 * (dhi - hi);
        lo = (long) dlo;
        lo = lo & LO56;
        if (flipped)  this.negate();
    }
    public double doubleValue()	{
        return ((double) (hi/((double) BIT56)+ lo/((double) BIT56)/((double) BIT56)));
    }

    public String toString()	{
        return("Hi:"+Long.toHexString(this.hi)+"Lo:"+Long.toHexString(this.lo));
    }

	public String toBinaryString()	{
		return("Hi:"+Long.toBinaryString(this.hi)+"Lo:"+Long.toBinaryString(this.lo));
	}

   public  Object clone()	{
        doubleLong cl = new doubleLong();
        cl.hi = this.hi;
        cl.lo = this.lo;
        return (cl);
    }

    public void copyTo(doubleLong dst)	{
        dst.hi = hi;
        dst.lo = lo;
    }

    public static void add(doubleLong thus, doubleLong that, doubleLong dst)	{
        dst.hi = thus.hi + that.hi;
        dst.lo = thus.lo + that.lo;
        dst.hi += dst.lo >>> 56;		// carry bits; unsigned shift
        dst.lo = dst.lo & LO56;
    }
    
    public void add(doubleLong that, doubleLong dst)	{
        add(this, that, dst);
    }
    
    public void add(doubleLong that)	{
        add(this, that, this);
    }

    public static void negate(doubleLong src, doubleLong dst)	{
        dst.hi = ~src.hi;
        dst.lo = ~src.lo + 1;
        src.lo = src.lo & LO56;
    }
    
    public void negate(doubleLong dst)	{
        negate(this, dst);
    }

    public void negate()	{
        negate(this, this);
    }

    public static void subtract(doubleLong thus, doubleLong that, doubleLong dst){
        doubleLong neg = (doubleLong) that.clone();
        neg.negate();
        add(thus, neg, dst);
    }

    public void subtract(doubleLong that, doubleLong dst)	{
        subtract(this, that, dst);
    }

    public void subtract(doubleLong that)	{
        subtract(this, that, this);
    }

    public static void multiply(doubleLong ithus, doubleLong ithat, doubleLong dst)	{
    	//doubleLong thus, that;
		ithus.copyTo(thus);
		ithat.copyTo(that);
        long b11, b12, b21, b22, b13, b23, b14, b24, p11, p12, p13, p22, p21, p31, p23, p32, p14, p41;
        long hacc = 0, lacc = 0;
        boolean flippedThus = false, flippedThat = false;;
        if (thus.hi < 0 ) {
        	thus.negate();
        	flippedThus = true;
        } 
        if ( that.hi < 0) {
        	that.negate();
        	flippedThat = true;
        } 
        // problems come from not having an unsigned long type; 
        // the important datum is: how many bits is the binary point
        // positioned to the left of the LSB of the number?
        // that is indicated for each result
        b11 = thus.hi >> 31;	// (.., 25) format
        b21 = that.hi >> 31;	// (.., 25)
        b12 = thus.hi & LO31;	// (.., 56)
        b22 = that.hi & LO31;	// (.., 56)
        b13 = thus.lo >> 25;	// (.., 87)
        b23 = that.lo >> 25;	// (.., 87)
        b14 = (thus.lo & LO25);	// (.., 112)
        b24 = (that.lo & LO25);	// (.., 112)
        p11 = b11 * b21;		// (..,50) 
        p12 = b11 * b22;	 	// (..,81)
        p21 = b12 * b21;
        p22 = b12 * b22;		// (.., 112)
        p13 = b11 * b23;		// (.., 112)
        p31 = b13 * b21;		
        p23 = b12 * b23;		// (.., 143)
        p32 = b13 * b22;
        p14 = b11 * b24;		// (.., 137)
        p41 = b21 * b14;
        hacc = p11 << 6;		// (.., 56)
        hacc += p12 >> 25;		// (.., 56)
        hacc += p21 >> 25;
        hacc += p13 >> 56;		// (.., 56)
        hacc += p31 >> 56;		// (.., 56)
        hacc += p22 >> 56;	
        						// need to get low accumulator to (.., 112)	
        lacc = (p12 & LO25) << 31;
        lacc += (p21 & LO25) << 31;
        lacc += (p13 & LO56);
		lacc += (p31 & LO56);
		lacc += (p22 & LO56);
		lacc += (p23 >> 31) & LO56;
		lacc += (p32 >> 31) & LO56;
		lacc += (p14 >> 25) & LO56;
		lacc += (p41 >> 25) & LO56;
        hacc += lacc >> 56;		// only working with positive numbers
        lacc = lacc & LO56;
        dst.hi = hacc;
        dst.lo = lacc;
		//if (flippedThus == true)  thus.negate();
		//if (flippedThat == true)  that.negate();
		if ((flippedThus == true &&  flippedThat == false)  ||(flippedThus == false &&  flippedThat == true) )  dst.negate();
    }

    public void multiply(doubleLong that, doubleLong dst)	{
        multiply(this, that, dst);
    }

    public void multiply(doubleLong that)	{
        multiply(this, that, this);
    }

    public void multiply(double d, doubleLong dst)	{
        doubleLong dd = new doubleLong(d);
        multiply(this, dd, dst);
    }

    public void multiply(double d)	{
        doubleLong dd = new doubleLong(d);
        multiply(this, dd, this);
    }

    public doubleLong multiplyN(doubleLong that)	{
        doubleLong ret = new doubleLong();
        multiply(this, that, ret);
        return ret;
    }
}
