/*
 * Analysis.java - the scientific analysis algorithms
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

package newmark.analysis;

import java.text.DecimalFormat;
import org.jfree.data.xy.XYSeries;

public class Analysis
{
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
	public static XYSeries xys;
	private static double last;
	private static boolean skipped;

	private static int perSec = 5;
	private static double interval = 1.0 / (double)perSec;
	private static double timeStor;

	protected static void setValueSize(final double Dint)
	{
		time = 0;
		dint = Dint;
		xys = new XYSeries("");
		last = -1;
		skipped = false;
		timeStor = 0;
	}

	protected static void store(final double d)
	{
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
		if(skipped)
			realStore(last, time - dint);
		realStore(d, time);
	}

	private static void realStore(final double d, final double time)
	{
		try {xys.add(new Float(time), new Float(d));}
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
