/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package screept.gui.utils.dependencycheck;

import java.io.File;

import org.apache.log4j.Logger;

import com.sun.jna.Native;

import maingUI.MainGuiUtils;
import multimedia.conversion.Text2Speech;
import oshi.SystemInfo;
import resources.GeneralConfig;

public class LibChecker {
	
	// mainGUI logger
	static Logger logger = Logger.getLogger(LibChecker.class);
		
	public static boolean checkJnetPcap() {
		try {
			Class.forName("org.jnetpcap.Pcap");
			logger.info(" \n => JnetPcap library found! :)");
			return true;
		} catch (UnsatisfiedLinkError e) {
			logger.error("! JnetPcap dll link error ! \n\n", e);
			return false;
		} catch (NoClassDefFoundError e) {
			logger.error("! JnetPcap class def missing ! \n\n", e);
			return false;
		} catch (Throwable throwable){
			logger.error("! JnetPcap class not found ! \n\n", throwable);
			return false;
		}
	}

	public static boolean checkVoiceSpeaker() {
		boolean libFound = false;
		try {
			Text2Speech objSpeaker = new Text2Speech();
			objSpeaker.dospeak(" " , "kevin16", (0.5f), 0.5f);
			libFound = true;
			logger.info(" \n => voice speaker library found! :)");
		} catch (Throwable e) {
			libFound = false;
			logger.error("! Cannot run voice speaker: undefined library dependencies: Text2Speech ! \n\n", e);
		}
		return libFound;
	}

	public static boolean checkHardwareInfoDependencies() {
		boolean libFound = false;
		boolean oshiFound = false;
		
		if(!MainGuiUtils.checkJavaVersionMaxCompatibility()) {
			return false;
		}
		
		try {
			Native.setProtected(true);
			logger.info("JNA PROTECTED MODE ACTIVE -> " + Native.isProtected());
		} catch(Throwable e) {
			logger.warn("Cannot set native protected mode", e);
		}
		
		try {
			Class.forName("oshi.SystemInfo");
			new SystemInfo();
			oshiFound = true;

			libFound = oshiFound;
			logger.info(" \n => HardwareInfo Oshi libraries found! :)");
		} catch (Throwable e) {
			libFound = false;
			logger.error("! Cannot instantiate HardwareInfo: undefined library dependencies: "
					+ "\n-> Oshi found:[" + oshiFound + "]! \n\n", e);
		}
		return libFound;
	}

	public static boolean checkTesseract() {
		String tessdataLib = GeneralConfig.RESOURCES_DIR + "lib/tessdata/eng.traineddata";
		boolean tessDataFound = false;
		try {
			
			new multimedia.conversion.ImageToTextExtractor();
			
			if(new File(tessdataLib).exists()) {
				tessDataFound = true;
				logger.info(" \n => Tesseract found! :)");
			} else {
				tessDataFound = false;
				logger.warn(" \n => Tesseract NOT found!");
			}
		} catch (Throwable e) {
			tessDataFound = false;
			logger.error("! Cannot run tesseract: undefined library: "+tessdataLib+" ! \n\n", e);
		} 
		return tessDataFound;
	}
}
