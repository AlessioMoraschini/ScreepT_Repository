package updater;

import static java.lang.System.out;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import files.Various;
import gui.UpdateProgressPanelMin;
import main.MainStandAloneUpdater;
import network.DownloadUtils;
import om.exception.ProgressBarInterruptedException;
import utility.PropertiesManagerLight;

public class UpdatesManager {

	public URL manifestRemoteUrl;
	
	public static PropertiesManagerLight resourcesFilePathsMappings = new PropertiesManagerLight(MainStandAloneUpdater.resourcesMappingDefPath);
	public static String RESOURCES_DIR = resourcesFilePathsMappings.getProperty("RESOURCES_DIR", "Resources_Scrypt/");  
	public static String DEFAULT_LAUNCHER_INI_PATH = resourcesFilePathsMappings.getProperty("DEFAULT_LAUNCHER_INI_PATH", "LAUNCHER/Launcher_Configuration.ini");
	public static String UPDATES_DIR = resourcesFilePathsMappings.getProperty("UPDATES_DIR", "UPDATES/");
	public static String WEB_UPDATER_VERSION_PROP = resourcesFilePathsMappings.getProperty("WEB_UPDATER_VERSION_PROP", "version");                                                                                                               
	public static String WEB_UPDATES_PACK_NAME = resourcesFilePathsMappings.getProperty("WEB_UPDATES_PACK_NAME", "updates.zip");                                                                                                               
	public static String WEB_UPDATES_PACK_PROP = resourcesFilePathsMappings.getProperty("WEB_UPDATES_PACK_PROP", "updatesZippedUrl");                                                                                                               
	public static String WEB_UPDATER_JAR_NAME = resourcesFilePathsMappings.getProperty("WEB_UPDATER_JAR_NAME", "updater.jar");                                                                                                               
	public static String WEB_UPDATER_JAR_PROP = resourcesFilePathsMappings.getProperty("WEB_UPDATER_JAR_PROP", "updaterJarUrl");                                                                                                               
	
	public static  final String localTempManifestName = "tempManifest.properties";
	public final String localTempManifestPath = UPDATES_DIR + localTempManifestName;
	public final String localTempZippedUpdates = UPDATES_DIR + WEB_UPDATES_PACK_NAME;

	public String currentVersion = "";
	public AtomicBoolean updatesReadyToInstall = new AtomicBoolean();
	
	public File[] lastUpdatesDownloaded;
	
	public UpdatesManager(URL manifestRemote, String currentVersion) {
		this.manifestRemoteUrl = manifestRemote;
		this.currentVersion = currentVersion;
		
		try {
			File updatesDir = new File(UPDATES_DIR);
			if(!updatesDir.exists()) {
				updatesDir.mkdirs();
				updatesDir.mkdir();
			}
		} catch (Exception e) {
			out.println("Cannot create updates dir (Resources_Scrypt/UPDATES) : " + Arrays.deepToString(e.getStackTrace()));
		}
	}
	
	/**
	 * @return null if current is updated, else it return the newest version downloadable
	 * @throws Exception 
	 */
	public String lookForUpdates(boolean silent) throws Exception, TimeoutException {
		String newVersionAvailable = null;
		
		UpdateProgressPanelMin updateManagerProgbar = null;
		try {
			if(DownloadUtils.isUrlReachable(manifestRemoteUrl)) {
				if (!silent) {
					updateManagerProgbar = new UpdateProgressPanelMin(true);
					updateManagerProgbar.getPb().setIndeterminate(true);
				}
				
				out.println(Various.getHeaderString("#", 40, "DOWNLOAD AND PARSE MANIFEST .properties"));
				File downloadedManifest = DownloadUtils.downloadFile(silent, manifestRemoteUrl, localTempManifestPath, updateManagerProgbar);
				out.println("Manifest Downloaded!! Size of " + manifestRemoteUrl + " is: " + Various.coolFileSize(downloadedManifest.length()));
				
				PropertiesManagerLight propsManager = new PropertiesManagerLight(downloadedManifest);
				
				String version = propsManager.getProperty(WEB_UPDATER_VERSION_PROP);
				out.println("Version: " + version);
				
				newVersionAvailable = (DownloadUtils.checkNewVersion(version, currentVersion)) ? version : null;
			} else {
				throw new TimeoutException("Server unreachable");
			}
		} catch (Exception e) {
			out.println("An error occurred:" + Arrays.deepToString(e.getStackTrace()));
			updatesReadyToInstall.set(false);
			if(e instanceof ProgressBarInterruptedException ) {
				out.println("User stopped process:" + Arrays.deepToString(e.getStackTrace()));
				emtpyUpdatesDir();
				throw e;
			}
		} finally {
			if(updateManagerProgbar != null) {
				updateManagerProgbar.forceClose();
			}
		}
		
		return newVersionAvailable;
	}
	
