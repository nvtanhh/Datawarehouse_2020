package dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;


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
			PreparedStatement ps = loadControlConnection().prepareStatement(sql);
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

	public static Connection loadControlConnection() {
		try (FileInputStream f = new FileInputStream("./config/config.properties")) {
			// load the properties file
			Properties pros = new Properties();
			pros.load(f);

			// assign db parameters
			String url = pros.getProperty("db.url");
			String user = pros.getProperty("db.user");
			String password = pros.getProperty("db.password");

			return getConnection(url, user, password);

		} catch (IOException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

}
