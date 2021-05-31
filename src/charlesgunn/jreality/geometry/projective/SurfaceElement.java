/*
 * Created on Oct 7, 2005
 *
 */
package charlesgunn.jreality.geometry.projective;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.LoggingSystem;
import de.jreality.util.SceneGraphUtility;

public class SurfaceElement {
	double[] point, plane;
	Color pointColor, planeColor;
	SceneGraphComponent theRepn = null;
	static double diskRadiusDefault = .3;
	double diskRadius = diskRadiusDefault;
	int metric = Pn.EUCLIDEAN;
	
	public SurfaceElement(double[] pt, double[] pl)	{
		super();
		point = pt;
		plane = pl;
		theRepn = SurfaceElement.surfaceElement(theRepn, point, plane, diskRadiusDefault, Pn.PROJECTIVE);
	}
	/**
	 * @return Returns the plane.
	 */
	public double[] getPlane() {
		return plane;
	}
	/**
	 * @return Returns the point.
	 */
	public double[] getPoint() {
		return point;
	}
	/**
	 * @return Returns the planeColor.
	 */
	public Color getPlaneColor() {
		return planeColor;
	}
	/**
	 * @param planeColor The planeColor to set.
	 */
	public void setPlaneColor(Color planeColor) {
		this.planeColor = planeColor;
	}
	/**
	 * @return Returns the pointColor.
	 */
	public Color getPointColor() {
		return pointColor;
	}
	/**
	 * @param pointColor The pointColor to set.
	 */
	public void setPointColor(Color pointColor) {
		this.pointColor = pointColor;
	}
	
	
	public double getDiskRadius() {
		return diskRadius;
	}
	
	public void setDiskRadius(double diskRadius) {
		this.diskRadius = diskRadius;
	}
	
	public static void setDiskRadiusDefault(double diskRadius) {
		SurfaceElement.diskRadiusDefault = diskRadius;
		setupDisk();
	}
	
