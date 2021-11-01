/*
 * Created on Aug 17, 2004
 *
  */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.geometry.projective.Abstract1DExtentFactory;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.geometry.projective.SurfaceElement;
import charlesgunn.jreality.newtools.FlyTool;
import charlesgunn.jreality.tools.CameraFlyTool;
import charlesgunn.jreality.tools.CameraTool;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.GlobalProperties;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.math.p5.PlueckerLineGeometry;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
/**
 * @author gunn
 *
 */
public class InterpolatePolarity extends 	Assignment  {
	private transient SceneGraphComponent c1, c2, ct, childList1, childList2, childListt, theWorld, sf, lines, strahlen, axen;
	private transient SceneGraphComponent[] kids = null;
	private transient int surfElementCount;
	private transient boolean showFirst = true, 
		showLast = false, 
		showSpears = false, 
		showAxen = false, 
		showAnimation = false, 
		showSphere = true;
	private transient double[] pos = {0,0,0};
	private transient double[] orientation = {1,0,0};
	private transient  int example = 0;
	private transient  SurfaceElement[] configuration;
	private transient  SurfaceElement[] polarConfiguration;
	private transient  double[][] innerProducts;
	private transient double[] point = new double[4];
	private transient double[] plane = new double[4];
	private transient int tickNumber = 0;
	
	public SceneGraphComponent getContent()	{
		theWorld = SceneGraphUtility.createFullSceneGraphComponent("theWorld");
		theWorld.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		c1 = SceneGraphUtility.createFullSceneGraphComponent("c1");
		c2 = SceneGraphUtility.createFullSceneGraphComponent("c2");
		ct = SceneGraphUtility.createFullSceneGraphComponent("ct");
		lines = SceneGraphUtility.createFullSceneGraphComponent("lines");
		lines.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		lines.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
		lines.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .05);
		lines.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+"polygonShader.diffuseColor", java.awt.Color.WHITE);
		strahlen = SceneGraphUtility.createFullSceneGraphComponent("strahlen");
		strahlen.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+".diffuseColor", java.awt.Color.YELLOW);
		axen = SceneGraphUtility.createFullSceneGraphComponent("axen");
		axen.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+".diffuseColor", java.awt.Color.WHITE);
		axen.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		lines.addChild(strahlen);
		lines.addChild(axen);
		lines.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.unitCube);
		
		childList1 = SceneGraphUtility.createFullSceneGraphComponent("c1");
		childList2 = new SceneGraphComponent();
		childListt = SceneGraphUtility.createFullSceneGraphComponent("childListt");
		c1.addChild(childList1);
		c2.addChild(childList2);
		ct.addChild(childListt);
		theWorld.addChild(c1);
		theWorld.addChild(c2);
		theWorld.addChild(ct);
		theWorld.addChild(lines);
