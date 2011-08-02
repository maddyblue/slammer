/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.DecimalFormat;
import slammer.*;
import slammer.analysis.*;

class SlammerTableModel extends DefaultTableModel implements SlammerTableInterface
{
	int currentModel;
	boolean selectTable, isSlammer;
	JComboBox primarySort, secondarySort, order;

	public int[] rowids = null;

	public SlammerTableModel(boolean selectTable, boolean isSlammer, JComboBox primarySort, JComboBox secondarySort, JComboBox order) throws Exception
	{
		this.selectTable = selectTable;
		this.isSlammer = isSlammer;
		this.primarySort = primarySort;
		this.secondarySort = secondarySort;
		this.order = order;
		setModel(RECORD);
	}

	public Class getColumnClass(int c)
	{
		return getValueAt(0, c).getClass();
	}

	public boolean isCellEditable(int row, int col)
	{
		return selectTable && getColumnCount() == (col + 1) && isSlammer;
	}

	// modelint should either be STATION, RECORD, or REFRESH.
	public void setModel(int modelint) throws Exception
	{
		switch(modelint)
		{
			case REFRESH:
				break;
			case STATION:
			case RECORD:
				currentModel = modelint;
				break;
			default:
				return;
		}

		String search = getSearchString(currentModel);
		Object[][] ret = Utils.getDB().runQuery(search);

		if(ret == null)
		{
			setRowCount(0);
			return;
		}

		Object cols[] = new Object[ret[0].length - 1];
		for(int i = 0; i < cols.length; i++)
			cols[i] = SlammerTable.getColValue(colDBName, colAbbrev, ret[0][i].toString());

		Object data[][] = new Object[ret.length - 1][cols.length];
		rowids = new int[data.length];
		for(int i = 0; i < data.length; i++)
		{
			for(int j = 0; j < cols.length; j++)
			{
				if(ret[i + 1][j] == null)
					data[i][j] = "";
				else if(cols[j] == fieldArray[rowAnalyze][colAbbrev])
					data[i][j] = new Boolean(ret[i + 1][j].toString().equals("1"));
				else if(cols[j] == fieldArray[rowFocMech][colAbbrev])
					data[i][j] = FocMechArray[Integer.parseInt(ret[i + 1][j].toString())];
				else if(cols[j] == fieldArray[rowSiteClass][colAbbrev])
					data[i][j] = SiteClassArray[Integer.parseInt(ret[i + 1][j].toString())];
				else if(
					cols[j] == fieldArray[rowAriasInt][colAbbrev] ||
					cols[j] == fieldArray[rowPGA][colAbbrev]
				)
					data[i][j] = Analysis.fmtThree.format(Double.parseDouble(ret[i + 1][j].toString()));
				else if(cols[j] == fieldArray[rowMeanPer][colAbbrev])
					data[i][j] = Analysis.fmtTwo.format(Double.parseDouble(ret[i + 1][j].toString()));
				else if(cols[j] == fieldArray[rowPGV][colAbbrev])
					data[i][j] = Analysis.fmtOne.format(Double.parseDouble(ret[i + 1][j].toString()));
				else
					data[i][j] = ret[i + 1][j].toString();
			}

			rowids[i] = Integer.parseInt(ret[i + 1][cols.length].toString());
		}

		setDataVector(data, cols);
	}

	private String getSearchString(int model)
	{
		String ret = "";

		int col = selectTable ? colSelectDisplay : colManagerDisplay;

		for(int i = 0; i < fieldArray.length; i++)
		{
			if((model & ((Integer)(fieldArray[i][col])).intValue()) != 0)
			{
				if(!isSlammer && i == rowAnalyze)
					continue;

				ret += fieldArray[i][colDBName] + ",";
			}
		}
		// chop off the last comma
		if(ret.length() > 0) ret = ret.substring(0, ret.length() - 1);

		String ord = order.getSelectedItem().toString();
		String primaryOrder = "asc", secondaryOrder = "asc";
		if(ord.equals("Ascending/Descending"))
		{
			secondaryOrder = "desc";
		}
		else if(ord.equals("Descending/Ascending"))
		{
			primaryOrder = "desc";
		}
		else if(ord.equals("Descending/Descending"))
		{
			primaryOrder = "desc";
			secondaryOrder = "desc";
		}

		ret = "SELECT " + ret + ",id FROM data WHERE select" + (selectTable ? "2" : "1") + "=1 ORDER BY "
			+ SlammerTable.getColValue(colDispName, colDBSearch, primarySort.getSelectedItem().toString()) + " " + primaryOrder + ","
			+ SlammerTable.getColValue(colDispName, colDBSearch, secondarySort.getSelectedItem().toString()) + " " + secondaryOrder;
		return ret;
	}
}
