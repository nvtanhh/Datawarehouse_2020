package download;

import com.chilkatsoft.*;

public class SCPDownload {
	private CkScp scp;
	private CkSsh ssh;

	static {
		try {
			System.loadLibrary("chilkat");
		} catch (UnsatisfiedLinkError e) {
			System.err.println("Native code library failed to load.\n" + e);
			System.exit(1);
		}
	}

	public SCPDownload(String hostname, int port, String username, String pass) {
		ssh = new CkSsh();
		CkGlobal glob = new CkGlobal();
		boolean success = glob.UnlockBundle("Anything for 30-day trial");
		if (success != true) {
			System.out.println(glob.lastErrorText());
			return;
		}

		// Connect to an SSH server:
		success = ssh.Connect(hostname, port);
		if (success != true) {
			System.out.println(ssh.lastErrorText());
			return;
		}

		// Wait a max of 5 seconds when reading responses..
		ssh.put_IdleTimeoutMs(5000);

		// Authenticate using login/password:
		success = ssh.AuthenticatePw(username, pass);
		if (success != true) {
			System.out.println(ssh.lastErrorText());
			return;
		}

		// Once the SSH object is connected and authenticated, we use it
		// in our SCP object.
		scp = new CkScp();

		success = scp.UseSsh(ssh);
		if (success != true) {
			System.out.println(scp.lastErrorText());
			return;
		}
	}

	public void DownloadFileByName(String remotePath, String localPath) {
		// /Users/chilkat/testApp/logs/test1.log
		remotePath = "/volume1/ECEP/song.nguyen/DW_2020/data/sinhvien_sang_nhom9.xlsx";
		localPath = "data/sinhvien_sang_nhom9.xlsx";
		boolean success = scp.DownloadFile(remotePath, localPath);
		if (success != true) {
			System.out.println(scp.lastErrorText());
			return;
		}
		System.out.println("SCP download file success.");
		disConnect();
	}

	
	
	public void downloadFileByExtentions(String[] extentions, String remoteDir, String localDir, int mode) {
		for (int i = 0; i < extentions.length; i++) {
			downloadFileByExtention(extentions[i], remoteDir, localDir, mode);
		}
	}

	private void downloadFileByExtention(String extention, String remoteDir, String localDir, int mode) {
		scp.put_HeartbeatMs(200);

		// Set the SyncMustMatch property to "*.pem" to download only .pem files
		scp.put_SyncMustMatch("*." + extention);

		// Download synchronization modes:
		// mode=0: Download all files
		// mode=1: Download all files that do not exist on the local filesystem.
		// mode=2: Download newer or non-existant files on local filesystem.
		// mode=3: Download only newer files.
		// If a file does not already exist on the local filesystem, it is not
		// downloaded from the server.
		// mode=5: Download only missing files or files with size differences.
		// mode=6: Same as mode 5, but also download newer files.

		// Do not recurse the remote directory tree. Only download files matching *.pem
		// from the given remote directory.
		boolean bRecurse = false;

		boolean success = scp.SyncTreeDownload(remoteDir, localDir, mode, bRecurse);
		if (success != true) {
			System.out.println(scp.lastErrorText());
			return;
		}

		System.out.println("SCP download matching success.");


	}

	private void disConnect() {
		ssh.Disconnect();
	}


}
