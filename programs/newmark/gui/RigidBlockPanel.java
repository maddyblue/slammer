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

/* $Id$ */

package newmark.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Vector;
import java.io.*;
import newmark.*;

class RigidBlockPanel extends JPanel implements ActionListener
{
	ParametersPanel parent;

	ButtonGroup SlopeGroup = new ButtonGroup();
	JRadioButton downSlope = new JRadioButton("Downslope displacement only", true);
	JRadioButton dualSlope = new JRadioButton("Downslope and upslope displacement");
	JTextField thrustAngle = new JTextField(7);

	ButtonGroup CAgroup = new ButtonGroup();
	JRadioButton nd = new JRadioButton("Constant critical acceleration");
	JRadioButton ndDisp = new JRadioButton("Varies with displacement");

	JTextField constCA = new JTextField();

	ParametersTableModel dispTableModel = new ParametersTableModel("Displacement (cm)", "Displacement");
	ParametersTable dispTable = new ParametersTable(dispTableModel);
	JButton dispAddRow = new JButton("Add Row");
	JButton dispDelRow = new JButton("Delete Last Row");
	JScrollPane dispPane;

	public RigidBlockPanel(ParametersPanel parent)
	{
		this.parent = parent;

		dispTable.setPreferredScrollableViewportSize(new Dimension(0,0));
		dispAddRow.setActionCommand("addDispRow");
		dispAddRow.addActionListener(this);
		dispDelRow.setActionCommand("delDispRow");
		dispDelRow.addActionListener(this);
		dispPane = new JScrollPane(dispTable);

		SlopeGroup.add(downSlope);
		downSlope.setMnemonic(KeyEvent.VK_D);
		downSlope.setActionCommand("change");
		downSlope.addActionListener(this);

		SlopeGroup.add(dualSlope);
		dualSlope.setMnemonic(KeyEvent.VK_U);
		dualSlope.setActionCommand("change");
		dualSlope.addActionListener(this);

		thrustAngle.setEnabled(false);
		thrustAngle.setBackground(GUIUtils.bg);

		CAgroup.add(nd);
		nd.setMnemonic(KeyEvent.VK_C);
		nd.setActionCommand("change");
		nd.addActionListener(this);

		CAgroup.add(ndDisp);
		ndDisp.setMnemonic(KeyEvent.VK_D);
		ndDisp.setActionCommand("change");
		ndDisp.addActionListener(this);

		dispTable.setEnabled(false);

		dispAddRow.setEnabled(false);
		dispDelRow.setEnabled(false);

		setLayout(new BorderLayout());

		add(BorderLayout.NORTH, createParamPanelNorth());
		add(BorderLayout.CENTER, createParamPanelTables());
	}

	private JPanel createParamPanelNorth()
	{
		JLabel label = new JLabel("Specify the critical (yield) acceleration of the landslide (in g's):");
		label.setFont(GUIUtils.headerFont);
		label.setBorder(new EmptyBorder(0,0,10,0));
		JPanel ret = new JPanel(new BorderLayout());
		ret.add(BorderLayout.WEST, label);

		Vector v = new Vector();
		v.add(dualSlope);
		v.add(Box.createHorizontalStrut(20));
		v.add(new JLabel("Thrust angle (in degrees): "));
		v.add(thrustAngle);

		JPanel temp = GUIUtils.makeRecursiveLayoutRight(v);

		v = new Vector();
		v.add(downSlope);
		v.add(temp);
		ret.add(BorderLayout.SOUTH, GUIUtils.makeRecursiveLayoutDown(v));

		ret.setBorder(new EmptyBorder(0,0,10,0));

		return ret;
	}

	private JPanel createParamPanelTables()
	{
		JPanel paramPanelTables = new JPanel(new BorderLayout());

		Box leftTable = new Box(BoxLayout.Y_AXIS);
		leftTable.add(nd);
		leftTable.add(constCA);
		JPanel westTable = new JPanel(new BorderLayout());
		westTable.add(BorderLayout.NORTH, leftTable);

		JPanel centerTable = new JPanel(new BorderLayout());
		centerTable.add(BorderLayout.NORTH, ndDisp);
		centerTable.add(BorderLayout.CENTER, dispPane);
		JPanel centerTableButtons = new JPanel(new GridLayout(0, 2));
		centerTableButtons.add(dispAddRow);
		centerTableButtons.add(dispDelRow);
		centerTable.add(BorderLayout.SOUTH, centerTableButtons);

		paramPanelTables.add(BorderLayout.WEST, westTable);
		paramPanelTables.add(BorderLayout.CENTER, centerTable);

		return paramPanelTables;
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
			else if(command.equals("change"))
			{
				constCA.setEnabled(false);
				dispTable.setEnabled(false);
				dispAddRow.setEnabled(false);
				dispDelRow.setEnabled(false);
				thrustAngle.setEnabled(false);
				ndDisp.setEnabled(true);

				thrustAngle.setBackground(GUIUtils.bg);

				if(downSlope.isSelected())
				{
					if(nd.isSelected())
					{
						constCA.setEnabled(true);
					}
					else if(ndDisp.isSelected())
					{
						dispTable.setEnabled(true);
						dispAddRow.setEnabled(true);
						dispDelRow.setEnabled(true);
					}
				}
				else if(dualSlope.isSelected())
				{
					constCA.setEnabled(true);
					thrustAngle.setEnabled(true);
					thrustAngle.setBackground(Color.white);
					nd.setSelected(true);
					ndDisp.setEnabled(false);
				}
			}
			else if(command.equals("delDispRow"))
			{
				if(dispTableModel.getRowCount() > 1)
					dispTableModel.removeRow(dispTableModel.getRowCount() - 1);
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}
}
