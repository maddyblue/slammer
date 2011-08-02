/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import org.jfree.data.xy.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import slammer.*;
import slammer.analysis.*;

class RecordManagerPanel extends JPanel implements ActionListener
{
	SlammerTabbedPane parent;

	SlammerTable table;

	JComboBox eqList = new JComboBox();
	JButton graph = new JButton("Graph");
	JButton graph2 = new JButton("Graph");
	JButton saveGraph = new JButton("Save results as text");
	JFileChooser saveChooser = new JFileChooser();
	JButton delete = new JButton("Delete selected record(s) from database");

	ButtonGroup timeAxisGroup = new ButtonGroup();
	JRadioButton timeAxisGs = new JRadioButton("g's", true);
	JRadioButton timeAxisCmss = new JRadioButton("cm/s/s");

	ButtonGroup saveGroup = new ButtonGroup();
	JRadioButton saveTab = new JRadioButton("Tab", true);
	JRadioButton saveSpace = new JRadioButton("Space");
	JRadioButton saveComma = new JRadioButton("Comma");

	JButton save = new JButton("Save changes");

	JTextField modFile = new JTextField(5);
	JTextField modEq = new JTextField(5);
	JTextField modRec = new JTextField(5);
	JTextField modDI = new JTextField(5);
	JTextField modLoc = new JTextField(5);
	JTextField modMag = new JTextField(5);
	JTextField modOwn = new JTextField(5);
	JTextField modEpi = new JTextField(5);
	JTextField modLat = new JTextField(5);
	JTextField modFoc = new JTextField(5);
	JTextField modLng = new JTextField(5);
	JTextField modRup = new JTextField(5);
	JTextField modVs = new JTextField(5);
	JComboBox  modSite = new JComboBox(SlammerTable.SiteClassArray);
	JComboBox  modMech = new JComboBox(SlammerTable.FocMechArray);

	ButtonGroup TypeGroup = new ButtonGroup();
	JRadioButton typeTime = new JRadioButton("Time history", true);
	JRadioButton typeFourier = new JRadioButton("Fourier amplitude spectrum");
	JRadioButton typeSpectra = new JRadioButton("Response spectra");

	JComboBox spectraCB = new JComboBox(new String[] { "Absolute acceleration", "Relative velocity", "Relative displacement", "Psuedo absolute acceleration", "Psuedo relative velocity" });
	String[] spectraNames = new String[] { "Absolute-Acceleration", "Relative-Velocity", "Relative-Displacement", "Psuedo-Absolute-Acceleration", "Psuedo-Relative-Velocity" };
	String[] spectraCBStr = new String[] { "cm/s/s", "cm/s", "cm", "cm/s/s", "cm/s" };

	JComboBox spectraDomain = new JComboBox(new String[] { "Frequency", "Period" });
	String[] spectraDomainStr = new String[] { "Hz", "s" };
	String[] spectraDirStr = new String[] { "Highest", "Shortest" };

	JLabel spectraDomainLabel = new JLabel();
	JTextField spectraDamp = new JTextField("0");
	JTextField spectraHigh = new JTextField("100.00");

	JButton add = new JButton("Add record(s)...");

	JTabbedPane managerTP = new JTabbedPane();

