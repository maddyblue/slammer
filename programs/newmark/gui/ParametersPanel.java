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

class ParametersPanel extends JPanel implements ActionListener
{
	NewmarkTabbedPane parent;

	ButtonGroup PGAgroup = new ButtonGroup();
	public JRadioButton scalePGAoff = new JRadioButton("Do not scale");
	public JRadioButton scalePGAon = new JRadioButton("Scale records to a uniform PGA (in g's) of");
	public JTextField scalePGAval = new JTextField("", 4);

	ButtonGroup SlopeGroup = new ButtonGroup();
	public JRadioButton downSlope = new JRadioButton("Downslope only", true);
	public JRadioButton dualSlope = new JRadioButton("Downslope and upslope at thrust angle of");
	public JTextField thrustAngle = new JTextField(4);

	ButtonGroup CAgroup = new ButtonGroup();
	public JRadioButton CAconst = new JRadioButton("Constant");
	public JRadioButton CAdisp = new JRadioButton("Varies with displacement");

	public JTextField CAconstTF = new JTextField();

	public ParametersTableModel dispTableModel = new ParametersTableModel("Displacement (cm)", "Displacement");
	ParametersTable dispTable = new ParametersTable(dispTableModel);
	JButton dispAddRow = new JButton("Add Row");
	JButton dispDelRow = new JButton("Delete Last Row");
	JScrollPane dispPane;

	ButtonGroup unitGroup = new ButtonGroup();
	public JRadioButton unitEnglish = new JRadioButton("English", true);
	public JRadioButton unitMetric = new JRadioButton("Metric");

	public JTextField paramUwgt = new JTextField(7);
	public JTextField paramHeight = new JTextField(7);
	public JTextField paramVs = new JTextField(7);
	public JTextField paramDamp = new JTextField(7);

	JLabel labelUwgt = new JLabel();
	JLabel labelHeight = new JLabel();
	JLabel labelVs = new JLabel();
	JLabel labelDamp = new JLabel("Damping ratio (percent)");

	final public static String stringUwgt = "Unit weight";
	final public static String stringHeight = "Height";
	final public static String stringVs = "Shear wave velocity";
	final public static String stringDisp = "Displacement";

	public JCheckBox typeRigid = new JCheckBox("Rigid Block");
	public JCheckBox typeDecoupled = new JCheckBox("Decoupled");
	public JCheckBox typeCoupled = new JCheckBox("Coupled");

	JButton next = new JButton("Perform Analysis");

	public ParametersPanel(NewmarkTabbedPane parent)
	{
		this.parent = parent;

		unitGroup.add(unitEnglish);
		unitGroup.add(unitMetric);

		unitEnglish.setActionCommand("unit");
		unitEnglish.addActionListener(this);

		unitMetric.setActionCommand("unit");
		unitMetric.addActionListener(this);

		PGAgroup.add(scalePGAon);
		scalePGAon.setActionCommand("scalePGA");
		scalePGAon.addActionListener(this);

		PGAgroup.add(scalePGAoff);
		scalePGAoff.setActionCommand("scalePGA");
		scalePGAoff.addActionListener(this);
		scalePGAoff.setSelected(true);

		scalePGAval.setEnabled(false);

		SlopeGroup.add(downSlope);
		downSlope.setActionCommand("slope");
		downSlope.addActionListener(this);

		SlopeGroup.add(dualSlope);
		dualSlope.setActionCommand("slope");
		dualSlope.addActionListener(this);

		thrustAngle.setEnabled(false);

		CAgroup.add(CAconst);
		CAconst.setActionCommand("ca");
		CAconst.addActionListener(this);

		CAgroup.add(CAdisp);
		CAdisp.setActionCommand("ca");
		CAdisp.addActionListener(this);

		dispTable.setPreferredScrollableViewportSize(new Dimension(0,0));
		dispAddRow.setActionCommand("addDispRow");
		dispAddRow.addActionListener(this);
		dispDelRow.setActionCommand("delDispRow");
		dispDelRow.addActionListener(this);
		dispPane = new JScrollPane(dispTable);

		dispTable.setEnabled(false);

		dispAddRow.setEnabled(false);
		dispDelRow.setEnabled(false);

		updateUnits();

		next.setActionCommand("next");
		next.addActionListener(this);

		setLayout(new BorderLayout());

		add(BorderLayout.CENTER, createCenterPanel());
		add(BorderLayout.SOUTH, createSouthPanel());
	}

