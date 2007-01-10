/*
 * Copyright (c) 2005 Matt Jibson
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

class WhichAnalysisPanel extends JPanel implements ActionListener
{
	SlammerTabbedPane parent;

	String zero = "0";

	JButton go = new JButton("Compute");
	JButton clear = new JButton("Clear fields");

	JLabel inputParameters = new JLabel("Input parameters:");

	JTextField h = new JTextField(7);
	JTextField vs = new JTextField(7);
	JTextField m = new JTextField(7);
	JTextField r = new JTextField(7);

	JTextField meanper = new JTextField(zero, 7);

	JLabel results = new JLabel("Results:");

	JLabel tstmLabel = new JLabel();
	JLabel rbLabel = new JLabel();
	JLabel cpLabel = new JLabel();
	JLabel dcLabel = new JLabel();

	Double hd, vsd, md, rd, meanperd;

	public static final int tstmIndex = 4;

	public WhichAnalysisPanel(SlammerTabbedPane parent) throws Exception
	{
		this.parent = parent;

		go.setActionCommand("go");
		go.addActionListener(this);

		clear.setActionCommand("clear");
		clear.addActionListener(this);

		inputParameters.setFont(GUIUtils.headerFont);
		results.setFont(GUIUtils.headerFont);

		createPanel();
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			System.out.println(command);
			if(command.equals("go"))
			{
				hd = (Double)Utils.checkNum(h.getText(), "Vertical Thickness field", null, false, null, new Double(0), true, null, false);
				if(hd == null) return;

				vsd = (Double)Utils.checkNum(vs.getText(), "Shear Wave Vel. field", null, false, null, new Double(0), true, null, false);
				if(vsd == null) return;

				md = (Double)Utils.checkNum(m.getText(), "Earthquake Magnitude field", null, false, null, new Double(0), true, null, false);
				if(md == null) return;

				rd = (Double)Utils.checkNum(r.getText(), "Earthquake Distance field", null, false, null, new Double(0), true, null, false);
				if(rd == null) return;

				meanperd = (Double)Utils.checkNum(meanper.getText(), "Mean Period Sigma field", null, false, null, new Double(0), true, null, false);
				if(meanperd == null) return;

				String[] res = DecoupledSimplified.BrayAndRathje(0, hd.doubleValue(), vsd.doubleValue(), md.doubleValue(), 0, rd.doubleValue(), 0, meanperd.doubleValue(), 0, 0, 0, false);

				// do other stuff

				tstmLabel.setText(res[tstmIndex]);
			}
			else if(command.equals("clear"))
			{
				h.setText("");
				vs.setText("");
				m.setText("");
				r.setText("");

				meanper.setText(zero);

				tstmLabel.setText("");
				rbLabel.setText("");
				cpLabel.setText("");
				dcLabel.setText("");
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	private void createPanel()
	{
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		JLabel label;
		Insets top = new Insets(10, 0, 0, 0);
		Insets none = new Insets(0, 0, 0, 0);

		setLayout(gridbag);

		int x = 0;
		int y = 0;

		c.anchor = GridBagConstraints.NORTHWEST;

		c.gridx = x++;
		c.gridy = y++;
		c.gridwidth = 2;
		gridbag.setConstraints(inputParameters, c);
		add(inputParameters);

		c.gridwidth = 1;
		c.gridy = y++;
		label = new JLabel("Vertical thickness, h (m): ");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x--;
		gridbag.setConstraints(h, c);
		add(h);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("Shear-wave velocity, Vs (m/s): ");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x--;
		gridbag.setConstraints(vs, c);
		add(vs);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("Earthquake magnitude, M: ");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x--;
		gridbag.setConstraints(m, c);
		add(m);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("Earthquake distance, r (km): ");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x--;
		gridbag.setConstraints(r, c);
		add(r);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("Mean Period (optional): ");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x--;
		gridbag.setConstraints(meanper, c);
		add(meanper);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = top;
		gridbag.setConstraints(go, c);
		add(go);

		c.gridx = x--;
		gridbag.setConstraints(clear, c);
		add(clear);

		c.gridx = x++;
		c.gridy = y++;
		gridbag.setConstraints(results, c);
		add(results);

		c.gridy = y++;
		c.insets = none;
		label = new JLabel("Ts/Tm:");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x--;
		gridbag.setConstraints(tstmLabel, c);
		add(tstmLabel);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("Rigid Block:");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x--;
		gridbag.setConstraints(rbLabel, c);
		add(rbLabel);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("Decoupled:");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x--;
		gridbag.setConstraints(dcLabel, c);
		add(dcLabel);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("Coupled:");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x--;
		gridbag.setConstraints(cpLabel, c);
		add(cpLabel);
	}
}
