package etl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;

import javax.naming.CommunicationException;

import model.MyLog;
import model.LogStatus;
import utils.CSVUtils;
import utils.FileExtentionUtils;
import utils.ImportCSV;

public class Extracter {

	public static void doExtract(int logID, String filePath, String stagingTable, Connection stagingConn,
			String stagingFields) throws Exception {
		MyLog log = null;
		String tempCsvFile = null;

		Timestamp startDT = new Timestamp(new Date().getTime());

		try {
			if (FileExtentionUtils.isExcel(filePath)) {  // if file is excel convert it to "csv" format
				tempCsvFile = CSVUtils.convertExcelToCSV(filePath);
			}

			if (FileExtentionUtils.isTxt(filePath) || FileExtentionUtils.isCSV(filePath)) {  // if file format is text or csv convert it to "csv" format
				tempCsvFile = CSVUtils.convertTxtToCSV(filePath);
			}

		} catch (Exception e) {
			delete(tempCsvFile);
			log = new MyLog();
			log.setId(logID);
			log.setExtractStartDT(startDT);
			log.setExtractEndDT(new Timestamp(new Date().getTime()));
			log.setStatus(LogStatus.ERROR);
			log.setComment(e.getMessage());
			log.commitExtract();
			delete(tempCsvFile);
			throw new Exception();
		}

		try {
			log = ImportCSV.importCSVtoDB(tempCsvFile.replace("\\", "\\\\"), stagingTable, stagingConn, stagingFields); // import csv file to staging
			log.setId(logID);
			log.setExtractStartDT(startDT);
			log.commitExtract();
			delete(tempCsvFile);
			if (log.getStatus() == LogStatus.ERROR) {
				throw new Exception();
			}
		} catch (CommunicationException e) {
			delete(tempCsvFile);
			log = new MyLog();
			log.setId(logID);
			log.setExtractStartDT(startDT);
			log.setExtractEndDT(new Timestamp(new Date().getTime()));
			log.setStatus(LogStatus.ERROR);
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
