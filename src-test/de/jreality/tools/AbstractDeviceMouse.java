package de.jreality.tools;

import java.awt.Robot;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

import de.jreality.math.Matrix;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.tool.InputSlot;
import de.jreality.toolsystem.ToolEvent;
import de.jreality.toolsystem.ToolEventQueue;
import de.jreality.util.LoggingSystem;

public abstract class AbstractDeviceMouse {

	protected ToolEventQueue queue;
	private boolean center;
	private boolean sentCenter;
	protected static Robot robot;
	private double lastX = -1;
	private double lastY = -1;
	protected int winCenterX;
	protected int winCenterY;

	protected HashMap usedSources = new HashMap();
	protected static HashSet<String> knownSources = new HashSet<String>();
	private Matrix axesMatrix = new Matrix();
	private Matrix axesEvolutionMatrix = new Matrix();
	private DoubleArray da = new DoubleArray(axesMatrix.getArray());
	private DoubleArray daEvolution = new DoubleArray(axesEvolutionMatrix
			.getArray());

	static {
		try {
			robot = new Robot();
		} catch (Exception e) {
			e.printStackTrace();
		}
		knownSources.add("left");
		knownSources.add("center");
		knownSources.add("right");
		knownSources.add("axes");
		knownSources.add("axesEvolution");
		knownSources.add("wheel_up");
		knownSources.add("wheel_down");
	}

	double[] xy = new double[2];
	int count = 0, numSamples = 3, samplingRate = 5;
	
	double[] weights = { .434, .333, .233, .1 };
	double[][] history = new double[numSamples][2];
	public AbstractDeviceMouse() {
//		if (numSamples > 1) 		{
//			Runnable mousePoller = new Runnable() {
//
//				public void run() {
//					startupPoller();
//				}
//			
//			};
//			mousePoller.run();
//		}
	}

	private void startupPoller()		{
		Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
		Controller firstMouse=null;
		for(int i=0;i<controllers.length && firstMouse==null;i++) {

			if(controllers[i].getType()==Controller.Type.MOUSE) {
				// Found a mouse
				firstMouse = controllers[i];
			}
		}
		if(firstMouse==null) {
			// Couldn't find a mouse
			System.out.println("Found no mouse");
			System.exit(0);
		}
		
		System.out.println("First mouse is: " + firstMouse.getName());
		
		while(true) {
			firstMouse.poll();
			Component[] components = firstMouse.getComponents();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<components.length;i++) {
				if(i>0) {
					buffer.append(", ");
				}
				buffer.append(components[i].getName());
				buffer.append(": ");
			if(components[i].isAnalog()) {
				buffer.append(components[i].getPollData());
			} else {
				if(components[i].getPollData()==1.0f) {
					buffer.append("On");
				} else {
					buffer.append("Off");
				}
			}
			}
			System.out.println(buffer.toString());
	
			try {
				Thread.sleep(samplingRate);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void weightedAverage() {
		xy[0] = xy[1] = 0.0;
		int cmod = count % numSamples;
		for (int i = 0; i < numSamples; ++i) {
			xy[0] += weights[(i) % numSamples]
					* history[(cmod + i) % numSamples][0];
			xy[1] += weights[(i) % numSamples]
					* history[(cmod + i) % numSamples][1];
		}
	}

	protected synchronized void mouseMoved(int ex, int ey) {
		history[count % numSamples][0] = ex;
		history[count % numSamples][1] = ey;
		// Rn.average(xy, history);
		weightedAverage();
		count = (count + 1);
		if (count < numSamples) {
			xy[0] = ex;
			xy[1] = ey;
		}
		// System.err.println("ex ey = "+ex+" "+ey);
		// System.err.println("x y = "+xy[0]+" "+xy[1]);
		InputSlot slot = (InputSlot) usedSources.get("axes");
		if (slot != null) {
			if (!isCenter()) {
				double xndc = -1. + (2. * xy[0]) / getWidth();
				double yndc = 1. - (2. * xy[1]) / getHeight();

				axesMatrix.setEntry(0, 3, xndc);
				axesMatrix.setEntry(1, 3, yndc);
				axesMatrix.setEntry(2, 3, -1);
				queue.addEvent(new ToolEvent(AbstractDeviceMouse.this, System
						.currentTimeMillis(), slot, da) {
					protected boolean compareTransformation(DoubleArray trafo1,
							DoubleArray trafo2) {
						return true;
					}
				});
			} else if (!sentCenter) {
				axesMatrix.setEntry(0, 3, 0);
				axesMatrix.setEntry(1, 3, 0);
				axesMatrix.setEntry(2, 3, -1);
				queue.addEvent(new ToolEvent(AbstractDeviceMouse.this, System
						.currentTimeMillis(), slot, da));
				sentCenter = true;
				// XXX HACK !!! this should be getWidth()/2 but differs by
				// border frame
				lastX = xy[0];
				lastY = xy[1];
			}
		}
		slot = (InputSlot) usedSources.get("axesEvolution");
		if (slot != null) {
			if (lastX == -1) {
				lastX = xy[0];
				lastY = xy[1];
				return;
			}

			double dx = xy[0] - lastX;
			double dy = xy[1] - lastY;

			if (dx == 0 && dy == 0)
				return;

			double dxndc = (2. * dx) / getWidth();
			double dyndc = -(2. * dy) / getHeight();

			axesEvolutionMatrix.setEntry(0, 3, dxndc);
			axesEvolutionMatrix.setEntry(1, 3, dyndc);
			axesEvolutionMatrix.setEntry(2, 3, -1);

			ToolEvent evolutionEvent = new ToolEvent(AbstractDeviceMouse.this,
					System.currentTimeMillis(), slot, daEvolution) {
				protected boolean compareTransformation(DoubleArray trafo1,
						DoubleArray trafo2) {
					return true;
				}

				protected void replaceWith(ToolEvent replacement) {
					Matrix m = new Matrix(replacement.getTransformation());
					m.multiplyOnRight(getTransformation().toDoubleArray(null));
					trafo = new DoubleArray(m.getArray());
					time = replacement.getTimeStamp();
				}
			};
			queue.addEvent(evolutionEvent);
			if (isCenter()) {
				try {
					robot.mouseMove(winCenterX, winCenterY);
				} catch (Exception exc) {
					LoggingSystem.getLogger(this).log(Level.CONFIG,
							"cannot use robot: ", exc);
				}
			} else {
				lastX = xy[0];
				lastY = xy[1];
			}
		}
	}

	public void setEventQueue(ToolEventQueue queue) {
		this.queue = queue;
	}

	public boolean isCenter() {
		return center;
	}

	public synchronized void setCenter(boolean center) {
		if (this.center != center)
			sentCenter = false;
		this.center = center;
		if (center) {
			calculateCenter();
			robot.mouseMove(winCenterX, winCenterY);
			installGrabs();
		} else {
			uninstallGrabs();
		}
	}

	protected abstract void calculateCenter();

	protected abstract void uninstallGrabs();

	protected abstract void installGrabs();

	protected abstract int getWidth();

	protected abstract int getHeight();

}
