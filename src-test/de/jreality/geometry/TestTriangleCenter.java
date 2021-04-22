package de.jreality.geometry;

import junit.framework.TestCase;
import charlesgunn.math.TriangleCenter;
import de.jreality.math.Pn;

public class TestTriangleCenter extends TestCase {

	public void testTriangleCenter()	{
		double[] p0 = {0,0,.2,1},
			p1 = {.5,0,-.3,1},
			p2 = {0,.5,.11,1};
		double[] center = TriangleCenter.triangleCenter(null, p0, p1, p2, Pn.ELLIPTIC);
		
	}
}
