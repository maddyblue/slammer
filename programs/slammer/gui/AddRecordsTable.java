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
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Vector;
import javax.swing.table.*;
import java.io.*;
import slammer.*;

class AddRecordsTable extends JPanel implements ActionListener
{
	AddRecordsTableModel model = new AddRecordsTableModel();
	JTable table = new JTable(model);

	int valueSel;
	static final int valueTF = 1;
	static final int valueFM = 2;
	static final int valueSC = 3;

	JTextField valueTextField = new JTextField(15);
	JComboBox valueFocMech = new JComboBox(SlammerTable.FocMechArray);
	JComboBox valueSiteClass = new JComboBox(SlammerTable.SiteClassArray);
	JComboBox colChoose = new JComboBox(SlammerTable.getColumnList(SlammerTable.colFieldName, SlammerTable.colImport, SlammerTable.IMCMB));
	JButton set = new JButton("Set");

	public AddRecordsTable()
	{
		TableColumn c = table.getColumn(SlammerTable.fieldArray[SlammerTable.rowFocMech][SlammerTable.colAbbrev]);
		c.setCellEditor(new DefaultCellEditor(new JComboBox(SlammerTable.FocMechArray)));

		c = table.getColumn(SlammerTable.fieldArray[SlammerTable.rowSiteClass][SlammerTable.colAbbrev]);
		c.setCellEditor(new DefaultCellEditor(new JComboBox(SlammerTable.SiteClassArray)));

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
			if(command.equals("colChoose"))
			{
				String col = colChoose.getSelectedItem().toString();

				valueTextField.setVisible(true);
				valueFocMech.setVisible(false);
				valueSiteClass.setVisible(false);

				valueSel = valueTF;

				for(int i = 0; i < SlammerTable.fieldArray.length; i++)
				{
					if(col.equals(SlammerTable.fieldArray[SlammerTable.rowFocMech][SlammerTable.colFieldName]))
					{
						valueSel = valueFM;
						valueFocMech.setVisible(true);
						valueTextField.setVisible(false);
						break;
					}
					else if(col.equals(SlammerTable.fieldArray[SlammerTable.rowSiteClass][SlammerTable.colFieldName]))
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
				String abbrev = SlammerTable.getColValue(SlammerTable.colFieldName, SlammerTable.colAbbrev, (String)colChoose.getSelectedItem());
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
