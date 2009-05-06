/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import slammer.*;

class GroupFrame extends JFrame implements ActionListener
{
	SelectRecordsPanel parent;
	JButton retrieveB = new JButton("Retrieve group");
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
	SlammerTableModel model;
	boolean canChange = true;

	public ArrayList list = new ArrayList();

	public GroupFrame(SlammerTableModel model, SelectRecordsPanel parent)
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

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);

		int x = 0;
		int y = 0;
		JLabel label;
		Border b = GUIUtils.makeCompoundBorder(1, 0, 0, 0);

		c.gridx = x++;
		c.gridy = y++;
		c.fill = GridBagConstraints.BOTH;
		label = new JLabel("Retrieve group:");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x++;
		gridbag.setConstraints(changeCB, c);
		add(changeCB);

		c.gridx = x;
		gridbag.setConstraints(retrieveB, c);
		add(retrieveB);

		x = 0;
		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Add group:");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x++;
		gridbag.setConstraints(addField, c);
		add(addField);

		c.gridx = x;
		gridbag.setConstraints(addGroupB, c);
		add(addGroupB);

		x = 0;
		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Delete group:");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x++;
		gridbag.setConstraints(deleteCB, c);
		add(deleteCB);

		c.gridx = x;
		gridbag.setConstraints(deleteGroupB, c);
		add(deleteGroupB);

		x = 0;
		c.gridx = x++;
		c.gridy = y++;
		c.gridwidth = 3;
		label = new JLabel("Export into a delimited text file:");
		label.setBorder(b);
		gridbag.setConstraints(label, c);
		add(label);

		c.gridy = y++;
		c.gridwidth = 1;
		label = new JLabel("Delimiter:");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x++;
		gridbag.setConstraints(delim, c);
		add(delim);

		c.gridx = x;
		c.gridheight = 2;
		gridbag.setConstraints(exportB, c);
		add(exportB);

		x = 0;
		c.gridy = y++;
		c.gridx = x++;
		c.gridheight = 1;
		label = new JLabel("Records:");
		gridbag.setConstraints(label, c);
		add(label);

		c.gridx = x--;
		gridbag.setConstraints(exportCB, c);
		add(exportCB);

		c.gridx = x;
		c.gridy = y++;
		c.gridwidth = 3;
		label = new JLabel("");
		label.setBorder(b);
		gridbag.setConstraints(label, c);
		add(label);

		c.gridy = y;
		gridbag.setConstraints(closeB, c);
		add(closeB);

		try
		{
			updateGroupList();
		}
		catch(Exception ex)
		{
			Utils.catchException(ex);
		}

		pack();
		setLocationRelativeTo(null);
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
			cur = (JComboBox)list.get(i);
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
			if(command.equals("add"))
			{
				String name = addField.getText();

				if(name == null || name.length() == 0)
				{
					GUIUtils.popupError("Enter a name.");
					return;
				}
				else if(Integer.parseInt(Utils.getDB().runQuery("select count(*) from data where select2=1")[1][0].toString()) == 0)
				{
					GUIUtils.popupError("No records selected.");
					return;
				}

				canChange = false;
				Utils.getDB().runUpdate("delete from grp where name='" + name + "'");
				Utils.getDB().runUpdate("insert into grp select id, '" + name + "', analyze from data where select2=1");
				updateGroupList();
				if(changeCB.getSelectedItem() == null) changeCB.setSelectedItem(name);
				canChange = true;
				addField.setText("");

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

					delimize(fw, del,
						SlammerTable.makeUnitName(SlammerTable.rowEarthquake, false),
						SlammerTable.makeUnitName(SlammerTable.rowRecord, false),
						SlammerTable.makeUnitName(SlammerTable.rowDigInt, false),
						SlammerTable.makeUnitName(SlammerTable.rowMagnitude, false),
						SlammerTable.makeUnitName(SlammerTable.rowAriasInt, false),
						SlammerTable.makeUnitName(SlammerTable.rowDuration, false),
						SlammerTable.makeUnitName(SlammerTable.rowPGA, false),
						SlammerTable.makeUnitName(SlammerTable.rowPGV, false),
						SlammerTable.makeUnitName(SlammerTable.rowMeanPer, false),
						SlammerTable.makeUnitName(SlammerTable.rowEpiDist, false),
						SlammerTable.makeUnitName(SlammerTable.rowFocalDist, false),
						SlammerTable.makeUnitName(SlammerTable.rowRupDist, false),
						SlammerTable.makeUnitName(SlammerTable.rowVs30, false),
						SlammerTable.makeUnitName(SlammerTable.rowSiteClass, false),
						SlammerTable.makeUnitName(SlammerTable.rowFocMech, false),
						SlammerTable.makeUnitName(SlammerTable.rowLocation, false),
						SlammerTable.makeUnitName(SlammerTable.rowOwner, false),
						SlammerTable.makeUnitName(SlammerTable.rowLat, false),
						SlammerTable.makeUnitName(SlammerTable.rowLng, false),
						SlammerTable.makeUnitName(SlammerTable.rowFile, false)
					);

					String query = "select eq, record, digi_int, mom_mag, arias, dobry, pga, pgv, mean_per, epi_dist, foc_dist, rup_dist, vs30, class, foc_mech, location, owner, latitude, longitude, path from data where ";

					if(index == 0)
						query += "select2=1";
					else
						query += "id in (select record from grp where name='" + exportCB.getSelectedItem().toString() + "')";
					query += " order by eq, record";

					Object[][] res = Utils.getDB().runQuery(query);

					for(int i = 1; res != null && i < res.length; i++)
					{
						delimize(fw, del,
							Utils.shorten(res[i][Slammer.DB_eq]),
							Utils.shorten(res[i][Slammer.DB_record]),
							Utils.shorten(res[i][Slammer.DB_digi_int]),
							Utils.shorten(res[i][Slammer.DB_mom_mag]),
							Utils.shorten(res[i][Slammer.DB_arias]),
							Utils.shorten(res[i][Slammer.DB_dobry]),
							Utils.shorten(res[i][Slammer.DB_pga]),
							Utils.shorten(res[i][Slammer.DB_pgv]),
							Utils.shorten(res[i][Slammer.DB_mean_per]),
							Utils.shorten(res[i][Slammer.DB_epi_dist]),
							Utils.shorten(res[i][Slammer.DB_foc_dist]),
							Utils.shorten(res[i][Slammer.DB_rup_dist]),
							Utils.shorten(res[i][Slammer.DB_vs30]),
							Utils.shorten(res[i][Slammer.DB_class]),
							Utils.shorten(res[i][Slammer.DB_foc_mech]),
							Utils.shorten(res[i][Slammer.DB_location]),
							Utils.shorten(res[i][Slammer.DB_owner]),
							Utils.shorten(res[i][Slammer.DB_latitude]),
							Utils.shorten(res[i][Slammer.DB_longitude]),
							Utils.shorten(res[i][Slammer.DB_LENGTH]) // path
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

				String grp = changeCB.getSelectedItem().toString();
				Utils.getDB().runUpdate("update data set select2=0 where select2=1");
				Utils.getDB().runUpdate("update data set select2=1, analyze=0 where id in (select record from grp where name='" + grp + "' and analyze=0)");
				Utils.getDB().runUpdate("update data set select2=1, analyze=1 where id in (select record from grp where name='" + grp + "' and analyze=1)");
				if(model != null) model.setModel(SlammerTable.REFRESH);

				parent.updateSelectLabel();
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	private void delimize(FileWriter fw, String delim, String eq, String record, String di, String mag, String arias, String dobry, String pga, String pgv, String meanper, String epi, String foc, String rup, String vs30, String siteclass, String mech, String location, String owner, String lat, String lng, String path) throws IOException
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
			+ delim + vs30
			+ delim + siteclass
			+ delim + mech
			+ delim + location
			+ delim + owner
			+ delim + lat
			+ delim + lng
			+ delim + path
			+ "\n");
	}
}
