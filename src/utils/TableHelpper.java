package utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

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

}
