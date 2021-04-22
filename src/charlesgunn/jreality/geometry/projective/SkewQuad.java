/*
 * Created on Nov 22, 2010
 *
 */
package charlesgunn.jreality.geometry.projective;

import de.jreality.math.Pn;
import de.jtem.projgeom.PlueckerLineGeometry;

public class SkewQuad extends RegulusFactory {

	double[][] corners, sideLines = new double[6][];
	int whichCorner;
	protected LinePencilFactory
		rp0 = new LinePencilFactory(),
		rp1  = new LinePencilFactory();
	
//	private SkewQuad()	{
//		super();
//	}
	
	public static SkewQuad skewQuadForCorners(double[][] corners)	{
		SkewQuad skewquad = new SkewQuad();
		skewquad.leitSchar = new SkewQuad();
		skewquad.leitSchar.isLeitSchar = true;
		skewquad.setCorners(corners);
		return skewquad;
	}
	@Override
	protected void checkDegeneracy() {
		super.checkDegeneracy();
		if (isDegenerate)	{
			double[] point = PlueckerLineGeometry.intersectionPoint(null, generators[intersectors[0]], generators[intersectors[1]]);
			double[] inpros = new double[4];
			int i;
			for (i = 0; i<4; ++i)	{
				inpros[i] = Pn.distanceBetween(point, corners[i], Pn.EUCLIDEAN);
				if (inpros[i] < 10E-6) 	{ // found it!
					break;
				}
			}
			if (i == 4)	
				throw new IllegalStateException("Not degenerate after all?");	
			whichCorner = i;
			System.err.println("Corner is "+whichCorner);
			// set up all the necessary line pencil factories
			if (whichCorner == 0 || whichCorner == 2)	{ // this doesn't work
				rp0.setIntersectingLines(sideLines[0], sideLines[4]);
				rp1.setIntersectingLines(sideLines[2], sideLines[4]);
				((SkewQuad) leitSchar).rp0.setIntersectingLines(sideLines[1], sideLines[4]);
				((SkewQuad) leitSchar).rp1.setIntersectingLines(sideLines[3], sideLines[4]);
			} else { // this works
				rp0.setIntersectingLines(sideLines[0], sideLines[5]);
				rp1.setIntersectingLines(sideLines[2], sideLines[5]);
				((SkewQuad) leitSchar).rp0.setIntersectingLines(sideLines[1], sideLines[5]);
				((SkewQuad) leitSchar).rp1.setIntersectingLines(sideLines[3], sideLines[5]);
			}
		}
	}


	@Override
	protected void computeLines() {
		if (!isDegenerate) {
			super.computeLines(); return;
		}
		if (pluckerLines == null || pluckerLines.length != numSegs)
			pluckerLines = new double[numSegs][];
		rp0.setFiniteSphere(finiteSphere);
		rp0.setSphereRadius(sphereRadius);
		rp0.setNumLines(numSegs/2);
		rp0.update();
		rp1.setFiniteSphere(finiteSphere);
		rp1.setSphereRadius(sphereRadius);
		rp1.setNumLines(numSegs - numSegs/2);
		rp1.update();
//		System.err.println("Point0 = "+Rn.toString(rp0.getPoint()));
//		System.err.println("Plane0 = "+Rn.toString(rp0.getPlane()));
//		System.err.println("Point1 = "+Rn.toString(rp1.getPoint()));
//		System.err.println("Plane1 = "+Rn.toString(rp1.getPlane()));
		double[][] tmp = rp0.getPluckerLines();
		System.arraycopy(tmp, 0, pluckerLines, 0, tmp.length);
		double[][] tmp2 = rp1.getPluckerLines();
		System.arraycopy(rp1.getPluckerLines(), 0, pluckerLines, tmp.length, tmp2.length);
	}
	
	@Override
	protected void updateLeitSchar() {
		if (!isDegenerate)	{
			super.updateLeitSchar();
			return;
		}
		System.err.println("Updating leitschar");
		leitSchar.update();
	}
	public double[][] getElements() {
		return sideLines;
	}
	public double[][] getCorners() {
		return corners;
	}
	
//	int[][] pluckerPairs = {{0,1},{0,2},{0,3}, {1,2},{3,1}, {2,3}};
	int[][] pluckerPairs = {{0,1},{1,2},{2,3}, {3,0}, {0,2}, {1,3}};
	
	public void setCorners(double[][] corners) {
		this.corners = corners;
		for (int i =0; i<pluckerPairs.length; ++i )	{
			sideLines[i] = PlueckerLineGeometry.lineFromPoints(null, 
					corners[pluckerPairs[i][0]], corners[pluckerPairs[i][1]]);
		}
		setElement0(sideLines[1]);
		setElement2(sideLines[3]);
		leitSchar.setElement0(sideLines[0]);
		leitSchar.setElement2(sideLines[2]);
	}

	@Override
	public RegulusFactory getLeitScharFactory() {
		if (leitSchar == null) {
			leitSchar = new SkewQuad();
			leitSchar.isLeitSchar = true;
		}
		return leitSchar;
	}

}
