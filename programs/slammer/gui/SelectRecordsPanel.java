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

package slammer.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Vector;
import slammer.*;

class SelectRecordsPanel extends JPanel implements ActionListener,TableModelListener
{
	SlammerTabbedPane parent;

	String[][] searchList = SlammerTable.getSearchList();
	JTextField[][] textFields = new JTextField[searchList.length][2];

	JButton searchButton = new JButton("Search for records");
	JButton clearButton = new JButton("Clear all search fields");
	JLabel searchTA = new JLabel();

	JComboBox eqList = new JComboBox();
	JComboBox recordList = new JComboBox();
	JButton selectRecord = new JButton("Add record");

	JCheckBox FocMechAll = new JCheckBox("All", true);
	JCheckBox FocMechStrikeSlip = new JCheckBox(SlammerTable.FocMechArray[SlammerTable.FMStrikeSlip]);
	JCheckBox FocMechReverse = new JCheckBox(SlammerTable.FocMechArray[SlammerTable.FMReverse]);
	JCheckBox FocMechNormal = new JCheckBox(SlammerTable.FocMechArray[SlammerTable.FMNormal]);
	JCheckBox FocMechObliqueReverse = new JCheckBox(SlammerTable.FMObliqueReverseLong);
	JCheckBox FocMechObliqueNormal = new JCheckBox(SlammerTable.FMObliqueNormalLong);

	JCheckBox SiteClassAll = new JCheckBox("All", true);
	JCheckBox SiteClassHardRock = new JCheckBox(SlammerTable.SiteClassArray[SlammerTable.SCHardRock]);
	JCheckBox SiteClassSoftRock = new JCheckBox(SlammerTable.SiteClassArray[SlammerTable.SCSoftRock]);
	JCheckBox SiteClassStiffSoil = new JCheckBox(SlammerTable.SiteClassArray[SlammerTable.SCStiffSoil]);
	JCheckBox SiteClassSoftSoil = new JCheckBox(SlammerTable.SiteClassArray[SlammerTable.SCSoftSoil]);

	JButton selectNone = new JButton("Deselect all for analysis");
	JButton selectAll = new JButton("Select all for analysis");
	JButton emptyTable = new JButton("Clear table");
	JButton deleteSelected = new JButton("Delete highlighted record(s)");

	JButton groupManage = new JButton("Manage groups...");
	GroupFrame groupFrame;

	SlammerTable table;

	JButton next = new JButton("Go to Step 2: Select Analyses");

	JLabel selectLabel = new JLabel();

	public SelectRecordsPanel(SlammerTabbedPane parent) throws Exception
	{
		this.parent = parent;

		searchButton.setActionCommand("search");
		searchButton.addActionListener(this);

		clearButton.setActionCommand("clear");
		clearButton.addActionListener(this);

		FocMechAll.setActionCommand("focMechAll");
		FocMechStrikeSlip.setActionCommand("focMechOther");
		FocMechReverse.setActionCommand("focMechOther");
		FocMechNormal.setActionCommand("focMechOther");
		FocMechObliqueReverse.setActionCommand("focMechOther");
		FocMechObliqueNormal.setActionCommand("focMechOther");

		SiteClassAll.setActionCommand("SiteClassAll");
		SiteClassHardRock.setActionCommand("siteClassOther");
		SiteClassSoftRock.setActionCommand("siteClassOther");
		SiteClassStiffSoil.setActionCommand("siteClassOther");
		SiteClassSoftSoil.setActionCommand("siteClassOther");

		FocMechAll.addActionListener(this);
		FocMechStrikeSlip.addActionListener(this);
		FocMechReverse.addActionListener(this);
		FocMechNormal.addActionListener(this);
		FocMechObliqueReverse.addActionListener(this);
		FocMechObliqueNormal.addActionListener(this);

		SiteClassAll.addActionListener(this);
		SiteClassHardRock.addActionListener(this);
		SiteClassSoftRock.addActionListener(this);
		SiteClassStiffSoil.addActionListener(this);
		SiteClassSoftSoil.addActionListener(this);

		Utils.addEQList(eqList);
		Utils.updateRecordList(recordList, eqList);
		eqList.setActionCommand("eqListChange");
		eqList.addActionListener(this);

		selectRecord.setActionCommand("addRecord");
		selectRecord.addActionListener(this);

		selectNone.setActionCommand("none");
		selectNone.addActionListener(this);

		selectAll.setActionCommand("all");
		selectAll.addActionListener(this);

		emptyTable.setActionCommand("empty");
		emptyTable.addActionListener(this);

		deleteSelected.setActionCommand("deleteSelected");
		deleteSelected.addActionListener(this);

		next.setActionCommand("next");
		next.addActionListener(this);

		groupManage.setActionCommand("groupmanage");
		groupManage.addActionListener(this);

		updateSelectLabel();

		JPanel selectPanel = new JPanel(new BorderLayout());

		selectPanel.add(BorderLayout.NORTH, createTabbedPanel());
		selectPanel.add(BorderLayout.CENTER, createSlammerTablePanel());
		selectPanel.add(BorderLayout.SOUTH, selectLabel);

		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, selectPanel);

