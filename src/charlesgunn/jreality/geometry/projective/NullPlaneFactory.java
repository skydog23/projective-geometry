/*
 * Created on Jun 17, 2010
 *
 */
package charlesgunn.jreality.geometry.projective;

import java.awt.Color;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.math.p5.PlueckerLineGeometry;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

public class NullPlaneFactory {

	SceneGraphComponent nullPlanes = SceneGraphUtility.createFullSceneGraphComponent("null planes");
	PointRangeFactory prf;
	int numPlanes = 10, numLines =20;
	SceneGraphComponent oneNP = nullPlaneRepresentation(numLines);
	double[] nullSystem = {1,0,2,3,0,1};
	
	public NullPlaneFactory(PointRangeFactory prf)	{
		this.prf = prf;
	}
	
	static final double[] zaxis = {0,0,1};
	public void update()	{
		double[] line = prf.getPluckerLine();
		double[][] samples = prf.getVertices();
		double[] skewMatrix = PlueckerLineGeometry.lineToSkewMatrix(null, nullSystem);
		System.err.println("skew matrix = "+Rn.matrixToString(skewMatrix));
		if (!prf.isFiniteSphere()) numPlanes = samples.length;
		for (int i = 0; i<numPlanes; ++i)	{
			double[] sample;
			if (prf.isFiniteSphere()) sample = AnimationUtility.linearInterpolation(i, 0, numPlanes-1, samples[0], samples[1]);	
			else sample = samples[i];
			sample = Pn.dehomogenize(null, sample);
			System.err.println("sample = "+Rn.toString(sample));
			SceneGraphComponent child = new SceneGraphComponent("nullplane"+i);
			double[] nullplane = Rn.matrixTimesVector(null, skewMatrix, sample);
			System.err.println("np = "+Rn.toString(nullplane));
			child.addChild(oneNP);
			double[] n = {nullplane[0], nullplane[1], nullplane[2]};
			MatrixBuilder.euclidean().translate(sample).rotateFromTo(zaxis, n).assignTo(child);
			System.err.println("matrix = "+Rn.matrixToString(child.getTransformation().getMatrix()));
			nullPlanes.addChild(child);
		}
//		return nullPlanes;
	}

	public double[] getNullSystem() {
		return nullSystem;
	}

	public void setNullSystem(double[] nullSystem) {
		this.nullSystem = nullSystem;
	}

	public SceneGraphComponent getNullPlanes() {
		return nullPlanes;
	}

	public int getNumPlanes() {
		return numPlanes;
	}

	public void setNumPlanes(int numPlanes) {
		this.numPlanes = numPlanes;
	}

	public static SceneGraphComponent nullPlaneRepresentation(int numLines) {
		Appearance ap;
		SceneGraphComponent oneNP = SceneGraphUtility.createFullSceneGraphComponent("oneNP");
		SceneGraphComponent plane = SceneGraphUtility.createFullSceneGraphComponent("null plane");
		ap = oneNP.getAppearance();
		//ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
//		ap.setAttribute("polygonShader.diffuseColor", Color.white);
		ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.black);
		ap.setAttribute("lineShader.tubeRadius", .005);
		ap.setAttribute("pointShader.polygonShader.diffuseColor", Color.black);
		ap.setAttribute("pointShader.pointRadius", .005);
		IndexedFaceSet ifs = Primitives.regularPolygon(4, .5); //100);
		plane.setGeometry(ifs);
		MatrixBuilder.euclidean().scale(1.4).assignTo(plane);
		
		LinePencilFactory lpf = new LinePencilFactory();
		lpf.setPoint(new double[]{0,0,0,1});
		lpf.setPlane(new double[]{0,0,1,0});
		lpf.setNumLines(numLines);
		lpf.setFiniteSphere(true);
		lpf.setSphereRadius(.8);
		lpf.setMetric(0);
		lpf.update();
	
		SceneGraphComponent pencil = SceneGraphUtility.createFullSceneGraphComponent("null lines");
		ap = pencil.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
//		ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.black);
//		ap.setAttribute("lineShader.tubeRadius", .005);
		pencil.addChild(lpf.getPencil());
		oneNP.addChildren(plane,pencil);
		return oneNP;
	}
}
