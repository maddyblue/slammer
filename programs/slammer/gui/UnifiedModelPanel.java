/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import slammer.*;
import slammer.analysis.*;

class UnifiedModelPanel extends JPanel implements ActionListener
{
	SlammerTabbedPane parent;

	JRadioButton UnifiedPAM = new JRadioButton("Peak acceleration and magnitude", true);
	JRadioButton UnifiedPAV = new JRadioButton("Peak acceleration and velocity");
	ButtonGroup group = new ButtonGroup();

	JTextField fieldAc = new WideTextField(7);
	JTextField fieldTs = new WideTextField(7);
	JTextField fieldM = new WideTextField(7);
	JTextField fieldPGA = new WideTextField(7);
	JTextField fieldPGV = new WideTextField(7);
	JTextField fieldTm = new WideTextField(7);

	JTextField fieldResPR = new WideTextField(7);
	JTextField fieldAcPGA = new WideTextField(7);
	JTextField fieldkmaxPGA = new WideTextField(7);
	JTextField fieldkvelmaxPGV = new WideTextField(7);
	JTextField fieldkmax = new WideTextField(7);
	JTextField fieldkvelmax = new WideTextField(7);
	JTextField fieldrbdisp = new WideTextField(7);

	JTextField fieldResCm = new WideTextField(7);
	JTextField fieldResIn = new WideTextField(7);

	Double Acd, Tsd, Md, PGAd, PGVd, Tmd;

	String unifiedStr = "Based on the unified model from Rathje and Antonakos (2011).  The unified model is designed to give reliable results for a full range of period ratios representing both flexible and rigid conditions.";

	JEditorPane ta = new JEditorPane("text/html", unifiedStr);
	JScrollPane sta = new JScrollPane(ta);
	JButton button = new JButton("Compute");

