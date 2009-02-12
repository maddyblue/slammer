/* This file is in the public domain. */

package slammer.analysis;

import java.text.DecimalFormat;
import slammer.*;
import java.io.*;

public class RigidBlock extends Analysis
{
	public static double SlammerRigorous(String oname, DoubleList data, final double d, final double[][] disp, final double mult, final boolean dualSlope, final double ta, final double unitMult)
	{
		Double val; // current data value from input file
		double a, n, q = 0, r = 0, s = 0, t, u = 0, v = 0, y = 0;

		// dual slope calculations
		final double l = Math.toRadians(ta);
		final double g = Math.sin(l) * Gcmss * unitMult;

		t = disp[0][1] * Gcmss * unitMult;
		if(dualSlope)
			t += g;

		int pos = 0; // position in the displacement/ca table
		double prop;

		try {

		setValueSize(d); // init the graphing data

		data.reset();
		while((val = data.each()) != null)
		{
			a = val.doubleValue() * mult * unitMult;

			if(dualSlope)
				a += g;

			if(a == 0.0 && mult > 0.0)
			{
				store(u);
				continue;
			}

			if(Math.abs(v) < 1e-15)
			{
				if(Math.abs(a) > t)
					n = sign(a);
				else
					n = a / t;
			}
			else
				n = sign(v);

			y = a - n * t;
			v = r + d / 2.0 * (y + s);

			if(
				(!dualSlope && v <= 0.0) ||
				(dualSlope && (!(Math.abs(r) < 1e-15 || (v / r) > 0.0)))
			)
			{
				v = 0;
				y = 0;
			}

			u = q + d / 2.0 * (v + r);

			q = u;
			r = v;
			s = y;

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
				t = Gcmss * unitMult * disp[pos][1];
				if(dualSlope)
					t += g;
			}
			else
			{
				prop = (u - disp[pos][0]) / (disp[pos + 1][0] - disp[pos][0]);
				t = Gcmss * unitMult * (disp[pos][1] - (disp[pos][1] - disp[pos + 1][1]) * prop);
				if(dualSlope)
					t += g;
			}
		}

		} catch(Exception e) {}

		end(u);
		return u;
	}
}
