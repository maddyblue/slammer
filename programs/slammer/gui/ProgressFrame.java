/*
 * Copyright (c) 2004 Matt Jibson
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

/* $Id$ */

package slammer.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import slammer.*;

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

		setVisible(true);
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
