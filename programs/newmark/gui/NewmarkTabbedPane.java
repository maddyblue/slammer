/*
 * NewmarkTabbedPane.java - main tabbed pane
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
import javax.swing.border.*;
import newmark.*;

public class NewmarkTabbedPane extends JTabbedPane
{
	public JFrame parent;

	public GettingStartedPanel GettingStarted;
	public SelectRecordsPanel SelectRecords;
	public ParametersPanel Parameters;
	public RigidBlockSimplifiedPanel RigidBlockSimplified;
	public DecoupledSimplifiedPanel DecoupledSimplified;
	public ResultsPanel Results;
	public UtilitiesPanel Utilities;
	public RecordManagerPanel RecordManager;
	public AddRecordsPanel AddRecords;

	public static Help help = null;

	JTabbedPane Rigorous = new JTabbedPane();
	JTabbedPane Simplified = new JTabbedPane();
	JTabbedPane Manager = new JTabbedPane();

	public NewmarkTabbedPane(JFrame parent) throws Exception
	{
		GettingStarted = new GettingStartedPanel(this);
		SelectRecords = new SelectRecordsPanel(this);
		Utilities = new UtilitiesPanel(this);
		RigidBlockSimplified = new RigidBlockSimplifiedPanel(this);
		DecoupledSimplified = new DecoupledSimplifiedPanel(this);
		Results = new ResultsPanel(this);
		Parameters = new ParametersPanel(this);
		RecordManager = new RecordManagerPanel(this);
		AddRecords = new AddRecordsPanel(this);

		if(help == null)
		{
			help = new Help();
		}

		Rigorous.addTab("Step 1: Select Records", SelectRecords);
		Rigorous.addTab("Step 2: Perform Analysis", Parameters);
		Rigorous.addTab("Step 3: View Results", Results);

		Simplified.addTab("Rigid-Block Analysis", RigidBlockSimplified);
		Simplified.addTab("Decoupled Analysis", DecoupledSimplified);

		Manager.addTab("Manage Records", RecordManager);
		Manager.addTab("Add Records", AddRecords);

		addTab("Getting Started", GettingStarted);
		addTab("Rigorous Analysis", Rigorous);
		addTab("Simplified Analyses", Simplified);
		addTab("Record Manager", Manager);
		addTab("Utilities", Utilities);
		addTab("Help", null);

		addChangeListener(new TabbedListener());
	}

	class TabbedListener implements ChangeListener
	{
		int last = 0;
		public void stateChanged(ChangeEvent e)
		{
			JTabbedPane t = (JTabbedPane)e.getSource();
			if(t.getSelectedIndex() == (t.getTabCount() - 1))
			{
				t.setSelectedIndex(last);
				NewmarkTabbedPane.help.show();
			}
			last = t.getSelectedIndex();
		}
	}


	public void selectSelectRecords()
	{
		Rigorous.setSelectedIndex(0);
		setSelectedIndex(1);
	}

	public void selectParameters()
	{
		Rigorous.setSelectedIndex(1);
		setSelectedIndex(1);
	}

	public void selectRigorousRigidBlock()
	{
		Rigorous.setSelectedIndex(2);
		setSelectedIndex(1);
	}

	public void incrementIndex()
	{
		setSelectedIndex(getSelectedIndex() + 1);
	}
}
