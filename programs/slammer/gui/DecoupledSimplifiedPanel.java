/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import slammer.*;
import slammer.analysis.*;

class DecoupledSimplifiedPanel extends JPanel implements ActionListener
{
	SlammerTabbedPane parent;

	JButton go = new JButton("Compute");
	JButton clear = new JButton("Clear fields");

	JTextField ky = new WideTextField(7);
	JTextField h = new WideTextField(7);
	JTextField vs = new WideTextField(7);
	JTextField m = new WideTextField(7);
	JTextField rock = new WideTextField(7);
	JTextField r = new WideTextField(7);

	JTextField allowdisp = new WideTextField(7);

	JTextField mheaS = new WideTextField("0", 7);
	JTextField meanperS = new WideTextField("0", 7);
	JTextField sigdurS = new WideTextField("0", 7);
	JTextField normdispS = new WideTextField("0", 7);

	JTextField siteper = new WideTextField(7);
	JTextField nrffact = new WideTextField(7);
	JTextField meanper = new WideTextField(7);
	JTextField dur = new WideTextField(7);
	JTextField tstm = new WideTextField(7);
	JTextField mheamhanrf = new WideTextField(7);
	JTextField kmax = new WideTextField(7);
	JTextField kykmax = new WideTextField(7);
	JTextField normdisp = new WideTextField(7);

	JTextField dispcm = new WideTextField(7);
	JTextField dispin = new WideTextField(7);
	JTextField medianfreq = new WideTextField(7);
	JTextField seiscoef = new WideTextField(7);

	JEditorPane ta = new JEditorPane("text/html", "<html>Procedure based on Bray and Rathje (1998) and Blake and others (2002).</html>");

	Double kyd, hd, vsd, md, rockd, rd, mheaSd, meanperSd, sigdurSd, normdispSd, allowdispd;

