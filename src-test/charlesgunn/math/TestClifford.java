/*
 * Created on Aug 21, 2014
 *
 */
package charlesgunn.math;

import static org.junit.Assert.*;

import org.junit.Test;

import charlesgunn.math.clifford.MultiVectorP3;
import charlesgunn.math.clifford.ThreeSpace;

public class TestClifford {

	MultiVectorP3 p1 = MultiVectorP3.plane(new double[]{1,0,0,1}),
			p2 = MultiVectorP3.plane(new double[]{1,2,0,1});
	ThreeSpace ts = new ThreeSpace(-1);

	@Test
	public void testGp() {
		ThreeSpace ts = new ThreeSpace();
		MultiVectorP3 gp = ts.gp(null, p1, p2);
		System.err.println("gp = "+gp.toString());
	}

	@Test
	public void testPolarize() {
		MultiVectorP3 pol = ts.polarize(null, p2);
		System.err.println("polar = "+pol.toString());
	}

	@Test
	public void testNormalize() {
		MultiVectorP3 norm = ts.normalize(null, p2);
		System.err.println("normalized = "+norm.toString());
	}

	@Test
	public void testInverse() {
		MultiVectorP3 inv = ts.inverse(null, p2);
		MultiVectorP3 prod = ts.gp(null, p2, inv);
		System.err.println("x xinv = "+prod.toString());
		
	}

	@Test
	public void testSandwichProduct() {
		fail("Not yet implemented");
	}

	@Test
	public void testMatrixForRotor() {
		fail("Not yet implemented");
	}

	@Test
	public void testExp() {
		fail("Not yet implemented");
	}

	@Test
	public void testLogarithmForRotor() {
		fail("Not yet implemented");
	}

}
