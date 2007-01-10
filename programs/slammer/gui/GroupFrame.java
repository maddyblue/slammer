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

package newmark.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;
import newmark.*;

class GroupFrame extends JFrame implements ActionListener
{
	SelectRecordsPanel parent;
	JButton retrieveB = new JButton("Retrieve");
	JComboBox deleteCB = new JComboBox();
	JButton addGroupB = new JButton("Add group");
	JTextField addField = new JTextField(5);
	JButton deleteGroupB = new JButton("Delete group");
	JButton closeB = new JButton("Close");
	JButton exportB = new JButton("Export...");
	JComboBox delim = new JComboBox();
	JComboBox exportCB = new JComboBox();
	JComboBox changeCB = new JComboBox();
	JFileChooser fc = new JFileChooser();
	NewmarkTableModel model;
	boolean canChange = true;

	public Vector list = new Vector();

	public GroupFrame(NewmarkTableModel model, SelectRecordsPanel parent)
	{
		super("Group manager");
		this.model = model;
		this.parent = parent;

		retrieveB.setActionCommand("retrieve");
		retrieveB.addActionListener(this);

		addGroupB.setActionCommand("add");
		addGroupB.addActionListener(this);

		deleteGroupB.setActionCommand("delete");
		deleteGroupB.addActionListener(this);

		exportB.setActionCommand("export");
		exportB.addActionListener(this);

		closeB.setActionCommand("close");
		closeB.addActionListener(this);

		delim.addItem("tab");
		delim.addItem("semi-colon");
		delim.addItem("comma");
		delim.addItem("colon");
		delim.addItem("space");

		addToList(deleteCB);
		addToList(changeCB);
		addToList(exportCB);

		JPanel north = new JPanel(new GridLayout(0, 3));
		north.setLayout(new GridLayout(0, 3));
		north.setBorder(GUIUtils.makeCompoundBorder(0, 0, 1, 0));

		north.add(new JLabel("Retrieve group:"));
		north.add(changeCB);
		north.add(retrieveB);

		north.add(new JLabel("Add group:"));
		north.add(addField);
		north.add(addGroupB);

		north.add(new JLabel("Delete group:"));
		north.add(deleteCB);
		north.add(deleteGroupB);

		JPanel south = new JPanel();
		south.setBorder(GUIUtils.makeCompoundBorder(1, 0, 0, 0));
		south.add(closeB);

		JPanel export = new JPanel(new BorderLayout());

		export.add(BorderLayout.NORTH, new JLabel("Export into a delimited text file:"));

		JPanel exportPanel = new JPanel(new VariableGridLayout(VariableGridLayout.FIXED_NUM_COLUMNS, 2));
		exportPanel.add(new JLabel("Delimiter:"));
		exportPanel.add(delim);
		exportPanel.add(new JLabel("Records:"));
		exportPanel.add(exportCB);

		export.add(BorderLayout.WEST, exportPanel);
		export.add(BorderLayout.SOUTH, exportB);

		Container c = getContentPane();
		c.setLayout(new BorderLayout());

		c.add(BorderLayout.NORTH, north);
		c.add(BorderLayout.WEST, export);
		c.add(BorderLayout.SOUTH, south);

		try
		{
			updateGroupList();
		}
		catch(Exception ex)
		{
			Utils.catchException(ex);
		}

		pack();

		GUIUtils.setLocationMiddle(this);
	}


	public void addToList(JComboBox add)
	{
		list.add(add);
	}

