/* This file is in the public domain. */

package slammer.analysis;

import java.text.DecimalFormat;

public class RigidBlockSimplified extends Analysis
{
	public static String Jibson1993(final double arias, final double ca)
	{
		return fmtOne.format(Math.pow(10, 1.46 * Math.log10(arias) - 6.642 * ca + 1.546));
	}

	public static String JibsonAndOthers1998(final double arias, final double ca)
	{
		return fmtOne.format(Math.pow(10, 1.521 * Math.log10(arias) - 1.993 * Math.log10(ca) - 1.546));
	}

	public static String Jibson2007CA(final double ca, final double maxa)
	{
		final double ratio = ca / maxa;
		return fmtOne.format(Math.pow(10, 0.215 + Math.log10(Math.pow(1.0 - ratio, 2.341) * Math.pow(ratio, -1.438))));
	}

	public static String Jibson2007CAM(final double ca, final double maxa, final double M)
	{
		final double ratio = ca / maxa;
		return fmtOne.format(Math.pow(10, -2.71 + Math.log10(Math.pow(1.0 - ratio, 2.335) * Math.pow(ratio, -1.478)) + 0.424 * M));
	}

	public static String Jibson2007AICA(final double arias, final double ca)
	{
		return fmtOne.format(Math.pow(10, 2.401 * Math.log10(arias) - 3.481 * Math.log10(ca) - 3.23));
	}

	public static String Jibson2007AICAR(final double arias, final double ca, final double maxa)
	{
		final double ratio = ca / maxa;
		return fmtOne.format(Math.pow(10, 0.561 * Math.log10(arias) - 3.833 * Math.log10(ratio) - 1.474));
	}

	public static String AmbraseysAndMenu(final double pga, final double ca)
	{
		final double ratio = ca / pga;
		return fmtOne.format(Math.pow(10, 0.90 + Math.log10(Math.pow(1.0 - ratio, 2.53) * Math.pow(ratio, -1.09))));
	}

	public static String ProbFailure(final double disp)
	{
		return fmtThree.format(0.335 * (1 - Math.exp(-0.048 * Math.pow(disp, 1.565))));
	}

	public static String SaygiliRathje2008CARPA(final double ac, final double amax)
	{
		final double ratio = ac / amax;
		final double ratio2 = ratio * ratio;
		final double ratio3 = ratio2 * ratio;
		final double ratio4 = ratio3 * ratio;
		return fmtOne.format(Math.pow(Math.E,
			5.52 * 4.43 * ratio - 20.39 * ratio2 + 42.61 * ratio3 - 28.74 * ratio4 + 0.72 * Math.log(amax)
		));
	}

	public static String SaygiliRathje2008CARPAPV(final double ac, final double amax, final double vmax)
	{
		final double ratio = ac / amax;
		final double ratio2 = ratio * ratio;
		final double ratio3 = ratio2 * ratio;
		final double ratio4 = ratio3 * ratio;
		return fmtOne.format(Math.pow(Math.E,
			-1.56 - 4.58 * ratio - 20.48 * ratio2 + 44.75 * ratio3 - 30.50 * ratio4 - 0.64 * Math.log(amax) + 1.55 * Math.log(vmax)
		));
	}

	public static String SaygiliRathje2008CARPAPVAI(final double ac, final double amax, final double vmax, final double ia)
	{
		final double ratio = ac / amax;
		final double ratio2 = ratio * ratio;
		final double ratio3 = ratio2 * ratio;
		final double ratio4 = ratio3 * ratio;
		return fmtOne.format(Math.pow(Math.E,
			-0.74 - 4.93 * ratio - 19.91 * ratio2 + 43.75 * ratio3 - 30.12 * ratio4 - 1.30 * Math.log(amax) + 1.04 * Math.log(vmax) + 0.67 * Math.log(ia)
		));
	}
}
