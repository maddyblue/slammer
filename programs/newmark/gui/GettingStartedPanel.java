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

/* $Id$ */

package newmark.gui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import newmark.*;

class GettingStartedPanel extends JPanel
{
	NewmarkTabbedPane parent;

	JEditorPane startedPane = new JEditorPane();

	public GettingStartedPanel(NewmarkTabbedPane parent) throws Exception
	{
		this.parent = parent;

		startedPane.setPage(new URL(Help.prefix + "program/gettingStarted.html"));

		setLayout(new BorderLayout());

		add(BorderLayout.CENTER, new JScrollPane(startedPane));
	}
}
