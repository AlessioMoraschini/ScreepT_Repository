package network;

import java.io.File;
import java.net.ConnectException;

import org.junit.Test;

import gui.GuiUtils;
import gui.JOptionHelper;
import testconfig.TestConstants;
import utils.TestUtils;

public class TestFtpConnection {

	private static final String HOST = "ftp.am-design-development.com";
	private static final String USER = "";
	private static final String PSW = "";
	private static final int PORT = 22;
	private static final String FOLDER_ON_SERVER = "/www.am-design-development.com/ScrYpT/Updates";
	private static final String TEST_FILE_UPLOAD = "TEST_FTP_UPLOAD.txt";
//	private static final String TEST_FILE_UPLOAD = "A.txt";
	private static final String TEST_FILE_DOWNLOAD = TEST_FILE_UPLOAD;
//	private static final String TEST_FILE_DOWNLOAD = "TEST_FTP_UPLOAD.txt";
	private static final String DOWNLOAD_DESTINATION_LOCAL = "C:/Users/xx/Desktop/Downloads/" + TEST_FILE_DOWNLOAD;
	
	@Test
	public void testConnection() throws ConnectException {
		
		TestUtils.printHeader("#", 60, "TEST FTP CONNECTION");
		TestUtils.printl("HOST: " + HOST);
		TestUtils.printl("USER: " + USER);
		TestUtils.printl("PORT: " + PORT);
		
		String psw = new JOptionHelper(null).askForString("Insert password", "Insert password");
		TestUtils.printl(psw);
		
		FtpDumper dumper = new FtpDumper(HOST, USER, psw, PORT);
		
		TestUtils.printArrayAsLines(1,dumper.GetRemoteFiles(FOLDER_ON_SERVER));
		
//		UpdateProgressPanelMin progPanel = new UpdateProgressPanelMin(false);
//		progPanel.setUndefinedDuration();
//		progPanel.updateText("Uploading " + TestConstants.sourceFolderPath + TEST_FILE_UPLOAD);
		dumper.uploadFile(TestConstants.sourceFolderPath + TEST_FILE_UPLOAD, TEST_FILE_UPLOAD, FOLDER_ON_SERVER);
		new JOptionHelper(null).info(TestConstants.sourceFolderPath + TEST_FILE_UPLOAD + ": Uploaded!", "Uploaded");
		
//		progPanel.updateText("Downloading " + FOLDER_ON_SERVER + "/" + TEST_FILE_DOWNLOAD);
		File downloaded = dumper.downloadFile(FOLDER_ON_SERVER, TEST_FILE_DOWNLOAD, DOWNLOAD_DESTINATION_LOCAL, false);

//		progPanel.forceClose();
		new JOptionHelper(null).info("Finished! Result file: " + downloaded, "Downloaded");
		GuiUtils.openInFileSystem(new File(DOWNLOAD_DESTINATION_LOCAL));
		
		dumper.disconnect();
	}
}
