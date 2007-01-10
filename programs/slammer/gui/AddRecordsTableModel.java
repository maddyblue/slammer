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
import javax.swing.table.DefaultTableModel;
import slammer.*;

public class AddRecordsTableModel extends DefaultTableModel
{
	public AddRecordsTableModel()
	{
		setColumnIdentifiers(SlammerTable.getColumnList(SlammerTable.colAbbrev, SlammerTable.colImport, SlammerTable.IMTBL));
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
