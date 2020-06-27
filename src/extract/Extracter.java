package extract;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import model.MyLog;
import model.Statuses;
import run.Control;
import utils.CSVUtils;
import utils.FileExtentionUtils;

public class Extracter {

	public static void extract2Staging() {
		String sql = "SELECT logs.id, staging_table, staging_db, local_path\r\n" + "FROM `config` JOIN `logs`"
				+ "WHERE logs.status = '" + Statuses.EXTRACT_READY + "'";
		try {
			Statement statement = Control.controlConn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				int logID = rs.getInt("id");
				String stagingTable = rs.getString("staging_table");
				int stagingDB = rs.getInt("staging_db");
				String filePath = rs.getString("local_path");

				doExtract(logID, filePath, stagingTable, stagingDB);
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private static void doExtract(int logID, String filePath, String stagingTable, int stagingDB) {
		MyLog log = null;
		String tempCsvFile = null;

		try {
			if (FileExtentionUtils.isExcel(filePath)) {
				tempCsvFile = CSVUtils.convertExcelToCSV(filePath);
			}

			if (FileExtentionUtils.isTxt(filePath) || FileExtentionUtils.isCSV(filePath)) {
				tempCsvFile = CSVUtils.convertTxtToCSV(filePath);
			}
			tempCsvFile = CSVUtils.fillter(tempCsvFile); // .csv

			if (tempCsvFile != null && CSVUtils.countField(tempCsvFile) > 8) {
				log = ImportCSV.importCSVtoDB(tempCsvFile.replace("\\", "\\\\"), stagingTable, stagingDB);
				log.setId(logID);
				log.commitExtract();
				delete(tempCsvFile);

				return;
			} else {
				log = new MyLog();
				log.setId(logID);
				log.setExtractStartDT(new Timestamp(new Date().getTime()));
				log.setStatus(Statuses.ERROR);
				log.setComment("Not enough fields");
				log.commitExtract();

				delete(tempCsvFile);
				return;
			}
		} catch (Exception e) {
			delete(tempCsvFile);

			log = new MyLog();
			log.setId(logID);
			log.setExtractStartDT(new Timestamp(new Date().getTime()));
			log.setStatus(Statuses.ERROR);
			log.setComment(e.getMessage());
			log.commitExtract();
		}
	}

	private static void delete(String src) {
		if (src == null || src.isEmpty())
			return;
		try {
			File file = new File(src);
			Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
		}
	}

	public static void main(String[] args) throws Exception {
		Control.loadControlConnection();
		doExtract(385,
				"D:\\Development\\workspace\\school\\2019-2020-HK2\\DataWarehouse\\data\\raw\\16130373_chieu_nhom15.xlsx",
				"student_staging1", 1);
	}

}
