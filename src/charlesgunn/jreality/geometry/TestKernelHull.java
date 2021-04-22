/*
 * Created on May 4, 2010
 *
 */
package charlesgunn.jreality.geometry;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JMenuBar;

import charlesgunn.jreality.SelectionComponent;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.groups.ArchimedeanSolids;
import de.jtem.discretegroup.util.WingedEdge;

public class TestKernelHull extends Assignment {

	KernelHull[] khlist = new KernelHull[10];
	private SelectionComponent parent;
	public static String[] archimedeanNames = {
			"3.3.3","3.3.3.3","3.3.3.3.3","4.4.4","5.5.5",
			"3.4.3.4","3.4.4.4","3.4.5.4","3.5.3.5","3.6.6",
			"3.8.8","3.10.10","4.6.6","4.6.8","4.6.10",
			"5.6.6",//"3.3.3.3.4","3.3.3.3.5"
			};
	int namecount = 0,
			levels = 3;
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	boolean[] showB = {true, true, false, false};
	
	@Override
	public SceneGraphComponent getContent() {
		
		parent = new SelectionComponent();
		parent.setAppearance(new Appearance());
		parent.setName("parent");
		Appearance ap = parent.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", new Color(250, 225, 225));
		ap.setAttribute("pointShader.diffuseColor", new Color(250, 225, 225));
		ap.setAttribute("lineShader.tubeRadius", .006);
		ap.setAttribute("pointShader.pointRadius", .006);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		world.addChild(parent);
		updateKernelHull();
		return world;
	}


	private SceneGraphComponent updateKernelHull() {
		namecount = namecount % archimedeanNames.length;
		WingedEdge solid = ArchimedeanSolids.archimedeanSolid(
				archimedeanNames[namecount]);
		System.err.println("solid = "+archimedeanNames[namecount]);
		khlist[0] = new KernelHull(solid);
		parent.removeAllChildren();
		for (int i = 0; i<levels; ++i)	{
			SceneGraphComponent instrument = KernelHull.instrument(khlist[i]);
			instrument.setName("kh"+i);
			KernelHull.setVisibility(instrument, showB);
			Rectangle3D bbox = BoundingBoxUtility.calculateBoundingBox(instrument);
			double max = bbox.getMaxExtent();
			MatrixBuilder.euclidean().scale(2.0/max).assignTo(instrument);
			parent.addChild(instrument);
			if (i<levels-1) khlist[i+1] = new KernelHull(khlist[i].getKernel());
		}
		parent.setSelectedChild(0);
		return world;
	}
	

	@Override
	public void display() {
		super.display();
		((Component) jrviewer.getViewer().getViewingComponent()).addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e)	{ 
				switch(e.getKeyCode())	{
					
				case KeyEvent.VK_H:
					System.err.println("	1: cycle through models");
					break;
	
				case KeyEvent.VK_1:
					int n = parent.getSelectedChild();
					if (n == 0 && e.isShiftDown()) n += parent.getChildComponentCount();
					if (e.isShiftDown())
						parent.setSelectedChild(n-1);
					else
						parent.setSelectedChild(n+1);
					break;
				case KeyEvent.VK_2:
					namecount++;
					updateKernelHull();
					break;
				case KeyEvent.VK_3:
					if (e.isShiftDown()) levels--;
					else levels++;
					if (levels >= khlist.length) levels = khlist.length-1;
					updateKernelHull();
					break;
				case KeyEvent.VK_4:
					updateVis(0);
					break;
				case KeyEvent.VK_5:
					updateVis(1);
					break;
				case KeyEvent.VK_6:
					updateVis(2);
					break;
				case KeyEvent.VK_7:
					updateVis(3);
					break;
				}
				
				
			}

			private void updateVis(int i) {
				showB[i] = !showB[i];
				int n = parent.getChildComponentCount();
				for (int j = 0; j<n; ++j)	{
					KernelHull.setVisibility(parent.getChildComponent(j), showB);
				}
			}

		});
	}

	public static void main(String[] args) {
		new TestKernelHull().display();
	}

}
