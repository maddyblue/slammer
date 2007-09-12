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

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.DecimalFormat;
import slammer.*;
import slammer.analysis.*;

class SlammerTableModel extends DefaultTableModel implements SlammerTableInterface
{
	int currentModel;
	boolean selectTable;
	JComboBox primarySort, secondarySort, order;

	public SlammerTableModel(boolean selectTable, JComboBox primarySort, JComboBox secondarySort, JComboBox order) throws Exception
	{
		this.selectTable = selectTable;
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
		if (selectTable && getColumnCount() == (col + 1))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	// modelint should either be STATION, RECORD, SWAP, or REFRESH.
	public void setModel(int modelint) throws Exception
	{
		switch(modelint)
		{
			case SWAP:
				setModel(RSBOTH ^ currentModel);
				return;
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
			int rows = getRowCount();
			while(--rows >= 0)
				removeRow(rows);
			return;
		}

		Object cols[] = new Object[ret[0].length];
		for(int i = 0; i < cols.length; i++)
			cols[i] = SlammerTable.getColValue(colDBName, colAbbrev, ret[0][i].toString());

		Object data[][] = new Object[ret.length - 1][cols.length];
		for(int i = 0; i < data.length; i++)
			for(int j = 0; j < cols.length; j++)
			{
				if(ret[i + 1][j] == null)
				{
					data[i][j] = "";
				}
				else if(cols[j] == fieldArray[rowDigInt][colAbbrev])
				{
					String temp = ret[i + 1][j].toString();
					while(temp.endsWith("0"))
					{
						temp = temp.substring(0, temp.length() - 1);
					}
					data[i][j] = temp;
				}
				else if(cols[j] == fieldArray[rowAnalyze][colAbbrev])
				{
					data[i][j] = new Boolean(ret[i + 1][j].toString().equals("1"));
				}
				else if(cols[j] == fieldArray[rowFocMech][colAbbrev])
				{
					data[i][j] = FocMechArray[Integer.parseInt(ret[i + 1][j].toString())];
				}
				else if(cols[j] == fieldArray[rowSiteClass][colAbbrev])
				{
					data[i][j] = SiteClassArray[Integer.parseInt(ret[i + 1][j].toString())];
				}
				else if(
					cols[j] == fieldArray[rowAriasInt][colAbbrev] ||
					cols[j] == fieldArray[rowPGA][colAbbrev]
				)
				{
					data[i][j] = Analysis.fmtThree.format(Double.parseDouble(ret[i + 1][j].toString()));
				}
				else if(cols[j] == fieldArray[rowMeanPer][colAbbrev])
				{
					data[i][j] = Analysis.fmtTwo.format(Double.parseDouble(ret[i + 1][j].toString()));
				}
				else if(cols[j] == fieldArray[rowPGV][colAbbrev])
				{
					data[i][j] = Analysis.fmtOne.format(Double.parseDouble(ret[i + 1][j].toString()));
				}
				else
				{
					data[i][j] = ret[i + 1][j].toString();
				}
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
				ret += fieldArray[i][colDBName] + ",";
			}
		}
		// chop off the last comma
		if(ret.length() > 0) ret = ret.substring(0, ret.length() - 1);

		String ord = order.getSelectedItem().toString();
		String primaryOrder = "asc", secondaryOrder = "asc";
		if(ord.equals("A/D"))
		{
			secondaryOrder = "desc";
		}
		else if(ord.equals("D/A"))
		{
			primaryOrder = "desc";
		}
		else if(ord.equals("D/D"))
		{
			primaryOrder = "desc";
			secondaryOrder = "desc";
		}

		ret = "SELECT " + ret + " FROM data WHERE select" + (selectTable ? "2" : "1") + "=1 ORDER BY "
			+ SlammerTable.getColValue(colFieldName, colDBName, primarySort.getSelectedItem().toString()) + " " + primaryOrder + ","
			+ SlammerTable.getColValue(colFieldName, colDBName, secondarySort.getSelectedItem().toString()) + " " + secondaryOrder;
		return ret;
	}
}
