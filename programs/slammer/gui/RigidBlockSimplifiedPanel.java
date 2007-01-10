/*
 * Copyright (c) 2002 Matt Jibson
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

/* $Id$ */

package slammer.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Vector;
import slammer.*;
import slammer.analysis.*;

class RigidBlockSimplifiedPanel extends JPanel implements ActionListener
{
	SlammerTabbedPane parent;

	JRadioButton Jibson1993 = new JRadioButton("Jibson (1993)");
	JRadioButton JibsonAndOthers1998 = new JRadioButton("Jibson and others (1998, 2000)");
	JRadioButton Jibson2007CA = new JRadioButton("Jibson (2007) Critical acceleration ratio");
	JRadioButton Jibson2007CAM = new JRadioButton("Jibson (2007) Critical acceleration ratio and magnitude");
	JRadioButton Jibson2007AICA = new JRadioButton("Jibson (2007) Arias intensity and critical acceleration");
	JRadioButton Jibson2007AICAR = new JRadioButton("Jibson (2007) Arias intensity and critical acceleration ratio");
	JRadioButton Ambraseys = new JRadioButton("Ambraseys and Menu (1998)");
	JRadioButton probFailure = new JRadioButton("Probability of Failure");
	ButtonGroup group = new ButtonGroup();

	JLabel labelOne = new JLabel(" ");
	JLabel labelTwo = new JLabel(" ");
	JLabel labelThree = new JLabel(" ");
	JLabel labelRes = new JLabel(" ");
	JTextField labelOnef = new JTextField(15);
	JTextField labelTwof = new JTextField(15);
	JTextField labelThreef = new JTextField(15);
	JLabel labelResf = new JLabel(" ");
	JEditorPane ta = new JEditorPane();
	JButton button = new JButton("Perform Analysis");

	String Jibson1993Str = "This program estimates rigid-block Slammer displacement as a function of Arias shaking intensity and critical acceleration as explained in Jibson (1993). The estimate is made using the following regression equation:"
	+ "<p>log <i>D<sub>n</sub></i> = 1.460 log <i>I<sub>a</sub></i> - 6.642 log <i>a<sub>c</sub></i> + 1.546"
	+ "<p>where <i>D<sub>n</sub></i> is Slammer displacement in centimeters, <i>I<sub>a</sub></i> is Arias Intensity in meters per second, and <i>a<sub>c</sub></i> is critical acceleration in g's. This equation was developed by conducting rigorous Slammer integrations on 11 single-component strong-motion records for several discrete values of critical acceleration. The regression model has an R<sup>2</sup> value of 87% and a model standard deviation of 0.409.</p>";

	String JibsonAndOthers1998Str = "This program estimates rigid-block Slammer displacement as a function of Arias shaking intensity and critical acceleration as explained in Jibson and others (1998, 2000). The estimate is made using the following regression equation:"
	+ "<p>log <i>D<sub>n</sub></i> = 1.521 log <i>I<sub>a</sub></i> - 1.993 log <i>a<sub>c</sub></i> - 1.546"
	+ "<p>where <i>D<sub>n</sub></i> is Slammer displacement in centimeters, <i>I<sub>a</sub></i> is Arias Intensity in meters per second, and <i>a<sub>c</sub></i> is critical acceleration in g's. This equation was developed by conducting rigorous Slammer integrations on 555 single-component strong-motion records from 13 earthquakes for several discrete values of critical acceleration. The regression model has an R<sup>2</sup> value of 87% and a model standard deviation of 0.375.</p>";

	String Jibson2007CAStr = "This program estimates rigid-block Slammer displacement as a function of critical acceleration ratio as explained in Jibson (2007). The estimate is made using the following regression equation:"
	+ "<p>log <i>D<sub>n</sub></i> = 0.215 + log [ ( 1 - <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) <sup>2.341</sup> ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) <sup>-1.438</sup> ]"
	+ "<p>where <i>D<sub>n</sub></i> is Slammer displacement in centimeters, <i>a<sub>c</sub></i> is critical acceleration in g's, and <i>a<sub>max</sub></i> is horizontal peak ground acceleration (PGA) in g's. This equation was developed by conducting rigorous Slammer integrations on 2270 single-component strong-motion records from 30 earthquakes for several discrete values of critical acceleration. The regression model has an R<sup>2</sup> value of 84% and a model standard deviation of 0.510.</p>";

