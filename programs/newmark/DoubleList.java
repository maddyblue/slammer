/*
 * DoubleList.java - linked list of doubles
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

/* $Id: DoubleList.java,v 1.1 2003/06/15 01:58:11 dolmant Exp $ */

package newmark;

import java.io.*;
import newmark.gui.*;

public class DoubleList
{
	class DoubleListElement
	{
		protected Double val;
		protected DoubleListElement next;

		public DoubleListElement()
		{
			val = null;
			next = null;
		}

		public DoubleListElement(Double val)
		{
			this.val = val;
			next = null;
		}
	}

	/* head is the beginning of the list, pointing to the first element */
	private DoubleListElement head, current;
	private int length;
	private int bad = 0;

	public DoubleList(String fname) throws IOException
	{
		this(fname, 0);
	}

	/* skip: the number of lines to skip at the beginning of the file */
	public DoubleList(String fname, int skip) throws IOException
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
			current.next = new DoubleListElement(new Double(dbl));
			current = current.next;
			length++;
		}
		head = head.next;
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

	public Double each()
	{
		if(current == null)
			return null;

		Double ret = current.val;
		current = current.next;
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
}