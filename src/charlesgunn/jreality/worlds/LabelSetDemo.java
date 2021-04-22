/*
 * Created on Apr 15, 2004
 *
 */
package charlesgunn.jreality.worlds;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.util.TextSlider;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.util.WingedEdge;


/**
 * @author gunn
 *
 */
public class LabelSetDemo extends LoadableScene {
	SceneGraphComponent  oloidkit, label;
	double a=.5,b=2.0;
	public SceneGraphComponent makeWorld()	{
		
		
		WingedEdge oloid = makeOloid();
		oloidkit = new SceneGraphComponent();
		oloidkit.setGeometry(oloid);
		SceneGraphComponent theWorld = SceneGraphUtility.createFullSceneGraphComponent("oloidWorld");
		theWorld.addChild(oloidkit);
		
		int n = oloid.getNumPoints();
		String[] labels = new String[n];
		for (int i = 0; i<n; ++i){
			labels[i] = "v"+i;
		}
		oloid.setVertexAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));
		
		Appearance a = new Appearance();
		a.setAttribute(CommonAttributes.VERTEX_DRAW,true);
		a.setAttribute(CommonAttributes.EDGE_DRAW,false);
		a.setAttribute("pointShader.textShader.font", new Font("Sans Serif",Font.BOLD, 48));
		a.setAttribute("pointShader.textShader.diffuseColor", Color.WHITE);
		a.setAttribute("pointShader.scale", .002);
		a.setAttribute("pointShader.offset", new double[]{.1,0,.1});
		oloidkit.setAppearance(a);
		
		return theWorld;
	}

	
	/**
	 * @param oloid
	 */
	private WingedEdge makeOloid() {
		WingedEdge oloid = new WingedEdge(20.0d);
		int num = 100;
		for (int i = 0; i<=num; ++i)  	{
			double angle = 2.0 * Math.PI * ( i/((double) num));
			double[] plane = {Math.cos(angle), Math.sin(angle), a * Math.cos(b*angle), -1d};
			oloid.cutWithPlane(plane);
		} 
		oloid.update();
		return oloid;
	}


	public int getMetric() {
		// TODO Auto-generated method stub
		return Pn.EUCLIDEAN;
	}
	
	public boolean isEncompass()	{return true; }
	public boolean addBackPlane()	{ return true; }
	
	public void setConfiguration(ConfigurationAttributes config) {
		// TODO Auto-generated method stub

	}

	Viewer viewer;
	public void customize(JMenuBar menuBar, Viewer viewer) {
//		try {
//			viewer.getSceneRoot().getAppearance().setAttribute("backgroundTexture", new Texture2D("/homes/geometer/gunn/Pictures/grabs/arch-solids.jpg"));
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		this.viewer = viewer;
	}
	public boolean hasInspector() {return true; }
	public Component getInspector(Viewer viewer) {
		Box container = Box.createVerticalBox();
		final TextSlider aSlider = new TextSlider.Double("a",  SwingConstants.HORIZONTAL, 0.0, 2.5, a);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				setA(aSlider.getValue().doubleValue());
			}
		});
		container.add(aSlider);
		final TextSlider bSlider = new TextSlider.Double("b",  SwingConstants.HORIZONTAL, 0.0, 2.5, b);
		bSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				setB(bSlider.getValue().doubleValue());
			}
		});
		container.add(bSlider);

		container.add(Box.createVerticalGlue());
		return container;
	}

    private void update()	{
    		WingedEdge we = makeOloid();
		oloidkit.setGeometry(we);
		viewer.render();

    }
	/**
	 * @param d
	 */
	protected void setB(double d) {
		b = d;
		update();
	}


	/**
	 * @param d
	 */
	protected void setA(double d) {
		a = d;
		update();
	}

}
