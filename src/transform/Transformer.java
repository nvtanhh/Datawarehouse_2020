package transform;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import dao.DBConnector;
import run.Control;

public class Transformer {
	static Timestamp startDT, endDT;

	public static void transform2warehouse() throws Exception {
//		String sql = "SELECT logs.id, staging_table, staging_db, local_path\r\n" + "FROM `config` JOIN `logs`"
//				+ "WHERE logs.status = '" + Statuses.TRANSFORM_READY + "'";

		String sql = "SELECT * FROM `config`";
		try {
			Statement statement = Control.controlConn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				String stagingTable = rs.getString("staging_table");
				int stagingDB = rs.getInt("staging_db");
				String warehouseTable = rs.getString("warehouse_table");
				int warehouseDB = rs.getInt("warehouse_db");

				doTransform(stagingDB, stagingTable, warehouseDB, warehouseTable);

			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void doTransform(int staging_db, String staging_table, int warehouse_db, String warehouse_table)
			throws Exception {
		Connection stagingConn = DBConnector.getConnectionFormDB(staging_db);
		Connection warehouseConn = DBConnector.getConnectionFormDB(warehouse_db);
		Statement stagingStatement = stagingConn.createStatement();

		String sql = "SELECT * FROM " + staging_table;
		String insertSql = "INSERT INTO " + warehouse_table
				+ "(mssv,lastname,firstname,dob,class_id,class_name,phone,email,hometown,note) VALUES(?,?,?,?,?,?,?,?,?,?)";
		ResultSet rsStaging = stagingStatement.executeQuery(sql);
		while (rsStaging.next()) {
				PreparedStatement pStatement = warehouseConn.prepareStatement(insertSql);
				pStatement.setString(1, rsStaging.getString("mssv"));
				pStatement.setString(2, rsStaging.getString("lastname"));
				pStatement.setString(3, rsStaging.getString("firstname"));
				pStatement.setDate(4, java.sql.Date.valueOf(rsStaging.getString("dob"))); // yyyy-mm-dd
				pStatement.setString(5, rsStaging.getString("class_id"));
				pStatement.setString(6, rsStaging.getString("class_name"));
				pStatement.setString(7, rsStaging.getString("phone"));
				pStatement.setString(8, rsStaging.getString("email"));
				pStatement.setString(9, rsStaging.getString("hometown"));
				pStatement.setString(10, rsStaging.getString("note"));
				
				pStatement.executeUpdate();
		}
	}

//	private static boolean isExistInWarehouse() {
//		return false;
//	}

	public static void main(String[] args) throws Exception {
		String d = "1998-11-20";
		System.out.println(Date.valueOf(d));
	}

}
