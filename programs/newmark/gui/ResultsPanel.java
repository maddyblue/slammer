/*
 * ResultsPanel.java
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

/* $Id: ResultsPanel.java,v 1.4 2003/07/18 05:25:15 dolmant Exp $ */

package newmark.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Vector;
import org.jfree.data.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.axis.*;
import java.text.DecimalFormat;
import java.util.Vector;
import java.io.*;
import newmark.*;

class ResultsPanel extends JPanel implements ActionListener
{
	NewmarkTabbedPane parent;

	JButton Analyze = new JButton("Perform Analysis");
	JButton ClearOutput = new JButton("Clear output");

	DefaultTableModel outputTableModel = new DefaultTableModel();
	JTable outputTable = new JTable(outputTableModel);
	JScrollPane outputTablePane = new JScrollPane(outputTable);

	JButton saveOutput = new JButton("Save output");
	JFileChooser fc = new JFileChooser();
	JLabel outputMean = new JLabel("Mean:");
	JLabel outputMedian = new JLabel("Median:");
	JLabel outputStdDev = new JLabel("Standard Deviation:");

	JButton plotOutput = new JButton("Plot histogram of Newmark displacements");
	JButton plotNewmark = new JButton("Plot Newmark displacements versus time");
	JCheckBox plotNewmarkLegend = new JCheckBox("Display legend", false);
	JTextField outputBins = new JTextField("10",4);

	XYSeriesCollection xysc = new XYSeriesCollection();

	JRadioButton outputDelTab = new JRadioButton("tab delimited", true);
	JRadioButton outputDelSpace = new JRadioButton("space delimited");
	JRadioButton outputDelComma = new JRadioButton("comma delimited");
	ButtonGroup outputDelGroup = new ButtonGroup();

	Vector dataVect;
	double max;
	XYSeriesCollection xycol;
	String parameters;

