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

	JRadioButton cmgs = new JRadioButton("Convert cm/s/s to g's");
	JRadioButton gscm = new JRadioButton("Convert g's to cm/s/s");
	JRadioButton mult = new JRadioButton("Multiply by a constant");
	JRadioButton redigit = new JRadioButton("Redigitize");
	JRadioButton peapick = new JRadioButton("Pea picker");
	ButtonGroup group = new ButtonGroup();

	JFileChooser fcs = new JFileChooser();
	JFileChooser fcd = new JFileChooser();
	JLabel source = new JLabel("Source file or directory");
	JLabel dest = new JLabel("Destination file or directory");
	JLabel constant = new JLabel(" ");
	JTextField sourcef = new JTextField(50);
	JTextField destf = new JTextField(50);
	JTextField constantf = new JTextField(7);
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

		cmgs.addActionListener(this);
		gscm.addActionListener(this);
		mult.addActionListener(this);
		redigit.addActionListener(this);
		peapick.addActionListener(this);

		cmgs.setMnemonic(KeyEvent.VK_G);
		gscm.setMnemonic(KeyEvent.VK_C);
		mult.setMnemonic(KeyEvent.VK_M);
		redigit.setMnemonic(KeyEvent.VK_R);
		peapick.setMnemonic(KeyEvent.VK_P);

		group.add(cmgs);
		group.add(gscm);
		group.add(mult);
		group.add(redigit);
		group.add(peapick);

		sourceb.setActionCommand("source");
		sourceb.addActionListener(this);

		destb.setActionCommand("dest");
		destb.addActionListener(this);

		go.setActionCommand("go");
		go.addActionListener(this);

		constantf.setEnabled(false);
		constantf.setBackground(GUIUtils.bg);

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
		JPanel panel = new JPanel(new VariableGridLayout(VariableGridLayout.FIXED_NUM_COLUMNS, 5));
		panel.add(cmgs);
		panel.add(gscm);
		panel.add(mult);
		panel.add(redigit);
		panel.add(peapick);
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
		list.add(constant);

		temp = new Vector();
		temp.add(constantf);

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
				constantf.setText("");
				constantf.setBackground(Color.white);
				if(cmgs.isSelected() || gscm.isSelected())
				{
					constant.setText(" ");
					constantf.setEnabled(false);
					constantf.setBackground(GUIUtils.bg);
					if(cmgs.isSelected())
						pane.setText("This program converts a file containing a sequence of accelerations in units of cm/s/s into a file containing a sequence of accelerations in units of g.  The program simply divides each value of cm/s/s by 980.665 to obtain values in terms of g.  Both the input and output file or directory must be specified or selected using the browser.");
					else
						pane.setText("This program converts a file containing a sequence of accelerations in units of g into a file containing a sequence of accelerations in units of cm/s/s.  The program simply multiplies each value by 980.665 to obtain values in cm/s/s.  Both the input and output file or directory must be specified or selected using the browser.");
				}
				else if(mult.isSelected())
				{
					constant.setText("Constant");
					constantf.setEnabled(true);
					pane.setText("This program multiplies the values in a file by a user-specified constant.  Both the input and output file or directory must be specified or selected using the browser.  The constant is specified in the \"Constant\" field.");
				}
				else if(redigit.isSelected())
				{
					constant.setText("Digitization Interval (s)");
					constantf.setEnabled(true);
					pane.setText("This program converts a time file (a file containing paired time and acceleration values) into a file containing a sequence of acceleration values having a constant time spacing (digitization interval) using an interpolation algorithm.  The input and output files or directories must be specified or selected using the browser.  The digitization interval for the output file must be specified in the indicated field; any value can be selected by the user, but values of 0.01-0.05 generally are appropriate.  The output file is in the format necessary to run the other programs in this package, but if the original time file had units of g's, it will be necessary to convert to cm/s/s before running other Analysis.");
				}
				else if(peapick.isSelected())
				{
					constant.setText("Gs to truncate record");
					constantf.setEnabled(true);
					pane.setText("This program removes the points of a record from the beginning and end of the file that are less than the specified number of Gs. 50 points are added to each side for lead in time.");
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

				Double val = new Double(0);
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
					val = (Double)Utils.checkNum(constantf.getText(), "constant field", null, false, null, null, false, null, false);
					if(val == null) return;
					sel = 3;
					selStr = "Multiplication by " + constantf.getText();
				}
				else if(redigit.isSelected())
				{
					val = (Double)Utils.checkNum(constantf.getText(), "Digitization Interval field", null, false, null, new Double(0), false, null, false);
					if(val == null) return;
					sel = 4;
					selStr = "Redigitization to digitization interval of " + constantf.getText();
				}
				else if(peapick.isSelected())
				{
					val = (Double)Utils.checkNum(constantf.getText(), "pea picker field", null, false, null, new Double(0), false, null, false);
					if(val == null) return;
					sel = 5;
					selStr = "Pea picking";
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
					errors = runUtil(sel, s, d, skipLines, val.doubleValue());
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
					progFrame.show();

					File list[] = s.listFiles();
					prog.setMaximum(list.length - 1);
					for(int i = 0; i < list.length; i++)
					{
						prog.setString(list[i].getAbsolutePath());
						prog.setValue(i);
						prog.paintImmediately(0,0,prog.getWidth(),prog.getHeight());

						errors += runUtil(sel, list[i],  new File(d.getAbsolutePath() + System.getProperty("file.separator") + list[i].getName()), skipLines, val.doubleValue());
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

	private String runUtil(int sel, File s, File d, int skip, double var) throws IOException
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
				Utilities.Mult(data, o, var);
				break;
			case 4: // redigit
				err = Utilities.Redigitize(data, o, var);
				break;
			case 5: // peapick
				Utilities.Peapick(data, o, var * Analysis.Gcmss);
				break;
		}

		if(!err.equals(""))
			err = "<br>" + err + " in file " + s.getAbsolutePath() + ".\n";

		return err;
	}
}
