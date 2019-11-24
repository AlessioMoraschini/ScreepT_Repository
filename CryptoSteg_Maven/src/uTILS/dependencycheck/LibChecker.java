package uTILS.dependencycheck;

import java.io.File;

import org.apache.log4j.Logger;

import conversion.Text2Speech;
import resources.GeneralConfig;

public class LibChecker {
	
	// mainGUI logger
	static Logger logger = Logger.getLogger(LibChecker.class);
		
	public static boolean checkJnetPcap() {
		try {
			org.jnetpcap.Pcap.libVersion();
			return true;
		} catch (UnsatisfiedLinkError e) {
			logger.error("! JnetPcap dll link error ! \n\n", e);
			return false;
		} catch (NoClassDefFoundError e) {
			logger.error("! JnetPcap class def missing ! \n\n", e);
			return false;
		}
	}

	public static boolean checkVoiceSpeaker() {
		boolean libFound = false;
		Text2Speech objSpeaker;
		try {
			objSpeaker = new Text2Speech();
			objSpeaker.dospeak(" " , "kevin16", (0.5f));
			libFound = true;
		} catch (Throwable e) {
			libFound = false;
			logger.error("!Cannot run voice speaker: undefined library dependencies: Text2Speech! \n\n", e);
		} 
		return libFound;
	}
	
	public static boolean checkTesseract() {
		String tessdataLib = GeneralConfig.RESOURCES_DIR + "lib/tessdata/eng.traineddata";
		boolean tessDataFound = false;
		try {
			if(new File(tessdataLib).exists()) {
				tessDataFound = true;
			} else {
				tessDataFound = false;
			}
		} catch (Exception e) {
			tessDataFound = false;
			logger.error("!Cannot run tesseract: undefined library: "+tessdataLib+"! \n\n", e);
		} 
		return tessDataFound;
	}
}
