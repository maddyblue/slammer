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

/* $Id: Analysis.java,v 1.1 2003/06/15 01:58:11 dolmant Exp $ */

package newmark;

import java.text.DecimalFormat;
import java.io.*;
import com.jrefinery.data.*;
import newmark.gui.*;

public class Analysis
{
	public static final String fmtFour              = "0.0000";
	public static final String fmtThree             = "0.000";
	public static final String fmtTwo               = "0.00";
	public static final String fmtOne               = "0.0";
	public static final String fmtZero              = "0";

	public static final int each = 1;
	private static double time;
	private static int eachAt;
	private static double dint;
	public static XYSeries xys;
	private static double last;
	private static boolean skipped;

	private static int perSec = 5;
	private static double interval = 1.0 / (double)perSec;
	private static double timeStor;

	private static void setValueSize(final double Dint)
	{
		time = 0;
		eachAt = each;
		dint = Dint;
		xys = new XYSeries("");
		last = -1;
		skipped = false;
		timeStor = 0;
	}

	private static void store_OLD(final double d)
	{
		if(eachAt != each)
		{
			time += dint;
			eachAt += 1;
			return;
		}

		if(d == last)
			skipped = true;
		else
		{
			if(skipped)
			{
				realStore(last, time - dint);
				skipped = false;
			}
			realStore(d,time);
		}
		time += dint;
		if(eachAt == each) eachAt = 0;
		else eachAt += 1;
	}

	private static void store(final double d)
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

	private static void end(final double d)
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

	/* the algorithms/programs
	 *
	 * Note: the arias, dobry, redigitize, and rigorous newmark algorithms were
	 * originally written by Ray Wilson in GW Basic, and then ported to C++ and
	 * Java.
	 */

	public static String Arias(DoubleList data, final double di)
	{
		DecimalFormat fmt = new DecimalFormat(fmtThree);
		return fmt.format(AriasDobry(data, di, false));
	}

	/* Arias, with optional dobry.  If the boolean dobry is true, this function
	 * will return the dobry duration as opposed to the arias intensity.
	 */
	private static double AriasDobry(DoubleList data, final double di, boolean dobry)
	{
		Double val;
		double d, u = 0, a, x, j, z, b, t, k = 0, res;
		d = di;
		j = .1603 * d * u;
		data.reset();
		while((val = data.each()) != null)
		{
			a = val.doubleValue();
			a /= 100;
			x = a * a;
			u += x;
		}
		j = .1603 * d * u;
		if(dobry)
		{	z = .95 * j / (.1603 * d);
			b = .05 * j / (.1603 * d);
			u = 0;
			data.reset();
			while((val = data.each()) != null)
			{
				a = val.doubleValue();
				a /= 100;
				x = a * a;
				u += x;
				if(u > z || u < b) k--;
				k++;
			}
			t = k * d;
			return t;
		}
		else
		{
			return j;
		}
	}

	public static String CM_GS(DoubleList data, FileWriter ofile) throws IOException
	{
		final double val = 1.0 / 980.665;
		return Mult(data, ofile, val);
	}

	public static String Count(DoubleList data)
	{
		DecimalFormat fmt = new DecimalFormat(fmtZero);
		return fmt.format(data.size());
	}

	public static String Dobry(DoubleList data, final double di)
	{
		DecimalFormat fmt = new DecimalFormat(fmtOne);
		return fmt.format(AriasDobry(data, di, true));
	}

	public static String PGA(DoubleList data)
	{
		DecimalFormat fmt = new DecimalFormat(fmtThree);
		return fmt.format(FindMax(data) / 980.665); // store in g's, but expect to be in cm/s/s
	}

	private static double FindMax(DoubleList data)
	{
		Double val;
		double here, max = 0;
		data.reset();
		while((val = data.each()) != null)
		{
			here = Math.abs(val.doubleValue());
			if(here < 0) here = -here;
			if(here > max) max = here;
		}
		return max;
	}

	public static String GS_CM(DoubleList data, FileWriter ofile) throws IOException
	{
		final double val = 980.665;
		return Mult(data, ofile, val);
	}

