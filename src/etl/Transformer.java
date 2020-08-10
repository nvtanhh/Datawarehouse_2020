package etl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import dao.DBConnector;
import model.LogStatus;
import model.MyLog;

public class Transformer {
	public static void doTransform(String stagingTable, Connection stagingConn, String warehouseTable,
			Connection warehouseConn, String targetFields, int processConfigID, String sourcesType, int logID)
			throws SQLException {
		Timestamp startDT = new Timestamp(new Date().getTime());
		try {
			String callQuery = loadCallQuery(processConfigID);
			CallableStatement callStatement = warehouseConn.prepareCall(callQuery);
			warehouseConn.setAutoCommit(false);
			String sql = "SELECT * FROM `" + stagingTable + "`";
			Statement stagingStatement = stagingConn.createStatement();
			ResultSet rs = stagingStatement.executeQuery(sql);
			int rowAffected = 0;

			if (sourcesType.equals("F")) { // if data type is fact
				while (rs.next()) {
					String[] spliter = targetFields.split(",");
					for (int i = 0; i < spliter.length; i++) {
						callStatement.setString(i + 1, rs.getString(spliter[i]));
					}
					try {
						callStatement.executeQuery();

					} catch (Exception e) {
						continue;
					}
					warehouseConn.commit();
					rowAffected++;
				}
			} else if (sourcesType.equals("D")) { // if data type is dim
				try {
					while (rs.next()) {
						String[] spliter = targetFields.split(",");
						for (int i = 0; i < spliter.length; i++) {
							callStatement.setString(i + 1, rs.getString(spliter[i]));
						}
						callStatement.executeQuery();
						rowAffected++;
					}
					warehouseConn.commit();
				} catch (Exception e) {
					warehouseConn.rollback();
					rowAffected = 0;
				}
			}

			MyLog log = new MyLog();
			log.setId(logID);
			log.setTransformStartDT(startDT);
			log.setTransformEndDT(new Timestamp(new Date().getTime()));
			log.setStatus(LogStatus.SUCCESS);
			log.setComment("Load " + rowAffected + " records to " + warehouseTable + " successfully");
			log.commitTransform();

		} catch (Exception e) {
			MyLog log = new MyLog();
			log.setId(logID);
			log.setTransformStartDT(startDT);
			log.setTransformEndDT(new Timestamp(new Date().getTime()));
			log.setStatus(LogStatus.ERROR);
			log.setComment(e.getMessage());
			log.commitTransform();
		}

	}

	private static String loadCallQuery(int processConfigID) throws SQLException {
		Statement connStatement = DBConnector.loadControlConnection().createStatement();
		String sql = "SELECT * FROM `process_config` WHERE id = " + processConfigID;
		ResultSet rs = connStatement.executeQuery(sql);
		String callquery = "call ";
		if (rs.next()) {
			callquery += rs.getString("process_function");
		}
		connStatement.close();
		return callquery;
	}

}
