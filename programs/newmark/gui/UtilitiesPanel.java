/*
 * UtilitiesPanel.java - the panel containing various record utilities
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
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Vector;
import java.io.*;
import newmark.*;
import newmark.analysis.*;

class UtilitiesPanel extends JPanel implements ActionListener
{
	NewmarkTabbedPane parent;

	JRadioButton cmgs = new JRadioButton("<html>Convert cm/s<sup>2</sup> to g's</html>");
	JRadioButton gscm = new JRadioButton("<html>Convert g's to cm/s<sup>2</sup></html>");
	JRadioButton mult = new JRadioButton("Multiply by a constant");
	JRadioButton redigit = new JRadioButton("Redigitize");
	JRadioButton peapick = new JRadioButton("Trim records");
	JRadioButton clip = new JRadioButton("Clip records");
	ButtonGroup group = new ButtonGroup();

	JFileChooser fcs = new JFileChooser();
	JFileChooser fcd = new JFileChooser();
	JLabel source = new JLabel("Source file or directory");
	JLabel dest = new JLabel("Destination file or directory");
	JLabel constant1 = new JLabel(" ");
	JLabel constant1Pre = new JLabel("");
	JLabel constant1Post = new JLabel("");
	JLabel constant2 = new JLabel(" ");
	JLabel constant2Pre = new JLabel("");
	JLabel constant2Post = new JLabel("");
	JTextField sourcef = new JTextField(50);
	JTextField destf = new JTextField(50);
	JTextField constant1f = new JTextField(5);
	JTextField constant2f = new JTextField(5);
	JButton sourceb = new JButton("Browse...");
	JButton destb = new JButton("Browse...");
	JButton go = new JButton("Execute");
	JTextField skip = new JTextField("0", 4);
	JEditorPane pane = new JEditorPane();

	JTextField headerField = new JTextField();

	public UtilitiesPanel(NewmarkTabbedPane parent)
	{
		this.parent = parent;

		fcs.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fcd.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		cmgs.setActionCommand("change");
		gscm.setActionCommand("change");
		mult.setActionCommand("change");
		redigit.setActionCommand("change");
		peapick.setActionCommand("change");
		clip.setActionCommand("change");

		cmgs.addActionListener(this);
		gscm.addActionListener(this);
		mult.addActionListener(this);
		redigit.addActionListener(this);
		peapick.addActionListener(this);
		clip.addActionListener(this);

		group.add(cmgs);
		group.add(gscm);
		group.add(mult);
		group.add(redigit);
		group.add(peapick);
		group.add(clip);

		sourceb.setActionCommand("source");
		sourceb.addActionListener(this);

		destb.setActionCommand("dest");
		destb.addActionListener(this);

		go.setActionCommand("go");
		go.addActionListener(this);

		constant1f.setEnabled(false);
		constant2f.setEnabled(false);

		pane.setEditable(false);
		pane.setContentType("text/html");

		setLayout(new BorderLayout());

		JPanel utilitiesPanelTemp = new JPanel(new BorderLayout());
		utilitiesPanelTemp.add(BorderLayout.NORTH, createUtilitiesPanelHeader());
		utilitiesPanelTemp.add(BorderLayout.WEST, createUtilitiesPanelWest());

		add(BorderLayout.NORTH, utilitiesPanelTemp);
		add(BorderLayout.CENTER, pane);
	}

	private JPanel createUtilitiesPanelHeader()
	{
		JPanel panel = new JPanel(new GridLayout(0, 1));

		JPanel top = new JPanel();
		top.add(cmgs);
		top.add(gscm);
		top.add(mult);

		JPanel bottom = new JPanel();
		bottom.add(redigit);
		bottom.add(peapick);
		bottom.add(clip);

		panel.add(top);
		panel.add(bottom);

		return panel;
	}

	private JPanel createUtilitiesPanelWest()
	{
		Vector list = new Vector();
		Vector temp;
		Vector line;

		list.add(new JLabel(" "));
		list.add(source);

		line = new Vector();
		line.add(sourcef);
		line.add(sourceb);

		list.add(GUIUtils.makeRecursiveLayoutRight(line));
		list.add(new JLabel(" "));
		list.add(dest);

		line = new Vector();
		line.add(destf);
		line.add(destb);

		list.add(GUIUtils.makeRecursiveLayoutRight(line));
		list.add(new JLabel(" "));
		list.add(constant1);

		temp = new Vector();
		temp.add(constant1Pre);
		temp.add(constant1f);
		temp.add(constant1Post);

		list.add(GUIUtils.makeRecursiveLayoutRight(temp));
		list.add(new JLabel(" "));

		list.add(new JLabel(" "));
		list.add(constant2);

		temp = new Vector();
		temp.add(constant2Pre);
		temp.add(constant2f);
		temp.add(constant2Post);

		list.add(GUIUtils.makeRecursiveLayoutRight(temp));
		list.add(new JLabel(" "));

		line = new Vector();
		line.add(new JLabel("Skip the first "));
		line.add(skip);
		line.add(new JLabel(" lines of the source file (use this to skip header data)."));

		list.add(GUIUtils.makeRecursiveLayoutRight(line));
		list.add(new JLabel(" "));
		list.add(go);
		list.add(new JLabel(" "));

		return GUIUtils.makeRecursiveLayoutDown(list);
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			System.out.println(command);
			if(command.equals("dest"))
			{
				if(fcd.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
				{
					destf.setText(fcd.getSelectedFile().getAbsolutePath());
				}
			}
			else if(command.equals("source"))
			{
				if(fcs.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
				{
					sourcef.setText(fcs.getSelectedFile().getAbsolutePath());
				}
			}
			else if(command.equals("change"))
			{
				constant1f.setText("");
				constant1f.setEnabled(false);
				constant1.setText(" ");
				constant1Pre.setText("");
				constant1Post.setText("");
				constant2f.setText("");
				constant2f.setEnabled(false);
				constant2.setText(" ");
				constant2Pre.setText("");
				constant2Post.setText("");
				if(cmgs.isSelected() || gscm.isSelected())
				{
					if(cmgs.isSelected())
						pane.setText("This program converts a file containing a sequence of accelerations in units of cm/s/s into a file containing a sequence of accelerations in units of g.  The program simply divides each value of cm/s/s by 980.665 to obtain values in terms of g.  Both the input and output file or directory must be specified or selected using the browser.");
					else
						pane.setText("This program converts a file containing a sequence of accelerations in units of g into a file containing a sequence of accelerations in units of cm/s/s.  The program simply multiplies each value by 980.665 to obtain values in cm/s/s.  Both the input and output file or directory must be specified or selected using the browser.");
				}
				else if(mult.isSelected())
				{
					constant1.setText("Constant");
					constant1f.setEnabled(true);
					pane.setText("This program multiplies the values in a file by a user-specified constant.  Both the input and output file or directory must be specified or selected using the browser.  The constant is specified in the \"Constant\" field.");
				}
				else if(redigit.isSelected())
				{
					constant1.setText("Digitization Interval (s)");
					constant1f.setEnabled(true);
					pane.setText("This program converts a time file (a file containing paired time and acceleration values) into a file containing a sequence of acceleration values having a constant time spacing (digitization interval) using an interpolation algorithm.  The input and output files or directories must be specified or selected using the browser.  The digitization interval for the output file must be specified in the indicated field; any value can be selected by the user, but values of 0.01-0.05 generally are appropriate.  The output file is in the format necessary to run the other programs in this package, but if the original time file had units of g's, it will be necessary to convert to cm/s/s before running other Analysis.");
				}
				else if(peapick.isSelected())
				{
					constant1Pre.setText("Trim record between g values of ");
					constant1f.setEnabled(true);
					pane.setText("This program removes the points of a record from the beginning and end of the file that are less than the specified number of Gs. 50 points are added to each side for lead in time.");
				}
				else if(clip.isSelected())
				{
					constant1Pre.setText("Clip records at ");
					constant1Post.setText(" seconds");
					constant1f.setEnabled(true);
					constant2.setText("Digitization Interval (s)");
					constant2f.setEnabled(true);
					pane.setText("<html>This program removes all data <b>after</b> the specified time from each file. If the file is shorter than the clip location, the file will simply be copied to the destination." +
						"<p>Using a <i>negative</i> value for the clip time will remove all data <b>before</b> the specified time. IE: to cut off the first two seconds but keep all data after two seconds, enter -2 as the clip time. If the clip time is longer than the file duration (ie: trying to remove the first 5 seconds of a 4 second file), it will be truncated to zero length.</html>"
					);
				}
			}
			else if(command.equals("go"))
			{
				String temp;
				File s, d;

				temp = sourcef.getText();
				if(temp == null || temp.equals(""))
				{
					GUIUtils.popupError("No source specified.");
					return;
				}

				temp = destf.getText();
				if(temp == null || temp.equals(""))
				{
					GUIUtils.popupError("No destination specified.");
					return;
				}

				s = new File(sourcef.getText());
				if(!s.exists() || !s.canRead())
				{
					GUIUtils.popupError(s.getAbsolutePath() + " does not exist or is not readable.");
					return;
				}

				if(!s.isFile() && !s.isDirectory())
				{
					GUIUtils.popupError(s.getAbsolutePath() + " is invalid.");
					return;
				}

				d = new File(destf.getText());

				if(s.isDirectory() && d.isFile())
				{
					GUIUtils.popupError("If the source is a directory the destination must also be a directory.");
					return;
				}
				else if(s.isFile() && d.isDirectory())
				{
					GUIUtils.popupError("If the source is a file the destination must also be a file.");
					return;
				}

				temp = skip.getText();
				int skipLines;
				if(temp == null || temp.equals(""))
					skipLines = 0;
				else
				{
					Double doub = (Double)Utils.checkNum(temp, "skip lines field", null, false, null, new Double(0), true, null, false);
					if(doub == null) return;
					skipLines = (int)doub.doubleValue();
				}

				Double val1 = new Double(0);
				Double val2 = new Double(0);
				int sel;
				String selStr;

				if(cmgs.isSelected())
				{
					sel = 1;
					selStr = "Conversion from cm/s/s to g's";
				}
				else if(gscm.isSelected())
				{
					sel = 2;
					selStr = "Conversion from g's to cm/s/s";
				}
				else if(mult.isSelected())
				{
					val1 = (Double)Utils.checkNum(constant1f.getText(), "constant field", null, false, null, null, false, null, false);
					if(val1 == null) return;
					sel = 3;
					selStr = "Multiplication by " + constant1f.getText();
				}
				else if(redigit.isSelected())
				{
					val1 = (Double)Utils.checkNum(constant1f.getText(), "Digitization Interval field", null, false, null, new Double(0), false, null, false);
					if(val1 == null) return;
					sel = 4;
					selStr = "Redigitization to digitization interval of " + constant1f.getText();
				}
				else if(peapick.isSelected())
				{
					val1 = (Double)Utils.checkNum(constant1f.getText(), "pea picker field", null, false, null, new Double(0), false, null, false);
					if(val1 == null) return;
					sel = 5;
					selStr = "Pea picking";
				}
				else if(clip.isSelected())
				{
					val1 = (Double)Utils.checkNum(constant1f.getText(), "clip time field", null, false, null, null, false, null, false);
					if(val1 == null) return;
					val2 = (Double)Utils.checkNum(constant2f.getText(), "digitization interval field", null, false, null, null, false, null, false);
					if(val2 == null) return;
					sel = 6;
					selStr = "Time clip";
				}
				else
				{
					GUIUtils.popupError("No utilitiy selected.");
					return;
				}

				int ret;
				String errors = "";

				if(s.isFile())
				{
					errors = runUtil(sel, s, d, skipLines, val1.doubleValue(), val2.doubleValue());
				}
				else if(s.isDirectory())
				{
					JProgressBar prog = new JProgressBar();
					prog.setStringPainted(true);
					prog.setMinimum(0);

					JFrame progFrame = new JFrame("Progress...");
					progFrame.getContentPane().add(prog);
					progFrame.setSize(600, 75);
					GUIUtils.setLocationMiddle(progFrame);
					progFrame.setVisible(true);

					File list[] = s.listFiles();
					prog.setMaximum(list.length - 1);
					for(int i = 0; i < list.length; i++)
					{
						prog.setString(list[i].getAbsolutePath());
						prog.setValue(i);
						prog.paintImmediately(0,0,prog.getWidth(),prog.getHeight());

						errors += runUtil(sel, list[i],  new File(d.getAbsolutePath() + System.getProperty("file.separator") + list[i].getName()), skipLines, val1.doubleValue(), val2.doubleValue());
					}
					progFrame.dispose();
				}
				else // ...uh?
				{
					GUIUtils.popupError("Error with source.");
					return;
				}

				pane.setText(selStr + " on " + d.getAbsolutePath() + (errors.equals("") ? " complete." : (" NOT complete.<p>Errors:" + errors)));
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	private String runUtil(int sel, File s, File d, int skip, double var1, double var2) throws IOException
	{
		DoubleList data = new DoubleList(s.getAbsolutePath(), skip);
		if(data.bad())
		{
			return ("<br>After skipping " + skip + " lines, invalid data encountered in file " + s.getAbsolutePath() + " at point " + data.badEntry() + ".");
		}

		FileWriter o = new FileWriter(d);
		String err = "";

		switch(sel)
		{
			case 1: // cmgs
				Utilities.CM_GS(data, o);
				break;
			case 2: // gscm
				Utilities.GS_CM(data, o);
				break;
			case 3: // mult
				Utilities.Mult(data, o, var1);
				break;
			case 4: // redigit
				err = Utilities.Redigitize(data, o, var1);
				break;
			case 5: // peapick
				Utilities.Peapick(data, o, var1 * Analysis.Gcmss);
				break;
			case 6: // clip
				Utilities.Clip(data, o, var1, var2);
		}

		if(!err.equals(""))
			err = "<br>" + err + " in file " + s.getAbsolutePath() + ".\n";

		return err;
	}
}
