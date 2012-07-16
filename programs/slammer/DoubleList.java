/* This file is in the public domain. */

package slammer;

import java.io.*;
import java.util.*;
import slammer.analysis.*;
import slammer.gui.*;

public class DoubleList
{

	private ArrayList<Double> data = new ArrayList<Double>(1024);
	private ListIterator<Double> iter;
	private int bad = 0;

	public DoubleList(String fname) throws IOException
	{
		this(fname, 0, 1.0);
	}

	public DoubleList(String fname, int skip, double scale) throws IOException
	{
		this(fname, skip, scale, false);
	}

	/* skip: the number of lines to skip at the beginning of the file */
	public DoubleList(String fname, int skip, final double scale, final boolean nomult) throws IOException
	{
		BufferedReader file = new BufferedReader(new FileReader(fname));
		Double dbl;

		char c;
		while(skip > 0 && file.ready())
		{
			do
			{
				c = (char)file.read();
				if(c == '\n') skip--;
			}	while(c != '\n' && file.ready());
		}

		while(file.ready())
		{
			try
			{
				dbl = nextDouble(file);
			}
			catch (NumberFormatException nfe)
			{
				bad = data.size() + 1;
				return;
			}

			if(dbl == null)
				break;

			if(!nomult)
				dbl *= scale * Analysis.Gcmss;

			data.add(dbl);
		}
	}

	private Double nextDouble(BufferedReader file) throws IOException
	{
		StringBuilder string = new StringBuilder(32);
		boolean stop = false;
		char temp;

		while(!stop && file.ready())
		{
			temp = (char)file.read();
			switch(temp)
			{
				/* whitespace or other delimteres */
				case ' ':
				case '\n':
				case '\r':
				case '\t':
				case ',':
					if(string.length() != 0) stop = true;
					break;
				/* valid number chars */
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case '0':
				case 'e':
				case 'E':
				case '.':
				case '-':
				case '+':
					string.append(temp);
					break;
				default:
					string.append(temp);
					stop = true;
					break;
			}
		}

		if(string.length() == 0)
			return null;

		return Double.valueOf(string.toString());
	}

	public void reset()
	{
		iter = data.listIterator();
	}

	public void end()
	{
		iter = data.listIterator(data.size());
	}

	public Double each()
	{
		if(!iter.hasNext())
			return null;

		return iter.next();
	}

	public void next()
	{
		if(iter.hasNext())
			iter.next();
	}

	public Double eachP()
	{
		if(!iter.hasPrevious())
			return null;

		return iter.previous();
	}

	public int size()
	{
		return data.size();
	}

	public void save(String fname)
	{
		try
		{
			FileWriter w = new FileWriter(fname);

			reset();
			for(Double d = each(); d != null; d = each())
			{
				w.write(d.toString());
				w.write('\n');
			}
			w.close();
		}
		catch(Exception ex)
		{
			Utils.catchException(ex);
		}
	}

	public boolean bad()
	{
		return (bad != 0);
	}

	public int badEntry()
	{
		return bad;
	}

	public double[] getAsArray()
	{
		double[] ret = new double[size()];

		for(int i = 0; i < ret.length; i++)
			ret[i] = data.get(i);

		return ret;
	}
}
