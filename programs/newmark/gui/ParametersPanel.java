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
import newmark.analysis.*;

class ParametersPanel extends JPanel implements ActionListener
{
	NewmarkTabbedPane parent;

	ButtonGroup PGAgroup = new ButtonGroup();
	public JRadioButton scalePGAoff = new JRadioButton("Do not scale", true);
	public JRadioButton scalePGAon = new JRadioButton("Scale records to a uniform PGA (in g's) of");
	public JTextField scalePGAval = new JTextField("", 4);

	ButtonGroup SlopeGroup = new ButtonGroup();
	public JRadioButton downSlope = new JRadioButton("Downslope only", true);
	public JRadioButton dualSlope = new JRadioButton("Downslope and upslope at thrust angle of");
	public JTextField thrustAngle = new JTextField(4);

	ButtonGroup CAgroup = new ButtonGroup();
	public JRadioButton CAconst = new JRadioButton("Constant (g)", true);
	public JRadioButton CAdisp = new JRadioButton("Varies with displacement");

	public JTextField CAconstTF = new JTextField();

	public ParametersTableModel dispTableModel = new ParametersTableModel("Displacement (cm)", "Displacement");
	JTable dispTable = new JTable(dispTableModel);
	JButton dispAddRow = new JButton("Add Row");
	JButton dispDelRow = new JButton("Delete Last Row");
	JScrollPane dispPane;

	ButtonGroup unitGroup = new ButtonGroup();
	public JRadioButton unitEnglish = new JRadioButton("English");
	public JRadioButton unitMetric = new JRadioButton("Metric", true);

	public JTextField paramUwgt = new JTextField(7);
	public JTextField paramHeight = new JTextField(7);
	public JTextField paramVs = new JTextField(7);
	public JTextField paramDamp = new JTextField(7);
	public JComboBox paramBaseType = new JComboBox(new Object[] {"rigid rock", "elastic rock"});
	public JTextField paramVr = new JTextField(7);
	public JComboBox paramSoilModel = new JComboBox(new Object[] {"linear elastic", "equivalent linear"});

	JLabel labelUwgt = new JLabel();
	JLabel labelHeight = new JLabel();
	JLabel labelVs = new JLabel();
	JLabel labelDamp = new JLabel(stringDamp + " (%)");
	JLabel labelBaseType = new JLabel(stringBaseType);
	JLabel labelVr = new JLabel();
	JLabel labelSoilModel = new JLabel(stringSoilModel);

	public JCheckBox typeRigid = new JCheckBox(stringRB);
	public JCheckBox typeDecoupled = new JCheckBox(stringDC);
	public JCheckBox typeCoupled = new JCheckBox(stringCP);

	final public static String stringUwgt = "Unit weight";
	final public static String stringHeight = "Height";
	final public static String stringVs = "Shear wave velocity (soil)";
	final public static String stringDisp = "Displacement";
	final public static String stringDamp = "Damping ratio";
	final public static String stringBaseType = "Base type";
	final public static String stringVr = "Shear wave velocity (base rock)";
	final public static String stringSoilModel = "Soil model";

	final public static String stringRB = "Rigid Block";
	final public static String stringDC = "Decoupled";
	final public static String stringCP = "Coupled";

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

		paramBaseType.setActionCommand("paramBaseType");
		paramBaseType.addActionListener(this);
		paramVr.setEnabled(false);

		updateUnits();

		next.setActionCommand("next");
		next.addActionListener(this);

