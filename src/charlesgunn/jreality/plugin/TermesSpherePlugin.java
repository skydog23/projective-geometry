package charlesgunn.jreality.plugin;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import charlesgunn.anim.core.KeyFrameAnimatedBean;
import charlesgunn.anim.plugin.AnimationPlugin;
import charlesgunn.jreality.geometry.TermesSphere;
import charlesgunn.util.TextSlider;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.content.DirectContent;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;

public class TermesSpherePlugin extends Plugin  {

	TermesSphere termes;
	Viewer viewer;
	boolean fromStills = false;		// if true, can calculate from current 3d scene	
	JRViewer illusjrv = null;
	ShrinkPanel mainPanel;
	boolean reducedGUI = false;
	public JCheckBox toggleViewB;
	
	private ViewMenuBar
		viewMenuBar = null;
	private View
		view = null;
	private Action
		toggleViewAction = new ToggleViewAction(),
		toggleUpdateOnRenderAction = new ToggleUpdateOnRenderAction(),
		calculateTermesAction = new CalculateTermesAction(),
		toggleLabelsAction = new ToggleLabelsAction(),
		toggleFullDomeAction = new ToggleFullDome(),
		cycleCameraAction = new CycleCameraAction();
	boolean asMenu = false;
	private String saveFileStem = "/tmp/foo";
	TermesSphereSPP shrinkPanel;
	
	public TermesSpherePlugin() {
		this(false);
	}
	
	public TermesSpherePlugin(boolean b) {
		asMenu = b;
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		view = c.getPlugin(View.class);
		viewer = view.getViewer().getCurrentViewer();
		termes = new TermesSphere(viewer);
		viewer.getSceneRoot().addChild(termes.getSceneGraphComponent());
		if (asMenu){
			viewMenuBar = c.getPlugin(ViewMenuBar.class);
			viewMenuBar.addMenuItem(getClass(), 1, calculateTermesAction, "Termes");
			viewMenuBar.addMenuItem(getClass(), 2, toggleViewAction, "Termes");
			viewMenuBar.addMenuItem(getClass(), 3, toggleLabelsAction, "Termes");
			viewMenuBar.addMenuItem(getClass(), 4, cycleCameraAction, "Termes");			
		} else {
			shrinkPanel = c.getPlugin(TermesSphereSPP.class);
			setupGUI();
		}
		AnimationPlugin ap = c.getPlugin(AnimationPlugin.class);
		if (ap != null)	{
			KeyFrameAnimatedBean<Camera> termesCamera = new KeyFrameAnimatedBean<Camera>(termes.getTermesCamera());
			termesCamera.setName("termesCamera");
			ap.getAnimated().add(termesCamera);
			System.err.println("Adding termes camera to animation system");
		}
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		if (asMenu) viewMenuBar.removeAll(getClass());
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Termes Sphere", "Charles Gunn");
	}

	private void setupGUI() {
		
		if (mainPanel != null) return;
		Insets insets = new Insets(1,0,1,0);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = insets;
		c.weighty = 0.0;
		c.anchor = GridBagConstraints.WEST;
		
		mainPanel = shrinkPanel.getShrinkPanel();
		mainPanel.removeAll();
		mainPanel.setLayout(new ShrinkPanel.MinSizeGridBagLayout());
				
		
		JCheckBox showLabelsBox = new JCheckBox(toggleLabelsAction);
		showLabelsBox.setSelected(termes.isShowLabels());
		showLabelsBox.setToolTipText("Show cube-face labels on sphere");
		JCheckBox updateOnRenderBox = new JCheckBox(toggleUpdateOnRenderAction);
		updateOnRenderBox.setSelected(termes.isUpdateOnRender());
		updateOnRenderBox.setToolTipText("Synchronize 6-point render with virtual scene render");
		JCheckBox doFullDomeBox = new JCheckBox(toggleFullDomeAction);
		doFullDomeBox.setSelected(termes.isDoFullDome());
		doFullDomeBox.setToolTipText("Show half-dome image");
		toggleViewB = new JCheckBox(toggleViewAction);
		toggleViewB.setSelected(termes.getVisible());
		toggleViewB.setToolTipText("Toggle between virtual scene and 6-point perspective");
		JCheckBox useFBOBox = new JCheckBox("FBO");
		useFBOBox.setSelected(termes.isFastFBO());
		useFBOBox.setToolTipText("Force use of FBO's for texture");
		useFBOBox.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				termes.setFastFBO(((JCheckBox)e.getSource()).isSelected());
			}
		});
		JButton showIllusB = new JButton("Illustrate");
		showIllusB.setToolTipText("Create illustration of process");
		showIllusB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SceneGraphComponent sgc = termes.getIllustrativeSGC(3.0);
				if (illusjrv == null) {	
					JRViewer.display(sgc);
					illusjrv = JRViewer.getLastJRViewer();
				}
				else  {
					Content content = illusjrv.getPlugin(DirectContent.class);
					if (content != null) content.setContent(sgc);					
				}
			}
		});
		JButton calcTermesB = new JButton(calculateTermesAction);
		calcTermesB.setToolTipText("Calculate 6-point perspective");
		JButton cycleCameraB = new JButton(cycleCameraAction);
		cycleCameraB.setToolTipText("Cycle through standard camera positions: on, in, out");
		JButton saveB = new JButton("Save cube map");
		saveB.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				termes.writeCubeMap(saveFileStem);
			}
		});


		final TextSlider sizeSl = new TextSlider.Integer("Texture size:",
				SwingConstants.HORIZONTAL,32, 2048, termes.getSize());
		sizeSl.setToolTipText("Set size of texture map for calculation of 6-point perspective");
		sizeSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				int size = sizeSl.getValue().intValue();
				System.err.println("size to "+size);
				termes.setSize(size);
			}
			
		});

		final TextSlider fovSl = new TextSlider.Double("FOV",
				SwingConstants.HORIZONTAL,30,175, termes.getFOV());
		fovSl.setToolTipText("Set the field of view of the camera");
		fovSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				double size = fovSl.getValue().doubleValue();
				System.err.println("FOV "+size);
				termes.setFOV(size);
			}
			
		});

		
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		Box hbox = Box.createHorizontalBox();
		panel.add(hbox,c);
		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = 0;
		hbox.add(toggleViewB);
