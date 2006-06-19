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
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Vector;
import javax.swing.table.*;
import java.io.*;
import newmark.*;

class AddRecordsTable extends JPanel implements ActionListener
{
	AddRecordsTableModel model = new AddRecordsTableModel();
	JTable table = new JTable(model);

	int valueSel;
	static final int valueTF = 1;
	static final int valueFM = 2;
	static final int valueSC = 3;

	JTextField valueTextField = new JTextField(15);
	JComboBox valueFocMech = new JComboBox(NewmarkTable.FocMechArray);
	JComboBox valueSiteClass = new JComboBox(NewmarkTable.SiteClassArray);
	JComboBox colChoose = new JComboBox(NewmarkTable.getColumnList(NewmarkTable.colFieldName, NewmarkTable.colImport, NewmarkTable.IMCMB));
	JButton set = new JButton("Set");

	public AddRecordsTable()
	{
		TableColumn c = table.getColumn(NewmarkTable.fieldArray[NewmarkTable.rowFocMech][NewmarkTable.colAbbrev]);
		c.setCellEditor(new DefaultCellEditor(new JComboBox(NewmarkTable.FocMechArray)));

		c = table.getColumn(NewmarkTable.fieldArray[NewmarkTable.rowSiteClass][NewmarkTable.colAbbrev]);
		c.setCellEditor(new DefaultCellEditor(new JComboBox(NewmarkTable.SiteClassArray)));

		set.setActionCommand("set");
		set.addActionListener(this);

		colChoose.setActionCommand("colChoose");
		colChoose.addActionListener(this);

		valueFocMech.setVisible(false);
		valueSiteClass.setVisible(false);

		setLayout(new BorderLayout());

		valueSel = valueTF;
		Vector north = new Vector();
		north.add(new JLabel("Set all values in column "));
		north.add(colChoose);
		north.add(new JLabel(" to "));
		north.add(valueTextField);
		north.add(valueFocMech);
		north.add(valueSiteClass);
		north.add(set);

		Vector trueNorth = new Vector();
		trueNorth.add(GUIUtils.makeRecursiveLayoutRight(north));
		trueNorth.add(new JLabel("Earthquake name, Record name, and digitization interval (in seconds) must be specified; other fields are optional."));

		add(BorderLayout.NORTH, GUIUtils.makeRecursiveLayoutDown(trueNorth));

		JPanel t = new JPanel(new BorderLayout());
		t.add(BorderLayout.NORTH, table.getTableHeader());
		t.add(BorderLayout.CENTER, table);

		JScrollPane scroll = new JScrollPane(t);

		Dimension d = table.getPreferredScrollableViewportSize();
		d.setSize(1000, d.getHeight());
		table.setPreferredScrollableViewportSize(d);

		add(BorderLayout.CENTER, scroll);
	}

	public void addLocation(File[] flist)
	{
		File files[];
		File f;

		for(int i = 0; i < flist.length; i++)
		{
			f = flist[i];

			if(f.isFile())
			{
				files = new File[1];
				files[0] = f;
			}
			else if(f.isDirectory())
			{
				files = f.listFiles();
			}
			else
			{
				return;
			}

			for(int j = 0; j < files.length; j++)
			{
				if(files[j].isFile())
				{
					model.addRow(files[j].getAbsolutePath());
				}
			}
		}
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			System.out.println(command);
			if(command.equals("colChoose"))
			{
				String col = colChoose.getSelectedItem().toString();

				valueTextField.setVisible(true);
				valueFocMech.setVisible(false);
				valueSiteClass.setVisible(false);

				valueSel = valueTF;

				for(int i = 0; i < NewmarkTable.fieldArray.length; i++)
				{
					if(col.equals(NewmarkTable.fieldArray[NewmarkTable.rowFocMech][NewmarkTable.colFieldName]))
					{
						valueSel = valueFM;
						valueFocMech.setVisible(true);
						valueTextField.setVisible(false);
						break;
					}
					else if(col.equals(NewmarkTable.fieldArray[NewmarkTable.rowSiteClass][NewmarkTable.colFieldName]))
					{
						valueSel = valueSC;
						valueSiteClass.setVisible(true);
						valueTextField.setVisible(false);
						break;
					}
				}
			}
			else if(command.equals("set"))
			{
				int col;
				String abbrev = NewmarkTable.getColValue(NewmarkTable.colFieldName, NewmarkTable.colAbbrev, (String)colChoose.getSelectedItem());
				String v;

				switch(valueSel)
				{
					case valueTF:
						v = valueTextField.getText();
						break;
					case valueFM:
						v = valueFocMech.getSelectedItem().toString();
						break;
					case valueSC:
						v = valueSiteClass.getSelectedItem().toString();
						break;
					default:
						v = "";
						break;
				}

				for(int i = 0; i < table.getColumnCount(); i++)
				{
					if(abbrev.equals(table.getColumnName(i)))
					{
						for(int j = 0; j < table.getRowCount(); j++)
						{
							table.setValueAt(v, j, i);
						}
						break;
					}
				}
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	public TableModel getModel()
	{
		return model;
	}

	public TableCellEditor getCellEditor()
	{
		return table.getCellEditor();
	}
}
