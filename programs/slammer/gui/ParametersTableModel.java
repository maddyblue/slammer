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
import javax.swing.ListSelectionModel;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.table.*;
import javax.swing.border.*;
import slammer.*;

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

