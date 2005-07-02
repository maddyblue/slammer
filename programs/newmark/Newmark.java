/*
 * Newmark.java - the main Newmark class
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

/* $Id$ */

package newmark;

import newmark.gui.*;
import newmark.analysis.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Newmark
{
	public static void main(String[] args) throws Exception
	{
		try
		{
			System.setProperty("derby.system.home", "database");

			if(args.length < 1)
			{
				args = new String[] {""};
			}

			if(args[0].equals("getsql")) // {{{
			{

				FileWriter fw = new FileWriter(".." + File.separatorChar + "records" + File.separatorChar + "eq.sql");

				Object[][] res = Utils.getDB().runQuery("select eq, record, digi_int, mom_mag, arias, dobry, pga, mean_per, epi_dist, foc_dist, rup_dist, foc_mech, location, owner, latitude, longitude, class from data order by eq, record");

				if(res == null || res.length <= 1)
				{
					System.out.println("No records returned.");
					return;
				}

				int i;

				for(i = 1; i < res.length; i++)
				{
					if(i > 1)
						fw.write("\n");

					for(int j = 0; j < res[0].length; j++)
					{
						if(j > 0)
							fw.write("\t");

						fw.write(Utils.shorten(res[i][j]));
					}
				}

				System.out.println(i - 1);

				fw.close();
				Utils.closeDB();
			} // }}}
			else if(args[0].equals("drop")) // {{{
			{
				Utils.getDB().runUpdate("drop table data");
				Utils.getDB().runUpdate("drop table grp");
				Utils.closeDB();
			} // }}}
			else if(args[0].equals("createdb")) // {{{
			{
				Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
				java.sql.Connection connection = java.sql.DriverManager.getConnection(EQDatabase.url + ";create=true");
				connection.close();
				try {
					java.sql.DriverManager.getConnection(EQDatabase.url + ";shutdown=true");
				} catch(Exception e) {}
			} // }}}
			else if(args[0].equals("createtable")) // {{{
			{
				Utils.getDB().runUpdate("create table data ("
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

				Utils.getDB().runUpdate("create table grp ("
					+ "record    integer      not null,"
					+ "name      varchar(100) not null,"
					+ "analyze   smallint     not null"
					+ ")");
			} // }}}
			else if(args[0].equals("import")) // {{{
			{
				FileReader fr = new FileReader(".." + File.separatorChar + "records" + File.separatorChar + "EQdata.txt");
				String s = "";
				String cur[] = new String[13];
				int i = 0;
				int incr;
				String path, q;
				double di;
				DoubleList data;
				Object res[][];
				int c;

				Utils.getDB().runUpdate("delete from data");

				do
				{
					c = (char)fr.read();

					switch(c)
					{
						case '\r':
							break;
						case '\t':
							switch(i)
							{
								case 1:
								case 10:
								case 11:
								case 12:
								case 6:
								case 7:
									cur[i] = Utils.nullify(s);
									break;
								default:
									cur[i] = Utils.addQuote(s);
									break;
							}
							i++;
							s = "";
							break;
						case '\n':
						{
							cur[i] = Utils.nullify(s);
							s = "";
							i = 0;

							incr = 0;
							path = "../records/" + cur[0] + "/" + cur[3];

							res = Utils.getDB().runQuery("select id from data where path='" + path + "'");
							if(res != null)
							{
								System.out.println("Already in db: " + path);
								continue;
							}

							di = Double.parseDouble(cur[9]);
							data = new DoubleList(path);
							if(data.bad())
							{
								GUIUtils.popupError("Invalid data in file " + path + " at point " + data.badEntry() + ".");
								continue;
							}
							q = "insert into data " +
								"(eq, record, digi_int, mom_mag, arias, dobry, pga, mean_per, epi_dist, foc_dist, rup_dist, foc_mech, location, owner, latitude, longitude, class, change, path, select1, analyze, select2)" +
								" values ( " +
								"'" + cur[0] + "'," +
								"'" + cur[3] + "'," +
								"" + cur[9] + "," +
								"" + cur[1] + "," +
								"" + ImportRecords.Arias(data, di) + "," +
								"" + ImportRecords.Dobry(data, di) + "," +
								"" + ImportRecords.PGA(data) + "," +
								"" + ImportRecords.MeanPer(data, di) + "," +
								"" + cur[10] + "," +
								"" + cur[11] + "," +
								"" + cur[12] + "," +
								"" + cur[2] + "," +
								"'" + cur[4] + "'," +
								"'" + cur[5] + "'," +
								"" + cur[6] + "," +
								"" + cur[7] + "," +
								"" + cur[8] + "," +
								"0," +
								"'" + path + "'," +
								"0," +
								"0," +
								"0" +
								")";
							System.out.println(q);
							Utils.getDB().runUpdate(q);
							cur = new String[13];

							break;
						}
						default:
							s += (char)c;
							break;
					}
				}	while(fr.ready());

				fr.close();
				Utils.closeDB();
			} // }}}
			else if(args[0].equals("importsql")) // {{{
			{
				FileReader fr = new FileReader(".." + File.separatorChar + "records" + File.separatorChar + "eq.sql");
				String s = "";
				String cur[] = new String[17];
				int j = 0;
				int reslen;
				char c;
				String q, path;

				Utils.getDB().runUpdate("delete from data");

				do
				{
					c = (char)fr.read();

					switch(c)
					{
						case '\r':
							break;
						case '\t':
							cur[j] = Utils.addQuote(s);
							j++;
							s = "";
							break;
						case '\n':
						{
							cur[j] = Utils.addQuote(s);
							s = "";
							j = 0;

							path = ".." + File.separatorChar + "records" + File.separator + cur[0] + File.separator + cur[1];

							q = "insert into data (eq, record, digi_int, mom_mag, arias, dobry, pga, mean_per, epi_dist, foc_dist, rup_dist, foc_mech, location, owner, latitude, longitude, class, change, path, select1, analyze, select2) values ('" + cur[0] + "', '" + cur[1] + "', " + cur[2] + ", " + Utils.nullify(cur[3]) + ", " + cur[4] + ", " + cur[5] + ", " + cur[6] + ", " + cur[7] + ", " + Utils.nullify(cur[8]) + ", " + Utils.nullify(cur[9]) + ", " + Utils.nullify(cur[10]) + ", " + cur[11] + ", '" + cur[12] + "', '" + cur[13] + "', " + Utils.nullify(cur[14]) + ", " + Utils.nullify(cur[15]) + ", " + cur[16] + ", " + 0 + ", '" + path + "', 0, 0, 0)";
							Utils.getDB().runUpdate(q);
							System.out.println(q);

							break;
						}
						default:
							s += (char)c;
							break;
					}
				}	while(fr.ready());

				Utils.closeDB();
				fr.close();
			} // }}}
			else if(args[0].equals("importjapan")) // {{{
			{
				String inpath = "/home/dolmant/Keepers";
				String outpath = "/home/dolmant/newmark/trunk/records";
				String path;
				File d = new File(inpath);
				File f[] = d.listFiles();
				String cur[] = new String[13];
				double di;
				DoubleList data;
				Double db;
				String eq = "Niigata-Ken-Chuetsu, Japan 2004";
				BufferedReader br;
				FileWriter fw = new FileWriter(outpath + "/EQdata-japan.txt");
				String line[];

				cur[0] = eq;

				cur[10] = ""; // epi dist
				cur[11] = ""; // foc dist
				cur[12] = ""; // rup dist
				cur[2] = "0"; // foc mech
				cur[4] = ""; // location
				cur[5] = ""; // owner
				cur[8] = "0"; // class

				for(int i = 0; i < f.length; i++)
				{
					br = new BufferedReader(new FileReader(f[i]));

					br.readLine();
					br.readLine();
					br.readLine();
					br.readLine();

					line = br.readLine().split("[ \t]+"); // mag
					cur[1] = line[1];

					line = br.readLine().split("[ \t]+"); // station name
					cur[3] = line[2];

					line = br.readLine().split("[ \t]+"); // lat
					cur[6] = line[2];

					line = br.readLine().split("[ \t]+"); // long
					cur[7] = line[2];

					br.readLine();
					br.readLine();

					line = br.readLine().split("[ \t]+"); // di
					di = 1.0 / Double.parseDouble(line[2].substring(0, 3));
					cur[9] = Double.toString(di);

					br.readLine();

					line = br.readLine().split("[ \t]+"); // dir
					path = cur[3] + "-";

					if(line[1].equals("E-W"))
						path += "090";
					else
						path += "000";

					cur[3] = path;
					path = outpath + "/" + eq + "/" + path;

					data = new DoubleList(f[i].getAbsolutePath(), 17);
					if(data.bad())
					{
						GUIUtils.popupError("Invalid data in file " + path + " at point " + data.badEntry() + ".");
						continue;
					}

					for(int j = 0; j < cur.length; j++)
					{
						if(j > 0)
							fw.write("\t");

						fw.write(cur[j]);
					}

					fw.write("\n");

					data.reset();

					double avg = 0;
					while((db = data.each()) != null)
						avg += db.doubleValue();

					avg /= data.size();

					Utilities.Shift(data, new FileWriter(path), 2000.0 / 8388608.0, -avg);

					System.out.println(path);
				}

				fw.close();
			} // }}}
			else // {{{
			{
				SplashScreen splash = new SplashScreen();

				Utils.startDB();

				splash.advance();

				Utils.getDB().runUpdate("update data set select1=0, select2=0");

				splash.advance();

				// if the OS supports a native LF, use it
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

				JFrame frame = new JFrame("Newmark");

				GUIUtils.bg = frame.getBackground();

				splash.advance();

				frame.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e)
					{
						try
						{
							e.getWindow().setVisible(false);
							Utils.closeDB();
						}
						catch(Exception ex)
						{
							Utils.catchException(ex);
						}
						System.exit(0);
					}
				});

				splash.advance();

				frame.getContentPane().add(new NewmarkTabbedPane(frame));
				frame.setSize(780,575);
				GUIUtils.setLocationMiddle(frame);

				splash.dispose();

				frame.setVisible(true);

			} // }}}
		}
		catch(Exception ex)
		{
			Utils.catchException(ex);
			System.exit(1);
		}
	}
}