	String Jibson2007CAMStr = "This program estimates rigid-block Slammer displacement as a function of critical acceleration ratio and magnitude as explained in Jibson (2007). The estimate is made using the following regression equation:"
	+ "<p>log <i>D<sub>n</sub></i> = -2.710 + log [ ( 1 - <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) <sup>2.335</sup> ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) <sup>-1.478</sup> ] + 0.424 <b>M</b>"
	+ "<p>where <i>D<sub>n</sub></i> is Slammer displacement in centimeters, <i>a<sub>c</sub></i> is critical acceleration in g's, <i>a<sub>max</sub></i> is horizontal peak ground acceleration (PGA) in g's, and <b>M</b> is moment magnitude. This equation was developed by conducting rigorous Slammer integrations on 2270 single-component strong-motion records from 30 earthquakes for several discrete values of critical acceleration. The regression model has an R<sup>2</sup> value of 87% and a model standard deviation of 0.454.</p>";

	String Jibson2007AICAStr = "This program estimates rigid-block Slammer displacement as a function of Arias intensity and critical acceleration as explained in Jibson (2007). The estimate is made using the following regression equation:"
	+ "<p>log <i>D<sub>n</sub></i> = 2.401 log <i>I<sub>a</sub></i> - 3.481 log <i>a<sub>c</sub></i> - 3.320"
	+ "<p>where <i>D<sub>n</sub></i> is Slammer displacement in centimeters, <i>I<sub>a</sub></i> is Arias Intensity in meters per second, and <i>a<sub>c</sub></i> is critical acceleration in g's. This equation was developed by conducting rigorous Slammer integrations on 875 single-component strong-motion records from 30 earthquakes for several discrete values of critical acceleration. The regression model has an R<sup>2</sup> value of 71% and a model standard deviation of 0.656.</p>";

	String Jibson2007AICARStr = "This program estimates rigid-block Slammer displacement as a function of Arias intensity and critical acceleration ratio as explained in Jibson (2007). The estimate is made using the following regression equation:"
	+ "<p>log <i>D<sub>n</sub></i> = 0.561 log <i>I<sub>a</sub></i> - 3.833 log ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) - 1.474"
	+ "<p>where <i>D<sub>n</sub></i> is Slammer displacement in centimeters, <i>I<sub>a</sub></i> is Arias Intensity in meters per second, <i>a<sub>c</sub></i> is critical acceleration in g's, and <i>a<sub>max</sub></i> is horizontal peak ground acceleration (PGA) in g's. This equation was developed by conducting rigorous Slammer integrations on 875 single-component strong-motion records from 30 earthquakes for several discrete values of critical acceleration. The regression model has an R<sup>2</sup> value of 75% and a model standard deviation of 0.616.</p>";

	String AmbraseysStr = "This program estimates rigid-block Slammer displacement as a function of the critical acceleration and peak ground acceleration using the following equation as explained in Ambraseys and Menu (1988):"
	+ "<p>log <i>D<sub>n</sub></i> = 0.90 + log[ (1 - a<sub><i>c</i></sub> / a<sub><i>max</i></sub>)<sup>2.53</sup> (a<sub><i>c</i></sub> / a<sub><i>max</i></sub>)<sup>-1.09</sup> ]"
	+ "<p>where <i>D<sub>n</sub></i> is Slammer displacement in centimeters, <i>a<sub>c</sub></i> is critical (yield) acceleration in g's, and <i>a<sub>max</sub></i> is the peak horizontal ground acceleration in g's.";

	String probFailureStr = "This program estimates probability of failure as a function of estimated Slammer displacement (specified in indicated field), as described by Jibson and others (1998, 2000). The probability is estimated using the following equation:"
	+ "<p><i>P(f)</i> = 0.335(1 - exp(-0.048 <i>D<sub>n</sub></i><sup>1.565</sup>)"
	+ "<p>where <i>P(f)</i> is the probability of failure and <i>D<sub>n</sub></i> is Slammer displacement in centimeters. This equation was calibrated using data from the 1994 Northridge, California, earthquake and is best suited to application in southern California (Jibson and others, 1998, 2000).";

	public RigidBlockSimplifiedPanel(SlammerTabbedPane parent) throws Exception
	{
		this.parent = parent;

		group.add(Jibson1993);
		group.add(JibsonAndOthers1998);
		group.add(Jibson2007CA);
		group.add(Jibson2007CAM);
		group.add(Jibson2007AICA);
		group.add(Jibson2007AICAR);
		group.add(Ambraseys);
		group.add(probFailure);

		Jibson1993.setActionCommand("change");
		Jibson1993.addActionListener(this);
		JibsonAndOthers1998.setActionCommand("change");
		JibsonAndOthers1998.addActionListener(this);
		Jibson2007CA.setActionCommand("change");
		Jibson2007CA.addActionListener(this);
		Jibson2007CAM.setActionCommand("change");
		Jibson2007CAM.addActionListener(this);
		Jibson2007AICA.setActionCommand("change");
		Jibson2007AICA.addActionListener(this);
		Jibson2007AICAR.setActionCommand("change");
		Jibson2007AICAR.addActionListener(this);
		Ambraseys.setActionCommand("change");
		Ambraseys.addActionListener(this);
		probFailure.setActionCommand("change");
		probFailure.addActionListener(this);

		labelOnef.setEnabled(false);
		labelTwof.setEnabled(false);
		labelThreef.setEnabled(false);

		button.setActionCommand("do");
		button.addActionListener(this);

		ta.setEditable(false);
		ta.setContentType("text/html");

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);