	public static String Mult(DoubleList data, FileWriter ofile, final double value) throws IOException
	{
		Double val;
		double temp;
		data.reset();
		while((val = data.each()) != null)
		{
			temp = val.doubleValue();
			temp *= value;
			ofile.write(Double.toString(temp));
			ofile.write('\n');
		}
		ofile.close();
		return null;
	}

	public static String NewmarkRigorous(DoubleList data, final double di, final double ca, final double mult)
	{
		Double val;
		double t, d, q = 0, r = 0, s = 0, y = 0, v = 0, u = 0, a, n;
		t = ca;
		t *= 980.665;
		d = di;
		int step = 0;
		setValueSize(di);
		data.reset();
		int count = 0;
		while((val = data.each()) != null)
		{
			a = val.doubleValue() * mult;
			if(a == 0)
			{
				store(u);
				continue;
			}
			if(v < .0001)
			{
				if(Math.abs(a) > t )
				{
					n = a / Math.abs(a);
				}
				else
				{
					n = a / t;
				}
			}
			else
			{
				n = 1;
			}
			y = a - n * t;
			v = r + d / 2.0 * (y + s);
			if (v <= 0.0)
			{
				v = 0;
				y = 0;
			}
			u = q + d / 2.0 * (v + r);
			q = u;
			r = v;
			s = y;
			if(mult > 0.0) store(u);
		}

		DecimalFormat fmt = new DecimalFormat(fmtOne);
		end(u);
		return fmt.format(u);
	}

	public static String NewmarkRigorousDisp(DoubleList data, final double di, final double[][] disp, final double mult)
	{
		Double val;
		double t, d, q = 0, r = 0, s = 0, y = 0, v = 0, u = 0, a, n;
		t = disp[0][1];
		t *= 980.665;
		int pos = 0, step = 0;
		double prop;
		d = di;
		setValueSize(di);
		data.reset();
		while((val = data.each()) != null)
		{
			a = val.doubleValue() * mult;
			if(a == 0)
			{
				store(u);
				continue;
			}
			if(v < .0001)
			{
				if(Math.abs(a) > t )
					n = a / Math.abs(a);
				else
					n = a / t;
			}
			else
				n = 1;
			y = a - n * t;
			v = r + d / 2 * (y + s);
			if (v <= 0)
			{
				v = 0;
				y = 0;
			}
			u = q + d / 2 * (v + r);
			q = u;
			r = v;
			s = y;
			if(mult > 0) store(u);
			if(pos == disp.length - 1)
				continue;
			while(u > disp[pos + 1][0])
			{
				pos++;
				if(pos == disp.length - 1) break;
			}
			if(pos == disp.length - 1)
			{
				t = 980.665 * disp[pos][1];
				continue;
			}
			prop = (u - disp[pos][0]) / (disp[pos + 1][0] - disp[pos][0]);
			t = 980.665 * (disp[pos][1] - (disp[pos][1] - disp[pos + 1][1]) * prop);
		}

		DecimalFormat fmt = new DecimalFormat(fmtOne);
		end(u);
		return fmt.format(u);
	}

	public static String NewmarkRigorousTime(DoubleList data, final double di, final double[][] disp, final double mult)
	{
		Double val;
		double t, d, q = 0, r = 0, s = 0, y = 0, v = 0, u = 0, a, n;
		double time = 0;
		t = disp[0][1];
		t *= 980.665;
		int pos = 0, step = 0;
		double prop;
		setValueSize(di);
		d = di;
		data.reset();
		while((val = data.each()) != null)
		{
			a = val.doubleValue() * mult;
			if(a == 0)
			{
				store(u);
				continue;
			}
			if(v < .0001)
			{
				if(Math.abs(a) > t )
					n = a / Math.abs(a);
				else
					n = a / t;
			}
			else
				n = 1;
			y = a - n * t;
			v = r + d / 2 * (y + s);
			if (v <= 0)
			{
				v = 0;
				y = 0;
			}
			u = q + d / 2 * (v + r);
			q = u;
			r = v;
			s = y;
			if(mult > 0) store(u);
			if(pos == disp.length - 1)
				continue;
			while(time > disp[pos + 1][0])
			{
				pos++;
				if(pos == disp.length - 1) break;
			}
			if(pos == disp.length - 1)
			{
				t = 980.665 * disp[pos][1];
				continue;
			}
			prop = (time - disp[pos][0]) / (disp[pos + 1][0] - disp[pos][0]);
			t = 980.665 * (disp[pos][1] - (disp[pos][1] - disp[pos + 1][1]) * prop);
			time += d;
		}

		DecimalFormat fmt = new DecimalFormat(fmtOne);
		end(u);
		return fmt.format(u);
	}

