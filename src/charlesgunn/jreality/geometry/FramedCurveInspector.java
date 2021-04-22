/*
 * Created on Aug 25, 2004
 *
 */
package charlesgunn.jreality.geometry;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import charlesgunn.anim.core.FramedCurve;
import charlesgunn.gui.TransformationInspector;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.plugin.HelpOverlay;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Rn;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.util.CameraUtility;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;

public class FramedCurveInspector extends JFrame {
	
	FramedCurve theCurve;
	protected Camera fciCamera;
	boolean showCurve;
	boolean moveWorld = false;		// default: means move the camera
	boolean loopPlay = false;
	FramedCurve.ControlPoint currentPoint = null;
	protected Viewer parent;
	SceneGraphPath camPath, origCamPath;
	SceneGraphComponent myCameraNode, worldNode, target, cameraScaleNode;
	int inspectedPoint = 0;
	//double currentTime = 0.0;
	MyKeyListener cdkl;
	final Component me;
	int totalTicks;
	static int framesPerSecond = 30;
	static double ycoord = 0.0;
	double totalSeconds;
	int timerInterval;
	final JTextField keyNo;
	final JTextField timeValue;
	final JTextField durationValue;
	final JTextField playtime	;
	final JTextField filename;
	final JTextField playBackF;
	JButton playButton;
	PlayAction playAction;
	protected Timer cameraMove, cameraEnd;
	protected double playbackFactor = 1.0;
	
	
	static String resourceDir = null;
	public static double globalPlaybackFactor = 1;
	static {
		String foo = Secure.getProperty("framedCurveInspector.resourceDir");
		if (foo != null) resourceDir = foo;
		//JOGLConfiguration.theLog.log(Level.FINE,"FCI resource dir is: "+resourceDir);
		foo = Secure.getProperty("framedCurveInspector.frameRate");
		if (foo != null) framesPerSecond = Integer.parseInt(foo);
		foo = Secure.getProperty("framedCurveInspector.ycoord");
		if (foo != null) ycoord = Double.parseDouble(foo);
		foo = Secure.getProperty("mars.playSpeed");
		if (foo != null) globalPlaybackFactor = Double.parseDouble(foo);
	}

	public FramedCurveInspector(Viewer v)	{
		this(v, null);
	}
	
