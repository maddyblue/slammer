/* This file is in the public domain. */

package slammer.analysis;

import java.text.DecimalFormat;
import org.jfree.data.xy.XYSeries;
import slammer.*;

public class Analysis
{
	public static final DecimalFormat fmtFive  = new DecimalFormat("0.00000");
	public static final DecimalFormat fmtFour  = new DecimalFormat("0.0000");
	public static final DecimalFormat fmtThree = new DecimalFormat("0.000");
	public static final DecimalFormat fmtTwo   = new DecimalFormat("0.00");
	public static final DecimalFormat fmtOne   = new DecimalFormat("0.0");
	public static final DecimalFormat fmtZero  = new DecimalFormat("0");

	public static final double CMtoFT = 0.032808399;
	public static final double FTtoIN = 12.0;
	public static final double CMtoIN = CMtoFT * FTtoIN; // 0.393700787
	public static final double Gcmss  = 980.665;
	public static final double Gftss  = Gcmss * CMtoFT; // 32.1740486
	public static final double Ginss  = Gcmss * CMtoIN; // 386.088583
	public static final double PCFtoKNM3 = 6.3659; // lb/ft^3 to kN/m^3
	public static final double FTtoM = 0.3048;
	public static final double FT3toIN3 = FTtoIN * FTtoIN * FTtoIN;
	public static final double MtoCM = 100;
	public static final double M3toCM3 = MtoCM * MtoCM * MtoCM;

	private double time;
	private double dint;
	private double last;
	private boolean skipped;

	private static final int perSec = 5;
	private static final double interval = 1.0 / (double)perSec;
	private double timeStor;

	public static boolean testing = false;

	public XYSeries graphData;

	protected void setValueSize(final double Dint)
	{
		if(testing) return;

		time = 0;
		dint = Dint;
		graphData = new XYSeries("");
		last = -1;
		skipped = false;
		timeStor = 0;
	}

	protected void store(final double d)
	{
		if(testing) return;

		if(d == last)
		{
			skipped = true;
		}
		else
		{
			if(skipped)
			{
				realStore(last, time - dint);
				skipped = false;
			}

			if(time >= (timeStor + interval))
			{
				realStore(d, time);
				timeStor = time;
			}
		}

		time += dint;
	}

	protected void end(final double d)
	{
		if(testing) return;

		if(skipped)
			realStore(last, time - dint);
		realStore(d, time);
	}

	private void realStore(final double d, final double time)
	{
		try {graphData.add(new Float(time), new Float(d));}
		catch (Exception e) {}
		last = d;
	}

	/* overridden functions */
	/* i'm sure there's a better way to do this, but this works */

	public double Decoupled(double[] ain_p, double uwgt_p, double height_p, double vs_p, double damp1_p, double dt_p, double scal_p, double g_p, double vr_p, double[][] ca, boolean dv3_p)
	{
		return 0;
	}
	public double Coupled(double[] ain_p, double uwgt_p, double height_p, double vs_p, double damp1_p, double dt_p, double scal_p, double g_p, double vr_p, double[][] ca, boolean dv3_p)
	{
		return 0;
	}
	public double SlammerRigorous(double[] data, final double d, final double[][] disp, final double mult, final boolean dualSlope, final double ta, final double unitMult)
	{
		return 0;
	}

	/* standard functions */

	public static double sign(final double val)
	{
		if(val >= 0)
			return 1;
		else
			return -1;
	}
}
