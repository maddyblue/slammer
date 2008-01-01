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
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Vector;
import slammer.*;
import slammer.analysis.*;
import java.io.*;

class AddRecordsPanel extends JPanel implements ActionListener
{
	SlammerTabbedPane parent;

	AddRecordsTable table = new AddRecordsTable();

	JFileChooser chooser = new JFileChooser();
	JButton browse = new JButton("Add files/directories to list");
	JButton clearTable = new JButton("Clear list");
	JButton clearSelected = new JButton("Clear highlited records from list");
	JButton add = new JButton("Import records");

	public AddRecordsPanel(SlammerTabbedPane parent)
	{
		this.parent = parent;

		browse.setActionCommand("browse");
		browse.addActionListener(this);

		clearTable.setActionCommand("clearTable");
		clearTable.addActionListener(this);

		clearSelected.setActionCommand("clearSelected");
		clearSelected.addActionListener(this);

		add.setActionCommand("add");
		add.addActionListener(this);

		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(true);

		Vector list = new Vector();
		list.add(browse);
		list.add(clearTable);
		list.add(clearSelected);

		JPanel south = new JPanel(new BorderLayout());
		south.add(BorderLayout.WEST, add);

		setLayout(new BorderLayout());

		add(BorderLayout.NORTH, GUIUtils.makeRecursiveLayoutRight(list));
		add(BorderLayout.CENTER, table);
		add(BorderLayout.SOUTH, south);
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			if(command.equals("add"))
			{
				// popup a warning about units
				int n = JOptionPane.showConfirmDialog(this,
					  "WARNING: Earthquake records MUST be in units of g's.\n"
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
				String arias, dobry, pga, pgv, meanper;
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
				progFrame.setVisible(true);

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
					file = Utils.addQuote(m.getValueAt(i, incr++).toString());
					eq = Utils.addQuote(m.getValueAt(i, incr++).toString());
					record = Utils.addQuote(m.getValueAt(i, incr++).toString());
					di = m.getValueAt(i, incr++).toString();
					mag = Utils.nullify(m.getValueAt(i, incr++).toString());
					epidist = Utils.nullify(m.getValueAt(i, incr++).toString());
					focdist = Utils.nullify(m.getValueAt(i, incr++).toString());
					rupdist = Utils.nullify(m.getValueAt(i, incr++).toString());
					focmech = m.getValueAt(i, incr++).toString();
					loc = Utils.addQuote(m.getValueAt(i, incr++).toString());
					owner = Utils.addQuote(m.getValueAt(i, incr++).toString());
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
					table.addLocation(chooser.getSelectedFiles());
				}
			}
			else if(command.equals("clearTable"))
			{
				table.model.empty();
			}
			else if(command.equals("clearSelected"))
			{
				int[] rows = table.table.getSelectedRows();

				for(int i = rows.length; i > 0; i--)
					table.model.removeRow(rows[i - 1]);
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
		String arias, dobry, pga, pgv, meanper;
		String errors = "", pre;
		Object ret;
		DoubleList data;

		eq = Utils.addQuote(eq);
		record = Utils.addQuote(record);
		file = Utils.addQuote(file);

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

		File f = new File(file);
		if(!f.isFile() || !f.canRead())
		{
			errors += "Cannot read file or file does not exist: " + file + "\n";
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

		arias = ImportRecords.Arias(data, dig_int);
		dobry = ImportRecords.Dobry(data, dig_int);
		meanper = ImportRecords.MeanPer(data, dig_int);
		pga = ImportRecords.PGA(data);
		pgv = ImportRecords.PGV(data, dig_int);

		// a few conversions
		pre = focmech;
		for(int j = 0; j < SlammerTable.FocMechArray.length; j++)
		{
			if(focmech.equals(SlammerTable.FocMechArray[j]))
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
		for(int j = 0; j < SlammerTable.SiteClassArray.length; j++)
		{
			if(siteclass.equals(SlammerTable.SiteClassArray[j]))
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
			String q = "insert into data " +
				"(eq, record, digi_int, mom_mag, arias, dobry, pga, pgv, mean_per, epi_dist, foc_dist, rup_dist, foc_mech, location, owner, latitude, longitude, class, change, path, select1, analyze, select2) values ( '" +
				eq + "', '" +
				record + "', " +
				di + ", " +
				mag + ", " +
				arias + ", " +
				dobry + ", " +
				pga + ", " +
				pgv + ", " +
				meanper + ", " +
				epidist + ", " +
				focdist + ", " +
				rupdist + ", " +
				focmech + ", '" +
				loc + "', '" +
				owner + "', " +
				lat + ", " +
				lng + ", " +
				siteclass + ", " +
				1 + ", '" +
				file + "', " +
				0 + ", " +
				0 + ", " +
				0 + ")";
			Utils.getDB().runUpdate(q);
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
				+ " , pgv= "       + pgv
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
				Utils.getDB().runUpdate(q);
		}

		return "";
	}
}