	public static String JibsonAndOthers(final double arias, final double ca)
	{
		DecimalFormat fmt = new DecimalFormat(fmtOne);
		return fmt.format(Math.pow(10, 1.521 * log10(arias) - 1.993 * log10(ca) -1.546));
	}

	public static String AmbraseysAndMenu(final double pga, final double ca)
	{
		final double ratio = ca / pga;
		DecimalFormat fmt = new DecimalFormat(fmtOne);
		return fmt.format(Math.pow(10, 0.90 + log10(Math.pow(1.0 - ratio, 2.53) * Math.pow(ratio, -1.09))));
	}

	public static String ProbFailure(final double disp)
	{
		DecimalFormat fmt = new DecimalFormat(fmtThree);
		return fmt.format(0.335 * (1 - Math.exp(-0.048 * Math.pow(disp, 1.565))));
	}

	public static String[] BrayAndRathje(final double ky, final double h, final double vs, final double m, final double rock, final double r, final double mheaS, final double meanperS, final double sigdurS, final double normdispS, final double allowdisp, final boolean doScreening)
	{
		String ret[] = new String[13];

		double siteper, nrffact, meanper, dur, tstm, mheamhanrf, kmax, kykmax, normdisp, dispcm, dispin;
		double dur1, dur2, dur3, dur4, dur5;
		double arg;

		siteper = 4.0 * h / vs;
		nrffact = 0.62247 + 0.91958 * Math.exp(-rock / 0.44491);

		if(m <= 7.25)
		{
			meanper = (0.411 + 0.0837 * (m - 6.0) + 0.00208 * r) * Math.exp(meanperS * 0.437);
		}
		else
		{
			meanper = (0.411 + 1.25 * 0.0837 + 0.00208 * r) * Math.exp(meanperS * 0.437);
		}

		dur1 = Math.exp(-0.532 + 0.552 * Math.log((0.95 - 0.05) / (1.0 - 0.95)) - 0.0262 * Math.pow(Math.log((0.95 - 0.05) / (1 - 0.95)), 2));
		dur2 = Math.exp(5.204 + 0.851 * (m - 6.0));
		dur3 = Math.pow(10, (1.5 * m + 16.05));
		dur4 = Math.pow(dur2 / dur3, -1.0 / 3.0) / (4900000 * 3.2) + 0.063 * (r - 10.0);
		dur5 = Math.pow(dur2 / dur3, -1.0 / 3.0) / (4900000 * 3.2);

		if(r >= 10.0)
		{
			dur = Math.exp(Math.log(Math.exp(Math.log(dur4) + Math.log(dur1) + (0.493 * sigdurS))));
		}
		else
		{
			dur = Math.exp(Math.log(Math.exp(Math.log(dur5) + Math.log(dur1) + (0.493 * sigdurS))));
		}

		arg = (siteper / meanper);
		tstm = arg > 8.0 ? 8.0 : arg;

		arg = Math.exp(-0.6244 - 0.7831 * Math.log(tstm) + 0.298 * mheaS);
		mheamhanrf = arg > 1.0 ? 1.0 : arg;

		kmax = mheamhanrf * rock * nrffact;

		kykmax = ky / kmax;

		normdisp = Math.pow(10, 1.87 - 3.477 * kykmax + (normdispS * 0.35));

		dispcm = normdisp * dur * kmax;
		dispin = dispcm / 2.54;

		int incr = 0;
		DecimalFormat fmt = new DecimalFormat(fmtThree);
		DecimalFormat fmt1 = new DecimalFormat(fmtOne);
		ret[incr++] = fmt.format(siteper);
		ret[incr++] = fmt.format(nrffact);
		ret[incr++] = fmt.format(meanper);
		ret[incr++] = fmt.format(dur);
		ret[incr++] = fmt.format(tstm);
		ret[incr++] = fmt.format(mheamhanrf);
		ret[incr++] = fmt.format(kmax);
		ret[incr++] = fmt.format(kykmax);
		ret[incr++] = fmt.format(normdisp);
		ret[incr++] = fmt1.format(dispcm);
		ret[incr++] = fmt1.format(dispin);

		if(doScreening)
		{
			double medianfreq = nrffact / 3.477 * (1.87 - log10(allowdisp / (rock * nrffact * dur)));
			ret[incr++] = fmt.format(medianfreq); // medianfreq
			ret[incr++] = fmt.format(medianfreq * rock); // seiscoef
		}
		else
		{
			ret[incr++] = "";
			ret[incr++] = "";
		}

		return ret;
	}

