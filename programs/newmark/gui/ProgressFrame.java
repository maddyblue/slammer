/*
 * Copyright (C) 2004 Matthew Jibson (dolmant@dolmant.net)
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

public class ProgressFrame extends JFrame implements ActionListener
{
	JProgressBar b = new JProgressBar();
	JButton stop = new JButton("Cancel");

	int status = 0;

	boolean pressed = false;

	public ProgressFrame(int count)
	{
		super("Progress...");

		stop.setActionCommand("stop");
		stop.addActionListener(this);

		b.setStringPainted(true);
		b.setMinimum(0);
		b.setMaximum(count);
		b.setValue(0);
		b.setSize(400, 75);

		setContentPane(createContentPane());

		setSize(400,100);
		setLocationRelativeTo(null);

		show();
	}

	public JPanel createContentPane()
	{
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		JPanel p = new JPanel();

		p.setLayout(gridbag);

		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 2;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(b, c);
		p.add(b);

		c.gridy = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.VERTICAL;
		gridbag.setConstraints(stop, c);
		p.add(stop);

		return p;
	}

	public void setMaximum(int i)
	{
		b.setMaximum(i);
	}

	public boolean increment(String s)
	{
		status++;
		return update(status, s);
	}

	public void update(String s)
	{
		b.setString(s);
	}

	public boolean update(int i)
	{
		return update(i, null);
	}

	public boolean update(int i, String s)
	{
		status = i;
		b.setValue(i);

		if(s != null)
			b.setString(s);

		return pressed;
	}

	public boolean isCanceled()
	{
		return pressed;
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			System.out.println(command);
			if(command.equals("stop"))
			{
				pressed = true;
			}
		}
		catch (Throwable ex)
		{
			Utils.catchException(ex);
		}
	}
}
