package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import dao.DBConnector;

public class Process {
	private int id, dataConfigID, processConfigID;
	private String status, comment;

	public int getProcessConfigID() {
		return processConfigID;
	}

	public void setProcessConfigID(int processConfigID) {
		this.processConfigID = processConfigID;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDataConfigID() {
		return dataConfigID;
	}

	public void setDataConfigID(int dataConfigID) {
		this.dataConfigID = dataConfigID;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void save() {
		Connection connection = DBConnector.loadControlConnection();
		String sql = "INSERT INTO process (data_config_id, process_config_id, status, comment) VALUES (?,?,?,?)";
		PreparedStatement pStatement = null;
		try {
			pStatement = connection.prepareStatement(sql);
			pStatement.setInt(1, dataConfigID);
			pStatement.setInt(2, processConfigID);
			pStatement.setString(3, status);
			pStatement.setString(4, comment);
			pStatement.executeUpdate();
			pStatement.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateStatuss(int processID, String status) {
		String sql = "UPDATE `process` SET process.status = '" + status + "' WHERE process.id = " + processID;
		Statement statement = null;
		try {
			statement = DBConnector.loadControlConnection().createStatement();
			statement.executeUpdate(sql);
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
