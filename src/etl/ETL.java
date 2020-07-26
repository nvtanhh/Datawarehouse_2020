package etl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import dao.DBConnector;
import model.LogStatus;
import model.Process;
import model.ProcessStatus;

public class ETL {
	public ETL() throws SQLException {

		String sql = "SELECT * FROM `process` WHERE process.status = '" + ProcessStatus.QUEUED
				+ "' OR process.status = '" + ProcessStatus.WAITTING + "'";

		Statement statement = DBConnector.loadControlConnection().createStatement();
		ResultSet rs = statement.executeQuery(sql);
		while (rs.next()) {
			final int processID = rs.getInt("id");
			if (checkParentProcess(processID)) { // hasn't implemented yet
				final int dataConfigID = rs.getInt("data_config_id");
				new Thread(() -> doETL(processID, dataConfigID)).start();
			} else {
				Process process = new Process();
				process.setDataConfigID(processID);
				process.setStatus(ProcessStatus.WAITTING);
				process.save();
			}
		}

	}

	private boolean checkParentProcess(int dataConfigID) {
//
//		String sql = "SELECT * FORM `config` JOIN `process_config` ON config.process_config_id = process_config.id WHERE config.id = "
//				+ dataConfigID;
//
//		try {
//			Connection conn = DBConnector.loadControlConnection();
//			Statement controlStatement = conn.createStatement();
//			ResultSet rs = controlStatement.executeQuery(sql);
//			String[] spliter = null;
//			if (rs.next()) {
//				spliter = rs.getString("parent_process_ids").split(",");
//			}
//			int[] parentIDs = new int[spliter.length];
//			for (int i = 0; i < spliter.length; i++) {
//				parentIDs[i] = Integer.parseInt(spliter[i]);
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		return true;
	}

	public void doETL(int processID, int dataConfigID) {
		Process.updateStatuss(processID, ProcessStatus.RUNNING);

		String sql = "SELECT * FROM `config` JOIN `logs` ON config.id = logs.config_id WHERE logs.status = '"
				+ LogStatus.EXTRACT_READY + "' AND config.id = " + dataConfigID;
		try {
			Statement statement = DBConnector.loadControlConnection().createStatement();
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				int logID = rs.getInt("logs.id");
				String filePath = rs.getString("file_path");
				String stagingTable = rs.getString("staging_table");
				int stagingDB = rs.getInt("staging_db");
				String targetFields = rs.getString("target_fields");
				String warehouseTable = rs.getString("warehouse_table");
				int warehouseDB = rs.getInt("warehouse_db");
				int processConfigID = rs.getInt("process_config_id");
				String sourcesType = rs.getString("sources_type");
				String stagingFields = rs.getString("staging_fields");

				Connection stagingConn = DBConnector.getConnectionFormDB(stagingDB);
				Connection warehouseConn = DBConnector.getConnectionFormDB(warehouseDB);

				try {
					Extracter.doExtract(logID, filePath, stagingTable, stagingConn, stagingFields);
				} catch (Exception e) {
					truncateTable(stagingTable, stagingDB);
					continue;
				}
				Transformer.doTransform(stagingTable, stagingConn, warehouseTable, warehouseConn, targetFields,
						processConfigID, sourcesType, logID);
				truncateTable(stagingTable, stagingDB);
				stagingConn.close();
				warehouseConn.close();
			}

			statement.close();
		} catch (SQLException e) {
			// QUEUE a new process in db
			Process process = new Process();
			process.setDataConfigID(processID);
			process.setStatus(ProcessStatus.ERROR);
			process.setComment(e.getMessage());
			process.save();
//			sent mail for notifycation
		}

		Process.updateStatuss(processID, ProcessStatus.SUCCESS);
	}

	private void truncateTable(String stagingTable, int stagingDB) throws SQLException {
		Statement stament = DBConnector.getConnectionFormDB(stagingDB).createStatement();
		String sql = "TRUNCATE TABLE " + stagingTable;
		stament.executeUpdate(sql);
		stament.close();
	}

	public static void main(String[] args) throws SQLException {
		new ETL();
	}
}
