/*
 * RecordTableModel.java - table model for the record table
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
import javax.swing.table.DefaultTableModel;
import newmark.*;

public class AddRecordsTableModel extends DefaultTableModel
{
	public AddRecordsTableModel()
	{
		setColumnIdentifiers(NewmarkTable.getColumnList(NewmarkTable.colAbbrev, NewmarkTable.colImport, NewmarkTable.IMTBL));
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
			if(cn.equals(NewmarkTable.fieldArray[NewmarkTable.rowImport][NewmarkTable.colAbbrev]))
			{
				dat[i] = new Boolean(true);
			}
			else if(cn.equals(NewmarkTable.fieldArray[NewmarkTable.rowFile][NewmarkTable.colAbbrev]))
			{
				dat[i] = s;
			}
			else if(cn.equals(NewmarkTable.fieldArray[NewmarkTable.rowEarthquake][NewmarkTable.colAbbrev]))
			{
				dat[i] = eq;
			}
			else if(cn.equals(NewmarkTable.fieldArray[NewmarkTable.rowRecord][NewmarkTable.colAbbrev]))
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