//		Rectangle3D bbox = new Rectangle3D(7,7,7);
//		theWorld.getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, bbox);
		
		c1.setVisible(showFirst);
		c2.setVisible(showLast);
		strahlen.setVisible(showSpears);
		axen.setVisible(showAxen);
		sf = Primitives.wireframeSphere();
		sf.getAppearance().setAttribute("lineShader.lineWidth",.5);
		sf.getAppearance().setAttribute("lineShader.diffuseColor",java.awt.Color.LIGHT_GRAY);
		theWorld.addChild(sf);
		MatrixBuilder.euclidean().rotateX(Math.PI/2).assignTo(theWorld.getTransformation());
		// get a configuration consisting of surface elements
		setupConfiguration(example);
		
		return theWorld;
	}

	/**
	 * @param which 
	 * 
	 */
	private static String[] configNames = {
		"cubic", 
		"singleton", 
		"plane pencil", 
		"plane row", 
		"row of plane pencils", 
		"centered plane bundle (coarse)", 
		"centered plane bundle (medium)", 
		"offcenter plane bundle (coarse)", 
		"offcenter plane bundle (medium)", 
		"offcenter plane bundle (fine)", 
		"offcenter plane bundle (finer)"
	};
	private void setupConfiguration(int which) {
		which = which % 11;
		switch (which)	{
		case 0:
			configuration  = SurfaceElement.getCube(.3); 
			break;
		case 1:
			configuration  = SurfaceElement.getPlanePencil(.6, 1, 2, 0.5, 1.0, 0.0); 
			break;
		case 2:
			configuration  = SurfaceElement.getPlanePencil(.6, 1, 12, 0.5, 1.0, 0.0); 
			break;
		case 3:
			configuration  = SurfaceElement.getPlanePencil(.6, 20, 1, 0.5, 2.0, 0.0);
			break;
		case 4:
			configuration  = SurfaceElement.getPlanePencil(.6, 20, 20, 0.5, 2.0, 0.0);
			break;
		case 5:
			configuration  = SurfaceElement.getPlaneBundle(.01, 0);
			break;
		case 6:
			configuration  = SurfaceElement.getPlaneBundle(.01, 1);
			break;
		case 7:
			configuration  = SurfaceElement.getPlaneBundle(.6, 0);
			break;
		case 8:
			configuration  = SurfaceElement.getPlaneBundle(.6, 1);
			break;
		case 9:
			configuration  = SurfaceElement.getPlaneBundle(.6, 2);
			break;
		case 10:
			configuration  = SurfaceElement.getPlaneBundle(.6, 3);
			break;
		}
		surfElementCount = configuration.length;
		polarConfiguration = new SurfaceElement[surfElementCount];
		kids = new SceneGraphComponent[surfElementCount];
		SceneGraphUtility.removeChildren(childList1);
		SceneGraphUtility.removeChildren(childList2);
		SceneGraphUtility.removeChildren(childListt);
		theWorld.setVisible(false);
		boolean debug = true;
		innerProducts = new double[surfElementCount][2];
		for (int i = 0; i<surfElementCount; ++i)	{
			polarConfiguration[i] = configuration[i].polarize(Pn.HYPERBOLIC);
			configuration[i].validate();
			polarConfiguration[i].validate();
			if (debug)	{
//				double k = Pn.innerProduct(configuration[i].getPoint(), polarConfiguration[i].getPoint(), Pn.HYPERBOLIC);
				double scale = adjustWeight(configuration[i].getPoint(), polarConfiguration[i].getPoint());
				Rn.times(configuration[i].getPoint(), scale,configuration[i].getPoint());
//				Rn.times(configuration[i].getPlane(), scale,configuration[i].getPlane());
//				double e = Pn.innerProduct(polarConfiguration[i].getPlane(), configuration[i].getPlane(), Pn.HYPERBOLIC);
				double[] midpoint = Rn.add(null, configuration[i].getPoint(), polarConfiguration[i].getPoint());
				double ip = Pn.innerProduct(midpoint, midpoint, Pn.HYPERBOLIC);
//				System.err.println("midpoint: "+Rn.toString(midpoint)+" hyp norm squared = "+ip);
				System.err.println("hyp norm squared = "+ip);
				// we need these inner products when animating to keep the point and plane incident
				double d = Rn.innerProduct(configuration[i].getPoint(), polarConfiguration[i].getPlane());
				double e = Rn.innerProduct(configuration[i].getPlane(), polarConfiguration[i].getPoint());
				innerProducts[i][0] = d;
				innerProducts[i][1] = e;
//				innerProducts[i][0] = Math.signum(d)*Math.sqrt(Math.abs(d));
//				innerProducts[i][1] = Math.signum(e)*Math.sqrt(Math.abs(e));
				System.err.println("inner products = "+d+" "+e);
			}
			SceneGraphComponent sgc = configuration[i].getRepresentation();
			childList1.addChild(sgc);
			sgc = polarConfiguration[i].getRepresentation();
			childList2.addChild(sgc);
			kids[i] = SceneGraphUtility.createFullSceneGraphComponent("disk"+i);
			if (configuration[i].getPlaneColor() != null) 
			kids[i].getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, configuration[i].getPlaneColor());
			childListt.addChild(kids[i]);
		}
		// calculate the lines (at least some of them)
		updateLines();
		
		showAnimation = false;
		ct.setVisible(showAnimation);
		theWorld.setVisible(true);
		DefaultMatrixSupport.getSharedInstance().storeDefaultMatrices(theWorld);
		tickNumber = 0;
		runAtTime(1.0);
	}

	private double adjustWeight(double[] v, double[] v0) {
		double a = Pn.innerProduct(v, v, Pn.HYPERBOLIC);
		double b = 2 * Pn.innerProduct(v0, v, Pn.HYPERBOLIC);
		double c = Pn.innerProduct(v0, v0, Pn.HYPERBOLIC);
		double d = b*b-4*a*c;
		if (d < 0) {
			System.err.println("negative root");
			return 1.0;
		}
		double root = (-b + Math.sqrt(d))/(2*a);
		double[] tmp = Rn.add(null, Rn.times(null, root, v), v0);
		double length = Pn.innerProduct(tmp, tmp, Pn.HYPERBOLIC);
		System.err.println("length = "+length);
		return root;
	}

	/**
	 * 
	 */
	private void updateLines() {
		IndexedLineSet ils = calculateLines();
		strahlen.setGeometry(ils);
		 ils = calculateAxes();
		axen.setGeometry(ils);
	}

	/**
	 * @return
	 */
	private IndexedLineSet calculateLines() {
		int numSegs = 12;
		IndexedLineSet ils = new IndexedLineSet();
		int[][] indices = new int[surfElementCount][numSegs+1];
		double[][] verts = new double[surfElementCount*numSegs][4];
		double[] p0, p1;
		int foo = 0;
		for (int i = 0; i<surfElementCount; ++i)	{
			foo = i*numSegs;
			p0 =  Pn.normalize(null, configuration[i].getPoint(), Pn.ELLIPTIC);
			p1 = Pn.normalize(null, polarConfiguration[i].getPoint(), Pn.ELLIPTIC);
			PointRangeFactory lf = new PointRangeFactory();
			lf.setNumberOfSamples(numSegs);
			lf.setFiniteSphere(false);
			lf.setOffset(foo);
			lf.setVertices(verts);
			lf.setElement0(p0);
			lf.setElement1(p1);
			lf.update();
//			LineFactory.coordinatesForLine(verts, foo, numSegs, p0, p1);
			for (int j = 0; j<=numSegs; ++j) indices[i][j] = foo+(j%numSegs);
		}
//		ils = IndexedLineSetUtility.setIndexedLineSetFrom(ils, indices, verts, null, null);
		IndexedLineSetFactory ifsf = new IndexedLineSetFactory();
		ifsf.setVertexCount(verts.length);
		ifsf.setVertexCoordinates(verts);
		ifsf.setEdgeCount(indices.length);
		ifsf.setEdgeIndices(indices);
		ifsf.update();
		ils = ifsf.getIndexedLineSet();
		ils.setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		return ils;
	}

	/**
	 * @return
	 */
	static double[] randomPlane0 = {1,2,-1,2},
		randomPlane2 = {2,-1,-3,1};
	
	private transient  double[][] skewMatricesForAxes;
	private IndexedLineSet calculateAxes() {
		int numSegs = 12;
		IndexedLineSet ils = new IndexedLineSet();
		int[][] indices = new int[surfElementCount][numSegs+1];
		double[][] verts = new double[surfElementCount*numSegs][4];
		double[] p0, p1, ptx = new double[4], pty = new double[4];
		double[] pl = new double[6];
		double[] skewMatrix = new double[16];
		int foo = 0;
		skewMatricesForAxes = new double[surfElementCount][16];
		for (int i = 0; i<surfElementCount; ++i)	{
			foo = i*numSegs;
			// get the two planes and find their line of intersection
			p0 = Pn.normalize(null, configuration[i].getPlane(), Pn.ELLIPTIC);
			p1 = Pn.normalize(null, polarConfiguration[i].getPlane(), Pn.ELLIPTIC);
			PlueckerLineGeometry.lineFromPoints(pl, p0, p1);
			skewMatrix = PlueckerLineGeometry.lineToSkewMatrix(skewMatricesForAxes[i], pl);
			// find  intersection of common line with x- and y- planes
			// TODO if line lines in one of these planes, need to use another!
			p0 = Rn.matrixTimesVector(ptx, skewMatrix, randomPlane0);
			p1 = Rn.matrixTimesVector(pty, skewMatrix, randomPlane2);
			PointRangeFactory lf = new PointRangeFactory();
			lf.setNumberOfSamples(numSegs);
			lf.setFiniteSphere(false);
			lf.setOffset(foo);
			lf.setVertices(verts);
			lf.setElement0(p0);
			lf.setElement1(p1);
			lf.update();
			//LineFactory.coordinatesForLine(verts, foo, numSegs, p0, p1);
			for (int j = 0; j<=numSegs; ++j) indices[i][j] = foo+(j%numSegs);
		}
//		ils = IndexedLineSetUtility.setIndexedLineSetFrom(ils, indices, verts, null, null);
		IndexedLineSetFactory ifsf = new IndexedLineSetFactory();
		ifsf.setVertexCount(verts.length);
		ifsf.setVertexCoordinates(verts);
		ifsf.setEdgeCount(indices.length);
		ifsf.setEdgeIndices(indices);
		ifsf.update();
		ils = ifsf.getIndexedLineSet();
		ils.setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		return ils;
	}

	final int numSteps = 200;
	private transient double totalTime = 1.0;
	private transient double dt = totalTime/(numSteps );
	private transient Timer anim = null;
	private transient boolean animating = false;
	public void animate()	{
		animating = !animating;
		if (!animating )  {
			if (anim != null) anim.stop();
			return;
		} 
		System.err.println("animating");
		if (anim == null)	{
			anim = new javax.swing.Timer(30, new ActionListener()	{
				public void actionPerformed(ActionEvent e) {tick(); } 
			} );
		}
		ct.setVisible(true);
		
		anim.start();
	}
	
	public void tick()	{
//		double s = (k>numSteps) ?   (1.0 -(k-numSteps)*dt): k * dt;		// ss pendels between 0 and 1
		double s = tickNumber*dt;		// s runs from -1 to 1
		timeSlider.setValue(s);
//		timeSlider.textField.setText(Double.toString(s));
		runAtTime(s);
		tickNumber++;
		if (tickNumber >= numSteps) tickNumber = -numSteps;
	}
	
	public void runAtTime(double s)	{
		ct.setVisible(true);
		double t0 = 1.0, t1 = 0.0;
//		if (s < 0.0)	{ negative = true; s = -s; }
//		if (s == 1.0) {
//			t0 = 0.0;  t1 = 1.0;
//		}
//		else {
//			t0 = 1.0;  t1 = (s < .5) ? (2*s) : (1.0/(2 - 2*s));
//		}
//		if (!negative) t1 = -t1;
		t0= Math.cos(s*Math.PI); //s<0? 1+s : 1-s;
		t1 = Math.sin(s*Math.PI); //s;
//		System.out.println("t0, t1: "+t0+" "+t1);
		for (int i = 0; i<surfElementCount; ++i)	{
			// form the appropriate linear combinations for this value of the animation parameter.
			// incidence is preserved since the ingredients have the desired incidence properties
			// and this is preserved under linear interpolation.
			Rn.linearCombination(point, t0, configuration[i].getPoint(), t1, polarConfiguration[i].getPoint());
			Rn.linearCombination(plane, -t0/innerProducts[i][1], configuration[i].getPlane(), t1/innerProducts[i][0], polarConfiguration[i].getPlane());
//			Rn.linearCombination(plane, -t1, configuration[i].getPlane(), t0, polarConfiguration[i].getPlane());
			double d = Rn.innerProduct(point, plane);
			if (Math.abs(d) > 10E-10)  
				System.err.println("point and plane not incident "+d);
			double[] foo  = Rn.matrixTimesVector(null, skewMatricesForAxes[i], plane);
			if (Rn.euclideanNorm(foo) > 10E-10)  
				System.err.println("null point of interpolated plane is "+Rn.toString(foo));
			SurfaceElement.surfaceElement(kids[i], point, plane, 0.3, Pn.EUCLIDEAN);
		}
		if (viewer != null) viewer.renderAsync();
	}
	private final double pause = 0.1;
	@Override
	public void setValueAtTime(double d) {
	    // break it up into four segments 
		double scaledTime = 4*d,
				localTime = scaledTime%1;
		int phase = (int) scaledTime;
		double interpT = AnimationUtility.hermiteInterpolation(localTime, pause, 1.0-pause, 0, 1);
		runAtTime((phase+interpT)/4);
		
	}

	//	public boolean isEncompass()	{return true;}
