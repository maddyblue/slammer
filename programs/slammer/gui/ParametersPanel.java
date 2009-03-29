/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.io.*;
import slammer.*;
import slammer.analysis.*;

class ParametersPanel extends JPanel implements ActionListener
{
	SlammerTabbedPane parent;

	ButtonGroup scaleGroup = new ButtonGroup();
	public JRadioButton scaleOff = new JRadioButton("Do not scale", true);
	public JRadioButton scalePGA = new JRadioButton("Scale records to a uniform PGA (in g's) of");
	public JRadioButton scaleOn = new JRadioButton("Scale records by a factor of");
	public JTextField scalePGAval = new JTextField("", 4);
	public JTextField scaleData = new JTextField("1.0", 5);

	ButtonGroup SlopeGroup = new ButtonGroup();
	public JRadioButton downSlope = new JRadioButton("Downslope only", true);
	public JRadioButton dualSlope = new JRadioButton("Downslope and upslope at thrust angle of");
	public JTextField thrustAngle = new JTextField(4);

	ButtonGroup CAgroup = new ButtonGroup();
	public JRadioButton CAconst = new JRadioButton("Constant (g)", true);
	public JRadioButton CAdisp = new JRadioButton("Varies with displacement");

	public JTextField CAconstTF = new JTextField(5);

	public ParametersTableModel dispTableModel = new ParametersTableModel("Displacement (cm)", "Displacement");
	JTable dispTable = new JTable(dispTableModel);
	JButton dispAddRow = new JButton("Add Row");
	JButton dispDelRow = new JButton("Delete Last Row");
	JScrollPane dispPane;

	ButtonGroup unitGroup = new ButtonGroup();
	public JRadioButton unitEnglish = new JRadioButton("English");
	public JRadioButton unitMetric = new JRadioButton("Metric", true);

	public JTextField paramHeight = new JTextField(7);
	public JTextField paramVs = new JTextField(7);
	public JTextField paramDamp = new JTextField(7);
	public JTextField paramVr = new JTextField(7);
	public JComboBox paramSoilModel = new JComboBox(new Object[] {"linear elastic", "equivalent linear"});

	JLabel labelHeight = new JLabel();
	JLabel labelVs = new JLabel();
	JLabel labelDamp = new JLabel(stringDamp + " (%)");
	JLabel labelVr = new JLabel();
	JLabel labelSoilModel = new JLabel(stringSoilModel);

	public JCheckBox typeRigid = new JCheckBox(stringRB);
	public JCheckBox typeDecoupled = new JCheckBox(stringDC);
	public JCheckBox typeCoupled = new JCheckBox(stringCP);

	final public static String stringHeight = "Height";
	final public static String stringVs = "Shear-wave velocity (material above slip surface)";
	final public static String stringDisp = "Displacement";
	final public static String stringDamp = "Damping ratio";
	final public static String stringVr = "Shear-wave velocity (material below slip surface)";
	final public static String stringSoilModel = "Soil model";

	final public static String stringRB = "Rigid Block";
	final public static String stringDC = "Decoupled";
	final public static String stringCP = "Coupled";

	JButton next = new JButton("Go to Step 3: Perform Analyses and View Results");

	public ParametersPanel(SlammerTabbedPane parent)
	{
		this.parent = parent;

		unitGroup.add(unitEnglish);
		unitGroup.add(unitMetric);

		unitEnglish.setActionCommand("unit");
		unitEnglish.addActionListener(this);

		unitMetric.setActionCommand("unit");
		unitMetric.addActionListener(this);

		scaleGroup.add(scalePGA);
		scalePGA.setActionCommand("scale");
		scalePGA.addActionListener(this);

		scaleGroup.add(scaleOff);
		scaleOff.setActionCommand("scale");
		scaleOff.addActionListener(this);
		scaleOff.setSelected(true);

		scaleGroup.add(scaleOn);
		scaleOn.setActionCommand("scale");
		scaleOn.addActionListener(this);

		scalePGAval.setEnabled(false);
		scaleData.setEnabled(false);

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

		typeRigid.setActionCommand("Rigid");
		typeRigid.addActionListener(this);
		updateRigid();

		typeDecoupled.setActionCommand("DeCoupled");
		typeDecoupled.addActionListener(this);
		typeCoupled.setActionCommand("DeCoupled");
		typeCoupled.addActionListener(this);
		updateDeCoupled();

		updateUnits();

		next.setActionCommand("next");
		next.addActionListener(this);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);
		JPanel jp;

		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;

		jp = createPanelNorth();
		gridbag.setConstraints(jp, c);
		add(jp);

