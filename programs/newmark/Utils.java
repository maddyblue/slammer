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

package newmark;

import java.sql.*;
import javax.swing.*;
import java.util.Vector;
import newmark.gui.*;

public class Utils
{
	public static EQDatabase db = null;
	private static Vector eqVec = new Vector();
	private static Vector eqMan = new Vector();

	private static boolean locked = false;

	public static void startDB() throws Exception
	{
		if(db == null)
			db = new EQDatabase();
	}

	public static EQDatabase getDB() throws Exception
	{
		if(db == null)
			startDB();

		return db;
	}

	public static void closeDB() throws Exception
	{
		if(db != null)
		{
			db.close();
			db = null;
		}
	}

	public static void catchException(Throwable ex)
	{
		StackTraceElement e[] = ex.getStackTrace();
		String trace = "";

		for(int i = 0; i < e.length; i++)
		{
			trace = trace + e[i].getClassName() + "(" + e[i].getFileName() + ":" + e[i].getLineNumber() + ")\n";
		}

		GUIUtils.popupError("Error: " + ex.getMessage() + "\n" + trace);
	}

	public static String addSlashes(String str)
	{
		String ret = "";
		char c;
		for(int i = 0; i < str.length(); i++)
		{
			c = str.charAt(i);
			switch(c)
			{
				case '\\':
				case '\'':
					ret += '\\';
					break;
			}
			ret += c;
		}
		return ret;
	}

	public static String addQuote(String str)
	{
		String ret = "";
		char c;
		for(int i = 0; i < str.length(); i++)
		{
			c = str.charAt(i);
			switch(c)
			{
				case '\'':
					ret += '\'';
					break;
			}
			ret += c;
		}
		return ret;
	}

	public static Object checkNum(String str, String label, Double lt, boolean lte, String lts, Double gt, boolean gte, String gts, boolean ret)
	{
		Object o = checkNum(str, label, lt, lte, lts, gt, gte, gts);

		if(ret)
		{
			return o;
		}
		else
		{
			if(o.getClass().getName().equals("java.lang.Double"))
			{
				return o;
			}
			else
			{
				GUIUtils.popupError(o.toString());
				return null;
			}
		}
	}

	private static Object checkNum(String str, String label, Double lt, boolean lte, String lts, Double gt, boolean gte, String gts)
	{
		Double d;

		try
		{
			d = Double.valueOf(str);
		}
		catch (NumberFormatException e)
		{
			return ("Error: non-existent number (\"" + str + "\") in " + label + ".");
		}

		double dd = d.doubleValue();
		double comp;
		String message = "Error: " + str + " (" + label + ") must be";
		if(lt != null)
		{
			message += " ";
			message += "less than ";
			if(lte)
				message += "or equal to ";
			message += lt;
			if(lts != null && !lts.equals(""))
			message += " (" + lts + ")";
		}
		if(gt != null)
		{
			message += " ";
			if(lt != null)
				message += "and ";
			message += "greater than ";
			if(gte)
				message += "or equal to ";
			message += gt;
			if(gts != null && !gts.equals(""))
				message += " (" + gts + ")";
		}
		message += ".";


		if(lt != null)
		{
			comp = lt.doubleValue();
			if(lte)
			{
				if(comp < dd)
				{
					return message;
				}
			}
			else
			{
				if(comp <= dd)
				{
					return message;
				}
			}
		}
		if(gt != null)
		{
			comp = gt.doubleValue();
			if(gte)
			{
				if(comp > dd)
				{
					return message;
				}
			}
			else
			{
				if(comp >= dd)
				{
					return message;
				}
			}
		}
		return d;
	}

	public static void addEQList(JComboBox eqList) throws Exception
	{
		addEQList(eqList, Boolean.FALSE);
	}

	public static void addEQList(JComboBox eqList, Boolean manager) throws Exception
	{
		if(eqVec.contains(eqList) == false)
		{
			eqVec.add(eqList);
			eqMan.add(manager);
			setEQList(eqList, manager);
		}
	}

	public static void setEQList(JComboBox eqList, Boolean manager) throws Exception
	{
		boolean man = (manager == Boolean.TRUE) ? true : false;

		eqList.removeAllItems();

		if(man)
			eqList.addItem("All earthquakes");

		String[] list = getDB().getEQList();
		for(int i = 0; i < list.length; i++)
			eqList.addItem(list[i]);

		if(man)
		{
			Object[][] names = Utils.getDB().runQuery("select distinct name from grp order by name");
			if(names != null)
			{
				eqList.addItem(" -- Groups -- ");
				for(int i = 1; i < names.length; i++)
					eqList.addItem(names[i][0]);
			}
		}
	}

	public static void updateEQLists() throws Exception
	{
		lock();
		for(int i = 0; i < eqVec.size(); i++)
			setEQList((JComboBox)eqVec.elementAt(i), (Boolean)eqMan.elementAt(i));
		unlock();
	}

	public static void updateRecordList(JComboBox recordList, String eq) throws Exception
	{
		recordList.removeAllItems();

		recordList.addItem("Select all records");
		String[] list = getDB().getRecordList(eq);
		for(int i = 0; i < list.length; i++)
			recordList.addItem(list[i]);
	}

	public static void updateRecordList(JComboBox recordList, JComboBox eqList) throws Exception
	{
		recordList.removeAllItems();

		if(eqList.getItemCount() == 0)
			return;

		recordList.addItem("Select all records");
		String[] list = getDB().getRecordList(eqList.getSelectedItem().toString());
		for(int i = 0; i < list.length; i++)
			recordList.addItem(list[i]);
	}

	public static String makeDefault(String s, String ifBlank)
	{
		if(s == null || s.equals(""))
			return ifBlank;

		return s;
	}

	public static boolean locked()
	{
		return locked;
	}

	public static void lock()
	{
		locked = true;
	}

	public static void unlock()
	{
		locked = false;
	}

	public static String shorten(Object s)
	{
		if(s == null)
			return "";
		return s.toString();
	}

	public static String nullify(String s)
	{
		if(s == null || s.equals(""))
			return "null";
		return s;
	}
}
