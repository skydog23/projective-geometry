/*
 * Created on Jun 2, 2010
 *
 */
package charlesgunn.jreality.geometry.projective;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.tutorial.util.TextSlider;
import de.jreality.util.SceneGraphUtility;

public class HomologyTest extends LoadableScene {

	double multiplier =  1.1;
	double[] center = {0,1,0,1};
	double[] axis = {0,1,0,0};
	double[] homology;
	int n = 10, m = 10, np = 24;
	private SceneGraphComponent world,
			curves,
			plane,
			linePencil,
			centerToolSGC;
	int pm = -1, pn = -1;
	private SceneGraphComponent plane1;
	LinePencilFactory lpf;
	@Override
	public SceneGraphComponent makeWorld() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		plane = SceneGraphUtility.createFullSceneGraphComponent("plane");
		curves = SceneGraphUtility.createFullSceneGraphComponent("curves");
		curves.setPickable(false);
		linePencil = SceneGraphUtility.createFullSceneGraphComponent("linePencil");
		centerToolSGC = SceneGraphUtility.createFullSceneGraphComponent("centerToolSGC");
		world.addChildren(linePencil, curves, centerToolSGC);
		double s = 10;
		centerToolSGC.setGeometry(Primitives.texturedQuadrilateral(new double[]{-s,-s,0, s, -s,0,  s,s,0,  -s,s,0}));
		MatrixBuilder.euclidean().translate(center).translate(0,0,.01).assignTo(centerToolSGC);
		centerToolSGC.getAppearance().setAttribute("transparencyEnabled", true);
		centerToolSGC.getAppearance().setAttribute("transparency", 1.0);
		centerToolSGC.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		centerToolSGC.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, true);
		centerToolSGC.addTool(new AbstractTool(InputSlot.LEFT_BUTTON) {
			double[] beginPoint = new double[4], beginCenter;
			boolean valid = false;
			{
		   		addCurrentSlot(InputSlot.getDevice("PointerTransformation"), "drags the texture");
		    	}
			@Override
			public void deactivate(ToolContext tc) {
				// TODO Auto-generated method stub
				super.deactivate(tc);
				valid = false;
				MatrixBuilder.euclidean().translate(center).translate(0,0,.01).assignTo(centerToolSGC);
			}

			@Override
			public void activate(ToolContext tc) {
				System.err.println("tool activated");
				PickResult currentPick = tc.getCurrentPick();
				if (currentPick == null) {valid = false; return;}
				valid = true;
				beginPoint = currentPick.getObjectCoordinates();
				beginCenter = center.clone();
			}

			@Override
			public void perform(ToolContext tc) {
				if (!valid) return;
				PickResult currentPick = tc.getCurrentPick();
				if (currentPick == null) return;
				SceneGraphPath toPick = currentPick.getPickPath();
				System.err.println("pick path = "+toPick.getLastComponent().getName());
				double[] point = currentPick.getObjectCoordinates();
				System.err.println("pick = "+Rn.toString(point));
				Rn.subtract(point, point, beginPoint);
//				System.err.println("pick2 = "+Rn.toString(point));
				center[0] = beginCenter[0] + point[0];
				center[1] = beginCenter[1] + point[1];
				updateGroup();
			}
		});
		
		lpf = new LinePencilFactory();
		lpf.setPlane(new double[]{0,0,1,0});
		lpf.setNumberJoints(6);
		lpf.setFiniteSphere(false);
		lpf.setPoint(center);
		lpf.setNumLines(np/2);
		lpf.update();
		linePencil.addChild(lpf.getPencil());
		linePencil.setPickable(false);
		linePencil.getAppearance().setAttribute("lineShader.diffuseColor", Color.blue);
		
		plane1 = SceneGraphUtility.createFullSceneGraphComponent("plane1");
		SceneGraphComponent plane2 = SceneGraphUtility.createFullSceneGraphComponent("plane2");
		plane.addChildren(plane1); //, plane2);
		world.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		world.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		world.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		IndexedFaceSet ding = Primitives.regularPolygon(np, 0.0);
		plane1.setGeometry(ding);
		plane1.getAppearance().setAttribute("lineShader.diffuseColor", Color.white);
		double[] flipper = Rn.diagonalMatrix(null, new double[]{-1,-1,-1,-1});
		Matrix foo = new Matrix(flipper);
		foo.assignTo(plane2);
		plane2.setGeometry(ding); 
		plane2.getAppearance().setAttribute("lineShader.diffuseColor", Color.yellow);
		
		updateGroup();
		
		MatrixBuilder.euclidean().translate(0, 0, -4).assignTo(world);
		return world;
	}
	
	private void updateNumPoints()	{
		lpf.setNumLines(np/2);
		lpf.update();
		IndexedFaceSet ding = Primitives.regularPolygon(np, 0.0);
		plane1.setGeometry(ding);
	}
	
	private void updateGroup() {
		boolean lengthChanged = (pm != m || pn != n);
		if (lengthChanged) SceneGraphUtility.removeChildren(curves);
		homology = Pn.makeGeneralizedProjection(null, center, axis, multiplier);
//		System.err.println("homology = "+Rn.matrixToString(homology));
		double[] tmp = Rn.identityMatrix(4);
		for (int j = 0; j<m; ++j) tmp = Rn.times(tmp, tmp, homology);
		double[] mat = Rn.inverse(null, tmp);
		for (int i = -m; i<n+1; ++i)	{
			SceneGraphComponent child;
			if (lengthChanged) {
				child = new SceneGraphComponent("child"+i);
				child.addChild(plane);
				curves.addChild(child);
			} else child = curves.getChildComponent(i+m);
			Matrix foo = new Matrix(mat);
			if (!lengthChanged) child.getTransformation().setReadOnly(false);
			foo.assignTo(child);
			child.getTransformation().setReadOnly(true);
			mat = Rn.times(mat, mat, homology);
		}
		pm = m; pn = n;
		lpf.setPoint(center);
		lpf.update();
		MatrixBuilder.euclidean().translate(center).scale(.2).assignTo(plane);
	}

	@Override
	public void customize(JMenuBar menuBar, Viewer viewer) {
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", Color.black);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.RENDER_S3, true);
//		List<Tool> tools = world.getTools();
//		for (Tool t : tools)  world.removeTool(t);
	}

	@Override
	public boolean hasInspector() {
		return true;
	}

	@Override
	public Component getInspector(final Viewer viewer) {
		Box container = Box.createVerticalBox();
		final TextSlider aSlider = new TextSlider.Integer("n",  SwingConstants.HORIZONTAL, 0, 200, n);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
                n = aSlider.getValue().intValue();
				updateGroup();
			}
		});
		container.add(aSlider);
		final TextSlider bSlider = new TextSlider.Integer("m",  SwingConstants.HORIZONTAL, 0, 200, m);
		bSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
                m = bSlider.getValue().intValue();
				updateGroup();
			}
		});
		container.add(bSlider);
		final TextSlider cSlider = new TextSlider.Integer("num points",  SwingConstants.HORIZONTAL, 3, 100, np);
		cSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
                np = cSlider.getValue().intValue();
				updateNumPoints();
				//updateGroup();
			}
		});
		container.add(cSlider);
		final TextSlider mSlider = new TextSlider.Double("multiplier",  SwingConstants.HORIZONTAL, -2.0, 2.0, multiplier);
		mSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
                multiplier = mSlider.getValue().doubleValue();
				updateGroup();
			}
		});
		container.add(mSlider);

		return container;
	}

	public static void main(String[] args)	{
		HomologyTest ht = new HomologyTest();
		JRViewer.display(ht.makeWorld());
	}
}
