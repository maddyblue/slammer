/*
 * InstallThread.java
 * Copyright (C) 1999, 2000 Slava Pestov
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
import java.awt.Dimension;
import javax.swing.*;

/*
 * The thread that performs installation.
 */
public class InstallThread extends Thread
{
	public InstallThread(Install installer, Progress progress,
		String installDir, String binDir, int size, Vector components, Vector sql)
	{
		super("Install thread");

		this.installer = installer;
		this.progress = progress;
		this.installDir = installDir;
		this.binDir = binDir;
		this.size = size;
		this.components = components;
		this.sql = sql;

		buf = new byte[32768];
	}

	public void setProgress(Progress progress)
	{
		this.progress = progress;
	}

	public void run()
	{
		progress.setMaximum(size * 1024);

		try
		{
			for(int i = 0; i < components.size(); i++)
			{
				installComponent((String)components.elementAt(i), sql.elementAt(i) != null);
			}

			String sqlfile;
			JProgressBar prog = new JProgressBar(0, sqlnum);
			JFrame progFrame = new JFrame("Progress...");

			prog.setStringPainted(true);
			prog.setString("Updating database");
			progFrame.getContentPane().add(prog);
			progFrame.setSize(300, 75);
			Dimension screen = progFrame.getToolkit().getScreenSize();
			Dimension size = progFrame.getSize();
			progFrame.setLocation((screen.width - size.width) / 2,	(screen.height - size.height) / 2);
			int incr = 0;

			for(int i = 0; i < sql.size(); i++)
			{
				if((sqlfile = (String)sql.elementAt(i)) != null)
				{
					progFrame.show();
					startdb();

					FileReader fr = new FileReader(installDir + File.separatorChar
						+ "records" + File.separatorChar + sqlfile);
					String s = "";
					String cur[] = new String[17];
					int j = 0;
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

								Object res[][] = runQuery("select id from data where eq='" + cur[0] + "' and record='" + cur[1] + "'");
								if(res != null)
								{
									// already in database: continue
									System.out.println("already in db");
									continue;
								}

								q = "insert into data values (uniquekey('data'), '" + cur[0] + "', '" + cur[1] + "', " + cur[2] + ", " + nullify(cur[3]) + ", " + cur[4] + ", " + cur[5] + ", " + cur[6] + ", " + cur[7] + ", " + nullify(cur[8]) + ", " + nullify(cur[9]) + ", " + nullify(cur[10]) + ", " + cur[11] + ", '" + cur[12] + "', '" + cur[13] + "', " + nullify(cur[14]) + ", " + nullify(cur[15]) + ", " + cur[16] + ", " + 0 + ", '" + path + "', 0, 0, 0)";
								runQuery(q);

								prog.setValue(incr++);
								prog.paintImmediately(0,0,prog.getWidth(),prog.getHeight());

								break;
							}
							default:
								s += (char)c;
								break;
						}
					}
				}
			}
			progFrame.dispose();

			closedb();

			// create it in case it doesn't already exist
			if(binDir != null)
				OperatingSystem.getOperatingSystem().mkdirs(binDir);

			OperatingSystem.getOperatingSystem().createScript(
				installer,installDir,binDir,
				installer.getProperty("app.name"));
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
			return;
		}
		catch(Exception ex)
		{
			progress.error(ex.toString());
			ex.printStackTrace();
			return;
		}

		progress.done();
	}

	// private members
	private Install installer;
	private Progress progress;
	private String installDir;
	private String binDir;
	private int size;
	private Vector components;
	private Vector sql;
	private byte[] buf;
	private int sqlnum = 0;

	private void installComponent(String name, boolean incrsql) throws IOException
	{
		BufferedReader fileList = new BufferedReader(
			new InputStreamReader(getClass()
			.getResourceAsStream(name)));

		String fileName;
		while((fileName = fileList.readLine()) != null)
		{
			String outfile = installDir + File.separatorChar
				+ fileName.replace('/',File.separatorChar);

			InputStream in = new BufferedInputStream(
				getClass().getResourceAsStream("/" + fileName));

			if(in == null)
				throw new FileNotFoundException(fileName);

			copy(in,outfile);
			in.close();

			if(incrsql)
				sqlnum++;
		}

		fileList.close();
	}

	private void copy(InputStream in, String outfile) throws IOException
	{
		File outFile = new File(outfile);

		OperatingSystem.getOperatingSystem().mkdirs(outFile.getParent());

		BufferedOutputStream out = new BufferedOutputStream(
			new FileOutputStream(outFile));

		int count;

		for(;;)
		{
			count = in.read(buf,0,buf.length);
			if(count == -1)
				break;

			out.write(buf,0,count);
			progress.advance(count);
		}

		in.close();
		out.close();
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

	// database connection stuff

	private java.sql.Connection connection = null;

	private void startdb() throws Exception
	{
		if(connection != null)
			return;

		Class.forName("com.mckoi.JDBCDriver");

		String url = "jdbc:mckoi:local://" + installDir + File.separatorChar + "programs" + File.separatorChar + "Database" + File.separatorChar + "db.conf";
		String username = "newmark";
		String password = "newmark";

		connection = java.sql.DriverManager.getConnection(url, username, password);
	}

	private Object[][] runQuery(String query) throws SQLException
	{
		Statement statement = connection.createStatement();
		ResultSet result = null;
		ResultSetMetaData resdata;

		result = statement.executeQuery(query);

		int row_count = 0;
		while(result.next()) row_count++;
		if(row_count <= 0)
		{
			statement.close();
			return null;
		}

		resdata = result.getMetaData();
		int word_len;
		int col_count = resdata.getColumnCount();
		result.first();
		result.previous();

		Object[][] array = new Object[row_count + 1][col_count];
		int[] col_len = new int[col_count];
		int temp;
		for(int i = 1; i <= col_count; i++)
		{
			array[0][i - 1] = resdata.getColumnName(i);
			temp = (array[0][i - 1].toString()).length();
			if(temp > col_len[i - 1]) col_len[i - 1] = temp;
		}
		for(int i1 = 1; result.next(); i1++)
		{
			for(int i2 = 1; i2 <= col_count; i2++)
			{
				array[i1][i2 - 1] = result.getString(i2);
				if(array[i1][i2 - 1] == null)
					temp = 0;
				else
					temp = (array[i1][i2 - 1].toString()).length();
				if(temp > col_len[i2 - 1]) col_len[i2 - 1] = temp;
			}
		}
		statement.close();
		return array;
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
