/*
 * Originally written by Matt Jibson for the SLAMMER project. This work has been
 * placed into the public domain. You may use this work in any way and for any
 * purpose you wish.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package slammer.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Vector;
import java.io.*;
import slammer.*;
import slammer.analysis.*;

class UtilitiesPanel extends JPanel implements ActionListener
{
	public final static int SELECT_CMGS    = 1;
	public final static int SELECT_GSCM    = 2;
	public final static int SELECT_MULT    = 3;
	public final static int SELECT_REDIGIT = 4;
	public final static int SELECT_BRACKET = 5;
	public final static int SELECT_TRIM    = 6;

	SlammerTabbedPane parent;

	JRadioButton cmgs = new JRadioButton("<html>Convert cm/s<sup>2</sup> to g's</html>");
	JRadioButton gscm = new JRadioButton("<html>Convert g's to cm/s<sup>2</sup></html>");
	JRadioButton mult = new JRadioButton("Multiply by a constant");
	JRadioButton redigit = new JRadioButton("Redigitize");
	JRadioButton peapick = new JRadioButton("Bracket records");
	JRadioButton clip = new JRadioButton("Trim records");
	ButtonGroup group = new ButtonGroup();

	JFileChooser fcs = new JFileChooser();
	JFileChooser fcd = new JFileChooser();
	JLabel source = new JLabel("Source file or directory");
	JLabel dest = new JLabel("Destination file or directory (leave blank to overwrite source file)");
	JLabel constant1 = new JLabel(" ");
	JLabel constant1Pre = new JLabel("");
	JLabel constant1Post = new JLabel("");
	JLabel constant2 = new JLabel(" ");
	JLabel constant2Pre = new JLabel("");
	JLabel constant2Post = new JLabel("");
	JLabel constant3 = new JLabel(" ");
	JLabel constant3Pre = new JLabel("");
	JLabel constant3Post = new JLabel("");
	JTextField sourcef = new JTextField(50);
	JTextField destf = new JTextField(50);
	JTextField constant1f = new JTextField(5);
	JTextField constant2f = new JTextField(5);
	JTextField constant3f = new JTextField(5);
	JButton sourceb = new JButton("Browse...");
	JButton destb = new JButton("Browse...");
	JButton go = new JButton("Execute");
	JTextField skip = new JTextField("0", 4);
	JEditorPane pane = new JEditorPane();

	JTextField headerField = new JTextField(5);

	public UtilitiesPanel(SlammerTabbedPane parent)
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
		constant3f.setEnabled(false);

		pane.setEditable(false);
		pane.setContentType("text/html");

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);

		Insets top = new Insets(10, 0, 0, 0);
		Insets none = new Insets(0, 0, 0, 0);

		Border b = BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(0, 0, 0, 5),
			BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK)
		);

		int x = 0;
		int y = 0;

		JPanel panel = new JPanel(new GridLayout(0, 1));

		panel.add(gscm);
		panel.add(cmgs);
		panel.add(mult);
		panel.add(redigit);
		panel.add(peapick);
		panel.add(clip);

		c.gridx = x++;
		c.gridy = y;
		c.gridheight = 12;
		c.anchor = GridBagConstraints.NORTHWEST;
		gridbag.setConstraints(panel, c);
		add(panel);

		c.gridx = x++;
		c.fill = GridBagConstraints.BOTH;
		JLabel label = new JLabel(" ");
		label.setBorder(b);
		gridbag.setConstraints(label, c);
		add(label);

		c.gridheight = 1;
		c.insets = none;
		c.gridx = x++;
		c.gridy = y++;
		gridbag.setConstraints(source, c);
		add(source);

		c.gridy = y++;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(sourcef, c);
		add(sourcef);

		c.gridx = x--;
		c.weightx = 0;
		gridbag.setConstraints(sourceb, c);
		add(sourceb);

		c.gridx = x++;
		c.gridy = y++;
		c.insets = top;
		gridbag.setConstraints(dest, c);
		add(dest);

		c.gridy = y++;
		c.insets = none;
		gridbag.setConstraints(destf, c);
		add(destf);

		c.gridx = x--;
		gridbag.setConstraints(destb, c);
		add(destb);

		c.gridx = x++;
		c.gridy = y++;
		c.gridwidth = 2;
		c.insets = top;
		gridbag.setConstraints(constant1, c);
		add(constant1);

		c.gridy = y++;
		c.insets = none;
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panel.add(constant1Pre);
		panel.add(constant1f);
		panel.add(constant1Post);
		gridbag.setConstraints(panel, c);
		add(panel);

		c.gridy = y++;
		c.insets = top;
		gridbag.setConstraints(constant2, c);
		add(constant2);

		c.gridy = y++;
		c.insets = none;
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panel.add(constant2Pre);
		panel.add(constant2f);
		panel.add(constant2Post);
		gridbag.setConstraints(panel, c);
		add(panel);

		c.gridy = y++;
		c.insets = top;
		gridbag.setConstraints(constant3, c);
		add(constant3);

		c.gridy = y++;
		c.insets = none;
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panel.add(constant3Pre);
		panel.add(constant3f);
		panel.add(constant3Post);
		gridbag.setConstraints(panel, c);
		add(panel);

		c.gridy = y++;
		c.insets = top;

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panel.add(new JLabel("Skip the first "));
		panel.add(skip);
		panel.add(new JLabel(" lines of the source file (use this to skip header data)."));
		gridbag.setConstraints(panel, c);
		add(panel);

		c.gridy = y++;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(0, 0, 10, 0);
		gridbag.setConstraints(go, c);
		add(go);

		c.gridx = 0;
		c.gridy = y;
		c.insets = none;
		c.gridwidth = 4;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(pane, c);
		add(pane);
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
				constant3f.setText("");
				constant3f.setEnabled(false);
				constant3.setText(" ");
				constant3Pre.setText("");
				constant3Post.setText("");
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
					pane.setText("This program converts a time file (a file containing paired time and acceleration values) into a file containing a sequence of acceleration values having a constant time spacing (digitization interval) using an interpolation algorithm.  The input and output files or directories must be specified or selected using the browser.  The digitization interval for the output file must be specified in the indicated field; any value can be selected by the user, but values of 0.01-0.05 generally are appropriate.  The output file is in the format necessary to run the other programs in this package, but if the original time file had units of g's, it will be necessary to convert to cm/s/s before running other analyses.");
				}
				else if(peapick.isSelected())
				{
					constant1Pre.setText("Trim record between g values of ");
					constant1f.setEnabled(true);
					pane.setText("This program removes the points of a record from the beginning and end of the file that are less than the specified number of g's. 50 points are added to each side for lead in time.");
				}
				else if(clip.isSelected())
				{
					constant1Pre.setText("Remove data before ");
					constant1Post.setText(" seconds");
					constant1f.setEnabled(true);
					constant2Pre.setText("Remove data after ");
					constant2Post.setText(" seconds");
					constant2f.setEnabled(true);
					constant3.setText("Digitization Interval (s)");
					constant3f.setEnabled(true);
					pane.setText("This program saves all data within (inclusive) the specified range. If the file is shorter than the specified range, the file will simply be copied to the destination.");
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

				if(destf.getText() == null || destf.getText().equals(""))
					d = s;
				else
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
				Double val3 = new Double(0);
				int sel;
				String selStr;

				if(cmgs.isSelected())
				{
					sel = SELECT_CMGS;
					selStr = "Conversion from cm/s/s to g's";
				}
				else if(gscm.isSelected())
				{
					sel = SELECT_GSCM;
					selStr = "Conversion from g's to cm/s/s";
				}
				else if(mult.isSelected())
				{
					val1 = (Double)Utils.checkNum(constant1f.getText(), "constant field", null, false, null, null, false, null, false);
					if(val1 == null) return;
					sel = SELECT_MULT;
					selStr = "Multiplication by " + constant1f.getText();
				}
				else if(redigit.isSelected())
				{
					val1 = (Double)Utils.checkNum(constant1f.getText(), "Digitization Interval field", null, false, null, new Double(0), false, null, false);
					if(val1 == null) return;
					sel = SELECT_REDIGIT;
					selStr = "Redigitization to digitization interval of " + constant1f.getText();
				}
				else if(peapick.isSelected())
				{
					val1 = (Double)Utils.checkNum(constant1f.getText(), "pea picker field", null, false, null, new Double(0), false, null, false);
					if(val1 == null) return;
					sel = SELECT_BRACKET;
					selStr = "Pea pick";
				}
				else if(clip.isSelected())
				{
					val1 = (Double)Utils.checkNum(constant1f.getText(), "first clip time field", null, false, null, null, false, null, false);
					if(val1 == null) return;
					val2 = (Double)Utils.checkNum(constant2f.getText(), "second clip time field", null, false, null, null, false, null, false);
					if(val2 == null) return;
					val3 = (Double)Utils.checkNum(constant3f.getText(), "digitization interval field", null, false, null, null, false, null, false);
					if(val3 == null) return;
					sel = SELECT_TRIM;
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
					errors = runUtil(sel, s, d, skipLines, val1.doubleValue(), val2.doubleValue(), val3.doubleValue());
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

						errors += runUtil(sel, list[i],  new File(d.getAbsolutePath() + System.getProperty("file.separator") + list[i].getName()), skipLines, val1.doubleValue(), val2.doubleValue(), val3.doubleValue());
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

	private String runUtil(int sel, File s, File d, int skip, double var1, double var2, double var3) throws IOException
	{
		DoubleList data = new DoubleList(s.getAbsolutePath(), skip, 1.0);
		if(data.bad())
		{
			return ("<br>After skipping " + skip + " lines, invalid data encountered in file " + s.getAbsolutePath() + " at point " + data.badEntry() + ".");
		}

		FileWriter o = new FileWriter(d);
		String err = "";

		switch(sel)
		{
			case SELECT_CMGS: // cmgs
				Utilities.CM_GS(data, o);
				break;
			case SELECT_GSCM: // gscm
				Utilities.GS_CM(data, o);
				break;
			case SELECT_MULT: // mult
				Utilities.Mult(data, o, var1);
				break;
			case SELECT_REDIGIT: // redigit
				err = Utilities.Redigitize(data, o, var1);
				break;
			case SELECT_BRACKET: // peapick
				Utilities.Bracket(data, o, var1 * Analysis.Gcmss);
				break;
			case SELECT_TRIM: // clip
				Utilities.Trim(data, o, var1, var2, var3);
		}

		if(!err.equals(""))
			err = "<br>" + err + " in file " + s.getAbsolutePath() + ".\n";

		return err;
	}
}
