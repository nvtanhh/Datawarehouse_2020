package download;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class DirWatcher extends Thread {
//	private Set<String> newFiles = new HashSet<String>();
	private Map<String, Timestamp> newFiles = new HashMap<String, Timestamp>();
//	private boolean start = false;
	private File folder;
	private boolean flag = false;
	private WatchService service;
	private WatchKey key;

	public DirWatcher(String src) {
		folder = new File(src);
		if (!folder.isDirectory()) {
			throw new IllegalArgumentException(src + " is not a folder");
		}
	}

	@Override
	public void run() {

		Path path = folder.toPath();

		flag = true;

		System.out.println("Watching folder " + folder.getName());

		// We obtain the file system of the Path
		FileSystem fs = path.getFileSystem();

		// We create the new WatchService using the new try() block
		try {
			service = fs.newWatchService();
			// We register the path to the service
			// We watch for creation events

			// Start the infinite polling loop
			key = path.register(service, ENTRY_CREATE, ENTRY_MODIFY);
			while (flag) {
				Kind<?> kind = null;
				for (WatchEvent<?> watchEvent : key.pollEvents()) {
					kind = watchEvent.kind();
					Path dir = (Path) key.watchable();
					String fullPath = dir.resolve((Path) watchEvent.context()).toAbsolutePath().toString();
//					Path newPath = ((WatchEvent<Path>) watchEvent).context();
					if (ENTRY_CREATE == kind || ENTRY_MODIFY == kind) {
						newFiles.put(fullPath, new Timestamp(new Date().getTime()));
					}
				}

				if (!key.reset()) {
					break; // loop
				}
			}

		} catch (IOException ioe) {
//			ioe.printStackTrace();
		}
	}

	public void stopRunning() {
		this.flag = false;
		try {
			key.cancel();
			this.service.close();
			this.interrupt();
		} catch (IOException e) {

		}
	}

	public Map<String, Timestamp> getNewFiles() {
		return newFiles;
	}
}