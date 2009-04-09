/* This file is in the public domain. */

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
	public ProbFailPanel ProbFail;
	public ResultsPanel Results;
	public WhichAnalysisPanel WhichAnalysis;
	public UtilitiesPanel Utilities;
	public RecordManagerPanel RecordManager;
	public AddRecordsPanel AddRecords;
	public Help help;

	JTabbedPane Rigorous;
	JTabbedPane Simplified;
	JTabbedPane Manager;

	public SlammerTabbedPane(JFrame parent, boolean isSlammer) throws Exception
	{
		SelectRecords = new SelectRecordsPanel(this, isSlammer);
		Utilities = new UtilitiesPanel(this);
		RecordManager = new RecordManagerPanel(this);
		AddRecords = new AddRecordsPanel(this);

		if(isSlammer)
		{
			GettingStarted = new GettingStartedPanel(this);
			RigidBlockSimplified = new RigidBlockSimplifiedPanel(this);
			DecoupledSimplified = new DecoupledSimplifiedPanel(this);
			ProbFail = new ProbFailPanel(this);
			Results = new ResultsPanel(this);
			WhichAnalysis = new WhichAnalysisPanel(this);
			Parameters = new ParametersPanel(this);

			Rigorous = new JTabbedPane();
			Simplified = new JTabbedPane();
			Manager = new JTabbedPane();

			Rigorous.addTab("Step 1: Select Records", SelectRecords);
			Rigorous.addTab("Step 2: Select Analyses", Parameters);
			Rigorous.addTab("Step 3: Perform Analyses and View Results", Results);
			Rigorous.addTab("Appendix: Which Analysis Should I Use?", WhichAnalysis);

			Simplified.addTab("Rigid-Block Analyses", RigidBlockSimplified);
			Simplified.addTab("Decoupled Analysis", DecoupledSimplified);
			Simplified.addTab("Probability of Failure", ProbFail);

			Manager.addTab("Manage Records", RecordManager);
			Manager.addTab("Add Records", AddRecords);

			addTab("Getting Started", GettingStarted);
			addTab("Rigorous Analyses", Rigorous);
			addTab("Simplified Analyses", Simplified);
			addTab("Manage/Add Records", Manager);
			addTab("Utilities", Utilities);
			addTab("Help", null);

			help = new Help();
			addChangeListener(new TabbedListener());
		}
		else // isSRM
		{
			addTab("Search", SelectRecords);
			addTab("Manage/View", RecordManager);
			addTab("Add", AddRecords);
			addTab("Utilities", Utilities);
		}
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
				help.setVisible(true);
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
