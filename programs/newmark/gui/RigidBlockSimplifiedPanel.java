/*
 * RigidBlockSimplifiedPanel.java
 *
 * Copyright (C) 2002 Matthew Jibson (dolmant@dolmant.net)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/* $Id$ */

package newmark.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Vector;
import newmark.*;
import newmark.analysis.*;

class RigidBlockSimplifiedPanel extends JPanel implements ActionListener
{
	NewmarkTabbedPane parent;

	JRadioButton Jibson = new JRadioButton("Jibson and Others");
	JRadioButton Ambraseys = new JRadioButton("Ambraseys and Menu");
	JRadioButton probFailure = new JRadioButton("Probability of Failure");
	ButtonGroup group = new ButtonGroup();

	JLabel labelOne = new JLabel(" ");
	JLabel labelTwo = new JLabel(" ");
	JLabel labelRes = new JLabel(" ");
	JTextField labelOnef = new JTextField("", 15);
	JTextField labelTwof = new JTextField("", 15);
	JLabel labelResf = new JLabel(" ");
	JEditorPane ta = new JEditorPane();
	JButton button = new JButton("Perform Analysis");

	String JibsonStr = "This program estimates rigid-block Newmark displacement as a function of Arias shaking intensity and critical acceleration as explained in Jibson and Others (1998, 2000).  The estimate is made using the following regression equation:"
	+ "<p>log <i>D<sub>n</sub></i> = 1.521 log <i>I<sub>a</sub></i> - 1.993 log <i>a<sub>c</sub></i> - 1.546"
	+ "<p>where <i>D<sub>n</sub></i> is Newmark displacement in centimeters, <i>I<sub>a</sub></i> is Arias Intensity in meters per second, and <i>a<sub>c</sub></i> is critical acceleration in g's.  This equation was developed by conducting rigorous Newmark integrations on 555 single-component strong-motion records from 13 earthquakes for several discrete values of critical acceleration.  The regression model has an R<sup>2</sup> value of 83% and a model standard deviation of 0.375.</p>";

	String AmbraseysStr = "This program estimates rigid-block Newmark displacement as a function of the critical acceleration and peak ground acceleration using the following equation as explained in Ambraseys and Menu (1988):"
	+ "<p>log <i>D<sub>n</sub></i> = 0.90 + log[ (1 - a<sub><i>c</i></sub> / a<sub><i>max</i></sub>)<sup>2.53</sup> (a<sub><i>c</i></sub> / a<sub><i>max</i></sub>)<sup>-1.09</sup> ]"
	+ "<p>where <i>D<sub>n</sub></i> is Newmark displacement in centimeters, <i>a<sub>c</sub></i> is critical (yield) acceleration in g's, and <i>a<sub>max</sub></i> is the peak horizontal ground acceleration in g's.";

	String probFailureStr = "This program estimates probability of failure as a function of estimated Newmark displacement (specified in indicated field), as described by Jibson and others (1998, 2000).  The probability is estimated using the following equation:"
	+ "<p><i>P(f)</i> = 0.335(1 - exp(-0.048 <i>D<sub>n</sub></i><sup>1.565</sup>)"
	+ "<p>where <i>P(f)</i> is the probability of failure and <i>D<sub>n</sub></i> is Newmark displacement in centimeters. This equation was calibrated using data from the 1994 Northridge, California, earthquake and is best suited to application in southern California (Jibson and others, 1998, 2000).";

