/*
 * Created on 25 Apr 2025
 *
 */
package charlesgunn.pathcurve;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.jreality.geometry.ClipBox;
import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.math.Complex;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class ImaginaryPathCurveSurface {
	
	double s0 = -2, s1 = 2, t0 = -2, t1 = 2,
			ds = s1 - s0, dt = t1-t0,
			clipSize = 4.0,
			shrink = .8,
			roughSize = 6.0;
	double alpha = 1.2;
	double beta =1;
	double scale = .85;
	double radius = .5;
	int 			ssteps = 150,
			tsteps = 180;
	double[] coordinateSystem = Rn.transpose(null, new double[]{
			1, 1, 0, 1, 
			1, -1, 0, 1,
			0, 0, 1, 1, 
			0, 0, -1, 1});
//	coordinateSystem = Rn.identityMatrix(4);
	double[] p = {radius,0,0,1};

	ClipBox clipbox = new ClipBox();
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("Imaginary path curve world");
	SceneGraphComponent mesh = SceneGraphUtility.createFullSceneGraphComponent("mesh");
	boolean boundit = true;
	
	ImaginaryPathCurveSurface() {
		initialize();
		clipbox.setDim(new double[]{clipSize, clipSize, clipSize});
		if (boundit)	{
			world.addChild(clipbox.getBox());
		}

	}
	
	void initialize() {
		
		update();
		world.addChild(mesh);
//		mesh.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		mesh.getAppearance().setAttribute("lineShader."+CommonAttributes.DIFFUSE_COLOR, new Color(129,252,129));
		mesh.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, 0.015);
		mesh.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
	//		SceneGraphComponent bs = TubeUtility.ballAndStick(clipped, .02,.01,new Color(.99f, 0f, .1f, .8f), Color.GREEN, Pn.EUCLIDEAN);
//		bs.getAppearance().setAttribute(CommonAttributes.BACK_FACE_CULLING_ENABLED, true);
//		world.addChild(bs);
		SceneGraphComponent tetra = SceneGraphUtility.createFullSceneGraphComponent("tetra");
		tetra.getTransformation().setMatrix(P3.makeStretchMatrix(null, roughSize));
		IndexedFaceSet tet = Primitives.cube();
		tetra.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.WHITE);
		BallAndStickFactory basf = new BallAndStickFactory(tet);
		basf.setBallColor(null);
		basf.setBallRadius(.01);
		basf.setStickColor(null);
		basf.setStickRadius(.01);
		basf.setMetric(Pn.EUCLIDEAN);
		basf.update();
		SceneGraphComponent ballAndStick = basf.getSceneGraphComponent();
		//tetra.addChild(TubeUtility.ballAndStick(tet,.01,.01, null, null, Pn.EUCLIDEAN));
		tetra.addChild(ballAndStick);
		tetra.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+".name", "default");
		world.addChild(tetra);

		double[][] verts = {{0,0,-5},{0,0,5},{1,-5,0},{1,5,0}};
		int[][] indices = {{0,1},{2,3}};
		DataList dl = StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(verts);
		IndexedLineSet axs = new IndexedLineSet(4,2);
		axs.setVertexAttributes(Attribute.COORDINATES, dl);
		axs.setEdgeAttributes(Attribute.INDICES, StorageModel.INT_ARRAY.array().createReadOnly(indices));
		basf = new BallAndStickFactory(axs);
		basf.setBallColor(Color.RED);
		basf.setBallRadius(.03);
		basf.setStickColor(Color.RED);
		basf.setStickRadius(.03);
		basf.setMetric(Pn.EUCLIDEAN);
		basf.update();
		SceneGraphComponent thickAxes = basf.getSceneGraphComponent();
