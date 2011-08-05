/* This file is in the public domain. */

package slammer.analysis;

import java.text.DecimalFormat;

public class CoupledSimplified extends Analysis
{
	public static String[] BrayAndTravasarou2007(final double ky, final double ts, final double sa, final double m)
	{
		String ret[] = new String[2];

		final double lnky = Math.log(ky);
		final double lnky2 = lnky * lnky;
		final double ts15 = ts * 1.5;
		final double lnsats15 = Math.log(sa * ts15);
		final double lnsats15_2 = lnsats15 * lnsats15;

		double dispcm = Math.pow(Math.E,
			-1.1 - 2.83 * lnky - 0.333 * lnky2 + 0.566 * lnky * lnsats15 + 3.04 * lnsats15 - 0.244 * lnsats15_2 + ts15 + 0.278 * (m - 7.0)
		);

		double dispin = dispcm / 2.54;

		int incr = 0;
		ret[incr++] = fmtOne.format(dispcm);
		ret[incr++] = fmtOne.format(dispin);

		return ret;
	}
}