	public void updateGroupList() throws Exception
	{
		canChange = false;
		Object[][] names = Utils.getDB().runQuery("select distinct name from grp order by name");
		JComboBox cur;
		Object sel = changeCB.getSelectedItem();
		for(int i = 0; i < list.size(); i++)
		{
			cur = (JComboBox)list.elementAt(i);
			if(cur == null)
				continue;
			cur.removeAllItems();

			if(cur.equals(exportCB))
			{
				cur.addItem("Use selected records");
				cur.addItem(" -- Groups -- ");
			}

			if(names == null)
				continue;

			for(int j = 1; j < names.length; j++)
				cur.addItem(names[j][0]);
		}
		if(sel != null) changeCB.setSelectedItem(sel);
		canChange = true;
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			System.out.println(command);
			if(command.equals("add"))
			{
				canChange = false;

				Object[][] res = Utils.getDB().runQuery("select id,analyze from data where select2=1");
				if(res == null)
				{
					addField.setText("Nothing to add");
					return;
				}

				String name = addField.getText();
				Utils.getDB().runUpdate("delete from grp where name='" + name + "'");
				for(int i = 1; i < res.length; i++)
					Utils.getDB().runUpdate("insert into grp values(" + res[i][0] + ", '" + name + "', " + res[i][1] + ")");
				updateGroupList();
				if(changeCB.getSelectedItem() == null) changeCB.setSelectedItem(name);
				canChange = true;

				Utils.updateEQLists();
			}
			else if(command.equals("close"))
			{
				setVisible(false);
			}
			else if(command.equals("delete"))
			{
				if(deleteCB.getItemCount() == 0) return;

				canChange = false;
				String s = deleteCB.getSelectedItem().toString();
				Utils.getDB().runUpdate("delete from grp where name='" + s + "'");
				if(changeCB.getSelectedItem() == s) changeCB.setSelectedItem(null);
				updateGroupList();
				canChange = true;

				Utils.updateEQLists();
			}
			else if(command.equals("export"))
			{
				int index = exportCB.getSelectedIndex();
				if(index == 1)
				{
					GUIUtils.popupError("Invalid record selection.");
					return;
				}

				int n = fc.showSaveDialog(this);
				if(n == JFileChooser.APPROVE_OPTION)
				{
					File f = fc.getSelectedFile();
					FileWriter fw = new FileWriter(f);

					String del;
					String d = delim.getSelectedItem().toString();
					if(d.equals("space"))
						del = " ";
					else if(d.equals("colon"))
						del = ":";
					else if(d.equals("semi-colon"))
						del = ";";
					else if(d.equals("comma"))
						del = ",";
					else
						del = "\t";

					int list[];
					Object[][] res;

					if(index == 0)
					{
						res = Utils.getDB().runQuery("select id from data where select2=1 order by eq, record");
					}
					else
					{
						String group = exportCB.getSelectedItem().toString();
						res = Utils.getDB().runQuery("select record from grp where name='" + group + "'");
					}

					if(res == null || res.length <= 1)
					{
						list = new int[0];
					}
					else
					{
						list = new int[res.length - 1];
						for(int i = 0; i < list.length; i++)
							list[i] = Integer.parseInt(res[i + 1][0].toString());
					}

					delimize(fw, del,
						NewmarkTable.makeUnitName(NewmarkTable.rowEarthquake),
						NewmarkTable.makeUnitName(NewmarkTable.rowRecord),
						NewmarkTable.makeUnitName(NewmarkTable.rowDigInt),
						NewmarkTable.makeUnitName(NewmarkTable.rowMagnitude),
						NewmarkTable.makeUnitName(NewmarkTable.rowAriasInt),
						NewmarkTable.makeUnitName(NewmarkTable.rowDuration),
						NewmarkTable.makeUnitName(NewmarkTable.rowPGA),
						NewmarkTable.makeUnitName(NewmarkTable.rowPGV),
						NewmarkTable.makeUnitName(NewmarkTable.rowMeanPer),
						NewmarkTable.makeUnitName(NewmarkTable.rowEpiDist),
						NewmarkTable.makeUnitName(NewmarkTable.rowFocalDist),
						NewmarkTable.makeUnitName(NewmarkTable.rowRupDist),
						NewmarkTable.makeUnitName(NewmarkTable.rowFocMech),
						NewmarkTable.makeUnitName(NewmarkTable.rowLocation),
						NewmarkTable.makeUnitName(NewmarkTable.rowOwner),
						NewmarkTable.makeUnitName(NewmarkTable.rowLat),
						NewmarkTable.makeUnitName(NewmarkTable.rowLng),
						NewmarkTable.makeUnitName(NewmarkTable.rowSiteClass),
						NewmarkTable.makeUnitName(NewmarkTable.rowFile)
					);

					int incr;
					for(int i = 0; i < list.length; i++)
					{
						res = Utils.getDB().runQuery("select eq, record, digi_int, mom_mag, arias, dobry, pga, pgv, mean_per, epi_dist, foc_dist, rup_dist, foc_mech, location, owner, latitude, longitude, class, path from data where id=" + list[i]);

						if(res == null || res.length <= 1)
							continue;

						incr = 0;
						delimize(fw, del,
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++]),
							Utils.shorten(res[1][incr++])
						);
					}

					fw.close();

					JOptionPane.showMessageDialog(null, "Export completed.", "Export complete", JOptionPane.INFORMATION_MESSAGE);
				}
			}
			else if(command.equals("retrieve"))
			{
				if(!canChange) return;
				if(changeCB.getSelectedItem() == null) return;

				Object[][] res = Utils.getDB().runQuery("select record, analyze from grp where name='" +
				changeCB.getSelectedItem().toString() + "'");
				Utils.getDB().runUpdate("update data set select2=0 where select2=1");
				for(int i = 1; i < res.length; i++)
					Utils.getDB().runUpdate("update data set select2=1, analyze=" + res[i][1].toString() + " where id=" + res[i][0].toString());
				if(model != null) model.setModel(NewmarkTable.REFRESH);

				parent.updateSelectLabel();
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	private void delimize(FileWriter fw, String delim, String eq, String record, String di, String mag, String arias, String dobry, String pga, String pgv, String meanper, String epi, String foc, String rup, String mech, String location, String owner, String lat, String lng, String site, String path) throws IOException
	{
		fw.write(
			          eq
			+ delim + record
			+ delim + di
			+ delim + mag
			+ delim + arias
			+ delim + dobry
			+ delim + pga
			+ delim + pgv
			+ delim + meanper
			+ delim + epi
			+ delim + foc
			+ delim + rup
			+ delim + mech
			+ delim + location
			+ delim + owner
			+ delim + lat
			+ delim + lng
			+ delim + site
			+ delim + path
			+ "\n");
	}
}