	public FramedCurveInspector(Viewer v, SceneGraphComponent world)	{
		super();
		setWorldNode(world);
		//worldNode = world;
		
		me = this;
		parent = v;
		cameraEnd = null;
		
		cameraMove = new Timer(timerInterval, new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				updateTime();
			}
		});
		
		setFrameRate(framesPerSecond);
		
		//setInspectedCurve(fc);

		cdkl = new MyKeyListener();
		//parent.getViewingComponent().addKeyListener(cdkl);
		//frame = new JFrame();
		
		SceneGraphComponent root = parent.getSceneRoot();
		camPath = new SceneGraphPath();
		camPath.push(root);
		
		target = myCameraNode = SceneGraphUtility.createFullSceneGraphComponent("FramedCurveInspectorCamera");
		myCameraNode.getTransformation().setMatrix( CameraUtility.getCameraNode(parent).getTransformation().getMatrix());
		setWorldNode(null);
		fciCamera = new Camera();	
		Camera oldCam = CameraUtility.getCamera(parent);
		// TODO implement clone() for Camera
		fciCamera.setNear(oldCam.getNear());
		fciCamera.setFar(oldCam.getFar());
		//JOGLConfiguration.theLog.log(Level.FINE,"Camera is: "+fciCamera);
		cameraScaleNode = SceneGraphUtility.createFullSceneGraphComponent("camera scale node");
		myCameraNode.addChild(cameraScaleNode);
		cameraScaleNode.setCamera(fciCamera);
		root.addChild(myCameraNode);
		camPath.push(myCameraNode);
		camPath.push(cameraScaleNode);
		camPath.push(cameraScaleNode.getCamera());
		
		// initialize with a default framed curve
		theCurve = new FramedCurve();
		double[][] mp =  new double[1][];
		mp[0] = myCameraNode.getTransformation().getMatrix();
		theCurve.setControlPoints(mp);
		
		int panelwidth = 600;
		int panelheight = 40;
		Box col = Box.createVerticalBox();
		Box panel = Box.createHorizontalBox();
		panel.setSize(panelwidth, panelheight);
		JMenuBar mb = new JMenuBar();
		JMenu testM = new JMenu("File");
		JMenuItem jcb = new JMenuItem("New");
		testM.add(jcb);
		jcb.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				FramedCurve fc = new FramedCurve();
				double[][] mp =  new double[1][];
				mp[0] = myCameraNode.getTransformation().getMatrix();
				fc.setControlPoints(mp);
				setInspectedCurve(fc );
				parent.renderAsync();
			}
		});
		jcb = new JMenuItem("Open...");
		testM.add(jcb);
		jcb.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				FramedCurve fc = openFramedCurve();
				if (fc == null) return;
				setInspectedCurve(fc );
				parent.renderAsync();
			}
		});
		jcb = new JMenuItem("Close");
		testM.add(jcb);
		jcb.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				JOGLConfiguration.theLog.log(Level.WARNING,"Not yet implemented");
				parent.renderAsync();
			}
		});
		jcb = new JMenuItem("Save");
		testM.add(jcb);
		jcb.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				theCurve.writeToFile(theCurve.getSourceFile());
				parent.renderAsync();
			}
		});
		jcb = new JMenuItem("Save As...");
		testM.add(jcb);
		jcb.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				saveFramedCurve(theCurve);
				updateFilename();
			}
		});
		mb.add(testM);
		// the inspected key frame
		testM = new JMenu("View");
		final JCheckBoxMenuItem scp = new JCheckBoxMenuItem("Show camera path");
		scp.setSelected(showCurve);
		testM.add(scp);
		scp.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				showCurve = !showCurve;
				handleShowCurve();
			}
		});
		final JCheckBoxMenuItem scw = new JCheckBoxMenuItem("Move world");
		scw.setSelected(moveWorld);
		testM.add(scw);
		scw.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				setMoveWorld(!moveWorld);
