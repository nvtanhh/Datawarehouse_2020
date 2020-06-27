package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

import run.Control;

public class MyLog {
	private int id;
	private int configID;
	private Timestamp downloadDT, extractStartDT, extractEndDT, transformStartDT, transformEndDT;
	private String status;
	private String comment;
	private String localPath;

	public MyLog() {
		super();
	}

	public MyLog(int config_id, Timestamp startDT, Timestamp endDT, String status, String cmt) {
//		super();
//		this.configID = config_id;
//		this.startDT = startDT;
//		this.endDT = endDT;
//		this.status = status;
//		this.comment = cmt;
	}

//	public void writetoDB() {
//		Connection connection = Control.controlConn;
//
//		String sql = "INSERT INTO logs (config_id , start_dt, end_dt, status, comment) VALUES (?,?,?,?,?)";
//
//		try {
//			PreparedStatement pStatement = connection.prepareStatement(sql);
//			pStatement.setInt(1, configID);
//			pStatement.setTimestamp(2, startDT);
//			pStatement.setTimestamp(3, endDT);
//			pStatement.setString(4, status);
//			pStatement.setString(5, comment);
//			pStatement.executeUpdate();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}

	public void commitDownload() {
		Connection connection = Control.controlConn;
		String sql = "INSERT INTO logs (config_id , download_dt, status, local_path) VALUES (?,?,?,?)";
		try {
			PreparedStatement pStatement = connection.prepareStatement(sql);
			pStatement.setInt(1, configID);
			pStatement.setTimestamp(2, downloadDT);
			pStatement.setString(3, status);
			pStatement.setString(4, localPath);
			pStatement.executeUpdate();
			pStatement.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	
	public void commitExtract() {
		Connection connection = Control.controlConn;
		String sql = "UPDATE `logs` SET extract_start_dt = ?, extract_end_dt = ?, status = ?, comment = ? WHERE id = ?";
		try {
			PreparedStatement pStatement = connection.prepareStatement(sql);
			pStatement.setTimestamp(1, this.extractStartDT);
			pStatement.setTimestamp(2, this.extractEndDT);
			pStatement.setString(3, this.status);
			pStatement.setString(4, this.comment);
			pStatement.setInt(5, this.id);
			pStatement.executeUpdate();
			pStatement.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

	public void setConfig_id(int config_id) {
		this.configID = config_id;
	}

	public int getConfigID() {
		return configID;
	}

	public void setConfigID(int configID) {
		this.configID = configID;
	}

	public Timestamp getDownloadDT() {
		return downloadDT;
	}

	public void setDownloadDT(Timestamp downloadDT) {
		this.downloadDT = downloadDT;
	}

	public Timestamp getExtractStartDT() {
		return extractStartDT;
	}

	public void setExtractStartDT(Timestamp extractStartDT) {
		this.extractStartDT = extractStartDT;
	}

	public Timestamp getExtractEndDT() {
		return extractEndDT;
	}

	public void setExtractEndDT(Timestamp extractEndDT) {
		this.extractEndDT = extractEndDT;
	}

	public Timestamp getTransformStartDT() {
		return transformStartDT;
	}

	public void setTransformStartDT(Timestamp transformStartDT) {
		this.transformStartDT = transformStartDT;
	}

	public Timestamp getTransformEndDT() {
		return transformEndDT;
	}

	public void setTransformEndDT(Timestamp transformEndDT) {
		this.transformEndDT = transformEndDT;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	

	

}