	public UnifiedModelPanel(SlammerTabbedPane parent) throws Exception
	{
		this.parent = parent;

		group.add(UnifiedPAV);
		group.add(UnifiedPAM);

		UnifiedPAV.setActionCommand("change");
		UnifiedPAV.addActionListener(this);
		UnifiedPAM.setActionCommand("change");
		UnifiedPAM.addActionListener(this);

		fieldPGV.setEditable(false);

		button.setActionCommand("go");
		button.addActionListener(this);

		fieldResPR.setEditable(false);
		fieldAcPGA.setEditable(false);
		fieldkmaxPGA.setEditable(false);
		fieldkvelmaxPGV.setEditable(false);
		fieldkmax.setEditable(false);
		fieldkvelmax.setEditable(false);
		fieldrbdisp.setEditable(false);
		fieldResCm.setEditable(false);
		fieldResIn.setEditable(false);

		ta.setEditable(false);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		Insets top = new Insets(10, 0, 0, 0);
		Insets none = new Insets(0, 0, 0, 0);

		Border b = BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(0, 0, 0, 5),
			BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK)
		);

		JPanel panel = new JPanel();
		panel.setLayout(gridbag);

		int x = 0;
		int y = 0;
		JLabel label;

		Box sidepanel = new Box(BoxLayout.Y_AXIS);

		label = new JLabel("Select analysis:");
		label.setFont(GUIUtils.headerFont);
		sidepanel.add(label);
		sidepanel.add(UnifiedPAM);
		sidepanel.add(UnifiedPAV);

		c.gridx = x++;
		c.gridy = y++;
		c.gridheight = 19;
		c.anchor = GridBagConstraints.NORTHWEST;
		gridbag.setConstraints(sidepanel, c);
		panel.add(sidepanel);

		c.gridx = x++;
		c.fill = GridBagConstraints.VERTICAL;
		label = new JLabel(" ");
		label.setBorder(b);
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridheight = 1;
		c.gridwidth = 3;
		c.gridx = x++;
		c.gridy = y++;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		label = new JLabel("Input parameters (Rathje and Antonakos, 2011):");
		label.setFont(GUIUtils.headerFont);
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		c.gridwidth = 1;
		label = new JLabel("<html>Critical (yield) acceleration, a<sub>c</sub> or k<sub>y</sub> (g):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldAc, c);
		panel.add(fieldAc);

		c.gridx = x + 2;
		c.insets = GUIUtils.insetsLeft;
		label = new JLabel(ParametersPanel.stringHelp);
		gridbag.setConstraints(label, c);
		panel.add(label);
		c.insets = GUIUtils.insetsNone;

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("<html>Site period, T<sub>s</sub> (s):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldTs, c);
		panel.add(fieldTs);

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Earthquake magnitude, M:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldM, c);
		panel.add(fieldM);

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Peak ground acceleration, PGA (g):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldPGA, c);
		panel.add(fieldPGA);

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Peak ground velocity, PGV (cm/s):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldPGV, c);
		panel.add(fieldPGV);

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("<html>Mean shaking period, T<sub>m</sub> (s):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldTm, c);
		panel.add(fieldTm);

		c.gridx = x++;
		c.gridy = y++;
		c.insets = top;
		c.gridwidth = 2;
		gridbag.setConstraints(button, c);
		panel.add(button);

		c.gridy = y++;
		label = new JLabel("Calculations:");
		label.setFont(GUIUtils.headerFont);
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		c.gridwidth = 1;
		c.insets = none;
		label = new JLabel("<html>Period ratio, T<sub>s</sub>/T<sub>m</sub>:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldResPR, c);
		panel.add(fieldResPR);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("<html>k<sub>max</sub> / PGA:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldkmaxPGA, c);
		panel.add(fieldkmaxPGA);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("<html>k-vel<sub>max</sub> / PGV:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldkvelmaxPGV, c);
		panel.add(fieldkvelmaxPGV);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("<html>k<sub>max</sub> (g):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldkmax, c);
		panel.add(fieldkmax);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("<html>k-vel<sub>max</sub> (cm/s):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldkvelmax, c);
		panel.add(fieldkvelmax);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("Results:");
		label.setFont(GUIUtils.headerFont);
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		c.insets = none;
		label = new JLabel("Displacement (cm):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldResCm, c);
		panel.add(fieldResCm);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("Displacement (in.):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldResIn, c);
		panel.add(fieldResIn);

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
			if(command.equals("go"))
			{
				Acd = (Double)Utils.checkNum(fieldAc.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
				if(Acd == null) return;

				Tsd = (Double)Utils.checkNum(fieldTs.getText(), "site period field", null, false, null, new Double(0), true, null, false);
				if(Tsd == null) return;

				Md = (Double)Utils.checkNum(fieldM.getText(), "magnitude field", null, false, null, new Double(0), true, null, false);
				if(Md == null) return;

				PGAd = (Double)Utils.checkNum(fieldPGA.getText(), "peak ground acceleration field", null, false, null, new Double(0), true, null, false);
				if(PGAd == null) return;

				if(fieldPGV.isEditable())
				{
					PGVd = (Double)Utils.checkNum(fieldPGV.getText(), "peak ground velocity field", null, false, null, new Double(0), true, null, false);
					if(PGVd == null) return;
				}
				else
					PGVd = new Double(0);

				Tmd = (Double)Utils.checkNum(fieldTm.getText(), "mean shaking period field", null, false, null, new Double(0), true, null, false);
				if(Tmd == null) return;

				int method = 0;
				if(UnifiedPAV.isSelected())
					method = UnifiedModel.METHOD_2008;
				else if(UnifiedPAM.isSelected())
					method = UnifiedModel.METHOD_2009;
				else
				{
					GUIUtils.popupError("No method selected.");
					return;
				}

				String[] res = UnifiedModel.UnifiedModel(Acd.doubleValue(), Tsd.doubleValue(), Md.doubleValue(), PGAd.doubleValue(), PGVd.doubleValue(), Tmd.doubleValue(), method);

				int incr = 0;
				fieldResPR.setText(res[incr++]);
				fieldAcPGA.setText(res[incr++]);
				fieldkmaxPGA.setText(res[incr++]);
				fieldkvelmaxPGV.setText(res[incr++]);
				fieldkmax.setText(res[incr++]);
				fieldkvelmax.setText(res[incr++]);
				fieldrbdisp.setText(res[incr++]);
				fieldResCm.setText(res[incr++]);
				fieldResIn.setText(res[incr++]);
			}
			else if(command.equals("change"))
			{
				fieldPGV.setEditable(!UnifiedPAM.isSelected());
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}
}
