/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.ArrayList;
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
	JButton selectRecord = new JButton("Select record(s)");

	JCheckBox FocMechAll = new JCheckBox("All", true);
	JCheckBox FocMechStrikeSlip = new JCheckBox(SlammerTable.FocMechArray[SlammerTable.FMStrikeSlip]);
	JCheckBox FocMechReverse = new JCheckBox(SlammerTable.FocMechArray[SlammerTable.FMReverse]);
	JCheckBox FocMechNormal = new JCheckBox(SlammerTable.FocMechArray[SlammerTable.FMNormal]);
	JCheckBox FocMechObliqueReverse = new JCheckBox(SlammerTable.FMObliqueReverseLong);
	JCheckBox FocMechObliqueNormal = new JCheckBox(SlammerTable.FMObliqueNormalLong);

	JCheckBox SiteClassAll = new JCheckBox("All", true);
	JCheckBox SiteClassA = new JCheckBox(SlammerTable.SiteClassArray[SlammerTable.SCA]);
	JCheckBox SiteClassB = new JCheckBox(SlammerTable.SiteClassArray[SlammerTable.SCB]);
	JCheckBox SiteClassC = new JCheckBox(SlammerTable.SiteClassArray[SlammerTable.SCC]);
	JCheckBox SiteClassD = new JCheckBox(SlammerTable.SiteClassArray[SlammerTable.SCD]);
	JCheckBox SiteClassE = new JCheckBox(SlammerTable.SiteClassArray[SlammerTable.SCE]);

	JButton selectNone = new JButton("Deselect all for analysis");
	JButton selectAll = new JButton("Select all for analysis");
	JButton emptyTable = new JButton("Clear table");
	JButton deleteSelected = new JButton("Clear highlighted record(s)");

	JButton groupManage = new JButton("Manage groups...");
	GroupFrame groupFrame;

	SlammerTable table;
	JButton next = new JButton("Go to Step 2: Select Analyses");
	JLabel selectLabel = new JLabel();
	boolean isSlammer;

	public SelectRecordsPanel(SlammerTabbedPane parent, boolean isSlammer) throws Exception
	{
		this.parent = parent;
		this.isSlammer = isSlammer;

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
		SiteClassA.setActionCommand("siteClassOther");
		SiteClassB.setActionCommand("siteClassOther");
		SiteClassC.setActionCommand("siteClassOther");
		SiteClassD.setActionCommand("siteClassOther");
		SiteClassE.setActionCommand("siteClassOther");

		FocMechAll.addActionListener(this);
		FocMechStrikeSlip.addActionListener(this);
		FocMechReverse.addActionListener(this);
		FocMechNormal.addActionListener(this);
		FocMechObliqueReverse.addActionListener(this);
		FocMechObliqueNormal.addActionListener(this);

		SiteClassAll.addActionListener(this);
		SiteClassA.addActionListener(this);
		SiteClassB.addActionListener(this);
		SiteClassC.addActionListener(this);
		SiteClassD.addActionListener(this);
		SiteClassE.addActionListener(this);

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
		JPanel selectHeaderArrayList = new JPanel(new BorderLayout());
		selectHeaderArrayList.add(BorderLayout.WEST, createParmsPanel());

		ArrayList checkBoxesArrayList = new ArrayList(2);
		checkBoxesArrayList.add(createSiteClassPanel());

		JPanel ret = new JPanel(new BorderLayout());
		ret.add(BorderLayout.NORTH, selectHeaderArrayList);
		ret.add(BorderLayout.WEST, GUIUtils.makeRecursiveLayoutDown(checkBoxesArrayList));
		ret.add(BorderLayout.SOUTH, createFocMechPanel());
		return ret;
	}

	private JPanel createFocMechPanel()
	{
		ArrayList list = new ArrayList();
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
		ArrayList list = new ArrayList();
		list.add(new JLabel(SlammerTable.fieldArray[SlammerTable.rowSiteClass][SlammerTable.colFieldName].toString() + ": "));
		list.add(SiteClassAll);
		list.add(SiteClassA);
		list.add(SiteClassB);
		list.add(SiteClassC);
		list.add(SiteClassD);
		list.add(SiteClassE);

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
		ArrayList list = new ArrayList();
		list.add(panel);
		panel = GUIUtils.makeRecursiveLayoutDown(list);
		list = new ArrayList();
		list.add(panel);
		panel = GUIUtils.makeRecursiveLayoutRight(list);

		return panel;
	}

	private JPanel createSlammerTablePanel() throws Exception
	{
		JPanel searchTablePanel = new JPanel(new BorderLayout());

		JPanel header = new JPanel(new BorderLayout());

		if(isSlammer)
		{
			JPanel headerEast = new JPanel(new GridLayout(1, 0));
			headerEast.add(selectAll);
			headerEast.add(selectNone);
			header.add(BorderLayout.EAST, headerEast);
		}

		JLabel label = new JLabel("Records selected (units as indicated above):");
		label.setFont(GUIUtils.headerFont);
		header.add(BorderLayout.WEST, label);
		header.setBorder(GUIUtils.makeCompoundBorder(3, 0, 0, 0));

		table = new SlammerTable(true, isSlammer);

		JPanel footer = new JPanel(new BorderLayout());
		Box footerEast = new Box(BoxLayout.X_AXIS);
		footerEast.add(emptyTable);
		footerEast.add(deleteSelected);
		if(isSlammer) footerEast.add(next);
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
				SiteClassA.setSelected(false);
				SiteClassB.setSelected(false);
				SiteClassC.setSelected(false);
				SiteClassD.setSelected(false);
				SiteClassE.setSelected(false);
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
				SiteClassWhere += makeCheckBoxString(SiteClassWhere, dbname, Integer.toString(SlammerTable.SCA), SiteClassA);
				SiteClassWhere += makeCheckBoxString(SiteClassWhere, dbname, Integer.toString(SlammerTable.SCB), SiteClassB);
				SiteClassWhere += makeCheckBoxString(SiteClassWhere, dbname, Integer.toString(SlammerTable.SCC), SiteClassC);
				SiteClassWhere += makeCheckBoxString(SiteClassWhere, dbname, Integer.toString(SlammerTable.SCD), SiteClassD);
				SiteClassWhere += makeCheckBoxString(SiteClassWhere, dbname, Integer.toString(SlammerTable.SCE), SiteClassE);
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
					SiteClassA.setSelected(false);
					SiteClassB.setSelected(false);
					SiteClassC.setSelected(false);
					SiteClassD.setSelected(false);
					SiteClassE.setSelected(false);
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

		String selectText;

		if(isSlammer)
			selectText = ret1[1][0].toString() + " of " + ret2[1][0].toString() + " records selected for analysis";
		else
			selectText = ret2[1][0].toString() + " records";

		selectLabel.setText(selectText);
	}
}
