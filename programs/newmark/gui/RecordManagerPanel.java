/*
 * RecordManagerPanel.java - manage records
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

/* $Id: RecordManagerPanel.java,v 1.2 2003/07/15 00:19:58 dolmant Exp $ */

package newmark.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.io.File;
import java.util.Vector;
import org.jfree.data.xy.*;
import org.jfree.chart.*;
import newmark.*;

class RecordManagerPanel extends JPanel implements ActionListener
{
	NewmarkTabbedPane parent;

	NewmarkTable table;

	JComboBox eqList = new JComboBox();
	JButton graph = new JButton("Graph selected record");
	JButton delete = new JButton("Delete selected record(s) from database");

	JButton save = new JButton("Save changes");

	JTextField modFile = new JTextField();
	JTextField modEq = new JTextField();
	JTextField modRec = new JTextField();
	JTextField modDI = new JTextField();
	JTextField modLoc = new JTextField();
	JTextField modMag = new JTextField();
	JTextField modOwn = new JTextField();
	JTextField modEpi = new JTextField();
	JTextField modLat = new JTextField();
	JTextField modFoc = new JTextField();
	JTextField modLng = new JTextField();
	JTextField modRup = new JTextField();
	JComboBox  modSite = new JComboBox(NewmarkTable.SiteClassArray);
	JComboBox  modMech = new JComboBox(NewmarkTable.FocMechArray);

	JButton add = new JButton("Add record(s)...");

