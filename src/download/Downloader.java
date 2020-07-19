package download;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import dao.DBConnector;
import model.MyLog;
import model.Statuses;
import run.Control;
import utils.FileExtentionUtils;

public class Downloader {

//	public static void startDowload() throws Exception {
//		Statement statement = Control.controlConn.createStatement();
//		String sql = "SELECT * FROM `config`";
//
//		ResultSet rs = statement.executeQuery(sql);
//		while (rs.next()) {
//			int configID = rs.getInt("id");
//			String hostname = rs.getString("url");
//			int ssh_port = rs.getInt("ssh_port");
//			String username = rs.getString("username");
//			String passwd = rs.getString("password");
//			String fileName = rs.getString("remote_dir");
//			String localDir = rs.getString("local_dir");
//			String types = rs.getString("types");
//
//			File folder = new File(localDir);
//			if (!folder.exists())
//				folder.mkdirs();
//
//			String[] extentions = { types };
//			if (types.split(",").length != 0) {
//				extentions = types.split(",");
//			}
//
//			SCPDownload scp = new SCPDownload(hostname, ssh_port, username, passwd);
//
//			DirWatcher watcher = new DirWatcher(localDir);
//			watcher.start(); // start watcher thread
//			scp.downloadFileByExtentions(extentions, fileName, localDir, 6);
//			// mode=6: Download newer and missing files or files with size differences.
//			watcher.stopRunning(); // stop watcher thread
//			Map<String, Timestamp> newFiles = watcher.getNewFiles();
//			Set<String> set = newFiles.keySet();
//			for (String key : set) {
//				MyLog log = new MyLog();
//				log.setConfig_id(configID);
//				log.setDownloadDT(newFiles.get(key));
//				log.setStatus(Statuses.EXTRACT_READY);
//				log.setLocalPath(key);
//				log.commitDownload();
//			}
//		}
//		statement.close();
//	}

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
			String remote_dir = rs.getString("remote_dir");
			String localDir = rs.getString("local_dir");
			String types = rs.getString("file_types");
			String regex = rs.getString("file_regex").replace("\\\\", "\\");

			File folder = new File(localDir);
			if (!folder.exists())
				folder.mkdirs();
			SSHManager instance = new SSHManager(userName, password, host, "", port);
			String errorMessage = instance.connect();
			if (errorMessage != null) {
				System.out.println(errorMessage);
				connectFailed();
			}
			String listFilesCmd = "ls " + remote_dir;
			String[] allFiles = instance.sendCommand(listFilesCmd).split("\n");

			ArrayList<String> filesNeedDownload = filter(allFiles, types, regex); // filter by extention and filename

			filesNeedDownload = checkSum(instance, filesNeedDownload, remote_dir, configID);
			for (int i = 0; i < filesNeedDownload.size(); i++) {

				instance.download(localDir, filesNeedDownload.get(i)); 
				// Write log
				MyLog log = new MyLog();
				log.setConfig_id(configID);
				log.setStatus(Statuses.EXTRACT_READY);
				log.setDownloadDT(new Timestamp(new Date().getTime()));
				log.setFilePath(filesNeedDownload.get(i));
				log.commitDownload(); 

			}
			instance.close();
		}
		statement.close();
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

	private static void connectFailed() {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws Exception {

		startDowload(1);

//		String n = "sinhvien_chieu_nhom15.xlsx";
//		Pattern pattern = Pattern.compile("^sinhvien_(chieu|sang)_nhom[0-9]{2}\\..*");
//		System.out.println(pattern.matcher(n).matches());

	}

}
