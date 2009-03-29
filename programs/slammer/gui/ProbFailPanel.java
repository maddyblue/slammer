/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import slammer.*;
import slammer.analysis.*;

class ProbFailPanel extends JPanel implements ActionListener
{
	SlammerTabbedPane parent;

	JLabel labelOne = new JLabel("What is the Newmark displacement (in cm)?");
	JLabel labelRes = new JLabel("Estimated probability of failure:");
	JTextField labelOnef = new JTextField(7);
	JTextField labelResf = new JTextField(7);
	JEditorPane ta = new JEditorPane();
	JScrollPane sta = new JScrollPane(ta);
	JButton button = new JButton("Perform Analysis");

	String probFailureStr = "This program estimates probability of failure as a function of estimated Newmark displacement (specified in indicated field), as described by Jibson and others (1998, 2000). The probability is estimated using the following equation:"
	+ "<p><i>P(f)</i> = 0.335(1 - exp(-0.048 <i>D<sub>n</sub></i><sup>1.565</sup>)"
	+ "<p>where <i>P(f)</i> is the probability of failure and <i>D<sub>n</sub></i> is Newmark displacement in centimeters. This equation was calibrated using data from the 1994 Northridge, California, earthquake and is best suited to application in southern California (Jibson and others, 1998, 2000).";

	public ProbFailPanel(SlammerTabbedPane parent) throws Exception
	{
		this.parent = parent;

		button.setActionCommand("do");
		button.addActionListener(this);

		ta.setEditable(false);
		ta.setContentType("text/html");
		ta.setText(probFailureStr);
		labelResf.setEditable(false);

		JLabel dummy = new JLabel(" ");

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);

		Insets top = new Insets(10, 0, 0, 0);
		Insets none = new Insets(0, 0, 0, 0);

		int x = 0;
		int y = 0;

		c.gridx = x++;
		c.gridy = y++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		gridbag.setConstraints(labelOne, c);
		add(labelOne);

		c.gridy = y++;
		gridbag.setConstraints(labelOnef, c);
		add(labelOnef);

		c.insets = top;
		c.gridy = y++;
		gridbag.setConstraints(button, c);
		add(button);

		c.insets = top;
		c.gridy = y++;
		gridbag.setConstraints(labelRes, c);
		add(labelRes);

		c.insets = none;
		c.gridy = y++;
		gridbag.setConstraints(labelResf, c);
		add(labelResf);

		c.gridx = x--;
		c.weightx = 1;
		gridbag.setConstraints(dummy, c);
		add(dummy);

		c.gridy = y;
		c.gridx = x;
		c.insets = none;
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(sta, c);
		add(sta);
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			if(command.equals("do"))
			{
				Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "Newmark displacement field", null, false, null, new Double(0), true, null, false);
				if(d1 == null) return;

				labelResf.setText(RigidBlockSimplified.ProbFailure(d1.doubleValue()));
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}
}