//		c.gridwidth = GridBagConstraints.REMAINDER;
		if (!reducedGUI) hbox.add(updateOnRenderBox);
		hbox.add(showLabelsBox);
		if (!reducedGUI) hbox.add(doFullDomeBox);
		if (!reducedGUI) hbox.add(useFBOBox);
//		c.gridwidth = GridBagConstraints.REMAINDER;
//		c.weightx = fromStills ? 0 : 1.0;
		c.gridy = 1;
		c.gridx = GridBagConstraints.RELATIVE;
		if (!reducedGUI) hbox = Box.createHorizontalBox();
		panel.add(hbox, c);
		if (!reducedGUI) hbox.add(showIllusB);
		if (!reducedGUI) hbox.add(calcTermesB);
		hbox.add(cycleCameraB);
		if (!reducedGUI) hbox.add(saveB);

		c.gridy = 2;
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 80;
		panel.add(fovSl, c);
		c.gridy = 3;
		panel.add(sizeSl, c);
		
		
		mainPanel.add(panel, c);
	}

	protected class ToggleViewAction extends AbstractAction {

		private static final long 
			serialVersionUID = 1L;

		public ToggleViewAction() {
			putValue(Action.NAME, "6 Point");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_MASK) );
		}
		
		
		public void actionPerformed(ActionEvent e) {
			termes.setVisible(!termes.getVisible());
			if (termes.getVisible()) termes.setCameraPosition(0);
		}
		
	}
	
	protected class CalculateTermesAction extends AbstractAction {
		
		private static final long 
			serialVersionUID = 1L;

		public CalculateTermesAction() {
			putValue(Action.NAME, "Calc");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_MASK) );
		}
		
		
		public void actionPerformed(ActionEvent e) {
			termes.update();
		}
		
	}
	
	
	protected class ToggleLabelsAction extends AbstractAction {
		
		private static final long 
			serialVersionUID = 1L;
		
		public ToggleLabelsAction() {
			putValue(Action.NAME, "Labels");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_MASK) );
		}
		
		public void actionPerformed(ActionEvent e) {
			termes.setShowLabels(!termes.isShowLabels());
			viewer.renderAsync();
		}
		
	}
	
	protected class ToggleUpdateOnRenderAction extends AbstractAction {
		
		private static final long 
			serialVersionUID = 1L;
		
		public ToggleUpdateOnRenderAction() {
			putValue(Action.NAME, "sync");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.CTRL_MASK) );
		}
		
		public void actionPerformed(ActionEvent e) {
			termes.setUpdateOnRender(!termes.isUpdateOnRender());
			viewer.renderAsync();
		}
		
	}
	
	protected class ToggleFullDome extends AbstractAction {
		
		private static final long 
			serialVersionUID = 1L;
		
		public  ToggleFullDome() {
		
			putValue(Action.NAME, "Dome");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_MASK) );
		}
		
		
		public void actionPerformed(ActionEvent e) {
			termes.setDoFullDome(!termes.isDoFullDome());
			viewer.renderAsync();
		}
		
	}
	
	
	protected class CycleCameraAction extends AbstractAction {

		private static final long 
		serialVersionUID = 1L;
	
		public CycleCameraAction() {
			putValue(Action.NAME, "Cycle camera");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_MASK) );
		}
	
	
		public void actionPerformed(ActionEvent e) {
			termes.cycleCameraPosition();
		}
		
	}


	public TermesSphere getTermes() {
		return termes;
	}


	public boolean isFromStills() {
		return fromStills;
	}


	public void setFromStills(boolean fromStills) {
		this.fromStills = fromStills;
	}

	public boolean isReducedGUI() {
		return reducedGUI;
	}

	public void setReducedGUI(boolean reducedGUI) {
		this.reducedGUI = reducedGUI;
	}


}
