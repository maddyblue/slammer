/* This file is in the public domain. */

package slammer.gui;

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import slammer.*;

class SlammerTable extends JPanel implements ActionListener, SlammerTableInterface
{
	SlammerTableModel model;
	JTable table;
	boolean selectTable;
	String selectStr;
	String[] sortList;
	JComboBox primarySort;
	JComboBox secondarySort;
	JComboBox order = new JComboBox();
	JRadioButton recordButton = new JRadioButton("Records", true);
	JRadioButton stationButton = new JRadioButton("Stations");
	ButtonGroup displayGroup = new ButtonGroup();

	/* selectTable determines if this is the table from the select records page, or
	 * from the records manager page.  If selectTable is false, the records manager
	 * page is used.
	 */
	public SlammerTable(boolean selectTable, boolean isSlammer) throws Exception
	{
		sortList = getSortList();
		primarySort = new JComboBox(sortList);
		secondarySort = new JComboBox(sortList);
		secondarySort.setSelectedIndex(1); // station

		this.selectTable = selectTable;
		selectStr = "select" + (selectTable ? 2 : 1);

		primarySort.setActionCommand("sort");
		primarySort.addActionListener(this);
		secondarySort.setActionCommand("sort");
		secondarySort.addActionListener(this);

		order.addItem("A/A");
		order.addItem("A/D");
		order.addItem("D/A");
		order.addItem("D/D");
		order.setActionCommand("sort");
		order.addActionListener(this);

		model = new SlammerTableModel(selectTable, isSlammer, primarySort, secondarySort, order);
		table = new JTable(model);

		recordButton.setActionCommand("record");
		recordButton.addActionListener(this);
		stationButton.setActionCommand("station");
		stationButton.addActionListener(this);
		displayGroup.add(recordButton);
		displayGroup.add(stationButton);

		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, new JScrollPane(table));

		JPanel north = new JPanel(new BorderLayout());

		ArrayList west = new ArrayList();
		west.add(new JLabel("Sort by "));
		west.add(primarySort);
		west.add(new JLabel(" then "));
		west.add(secondarySort);
		west.add(order);
		north.add(BorderLayout.WEST, GUIUtils.makeRecursiveLayoutRight(west));

		ArrayList east = new ArrayList();
		east.add(new JLabel("Display properties of: "));
		east.add(recordButton);
		east.add(stationButton);
		north.add(BorderLayout.EAST, GUIUtils.makeRecursiveLayoutRight(east));

		add(BorderLayout.NORTH, north);
	}

	private String[] getSortList()
	{
		ArrayList list = new ArrayList(fieldArray.length);
		for(int i = 0; i < fieldArray.length; i++)
			if(fieldArray[i][colSortField] == Boolean.TRUE) list.add(fieldArray[i][colDispName]);
		String[] slist = new String[list.size()];
		for(int i = 0; i < list.size(); i++)
			slist[i] = list.get(i).toString();
		return slist;
	}

	public void setModel(int modelint) throws Exception
	{
		model.setModel(modelint);
	}

	public void deleteSelected() throws Exception
	{
		deleteSelected(false);
	}

	public void deleteSelected(boolean fromDB) throws Exception
	{
		int[] rows = table.getSelectedRows();
		StringBuilder query = new StringBuilder("where id in (");

		for(int i = rows.length - 1; i >= 0; i--)
		{
			query.append(model.rowids[rows[i]]);
			if(i > 0)
				query.append(",");
		}
		query.append(")");

		Utils.getDB().runUpdate("update data set " + selectStr + "=0 " + query);
		model.setModel(REFRESH);

		if(fromDB)
		{
			Utils.getDB().runUpdate("delete from data " + query);
			Utils.updateEQLists();
		}
	}

	public void empty() throws Exception
	{
		Utils.getDB().runUpdate("update data set " + selectStr + "=0 where " + selectStr + "=1");
		model.setModel(REFRESH);
	}

	private void set(int row, String value) throws Exception
	{
		Utils.getDB().set(
			model.getValueAt(row, 0).toString(),
			model.getValueAt(row, 1).toString(),
			value
			);
	}

	public void addRecord(String eq, String record, boolean setAnalyze) throws Exception
	{
		String set = selectStr + "=1";
		if(setAnalyze)
			 set += ", analyze=1";

		Utils.getDB().set(eq, record, set);
		model.setModel(REFRESH);
	}

	public void addRecord(String eq, String record) throws Exception
	{
		addRecord(eq, record, false);
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			if(command.equals("record"))
			{
				int rows[] = table.getSelectedRows();
				model.setModel(RECORD);
				for(int i = 0; i < rows.length; i++)
					table.addRowSelectionInterval(rows[i], rows[i]);
			}
			else if(command.equals("sort"))
			{
				model.setModel(REFRESH);
			}
			else if(command.equals("station"))
			{
				int rows[] = table.getSelectedRows();
				model.setModel(STATION);
				for(int i = 0; i < rows.length; i++)
					table.addRowSelectionInterval(rows[i], rows[i]);
			}
		}
		catch(Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	public static String[][] getSearchList()
	{
		ArrayList rows = new ArrayList();
		for(int i = 0; i < fieldArray.length; i++)
			if(fieldArray[i][colSearchable] == Boolean.TRUE)
				rows.add(new Integer(i));

		String[][] list = new String[rows.size()][3];
		int num;
		for(int i = 0; rows.size() != 0; i++)
		{
			num = ((Integer)rows.remove(0)).intValue();
			if(fieldArray[num][colSearchable] == Boolean.TRUE)
			{
				list[i][0] = makeUnitName(num);
				list[i][1] = fieldArray[num][colDBSearch].toString();
				list[i][2] = fieldArray[num][colDispName].toString();
			}
		}

		return list;
	}

	public static String makeUnitName(int row)
	{
		return makeUnitName(row, true);
	}

	public static String makeUnitName(int row, boolean html)
	{
		StringBuilder ret = new StringBuilder();

		if(html)
			ret.append("<html>" + fieldArray[row][colFieldName].toString());
		else
			ret.append(fieldArray[row][colDispName].toString());

		if(fieldArray[row][colUnits].equals("") == false)
			ret.append(" (" + fieldArray[row][colUnits].toString() + ")");

		if(html)
			ret.append("</html>");

		return ret.toString();
	}

	public SlammerTableModel getModel()
	{
		return model;
	}

	public static String getColValue(int from, int to, String value)
	{
		for(int i = 0; i < fieldArray.length; i++)
			if(fieldArray[i][from].toString().equals(value)) return fieldArray[i][to].toString();
		return null;
	}

	public static Object[] getColumnList(int col, int compareCol, int contain)
	{
		ArrayList v = new ArrayList();
		for(int i = 0; i < fieldArray.length; i++)
		{
			if((contain & ((Integer)(fieldArray[i][compareCol])).intValue()) != 0)
			{
				v.add(fieldArray[i][col]);
			}
		}

		return v.toArray();
	}

	public ListSelectionModel getSelectionModel()
	{
		return table.getSelectionModel();
	}

	public int getSelectedRow()
	{
		return table.getSelectedRow();
	}
}
