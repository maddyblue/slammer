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

public class RigidBlock extends Analysis
{
	public static String NewmarkRigorousDual(DoubleList data, final double d, final double ca, final double ta, final double mult)
	{
		Double val;
		double a, n, q=0, r=0, s=0, y=0, v=0, u=0;

		final double l = Math.toRadians(ta);
		final double g = Math.sin(l) * Gcmss;
		final double t = (ca * Gcmss) + g;

		setValueSize(d);
		data.reset();
		while((val = data.each()) != null)
		{
			a = (val.doubleValue() * mult) + g;

			if(a == 0)
			{
				store(u);
				continue;
			}

			if(Math.abs(v) < .0001)
			{
				if(Math.abs(a) > t)
				{
					n = sign(a);
				}
				else
				{
					n = a / t;
				}
			}
			else
			{
				n = sign(v);
			}
			y = a - n * t;
			v = r + d / 2.0 * (y + s);
			if (!(r == 0.0 || (v / r) > 0))
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

	public static String NewmarkRigorous(DoubleList data, final double di, final double ca, final double mult)
	{
		Double val;
		double t, d, q = 0, r = 0, s = 0, y = 0, v = 0, u = 0, a, n;
		t = ca;
		t *= Gcmss;
		d = di;
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
					n = sign(a);
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
		t *= Gcmss;
		int pos = 0;
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
					n = sign(a);
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
				t = Gcmss * disp[pos][1];
				continue;
			}
			prop = (u - disp[pos][0]) / (disp[pos + 1][0] - disp[pos][0]);
			t = Gcmss * (disp[pos][1] - (disp[pos][1] - disp[pos + 1][1]) * prop);
		}

		DecimalFormat fmt = new DecimalFormat(fmtOne);
		end(u);
		return fmt.format(u);
	}
}
