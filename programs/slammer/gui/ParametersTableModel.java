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

package newmark.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.table.*;
import javax.swing.border.*;
import newmark.*;

class ParametersTableModel extends DefaultTableModel implements TableModelListener
{
	String name;
	String right = "Critical Acceleration (g)";

	public ParametersTableModel(String left, String name)
	{
		this.name = name;
		addColumn(left);
		addColumn(right);
		addTableModelListener(this);
		addRow(new Object[] {"0", ""});
	}

	public boolean isCellEditable(int row, int col)
	{
		return (row != 0 || col != 0);
	}

	public void addRow()
	{
		addRow(new Object[] {"", ""});
	}

	public void setColName(String name)
	{
		this.name = name;
		Object i[] = new Object[2];
		i[0] = name;
		i[1] = right;

		setColumnIdentifiers(i);
	}

	public void tableChanged(TableModelEvent e)
	{
		int r = e.getFirstRow();
		int c = e.getColumn();
		Double d;
		double mid;
		if(c >= 0 && r >= 0)
		{
			String temp = getValueAt(r, c).toString().trim();
			if(temp != null && !temp.equals(""))
				d = (Double)Utils.checkNum(getValueAt(r, c).toString(), name + " table: row " + (r + 1) + ", column " + (c + 1), null, false, null, new Double(0), true, null, false);
			else
				return;
			if(d == null)
			{
				setValueAt("", r, c);
				return;
			}
		}
		if(c == 0)
		{
			d = (Double)Utils.checkNum(getValueAt(r, c).toString(), name + " table: row " + (r + 1) + ", column " + (c + 1), null, false, null, new Double(0), true, null, false);
			if(d == null) return;
			mid = d.doubleValue();
		}
		else
			return;
		if(r > 0)
		{
			String temp = getValueAt(r - 1, c).toString().trim();
			if(temp != null && !temp.equals(""))
			{
				d = (Double)Utils.checkNum(getValueAt(r - 1, c).toString(), name + " table: row " + r + ", column " + c, null, false, null, new Double(0), true, null, false);
				if(d == null) return;
				double top = d.doubleValue();
				if(top >= mid)
				{
					setValueAt("", r, c);
					GUIUtils.popupError(name + " values must be in ascending order: row " + (r + 1) + ", column " + (c + 1));
				}
			}
		}
		if(r < (getRowCount() - 1))
		{
			String temp = getValueAt(r + 1, c).toString().trim();
			if(temp != null && !temp.equals(""))
			{
				d = (Double)Utils.checkNum(getValueAt(r + 1, c).toString(), name + " table: row " + (r + 2) + ", column " + c, null, false, null, new Double(0), true, null, false);
				if(d == null) return;
				double bottom = d.doubleValue();
				if(mid >= bottom)
				{
					setValueAt("", r, c);
					GUIUtils.popupError(name + " values must be in ascending order: row " + (r + 1) + ", column " + (c + 1));
				}
			}
		}
	}
}

