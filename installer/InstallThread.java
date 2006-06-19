/*
 * InstallThread.java
 *
 * Originally written by Slava Pestov for the jEdit installer project. This work
 * has been placed into the public domain. You may use this work in any way and
 * for any purpose you wish.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */
package installer;

import java.io.*;
import java.util.Vector;
import java.sql.*;

/*
 * The thread that performs installation.
 */
public class InstallThread extends Thread
{
	public InstallThread(Install installer, Progress progress,
		String installDir, OperatingSystem.OSTask[] osTasks,
		int size, Vector components)
	{
		super("Install thread");

		this.installer = installer;
		this.progress = progress;
		this.installDir = installDir;
		this.osTasks = osTasks;
		this.size = size;
		this.components = components;
	}

	public void run()
	{
		// The *2 is so the last 50% can be used for db sync.
		progress.setMaximum(size * 1024 * 2);

		try
		{
			// install user-selected packages
			for(int i = 0; i < components.size(); i++)
			{
				String comp = installer.getProperty(
					"comp." + ((Integer)components.elementAt(i)).toString() + ".fileset");
				System.err.println("Installing " + comp);
				installComponent(comp);
			}

			// do operating system specific stuff (creating startup
			// scripts, installing man pages, etc.)
			for(int i = 0; i < osTasks.length; i++)
			{
				System.err.println("Performing task " +
					osTasks[i].getName());
				osTasks[i].perform(installDir,components);
			}
		}
		catch(FileNotFoundException fnf)
		{
			progress.error("The installer could not create the "
				+ "destination directory.\n"
				+ "Maybe you do not have write permission?");
			return;
		}
		catch(IOException io)
		{
			progress.error(io.toString());
			io.printStackTrace();
			return;
		}

		// File install is done, begin database sync

		try
		{

			int sqllen = 0;

			// start the database
			String DBloc = installDir + File.separatorChar + "programs" + File.separatorChar + "database" + File.separatorChar + "db";
			File f = new File(DBloc);
			boolean newDB = !f.exists();

			startdb(newDB);

			// install any .sql files
			String sqlfile;

			for(int j = 0; j < components.size(); j++)
			{
				sqlfile = installer.getProperty("comp." + ((Integer)components.elementAt(j)).toString() + ".sql");

				if(sqlfile != null)
				{
					String outfile = installDir + File.separatorChar
						+ "records" + File.separatorChar + sqlfile;

					InputStream in = new BufferedInputStream(
					getClass().getResourceAsStream("/records/" + sqlfile));

					if(in == null)
						throw new FileNotFoundException(sqlfile);

					installer.copy(in, outfile, null);
					in.close();

					dbvect.addElement(sqlfile);

					sqllen += installer.getIntegerProperty("comp." + ((Integer)components.elementAt(j)).toString() + ".sqllen");
				}
			}

			progress.setMaximum(sqllen * 2);
			progress.advance(sqllen); // go back to 50%

			for(int i = 0; i < dbvect.size(); i++)
			{
				sqlfile = (String)dbvect.elementAt(i);

				FileReader fr = new FileReader(installDir + File.separatorChar
					+ "records" + File.separatorChar + sqlfile);
				String s = "";
				String cur[] = new String[DB_LENGTH];
				int j = 0;
				char c;
				String q, path;

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

							if(countQuery("select count(*) from data where eq='" + cur[0] + "' and record='" + cur[1] + "'") > 0)
							{
								System.out.println(cur[0] + " - " + cur[1] + ": already in db");
							}
							else
							{
								path = installDir + File.separator + "records" + File.separator + cur[0] + File.separator + cur[1];

								progress.advance(1);

								q = "insert into data (eq, record, digi_int, mom_mag, arias, dobry, pga, pgv, mean_per, epi_dist, foc_dist, rup_dist, foc_mech, location, owner, latitude, longitude, class, change, path, select1, analyze, select2) values ('" + cur[DB_eq] + "', '" + cur[DB_record] + "', " + cur[DB_digi_int] + ", " + nullify(cur[DB_mom_mag]) + ", " + cur[DB_arias] + ", " + cur[DB_dobry] + ", " + cur[DB_pga] + ", " + cur[DB_pgv] + ", " + cur[DB_mean_per] + ", " + nullify(cur[DB_epi_dist]) + ", " + nullify(cur[DB_foc_dist]) + ", " + nullify(cur[DB_rup_dist]) + ", " + cur[DB_foc_mech] + ", '" + cur[DB_location] + "', '" + cur[DB_owner] + "', " + nullify(cur[DB_latitude]) + ", " + nullify(cur[DB_longitude]) + ", " + cur[DB_class] + ", " + 0 + ", '" + path + "', 0, 0, 0)";
								runUpdate(q);
								System.out.println("adding " + cur[0] + " - " + cur[1]);
							}

							break;
						}
						default:
							s += c;
							break;
					}
				}
			}

			closedb();
		}
		catch(Exception ex)
		{
			progress.error(ex.toString());
			ex.printStackTrace();
		}

		// done with db sync

		progress.done();
	}

	// private members
	private Install installer;
	private Progress progress;
	private String installDir;
	private OperatingSystem.OSTask[] osTasks;
	private int size;
	private Vector components;

	// order of fields for the eq.sql file
	private static final int DB_eq = 0;
	private static final int DB_record = 1;
	private static final int DB_digi_int = 2;
	private static final int DB_mom_mag = 3;
	private static final int DB_arias = 4;
	private static final int DB_dobry = 5;
	private static final int DB_pga = 6;
	private static final int DB_pgv = 7;
	private static final int DB_mean_per = 8;
	private static final int DB_epi_dist = 9;
	private static final int DB_foc_dist = 10;
	private static final int DB_rup_dist = 11;
	private static final int DB_foc_mech = 12;
	private static final int DB_location = 13;
	private static final int DB_owner = 14;
	private static final int DB_latitude = 15;
	private static final int DB_longitude = 16;
	private static final int DB_class = 17;
	private static final int DB_LENGTH = 18;

	private void installComponent(String name) throws IOException
	{
		InputStream in = new BufferedInputStream(
			getClass().getResourceAsStream(name + ".tar.bz2"));
		// skip header bytes
		// maybe should check if they're valid or not?
		in.read();
		in.read();

		TarInputStream tarInput = new TarInputStream(
			new CBZip2InputStream(in));
		TarEntry entry;
		while((entry = tarInput.getNextEntry()) != null)
		{
			if(entry.isDirectory())
				continue;
			String fileName = entry.getName();
			System.err.println(fileName);
			String outfile = installDir + File.separatorChar
				+ fileName.replace('/',File.separatorChar);
			installer.copy(tarInput,outfile,progress);
		}

		tarInput.close();
		in.close();
	}

	// database stuff
	private java.sql.Connection connection = null;
	public static final String url = "jdbc:derby:db";
	private Vector dbvect = new Vector();

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
				+ "pgv       double       not null,"
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

	private int countQuery(String query) throws SQLException
	{
		Statement statement = connection.createStatement();
		ResultSet result = null;

		result = statement.executeQuery(query);
		result.next();
		int ret = result.getInt(1);
		statement.close();

		return ret;
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
}
