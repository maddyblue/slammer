/*
 * Utils.java - random utilities
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

package newmark;

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
import newmark.gui.*;

public class Help extends JFrame
{
	private JEditorPane htmlPane;
	private boolean DEBUG = false;
	private URL helpURL;

	private boolean playWithLineStyle = false;
	private String lineStyle = "Angled";

	public Help()
	{
		super("Newmark Help");

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
					if (DEBUG)
					{
						System.out.print(book.bookURL + ":  \n    ");
					}
				}
				else
				{
					displayURL(helpURL);
				}
				if (DEBUG)
				{
					System.out.println(nodeInfo.toString());
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

		splitPane.setPreferredSize(new Dimension(500, 300));

		//Add the split pane to this frame.
		getContentPane().add(splitPane, BorderLayout.CENTER);

		pack();
		GUIUtils.setLocationMiddle(this);
	}

	private class BookInfo
	{
		public String bookName;
		public URL bookURL;
		public String prefix = "file:"
		+ System.getProperty("user.dir")
		+ System.getProperty("file.separator");
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
			s = "file:"
			+ System.getProperty("user.dir")
			+ System.getProperty("file.separator")
			+ "help/program/introduction.html";
			if (DEBUG)
			{
				System.out.println("Help URL is " + s);
			}
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
			"help/program/introduction.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("File Formats",
			"help/program/fileFormats.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Navigating the Program Pages",
			"help/program/nav.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Earthquake Data",
			"help/program/eqdata.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Adding Earthquake Sets From CD",
			"help/program/eqsets.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Acknowledgements",
			"help/program/acknowledgements.html"));
			category.add(book);

		category = new DefaultMutableTreeNode("Program Pages");
		top.add(category);

			book = new DefaultMutableTreeNode(new BookInfo
			("Getting Started",
			"help/help/gettingStarted.html"));
			category.add(book);

			section = new DefaultMutableTreeNode("Rigorous Rigid-Block Analysis");
			category.add(section);

				book = new DefaultMutableTreeNode(new BookInfo
				("Rigorous Rigid-Block Analysis",
				"help/help/rigorousRigidBlockAnalysis.html"));
				section.add(book);

				book = new DefaultMutableTreeNode(new BookInfo
				("Step1: Select Records",
				"help/help/rigorousRigidBlockAnalysis.html#step1"));
				section.add(book);

				book = new DefaultMutableTreeNode(new BookInfo
				("Step2: Perform Rigid-Block Analysis",
				"help/help/rigorousRigidBlockAnalysis.html#step2"));
				section.add(book);

				book = new DefaultMutableTreeNode(new BookInfo
				("Step3: View Results",
				"help/help/rigorousRigidBlockAnalysis.html#step3"));
				section.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Simplified Analyses",
			"help/help/simplifiedAnalyses.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Utilities",
			"help/help/utilities.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Record Manager",
			"help/help/recordManager.html"));
			category.add(book);

		category = new DefaultMutableTreeNode("Definition of Terms");
		top.add(category);

			book = new DefaultMutableTreeNode(new BookInfo
			("Arias Intensity",
			"help/terms/ariasIntensity.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Coupled Analysis",
			"help/terms/coupledAnalysis.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Critical Acceleration",
			"help/terms/criticalAcceleration.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Decoupled Analysis",
			"help/terms/decoupledAnalysis.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Digitization Interval",
			"help/terms/digint.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Dobry Duration",
			"help/terms/dobryDuration.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Epicentral Distance",
			"help/terms/epiDist.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Focal Distance",
			"help/terms/focDist.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Mean Period",
			"help/terms/meanPeriod.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Newmark Analysis",
			"help/terms/newmarkAnalysis.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Newmark Displacement",
			"help/terms/newmarkDisplacement.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Peak Ground Acceleration",
			"help/terms/pga.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Rigid-Block Analysis",
			"help/terms/rigidBlockAnalysis.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Rupture Distance",
			"help/terms/rupDist.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Thrust Angle",
			"help/terms/thrustAngle.html"));
			category.add(book);

		category = new DefaultMutableTreeNode("Newmark Documentation");
		top.add(category);

			book = new DefaultMutableTreeNode(new BookInfo
			("Title",
			"help/science/newmark.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Abstract",
			"help/science/newmark.html#ABSTRACT"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Introduction",
			"help/science/newmark.html#INTRODUCTION"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Past Applications Of Newmark's Method",
			"help/science/newmark.html#PAST APPLICATIONS OF NEWMARK'S METHOD"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Types of Sliding-Block Analysis Currently in Use",
			"help/science/newmark.html#TYPES OF SLIDING-BLOCK ANALYSIS CURRENTLY IN USE"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Which Analysis Should Be Used?",
			"help/science/newmark.html#WHICH ANALYSIS SHOULD BE USED?"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Conducting A Newmark Analysis",
			"help/science/newmark.html#CONDUCTING A NEWMARK ANALYSIS"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("A Simplified Newmark Method",
			"help/science/newmark.html#A SIMPLIFIED NEWMARK METHOD"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Interpreting Newmark Displacements",
			"help/science/newmark.html#INTERPRETING NEWMARK DISPLACEMENTS"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Discussion",
			"help/science/newmark.html#DISCUSSION"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Conclusion",
			"help/science/newmark.html#CONCLUSION"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("References",
			"help/science/newmark.html#REFERENCES"));
			category.add(book);
	}
}