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

/* $Id: Newmark.java,v 1.3 2003/07/18 05:25:15 dolmant Exp $ */

package newmark;

import newmark.gui.*;
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
			if(args.length < 1)
			{
				args = new String[] {""};
			}

			if(args[0].equals("getsql"))
			{

				FileWriter fw = new FileWriter(".." + File.separatorChar + "records" + File.separatorChar + "eq.sql");

				Object[][] res = Utils.getDB().runQuery("select eq, record, digi_int, mom_mag, arias, dobry, pga, mean_per, epi_dist, foc_dist, rup_dist, foc_mech, location, owner, latitude, longitude, class from data order by eq, record");

				if(res == null || res.length <= 1)
				{
					System.out.println("No records returned.");
					return;
				}

				for(int i = 1; i < res.length; i++)
				{
					if((i % 20) == 0)
						System.out.println(i);

					if(i > 1)
						fw.write("\n");

					for(int j = 0; j < res[0].length; j++)
					{
						if(j > 0)
							fw.write("\t");

						fw.write(Utils.shorten(res[i][j]));
					}
				}

				fw.close();
				Utils.closeDB();
			}
			else if(args[0].equals("create"))
			{
				Utils.getDB().runQuery("create table data ("
					+ "id        integer(10)  not null,"
					+ "eq        varchar(100) not null,"
					+ "record    varchar(100) not null,"
					+ "digi_int  double       not null,"
					+ "mom_mag   double           null,"
					+ "arias     double       not null,"
					+ "dobry     double       not null,"
					+ "pga       double       not null,"
					+ "mean_per  double       not null,"
					+ "epi_dist  double           null,"
					+ "foc_dist  double           null,"
					+ "rup_dist  double           null,"
					+ "foc_mech  tinyint      not null,"
					+ "location  varchar(100) not null,"
					+ "owner     varchar(100) not null,"
					+ "latitude  double           null,"
					+ "longitude double           null,"
					+ "class     tinyint      not null,"
					+ "change    bit          not null,"
					+ "path      varchar(255) not null,"
					+ "select1   bit          not null,"
					+ "analyze   bit          not null,"
					+ "select2   bit          not null,"
					+ "unique (id)"
					+ ")");

				Utils.getDB().runQuery("create table group ("
					+ "record    integer(10)  not null,"
					+ "name      varchar(100) not null,"
					+ "analyze   bit          not null"
					+ ")");
				Utils.closeDB();
			}
			else if(args[0].equals("import"))
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

				while(fr.ready())
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
									cur[i] = Utils.addSlashes(s);
									break;
							}
							i++;
							s = "";
							break;
						case '\n':
						{
							cur[i] = s;
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
							q = "insert into data values (uniquekey('data'), '" + cur[0] + "', '" + cur[3] + "', " + cur[9] + ", " + cur[1] + ", " + Analysis.Arias(data, di) + ", " + Analysis.Dobry(data, di) + ", " + Analysis.PGA(data) + ", " + Analysis.MeanPer(data, di) + ", " + cur[10] + ", " + cur[11] + ", " + cur[12] + ", " + cur[2] + ", '" + cur[4] + "', '" + cur[5] + "', " + cur[6] + ", " + cur[7] + ", " + cur[8] + ", 0, '" + path + "', 0, 0, 0)";
							System.out.println(q);
							Utils.getDB().runQuery(q);
							cur = new String[13];

							break;
						}
						default:
							s += (char)c;
							break;
					}
				}

				fr.close();
				Utils.closeDB();
			}
			else
			{
				Utils.getDB().runQuery("update data set select1=false where select1=true");
				Utils.getDB().runQuery("update data set select2=false where select2=true");

				JFrame frame = new JFrame("Newmark");
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
				frame.getContentPane().add(new NewmarkTabbedPane(frame));
				frame.setSize(780,575);
				GUIUtils.setLocationMiddle(frame);
				frame.setVisible(true);
			}
		}
		catch(Exception ex)
		{
			Utils.catchException(ex);
		}
	}
}
