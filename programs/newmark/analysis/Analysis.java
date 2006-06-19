/*
 * Copyright (c) 2002 Matthew Jibson
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

package newmark.analysis;

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
