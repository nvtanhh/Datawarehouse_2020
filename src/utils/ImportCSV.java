package utils;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import javax.naming.CommunicationException;

import model.MyLog;
import model.LogStatus;

public class ImportCSV {
	static Timestamp endDT;

	public static MyLog importCSVtoDB(String src, String stagingTable, Connection stagingConn, String stagingFields)
			throws CommunicationException {
		String cmt = "";
		
		if (!TableHelpper.isExist(stagingConn, stagingTable)) {
			TableHelpper.createTable(stagingConn, stagingTable, stagingFields);
		}

		try {
//			stagingFields = TableHelpper.getCols(stagingConn, stagingTable);
			if (src != null && checkFields(stagingFields, src)) {
				String loadQuery = "LOAD DATA LOCAL INFILE '" + src + "' INTO TABLE " + stagingTable + ""
						+ " FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\\r\\n' (" + stagingFields
						+ ")";
				Statement stmt = stagingConn.createStatement();
				int rs = stmt.executeUpdate(loadQuery);
				stmt.close();

				endDT = new Timestamp(new Date().getTime());

				cmt = "Extract " + rs + " from '" + src + "' records into " + stagingConn.getCatalog() + "/"
						+ stagingTable;

				MyLog log = new MyLog();
				log.setExtractEndDT(endDT);
				log.setStatus(LogStatus.TRANSFORM_READY);
				log.setComment(cmt);

				return log;
			} else {
				MyLog log = new MyLog();
				log.setExtractEndDT(endDT);
				log.setStatus(LogStatus.ERROR);
				log.setComment("Not enough fields");
				return log;
			}
		} catch (Exception e) {
			throw new CommunicationException(e.getMessage());
		}
	}

	private static boolean checkFields(String which_column, String src) throws CommunicationException {
		return which_column.split(",").length == CSVUtils.countField(src);
	}

}
