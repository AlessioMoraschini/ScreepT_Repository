package utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import gui.UpdateProgressPanelMin;
import gui.Various;
import resources.GeneralConfig;
import resources.PropertiesManager;

import static testUtils.TestUtils.*;

public class TestDownload {

	public static final String downloadTestFolder = "./DownloadTest/";
	public static final File downloadTestFolderFile = new File(downloadTestFolder);
	public static URL urlManifestRemote;
	public static String tempManifestFilePath = downloadTestFolder + "tempManifest.properties";
	public static File tempManifestFile = new File(tempManifestFilePath);
	public static String tempUpdaterFilePath = downloadTestFolder + GeneralConfig.WEB_UPDATER_JAR_NAME;
	public static File tempUpdaterFile = new File(tempUpdaterFilePath);
	public static String tempUpdatesZipFilePath = downloadTestFolder + GeneralConfig.WEB_UPDATES_PACK_NAME;
	public static File tempUpdatesZipFile = new File(tempUpdatesZipFilePath);
	
	@Before
	public void initialize() throws IOException {
		urlManifestRemote = new URL("https://" + GeneralConfig.WEB_UPDATES_MANIFEST_URL);
		tempManifestFile.delete();
		tempManifestFile.createNewFile();
		tempUpdaterFile.delete();
		tempUpdaterFile.createNewFile();
		tempUpdatesZipFile.delete();
		tempUpdatesZipFile.createNewFile();
	}
	
	@Test
	public void testUrlFileSize() {
		int size = DownloadUtils.getFileSize(urlManifestRemote);
		printl(getHeaderString("#", 40, "TEST SIZE"));
		printl("Size of " + urlManifestRemote + " is: " + Various.coolFileSize((long)size));
		
	}

	@Test
	public void testUrlFileDownload() throws MalformedURLException {
		printl(getHeaderString("#", 40, "TEST DOWNLOAD AND PARSE MANIFEST .properties"));
		File downloadedManifest = DownloadUtils.downloadFile(urlManifestRemote, tempManifestFilePath, new UpdateProgressPanelMin());
		printl("Manifest Downloaded!! Size of " + urlManifestRemote + " is: " + Various.coolFileSize(downloadedManifest.length()));
		
		PropertiesManager propsManager = new PropertiesManager(downloadedManifest);
		
		String version = propsManager.getProperty(GeneralConfig.WEB_UPDATER_VERSION_PROP);
		printl("Version: " + version);
		URL updaterJarUrl = new URL(propsManager.getProperty(GeneralConfig.WEB_UPDATER_JAR_PROP));
		printl("updaterJarUrl: " + updaterJarUrl);
		URL updatesZippedUrl = new URL(propsManager.getProperty(GeneralConfig.WEB_UPDATES_PACK_PROP));
		printl("updatesZippedUrl: " + updatesZippedUrl);
		
		printl(getHeaderString("#", 40, "TEST PARSE AND DOWNLOAD PACKETS"));
		File downloadedUpdater = DownloadUtils.downloadFile(updaterJarUrl, tempUpdaterFilePath, new UpdateProgressPanelMin());
		printl("Updater jar Downloaded!! Size of " + updaterJarUrl + " is: " + Various.coolFileSize(downloadedUpdater.length()));

		File downloadedUpdatesZip = DownloadUtils.downloadFile(updatesZippedUrl, tempUpdatesZipFilePath, new UpdateProgressPanelMin());
		printl("Updates ZIP Downloaded!! Size of " + updatesZippedUrl + " is: " + Various.coolFileSize(downloadedUpdatesZip.length()));
		
	}
}
