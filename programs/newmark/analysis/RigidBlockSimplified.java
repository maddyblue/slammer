/*
 * Copyright (c) 2004 Matt Jibson
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

public class RigidBlockSimplified extends Analysis
{
	public static String Jibson1993(final double arias, final double ca)
	{
		return fmtOne.format(Math.pow(10, 1.46 * log10(arias) - 6.642 * ca + 1.546));
	}

	public static String JibsonAndOthers1998(final double arias, final double ca)
	{
		return fmtOne.format(Math.pow(10, 1.521 * log10(arias) - 1.993 * log10(ca) - 1.546));
	}

	public static String Jibson2007CA(final double ca, final double maxa)
	{
		final double ratio = ca / maxa;
		return fmtOne.format(Math.pow(10, 0.215 + log10(Math.pow(1.0 - ratio, 2.341) * Math.pow(ratio, -1.438))));
	}

	public static String Jibson2007CAM(final double ca, final double maxa, final double M)
	{
		final double ratio = ca / maxa;
		return fmtOne.format(Math.pow(10, -2.71 + log10(Math.pow(1.0 - ratio, 2.335) * Math.pow(ratio, -1.478)) + 0.424 * M));
	}

	public static String Jibson2007AICA(final double arias, final double ca)
	{
		return fmtOne.format(Math.pow(10, 2.401 * log10(arias) - 3.481 * log10(ca) - 3.23));
	}

	public static String Jibson2007AICAR(final double arias, final double ca, final double maxa)
	{
		final double ratio = ca / maxa;
		return fmtOne.format(Math.pow(10, 0.561 * log10(arias) - 3.833 * log10(ratio) - 1.474));
	}

	public static String AmbraseysAndMenu(final double pga, final double ca)
	{
		final double ratio = ca / pga;
		return fmtOne.format(Math.pow(10, 0.90 + log10(Math.pow(1.0 - ratio, 2.53) * Math.pow(ratio, -1.09))));
	}

	public static String ProbFailure(final double disp)
	{
		return fmtThree.format(0.335 * (1 - Math.exp(-0.048 * Math.pow(disp, 1.565))));
	}
}
