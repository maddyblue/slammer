/*
 * AddRecordsPanel.java - panel to add records
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

/* $Id: AddRecordsPanel.java,v 1.1 2003/06/15 01:58:11 dolmant Exp $ */

package newmark.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Vector;
import newmark.*;

class AddRecordsPanel extends JPanel implements ActionListener
{
	NewmarkTabbedPane parent;

	AddRecordsTable table = new AddRecordsTable();

	JTextField location = new JTextField();
	JFileChooser chooser = new JFileChooser();
	JButton browse = new JButton("Browse...");
	JButton add = new JButton("Add record(s)");

	public AddRecordsPanel(NewmarkTabbedPane parent)
	{
		this.parent = parent;

		browse.setMnemonic(KeyEvent.VK_B);
		browse.setActionCommand("browse");
		browse.addActionListener(this);

		add.setMnemonic(KeyEvent.VK_A);
		add.setActionCommand("add");
		add.addActionListener(this);

		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		location.setEditable(false);

		Vector list = new Vector();
		list.add(new JLabel("Select a file or directory: "));
		list.add(browse);

		JPanel north = new JPanel(new BorderLayout());
		north.add(BorderLayout.WEST, GUIUtils.makeRecursiveLayoutRight(list));
		north.add(BorderLayout.CENTER, location);

		JPanel south = new JPanel(new BorderLayout());
		south.add(BorderLayout.WEST, add);

		setLayout(new BorderLayout());

		add(BorderLayout.NORTH, north);
		add(BorderLayout.CENTER, table);
		add(BorderLayout.SOUTH, south);
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			System.out.println(command);
			if(command.equals("add"))
			{
				// popup a warning about units
				int n = JOptionPane.showConfirmDialog(this,
					  "WARNING: Earthquake records MUST be in units of cm/s/s.\n"
					+ "Records must also have no header data.\n"
					+ "\n"
					+ "If you need to convert units or strip header data, use the Utilities page.\n"
					+ "\n"
					+ "Import these records?",
					"Continue?",
					JOptionPane.YES_NO_OPTION);
				if(n != JOptionPane.YES_OPTION)
					return;

				// they don't have to press enter to complete cell editing:
				TableCellEditor edit = table.getCellEditor();
						if(edit != null) edit.stopCellEditing();

				// ok, the user says they are all in cm/s/s, continue
				String file, eq, record, di, mag, epidist, focdist, rupdist, focmech, loc, owner, lat, lng, siteclass;
				String arias, dobry, pga, meanper;
				String status = "", error;

				TableModel m = table.getModel();
				if(m.getRowCount() == 0)
					return;

				JProgressBar prog = new JProgressBar();
				prog.setStringPainted(true);
				prog.setMinimum(0);
				prog.setMaximum(m.getRowCount() - 1);
				JFrame progFrame = new JFrame("Import progress...");
				progFrame.getContentPane().add(prog);
				progFrame.setSize(600, 75);
				GUIUtils.setLocationMiddle(progFrame);
				progFrame.show();

				int incr;
				for(int i = 0; i < m.getRowCount(); i++)
				{

					// do we add this row?
					if(((Boolean)m.getValueAt(i, 0)).booleanValue() == false)
					{
						continue;
					}

					// get the data
					incr = 1;
					file = Utils.addSlashes(m.getValueAt(i, incr++).toString());
					eq = Utils.addSlashes(m.getValueAt(i, incr++).toString());
					record = Utils.addSlashes(m.getValueAt(i, incr++).toString());
					di = m.getValueAt(i, incr++).toString();
					mag = Utils.nullify(m.getValueAt(i, incr++).toString());
					epidist = Utils.nullify(m.getValueAt(i, incr++).toString());
					focdist = Utils.nullify(m.getValueAt(i, incr++).toString());
					rupdist = Utils.nullify(m.getValueAt(i, incr++).toString());
					focmech = m.getValueAt(i, incr++).toString();
					loc = Utils.addSlashes(m.getValueAt(i, incr++).toString());
					owner = Utils.addSlashes(m.getValueAt(i, incr++).toString());
					lat = Utils.nullify(m.getValueAt(i, incr++).toString());
					lng = Utils.nullify(m.getValueAt(i, incr++).toString());
					siteclass = m.getValueAt(i, incr++).toString();

					prog.setString(file);
					prog.setValue(i);
					prog.paintImmediately(0,0,prog.getWidth(),prog.getHeight());

					error = manipRecord(true, file, eq, record, di, mag, epidist, focdist, rupdist, focmech, loc, owner, lat, lng, siteclass);

					if(error.equals(""))
						m.setValueAt(new Boolean(false), i, 0);
					else
						status += error;
				}

				progFrame.dispose();

				if(!status.equals(""))
					GUIUtils.popupError(status);

				Utils.updateEQLists();
			}
			else if(command.equals("browse"))
			{
				int r = chooser.showOpenDialog(this);
				if(r == JFileChooser.APPROVE_OPTION)
				{
					table.setLocation(chooser.getSelectedFile());
					location.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	public static String manipRecord(boolean add, String file, String eq, String record, String di, String mag, String epidist, String focdist, String rupdist, String focmech, String loc, String owner, String lat, String lng, String siteclass) throws Exception
	{
		double dig_int = 0;
		String arias, dobry, pga, meanper;
		String errors = "", pre;
		Object ret;
		DoubleList data;

		eq = Utils.addSlashes(eq);
		record = Utils.addSlashes(record);
		file = Utils.addSlashes(file);

		// verify existence of must-have data
		if(eq.equals(""))
		{
			errors += "Earthquake value is blank.\n";
		}
		if(record.equals(""))
		{
			errors += "Record value is blank.\n";
		}
		if(di.equals(""))
		{
			errors += "Digitization interval is blank.\n";
		}

		// record already here?
		if(add)
		{
			Object[][] res = Utils.getDB().runQuery("select eq from data where eq='" + eq + "' and record='" + record + "'");
			if(res != null)
			{
				errors += "Record already exists in the database.\n";
			}
		}

		// check data

		// we already checked for an empty di - don't want two errors about it
		if(!di.equals(""))
		{
			ret = Utils.checkNum(di, "Digitization interval field", null, false, null, new Double(0), false, null, true);
			if(ret.getClass().getName().equals("java.lang.String"))
			{
				errors += ret.toString() + "\n";
			}
			else
			{
				dig_int = ((Double)ret).doubleValue();
			}
		}

		if(!mag.equals("null"))
		{
			ret = Utils.checkNum(mag, "Moment Magnitude field", null, false, null, new Double(0), true, null, true);
			if(ret.getClass().getName().equals("java.lang.String"))
			{
				errors += ret.toString() + "\n";
			}
		}

		if(!epidist.equals("null"))
		{
			ret = Utils.checkNum(epidist, "Epicentral distance field", null, false, null, new Double(0), true, null, true);
			if(ret.getClass().getName().equals("java.lang.String"))
			{
				errors += ret.toString() + "\n";
			}
		}

		if(!focdist.equals("null"))
		{
			ret = Utils.checkNum(focdist, "Focal distance field", null, false, null, new Double(0), true, null, true);
			if(ret.getClass().getName().equals("java.lang.String"))
			{
				errors += ret.toString() + "\n";
			}
		}

		if(!rupdist.equals("null"))
		{
			ret = Utils.checkNum(rupdist, "Rupture distance field", null, false, null, new Double(0), true, null, true);
			if(ret.getClass().getName().equals("java.lang.String"))
			{
				errors += ret.toString() + "\n";
			}
		}

		if(!lat.equals("null"))
		{
			ret = Utils.checkNum(lat, "Latitude field", new Double(90), true, null, new Double(-90), true, null, true);
			if(ret.getClass().getName().equals("java.lang.String"))
			{
				errors += ret.toString() + "\n";
			}
		}

		if(!lng.equals("null"))
		{
			ret = Utils.checkNum(lng, "Longitude field", new Double(180), true, null, new Double(-180), true, null, true);
			if(ret.getClass().getName().equals("java.lang.String"))
			{
				errors += ret.toString() + "\n";
			}
		}

		if(!errors.equals(""))
		{
			return ("Errors on file " + file + ", earthquake " + eq + ", record " + record + ":\n" + errors + "\n");
		}

		// computations
		data = new DoubleList(file);

		if(data.bad())
		{
			return ("Errors on file " + file + ", earthquake " + eq + ", record " + record + ":\nInvalid data at data point " + data.badEntry() + "\n");
		}

		arias = Analysis.Arias(data, dig_int);
		dobry = Analysis.Dobry(data, dig_int);
		meanper = Analysis.MeanPer(data, dig_int);
		pga = Analysis.PGA(data);

		// a few conversions
		pre = focmech;
		for(int j = 0; j < NewmarkTable.FocMechArray.length; j++)
		{
			if(focmech.equals(NewmarkTable.FocMechArray[j]))
			{
				focmech = Integer.toString(j);
				break;
			}
		}
		if(pre.equals(focmech))
		{
			focmech = "0";
		}

		pre = siteclass;
		for(int j = 0; j < NewmarkTable.SiteClassArray.length; j++)
		{
			if(siteclass.equals(NewmarkTable.SiteClassArray[j]))
			{
				siteclass = Integer.toString(j);
				break;
			}
		}
		if(pre.equals(siteclass))
		{
			siteclass = "0";
		}

		if(add)
		{
			// add it to the db
			Utils.getDB().runQuery("insert into data values (uniquekey('data'), '" + eq + "', '" + record + "', " + di + ", " + mag + ", " + arias + ", " + dobry + ", " + pga + ", " + meanper + ", " + epidist + ", " + focdist + ", " + rupdist + ", " + focmech + ", '" + loc + "', '" + owner + "', " + lat + ", " + lng + ", " + siteclass + ", " + 1 + ", '" + file + "', " + 0 + ", " + 0 + ", " + 0 + ")");
		}
		else
		{
			// modify it in the db
			String q = "update data set"
				+   " eq='"        + eq
				+ "', record='"    + record
				+ "', digi_int= "  + di
				+ " , mom_mag= "   + mag
				+ " , arias= "     + arias
				+ " , dobry= "     + dobry
				+ " , pga= "       + pga
				+ " , mean_per= "  + meanper
				+ " , epi_dist= "  + epidist
				+ " , foc_dist= "  + focdist
				+ " , rup_dist= "  + rupdist
				+ " , foc_mech= "  + focmech
				+ " , location='"  + loc
				+ "', owner='"     + owner
				+ "', latitude= "  + lat
				+ " , longitude= " + lng
				+ " , class= "     + siteclass
				+ "  where path='" + file + "'";
				Utils.getDB().runQuery(q);
		}

		return "";
	}
}
