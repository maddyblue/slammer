/* This file is in the public domain. */

package slammer.analysis;

import java.text.DecimalFormat;

public class CoupledSimplified extends Analysis
{
	public static String[] BrayAndTravasarou2007(final double ky, final double ts, final double sa, final double m)
	{
		String ret[] = new String[3];

		final double p = 0.2316419;
		final double b1 = 0.319381530;
		final double b2 = -0.356563782;
		final double b3 = 1.781477937;
		final double b4 = -1.821255978;
		final double b5 = 1.330274429;

		final double lnky = Math.log(ky);
		final double lnky2 = lnky * lnky;
		final double ts15 = ts * 1.5;
		final double lnsats15 = Math.log(sa * ts15);
		final double lnsats15_2 = lnsats15 * lnsats15;

		double dispcm = Math.pow(Math.E,
			-1.1 - 2.83 * lnky - 0.333 * lnky2 + 0.566 * lnky * lnsats15 + 3.04 * lnsats15 - 0.244 * lnsats15_2 + ts15 + 0.278 * (m - 7.0)
		);

		double dispin = dispcm / 2.54;

		/* NORMSDIST from http://support.microsoft.com/?kbid=214111 */
		double x = -1.76 - 3.22 * lnky - 0.484 * ts * lnky + 3.52 * lnsats15;
		double zx = (1. / Math.sqrt(2 * Math.PI)) * Math.exp(-(x * x) / 2.);
		double t = 1. / (1. + p * x);
		double t2 = t * t;
		double t3 = t2 * t;
		double t4 = t3 * t;
		double t5 = t4 * t;
		double px = 1 - zx * (b1 * t + b2 * t2 + b3 * t3 + b4 * t4 + b5 * t5);
		double prob_zero_disp = 1.0 - px;

		int incr = 0;
		ret[incr++] = fmtOne.format(dispcm);
		ret[incr++] = fmtOne.format(dispin);
		ret[incr++] = fmtTwo.format(prob_zero_disp);

		return ret;
	}
}
