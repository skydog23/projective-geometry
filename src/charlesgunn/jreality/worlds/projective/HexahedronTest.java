/*
 * Created on Dec 30, 2013
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.plugin.TermesSpherePlugin;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.experimental.ViewerKeyListenerPlugin;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.jrworkspace.plugin.simplecontroller.SimpleController.PropertiesMode;

public class HexahedronTest extends Assignment {

	double alpha = Math.PI/2,
			projcoord = .5;
	boolean useProjCoord = true,
			showLines = true;
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	SceneGraphComponent eucworld = SceneGraphUtility.createFullSceneGraphComponent("eucworld");
	SceneGraphComponent cube = SceneGraphUtility.createFullSceneGraphComponent("cube");
	SceneGraphComponent lines = SceneGraphUtility.createFullSceneGraphComponent("lines");
	SceneGraphComponent edges = SceneGraphUtility.createFullSceneGraphComponent("edges");
	SceneGraphComponent diagonals = SceneGraphUtility.createFullSceneGraphComponent("diagonals");
	PointRangeFactory[] factories = new PointRangeFactory[16];
	private TextSlider alphas;
	
	@Override
	public SceneGraphComponent getContent() {
		IndexedFaceSet coloredCube = Primitives.coloredCube();
		cube.setGeometry(coloredCube);
		Appearance ap = cube.getAppearance();
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		world.addChild(eucworld);
		MatrixBuilder.euclidean().scale(.5).assignTo(eucworld);
		eucworld.addChildren(cube, lines);
		lines.addChildren(edges, diagonals);
		ap = lines.getAppearance();
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		edges.getAppearance().setAttribute("lineShader.diffuseColor", Color.white);
		edges.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		diagonals.getAppearance().setAttribute("lineShader.diffuseColor", Color.yellow);
		diagonals.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		
		int[][] edgeArray = coloredCube.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null);
		double[][] verts = coloredCube.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		for (int i = 0; i<edgeArray.length; ++i)	{
			SceneGraphComponent child = new SceneGraphComponent("edge"+i);
			edges.addChild(child);
			PointRangeFactory prf = new PointRangeFactory();
			factories[i] = prf;
			prf.setElement0(verts[edgeArray[i][0]]);
			prf.setElement1(verts[edgeArray[i][1]]);
			prf.setFiniteSphere(false);
			prf.update();
			child.setGeometry(prf.getLine());
		}
		for (int i = 0; i<4; ++i)	{
			SceneGraphComponent child = new SceneGraphComponent("diagonal"+i);
			diagonals.addChild(child);
			PointRangeFactory prf = new PointRangeFactory();
			factories[12+i] = prf;
			prf.setElement0(verts[i]);
			prf.setElement1(verts[7-i]);
			prf.setFiniteSphere(false);
			prf.update();
			child.setGeometry(prf.getLine());
		}
		update();
		return world;
	}

	public void update()	{
		if (useProjCoord) updateProjCoord();
		else updateAlpha();
	}
	/**
	 * the projective coordinate is the ratio of two distances:
	 * from the origin to the two diametrically opposite vertices of the hexahedron 
	 * lying on the symmetry axis.  For the cube this is .5
	 */
	public void updateProjCoord() {
		if (projcoord == .5) {
			alpha = Math.PI/2;
			updateAlpha();
		}
		// work with normalized figure d = 1
		// the explanation for the following is in my notebook for Xmas 2013
		double iprojcoord = 1.0 - projcoord;

		double d = 1.0,
				h = iprojcoord/(2*iprojcoord - 1),
				x = Math.sqrt((h*(h-d)/2)),
				delta = Math.atan2(2*x, h),
				beta = Math.atan2(x, h),
				gamma = delta+beta,
				b = Math.cos(gamma),
				det = Math.sqrt(b*b+8),
				r1 = (b + det)/4,
				r2 = (b - det)/4;
		alpha = 2*Math.acos(r1);
				double c = Math.sin(gamma),
				q = b*b - .5 * c*c,
				alpha2 = Math.acos(q);
		System.err.println("alpha = "+alpha+"\talpha2 = "+alpha2);
		alphas.setEnabled(false);
		alphas.setValue(alpha);
		alphas.setEnabled(true);
		updateAlpha();
	}
	
	public void updateAlpha() {
		if (alpha == Math.PI/2) {
			new Matrix().assignTo(world);
			return;
		}
		double magicFactor = 1.0;
		double d = Math.sqrt(3.0) * Math.cos(alpha/2)/(Math.sin(alpha/2)*Math.sqrt(1+2*Math.cos(alpha))),
				h = (1.0/Math.sqrt(3.0)) * Math.cos(alpha/2)*Math.sqrt(1+2*Math.cos(alpha))/(Math.sin(alpha/2)*(Math.cos(alpha))),
				k0 = -magicFactor*(d * h)/(2*h-d),
				k1 = 0,
				k2 = magicFactor*d*(h-d)/(2*h-d),
				k3 = magicFactor*2 * h *(h-d)/(2*h-d),
				x = magicFactor*2*Math.sqrt((h*(h-d)/2));
		System.err.println("d h k0 k1 k2 \n"+d+" "+h+" "+k0+" "+k1+" "+k2);
		// cross ratio should be -1
		double cr = (k1-k0)*(k3-k2)/((k1-k2)*(k3-k0));
		System.err.println("cr = "+cr);
		double flip = (h < 0 ? -1 : 1);
		x = x*flip;
		double[] newTip = Pn.dragTowards(null, P3.originP3, new double[]{1,1,1,0}, k0, Pn.EUCLIDEAN),
				foot = Pn.dragTowards(null, P3.originP3, new double[]{1,1,1,0}, k3, Pn.EUCLIDEAN),
				leg1 = Pn.dragTowards(null, foot, new double[]{-.5,-.5,1,0}, x, Pn.EUCLIDEAN),
				leg2 = Pn.dragTowards(null, foot, new double[]{-.5,1,-.5,0}, x, Pn.EUCLIDEAN),
				leg3 = Pn.dragTowards(null, foot, new double[]{1,-.5,-.5,0}, x, Pn.EUCLIDEAN);
		
		double[][] 
		from = {
				{1,0,0,0},
				{0,1,0,0},
				{0,0,1,0},
				{0,0,0,1},
				{-.5*magicFactor,-.5*magicFactor,-.5*magicFactor,1}
		},
		to = {
				leg3,
				leg2,
				leg1,
				{0,0,0,1},
				newTip
		};
		System.err.println("to = \n"+Rn.toString(to));
		double[] proj = Pn.projectivity(null, from, to);
		new Matrix(proj).assignTo(world);
	}
	@Override
	public void setupJRViewer(JRViewer v)	{
		v.getController().setPropertiesMode(PropertiesMode.StaticPropertiesFile);
		v.getController().setStaticPropertiesFile(new File("src/charlesgunn/jreality/worlds/projective/hexahedron.xml"));
		v.registerPlugin(new TermesSpherePlugin());
		super.setupJRViewer(v);
	}

	public static void main(String[] args) {
		new HexahedronTest().display();
	}

	@Override
	public Component getInspector() {
		Box vbox = Box.createVerticalBox();
		alphas = new TextSlider.Double("alpha", SwingConstants.HORIZONTAL, 0, 2*Math.PI/3, alpha);
		alphas.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				alpha = alphas.getValue().doubleValue();
				useProjCoord = false;
				update();
			}
		});
		vbox.add(alphas);
		final TextSlider projs = new TextSlider.Double("proj coord", SwingConstants.HORIZONTAL, 0, 1, projcoord);
		projs.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				projcoord = projs.getValue().doubleValue();
				useProjCoord = true;
				update();
			}
		});
		vbox.add(projs);
		return vbox;
	}
	
	
}
