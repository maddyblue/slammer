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

package slammer.analysis;

import java.text.DecimalFormat;
import org.jfree.data.xy.XYSeries;

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
	public static final double Gcmss	= 980.665;
	public static final double Gftss  = Gcmss * CMtoFT; // 32.1740486
	public static final double Ginss  = Gcmss * CMtoIN; // 386.088583
	public static final double PCFtoKNM3 = 6.3659; // lb/ft^3 to kN/m^3
	public static final double FTtoM = 0.3048;
	public static final double FT3toIN3 = FTtoIN * FTtoIN * FTtoIN;
	public static final double MtoCM = 100;
	public static final double M3toCM3 = MtoCM * MtoCM * MtoCM;

	private static double time;
	private static double dint;
	public static XYSeries graphData;
	private static double last;
	private static boolean skipped;

	private static int perSec = 5;
	private static double interval = 1.0 / (double)perSec;
	private static double timeStor;

	public static boolean testing = false;

	protected static void setValueSize(final double Dint)
	{
		if(testing) return;

		time = 0;
		dint = Dint;
		graphData = new XYSeries("");
		last = -1;
		skipped = false;
		timeStor = 0;
	}

	protected static void store(final double d)
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

	protected static void end(final double d)
	{
		if(testing) return;

		if(skipped)
			realStore(last, time - dint);
		realStore(d, time);
	}

	private static void realStore(final double d, final double time)
	{
		try {graphData.add(new Float(time), new Float(d));}
		catch (Exception e) {}
		last = d;
	}

	/* standard functions */

	public static double log10(final double val)
	{
		return Math.log(val) / Math.log(10.0);
	}

	public static double sign(final double val)
	{
		if(val >= 0)
			return 1;
		else
			return -1;
	}
}