//		SceneGraphComponent thickAxes = TubeUtility.ballAndStick(axs, .03, .03, Color.RED, Color.RED, Pn.EUCLIDEAN);
		thickAxes.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER, "default");
		thickAxes.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+".name", "default");
		world.addChild(thickAxes);
	}

	protected void update() {
		Complex[] ev = new Complex[4];
		ev[0] = new Complex(0,alpha);
		ev[1] = Complex.conjugate(null, ev[0]);
		ev[2] = new Complex(Math.log(scale), -beta);
		ev[3] = Complex.conjugate(null, ev[2]);
		double[][] orbit = PathCurveUtility.pathCurveOrbit(ev, coordinateSystem, p, s0, s0+ds, ssteps);
		ev = new Complex[4];
		ev[0] = new Complex(-Math.log(scale), alpha);
		ev[1] = Complex.conjugate(null, ev[0]);
		ev[2] = new Complex(Math.log(scale), beta);
		ev[3] = Complex.conjugate(null, ev[2]);
		IndexedFaceSet qms = PathCurveUtility.pathCurveMesh(ev, coordinateSystem, orbit, t0, t0+dt, tsteps);
		IndexedFaceSet clipped = doRoughClip(qms, roughSize);
		mesh.setGeometry(clipped);
	}
	protected IndexedFaceSet doRoughClip(IndexedFaceSet qms, double size) {
		Rectangle3D box = new Rectangle3D();
		double[][] bnds = box.getBounds();
		double b = size + .01;
		double bndsize = size+.5;
		bnds[0][0] = bnds[0][1] = bnds[0][2] = -bndsize;
		bnds[1][0] = bnds[1][1] = bnds[1][2] = bndsize;
		box.setBounds(bnds);
		box.update();
		IndexedFaceSet clipped = GeometryUtilityOverflow.clipToBox(qms, box);
		String attrbox = GeometryUtility.BOUNDING_BOX;
		double[][] bounds = {{-b,-b,-b},{b,b,b}};
		Rectangle3D bbox = new Rectangle3D(bounds);
		clipped.setGeometryAttributes(attrbox, bbox);
		return clipped;
	}
	
	public  ClipBox getClipbox() {
		return clipbox;
	}

	public SceneGraphComponent getSGC()  {
		return world;
	}
	
	public Component getInspector() {	
		Box inspectionPanel =  Box.createVerticalBox();
		final TextSlider<Double> clipBSlider = new TextSlider.Double("bound",SwingConstants.HORIZONTAL,0.0, 6.0, clipSize);
		clipBSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				clipSize = clipBSlider.getValue().doubleValue();
				clipbox.setDim(new double[] {clipSize, clipSize, clipSize});
		}
		});
		inspectionPanel.add(clipBSlider);
		final TextSlider<Double> implodeSlider = new TextSlider.Double("shrink",SwingConstants.HORIZONTAL,-1.0,1.0,shrink);
		implodeSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				shrink = implodeSlider.getValue().doubleValue();
				mesh.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+".implodeFactor", shrink);
			}
		});
		inspectionPanel.add(implodeSlider);
		final TextSlider<Double> s0Slider = new TextSlider.Double("smin",SwingConstants.HORIZONTAL,-4, 4, s0);
		s0Slider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				s0 = s0Slider.getValue().doubleValue();
				update();
		}
		});
		inspectionPanel.add(s0Slider);
		final TextSlider<Double> dsSlider = new TextSlider.Double("ds",SwingConstants.HORIZONTAL,0, 5, ds);
		dsSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				ds = dsSlider.getValue().doubleValue();
				update();
			}
		});
		inspectionPanel.add(dsSlider);
		final TextSlider<Double> t0Slider = new TextSlider.Double("tmin",SwingConstants.HORIZONTAL,-4, 4, t0);
		t0Slider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				t0 = t0Slider.getValue().doubleValue();
				update();
		}
		});
		inspectionPanel.add(t0Slider);
		final TextSlider<Double> dtSlider = new TextSlider.Double("dt",SwingConstants.HORIZONTAL,0, 5, dt);
		dtSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				dt = dtSlider.getValue().doubleValue();
				update();
			}
		});
		inspectionPanel.add(dtSlider);
		return inspectionPanel;
	}

}

//PathCurveFactory pcf = new PathCurveFactory();
//PathCurveParametricSurfaceFactory pcpsf = new PathCurveParametricSurfaceFactory();

//pcf.setCoordinateSystem(coordinateSystem);
//pcf.setEigenvalues(ev);
//pcf.setTmin(s0);
//pcf.setTmax(s1);
//pcf.setNumberSteps(steps);
//pcf.setInitialPoint(p);
//pcf.update();
//pcpsf.setCoordinateSystem(coordinateSystem);
//pcpsf.setEigenvalues(ev);
//pcpsf.setInitialCurveFactory(pcf);
//pcpsf.setTmin(t0);
//pcpsf.setTmax(t1);
//pcpsf.setNumberSteps(steps);
//pcpsf.update();


