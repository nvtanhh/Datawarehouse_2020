package utils;

public class FileExtentionUtils {
	public static boolean isExcel(String fileName) {
		return fileName.toLowerCase().endsWith(".xlsx")||fileName.endsWith(".xls");
	}
	public static String getExtention(String fileName) {
		return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
	}
	public static boolean isTxt(String src) {
		return src.endsWith(".txt");
	}
	public static boolean isCSV(String filePath) {
		return filePath.toLowerCase().endsWith(".csv");
	}
	
}
