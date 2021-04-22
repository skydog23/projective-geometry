/*
 * Created on Jul 29, 2008
 *
 */
package de.jreality.geometry;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingConstants;

import de.jreality.tutorial.util.TextSlider;

public class CellularAutomata {

	int width = 256, initWidth = 128;
	int height = 256;
	int[][] rows = new int[2][width];
	int current = 0, currentRow;
	BufferedImage image;
	Graphics2D context;
	double threshhold = .5;
	int zoom = 4;
	static int delay = 1;
	int[][] rules = new int[4][32];
	boolean symmetrize = true, 
		singleStep = false,
		pause = false, step = false;

	int[][] symmetries = {{1,16},{2,8},{3,24},{5,20},{6,12},{7,28},{9,18},
			{11,26},{13,22},{15,30},{19,25},{23,29}};
	
	public void initialize()	{
		image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		context = image.createGraphics();
		int begin = (width-initWidth)/2;
		int extent = initWidth;
		for (int i = 0; i<width; ++i)	{
			if (i >= begin && i < (begin+extent)) 
				rows[0][i] = (Math.random() > threshhold) ?  1 : 0;
			else rows[0][i] = 0;
			rows[1][i] = 0;
		}
		currentRow = current  = 0;
	}
	private void newRule() {
		for (int i = 0; i<numRules; ++i)	{
			int val = (int) (((1<<31)-1) * Math.random());
			for (int j = 0; j<32; ++j)	{
				rules[i][j] = ((val>>j)%2);
//				System.err.println("rule "+i+" "+j+" "+rules[i][j]);
			}
//			rules[i][0] = 0;
			if (symmetrize)	{
				for (int j = 0; j<symmetries.length; ++j)	{
					rules[i][symmetries[i][1]] = rules[i][symmetries[i][0]];
				}
			}
			System.err.println(String.format("%x", val));
		}
	}
	double[][] colors = {{255,255,255,255},{0,0,0,255}};
	int numRules = 1;
	private void doOneRow()	{
		int next = 1 - current;
//		System.err.println("row "+currentRow);
		for (int i = 0; i<width; ++i)	{
			int[] inds =  { (i+width-2)%width,
				(i+width-1)%width,
				i,
				(i+1)%width,
				(i+2)%width};
			int val = 0;
			for (int j = 0; j<5; ++j) {
				val += rows[current][inds[j]]<<j;
			}
//			System.err.println("Val = "+val);
			int whichrule = (numRules*i)/width;
			rows[next][i] = rules[whichrule][val];
			
			image.getRaster().setPixel(i, currentRow, colors[rows[next][i]]);
		}
		current = 1 -current;
		currentRow = (currentRow + 1)%height;
	}
	
	int count = 0;
	private void run()	{
		while (true)	{
			doOneRow();
			if ( (currentRow % height) == 0) {
				step = false;
				if (singleStep)	{
					while (!step);
					step = false;
				}
				getComponent().repaint();
//				System.err.println("Redrawing "+(count++));
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			while (pause) ;
			
		}

	}
	JComponent comp = null;
	JComponent getComponent()	{
		if (comp != null) return comp;
		Box hbox = Box.createHorizontalBox();
		final JCheckBox pauseb = new JCheckBox("pause");
		pauseb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				pause = pauseb.isSelected();
			}
			
		});
		Box container = Box.createVerticalBox();
		container.add(hbox);
		hbox.add(pauseb);
		final JButton newrule = new JButton("new rule");
		newrule.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				boolean oldPause = pause;
				pause = true;
				step = true;
				newRule();
				initialize();
				pause = oldPause;
			}
			
		});
		hbox.add(newrule);
		final JButton reset = new JButton("reset");
		reset.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				boolean oldPause = pause;
				pause = true;
				step = true;
				initialize();
				pause = oldPause;
			}
			
		});
		hbox.add(reset);
		final JCheckBox singleStepB = new JCheckBox("single step");
		singleStepB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				singleStep = singleStepB.isSelected();
				if (singleStep) step = false;
			}
			
		});
		hbox.add(singleStepB);
		final JButton stepB = new JButton("step");
		stepB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				step = true;
			}
			
		});
		hbox.add(stepB);
		final TextSlider delayS = new TextSlider.Integer("delay",SwingConstants.HORIZONTAL,0,200,delay);
		delayS.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				delay = delayS.getValue().intValue();
			}
			
		});
		container.add(delayS);
		final TextSlider threshS = new TextSlider.Double("density",SwingConstants.HORIZONTAL,0,1,threshhold);
		threshS.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				threshhold= threshS.getValue().doubleValue();
				//initialize();
			}
			
		});
		container.add(threshS);
		
		final TextSlider fracS = new TextSlider.Integer("init width",SwingConstants.HORIZONTAL,0,width,initWidth);
		fracS.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				initWidth= fracS.getValue().intValue();
				//initialize();
			}
			
		});
		container.add(fracS);
		
		JComponent jc = new JComponent() {
			protected void paintComponent(Graphics g) {
				g.drawImage(image.getScaledInstance(
						zoom*width, 
						zoom*height, 
						BufferedImage.SCALE_REPLICATE),
				0,0,null);
//				System.err.println("Redrawing "+(count++));
			}
		};
		jc.setDoubleBuffered(true);
		container.add(jc);
		container.addKeyListener(new KeyAdapter()	{
			public void keyPressed(KeyEvent e) {
				System.err.println("key event "+e);
				switch(e.getKeyCode())	{
					case KeyEvent.VK_1:
						pause = !pause;
						break;
					case KeyEvent.VK_2:
						pause = true;
						initialize();
						pause = false;
						System.err.println("new rule");
						break;

				}
			}	
		});
		jc.setPreferredSize(new Dimension(zoom*width, zoom*height));
		comp = container;
		return comp;

	}
	public static void main(String[] args) {
		final CellularAutomata ca = new CellularAutomata();
		ca.initialize();
		ca.newRule();
//		for (int i = 0; i<256; ++i)	{
//			ca.doOneRow();
//		}
		JFrame f = new JFrame("Cellular automata");
		f.getContentPane().add(ca.getComponent());
		f.validate();
		f.setVisible(true);
		ca.run();
	}

}
