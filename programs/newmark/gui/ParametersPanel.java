/*
 * ParametersPanel.java - the panel to specify analysis parameters
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

/* $Id: ParametersPanel.java,v 1.4 2003/07/31 21:32:53 dolmant Exp $ */

package newmark.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Vector;
import java.io.*;
import newmark.*;

class ParametersPanel extends JPanel implements ActionListener
{
	NewmarkTabbedPane parent;

	RigidBlockPanel RigidBlock = new RigidBlockPanel(this);
	CoupledPanel Coupled = new CoupledPanel(this);

	ButtonGroup PGAgroup = new ButtonGroup();
	JRadioButton scalePGAoff = new JRadioButton("Do not scale earthquake records");
	JRadioButton scalePGAon = new JRadioButton("Scale all earthquake records to a uniform PGA (in g's) of");
	JTextField scalePGAval = new JTextField("", 7);
	JLabel scalePGAlabel = new JLabel("Scale to (in g's):");

	JTabbedPane tabbedPane = new JTabbedPane();

	JButton next = new JButton("Perform Analysis");

	public ParametersPanel(NewmarkTabbedPane parent)
	{
		this.parent = parent;

		PGAgroup.add(scalePGAon);
		scalePGAon.setMnemonic(KeyEvent.VK_S);
		scalePGAon.setActionCommand("scalePGA");
		scalePGAon.addActionListener(this);

		PGAgroup.add(scalePGAoff);
		scalePGAoff.setMnemonic(KeyEvent.VK_N);
		scalePGAoff.setActionCommand("scalePGA");
		scalePGAoff.addActionListener(this);
		scalePGAoff.setSelected(true);

		scalePGAval.setEnabled(false);

		next.setMnemonic(KeyEvent.VK_A);
		next.setActionCommand("next");
		next.addActionListener(this);

		tabbedPane.addTab("Rigid-Block", RigidBlock);
		tabbedPane.addTab("Coupled", Coupled);

		setLayout(new BorderLayout());

		add(BorderLayout.CENTER, tabbedPane);
		add(BorderLayout.SOUTH, createParamPanelFooter());
	}

	private JPanel createParamPanelFooter()
	{
		JPanel paramPanelFooter = new JPanel(new BorderLayout());

		Vector list = new Vector();
		list.add(new JLabel("Scaling:"));
		list.add(scalePGAoff);

		Vector line = new Vector();
		line.add(scalePGAon);
		line.add(scalePGAval);

		list.add(GUIUtils.makeRecursiveLayoutRight(line));

		JPanel nextPanel = new JPanel(new BorderLayout());
		nextPanel.add(BorderLayout.SOUTH, next);

		paramPanelFooter.add(BorderLayout.WEST, GUIUtils.makeRecursiveLayoutDown(list));
		paramPanelFooter.add(BorderLayout.EAST, nextPanel);

		return paramPanelFooter;
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			System.out.println(command);
			if(command.equals("next"))
			{
				parent.selectRigorousRigidBlock();
				parent.Results.Analyze.doClick();
			}
			else if(command.equals("scalePGA"))
			{
				if(scalePGAon.isSelected())
					scalePGAval.setEnabled(true);
				else
					scalePGAval.setEnabled(false);
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}
}
