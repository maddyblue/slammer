/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.ArrayList;
import slammer.*;
import slammer.analysis.*;
import java.io.*;
import java.sql.*;

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

		ArrayList list = new ArrayList();
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
				String file, eq, record, di, mag, epidist, focdist, rupdist, vs30, focmech, loc, owner, lat, lng, siteclass;
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
				progFrame.setLocationRelativeTo(null);
				progFrame.setVisible(true);

				int incr;
				for(int i = 0; i < m.getRowCount(); i++)
				{
					// do we add this row?
					if(((Boolean)m.getValueAt(i, 0)).booleanValue() == false)
						continue;

					// get the data
					incr = 1;
					file = m.getValueAt(i, incr++).toString();
					eq = m.getValueAt(i, incr++).toString();
					record = m.getValueAt(i, incr++).toString();
					di = m.getValueAt(i, incr++).toString();
					mag = m.getValueAt(i, incr++).toString();
					epidist = m.getValueAt(i, incr++).toString();
					focdist = m.getValueAt(i, incr++).toString();
					rupdist = m.getValueAt(i, incr++).toString();
					focmech = m.getValueAt(i, incr++).toString();
					loc = m.getValueAt(i, incr++).toString();
					owner = m.getValueAt(i, incr++).toString();
					vs30 = m.getValueAt(i, incr++).toString();
					siteclass = m.getValueAt(i, incr++).toString();
					lat = m.getValueAt(i, incr++).toString();
					lng = m.getValueAt(i, incr++).toString();

					prog.setString(file);
					prog.setValue(i);
					prog.paintImmediately(0,0,prog.getWidth(),prog.getHeight());

					error = manipRecord(true, file, eq, record, di, mag, epidist, focdist, rupdist, vs30, focmech, loc, owner, lat, lng, siteclass);

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

	public static String manipRecord(boolean add, String file, String eq, String record, String di, String mag, String epidist, String focdist, String rupdist, String vs30, String focmech, String loc, String owner, String lat, String lng, String siteclass) throws Exception
	{
		double dig_int = 0;
		double arias, dobry, pga, pgv, meanper;
		String errors = "", pre;
		Object ret;
		DoubleList data;
		int siteclassI = 0, focmechI = 0;
		String nulls = new String();

		// verify existence of must-have data
		if(eq.equals(""))
			errors += "Earthquake value is blank.\n";
		if(record.equals(""))
			errors += "Record value is blank.\n";
		if(di.equals(""))
			errors += "Digitization interval is blank.\n";

		// record already here?
		if(add)
		{
			Object[][] res = Utils.getDB().preparedQuery("select eq from data where eq=? and record=?", eq, record);
			if(res != null)
				errors += "Record already exists in the database.\n";
		}

		// check data

		// we already checked for an empty di - don't want two errors about it
		if(!di.equals(""))
		{
			ret = Utils.checkNum(di, "digitization interval field", null, false, null, new Double(0), false, null, true);
			if(ret.getClass().getName().equals("java.lang.String"))
				errors += ret.toString() + "\n";
			else
				dig_int = ((Double)ret).doubleValue();
		}

		if(!mag.equals(""))
		{
			ret = Utils.checkNum(mag, "moment magnitude field", null, false, null, new Double(0), true, null, true);
			if(ret.getClass().getName().equals("java.lang.String"))
				errors += ret.toString() + "\n";
		}
		else mag = nulls;

		if(!epidist.equals(""))
		{
			ret = Utils.checkNum(epidist, "epicentral distance field", null, false, null, new Double(0), true, null, true);
			if(ret.getClass().getName().equals("java.lang.String"))
				errors += ret.toString() + "\n";
		}
		else epidist = nulls;

		if(!focdist.equals(""))
		{
			ret = Utils.checkNum(focdist, "focal distance field", null, false, null, new Double(0), true, null, true);
			if(ret.getClass().getName().equals("java.lang.String"))
				errors += ret.toString() + "\n";
		}
		else focdist = nulls;

		if(!rupdist.equals(""))
		{
			ret = Utils.checkNum(rupdist, "rupture distance field", null, false, null, new Double(0), true, null, true);
			if(ret.getClass().getName().equals("java.lang.String"))
				errors += ret.toString() + "\n";
		}
		else rupdist = nulls;

		if(!vs30.equals(""))
		{
			ret = Utils.checkNum(vs30, "Vs30 field", null, false, null, new Double(0), true, null, true);
			if(ret.getClass().getName().equals("java.lang.String"))
				errors += ret.toString() + "\n";
		}
		else vs30 = nulls;

		if(!lat.equals(""))
		{
			ret = Utils.checkNum(lat, "latitude field", new Double(90), true, null, new Double(-90), true, null, true);
			if(ret.getClass().getName().equals("java.lang.String"))
				errors += ret.toString() + "\n";
		}
		else lat = nulls;

		if(!lng.equals(""))
		{
			ret = Utils.checkNum(lng, "longitude field", new Double(180), true, null, new Double(-180), true, null, true);
			if(ret.getClass().getName().equals("java.lang.String"))
				errors += ret.toString() + "\n";
		}
		else lng = nulls;

		File f = new File(file);
		if(!f.isFile() || !f.canRead())
			errors += "Cannot read file or file does not exist: " + file + "\n";

		if(!errors.equals(""))
			return ("Errors on file " + file + ", earthquake " + eq + ", record " + record + ":\n" + errors + "\n");

		// computations
		data = new DoubleList(file);

		if(data.bad())
			return ("Errors on file " + file + ", earthquake " + eq + ", record " + record + ":\nInvalid data at data point " + data.badEntry() + "\n");

		arias = ImportRecords.arias(data, dig_int);
		dobry = ImportRecords.dobry(data, dig_int);
		meanper = ImportRecords.meanPer(data, dig_int);
		pga = ImportRecords.pga(data);
		pgv = ImportRecords.pgv(data, dig_int);

		// a few conversions
		pre = focmech;
		for(int j = 0; j < SlammerTable.FocMechArray.length; j++)
		{
			if(focmech.equals(SlammerTable.FocMechArray[j]))
			{
				focmechI = j;
				break;
			}
		}
		if(pre.equals(focmech))
			focmechI = 0;

		pre = siteclass;
		for(int j = 0; j < SlammerTable.SiteClassArray.length; j++)
		{
			if(siteclass.equals(SlammerTable.SiteClassArray[j]))
			{
				siteclassI = j;
				break;
			}
		}
		if(pre.equals(siteclass))
			siteclassI = 0;

		String q;
		Object[] objects;
		if(add)
		{
			// add it to the db
			q = "insert into data " +
				"(eq, record, digi_int, mom_mag, arias, dobry, pga, pgv, mean_per, epi_dist, foc_dist, rup_dist, vs30, foc_mech, location, owner, latitude, longitude, class, change, path, select1, analyze, select2) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			objects = new Object[] {
				eq,
				record,
				dig_int,
				mag,
				arias,
				dobry,
				pga,
				pgv,
				meanper,
				epidist,
				focdist,
				rupdist,
				vs30,
				focmechI,
				loc,
				owner,
				lat,
				lng,
				siteclassI,
				1,
				file,
				0,
				0,
				0,
			};
		}
		else
		{
			q = "update data set" +
				" eq=?" +
				", record=?" +
				", digi_int=?" +
				", mom_mag=?" +
				", arias=?" +
				", dobry=?" +
				", pga=?" +
				", pgv=?" +
				", mean_per=?" +
				", epi_dist=?" +
				", foc_dist=?" +
				", rup_dist=?" +
				", vs30=?" +
				", foc_mech=?" +
				", location=?" +
				", owner=?" +
				", latitude=?" +
				", longitude=?" +
				", class=?" +
				"  where path=?";
			objects = new Object[] {
				eq,
				record,
				di,
				mag,
				arias,
				dobry,
				pga,
				pgv,
				meanper,
				epidist,
				focdist,
				rupdist,
				vs30,
				focmechI,
				loc,
				owner,
				lat,
				lng,
				siteclassI,
				file,
			};
		}

		PreparedStatement ps = Utils.getDB().preparedStatement(q);
		for(int i = 0; i < objects.length; i++) {
			if (objects[i] == nulls) {
				ps.setNull(i+1, Types.VARCHAR);
			} else {
				ps.setObject(i+1, objects[i]);
			}
		}
		ps.executeUpdate();
		ps.close();

		Utils.getDB().syncRecords("where eq=? and record=?", eq, record);
		return "";
	}
}
