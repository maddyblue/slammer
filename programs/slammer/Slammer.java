/* This file is in the public domain. */

package slammer;

import slammer.gui.*;
import slammer.analysis.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Slammer
{
	// order of fields for the eq.sql file
	public static final int DB_eq        = 0;
	public static final int DB_record    = 1;
	public static final int DB_digi_int  = 2;
	public static final int DB_mom_mag   = 3;
	public static final int DB_arias     = 4;
	public static final int DB_dobry     = 5;
	public static final int DB_pga       = 6;
	public static final int DB_pgv       = 7;
	public static final int DB_mean_per  = 8;
	public static final int DB_epi_dist  = 9;
	public static final int DB_foc_dist  = 10;
	public static final int DB_rup_dist  = 11;
	public static final int DB_vs30      = 12;
	public static final int DB_class     = 13;
	public static final int DB_foc_mech  = 14;
	public static final int DB_location  = 15;
	public static final int DB_owner     = 16;
	public static final int DB_latitude  = 17;
	public static final int DB_longitude = 18;
	public static final int DB_LENGTH    = 19;

	// order of fields for the EQdata.txt file
	public static final int EQDAT_eq = 0;
	public static final int EQDAT_mom_mag = 1;
	public static final int EQDAT_foc_mech = 2;
	public static final int EQDAT_record = 3;
	public static final int EQDAT_location = 4;
	public static final int EQDAT_owner = 5;
	public static final int EQDAT_latitude = 6;
	public static final int EQDAT_longitude = 7;
	public static final int EQDAT_class = 8;
	public static final int EQDAT_digi_int = 9;
	public static final int EQDAT_epi_dist = 10;
	public static final int EQDAT_foc_dist = 11;
	public static final int EQDAT_rup_dist = 12;
	public static final int EQDAT_LENGTH = 13;

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

				FileWriter fw = new FileWriter(".." + File.separator + "records" + File.separator + "eq.sql");

				Object[][] res = Utils.getDB().runQuery("select eq, record, digi_int, mom_mag, arias, dobry, pga, pgv, mean_per, epi_dist, foc_dist, rup_dist, vs30, class, foc_mech, location, owner, latitude, longitude from data order by eq, record");

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
					+ "pgv       double       not null,"
					+ "mean_per  double       not null,"
					+ "epi_dist  double               ,"
					+ "foc_dist  double               ,"
					+ "rup_dist  double               ,"
					+ "vs30      double               ,"
					+ "class     smallint     not null,"
					+ "foc_mech  smallint     not null,"
					+ "location  varchar(100) not null,"
					+ "owner     varchar(100) not null,"
					+ "latitude  double               ,"
					+ "longitude double               ,"
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
				String cur[] = new String[EQDAT_LENGTH];
				int i = 0;
				int incr;
				String path, q;
				double di;
				DoubleList data;
				Object res[][];
				int c;

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
								case EQDAT_mom_mag:
								case EQDAT_epi_dist:
								case EQDAT_foc_dist:
								case EQDAT_rup_dist:
								case EQDAT_latitude:
								case EQDAT_longitude:
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
							path = "../records/" + cur[EQDAT_eq] + "/" + cur[EQDAT_record];

							res = Utils.getDB().runQuery("select id from data where path='" + path + "'");
							if(res != null)
							{
								System.out.println("Already in db: " + path);
								continue;
							}

							di = Double.parseDouble(cur[EQDAT_digi_int]);
							data = new DoubleList(path);
							if(data.bad())
							{
								GUIUtils.popupError("Invalid data in file " + path + " at point " + data.badEntry() + ".");
								continue;
							}
							q = "insert into data " +
								"(eq, record, digi_int, mom_mag, arias, dobry, pga, pgv, mean_per, epi_dist, foc_dist, rup_dist, foc_mech, location, owner, latitude, longitude, class, change, path, select1, analyze, select2)" +
								" values ( " +
								"'" + cur[EQDAT_eq] + "'," + // eq
								"'" + cur[EQDAT_record] + "'," + // record
								"" + cur[EQDAT_digi_int] + "," + // digi_int
								"" + cur[EQDAT_mom_mag] + "," + // mom_mag
								"" + ImportRecords.Arias(data, di) + "," + // arias
								"" + ImportRecords.Dobry(data, di) + "," + // dobry
								"" + ImportRecords.PGA(data) + "," + // pga
								"" + ImportRecords.PGV(data, di) + "," + // pgv
								"" + ImportRecords.MeanPer(data, di) + "," + // mear_per
								"" + cur[EQDAT_epi_dist] + "," + // epi_dist
								"" + cur[EQDAT_foc_dist] + "," + // foc_dist
								"" + cur[EQDAT_rup_dist] + "," + // rup_dist
								"" + cur[EQDAT_foc_mech] + "," + // foc_mech
								"'" + cur[EQDAT_location] + "'," + // location
								"'" + cur[EQDAT_owner] + "'," + // owner
								"" + cur[EQDAT_latitude] + "," + // latitude
								"" + cur[EQDAT_longitude] + "," + // longitude
								"" + cur[EQDAT_class] + "," + // class
								"0," + // change
								"'" + path + "'," + // path
								"0," + // select1
								"0," + // analyze
								"0" + // select2
								")";
							System.out.println(q);
							Utils.getDB().runUpdate(q);
							cur = new String[EQDAT_LENGTH];

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
				String cur[] = new String[DB_LENGTH];
				int j = 0;
				int reslen;
				char c;
				String q, path;

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

							path = ".." + File.separatorChar + "records" + File.separator + cur[DB_eq] + File.separator + cur[DB_record];

							q = "insert into data (eq, record, digi_int, mom_mag, arias, dobry, pga, pgv, mean_per, epi_dist, foc_dist, rup_dist, vs30, class, foc_mech, location, owner, latitude, longitude, change, path, select1, analyze, select2) values ('" +
								cur[DB_eq] + "', '" +
								cur[DB_record] + "', " +
								cur[DB_digi_int] + ", " +
								Utils.nullify(cur[DB_mom_mag]) + ", " +
								cur[DB_arias] + ", " +
								cur[DB_dobry] + ", " +
								cur[DB_pga] + ", " +
								cur[DB_pgv] + ", " +
								cur[DB_mean_per] + ", " +
								Utils.nullify(cur[DB_epi_dist]) + ", " +
								Utils.nullify(cur[DB_foc_dist]) + ", " +
								Utils.nullify(cur[DB_rup_dist]) + ", " +
								Utils.nullify(cur[DB_vs30]) + ", " +
								cur[DB_class] + ", " +
								cur[DB_foc_mech] + ", '" +
								cur[DB_location] + "', '" +
								cur[DB_owner] + "', " +
								Utils.nullify(cur[DB_latitude]) + ", " +
								Utils.nullify(cur[DB_longitude]) + ", " +
								0 + ", '" +
								path + "', 0, 0, 0)";
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
			else if(args[0].equals("test")) // {{{
			{
				junit.textui.TestRunner.run(SlammerTest.suite());
			} // }}}
			else if(args[0].equals("testg")) // {{{
			{
				junit.swingui.TestRunner tr = new junit.swingui.TestRunner();
				tr.start(new String[] { "slammer.SlammerTest" });
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
				JFrame frame = new JFrame("Slammer");
				frame.setIconImage(new ImageIcon(
					frame.getClass().getResource("/slammer/images/icon.jpg")).getImage());
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

				frame.getContentPane().add(new SlammerTabbedPane(frame));
				Dimension screen = frame.getToolkit().getScreenSize();
				int x = (int) (screen.width * 0.8), y = (int) (screen.height * 0.8);

				frame.setSize(x, y);
				GUIUtils.setLocationMiddle(frame);
				splash.advance();

				frame.setVisible(true);
				splash.dispose();
			} // }}}
		}
		catch(Exception ex)
		{
			Utils.catchException(ex);
			System.exit(1);
		}
	}
}
