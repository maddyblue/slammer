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

package slammer.analysis;

import java.text.DecimalFormat;
import slammer.*;

public class ImportRecords extends Analysis
{
	/* the algorithms/programs
	 *
	 * Note: the arias, dobry, redigitize, and rigorous slammer algorithms were
	 * originally written by Ray Wilson in GW Basic, and then ported to C++ and
	 * Java.
	 */

	public static String Arias(DoubleList data, final double di)
	{
		return fmtThree.format(AriasDobry(data, di, false));
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
			a /= 100.0;
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
				a /= 100.0;
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

	public static String Dobry(DoubleList data, final double di)
	{
		return fmtOne.format(AriasDobry(data, di, true));
	}

	public static String PGA(DoubleList data)
	{
		return fmtThree.format(FindMax(data) / Gcmss); // store in g's, but expect to be in cm/s/s
	}

	public static String PGV(DoubleList data, final double di)
	{
		Double val;
		double max, cur, curabs;

		max = 0;
		cur = 0;

		data.reset();
		while((val = data.each()) != null)
		{
			cur += val.doubleValue();
			curabs = Math.abs(cur);

			if(curabs > max)
				max = curabs;
		}

		return fmtOne.format(max * di);
	}

	private static double FindMax(DoubleList data)
	{
		Double val;
		double here, max = 0;
		data.reset();
		while((val = data.each()) != null)
		{
			here = Math.abs(val.doubleValue());
			if(here > max) max = here;
		}
		return max;
	}

	public static String MeanPer(DoubleList data, final double di)
	{
		double[] arr = new double[data.size()];

		Double temp;
		data.reset();
		for(int i = 0; (temp = data.each()) != null; i++)
			arr[i] = temp.doubleValue();

		double[][] narr = fftWrap(arr, di);

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

		return fmtTwo.format(top / bot);
	}

	public static double[][] fftWrap(final double[] array, final double di)
	{
		rdc(array);
		taper(array);

		// Pads the array so its length is a power of 2.
		int test = 0;

		for(int i = 1; test < array.length; i++)
		{
			test = (int)Math.pow(2, i);
		}

		double[][] narr = new double[test][2];

		for(int i = 0; i < array.length; i++)
		{
			narr[i][0] = array[i];
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

		return narr;
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
			arr[i] *= (1.0 + Math.cos(arg)) / 2.0;
		}

		//ending 5%
		for(int i = 0; i < n; i++)
		{
			arg = Math.PI * (double)(i) / (double)(n) + Math.PI;
			arr[arr.length - i - 1] *= (1.0 + Math.cos(arg)) / 2.0;
		}
	}

	/* Compute maximum response
	 * Written by I.M. Idriss at U.C. Berkeley 1968, using the
	 * technique presented by Nigam and Jennigs (1969( in
	 * "Calculation of Response Spectra from String-Motion
	 * Earthquake Records": Bulletin of the Seismological Society
	 * of America, vol. 59, pp. 909-922.
	 *
	 * April Converse made minor changes in 1989.
	 * Ported to Java by Matt Jibson in 2005.
	 *
	 * arr = time series data, cm/sec/sec
	 * w = oscillator natural frequency = (2*pi)/(oscillator period)
	 * d = damping, as fraction of critical damping
	 * dt = digitization interval
	 *
	 * Returns:
	 * ZA   = maximum absolute acceleration response, cm/sec/sec.
	 * ZV   =   "     relative velocity       "     , cm/sec.
	 * ZD   =   "     relative displacement   "     , cm.
	 * w2*zd= psuedo absolute acceleration
	 * w*zd = psuedo relative velocity
	 */
	public static double[] cmpmax(double[] arr, double w, double d, double dt)
	{
		double w2 = w * w;
		double w3 = w2 * w;
		double wd = w * Math.sqrt(1.0 - d * d);

		double xd[] = {0, 0};
		double xv[] = {0, 0};

		double f1 = 2.0 * d / (w3 * dt);
		double f2 = 1.0 / w2;
		double f3 = d * w;
		double f4 = 1.0 / wd;
		double f5 = f3 * f4;
		double f6 = 2.0 * f3;
		double e = Math.exp(-f3 * dt);
		double s = Math.sin(wd * dt);
		double c = Math.cos(wd * dt);
		double g1 = e * s;
		double g2 = e * c;
		double h1 = wd * g2 - f3 * g1;
		double h2 = wd * g1 + f3 * g2;
		double dug, z1, z2, z3, z4, a, b, aa, f, g, h, zd = 0, zv = 0, za = 0;

		for(int k = 0; k < (arr.length - 1); k++)
		{
			dug = arr[k + 1] - arr[k];
			z1 = f2 * dug;
			z2 = f2 * arr[k];
			z3 = f1 * dug;
			z4 = z1 / dt;
			b = xd[0] + z2 - z3;
			a = f4 * xv[0] + f5 * b + f4 * z4;
			xd[1] = a * g1 + b * g2 + z3 - z2 - z1;
			xv[1] = a * h1 - b * h2 - z4;
			xd[0] = xd[1];
			xv[0] = xv[1];
			aa = -f6 * xv[0] - w2 * xd[0];
			f = Math.abs(xd[0]);
			g = Math.abs(xv[0]);
			h = Math.abs(aa);
			if(f > zd) zd = f;
			if(g > zv) zv = g;
			if(h > za) za = h;
		}

		return new double[] {za, zv, zd, w2 * zd, w * zd};
	}
}
