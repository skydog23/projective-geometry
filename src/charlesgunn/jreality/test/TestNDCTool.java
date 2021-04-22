/*
 * Created on Sep 1, 2011
 *
 */
package charlesgunn.jreality.test;

import java.awt.Color;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenuBar;

import charlesgunn.jreality.tools.MouseTool;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.jogl.plugin.HelpOverlay;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.pick.PickSystem;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

public class TestNDCTool extends LoadableScene {

	PointSetFactory psf = new PointSetFactory();
	@Override
	public SceneGraphComponent makeWorld() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		SceneGraphComponent child1 = SceneGraphUtility.createFullSceneGraphComponent("child1");
		child1.setGeometry(Primitives.box(1, 2, 3,false));
		SceneGraphComponent child2 = SceneGraphUtility.createFullSceneGraphComponent("child2");
		psf.setVertexCount(1);
		psf.setVertexCoordinates(P3.originP3);
		psf.update();
		child2.setGeometry(psf.getGeometry());
		child2.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		child2.getAppearance().setAttribute("pointShader.diffuseColor", Color.white);
		child2.setPickable(false);
		world.addChildren(child1);
		child1.addChild(child2);
		return world;
	}
	Viewer tviewer;
	private SceneGraphComponent world;
	@Override
	public void customize(JMenuBar menuBar, PluginSceneLoader psl) {
		tviewer = psl.getViewer();
		
		MouseTool ndctool = new MouseTool(InputSlot.getDevice("PrimaryAction"))	{
			Graphics3D g3d;
			PickSystem ps = new AABBPickSystem();
			{
				g3d = new Graphics3D(tviewer);
				ps.setSceneRoot(tviewer.getSceneRoot());
			}
			@Override
			public void perform(ToolContext tc) {
				super.perform(tc);
				PickResult pr = pick(viewer, currentNDC[0], currentNDC[1]);
				if (pr != null)	{
					psf.setVertexCoordinates(pr.getObjectCoordinates());
					psf.update();
				}
			}
			
			public PickResult pick(Viewer viewer, double ndcx, double ndcy) {  
				Graphics3D g3d  = new Graphics3D(viewer);;
				PickSystem ps = new AABBPickSystem();
				ps.setSceneRoot(tviewer.getSceneRoot());
				double[] front = {ndcx,ndcy,-1,1}, back = {ndcx, ndcy,1,1};
				double[] ndc2world = g3d.getNDCToWorld();
				double[] wfront = Rn.matrixTimesVector(null, ndc2world, front);
				double[] wback = Rn.matrixTimesVector(null, ndc2world, back);
				List<PickResult> results = ps.computePick(wfront, wback);
				// if all is well, results.get(0) contains a PickResult describing the first geometry under the pick point
				return results.size() != 0 ?  results.get(0) : null;
		}
			@Override
			public ImageIcon getIcon(int size) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void registerHelp(HelpOverlay overlay) {
				// TODO Auto-generated method stub
				
			}

		};
		world.addTool(ndctool);

	}

}
