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
	JTextField outputBins = new JTextField("10", 2);
	JComboBox plotAnalysis = new JComboBox(new Object[] {ParametersPanel.stringRB, ParametersPanel.stringDC, ParametersPanel.stringCP});

	XYSeriesCollection xysc[] = new XYSeriesCollection[3];

	JRadioButton outputDelTab = new JRadioButton("tab delimited", true);
	JRadioButton outputDelSpace = new JRadioButton("space delimited");
	JRadioButton outputDelComma = new JRadioButton("comma delimited");
	ButtonGroup outputDelGroup = new ButtonGroup();

	boolean paramUnit = false;
	String unitDisplacement = "";
	DecimalFormat unitFmt;
	Vector dataVect[] = new Vector[3];
	double max;
	XYSeriesCollection xycol;
	String parameters;

	public ResultsPanel(NewmarkTabbedPane parent) throws Exception
	{
		this.parent = parent;

		outputTableModel.addColumn("Earthquake");
		outputTableModel.addColumn("Record");
		outputTableModel.addColumn(ParametersPanel.stringRB);
		outputTableModel.addColumn(ParametersPanel.stringDC);
		outputTableModel.addColumn(ParametersPanel.stringCP);

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
				final SwingWorker worker = new SwingWorker()
				{
					ProgressFrame pm = new ProgressFrame(0);

					public Object construct()
					{
						try
						{
							clearOutput();

							paramUnit = parent.Parameters.unitMetric.isSelected();
							final double g = paramUnit ? Analysis.Gcmss : Analysis.Gftss;
							unitDisplacement = paramUnit ? "(cm)" : "(ft)";
							outputTableModel.setColumnIdentifiers(new Object[] {"Earthquake", "Record", ParametersPanel.stringRB + " " + unitDisplacement, ParametersPanel.stringDC + " " + unitDisplacement, ParametersPanel.stringCP + " " + unitDisplacement});
							unitFmt = paramUnit ? Analysis.fmtOne : Analysis.fmtFour;

							boolean paramDualslope = parent.Parameters.dualSlope.isSelected();

							double paramScale;
							if(parent.Parameters.scalePGAon.isSelected())
							{
								Double d = (Double)Utils.checkNum(parent.Parameters.scalePGAval.getText(), "scale PGA field", null, false, null, new Double(0), false, null, false);
								if(d == null)
								{
									parent.selectParameters();
									return null;
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
								parent.selectParameters();
								GUIUtils.popupError("Error: no analysis methods selected.");
								return null;
							}

							Object[][] res = Utils.getDB().runQuery("select eq, record, digi_int, path, pga from data where select2=true and analyze=true");

							if(res == null || res.length <= 1)
							{
								parent.selectSelectRecords();
								GUIUtils.popupError("No records selected for analysis.");
								return null;
							}

							String eq, record;
							DoubleList dat;
							double di;
							double num = 0;
							Double avg;
							double total[] = new double[3];
							double scale = 1, iscale, scaleRB;
							double inv, norm;
							double[][] ca;
							double[] ain;
							double thrust = 0, uwgt = 0, height = 0, vs = 0, damp = 0, vr = 0;
							int dv2 = 0, dv3 = 0;

							scaleRB = paramUnit ? 1 : Analysis.CMtoFT;

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
											GUIUtils.popupError("Error: empty field in table.\nPlease complete the displacement table so that all data pairs have values, or delete all empty rows.");
											return null;
										}
										Double d = (Double)Utils.checkNum(value, "displacement table", null, false, null, null, false, null, false);
										if(d == null)
										{
											parent.selectParameters();
											return null;
										}
										ca[i][j] = d.doubleValue();
									}
								}

								if(caVect.size() == 0)
								{
									parent.selectParameters();
									GUIUtils.popupError("Error: no displacements listed in displacement table.");
									return null;
								}
							}
							else
							{
								Double d = (Double)Utils.checkNum(parent.Parameters.CAconstTF.getText(), "constant critical acceleration field", null, false, null, new Double(0), true, null, false);
								if(d == null)
								{
									parent.selectParameters();
									return null;
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
									return null;
								}
								else
									thrust = thrustD.doubleValue();
							}

							if(paramDecoupled || paramCoupled)
							{
								Double tempd;

								tempd = (Double)Utils.checkNum(parent.Parameters.paramUwgt.getText(), ParametersPanel.stringUwgt + " field", null, false, null, null, false, null, false);
								if(tempd == null)
								{
									parent.selectParameters();
									return null;
								}
								else
									uwgt = tempd.doubleValue();

								tempd = (Double)Utils.checkNum(parent.Parameters.paramHeight.getText(), ParametersPanel.stringHeight + " field", null, false, null, null, false, null, false);
								if(tempd == null)
								{
									parent.selectParameters();
									return null;
								}
								else
									height = tempd.doubleValue();

								tempd = (Double)Utils.checkNum(parent.Parameters.paramVs.getText(), ParametersPanel.stringVs + " field", null, false, null, null, false, null, false);
								if(tempd == null)
								{
									parent.selectParameters();
									return null;
								}
								else
									vs = tempd.doubleValue();

								tempd = (Double)Utils.checkNum(parent.Parameters.paramDamp.getText(), ParametersPanel.stringDamp + " field", null, false, null, null, false, null, false);
								if(tempd == null)
								{
									parent.selectParameters();
									return null;
								}
								else
									damp = tempd.doubleValue() / 100.0;

								dv2 = parent.Parameters.paramBaseType.getSelectedIndex();
								dv3 = parent.Parameters.paramSoilModel.getSelectedIndex();

								if(dv2 == 1)
								{
									tempd = (Double)Utils.checkNum(parent.Parameters.paramVr.getText(), ParametersPanel.stringVr + " field", null, false, null, null, false, null, false);
									if(tempd == null)
									{
										parent.selectParameters();
										return null;
									}

									vr = tempd.doubleValue();

									if((vr / vs) <= 2.5)
									{
										GUIUtils.popupError("Error: Shear wave velocity of rock must be at least 2.5 times larger than Shear wave velocity of soil.");
										parent.selectParameters();
										return null;
									}
								}
							}

							File testFile;
							String path;

							if(paramRigid)
							{
								dataVect[RB] = new Vector(res.length - 1);
								xysc[RB] = new XYSeriesCollection();
							}
							else
							{
								dataVect[RB] = null;
								xysc[RB] = null;
							}

							if(paramDecoupled)
							{
								dataVect[DC] = new Vector(res.length - 1);
								xysc[DC] = new XYSeriesCollection();
							}
							else
							{
								dataVect[DC] = null;
								xysc[DC] = null;
							}

							if(paramCoupled)
							{
								dataVect[CP] = new Vector(res.length - 1);
								xysc[CP] = new XYSeriesCollection();
							}
							else
							{
								dataVect[CP] = null;
								xysc[CP] = null;
							}

							iscale = -1.0 * scale;


							pm.setMaximum(res.length);

							int j;
							Object[] row;

							for(int i = 1; i < res.length && !pm.isCanceled(); i++)
							{
								row = new Object[5];
								eq = res[i][0].toString();
								record = res[i][1].toString();

								pm.update(i, eq + " - " + record);

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
									iscale = -scale;
								}

								ain = dat.getAsArray();

								// do the actual analysis

								if(paramRigid)
								{
									inv = RigidBlock.NewmarkRigorous(dat, di, ca, iscale * scaleRB, paramDualslope, thrust);
									norm = RigidBlock.NewmarkRigorous(dat, di, ca, scale * scaleRB, paramDualslope, thrust);
									avg = avg(inv, norm, unitFmt);

									total[RB] += avg.doubleValue();
									row[RBC] = avg;

									Analysis.xys.setName(eq + " - " + record);
									xysc[RB].addSeries(Analysis.xys);

									for(j = 0; j < dataVect[RB].size() && ((Double)dataVect[RB].elementAt(j)).doubleValue() < avg.doubleValue(); j++);
									dataVect[RB].insertElementAt(avg, j);
								}

								if(paramDecoupled)
								{
									inv = Decoupled.Decoupled(ain, uwgt, height, vs, damp, di, iscale / Analysis.Gcmss, g, vr, ca, dv2, dv3);
									norm = Decoupled.Decoupled(ain, uwgt, height, vs, damp, di, scale / Analysis.Gcmss, g, vr, ca, dv2, dv3);

									avg = avg(inv, norm, unitFmt);

									total[DC] += avg.doubleValue();
									row[DCC] = avg;

									Analysis.xys.setName(eq + " - " + record);
									xysc[DC].addSeries(Analysis.xys);

									for(j = 0; j < dataVect[DC].size() && ((Double)dataVect[DC].elementAt(j)).doubleValue() < avg.doubleValue(); j++);
									dataVect[DC].insertElementAt(avg, j);
								}

								if(paramCoupled)
								{
									inv = Coupled.Coupled(ain, uwgt, height, vs, damp, di, iscale / Analysis.Gcmss, g, vr, ca, dv2, dv3);
									norm = Coupled.Coupled(ain, uwgt, height, vs, damp, di, scale / Analysis.Gcmss, g, vr, ca, dv2, dv3);

									avg = avg(inv, norm, unitFmt);

									total[CP] += avg.doubleValue();
									row[CPC] = avg;

									Analysis.xys.setName(eq + " - " + record);
									xysc[CP].addSeries(Analysis.xys);

									for(j = 0; j < dataVect[CP].size() && ((Double)dataVect[CP].elementAt(j)).doubleValue() < avg.doubleValue(); j++);
									dataVect[CP].insertElementAt(avg, j);
								}

								outputTableModel.addRow(row);
							}
							pm.update("Calculating stastistics...");
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
						}
						catch(Throwable ex)
						{
							Utils.catchException(ex);
						}

						return null;
					}

					public void finished() {
						pm.dispose();
					}
				};
				worker.start();
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
				int i = plotAnalysis.getSelectedIndex();
				String s = plotAnalysis.getSelectedItem().toString();

				if(dataVect[i] == null)
					return;

				Double Bins = (Double)Utils.checkNum(outputBins.getText(), "output bins field", null, false, null, new Double(0), false, null, false);

				if(Bins != null)
				{
					double series[] = new double[dataVect[i].size()];

					for(int j = 0; j < dataVect[i].size(); j++)
					{
						series[j] = (((Double)dataVect[i].elementAt(j)).doubleValue());
					}

					HistogramDataset dataset = new HistogramDataset();
					dataset.addSeries("", series, (int)Bins.doubleValue());

					JFreeChart hist = ChartFactory.createHistogram("Histogram of " + s + " Displacements", s + " " + unitDisplacement, "Number of Records", dataset, org.jfree.chart.plot.PlotOrientation.VERTICAL, false, false, false);
					ChartFrame frame = new ChartFrame("Histogram of " + s + " Displacements", hist);

					frame.pack();
					frame.setLocationRelativeTo(null);
					frame.show();
				}
			}
			else if(command.equals("plotNewmark"))
			{
				int i = plotAnalysis.getSelectedIndex();
				String s = plotAnalysis.getSelectedItem().toString();

				if(xysc[i] == null)
					return;

				JFreeChart chart = ChartFactory.createXYLineChart(s + " displacement versus time", "Time (s)", s + " displacement " + unitDisplacement, xysc[i], org.jfree.chart.plot.PlotOrientation.VERTICAL, plotNewmarkLegend.isSelected(), true, false);
				ChartFrame frame = new ChartFrame(s + " displacement versus time", chart);
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
		dataVect[RB] = null;
		dataVect[DC] = null;
		dataVect[CP] = null;

		xysc[RB] = null;
		xysc[DC] = null;
		xysc[CP] = null;

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
		JPanel panel = new JPanel();

		Insets left = new Insets(0, 20, 0, 0);
		Insets none = new Insets(0, 0, 0, 0);

		GridBagLayout gridbag = new GridBagLayout();
		panel.setLayout(gridbag);

		GridBagConstraints c = new GridBagConstraints();
		JLabel label;

		int y = 0;
		int x = 0;

		c.gridx = x++;
		c.gridy = y++;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(plotOutput, c);
		panel.add(plotOutput);

		c.gridx = x++;
		c.weightx = 0;
		label = new JLabel("Plot with ");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		gridbag.setConstraints(outputBins, c);
		panel.add(outputBins);

		c.gridx = x++;
		label = new JLabel(" bins.");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridx = x++;
		c.insets = left;
		label = new JLabel("Analysis:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		x = 0;

		c.gridy = y++;
		c.gridx = x++;
		c.weightx = 1;
		c.insets = none;
		gridbag.setConstraints(plotNewmark, c);
		panel.add(plotNewmark);

		c.gridx = x;
		c.gridwidth = 3;
		c.weightx = 0;
		gridbag.setConstraints(plotNewmarkLegend, c);
		panel.add(plotNewmarkLegend);

		x += 3;

		c.gridx = x;
		c.gridwidth = 1;
		c.insets = left;
		gridbag.setConstraints(plotAnalysis, c);
		panel.add(plotAnalysis);

		return panel;
	}

	private Double avg(final double a, final double b, DecimalFormat f)
	{
		return (new Double(f.format(new Double(
			(a + b) / 2.0
			))));
	}
}
