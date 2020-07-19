package utils;

public class FileExtentionUtils {
	public static boolean isExcel(String fileName) {
		return fileName.toLowerCase().endsWith(".xlsx") || fileName.endsWith(".xls");
	}

	public static String getExtention(String fileName) {
		try {
			return fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
		} catch (IndexOutOfBoundsException e) {
			return null;
		}

	}

	public static boolean isTxt(String src) {
		return src.endsWith(".txt");
	}

	public static boolean isCSV(String filePath) {
		return filePath.toLowerCase().endsWith(".csv");
	}

}
