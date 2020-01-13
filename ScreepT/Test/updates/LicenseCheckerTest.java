package updates;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import gui.LightLicenseViewer;
import gui.LightLicenseViewer.UserLicenseChoice;
import maingUI.WindowCreator;
import network.utils.DownloadUtils;
import resources.GeneralConfig;
import utility.SafeLogger;
import utility.hash.ChecksumHasher;
import utility.hash.HashingFunction;

public class LicenseCheckerTest {

	public static File LICENSE_FILE = new File("TEST_FILES/License_ScreepT.html");
	public static SafeLogger logger = new SafeLogger(LicenseCheckerTest.class);
	
	@Test
	public void testLicenseCheck() {
		GeneralConfig.LICENSE_FILE = LICENSE_FILE;
		assertTrue(checkNewLicense());
	}
	
	public static boolean checkNewLicense() {
		
		try {
			File newLicense = DownloadUtils.downloadFile(false, new URL(GeneralConfig.WEB_UPDATES_LICENSE_URL), GeneralConfig.TEMP_DIRECTORY+"tempNewLicense.html", null);
			
			if (!ChecksumHasher.equalsFilesChecksum(newLicense, GeneralConfig.LICENSE_FILE, HashingFunction.SHA512)) {
				LightLicenseViewer licenseViewer = new WindowCreator(null).newLicenseFrame(true, false);
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
