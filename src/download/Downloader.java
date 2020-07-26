package download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import dao.DBConnector;
import model.LogStatuses;
import model.MyLog;
import utils.FileExtentionUtils;

public class Downloader {


	public static void startDowload(int id) throws Exception {
		Statement statement = DBConnector.loadControlConnection().createStatement();
		String sql = "SELECT * FROM `config` WHERE id = " + id;

		ResultSet rs = statement.executeQuery(sql);
		if (rs.next()) {
			int configID = rs.getInt("id");
			String host = rs.getString("host");
			int port = rs.getInt("port");
			String userName = rs.getString("username");
			String password = rs.getString("password");
			String remoteDir = rs.getString("remote_dir");
			String localDir = rs.getString("local_dir");
			String types = rs.getString("file_extentions");
			String regex = rs.getString("file_regex").replace("\\\\", "\\");
			String sourcesStatus = rs.getString("sources_status");

			if (sourcesStatus.equals("REMOTE")) {
				File folder = new File(localDir);
				if (!folder.exists())
					folder.mkdirs();
				SSHManager instance = new SSHManager(userName, password, host, "", port);
				String errorMessage = instance.connect();
				if (errorMessage != null) {
					System.out.println(errorMessage);
					connectFailed(statement, configID);
				}
				String listFilesCmd = "ls " + remoteDir;
				String[] allFiles = instance.sendCommand(listFilesCmd).split("\n");

				ArrayList<String> filesNeedDownload = filter(allFiles, types, regex); // filter by extention and
																						// filename

				filesNeedDownload = checkSum(instance, filesNeedDownload, remoteDir, configID);
				for (int i = 0; i < filesNeedDownload.size(); i++) {
					String rfile = filesNeedDownload.get(i);
					String lfile = localDir + "/" + rfile.substring(rfile.lastIndexOf("/") + 1);
					instance.download(lfile, rfile);
					// Write log
					MyLog log = new MyLog();
					log.setConfig_id(configID);
					log.setStatus(LogStatuses.EXTRACT_READY);
					log.setDownloadDT(new Timestamp(new Date().getTime()));
					log.setFilePath(lfile);
					log.commitDownload();
				}
				instance.close();
			} else if (sourcesStatus.equals("LOCAL")) {
				File local = new File(localDir);
				if (!local.exists()) {
					MyLog log = new MyLog();
					log.setConfig_id(configID);
					log.setStatus(LogStatuses.ERROR);
					log.setDownloadDT(new Timestamp(new Date().getTime()));
					log.setFilePath(local.getAbsolutePath());
					log.setComment("File is not exists");
					log.commitDownload();
				} else {
					if (local.isFile()) {
						if (isCheckSumHasChange(local.getAbsolutePath(), configID)) {
							MyLog log = new MyLog();
							log.setConfig_id(configID);
							log.setStatus(LogStatuses.EXTRACT_READY);
							log.setDownloadDT(new Timestamp(new Date().getTime()));
							log.setFilePath(local.getAbsolutePath());
							log.commitDownload();
						}
					} else if (local.isDirectory()) {
						File[] files = local.listFiles();
						for (int i = 0; i < files.length; i++) {
							if (isCheckSumHasChange(local.getAbsolutePath(), configID)) {
								MyLog log = new MyLog();
								log.setConfig_id(configID);
								log.setStatus(LogStatuses.EXTRACT_READY);
								log.setDownloadDT(new Timestamp(new Date().getTime()));
								log.setFilePath(files[i].getAbsolutePath());
								log.commitDownload();
							}
						}
					}
				}
			}
			statement.close();
		}
	}

	private static boolean isCheckSumHasChange(String src, int configID) throws IOException, SQLException {
		String md5 = getHashedMd5(src);
		return hasChange(md5 + "  " + src, configID);  // 2 white space
	}

	private static String getHashedMd5(String src) throws IOException {
		try (InputStream is = Files.newInputStream(Paths.get(src))) {
			String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
			is.close();
			return md5;
		}
	}

	private static ArrayList<String> filter(String[] allFiles, String types, String fileRegex) {
		ArrayList<String> result = new ArrayList<String>();
		Pattern pattern = Pattern.compile(fileRegex);
		List<String> extentions = Arrays.asList(types.split(","));
		for (int i = 0; i < allFiles.length; i++) {
			String fileName = allFiles[i];
			if (extentions.contains(FileExtentionUtils.getExtention(fileName)) && pattern.matcher(fileName).matches()) {
				result.add(fileName);
			}
		}
		return result;
	}

	private static ArrayList<String> checkSum(SSHManager instance, ArrayList<String> filesNeedDownload,
			String remote_dir, int configID) throws SQLException {
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < filesNeedDownload.size(); i++) {
			String absolutePath = remote_dir + "/" + filesNeedDownload.get(i);
			String checkSumCmd = "md5sum " + absolutePath;
			String respone = instance.sendCommand(checkSumCmd);

			if (hasChange(respone, configID)) {
				result.add(absolutePath);
			}
		}
		return result;
	}

	private static boolean hasChange(String respone, int configID) throws SQLException {
		if (!respone.isEmpty()) {
			String[] spliter = respone.split("  "); // 2 white space
			Statement statement = DBConnector.loadControlConnection().createStatement();
			String sql = "SELECT * FROM `resources_control` WHERE remote_file = '" + spliter[1] + "' AND "
					+ "config_id = " + configID;
			ResultSet resultSet = statement.executeQuery(sql);
			if (resultSet.next()) {
				if (!resultSet.getString("checksum").equals(spliter[0])) {
					return true;
				} else
					return false;
			} else {
				sql = "INSERT INTO `resources_control` VALUES(default," + configID + ",'" + spliter[1] + "','"
						+ spliter[0] + "')";
				statement.executeUpdate(sql);
				return true;
			}
		}
		return false;
	}

	private static void connectFailed(Statement statement, int configID) throws SQLException {

		MyLog log = new MyLog();
		log.setConfig_id(configID);
		log.setStatus(LogStatuses.EXTRACT_READY);
		log.setDownloadDT(new Timestamp(new Date().getTime()));
		log.setComment("Connect Failed");
		log.commitDownload();
	}

	public static void main(String[] args) throws Exception {

//		startDowload(1);

		System.out.println(getHashedMd5("D:\\Development\\workspace\\school\\2019-2020-HK2\\DataWarehouse\\data\\sinhvien\\sinhvien_sang_nhom9.xlsx"));

	}

}
