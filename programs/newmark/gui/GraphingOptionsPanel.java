/*
 * Copyright (C) 2005 Matthew Jibson (dolmant@dolmant.net)
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
import newmark.*;

class GraphingOptionsPanel extends JPanel implements ActionListener
{
	NewmarkTabbedPane parent;

	ButtonGroup TypeGroup = new ButtonGroup();
	JRadioButton typeTime = new JRadioButton("Time Series", true);
	JRadioButton typeFourier = new JRadioButton("Fourier Amplitude Spectrum");
	JRadioButton typeSpectra = new JRadioButton("Response Spectra");

	JComboBox spectraCB = new JComboBox();

	JTextField spectraDamp = new JTextField("0", 5);
	JTextField spectraIncr = new JTextField("0.01", 5);
	JTextField spectraHigh = new JTextField("15.0", 5);

	final public static String spectraAccStr = "Absolute-Acceleration";
	final public static String spectraVelStr = "Relative-Velocity";
	final public static String spectraDisStr = "Relative-Displacement";

	public GraphingOptionsPanel(NewmarkTabbedPane parent) throws Exception
	{
		this.parent = parent;

		TypeGroup.add(typeTime);
		TypeGroup.add(typeFourier);
		TypeGroup.add(typeSpectra);

		spectraCB.addItem(spectraAccStr);
		spectraCB.addItem(spectraVelStr);
		spectraCB.addItem(spectraDisStr);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		JLabel label;

		setLayout(gridbag);

		int x = 0;
		int y = 0;

		c.anchor = GridBagConstraints.NORTHWEST;

		c.gridx = x++;
		c.gridy = y++;
		gridbag.setConstraints(typeTime, c);
		add(typeTime);

		c.gridy = y++;
		gridbag.setConstraints(typeFourier, c);
		add(typeFourier);

		c.gridy = y++;
		gridbag.setConstraints(typeSpectra, c);
		add(typeSpectra);

		c.gridx = x++;
		c.gridy = y++;
		gridbag.setConstraints(spectraCB, c);
		add(spectraCB);

		x = 1;
		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Damping (%)");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x++;
		label = new JLabel("Frequency Intrement (Hz)");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x++;
		label = new JLabel("High Frequency (Hz)");
		gridbag.setConstraints(label, c);
		add(label);

		x = 1;
		c.gridx = x++;
		c.gridy = y++;
		gridbag.setConstraints(spectraDamp, c);
		add(spectraDamp);

		c.gridx = x++;
		gridbag.setConstraints(spectraIncr, c);
		add(spectraIncr);

		c.gridx = x++;
		gridbag.setConstraints(spectraHigh, c);
		add(spectraHigh);
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			System.out.println(command);
			if(command.equals(""))
			{
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}
}
