package charlesgunn.jreality.worlds.projective;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class FourSimplexInS3 extends Assignment {

	private double[][] verts;
	private Graphics3D gc;
	private SceneGraphComponent world;

	@Override
	public void display() {
		super.display();
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.METRIC, Pn.ELLIPTIC);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.RENDER_S3, true);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.USE_GLSL, true);
		System.err.println("root app name = "+viewer.getSceneRoot().getAppearance().getName());
		Camera cam = CameraUtility.getCamera(viewer);
		cam.setNear(.01);
		cam.setFar(-.01);
		gc = new Graphics3D(viewer);
		gc.setCurrentPath(new SceneGraphPath(viewer.getSceneRoot(), world));
		printndc();
		((Component) viewer.getViewingComponent()).addKeyListener( new KeyAdapter()	{
			public void keyPressed(KeyEvent e)	{ 
				switch(e.getKeyCode())	{
					
				case KeyEvent.VK_1:
					printndc();
					break;
			}

			}
		});
	}

	private void printndc() {
		double[] world2ndc = gc.getObjectToNDC();
		double[][] vertsndc = Rn.matrixTimesVector(null, world2ndc, verts);
		for (int i = 0; i<vertsndc.length; ++i) {
			double[] sphndc = Rn.matrixTimesVector(null, vertsndc[i][3] > 0 ? JOGLRenderer.frontZBuffer : JOGLRenderer.backZBuffer, vertsndc[i]);
			Rn.times(vertsndc[i], 1.0/Math.abs(sphndc[3]), sphndc);
		}
		System.err.println("Verts ndc = "+Rn.toString(vertsndc));
	}

	@Override
	public SceneGraphComponent getContent() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		world.getAppearance().setAttribute(CommonAttributes.METRIC, Pn.ELLIPTIC);
		world.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		world.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		world.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW, false);
		world.getAppearance().setAttribute(CommonAttributes.TEXT_SCALE, .008);
		world.getAppearance().setAttribute(CommonAttributes.TEXT_ALIGNMENT, SwingConstants.BOTTOM);
		world.getAppearance().setAttribute(CommonAttributes.TEXT_OFFSET, new double[]{0,0,0});
		
		double a = Math.sqrt(5.0)/4;
		verts = new double[][] {{0,0,0,-1},{-a,-a,-a,.25},{-a,a,a,.25},{a,-a,a,.25},{a,a,-a,.25}};
		int[][] edges = {{0,1},{0,2},{0,3},{0,4},{1,2},{1,3},{1,4},{2,3},{2,4},{3,4}};
		Color[] edgeC = {
				Color.black,
				Color.blue,
				Color.green,
				Color.cyan,
				Color.pink,
				Color.red,
				Color.orange,
				Color.yellow,
				Color.magenta,
				Color.gray
		};
		IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
		ilsf.setVertexCount(5);
		ilsf.setVertexCoordinates(verts = Rn.times(null, -1, verts));
		ilsf.setEdgeCount(10);
		ilsf.setEdgeIndices(edges);
		ilsf.setEdgeColors(edgeC);
		ilsf.setGenerateVertexLabels(true);
		ilsf.setGenerateEdgeLabels(true);
		ilsf.update();
		IndexedLineSet ils = ilsf.getIndexedLineSet();
//		ils = IndexedLineSetUtility.refine(ils, 8);
		IndexedLineSet simplex = ils;
		world.setGeometry(simplex);
		return world;
	}

	public static void main(String[] args) {
		new FourSimplexInS3().display();
	}
}
