/*
 * Author	gunn
 * Created on Aug 8, 2005
 *
 */
package charlesgunn.jreality;

import de.jreality.math.Pn;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphPath;

public class CameraUtilityOverflow {

	private CameraUtilityOverflow() {
	}

	public static void reset(Camera camera, int metric) {
		double near = 0, far = 0, fieldOfView = 0, focus = 0;
		switch (metric) {
		case Pn.EUCLIDEAN:
			near = .1;
			far = 50.0;
			fieldOfView = (60.0);
			focus = 3.0;
			break;
		case Pn.HYPERBOLIC:
			near = .01;
			far = 100.0;
			fieldOfView = (60.0);
			focus = 2.5;
			break;
		case Pn.ELLIPTIC:
			near = .01;
			far = -.05;
			fieldOfView = (60.0);
			focus = 0.5;
			break;
		}
		camera.setNear(near);
		camera.setFar(far);
		camera.setFocus(focus);
		camera.setFieldOfView(fieldOfView);
//		camera.setPerspective(true);
//		camera.update();
	}

	  public static boolean isCameraPathValid(SceneGraphPath cameraPath) {
	      return cameraPath != null
	      && (cameraPath.isValid()
	      && (cameraPath.getLastElement() instanceof Camera));
  }

}
