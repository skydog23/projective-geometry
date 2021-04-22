/*
 * Author	gunn
 * Created on Oct 17, 2005
 *
 */
package charlesgunn.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import de.jreality.math.FactoredMatrix;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Transformation;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;

/**
 * @author gunn
 *
 */
public abstract class TransformationInspector extends JPanel implements ActionListener, TransformationListener	{
	Transformation tform = null;
	ActionListener actionL;
	Box vbox, hbox;
	
	public TransformationInspector(double[] m, ActionListener al)	{
		this(new Transformation(m), al);
	}
	
	public TransformationInspector(Transformation t, ActionListener al)	{
		super();
		tform = t;
		actionL = al;		
		tform.addTransformationListener(this);
		vbox = Box.createVerticalBox();
		hbox = Box.createHorizontalBox();
		hbox.add(vbox);
		hbox.add(Box.createHorizontalGlue());
		add(hbox);
		addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent e)	{
				update();
			}
		});
		
	}

    public void addBorderTitle(String s)	{
		TitledBorder title = BorderFactory.createTitledBorder(
                BorderFactory.createRaisedBevelBorder(), s);
		hbox.setBorder(title);
    }
    
	public void actionPerformed(ActionEvent e) {
		actionL.actionPerformed(e);
	}

	public void transformationMatrixChanged(TransformationEvent ev) {
		if (isShowing()) update();
	}


	public void update() {
		repaint();
	}
	
	public abstract String getDescriptiveLabel();
	
	public static class Factored 	extends TransformationInspector	{
		
		FactoredMatrix fm = null;
		double[] translation, rotation, stretch;

		double angle;
		Box tlate, rotate, scale;
		OneEntry[] theEntries;

		public Factored(Transformation t, ActionListener al) {
			super(t, al);
			fm = new FactoredMatrix(tform.getMatrix());
			translation = fm.getTranslation();
			rotation = fm.getRotationAxis();
			angle = fm.getRotationAngle();
			stretch = fm.getStretch();
			theEntries = new OneEntry[10];
			tlate = Box.createHorizontalBox();
			rotate = Box.createHorizontalBox();
			scale = Box.createHorizontalBox();
			Font f = new Font("Helvetica", Font.PLAIN, 12);
			JLabel label = new JLabel("translate", JLabel.LEFT);
			label.setFont(f);
			tlate.add(label);
			tlate.add(theEntries[0] = new OneEntry("x", translation[0], this));
			tlate.add(theEntries[1] = new OneEntry("y", translation[1], this));
			tlate.add(theEntries[2] = new OneEntry("z", translation[2], this));
			tlate.add(Box.createHorizontalGlue());
			label = new JLabel("rotation", JLabel.LEFT);
			label.setFont(f);
			rotate.add(label);
			rotate.add(theEntries[3] = new OneEntry("x", rotation[0], this));
			rotate.add(theEntries[4] = new OneEntry("y", rotation[1], this));
			rotate.add(theEntries[5] = new OneEntry("z", rotation[2], this));
			rotate.add(theEntries[6] = new OneEntry("angle", angle, this));
			rotate.add(Box.createHorizontalGlue());
			label = new JLabel("scale", JLabel.LEFT);
			label.setFont(f);
			scale.add(label);
			scale.add(theEntries[7] = new OneEntry("x", stretch[0], this));
			scale.add(theEntries[8] = new OneEntry("y", stretch[1], this));
			scale.add(theEntries[9] = new OneEntry("z", stretch[2], this));
			scale.add(Box.createHorizontalGlue());
			vbox.add(scale);
			vbox.add(rotate);
			vbox.add(tlate);
		}

		public void actionPerformed(ActionEvent arg0) {
			translation[0] = theEntries[0].getValue();
			translation[1] = theEntries[1].getValue();
			translation[2] = theEntries[2].getValue();
			rotation[0] = theEntries[3].getValue();
			rotation[1] = theEntries[4].getValue();
			rotation[2] = theEntries[5].getValue();
			angle = theEntries[6].getValue();
			stretch[0] = theEntries[7].getValue();
			stretch[1] = theEntries[8].getValue();
			stretch[2] = theEntries[9].getValue();
			fm.setRotation(angle, rotation);
			fm.setTranslation(translation);
			fm.setStretch(stretch);
			fm.update();
			tform.setMatrix(fm.getArray());
			super.actionPerformed(arg0);
		}

		public void update() {
			fm = new FactoredMatrix(tform.getMatrix());
			translation = fm.getTranslation();
			rotation = fm.getRotationAxis();
			stretch = fm.getStretch();
			angle = fm.getRotationAngle();
			theEntries[0].setValue(translation[0]);
			theEntries[1].setValue(translation[1]);
			theEntries[2].setValue(translation[2]);
			theEntries[3].setValue(rotation[0]);
			theEntries[4].setValue(rotation[1]);
			theEntries[5].setValue(rotation[2]);
			theEntries[6].setValue(angle);
			theEntries[7].setValue(stretch[0]);
			theEntries[8].setValue(stretch[1]);
			theEntries[9].setValue(stretch[2]);
			super.update();
		}

		public String getDescriptiveLabel() {
			return "Factored";
		}

	}

	public static class Lookat 	extends TransformationInspector	{
		
		double[] from = new double[4], 
			to = new double[4], 
			up = new double[4], 
			upNoRoll = new double[4], 
			up3 = new double[3],
			upNoRoll3 = new double[3],
			cameraToWorld = new double[16], 
			mNoRoll = new double[16];
		double roll;
		Box fromB, toB, rollB;
		OneEntry[] theEntries;

		public Lookat(Transformation t, ActionListener al) {
			super(t, al);
			processCameraTransform();
			
			theEntries = new OneEntry[9];
			fromB = Box.createHorizontalBox();
			toB = Box.createHorizontalBox();
			rollB = Box.createHorizontalBox();
			Font f = new Font("Helvetica", Font.PLAIN, 12);
			JLabel label = new JLabel("from", JLabel.LEFT);
			label.setFont(f);
			fromB.add(label);
			fromB.add(theEntries[0] = new OneEntry("x", from[0], null));
			fromB.add(theEntries[1] = new OneEntry("y", from[1], null));
			fromB.add(theEntries[2] = new OneEntry("z", from[2], null));
			fromB.add(theEntries[3] = new OneEntry("w", from[3], null));
			fromB.add(Box.createHorizontalGlue());
			label = new JLabel("to", JLabel.LEFT);
			label.setFont(f);
			toB.add(label);
			toB.add(theEntries[4] = new OneEntry("x", to[0], null));
			toB.add(theEntries[5] = new OneEntry("y", to[1], null));
			toB.add(theEntries[6] = new OneEntry("z", to[2], null));
			toB.add(theEntries[7] = new OneEntry("w", to[3], null));
			toB.add(Box.createHorizontalGlue());
			rollB.add(theEntries[8] = new OneEntry("roll", roll, null));
			JButton butt = new JButton("Apply");
			butt.setFont(f);
			butt.addActionListener( new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					updateEntries(e);
				}
			});
			
			rollB.add(butt);
			rollB.add(Box.createHorizontalGlue());
			vbox.add(fromB);
			vbox.add(toB);
			vbox.add(rollB);
		}


		private void updateEntries(ActionEvent e) {
			for (int i = 0; i<theEntries.length; ++i)	
				theEntries[i].updateValue();
			actionPerformed(e);
		}
		/**
		 * 
		 */
		private void processCameraTransform() {
			cameraToWorld = tform.getMatrix();
			from = new double[4];
			to = new double[4];
			up = new double[4];
			upNoRoll = new double[4];
			for (int i = 0; i<4; ++i)	{
				from[i] = cameraToWorld[4*i+3];
				to[i] = -cameraToWorld[4*i+2];
				up[i] = cameraToWorld[4*i+1];
			}
			// the lookat matrix is a worldToCamera transformation
			mNoRoll = Rn.inverse(null,P3.makeLookatMatrix(null, from, to, 0.0, Pn.EUCLIDEAN));
			for (int i = 0; i<4; ++i)	
				upNoRoll[i] = mNoRoll[4*i+1];
			
			Pn.dehomogenize(up3, up);
			Pn.dehomogenize(upNoRoll3, upNoRoll);
			roll = 180.0/Math.PI * Pn.angleBetween(up3, upNoRoll3, Pn.EUCLIDEAN);
			if (roll != 0.0)	{
				double foo = P3.orientation(to, up, upNoRoll);
//				System.out.println(("orientation is "+((foo<0.0)?"-":"+")));
				if (foo < 0) roll *= -1;
			}
		}

		public void actionPerformed(ActionEvent arg0) {
			from[0] = theEntries[0].getValue();
			from[1] = theEntries[1].getValue();
			from[2] = theEntries[2].getValue();
			from[3] = theEntries[3].getValue();
			to[0] = theEntries[4].getValue();
			to[1] = theEntries[5].getValue();
			to[2] = theEntries[6].getValue();
			to[3] = theEntries[7].getValue();
			roll = theEntries[8].getValue();
			cameraToWorld = Rn.inverse(null, P3.makeLookatMatrix(null, from, to, Math.PI /180.0 * roll, Pn.EUCLIDEAN));
//			System.out.println("Lookat: "+Rn.matrixToString(cameraToWorld));
			tform.setMatrix(cameraToWorld);
			super.actionPerformed(arg0);
		}

		public void update() {
			processCameraTransform();
			theEntries[0].setValue(from[0]);
			theEntries[1].setValue(from[1]);
			theEntries[2].setValue(from[2]);
			theEntries[3].setValue(from[3]);
			theEntries[4].setValue(to[0]);
			theEntries[5].setValue(to[1]);
			theEntries[6].setValue(to[2]);
			theEntries[7].setValue(to[3]);
			theEntries[8].setValue(roll);
			super.update();
		}


		public String getDescriptiveLabel() {
			return "Lookat";
		}

	}

	public static class Raw 	extends TransformationInspector	{
		
		Box[] hboxes;
		OneEntry[] theEntries;
		double[] matrix;
		public Raw(Transformation t, ActionListener al) {
			super(t, al);
			matrix = tform.getMatrix();
			theEntries = new OneEntry[16];
			hboxes = new Box[4];
			for (int i = 0; i<4; ++i)	{
				hboxes[i] = Box.createHorizontalBox();
				vbox.add(hboxes[i]);
				for (int j = 0; j<4; ++j)	
					hboxes[i].add(theEntries[4*i+j] = new OneEntry("m"+i+j, matrix[4*i+j], this));
			}
		}

		public void actionPerformed(ActionEvent arg0) {
			for (int i = 0; i<4; ++i)	
				for (int j = 0; j<4; ++j)	
					matrix[4*i+j] = theEntries[4*i+j].getValue();

			tform.setMatrix(matrix);
			super.actionPerformed(arg0);
		}

		public void update() {
			matrix = tform.getMatrix();
			for (int i = 0; i<4; ++i)	{
				for (int j = 0; j<4; ++j)	
					theEntries[4*i+j].setValue(matrix[4*i+j]);
			}
			super.update();
		}

		public String getDescriptiveLabel() {
			return "Raw";
		}

	}
	

}
