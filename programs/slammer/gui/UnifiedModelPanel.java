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

	JRadioButton SaygiliRathje2008CARPAPV = new JRadioButton("<html>Saygili and Rathje (2008) Critical acceleration ratio,<br/>peak acceleration, peak velocity</html>");
	JRadioButton SaygiliRathje2009CARPAM = new JRadioButton("<html>Rathje and Saygili (2009) Critical acceleration ratio,<br/>peak acceleration, and magnitude</html>", true);
	ButtonGroup group = new ButtonGroup();

	JTextField fieldAc = new WideTextField(7);
	JTextField fieldVthick = new WideTextField(7);
	JTextField fieldVs = new WideTextField(7);
	JTextField fieldM = new WideTextField(7);
	JTextField fieldPGA = new WideTextField(7);
	JTextField fieldPGV = new WideTextField(7);
	JTextField fieldR = new WideTextField(7);

	JTextField fieldResTs = new WideTextField(7);
	JTextField fieldResTm = new WideTextField(7);
	JTextField fieldResPR = new WideTextField(7);

	JTextField fieldAcAmax = new WideTextField(7);
	JTextField fieldkmaxPGA = new WideTextField(7);
	JTextField fieldkvelmaxPGV = new WideTextField(7);
	JTextField fieldkmax = new WideTextField(7);
	JTextField fieldkvelmax = new WideTextField(7);
	JTextField fieldrbdisp = new WideTextField(7);

	JTextField fieldResCm = new WideTextField(7);

	Double Acd, Vthickd, Vsd, Md, PGAd, PGVd, Rd;

	JEditorPane ta = new JEditorPane();
	JScrollPane sta = new JScrollPane(ta);
	JButton button = new JButton("Compute");

	String SaygiliRathje2008CARPAPVStr = "This program estimates rigid-block Newmark displacement as a function of critical acceleration, peak ground acceleration (<i>a<sub>max</sub></i>), and peak ground velocity (<i>v<sub>max</sub></i>) as explained in Saygili and Rathje (2008).  The estimate is made using the following regression equation:"
	+ "<p>ln <i>D<sub>n</sub></i> = -1.56 - 4.58 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) - 20.84 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>2</sup> + 44.75 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>3</sup> - 30.50 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>4</sup> - 0.64 ln <i>a<sub>max</sub></i> + 1.55 ln <i>v<sub>max</sub></i>"
	+ "<p>where <i>D<sub>n</sub></i> is Newmark displacement in centimeters, <i>a<sub>c</sub></i> is critical acceleration in g's, <i>a<sub>max</sub></i> is peak ground acceleration in g's, and <i>v<sub>max</sub></i> is peak ground velocity in centimeters per second. The equation was developed by conducting rigorous Newmark integrations on 2383 strong-motion records for critical acceleration values between 0.05 and 0.30 g.  The regression model has a standard deviation of 0.41 + 0.52(<i>a<sub>c</sub></i> / <i>a<sub>max</sub></i>).";

	String SaygiliRathje2009CARPAMStr = "This program estimates rigid-block Newmark displacement as a function of critical acceleration ratio, peak acceleration, and moment magnitude as explained in Rathje and Saygili (2009).  The estimate is made using the following regression equation:"
	+ "<p>ln <i>D<sub>n</sub></i> = 4.89 - 4.85 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> ) - 19.64 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>2</sup> + 42.49 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>3</sup> - 29.06 ( <i>a<sub>c</sub></i> / <i>a<sub>max</sub></i> )<sup>4</sup> + 0.72 ln <i>a<sub>max</sub></i> + 0.89 ( <b>M</b> - 6 )"
	+ "<p>where <i>D<sub>n</sub></i> is Newmark displacement in centimeters, <i>a<sub>c</sub></i> is critical acceleration in g's, <i>a<sub>max</sub></i> is horizontal peak ground acceleration (PGA) in g's, and <b>M</b> is moment magnitude.  This equation was developed by conducting rigorous Newmark integrations on more than 2000 single-component strong-motion records for several discrete values of critical acceleration.  The standard deviation of the model is 0.95.";

	public UnifiedModelPanel(SlammerTabbedPane parent) throws Exception
	{
		this.parent = parent;

		group.add(SaygiliRathje2008CARPAPV);
		group.add(SaygiliRathje2009CARPAM);

		button.setActionCommand("go");
		button.addActionListener(this);

		fieldResTs.setEditable(false);
		fieldResTm.setEditable(false);
		fieldResPR.setEditable(false);
		fieldAcAmax.setEditable(false);
		fieldkmaxPGA.setEditable(false);
		fieldkvelmaxPGV.setEditable(false);
		fieldkmax.setEditable(false);
		fieldkvelmax.setEditable(false);
		fieldrbdisp.setEditable(false);
		fieldResCm.setEditable(false);

		ta.setEditable(false);
		ta.setContentType("text/html");

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		Insets top = new Insets(10, 0, 0, 0);
		Insets none = new Insets(0, 0, 0, 0);

		Border b = BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(0, 0, 0, 5),
			BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK)
		);

		JPanel panel = this;
		panel.setLayout(gridbag);

		int x = 0;
		int y = 0;
		JLabel label;

		Box sidepanel = new Box(BoxLayout.Y_AXIS);

		label = new JLabel("Select analysis:");
		label.setFont(GUIUtils.headerFont);
		sidepanel.add(label);
		sidepanel.add(SaygiliRathje2009CARPAM);
		sidepanel.add(SaygiliRathje2008CARPAPV);

		c.gridx = x++;
		c.gridy = y++;
		c.gridheight = 11;
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
		c.gridwidth = 2;
		c.gridx = x++;
		c.gridy = y++;
		c.fill = GridBagConstraints.NONE;
		label = new JLabel("Input parameters:");
		label.setFont(GUIUtils.headerFont);
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		c.gridwidth = 1;
		label = new JLabel("<html>Critical (yield) acceleration, a<sub>c</sub> (g):</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldAc, c);
		panel.add(fieldAc);

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Vertical thickness, h (m):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldVthick, c);
		panel.add(fieldVthick);

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("<html>Shear-wave velocity, V<sub>s</sub> (m/s):</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldVs, c);
		panel.add(fieldVs);

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
		label = new JLabel("Earthquake distance, r (km):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldR, c);
		panel.add(fieldR);

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
		label = new JLabel("<html>Site period, T<sub>s</sub> (s):</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldResTs, c);
		panel.add(fieldResTs);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("<html>Mean shaking period, T<sub>m</sub> (s):</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldResTm, c);
		panel.add(fieldResTm);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("<html>Period ratio, T<sub>s</sub>/T<sub>m</sub>:</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldResPR, c);
		panel.add(fieldResPR);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("<html>a<sub>c</sub> / a<sub>max</sub>:</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldAcAmax, c);
		panel.add(fieldAcAmax);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("<html>k<sub>max</sub> / PGA:</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldkmaxPGA, c);
		panel.add(fieldkmaxPGA);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("<html>K-vel<sub>max</sub> / PGV:</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldkvelmaxPGV, c);
		panel.add(fieldkvelmaxPGV);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("<html>k<sub>max</sub> (g):</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldkmax, c);
		panel.add(fieldkmax);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("<html>k-vel<sub>max</sub> (cm/s):</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldkvelmax, c);
		panel.add(fieldkvelmax);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("<html>Rigid-block displacement (cm):</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(fieldrbdisp, c);
		panel.add(fieldrbdisp);

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

		c.gridx = x++;
		gridbag.setConstraints(fieldResCm, c);
		panel.add(fieldResCm);

		c.gridx = 0;
		c.gridy = y;
		c.insets = none;
		c.gridwidth = 4;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(sta, c);
		panel.add(sta);
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

				Vthickd = (Double)Utils.checkNum(fieldVthick.getText(), "vertical thickness field", null, false, null, new Double(0), true, null, false);
				if(Vthickd == null) return;

				Vsd = (Double)Utils.checkNum(fieldVs.getText(), "shear-wave velocity field", null, false, null, new Double(0), true, null, false);
				if(Vsd == null) return;

				Md = (Double)Utils.checkNum(fieldM.getText(), "magnitude field", null, false, null, new Double(0), true, null, false);
				if(Md == null) return;

				PGAd = (Double)Utils.checkNum(fieldPGA.getText(), "peak ground acceleration field", null, false, null, new Double(0), true, null, false);
				if(PGAd == null) return;

				PGVd = (Double)Utils.checkNum(fieldPGV.getText(), "peak ground velocity field", null, false, null, new Double(0), true, null, false);
				if(PGVd == null) return;

				Rd = (Double)Utils.checkNum(fieldR.getText(), "distance field", null, false, null, new Double(0), true, null, false);
				if(Rd == null) return;

				int method = 0;
				if(SaygiliRathje2008CARPAPV.isSelected())
					method = UnifiedModel.METHOD_2008;
				else if(SaygiliRathje2009CARPAM.isSelected())
					method = UnifiedModel.METHOD_2009;
				else
				{
					GUIUtils.popupError("No method selected.");
					return;
				}

				String[] res = UnifiedModel.UnifiedModel(Acd.doubleValue(), Vthickd.doubleValue(), Vsd.doubleValue(), Md.doubleValue(), PGAd.doubleValue(), PGVd.doubleValue(), Rd.doubleValue(), method);

				int incr = 0;
				fieldResTs.setText(res[incr++]);
				fieldResTm.setText(res[incr++]);
				fieldResPR.setText(res[incr++]);
				//fieldAcAmax.setText(res[incr++]);
				fieldkmaxPGA.setText(res[incr++]);
				fieldkvelmaxPGV.setText(res[incr++]);
				fieldkmax.setText(res[incr++]);
				fieldkvelmax.setText(res[incr++]);
				fieldrbdisp.setText(res[incr++]);
				fieldResCm.setText(res[incr++]);

			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}
}
