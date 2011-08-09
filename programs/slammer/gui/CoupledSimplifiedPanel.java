/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import slammer.*;
import slammer.analysis.*;

class CoupledSimplifiedPanel extends JPanel implements ActionListener
{
	SlammerTabbedPane parent;

	JButton go = new JButton("Compute");
	JButton clear = new JButton("Clear fields");

	JTextField ky = new WideTextField(7);
	JTextField ts = new WideTextField(7);
	JTextField sa = new WideTextField(7);
	JTextField m = new WideTextField(7);

	JTextField dispcm = new WideTextField(7);
	JTextField dispin = new WideTextField(7);

	JEditorPane ta = new JEditorPane("text/html",
		"<html>Procedure based on Bray and Travasarou (2007) equation (5):"
		+ "<p>ln <i>D</i> = -1.10 - 2.83 ln ( <i>k<sub>y</sub></i> ) - 0.333 ( ln ( <i>k<sub>y</sub></i> ) )<sup>2</sup> + 0.566 ln ( <i>k<sub>y</sub></i> ) ln ( <i>S<sub>a</sub></i> ( 1.5 <i>T<sub>s</sub></i> ) ) + 3.04 ln ( <i>S<sub>a</sub></i> ( 1.5 <i>T<sub>s</sub></i> ) ) - 0.244 ( ln ( <i>S<sub>a</sub></i> ( 1.5 <i>T<sub>s</sub></i> ) ) )<sup>2</sup> + 1.50 <i>T<sub>s</sub></i> + 0.278 ( M - 7 ),"
		+ "<p>where <i>D</i> is displacement in centimeters and other parameters are as shown above.  This equation assumes that there will be a non-zero displacement; to estimate the probability of a non-zero displacement, see Bray and Travasarou (2007).</html>"
	);

	Double kyd, tsd, sad, md;

	public CoupledSimplifiedPanel(SlammerTabbedPane parent) throws Exception
	{
		this.parent = parent;

		go.setActionCommand("go");
		go.addActionListener(this);

		clear.setActionCommand("clear");
		clear.addActionListener(this);

		dispcm.setEditable(false);
		dispin.setEditable(false);

		ta.setEditable(false);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		Insets left = new Insets(0, 20, 0, 0);
		Insets halfLeft = new Insets(0, 10, 0, 0);
		Insets top = new Insets(10, 0, 0, 0);
		Insets none = new Insets(0, 0, 0, 0);

		setLayout(gridbag);

		int x = 0;
		int y = 0;
		JLabel label;

		c.gridx = x;
		c.gridy = y++;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.NORTHWEST;
		label = new JLabel("Input parameters:");
		label.setFont(GUIUtils.headerFont);
		gridbag.setConstraints(label, c);
		add(label);

		c.gridwidth = 1;
		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("<html>Yield coefficient, k<sub>y</sub>:</html>");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x++;
		gridbag.setConstraints(ky, c);
		add(ky);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("<html>Fundamental site period, T<sub>s</sub>:</html>");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x++;
		gridbag.setConstraints(ts, c);
		add(ts);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("<html>Spectral acceleration at 1.5 * T<sub>s</sub> (g):</html>");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x++;
		gridbag.setConstraints(sa, c);
		add(sa);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Earthquake magnitude, M:");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x++;
		gridbag.setConstraints(m, c);
		add(m);

		c.gridy = y++;
		x = 0;
		c.gridx = x;
		c.insets = top;
		gridbag.setConstraints(go, c);
		add(go);

		c.anchor = GridBagConstraints.SOUTHEAST;
		gridbag.setConstraints(clear, c);
		add(clear);

		c.gridy = y++;
		x = 0;
		c.gridx = x;
		c.gridwidth = 1;
		c.insets = top;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTHWEST;
		label = new JLabel("Results:");
		label.setFont(GUIUtils.headerFont);
		gridbag.setConstraints(label, c);
		add(label);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("Estimated displacement (cm):");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x++;
		gridbag.setConstraints(dispcm, c);
		add(dispcm);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Estimated displacement (in.):");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x++;
		gridbag.setConstraints(dispin, c);
		add(dispin);

		c.gridx = x;
		label = new JLabel("");
		c.weightx = 1;
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = 0;
		c.gridy = y;
		c.gridwidth = 5;
		c.weightx = 1;
		c.weighty = 1;
		c.insets = top;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(ta, c);
		add(ta);
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			if(command.equals("go"))
			{
				kyd = (Double)Utils.checkNum(ky.getText(), "critical acceleration field", null, false, null, new Double(0), true, null, false);
				if(kyd == null) return;

				tsd = (Double)Utils.checkNum(ts.getText(), "fundamental site period field", null, false, null, new Double(0), true, null, false);
				if(tsd == null) return;

				sad = (Double)Utils.checkNum(sa.getText(), "spectral acceleration field", null, false, null, new Double(0), true, null, false);
				if(sad == null) return;

				md = (Double)Utils.checkNum(m.getText(), "earthquake magnitude field", null, false, null, new Double(0), true, null, false);
				if(md == null) return;

				String[] res = CoupledSimplified.BrayAndTravasarou2007(kyd.doubleValue(), tsd.doubleValue(), sad.doubleValue(), md.doubleValue());

				int incr = 0;
				dispcm.setText(res[incr++]);
				dispin.setText(res[incr++]);
			}
			else if(command.equals("clear"))
			{
				ky.setText("");
				ts.setText("");
				sa.setText("");
				m.setText("");

				dispcm.setText("");
				dispin.setText("");
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}
}