		Insets top = new Insets(10, 0, 0, 0);
		Insets none = new Insets(0, 0, 0, 0);

		Border b = BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(0, 0, 0, 5),
			BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK)
		);

		int x = 0;
		int y = 0;

		JPanel panel = new JPanel(new GridLayout(0, 1));

		panel.add(Jibson1993);
		panel.add(JibsonAndOthers1998);
		panel.add(Jibson2007CA);
		panel.add(Jibson2007CAM);
		panel.add(Jibson2007AICA);
		panel.add(Jibson2007AICAR);
		panel.add(Ambraseys);
		panel.add(probFailure);

		c.gridx = x++;
		c.gridy = y;
		c.gridheight = 9;
		c.anchor = GridBagConstraints.NORTHWEST;
		gridbag.setConstraints(panel, c);
		add(panel);

		c.gridx = x++;
		c.fill = GridBagConstraints.BOTH;
		JLabel label = new JLabel(" ");
		label.setBorder(b);
		gridbag.setConstraints(label, c);
		add(label);

		c.gridheight = 1;
		c.insets = none;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = x++;
		c.gridy = y++;
		gridbag.setConstraints(labelOne, c);
		add(labelOne);

		c.gridy = y++;
		gridbag.setConstraints(labelOnef, c);
		add(labelOnef);

		c.insets = top;
		c.gridy = y++;
		gridbag.setConstraints(labelTwo, c);
		add(labelTwo);

		c.insets = none;
		c.gridy = y++;
		gridbag.setConstraints(labelTwof, c);
		add(labelTwof);

		c.insets = top;
		c.gridy = y++;
		gridbag.setConstraints(labelThree, c);
		add(labelThree);

		c.insets = none;
		c.gridy = y++;
		gridbag.setConstraints(labelThreef, c);
		add(labelThreef);

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

		//*
		c.gridx = 0;
		c.gridy = y;
		c.insets = none;
		c.gridwidth = 3;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(ta, c);
		add(ta);
		//*/
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			System.out.println(command);
			if(command.equals("change"))
			{
				labelOne.setText("");
				labelTwo.setText("");
				labelThree.setText("");
				labelOnef.setText("");
				labelTwof.setText("");
				labelThreef.setText("");
				labelResf.setText(" ");

				labelOnef.setEnabled(false);
				labelTwof.setEnabled(false);
				labelThreef.setEnabled(false);

				if(Jibson1993.isSelected())
				{
					labelOne.setText("What is the critical (yield) acceleration (in g's)?");
					labelTwo.setText("What is the Arias Intensity (in m/s)?");
					labelOnef.setEnabled(true);
					labelTwof.setEnabled(true);
					labelRes.setText("Estimated Slammer Displacement (in cm):");
					ta.setText(Jibson1993Str);
				}
				else if(JibsonAndOthers1998.isSelected())
				{
					labelOne.setText("What is the critical (yield) acceleration (in g's)?");
					labelTwo.setText("What is the Arias Intensity (in m/s)?");
					labelOnef.setEnabled(true);
					labelTwof.setEnabled(true);
					labelRes.setText("Estimated Slammer Displacement (in cm):");
					ta.setText(JibsonAndOthers1998Str);
				}
				else if(Jibson2007CA.isSelected())
				{
					labelOne.setText("What is the critical (yield) acceleration (in g's)?");
					labelTwo.setText("What is the peak ground acceleration (in g's)?");
					labelOnef.setEnabled(true);
					labelTwof.setEnabled(true);
					labelRes.setText("Estimated Slammer Displacement (in cm):");
					ta.setText(Jibson2007CAStr);
				}
				else if(Jibson2007CAM.isSelected())
				{
					labelOne.setText("What is the critical (yield) acceleration (in g's)?");
					labelTwo.setText("What is the peak ground acceleration (in g's)?");
					labelThree.setText("What is the magnitude?");
					labelOnef.setEnabled(true);
					labelTwof.setEnabled(true);
					labelThreef.setEnabled(true);
					labelRes.setText("Estimated Slammer Displacement (in cm):");
					ta.setText(Jibson2007CAMStr);
				}
				else if(Jibson2007AICA.isSelected())
				{
					labelOne.setText("What is the critical (yield) acceleration (in g's)?");
					labelTwo.setText("What is the Arias Intensity (in m/s)?");
					labelOnef.setEnabled(true);
					labelTwof.setEnabled(true);
					labelRes.setText("Estimated Slammer Displacement (in cm):");
					ta.setText(Jibson2007AICAStr);
				}
				else if(Jibson2007AICAR.isSelected())
				{
					labelOne.setText("What is the critical (yield) acceleration (in g's)?");
					labelTwo.setText("What is the peak ground acceleration (in g's)?");
					labelThree.setText("What is the Arias Intensity (in m/s)?");
					labelOnef.setEnabled(true);
					labelTwof.setEnabled(true);
					labelThreef.setEnabled(true);
					labelRes.setText("Estimated Slammer Displacement (in cm):");
					ta.setText(Jibson2007AICARStr);
				}
				else if(Ambraseys.isSelected())
				{
					labelOne.setText("What is the critical (yield) acceleration (in g's)?");
					labelTwo.setText("What is the peak ground acceleration (in g's)?");
					labelOnef.setEnabled(true);
					labelTwof.setEnabled(true);
					labelRes.setText("Estimated Slammer Displacement (in cm):");
					ta.setText(AmbraseysStr);
				}
				else if(probFailure.isSelected())
				{
					labelOne.setText("What is the Slammer displacement (in cm)?");
					labelTwo.setText(" ");
					labelOnef.setEnabled(true);
					labelRes.setText("Estimated probability of failure:");
					ta.setText(probFailureStr);
				}
			}
			else if(command.equals("do"))
			{
				if(Jibson1993.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					Double d2 = (Double)Utils.checkNum(labelTwof.getText(), "Arias Intensity field", null, false, null, new Double(0), false, null, false);
					if(d2 == null) return;

					labelResf.setText(RigidBlockSimplified.Jibson1993(d2.doubleValue(), d1.doubleValue()));
				}
				else if(JibsonAndOthers1998.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					Double d2 = (Double)Utils.checkNum(labelTwof.getText(), "Arias Intensity field", null, false, null, new Double(0), false, null, false);
					if(d2 == null) return;

					labelResf.setText(RigidBlockSimplified.JibsonAndOthers1998(d2.doubleValue(), d1.doubleValue()));
				}
				else if(Jibson2007CA.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					Double d2 = (Double)Utils.checkNum(labelTwof.getText(), "peak ground acceleration field", null, false, null, new Double(0), false, null, false);
					if(d2 == null) return;

					labelResf.setText(RigidBlockSimplified.Jibson2007CA(d1.doubleValue(), d2.doubleValue()));
				}
				else if(Jibson2007CAM.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					Double d2 = (Double)Utils.checkNum(labelTwof.getText(), "peak ground acceleration field", null, false, null, new Double(0), false, null, false);
					if(d2 == null) return;

					Double d3 = (Double)Utils.checkNum(labelThreef.getText(), "magnitude field", null, false, null, new Double(0), false, null, false);
					if(d3 == null) return;

					labelResf.setText(RigidBlockSimplified.Jibson2007CAM(d1.doubleValue(), d2.doubleValue(), d3.doubleValue()));
				}
				else if(Jibson2007AICA.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					Double d2 = (Double)Utils.checkNum(labelTwof.getText(), "Arias intensity field", null, false, null, new Double(0), false, null, false);
					if(d2 == null) return;

					labelResf.setText(RigidBlockSimplified.Jibson2007AICA(d2.doubleValue(), d1.doubleValue()));
				}
				else if(Jibson2007AICAR.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					Double d2 = (Double)Utils.checkNum(labelOnef.getText(), "peak ground acceleration field", null, false, null, new Double(0), true, null, false);
					if(d2 == null) return;

					Double d3 = (Double)Utils.checkNum(labelThreef.getText(), "Arias intensity field", null, false, null, new Double(0), false, null, false);
					if(d3 == null) return;

					labelResf.setText(RigidBlockSimplified.Jibson2007AICAR(d3.doubleValue(), d1.doubleValue(), d2.doubleValue()));
				}
				else if(Ambraseys.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					Double d2 = (Double)Utils.checkNum(labelTwof.getText(), "peak ground acceleration field", null, false, null, new Double(0), false, null, false);
					if(d2 == null) return;

					labelResf.setText(RigidBlockSimplified.AmbraseysAndMenu(d2.doubleValue(), d1.doubleValue()));
				}
				else if(probFailure.isSelected())
				{
					Double d1 = (Double)Utils.checkNum(labelOnef.getText(), "Slammer displacement field", null, false, null, new Double(0), true, null, false);
					if(d1 == null) return;

					labelResf.setText(RigidBlockSimplified.ProbFailure(d1.doubleValue()));
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
}
