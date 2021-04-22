package charlesgunn.jreality.worlds;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.InfoOverlayPlugin;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.content.ContentTools;
import de.jreality.plugin.experimental.ViewerKeyListenerPlugin;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class LorenzAttractor extends LoadableScene  {
	double ro = 28, sigma = 10, beta = 8.0/3.0;
	int numPoints = 32, 
		curveLength = 100;
	int tick = 0;
	Timer timer  = null;
	double dt = .009;

	int[][] snakeIndices = null;
	IndexedLineSet[] snakes = null;
	IndexedLineSetFactory[] snakeFacs = null;
	SceneGraphComponent[] snakeSGC = null;

	PointSetFactory leadPointsFactory = null;	
	double[][] leadPoints = null;
	SceneGraphComponent leadPointsSGC = new SceneGraphComponent();
	boolean running = true, reset = false;
	Color[] curveColors = null;
	Color[] baseColors = {new Color(255,0,0), new Color(255,255,0)};
	@Override
	public SceneGraphComponent makeWorld() {
		world = SceneGraphUtility.createFullSceneGraphComponent("LorenzWorld");
		world.getAppearance().setAttribute("anyDisplayLists", false);
		world.getAppearance().setAttribute("pointShader.pointRadius", .3);
//		world.getAppearance().setAttribute("pointShader.pointSize", 15.0);
//		world.getAppearance().setAttribute("pointShader."+CommonAttributes.SPHERES_DRAW, false);
		world.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		world.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, false);
//		world.getAppearance().setAttribute("lineShader."+CommonAttributes.LINE_LIGHTING_ENABLED, true);
		world.getAppearance().setAttribute("pointShader."+CommonAttributes.ATTENUATE_POINT_SIZE, false);
		Rectangle3D bbox = new Rectangle3D(new double[][]{{-17.8628,	-23.6261,	6.398},{15.8590,	20.2837,	44.7060}});
		PointSet desiredBoundingBoxProxy = new PointSet();
		desiredBoundingBoxProxy.setGeometryAttributes(GeometryUtility.BOUNDING_BOX, bbox);
		world.setGeometry(desiredBoundingBoxProxy);
		leadPointsSGC.setAppearance(new Appearance());
		leadPointsSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		leadPointsSGC.getAppearance().setAttribute("pointShader.diffuseColor",Color.white);
//		leadPointsSGC.getAppearance().setAttribute("pointShader.pointRadius", .15);
		initialize();
		
		timer = new Timer(20, new ActionListener()	{

			public void actionPerformed(ActionEvent e) {
				updateCurves();
			}
			
		});
		timer.start();
		return world;
	}
	double[][][] verts = null; 
	
	double initialSeparation = .003;
	double[] initialPoint = {-1, -1.5, 20	},
		initialDirection = {1,0,0};
	private void initialize() {
		if (snakeSGC == null || snakeSGC[0] == null || snakeSGC.length != numPoints)	{
			SceneGraphUtility.removeChildren(world);
			world.addChild(leadPointsSGC);
			snakeSGC = new SceneGraphComponent[numPoints];
			curveColors = new Color[numPoints];
			for (int i = 0; i < numPoints; ++i)	{
				snakeSGC[i] = new SceneGraphComponent();
				snakeSGC[i].setAppearance(new Appearance());
				double t = i/(numPoints-1.0);
				curveColors[i] = AnimationUtility.linearInterpolation(baseColors[0], baseColors[1], t);
				setColorValues(i);
				world.addChild(snakeSGC[i]);
			}
			
		}
//		if (verts == null || verts.length != numPoints  || verts[0].length != curveLength)
		verts = new double[numPoints][curveLength][3];
		for (int i = 0; i<numPoints; ++i) {
			Rn.add(verts[i][0], initialPoint, Rn.times(null, i*initialSeparation, initialDirection));
		}
			snakeFacs = new IndexedLineSetFactory[numPoints];
			snakeIndices = new int[1][curveLength];
			snakes = new IndexedLineSet[numPoints];
			for (int i = 0; i<numPoints; ++i)	{
				snakeFacs[i] = new IndexedLineSetFactory();
				snakeFacs[i].setVertexCount(curveLength);
				snakeFacs[i].setVertexCoordinates(verts[i]);
				snakeFacs[i].setEdgeCount(1);
				snakeFacs[i].setEdgeIndices(snakeIndices);
				snakeFacs[i].update();
				snakes[i] = snakeFacs[i].getIndexedLineSet();
			}
		for (int i = 0; i<numPoints; ++i) {
			snakeSGC[i].setGeometry(snakes[i]);
		}
		if (leadPoints == null || leadPoints.length != numPoints) {
			leadPoints = new double[numPoints][];
			leadPointsFactory = new PointSetFactory();
			leadPointsFactory.setVertexCount(numPoints);	
			leadPointsFactory.setVertexColors(curveColors);
			leadPointsSGC.setGeometry(null);
		}
		System.err.println("Initialized with "+numPoints+" curves");
		tick = 0;
	}

	private void setColorValues(int i) {
		snakeSGC[i].getAppearance().setAttribute("lineShader.diffuseColor", curveColors[i]);
		snakeSGC[i].getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", curveColors[i]);
		snakeSGC[i].getAppearance().setAttribute("pointShader.diffuseColor", curveColors[i]);
		snakeSGC[i].getAppearance().setAttribute("pointShader.polygonShader.diffuseColor", curveColors[i]);
	}
	Viewer viewer = null;
	private SceneGraphComponent world;
	
	protected void updateCurves() {
		if (viewer == null) return;
		tick++;
		for (int i = 0; i<curveLength; ++i)	
			snakeIndices[0][i] = (tick+1+i)%curveLength;
		for (int i = 0; i<numPoints; ++i)	{
			int j = (tick-1)%curveLength;
			int k = tick%curveLength;
			double x = verts[i][j][0];
			double y = verts[i][j][1];
			double z = verts[i][j][2];
			verts[i][k][0] = x + dt*(sigma * (y - x));
			verts[i][k][1] = y + dt*(x*(ro-z) - y);
			verts[i][k][2] = z + dt*(x*y - beta*z);
			snakeFacs[i].setVertexCoordinates(verts[i]);
			snakeFacs[i].setEdgeIndices(snakeIndices);
			snakeFacs[i].update();
			leadPoints[i] = verts[i][k];
		}		
		leadPointsFactory.setVertexCoordinates(leadPoints);
		leadPointsFactory.update();
		if (leadPointsSGC.getGeometry() == null)
			leadPointsSGC.setGeometry(leadPointsFactory.getGeometry());
		viewer.renderAsync();
	}
	
	public boolean isEncompass() {return true; }
	
	@Override
	public void customize(JMenuBar menuBar, Viewer v) {
		this.viewer = v;	
		v.getSceneRoot().getAppearance().setAttribute("backgroundColor", new Color(20,20,40));
		JMenu testM = new JMenu("View");
		final JCheckBoxMenuItem jcm = new JCheckBoxMenuItem("Toggle run");
		jcm.setSelected(running);
		testM.add(jcm);
		jcm.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				running = ((JCheckBoxMenuItem) e.getSource()).isSelected();
				if (running) timer.start();
				else timer.stop();
				viewer.renderAsync();
			}
		});
		JMenuItem jm = new JMenuItem("Reset");
		testM.add(jm);
		jm.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				timer.stop();
				initialize();
				timer.start();
				viewer.renderAsync();
			}
		});
		menuBar.add(testM);
	}
	
	public boolean hasInspector() {return true; }
	public Component getInspector(Viewer v)	{
		viewer = v;
		return getInspector();
	}
	public Component getInspector() {	
		Box inspectionPanel =  Box.createVerticalBox();
		TextSlider slider = new TextSlider.Double("rho",SwingConstants.HORIZONTAL,0.0,50,ro);
		slider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				ro = ((TextSlider) e.getSource()).getValue().doubleValue();
			}
		});
		inspectionPanel.add(slider);
		slider = new TextSlider.Double("sigma",SwingConstants.HORIZONTAL,0.0,10,sigma);
		slider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				sigma = ((TextSlider) e.getSource()).getValue().doubleValue();
			}
		});
		inspectionPanel.add(slider);
		slider = new TextSlider.Double("beta",SwingConstants.HORIZONTAL,0.0,10, beta);
		slider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				beta =((TextSlider) e.getSource()).getValue().doubleValue();
			}
		});
		inspectionPanel.add(slider);
		slider = new TextSlider.Double("dt",SwingConstants.HORIZONTAL,0.0,0.1, dt);
		slider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				dt =((TextSlider) e.getSource()).getValue().doubleValue();
			}
		});
		inspectionPanel.add(slider);
		final TextSlider npSlider = new TextSlider.Integer("# points",SwingConstants.HORIZONTAL,1, 128, numPoints);
		npSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				numPoints =((TextSlider) e.getSource()).getValue().intValue();
				if (running) timer.stop();
				initialize();
				if (running) timer.start();
			}
		});
		inspectionPanel.add(npSlider);
		JButton[] colorsb = new JButton[2];
		Box hbox = Box.createHorizontalBox();
		for (int i = 0; i<2; ++i)	{
			colorsb[i] = new JButton("color "+i);
			colorsb[i].setForeground(baseColors[i]);
			final int j = i;
			colorsb[i].addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					Color color = JColorChooser.showDialog((Component) viewer.getViewingComponent(), "Select color ",  null);
					if (color != null) {
						((JButton)e.getSource()).setForeground(color);
						updateColors(color, j);
					}
				}
			});
			hbox.add(colorsb[i]);
		}
		inspectionPanel.add(hbox);
		inspectionPanel.setName("Parameters");
		return inspectionPanel;
	}

	void updateColors(Color c, int which)	{
		baseColors[which] = c;
		for (int i = 0; i<numPoints; ++i)	{
			double t = i/(numPoints-1.0);
			curveColors[i] = AnimationUtility.linearInterpolation(baseColors[0], baseColors[1], t);
			setColorValues(i);
		}
		leadPointsFactory.setVertexColors(curveColors);
		leadPointsFactory.update();
	}
	static LorenzAttractor la = new LorenzAttractor();
	boolean doVR = false;
	public void doIt()	{
		JRViewer v = new JRViewer();
		v.addBasicUI();
		if (doVR) {
			v.addVRSupport();
			v.addContentSupport(ContentType.TerrainAligned);
		} else {
			v.addContentSupport(ContentType.Raw);
		}
		v.setContent(makeWorld());
		v.registerPlugin(new ContentTools());
		v.registerPlugin(new InfoOverlayPlugin());
		v.registerPlugin(new ViewerKeyListenerPlugin());
		v.registerPlugin(JRViewer.createSceneShrinkPanel(getInspector(), "Lorenz"));
//		v.registerPlugin(JRViewer.createSceneShrinkPanel(schatzcube.getReadMePanel(), "ReadMe"));
//		v.setPropertiesFile("schatzCubeVR.xml");
		v.startup();
		viewer = v.getPlugin(View.class).getViewer();//.getCurrentViewer();
		if (!doVR)	{
			viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", new Color(20,20,40));
			viewer.getSceneRoot().getAppearance().setAttribute("backgroundColors", Appearance.INHERITED);
			CameraUtility.encompass(viewer);
		}
		
	}
	public static void main(String[] args)	{
		la.doIt();
	}
	
}
//} else {
//int[][] info;
//info = new int[numPoints][];
//snakes = new Snake[numPoints];
//for (int i = 0; i<numPoints; ++i) {
//	snakes[i] = new Snake(verts[i]);
//	info[i] = ((Snake)snakes[i]).getInfo();
//	info[i][0] = 0;
//	info[i][1] = curveLength;
//}			
//}
//code from updateCurves()
//} else {
//info[i][0] = (tick+1)%curveLength;
//((Snake) snakes[i]).update();
//}
