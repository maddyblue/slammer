/*
 * SelectRecordsPanel.java - the panel to search for and choose records
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
import newmark.*;

class SelectRecordsPanel extends JPanel implements ActionListener,TableModelListener
{
	NewmarkTabbedPane parent;

	String[][] searchList = NewmarkTable.getSearchList();
	JTextField[][] textFields = new JTextField[searchList.length][2];

	JButton searchButton = new JButton("Search for records");
	JButton clearButton = new JButton("Clear all search fields");
	JTextArea searchTA = new JTextArea();

	JComboBox eqList = new JComboBox();
	JComboBox recordList = new JComboBox();
	JButton selectRecord = new JButton("Add record");

	JCheckBox FocMechAll = new JCheckBox("All", true);
	JCheckBox FocMechStrikeSlip = new JCheckBox(NewmarkTable.FocMechArray[NewmarkTable.FMStrikeSlip]);
	JCheckBox FocMechReverse = new JCheckBox(NewmarkTable.FocMechArray[NewmarkTable.FMReverse]);
	JCheckBox FocMechNormal = new JCheckBox(NewmarkTable.FocMechArray[NewmarkTable.FMNormal]);
	JCheckBox FocMechObliqueReverse = new JCheckBox(NewmarkTable.FMObliqueReverseLong);
	JCheckBox FocMechObliqueNormal = new JCheckBox(NewmarkTable.FMObliqueNormalLong);

	JCheckBox SiteClassAll = new JCheckBox("All", true);
	JCheckBox SiteClassHardRock = new JCheckBox(NewmarkTable.SiteClassArray[NewmarkTable.SCHardRock]);
	JCheckBox SiteClassSoftRock = new JCheckBox(NewmarkTable.SiteClassArray[NewmarkTable.SCSoftRock]);
	JCheckBox SiteClassStiffSoil = new JCheckBox(NewmarkTable.SiteClassArray[NewmarkTable.SCStiffSoil]);
	JCheckBox SiteClassSoftSoil = new JCheckBox(NewmarkTable.SiteClassArray[NewmarkTable.SCSoftSoil]);

	JButton selectNone = new JButton("Deselect all for analysis");
	JButton selectAll = new JButton("Select all for analysis");
	JButton emptyTable = new JButton("Clear table");
	JButton deleteSelected = new JButton("Delete highlighted record(s)");

	JButton groupManage = new JButton("Manage groups...");
	GroupFrame groupFrame;

	NewmarkTable table;

	JButton next = new JButton("Go to Perform Rigid-Block Analysis page");

	JLabel selectLabel = new JLabel();

	public SelectRecordsPanel(NewmarkTabbedPane parent) throws Exception
	{
		this.parent = parent;

		searchButton.setActionCommand("search");
		searchButton.addActionListener(this);

		clearButton.setActionCommand("clear");
		clearButton.addActionListener(this);

		searchTA.setLineWrap(true);
		searchTA.setWrapStyleWord(true);
		searchTA.setEditable(false);
		searchTA.setBackground(GUIUtils.bg);

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

		searchTA.setRows(3);

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
		selectPanel.add(BorderLayout.CENTER, createNewmarkTablePanel());
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
		selectHeaderVector.add(BorderLayout.CENTER, createParmsRightPanel());

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
		list.add(new JLabel(NewmarkTable.fieldArray[NewmarkTable.rowFocMech][NewmarkTable.colFieldName].toString() + ": "));
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
		list.add(new JLabel(NewmarkTable.fieldArray[NewmarkTable.rowSiteClass][NewmarkTable.colFieldName].toString() + ": "));
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

		JPanel searchFields = new JPanel(new GridLayout(0, 3));
		searchFields.add(new JLabel(""));

		JLabel greaterLabel = new JLabel("Greater than or equal to:");
		greaterLabel.setHorizontalAlignment(JLabel.RIGHT);
		searchFields.add(greaterLabel);

		JLabel lessLabel = new JLabel("Less than or equal to:");
		lessLabel.setHorizontalAlignment(JLabel.RIGHT);
		searchFields.add(lessLabel);

		for(int i = 0; i < textFields.length; i++)
		{
			textFields[i][0] = new JTextField();
			textFields[i][1] = new JTextField();
			searchFields.add(new JLabel(searchList[i][0]));
			searchFields.add(textFields[i][0]);
			searchFields.add(textFields[i][1]);
		}

		searchPanel.add(BorderLayout.WEST, searchFields);

		return searchPanel;
	}

	private JPanel createParmsRightPanel()
	{
		Vector buttonsVector = new Vector(2);
		buttonsVector.add(searchButton);
		buttonsVector.add(clearButton);
		JPanel buttons = GUIUtils.makeRecursiveLayoutDown(buttonsVector);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		JPanel panel = new JPanel();
		panel.setLayout(gridbag);

		gridbag.setConstraints(buttons, c);
		panel.add(buttons);

		JPanel ret = new JPanel(new BorderLayout());
		ret.add(BorderLayout.WEST, panel);
		ret.add(BorderLayout.SOUTH, searchTA);

		return ret;
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

	private JPanel createNewmarkTablePanel() throws Exception
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

		table = new NewmarkTable(true);

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
						Utils.getDB().runQuery("update data set select2=true, analyze=true where eq='" + eq + "' and select2=false");
						table.setModel(NewmarkTable.REFRESH);
					}
					else
						table.addRecord(eq, record, true);
				}
				updateSelectLabel();
			}
			else if(command.equals("all"))
			{
				Utils.getDB().runQuery("update data set analyze=true where select2=true and analyze=false");
				table.setModel(NewmarkTable.REFRESH);
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

				FocMechAll.setSelected(false);
				FocMechStrikeSlip.setSelected(false);
				FocMechReverse.setSelected(false);
				FocMechNormal.setSelected(false);
				FocMechObliqueReverse.setSelected(false);
				FocMechObliqueNormal.setSelected(false);

				SiteClassAll.setSelected(false);
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
				groupFrame.show();
			}
			else if(command.equals("next"))
			{
				parent.selectParameters();
			}
			else if(command.equals("none"))
			{
				Utils.getDB().runQuery("update data set analyze=false where select2=true and analyze=true");
				table.setModel(NewmarkTable.REFRESH);
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
				String dbname = NewmarkTable.fieldArray[NewmarkTable.rowFocMech][NewmarkTable.colDBName].toString();
				FocMechWhere += makeCheckBoxString(FocMechWhere, dbname, Integer.toString(NewmarkTable.FMStrikeSlip), FocMechStrikeSlip);
				FocMechWhere += makeCheckBoxString(FocMechWhere, dbname, Integer.toString(NewmarkTable.FMReverse), FocMechReverse);
				FocMechWhere += makeCheckBoxString(FocMechWhere, dbname, Integer.toString(NewmarkTable.FMNormal), FocMechNormal);
				FocMechWhere += makeCheckBoxString(FocMechWhere, dbname, Integer.toString(NewmarkTable.FMObliqueReverse), FocMechObliqueReverse);
				FocMechWhere += makeCheckBoxString(FocMechWhere, dbname, Integer.toString(NewmarkTable.FMObliqueNormal), FocMechObliqueNormal);
				if(FocMechWhere.equals(""))
					FocMechAll.setSelected(true);
				if(FocMechAll.isSelected())
					FocMechWhere = "";

				String SiteClassWhere = "";
				dbname = NewmarkTable.fieldArray[NewmarkTable.rowSiteClass][NewmarkTable.colDBName].toString();
				SiteClassWhere += makeCheckBoxString(SiteClassWhere, dbname, Integer.toString(NewmarkTable.SCHardRock), SiteClassHardRock);
				SiteClassWhere += makeCheckBoxString(SiteClassWhere, dbname, Integer.toString(NewmarkTable.SCSoftRock), SiteClassSoftRock);
				SiteClassWhere += makeCheckBoxString(SiteClassWhere, dbname, Integer.toString(NewmarkTable.SCStiffSoil), SiteClassStiffSoil);
				SiteClassWhere += makeCheckBoxString(SiteClassWhere, dbname, Integer.toString(NewmarkTable.SCSoftSoil), SiteClassSoftSoil);
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
						Object[][] result = Utils.getDB().runQuery("update data set select2=true, analyze=true " + where);
						if(result == null)
						{
							searchTA.setText("Search complete. No records found.");
							return;
						}
						else
						{
							searchTA.setText("Search complete. " + (result[1][0].toString()) + " records found.");
							table.setModel(NewmarkTable.REFRESH);
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
		NewmarkTableModel model = table.getModel();
		if(model.isCellEditable(row, column) == false) return;

		try
		{
			Utils.getDB().set(
				model.getValueAt(row, 0).toString(),
				model.getValueAt(row, 1).toString(),
				("analyze=" + model.getValueAt(row, column).toString())
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
		Object[][] ret1 = Utils.getDB().runQuery("select count(*) from data where select2=true and analyze=true");
		Object[][] ret2 = Utils.getDB().runQuery("select count(*) from data where select2=true");
		if(ret1 == null)
			ret1 = new Object[][]{{null, "0"}};
		if(ret2 == null)
			ret2 = new Object[][]{{null, "0"}};

		selectLabel.setText(ret1[1][0].toString() + " of " + ret2[1][0].toString() + " records selected for analysis");
	}
}