	public DecoupledSimplifiedPanel(SlammerTabbedPane parent) throws Exception
	{
		this.parent = parent;

		go.setActionCommand("go");
		go.addActionListener(this);

		clear.setActionCommand("clear");
		clear.addActionListener(this);

		siteper.setEditable(false);
		nrffact.setEditable(false);
		meanper.setEditable(false);
		dur.setEditable(false);
		tstm.setEditable(false);
		mheamhanrf.setEditable(false);
		kmax.setEditable(false);
		kykmax.setEditable(false);
		normdisp.setEditable(false);

		dispcm.setEditable(false);
		dispin.setEditable(false);
		medianfreq.setEditable(false);
		seiscoef.setEditable(false);

		ta.setEditable(false);

		setLayout(new BorderLayout());

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		Insets left = new Insets(0, 20, 0, 0);
		Insets halfLeft = new Insets(0, 10, 0, 0);
		Insets top = new Insets(10, 0, 0, 0);
		Insets none = new Insets(0, 0, 0, 0);

		JPanel panel = new JPanel();
		panel.setLayout(gridbag);

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
		panel.add(label);

		x += 2;
		c.gridx = x;
		c.insets = left;
		label = new JLabel("Standard deviations (optional):");
		label.setFont(GUIUtils.headerFont);
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridwidth = 1;
		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("<html>Critical (yield) acceleration, a<sub>c</sub> (g):</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(ky, c);
		panel.add(ky);

		c.gridx = x++;
		c.insets = left;
		label = new JLabel("Normalized MHEA:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		c.insets = none;
		gridbag.setConstraints(mheaS, c);
		panel.add(mheaS);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Vertical thickness, h (m):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(h, c);
		panel.add(h);

		c.gridx = x++;
		c.insets = left;
		label = new JLabel("Mean period:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		c.insets = none;
		gridbag.setConstraints(meanperS, c);
		panel.add(meanperS);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("<html>Shear-wave velocity, V<sub>s</sub> (m/s):</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(vs, c);
		panel.add(vs);

		c.gridx = x++;
		c.insets = left;
		label = new JLabel("Significant duration:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		c.insets = none;
		gridbag.setConstraints(sigdurS, c);
		panel.add(sigdurS);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Earthquake magnitude, M:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(m, c);
		panel.add(m);

		c.gridx = x++;
		c.insets = left;
		label = new JLabel("Normalized displacement:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		c.insets = none;
		gridbag.setConstraints(normdispS, c);
		panel.add(normdispS);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Peak bedrock acceleration, MHA (g):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(rock, c);
		panel.add(rock);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("Earthquake distance, r (km):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(r, c);
		panel.add(r);

		c.gridy = y++;
		c.gridx = x;
		c.insets = top;
		gridbag.setConstraints(go, c);
		panel.add(go);

		c.anchor = GridBagConstraints.SOUTHEAST;
		gridbag.setConstraints(clear, c);
		panel.add(clear);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = top;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.SOUTHWEST;
		label = new JLabel("Calculations:");
		label.setFont(GUIUtils.headerFont);
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		c.insets = none;
		label = new JLabel("<html>Site period, T<sub>s</sub> (s):</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(siteper, c);
		panel.add(siteper);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("<html>Mean shaking period, T<sub>m</sub> (s):</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(meanper, c);
		panel.add(meanper);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("<html>Period ratio, T<sub>s</sub>/T<sub>m</sub>:</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(tstm, c);
		panel.add(tstm);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("<html>Duration, D<sub>(5-95%)</sub> (s):</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(dur, c);
		panel.add(dur);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("Non-linear response factor (NRF):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(nrffact, c);
		panel.add(nrffact);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("<html>Max. hor. equiv. acc. (MHEA), a<sub>max</sub> (g):</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(kmax, c);
		panel.add(kmax);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("MHEA/(MHA*NRF):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(mheamhanrf, c);
		panel.add(mheamhanrf);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("<html>a<sub>c</sub>/a<sub>max</sub>:</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		gridbag.setConstraints(kykmax, c);
		panel.add(kykmax);

		c.gridy = y++;
		c.gridx = x++;
		label = new JLabel("Normalized displacement (cm/s):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(normdisp, c);
		panel.add(normdisp);

		c.gridx = x;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.SOUTHWEST;
		c.insets = halfLeft;
		c.fill = GridBagConstraints.BOTH;
		label = new JLabel("Site screening procedure (optional):");
		label.setFont(GUIUtils.headerFont);
		label.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(1, 1, 0, 0, Color.black),
			BorderFactory.createEmptyBorder(4, 9, 0, 0)));
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		x = 0;
		c.gridx = x;
		c.gridwidth = 1;
		c.insets = top;
		c.fill = GridBagConstraints.NONE;
		label = new JLabel("Results:");
		label.setFont(GUIUtils.headerFont);
		gridbag.setConstraints(label, c);
		panel.add(label);

		x += 2;
		c.gridx = x++;
		c.gridheight = 3;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = halfLeft;
		label = new JLabel("Allowable displacement (cm):");
		label.setVerticalAlignment(SwingConstants.TOP);
		label.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 1, 0, 0, Color.black),
			BorderFactory.createEmptyBorder(0, 9, 0, 0)));
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x;
		c.insets = none;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(allowdisp, c);
		panel.add(allowdisp);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("Estimated displacement (cm):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(dispcm, c);
		panel.add(dispcm);

		c.gridx = x++;
		c.insets = left;
		label = new JLabel("<html>Median F<sub>eq</sub> for screen procedure:</html>");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		c.insets = none;
		gridbag.setConstraints(medianfreq, c);
		panel.add(medianfreq);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Estimated displacement (in.):");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(dispin, c);
		panel.add(dispin);

		c.gridx = x++;
		c.insets = left;
		label = new JLabel("Seismic coefficient for screen procedure:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		c.insets = none;
		gridbag.setConstraints(seiscoef, c);
		panel.add(seiscoef);

		c.gridx = x;
		c.weightx = 1;
		label = new JLabel("");
		gridbag.setConstraints(label, c);
		panel.add(label);

		add(BorderLayout.NORTH, panel);
		add(BorderLayout.CENTER, ta);;
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

				hd = (Double)Utils.checkNum(h.getText(), "vertical thickness field", null, false, null, new Double(0), true, null, false);
				if(hd == null) return;

				vsd = (Double)Utils.checkNum(vs.getText(), "shear-wave velocity field", null, false, null, new Double(0), true, null, false);
				if(vsd == null) return;

				md = (Double)Utils.checkNum(m.getText(), "earthquake magnitude field", null, false, null, new Double(0), true, null, false);
				if(md == null) return;

				rockd = (Double)Utils.checkNum(rock.getText(), "peak bedrock acceleration field", null, false, null, new Double(0), true, null, false);
				if(rockd == null) return;

				rd = (Double)Utils.checkNum(r.getText(), "earthquake distance field", null, false, null, new Double(0), true, null, false);
				if(rd == null) return;

				mheaSd = (Double)Utils.checkNum(mheaS.getText(), "normalized MHEA field", null, false, null, new Double(0), true, null, false);
				if(mheaSd == null) return;

				meanperSd = (Double)Utils.checkNum(meanperS.getText(), "mean period field", null, false, null, new Double(0), true, null, false);
				if(meanperSd == null) return;

				sigdurSd = (Double)Utils.checkNum(sigdurS.getText(), "significant duration field", null, false, null, new Double(0), true, null, false);
				if(sigdurSd == null) return;

				normdispSd = (Double)Utils.checkNum(normdispS.getText(), "normalized displacement field", null, false, null, new Double(0), true, null, false);
				if(normdispSd == null) return;

				boolean doScreening = allowdisp.getText().equals("") ? false : true;
				if(doScreening)
				{
					allowdispd = (Double)Utils.checkNum(allowdisp.getText(), "allowable displacement field", null, false, null, new Double(0), true, null, false);
					if(allowdispd == null) return;
				}
				else
				{
					allowdispd = new Double(0);
				}

				String[] res = DecoupledSimplified.BrayAndRathje(kyd.doubleValue(), hd.doubleValue(), vsd.doubleValue(), md.doubleValue(), rockd.doubleValue(), rd.doubleValue(), mheaSd.doubleValue(), meanperSd.doubleValue(), sigdurSd.doubleValue(), normdispSd.doubleValue(), allowdispd.doubleValue(), doScreening);

				int incr = 0;
				siteper.setText(res[incr++]);
				nrffact.setText(res[incr++]);
				meanper.setText(res[incr++]);
				dur.setText(res[incr++]);
				tstm.setText(res[incr++]);
				mheamhanrf.setText(res[incr++]);
				kmax.setText(res[incr++]);
				kykmax.setText(res[incr++]);
				normdisp.setText(res[incr++]);
				dispcm.setText(res[incr++]);
				dispin.setText(res[incr++]);
				medianfreq.setText(res[incr++]);
				seiscoef.setText(res[incr++]);
			}
			else if(command.equals("clear"))
			{
				ky.setText("");
				h.setText("");
				vs.setText("");
				m.setText("");
				rock.setText("");
				r.setText("");
				allowdisp.setText("");

				mheaS.setText("0");
				meanperS.setText("0");
				sigdurS.setText("0");
				normdispS.setText("0");

				siteper.setText("");
				nrffact.setText("");
				meanper.setText("");
				dur.setText("");
				tstm.setText("");
				mheamhanrf.setText("");
				kmax.setText("");
				kykmax.setText("");
				normdisp.setText("");

				dispcm.setText("");
				dispin.setText("");
				medianfreq.setText("");
				seiscoef.setText("");
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}
}
