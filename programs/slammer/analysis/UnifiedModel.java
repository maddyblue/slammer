/* This file is in the public domain. */

package slammer.analysis;

import java.text.DecimalFormat;

public class UnifiedModel extends Analysis
{
	public final static int METHOD_2008 = 1;
	public final static int METHOD_2009 = 2;

	public static String[] UnifiedModel(final double ac, final double h, final double vs, final double m, final double pga, final double pgv, final double r, int method)
	{
		String ret[] = new String[11];
		int incr = 0;

		double ts, tm, pr;

		ts = 4.0 * h / vs;

		if(m <= 7.25)
			tm = (0.411 + 0.0837 * (m - 6.0) + 0.00208 * r);
		else
			tm = (0.411 + 1.25 * 0.0837 + 0.00208 * r);

		pr = ts / tm;
		pr = pr > 8.0 ? 8.0 : pr;

		ret[incr++] = fmtThree.format(ts);
		ret[incr++] = fmtThree.format(tm);
		ret[incr++] = fmtThree.format(pr);
		ret[incr++] = fmtThree.format(ac / pga);

		// eq 1

		double kmax_pga, ln_pr, ln_pr2;
		if(pr < 0.1)
			kmax_pga = 1;
		else
		{
			ln_pr = Math.log(pr / 0.1);
			ln_pr2 = ln_pr * ln_pr;
			kmax_pga = Math.exp((0.459 - 0.702 * pga) * ln_pr + (-0.228 + 0.076 * pga) * ln_pr2);
		}
		ret[incr++] = fmtThree.format(kmax_pga);

		// eq 2

		double kvelmax_pgv;
		if(pr < 0.2)
			kvelmax_pgv = 1;
		else
		{
			ln_pr = Math.log(pr / 0.2);
			ln_pr2 = ln_pr * ln_pr;
			kvelmax_pgv = Math.exp(0.24 * ln_pr + (-0.091 - 0.171 * pga) * ln_pr2);
		}
		ret[incr++] = fmtThree.format(kvelmax_pgv);

		double kmax = kmax_pga * pga;
		ret[incr++] = fmtThree.format(kmax);

		double disp = 0, dflexible = 0;
		if(method == METHOD_2008)
		{
			double kvelmax = kvelmax_pgv * pgv;
			ret[incr++] = fmtThree.format(kvelmax);

			disp = RigidBlockSimplified.SaygiliRathje2008CARPAPV_d(ac, kmax, kvelmax);
			if(ts <= 0.5)
				dflexible = Math.exp(Math.log(disp) + 1.42 * ts);
			else
				dflexible = Math.exp(Math.log(disp) + 0.71);
		}
		else if(method == METHOD_2009)
		{
			ret[incr++] = "";

			disp = RigidBlockSimplified.SaygiliRathje2009CARPAM_d(ac, kmax, m);
			if(ts <= 1.5)
				dflexible = Math.exp(Math.log(disp) + 3.69 * ts - 1.22 * ts * ts);
			else
				dflexible = Math.exp(Math.log(disp) + 2.78);
		}
		ret[incr++] = fmtThree.format(disp);
		ret[incr++] = fmtThree.format(dflexible);
		ret[incr++] = fmtThree.format(dflexible / 2.54);

		return ret;
	}
}
