/*
 * GettingStartedPanel.java
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

/* $Id: GettingStartedPanel.java,v 1.1 2003/06/15 01:58:11 dolmant Exp $ */

package newmark.gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import newmark.*;

class GettingStartedPanel extends JPanel
{
	NewmarkTabbedPane parent;

	JEditorPane startedPane = new JEditorPane();

	public GettingStartedPanel(NewmarkTabbedPane parent) throws Exception
	{
		this.parent = parent;

		FileReader read = new FileReader("help/program/gettingStarted.html");
		String str = "";
		while(read.ready())
			str += (char)read.read();
		read.close();

		startedPane.setEditable(false);
		startedPane.setContentType("text/html");
		startedPane.setText(str);

		setLayout(new BorderLayout());

		add(BorderLayout.CENTER, new JScrollPane(startedPane));
	}
}
