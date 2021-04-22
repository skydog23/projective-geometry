/*
 * Author	gunn
 * Created on Jul 29, 2005
 *
 */
package charlesgunn.jreality;

import java.util.logging.Level;

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.util.CameraUtility;
import de.jreality.util.LoggingSystem;
import de.jreality.util.Rectangle3D;

/**
 * @author gunn
 *
 */
public class MiscellaneousCodeContainer {

	/**
	 * 
	 */
	public MiscellaneousCodeContainer() {
		super();
		// TODO Auto-generated constructor stub
	}
    /**
	 * @deprecated  This encompass method is adapted from de.jreality.soft.MouseTool.
	 * 
	 */
	static boolean debug = false;
	public static void encompass2(Viewer v) {
		SceneGraphPath cameraPath = v.getCameraPath();
		SceneGraphComponent root = v.getSceneRoot();
		if (root == null) return;
		Transformation transformation = null;
		if(cameraPath.getLength()!= 0)
            transformation = cameraPath.getLastComponent().getTransformation();

        if (transformation == null)
            return;
//        BoundingBoxTraversal bt =new BoundingBoxTraversal();
        	double[] tmp = cameraPath.getInverseMatrix(null);
//        bt.setInitialMatrix(tmp);
//        bt.traverse(root);
//        double[] center = new double[3];
//        bt.getBoundingBoxCenter(center);
        Rectangle3D bbox = BoundingBoxUtility.calculateBoundingBox(tmp, root);
        double[] center = bbox.getCenter();
        if (debug) LoggingSystem.getLogger(CameraUtility.class).log(Level.FINER,"encompass center "+center[0]+" "+center[1]+" "+center[2]);
        
       Camera camera = CameraUtility.getCamera(v);
        double fl = 1/Math.tan((Math.PI/180.0)*(camera).getFieldOfView()/2);
        double[] extent = bbox.getExtent();
        double distance = 0.5*(extent[2]);
        double wc = .5*(extent[0])*fl;
        double hc = .5*(extent[1])*fl;

        distance += Math.max(wc,hc);
       if (debug) LoggingSystem.getLogger(CameraUtility.class).log(Level.FINER,"dist"+wc+" "+hc);
        
        center[2] += distance;
        P3.makeTranslationMatrix(tmp, center, Pn.EUCLIDEAN);
        transformation.multiplyOnRight(tmp); 
        //transformation.setTranslation(center);
             
        tmp = cameraPath.getInverseMatrix(null);
        bbox = BoundingBoxUtility.calculateBoundingBox(tmp, root);
        if (debug) LoggingSystem.getLogger(CameraUtility.class).log(Level.FINER,"New bbox is "+bbox.toString());
        double d = -bbox.getMinZ();
        camera.setFar(10*d);
        d = -bbox.getMaxZ();
       camera.setNear(.1*d);
        if (debug) LoggingSystem.getLogger(CameraUtility.class).log(Level.FINER,"far "+camera.getFar()+" near "+camera.getNear());
	}
	
	static double[] zdirection = {0,0,2};
	public static double[] reflectIntoInside(double[] dst, double[] src, double[][] polygon, int metric) {
		if (src.length != 3)
			throw new IllegalArgumentException("only support 3-vectors now");
		if (dst == null) dst = new double[3];
		double[] det = new double[9];
		for (int i = 0; i< src.length; ++i) dst[i] = src[i];
		boolean stillHappening = true;
		while (stillHappening)	{
			stillHappening = false;
			for (int i = 0; i < polygon.length; ++i) {
				int j = (i+1)%polygon.length;
				System.arraycopy(polygon[i], 0, det, 0, 3);
				System.arraycopy(polygon[j], 0, det, 3, 3);
				System.arraycopy(src, 0, det, 6, 3);
				double determinant = Rn.determinant(det);
//				System.err.println("Det = "+determinant);
				if (determinant > 0)	{
//					System.err.println("Reflecting");
					double[] plane = P3.planeFromPoints(null, polygon[i], polygon[j], zdirection);
					double[] refl = P3.makeReflectionMatrix(null, plane, metric);	
					Rn.matrixTimesVector(dst, refl, dst);
					stillHappening = true;
				}
			
			}
			stillHappening = false;
		}
		return dst;
	}

	public static boolean isPositiveMultipleIdentityMatrix(double[] mat, double tol)	{
		if (mat[0] <= 0.0) return false;
		double scale = 1.0/mat[0];
		double[] mm = Rn.times(null, scale, mat);
		return Rn.isIdentityMatrix(mm, tol);
	}


//	no more reference to world coordinates in the following routines
//	as there is no longer anything special about that coordinate system
// * Project the 2D point \IT{pt} in NDC, for example a mouse position, 
// * into a line in camera coordinates
// * whose intersections with the near and far clipping planes are
// * \IT{nearPt} and \IT{farPt}  respectively.
//	public static void projectPointToLine(Camera cam, double[] pt, double[] nearPt, double[] farPt)	{
//		if (pt.length != 2 || nearPt.length != 3 || farPt.length != 3)	{
//			throw new IllegalArgumentException("Invalid dimensions");
//		}
//		double[] pt3 = new double[3];
//		Rn.setToValue(pt3, pt[0], pt[1], -1.0);
//		Rn.matrixTimesVector(nearPt, Rn.inverse(null, CameraUtility.getCameraToNDC( )), pt3);
//		Rn.setToValue(pt3, pt[0], pt[1], 1.0);
//		Rn.matrixTimesVector(farPt, Rn.inverse(null, CameraUtility.getCameraToNDC(cam, cam.aspectRatio, CameraUtility.MIDDLE_EYE)), pt3);
//	}
// 
// * Convert the point \IT{inV} in camera coordinates to the
//	  * point \IT{outV} in NDC coordinates.  The result
//	  * is not dehomogenized.
//	public static void projectToScreen(Camera cam, double[] inV, double[] outV)	{
//		if (inV.length != 3 || outV.length != 3)	{
//			throw new IllegalArgumentException("Invalid dimensions");
//		}
//		Rn.matrixTimesVector(outV, CameraUtility.getCameraToNDC(cam, cam.aspectRatio, CameraUtility.MIDDLE_EYE), inV);
//	}
//	
//	* How large does a sphere centered at the point \IT{center},
//	* in camera coordinates, need to be in order to project to a circle of radius
//	* \IT{aRadius} in screen (NDC) coordinates?
//	* Makes a guess and returns it.
//	public static double getCameraToScreenScale(Camera cam, double[] center, double r)	{
//		if (center.length != 3)	{
//			throw new IllegalArgumentException("Invalid dimensions");
//		}
//		double[] tmp, tmp2, tmp3;
//		double d;
//		tmp = Rn.matrixTimesVector(null, CameraUtility.getCameraToNDC(cam, cam.aspectRatio, CameraUtility.MIDDLE_EYE), center);
//		tmp2 = Rn.copy(null, tmp);
//		tmp2[0] += r;
//		tmp3 = Rn.matrixTimesVector(null, Rn.inverse(null, CameraUtility.getCameraToNDC(cam, cam.aspectRatio, CameraUtility.MIDDLE_EYE)), tmp2);
//		d = Rn.euclideanDistance(tmp3, center);
//		return d;
//	}

}
