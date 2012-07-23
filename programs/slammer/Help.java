/* This file is in the public domain. */

package slammer;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeSelectionModel;
import java.net.URL;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JFrame;
import java.awt.*;
import java.awt.event.*;
import slammer.gui.*;

public class Help extends JFrame
{
	private JEditorPane htmlPane;
	private boolean DEBUG = false;
	private URL helpURL;

	private boolean playWithLineStyle = false;
	private String lineStyle = "Angled";

	public static final String prefix = "jar:file:slammer.jar!/help/";

	public Help()
	{
		super("Slammer Help");

		//Create the nodes.
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Help");
		createNodes(top);

		//Create a tree that allows one selection at a time.
		final JTree tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode
		(TreeSelectionModel.SINGLE_TREE_SELECTION);

		//Listen for when the selection changes.
		tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				tree.getLastSelectedPathComponent();

				if (node == null) return;

				Object nodeInfo = node.getUserObject();
				if (node.isLeaf())
				{
					BookInfo book = (BookInfo)nodeInfo;
					displayURL(book.bookURL);
				}
				else
				{
					displayURL(helpURL);
				}
			}
		});

		if (playWithLineStyle)
		{
			tree.putClientProperty("JTree.lineStyle", lineStyle);
		}

		//Create the scroll pane and add the tree to it.
		JScrollPane treeView = new JScrollPane(tree);

		//Create the HTML viewing pane.
		htmlPane = new JEditorPane();
		htmlPane.setEditable(false);
		initHelp();
		JScrollPane htmlView = new JScrollPane(htmlPane);

		//Add the scroll panes to a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setTopComponent(treeView);
		splitPane.setBottomComponent(htmlView);

		Dimension minimumSize = new Dimension(100, 50);
		htmlView.setMinimumSize(minimumSize);
		treeView.setMinimumSize(minimumSize);
		splitPane.setDividerLocation(175);

		splitPane.setPreferredSize(new Dimension(700, 500));

		//Add the split pane to this frame.
		getContentPane().add(splitPane, BorderLayout.CENTER);

		pack();
		setLocationRelativeTo(null);
	}

	private class BookInfo
	{
		public String bookName;
		public URL bookURL;
		public BookInfo(String book, String filename)
		{
			bookName = book;
			try
			{
				bookURL = new URL(prefix + filename);
			}
			catch (java.net.MalformedURLException exc)
			{
				System.err.println("Attempted to create a BookInfo "
				+ "with a bad URL: " + bookURL);
				bookURL = null;
			}
		}

		public String toString()
		{
			return bookName;
		}
	}

	private void initHelp()
	{
		String s = null;
		try
		{
			s = prefix + "program/introduction.html";
			helpURL = new URL(s);
			displayURL(helpURL);
		}
		catch (Exception e)
		{
			System.err.println("Couldn't create help URL: " + s);
		}
	}

	private void displayURL(URL url)
	{
		try
		{
			htmlPane.setPage(url);
		}
		catch (IOException e)
		{
			System.err.println("Attempted to read a bad URL: " + url);
		}
	}

	private void createNodes(DefaultMutableTreeNode top)
	{
		DefaultMutableTreeNode category = null;
		DefaultMutableTreeNode book = null;
		DefaultMutableTreeNode section = null;

		category = new DefaultMutableTreeNode("Help Files");
		top.add(category);

			book = new DefaultMutableTreeNode(new BookInfo
			("Introduction",
			"program/introduction.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Strong-Motion Records",
			"program/records.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("File Formats",
			"program/fileFormats.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Navigating the Program Pages",
			"program/nav.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Earthquake Data",
			"program/eqdata.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("References",
			"program/references.html"));
			category.add(book);

		category = new DefaultMutableTreeNode("Program Pages");
		top.add(category);

			book = new DefaultMutableTreeNode(new BookInfo
			("Getting Started",
			"help/gettingStarted.html"));
			category.add(book);

			section = new DefaultMutableTreeNode("Rigorous Rigid-Block Analysis");
			category.add(section);

				book = new DefaultMutableTreeNode(new BookInfo
				("Rigorous Analyses",
				"help/rigorous.html"));
				section.add(book);

				book = new DefaultMutableTreeNode(new BookInfo
				("Step1: Select Records",
				"help/rigorous.html#step1"));
				section.add(book);

				book = new DefaultMutableTreeNode(new BookInfo
				("Step2: Perform Rigid-Block Analysis",
				"help/rigorous.html#step2"));
				section.add(book);

				book = new DefaultMutableTreeNode(new BookInfo
				("Step3: View Results",
				"help/rigorous.html#step3"));
				section.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Simplified Analyses",
			"help/simplifiedAnalyses.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Record Manager",
			"help/recordManager.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Utilities",
			"help/utilities.html"));
			category.add(book);

		category = new DefaultMutableTreeNode("Definition of Terms");
		top.add(category);

		String[][] terms = {
			{ "Arias intensity", "ariasIntensity" },
			{ "Coupled analysis", "coupledAnalysis" },
			{ "Critical acceleration", "criticalAcceleration" },
			{ "Decoupled analysis", "decoupledAnalysis" },
			{ "Digitization interval", "digitizationInterval" },
			{ "Duration", "duration" },
			{ "Dynamic analysis", "dynamicAnalysis" },
			{ "Epicentral distance", "epicentralDistance" },
			{ "Equivalent-linear analysis", "equivalentLinearAnalysis" },
			{ "Focal distance", "focalDistance" },
			{ "Focal mechanism", "focalMechanism" },
			{ "Fundamental site period", "fundamentalSitePeriod" },
			{ "<html>k<sub>max</sub>", "kMax" },
			{ "Linear elastic analysis", "linearElasticAnalysis" },
			{ "Mean shaking period", "meanShakingPeriod" },
			{ "Newmark analysis", "newmarkAnalysis" },
			{ "Newmark displacement", "newmarkDisplacement" },
			{ "Peak ground acceleration", "peakGroundAcceleration" },
			{ "Peak ground velocity", "peakGroundVelocity" },
			{ "Period ratio", "periodRatio" },
			{ "Reference strain", "referenceStrain" },
			{ "Rigid-block analysis", "rigidBlockAnalysis" },
			{ "Rupture distance", "ruptureDistance" },
			{ "Shear-wave velocity", "shearWaveVelocity" },
			{ "Site classification", "siteClassification" },
			{ "Site period", "sitePeriod" },
			{ "Spectral acceleration", "spectralAcceleration" },
			{ "Thrust angle", "thrustAngle" },
			{ "<html>Vs<sup>30</sup>", "vs30" },
			{ "Yield coefficient", "yieldCoefficient" },
		};

		for(int i = 0; i < terms.length; i++)
		{
			book = new DefaultMutableTreeNode(new BookInfo(terms[i][0], "help/terms.html#" + terms[i][1]));
			category.add(book);
		}
	}
}
