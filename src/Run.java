import download.Downloader;
import etl.ETL;

public class Run {
	public static void main(String[] args) throws Exception {
		try {
			if (args.length == 0) {
				System.out.println("missing input value");
			} else {
				if (args[0].equals("download")) {
					int configID = Integer.parseInt(args[1]);
					Downloader.startDowload(configID);
				} else if (args[0].equals("etl")) {
					new ETL();
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

}
