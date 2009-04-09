/* This file is in the public domain. */

package slammer;

import java.sql.*;

public class EQDatabase
{
	protected java.sql.Connection connection;
	public static final String url = "jdbc:derby:db";

	public EQDatabase() throws Exception
	{
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		connection = java.sql.DriverManager.getConnection(url);
	}

	private String format(Object obj, int len)
	{
		String str;
		if(obj == null) str = "";
		else str = obj.toString();

		if(str.length() > len) str = str.substring(0,len);
		for(int i = str.length(); i < len; i++)
		{
			str = str + " ";
		}

		str = " " + str + " ";
		return str;
	}

	private String formatting(int len, int[] col_len)
	{
		// print -s and +s
		String str = "+";
		for(int i1 = 0; i1 < len; i1++)
		{
			// starts at -2 because of the extra spaces on either side (refer to the format function)
			for(int i2 = -2; i2 < col_len[i1]; i2++)
			{
				str = str + "-";
			}
			str = str + "+";
		}
		return str;
	}

	public void set(String eq, String record, String set) throws SQLException
	{
		runUpdate("update data set " + set + " where eq='" + eq + "' and record='" + record + "'");
	}

	public int runUpdate(String update) throws SQLException
	{
		Statement statement = connection.createStatement();
		int i = statement.executeUpdate(update);
		statement.close();
		return i;
	}

	// print determines if the returned results from the query are printed
	public Object[][] runQuery(String query) throws SQLException
	{
		Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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

	public void close() throws SQLException
	{
		connection.close();
		connection = null;
		try {
			java.sql.DriverManager.getConnection(EQDatabase.url + ";shutdown=true");
		} catch(Exception e) {}
	}

	// EQ functions

	public Double getDI(String eq, String record) throws SQLException
	{
		Object[][] array = runQuery("select digi_int from data where eq='" + eq + "' and record='" + record + "'");
		return new Double((array[1][0]).toString());
	}

	public String[] getEQList() throws SQLException
	{
		Object[][] array = runQuery("select distinct(eq) from data order by eq");

		if(array == null)
			return (new String[0]);

		String[] list = new String[array.length - 1];

		for(int i = 0; i < list.length; i++)
			list[i] =  Utils.shorten(array[i + 1][0]);

		return list;
	}

	public String[] getRecordList(String eq) throws SQLException
	{
		Object[][] array = runQuery("select record from data where eq='" + eq + "' order by record");
		String[] list = new String[array.length - 1];
		for(int i = 0; i < list.length; i++)
		{
			list[i] = array[i + 1][0].toString();
		}
		return list;
	}

	public String getPath(String eq, String record) throws SQLException
	{
		Object[][] array = runQuery("select path from data where eq='" + eq + "' and record='" + record + "'");
		return array[1][0].toString();
	}

	public double getPGA(String eq, String record) throws SQLException
	{
		Object[][] array = runQuery("select pga_g from data where eq='" + eq + "' and record='" + record + "'");
		return Double.parseDouble((array[1][0]).toString());
	}

	public void syncRecords(String where) throws SQLException
	{
		runUpdate("update data set " +
			"mag_srch=cast(cast(mom_mag as decimal(10,4)) as double), " +
			"epi_srch=cast(cast(epi_dist as decimal(10,4)) as double), " +
			"foc_srch=cast(cast(foc_dist as decimal(10,4)) as double), " +
			"rup_srch=cast(cast(rup_dist as decimal(10,4)) as double), " +
			"vs30_srch=cast(cast(vs30 as decimal(10,4)) as double), " +
			"lat_srch=cast(cast(latitude as decimal(20,15)) as double), " +
			"lng_srch=cast(cast(longitude as decimal(20,15)) as double) " +
			where);
	}
}
