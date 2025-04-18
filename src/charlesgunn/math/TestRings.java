/*
 * Created on 31 Mar 2025
 *
 */
package charlesgunn.math;

import cc.redberry.rings.*;
import cc.redberry.rings.poly.*;
import cc.redberry.rings.poly.univar.*;
import cc.redberry.rings.poly.multivar.*;
import cc.redberry.rings.bigint.BigInteger;

import static cc.redberry.rings.poly.PolynomialMethods.*;
import static cc.redberry.rings.Rings.*;
public class TestRings {

	public static void main(String[] args) {
		// ring Z[x, y, z]
		MultivariateRing<MultivariatePolynomial<BigInteger>> ring = MultivariateRing(3, Z);
		// assign "x", "y" and "z" to variables
		MultivariatePolynomial<BigInteger>
		        x = ring.variable(0),
		        y = ring.variable(1),
		        z = ring.variable(2);

		// construct some polynomials
		MultivariatePolynomial<BigInteger> a = ring.decrement(ring.pow(ring.add(x, y, z), 2));
		MultivariatePolynomial<BigInteger> b = ring.add(
		        ring.pow(ring.add(x, ring.negate(y), ring.negate(z), ring.getNegativeOne()), 2),
		        x, y, z, ring.getNegativeOne());
		MultivariatePolynomial<BigInteger> c = ring.add(
		        ring.pow(ring.add(a, b, ring.getOne()), 9),
		        ring.negate(a), ring.negate(b), ring.getNegativeOne());

		// reduce c modulo a and b (multivariate division with remainder)
		MultivariatePolynomial<BigInteger>[] divRem = MultivariateDivision.divideAndRemainder(c, a, b);
		MultivariatePolynomial<BigInteger>
		        div1 = divRem[0],
		        div2 = divRem[1],
		        rem = divRem[2];


	}

}
