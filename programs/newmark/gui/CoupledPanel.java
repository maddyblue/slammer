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

/* $Id: CoupledPanel.java,v 1.1 2003/07/31 21:32:53 dolmant Exp $ */

package newmark.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Vector;
import java.io.*;
import newmark.*;

class CoupledPanel extends JPanel implements ActionListener
{
	ParametersPanel parent;

	ButtonGroup unitGroup = new ButtonGroup();
	JRadioButton unitEnglish = new JRadioButton("English", true);
	JRadioButton unitMetric = new JRadioButton("Metric");

	JTextField paramUwgt = new JTextField(7);
	JTextField paramHeight = new JTextField(7);
	JTextField paramVs = new JTextField(7);
	JTextField paramDamp = new JTextField(7);

	JLabel labelUwgt = new JLabel();
	JLabel labelHeight = new JLabel();
	JLabel labelVs = new JLabel();
	JLabel labelDamp = new JLabel(stringDamp + " (percent)");

	final public static String stringUwgt = "Unit weight";
	final public static String stringHeight = "Height";
	final public static String stringVs = "Shear wave velocity";
	final public static String stringDamp = "Damping ratio";
	final public static String stringDisp = "Displacement";

	ParametersTableModel dispTableModel = new ParametersTableModel("", "Displacement");
	ParametersTable dispTable = new ParametersTable(dispTableModel);
	JButton dispAddRow = new JButton("Add Row");
	JButton dispDelRow = new JButton("Delete Last Row");
	JScrollPane dispPane;

	public CoupledPanel(ParametersPanel parent)
	{
		this.parent = parent;

		unitGroup.add(unitEnglish);
		unitGroup.add(unitMetric);

		unitEnglish.setActionCommand("unit");
		unitEnglish.addActionListener(this);
		unitEnglish.setMnemonic(KeyEvent.VK_E);

		unitMetric.setActionCommand("unit");
		unitMetric.addActionListener(this);
		unitMetric.setMnemonic(KeyEvent.VK_M);

		updateUnits();

		dispTable.setPreferredScrollableViewportSize(new Dimension(0,0));
		dispAddRow.setActionCommand("addDispRow");
		dispAddRow.addActionListener(this);
		dispDelRow.setActionCommand("delDispRow");
		dispDelRow.addActionListener(this);
		dispPane = new JScrollPane(dispTable);

		GridBagLayout gridbag = new GridBagLayout();
		setLayout(gridbag);

		GridBagConstraints c = new GridBagConstraints();
		JLabel label;

		int y = 0;

		c.anchor = GridBagConstraints.NORTHWEST;

		c.gridx = 0;
		c.gridy = y++;
		label = new JLabel("Units:");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridy = y++;
		gridbag.setConstraints(unitEnglish, c);
		add(unitEnglish);

		c.gridx = 1;
		gridbag.setConstraints(unitMetric, c);
		add(unitMetric);

		c.gridy = y++;
		c.gridx = 0;
		c.gridwidth = 2;
		gridbag.setConstraints(labelUwgt, c);
		add(labelUwgt);

		c.gridx = 2;
		gridbag.setConstraints(paramUwgt, c);
		add(paramUwgt);

		c.gridy = y++;
		c.gridx = 0;
		gridbag.setConstraints(labelHeight, c);
		add(labelHeight);

		c.gridx = 2;
		gridbag.setConstraints(paramHeight, c);
		add(paramHeight);

		c.gridy = y++;
		c.gridx = 0;
		gridbag.setConstraints(labelVs, c);
		add(labelVs);

		c.gridx = 2;
		gridbag.setConstraints(paramVs, c);
		add(paramVs);

		c.gridy = y++;
		c.gridx = 0;
		gridbag.setConstraints(labelDamp, c);
		add(labelDamp, c);

		c.gridx = 2;
		gridbag.setConstraints(paramDamp, c);
		add(paramDamp);

		c.gridy = 2;
		c.gridx = 4;
		c.gridheight = y - 2;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		gridbag.setConstraints(dispPane, c);
		add(dispPane);

		c.gridy = y;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0;
		JPanel dispButtonsPanel = new JPanel(new GridLayout(0, 2));
		dispButtonsPanel.add(dispAddRow);
		dispButtonsPanel.add(dispDelRow);
		gridbag.setConstraints(dispButtonsPanel, c);
		add(dispButtonsPanel);
	}

	private void updateUnits()
	{
		if(unitMetric.isSelected())
		{
			labelUwgt.setText(stringUwgt + " (kilo-Newtons per cubic-meter)");
			labelHeight.setText(stringHeight + " (meters)");
			labelVs.setText(stringVs + " (meters per second)");
			dispTableModel.setColName(stringDisp + " (cm)");
		}
		else if(unitEnglish.isSelected())
		{
			labelUwgt.setText(stringUwgt + " (pounds per cubic foot)");
			labelHeight.setText(stringHeight + " (feet)");
			labelVs.setText(stringVs + " (feet per second)");
			dispTableModel.setColName(stringDisp + " (inches)");
		}
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			System.out.println(command);
			if(command.equals("addDispRow"))
			{
				dispTableModel.addRow();
			}
			else if(command.equals("delDispRow"))
			{
				if(dispTableModel.getRowCount() > 1)
					dispTableModel.removeRow(dispTableModel.getRowCount() - 1);
			}
			else if(command.equals("unit"))
			{
				updateUnits();
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}
}