		table.getModel().addTableModelListener(this);
		groupFrame = new GroupFrame(table.getModel(), this);
	}

	private JTabbedPane createTabbedPanel()
	{
		JTabbedPane pane = new JTabbedPane();
		pane.addTab("Search records by properties", createSearchPanel());
		pane.addTab("Select individual records", createSelectIndivPanel());

		return pane;
	}

	private JPanel createSearchPanel()
	{
		JPanel selectHeaderVector = new JPanel(new BorderLayout());
		selectHeaderVector.add(BorderLayout.WEST, createParmsPanel());

		Vector checkBoxesVector = new Vector(2);
		checkBoxesVector.add(createFocMechPanel());

		JPanel ret = new JPanel(new BorderLayout());
		ret.add(BorderLayout.NORTH, selectHeaderVector);
		ret.add(BorderLayout.WEST, GUIUtils.makeRecursiveLayoutDown(checkBoxesVector));
		ret.add(BorderLayout.SOUTH, createSiteClassPanel());
		return ret;
	}

	private JPanel createFocMechPanel()
	{
		Vector list = new Vector();
		list.add(new JLabel(SlammerTable.fieldArray[SlammerTable.rowFocMech][SlammerTable.colFieldName].toString() + ": "));
		list.add(FocMechAll);
		list.add(FocMechStrikeSlip);
		list.add(FocMechNormal);
		list.add(FocMechReverse);
		list.add(FocMechObliqueNormal);
		list.add(FocMechObliqueReverse);

		return GUIUtils.makeRecursiveLayoutRight(list);
	}

	private JPanel createSiteClassPanel()
	{
		Vector list = new Vector();
		list.add(new JLabel(SlammerTable.fieldArray[SlammerTable.rowSiteClass][SlammerTable.colFieldName].toString() + ": "));
		list.add(SiteClassAll);
		list.add(SiteClassHardRock);
		list.add(SiteClassSoftRock);
		list.add(SiteClassStiffSoil);
		list.add(SiteClassSoftSoil);

		return GUIUtils.makeRecursiveLayoutRight(list);
	}

	private JPanel createParmsPanel()
	{
		JPanel searchPanel = new JPanel(new BorderLayout());

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		JPanel searchFields = new JPanel();
		searchFields.setLayout(gridbag);
		Component comp;

		int x = 1;
		int y = 0;

		c.gridx = x++;
		c.gridy = y++;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(0, 0, 0, 8);
		comp = new JLabel("Greater than or equal to:");
		gridbag.setConstraints(comp, c);
		searchFields.add(comp);

		c.gridx = x++;
		comp = new JLabel("Less than or equal to:");
		gridbag.setConstraints(comp, c);
		searchFields.add(comp);
		c.insets = new Insets(0, 0, 0, 0);

		for(int i = 0; i < textFields.length; i++)
		{
			textFields[i][0] = new JTextField(5);
			textFields[i][1] = new JTextField(5);

			x = 0;
			c.gridx = x++;
			c.gridy = y++;
			c.weightx = 0;
			comp = new JLabel(searchList[i][0]);
			gridbag.setConstraints(comp, c);
			searchFields.add(comp);

			c.gridx = x++;
			c.weightx = 1;
			comp = textFields[i][0];
			gridbag.setConstraints(comp, c);
			searchFields.add(comp);

			c.gridx = x++;
			comp = textFields[i][1];
			gridbag.setConstraints(comp, c);
			searchFields.add(comp);

			switch(i)
			{
				case 0: comp = searchButton; break;
				case 2: comp = clearButton; break;
				case 4: comp = searchTA; break;
				default: comp = null; break;
			}

			if(comp != null)
			{
				c.gridx = x++;
				c.weightx = 1;
				c.gridheight = 2;
				c.fill = GridBagConstraints.NONE;
				gridbag.setConstraints(comp, c);
				searchFields.add(comp);
				c.gridheight = 1;
				c.fill = GridBagConstraints.HORIZONTAL;
			}
		}

		searchPanel.add(BorderLayout.WEST, searchFields);

		return searchPanel;
	}

	private JPanel createSelectIndivPanel()
	{
		JPanel panel = new JPanel();
		GridLayout gl = new GridLayout(0, 3);
		gl.setHgap(10);
		panel.setLayout(gl);

		panel.add(new JLabel("Earthquake"));
		panel.add(new JLabel("Record name"));
		panel.add(new JLabel());
		panel.add(eqList);
		panel.add(recordList);
		panel.add(selectRecord);

		/* This is to make it not take up the entire space */
		Vector list = new Vector();
		list.add(panel);
		panel = GUIUtils.makeRecursiveLayoutDown(list);
		list = new Vector();
		list.add(panel);
		panel = GUIUtils.makeRecursiveLayoutRight(list);

		return panel;
	}

	private JPanel createSlammerTablePanel() throws Exception
	{
		JPanel searchTablePanel = new JPanel(new BorderLayout());

		JPanel header = new JPanel(new BorderLayout());
		JPanel headerEast = new JPanel(new GridLayout(1, 0));
		headerEast.add(selectAll);
		headerEast.add(selectNone);
		header.add(BorderLayout.EAST, headerEast);
		JLabel label = new JLabel("Records selected (units as indicated above):");
		label.setFont(GUIUtils.headerFont);
		header.add(BorderLayout.WEST, label);
		header.setBorder(GUIUtils.makeCompoundBorder(3, 0, 0, 0));

		table = new SlammerTable(true);

		JPanel footer = new JPanel(new BorderLayout());
		Box footerEast = new Box(BoxLayout.X_AXIS);
		footerEast.add(emptyTable);
		footerEast.add(deleteSelected);
		footerEast.add(next);
		footer.add(BorderLayout.EAST, footerEast);
		footer.add(BorderLayout.WEST, groupManage);

		searchTablePanel.add(BorderLayout.NORTH, header);
		searchTablePanel.add(BorderLayout.CENTER, table);
		searchTablePanel.add(BorderLayout.SOUTH, footer);

		return searchTablePanel;
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			System.out.println(command);
			if(command.equals("addRecord"))
			{
				if(eqList.getItemCount() == 0)
					return;

				String eq = eqList.getSelectedItem().toString();
				String record = recordList.getSelectedItem().toString();
				if(eq != null && record != null)
				{
					if(record == "Select all records")
					{
						Utils.getDB().runUpdate("update data set select2=1, analyze=1 where eq='" + eq + "' and select2=0");
						table.setModel(SlammerTable.REFRESH);
					}
					else
						table.addRecord(eq, record, true);
				}
				updateSelectLabel();
			}
			else if(command.equals("all"))
			{
				Utils.getDB().runUpdate("update data set analyze=1 where select2=1 and analyze=0");
				table.setModel(SlammerTable.REFRESH);
				updateSelectLabel();
			}
			else if(command.equals("clear"))
			{
				searchTA.setText("");
				for(int i1 = textFields.length - 1; i1 >= 0; i1--)
				{
					for(int i2 = textFields[i1].length - 1; i2 >= 0; i2--)
					{
						textFields[i1][i2].setText("");
					}
				}

				FocMechAll.setSelected(true);
				FocMechStrikeSlip.setSelected(false);
				FocMechReverse.setSelected(false);
				FocMechNormal.setSelected(false);
				FocMechObliqueReverse.setSelected(false);
				FocMechObliqueNormal.setSelected(false);

				SiteClassAll.setSelected(true);
				SiteClassHardRock.setSelected(false);
				SiteClassSoftRock.setSelected(false);
				SiteClassStiffSoil.setSelected(false);
				SiteClassSoftSoil.setSelected(false);
			}
			else if(command.equals("deleteSelected"))
			{
				table.deleteSelected();
				updateSelectLabel();
			}
			else if(command.equals("empty"))
			{
				table.empty();
				updateSelectLabel();
			}
			else if(command.equals("eqListChange"))
			{
				if(Utils.locked())
					return;

				Utils.updateRecordList(recordList, eqList.getSelectedItem().toString());
			}
			else if(command.equals("focMechAll"))
			{
				if(FocMechAll.isSelected())
				{
					FocMechStrikeSlip.setSelected(false);
					FocMechReverse.setSelected(false);
					FocMechNormal.setSelected(false);
					FocMechObliqueReverse.setSelected(false);
					FocMechObliqueNormal.setSelected(false);
				}
			}
			else if(command.equals("focMechOther"))
			{
				FocMechAll.setSelected(false);
			}
			else if(command.equals("groupmanage"))
			{
				groupFrame.setVisible(true);
			}
			else if(command.equals("next"))
			{
				parent.selectParameters();
			}
			else if(command.equals("none"))
			{
				Utils.getDB().runUpdate("update data set analyze=0 where select2=1 and analyze=1");
				table.setModel(SlammerTable.REFRESH);
				updateSelectLabel();
			}
			else if(command.equals("search"))
			{
				String where = "";
				String lefts, rights;
				Double left = null,right = null;
				for(int i = 0; i < textFields.length; i++)
				{
					left = null;
					right = null;
					lefts = textFields[i][0].getText().trim();
					if(!lefts.equals(""))
					{
						left = (Double)Utils.checkNum(lefts, searchList[i][0] + " greater than side", null, false, null, new Double(0), true, null, false);
						if(left == null) return;
					}
					rights = textFields[i][1].getText().trim();
					if(!rights.equals(""))
					{
						right = (Double)Utils.checkNum(rights, searchList[i][0] + " less than side", null, false, null, new Double(0), false, null, false);
						if(right == null) return;
					}
					if(left != null && right != null)
					{
						 if(Utils.checkNum(lefts, searchList[i][0] + " greater than side", right, true, "less than side", null, false, null, false) == null) return;
					}
					if(left != null)
						where += "and " + searchList[i][1] + ">=" + lefts + " ";
					if(right != null)
						where += "and " + searchList[i][1] + "<=" + rights + " ";
				}

				String FocMechWhere = "";
				String dbname = SlammerTable.fieldArray[SlammerTable.rowFocMech][SlammerTable.colDBName].toString();
				FocMechWhere += makeCheckBoxString(FocMechWhere, dbname, Integer.toString(SlammerTable.FMStrikeSlip), FocMechStrikeSlip);
				FocMechWhere += makeCheckBoxString(FocMechWhere, dbname, Integer.toString(SlammerTable.FMReverse), FocMechReverse);
				FocMechWhere += makeCheckBoxString(FocMechWhere, dbname, Integer.toString(SlammerTable.FMNormal), FocMechNormal);
				FocMechWhere += makeCheckBoxString(FocMechWhere, dbname, Integer.toString(SlammerTable.FMObliqueReverse), FocMechObliqueReverse);
				FocMechWhere += makeCheckBoxString(FocMechWhere, dbname, Integer.toString(SlammerTable.FMObliqueNormal), FocMechObliqueNormal);
				if(FocMechWhere.equals(""))
					FocMechAll.setSelected(true);
				if(FocMechAll.isSelected())
					FocMechWhere = "";

				String SiteClassWhere = "";
				dbname = SlammerTable.fieldArray[SlammerTable.rowSiteClass][SlammerTable.colDBName].toString();
				SiteClassWhere += makeCheckBoxString(SiteClassWhere, dbname, Integer.toString(SlammerTable.SCHardRock), SiteClassHardRock);
				SiteClassWhere += makeCheckBoxString(SiteClassWhere, dbname, Integer.toString(SlammerTable.SCSoftRock), SiteClassSoftRock);
				SiteClassWhere += makeCheckBoxString(SiteClassWhere, dbname, Integer.toString(SlammerTable.SCStiffSoil), SiteClassStiffSoil);
				SiteClassWhere += makeCheckBoxString(SiteClassWhere, dbname, Integer.toString(SlammerTable.SCSoftSoil), SiteClassSoftSoil);
				if(SiteClassWhere.equals(""))
					SiteClassAll.setSelected(true);
				if(SiteClassAll.isSelected())
					SiteClassWhere = "";

				where += fixCheckBoxWhere(FocMechWhere, SiteClassWhere);

				if(!where.equals(""))
				{
					where = "where " + where.substring(4);
					try
					{
						int result = Utils.getDB().runUpdate("update data set select2=1, analyze=1 " + where);
						if(result == 0)
						{
							searchTA.setText("Search complete. No records found.");
							return;
						}
						else
						{
							searchTA.setText("Search complete. " + result + " records found.");
							table.setModel(SlammerTable.REFRESH);
							updateSelectLabel();
						}
					}
					catch(Exception ex)
					{
						Utils.catchException(ex);
					}
				}
				else
				{
					searchTA.setText("No search parameters defined: nothing to search for.");
				}
			}
			else if(command.equals("SiteClassAll"))
			{
				if(SiteClassAll.isSelected())
				{
					SiteClassHardRock.setSelected(false);
					SiteClassSoftRock.setSelected(false);
					SiteClassStiffSoil.setSelected(false);
					SiteClassSoftSoil.setSelected(false);
				}
			}
			else if(command.equals("siteClassOther"))
			{
				SiteClassAll.setSelected(false);
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	public void tableChanged(TableModelEvent e)
	{
		int row = e.getFirstRow();
		int column = e.getColumn();
		SlammerTableModel model = table.getModel();
		if(model.isCellEditable(row, column) == false) return;

		try
		{
			Utils.getDB().set(
				model.getValueAt(row, 0).toString(),
				model.getValueAt(row, 1).toString(),
				("analyze=" + (model.getValueAt(row, column).toString().equals("true") ? "1" : "0"))
				);
			updateSelectLabel();
		}
		catch(Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	private String makeCheckBoxString(String append, String colName, String value, JCheckBox box)
	{
		String ret = "";
		if(box.isSelected() == true)
		{
			if(append.equals("") == false)
				ret = "or ";
			ret += colName + "=" + value + " ";
		}
		return ret;
	}

	private String fixCheckBoxWhere(String one, String two)
	{
		boolean oneb = (one.equals("") ? true : false);
		boolean twob = (two.equals("") ? true : false);
		if(oneb && twob) return "";
		if(oneb) return ("and ( " + two + ") ");
		if(twob) return ("and ( " + one + ") ");
		return ("and ( " + one + ") and ( " + two + ") ");
	}

	public void updateSelectLabel() throws Exception
	{
		Object[][] ret1 = Utils.getDB().runQuery("select count(*) from data where select2=1 and analyze=1");
		Object[][] ret2 = Utils.getDB().runQuery("select count(*) from data where select2=1");
		if(ret1 == null)
			ret1 = new Object[][]{{null, "0"}};
		if(ret2 == null)
			ret2 = new Object[][]{{null, "0"}};

		selectLabel.setText(ret1[1][0].toString() + " of " + ret2[1][0].toString() + " records selected for analysis");
	}
}
