package etl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import dao.DBConnector;
import model.LogStatuses;

public class ETL {
	public ETL() {
		

		String sql = "SELECT * FROM `config` JOIN `logs` ON config.id = logs.config_id WHERE logs.status = '"
				+ LogStatuses.EXTRACT_READY + "'";
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
				int processID = rs.getInt("process_id");
				String sourcesType = rs.getString("sources_type");

				Connection stagingConn = DBConnector.getConnectionFormDB(stagingDB);
				Connection warehouseConn = DBConnector.getConnectionFormDB(warehouseDB);

				try {
					Extracter.doExtract(logID, filePath, stagingTable, stagingConn);
				} catch (Exception e) {
					truncateTable(stagingTable, stagingDB);
					continue;
				}
				Transformer.doTransform(stagingTable, stagingConn, warehouseTable, warehouseConn, targetFields, processID,
						sourcesType, logID);
				truncateTable(stagingTable, stagingDB);
				stagingConn.close();
				warehouseConn.close();
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	private void truncateTable(String stagingTable, int stagingDB) throws SQLException {
		Statement stament = DBConnector.getConnectionFormDB(stagingDB).createStatement();
		String sql = "TRUNCATE TABLE " + stagingTable;
		stament.executeUpdate(sql);
		stament.close();
	}


	


	public static void main(String[] args) {
		new ETL();
	}
}
