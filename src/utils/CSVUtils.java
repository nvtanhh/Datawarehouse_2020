package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.naming.CommunicationException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class CSVUtils {

	public static String convertExcelToCSV(String fileName) throws CommunicationException {
		InputStream is = null;
		PrintWriter output = null;
		try {
			is = new FileInputStream(fileName);

			File file = new File(fileName + ".csv");
			file.createNewFile();
			file.setWritable(true);
			output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

			Workbook wb = WorkbookFactory.create(is);

			Sheet sheet = wb.getSheetAt(0);

			ArrayList<String> header = new ArrayList<String>();

			Row firstRow = sheet.getRow(0);
			for (int i = 0; i < firstRow.getLastCellNum(); i++) {
				if (firstRow.getCell(i).getCellType() == CellType.STRING) {
					header.add(firstRow.getCell(i).getRichStringCellValue().toString());
				}
			}

			// hopefully the first row is a header and has a full compliment of
			// cells, else you'll have to pass in a max (yuck)
			int maxCol = sheet.getRow(0).getLastCellNum();
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				int count = 0;
				// row.getFirstCellNum() and row.getLastCellNum() don't return the
				// first and last index when the first or last column is blank

				String buf = "";
				for (int i = 0; i < maxCol; i++) {

					Cell cell = row.getCell(i);
					if (i > 0) {
						buf += ",";
					}

					if (cell == null) {
						count++;
					} else {
						String v = null;
						switch (cell.getCellType()) {
						case STRING:
							v = cell.getRichStringCellValue().getString();
							if (v.contains("/") || v.contains("-")) {
								v = parseDate(v);
							}
							break;
						case NUMERIC:
							if (DateUtil.isCellDateFormatted(cell)) {
								SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd");
								v = formatter.format(cell.getDateCellValue());
							} else
								v = String.valueOf((int) cell.getNumericCellValue());
							break;
						case BOOLEAN:
							v = String.valueOf(cell.getBooleanCellValue());
							break;
						case FORMULA:
							if (cell.getCachedFormulaResultType() == CellType.NUMERIC) {
								v = String.valueOf((int) cell.getNumericCellValue());
							} else {
								v = cell.getRichStringCellValue().toString();
							}
							break;
						default:
							count++;
							break;
						}

						buf += toCSV(v);
					}
				}
				if (count < maxCol - 1 && !isHeaderName(buf)) {
					output.println(buf);
				}
				buf = "";
			}
			is.close();
			output.close();
			return file.getAbsolutePath();
		} catch (Exception e) {
			throw new CommunicationException(e.getMessage());
		}

	}

	/*
	 * </strong> Escape the given value for output to a CSV file. Assumes the value
	 * does not have a double quote wrapper.
	 * 
	 * @return
	 */
	public static String toCSV(String value) {

		String v = null;
		boolean doWrap = false;

		if (value != null) {
			v = value;
			if (v.contains("\"")) {
				v = v.replace("\"", "\"\""); // escape embedded double quotes
				doWrap = true;
			}

			if (v.contains(",") || v.contains("\n")) {
				doWrap = true;
			}
			if (doWrap) {
				v = "\"" + v + "\""; // wrap with double quotes to hide the comma
			}
		}
		return v;
	}

	public static String fillter(String src) throws CommunicationException {
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			File fileIn = new File(src);
			in = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn)));
			File fileOut = new File(src + ".temp");
			fileOut.createNewFile();
			out = new PrintWriter(new FileOutputStream(fileOut), true);
			String line = in.readLine();
			if (!isHeaderName(line)) { // remove header or not
				out.println(line.substring(line.indexOf(",") + 1));
			}

			while ((line = in.readLine()) != null) {
				out.println(line);
			}

			in.close();
			out.close();
			fileIn.delete();
			fileOut.renameTo(fileIn);
			return fileIn.getAbsolutePath();
		} catch (Exception e) {
			throw new CommunicationException(e.getMessage());
		}

	}

	private static boolean isHeaderName(String line) {
		String[] colName = { "stt", "mssv", "id", "firstname", "Họ lót", "tên", "lastname", "email" };
		String[] spliter = line.split(",");
		for (int i = 0; i < colName.length; i++) {
			for (int j = 0; j < spliter.length; j++) {
				if (spliter[j].toLowerCase().contains(colName[i]))
					return true;
			}
		}
		return false;
	}

	public static void main(String[] args) throws Exception {
//		String s = convertExcelToCSV(
//				"D:\\Development\\workspace\\school\\2019-2020-HK2\\DataWarehouse\\data\\raw\\17130132_Data.xlsx"); // ok
//		fillter(s);
//		convertTxtToCSV("data/raw/a.txt");
		System.out.println(parseDate("2/27/1997"));

	}

	public static String convertTxtToCSV(String src) throws CommunicationException {
		try {
			File fileIn = new File(src);
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn)));
			File fileOut = new File(src + ".csv");
			PrintWriter out = new PrintWriter(new FileOutputStream(fileOut), true);
			String line;
			while ((line = in.readLine()) != null) {
				line = line.replace("|", ",").replace("\t", ","); // Dilimiter
				line = reFormatDate(line);
				out.println(line);
			}
			in.close();
			out.close();
			return fileOut.getAbsolutePath();
		} catch (Exception e) {
			throw new CommunicationException(e.getMessage());
		}
	}

	private static String reFormatDate(String line) {
		String rs = "";
		String[] spliter = line.split(",");
		for (int i = 0; i < spliter.length; i++) {
			if (spliter[i].contains("/") || spliter[i].contains("-")) {
				String dateField = spliter[i].replace("/", "-");
				spliter[i] = parseDate(dateField);
			}
		}
		for (int i = 0; i < spliter.length; i++) {
			rs += spliter[i] + ",";
		}
		return rs.substring(0, rs.length() - 1);
	}

	public static String parseDate(String strDate) {
		if (strDate != null && !strDate.isEmpty()) {
			SimpleDateFormat[] formats = new SimpleDateFormat[] { new SimpleDateFormat("yyyy-MM-dd"),
					new SimpleDateFormat("yyyy/MM/dd"), new SimpleDateFormat("MM-dd-yyyy"),
					new SimpleDateFormat("MM/dd/yyyy"), new SimpleDateFormat("dd-MM-yyyy"),
					new SimpleDateFormat("dd/MM/yyyy") };
			SimpleDateFormat goal = new SimpleDateFormat("yyyy-MM-dd");
			Date parsedDate = null;
			for (int i = 0; i < formats.length; i++) {
				try {
					formats[i].setLenient(false);
					parsedDate = formats[i].parse(strDate);
					return goal.format(parsedDate);
				} catch (ParseException e) {
					continue;
				}
			}
		}
		return strDate;
	}

	public static int countField(String src) throws CommunicationException {
		if (src.isEmpty())
			throw new CommunicationException("Empty source");
		File fileIn = new File(src);
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn)));
			String[] spliter = in.readLine().split(",");
			in.close();
			return spliter.length;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

}
