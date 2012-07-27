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
					NodeData n = (NodeData)nodeInfo;
					displayURL(n.url);
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

	public class NodeData
	{
		public URL url;
		public String name;

		public NodeData(String n, String u)
		{
			name = n;

			try
			{
				url = new URL(prefix + u);
			}
			catch (java.net.MalformedURLException exc)
			{
				System.err.println("Attempted to create a Node "
				+ "with a bad URL: " + url);
				url = null;
			}
		}

		public String toString()
		{
			return name;
		}
	}

	private class Node extends DefaultMutableTreeNode
	{
		public Node(String s, String filename)
		{
			super(new NodeData(s, filename));
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
		Node category = null;
		DefaultMutableTreeNode book = null;
		Node section = null;

		category = new Node("General Information", "program/introduction.html");
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
			("Earthquake Data",
			"program/eqdata.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Navigating the Program Pages",
			"program/nav.html"));
			category.add(book);

		category = new Node("Program Pages", "help/gettingStarted.html");
		top.add(category);

			book = new DefaultMutableTreeNode(new BookInfo
			("Getting Started",
			"help/gettingStarted.html"));
			category.add(book);

			section = new Node("Rigorous Analyses", "help/rigorous.html");
			category.add(section);

				book = new DefaultMutableTreeNode(new BookInfo
				("Step 1: Select records",
				"help/rigorous.html#step1"));
				section.add(book);

				book = new DefaultMutableTreeNode(new BookInfo
				("Step 2: Select analyses",
				"help/rigorous.html#step2"));
				section.add(book);

				book = new DefaultMutableTreeNode(new BookInfo
				("Step 3: Perform analyses and view results",
				"help/rigorous.html#step3"));
				section.add(book);

			section = new Node("Simplified Empirical Models", "help/simplifiedAnalyses.html");
			category.add(section);

				book = new DefaultMutableTreeNode(new BookInfo
				("Rigid",
				"help/simplifiedAnalyses.html#rigid"));
				section.add(book);

				book = new DefaultMutableTreeNode(new BookInfo
				("Flexible (coupled)",
				"help/simplifiedAnalyses.html#coupled"));
				section.add(book);

				book = new DefaultMutableTreeNode(new BookInfo
				("Flexible/Rigid (unified)",
				"help/simplifiedAnalyses.html#unified"));
				section.add(book);

				book = new DefaultMutableTreeNode(new BookInfo
				("Probability of failure",
				"help/simplifiedAnalyses.html#failure"));
				section.add(book);

			section = new Node("Manage/Add records", "help/recordManager.html");
			category.add(section);

				book = new DefaultMutableTreeNode(new BookInfo
				("Manage records",
				"help/recordManager.html#manage"));
				section.add(book);

				book = new DefaultMutableTreeNode(new BookInfo
				("Add records",
				"help/recordManager.html#add"));
				section.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Utilities",
			"help/utilities.html"));
			category.add(book);

		category = new Node("Definition of Terms", "help/terms.html");
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

		book = new DefaultMutableTreeNode(new BookInfo
		("References",
		"program/references.html"));
		top.add(book);

	}
}
