/*
 * Created on Mar 17, 2005
 *
 */
package charlesgunn.jreality.worlds;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import charlesgunn.jreality.geometry.Snake;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.util.MyMidiSynth;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.Primitives;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

public class BouncingSpheres extends Assignment {

	double[][] positions;
	double[][] directions;
	double[][] colors;
	double[] speeds;
	Snake snake;
	Object lockObject = new Object();
	int framesPerSecond = 60;
	int ballCount = 50;
	double globalSpeed = 1.0;
	boolean firstTime = true;
	boolean wallSound = true;
	boolean doSound = true;
	MyMidiSynth midi = new MyMidiSynth();
	javax.swing.Timer updateTimer = null;
	int[] indices = {8,9,10,11,12,13};
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("bouncingSpheres");
	Viewer viewer;
	
	public Geometry createSpheres(int length)	{
		setDoSound(doSound);
		positions = new double[length][3];
		directions = new double[length][3];
		colors = new double[length][3];
		speeds = new double[length];
		for (int i = 0; i<length; ++i)	{
			for (int j = 0; j<3; ++j)	{
				positions[i][j] = 2 * (-.5 + Math.random());
				directions[i][j]  = -.5 + Math.random();
				colors[i][j]  = Math.random();
			}
			Rn.normalize(directions[i], directions[i]);
			Rn.times(directions[i], Math.random(), directions[i]);
			speeds[i] = Math.random();
		}
		snake = new Snake(positions);
		snake.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(colors));
		//System.out.println("VC is originally: "+snake.getVertexAttributes(Attribute.COLORS));
		return snake;
	}
	public SceneGraphComponent getContent() {
		Appearance ap  = world.getAppearance();
		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.SPHERES_DRAW, true);
		ap.setAttribute(CommonAttributes.ANY_DISPLAY_LISTS, false);
		world.setGeometry(createSpheres(ballCount));
		SceneGraphComponent frame = SceneGraphUtility.createFullSceneGraphComponent("Frame");
		ap = frame.getAppearance();
		ap.setAttribute(CommonAttributes.FACE_DRAW, true);
		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TRANSPARENCY, .9);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		frame.setGeometry(Primitives.cube());
		
		updateTimer = new javax.swing.Timer(1000/framesPerSecond, new ActionListener()	{
			public void actionPerformed(ActionEvent e) {
				updatePositions(); 
			} 
		} );
		updateTimer.setCoalesce(true);
		updateTimer.start();
		world.addChild(frame);
		return world;
	}
 	public void dispose() {
		if (updateTimer != null)	{
			updateTimer.stop();
			updateTimer = null;
		}
		if (midi != null) midi.close();
	}
        
	public void display() {
		viewer = jrviewer.getViewer();
	}

	private void setDoSound(boolean b) {
		doSound = b;
		if (doSound)	{
			doSound = midi.open();
			if (!doSound) return;
			for (int j = 0; j<indices.length; ++j)	{
				midi.getSynthesizer().loadInstrument(midi.getInstruments()[indices[j]]);
				midi.getChannels()[j].channel.programChange(indices[j]);			
			}
		} else midi.close();
	}
	
	private void setBalllCount(int n)	{
		ballCount = n;
		synchronized(lockObject)	{
			world.setGeometry( createSpheres(ballCount));
		}
	}
	
	private void setFramesPerSecond(int n)	{
		framesPerSecond = n;
		updateTimer.setDelay(1000/framesPerSecond);
	}
	private void setGlobalSpeed(double n)	{
		globalSpeed = n;
	}
	public void updatePositions()	{
		if (firstTime) { firstTime = false; return; }
		double factor = globalSpeed/framesPerSecond;
			for (int i = 0; i<ballCount; ++i)	{
				Rn.add(positions[i], positions[i], Rn.times(null, factor, directions[i]));
				for (int j = 0; j<3; ++j)		{
					if (positions[i][j] > 1)		{
						positions[i][j] -= 2*(positions[i][j] -1 );
						directions[i][j] *= -1.0;
						if (doSound) midi.getChannels()[2*j].channel.noteOn((int) (24+speeds[i]*64), midi.getChannels()[2*j].getVelocity());
						//System.out.print("+"+j);
						break;
					}
					else if (positions[i][j] < -1)		{
						positions[i][j] -= 2*(positions[i][j] + 1);
						directions[i][j] *= -1.0;
						if (doSound) midi.getChannels()[2*j+1].channel.noteOn((int) (24+speeds[i]*64), midi.getChannels()[2*j+1].getVelocity());
						//System.out.print("-"+j);
						break;
					}
				}
			}
			snake.update();
			snake.fireChange();
			
		if (viewer != null) viewer.renderAsync();
	}
	
	@Override
	public Component getInspector() {
		Box container = inspector;
		final TextSlider ballCount = new TextSlider.Integer("ballCount", SwingConstants.HORIZONTAL, 0, 250, 100);
	    ballCount.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e) {
				setBalllCount(ballCount.getValue().intValue());
			}	       	  
	    });
		container.add(ballCount);
		final TextSlider frameTime = new TextSlider.Integer("frameRate",  SwingConstants.HORIZONTAL, 0, 250, 60);
	    frameTime.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e) {
				setFramesPerSecond(frameTime.getValue().intValue());
			}	       	  
	    });
		container.add(frameTime);
		final TextSlider globalSpeed = new TextSlider.Double("globalSpeed",  SwingConstants.HORIZONTAL, 0.0, 10.0, 1.0);
	    globalSpeed.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e) {
				setGlobalSpeed(globalSpeed.getValue().doubleValue());
			}	       	  
	    });
		container.add(globalSpeed);
		
		final JCheckBox doSoundBox = new JCheckBox("Audio", doSound );
		doSoundBox.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				doSound = doSoundBox.isSelected();
				setDoSound(doSound);
				viewer.render();
			}

		});
	    doSoundBox.setAlignmentX(0.0f);
	    Box sb = new Box(BoxLayout.LINE_AXIS);
	    sb.add(doSoundBox);
	    sb.add(Box.createHorizontalGlue());
		container.add(sb);
		
		container.add(Box.createVerticalGlue());
		container.setName("Parameters");
		return container;
	}
	
	public static void main(String[] args) {
		new BouncingSpheres().display();
	}
}
