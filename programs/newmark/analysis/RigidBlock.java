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
	public static double NewmarkRigorous(DoubleList data, final double d, final double[][] disp, final double mult, final boolean dualSlope, final double ta)
	{
		Double val; // current data value from input file
		double a, n, q = 0, r = 0, s = 0, t, u = 0, v = 0, y = 0;

		// dual slope calculations
		final double l = Math.toRadians(ta);
		final double g = Math.sin(l) * Gcmss;

		t = disp[0][1] * Gcmss;
		if(dualSlope)
			t += g;

		int pos = 0; // position in the displacement/ca table
		double prop;

		setValueSize(d); // init the graphing data

		data.reset();
		while((val = data.each()) != null)
		{
			a = val.doubleValue() * mult;
			if(dualSlope)
				a += g;

			if(a == 0 && mult > 0.0)
			{
				store(u);
				continue;
			}

			if(
				(!dualSlope && v < .0001) ||
				(dualSlope && Math.abs(v) < .0001)
			)
			{
				if(Math.abs(a) > t )
					n = sign(a);
				else
					n = a / t;
			}
			else
				n = dualSlope ? sign(v) : 1;

			y = a - n * t;
			v = r + d / 2.0 * (y + s);

			if (
				(!dualSlope && v <= 0.0) ||
				(dualSlope && (!(r == 0.0 || (v / r) > 0.0)))
			)
			{
				v = 0;
				y = 0;
			}

			u = q + d / 2.0 * (v + r);
			q = u;
			r = v;
			s = y;

			// only store stuff if we aren't in the inverse run. this is just a simple speedup.
			if(mult > 0.0)
				store(u);

			// if we are at the end of the disp/ca table, don't bother doing anything else
			if(pos == disp.length - 1)
				continue;

			// figure out the new pos based on current displacement
			while(u > disp[pos + 1][0])
			{
				pos++;
				if(pos == disp.length - 1) break;
			}

			if(pos == disp.length - 1)
			{
				t = Gcmss * disp[pos][1];
			}
			else
			{
				prop = (u - disp[pos][0]) / (disp[pos + 1][0] - disp[pos][0]);
				t = Gcmss * (disp[pos][1] - (disp[pos][1] - disp[pos + 1][1]) * prop);
			}
		}

		end(u);
		return u;
	}
}
