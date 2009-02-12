/* This file is in the public domain. */

package slammer.analysis;

import java.text.DecimalFormat;
import java.io.*;
import org.jfree.data.xy.XYSeries;
import slammer.gui.*;
import slammer.*;

public class Utilities extends Analysis
{
	public static String CM_GS(DoubleList data, FileWriter ofile) throws IOException
	{
		final double val = 1.0 / Gcmss;
		return Mult(data, ofile, val);
	}

	public static String GS_CM(DoubleList data, FileWriter ofile) throws IOException
	{
		final double val = Gcmss;
		return Mult(data, ofile, val);
	}

	public static String Mult(DoubleList data, FileWriter ofile, final double value) throws IOException
	{
		return Shift(data, ofile, value, 0);
	}

	// first add shift to every value, then multiply by value
	public static String Shift(DoubleList data, FileWriter ofile, final double value, final double shift) throws IOException
	{
		Double val;
		double temp;
		data.reset();
		while((val = data.each()) != null)
		{
			temp = (val.doubleValue() + shift) * value;
			ofile.write(temp + "\n");
		}
		ofile.close();
		return null;
	}

	public static String Bracket(DoubleList data, FileWriter ofile, final double value) throws IOException
	{
		Double val;
		int top = data.size() - 1;
		int indexl = 0, index = 0, indexr = top;

		data.reset();
		while((val = data.each()) != null)
		{
			if(Math.abs(val.doubleValue()) >= value)
			{
				indexl = index - 50;
				if(indexl < 0)
					indexl = 0;
				break;
			}
			index++;
		}

		data.end();
		index = top;
		while((val = data.eachP()) != null)
		{
			if(Math.abs(val.doubleValue()) >= value)
			{
				indexr = index + 50;
				if(indexr > top)
					indexr = top;
				break;
			}
			index--;
		}

		// put data.current at indexl
		data.reset();
		for(int i = 0; i < indexl; i++)
			data.next();

		// start writing to the file
		for(int i = indexl; i <= indexr; i++)
		{
			ofile.write(data.each().toString());
			ofile.write('\n');
		}

		ofile.close();

		return null;
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

	public static String Trim(DoubleList data, FileWriter ofile, final double clipL, final double clipR, final double dt) throws IOException
	{
		Double val;
		data.reset();
		int tStart = (int)((double)clipL / dt);
		int tEnd = (int)((double)clipR / dt);

		for(int i = 0; (val = data.each()) != null; i++)
		{
			if(i < tStart || i > tEnd)
				continue;

			ofile.write(val + "\n");
		}

		ofile.close();

		return "";
	}
}