	public RecordManagerPanel(NewmarkTabbedPane parent) throws Exception
	{
		this.parent = parent;

		table = new NewmarkTable(false);

		modFile.setEnabled(false);

		ListSelectionModel recordSelect = table.getSelectionModel();
		recordSelect.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent event)
			{
				try
				{
					recordSelect(event);
				}
				catch (Exception e)
				{
					Utils.catchException(e);
				}
			}
		});

		Utils.addEQList(eqList, Boolean.TRUE);
		eqList.setActionCommand("eqListChange");
		eqList.addActionListener(this);

		save.setMnemonic(KeyEvent.VK_S);
		save.setActionCommand("save");
		save.addActionListener(this);

		delete.setMnemonic(KeyEvent.VK_D);
		delete.setActionCommand("delete");
		delete.addActionListener(this);

		graph.setMnemonic(KeyEvent.VK_G);
		graph.setActionCommand("graph");
		graph.addActionListener(this);

		setLayout(new BorderLayout());
		add(BorderLayout.NORTH, createNorthPanel());
		add(BorderLayout.CENTER, table);
		add(BorderLayout.SOUTH, createSouthPanel());

		recordClear();
	}

	private JPanel createNorthPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());

		Vector list = new Vector();
		list.add(new JLabel("Display records from: "));
		list.add(eqList);
		panel.add(BorderLayout.WEST, GUIUtils.makeRecursiveLayoutRight(list));

		list = new Vector();
		list.add(graph);
		list.add(delete);
		panel.add(BorderLayout.EAST, GUIUtils.makeRecursiveLayoutRight(list));

		return panel;
	}

	private JPanel createSouthPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());

		JPanel north = new JPanel(new BorderLayout());

		JLabel label = new JLabel("Modify record:");
		label.setFont(GUIUtils.headerFont);

		JPanel file = new JPanel(new BorderLayout());
		file.add(BorderLayout.WEST, new JLabel("File Location "));
		file.add(BorderLayout.CENTER, modFile);

		north.add(BorderLayout.WEST, label);
		north.add(BorderLayout.EAST, save);
		north.add(BorderLayout.SOUTH, file);

		panel.add(BorderLayout.NORTH, north);

		JPanel south = new JPanel(new GridLayout(0, 4));
		south.add(new JLabel("Earthquake name"));
		south.add(modEq);
		south.add(new JLabel("     Record name"));
		south.add(modRec);
		south.add(new JLabel("Digitization Interval (s)"));
		south.add(modDI);
		south.add(new JLabel("     Location [optional]"));
		south.add(modLoc);
		south.add(new JLabel("Moment Magnitude [optional]"));
		south.add(modMag);
		south.add(new JLabel("     Station owner [optional]"));
		south.add(modOwn);
		south.add(new JLabel("Epicentral distance (km) [optional]"));
		south.add(modEpi);
		south.add(new JLabel("     Latitude [optional]"));
		south.add(modLat);
		south.add(new JLabel("Focal distance (km) [optional]"));
		south.add(modFoc);
		south.add(new JLabel("     Longitude [optional]"));
		south.add(modLng);
		south.add(new JLabel("Rupture distance (km) [optional]"));
		south.add(modRup);
		south.add(new JLabel("     Site Class [optional]"));
		south.add(modSite);
		south.add(new JLabel("Focal Mechanism [optional]"));
		south.add(modMech);

		panel.add(BorderLayout.SOUTH, south);

		return panel;
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			System.out.println(command);
			if(command.equals("delete"))
			{
				int n = JOptionPane.showConfirmDialog(this, "Do you want to delete these records?", "Delete?", JOptionPane.YES_NO_OPTION);
				if(n != JOptionPane.YES_OPTION)
					return;

				table.deleteSelected(true);
			}
			else if(command.equals("eqListChange"))
			{
				if(Utils.locked())
					return;

				boolean isEq = true;
				int index = eqList.getSelectedIndex();
				for(int i = 0; i < eqList.getItemCount(); i++)
				{
					if(((String)eqList.getItemAt(i)).equals(" -- Groups -- "))
					{
						if(i == index)
							return;
						else if(index > i)
							isEq = false;
						break;
					}
				}

				Utils.getDB().runQuery("update data set select1=false where select1=true");

				if(isEq)
				{
					String where;

					if(index == 0) // "All earthquakes"
						where = "";
					else
						where = "where eq='" + (String)eqList.getSelectedItem() + "'";

					Utils.getDB().runQuery("update data set select1=true " + where);
				}
				else
				{
					Object[][] res = Utils.getDB().runQuery("select record,analyze from group where name='" + (String)eqList.getSelectedItem() + "'");

					for(int i = 1; i < res.length; i++)
						Utils.getDB().runQuery("update data set select1=true where id=" + res[i][0].toString());
				}

				table.setModel(NewmarkTable.REFRESH);
				recordClear();
			}
			else if(command.equals("graph"))
			{
				int row = table.getSelectedRow();
				if(row == -1)
					return;

				String eq = table.getModel().getValueAt(row, 0).toString();
				String record = table.getModel().getValueAt(row, 1).toString();

				Object[][] res = null;
				res = Utils.getDB().runQuery("select path,digi_int from data where eq='" + eq + "' and record='" + record + "'");
				if(res == null) return;

				String path = res[1][0].toString();
				double di = Double.parseDouble(res[1][1].toString());

				File f = new File(path);
				if(f.canRead() == false)
				{
					GUIUtils.popupError("Cannot read or open the file " + path + ".");
					return;
				}

				DoubleList dat = new DoubleList(path);

				if(dat.bad())
				{
					GUIUtils.popupError("Invalid data at data point " + dat.badEntry() + " in " + path + ".");
					return;
				}

				XYSeries xys = new XYSeries("");
				dat.reset();

				Double val;
				double last1 = 0, last2 = 0, current;
				double diff1, diff2;
				double time = 0, timeStor = 0, td;
				int perSec = 50;
				double interval = 1.0 / (double)perSec;
				int i = 0;

				// add the first point
				if((val = dat.each()) != null)
				{
					xys.add(time, val);
					time += di;
					last2 = val.doubleValue();
				}

				// don't add the second point, but update the data
				if((val = dat.each()) != null)
				{
					time += di;
					last1 = val.doubleValue();
				}

				while((val = dat.each()) != null)
				{
					td = time - di;
					current = val.doubleValue();

					diff1 = last1 - current;
					diff2 = last1 - last2;

					if(
						(diff1 <= 0 && diff2 <= 0) ||
						(diff1 >= 0 && diff2 >= 0) ||
						(td >= (timeStor + interval)))
					{
						xys.add(td, last1);
						timeStor = td;
					}

					last2 = last1;
					last1 = current;
					time += di;
				}

				String title = eq + ": " + record;
				XYSeriesCollection xysc = new XYSeriesCollection(xys);
				JFreeChart chart = ChartFactory.createXYLineChart(title, "Time (s)", "Acceleration (cm/s/s)", xysc, org.jfree.chart.plot.PlotOrientation.VERTICAL, false, true, false);
				ChartFrame frame = new ChartFrame(eq + ": " + record, chart);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.show();
			}
			else if(command.equals("save"))
			{
				int n = JOptionPane.showConfirmDialog(this,"Are you sure you want to modify this records?", "Are you sure?", JOptionPane.YES_NO_OPTION);
				if(n != JOptionPane.YES_OPTION)
					return;

				String error = AddRecordsPanel.manipRecord(false,
					modFile.getText(),
					modEq.getText(),
					modRec.getText(),
					modDI.getText(),
					Utils.nullify(modMag.getText()),
					Utils.nullify(modEpi.getText()),
					Utils.nullify(modFoc.getText()),
					Utils.nullify(modRup.getText()),
					modMech.getSelectedItem().toString(),
					modLoc.getText(),
					modOwn.getText(),
					Utils.nullify(modLat.getText()),
					Utils.nullify(modLng.getText()),
					modSite.getSelectedItem().toString()
				);

				if(!error.equals(""))
					GUIUtils.popupError(error);

				table.setModel(NewmarkTable.REFRESH);
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	public void recordSelect(ListSelectionEvent e)
	{
		if(e == null) return;

		if (e.getValueIsAdjusting()) return;

		ListSelectionModel lsm = (ListSelectionModel)e.getSource();

		if (!lsm.isSelectionEmpty())
		{
			int selectedRow = lsm.getMinSelectionIndex();

			String eq = table.getModel().getValueAt(selectedRow, 0).toString();
			String record = table.getModel().getValueAt(selectedRow, 1).toString();

			Object[][] res = null;

			try
			{
				res = Utils.getDB().runQuery("select path, eq, record, trim(trailing '0' from digi_int), location, mom_mag, owner, epi_dist, latitude, foc_dist, longitude, rup_dist, class, foc_mech, change from data where eq='" + eq + "' and record='" + record + "'");
			}
			catch(Exception ex)
			{
				Utils.catchException(ex);
			}
			boolean enable = false;
			if(res != null)
			{
				if(res.length > 1)
				{
					int incr = 0;
					modFile.setText(res[1][incr++].toString());
					modEq.setText(res[1][incr++].toString());
					modRec.setText(res[1][incr++].toString());
					modDI.setText(res[1][incr++].toString());
					modLoc.setText(res[1][incr++].toString());
					modMag.setText(Utils.shorten(res[1][incr++]));
					modOwn.setText(res[1][incr++].toString());
					modEpi.setText(Utils.shorten(res[1][incr++]));
					modLat.setText(Utils.shorten(res[1][incr++]));
					modFoc.setText(Utils.shorten(res[1][incr++]));
					modLng.setText(Utils.shorten(res[1][incr++]));
					modRup.setText(Utils.shorten(res[1][incr++]));
					modSite.setSelectedItem(NewmarkTable.SiteClassArray[Integer.parseInt(Utils.shorten(res[1][incr++].toString()))]);
					modMech.setSelectedItem(NewmarkTable.FocMechArray[Integer.parseInt(Utils.shorten(res[1][incr++].toString()))]);
					enable = res[1][incr++].toString().equals("true") ? true : false; // Boolean.getBoolean() didn't work
				}
				else
				{
					recordClear();
				}
			}
			else
			{
				recordClear();
			}
			recordEnable(enable);
		}
	}

	public void recordEnable(boolean b)
	{
		modEq.setEnabled(b);
		modRec.setEnabled(b);
		modDI.setEnabled(b);
		modLoc.setEnabled(b);
		modMag.setEnabled(b);
		modOwn.setEnabled(b);
		modEpi.setEnabled(b);
		modLat.setEnabled(b);
		modFoc.setEnabled(b);
		modLng.setEnabled(b);
		modRup.setEnabled(b);
		modSite.setEnabled(b);
		modMech.setEnabled(b);
		save.setEnabled(b);
	}

	public void recordClear()
	{
		modFile.setText("");
		modEq.setText("");
		modRec.setText("");
		modDI.setText("");
		modLoc.setText("");
		modMag.setText("");
		modOwn.setText("");
		modEpi.setText("");
		modLat.setText("");
		modFoc.setText("");
		modLng.setText("");
		modRup.setText("");
		modSite.setSelectedItem("");
		modMech.setSelectedItem("");

		recordEnable(false);
	}
}
