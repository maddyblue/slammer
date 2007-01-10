/*
 * Copyright (c) 2002 Matt Jibson
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

package slammer;

import java.io.*;
import slammer.gui.*;

public class DoubleList
{
	class DoubleListElement
	{
		protected Double val;
		protected DoubleListElement next, prev;

		public DoubleListElement()
		{
			val = null;
			next = null;
			prev = null;
		}

		public DoubleListElement(Double val)
		{
			this.val = val;
			next = null;
			prev = null;
		}
	}

	/* head is the beginning of the list, pointing to the first element */
	private DoubleListElement head, current, end;
	private int length;
	private int bad = 0;

	public DoubleList(String fname) throws IOException
	{
		this(fname, 0, 1.0);
	}

	/* skip: the number of lines to skip at the beginning of the file */
	public DoubleList(String fname, int skip, final double scale) throws IOException
	{
		head = new DoubleListElement();
		current = head;
		FileReader file = new FileReader(fname);
		String dbl = "";
		length = 0;

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
			dbl = nextDouble(file);
			if(dbl == null)
			{
				bad = length + 1;
				return;
			}
			else if(dbl.equals("")) break;
			current.next = new DoubleListElement(new Double(Double.parseDouble(dbl) * scale));
			current.next.prev = current;
			current = current.next;
			length++;
		}
		head = head.next;
		end = current;
		current = head;
	}

	private String nextDouble(FileReader file) throws IOException
	{
		String string = "";
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
					string += temp;
					break;
				default:
					string += temp;
					stop = true;
					break;
			}
		}
		try
		{
			string = string.trim();
			if(string.equals("")) return "";

			// just to throw a NumberFormatException, if needed
			Double.valueOf(string);

			return string;
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}

	public void reset()
	{
		current = head;
	}

	public void end()
	{
		current = end;
	}

	public Double each()
	{
		if(current == null)
			return null;

		Double ret = current.val;
		current = current.next;
		return ret;
	}

	public void next()
	{
		if(current == null)
			return;

		current = current.next;
	}

	public Double eachP()
	{
		if(current == null)
				return null;

		Double ret = current.val;
		current = current.prev;
		return ret;
	}

	public int size()
	{
		return length;
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

		DoubleListElement c = head;

		for(int i = 0; c != null; i++)
		{
			ret[i] = c.val.doubleValue();
			c = c.next;
		}

		return ret;
	}
}