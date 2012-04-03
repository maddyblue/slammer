/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import org.jfree.data.xy.*;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import slammer.*;
import slammer.analysis.*;

class ResultsPanel extends JPanel implements ActionListener
{
	public class ResultsRenderer extends DefaultTableCellRenderer
	{
		// from JTableHeader.java
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if (table != null) {
				JTableHeader header = table.getTableHeader();
				if (header != null) {
					setForeground(header.getForeground());
					setBackground(header.getBackground());
					setFont(header.getFont());
				}
			}

			setText((value == null) ? "" : value.toString());
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setPreferredSize(new Dimension(table.getColumnModel().getTotalColumnWidth(), 32));
			setHorizontalAlignment(JLabel.CENTER);

			return this;
		}
	}

	public class ResultThread implements Runnable
	{
		private boolean finished = false;

		int idx;
		int row;
		int analysis;
		int orientation;

		String eq, record;

		double[] ain;

		double result;
		XYSeries graphData;
		double _kmax, _vs, _damp;

		double scale, scaleRB;
		double di;
		double thrust, uwgt, height, vs, damp, refstrain, vr, g;
		double[][] ca;
		boolean paramDualslope, dv3;

		SynchronizedProgressFrame pm;

		// RigidBlock
		ResultThread(String eq, String record, int idx, int row, int analysis, int orientation, double[] ain, double di, double[][] ca, double scale, boolean paramDualslope, double thrust, double scaleRB, SynchronizedProgressFrame pm)
		{
			this.eq = eq;
			this.record = record;
			this.idx = idx;
			this.row = row;
			this.analysis = analysis;
			this.orientation = orientation;
			this.ain = ain;
			this.di = di;
			this.ca = ca;
			this.scale = scale;
			this.paramDualslope = paramDualslope;
			this.thrust = thrust;
			this.scaleRB = scaleRB;
			this.pm = pm;
		}

		// Decoupled
		ResultThread(String eq, String record, int idx, int row, int analysis, int orientation, double[] ain, double uwgt, double height, double vs, double damp, double refstrain, double di, double scale, double g, double vr, double[][] ca, boolean dv3, SynchronizedProgressFrame pm)
		{
			this.eq = eq;
			this.record = record;
			this.idx = idx;
			this.row = row;
			this.analysis = analysis;
			this.orientation = orientation;
			this.ain = ain;
			this.uwgt = uwgt;
			this.height = height;
			this.vs = vs;
			this.damp = damp;
			this.refstrain = refstrain;
			this.di = di;
			this.scale = scale;
			this.g = g;
			this.vr = vr;
			this.ca = ca;
			this.dv3 = dv3;
			this.pm = pm;
		}

		public void run()
		{
			Analysis a;

			if(analysis == RB)
			{
				a = new RigidBlock();
				result = a.SlammerRigorous(ain, di, ca, scale, paramDualslope, thrust, scaleRB);
			}
			else if(analysis == DC)
			{
				a = new Decoupled();
				result = a.Decoupled(ain, uwgt, height, vs, damp, refstrain, di, scale, g, vr, ca, dv3);
				_kmax = Math.abs(a._kmax);
				_vs = Math.abs(a._vs);
				_damp = Math.abs(a._damp);
			}
			else if(analysis == CP)
			{
				a = new Coupled();
				result = a.Coupled(ain, uwgt, height, vs, damp, refstrain, di, scale, g, vr, ca, dv3);
				_kmax = Math.abs(a._kmax);
				_vs = Math.abs(a._vs);
				_damp = Math.abs(a._damp);
			}
			else
				a = null;

			graphData = a.graphData;
			finished = true;
			pm.incr(eq + " - " + record);
		}

		public boolean finished()
		{
			return finished;
		}
	}

	public class SynchronizedProgressFrame extends ProgressFrame
	{
		private int i;

		public SynchronizedProgressFrame(int i)
		{
			super(i);
			this.i = i;
		}

		public synchronized void incr(String s)
		{
			i++;

			if(!isCanceled())
				update(i, s);
		}
	}

	// array indexes
	public final static int RB = 0; // rigid block
	public final static int DC = 1; // decoupled
	public final static int CP = 2; // coupled
	public final static int ANALYSIS_TYPES = 3;
	public final static int AVG = 2; // average
	public final static int NOR = 0; // normal
	public final static int INV = 1; // inverse
	public final static int ORIENTATION_TYPES = 3;

	// table column indicies
	public final static int[][] tableCols = {
	//  N RB  N DC   N  CP  N   DY LEN
		{ 2, 3, 6, 7, 10, 11, 0,   0, 14 }, // no dynamic
		{ 2, 3, 6, 7, 10, 11, 14, 15, 18 }, // with dynamic
	};

	public final static int N_RB = 0;
	public final static int I_RB = 1;
	public final static int N_DC = 2;
	public final static int I_DC = 3;
	public final static int N_CP = 4;
	public final static int I_CP = 5;
	public final static int N_DY = 6;
	public final static int I_DY = 7;
	public final static int LEN = 8;

	public final static int NO_DYN = 0;
	public final static int WITH_DYN = 1;

	public static int NUM_CORES;
	java.util.Vector<ResultThread> resultVec;
	ExecutorService pool;

	String polarityName[] = { "Normal", "Inverse", "Average" };
	SlammerTabbedPane parent;
	JTextField decimalsTF = new JTextField("1", 2);
	JButton Analyze = new JButton("Perform analyses");
	JCheckBox dynamicRespParams = new JCheckBox("Display dynamic response parameters");
	JButton ClearOutput = new JButton("Clear output");
	DefaultTableModel outputTableModel = new DefaultTableModel();

	JTable outputTable = new JTable(outputTableModel);
	JScrollPane outputTablePane = new JScrollPane(outputTable);

	JButton saveResultsOutput = new JButton("Save results table");
	JFileChooser fc = new JFileChooser();

	JButton plotHistogram = new JButton("Plot histogram of displacements");
	JButton plotDisplacement = new JButton("Plot displacements versus time");
	JCheckBox plotDisplacementLegend = new JCheckBox("Display legend", false);
	JTextField outputBins = new JTextField("10", 2);

	JRadioButton polarityNorDisp = new JRadioButton(polarityName[NOR], true);
	JRadioButton polarityInvDisp = new JRadioButton(polarityName[INV]);
	ButtonGroup polarityGroupDisp = new ButtonGroup();

	JRadioButton polarityAvgHist = new JRadioButton(polarityName[AVG], true);
	JRadioButton polarityNorHist = new JRadioButton(polarityName[NOR]);
	JRadioButton polarityInvHist = new JRadioButton(polarityName[INV]);
	ButtonGroup polarityGroupHist = new ButtonGroup();

	JCheckBox analysisDisp[] = new JCheckBox[ANALYSIS_TYPES];
	JRadioButton analysisHist[] = new JRadioButton[ANALYSIS_TYPES];
	String analysisTitle[] = new String[ANALYSIS_TYPES];
	ButtonGroup analysisHistGroup = new ButtonGroup();

	ArrayList results;
	XYSeries xys[][][];

	JRadioButton outputDelTab = new JRadioButton("Tab delimited", true);
	JRadioButton outputDelSpace = new JRadioButton("Space delimited");
	JRadioButton outputDelComma = new JRadioButton("Comma delimited");
	ButtonGroup outputDelGroup = new ButtonGroup();

	boolean paramUnit = false;
	String unitDisplacement = "";
	DecimalFormat unitFmt;
	ArrayList dataVect[][];
	XYSeriesCollection xycol;
	String parameters;

	public ResultsPanel(SlammerTabbedPane parent) throws Exception
	{
		this.parent = parent;

		Analyze.setActionCommand("analyze");
		Analyze.addActionListener(this);

		ClearOutput.setActionCommand("clearOutput");
		ClearOutput.addActionListener(this);

		saveResultsOutput.setActionCommand("saveResultsOutput");
		saveResultsOutput.addActionListener(this);

		plotHistogram.setActionCommand("plotHistogram");
		plotHistogram.addActionListener(this);

		plotDisplacement.setActionCommand("plotDisplacement");
		plotDisplacement.addActionListener(this);

		plotDisplacementLegend.setActionCommand("plotDisplacementLegend");
		plotDisplacementLegend.addActionListener(this);

		outputDelGroup.add(outputDelTab);
		outputDelGroup.add(outputDelSpace);
		outputDelGroup.add(outputDelComma);

		polarityGroupDisp.add(polarityNorDisp);
		polarityGroupDisp.add(polarityInvDisp);

		polarityGroupHist.add(polarityAvgHist);
		polarityGroupHist.add(polarityNorHist);
		polarityGroupHist.add(polarityInvHist);

		analysisDisp[RB] = new JCheckBox(ParametersPanel.stringRB);
		analysisDisp[DC] = new JCheckBox(ParametersPanel.stringDC);
		analysisDisp[CP] = new JCheckBox(ParametersPanel.stringCP);
		analysisHist[RB] = new JRadioButton(ParametersPanel.stringRB);
		analysisHist[DC] = new JRadioButton(ParametersPanel.stringDC);
		analysisHist[CP] = new JRadioButton(ParametersPanel.stringCP);

		analysisHistGroup.add(analysisHist[RB]);
		analysisHistGroup.add(analysisHist[DC]);
		analysisHistGroup.add(analysisHist[CP]);

		analysisTitle[RB] = "Rigid-Block";
		analysisTitle[DC] = "Decoupled";
		analysisTitle[CP] = "Coupled";

		setLayout(new BorderLayout());

		add(BorderLayout.NORTH, createHeader());
		add(BorderLayout.CENTER, createTable());
		add(BorderLayout.SOUTH, createGraphs());

		clearOutput();
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			if(command.equals("analyze"))
			{
				final SwingWorker worker = new SwingWorker()
				{
					SynchronizedProgressFrame pm = new SynchronizedProgressFrame(0);

					public Object construct()
					{
						try
						{
							clearOutput();

							paramUnit = parent.Parameters.unitMetric.isSelected();
							final double g = paramUnit ? Analysis.Gcmss : Analysis.Ginss;
							int dyn = dynamicRespParams.isSelected() ? WITH_DYN : NO_DYN;
							unitDisplacement = paramUnit ? "(cm)" : "(in.)";

							String h_rb = "<html><center>" + ParametersPanel.stringRB + " " + unitDisplacement + "<p>";
							String h_dc = "<html><center>" + ParametersPanel.stringDC + " " + unitDisplacement + "<p>";
							String h_cp = "<html><center>" + ParametersPanel.stringCP + " " + unitDisplacement + "<p>";
							String h_km = "<html><center>k<sub>max</sub> (g)<p>";
							String h_damp = "<html><center>damp<p>";

							String h_vs = "<html><center>";
							if(parent.Parameters.paramSoilModel.getSelectedIndex() == 1)
								h_vs += "EQL ";
							h_vs += "V<sub>s</sub> (m/s)<p>";

							if(dyn == NO_DYN)
								outputTableModel.setColumnIdentifiers(new Object[] {"Earthquake", "Record", "",
									h_rb + polarityName[NOR], h_rb + polarityName[INV], h_rb + polarityName[AVG], "",
									h_dc + polarityName[NOR], h_dc + polarityName[INV], h_dc + polarityName[AVG], "",
									h_cp + polarityName[NOR], h_cp + polarityName[INV], h_cp + polarityName[AVG]
								});
							else
								outputTableModel.setColumnIdentifiers(new Object[] {"Earthquake", "Record", "",
									h_rb + polarityName[NOR], h_rb + polarityName[INV], h_rb + polarityName[AVG], "",
									h_dc + polarityName[NOR], h_dc + polarityName[INV], h_dc + polarityName[AVG], "",
									h_cp + polarityName[NOR], h_cp + polarityName[INV], h_cp + polarityName[AVG], "",
									h_km, h_vs, h_damp
								});

							outputTable.getTableHeader().setDefaultRenderer(new ResultsRenderer());

							outputTable.getColumnModel().getColumn(tableCols[dyn][N_RB]).setMinWidth(0);
							outputTable.getColumnModel().getColumn(tableCols[dyn][N_DC]).setMinWidth(0);
							outputTable.getColumnModel().getColumn(tableCols[dyn][N_CP]).setMinWidth(0);
							outputTable.getColumnModel().getColumn(tableCols[dyn][N_RB]).setPreferredWidth(5);
							outputTable.getColumnModel().getColumn(tableCols[dyn][N_DC]).setPreferredWidth(5);
							outputTable.getColumnModel().getColumn(tableCols[dyn][N_CP]).setPreferredWidth(5);
							outputTable.getColumnModel().getColumn(tableCols[dyn][N_RB]).setMaxWidth(5);
							outputTable.getColumnModel().getColumn(tableCols[dyn][N_DC]).setMaxWidth(5);
							outputTable.getColumnModel().getColumn(tableCols[dyn][N_CP]).setMaxWidth(5);

							if(dyn == WITH_DYN)
							{
								outputTable.getColumnModel().getColumn(tableCols[dyn][N_DY]).setMinWidth(0);
								outputTable.getColumnModel().getColumn(tableCols[dyn][N_DY]).setPreferredWidth(5);
								outputTable.getColumnModel().getColumn(tableCols[dyn][N_DY]).setMaxWidth(5);
							}

							boolean paramDualslope = parent.Parameters.dualSlope.isSelected();
							Double d;

							double paramScale;
							if(parent.Parameters.scalePGA.isSelected())
							{
								d = (Double)Utils.checkNum(parent.Parameters.scalePGAval.getText(), "scale PGA field", null, false, null, new Double(0), false, null, false);
								if(d == null)
								{
									parent.selectParameters();
									return null;
								}
								paramScale = d.doubleValue();
							}
							else if(parent.Parameters.scaleOn.isSelected())
							{
								d = (Double)Utils.checkNum(parent.Parameters.scaleData.getText(), "scale data field", null, false, null, null, false, null, false);
								if(d == null)
								{
									parent.selectParameters();
									return null;
								}
								paramScale = d.doubleValue();
							}
							else
								paramScale = 0;

							changeDecimal();

							boolean paramRigid = parent.Parameters.typeRigid.isSelected();
							boolean paramDecoupled = parent.Parameters.typeDecoupled.isSelected();
							boolean paramCoupled = parent.Parameters.typeCoupled.isSelected();

							graphDisp(paramRigid, paramDecoupled, paramCoupled);

							if(!paramRigid && !paramDecoupled && !paramCoupled)
							{
								parent.selectParameters();
								GUIUtils.popupError("Error: no analyses methods selected.");
								return null;
							}

							Object[][] res = Utils.getDB().runQuery("select eq, record, digi_int, path, pga from data where select2=1 and analyze=1");

							if(res == null || res.length <= 1)
							{
								parent.selectSelectRecords();
								GUIUtils.popupError("No records selected for analysis.");
								return null;
							}

							xys = new XYSeries[res.length][3][2];
							dataVect = new ArrayList[3][3];

							String eq, record;
							DoubleList dat;
							double di;
							int num = 0;
							double avg;
							double total[][] = new double[3][3];
							double scale = 1, iscale, scaleRB;
							double inv, norm;
							double[][] ca;
							double[] ain = null;
							double thrust = 0, uwgt = 0, height = 0, vs = 0, damp = 0, refstrain = 0, vr = 0;
							boolean dv3 = false;

							scaleRB = paramUnit ? 1 : Analysis.CMtoIN;

							if(parent.Parameters.CAdisp.isSelected())
							{
								String value;
								java.util.Vector caVect;
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
										value = (String)(((java.util.Vector)(caVect.get(i))).get(j));
										if(value == null || value == "")
										{
											parent.selectParameters();
											GUIUtils.popupError("Error: empty field in table.\nPlease complete the displacement table so that all data pairs have values, or delete all empty rows.");
											return null;
										}
										d = (Double)Utils.checkNum(value, "displacement table", null, false, null, null, false, null, false);
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
								d = (Double)Utils.checkNum(parent.Parameters.CAconstTF.getText(), "constant critical acceleration field", null, false, null, new Double(0), true, null, false);
								if(d == null)
								{
									parent.selectParameters();
									return null;
								}
								ca = new double[1][2];
								ca[0][0] = 0;
								ca[0][1] = d.doubleValue();
							}

							if(paramRigid && paramDualslope)
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

								uwgt = 100.0; // unit weight is disabled, so hardcode to 100

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

								tempd = (Double)Utils.checkNum(parent.Parameters.paramVr.getText(), ParametersPanel.stringVr + " field", null, false, null, null, false, null, false);
								if(tempd == null)
								{
									parent.selectParameters();
									return null;
								}
								else
									vr = tempd.doubleValue();

								tempd = (Double)Utils.checkNum(parent.Parameters.paramDamp.getText(), ParametersPanel.stringDamp + " field", null, false, null, null, false, null, false);
								if(tempd == null)
								{
									parent.selectParameters();
									return null;
								}
								else
									damp = tempd.doubleValue() / 100.0;

								tempd = (Double)Utils.checkNum(parent.Parameters.paramRefStrain.getText(), ParametersPanel.stringRefStrain + " field", null, false, null, null, false, null, false);
								if(tempd == null)
								{
									parent.selectParameters();
									return null;
								}
								else
									refstrain = tempd.doubleValue();

								dv3 = parent.Parameters.paramSoilModel.getSelectedIndex() == 1;

								// metric
								if(paramUnit)
								{
									uwgt /= Analysis.M3toCM3;
									height *= Analysis.MtoCM;
									vs *= Analysis.MtoCM;
									vr *= Analysis.MtoCM;
								}
								// english
								else
								{
									uwgt /= Analysis.FT3toIN3;
									height *= Analysis.FTtoIN;
									vs *= Analysis.FTtoIN;
									vr *= Analysis.FTtoIN;
								}
							}

							File testFile;
							String path;
							int num_analyses = 0;

							if(paramRigid)
							{
								num_analyses++;
								dataVect[RB][NOR] = new ArrayList<Double>(res.length - 1);
								dataVect[RB][INV] = new ArrayList<Double>(res.length - 1);
								dataVect[RB][AVG] = new ArrayList<Double>(res.length - 1);
							}

							if(paramDecoupled)
							{
								num_analyses++;
								dataVect[DC][NOR] = new ArrayList<Double>(res.length - 1);
								dataVect[DC][INV] = new ArrayList<Double>(res.length - 1);
								dataVect[DC][AVG] = new ArrayList<Double>(res.length - 1);
							}

							if(paramCoupled)
							{
								num_analyses++;
								dataVect[CP][NOR] = new ArrayList<Double>(res.length - 1);
								dataVect[CP][INV] = new ArrayList<Double>(res.length - 1);
								dataVect[CP][AVG] = new ArrayList<Double>(res.length - 1);
							}

							iscale = -1.0 * scale;

							pm.setMaximum(res.length * 2 * num_analyses);
							pm.update(0, "Initializing");

							int j, k;
							Object[] row;
							int rowcount = 0;

							resultVec = new java.util.Vector<ResultThread>(res.length * 2 * ANALYSIS_TYPES); // 2 orientations per analysis

							NUM_CORES = Runtime.getRuntime().availableProcessors();
							pool = Executors.newFixedThreadPool(NUM_CORES);
							ResultThread rt;

							int row_idx;

							long startTime = System.currentTimeMillis();

							for(int i = 1; i < res.length && !pm.isCanceled(); i++)
							{
								row = new Object[tableCols[dyn][LEN]];
								eq = res[i][0].toString();
								row_idx = i - 1;
								record = res[i][1].toString();

								row[0] = eq;
								row[1] = record;

								path = res[i][3].toString();
								testFile = new File(path);
								if(!testFile.exists() || !testFile.canRead())
								{
									row[2] = "File does not exist or is not readable";
									row[3] = path;
									outputTableModel.addRow(row);
									rowcount++;
									continue;
								}

								dat = new DoubleList(path, 0, parent.Parameters.scaleOn.isSelected() ? paramScale : 1.0);
								if(dat.bad())
								{
									row[2] = "Invalid data at point " + dat.badEntry();
									row[3] = path;
									outputTableModel.addRow(row);
									rowcount++;
									continue;
								}

								num++;

								di = Double.parseDouble(res[i][2].toString());

								if(parent.Parameters.scalePGA.isSelected())
								{
									scale = paramScale / Double.parseDouble(res[i][4].toString());
									iscale = -scale;
								}

								ain = dat.getAsArray();

								// do the actual analysis

								if(paramRigid)
								{
									rt = new ResultThread(eq, record, row_idx, rowcount, RB, NOR, ain, di, ca, scale, paramDualslope, thrust, scaleRB, pm);
									pool.execute(rt);
									resultVec.add(rt);
									rt = new ResultThread(eq, record, row_idx, rowcount, RB, INV, ain, di, ca, iscale, paramDualslope, thrust, scaleRB, pm);
									pool.execute(rt);
									resultVec.add(rt);
								}
								if(paramDecoupled)
								{
									// [i]scale is divided by Gcmss because the algorithm expects input data in Gs, but our input files are in cmss. this has nothing to do with, and is not affected by, the unit base being used (english or metric).
									rt = new ResultThread(eq, record, row_idx, rowcount, DC, NOR, ain, uwgt, height, vs, damp, refstrain, di, scale / Analysis.Gcmss, g, vr, ca, dv3, pm);
									pool.execute(rt);
									resultVec.add(rt);
									rt = new ResultThread(eq, record, row_idx, rowcount, DC, INV, ain, uwgt, height, vs, damp, refstrain, di, iscale / Analysis.Gcmss, g, vr, ca, dv3, pm);
									pool.execute(rt);
									resultVec.add(rt);
								}
								if(paramCoupled)
								{
									// [i]scale is divided by Gcmss because the algorithm expects input data in Gs, but our input files are in cmss. this has nothing to do with, and is not affected by, the unit base being used (english or metric).
									rt = new ResultThread(eq, record, row_idx, rowcount, CP, NOR, ain, uwgt, height, vs, damp, refstrain, di, scale / Analysis.Gcmss, g, vr, ca, dv3, pm);
									pool.execute(rt);
									resultVec.add(rt);
									rt = new ResultThread(eq, record, row_idx, rowcount, CP, INV, ain, uwgt, height, vs, damp, refstrain, di, iscale / Analysis.Gcmss, g, vr, ca, dv3, pm);
									pool.execute(rt);
									resultVec.add(rt);
								}

								outputTableModel.addRow(row);
								rowcount++;
							}

							pool.shutdown();

							while(!pool.awaitTermination(1, TimeUnit.SECONDS))
							{
								if(pm.isCanceled())
								{
									pool.shutdownNow();
									break;
								}
							}

							pm.update("Calculating results...");
							ResultThread prt;
							int i_analysis;

							for(int i = 0; i < resultVec.size(); i++)
							{
								rt = resultVec.get(i);

								if(!rt.finished())
									continue;

								rt.graphData.setKey(rt.eq + " - " + rt.record + " - " + ParametersPanel.stringRB + ", " + polarityName[NOR]);
								xys[rt.idx][rt.analysis][rt.orientation] = rt.graphData;
								total[rt.analysis][rt.orientation] += rt.result;
								i_analysis = 1 + rt.analysis * 2;

								for(j = 0; j < dataVect[rt.analysis][rt.orientation].size() && ((Double)dataVect[rt.analysis][rt.orientation].get(j)).doubleValue() < rt.result; j++)
									;
								dataVect[rt.analysis][rt.orientation].add(j, new Double(rt.result));

								outputTableModel.setValueAt(unitFmt.format(rt.result), rt.row, tableCols[dyn][i_analysis] + rt.orientation);

								// INV is always second, so NOR was already computed: we can compute avg now
								if(rt.orientation == INV)
								{
									prt = resultVec.get(i - 1); // NOR result
									avg = avg(rt.result, prt.result);

									total[rt.analysis][AVG] += avg;

									for(j = 0; j < dataVect[rt.analysis][AVG].size() && ((Double)dataVect[rt.analysis][AVG].get(j)).doubleValue() < avg; j++)
										;
									dataVect[rt.analysis][AVG].add(j, new Double(avg));

									outputTableModel.setValueAt(unitFmt.format(avg), rt.row, tableCols[dyn][i_analysis] + AVG);

									if(dyn == WITH_DYN && (rt.analysis == DC || rt.analysis == CP))
									{
										outputTableModel.setValueAt(unitFmt.format(rt._kmax / g), rt.row, tableCols[dyn][I_DY] + 0);
										outputTableModel.setValueAt(unitFmt.format(rt._vs  / Analysis.MtoCM), rt.row, tableCols[dyn][I_DY] + 1);
										outputTableModel.setValueAt(unitFmt.format(rt._damp), rt.row, tableCols[dyn][I_DY] + 2);
									}
								}
							}

							if(!pm.isCanceled())
							{
								double mean, value, valtemp;
								int idx;
								Object[] rmean = new Object[tableCols[dyn][LEN]];
								Object[] rmedian = new Object[tableCols[dyn][LEN]];
								Object[] rsd = new Object[tableCols[dyn][LEN]];

								rmean[1] = "Mean value";
								rmedian[1] = "Median value";
								rsd[1] = "Standard deviation";

								for(j = 0; j < total.length; j++)
								{
									for(k = 0; k < total[j].length; k++)
									{
										if(dataVect[j][k] == null || dataVect[j][k].size() == 0)
											continue;

										idx = tableCols[dyn][1 + j * 2] + k;

										mean = Double.parseDouble(unitFmt.format(total[j][k] / num));
										rmean[idx] = unitFmt.format(mean);

										if(num % 2 == 0)
										{
											double fst = (Double)dataVect[j][k].get(num / 2);
											double snd = (Double)dataVect[j][k].get(num / 2 - 1);
											rmedian[idx] = unitFmt.format(avg(fst, snd));
										}
										else
											rmedian[idx] = unitFmt.format(dataVect[j][k].get(num / 2));

										value = 0;

										for(int i = 0; i < num; i++)
										{
											valtemp = mean - ((Double)dataVect[j][k].get(i)).doubleValue();
											value += (valtemp * valtemp);
										}

										value /= num;
										value = Math.sqrt(value);
										rsd[idx] = unitFmt.format(value);
									}
								}

								outputTableModel.addRow(new Object[0]);
								outputTableModel.addRow(rmean);
								outputTableModel.addRow(rmedian);
								outputTableModel.addRow(rsd);
							}
						}
						catch(Throwable ex)
						{
							Utils.catchException(ex);
						}

						return null;
					}

					public void finished()
					{
						pm.dispose();
					}
				};
				worker.start();
			}
			else if(command.equals("clearOutput"))
			{
				clearOutput();
			}
			else if(command.equals("saveResultsOutput"))
			{
				if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
				{
					FileWriter fw = new FileWriter(fc.getSelectedFile());

					String delim;

					if(outputDelSpace.isSelected())
						delim = " ";
					else if(outputDelComma.isSelected())
						delim = ",";
					else
						delim = "\t";

					int c = outputTableModel.getColumnCount();
					int r = outputTableModel.getRowCount();

					// table column headers
					for(int i = 0; i < c; i++)
					{
						if(i != 0)
							fw.write(delim);

						fw.write(outputTableModel.getColumnName(i).replaceAll("\\<.*?\\>", ""));
					}

					fw.write("\n");

					Object o;

					for(int i = 0; i < r; i++)
					{
						for(int j = 0; j < c; j++)
						{
							if(j != 0)
								fw.write(delim);

							o = outputTableModel.getValueAt(i, j);
							if(o == null)
								o = "";

							fw.write(o.toString());
						}

						fw.write("\n");
					}

					fw.close();
				}
			}
			else if(command.equals("plotHistogram"))
			{
				if(dataVect == null)
					return;

				String name = "", title, pname;
				HistogramDataset dataset = new HistogramDataset();

				int polarity, analysis = -1;

				if(polarityAvgHist.isSelected()) polarity = AVG;
				else if(polarityNorHist.isSelected()) polarity = NOR;
				else if(polarityInvHist.isSelected()) polarity = INV;
				else polarity = -1;

				for(int i = 0; i < analysisHist.length; i++)
					if(analysisHist[i].isSelected())
						analysis = i;

				if(analysis == -1)
					return;

				pname = polarityName[polarity];

				Double Bins = (Double)Utils.checkNum(outputBins.getText(), "output bins field", null, false, null, new Double(0), false, null, false);

				if(Bins == null || dataVect[analysis][polarity] == null)
					return;

				name = analysisTitle[analysis];

				double series[] = new double[dataVect[analysis][polarity].size()];

				for(int j = 0; j < dataVect[analysis][polarity].size(); j++)
					series[j] = (((Double)dataVect[analysis][polarity].get(j)).doubleValue());

				dataset.addSeries(name, series, (int)Bins.doubleValue());

				title = "Histogram of " + name + " Displacements (" + pname + " Polarity)";

				JFreeChart hist = ChartFactory.createHistogram(title, "Displacement " + unitDisplacement, "Number of Records", dataset, org.jfree.chart.plot.PlotOrientation.VERTICAL, false, true, false);
				ChartFrame frame = new ChartFrame(title, hist);

				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
			else if(command.equals("plotDisplacement"))
			{
				XYSeriesCollection xysc = new XYSeriesCollection();

				int polarity = polarityNorDisp.isSelected() ? NOR : INV;
				String pname = polarityName[polarity];

				String name = "";
				boolean first = true;

				for(int i = 0; i < analysisDisp.length; i++)
				{
					if(analysisDisp[i].isSelected() && dataVect[i][polarity] != null)
					{
						if(first)
							first = false;
						else
							name += ", ";

						name += analysisTitle[i];

						for(int j = 0; j < dataVect[i][polarity].size(); j++)
							xysc.addSeries(xys[j][i][polarity]);
					}
				}

				if(first)
					return;

				name += " Displacement versus Time";

				JFreeChart chart = ChartFactory.createXYLineChart(name, "Time (s)", "Displacement--" + pname + " polarity " + unitDisplacement, xysc, org.jfree.chart.plot.PlotOrientation.VERTICAL, plotDisplacementLegend.isSelected(), true, false);

				//margins are stupid
				chart.getXYPlot().getDomainAxis().setLowerMargin(0);
				chart.getXYPlot().getDomainAxis().setUpperMargin(0);
				chart.getXYPlot().getDomainAxis().setLowerBound(0);

				ChartFrame frame = new ChartFrame(name, chart);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		}
		catch(Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	private JPanel createHeader()
	{
		JPanel containerL = new JPanel();
		containerL.add(new JLabel("Display results to"));
		containerL.add(decimalsTF);
		containerL.add(new JLabel("decimals. "));

		JPanel container = new JPanel(new BorderLayout());
		container.add(containerL, BorderLayout.WEST);
		container.add(Analyze, BorderLayout.CENTER);

		JPanel east = new JPanel();
		east.add(dynamicRespParams);
		east.add(ClearOutput);
		container.add(east, BorderLayout.EAST);

		return container;
	}

	private JPanel createTable()
	{
		JPanel outputTablePanel = new JPanel(new BorderLayout());
		outputTablePanel.add(BorderLayout.CENTER, outputTablePane);

		return outputTablePanel;
	}

	private JTabbedPane createGraphs()
	{
		JTabbedPane jtp = new JTabbedPane();

		jtp.add("Graph displacements", createGraphDisplacementsPanel());
		jtp.add("Graph histogram", createGraphHistogramPanel());
		jtp.add("Save results table", createSaveResultsPanel());

		return jtp;
	}

	private JPanel createGraphDisplacementsPanel()
	{
		JPanel panel = new JPanel();

		GridBagLayout gridbag = new GridBagLayout();
		panel.setLayout(gridbag);
		GridBagConstraints c = new GridBagConstraints();
		JLabel label;

		int x = 0;
		int y = 0;

		c.anchor = GridBagConstraints.NORTHWEST;

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Analyses:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		gridbag.setConstraints(analysisDisp[RB], c);
		panel.add(analysisDisp[RB]);

		c.gridy = y++;
		gridbag.setConstraints(analysisDisp[DC], c);
		panel.add(analysisDisp[DC]);

		c.gridy = y++;
		gridbag.setConstraints(analysisDisp[CP], c);
		panel.add(analysisDisp[CP]);

		y = 0;

		c.gridy = y++;
		c.gridx = x++;
		c.insets = new Insets(0, 5, 0, 0);
		label = new JLabel("Polarity:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		gridbag.setConstraints(polarityNorDisp, c);
		panel.add(polarityNorDisp);

		c.gridy = y++;
		gridbag.setConstraints(polarityInvDisp, c);
		panel.add(polarityInvDisp);

		y = 0;

		c.gridy = y++;
		c.gridx = x++;
		c.gridheight = 2;
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(plotDisplacement, c);
		panel.add(plotDisplacement);

		y++;
		c.gridy = y;
		gridbag.setConstraints(plotDisplacementLegend, c);
		panel.add(plotDisplacementLegend);

		c.gridx = x;
		label = new JLabel();
		c.weightx = 1;
		gridbag.setConstraints(label, c);
		panel.add(label);

		return panel;
	}

	private JPanel createGraphHistogramPanel()
	{
		JPanel panel = new JPanel();

		GridBagLayout gridbag = new GridBagLayout();
		panel.setLayout(gridbag);
		GridBagConstraints c = new GridBagConstraints();
		JLabel label;

		int x = 0;
		int y = 0;

		c.anchor = GridBagConstraints.NORTHWEST;

		c.gridx = x++;
		c.gridy = y++;
		label = new JLabel("Analysis:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		gridbag.setConstraints(analysisHist[RB], c);
		panel.add(analysisHist[RB]);

		c.gridy = y++;
		gridbag.setConstraints(analysisHist[DC], c);
		panel.add(analysisHist[DC]);

		c.gridy = y++;
		gridbag.setConstraints(analysisHist[CP], c);
		panel.add(analysisHist[CP]);

		y = 0;

		c.gridy = y++;
		c.gridx = x++;
		c.insets = new Insets(0, 5, 0, 0);
		label = new JLabel("Polarity:");
		gridbag.setConstraints(label, c);
		panel.add(label);

		c.gridy = y++;
		gridbag.setConstraints(polarityAvgHist, c);
		panel.add(polarityAvgHist);

		c.gridy = y++;
		gridbag.setConstraints(polarityNorHist, c);
		panel.add(polarityNorHist);

		c.gridy = y++;
		gridbag.setConstraints(polarityInvHist, c);
		panel.add(polarityInvHist);

		y = 0;

		c.gridx = x++;
		c.gridy = y++;
		c.gridheight = 2;
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(plotHistogram, c);
		panel.add(plotHistogram);

		JPanel bins = new JPanel();
		bins.add(new JLabel("Plot with "));
		bins.add(outputBins);
		bins.add(new JLabel(" bins."));
		y++;
		c.gridy = y;
		gridbag.setConstraints(bins, c);
		panel.add(bins);

		c.gridx = x;
		label = new JLabel();
		c.weightx = 1;
		gridbag.setConstraints(label, c);
		panel.add(label);

		return panel;
	}

	private JPanel createSaveResultsPanel()
	{
		JPanel panel = new JPanel();

		GridBagLayout gridbag = new GridBagLayout();
		panel.setLayout(gridbag);
		GridBagConstraints c = new GridBagConstraints();
		JLabel label;

		int x = 0;
		int y = 0;

		c.anchor = GridBagConstraints.WEST;

		c.gridx = x++;
		c.gridy = y++;
		gridbag.setConstraints(outputDelTab, c);
		panel.add(outputDelTab);

		c.gridy = y++;
		gridbag.setConstraints(outputDelSpace, c);
		panel.add(outputDelSpace);

		c.gridy = y++;
		gridbag.setConstraints(outputDelComma, c);
		panel.add(outputDelComma);

		c.gridx = x++;
		c.gridheight = y;
		c.gridy = 0;
		gridbag.setConstraints(saveResultsOutput, c);
		panel.add(saveResultsOutput);

		c.gridx = x;
		label = new JLabel();
		c.weightx = 1;
		gridbag.setConstraints(label, c);
		panel.add(label);

		return panel;
	}

	private void clearOutput()
	{
		dataVect = null;
		xys = null;
		graphDisp(false, false, false);
		outputTableModel.setRowCount(0);
	}

	private void graphDisp(boolean rigid, boolean decoupled, boolean coupled)
	{
		boolean any = rigid || decoupled || coupled;

		analysisDisp[RB].setEnabled(rigid);
		analysisHist[RB].setEnabled(rigid);
		analysisDisp[DC].setEnabled(decoupled);
		analysisHist[DC].setEnabled(decoupled);
		analysisDisp[CP].setEnabled(coupled);
		analysisHist[CP].setEnabled(coupled);

		plotHistogram.setEnabled(any);
		plotDisplacement.setEnabled(any);
	}

	private void changeDecimal()
	{
		int i;

		Double d = (Double)Utils.checkNum(decimalsTF.getText(), "display decimals field", null, false, null, new Double(0), true, null, false);
		if(d == null)
			i = 1;
		else
			i = (int)d.doubleValue();

		String s = "0";

		if(i > 0)
			s = s + ".";

		while(i-- > 0)
			s = s + "0";

		unitFmt = new DecimalFormat(s);
	}

	private double avg(final double a, final double b)
	{
		return (a + b) / 2.0;
	}
}
