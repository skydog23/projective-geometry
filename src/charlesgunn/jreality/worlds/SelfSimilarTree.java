/*
 * Author	gunn
 * Created on Aug 12, 2005
 *
 */
package charlesgunn.jreality.worlds;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import charlesgunn.gui.TransformationInspector;
import charlesgunn.jreality.tools.MotionManager;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.ui.viewerapp.SelectionEvent;
import de.jreality.ui.viewerapp.SelectionListener;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.SelectionManagerImpl;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;



public class SelfSimilarTree extends LoadableScene implements SelectionListener {

	SceneGraphComponent icokit;
	private SelfSimilarTreeImpl it;
	Viewer theViewer = null;
	boolean showSurface = false;
	double[][] profile = {{1,1,0},{-1,1,0}};
	double scale = .2;
	int valence = 2;
	private SceneGraphComponent[] kids;
	SceneGraphComponent surfaceSGC;
	float base = .83f;
	Color[] baseColors = {new Color(1f,base,base), new Color(1f,1f,base), new Color(base,1f,1f)};
	public SceneGraphComponent makeWorld()	{
		SceneGraphComponent cylinderWrapper = SceneGraphUtility.createFullSceneGraphComponent();
		cylinderWrapper.setGeometry(Primitives.cylinder(20)); 
		ImageData id = null;
		try {
			id = ImageData.load(Input.getInput("http://www.math.tu-berlin.de/~gunn/Pictures/textures/bark4.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Texture2D barktex = TextureUtility.createTexture(cylinderWrapper.getAppearance(), CommonAttributes.POLYGON_SHADER, 0, id);
		MatrixBuilder.euclidean().translate(0,0,1).scale(scale, scale,1).assignTo(cylinderWrapper.getTransformation());
		SceneGraphComponent wrapper2 = SceneGraphUtility.createFullSceneGraphComponent();
		wrapper2.addChild(cylinderWrapper);
		wrapper2.setName("Wrapper");
		wrapper2.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.VERTEX_SHADER,"simple");
		//wrapper2.addChild(Primitives.sphere(scale, 0,0,2));
		//wrapper.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new java.awt.Color(200, 150,0));
		wrapper2.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,false);
//			wrapper2.getAppearance().setAttribute(CommonAttributes.SMOOTH_SHADING,false);
		kids = new SceneGraphComponent[valence];
		kids[0] = SceneGraphUtility.createFullSceneGraphComponent("kid1");
		kids[1] = SceneGraphUtility.createFullSceneGraphComponent("kid2");
//			kids[2] = SceneGraphUtility.createFullSceneGraphComponent("kid3");
		MatrixBuilder.euclidean().translate(scale/2,0,2.0).rotateY(Math.PI/3).scale(.7).assignTo(kids[0].getTransformation());
		MatrixBuilder.euclidean().translate(-scale/2,0,2.0).rotateY(-Math.PI/3).scale(.55).assignTo(kids[1].getTransformation());
//			MatrixBuilder.euclidean().translate(0,.2,1.8).rotateX(-Math.PI/5).scale(.7).assignTo(kids[2].getTransformation());
		kids[0].getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, baseColors[0]);
		kids[1].getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, baseColors[1]);
//			kids[2].getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, baseColors[2]);
		it = new SelfSimilarTreeImpl(kids,8, wrapper2);
		SceneGraphComponent theWorld = SceneGraphUtility.createFullSceneGraphComponent("world");
		MatrixBuilder.euclidean().rotateX(-Math.PI/2).assignTo(theWorld.getTransformation());
		it.getAppearance().setAttribute(CommonAttributes.CENTER_ON_BOUNDING_BOX, false);
		it.getAppearance().setAttribute(CommonAttributes.BACKEND_RETAIN_GEOMETRY, true);
		theWorld.addChild(it);
		theWorld.setAppearance(new Appearance());
		theWorld.addChild(Primitives.sphere(.0001,0d,0d,0d));
		surfaceSGC = SceneGraphUtility.createFullSceneGraphComponent("surface");
		surfaceSGC.setVisible(showSurface);
		return theWorld;
	}
	public void customize(JMenuBar menuBar, Viewer viewer) {
		theViewer = viewer;
		updateSelections(viewer);
		SelectionManagerImpl.selectionManagerForViewer(viewer).addSelectionListener(this);
		MotionManager.motionManagerForViewer(viewer).oneAtATime = false;
		ImageData id = null;
		try {
			id = ImageData.load(Input.getInput("http://www.math.tu-berlin.de/~gunn/Pictures/textures/Birch_Bark.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TextureUtility.setBackgroundTexture(viewer.getSceneRoot().getAppearance(), id);
	}
	
	SceneGraphPath toIt, toB0, toB1;
	protected void updateSelections(Viewer viewer) {
		System.err.println("Setting selections");
		List l = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), it);
		SceneGraphPath world = (SceneGraphPath) l.get(0);
		SceneGraphPath sgp=(SceneGraphPath) world.clone();
		sgp.push(it.getChildComponent(1));
		SelectionManager selectionManagerForViewer = SelectionManagerImpl.selectionManagerForViewer(viewer);
		selectionManagerForViewer.removeSelection(toB0);
		toB0 = new SceneGraphPath(sgp);
		selectionManagerForViewer.addSelection(toB0);
		selectionManagerForViewer.setSelectionPath(toB0);
		selectionManagerForViewer.removeSelection(toB1);
		toB1 = new SceneGraphPath(sgp);
		toB1.pop();
		toB1.push(it.getChildComponent(2));
		selectionManagerForViewer.addSelection(toB1);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR,new java.awt.Color(0,0,80));
		selectionManagerForViewer.addSelection(world);
	}

	public boolean isEncompass() {
		return true;
	}
	protected class SelfSimilarTreeImpl extends SceneGraphComponent implements TransformationListener {
		int iterationCount = 0;
		int branches = 0;
		boolean countChanged = true;
		SceneGraphComponent[] tforms;
		SceneGraphComponent geometry;

		public SelfSimilarTreeImpl(SceneGraphComponent[] t, int itcount, SceneGraphComponent g) {
			super();
			tforms = t;
			branches = tforms.length;
			for (int i = 0; i<branches;++i)	
				tforms[i].getTransformation().addTransformationListener(this);
	 		geometry = g;
	 		setTransformation(new Transformation());
	 		setAppearance(new Appearance());
	 		setName("Branch");
	 		getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.WHITE);
			setIterationDepth(itcount);
		}

		public void setIterationDepth(int i) {
			if ( i == iterationCount) return;
			iterationCount = i;
			countChanged = true;
			update();
		}
		
		public int getIterationDepth()	{
			return iterationCount;
		}
		
		public void update()	{
			if (countChanged) {
				SceneGraphUtility.removeChildren(this);
				this.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, baseColors[0]);						
				this.addChild(geometry);
				branch(this, iterationCount);
				countChanged = false;
			} 
		}

		protected void branch(SceneGraphComponent sgc, int depth)	{
			if (depth == 0) return;
			
			for (int i = 0; i<branches; ++i)	{
				SceneGraphComponent child = new SceneGraphComponent();
				child.setName(sgc.getName()+i);
				child.setTransformation(tforms[i].getTransformation());
				if (depth < iterationCount)	{
					child.setAppearance(new Appearance());
					double factor = 1.0-Math.pow(.5,iterationCount-depth);
					Color interp = linearInterpolation(factor, (Color) sgc.getAppearance().getAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.class), baseColors[i]);
					child.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, interp);						
					if (depth == 1) child.getAppearance().setAttribute("collectMe",true);
				} else 
					child.setAppearance(tforms[i].getAppearance());
				child.addChild(geometry);
				sgc.addChild(child);
				branch(child, depth-1);
			}
		}

		protected void updateColors()	{
			this.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, baseColors[0]);						
			branchColors(this, iterationCount);
		}
		
		protected void branchColors(SceneGraphComponent sgc, int depth)	{
			if (depth == 0) return;
			Color thisC = Color.WHITE;
			Object foo = sgc.getAppearance().getAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.class);
			if (foo instanceof Color)	thisC = (Color) foo;
			for (int i = 0; i<branches; ++i)	{
				SceneGraphComponent child = sgc.getChildComponent(i+1);
				System.out.println("Processing "+sgc.getName());
				if (depth < iterationCount)	{
					Appearance ap = child.getAppearance();
					double factor = 1.0-Math.pow(.5,iterationCount-depth);
					Color interp = linearInterpolation(factor, thisC, baseColors[i]);							
					ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, interp);						
				} else 
					tforms[i].getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, baseColors[i]);
				branchColors(child, depth-1);
			}
		}
		/* (non-Javadoc)
		 * @see de.jreality.scene.event.TransformationListener#transformationMatrixChanged(de.jreality.scene.event.TransformationEvent)
		 */
		public void transformationMatrixChanged(TransformationEvent ev) {
			update();

		}

	}
	private Color linearInterpolation(double t, Color c1, Color c2)	{
		float[] fc1 = c1.getRGBComponents(null);
		float[] fc2 = c2.getRGBColorComponents(null);
		float[] fc3 = new float[3];
		for (int i = 0; i<3; ++i)  fc3[i] = (float) (t*fc1[i] + (1-t) * fc2[i]);
		return new Color(fc3[0], fc3[1], fc3[2]);
	}
	
	private TransformationInspector[] tformInsp = new TransformationInspector[2], worldTformInsp;
	JButton[] colorsb = new JButton[2];
	Box container;
	public boolean hasInspector() {return true; }
	public Component getInspector(Viewer viewer) {
		container = Box.createVerticalBox();
		TextSlider aSlider = new TextSlider.Integer("depth",  SwingConstants.HORIZONTAL, 0, 15, it.getIterationDepth());
	    aSlider.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				int depth = ((TextSlider) arg0.getSource()).getValue().intValue();
                it.setIterationDepth(depth);
                updateSelections(theViewer);
                theViewer.renderAsync();
				
			}
	    });
	    container.add(aSlider);
		Box hbox = Box.createHorizontalBox();
		for (int i = 0; i<2; ++i)	{
			colorsb[i] = new JButton("color "+i);
			colorsb[i].setBackground(baseColors[i]);
			final int j = i;
			colorsb[i].addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					Color color = JColorChooser.showDialog((Component) theViewer.getViewingComponent(), "Select color ",  null);
					if (color != null) updateColors(color, j);
				}
			});
			hbox.add(colorsb[i]);
		}
		hbox.add(Box.createHorizontalGlue());
		
		final JCheckBox showSurfB = new JCheckBox("Show surface");
		showSurfB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				showSurface = showSurfB.isSelected();
			}
			
		});
		container.add(hbox);
		hbox = Box.createHorizontalBox();
		 String[] names = {"right transform","left transform"};
		for (int i = 0; i<2; ++i)		{
			tformInsp[i] = new TransformationInspector.Factored(kids[i].getTransformation(), new ActionListener()	{
				public void actionPerformed(ActionEvent e) {
					theViewer.render();
				}
			});
			tformInsp[i].addBorderTitle(names[i]);
			//tformInsp[i].setBackground( baseColors[i]);
			tformInsp[i].setVisible(i==0);				
			hbox.add(tformInsp[i]);
		}
		container.add(hbox);
//			container.addMouseListener(new MouseAdapter()	{
//				public void mouseEntered(MouseEvent e)	{
//					container.requestFocus();
//					System.out.println("request focus");
//				}
//				
//				public void mouseExited(MouseEvent e)	{
//					System.out.println("leaving");
//				}
//				
//			});
		return container;
	}
	
	private void updateColors(Color color, int i)	{
		colorsb[i].setBackground(color);
		baseColors[i] = color;
		it.updateColors();
		theViewer.render();
	}
	
	public String getHelpSet() {
		return "SelfSimilarTreeHelp/helpset.hs";
	}
	public boolean hasHelpset() {
		return true;
	}
	public void selectionChanged(SelectionEvent e) {
		SceneGraphPath sgp = SelectionManagerImpl.selectionManagerForViewer(theViewer).getSelectionPath();
		int which = -1;
		if (sgp.getLastComponent().getTransformation() == kids[0].getTransformation())	
			which = 0;
		else if (sgp.getLastComponent().getTransformation() == kids[1].getTransformation())
			which = 1;
//			System.out.println("which is "+which);
		for (int i = 0; i<2; ++i)	tformInsp[i].setVisible(which == i);
		container.validate();
	}

}
