package etl;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import javax.naming.CommunicationException;

import dao.DBConnector;
import model.MyLog;
import model.Statuses;
import utils.TableHelpper;

public class ImportCSV {
	static Timestamp  endDT;

	public static MyLog importCSVtoDB(String src, String stagingTable, int stagingDB) throws CommunicationException {
		String cmt = "";

		try (Connection connection = DBConnector.getConnectionFormDB(stagingDB)) {

			String which_column = TableHelpper.getCols(connection, stagingTable);

			

			String loadQuery = "LOAD DATA LOCAL INFILE '" + src + "' INTO TABLE " + stagingTable + ""
					+ " FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\\r\\n' (" + which_column + ")";
			Statement stmt = connection.createStatement();
			int rs = stmt.executeUpdate(loadQuery);
			stmt.close();
			connection.close();

			endDT = new Timestamp(new Date().getTime());

			cmt = "Extract " + rs + " from '" + src + "' records into " + connection.getCatalog() + "/" + stagingTable;

			MyLog log = new MyLog();
			log.setExtractEndDT(endDT);
			log.setStatus(Statuses.TRANSFORM_READY);
			log.setComment(cmt);

			return log;
		} catch (Exception e) {
			throw new CommunicationException(e.getMessage());
		}
	}

//	private static void createTable(Connection connection, String into_table, String which_column) {
//		String[] columns = which_column.split(",");
//
//		String sql = "CREATE TABLE " + into_table + " (id INTEGER not NULL AUTO_INCREMENT, ";
//		for (int i = 0; i < columns.length; i++) {
//			sql += columns[i] + " VARCHAR(255), ";
//		}
//		sql += " PRIMARY KEY ( id ))";
//
//		Statement statement;
//		try {
//			statement = connection.createStatement();
//			statement.executeUpdate(sql);
//			System.out.println("CREATE TABLE " + into_table + " WITH " + which_column);
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//	}

}