	public ResultsPanel(NewmarkTabbedPane parent) throws Exception
	{
		this.parent = parent;

		outputTableModel.addColumn("Earthquake");
		outputTableModel.addColumn("Record");
		outputTableModel.addColumn("Displacement 1 (cm)");
		outputTableModel.addColumn("Displacement 2 (cm)");
		outputTableModel.addColumn("Average Disp. (cm)");

		Analyze.setMnemonic(KeyEvent.VK_A);
		Analyze.setActionCommand("analyze");
		Analyze.addActionListener(this);

		ClearOutput.setMnemonic(KeyEvent.VK_C);
		ClearOutput.setActionCommand("clearOutput");
		ClearOutput.addActionListener(this);

		saveOutput.setMnemonic(KeyEvent.VK_S);
		saveOutput.setActionCommand("saveOutput");
		saveOutput.addActionListener(this);

		plotOutput.setMnemonic(KeyEvent.VK_H);
		plotOutput.setActionCommand("plotOutput");
		plotOutput.addActionListener(this);

		plotNewmark.setMnemonic(KeyEvent.VK_V);
		plotNewmark.setActionCommand("plotNewmark");
		plotNewmark.addActionListener(this);

		plotNewmarkLegend.setMnemonic(KeyEvent.VK_L);
		plotNewmarkLegend.setActionCommand("plotNewmarkLegend");
		plotNewmarkLegend.addActionListener(this);

		outputDelGroup.add(outputDelTab);
		outputDelGroup.add(outputDelSpace);
		outputDelGroup.add(outputDelComma);

		setLayout(new BorderLayout());

		add(BorderLayout.NORTH, createHeader());
		add(BorderLayout.CENTER, createTable());
		add(BorderLayout.SOUTH, createGraphs());
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			System.out.println(command);
			if(command.equals("analyze"))
			{
				// save the parameters first
				String slope, displacement, scaling;

				if(parent.Parameters.downSlope.isSelected())
					slope = "Downslope displacement only.";
				else
					slope = "Downslope and upslope displacement.\nThrust angle (in degrees): " + parent.Parameters.thrustAngle.getText();

				if(parent.Parameters.nd.isSelected())
					displacement = "Constant critical acceleration.\nAcceleration: " + parent.Parameters.constCA.getText();
				else
				{
					if(parent.Parameters.ndDisp.isSelected())
					{
						displacement = "Varies with displacement";
					}
				}

				JProgressBar mon = new JProgressBar();
				mon.setStringPainted(true);
				JFrame monFrame = new JFrame("Progress...");
				monFrame.getContentPane().add(mon);
				monFrame.setSize(400,75);
				GUIUtils.setLocationMiddle(monFrame);
				boolean none = true;

				Object[][] res = Utils.getDB().runQuery("select eq, record, digi_int, path, pga from data where select2=true and analyze=true");

				if(res == null || res.length <= 1)
				{
					parent.selectSelectRecords();
					monFrame.dispose();
					GUIUtils.popupError("No records selected for analysis.");
					return;
				}
				String eq, record;
				DoubleList dat;
				double di, ca = 0, avg, total = 0;
				double num = 0;
				double scale = 1;
				Double norm = new Double(0), inv = new Double(0);
				double[][] caList = null;
				Vector caVect;
				Object[] caTemp;
				String temp1;
				Double temp;
				int i2, i3;

				clearOutput();

				if(parent.Parameters.ndDisp.isSelected() == true || parent.Parameters.ndTime.isSelected() == true)
				{
					String tableSelect;
					if(parent.Parameters.ndDisp.isSelected() == true)
					{
						tableSelect = "displacement";
						TableCellEditor edit = parent.Parameters.dispTable.getCellEditor();
						if(edit != null) edit.stopCellEditing();
						caVect = parent.Parameters.dispTableModel.getDataVector();
					}
					else
					{
						tableSelect = "time";
						TableCellEditor edit = parent.Parameters.timeTable.getCellEditor();
						if(edit != null) edit.stopCellEditing();
						caVect = parent.Parameters.timeTableModel.getDataVector();
					}

					caList = new double[caVect.size()][2];
					caTemp = caVect.toArray();
					for(int i1 = 0; i1 < caTemp.length; i1++)
					{
						for(i2 = 0; i2 < 2; i2++)
						{
							temp1 = (String)(((Vector)caTemp[i1]).elementAt(i2));
							if(temp1 == null || temp1 == "")
							{
								parent.selectParameters();
								monFrame.dispose();
								GUIUtils.popupError("Error: empty field in data table.\nPlease complete the " + tableSelect + " table so that all data pairs have values or delete empty rows.");
								return;
							}
							Double d = (Double)Utils.checkNum(temp1, tableSelect + " table", null, false, null, null, false, null, false);
							if(d == null)
							{
								monFrame.dispose();
								return;
							}
							caList[i1][i2] = d.doubleValue();
						}
					}
				}
				else if(parent.Parameters.nd.isSelected() == true)
				{
					Double d = (Double)Utils.checkNum(parent.Parameters.constCA.getText(), "constant critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d == null)
					{
						parent.selectParameters();
						monFrame.dispose();
						return;
					}
					ca = d.doubleValue();
				}
				else
				{
					monFrame.dispose();
					parent.selectParameters();
					GUIUtils.popupError("Error: no analysis method selected.");
					return;
				}

				Double thrustD;
				double thrust = 0;
				if(parent.Parameters.dualSlope.isSelected())
				{
					thrustD =  (Double)Utils.checkNum(parent.Parameters.thrustAngle.getText(), "thrust angle field", new Double(90), true, null, new Double(0), true, null, false);
					if(thrustD == null)
					{
						parent.selectParameters();
						monFrame.dispose();
						return;
					}
					else
						thrust = thrustD.doubleValue();
				}

				File testFile;
				String path;
				DecimalFormat fmt = new DecimalFormat(Analysis.fmtOne);
				mon.setMinimum(0);
				mon.setMaximum(res.length - 2);
				monFrame.show();
				dataVect = new Vector(res.length - 1);
				xysc = new XYSeriesCollection();
				double iscale = -1.0 * scale;
				for(int i = 1; i < res.length; i++)
				{
					eq = res[i][0].toString();
					record = res[i][1].toString();
					mon.setString(eq + " - " + record);
					mon.setValue(i);
					mon.paintImmediately(0,0,mon.getWidth(),mon.getHeight());

					di = Double.parseDouble(res[i][2].toString());
					path = res[i][3].toString();
					testFile = new File(path);
					if(!testFile.exists() || !testFile.canRead())
					{
						outputTableModel.addRow(new Object[] {eq, record, "File does not exist or is not readable", path, null});
						continue;
					}
					dat = new DoubleList(path);
					if(dat.bad())
					{
						outputTableModel.addRow(new Object[] {eq, record, "Invalid data at point " + dat.badEntry(), path, null});
						continue;
					}

					if(parent.Parameters.scalePGAon.isSelected() == true)
					{
						Double d = (Double)Utils.checkNum(parent.Parameters.scalePGAval.getText(), "scale PGA field", null, false, null, new Double(0), false, null, false);
						if(d == null)
						{
							parent.selectParameters();
							monFrame.dispose();
							return;
						}
						scale = d.doubleValue() / Double.parseDouble(res[i][4].toString());
					}
					if(parent.Parameters.dualSlope.isSelected())
					{
						inv = new Double((String)Analysis.NewmarkRigorousDual(dat, di, ca, thrust, iscale));
						norm = new Double((String)Analysis.NewmarkRigorousDual(dat, di, ca, thrust, scale));
					}
					else if(parent.Parameters.downSlope.isSelected())
					{
						if(parent.Parameters.nd.isSelected())
						{
							inv = new Double((String)Analysis.NewmarkRigorous(dat, di, ca, iscale));
							norm = new Double((String)Analysis.NewmarkRigorous(dat, di, ca, scale));
						}
						else if(parent.Parameters.ndDisp.isSelected())
						{
							inv = new Double((String)Analysis.NewmarkRigorousDisp(dat, di, caList, iscale));
							norm = new Double((String)Analysis.NewmarkRigorousDisp(dat, di, caList, scale));
						}
						else if(parent.Parameters.ndTime.isSelected())
						{
							inv = new Double((String)Analysis.NewmarkRigorousTime(dat, di, caList, iscale));
							norm = new Double((String)Analysis.NewmarkRigorousTime(dat, di, caList, scale));
						}
						else
						{
							GUIUtils.popupError("No analysis method selected.");
							return;
						}
					}
					else
					{
						GUIUtils.popupError("No analysis method selected.");
						return;
					}

					Analysis.xys.setName(eq + " - " + record);
					xysc.addSeries(Analysis.xys);

					avg = (norm.doubleValue()+inv.doubleValue())/2.0;
					total += avg;
					num++;
					for(i3 = 0; i3 < dataVect.size() && ((Double)dataVect.elementAt(i3)).doubleValue() < avg; i3++);
					dataVect.insertElementAt(new Double(avg), i3);
					outputTableModel.addRow(new Object[] {eq, record, fmt.format(norm), fmt.format(inv), fmt.format(avg)});
				}
				mon.setString("Calculating stastistics...");
				mon.paintImmediately(0,0,mon.getWidth(),mon.getHeight());
				if(dataVect.size() == 0)
				{
					monFrame.dispose();
					return;
				}
				max = ((Double)dataVect.elementAt(dataVect.size() - 1)).doubleValue();

				fmt = new DecimalFormat(Analysis.fmtOne);
				double mean = Double.parseDouble(fmt.format(total/num));
				outputMean.setText("Mean value is: " + fmt.format(mean) + " cm");
				outputMedian.setText("Median value is: " + fmt.format(dataVect.elementAt((int)(num/2))) + " cm");

				double value = 0;
				double valtemp;
				for(int i = 0; i < num; i++)
				{
					valtemp = mean - ((Double)dataVect.elementAt(i)).doubleValue();
					value += (valtemp * valtemp);
				}
				value /= num - 1;
				value = Math.sqrt(value);
				outputStdDev.setText("Standard Deviation is: " + fmt.format(value) + " cm");
				monFrame.dispose();
			}
			else if(command.equals("clearOutput"))
			{
				clearOutput();
			}
			else if(command.equals("saveOutput"))
			{
				if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
				{
					FileWriter fw = new FileWriter(fc.getSelectedFile());

					fw.write(outputMean.getText() + "\n");
					fw.write(outputMedian.getText() + "\n");
					fw.write(outputStdDev.getText() + "\n");

					Vector v = outputTableModel.getDataVector(), v1;
					for(int i = 0; i < v.size(); i++)
					{
						v1 = (Vector)v.elementAt(i);
						String delim;
						if(outputDelSpace.isSelected()) delim = " ";
						else if(outputDelComma.isSelected()) delim = ",";
						else delim = "\t";
						for(int i1 = 0; i1 < v1.size(); i1++)
						{
							if(i1 != 0) fw.write(delim);
							fw.write(v1.elementAt(i1).toString());
						}
						fw.write("\n");
					}
					fw.close();
				}
			}
			else if(command.equals("plotOutput"))
			{
				if(dataVect == null) return;
				Double Bins = (Double)Utils.checkNum(outputBins.getText(), "output bins field", null, false, null, new Double(0), false, null, false);
				if(Bins != null)
				{
					double series[] = new double[dataVect.size()];

					for(int i = 0; i < dataVect.size(); i++)
					{
						series[i] = (((Double)dataVect.elementAt(i)).doubleValue());
					}

					HistogramDataset dataset = new HistogramDataset();
					dataset.addSeries("", series, (int)Bins.doubleValue());

					JFreeChart hist = ChartFactory.createHistogram("Histogram of Newmark Displacements", "Newmark Displacement (cm)", "Number of Records", dataset, PlotOrientation.VERTICAL, false, false, false);
					ChartFrame frame = new ChartFrame("Histogram of Newmark Displacements", hist);

					frame.pack();
					frame.setLocationRelativeTo(null);
					frame.show();
				}
			}
			else if(command.equals("plotNewmark"))
			{
				JFreeChart chart = ChartFactory.createLineXYChart("Newmark displacement versus time", "Time (s)", "Newmark displacement (cm)", xysc, plotNewmarkLegend.isSelected(), true, false);
				ChartFrame frame = new ChartFrame("Newmark displacement versus time", chart);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.show();
			}
		}
		catch (Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	private void clearOutput()
	{
		dataVect = null;
		int rows = outputTableModel.getRowCount();
		while(--rows >= 0)
			outputTableModel.removeRow(rows);
		outputMean.setText("Mean:");
		outputMedian.setText("Median:");
		outputStdDev.setText("Standard Deviation:");
	}

	private JPanel createHeader()
	{
		JPanel container = new JPanel(new GridLayout(0, 2));
		container.add(Analyze);
		container.add(ClearOutput);

		return container;
	}

	private JPanel createTable()
	{
		JPanel outputTablePanel = new JPanel(new BorderLayout());

		JPanel outputData = new JPanel(new BorderLayout());
		JPanel filler = new JPanel(new GridLayout(0, 1));
		filler.add(outputMean);
		filler.add(outputMedian);
		filler.add(outputStdDev);
		outputData.add(BorderLayout.EAST, filler);
		JPanel west = new JPanel(new BorderLayout());
		Box list = new Box(BoxLayout.X_AXIS);
		list.add(outputDelTab);
		list.add(outputDelSpace);
		list.add(outputDelComma);
		west.add(BorderLayout.CENTER,list);
		west.add(BorderLayout.WEST, saveOutput);
		outputData.add(BorderLayout.WEST, west);

		outputTablePanel.add(BorderLayout.CENTER, outputTablePane);
		outputTablePanel.add(BorderLayout.SOUTH, outputData);

		return outputTablePanel;
	}

	private JPanel createGraphs()
	{
		JPanel outputGraphs = new JPanel(new VariableGridLayout(VariableGridLayout.FIXED_NUM_COLUMNS,2));

		outputGraphs.add(plotOutput);
		Box b = new Box(BoxLayout.X_AXIS);
		b.add(new JLabel("Plot with "));
		b.add(outputBins);
		b.add(new JLabel(" bins"));
		JPanel p = new JPanel(new BorderLayout());
		p.add(BorderLayout.WEST, b);
		outputGraphs.add(p);
		outputGraphs.add(plotNewmark);
		outputGraphs.add(plotNewmarkLegend);

		return outputGraphs;
	}

}