	public RigidBlockSimplifiedPanel(NewmarkTabbedPane parent) throws Exception
	{
		this.parent = parent;

		group.add(Jibson);
		group.add(Ambraseys);
		group.add(probFailure);

		Jibson.setMnemonic(KeyEvent.VK_J);
		Jibson.setActionCommand("change");
		Jibson.addActionListener(this);

		Ambraseys.setMnemonic(KeyEvent.VK_A);
		Ambraseys.setActionCommand("change");
		Ambraseys.addActionListener(this);

		probFailure.setMnemonic(KeyEvent.VK_F);
		probFailure.setActionCommand("change");
		probFailure.addActionListener(this);

		button.setMnemonic(KeyEvent.VK_P);
		button.setActionCommand("do");
		button.addActionListener(this);

		ta.setEditable(false);
		ta.setContentType("text/html");

		labelOnef.setBackground(GUIUtils.bg);
		labelTwof.setBackground(GUIUtils.bg);

		setLayout(new BorderLayout ());

		add(BorderLayout.NORTH, createSimplePanelTop());
		add(BorderLayout.CENTER, ta);
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			System.out.println(command);
			if(command.equals("change"))
			{
				labelOnef.setText("");
				labelTwof.setText("");
				labelResf.setText(" ");
				labelOnef.setBackground(Color.white);
				if(Jibson.isSelected())
				{
					labelOne.setText("What is the critical (yield) acceleration (in g's)?");
					labelTwo.setText("What is the Arias Intensity (in m/s)?");
					labelOnef.setEnabled(true);
					labelTwof.setEnabled(true);
					labelTwof.setBackground(Color.white);
					labelRes.setText("Estimated Newmark Displacement (in cm)");
					ta.setText(JibsonStr);
				}
				else if(Ambraseys.isSelected())
				{
					labelOne.setText("What is the critical (yield) acceleration (in g's)?");
					labelTwo.setText("What is the peak ground acceleration (in g's)?");
					labelOnef.setEnabled(true);
					labelTwof.setEnabled(true);
					labelTwof.setBackground(Color.white);
					labelRes.setText("Estimated Newmark Displacement (in cm)");
					ta.setText(AmbraseysStr);
				}
				else if(probFailure.isSelected())
				{
					labelOne.setText("What is the Newmark displacement (in cm)?");
					labelTwo.setText(" ");
					labelOnef.setEnabled(true);
					labelTwof.setEnabled(false);
					labelTwof.setBackground(GUIUtils.bg);
					labelRes.setText("Estimated probability of failure");
					ta.setText(probFailureStr);
				}
			}
			else if(command.equals("do"))
			{
				if(Jibson.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;
					Double d = (Double)Utils.checkNum(labelTwof.getText(), "Arias Intensity field", null, false, null, new Double(0), false, null, false);
					if(d == null) return;
					labelResf.setText(RigidBlockSimplified.JibsonAndOthers(d.doubleValue(), d1.doubleValue()));
				}
				else if(Ambraseys.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;
					Double d = (Double)Utils.checkNum(labelTwof.getText(), "peak ground acceleration field", null, false, null, new Double(0), false, null, false);
					if(d == null) return;
					labelResf.setText(RigidBlockSimplified.AmbraseysAndMenu(d.doubleValue(), d1.doubleValue()));
				}
				else if(probFailure.isSelected())
				{
					Double d = (Double)Utils.checkNum(labelOnef.getText(), "Newmark displacement field", null, false, null, new Double(0), true, null, false);
					if(d == null) return;
					labelResf.setText(RigidBlockSimplified.ProbFailure(d.doubleValue()));
				}
				else
				{
					GUIUtils.popupError("No function selected.");
				}

			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	private JPanel createSimplePanelTop()
	{
		JPanel panel = new JPanel(new BorderLayout());
		Box temp = new Box(BoxLayout.Y_AXIS);

		JPanel temp1 = new JPanel(new VariableGridLayout(VariableGridLayout.FIXED_NUM_COLUMNS, 3));
		temp1.add(Ambraseys);
		temp1.add(Jibson);
		temp1.add(probFailure);
		temp1.setAlignmentX(LEFT_ALIGNMENT);

		temp.add(temp1);
		temp.add(Box.createVerticalStrut(20));
		labelOne.setAlignmentX(LEFT_ALIGNMENT);
		temp.add(labelOne);

		Dimension d = labelOnef.getMinimumSize();
		labelOnef.setMinimumSize(d);
		labelOnef.setPreferredSize(d);
		labelOnef.setMaximumSize(d);
		labelOnef.setAlignmentX(LEFT_ALIGNMENT);
		temp.add(labelOnef);
		temp.add(Box.createVerticalStrut(20));

		labelTwo.setAlignmentX(LEFT_ALIGNMENT);
		temp.add(labelTwo);
		d = labelTwof.getMinimumSize();
		labelTwof.setMinimumSize(d);
		labelTwof.setPreferredSize(d);
		labelTwof.setMaximumSize(d);
		labelTwof.setAlignmentX(LEFT_ALIGNMENT);
		temp.add(labelTwof);
		temp.add(Box.createVerticalStrut(20));

		button.setAlignmentX(LEFT_ALIGNMENT);
		temp.add(button);
		temp.add(Box.createVerticalStrut(20));

		labelRes.setAlignmentX(LEFT_ALIGNMENT);
		labelResf.setAlignmentX(LEFT_ALIGNMENT);
		temp.add(labelRes);
		temp.add(labelResf);

		panel.add(BorderLayout.WEST, temp);

		return panel;
	}
}
