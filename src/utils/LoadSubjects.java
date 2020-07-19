package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;

import javax.naming.CommunicationException;

import dao.DBConnector;
import etl.Extracter;
import etl.ImportCSV;

public class LoadSubjects {
	public static void main(String[] args) throws Exception {
		loadBefore2013();
//		loadAfter2014();

	}

	private static void loadBefore2013() throws CommunicationException {
		try {
			String filePath = "data/subjects/subjects1.xlsx";
			String tempCsvFile = CSVUtils.convertExcelToCSV(filePath);
			ImportCSV.importCSVtoDB(tempCsvFile.replace("\\", "\\\\"), "subjects", 2); // 2 is warehouse db
			Extracter.delete(tempCsvFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void loadAfter2014() throws Exception {
		String filePath = "data/subjects/subjects2.xlsx";
		String tempCsvFile = CSVUtils.convertExcelToCSV(filePath);

		Connection connection = DBConnector.getConnectionFormDB(2);
		BufferedReader in = new BufferedReader(new FileReader(tempCsvFile));
		String line = null;
		Statement statement = connection.createStatement();
		while ((line = in.readLine()) != null) {
			try {
				String sql = "INSERT INTO `subjects` VALUES (" + line + ") ON DUPLICATE KEY UPDATE active = VALUES(1)";
				statement.execute(sql);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		in.close();
		statement.close();
		connection.close();
	}

}