	public boolean udpdatesReachable() {
		return DownloadUtils.isUrlReachable(manifestRemoteUrl);
	}
	
	public boolean emtpyUpdatesDir() {
		File updatesDir = new File(UPDATES_DIR);
		return Various.deleteDirContentRecursive(updatesDir);
	}
	
	/**
	 * Returns an array of two elements: first is jar updater, second is updates extracted folder reference
	 */
	public File[] downloadAndExtractUpdates() throws Exception, TimeoutException {
		
		emtpyUpdatesDir();
		
		lastUpdatesDownloaded = new File[3];
		
		UpdateProgressPanelMin updateProgressBar = null;
		try {
			if(UPDATES_DIR == null || !new File(UPDATES_DIR).isDirectory()) {
				return null;
			}
			
			updateProgressBar = new UpdateProgressPanelMin(true);
			updateProgressBar.update(20, "Downloading "+manifestRemoteUrl, true);
			out.println(Various.getHeaderString("#", 40, "DOWNLOAD AND PARSE MANIFEST .properties"));
			
			// Download manifest.properties into UPDATES -> /UPDATES/manifest.properties
			File downloadedManifest = DownloadUtils.downloadFile(false, manifestRemoteUrl, localTempManifestPath, updateProgressBar);
			out.println("Manifest Downloaded!! Size of " + manifestRemoteUrl + " is: " + Various.coolFileSize(downloadedManifest.length()));
			
			PropertiesManagerLight propsManager = new PropertiesManagerLight(downloadedManifest);
			
			String version = propsManager.getProperty(WEB_UPDATER_VERSION_PROP);
			out.println("Version: " + version);
			
			// Calculate jar main application reference after extract -> /UPDATES/ScrYpT_X.Y.Z.jar
			lastUpdatesDownloaded[2] = new File("ScrYpT_"+version+".jar");
			
			// READ FROM DOWNLOADED PROPS
			URL updaterJarUrl = new URL(propsManager.getProperty(WEB_UPDATER_JAR_PROP));
			out.println("updaterJarUrl: " + updaterJarUrl);
			
			URL updatesZippedUrl = new URL(propsManager.getProperty(WEB_UPDATES_PACK_PROP));
			out.println("updatesZippedUrl: " + updatesZippedUrl);
			
			// Download jar updater into root -> /application/updater.jar
			updateProgressBar.update(40, "Downloading "+updaterJarUrl, true);
			out.println(Various.getHeaderString("#", 40, "PARSE AND DOWNLOAD PACKETS"));
			File downloadedUpdater = DownloadUtils.downloadFile(false, updaterJarUrl, WEB_UPDATER_JAR_NAME, updateProgressBar);
			lastUpdatesDownloaded[0] = downloadedUpdater;
			out.println("Updater jar Downloaded!! Size of " + updaterJarUrl + " is: " + Various.coolFileSize(downloadedUpdater.length()));

			// Download zipped updates -> /UPDATES/updates.zip
			updateProgressBar.update(70, "Downloading "+updaterJarUrl, true);
			File downloadedUpdatesZip = DownloadUtils.downloadFile(false, updatesZippedUrl, localTempZippedUpdates, updateProgressBar);
			out.println("Updates ZIP Downloaded!! Size of " + updatesZippedUrl + " is: " + Various.coolFileSize(downloadedUpdatesZip.length()));
			
			updateProgressBar.forceClose();
			
			// Extract zipped updates -> /UPDATES/updates/
			File unzippedUpdates = DownloadUtils.unzipFile(downloadedUpdatesZip.getAbsolutePath(), UPDATES_DIR, true);
			out.println("Unzipped updates: " + unzippedUpdates);
			lastUpdatesDownloaded[1] = unzippedUpdates;
		
		} catch (Exception e) {
			out.println("An error occurred!" + Arrays.deepToString(e.getStackTrace()));
			updatesReadyToInstall.set(false);
			if (e instanceof ProgressBarInterruptedException) {
				out.println("User stopped process:" + Arrays.deepToString(e.getStackTrace()));
				emtpyUpdatesDir();
				throw e;
			}
			
			throw e;
		} finally {
			if(updateProgressBar != null) {
				updateProgressBar.forceClose();
			}
		}
		
		// Return the reference of updater application jar file
		return lastUpdatesDownloaded;
	}
	
	public File[] getLastUpdates() {
		try {
			if (lastUpdatesDownloaded == null || lastUpdatesDownloaded.length < 2 || !lastUpdatesDownloaded[0].exists() || !lastUpdatesDownloaded[1].exists()) {
				return null;
			} else {
				return lastUpdatesDownloaded;
			} 
		} catch (Exception e) {
			updatesReadyToInstall.set(false);
			return null;
		}
	}
	
}
