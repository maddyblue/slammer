/*
 * ParametersTableModel.java
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
	String right = "Crit. Accel. (g)";

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

