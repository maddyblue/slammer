/*
 * DBThread.java
 * Copyright (C) 2003 Matt Jibson
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

package installer;

import java.io.*;
import java.util.Vector;
import java.sql.*;

public class DBThread extends Thread
{
	public DBThread(Vector dbvect, Vector dblen, String installDir, Progress progress)
	{
		this.dbvect = dbvect;
		this.dblen = dblen;
		this.installDir = installDir;
		this.progress = progress;
	}

	public void run()
	{
		try
		{
			String len;
			int length = 0;

			for(int i = 0; i < dblen.size(); i++)
			{
				len = (String)dblen.elementAt(i);

				if(len != null)
					length += Integer.parseInt(len);
			}

			progress.setMaximumDB(length);

			String sqlfile;

			for(int i = 0; i < dbvect.size(); i++)
			{
				sqlfile = (String)dbvect.elementAt(i);

				FileReader fr = new FileReader(installDir + File.separatorChar
					+ "records" + File.separatorChar + sqlfile);
				String s = "";
				String cur[] = new String[17];
				int j = 0;
				int reslen;
				char c;
				String q;

				while(fr.ready())
				{
					c = (char)fr.read();

					switch(c)
					{
						case '\r':
							break;
						case '\t':
							cur[j] = addSlashes(s);
							j++;
							s = "";
							break;
						case '\n':
						{
							cur[j] = addSlashes(s);
							s = "";
							j = 0;

							String path = addSlashes(installDir + File.separator + "records" + File.separator) + cur[0] + addSlashes(File.separator) + cur[1];

							progress.advanceDB();

							reslen = runQuery("select id from data where eq='" + cur[0] + "' and record='" + cur[1] + "'");
							System.out.println(reslen);

							// entry is not already in db
							if(reslen == 0)
							{
								q = "insert into data values (uniquekey('data'), '" + cur[0] + "', '" + cur[1] + "', " + cur[2] + ", " + nullify(cur[3]) + ", " + cur[4] + ", " + cur[5] + ", " + cur[6] + ", " + cur[7] + ", " + nullify(cur[8]) + ", " + nullify(cur[9]) + ", " + nullify(cur[10]) + ", " + cur[11] + ", '" + cur[12] + "', '" + cur[13] + "', " + nullify(cur[14]) + ", " + nullify(cur[15]) + ", " + cur[16] + ", " + 0 + ", '" + path + "', 0, 0, 0)";
								runQuery(q);
								System.out.println(q);
							}
							else
							{
								System.out.println(cur[0] + " - " + cur[1] + ": already in db");
							}

							break;
						}
						default:
							s += (char)c;
							break;
					}
				}
			}

			closedb();
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
			ex.printStackTrace();
			return;
		}
	}

	// private members
	private Vector dbvect, dblen;
	private String installDir;
	private Progress progress;

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

	// database connection stuff
	private java.sql.Connection connection = null;

	public void startdb() throws Exception
	{
		if(connection != null)
			return;

		Class.forName("com.mckoi.JDBCDriver");

		String url = "jdbc:mckoi:local://" + installDir + File.separatorChar + "programs" + File.separatorChar + "Database" + File.separatorChar + "db.conf";
		String username = "newmark";
		String password = "newmark";

		connection = java.sql.DriverManager.getConnection(url, username, password);
	}

	private int runQuery(String query) throws SQLException
	{
		Statement statement = connection.createStatement();
		ResultSet result = null;

		result = statement.executeQuery(query);
		statement.close();

		int ret = 0;

		while(result.next())
			ret++;

		return ret;
	}

	public void closedb() throws SQLException
	{
		if(connection != null)
			connection.close();
	}

	// utility functions

	public static String nullify(String s)
	{
		if(s == null || s.equals(""))
			return "null";
		return s;
	}
}