	public RecordManagerPanel(SlammerTabbedPane parent) throws Exception
	{
		this.parent = parent;

		table = new SlammerTable(false, false);

		ListSelectionModel recordSelect = table.getSelectionModel();
		recordSelect.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent event)
			{
				try
				{
					recordSelect(event);
				}
				catch (Exception e)
				{
					Utils.catchException(e);
				}
			}
		});

		TypeGroup.add(typeTime);
		TypeGroup.add(typeFourier);
		TypeGroup.add(typeSpectra);

		timeAxisGroup.add(timeAxisGs);
		timeAxisGroup.add(timeAxisCmss);

		saveGroup.add(saveTab);
		saveGroup.add(saveSpace);
		saveGroup.add(saveComma);

		Utils.addEQList(eqList, Boolean.TRUE);
		eqList.setActionCommand("eqListChange");
		eqList.addActionListener(this);

		save.setActionCommand("save");
		save.addActionListener(this);

		delete.setActionCommand("delete");
		delete.addActionListener(this);

		graph.setActionCommand("graph");
		graph.addActionListener(this);

		graph2.setActionCommand("graph");
		graph2.addActionListener(this);

		saveGraph.setActionCommand("saveGraph");
		saveGraph.addActionListener(this);

		managerTP.addTab("Modify record", createModifyPanel());
		managerTP.addTab("Graph record", createGraphPanel());

		spectraDomain.setActionCommand("domain");
		spectraDomain.addActionListener(this);
		updateDomainLabel();

		setLayout(new BorderLayout());
		add(BorderLayout.NORTH, createNorthPanel());
		add(BorderLayout.CENTER, table);
		add(BorderLayout.SOUTH, managerTP);

		recordClear();
	}

	private JPanel createNorthPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());

		ArrayList list = new ArrayList();
		list.add(new JLabel("Display records from: "));
		list.add(eqList);
		panel.add(BorderLayout.WEST, GUIUtils.makeRecursiveLayoutRight(list));

		list = new ArrayList();
		list.add(graph);
		list.add(delete);
		panel.add(BorderLayout.EAST, GUIUtils.makeRecursiveLayoutRight(list));

		return panel;
	}

	private JPanel createModifyPanel()
	{
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c =new GridBagConstraints();
		JPanel panel = new JPanel(gridbag);

		int x = 0;
		int y = 0;

		c.gridx = x++;
		c.gridy = y++;
		c.gridwidth = 4;
		c.anchor = GridBagConstraints.WEST;
		JLabel label = new JLabel("Modify record properties:");
		label.setFont(GUIUtils.headerFont);
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(save, c);
		panel.add(save);

		c.gridy = y++;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.BOTH;
		label = new JLabel("File location");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		c.gridwidth = 3;
		c.weightx = 1;
		gridbag.setConstraints(modFile, c);
		panel.add(modFile);
		c.gridwidth = 1;

		int ytop = y;

		c.weightx = 0;
		c.gridx = x++;

		c.gridy = y++;
		label = new JLabel(SlammerTable.makeUnitName(SlammerTable.rowEarthquake));
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		label = new JLabel(SlammerTable.makeUnitName(SlammerTable.rowDigInt));
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		label = new JLabel(SlammerTable.makeUnitName(SlammerTable.rowMagnitude));
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		label = new JLabel(SlammerTable.makeUnitName(SlammerTable.rowEpiDist));
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		label = new JLabel(SlammerTable.makeUnitName(SlammerTable.rowFocalDist));
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		label = new JLabel(SlammerTable.makeUnitName(SlammerTable.rowRupDist));
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		label = new JLabel(SlammerTable.makeUnitName(SlammerTable.rowFocMech));
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		y = ytop;

		c.gridy = y++;
		c.weightx = 1;
		gridbag.setConstraints(modEq, c);
		panel.add(modEq);

		c.gridy = y++;
		c.weightx = 1;
		gridbag.setConstraints(modDI, c);
		panel.add(modDI);

		c.gridy = y++;
		c.weightx = 1;
		gridbag.setConstraints(modMag, c);
		panel.add(modMag);

		c.gridy = y++;
		c.weightx = 1;
		gridbag.setConstraints(modEpi, c);
		panel.add(modEpi);

		c.gridy = y++;
		c.weightx = 1;
		gridbag.setConstraints(modFoc, c);
		panel.add(modFoc);

		c.gridy = y++;
		c.weightx = 1;
		gridbag.setConstraints(modRup, c);
		panel.add(modRup);

		c.gridy = y++;
		c.weightx = 1;
		gridbag.setConstraints(modMech, c);
		panel.add(modMech);

		c.gridx = x++;
		y = ytop;
		c.weightx = 0;
		c.insets = new Insets(0, 10, 0, 0);

		c.gridy = y++;
		label = new JLabel(SlammerTable.makeUnitName(SlammerTable.rowRecord));
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		label = new JLabel(SlammerTable.makeUnitName(SlammerTable.rowLocation));
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		label = new JLabel(SlammerTable.makeUnitName(SlammerTable.rowOwner));
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		label = new JLabel(SlammerTable.makeUnitName(SlammerTable.rowLat));
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		label = new JLabel(SlammerTable.makeUnitName(SlammerTable.rowLng));
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		label = new JLabel(SlammerTable.makeUnitName(SlammerTable.rowVs30));
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		label = new JLabel(SlammerTable.makeUnitName(SlammerTable.rowSiteClass));
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		y = ytop;
		c.weightx = 1;
		c.insets = new Insets(0, 0, 0, 0);

		c.gridy = y++;
		c.weightx = 1;
		gridbag.setConstraints(modRec, c);
		panel.add(modRec);

		c.gridy = y++;
		c.weightx = 1;
		gridbag.setConstraints(modLoc, c);
		panel.add(modLoc);

		c.gridy = y++;
		c.weightx = 1;
		gridbag.setConstraints(modOwn, c);
		panel.add(modOwn);

		c.gridy = y++;
		c.weightx = 1;
		gridbag.setConstraints(modLat, c);
		panel.add(modLat);

		c.gridy = y++;
		c.weightx = 1;
		gridbag.setConstraints(modLng, c);
		panel.add(modLng);

		c.gridy = y++;
		c.weightx = 1;
		gridbag.setConstraints(modVs, c);
		panel.add(modVs);

		c.gridy = y++;
		c.weightx = 1;
		gridbag.setConstraints(modSite, c);
		panel.add(modSite);

		return panel;
	}

	public JPanel createGraphPanel()
	{
		JPanel panel = new JPanel();

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		JLabel label;
		Insets left = new Insets(0, 1, 0, 0);
		Insets none = new Insets(0, 0, 0, 0);

		panel.setLayout(gridbag);

		int x = 0;
		int y = 0;

		c.fill = GridBagConstraints.BOTH;

		Border b = BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK),
			BorderFactory.createEmptyBorder(2, 1, 2, 1)
		);

		Border bleft = BorderFactory.createMatteBorder(0, 1, 0, 0, Color.BLACK);
		Border bdown = BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK),
			BorderFactory.createEmptyBorder(0, 2, 0, 0)
		);

		JPanel graphPanel = new JPanel(new GridLayout(1, 0));
		graphPanel.add(graph2);
		graphPanel.add(saveGraph);

		c.gridx = x++;
		c.gridy = y++;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.CENTER;
		label = new JLabel("Select a record above for graphing");
		//label.setFont(GUIUtils.headerFont);
		gridbag.setConstraints(label, c);
		panel.add(label);
		c.anchor = GridBagConstraints.WEST;

		c.gridy = y++;
		gridbag.setConstraints(graphPanel, c);
		panel.add(graphPanel);
		c.gridwidth = 1;

		c.gridy = y++;
		typeTime.setBorder(b);
		typeTime.setBorderPainted(true);
		gridbag.setConstraints(typeTime, c);
		panel.add(typeTime);

		c.gridx = x++;
		label = new JLabel("Vertical axis");
		label.setBorder(bdown);
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		Box timeAxisPanel = new Box(BoxLayout.X_AXIS);
		timeAxisPanel.add(timeAxisGs);
		timeAxisPanel.add(timeAxisCmss);
		timeAxisPanel.setBorder(bdown);
		gridbag.setConstraints(timeAxisPanel, c);
		panel.add(timeAxisPanel);
		x -= 3;

		c.gridx = x++;
		c.gridy = y++;
		typeFourier.setBorder(b);
		typeFourier.setBorderPainted(true);
		gridbag.setConstraints(typeFourier, c);
		panel.add(typeFourier);

		c.gridy = y--;
		c.gridheight = 4;
		typeSpectra.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK));
		typeSpectra.setBorderPainted(true);
		gridbag.setConstraints(typeSpectra, c);
		panel.add(typeSpectra);

		c.gridx = x++;
		c.gridy = y++;
		c.gridheight = 1;
		c.gridwidth = 2;
		label = new JLabel("");
		label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		c.gridwidth = 1;
		c.insets = left;
		label = new JLabel("Horizontal axis");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		c.insets = none;
		gridbag.setConstraints(spectraDomain, c);
		panel.add(spectraDomain);

		c.gridx = x++;
		c.gridy = y++;
		c.insets = left;
		label = new JLabel("Response type");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		c.insets = none;
		gridbag.setConstraints(spectraCB, c);
		panel.add(spectraCB);

		c.gridx = x++;
		c.gridy = y++;
		c.insets = left;
		label = new JLabel("Damping (%)");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x--;
		c.insets = none;
		gridbag.setConstraints(spectraDamp, c);
		panel.add(spectraDamp);

		c.gridx = x++;
		c.gridy = y++;
		c.insets = left;
		gridbag.setConstraints(spectraDomainLabel, c);
		panel.add(spectraDomainLabel);

		c.gridx = x++;
		c.insets = none;
		gridbag.setConstraints(spectraHigh, c);
		panel.add(spectraHigh);

		y -= 7;
		c.gridy = y++;
		c.insets = new Insets(0, 10, 0, 0);
		c.gridx = x++;
		label = new JLabel("Save delimiter:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		gridbag.setConstraints(saveTab, c);
		panel.add(saveTab);

		c.gridy = y++;
		gridbag.setConstraints(saveSpace, c);
		panel.add(saveSpace);

		c.gridy = y++;
		gridbag.setConstraints(saveComma, c);
		panel.add(saveComma);

		return panel;
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			if(command.equals("delete"))
			{
				int n = JOptionPane.showConfirmDialog(this, "Do you want to delete these records?", "Delete?", JOptionPane.YES_NO_OPTION);
				if(n != JOptionPane.YES_OPTION)
					return;

				table.deleteSelected(true);
			}
			else if(command.equals("domain"))
			{
				try
				{
					Double d = new Double(spectraHigh.getText());
					spectraHigh.setText(Analysis.fmtTwo.format(new Double(1.0 / d.doubleValue())));
				}
				catch(Exception ex){}

				updateDomainLabel();
			}
			else if(command.equals("eqListChange"))
			{
				if(Utils.locked())
					return;

				boolean isEq = true;
				int index = eqList.getSelectedIndex();
				for(int i = 0; i < eqList.getItemCount(); i++)
				{
					if(((String)eqList.getItemAt(i)).equals(" -- Groups -- "))
					{
						if(i == index)
							return;
						else if(index > i)
							isEq = false;
						break;
					}
				}

				Utils.getDB().runUpdate("update data set select1=0 where select1=1");

				if(isEq)
				{
					String where;

					if(index == 0) // "All earthquakes"
						where = "";
					else
						where = "where eq='" + (String)eqList.getSelectedItem() + "'";

					Utils.getDB().runUpdate("update data set select1=1 " + where);
				}
				else
				{
					Object[][] res = Utils.getDB().runQuery("select record,analyze from grp where name='" + (String)eqList.getSelectedItem() + "'");

					for(int i = 1; i < res.length; i++)
						Utils.getDB().runUpdate("update data set select1=1 where id=" + res[i][0].toString());
				}

				table.setModel(SlammerTable.REFRESH);
				recordClear();
			}
			else if(command.equals("graph") || command.equals("saveGraph"))
			{
				int row = table.getSelectedRow();
				if(row == -1)
				{
					GUIUtils.popupError("No selected record.");
					return;
				}

				boolean save = command.equals("saveGraph");

				String eq = table.getModel().getValueAt(row, 0).toString();
				String record = table.getModel().getValueAt(row, 1).toString();

				Object[][] res = null;
				res = Utils.getDB().runQuery("select path,digi_int from data where eq='" + eq + "' and record='" + record + "'");
				if(res == null) return;

				String path = res[1][0].toString();
				BigDecimal bdi = new BigDecimal(res[1][1].toString());
				double di = bdi.doubleValue();
				BigDecimal x = new BigDecimal(0);

				File f = new File(path);
				if(f.canRead() == false)
				{
					GUIUtils.popupError("Cannot read or open the file " + path + ".");
					return;
				}

				DoubleList dat = new DoubleList(path, 0, 1, typeTime.isSelected() && timeAxisGs.isSelected());

				if(dat.bad())
				{
					GUIUtils.popupError("Invalid data at data point " + dat.badEntry() + " in " + path + ".");
					return;
				}

				XYSeries xys = new XYSeries("");
				dat.reset();

				String xAxis = null, yAxis = null, title = "";

				if(typeTime.isSelected())
				{
					title = "Time Series";
					xAxis = "Time (s)";
					Double val;
					double last1 = 0, last2 = 0, current;
					double diff1, diff2;
					double time = 0, timeStor = 0, td;
					int perSec = 50;
					double interval = 1.0 / (double)perSec;
					BigDecimal btd;

					yAxis = "Acceleration (";
					if(timeAxisGs.isSelected())
						yAxis += "g's";
					else
						yAxis += "cm/s/s";
					yAxis += ")";

					if(save)
					{
						while((val = dat.each()) != null)
						{
							xys.add(x.doubleValue(), val);
							x = x.add(bdi);
						}
					}
					else
					{
						// add the first point
						if((val = dat.each()) != null)
						{
							xys.add(x.doubleValue(), val);
							x = x.add(bdi);
							last2 = val;
						}

						// don't add the second point, but update the data
						if((val = dat.each()) != null)
						{
							last1 = val;
							x = x.add(bdi);
						}

						while((val = dat.each()) != null)
						{
							btd = x;
							td = btd.doubleValue();
							x = x.add(bdi);
							current = val.doubleValue();

							diff1 = last1 - current;
							diff2 = last1 - last2;

							if(
								(diff1 <= 0 && diff2 <= 0) ||
								(diff1 >= 0 && diff2 >= 0) ||
								(td >= (timeStor + interval)))
							{
								xys.add(btd.doubleValue(), (float)last1);
								timeStor = td;
							}

							last2 = last1;
							last1 = current;
						}
					}
				}
				else if(typeFourier.isSelected())
				{
					title = "Fourier Amplitude Spectrum";
					xAxis = "Frequency (Hz)";
					yAxis = "Fourier Amplitude (cm/s)";
					double[] arr = new double[dat.size()];

					Double temp;
					for(int i = 0; (temp = dat.each()) != null; i++)
						arr[i] = temp.doubleValue();

					double[][] fft = ImportRecords.fftWrap(arr, di);

					BigDecimal bdf = new BigDecimal(1.0 / ((double)(arr.length) * di));
					BigDecimal zeroPtOne = new BigDecimal(0.1);
					BigDecimal ten = new BigDecimal(10);

					double current;
					int step, i;

					if(save)
					{
						for(i = 0; i < arr.length; i++)
						{
							xys.add(x.doubleValue(), Math.sqrt(Math.pow(fft[i][0], 2) + Math.pow(fft[i][1], 2)));
							x = x.add(bdf);
						}
					}
					else
					{
						for(i = 0; i < arr.length; i++)
						{
							// don't graph anything below 0.1 hz
							if(x.compareTo(zeroPtOne) > 0)
								xys.add(x.doubleValue(), Math.sqrt(Math.pow(fft[i][0], 2) + Math.pow(fft[i][1], 2)));

							// throw out 3/4 of the points above 10 hz
							if(x.compareTo(ten) > 0)
								step = 4;
							else
								step = 1;

							x = x.add(bdf);
						}
					}
				}
				else if(typeSpectra.isSelected())
				{
					int index = spectraCB.getSelectedIndex();

					title = spectraNames[index] + " Response Spectrum at ";
					xAxis = spectraDomain.getSelectedItem() + " (" + spectraDomainStr[spectraDomain.getSelectedIndex()] + ")";
					yAxis = "Response (" + spectraCBStr[index] + ")";
					double[] arr = new double[dat.size()];

					Double temp;
					for(int i = 0; (temp = dat.each()) != null; i++)
						arr[i] = temp.doubleValue();

					double freqMax = Double.parseDouble(spectraHigh.getText());
					double damp = Double.parseDouble(spectraDamp.getText()) / 100.0;

					title = title + spectraDamp.getText()+ "% Damping";

					boolean domainFreq = true;

					if(spectraDomain.getSelectedItem().equals("Period"))
					{
						domainFreq = false;
						freqMax = 1.0 / freqMax;
					}

					double[] z;

					// don't start with 0, LogarithmicAxis doesn't allow it
					for(double prev = 0, p = 0.05; prev < freqMax;)
					{
						z = ImportRecords.cmpmax(arr, 2.0 * Math.PI * p, damp, di);
						xys.add(
							domainFreq ? p : 1.0 / p,
							z[index]);

						// make sure we always hit the max frequency
						 prev = p;
						 p += Math.log10(p + 1.0) / 8.0;
						 if(p > freqMax)
							 p = freqMax;
					}
				}

				title += ": " + eq + " - " + record;

				if(command.equals("graph"))
				{
					XYSeriesCollection xysc = new XYSeriesCollection(xys);

					JFreeChart chart = ChartFactory.createXYLineChart(title, xAxis, yAxis, xysc, org.jfree.chart.plot.PlotOrientation.VERTICAL, false, true, false);

					if(typeFourier.isSelected() || typeSpectra.isSelected())
					{
						chart.getXYPlot().setRangeAxis(new LogarithmicAxis(yAxis));
						chart.getXYPlot().setDomainAxis(new LogarithmicAxis(xAxis));
					}

					ChartFrame frame = new ChartFrame(title, chart);
					frame.pack();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				}
				else if(command.equals("saveGraph"))
				{
					if(JFileChooser.APPROVE_OPTION == saveChooser.showSaveDialog(this))
					{
						double xysdata[][] = xys.toArray();
						FileWriter w = new FileWriter(saveChooser.getSelectedFile());

						String delim = "\t";
						if(saveSpace.isSelected()) delim = " ";
						if(saveComma.isSelected()) delim = ",";

						w.write("# " + title + "\n");
						w.write("# " + xAxis + delim + yAxis + "\n");

						for(int i = 0; i < xysdata[0].length; i++)
							w.write(xysdata[0][i] + delim + xysdata[1][i] + "\n");

						w.close();
					}
				}
			}
			else if(command.equals("save"))
			{
				if(JOptionPane.showConfirmDialog(this, "Are you sure you want to modify this record?", "Are you sure?", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;

				String error = AddRecordsPanel.manipRecord(false,
					modFile.getText(),
					modEq.getText(),
					modRec.getText(),
					modDI.getText(),
					Utils.nullify(modMag.getText()),
					Utils.nullify(modEpi.getText()),
					Utils.nullify(modFoc.getText()),
					Utils.nullify(modRup.getText()),
					Utils.nullify(modVs.getText()),
					modMech.getSelectedItem().toString(),
					modLoc.getText(),
					modOwn.getText(),
					Utils.nullify(modLat.getText()),
					Utils.nullify(modLng.getText()),
					modSite.getSelectedItem().toString()
				);

				if(!error.equals(""))
					GUIUtils.popupError(error);

				table.setModel(SlammerTable.REFRESH);
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	public void recordSelect(ListSelectionEvent e)
	{
		if(e == null) return;

		if (e.getValueIsAdjusting()) return;

		ListSelectionModel lsm = (ListSelectionModel)e.getSource();

		if (!lsm.isSelectionEmpty())
		{
			int selectedRow = lsm.getMinSelectionIndex();

			String eq = table.getModel().getValueAt(selectedRow, 0).toString();
			String record = table.getModel().getValueAt(selectedRow, 1).toString();

			Object[][] res = null;

			try
			{
				res = Utils.getDB().runQuery("select path, eq, record, digi_int, location, mom_mag, owner, epi_dist, latitude, foc_dist, longitude, rup_dist, vs30, class, foc_mech, change from data where eq='" + eq + "' and record='" + record + "'");
			}
			catch(Exception ex)
			{
				Utils.catchException(ex);
			}
			boolean enable = false;
			if(res != null)
			{
				if(res.length > 1)
				{
					int incr = 0;
					modFile.setText(res[1][incr++].toString());
					modEq.setText(res[1][incr++].toString());
					modRec.setText(res[1][incr++].toString());
					modDI.setText(res[1][incr++].toString());
					modLoc.setText(res[1][incr++].toString());
					modMag.setText(Utils.shorten(res[1][incr++]));
					modOwn.setText(res[1][incr++].toString());
					modEpi.setText(Utils.shorten(res[1][incr++]));
					modLat.setText(Utils.shorten(res[1][incr++]));
					modFoc.setText(Utils.shorten(res[1][incr++]));
					modLng.setText(Utils.shorten(res[1][incr++]));
					modRup.setText(Utils.shorten(res[1][incr++]));
					modVs.setText(Utils.shorten(res[1][incr++]));
					modSite.setSelectedItem(SlammerTable.SiteClassArray[Integer.parseInt(Utils.shorten(res[1][incr++].toString()))]);
					modMech.setSelectedItem(SlammerTable.FocMechArray[Integer.parseInt(Utils.shorten(res[1][incr++].toString()))]);
					enable = res[1][incr++].toString().equals("1") ? true : false;
				}
				else
				{
					recordClear();
				}
			}
			else
			{
				recordClear();
			}
			recordEnable(true);
		}
	}

	public void recordEnable(boolean b)
	{
		modFile.setEditable(b);
		modEq.setEditable(b);
		modRec.setEditable(b);
		modDI.setEditable(b);
		modLoc.setEditable(b);
		modMag.setEditable(b);
		modOwn.setEditable(b);
		modEpi.setEditable(b);
		modLat.setEditable(b);
		modFoc.setEditable(b);
		modLng.setEditable(b);
		modRup.setEditable(b);
		modVs.setEditable(b);
		modSite.setEnabled(b);
		modMech.setEnabled(b);
		save.setEnabled(b);
	}

	public void recordClear()
	{
		modFile.setText("");
		modEq.setText("");
		modRec.setText("");
		modDI.setText("");
		modLoc.setText("");
		modMag.setText("");
		modOwn.setText("");
		modEpi.setText("");
		modLat.setText("");
		modFoc.setText("");
		modLng.setText("");
		modRup.setText("");
		modVs.setText("");
		modSite.setSelectedItem("");
		modMech.setSelectedItem("");

		recordEnable(false);
	}

	public void updateDomainLabel()
	{
		spectraDomainLabel.setText(spectraDirStr[spectraDomain.getSelectedIndex()] + " " + spectraDomain.getSelectedItem() + " (" + spectraDomainStr[spectraDomain.getSelectedIndex()] + ")");
	}
}
