/*
 * DecoupledSimplifiedPanel.java
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
import newmark.*;
import newmark.analysis.*;

class DecoupledSimplifiedPanel extends JPanel implements ActionListener
{
	NewmarkTabbedPane parent;

	String zero = "0";

	JButton go = new JButton("Compute");
	JButton clear = new JButton("Clear fields");

	JLabel inputParameters = new JLabel("Input parameters:");

	JTextField ky = new JTextField(7);
	JTextField h = new JTextField(7);
	JTextField vs = new JTextField(7);
	JTextField m = new JTextField(7);
	JTextField rock = new JTextField(7);
	JTextField r = new JTextField(7);

	JLabel siteScreening = new JLabel("Site screening procedure (optional):");

	JTextField allowdisp = new JTextField(7);

	JLabel standardDeviations = new JLabel("Standard deviations (sigma), if known:");

	JTextField mheaS = new JTextField(zero, 7);
	JTextField meanperS = new JTextField(zero, 7);
	JTextField sigdurS = new JTextField(zero, 7);
	JTextField normdispS = new JTextField(zero, 7);

	JLabel calculations = new JLabel("Calculations:");

	JTextField siteper = new JTextField(7);
	JTextField nrffact = new JTextField(7);
	JTextField meanper = new JTextField(7);
	JTextField dur = new JTextField(7);
	JTextField tstm = new JTextField(7);
	JTextField mheamhanrf = new JTextField(7);
	JTextField kmax = new JTextField(7);
	JTextField kykmax = new JTextField(7);
	JTextField normdisp = new JTextField(7);

	JLabel results = new JLabel("Results:");

	JTextField dispcm = new JTextField(7);
	JTextField dispin = new JTextField(7);
	JTextField medianfreq = new JTextField(7);
	JTextField seiscoef = new JTextField(7);

	Double kyd, hd, vsd, md, rockd, rd, mheaSd, meanperSd, sigdurSd, normdispSd, allowdispd;

	public DecoupledSimplifiedPanel(NewmarkTabbedPane parent) throws Exception
	{
		this.parent = parent;

		go.setMnemonic(KeyEvent.VK_C);
		go.setActionCommand("go");
		go.addActionListener(this);

		clear.setMnemonic(KeyEvent.VK_F);
		clear.setActionCommand("clear");
		clear.addActionListener(this);

		inputParameters.setFont(GUIUtils.headerFont);
		siteScreening.setFont(GUIUtils.headerFont);
		standardDeviations.setFont(GUIUtils.headerFont);
		calculations.setFont(GUIUtils.headerFont);
		results.setFont(GUIUtils.headerFont);

		siteper.setEnabled(false);
		nrffact.setEnabled(false);
		meanper.setEnabled(false);
		dur.setEnabled(false);
		tstm.setEnabled(false);
		mheamhanrf.setEnabled(false);
		kmax.setEnabled(false);
		kykmax.setEnabled(false);
		normdisp.setEnabled(false);

		dispcm.setEnabled(false);
		dispin.setEnabled(false);
		medianfreq.setEnabled(false);
		seiscoef.setEnabled(false);

		add(createPanel());
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			System.out.println(command);
			if(command.equals("go"))
			{
				kyd = (Double)Utils.checkNum(ky.getText(), "Yield Acceleration field", null, false, null, new Double(0), true, null, false);
				if(kyd == null) return;

				hd = (Double)Utils.checkNum(h.getText(), "Vertical Thickness field", null, false, null, new Double(0), true, null, false);
				if(hd == null) return;

				vsd = (Double)Utils.checkNum(vs.getText(), "Shear Wave Vel. field", null, false, null, new Double(0), true, null, false);
				if(vsd == null) return;

				md = (Double)Utils.checkNum(m.getText(), "Earthquake Magnitude field", null, false, null, new Double(0), true, null, false);
				if(md == null) return;

				rockd = (Double)Utils.checkNum(rock.getText(), "Earthquake Accel. field", null, false, null, new Double(0), true, null, false);
				if(rockd == null) return;

				rd = (Double)Utils.checkNum(r.getText(), "Earthquake Distance field", null, false, null, new Double(0), true, null, false);
				if(rd == null) return;

				mheaSd = (Double)Utils.checkNum(mheaS.getText(), "Normalized MHEA Sigma field", null, false, null, new Double(0), true, null, false);
				if(mheaSd == null) return;

				meanperSd = (Double)Utils.checkNum(meanperS.getText(), "Mean Period Sigma field", null, false, null, new Double(0), true, null, false);
				if(meanperSd == null) return;

				sigdurSd = (Double)Utils.checkNum(sigdurS.getText(), "Significant Duration Sigma field", null, false, null, new Double(0), true, null, false);
				if(sigdurSd == null) return;

				normdispSd = (Double)Utils.checkNum(normdispS.getText(), "Normalized Displacement Sigma field", null, false, null, new Double(0), true, null, false);
				if(normdispSd == null) return;

				boolean doScreening = allowdisp.getText().equals("") ? false : true;
				if(doScreening)
				{
					allowdispd = (Double)Utils.checkNum(allowdisp.getText(), "Allowable Displacement field", null, false, null, new Double(0), true, null, false);
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
				String blank = "";

				ky.setText(blank);
				h.setText(blank);
				vs.setText(blank);
				m.setText(blank);
				rock.setText(blank);
				r.setText(blank);
				allowdisp.setText(blank);

				mheaS.setText(zero);
				meanperS.setText(zero);
				sigdurS.setText(zero);
				normdispS.setText(zero);

				siteper.setText(blank);
				nrffact.setText(blank);
				meanper.setText(blank);
				dur.setText(blank);
				tstm.setText(blank);
				mheamhanrf.setText(blank);
				kmax.setText(blank);
				kykmax.setText(blank);
				normdisp.setText(blank);

				dispcm.setText(blank);
				dispin.setText(blank);
				medianfreq.setText(blank);
				seiscoef.setText(blank);
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	private JPanel createPanel()
	{
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		JLabel label;
		Border leftBorder = BorderFactory.createCompoundBorder(
			new MatteBorder(0, 1, 0, 0, Color.black),
			new EmptyBorder(0, 9, 0, 0));

		Insets left = new Insets(0, 20, 0, 0);
		Insets halfLeft = new Insets(0, 10, 0, 0);
		Insets top = new Insets(10, 0, 0, 0);
		Insets none = new Insets(0, 0, 0, 0);

		JPanel panel = new JPanel();
		panel.setLayout(gridbag);

		int x = 0;
		int y = 0;

		c.anchor = GridBagConstraints.SOUTHWEST;

		c.gridx = 0;
		c.gridy = y++;
		c.gridwidth = 2;
		gridbag.setConstraints(inputParameters, c);
		panel.add(inputParameters);

		c.gridx = 2;
		c.insets = left;
		gridbag.setConstraints(standardDeviations, c);
		panel.add(standardDeviations);

		c.gridwidth = 1;
		c.gridy = y++;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("Critical (yield) acceleration, Ac (g): ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(ky, c);
		panel.add(ky);

		c.gridx = x++;
		c.insets = left;
		label = new JLabel("Normalized MHEA: ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		c.insets = none;
		gridbag.setConstraints(mheaS, c);
		panel.add(mheaS);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Vertical thickness, h (m): ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(h, c);
		panel.add(h);

		c.gridx = x++;
		c.insets = left;
		label = new JLabel("Mean Period: ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		c.insets = none;
		gridbag.setConstraints(meanperS, c);
		panel.add(meanperS);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Shear-wave velocity, Vs (m/s): ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(vs, c);
		panel.add(vs);

		c.gridx = x++;
		c.insets = left;
		label = new JLabel("Significant duration: ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		c.insets = none;
		gridbag.setConstraints(sigdurS, c);
		panel.add(sigdurS);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Earthquake magnitude, M: ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(m, c);
		panel.add(m);

		c.gridx = x++;
		c.insets = left;
		label = new JLabel("Normalized displacement: ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		c.insets = none;
		gridbag.setConstraints(normdispS, c);
		panel.add(normdispS);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Peak bedrock acceleration, MHA (g): ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(rock, c);
		panel.add(rock);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Earthquake distance, r (km): ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(r, c);
		panel.add(r);

		c.gridy = y++;
		x = 0;
		c.gridx = x;
		c.insets = top;
		gridbag.setConstraints(go, c);
		panel.add(go);

		c.anchor = GridBagConstraints.SOUTHEAST;
		gridbag.setConstraints(clear, c);
		panel.add(clear);

		x += 2;
		c.gridx = x;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.SOUTHWEST;
		c.insets = halfLeft;
		c.fill = GridBagConstraints.BOTH;
		siteScreening.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(1, 1, 0, 0, Color.black),
			BorderFactory.createEmptyBorder(4, 9, 0, 0)));
		gridbag.setConstraints(siteScreening, c);
		panel.add(siteScreening);

		c.gridy = y++;
		x = 0;
		c.gridx = x;
		c.gridwidth = 1;
		c.insets = top;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(calculations, c);
		panel.add(calculations);

		x += 2;
		c.gridx = x++;
		c.gridheight = 13;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = halfLeft;
		label = new JLabel("Allowable displacement (cm): ");
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
		c.anchor = GridBagConstraints.SOUTHWEST;
		label = new JLabel("Site period, Ts (s): ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(siteper, c);
		panel.add(siteper);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Mean shaking period, Tm (s): ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(meanper, c);
		panel.add(meanper);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Period ratio, Ts/Tm: ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(tstm, c);
		panel.add(tstm);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Duration, D(5-95%) (s): ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(dur, c);
		panel.add(dur);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Non-linear response factor (NRF): ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(nrffact, c);
		panel.add(nrffact);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Max. hor. equiv. acc. (MHEA), Amax (g): ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(kmax, c);
		panel.add(kmax);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("MHEA/(MHA*NRF): ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(mheamhanrf, c);
		panel.add(mheamhanrf);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Ac/Amax: ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(kykmax, c);
		panel.add(kykmax);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Normalized displacement (cm/s): ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(normdisp, c);
		panel.add(normdisp);

		c.gridy = y++;
		x = 0;
		c.gridx = x;
		c.insets = top;
		gridbag.setConstraints(results, c);
		panel.add(results);

		c.gridy = y++;
		c.gridx = x++;
		c.insets = none;
		label = new JLabel("Estimated displacement (cm): ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(dispcm, c);
		panel.add(dispcm);

		c.gridx = x++;
		c.insets = left;
		label = new JLabel("Median Feq for screen procedure: ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		c.insets = none;
		gridbag.setConstraints(medianfreq, c);
		panel.add(medianfreq);

		c.gridy = y++;
		x = 0;
		c.gridx = x++;
		label = new JLabel("Estimated displacement (in): ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(dispin, c);
		panel.add(dispin);

		c.gridx = x++;
		c.insets = left;
		label = new JLabel("Seismic coefficient for screen procedure: ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		c.insets = none;
		gridbag.setConstraints(seiscoef, c);
		panel.add(seiscoef);

		return panel;
	}
}
