/*
 * Created on Feb 23, 2004
 *
 */
package charlesgunn.jreality.worlds;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import charlesgunn.gui.TransformationInspector;
import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.geometry.IteratedTransform;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.util.TextSlider;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.SelectionManagerImpl;
import de.jreality.util.SceneGraphUtility;

public class ElephantTrunk extends LoadableScene{
	SceneGraphComponent icokit;
	private IteratedTransform it;
	private Transformation iteratedTform;
	Viewer viewer;
	boolean showInspector = true;
	private SceneGraphComponent theWorld;
		public SceneGraphComponent makeWorld()	{
			double[][] profile = {{-2,1,0},{-1.5,.5,0}, {-.5,.5,0},{0,1,0}};
			IndexedFaceSet foo = GeometryUtilityOverflow.surfaceOfRevolutionAsIFS(profile, 20, Math.PI*2);
			SceneGraphComponent wrapper = SceneGraphUtility.createFullSceneGraphComponent();
			MatrixBuilder.euclidean().rotateY(Math.PI/2).scale(1,.5,.5).assignTo(wrapper.getTransformation());
			wrapper.setGeometry(foo); 
			iteratedTform = new Transformation();
			MatrixBuilder.euclidean().translate(0,0,1.9).rotateX(0.1).scale(.96).assignTo(iteratedTform);
			it = new IteratedTransform(iteratedTform, 75, wrapper);
			it.setName("iteratedTransform");
			theWorld = new SceneGraphComponent();
			theWorld.setName("theWorld");
			theWorld.addChild(it);
			theWorld.setAppearance(new Appearance());
			theWorld.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,false);
			theWorld.getAppearance().setAttribute(CommonAttributes.SMOOTH_SHADING,false);
			theWorld.setTransformation(new Transformation());
			MatrixBuilder.euclidean().rotateY(Math.PI/2).assignTo(theWorld.getTransformation());
			it.getAppearance().setAttribute(CommonAttributes.CENTER_ON_BOUNDING_BOX, false);
			it.getAppearance().setAttribute(CommonAttributes.BACKEND_RETAIN_GEOMETRY, true);
			return theWorld;
		}
		public void customize(JMenuBar menuBar, Viewer viewer) {
			List l = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), theWorld);
			SceneGraphPath sgp = (SceneGraphPath) l.get(0);
			SelectionManagerImpl.selectionManagerForViewer(viewer).addSelection(sgp);
			l = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), it.getChildComponent(1));
			sgp = (SceneGraphPath) l.get(0);
			SelectionManagerImpl.selectionManagerForViewer(viewer).setSelectionPath(sgp);
			SelectionManagerImpl.selectionManagerForViewer(viewer).addSelection(sgp);
			viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR,new java.awt.Color(0,40,40));
			this.viewer = viewer;

		}
	
		public boolean isEncompass() {
			return true;
		}
		double[] translation = {0,0,1};
		double angle = 0.0;
		double scale = .96;
		double[] axis = {0,0,1};
		private TransformationInspector tformInsp;
		Box container;
		public boolean hasInspector() {return true; }
		public Component getInspector(final Viewer viewer) {
			container = Box.createVerticalBox();
			final TextSlider aSlider = new TextSlider.Integer("iterates",  SwingConstants.HORIZONTAL, 1, 200, it.getIterationCount());
			aSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
	                it.setIterationCount(aSlider.getValue().intValue());
	                viewer.render();
				}
			});
			container.add(aSlider);

			tformInsp = new TransformationInspector.Factored(iteratedTform, new ActionListener()	{
				public void actionPerformed(ActionEvent e) {
					viewer.render();
				}
			});
			tformInsp.addBorderTitle("Iterated Transform");
			container.add(tformInsp);
			container.add(Box.createVerticalGlue());
			return container;
		}
		public String getHelpSet() {
			return "ElephantTrunkHelp/helpset.hs";
		}
		public boolean hasHelpset() {
			return true;
		}


}
