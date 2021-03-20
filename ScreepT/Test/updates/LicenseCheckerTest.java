package updates;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import general.utils.WindowCreator;
import gui.LightLicenseViewer;
import gui.LightLicenseViewer.UserLicenseChoice;
import network.utils.DownloadUtils;
import resources.GeneralConfig;
import utility.hash.ChecksumHasher;
import utility.hash.HashingFunction;
import utility.log.SafeLogger;

public class LicenseCheckerTest {

	public static File LICENSE_FILE = new File("TEST_FILES/License_ScreepT.html");
	public static SafeLogger logger = new SafeLogger(LicenseCheckerTest.class);
	
	@Test
	public void testLicenseCheck() {
		GeneralConfig.LICENSE_FILE = LICENSE_FILE;
		GeneralConfig.TEMP_DIRECTORY = "TEST_FILES/";
		LightLicenseViewer.MAIN_LIBRARIES_FOLDER = new File("TEST_FILES/Libraries_licenses");
		assertTrue(checkNewLicense());
	}
	
	public static boolean checkNewLicense() {
		
		try {
			File newLicense = DownloadUtils.downloadFile(false, new URL(GeneralConfig.WEB_UPDATES_LICENSE_URL), GeneralConfig.TEMP_DIRECTORY+"tempNewLicense.html", null);
			
			if (!ChecksumHasher.equalsFilesChecksum(newLicense, GeneralConfig.LICENSE_FILE, HashingFunction.SHA512)) {
				LightLicenseViewer licenseViewer = new WindowCreator(null).newLicenseFrame(newLicense, true, false);
				return UserLicenseChoice.ACCEPTED.equals(licenseViewer.userChoice);
			} else {
				return true;
			}
		} catch (Throwable e) {
			logger.error("An error occurred while comparing license!", e);
			return true;
		}
	}
}
