package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import run.Control;

public class DBConnector {
	private static String dbConfig = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Asia/Ho_Chi_Minh&allowLoadLocalInfile = true";

	public static Connection getConnection(String url, String user, String password) {
		Connection conn = null;
		url += dbConfig;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return conn;
	}

	public static Connection getConnectionFormDB(int desDB) {
		try {
			String sql = "SELECT * FROM `db_infor` WHERE id = ?";
			PreparedStatement ps = Control.controlConn.prepareStatement(sql);
			ps.setInt(1, desDB);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				String url = rs.getString("url") + dbConfig;
				return DBConnector.getConnection(url, rs.getString("user"), rs.getString("password"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
