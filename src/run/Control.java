package run;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

import dao.DBConnector;
import download.Dowloader;
import etl.Extracter;
import etl.Transformer;

public class Control {
	public static Connection controlConn;
	public static String watcher = "17130171@st.hcmuaf.edu.vn";

	public Control() throws Exception {
		loadControlConnection();

//		Dowloader.startDowload();

//		Extracter.extract2Staging();
//		
		Transformer.transform2warehouse();

	}


	public static void loadControlConnection() {
		try (FileInputStream f = new FileInputStream("src/config.properties")) {

			// load the properties file
			Properties pros = new Properties();
			pros.load(f);

			// assign db parameters
			String url = pros.getProperty("db.url");
			String user = pros.getProperty("db.user");
			String password = pros.getProperty("db.password");

			controlConn = DBConnector.getConnection(url, user, password);

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void main(String[] args) throws Exception {
		new Control();
	}

}