//	public boolean addBackPlane()	{return false;}
	@Override
	public void display()	{
		super.display();
		viewer = jrviewer.getViewer();
		animationPlugin.setAnimateCamera(true);
		Color[] bg  = new Color[4];
		bg[0] = new Color(60, 60, 100);
		bg[1]= new Color(40, 40, 30);
		bg[2] = new Color(10, 50, 60);
		bg[3] = new Color(20, 30, 50);
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor",Color.BLACK); //bg[0]);
		viewer.getSceneRoot().getAppearance().setAttribute("ambientCoefficient",0.0);
		viewer.getSceneRoot().getAppearance().setAttribute("diffuseCoefficient",1.0);
		viewer.getSceneRoot().getAppearance().setAttribute("lineShader.diffuseColor",java.awt.Color.GRAY);
//		CameraUtility.getCamera(viewer).setNear(.01);
		CameraUtility.getCamera(viewer).setFar(500.0);
		CameraUtility.getCameraNode(viewer).addTool(new CameraFlyTool());
		SceneGraphComponent cameraNode = CameraUtility.getCameraNode(viewer);
	    FlyTool flytool = new FlyTool();
	    flytool.setGain(.15);
		cameraNode.addTool(flytool);
	}
	TextSlider<Double> timeSlider = null, diskSizeSlider=null, testLineCoordSlider = null;
	@Override
	public Component getInspector() {
		Box container = inspector; //Box.createVerticalBox();
		timeSlider = new TextSlider.Double("time",  SwingConstants.HORIZONTAL, 0, 1.0, 0);
	    timeSlider.addActionListener( new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					double scale = timeSlider.getValue().doubleValue();
			        runAtTime(scale);
				}
 	       });

	    //globalSpeed.setAlignmentX(1.0f);
		container.add(timeSlider);
		diskSizeSlider = new TextSlider.Double("disk size",  SwingConstants.HORIZONTAL, .0, 4.0, 1.0);
	    diskSizeSlider.addActionListener( new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				double scale = diskSizeSlider.getValue().doubleValue();
                MatrixBuilder.euclidean().scale(scale).assignTo(
                		SurfaceElement.smallDisk);
                viewer.renderAsync();
			}
	       });
		container.add(diskSizeSlider);
		
		JRadioButton playPause = new JRadioButton("run animation");
		playPause.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				animate();
			}
		});
		container.add(playPause);

		Box hbox = Box.createHorizontalBox();
		container.add(hbox);
		
		Box vbox = Box.createVerticalBox();
		hbox.add(vbox);
		vbox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder(BorderFactory
						.createEtchedBorder(), "Configuration")));

		ButtonGroup butg = new ButtonGroup();
		for (int i = 0; i<11; ++i)	{
//			if (i >= 3 && i <= 4) continue;
			final int j = i;
			JRadioButton jrb = new JRadioButton(configNames[i]);
			vbox.add(jrb);
			jrb.setSelected( i == 0);
			jrb.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)	{
					setupConfiguration(j);
					example = j;
				}
			});
			butg.add(jrb);
		}
		
		vbox = Box.createVerticalBox();
		hbox.add(vbox);
		vbox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder(BorderFactory
						.createEtchedBorder(), "View")));

		final JCheckBox begin = new JCheckBox("Show begin");
		begin.setSelected(showFirst);
		begin.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showFirst = begin.isSelected();
				c1.setVisible(showFirst);
				viewer.renderAsync();
			}
		});
		vbox.add(begin);
		
		final JCheckBox end = new JCheckBox("Show end");
		end.setSelected(showLast);
		end.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showLast = end.isSelected();
				c2.setVisible(showLast);
				viewer.renderAsync();
			}
		});
		vbox.add(end);
		
		final JCheckBox strahlenCB = new JCheckBox("Show spears");
		strahlenCB.setSelected(showSpears);
		strahlenCB.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showSpears = strahlenCB.isSelected();
				strahlen.setVisible(showSpears);
				viewer.renderAsync();
			}
		});
		vbox.add(strahlenCB);
		
		final JCheckBox axesCB = new JCheckBox("Show axes");
		axesCB.setSelected(showAxen);
		axesCB.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showAxen = axesCB.isSelected();
				axen.setVisible(showAxen);
				viewer.renderAsync();
			}
		});
		vbox.add(axesCB);
		
		final JCheckBox sphereCB = new JCheckBox("Show sphere");
		sphereCB.setSelected(showSphere);
		sphereCB.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showSphere = sphereCB.isSelected();
				sf.setVisible(showSphere);
				viewer.renderAsync();
			}
		});
		vbox.add(sphereCB);
		
			
