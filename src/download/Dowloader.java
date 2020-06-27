package download;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

import model.MyLog;
import model.Statuses;
import run.Control;

public class Dowloader {
	public static void startDowload() throws Exception {
		Statement statement = Control.controlConn.createStatement();
		String sql = "SELECT * FROM `config`";

		ResultSet rs = statement.executeQuery(sql);
		while (rs.next()) {
			int configID = rs.getInt("id");
			String hostname = rs.getString("url");
			int ssh_port = rs.getInt("ssh_port");
			String username = rs.getString("username");
			String passwd = rs.getString("password");
			String fileName = rs.getString("remote_dir");
			String storeAt = rs.getString("local_dir");
			String types = rs.getString("types");

			String[] extentions = { types };
			if (types.split(",").length != 0) {
				extentions = types.split(",");
			}

			SCPDownload scp = new SCPDownload(hostname, ssh_port, username, passwd);

			DirWatcher watcher = new DirWatcher(storeAt);
			watcher.start();
			scp.downloadFileByExtentions(extentions, fileName, storeAt, 6);
			// mode=6: Download newer and missing files or files with size differences.
			watcher.stopRunning();
			Map<String, Timestamp> newFiles = watcher.getNewFiles();
			Set<String> set = newFiles.keySet();
			for (String key : set) {
				MyLog log = new MyLog();
				log.setConfig_id(configID);
				log.setDownloadDT(newFiles.get(key));
				log.setStatus(Statuses.EXTRACT_READY);
				log.setLocalPath(key);
				log.commitDownload();
			}
		}
		statement.close();
	}

}