//				handleMoveWorld();
			}
		});
		mb.add(testM);
		
		panel.add(mb);
		panel.add(Box.createHorizontalGlue());
		col.add(Box.createVerticalStrut(10));
		col.add(panel);
		col.add(Box.createVerticalStrut(10));
		
		panel = Box.createHorizontalBox();
		panel.setSize(panelwidth, panelheight);
		JLabel label = new JLabel("Filename:", SwingConstants.LEFT);
		panel.add(label);
		filename = new JTextField();
		filename.setText("Untitled");
		filename.setEnabled(false);
		panel.add(filename);
		col.add(Box.createVerticalGlue());
		col.add(panel);
		
		panel = Box.createHorizontalBox();
		label = new JLabel("Key frame", SwingConstants.LEFT);
		panel.add(label);
		label = new JLabel("#=", SwingConstants.RIGHT);
		panel.add(label);
		keyNo = new JTextField(4);
		keyNo.setText("0");
		keyNo.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				int value = Integer.parseInt(keyNo.getText());
				if (value < 0 || value >= theCurve.getNumberControlPoints()) {
					keyNo.setText(Integer.toString(inspectedPoint)); 
					return; 
				}
				inspectedPoint = value;
				inspectKeyFrame();
			}
		});
		panel.add(keyNo);

		panel.add(new JButton(new RetreatAction()));
		panel.add(new JButton(new AdvanceAction()));
		
		label = new JLabel("t=", SwingConstants.RIGHT);
		panel.add(label);
		
		timeValue = new JTextField( 8);
		timeValue.setText("0");
		timeValue.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				double value = Double.parseDouble(timeValue.getText());
				int previous = inspectedPoint - 1;
				if (previous < 0) previous = 0;
				double thisTime = theCurve.getControlPoint(inspectedPoint).getTime();
				double prevTime = theCurve.getControlPoint(previous).getTime();
				int next = inspectedPoint + 1;
				double nextTime = 0.0;
				boolean atEnd = false;
				if (next >= theCurve.getNumberControlPoints()) atEnd = true;
				else nextTime = theCurve.getControlPoint(next).getTime();
				if (value < prevTime || (!atEnd && value > nextTime))	{
					JOGLConfiguration.theLog.log(Level.WARNING,"Invalid time");
					timeValue.setText(doubleToString(thisTime));
					return;
				}
				theCurve.getControlPoint(inspectedPoint).setTime(value);
				theCurve.setOutOfDate(true);
			}
		});
		panel.add(timeValue);
		
		label = new JLabel("dt=", SwingConstants.RIGHT);
		panel.add(label);
		
		durationValue = new JTextField( 8);
		durationValue.setText("0");
		durationValue.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				double value = Double.parseDouble(durationValue.getText());
				int next = inspectedPoint + 1;
				if (next >= theCurve.getNumberControlPoints()) return;
				int n = theCurve.getNumberControlPoints();
				double thisTime = theCurve.getControlPoint(inspectedPoint).getTime();
				double nextTime = theCurve.getControlPoint(next).getTime();
				double dt = value - (nextTime - thisTime);
				for (int i = next; i< n; ++i)	{
					double foo = theCurve.getControlPoint(i).getTime();
					theCurve.getControlPoint(i).setTime(foo + dt);
				}
				theCurve.setOutOfDate(true);
			}
		});
		panel.add(durationValue);
		
		panel.add(new JButton(new SaveKeyAction()));
		panel.add(new JButton(new InsertKeyAction()));
		panel.add(new JButton(new DeleteKeyAction()));
		
		//getContentPane().add(panel, BorderLayout.NORTH);
		col.add(Box.createVerticalStrut(10));
		col.add(panel);		
		
		panel = Box.createHorizontalBox();
		panel.setSize(panelwidth, panelheight);
		label = new JLabel("Playback", SwingConstants.LEFT);
		panel.add(label);
		
		playAction = new PlayAction();
		playButton = new JButton( playAction);
		panel.add(playButton);
		
		label = new JLabel("t=", SwingConstants.RIGHT);
		panel.add(label);

		playtime = new JTextField( 8);
		playtime.setText("0");
		playtime.setEnabled(false);
		panel.add(playtime);
		col.add(Box.createVerticalStrut(10));
		col.add(panel);

		getContentPane().add(col, BorderLayout.CENTER);
		
		label = new JLabel("x=", SwingConstants.RIGHT);
		panel.add(label);

		playBackF = new JTextField(4);
		setPlaybackFactor(globalPlaybackFactor);
		playBackF.setText(Double.toString(playbackFactor));
		playBackF.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				double value = Double.parseDouble(playBackF.getText());
				if (value < .1 || value >= 250) {
					playBackF.setText(doubleToString(playbackFactor)); 
					return; 
				}
				playbackFactor = value;
			}
		});
		panel.add(playBackF);
		JTabbedPane tp = new JTabbedPane();
		TransformationInspector ti = new TransformationInspector.Lookat(myCameraNode.getTransformation(), new ActionListener()	{
			public void actionPerformed(ActionEvent e) {
				parent.renderAsync();
			}
		});
		tp.addTab("From-to-roll", ti);
//		ti.addBorderTitle("Lookat form");
		ti = new TransformationInspector.Raw(myCameraNode.getTransformation(), new ActionListener()	{
			public void actionPerformed(ActionEvent e) {
				parent.renderAsync();
			}
		});
		tp.addTab("Raw", ti);
//		ti.addBorderTitle("Raw form");
		ti = new TransformationInspector.Factored(myCameraNode.getTransformation(), new ActionListener()	{
			public void actionPerformed(ActionEvent e) {
				parent.renderAsync();
			}
		});