//		jm = testM.add(new JMenuItem("Next example"));
//		jm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_8, 0));
//		jm.addActionListener( new ActionListener() {
//			public void actionPerformed(ActionEvent e)	{
//				example++;
//				setupConfiguration(example);
//				viewer.render();
//			}
//		});
//
//		
//		jm = testM.add(new JMenuItem("Previous example"));
//		jm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_9, 0));
//		jm.addActionListener( new ActionListener() {
//			public void actionPerformed(ActionEvent e)	{
//				example--;
//				if (example<0) example += 11;
//				setupConfiguration(example);
//				viewer.render();
//			}
//		});
//		theMenuBar.add(testM);

//		testLineCoordSlider = new TextSlider.Double("line coord",  SwingConstants.HORIZONTAL, -1.0 , 1.0 , 0.0);
//	    testLineCoordSlider.textField.addPropertyChangeListener(new PropertyChangeListener()	{
//		    public void propertyChange(PropertyChangeEvent e) {
//		        if ("value".equals(e.getPropertyName())) {
//		            Number value = (Number)e.getNewValue();
//		            if (value != null) {
//		                testLineCoord = value.doubleValue();
//		                updateLines();
//		                viewer.render();
//		            }
//		        }
//		    }	       	
//	       });
//		container.add(testLineCoordSlider);
		
		return container;
//		JFrame inspector = new JFrame("parameters");
//		inspector.getContentPane().add(container);
//		inspector.pack();
//		return null;
	}

	public SceneGraphComponent makeLights() {
		SceneGraphComponent sgc = GlobalProperties.makeLightsS();
		DirectionalLight dl = new DirectionalLight();
		dl.setColor(new Color(250, 200, 250));
		dl.setIntensity(.7);
		SceneGraphComponent l0 = SceneGraphUtility.createFullSceneGraphComponent("light1");
		double[] zaxis = {0,0,1};
		double[] other = {-.5,-.5,1};
		l0.getTransformation().setMatrix( P3.makeRotationMatrix(null, zaxis, other));
		l0.setLight(dl);
		sgc.addChild(l0);
		return sgc;

	}
	
	public static void main(String[] args) {
		new InterpolatePolarity().display();
	}

}