	public static String Redigitize(DoubleList data, FileWriter ofile, final double di) throws IOException
	{
		Double val;
		double d, r, u, t1 = 0, t2, a1 = 0, a2, t0, a0;
		d = di;
		data.reset();
		if((val = data.each()) == null)
		{
			ofile.close();
			return "No data";
		}
		t2 = val.doubleValue();
		if((val = data.each()) == null)
		{
			ofile.close();
			return "Odd number of values";
		}
		a2 = val.doubleValue();
		boolean flag = false;
		for(int i = 0; !flag; i++)
		{
			t0 = (double)i * d;
			while(t0 > t2)
			{
				t1 = t2;
				a1 = a2;
				if((val = data.each()) == null)
				{
					flag = true;
					break;
				}
				t2 = val.doubleValue();
				if((val = data.each()) == null)
				{
					flag = true;
					return "Odd number of values";
				}
				a2 = val.doubleValue();
			}
			if(flag) break;
			r = t2 - t1;
			u = a2 - a1;
			if(r == 0)
			{
				a0 = a1;
			}
			else
			{
				a0 = a1 + (t0 - t1) * u / r;
			}
			ofile.write(Double.toString(a0));
			ofile.write('\n');
		}
		ofile.close();
		return "";
	}

	public static String TotalDuration(DoubleList data, final double di)
	{
		DecimalFormat fmt = new DecimalFormat(fmtOne);
		return fmt.format((double)(data.size()) * di);
	}

	public static String MeanPer(DoubleList data, final double di)
	{
		double[] arr = new double[data.size()];

		Double temp;
		data.reset();
		for(int i = 0; (temp = data.each()) != null; i++)
			arr[i] = temp.doubleValue();

		rdc(arr);

		taper(arr);

		// Pads the array so its length is a power of 2.
		int test = 0;

		for(int i = 1; test < arr.length; i++)
		{
			test = (int)Math.pow(2, i);
		}

		double[][] narr = new double[test][2];

		for(int i = 0; i < arr.length; i++)
		{
			narr[i][0] = arr[i];
			narr[i][1] = 0;
		}

		for(int i = narr.length; i < test; i++)
		{
			narr[i][0] = 0;
			narr[i][1] = 0;
		}

		// forward fft
		fft(narr);

		// scale to keep units correct
		for(int i = 0; i < narr.length; i++)
		{
			narr[i][0] *= di;
			narr[i][1] *= di;
		}

		// set frequency increment
		double df = 1.0 / ((double)(narr.length) * di);

		double top = 0, bot = 0, top2 = 0, bot2 = 0, tms = 0, to = 0;
		double f;

		for(int i = 0; i < arr.length; i++)
		{
			arr[i] = Math.sqrt(Math.pow(narr[i][0], 2) + Math.pow(narr[i][1], 2));
			f = i * df;

			if(f > 0.25 && f < 20.0)
			{
				top += (1.0 / f) * Math.pow(arr[i], 2);
				bot += Math.pow(arr[i], 2);
				top2 += Math.pow(1.0 / f, 2) * Math.pow(arr[i], 2);
				bot2 += Math.pow(arr[i], 2);
			}
		}

		DecimalFormat fmt = new DecimalFormat(fmtTwo);
		return fmt.format(top / bot);
	}

