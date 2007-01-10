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

	public static final String prefix = "jar:file:newmark.jar!/help/";

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

		splitPane.setPreferredSize(new Dimension(700, 500));

		//Add the split pane to this frame.
		getContentPane().add(splitPane, BorderLayout.CENTER);

		pack();
		GUIUtils.setLocationMiddle(this);
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
			"program/introduction.html"));
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
			("Adding Earthquake Sets From CD",
			"program/eqsets.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Acknowledgements",
			"program/acknowledgements.html"));
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
				("Rigorous Rigid-Block Analysis",
				"help/rigorousRigidBlockAnalysis.html"));
				section.add(book);

				book = new DefaultMutableTreeNode(new BookInfo
				("Step1: Select Records",
				"help/rigorousRigidBlockAnalysis.html#step1"));
				section.add(book);

				book = new DefaultMutableTreeNode(new BookInfo
				("Step2: Perform Rigid-Block Analysis",
				"help/rigorousRigidBlockAnalysis.html#step2"));
				section.add(book);

				book = new DefaultMutableTreeNode(new BookInfo
				("Step3: View Results",
				"help/rigorousRigidBlockAnalysis.html#step3"));
				section.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Simplified Analyses",
			"help/simplifiedAnalyses.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Utilities",
			"help/utilities.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Record Manager",
			"help/recordManager.html"));
			category.add(book);

		category = new DefaultMutableTreeNode("Definition of Terms");
		top.add(category);

			book = new DefaultMutableTreeNode(new BookInfo
			("Arias Intensity",
			"terms/ariasIntensity.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Coupled Analysis",
			"terms/coupledAnalysis.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Critical Acceleration",
			"terms/criticalAcceleration.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Decoupled Analysis",
			"terms/decoupledAnalysis.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Digitization Interval",
			"terms/digint.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Dobry Duration",
			"terms/dobryDuration.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Epicentral Distance",
			"terms/epiDist.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Focal Distance",
			"terms/focDist.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Mean Period",
			"terms/meanPeriod.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Newmark Analysis",
			"terms/newmarkAnalysis.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Newmark Displacement",
			"terms/newmarkDisplacement.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Peak Ground Acceleration",
			"terms/pga.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Rigid-Block Analysis",
			"terms/rigidBlockAnalysis.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Rupture Distance",
			"terms/rupDist.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Thrust Angle",
			"terms/thrustAngle.html"));
			category.add(book);

		category = new DefaultMutableTreeNode("Newmark Documentation");
		top.add(category);

			book = new DefaultMutableTreeNode(new BookInfo
			("Title",
			"science/newmark.html"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Abstract",
			"science/newmark.html#ABSTRACT"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Introduction",
			"science/newmark.html#INTRODUCTION"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Past Applications Of Newmark's Method",
			"science/newmark.html#PAST APPLICATIONS OF NEWMARK'S METHOD"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Types of Sliding-Block Analysis Currently in Use",
			"science/newmark.html#TYPES OF SLIDING-BLOCK ANALYSIS CURRENTLY IN USE"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Which Analysis Should Be Used?",
			"science/newmark.html#WHICH ANALYSIS SHOULD BE USED?"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Conducting A Newmark Analysis",
			"science/newmark.html#CONDUCTING A NEWMARK ANALYSIS"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("A Simplified Newmark Method",
			"science/newmark.html#A SIMPLIFIED NEWMARK METHOD"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Interpreting Newmark Displacements",
			"science/newmark.html#INTERPRETING NEWMARK DISPLACEMENTS"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Discussion",
			"science/newmark.html#DISCUSSION"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("Conclusion",
			"science/newmark.html#CONCLUSION"));
			category.add(book);

			book = new DefaultMutableTreeNode(new BookInfo
			("References",
			"science/newmark.html#REFERENCES"));
			category.add(book);
	}
}