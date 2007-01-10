/*
 * Copyright (c) 2002 Matt Jibson
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
import javax.swing.border.*;
import slammer.*;

public class SlammerTabbedPane extends JTabbedPane
{
	public JFrame parent;

	public GettingStartedPanel GettingStarted;
	public SelectRecordsPanel SelectRecords;
	public ParametersPanel Parameters;
	public RigidBlockSimplifiedPanel RigidBlockSimplified;
	public DecoupledSimplifiedPanel DecoupledSimplified;
	public ResultsPanel Results;
	public WhichAnalysisPanel WhichAnalysis;
	public UtilitiesPanel Utilities;
	public RecordManagerPanel RecordManager;
	public AddRecordsPanel AddRecords;

	public static Help help = new Help();

	JTabbedPane Rigorous = new JTabbedPane();
	JTabbedPane Simplified = new JTabbedPane();
	JTabbedPane Manager = new JTabbedPane();

	public SlammerTabbedPane(JFrame parent) throws Exception
	{
		GettingStarted = new GettingStartedPanel(this);
		SelectRecords = new SelectRecordsPanel(this);
		Utilities = new UtilitiesPanel(this);
		RigidBlockSimplified = new RigidBlockSimplifiedPanel(this);
		DecoupledSimplified = new DecoupledSimplifiedPanel(this);
		Results = new ResultsPanel(this);
		WhichAnalysis = new WhichAnalysisPanel(this);
		Parameters = new ParametersPanel(this);
		RecordManager = new RecordManagerPanel(this);
		AddRecords = new AddRecordsPanel(this);

		Rigorous.addTab("Step 1: Select Records", SelectRecords);
		Rigorous.addTab("Step 2: Select Analyses", Parameters);
		Rigorous.addTab("Step 3: Perform Analyses and View Results", Results);
		Rigorous.addTab("Appendix: Which Analysis Should I Use?", WhichAnalysis);

		Simplified.addTab("Rigid-Block Analysis", RigidBlockSimplified);
		Simplified.addTab("Decoupled Analysis", DecoupledSimplified);

		Manager.addTab("Manage Records", RecordManager);
		Manager.addTab("Add Records", AddRecords);

		addTab("Getting Started", GettingStarted);
		addTab("Rigorous Analyses", Rigorous);
		addTab("Simplified Analyses", Simplified);
		addTab("Manage/Add Records", Manager);
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
				SlammerTabbedPane.help.setVisible(true);
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