	public static void fft1(double[][] arr)
	{
		double temp[], carg, cw;
		temp = new double[2];
		int lx, m, j, l, istep;

		lx = arr.length;
		j = 1;

		for(int i = 1; i < lx; i++)
		{
			if(i < j)
			{
				temp[0] = arr[j - 1][0];
				temp[1] = arr[j - 1][1];

				arr[j - 1][0] = arr[i - 1][0];
				arr[j - 1][1] = arr[i - 1][1];

				arr[i - 1][0] = temp[0];
				arr[i - 1][1] = temp[1];
			}

			m = lx / 2;

			while(m < j)
			{
				j -= m;
				m /= 2;
			}

			j += m;
		}

		l = 1;

		do
		{
			istep = l + 1;

			for(m = 0; m < l; m++)
			{
				carg = (-Math.PI) * (double)(m - 1) / (double)l;
				cw = Math.exp(carg);
				for(int i = m; i < lx; lx += istep)
				{
					temp[0] = arr[i + 1][0];
					temp[1] = cw * arr[i + 1][1];

					arr[i + 1][0] = arr[i][0] - temp[0];
					arr[i + 1][1] = arr[i][1] - temp[1];

					arr[i][0] += temp[0];
					arr[i][1] += temp[1];
				}
			}

			l = istep;
		}	while(l < lx);
	}

	public static void fft(double[][] array)
	{
		double u_r,u_i, w_r,w_i, t_r,t_i;
		int ln, nv2, k, l, le, le1, j, ip, i, n;

		n = array.length;
		ln = (int)(Math.log((double)n) / Math.log(2) + 0.5);
		nv2 = n / 2;
		j = 1;

		for (i = 1; i < n; i++ )
		{
			if (i < j)
			{
				t_r = array[i - 1][0];
				t_i = array[i - 1][1];
				array[i - 1][0] = array[j - 1][0];
				array[i - 1][1] = array[j - 1][1];
				array[j - 1][0] = t_r;
				array[j - 1][1] = t_i;
			}

			k = nv2;

			while (k < j)
			{
				j = j - k;
				k = k / 2;
			}

			j = j + k;
		}

		for (l = 1; l <= ln; l++) /* loops thru stages */
		{
			le = (int)(Math.exp((double)l * Math.log(2)) + 0.5);
			le1 = le / 2;
			u_r = 1.0;
			u_i = 0.0;
			w_r =  Math.cos(Math.PI / (double)le1);
			w_i = -Math.sin(Math.PI / (double)le1);

			for (j = 1; j <= le1; j++) /* loops thru 1/2 twiddle values per stage */
			{
				for (i = j; i <= n; i += le) /* loops thru points per 1/2 twiddle */
				{
					ip = i + le1;
					t_r = array[ip - 1][0] * u_r - u_i * array[ip - 1][1];
					t_i = array[ip - 1][1] * u_r + u_i * array[ip - 1][0];

					array[ip - 1][0] = array[i - 1][0] - t_r;
					array[ip - 1][1] = array[i - 1][1] - t_i;

					array[i - 1][0] =  array[i - 1][0] + t_r;
					array[i - 1][1] =  array[i - 1][1] + t_i;
				}
				t_r = u_r * w_r - w_i * u_i;
				u_i = w_r * u_i + w_i * u_r;
				u_r = t_r;
			}
		}
	}


	// Removes a dc shift from the data by removing the mean.
	public static void rdc(double[] arr)
	{
		double sum = 0, mean;

		for(int i = 0; i < arr.length; i++)
			sum += arr[i];

		mean = sum / (double)(arr.length);

		for(int i = 0; i < arr.length; i++)
			arr[i] -= mean;
	}

	// Tapers the first and last 5% of the data.
	public static void taper(double[] arr)
	{
		double n, arg;
		double taper = 5;

		n = (double)(arr.length) * taper / 100.0;

		// beginning 5%
		for(int i = 0; i < n; i++)
		{
			arg = Math.PI * (double)(i) / (double)(n) + Math.PI;
			arr[i] *= (1.0 + Math.cos(arg)) / 2;
		}

		//ending 5%
		for(int i = 0; i < n; i++)
		{
			arg = Math.PI * (double)(i) / (double)(n) + Math.PI;
			arr[arr.length - i - 1] *= (1.0 + Math.cos(arg)) / 2;
		}
	}
}
