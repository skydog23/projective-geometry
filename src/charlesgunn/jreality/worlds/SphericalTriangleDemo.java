package charlesgunn.jreality.worlds;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import charlesgunn.jreality.geometry.FullSphericalTriangleFactory;
import charlesgunn.jreality.geometry.SphericalTriangleFactory;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.util.CameraUtility;

public class SphericalTriangleDemo extends LoadableScene {

	private SceneGraphComponent world;
	boolean split = false;
	private FullSphericalTriangleFactory fstf;
	SceneGraphComponent 		transpSphereSGC = Primitives.wireframeSphere(80,40);
	@Override
	public SceneGraphComponent makeWorld() {
		SphericalTriangleFactory stf = new SphericalTriangleFactory();
		stf.update();
		fstf = new FullSphericalTriangleFactory(stf);
		world = fstf.getSceneGraphComponent();
		transpSphereSGC.setGeometry(SphereUtility.tessellatedIcosahedronSphere(
				SphereUtility.SPHERE_SUPERFINE));
		world.addChild(transpSphereSGC);
		return world;
	}

	@Override
	public boolean addBackPlane() {
		return true;
	}

	public boolean isEncompass() {return true; }
	@Override
	public Component getInspector(Viewer v) {
		JPanel mypanel = new JPanel();
		mypanel.setName("ReadMe");
		JTextArea textarea = new JTextArea(10,20);
		textarea.setEditable(false);
		textarea.append("This application shows a spherical triangle\n");
		textarea.append("and its polar.\n");
		textarea.append("You can drag the vertices of the triangle.\n"+
				"The vertices are colored red, yellow, and blue.\n" +
				"The polar triangle is updated automatically.\n\n"+
				"The sides are orange, green, and violet.\n"+
				"The polar triangle is colored ... polarly.\n"+
				"It is drawn with with smaller features\n"+
				"to distinguish it from the original triangle.\n"+
				"\n"+
				"The following key strokes also have effects:\n"+
				"    '1':    toggle visibility of triangle.\n"+
				"    '2':    toggle visibility of polar triangle.\n"+
				"    '3':    toggle split view.\n"+
				"    '4':    toggle wireframe sphere\n\n"+
				"    '5':    reset.\n\n"+
				"    'h':    display general help overlay for viewer.\n"+
				"\nIf the cursor isn't over a vertex, \n"+
				"then dragging with the mouse rotates the object.\n"+
				"Use mouse click wheel to zoom in and out.\n"+
				"Shift-cntl-f  toggles fullscreen mode.\n"+
				"\nClick on the tab 'Scene Graph' to explore structure\n"+
				"\nAuthor: Charles Gunn\n"+
				"    gunn at math.tu-berlin.de\n");
		mypanel.add(textarea);
		return mypanel;
	}

	@Override
	public boolean hasInspector() {
		return true;
	}

	@Override
	public void customize(JMenuBar menuBar, final Viewer viewer) {
		((Component) viewer.getViewingComponent()).addKeyListener( new KeyAdapter()	{
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode())	{
				
				case KeyEvent.VK_1:
					world.getChildComponent(0).setVisible(!world.getChildComponent(0).isVisible());
					break;

				case KeyEvent.VK_2:
					world.getChildComponent(1).setVisible(!world.getChildComponent(1).isVisible());					
					break;

				case KeyEvent.VK_3:
					split = !split;
					MatrixBuilder.euclidean().assignTo(world);
					if (split)	{
						MatrixBuilder.euclidean().translate(-1.5,0,0).assignTo(fstf.getTriangle().getSceneGraphComponent());
						MatrixBuilder.euclidean().translate(1.5,0,0).assignTo(fstf.getDualTriangle().getSceneGraphComponent());						
					} else {
						MatrixBuilder.euclidean().assignTo(fstf.getTriangle().getSceneGraphComponent());
						MatrixBuilder.euclidean().assignTo(fstf.getDualTriangle().getSceneGraphComponent());						
					}
					transpSphereSGC.setVisible(!split);
					CameraUtility.encompass(viewer);
					break;

				case KeyEvent.VK_5:
					fstf.getTriangle().reset();
					fstf.update();
					break;
					
				case KeyEvent.VK_4:
					transpSphereSGC.setVisible(!transpSphereSGC.isVisible());
					break;
				}
			}	
			
		});
	}
}
