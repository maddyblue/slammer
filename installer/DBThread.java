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
							cur[j] = addQuote(s);
							j++;
							s = "";
							break;
						case '\n':
						{
							cur[j] = addQuote(s);
							s = "";
							j = 0;

							String path = addQuote(installDir + File.separator + "records" + File.separator) + cur[0] + addQuote(File.separator) + cur[1];

							progress.advanceDB();

							q = "insert into data (eq, record, digi_int, mom_mag, arias, dobry, pga, mean_per, epi_dist, foc_dist, rup_dist, foc_mech, location, owner, latitude, longitude, class, change, path, select1, analyze, select2) values ('" + cur[0] + "', '" + cur[1] + "', " + cur[2] + ", " + nullify(cur[3]) + ", " + cur[4] + ", " + cur[5] + ", " + cur[6] + ", " + cur[7] + ", " + nullify(cur[8]) + ", " + nullify(cur[9]) + ", " + nullify(cur[10]) + ", " + cur[11] + ", '" + cur[12] + "', '" + cur[13] + "', " + nullify(cur[14]) + ", " + nullify(cur[15]) + ", " + cur[16] + ", " + 0 + ", '" + path + "', 0, 0, 0)";
							runUpdate(q);
							System.out.println(q);

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

	// database connection stuff
	private java.sql.Connection connection = null;
	public static final String url = "jdbc:derby:db";

	public void startdb(boolean newDB) throws Exception
	{
		if(connection != null)
			return;

		System.setProperty("derby.system.home", installDir + File.separator + "programs" + File.separator + "database");
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

		String connect_url = url;
		if(newDB)
			connect_url += ";create=true";

		connection = java.sql.DriverManager.getConnection(connect_url);

		if(newDB)
		{
			runUpdate("create table data ("
				+ "id        integer      not null generated always as identity primary key,"
				+ "eq        varchar(100) not null,"
				+ "record    varchar(100) not null,"
				+ "digi_int  double       not null,"
				+ "mom_mag   double               ,"
				+ "arias     double       not null,"
				+ "dobry     double       not null,"
				+ "pga       double       not null,"
				+ "mean_per  double       not null,"
				+ "epi_dist  double               ,"
				+ "foc_dist  double               ,"
				+ "rup_dist  double               ,"
				+ "foc_mech  smallint     not null,"
				+ "location  varchar(100) not null,"
				+ "owner     varchar(100) not null,"
				+ "latitude  double               ,"
				+ "longitude double               ,"
				+ "class     smallint     not null,"
				+ "change    smallint     not null,"
				+ "path      varchar(255) not null,"
				+ "select1   smallint     not null,"
				+ "analyze   smallint     not null,"
				+ "select2   smallint     not null"
				+ ")");

			runUpdate("create table grp ("
				+ "record    integer      not null,"
				+ "name      varchar(100) not null,"
				+ "analyze   smallint     not null"
				+ ")");
		}
	}

	private int runUpdate(String update) throws SQLException
	{
		Statement statement = connection.createStatement();
		int result = statement.executeUpdate(update);
		statement.close();
		return result;
	}

	public void closedb() throws SQLException
	{
		if(connection != null)
		{
			connection.close();
			connection = null;
			try {
				java.sql.DriverManager.getConnection(url + ";shutdown=true");
			} catch(Exception e) {}
		}
	}

	// utility functions

	public static String nullify(String s)
	{
		if(s == null || s.equals(""))
			return "null";
		return s;
	}
}
