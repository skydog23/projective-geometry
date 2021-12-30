package charlesgunn.jreality.worlds.projective;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.jreality.geometry.projective.LineUtility;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.math.p5.PlueckerLineGeometry;
import charlesgunn.util.TextSlider;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.util.SceneGraphUtility;

public class LineAndPolarLineDemo extends Assignment{
	
	SceneGraphComponent world, line, polarline;
	double a=0, b=0, c=1;
	PointRangeFactory[] prf = new PointRangeFactory[2];
		
	@Override
	public SceneGraphComponent getContent() {
		world = SceneGraphUtility.createFullSceneGraphComponent();
		line = SceneGraphUtility.createFullSceneGraphComponent();
		polarline = SceneGraphUtility.createFullSceneGraphComponent();
		line.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", Color.red);
		polarline.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", Color.green);
		world.addChild(line);
		world.addChild(polarline);
		for (int i = 0; i<2; ++i)	{
			prf[i] = new PointRangeFactory();
			prf[i].setFiniteSphere(true);
			prf[i].setSphereRadius(10E1);
		}
		update();
		MatrixBuilder.euclidean().translate(0,0,-4).rotateX(Math.PI/2).assignTo(world);
		return world;
	}
	
	protected void update()	{
		double[] p0 = {a,b,0,c};
		double[] p1 = {a,b,1,c};
		prf[0].setElement0(p0);
		prf[0].setElement1(p1);
		prf[0].update();
		double[] plucker = prf[0].getPluckerLine();
		double[] polar = PlueckerLineGeometry.dualizeLine(null, plucker);
		double[][] pts = LineUtility.twoPointsOnLine(null, polar);
		prf[1].setElement0(pts[0]);
		prf[1].setElement1(pts[1]);
		prf[1].update();
		if (line.getGeometry() == null)
			line.setGeometry(prf[0].getLine());
		if (polarline.getGeometry() == null)
			polarline.setGeometry(prf[1].getLine());
	}
	
	@Override
	public Component getInspector() {	
		Box inspectionPanel =  inspector;
		final TextSlider aSlider = new TextSlider.Double("a",SwingConstants.HORIZONTAL,-2.0, 2.0,a);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				a = aSlider.getValue().doubleValue();
				update();
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(aSlider);
		final TextSlider bSlider = new TextSlider.Double("b",SwingConstants.HORIZONTAL,-2.0, 2.0,c);
		bSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				b = bSlider.getValue().doubleValue();
				update();
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(bSlider);
		final TextSlider cSlider = new TextSlider.Double("c",SwingConstants.HORIZONTAL,-2.0, 2.0,c);
		cSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				c = cSlider.getValue().doubleValue();
				update();
				viewer.renderAsync();
			}
		});
		inspectionPanel.add(cSlider);
		return inspectionPanel;
	}

	public static void main(String[] args) {
		new LineAndPolarLineDemo().display();
	}
}
