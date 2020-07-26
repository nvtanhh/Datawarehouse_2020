package utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class TableHelpper {

	public static String getCols(Connection conn, String tableName) throws Exception {
		String rs = "";
		ResultSet resultSet = conn.createStatement().executeQuery("SELECT * FROM " + tableName);
		ResultSetMetaData metadata = resultSet.getMetaData();
		for (int i = 1; i <= metadata.getColumnCount(); i++) {
			String columnName = metadata.getColumnName(i);
			if (columnName.equals("id")) {
				continue;
			} else
				rs += columnName + ",";
		}
		return rs.substring(0, rs.length() - 1);
	}

	public static boolean isExist(Connection connection, String tableName) {
		try {
			DatabaseMetaData dbm = connection.getMetaData();
			ResultSet tables = dbm.getTables(null, null, tableName, null);
			return tables.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	public static void createTable(Connection connection, String into_table, String which_column) {
		String[] columns = which_column.split(",");

		String sql = "CREATE TABLE " + into_table + " (id INTEGER NOT NULL AUTO_INCREMENT, ";
		for (int i = 0; i < columns.length; i++) {
			sql += columns[i] + " VARCHAR(255), ";
		}
		sql += " PRIMARY KEY ( id ))";

		Statement statement;
		try {
			statement = connection.createStatement();
			statement.executeUpdate(sql);
			statement.close();
			System.out.println("CREATE TABLE " + into_table + " WITH " + which_column);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
