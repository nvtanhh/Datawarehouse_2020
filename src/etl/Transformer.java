package etl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import dao.DBConnector;
import model.LogStatus;
import model.MyLog;

public class Transformer {
	public static void doTransform(String stagingTable, Connection stagingConn, String warehouseTable,
			Connection warehouseConn, String targetFields, int processConfigID, String sourcesType, int logID)
			throws SQLException {
		Timestamp startDT = new Timestamp(new Date().getTime()); // thời gian bắt đầu
		try {
			String callQuery = buildCallQuery(processConfigID); // gán phương thức
			CallableStatement callStatement = warehouseConn.prepareCall(callQuery); // thực thi phương thức
			warehouseConn.setAutoCommit(false); // dữ liệu nháp chưa lưu vào db
			String sql = "SELECT * FROM `" + stagingTable + "`"; // sql load data từ staging
			Statement stagingStatement = stagingConn.createStatement();
			ResultSet rs = stagingStatement.executeQuery(sql);
			int rowAffected = 0; // các dòng đã được load

			if (sourcesType.equals("F")) { // if data type is fact
				while (rs.next()) { // duyệt từng dòng dữ liệu trong table staging
					// gán các trường vào mảng và các trường cách nhau dấu phẩy
					String[] spliter = targetFields.split(",");
					try {
						for (int i = 0; i < spliter.length; i++) {
							callStatement.setString(i + 1, rs.getString(spliter[i])); // lấy dữ liệu
						}
						callStatement.executeQuery();
					} catch (Exception e) { // bỏ qua nếu dữ liệu load sai và tiếp tục load
						continue;
					}
					warehouseConn.commit(); // lưu dữ liệu load đúng vào trong db dw_warehouse
					rowAffected++;
				}
			} else if (sourcesType.equals("D")) { // if data type is dim
				try {
					while (rs.next()) { // duyệt từng dòng dữ liệu trong table staging
						// gán các trường vào mảng và các trường cách nhau dấu phẩy
						String[] spliter = targetFields.split(",");
						for (int i = 0; i < spliter.length; i++) {
							callStatement.setString(i + 1, rs.getString(spliter[i]));
						}
						callStatement.executeQuery();
						rowAffected++;
					}
					warehouseConn.commit(); // lưu dữ liệu vào trong db dw_warehouse
				} catch (Exception e) { // xóa hết dữ liệu nếu load sai
					warehouseConn.rollback();
					rowAffected = 0;
				}
			}
			// load thành công thì ghi log
			MyLog log = new MyLog();
			log.setId(logID);
			log.setTransformStartDT(startDT);
			log.setTransformEndDT(new Timestamp(new Date().getTime()));
			log.setStatus(LogStatus.SUCCESS);
			log.setComment("Load " + rowAffected + " records to " + warehouseTable + " successfully");
			log.commitTransform();

		} catch (Exception e) {
			// load thất bại thì ghi log
			MyLog log = new MyLog();
			log.setId(logID);
			log.setTransformStartDT(startDT);
			log.setTransformEndDT(new Timestamp(new Date().getTime()));
			log.setStatus(LogStatus.ERROR);
			log.setComment(e.getMessage());
			log.commitTransform();
		}

	}

	private static String buildCallQuery(int processConfigID) throws SQLException {
		Statement connStatement = DBConnector.loadControlConnection().createStatement(); // kết nối controldb
		String sql = "SELECT * FROM `process_config` WHERE id = " + processConfigID; // spl load dữ liệu table config
		ResultSet rs = connStatement.executeQuery(sql);
		String callquery = "call ";
		// lấy store procedure từ table process_config với field name là
		// process_function
		if (rs.next()) {
			callquery += rs.getString("process_function"); // lấy String add_student(?,?,?,?)
		}
		connStatement.close();
		return callquery;
	}

}
