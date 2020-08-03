import download.Downloader;
import etl.ETL;

public class Run {
	public static void main(String[] args) throws Exception {
		try {
			if (args.length == 0) {
				System.out.println("missing input value");
			} else {
				if (args[0].equals("download")) {
					if (args.length == 2 && args[1] != null) {
						Downloader.startDowload(Integer.parseInt(args[1]));
					} else {
						Downloader.startDowload();
					}
				} else if (args[0].equals("etl")) {
					if (args.length == 2 && args[1] != null) {
						ETL.startETL(Integer.parseInt(args[1]));
					} else {
						ETL.startETL();
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

}
