/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import slammer.*;

public class AddRecordsTableModel extends DefaultTableModel
{
	private int fileCol;

	public AddRecordsTableModel()
	{
		setColumnIdentifiers(SlammerTable.getColumnList(SlammerTable.colAbbrev, SlammerTable.colImport, SlammerTable.IMTBL));

		for(fileCol = 0; getColumnName(fileCol) != SlammerTable.fieldArray[SlammerTable.rowFile][SlammerTable.colAbbrev]; fileCol++)
			;
	}

	public boolean isCellEditable(int row, int col)
	{
		if(col == 1)
			return false;

		return true;
	}

	public Class getColumnClass(int c)
	{
		return getValueAt(0, c).getClass();
	}

	public void empty()
	{
		int rows = getRowCount();
		while(--rows >= 0)
			removeRow(rows);
		return;
	}

	public void addRow(String s)
	{
		// check if this file is already here
		for(int i = 0; i < getRowCount(); i++)
		{
			if(((String)getValueAt(i, fileCol)).equals(s))
				return;
		}

		String sep = System.getProperty("file.separator");
		String name, eq = "";
		int locL, locR;

		locL = s.lastIndexOf(sep);
		locR = s.length();
		if(locL == -1)
		{
			name = s;
		}
		else
		{
			name = s.substring(locL + 1, locR);
		}

		locR = locL - 1;
		locL = s.lastIndexOf(sep, locR);
		if(locL != -1)
		{
			eq = s.substring(locL + 1, locR + 1);
		}

		Object dat[] = new Object[getColumnCount()];

		String cn;
		for(int i = 0; i < getColumnCount(); i++)
		{
			cn = getColumnName(i);
			if(cn.equals(SlammerTable.fieldArray[SlammerTable.rowImport][SlammerTable.colAbbrev]))
			{
				dat[i] = new Boolean(true);
			}
			else if(cn.equals(SlammerTable.fieldArray[SlammerTable.rowFile][SlammerTable.colAbbrev]))
			{
				dat[i] = s;
			}
			else if(cn.equals(SlammerTable.fieldArray[SlammerTable.rowEarthquake][SlammerTable.colAbbrev]))
			{
				dat[i] = eq;
			}
			else if(cn.equals(SlammerTable.fieldArray[SlammerTable.rowRecord][SlammerTable.colAbbrev]))
			{
				dat[i] = name;
			}
			else
			{
				dat[i] = "";
			}
		}

		addRow(dat);
	}
}
