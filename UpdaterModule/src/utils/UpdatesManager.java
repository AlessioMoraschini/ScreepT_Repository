package utils;

import static testUtils.TestUtils.getHeaderString;
import static testUtils.TestUtils.printl;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import gui.UpdateProgressPanelMin;
import resources.GeneralConfig;
import resources.PropertiesManager;

public class UpdatesManager {

	public URL manifestRemoteUrl;
	
	public final String localTempManifestName = "tempManifest.properties";
	public final String localTempManifestPath = GeneralConfig.UPDATES_DIR + "tempManifest.properties";
	public final String localTempZippedUpdates = GeneralConfig.UPDATES_DIR + GeneralConfig.WEB_UPDATES_PACK_NAME;
	public String currentVersion = "";
	public AtomicBoolean updatesReadyToInstall = new AtomicBoolean();
	
	public File[] lastUpdatesDownloaded;
	
	public UpdatesManager(URL manifestRemote, String currentVersion) {
		this.manifestRemoteUrl = manifestRemote;
		this.currentVersion = currentVersion;
		
		try {
			File updatesDir = new File(GeneralConfig.UPDATES_DIR);
			if(!updatesDir.exists() && updatesDir.isDirectory()) {
				updatesDir.mkdirs();
				updatesDir.mkdir();
			}
		} catch (Exception e) {
			printl("Cannot create updates dir (Resources_Scrypt/UPDATES) : " + Arrays.deepToString(e.getStackTrace()));
		}
	}
	
	/**
	 * @return null if current is updated, else it return the newest version downloadable
	 * @throws Exception 
	 */
	public String lookForUpdates() throws Exception {
		String newVersionAvailable = null;
		
		if(DownloadUtils.isUrlReachable(manifestRemoteUrl)) {
			printl(getHeaderString("#", 40, "DOWNLOAD AND PARSE MANIFEST .properties"));
			UpdateProgressPanelMin updateManagerProgbar = new UpdateProgressPanelMin(true);
			updateManagerProgbar.getPb().setIndeterminate(true); 
			File downloadedManifest = DownloadUtils.downloadFile(manifestRemoteUrl, localTempManifestPath, updateManagerProgbar);
			printl("Manifest Downloaded!! Size of " + manifestRemoteUrl + " is: " + Various.coolFileSize(downloadedManifest.length()));
			
			PropertiesManager propsManager = new PropertiesManager(downloadedManifest);
			
			String version = propsManager.getProperty(GeneralConfig.WEB_UPDATER_VERSION_PROP);
			printl("Version: " + version);
			
			newVersionAvailable = (DownloadUtils.checkNewVersion(version, currentVersion)) ? version : null;
		} 
		
		return newVersionAvailable;
	}
	
	public boolean udpdatesReachable() {
		return DownloadUtils.isUrlReachable(manifestRemoteUrl);
	}
	
	public boolean emtpyUpdatesDir() {
		File updatesDir = new File(GeneralConfig.UPDATES_DIR);
		return Various.deleteDirContentRecursive(updatesDir);
	}
	
	/**
	 * Returns an array of two elements: first is jar updater, second is updates extracted folder reference
	 */
	public File[] downloadAndExtractUpdates() throws Exception {
		
		emtpyUpdatesDir();
		
		if(GeneralConfig.UPDATES_DIR == null || !new File(GeneralConfig.UPDATES_DIR).isDirectory()) {
			return null;
		}
		
		File[] output = new File[3];
		UpdateProgressPanelMin updateProgressBar = new UpdateProgressPanelMin(true);
		
		updateProgressBar.update(20, "Downloading "+manifestRemoteUrl, true);
		printl(getHeaderString("#", 40, "DOWNLOAD AND PARSE MANIFEST .properties"));
		File downloadedManifest = DownloadUtils.downloadFile(manifestRemoteUrl, localTempManifestPath, updateProgressBar);
		printl("Manifest Downloaded!! Size of " + manifestRemoteUrl + " is: " + Various.coolFileSize(downloadedManifest.length()));
		
		
		PropertiesManager propsManager = new PropertiesManager(downloadedManifest);
		
		String version = propsManager.getProperty(GeneralConfig.WEB_UPDATER_VERSION_PROP);
		printl("Version: " + version);
		output[2] = new File("ScrYpT_"+version+".jar");
		
		URL updaterJarUrl = new URL(propsManager.getProperty(GeneralConfig.WEB_UPDATER_JAR_PROP));
		printl("updaterJarUrl: " + updaterJarUrl);
		
		URL updatesZippedUrl = new URL(propsManager.getProperty(GeneralConfig.WEB_UPDATES_PACK_PROP));
		printl("updatesZippedUrl: " + updatesZippedUrl);
		
		// Download jar updater into same root as main application that is downloading updates
		updateProgressBar.update(40, "Downloading "+updaterJarUrl, true);
		printl(getHeaderString("#", 40, "PARSE AND DOWNLOAD PACKETS"));
		File downloadedUpdater = DownloadUtils.downloadFile(updaterJarUrl, GeneralConfig.WEB_UPDATER_JAR_NAME, updateProgressBar);
		output[0] = downloadedUpdater;
		printl("Updater jar Downloaded!! Size of " + updaterJarUrl + " is: " + Various.coolFileSize(downloadedUpdater.length()));

		// Download zipped updates into UPDATES folder
		updateProgressBar.update(70, "Downloading "+updaterJarUrl, true);
		File downloadedUpdatesZip = DownloadUtils.downloadFile(updatesZippedUrl, localTempZippedUpdates, updateProgressBar);
		printl("Updates ZIP Downloaded!! Size of " + updatesZippedUrl + " is: " + Various.coolFileSize(downloadedUpdatesZip.length()));
		
		updateProgressBar.forceClose();
		
		// Extract zipped updates into UPDATES_DIR destination folder "updates"
		File unzippedUpdates = DownloadUtils.unzipFile(downloadedUpdatesZip.getAbsolutePath(), GeneralConfig.UPDATES_DIR, true);
		printl("Unzipped updates: " + unzippedUpdates);
		output[1] = unzippedUpdates;
		
		lastUpdatesDownloaded = output;
		
		// Return the reference of updater application jar file
		return output;
	}
	
	public File[] getLastUpdates() {
		try {
			if (lastUpdatesDownloaded == null || lastUpdatesDownloaded.length < 2 || !lastUpdatesDownloaded[0].exists() || !lastUpdatesDownloaded[1].exists()) {
				return null;
			} else {
				return lastUpdatesDownloaded;
			} 
		} catch (Exception e) {
			return null;
		}
	}
	
}
