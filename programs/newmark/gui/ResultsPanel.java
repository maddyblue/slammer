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

/* $Id$ */

package newmark.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Vector;
import org.jfree.data.xy.*;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.chart.*;
import java.text.DecimalFormat;
import java.util.Vector;
import java.io.*;
import newmark.*;
import newmark.analysis.*;

class ResultsPanel extends JPanel implements ActionListener
{
	// array indexes
	final public static int RB = 0; // rigid block
	final public static int DC = 1; // decoupled
	final public static int CP = 2; // coupled

	// table column indexes
	final public static int RBC = 2;
	final public static int DCC = 3;
	final public static int CPC = 4;

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

	XYSeriesCollection xysc[] = new XYSeriesCollection[3];

	JRadioButton outputDelTab = new JRadioButton("tab delimited", true);
	JRadioButton outputDelSpace = new JRadioButton("space delimited");
	JRadioButton outputDelComma = new JRadioButton("comma delimited");
	ButtonGroup outputDelGroup = new ButtonGroup();

	Vector dataVect[] = new Vector[3];
	double max;
	XYSeriesCollection xycol;
	String parameters;

	public ResultsPanel(NewmarkTabbedPane parent) throws Exception
	{
		this.parent = parent;

		outputTableModel.addColumn("Earthquake");
		outputTableModel.addColumn("Record");
		outputTableModel.addColumn("Rigid Block");
		outputTableModel.addColumn("Decoupled");
		outputTableModel.addColumn("Coupled");

		Analyze.setActionCommand("analyze");
		Analyze.addActionListener(this);

		ClearOutput.setActionCommand("clearOutput");
		ClearOutput.addActionListener(this);

		saveOutput.setActionCommand("saveOutput");
		saveOutput.addActionListener(this);

		plotOutput.setActionCommand("plotOutput");
		plotOutput.addActionListener(this);

		plotNewmark.setActionCommand("plotNewmark");
		plotNewmark.addActionListener(this);

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
				clearOutput();

				JProgressBar mon = new JProgressBar();
				mon.setStringPainted(true);
				JFrame monFrame = new JFrame("Progress...");
				monFrame.getContentPane().add(mon);
				monFrame.setSize(400,75);
				GUIUtils.setLocationMiddle(monFrame);

				boolean paramUnit = parent.Parameters.unitEnglish.isSelected();
				boolean paramDualslope = parent.Parameters.dualSlope.isSelected();

				double paramScale;
				if(parent.Parameters.scalePGAon.isSelected())
				{
					Double d = (Double)Utils.checkNum(parent.Parameters.scalePGAval.getText(), "scale PGA field", null, false, null, new Double(0), false, null, false);
					if(d == null)
					{
						parent.selectParameters();
						monFrame.dispose();
						return;
					}
					paramScale = d.doubleValue();
				}
				else
					paramScale = 0;

				boolean paramDoScale = (paramScale > 0);

				boolean paramRigid = parent.Parameters.typeRigid.isSelected();
				boolean paramDecoupled = parent.Parameters.typeDecoupled.isSelected();
				boolean paramCoupled = parent.Parameters.typeCoupled.isSelected();
				if(!paramRigid && !paramDecoupled && !paramCoupled)
				{
					monFrame.dispose();
					parent.selectParameters();
					GUIUtils.popupError("Error: no analysis methods selected.");
					return;
				}

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
				double di;
				double avg, num = 0;
				double total[] = new double[3];
				double scale = 1, iscale;
				String norm = "", inv = "";
				double[][] ca;
				double thrust = 0, uwgt = 0, height = 0, vs = 0, damp = 0;

				if(parent.Parameters.CAdisp.isSelected())
				{
					String value;
					Vector caVect;
					TableCellEditor editor = null;

					editor = parent.Parameters.dispTable.getCellEditor();
					caVect = parent.Parameters.dispTableModel.getDataVector();

					if(editor != null)
						editor.stopCellEditing();

					ca = new double[caVect.size()][2];

					for(int i = 0; i < caVect.size(); i++)
					{
						for(int j = 0; j < 2; j++)
						{
							value = (String)(((Vector)(caVect.elementAt(i))).elementAt(j));
							if(value == null || value == "")
							{
								parent.selectParameters();
								monFrame.dispose();
								GUIUtils.popupError("Error: empty field in table.\nPlease complete the displacement table so that all data pairs have values, or delete all empty rows.");
								return;
							}
							Double d = (Double)Utils.checkNum(value, "displacement table", null, false, null, null, false, null, false);
							if(d == null)
							{
								monFrame.dispose();
								return;
							}
							ca[i][j] = d.doubleValue();
						}
					}

					if(caVect.size() == 0)
					{
						parent.selectParameters();
						monFrame.dispose();
						GUIUtils.popupError("Error: no displacements listed in displacement table.");
						return;
					}
				}
				else
				{
					Double d = (Double)Utils.checkNum(parent.Parameters.CAconstTF.getText(), "constant critical acceleration field", null, false, null, new Double(0), true, null, false);
					if(d == null)
					{
						parent.selectParameters();
						monFrame.dispose();
						return;
					}
					ca = new double[1][2];
					ca[0][0] = 0;
					ca[0][1] = d.doubleValue();
				}

				if(paramDualslope)
				{
					Double thrustD = (Double)Utils.checkNum(parent.Parameters.thrustAngle.getText(), "thrust angle field", new Double(90), true, null, new Double(0), true, null, false);
					if(thrustD == null)
					{
						parent.selectParameters();
						monFrame.dispose();
						return;
					}
					else
						thrust = thrustD.doubleValue();
				}

				if(paramDecoupled || paramCoupled)
				{
					Double tempd;

					tempd = (Double)Utils.checkNum(parent.Parameters.paramUwgt.getText(), CoupledPanel.stringUwgt + " field", null, false, null, null, false, null, false);
					if(tempd == null)
					{
						parent.selectParameters();
						monFrame.dispose();
						return;
					}
					else
						uwgt = tempd.doubleValue();

					tempd = (Double)Utils.checkNum(parent.Parameters.paramHeight.getText(), CoupledPanel.stringHeight + " field", null, false, null, null, false, null, false);
					if(tempd == null)
					{
						parent.selectParameters();
						monFrame.dispose();
						return;
					}
					else
						height = tempd.doubleValue();

					tempd = (Double)Utils.checkNum(parent.Parameters.paramVs.getText(), CoupledPanel.stringVs + " field", null, false, null, null, false, null, false);
					if(tempd == null)
					{
						parent.selectParameters();
						monFrame.dispose();
						return;
					}
					else
						vs = tempd.doubleValue();

					tempd = (Double)Utils.checkNum(parent.Parameters.paramDamp.getText(), CoupledPanel.stringDamp + " field", null, false, null, null, false, null, false);
					if(tempd == null)
					{
						parent.selectParameters();
						monFrame.dispose();
						return;
					}
					else
						damp = tempd.doubleValue() / 100.0;
				}

				File testFile;
				String path;
				mon.setMinimum(0);
				mon.setMaximum(res.length - 2);
				monFrame.show();
				dataVect[RB] = new Vector(res.length - 1);
				dataVect[DC] = new Vector(res.length - 1);
				dataVect[CP] = new Vector(res.length - 1);
				xysc[RB] = new XYSeriesCollection();
				xysc[DC] = new XYSeriesCollection();
				xysc[CP] = new XYSeriesCollection();
				iscale = -1.0 * scale;
				int j;
				Object[] row;
				for(int i = 1; i < res.length; i++)
				{
					row = new Object[5];
					eq = res[i][0].toString();
					record = res[i][1].toString();
					mon.setString(eq + " - " + record);
					mon.setValue(i);
					mon.paintImmediately(0,0,mon.getWidth(),mon.getHeight());

					row[0] = eq;
					row[1] = record;
					row[2] = null;
					row[3] = null;
					row[4] = null;

					path = res[i][3].toString();
					testFile = new File(path);
					if(!testFile.exists() || !testFile.canRead())
					{
						row[2] = "File does not exist or is not readable";
						row[3] = path;
						outputTableModel.addRow(row);
						continue;
					}
					dat = new DoubleList(path);
					if(dat.bad())
					{
						row[2] = "Invalid data at point " + dat.badEntry();
						row[3] = path;
						outputTableModel.addRow(row);
						continue;
					}

					di = Double.parseDouble(res[i][2].toString());

					if(paramDoScale)
					{
						scale = paramScale / Double.parseDouble(res[i][4].toString());
						iscale = -1.0 * scale;
					}

					// do the actual analysis

					if(paramRigid)
					{
						inv = RigidBlock.NewmarkRigorousDisp(dat, di, ca, iscale);
						norm = RigidBlock.NewmarkRigorousDisp(dat, di, ca, scale);
						avg = avg(inv, norm);

						total[RB] += avg;
						row[RBC] = new Double(avg);

						Analysis.xys.setName(eq + " - " + record);
						xysc[RB].addSeries(Analysis.xys);

						for(j = 0; j < dataVect[RB].size() && ((Double)dataVect[RB].elementAt(j)).doubleValue() < avg; j++);
						dataVect[RB].insertElementAt(new Double(avg), j);
					}

					if(paramCoupled)
					{
						inv = Coupled.Coupled(dat, Analysis.Gcmss, di, iscale, uwgt, height, vs, damp, 0, ca);
						norm = Coupled.Coupled(dat, Analysis.Gcmss, di, scale, uwgt, height, vs, damp, 0, ca);
						avg = avg(inv, norm);

						total[CP] += avg;
						row[CPC] = new Double(avg);

						Analysis.xys.setName(eq + " - " + record);
						xysc[CP].addSeries(Analysis.xys);

						for(j = 0; j < dataVect[CP].size() && ((Double)dataVect[CP].elementAt(j)).doubleValue() < avg; j++);
						dataVect[CP].insertElementAt(new Double(avg), j);
					}

					outputTableModel.addRow(row);
				}
				mon.setString("Calculating stastistics...");
				mon.paintImmediately(0,0,mon.getWidth(),mon.getHeight());
				/*
				if(dataVect.size() == 0)
				{
					monFrame.dispose();
					return;
				}
				max = ((Double)dataVect.elementAt(dataVect.size() - 1)).doubleValue();

				double mean = Double.parseDouble(Analysis.fmtOne.format(total/num));
				outputMean.setText("Mean value is: " + Analysis.fmtOne.format(mean) + " cm");
				outputMedian.setText("Median value is: " + Analysis.fmtOne.format(dataVect.elementAt((int)(num/2))) + " cm");

				double value = 0;
				double valtemp;
				for(int i = 0; i < num; i++)
				{
					valtemp = mean - ((Double)dataVect.elementAt(i)).doubleValue();
					value += (valtemp * valtemp);
				}
				value /= num - 1;
				value = Math.sqrt(value);
				outputStdDev.setText("Standard Deviation is: " + Analysis.fmtOne.format(value) + " cm");
				*/
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
				/*
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

					JFreeChart hist = ChartFactory.createHistogram("Histogram of Newmark Displacements", "Newmark Displacement (cm)", "Number of Records", dataset, org.jfree.chart.plot.PlotOrientation.VERTICAL, false, false, false);
					ChartFrame frame = new ChartFrame("Histogram of Newmark Displacements", hist);

					frame.pack();
					frame.setLocationRelativeTo(null);
					frame.show();
				}
				*/
			}
			else if(command.equals("plotNewmark"))
			{
				JFreeChart chart = ChartFactory.createXYLineChart("Newmark displacement versus time", "Time (s)", "Newmark displacement (cm)", xysc[RB], org.jfree.chart.plot.PlotOrientation.VERTICAL, plotNewmarkLegend.isSelected(), true, false);
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

	private double avg(String a, String b)
	{
		return((Double.parseDouble(a) + Double.parseDouble(b)) / 2.0);
	}
}
