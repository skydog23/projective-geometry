/*
 * Author	gunn
 * Created on Oct 21, 2005
 *
 */
package charlesgunn.gui;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.Box;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

public class OneEntry extends JPanel implements ActionListener {
	public JLabel label;

	public JFormattedTextField textField;
	NumberFormatter formatter = null;
	NumberFormat numberFormat = null;
	double value;

	ActionListener parent;

	public OneEntry(String l, double d, ActionListener p) {
		super();
		value = d;
		parent = p;
		//setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		// JOGLConfiguration.theLog.log(Level.FINER,"Initializing slider
		// with min/max: "+min+"/"+max);
		label = new JLabel(l + ":", JLabel.LEFT);
		Font f = new Font("Helvetica", Font.PLAIN, 12);
		label.setFont(f);

		numberFormat = NumberFormat.getNumberInstance();
		formatter = new NumberFormatter(numberFormat);
		formatter.setValueClass(Double.class);
		textField = new JFormattedTextField(formatter);
		f = new Font("Courier", Font.PLAIN, 12);
		textField.setFont(f);
		textField.setValue(new Double(d));
		textField.setColumns(8); // get some space
		textField.addActionListener(this);
		Box box = Box.createHorizontalBox();
		box.add(label);
		box.add(Box.createHorizontalStrut(4));
		box.add(textField);
		add(box);
	}

	public void actionPerformed(ActionEvent arg0) {
		updateValue();
		if (parent != null) parent.actionPerformed(arg0);
	}

	/**
	 * 
	 */
	public void updateValue() {
		if (!textField.isEditValid()) { // The text is invalid.
			Toolkit.getDefaultToolkit().beep();
			textField.selectAll();
		} else
			try { // The text is valid,
				textField.commitEdit(); // so use it.
				java.lang.Double dd = (java.lang.Double) textField.getValue();
				value = dd.doubleValue();
			} catch (java.text.ParseException exc) {
				exc.printStackTrace();
			}
	}

	public void setValue(double d) {
		textField.setValue(new Double(d));
		value = d;
	}

	public double getValue() {
		return value;
	}
}
