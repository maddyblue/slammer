/*
 * Copyright (C) 2004 Matthew Jibson (dolmant@dolmant.net)
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
import newmark.*;

public class ImportRecords extends Analysis
{
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

	public static String Dobry(DoubleList data, final double di)
	{
		DecimalFormat fmt = new DecimalFormat(fmtOne);
		return fmt.format(AriasDobry(data, di, true));
	}

	public static String PGA(DoubleList data)
	{
		DecimalFormat fmt = new DecimalFormat(fmtThree);
		return fmt.format(FindMax(data) / Gcmss); // store in g's, but expect to be in cm/s/s
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
