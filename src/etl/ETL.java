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

		String sql = "SELECT * FROM `process` WHERE process.status = '" + ProcessStatus.QUEUED+"'";
		Statement statement = DBConnector.loadControlConnection().createStatement();
		ResultSet rs = statement.executeQuery(sql);
		while (rs.next()) {
			final int processID = rs.getInt("id");
			final int dataConfigID = rs.getInt("data_config_id");

			new Thread(() -> doETL(processID, dataConfigID)).start();
		}

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

				Connection stagingConn = DBConnector.getConnectionFormDB(stagingDB);
				Connection warehouseConn = DBConnector.getConnectionFormDB(warehouseDB);

				try {
					Extracter.doExtract(logID, filePath, stagingTable, stagingConn);
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