		createPanel();
	}

	private void createPanel()
	{
		GridBagLayout gridbag = new GridBagLayout();
		setLayout(gridbag);

		GridBagConstraints c = new GridBagConstraints();
		JLabel label;

		int y = 0;
		int x = 0;

		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.VERTICAL;

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Choose Analysis Properties:");
		label.setFont(GUIUtils.headerFont);
		c.gridwidth = 5;
		gridbag.setConstraints(label, c);
		add(label);
		c.gridwidth = 1;

		x = 0;
		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("     ");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x++;
		label = new JLabel("Units:");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x++;
		gridbag.setConstraints(unitMetric, c);
		add(unitMetric);

		c.gridx = x++;
		gridbag.setConstraints(unitEnglish, c);
		add(unitEnglish);

		x = 1;
		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Scaling:");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x++;
		gridbag.setConstraints(scalePGAoff, c);
		add(scalePGAoff);

		c.gridx = x++;
		gridbag.setConstraints(scalePGAon, c);
		add(scalePGAon);

		c.gridx = x++;
		gridbag.setConstraints(scalePGAval, c);
		add(scalePGAval);

		x = 1;
		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Critical Acceleration:");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x++;
		gridbag.setConstraints(CAconst, c);
		add(CAconst);

		c.gridx = x++;
		gridbag.setConstraints(CAdisp, c);
		add(CAdisp);

		x = 2;
		c.gridx = x++;
		c.gridy = y++;
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(CAconstTF, c);
		add(CAconstTF);

		c.gridx = x++;
		c.weighty = 1;
		c.weightx = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(dispPane, c);
		add(dispPane);

		JPanel jp = new JPanel();
		jp.setLayout(new GridLayout(1, 2));
		jp.add(dispAddRow);
		jp.add(dispDelRow);

		c.gridy = y++;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(jp, c);
		add(jp);

		x = 0;
		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Choose Types of Analysis:");
		label.setFont(GUIUtils.headerFont);
		label.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, Color.BLACK));
		c.gridwidth = 5;
		c.anchor = GridBagConstraints.NORTHWEST;
		gridbag.setConstraints(label, c);
		add(label);
		c.gridwidth = 1;

		c.gridx = x++;
		c.gridy = y++;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(typeRigid, c);
		add(typeRigid);

		jp = new JPanel();
		label = new JLabel("Slope Displacement:");
		jp.add(label);
		jp.add(downSlope);
		jp.add(dualSlope);
		jp.add(thrustAngle);

		c.gridx = x;
		c.gridwidth = 3;
		gridbag.setConstraints(jp, c);
		add(jp);
		c.gridwidth = 1;

		x = 1;
		c.gridx = x;
		c.gridy = y++;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		jp = new JPanel();
		jp.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
		gridbag.setConstraints(jp, c);
		add(jp);
		c.gridwidth = 1;

		c.gridy = y++;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(typeCoupled, c);
		add(typeCoupled);

		c.gridy = y--;
		gridbag.setConstraints(typeDecoupled, c);
		add(typeDecoupled);

		x = 2;
		c.gridy = y++;
		c.gridx = x++;
		gridbag.setConstraints(labelUwgt, c);
		add(labelUwgt, c);

		c.gridx = x--;
		gridbag.setConstraints(paramUwgt, c);
		add(paramUwgt, c);

		c.gridy = y++;
		c.gridx = x++;
		gridbag.setConstraints(labelHeight, c);
		add(labelHeight, c);

		c.gridx = x--;
		gridbag.setConstraints(paramHeight, c);
		add(paramHeight, c);

		c.gridy = y++;
		c.gridx = x++;
		gridbag.setConstraints(labelVs, c);
		add(labelVs, c);

		c.gridx = x--;
		gridbag.setConstraints(paramVs, c);
		add(paramVs, c);

		c.gridy = y++;
		c.gridx = x++;
		gridbag.setConstraints(labelDamp, c);
		add(labelDamp, c);

		c.gridx = x--;
		gridbag.setConstraints(paramDamp, c);
		add(paramDamp, c);

		c.gridy = y++;
		c.gridx = x++;
		gridbag.setConstraints(labelBaseType, c);
		add(labelBaseType, c);

		c.gridx = x--;
		gridbag.setConstraints(paramBaseType, c);
		add(paramBaseType, c);

		c.gridy = y++;
		c.gridx = x++;
		gridbag.setConstraints(labelVr, c);
		add(labelVr, c);

		c.gridx = x--;
		gridbag.setConstraints(paramVr, c);
		add(paramVr, c);

		c.gridy = y++;
		c.gridx = x++;
		gridbag.setConstraints(labelSoilModel, c);
		add(labelSoilModel, c);

		c.gridx = x--;
		gridbag.setConstraints(paramSoilModel, c);
		add(paramSoilModel, c);
	}

	private void updateUnits()
	{
		Double d;
		if(unitMetric.isSelected())
		{
			labelUwgt.setText("<html>" + stringUwgt + " (kN/m<sup>3</sup>)</html>");
			labelHeight.setText(stringHeight + " (m)");
			labelVs.setText(stringVs + " (m/s)");
			labelVr.setText(stringVr + " (m/s)");
			dispTableModel.setColName(stringDisp + " (cm)");

			try{d = new Double(paramUwgt.getText()); paramUwgt.setText(Analysis.fmtTwo.format(new Double(d.doubleValue() / Analysis.PCFtoKNM3)));} catch(Exception ex){}
			try{d = new Double(paramHeight.getText()); paramHeight.setText(Analysis.fmtTwo.format(new Double(d.doubleValue() * Analysis.FTtoM)));} catch(Exception ex){}
			try{d = new Double(paramVs.getText()); paramVs.setText(Analysis.fmtTwo.format(new Double(d.doubleValue() * Analysis.FTtoM)));} catch(Exception ex){}
			try{d = new Double(paramVr.getText()); paramVr.setText(Analysis.fmtTwo.format(new Double(d.doubleValue() * Analysis.FTtoM)));} catch(Exception ex){}
		}
		else if(unitEnglish.isSelected())
		{
			labelUwgt.setText("<html>" + stringUwgt + " (lb/ft<sup>3</sup>)</html>");
			labelHeight.setText(stringHeight + " (ft)");
			labelVs.setText(stringVs + " (ft/s)");
			labelVr.setText(stringVr + " (ft/s)");
			dispTableModel.setColName(stringDisp + " (in)");
			try{d = new Double(paramUwgt.getText()); paramUwgt.setText(Analysis.fmtTwo.format(new Double(d.doubleValue() * Analysis.PCFtoKNM3)));} catch(Exception ex){}
			try{d = new Double(paramHeight.getText()); paramHeight.setText(Analysis.fmtTwo.format(new Double(d.doubleValue() / Analysis.FTtoM)));} catch(Exception ex){}
			try{d = new Double(paramVs.getText()); paramVs.setText(Analysis.fmtTwo.format(new Double(d.doubleValue() / Analysis.FTtoM)));} catch(Exception ex){}
			try{d = new Double(paramVr.getText()); paramVr.setText(Analysis.fmtTwo.format(new Double(d.doubleValue() / Analysis.FTtoM)));} catch(Exception ex){}
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
			else if(command.equals("paramBaseType"))
			{
				paramVr.setEnabled(paramBaseType.getSelectedIndex() == 1);
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}
}
