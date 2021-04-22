/*
 * Created on Sep 15, 2011
 *
 */
package charlesgunn.jreality.test;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JMenuBar;

import charlesgunn.jreality.geometry.projective.DualizeSceneGraph;
import charlesgunn.jreality.tools.TranslateShapeTool;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class TestDualizeVisitor extends LoadableScene {

	private SceneGraphComponent standardSGC, dualSGC;
	int showWhich = 1, which = 0;
	private SceneGraphComponent childSGC, world;
	@Override
	public SceneGraphComponent makeWorld() {
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		standardSGC = SceneGraphUtility.createFullSceneGraphComponent("standard");
		
		Appearance ap = world.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute(CommonAttributes.SPHERES_DRAW, false);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		standardSGC.getAppearance().setAttribute(DualizeSceneGraph.FAN_RADIUS, .15);
		final IndexedLineSetFactory circle1 = IndexedLineSetUtility.circleFactory(12,0,0,1);
		double[][] verts = circle1.getIndexedLineSet().getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		
		Color[] clrs = { Color.blue, Color.red, Color.green, Color.cyan, Color.yellow, Color.black, 
				Color.blue, Color.red, Color.green, Color.cyan, Color.yellow,Color.black
		};
		for (int i = 0; i<7; ++i)	{
			childSGC = SceneGraphUtility.createFullSceneGraphComponent();
			standardSGC.addChild(childSGC);
			IndexedLineSetFactory circle = new IndexedLineSetFactory();
			circle.setVertexCount(circle1.getVertexCount());
			circle.setVertexCoordinates(verts);
			if (i==0)	{
				circle.setVertexColors(clrs);
			} else {
				Color[] vcolors = new Color[12];
				int edgec = 0, edgesize = 0;
				int remainder =  12 % i;
				if (remainder == 0)	{
					edgesize = 12 / i;
				} else edgesize = 12;
				edgec = 12 / edgesize;
				int[][] edges = new int[edgec][edgesize+1];
				Color[] ecolors = new Color[edgec];
				int skip = (edgec == 1) ?  i : edgec;
				for (int j = 0; j<edgec; ++j) {
					for (int k = 0; k<edgesize; ++k)	{
						edges[j][k] = (j + k * skip) % 12;
						vcolors[(j + k * skip) % 12] = clrs[j];
					}
					edges[j][edgesize] = edges[j][0];
					ecolors[j] = clrs[j];
				}
				if (edgesize > 1)	{
					circle.setEdgeCount(edgec);
					circle.setEdgeIndices(edges);
					circle.setEdgeColors(ecolors);
				}
				circle.setVertexColors(vcolors);				
			}
			circle.update();
			circle.getIndexedLineSet().setName("Circle");
			childSGC.setGeometry(circle.getIndexedLineSet());
			childSGC.setName("standardSGC");
		}
		dualSGC = DualizeSceneGraph.dualize(standardSGC);
		dualSGC.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		updateVisible();
		final SceneGraphComponent dummySGC = SceneGraphUtility.createFullSceneGraphComponent("dummy");
		dummySGC.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		dummySGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		dummySGC.getAppearance().setAttribute("transparency", 1.0);
		dummySGC.getAppearance().setAttribute("transparencyEnabled", true);
		dummySGC.setGeometry(Primitives.regularPolygon(20));
		
		dummySGC.addTool(new TranslateShapeTool());
		dummySGC.getTransformation().addTransformationListener(new TransformationListener() {
			
			public void transformationMatrixChanged(TransformationEvent ev) {
				Matrix m = new Matrix(dummySGC.getTransformation());
				m.assignTo(standardSGC);
				double[] mat = m.getArray();
				mat = Rn.transpose(null,Rn.inverse(null, mat));
				new Matrix(mat).assignTo(dualSGC);
			}
		});
		world.addChildren(standardSGC, dualSGC, dummySGC);
		return world;
	}
	
	@Override
	public void customize(JMenuBar menuBar, PluginSceneLoader psl) {
		psl.getViewer().getSceneRoot().getAppearance().setAttribute("backgroundColor", Color.white);
		((Component) psl.getViewer().getViewingComponent()).addKeyListener( new KeyAdapter()	{

			public void keyPressed(KeyEvent e)	{ 
				switch(e.getKeyCode())	{
					
				case KeyEvent.VK_H:
					System.out.println("	1:  cycle selection");
					System.out.println("	2:  cycle dual");
					break;
	
				case KeyEvent.VK_1:
					which++;
					which = which  % standardSGC.getChildComponentCount();
					updateVisible();
					break;
				
				case KeyEvent.VK_2:
					showWhich++;
					updateVisible();
					break;
					

				default:
					break;
				}
				
			}

		});
	}

	private void updateVisible() {
		for (SceneGraphComponent child : standardSGC.getChildComponents()) {
			child.setVisible(false);
		}
		for (SceneGraphComponent child : dualSGC.getChildComponent(0).getChildComponents()) {
			child.setVisible(false);
		}
//		System.err.println("which = "+which);
		standardSGC.getChildComponent(which).setVisible(true);
		dualSGC.getChildComponent(0).getChildComponent(which).setVisible(true);
		showWhich = showWhich % 4;
		if (showWhich == 0) showWhich++;
		standardSGC.setVisible((showWhich & 1) != 0);
		dualSGC.setVisible((showWhich & 2) != 0);
	}
	@Override
	public boolean isEncompass() {
		// TODO Auto-generated method stub
		return true;
	}


}
