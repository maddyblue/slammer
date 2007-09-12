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

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
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
	public SlammerTable(boolean selectTable) throws Exception
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

		model = new SlammerTableModel(selectTable, primarySort, secondarySort, order);
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

		Vector west = new Vector();
		west.add(new JLabel("Sort by "));
		west.add(primarySort);
		west.add(new JLabel(" then "));
		west.add(secondarySort);
		west.add(order);
		north.add(BorderLayout.WEST, GUIUtils.makeRecursiveLayoutRight(west));

		Vector east = new Vector();
		east.add(new JLabel("Display properties of: "));
		east.add(recordButton);
		east.add(stationButton);
		north.add(BorderLayout.EAST, GUIUtils.makeRecursiveLayoutRight(east));

		add(BorderLayout.NORTH, north);
	}

	private String[] getSortList()
	{
		Vector list = new Vector(fieldArray.length);
		for(int i = 0; i < fieldArray.length; i++)
			if(fieldArray[i][colSortField] == Boolean.TRUE) list.add(fieldArray[i][colFieldName]);
		String[] slist = new String[list.size()];
		for(int i = 0; i < list.size(); i++)
			slist[i] = list.elementAt(i).toString();
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
		Object[][] res;
		String eq, record;

		for(int i = rows.length - 1; i >= 0; i--)
		{
			eq = model.getValueAt(rows[i], 0).toString();
			record = model.getValueAt(rows[i], 1).toString();

			res = Utils.getDB().runQuery("select change from data where eq='" + eq + "' and record='" + record + "'");
			if(fromDB && (res == null || res.length <= 1))
				continue;

			set(rows[i], selectStr + "=0");

			if(fromDB)
				Utils.getDB().runUpdate("delete from data where eq='" + eq + "' and record='" + record + "'");
		}
		model.setModel(REFRESH);

		if(fromDB)
			Utils.updateEQLists();
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
			System.out.println(command);
			if(command.equals("record"))
			{
				model.setModel(RECORD);
			}
			else if(command.equals("sort"))
			{
				model.setModel(REFRESH);
			}
			else if(command.equals("station"))
			{
				model.setModel(STATION);
			}
		}
		catch(Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	public static String[][] getSearchList()
	{
		Vector rows = new Vector();
		for(int i = 0; i < fieldArray.length; i++)
			if(fieldArray[i][colSearchable] == Boolean.TRUE)
				rows.add(new Integer(i));

		String[][] list = new String[rows.size()][2];
		int num;
		for(int i = 0; rows.size() != 0; i++)
		{
			num = ((Integer)rows.remove(0)).intValue();
			if(fieldArray[num][colSearchable] == Boolean.TRUE)
			{
				list[i][0] = makeUnitName(num);
				list[i][1] = fieldArray[num][colDBName].toString();
			}
		}

		return list;
	}

	public static String makeUnitName(int row)
	{
		String ret = fieldArray[row][colFieldName].toString();
		if(fieldArray[row][colUnits].equals("") == false)
			ret += " (" + fieldArray[row][colUnits].toString() + ")";
		return ret;
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
		Vector v = new Vector();
		for(int i = 0; i < fieldArray.length; i++)
		{
			if((contain & ((Integer)(fieldArray[i][compareCol])).intValue()) != 0)
			{
				v.addElement(fieldArray[i][col]);
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
