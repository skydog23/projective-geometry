package charlesgunn.jreality.geometry.projective;

import static de.jreality.shader.CommonAttributes.EDGE_DRAW;

import java.awt.Color;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;

/**
 * This class uses the class {@link CircleFactory} to show how stereographic projection
 * acts on the three great circles (equator and two through north pole). CircleFactory is
 * designed to map circles to circles under stereographic projection. The lines connecting
 * the two circles are added in this class.  The whole is kept up-to-date by using the
 * method {@link #setTransform(double[])} and then {@link #update()}.
 * @author Charles Gunn
 *
 */

public  class StarLinesFactory		{
	private static final int channel = 40, channel2 = 50;
	// following variables are used by the StartLinesFactory
	static double[] projPoint = {0,0,1};
	static double[][] planeEquations = {{1,0,0,0},{0,1,0,0},{0,0,1,0}};
	static Matrix movePlane = new Matrix(), expandSphere = new Matrix();
	private double tubeRadius = .003;
	int samples = 15;
	int curves = 3;		// first attempt: three orthogonal great circles of sample rays
	IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
	int[][] indices = new int[curves*samples][2];
	double[][] verts = new double[curves*samples][3];
	Color[] edgeColors = new Color[curves*samples];
	double[] transform = Rn.identityMatrix(4);
	SceneGraphComponent sgc, star, plane, sphere, sphereIcon;
	CircleFactory[] circles = new CircleFactory[curves];
	Color starColor = new Color(channel, channel, channel2); //new Color(channel2, 130,0);
	Color[] starColors = {new Color(channel, channel, channel2), new Color(channel,channel2,channel), new Color(channel2,channel,channel)};
	public StarLinesFactory()	{
		sgc = new SceneGraphComponent("star root");
		star = new SceneGraphComponent("star");
		plane = new SceneGraphComponent("plane");
		sphere = new SceneGraphComponent("sphere");
		sphereIcon = new SceneGraphComponent("sphereIcon");
		sgc.addChildren(star, sphereIcon);
		MatrixBuilder.euclidean().translate(0,0,1).scale(.05).assignTo(sphereIcon);
		sphereIcon.setGeometry(new Sphere());
		Appearance ap = new Appearance();
		sphereIcon.setAppearance(ap);
		ap.setAttribute("polygonShader.diffuseColor", starColor);
		ap = new Appearance();
		sgc.setAppearance(ap);
		MatrixBuilder.euclidean().translate(0,0,1).scale(2).translate(0,0,-1).assignTo(movePlane);
		movePlane.assignTo(star);
		movePlane.assignTo(plane);
		expandSphere.assignTo(sphere);
		ap.setAttribute("lineShader.diffuseColor", starColor);
		ap.setAttribute("lineShader.ambientColor", Color.white);
//		ap.setAttribute("lineShader.ambientCoefficient", .2);
		ap.setAttribute("lineShader.specularCoefficient", 0.0);
		ap.setAttribute("lineShader.tubeRadius", tubeRadius);
		ap.setAttribute("lineShader.lineWidth", 2.0);
		ap.setAttribute(EDGE_DRAW, true);
//		ap.setAttribute(TUBES_DRAW, true);
		sgc.addChildren(plane, sphere);
		for (int i = 0; i<3; ++i)	{
			circles[i] = new CircleFactory();
			circles[i].setPlaneEquation(planeEquations[i]);
			circles[i].update();
//			circles[i].getSceneGraphComponent().getAppearance().setAttribute("lineShader.diffuseColor", starColors[i]);
			circles[i].getPlaneSGC().getAppearance().setAttribute("lineShader.diffuseColor", starColors[i]);
			plane.addChild(circles[i].getPlaneSGC());
			circles[i].getSphereSGC().getAppearance().setAttribute("lineShader.diffuseColor", starColors[i]);
			circles[i].getSphereSGC().getAppearance().setAttribute("lineShader.tubeRadius", 2*tubeRadius);
//			circles[i].getSphereSGC().getAppearance().setAttribute(CommonAttributes.RADII_WORLD_COORDINATES, true);
			sphere.addChild(circles[i].getSphereSGC());
			for (int j = 0; j<samples; ++j)	{
				double angle = (j*Math.PI*2.0)/(samples);
				int ind = i*samples+j;
				verts[ind][i] = Math.cos(angle);
				verts[ind][(i+1)%3] = Math.sin(angle);
				verts[ind][(i+2)%3] = 0.0;
				indices[ind][0] = ind;
				indices[ind][1] = ind+curves*samples;
				edgeColors[ind] = starColors[(i+2)%3];
			}
		}
		ilsf.setVertexCount(2*verts.length);
		ilsf.setEdgeCount(indices.length);
		ilsf.setEdgeIndices(indices);
		ilsf.setEdgeColors(edgeColors);
		update();
		IndexedLineSet currentStar = ilsf.getIndexedLineSet();
		star.setGeometry(currentStar);
	}
	
	public void setTransform(double[] t)	{
		transform = t;
	}
	
	public SceneGraphComponent getSceneGraphComponent()	{
		return sgc;
	}
	
	public void update()	{
		int n = curves*samples;
		for (int i = 0; i<curves; ++i)	{
			double[] newP = Rn.matrixTimesVector(null, transform, planeEquations[i]);
			circles[i].setPlaneEquation(newP);
			circles[i].update();
		}
		double[][] newVerts = new double[n*2][3];
		Rn.matrixTimesVector(newVerts, transform, verts);
		for (int i = 0; i<curves*samples; ++i)	{
			newVerts[i] = CircleFactory.stereoProj(newVerts[i], newVerts[i]);
			newVerts[i][2] = 1.0;
			double length = Rn.euclideanNorm(newVerts[i]);
			double factor = .1/length;
			newVerts[i][2] = 0.0;
			Rn.linearCombination(newVerts[i+n], 1+factor, projPoint, -factor, newVerts[i]);
		}
		ilsf.setVertexCoordinates(newVerts);
		ilsf.update();
	}
}
