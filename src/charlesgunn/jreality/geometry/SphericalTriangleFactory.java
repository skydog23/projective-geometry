package charlesgunn.jreality.geometry;

import java.awt.Color;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

public class SphericalTriangleFactory {

	// the sides are considered to depend on the verts
	// if you want the verts to depend on the sides, then you'll
	// have to use the dual triangle
	final static double[][] defaultVerts = {{1,0,0},{0,1,0},{0,0,1}};
	double[][] verts = {{1,0,0},{0,1,0},{0,0,1}}, 
		sides = {{1,0,0},{0,1,0},{0,0,1}},
		edgeverts = new double[4][3];
	static int[][] edgeInd = {{0,3},{1,3},{2,3}};
	double[] sidelengths, angles;
	boolean vertsChanged = true;
	static IndexedLineSet baseCircle = IndexedLineSetUtility.circle(100);
	static IndexedFaceSet baseDisk = IndexedFaceSetUtility.constructPolygon(
			baseCircle.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null));
	SceneGraphComponent all = new SceneGraphComponent("triangle"),
		sSGC = new SceneGraphComponent("sides");
	SceneGraphComponent[] sidesSGC = new SceneGraphComponent[3];
	SceneGraphComponent vertSGC  = new SceneGraphComponent("verts");
	SceneGraphComponent edgesSGC = new SceneGraphComponent("edges");
	PointSetFactory verticesPSF = new PointSetFactory();
	IndexedLineSetFactory edgesIFSF = new IndexedLineSetFactory();
	static double[] zaxis = {0,0,1};
	static double[] origin = {0,0,0};
	boolean isDual = true;
	static Color[] colors = {
		new Color(255,0,0),		// red
		new Color(0,255,0),		// oorange
		new Color(255,255,0),	// yellow
		new Color(255,0,255),	// green
		new Color(0,0, 255),	// blue
		new Color(255,140,0)};	// orange
	static Color[] vertsOrig = {colors[0], colors[2], colors[4]},
		vertsDual = {colors[1], colors[3], colors[5]};
	
	public SphericalTriangleFactory()	{
		for (int i = 0; i<3; ++i) {
			sidesSGC[i] = new SceneGraphComponent("s"+i);
			sSGC.addChild(sidesSGC[i]);
			sidesSGC[i].setGeometry(baseDisk);
			sidesSGC[i].setAppearance(new Appearance());
			sidesSGC[i].getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", colors[2*i+1]);
			sidesSGC[i].getAppearance().setAttribute("polygonShader.diffuseColor", colors[2*i+1]);
		}
		sSGC.setAppearance(new Appearance());
		sSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		sSGC.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		sSGC.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .025);
		
		all.addChildren(sSGC, vertSGC, edgesSGC);
//		transpSphereSGC.setGeometry(SphereUtility.tessellatedIcosahedronSphere(SphereUtility.SPHERE_FINEST));
//		Appearance ap = new Appearance();
//		transpSphereSGC.setAppearance(ap);
//		transpSphereSGC = Primitives.wireframeSphere(80,40);
//		Appearance ap = transpSphereSGC.getAppearance();
//		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
//		dgs.setShowFaces(false);
//		dgs.setShowLines(true);
//		dgs.setShowPoints(false);
		
		verticesPSF.setVertexCount(3);
		verticesPSF.setVertexCoordinates(verts);
		verticesPSF.setVertexColors(vertsOrig);
		verticesPSF.update();
		vertSGC.setGeometry(verticesPSF.getPointSet());
		vertSGC.setAppearance(new Appearance());
		vertSGC.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, 0.05);
		vertSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		sSGC.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .02);
		vertSGC.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, 0.04);
		edgesIFSF.setVertexCount(4);
		for (int i = 0; i<3; ++i) edgeverts[i] = verts[i];
		edgesIFSF.setVertexCoordinates(edgeverts);
		edgesIFSF.setEdgeCount(3);
		edgesIFSF.setEdgeIndices(edgeInd);
		edgesIFSF.setEdgeColors(vertsOrig);
		edgesIFSF.update();
		edgesSGC.setGeometry(edgesIFSF.getIndexedLineSet());
		edgesSGC.setAppearance(sSGC.getAppearance());
	}
	public SceneGraphComponent getSceneGraphComponent()	{
		return all;
	}
	
	public SceneGraphComponent getVerticesSGC()	{
		return vertSGC;
	}
	
	public void update()	{
		if (vertsChanged) {
//			System.err.println("Updating, dual = "+isDual+" "+Rn.toString(verts));
			verticesPSF.setVertexCoordinates(verts);
			verticesPSF.update();
			for (int i = 0; i<3; ++i) edgeverts[i] = verts[i];
			edgesIFSF.setVertexCoordinates(edgeverts);
			edgesIFSF.update();
			for (int i = 0; i<3; ++i) {
				Rn.crossProduct(sides[i], verts[(i+1)%3], verts[(i+2)%3]);
				Rn.normalize(sides[i], sides[i]);
				MatrixBuilder.euclidean().rotateFromTo(zaxis, sides[i]).assignTo(sidesSGC[i]);
			}
			vertsChanged = false;
		}
		
	}
	public void reset()	{
		for (int i = 0; i<3; ++i)	{
			System.arraycopy(defaultVerts[i], 0, verts[i], 0, 3);
		}
		vertsChanged = true;
		update();
	}
	
	public boolean isDual() {
		return isDual;
	}
	public void setDual(boolean showDual) {
		this.isDual = showDual;
		if (isDual) {
			all.setName("dualTriangle");
			sSGC.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
			sSGC.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, .1);
			sSGC.getAppearance().setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, true);
			sSGC.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .01);
			vertSGC.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, 0.02);
		}
		for (int i = 0; i<3; ++i)	{
			sidesSGC[i].getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", isDual? colors[2*i] : colors[2*i+1]);			
			sidesSGC[i].getAppearance().setAttribute("polygonShader.diffuseColor", isDual? colors[2*i] : colors[2*i+1]);			
		}
		edgesIFSF.setEdgeColors(isDual ? vertsDual : vertsOrig);
		edgesIFSF.update();
		verticesPSF.setVertexColors(isDual ? vertsDual : vertsOrig);
		verticesPSF.update();
	}
	public double[][] getSides() {
		return sides;
	}
	public double[][] getVerts() {
		return verts;
	}
	public void setVerts(double[][] verts) {
		if (verts[0].length == 3)  this.verts = verts;
		else this.verts = Pn.dehomogenize(new double[3][3], verts);
		vertsChanged = true;
	}
	public void setSides(double[][] sides) {
		this.sides = sides;
	}
	
	static IndexedLineSet circle = IndexedLineSetUtility.circle(20);
	static IndexedFaceSet disk = IndexedFaceSetUtility.constructPolygon(
			circle.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null));
	public static SceneGraphComponent dualPlane(SceneGraphComponent sgc, double[] point, int segs)	{

		if (sgc == null) sgc = SceneGraphUtility.createFullSceneGraphComponent("dualPlane");
		MatrixBuilder.euclidean().rotateFromTo(zaxis, point).assignTo(sgc);
		if (sgc.getGeometry() != disk) sgc.setGeometry(disk);
		return sgc;
	}
}
