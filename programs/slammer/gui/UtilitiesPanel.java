/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
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
	JRadioButton bracket = new JRadioButton("Bracket records");
	JRadioButton clip = new JRadioButton("Trim records");
	ButtonGroup group = new ButtonGroup();

	JFileChooser fcs = new JFileChooser();
	JFileChooser fcd = new JFileChooser();
	JLabel source = new JLabel("Source files and directories");
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
	JCheckBox overwrite = new JCheckBox("Overwrite files without prompting");
	JEditorPane pane = new JEditorPane();
	JScrollPane spane = new JScrollPane(pane);

	int global_overwrite = OVW_NONE;

	public final static int OVW_NONE      = 0;
	public final static int OVW_OVERWRITE = 1;
	public final static int OVW_SKIP      = 2;

	public UtilitiesPanel(SlammerTabbedPane parent)
	{
		this.parent = parent;

		fcs.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fcs.setMultiSelectionEnabled(true);
		sourcef.setEditable(false);
		fcd.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		cmgs.setActionCommand("change");
		gscm.setActionCommand("change");
		mult.setActionCommand("change");
		redigit.setActionCommand("change");
		bracket.setActionCommand("change");
		clip.setActionCommand("change");

		cmgs.addActionListener(this);
		gscm.addActionListener(this);
		mult.addActionListener(this);
		redigit.addActionListener(this);
		bracket.addActionListener(this);
		clip.addActionListener(this);

		group.add(cmgs);
		group.add(gscm);
		group.add(mult);
		group.add(redigit);
		group.add(bracket);
		group.add(clip);

		sourceb.setActionCommand("source");
		sourceb.addActionListener(this);

		destb.setActionCommand("dest");
		destb.addActionListener(this);

		go.setActionCommand("go");
		go.addActionListener(this);

		constant1f.setEditable(false);
		constant2f.setEditable(false);
		constant3f.setEditable(false);

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

		panel.add(cmgs);
		panel.add(gscm);
		panel.add(mult);
		panel.add(redigit);
		panel.add(bracket);
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
		panel.add(new JLabel(" lines of the source file (use this to skip header data)"));
		gridbag.setConstraints(panel, c);
		add(panel);

		c.gridy = y++;
		gridbag.setConstraints(overwrite, c);
		add(overwrite);

		c.gridy = y++;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(10, 0, 10, 0);
		gridbag.setConstraints(go, c);
		add(go);

		c.gridx = 0;
		c.gridy = y;
		c.insets = none;
		c.gridwidth = 4;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(spane, c);
		add(spane);
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
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
					File files[] = fcs.getSelectedFiles();
					String sf = "";
					for(int i = 0; i < files.length; i++)
					{
						if(i > 0)
							sf += ", ";

						sf += files[i].getAbsolutePath();
					}
					sourcef.setText(sf);
				}
			}
			else if(command.equals("change"))
			{
				constant1f.setText("");
				constant1f.setEditable(false);
				constant1.setText(" ");
				constant1Pre.setText("");
				constant1Post.setText("");
				constant2f.setText("");
				constant2f.setEditable(false);
				constant2.setText(" ");
				constant2Pre.setText("");
				constant2Post.setText("");
				constant3f.setText("");
				constant3f.setEditable(false);
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
					constant1f.setEditable(true);
					pane.setText("This program multiplies the values in a file by a user-specified constant.  Both the input and output file or directory must be specified or selected using the browser.  The constant is specified in the \"Constant\" field.");
				}
				else if(redigit.isSelected())
				{
					constant1.setText("Digitization Interval (s)");
					constant1f.setEditable(true);
					pane.setText("This program converts a time file (a file containing paired time and acceleration values) into a file containing a sequence of acceleration values having a constant time spacing (digitization interval) using an interpolation algorithm.  The input and output files or directories must be specified or selected using the browser.  The digitization interval for the output file must be specified in the indicated field; any value can be selected by the user, but values of 0.01-0.05 generally are appropriate.  The output file is in the format necessary to be imported into the program, but it must have units of g.");
				}
				else if(bracket.isSelected())
				{
					constant1Pre.setText("Bracket record between values of ");
					constant1Post.setText(" (in units of source file)");
					constant1f.setEditable(true);
					constant2Pre.setText("Retain ");
					constant2Post.setText(" points to each side for lead-in and -out time");
					constant2f.setEditable(true);
					constant2f.setText("0");
					pane.setText("This program removes points from a record from the beginning and end of the file that have values less than that specified in the input box.");
				}
				else if(clip.isSelected())
				{
					constant1Pre.setText("Remove data before ");
					constant1Post.setText(" seconds");
					constant1f.setEditable(true);
					constant2Pre.setText("Remove data after ");
					constant2Post.setText(" seconds");
					constant2f.setEditable(true);
					constant3.setText("Digitization Interval (s)");
					constant3f.setEditable(true);
					pane.setText("This program saves all data within (inclusive) the specified range. If the file is shorter than the specified range, the file will simply be copied to the destination.");
				}
			}
			else if(command.equals("go"))
			{
				File sources[], source, dest, d;
				String temp;

				sources = fcs.getSelectedFiles();
				if(sources == null || sources.length == 0)
				{
					GUIUtils.popupError("No sources specified.");
					return;
				}

				temp = destf.getText();
				if(temp == null || temp.equals(""))
					dest = null;
				else
					dest = new File(temp);

				if(dest != null && (sources.length > 1 || !sources[0].isFile()) && dest.isFile())
				{
					GUIUtils.popupError("Destination must be a directory.");
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
					skipLines = doub.intValue();
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
				else if(bracket.isSelected())
				{
					val1 = (Double)Utils.checkNum(constant1f.getText(), "bracket value field", null, false, null, new Double(0), true, null, false);
					if(val1 == null) return;
					val2 = (Double)Utils.checkNum(constant2f.getText(), "bracket lead-in field", null, false, null, new Double(0), true, null, false);
					if(val2 == null) return;
					sel = SELECT_BRACKET;
					selStr = "Bracket";
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
				boolean ovw = overwrite.isSelected();
				String results = "";
				int errors = 0;
				global_overwrite = OVW_NONE;

				JFrame progFrame = new JFrame("Progress...");
				JProgressBar prog = new JProgressBar();
				prog.setStringPainted(true);
				prog.setMinimum(0);
				prog.setMaximum(sources.length);

				progFrame.getContentPane().add(prog);
				progFrame.setSize(600, 75);
				progFrame.setLocationRelativeTo(null);
				progFrame.setVisible(true);

				for(int i = 0; i < sources.length; i++)
				{
					source = sources[i];
					prog.setString(source.getAbsolutePath());
					prog.setValue(i);
					prog.paintImmediately(0, 0, prog.getWidth(), prog.getHeight());

					if(source.isFile())
					{
						try
						{
							results += "<br>" + runUtil(sel, source, dest, skipLines, val1.doubleValue(), val2.doubleValue(), val3.doubleValue(), ovw);
						}
						catch (Exception ex)
						{
							results = "<br>" + ex.getMessage() + results;
							errors++;
						}
					}
					else if(source.isDirectory())
					{
						File list[] = source.listFiles();

						if(dest == null)
							d = null;
						else
						{
							d = new File(dest, source.getName());
							d.mkdirs();
						}

						for(int j = 0; j < list.length; j++)
						{
							if(!list[j].isFile())
								continue;

							prog.setString(list[j].getAbsolutePath());
							prog.paintImmediately(0, 0, prog.getWidth(), prog.getHeight());

							try
							{
								results += "<br>" + runUtil(sel, list[j], d, skipLines, val1.doubleValue(), val2.doubleValue(), val3.doubleValue(), ovw);
							}
							catch (Exception ex)
							{
								results = "<br>" + ex.getMessage() + results;
								errors++;
							}
						}
					}
				}

				progFrame.dispose();
				pane.setText(selStr + (errors == 0 ? " complete" : (" NOT complete: " + errors + " errors")) + "<hr>" + results);
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	private String runUtil(int sel, File s, File d, int skip, double var1, double var2, double var3, boolean ovw) throws Exception
	{
		File dest;
		boolean overwrite = false;

		if(!s.exists() || !s.canRead())
			throw new Exception("Not readable: " + s.getAbsolutePath());
		else if(!s.isFile())
			throw new Exception("Invalid file: " + s.getAbsolutePath());

		if(d == null)
			dest = s;
		else if(d.isDirectory())
			dest = new File(d, s.getName());
		else
			dest = d;

		String ret = dest.toString();

		if(dest.exists())
		{
			if(ovw || global_overwrite == OVW_OVERWRITE)
				overwrite = true;
			else if(global_overwrite == OVW_SKIP)
				return "Skip: " + ret;
			else
			{
				Object[] options = { "Overwrite", "Overwrite All", "Skip", "Skip All" };
				switch(JOptionPane.showOptionDialog(null, "File exists:\n" + dest.toString(), "Overwrite",
					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
					null, options, options[0]))
				{
					case 0: // overwrite
						overwrite = true;
						break;
					case 1: // overwrite all
						overwrite = true;
						global_overwrite = OVW_OVERWRITE;
						break;
					case 3: // skip all
						global_overwrite = OVW_SKIP;
					case 2: // skip
					default:
						return "Skip: " + ret;
				}
			}
		}

		DoubleList data = new DoubleList(s.getAbsolutePath(), skip, 1.0, true);
		if(data.bad())
			throw new Exception("Invalid data in " + ret + " at point " + data.badEntry());

		try
		{
			FileWriter o = new FileWriter(dest);

			switch(sel)
			{
				case SELECT_CMGS:
					Utilities.CM_GS(data, o);
					break;
				case SELECT_GSCM:
					Utilities.GS_CM(data, o);
					break;
				case SELECT_MULT:
					Utilities.Mult(data, o, var1);
					break;
				case SELECT_REDIGIT:
					Utilities.Redigitize(data, o, var1);
					break;
				case SELECT_BRACKET:
					Utilities.Bracket(data, o, var1, (int)var2);
					break;
				case SELECT_TRIM:
					Utilities.Trim(data, o, var1, var2, var3);
			}
		}
		catch (Exception ex)
		{
			throw new Exception(ex.getMessage() + ": " + ret);
		}

		if(overwrite)
			ret = "(overwritten) " + ret;

		return "Success: " + ret;
	}
}