	private JPanel createCenterPanel()
	{
		JPanel panel = new JPanel();

		GridBagLayout gridbag = new GridBagLayout();
		panel.setLayout(gridbag);

		GridBagConstraints c = new GridBagConstraints();
		JLabel label;

		int y = 0;
		int x = 0;

		c.anchor = GridBagConstraints.NORTHWEST;

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Units:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(unitEnglish, c);
		panel.add(unitEnglish);

		c.gridx = x++;
		gridbag.setConstraints(unitMetric, c);
		panel.add(unitMetric);

		x = 0;

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Scaling:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(scalePGAoff, c);
		panel.add(scalePGAoff);

		c.gridx = x++;
		gridbag.setConstraints(scalePGAon, c);
		panel.add(scalePGAon);

		c.gridx = x++;
		gridbag.setConstraints(scalePGAval, c);
		panel.add(scalePGAval);

		x = 0;

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Slope Displacement:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(downSlope, c);
		panel.add(downSlope);

		c.gridx = x++;
		gridbag.setConstraints(dualSlope, c);
		panel.add(dualSlope);

		c.gridx = x++;
		gridbag.setConstraints(thrustAngle, c);
		panel.add(thrustAngle, c);

		x = 0;

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Critical Acceleration:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(CAconst, c);
		panel.add(CAconst);

		c.gridx = x++;
		gridbag.setConstraints(CAdisp, c);
		panel.add(CAdisp);

		x = 1;

		c.gridx = x++;
		c.gridy = y++;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(CAconstTF, c);
		panel.add(CAconstTF);

		c.gridx = x++;
		c.weighty = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(dispPane, c);
		panel.add(dispPane);

		JPanel jp = new JPanel();
		jp.setLayout(new GridLayout(1, 2));
		jp.add(dispAddRow);
		jp.add(dispDelRow);

		c.gridy = y++;
		c.weightx = 0;
		c.weighty = 0;
		gridbag.setConstraints(jp, c);
		panel.add(jp);

		return panel;
	}

	private JPanel createSouthPanel()
	{
		JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));

		GridBagLayout gridbag = new GridBagLayout();
		panel.setLayout(gridbag);

		GridBagConstraints c = new GridBagConstraints();
		JLabel label;

		int y = 0;
		int x = 0;

		c.gridx = x++;
		c.gridy = y++;
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(typeRigid, c);
		panel.add(typeRigid);

		// i can't get borders to show around JCheckBoxes, so make a fake one
		label = new JLabel(" ");
		label.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.BLACK));
		c.gridx = x++;
		c.gridheight = 5;
		c.fill = GridBagConstraints.VERTICAL;
		gridbag.setConstraints(label, c);
		panel.add(label, c);
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;

		c.gridx = x++;
		gridbag.setConstraints(typeDecoupled, c);
		panel.add(typeDecoupled);

		c.gridx = x--;
		gridbag.setConstraints(typeCoupled, c);
		panel.add(typeCoupled);

		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		c.gridx = x++;
		c.gridy = y++;
		gridbag.setConstraints(labelUwgt, c);
		panel.add(labelUwgt);

		c.gridx = x--;
		gridbag.setConstraints(paramUwgt, c);
		panel.add(paramUwgt);

		c.gridy = y++;
		c.gridx = x++;
		gridbag.setConstraints(labelHeight, c);
		panel.add(labelHeight, c);

		c.gridx = x--;
		gridbag.setConstraints(paramHeight, c);
		panel.add(paramHeight, c);

		c.gridy = y++;
		c.gridx = x++;
		gridbag.setConstraints(labelVs, c);
		panel.add(labelVs, c);

		c.gridx = x--;
		gridbag.setConstraints(paramVs, c);
		panel.add(paramVs, c);

		c.gridy = y++;
		c.gridx = x++;
		gridbag.setConstraints(labelDamp, c);
		panel.add(labelDamp, c);

		c.gridx = x--;
		gridbag.setConstraints(paramDamp, c);
		panel.add(paramDamp, c);

		return panel;
	}

	private void updateUnits()
	{
		if(unitMetric.isSelected())
		{
			labelUwgt.setText("<html>" + stringUwgt + " (kilo-Newtons per m<sup>3</sup>)</html>");
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
			if(command.equals("next"))
			{
				parent.selectRigorousRigidBlock();
				parent.Results.Analyze.doClick();
			}
			else if(command.equals("scalePGA"))
			{
				scalePGAval.setEnabled(scalePGAon.isSelected());
			}
			else if(command.equals("slope"))
			{
				thrustAngle.setEnabled(dualSlope.isSelected());
			}
			else if(command.equals("addDispRow"))
			{
				dispTableModel.addRow();
			}
			else if(command.equals("delDispRow"))
			{
				if(dispTableModel.getRowCount() > 1)
					dispTableModel.removeRow(dispTableModel.getRowCount() - 1);
			}
			else if(command.equals("ca"))
			{
				boolean set = CAdisp.isSelected();
				CAconstTF.setEnabled(!set);
				dispTable.setEnabled(set);
				dispAddRow.setEnabled(set);
				dispDelRow.setEnabled(set);
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