		c.gridy = 1;
		c.weighty = 0;
		jp = createPanelSouth();
		gridbag.setConstraints(jp, c);
		add(jp);

		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(next, c);
		add(next);
	}

	private JPanel createPanelNorth()
	{
		JPanel panel = new JPanel();
		JPanel jp;
		GridBagLayout gridbag = new GridBagLayout();
		panel.setLayout(gridbag);

		Insets left = new Insets(0, 10, 0, 0);
		Insets none = new Insets(0, 0, 0, 0);

		GridBagConstraints c = new GridBagConstraints();
		JLabel label;

		int y = 0;
		int x = 0;

		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.VERTICAL;

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Choose Analysis Properties:");
		label.setFont(GUIUtils.headerFont);
		c.gridwidth = 5;
		gridbag.setConstraints(label, c);
		panel.add(label);
		c.gridwidth = 1;

		x = 0;
		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("     ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		label = new JLabel("Units:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(unitMetric, c);
		panel.add(unitMetric);

		c.gridx = x++;
		gridbag.setConstraints(unitEnglish, c);
		panel.add(unitEnglish);

		x = 1;
		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Scaling:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		c.gridwidth = 2;
		jp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		jp.add(scaleOff);
		gridbag.setConstraints(jp, c);
		panel.add(jp);

		c.gridy = y++;
		jp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		jp.add(scalePGA);
		jp.add(scalePGAval);
		gridbag.setConstraints(jp, c);
		panel.add(jp);

		c.gridy = y++;
		jp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		jp.add(scaleOn);
		jp.add(scaleData);
		gridbag.setConstraints(jp, c);
		panel.add(jp);

		x = 1;
		c.gridwidth = 1;
		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Critical Acceleration:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(CAconst, c);
		panel.add(CAconst);

		c.gridx = x++;
		c.insets = left;
		gridbag.setConstraints(CAdisp, c);
		panel.add(CAdisp);

		x = 2;
		c.insets = none;
		c.gridx = x++;
		c.gridy = y++;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(CAconstTF, c);
		panel.add(CAconstTF);

		c.gridx = x++;
		c.insets = left;
		c.weighty = 1;
		c.weightx = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(dispPane, c);
		panel.add(dispPane);

		jp = new JPanel();
		jp.setLayout(new GridLayout(1, 2));
		jp.add(dispAddRow);
		jp.add(dispDelRow);

		c.gridy = y++;
		c.weighty = 0;
		gridbag.setConstraints(jp, c);
		panel.add(jp);
		c.insets = none;

		// fake container to claim the free space forcing everything left
		c.gridx = x + 1;
		c.weightx = 1;
		label = new JLabel("");
		gridbag.setConstraints(label, c);
		panel.add(label);

		return panel;
	}

	private JPanel createPanelSouth()
	{
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, Color.BLACK));
		GridBagLayout gridbag = new GridBagLayout();
		panel.setLayout(gridbag);

		GridBagConstraints c = new GridBagConstraints();
		JLabel label;
		JPanel jp;
		Insets left = new Insets(0, 5, 0, 0);
		Insets none = new Insets(0, 0, 0, 0);

		int y = 0;
		int x = 0;

		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;

		c.gridx = x++;
		c.gridy = y++;
		c.weightx = 1;
		label = new JLabel("Choose Types of Analysis:");
		label.setFont(GUIUtils.headerFont);
		c.gridwidth = 4;
		c.anchor = GridBagConstraints.NORTHWEST;
		gridbag.setConstraints(label, c);
		panel.add(label);
		c.gridwidth = 1;

		c.gridy = y++;
		c.weightx = 0;
		label = new JLabel("     ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.VERTICAL;
		typeRigid.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK), BorderFactory.createEmptyBorder(0, 0, 0, 5)));
		typeRigid.setBorderPainted(true);
		gridbag.setConstraints(typeRigid, c);
		panel.add(typeRigid);

		c.gridx = x--;
		c.gridwidth = 2;
		jp = new JPanel();
		label = new JLabel("Slope Displacement:");
		jp.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
		jp.add(label);
		jp.add(downSlope);
		jp.add(dualSlope);
		jp.add(thrustAngle);
		gridbag.setConstraints(jp, c);
		panel.add(jp);

		JPanel dcpanel = new JPanel(new GridLayout(0, 1));
		dcpanel.add(typeCoupled);
		dcpanel.add(typeDecoupled);
		typeCoupled.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK), BorderFactory.createEmptyBorder(0, 0, 0, 5)));
		typeDecoupled.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK), BorderFactory.createEmptyBorder(0, 0, 0, 5)));
		typeCoupled.setBorderPainted(true);
		typeDecoupled.setBorderPainted(true);

		c.gridx = x++;
		c.gridy = y++;
		c.gridwidth = 1;
		c.gridheight = 5;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(dcpanel, c);
		panel.add(dcpanel);

		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;

		c.gridx = x++;
		c.insets = left;
		gridbag.setConstraints(labelHeight, c);
		panel.add(labelHeight, c);

		c.gridx = x--;
		c.insets = none;
		gridbag.setConstraints(paramHeight, c);
		panel.add(paramHeight, c);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = left;
		gridbag.setConstraints(labelVs, c);
		panel.add(labelVs, c);

		c.gridx = x--;
		c.insets = none;
		gridbag.setConstraints(paramVs, c);
		panel.add(paramVs, c);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = left;
		gridbag.setConstraints(labelVr, c);
		panel.add(labelVr, c);

		c.gridx = x--;
		c.insets = none;
		gridbag.setConstraints(paramVr, c);
		panel.add(paramVr, c);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = left;
		gridbag.setConstraints(labelDamp, c);
		panel.add(labelDamp, c);

		c.gridx = x--;
		c.insets = none;
		gridbag.setConstraints(paramDamp, c);
		panel.add(paramDamp, c);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = left;
		gridbag.setConstraints(labelSoilModel, c);
		panel.add(labelSoilModel, c);

		c.gridx = x--;
		c.insets = none;
		gridbag.setConstraints(paramSoilModel, c);
		panel.add(paramSoilModel, c);

		return panel;
	}

	private void updateUnits()
	{
		Double d;
		if(unitMetric.isSelected())
		{
			labelHeight.setText(stringHeight + " (m)");
			labelVs.setText(stringVs + " (m/s)");
			labelVr.setText(stringVr + " (m/s)");
			dispTableModel.setColName(stringDisp + " (cm)");

			try{d = new Double(paramHeight.getText()); paramHeight.setText(Analysis.fmtTwo.format(new Double(d.doubleValue() * Analysis.FTtoM)));} catch(Exception ex){}
			try{d = new Double(paramVs.getText()); paramVs.setText(Analysis.fmtTwo.format(new Double(d.doubleValue() * Analysis.FTtoM)));} catch(Exception ex){}
			try{d = new Double(paramVr.getText()); paramVr.setText(Analysis.fmtTwo.format(new Double(d.doubleValue() * Analysis.FTtoM)));} catch(Exception ex){}
		}
		else if(unitEnglish.isSelected())
		{
			labelHeight.setText(stringHeight + " (ft)");
			labelVs.setText(stringVs + " (ft/s)");
			labelVr.setText(stringVr + " (ft/s)");
			dispTableModel.setColName(stringDisp + " (in)");
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
			if(command.equals("addDispRow"))
			{
				dispTableModel.addRow();
			}
			else if(command.equals("ca"))
			{
				boolean set = CAdisp.isSelected();
				CAconstTF.setEnabled(!set);
				dispTable.setEnabled(set);
				dispAddRow.setEnabled(set);
				dispDelRow.setEnabled(set);
			}
			else if(command.equals("delDispRow"))
			{
				if(dispTableModel.getRowCount() > 1)
					dispTableModel.removeRow(dispTableModel.getRowCount() - 1);
			}
			else if(command.equals("next"))
			{
				parent.selectRigorousRigidBlock();
			}
			else if(command.equals("scale"))
			{
				scalePGAval.setEnabled(scalePGA.isSelected());
				scaleData.setEnabled(scaleOn.isSelected());
			}
			else if(command.equals("slope"))
			{
				thrustAngle.setEnabled(dualSlope.isSelected());
			}
			else if(command.equals("unit"))
			{
				updateUnits();
			}
			else if(command.equals("DeCoupled"))
			{
				updateDeCoupled();
			}
			else if(command.equals("Rigid"))
			{
				updateRigid();
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	public void updateDeCoupled()
	{
		boolean selected = typeDecoupled.isSelected() || typeCoupled.isSelected();

		paramHeight.setEnabled(selected);
		paramVs.setEnabled(selected);
		paramDamp.setEnabled(selected);
		paramVr.setEnabled(selected);
		paramSoilModel.setEnabled(selected);
	}

	public void updateRigid()
	{
		boolean selected = typeRigid.isSelected();

		downSlope.setEnabled(selected);
		dualSlope.setEnabled(selected);
		thrustAngle.setEnabled(selected && dualSlope.isSelected());
	}
}
