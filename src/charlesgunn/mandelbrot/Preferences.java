//
//	File:		Preferences.java
//

package charlesgunn.mandelbrot;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Preferences extends Frame
					  implements ActionListener
{
    protected Button okButton;
    protected Label prefsText;

    public Preferences()
    {
        super();
        this.setLayout(new BorderLayout(15, 15));
        this.setFont(new Font ("SansSerif", Font.BOLD, 14));

        prefsText = new Label ("PROJECTNAMEASIDENTIFIER Preferences...");
        Panel textPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        textPanel.add(prefsText);
        this.add (textPanel, BorderLayout.NORTH);
		
        okButton = new Button("OK");
        Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.add (okButton);
        okButton.addActionListener(this);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }
	

    public void actionPerformed(ActionEvent newEvent) 
    {
        setVisible(false);
    }	
	
}