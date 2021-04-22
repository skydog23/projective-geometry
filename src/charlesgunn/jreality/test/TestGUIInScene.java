package charlesgunn.jreality.test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuBar;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.PickShowTool;
import de.jreality.tools.Timer;

public class TestGUIInScene extends LoadableScene {

	double[][] verts = {{1,-.5,0},{1,-1,0},{.5,-1,0}};
	int[][] inds = {{0,1,2}};
	private SceneGraphComponent humphrey;
	@Override
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = new SceneGraphComponent();
		return world;
	}
	@Override
	public void customize(JMenuBar menuBar, Viewer viewer) {
		SceneGraphPath cp = viewer.getCameraPath();
		SceneGraphComponent avatar = cp.popNew().popNew().getLastComponent();
		humphrey = new SceneGraphComponent();
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(verts.length);
		ifsf.setVertexCoordinates(verts);
		ifsf.setFaceCount(1);
		ifsf.setFaceIndices(inds);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		humphrey.setGeometry(ifsf.getIndexedFaceSet());
		MatrixBuilder.euclidean().translate(0,0,-2).assignTo(humphrey);
		humphrey.addTool(new PickShowTool());
		final Appearance ap  = new Appearance();
		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		ap.setAttribute(CommonAttributes.TRANSPARENCY, 0.5);
		humphrey.setAppearance(ap);
		AbstractTool atool = new AbstractTool()	{
			boolean active = false;
			long time = System.currentTimeMillis();
			Timer timer = new Timer(20, new ActionListener() {

				public void actionPerformed(ActionEvent e) {
				}
				
			});
			{
				addCurrentSlot(InputSlot.getDevice("PointerTransformation"));
//				addCurrentSlot(InputSlot.getDevice("SystemTimer"));
			}
			public void perform(ToolContext tc)	{
				System.err.println("Source is "+tc.getSource().getName());
				PickResult currentPick = tc.getCurrentPick();
				if (currentPick != null && 
					currentPick.getPickPath() != null &&
					currentPick.getPickPath().getLastComponent() == humphrey) {
					ap.setAttribute(CommonAttributes.TRANSPARENCY, 0.0);
					active = true;
				} else {
					ap.setAttribute(CommonAttributes.TRANSPARENCY, 1.0);
					active = false;							
				}
			}
		};
		humphrey.addTool(atool);
		avatar.addChild(humphrey);
	}
	

}