//		ti.addBorderTitle("Factored form");

		tp.addTab("Factored", ti);
		col.add(tp);
		setSize(600,400);
		setLocation(500,600);
		
	}
	
	/**
	 * @param b
	 */
	public void setMoveWorld(boolean b) {
		if (moveWorld == b) return;
		moveWorld=b;
		DefaultMatrixSupport.getSharedInstance().restoreDefault(target.getTransformation(), true);
		if (moveWorld && worldNode != null) target = worldNode;
		else target = myCameraNode;
		JOGLConfiguration.theLog.log(Level.FINE,"move world is: "+moveWorld);
		updateCameraPosition();
	}

	/**
	 * @return Returns the worldNode.
	 */
	public SceneGraphComponent getWorldNode() {
		return worldNode;
	}
	/**
	 * @param worldNode The worldNode to set.
	 */
	public void setWorldNode(SceneGraphComponent wn) {
		if (wn == null)	{
			return;
		}
		worldNode = wn;
	}

//	/**
//	 * 
//	 */
//	protected void handleMoveWorld() {
//		target.getTransformation().resetMatrix();
//		if (moveWorld) setWorldNode(worldNode);
//		else setWorldNode(null);
//		JOGLConfiguration.theLog.log(Level.FINE,"move world is: "+moveWorld);
//		updateCameraPosition();
//	}

	/**
	 * 
	 */
	protected void updateFilename() {
		if (filename != null && theCurve != null)	{
			if (theCurve.getSourceFile() != null) {
				JOGLConfiguration.theLog.log(Level.FINE,"filename is"+theCurve.getSourceFile().toString());
				filename.setText(theCurve.getSourceFile().getName());
				resourceDir = theCurve.getSourceFile().getAbsolutePath();
			}
			else filename.setText("Untitled");		
		}
	}

	public void setInspectedCurve(FramedCurve fc) {
		// calculate time-related constants
		if (showCurve)	{
			removeCurve();
		}
		theCurve = fc;
		handleShowCurve();
		inspectedPoint = 0;
		inspectKeyFrame();
		tick = 0;
		totalSeconds = fc.getTmax() - fc.getTmin();
		totalTicks = (int) totalSeconds * framesPerSecond;
		updateFilename();
	}
	
	private void setFrameRate(int f) {
		framesPerSecond = f;
		timerInterval = 1000/framesPerSecond;
		cameraMove.setDelay(timerInterval);
	}

	private String doubleToString(double dd)	{
		return Double.toString(  (   (Math.floor(1000*dd))  * 0.001d)    )   ;
	}

	public void setTime(double t)	{
		tick = tickFromTime(t);
		updatePlayState();
	}
	
	private void inspectKeyFrame() {
		updateKeyFrame();
		time = currentPoint.getTime();
		tick = tickFromTime(currentPoint.getTime());
		writeTarget(currentPoint.getTt().getArray());
		parent.renderAsync();
	}

	private void writeTarget(double[] m) {
		//JOGLConfiguration.theLog.log(Level.FINE,"Setting time to: "+currentPoint.getTime());
		if (moveWorld)	target.getTransformation().setMatrix(Rn.inverse(null,m));
		else 		target.getTransformation().setMatrix(m);
		//System.err.println("Camera matrix is "+Rn.matrixToString(m));
	}

	private double[] readTarget() {
		if (moveWorld) return Rn.inverse(null, target.getTransformation().getMatrix());
		return target.getTransformation().getMatrix();
	}
	
	/**
	 * Update the inspector but don't render it
	 */
	private void updateKeyFrame() {
		if (inspectedPoint<0) inspectedPoint = 0;
		if (inspectedPoint>theCurve.getNumberControlPoints()-1) inspectedPoint = theCurve.getNumberControlPoints()-1;
		keyNo.setText(Integer.toString(inspectedPoint));
		timeValue.setText(doubleToString(theCurve.getControlPoint(inspectedPoint).getTime()));
		int next = inspectedPoint+1;
		if (next >= theCurve.getNumberControlPoints()) {
			durationValue.setEnabled(false);
		} else {
			durationValue.setEnabled(true);
			double t0 = theCurve.getControlPoint(inspectedPoint).getTime();
			double t1 = theCurve.getControlPoint(next).getTime();
			durationValue.setText(doubleToString(t1 - t0));
		}
		currentPoint = theCurve.getControlPoint(inspectedPoint);
	}

	public void beginInspection()	{
		((Component) parent.getViewingComponent()).addKeyListener(cdkl);
		origCamPath = parent.getCameraPath();
		parent.setCameraPath(camPath);
		//JOGLConfiguration.theLog.log(Level.FINE,"Camera is: "+CameraUtility.getCamera(parent));
		//setVisible(true);
		repaint();
		inspectKeyFrame();
	}
	
	public void endInspection()	{
		parent.setCameraPath(origCamPath);
		//JOGLConfiguration.theLog.log(Level.FINE,"Camera is: "+fciCamera);
		setVisible(false);
		repaint();		
		((Component) parent.getViewingComponent()).removeKeyListener(cdkl);
	}
	
	class SaveKeyAction extends AbstractAction		{
		
		SaveKeyAction()	{
			super();
			putValue(Action.NAME, "Save");
		}
		
		public void actionPerformed(ActionEvent e) {
			currentPoint.getTt().assignFrom(readTarget());
			theCurve.setOutOfDate(true);
		}

}

	class DeleteKeyAction extends AbstractAction		{
		
		DeleteKeyAction()	{
			super();
			putValue(Action.NAME, "Delete");
		}
		
		public void actionPerformed(ActionEvent e) {
			theCurve.deleteControlPoint(inspectedPoint);
			inspectKeyFrame();
		}
		
	}

	class InsertKeyAction extends AbstractAction		{
		
		InsertKeyAction()	{
			super();
			putValue(Action.NAME, "Insert");
		}
		
		public void actionPerformed(ActionEvent e) {
			if (inspectedPoint == theCurve.getNumberControlPoints()-1) return;
			double tm = (theCurve.getControlPoint(inspectedPoint).getTime() + theCurve.getControlPoint(inspectedPoint+1).getTime())*.5;
			FactoredMatrix tt = new FactoredMatrix();
			theCurve.getValueAtTime(tm, tt );
			FramedCurve.ControlPoint ncp = new FramedCurve.ControlPoint(tt, tm);
			theCurve.addControlPoint(ncp);
			inspectedPoint++;
			inspectKeyFrame();
		}
		
	}

	class AdvanceAction extends AbstractAction		{
		
		AdvanceAction()	{
			super();
			putValue(Action.NAME, ">");
		}
		
		public void actionPerformed(ActionEvent e) {
			inspectedPoint++;
			inspectedPoint = inspectedPoint % theCurve.getNumberControlPoints();
			inspectKeyFrame();
		}
		
	}

	class RetreatAction extends AbstractAction		{
		
		RetreatAction()	{
			super();
			putValue(Action.NAME, "<");
		}
		
		public void actionPerformed(ActionEvent e) {
			inspectedPoint--;
			if (inspectedPoint < 0) inspectedPoint += theCurve.getNumberControlPoints();

			inspectKeyFrame();
		}
		
	}

	class SaveAllAction extends AbstractAction		{
		Component parent;
		SaveAllAction()	{
			super();
			putValue(Action.NAME, "Save As...");
		}
		
		public void actionPerformed(ActionEvent e) {
			saveFramedCurve(theCurve);
		}

	}

	protected FramedCurve openFramedCurve() {
		JFileChooser fc = new JFileChooser(resourceDir);
		//JOGLConfiguration.theLog.log(Level.FINE,"FCI resource dir is: "+resourceDir);
		int result = fc.showOpenDialog(this);
		FramedCurve aCurve = null;
		if (result == JFileChooser.APPROVE_OPTION)	{
			File file = fc.getSelectedFile();
			aCurve = FramedCurve.readFromFile(file);
		} else {
			JOGLConfiguration.theLog.log(Level.FINE,"Unable to open file");
		}
		return aCurve;
	}

	private void saveFramedCurve(FramedCurve theCurve) {
		JFileChooser fc = new JFileChooser(resourceDir);
		int result = fc.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION)	{
			File file = fc.getSelectedFile();
			theCurve.writeToFile(file);
			theCurve.setSourceFile(file);
		}
	}
	
	boolean isPaused = true;
	class PlayAction extends AbstractAction		{
		PlayAction()	{
			super();
			putValue(Action.NAME, "Play");
		}
		
		public void actionPerformed(ActionEvent e) {
			togglePlay();
		}
	}
	
	private double tickFromTime(double t)	{
		return totalTicks * (t - theCurve.getTmin())/(theCurve.getTmax() - theCurve.getTmin());
	}
	
	double tick = 0;
	Transformation theTransform = new Transformation();
	protected int count = 0;
	protected double time;
	public void updateTime()	{
		// time is a parameter between 0 and 1
		time = (tick % totalTicks)/totalTicks;
		double factor = theCurve.getTmax() - theCurve.getTmin();
		time = theCurve.getTmin() + time * factor;
		updateCameraPosition();
		tick += playbackFactor;
		if (tick >= totalTicks) {
			tick=0;
			if (!loopPlay)	{
				cameraMove.stop();
				// TODO Remove this hack!
				if (cameraEnd != null) cameraEnd.start();
				playAction.putValue(Action.NAME, "Play");
			}
		}
	}
	protected boolean realTimeRendering = true;
	public void updateCameraPosition()		{
		int which = theCurve.getSegmentAtTime(time);
		if (which != inspectedPoint) {
			inspectedPoint = which;
			updateKeyFrame();
		}
		FactoredMatrix fm = theCurve.getValueAtTime(time);
		fm.assignTo(theTransform);
		playtime.setText(doubleToString(time));
		writeTarget(theTransform.getMatrix());
		if (realTimeRendering) parent.renderAsync();
		count++;
	}
	
	public void updateData()		{
		// for subclasses to implement
		JOGLConfiguration.theLog.log(Level.FINE,"Updating data");
	}

	private void handleShowCurve() {
		SceneGraphComponent sgc = theCurve.getSceneGraphRepresentation();
		if (showCurve) {
			if (worldNode != null) 
				if (!worldNode.isDirectAncestor(sgc)) worldNode.addChild(sgc);
			else  if (parent.getAuxiliaryRoot() == null || 
					!parent.getAuxiliaryRoot().isDirectAncestor(sgc)) addAuxiliaryComponent(parent, sgc);
		 	sgc.setVisible(true);
		}
		else {
			sgc.setVisible(false);
		}
		parent.renderAsync();
	}

	public void removeCurve()	{
		SceneGraphComponent sgc = theCurve.getSceneGraphRepresentation();
		if (worldNode.isDirectAncestor(sgc)) worldNode.removeChild(sgc);
		removeAuxiliaryComponent(parent, sgc);		
	}

	public void togglePlay() {
		isPaused = !isPaused;
		updatePlayState();
	}

	protected void updatePlayState() {
		if (isPaused) {
			cameraMove.stop();
			playAction.putValue(Action.NAME, "Play");
		} else	{
			totalSeconds = theCurve.getTmax() - theCurve.getTmin();
			totalTicks = (int) totalSeconds * framesPerSecond;

			cameraMove.start();
			playAction.putValue(Action.NAME, "Pause");
		}
		parent.renderAsync();
	}

	public boolean isLoopPlay() {
		return loopPlay;
	}
	public void setLoopPlay(boolean loopPlay) {
		this.loopPlay = loopPlay;
	}

	private class MyKeyListener extends KeyAdapter {
		HelpOverlay helpOverlay;	
		long beginCurveTime = 0;
		boolean active = false;
		double startTime;
		/**
		 * 
		 */
		MyKeyListener() {
			super();
			
		}
		public void keyPressed(KeyEvent e)	{
			System.err.println("Framed curve key listener got: "+e.getKeyChar());
				switch(e.getKeyCode())	{
					
				case KeyEvent.VK_0:
					tick = 0;
				    // revert to original speed
				    setPlaybackFactor(globalPlaybackFactor);
					isPaused = true;
					updatePlayState();
					if (cameraEnd != null) cameraEnd.stop();
					updateTime();
					JOGLConfiguration.theLog.log(Level.INFO,"Enter shift-P to resume playing");
					break;
					
				case KeyEvent.VK_6:
					if (e.isShiftDown()) playbackFactor /= 1.2;
					else playbackFactor *= 1.2;
					setPlaybackFactor(playbackFactor);
					JOGLConfiguration.theLog.log(Level.INFO,"playbackFactor is "+playbackFactor);
					break;
	
				case KeyEvent.VK_H:
					System.out.println("	0:  reset to beginning and pause.");
//					System.out.println("shift-M:	generate camera focus animation parameters for current path (to System.out)");
					System.out.println("shift-P: toggle play/pause");
					System.out.println("shift-U: update global anim data file");
					System.out.println("period:  append key-frame");
					System.out.println("shift-period: append key-frame and end");
					System.out.println("	6:  increase speed");
					System.out.println("shift-6:  decrease speed");
					break;
					
//				case KeyEvent.VK_M:
//					if (e.isShiftDown()) guessCameraFocusOnPath(theCurve, parent);
//					break;
					
				case KeyEvent.VK_G:
					if (e.isShiftDown()) System.gc();
					JOGLConfiguration.theLog.log(Level.INFO,"Garbage collecting");
					break;
					
				case KeyEvent.VK_P:
					if (e.isShiftDown()) togglePlay();
					break;
					
				case KeyEvent.VK_U:
					if (e.isShiftDown()) updateData();
					break;
					
				case KeyEvent.VK_PERIOD:
						// we add the new control points to the end of the inspected curve
						// TODO figure out how to insert the control points.
						if (!active) {
							beginCurveTime = e.getWhen();
							startTime = theCurve.getTmax();
							active = true;
							JOGLConfiguration.theLog.log(Level.INFO,"Key frame saving activated");
							return;
						}
					JOGLConfiguration.theLog.log(Level.INFO,"time is: "+e.getWhen());
					//JOGLConfiguration.theLog.log(Level.INFO,"Camera node is: "+Rn.matrixToString(myCameraNode.getTransformation().getMatrix()));
					
					FactoredMatrix tt = new FactoredMatrix();
					//TODO apply inverse of objectToWorld transform here
					tt.assignFrom(readTarget());
					double t = (e.getWhen() - beginCurveTime)/1000.0 + startTime;
					theCurve.addControlPoint(new FramedCurve.ControlPoint(tt,t));
					// save the curve
					if (e.isShiftDown())	{
						//saveFramedCurve(theCurve);
						//updateFilename();
						active = false;
						JOGLConfiguration.theLog.log(Level.INFO,"Key frame saving de-activated");
					}
					break;
				}
			}
	}

	public double getPlaybackFactor() {
		return playbackFactor;
	}
	public void setPlaybackFactor(double playbackFactor) {
		this.playbackFactor = playbackFactor;
		playBackF.setText(Double.toString(playbackFactor));
	}
	
	public static void addAuxiliaryComponent(Viewer v, SceneGraphComponent aux)	{
		SceneGraphComponent auxiliaryRoot = v.getAuxiliaryRoot();
		if (auxiliaryRoot == null)	{
			v.setAuxiliaryRoot(SceneGraphUtility.createFullSceneGraphComponent("AuxiliaryRoot"));
		}
		if (!auxiliaryRoot.isDirectAncestor(aux)) auxiliaryRoot.addChild(aux);
	}
	
	public static void removeAuxiliaryComponent(Viewer v, SceneGraphComponent aux)	{
		SceneGraphComponent auxiliaryRoot = v.getAuxiliaryRoot();
		if (auxiliaryRoot == null)	return;
		if (!auxiliaryRoot.isDirectAncestor(aux) ) return;
		auxiliaryRoot.removeChild(aux);
	}

	public SceneGraphComponent getCameraScaleNode() {
		return cameraScaleNode;
	}

	public void setCameraScaleNode(SceneGraphComponent cameraScaleNode) {
		this.cameraScaleNode = cameraScaleNode;
	}

}
