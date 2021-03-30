package updates;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import resources.GeneralConfig;
import screept.general.utils.WindowCreator;
import various.common.light.gui.LightLicenseViewer;
import various.common.light.gui.LightLicenseViewer.UserLicenseChoice;
import various.common.light.network.utils.DownloadUtils;
import various.common.light.utility.hash.ChecksumHasher;
import various.common.light.utility.hash.HashingFunction;
import various.common.light.utility.log.SafeLogger;

public class LicenseCheckerTest {

	public static File LICENSE_FILE = new File("./TEST_FILES/License_ScreepT.html");
	public static SafeLogger logger = new SafeLogger(LicenseCheckerTest.class);

	@Test
	public void testLicenseCheck() {
		GeneralConfig.LICENSE_FILE = LICENSE_FILE;
		GeneralConfig.TEMP_DIRECTORY = "TEST_FILES/";
		LightLicenseViewer.MAIN_LIBRARIES_FOLDER_DETAIL = new File("./TEST_FILES/Libraries_licenses/detailedReport");
		LightLicenseViewer.MAIN_LIBRARIES_FOLDER = new File("TEST_FILES/Libraries_licenses");
		assertTrue(checkNewLicense());
	}

	public static boolean checkNewLicense() {

		try {
			File newLicense = DownloadUtils.downloadFile(false, new URL(GeneralConfig.WEB_UPDATES_LICENSE_URL), GeneralConfig.TEMP_DIRECTORY+"tempNewLicense.html", null);

			if (!ChecksumHasher.equalsFilesChecksum(newLicense, GeneralConfig.LICENSE_FILE, HashingFunction.SHA512)) {
				LightLicenseViewer licenseViewer = new WindowCreator(null).newLicenseFrame(newLicense, true, false, new boolean[] {true, false, false});
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
