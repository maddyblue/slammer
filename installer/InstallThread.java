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
	public InstallThread(Install installer, Progress progress, String installDir, String binDir, int size, Vector components, Vector sql, Vector sqllen, DBThread dbthread, Vector dbvect)
	{
		super("Install thread");

		this.installer = installer;
		this.progress = progress;
		this.installDir = installDir;
		this.binDir = binDir;
		this.size = size;
		this.components = components;
		this.sql = sql;
		this.sqllen = sqllen;
		this.dbthread = dbthread;
		this.dbvect = dbvect;

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
			int i = 0;
			boolean newDB = false;

			// first, install the Newmark fileset
			if(((String)components.elementAt(i)).equals("newmark-program"))
			{
				installComponent((String)components.elementAt(i));
				i++;
				newDB = true;
			}

			// with the remaining sets, install any sql files
			String sqlfile;

			for(int j = i; j < components.size(); j++)
			{
				sqlfile = (String)sql.elementAt(j);

				if(sqlfile != null)
				{
					String outfile = installDir + File.separatorChar
						+ "records" + File.separatorChar + sqlfile;

					InputStream in = new BufferedInputStream(
					getClass().getResourceAsStream("/records/" + sqlfile));

					if(in == null)
						throw new FileNotFoundException(sqlfile);

					copy(in, outfile);
					in.close();

					dbvect.addElement(sqlfile);
				}
			}

			// now start the database thread
			dbthread.startdb(newDB);
			setPriority(NORM_PRIORITY);
			dbthread.setPriority(NORM_PRIORITY);
			dbthread.start();

			// install the eq sets concurrently with the database updates
			for(; i < components.size(); i++)
			{
				installComponent((String)components.elementAt(i));
			}

			// wait until the database is done updating
			dbthread.join();

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
	private Vector sql, sqllen;
	private byte[] buf;
	private DBThread dbthread;
	private Vector dbvect;

	private void installComponent(String name) throws IOException
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
}
