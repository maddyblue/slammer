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