	public SurfaceElement polarize( int metric)	{
		double[] polarPoint = Pn.polarize(null, plane, metric);
		double[] polarPlane = Pn.polarize(null, point, metric);
		if (Pn.innerProduct(point, point, metric) > 0.0)		// "outside" hyperbolic space
			Rn.times(polarPlane, -1.0, polarPlane);
		Pn.normalize(polarPoint, polarPoint, metric);
		Pn.normalize(polarPlane, polarPlane, metric);
		SurfaceElement polar = new SurfaceElement(polarPoint, polarPlane);
		polar.setPlaneColor(pointColor);
		polar.setPointColor(planeColor);
		return polar;
	}
	/**
	 * @return Returns the theRepn.
	 */
	public SceneGraphComponent getRepresentation() {
		if (planeColor != null)
			theRepn.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, planeColor );
		else if (pointColor != null)
			theRepn.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, pointColor );
		return theRepn;
	}
	public String toString()	{
		return "Plane: "+Rn.toString(plane)+"\nPoint: "+Rn.toString(point);
	}
	
	public void validate()		{
		double dot = Rn.innerProduct(point, plane);
		if (Math.abs(dot) > 10E-8)	{
			Object[] result = new Object[1];
			result[0] = new Double(dot);
			LoggingSystem.getLogger(SurfaceElement.class).warning("Bad incidence, point and plane");
		}

	}
	static IndexedFaceSet disk  = null;
	static double[] zaxis = {0,0,1};
	public static SceneGraphComponent smallDisk = null;
	static Transformation diskTransform = new Transformation();
	static int sides = 32;
	static {
		setupDisk();
//		MatrixBuilder.euclidean().scale(1.0).assignTo(diskTransform);
//		smallDisk.setTransformation(diskTransform);
	}

	private static void setupDisk() {
		double[][] coords = new double[sides][3];
		for (int i = 0; i<sides; ++i)	{
			double angle = i*2*Math.PI/sides;
			coords[i][0] = diskRadiusDefault*Math.cos(angle);
			coords[i][1] = diskRadiusDefault*Math.sin(angle);
			coords[i][2] = 0.001;
		}
		disk = IndexedFaceSetUtility.constructPolygon(coords);
		smallDisk = new SceneGraphComponent();
		smallDisk.setGeometry(disk);
	}
	// TODO figure out how to make this thread safe!
	private static double[] rot = new double[16],
		translation = new double[16],
		tmp = new double[16];
	
	public static SceneGraphComponent surfaceElement(SceneGraphComponent existing, double[] pointElement, double[] planeElement, double radius, int metric)	{
		double d = Rn.innerProduct(pointElement, planeElement);
//		if (Math.abs(d) > 10E-10)  
//			throw new IllegalStateException("point and plane not incident "+d);
		SceneGraphComponent theDisk;
		if (existing != null) {
			theDisk = existing;
		}
		else  {
			theDisk = SceneGraphUtility.createFullSceneGraphComponent("disk");
			theDisk.getAppearance().setAttribute("polygonShader.vertexShader","simple");
			theDisk.getAppearance().setAttribute("lineShader","simple");
		}
		if (pointElement[3] == 0.0)  {
			theDisk.setVisible(false);
			return theDisk;
		}
		theDisk.setVisible(true);
		double[] plane = planeElement.clone();
//		if (plane[3] < 0) Rn.times(plane, -1, plane);
		P3.makeRotationMatrix(rot, zaxis, plane);
		double[] point = pointElement.clone();
//		if (point[3] < 0) Rn.times(point, -1, point);
		P3.makeTranslationMatrix(translation, point, Pn.EUCLIDEAN);
		Rn.times(tmp, translation, rot);
		theDisk.getTransformation().setMatrix(tmp); //Rn.times(tmp2, tmp, stretch));
		if (theDisk.getChildComponentCount() == 0) 			theDisk.addChild(smallDisk);
	
		else if (theDisk.getChildComponent(0) != smallDisk) {
			SceneGraphUtility.removeChildren(theDisk);
			theDisk.addChild(smallDisk);
		}
		Pn.normalize(point, point, Pn.HYPERBOLIC);
		Pn.normalize(plane, plane, Pn.HYPERBOLIC);
		return theDisk;
	}
	
	public void update() {
		
	}
	public static SurfaceElement[] getPlaneBundle(double c, int l)	{
//		 now calculate the polar of this
		// the points are the polars of the original planes; the planes are the polars of the original points
		double[][] verts;
		double[][] planes;
		double[] centerPoint;
		verts = SphereUtility.tessellatedIcosahedronSphere(l, true).getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		int n = verts.length;
		SurfaceElement[] planeBundle = new SurfaceElement[n];
		double[][] planesAtOrigin = new double[n][4];
		planes = new double[n][4];
		double[] tmp = new double[3], tmp2 = new double[3];
		centerPoint = new double[]{0, c,0, 1};
		// It's important that the translation be hyperbolic since that's the ruling quadric here
		// (How does this interpolation work in euclidean space?  Or does it work?
		int sig = Pn.HYPERBOLIC;
		double[] transform = P3.makeTranslationMatrix(null, centerPoint, sig);
		for (int i = 0; i<n; ++i)		{
			System.arraycopy(verts[i], 0, planesAtOrigin[i],0,3);
			planesAtOrigin[i][3] = 0.0;
		}
		double[] itransform = Rn.inverse(null, transform);
		Rn.matrixTimesVector(planes, itransform, planesAtOrigin);
		Pn.normalize(planes, planes,sig);
		Pn.normalize(centerPoint, centerPoint,sig);
		for (int i = 0; i<n; ++i)	{
			planeBundle[i] = new SurfaceElement(centerPoint.clone(), planes[i]);
			double max = Rn.maxNorm(verts[i]);
			if (max != 0) max = .99/max;
			Rn.times(tmp, max, verts[i]);
			for (int j = 0; j<3; ++j)	tmp[j] = .5 + .5 * tmp[j];
			Color f = new Color((float) tmp[0], (float) tmp[1], (float) tmp[2]);
			planeBundle[i].setPlaneColor(f);
		}
		return planeBundle;
	}
	
	public static SurfaceElement[] getPlanePencil(double c, int nodes, int leaves, double beginAngle, double length, double middle)	{
//		 now calculate the polar of this
		// the points are the polars of the original planes; the planes are the polars of the original points
		double[][] planesAtOrigin;
		double[][] planes;
		double[] centerPoint = null;
		planesAtOrigin = new double[leaves][4];
		for (int i = 0; i<leaves; ++i)	{
			double angle =  Math.PI * (2*i+beginAngle)/(leaves);
			planesAtOrigin[i] = new double[4];
			planesAtOrigin[i][0] = Math.cos(angle);
			planesAtOrigin[i][1] = Math.sin(angle);
			planesAtOrigin[i][2] = .00001;
			planesAtOrigin[i][3] = 0.0;
		}
		int m = nodes;
		int k = planesAtOrigin.length;
		int n = k* m;
		SurfaceElement[] planeBundle = new SurfaceElement[n];
		planes = new double[n][4];
		// It's important that the translation be hyperbolic since that's the ruling quadric here
		// (How does this interpolation work in euclidean space?  Or does it work?
		double lim = (m==1)? 0.0 : length;
		int sig = Pn.HYPERBOLIC;
		for (int j = 0; j< m; ++j)	{
			double s = (m>1) ? (j/(m-1.0)) : 1.0;
			double y = middle -lim + 2*lim*s;
			centerPoint = new double[]{0,c,y, 1};
			Pn.normalize(centerPoint, centerPoint, sig);
			double[] transform = P3.makeTranslationMatrix(null, centerPoint, Pn.EUCLIDEAN);
			double[] itransform =  Rn.transpose(null, Rn.inverse(null, transform));
			for (int i = 0; i<k; ++i)		{
				double t = (i/(k-1.0));
//				System.arraycopy(verts[i], 0, planesAtOrigin[i],0,3);
//				planesAtOrigin[i][3] = 0.0;
				Rn.matrixTimesVector(planes[i+j*k], itransform, planesAtOrigin[i]);
				Pn.normalize(planes[i+j*k], planes[i+j*k], sig);
				planeBundle[i+j*k] = new SurfaceElement(centerPoint.clone(), planes[i+j*k]);
//				System.out.println(i+" "+planeBundle[i+j*k].toString());
				Color f = new Color(  (float) (.2+.8*s), (float) (.2+.8*t), (float) 0.0);
				planeBundle[i+j*k].setPlaneColor(f);
			}	
		}
		return planeBundle;
	}
	static double[][] cubePlanes = {{-1,0,0,1},{1,0,0,1},{0,-1,0,1},{0,1,0,1},{0,0,-1,1},{0,0,1,1}};
	static double[][] cubeVertices = {{1,1,1},{-1,1,1},{1,-1,1},{-1,-1,1},{1,1,-1},{-1,1,-1},{1,-1,-1},{-1,-1,-1}};
	static Color[] faceColors = {Color.RED, Color.GREEN, Color.YELLOW, new Color(255,0,255),Color.BLUE, new Color(255, 155,0)};
	static int[][] lookup = {{0,1},{0,-1},{1,1},{1,-1},{2,1},{2,-1}};
	
	public static SurfaceElement[] getCube(double scale) {
		SurfaceElement[] cubeEls = new SurfaceElement[24];
		int count = 0;
		for (int i = 0; i<6; ++i)	{
			double[] plane = cubePlanes[i];
			plane[3] *= scale;
			int index = lookup[i][0];
			double val =  lookup[i][1];
			for (int j=0;j<8;++j)	{
				if (cubeVertices[j][index] == val) {
					double[] point = Pn.homogenize(null, Rn.times(null, scale, cubeVertices[j]));
					Pn.normalize(point, point, Pn.HYPERBOLIC);
					Pn.normalize(plane, plane, Pn.HYPERBOLIC);
					cubeEls[count] = new SurfaceElement( point, plane);
					cubeEls[count].setPlaneColor(faceColors[i]);
					count++;
				}
			}
		}
		return cubeEls;
	}


}
