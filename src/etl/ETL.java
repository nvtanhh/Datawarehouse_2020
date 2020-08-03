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

	public static void startETL() throws SQLException {

		String sql = "SELECT * FROM `process` WHERE process.status = '" + ProcessStatus.QUEUED
				+ "' OR process.status = '" + ProcessStatus.WAITTING + "'";

		Statement statement = DBConnector.loadControlConnection().createStatement();
		ResultSet rs = statement.executeQuery(sql);
		while (rs.next()) {
			final int processID = rs.getInt("id");
			if (checkParentProcess(processID)) {
				new Thread(() -> doETL(processID)).start();
			} else {
				Process.updateStatuss(processID, ProcessStatus.WAITTING);
			}
		}
	}

	public static void startETL(int id) throws SQLException {
		if (checkParentProcess(id)) {
			new Thread(() -> doETL(id)).start();
		} else {
			Process.updateStatuss(id, ProcessStatus.WAITTING);
		}
	}

	public ETL() throws SQLException {

	}

	private static boolean checkParentProcess(int dataConfigID) {

		String sql = "SELECT * FROM `process` JOIN `process_config` ON process_config.id = process.process_config_id WHERE process.id = "
				+ dataConfigID;

		try (Statement controlStatement = DBConnector.loadControlConnection().createStatement()) {
			ResultSet rs = controlStatement.executeQuery(sql);
			String[] parentIDs = null; // var use to store id's parent processes of this process
			if (rs.next()) {
				String processIDs = rs.getString("parent_process_ids");
				if (processIDs == null)
					return true;
				parentIDs = processIDs.split(",");
			} else {
				return false;
			}
			for (int i = 0; i < parentIDs.length; i++) {
				sql = "SELECT 1 FROM `process` WHERE process.process_config_id = " + Integer.parseInt(parentIDs[i])
						+ "  AND DATE(process.update_at) = CURDATE() AND process.status = '" + ProcessStatus.SUCCESS
						+ "'";
				rs = controlStatement.executeQuery(sql);
				if (!rs.next()) {
					return false;
				}
			}
			controlStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	public static void doETL(int processID) {
		Process.updateStatuss(processID, ProcessStatus.RUNNING);

		String sql = "SELECT * FROM `config` JOIN `logs` ON config.id = logs.config_id JOIN `process` ON process.data_config_id = config.id WHERE logs.status = '"
				+ LogStatus.EXTRACT_READY + "' AND process.id = " + processID;
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
			Process.updateStatuss(processID, ProcessStatus.SUCCESS);
			statement.close();
		} catch (SQLException e) {
			// QUEUE a new process in db
			Process process = new Process();
			process.setDataConfigID(processID);
			process.setStatus(ProcessStatus.ERROR);
			process.setComment(e.getMessage());
			process.save();
//			sent mail for notifycation
			return;
		}

	}

	private static void truncateTable(String stagingTable, int stagingDB) throws SQLException {
		Statement stament = DBConnector.getConnectionFormDB(stagingDB).createStatement();
		String sql = "TRUNCATE TABLE " + stagingTable;
		stament.executeUpdate(sql);
		stament.close();
	}

}
