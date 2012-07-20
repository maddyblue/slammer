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

	JTextField displacement = new WideTextField(7);
	JTextField result = new WideTextField(7);
	JEditorPane ta = new JEditorPane();
	JScrollPane sta = new JScrollPane(ta);
	JButton button = new JButton("Compute");

	String probFailureStr = "This program estimates probability of failure as a function of estimated Newmark displacement (specified in indicated field), as described by Jibson and others (1998, 2000). The probability is estimated using the following equation:"
	+ "<p><i>P(f)</i> = 0.335(1 - exp(-0.048 <i>D<sub>n</sub></i><sup>1.565</sup>)"
	+ "<p>where <i>P(f)</i> is the probability of failure and <i>D<sub>n</sub></i> is Newmark displacement in centimeters. This equation was calibrated using data from the 1994 Northridge, California, earthquake and is best suited to shallow landslides in southern California (Jibson and others, 1998, 2000).";

	public ProbFailPanel(SlammerTabbedPane parent) throws Exception
	{
		this.parent = parent;

		button.setActionCommand("do");
		button.addActionListener(this);

		ta.setEditable(false);
		ta.setContentType("text/html");
		ta.setText(probFailureStr);
		result.setEditable(false);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		JPanel panel = new JPanel();
		panel.setLayout(gridbag);

		Insets top = new Insets(10, 0, 0, 0);
		Insets none = new Insets(0, 0, 0, 0);

		int x = 0;
		int y = 0;
		JLabel label;

		c.gridx = x++;
		c.gridy = y++;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		label = new JLabel("Input parameter (Jibson and others, 1998, 2000):");
		label.setFont(GUIUtils.headerFont);
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		c.gridwidth = 1;
		label = new JLabel("Newmark displacement (cm):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(displacement, c);
		panel.add(displacement);

		c.insets = top;
		c.gridx = x++;
		c.gridy = y++;
		gridbag.setConstraints(button, c);
		panel.add(button);

		c.gridy = y++;
		label = new JLabel("Result:");
		label.setFont(GUIUtils.headerFont);
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.insets = none;
		c.gridy = y++;
		label = new JLabel("Estimated probability of failure:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(result, c);
		panel.add(result);
		
		c.gridx = x + 2;
		c.weightx = 1;
		label = new JLabel("");
		gridbag.setConstraints(label, c);
		panel.add(label);

		setLayout(new BorderLayout());
		add(panel, BorderLayout.NORTH);
		add(sta, BorderLayout.CENTER);
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			if(command.equals("do"))
			{
				Double d1 = (Double)Utils.checkNum(displacement.getText(), "Newmark displacement field", null, false, null, new Double(0), true, null, false);
				if(d1 == null) return;

				result.setText(RigidBlockSimplified.ProbFailure(d1.doubleValue()));
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}
}
