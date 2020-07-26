package etl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;

import javax.naming.CommunicationException;

import model.MyLog;
import model.LogStatuses;
import utils.CSVUtils;
import utils.FileExtentionUtils;
import utils.ImportCSV;

public class Extracter {

	public static void doExtract(int logID, String filePath, String stagingTable, Connection stagingConn)
			throws Exception {
		MyLog log = null;
		String tempCsvFile = null;

		Timestamp startDT = new Timestamp(new Date().getTime());

		try {
			if (FileExtentionUtils.isExcel(filePath)) {
				tempCsvFile = CSVUtils.convertExcelToCSV(filePath);
			}

			if (FileExtentionUtils.isTxt(filePath) || FileExtentionUtils.isCSV(filePath)) {
				tempCsvFile = CSVUtils.convertTxtToCSV(filePath);
			}
		} catch (Exception e) {
			delete(tempCsvFile);
			log = new MyLog();
			log.setId(logID);
			log.setExtractStartDT(startDT);
			log.setExtractEndDT(new Timestamp(new Date().getTime()));
			log.setStatus(LogStatuses.ERROR);
			log.setComment(e.getMessage());
			log.commitExtract();
//			sendMail("error", logID, e.getMessage());
			throw new Exception();
		}

		try {
			log = ImportCSV.importCSVtoDB(tempCsvFile.replace("\\", "\\\\"), stagingTable, stagingConn);
			log.setId(logID);
			log.setExtractStartDT(startDT);
			log.commitExtract();
			delete(tempCsvFile);
			if (log.getStatus() == LogStatuses.ERROR) {
//					sendMail("error", logID, log.getComment());
				throw new Exception();
			}
		} catch (CommunicationException e) {
			delete(tempCsvFile);
			log = new MyLog();
			log.setId(logID);
			log.setExtractStartDT(startDT);
			log.setExtractEndDT(new Timestamp(new Date().getTime()));
			log.setStatus(LogStatuses.ERROR);
			log.setComment(e.getMessage());
			log.commitExtract();
		}

	}

	public static void delete(String src) {
		if (src == null || src.isEmpty())
			return;
		try {
			File file = new File(src);
			Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
		}
	}

}
