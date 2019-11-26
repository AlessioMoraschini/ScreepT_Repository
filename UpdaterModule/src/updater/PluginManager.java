package updater;

import static java.lang.System.out;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import exception.ProgressBarInterruptedException;
import forks.merged.PropertiesManagerFork;
import forks.merged.Various;
import gui.UpdateProgressPanelMin;
import main.MainStandAloneUpdater;

/**
 * This class contains the methods to manage plugins download, verification, and installation. 
 * It also provides a local caching functionality to store temporaneus valid downloaded plugins.
 * @author xx
 */
public class PluginManager {
	
	public static final String installedPluginPropsFileName = "Installed_plugins.properties";
	public static final String installedPluginPropsListKey = "INSTALLED_LIST";

	public static final HashingFunction DEFAULT_HASHING_F_PLUGINS = HashingFunction.MD5;

	public static PropertiesManagerFork resourcesFilePathsMappings = new PropertiesManagerFork(MainStandAloneUpdater.resourcesMappingDefPath);
	public static PropertiesManagerFork installedPluginProperties = new PropertiesManagerFork(installedPluginPropsFileName);
	
	public static final String LOCAL_PLUGINS_DIR = resourcesFilePathsMappings.getProperty("PLUGINS_DIR", "PLUGINS/");
	public static final String WEB_PLUGINS_BASE_URL = resourcesFilePathsMappings.getProperty("WEB_PLUGINS_BASE_URL", "https://www.am-design-development.com/ScrYpT/Plugins/");                                                                                                               
	public static final String WEB_PLUGINS_MANIFEST_URL = resourcesFilePathsMappings.getProperty("WEB_PLUGINS_MANIFEST_URL", "https://www.am-design-development.com/ScrYpT/Plugins/manifest.properties");                                                                                                               
	public static final String LOCAL_PLUGINS_MANIFEST = LOCAL_PLUGINS_DIR + "manifest.properties";                                                                                                               
	public static final String PLUGIN_LIST_MANIFET_KEY = "AvailablePluginsList";
	
	public static final File APPLICATION_ROOT_DIR = new File(System.getProperty("user.dir"));

	// This is the plugin informations cache, updated at each download. NB there is not data here, only informations about it
	private HashMap<String, PluginDTO> pluginLocalMap;
	
	public ArrayList<String> installedPluginNames;
	
	public PluginManager() {
		pluginLocalMap = new HashMap<>();
		installedPluginNames = getInstalledPluginsList();
		
		try {
			File pluginsDir = new File(LOCAL_PLUGINS_DIR);
			if(!pluginsDir.exists()) {
				pluginsDir.mkdirs();
				pluginsDir.mkdir();
			}
		} catch (Exception e) {
			out.println("Cannot create updates dir (Resources_Scrypt/UPDATES) : " + Arrays.deepToString(e.getStackTrace()));
		}
	}
	
	///// PLUGINS /////////////
	
	/**
	 * Copies a downloaded plugin from current zipped folder into the correct folder of scrypt 
	 * (by design plugins zipped folders are supposed to be relative to main /application directory)
	 * @param askUserIfCannotVerifyChecksum if this is true and hash cannot be verified, then user will be asked what to do.
	 * 	if this is false installation will be automatically stopped if files are not (or cannot be) validated.
	 * @throws Throwable if a generic error occur
	 * @throws ProgressBarInterruptedException if user stopped the process
	 */
	public PluginDTO installPlugin(PluginDTO plugin, boolean askUserIfUnverifiedChecksum) throws Throwable, ProgressBarInterruptedException {
		
		// Input validation
		if(plugin == null || plugin.localFile == null || !plugin.localFile.exists()) {
			return plugin;
		}
		
		plugin.setInstallationCompleted(false);
		
		// Verify checksum asking user what to do in case of problems
		boolean hashOk = checkPluginHash(plugin);
		if(!hashOk) {
			boolean throwHashException = false;
			if(askUserIfUnverifiedChecksum) {
				String askMessage = plugin.getName() + " -> File integrity cannot be verified, install anyway?";
				int userChoice = JOptionPane.showConfirmDialog(null, askMessage, "File verification failed", JOptionPane.YES_NO_OPTION);
				if(userChoice == JOptionPane.NO_OPTION) {
					throw new Exception("Hash [" + plugin.getCheckSum() + "] not verified for file: " + plugin.localFile);
				}
			} else {
				throwHashException = true;
			}

			if (throwHashException) {
				throw new ProgressBarInterruptedException(
						"User has chosen to exit plugin installation: Hash [" + plugin.getCheckSum() + "] not verified for file: " + plugin.localFile);
			}
		}
		
		
		// Clear destination plugin folder if extraction dir is already present
		String folderName = FilenameUtils.removeExtension(plugin.getName());
		File destinationNamedDir = new File(LOCAL_PLUGINS_DIR + File.separator + folderName);
		if(destinationNamedDir.exists() && destinationNamedDir.isDirectory() && destinationNamedDir.list().length > 0) {
			Various.deleteDirContentRecursive(destinationNamedDir);
		}
		
		// Unzip file
		File unzippedPlugin = DownloadUtils.unzipFile(plugin.getLocalFile().getAbsolutePath(), destinationNamedDir.getAbsolutePath(), true);
		plugin.setLocalExtractedDir(unzippedPlugin);
		
		// Copy content into relative directories into /application
		System.out.print("Copying content of " + plugin.getName() + " into " + APPLICATION_ROOT_DIR);
		Various.copyDirectoryContents(plugin.getLocalExtractedDir(), APPLICATION_ROOT_DIR);
		plugin.setInstallationCompleted(true);
		addToInstalledList(plugin);
		installedPluginProperties.saveProperty(plugin.getName(),getFileAsStringListFromFolder(unzippedPlugin));
		
		return plugin;
	}
	
	public boolean removePlugin(PluginDTO pluginToRemove) {
		
		boolean out = false;
		
		if(pluginToRemove == null || pluginToRemove.getName() == null) {
			return false;
		}
		
		ArrayList<String> strPluginFilesList = installedPluginProperties.getStringList(pluginToRemove.getName(), ",");
		for(String toDelete : strPluginFilesList) {
			File filePending = new File(toDelete);
			if (filePending.exists() && filePending.isFile()) {
				try {
					boolean tempRes = filePending.delete();
					out = (tempRes) ? out : false;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return out;
	}
	
	
	/**
	 * Automatically download a single plugin into plugins folder.
	 * @param pluginToDownload
	 * @throws ProgressBarInterruptedException if user clicked on close during download process
	 */
	public PluginDTO downloadPlugin(PluginDTO targetPlugin) throws Throwable, ProgressBarInterruptedException {
		
		if(targetPlugin == null || targetPlugin.getCompleteURL() == null || "".equals(targetPlugin.getCompleteURL().trim())) {
			return null;
		}
		
		ArrayList<PluginDTO> fakeList = new ArrayList<>();
		fakeList.add(targetPlugin);
		
		ArrayList<PluginDTO> downloaded = downloadPlugins(fakeList);
		
		if(downloaded != null && !downloaded.isEmpty()) {
			return downloaded.get(0);
		} else {
			return null;
		}
	}
	
	/**
	 * Automatically download given list into plugins folder.
	 * @param pluginsToDownload
	 * @throws ProgressBarInterruptedException if user clicked on close during download process
	 */
	public ArrayList<PluginDTO> downloadPlugins(ArrayList<PluginDTO> pluginsToDownload) throws Throwable, ProgressBarInterruptedException{
		
		ArrayList<PluginDTO> downloadedList = new ArrayList<>();
		
		if(pluginsToDownload == null || pluginsToDownload.isEmpty()) {
			return downloadedList;
		}
		
		int downloadFilesNumber = pluginsToDownload.size();
		
		UpdateProgressPanelMin pluginManagerProgbar = null;
		
		try {
			pluginManagerProgbar = new UpdateProgressPanelMin(true);
			pluginManagerProgbar.getPb().setIndeterminate(true);
			pluginManagerProgbar.getGeneralBar().setIndeterminate(true);
			
			int i = 1;
			
			for(PluginDTO currentRemote : pluginsToDownload) {
				
				try {
					int perc = (int)((float)(i*100)/(float)downloadFilesNumber);
					pluginManagerProgbar.update(perc, "Downloading "+currentRemote.completeURL, true);
					String localOut = LOCAL_PLUGINS_DIR + currentRemote.name;
					currentRemote.setLocalFile(DownloadUtils.downloadFile(false, new URL(currentRemote.completeURL), localOut, pluginManagerProgbar));
					
					downloadedList.add(currentRemote);
					
					// Use file name with intent to overwrite previously downloaded plugins with same name (each plugin has different name by design).
					// * permit an easy retrieve by key
					pluginLocalMap.put(currentRemote.name, currentRemote);
				
				} catch (Exception e) {
					removeFromCache(currentRemote);
					e.printStackTrace();
				}
				
				i++;
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		} finally {
			pluginManagerProgbar.forceClose();
		}
		
		updateAlreadyInstalledIntoList(downloadedList);
		
		return downloadedList;
	}
	
	/**
	 * Retrieve from web Last available plugins and return them as list of objects of type PluginDTO
	 * @throws Throwable 
	 */
	public ArrayList<PluginDTO> discoverLatestPlugins() throws Throwable{
		
		ArrayList<PluginDTO> plugins = new ArrayList<>();
		UpdateProgressPanelMin pluginManagerProgbar = null;
		
		try {
			if(DownloadUtils.isUrlReachable(new URL(WEB_PLUGINS_MANIFEST_URL))) {
				pluginManagerProgbar = new UpdateProgressPanelMin(true);
				pluginManagerProgbar.getPb().setIndeterminate(true);
				
				out.println(Various.getHeaderString("#", 40, "DOWNLOAD AND PARSE MANIFEST .properties"));
				File downloadedManifest = DownloadUtils.downloadFile(false, new URL(WEB_PLUGINS_MANIFEST_URL), LOCAL_PLUGINS_MANIFEST, pluginManagerProgbar);
				out.println("Manifest Downloaded!! Size of " + WEB_PLUGINS_MANIFEST_URL + " is: " + Various.coolFileSize(downloadedManifest.length()));
				
				PropertiesManagerFork propsManager = new PropertiesManagerFork(downloadedManifest);
				
				ArrayList<String> remotePlugins = propsManager.getStringList(PLUGIN_LIST_MANIFET_KEY, ",");
				for(String plugin : remotePlugins) {
					String checkSum = propsManager.getProperty(plugin);
					String completeUrl = WEB_PLUGINS_BASE_URL + plugin;
					
					plugins.add(new PluginDTO(completeUrl, plugin, checkSum));
				}
			}
		} catch (Throwable e){
			e.printStackTrace();
			throw e;
		}
		
		updateAlreadyInstalledIntoList(plugins);
		
		return plugins;
	}
	
	public void updateAlreadyInstalledIntoList(ArrayList<PluginDTO> plugins) {
		
		if(plugins == null || plugins.isEmpty()) {
			return;
		}
		
		ArrayList<String> installedList = getInstalledPluginsList();
		
		for(PluginDTO current : plugins) {
			if(installedList.contains(current.getName())) {
				current.installationCompleted = true;
			}
		}
	}
	
	/**
	 * Deletes all downloaded files and folders into PLUGINS directory, deleting also the cache.
	 * @return
	 */
	public boolean clearPluginsLocalDir() {
		boolean result = false;
		
		try {
			File pluginsFolder = new File(LOCAL_PLUGINS_DIR);
			if(pluginsFolder.exists()) {
				result = Various.deleteDirContentRecursive(pluginsFolder);
				clearCache();
			}
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		
		return result;
	}
	
	public ArrayList<String> getInstalledPluginsList() {
		installedPluginProperties.readFromFile();
		return installedPluginProperties.getStringList(installedPluginPropsListKey, ",");
	}
	
	private void addToInstalledList(PluginDTO plugin) {
		if(!installedPluginNames.contains(plugin.getName())) {
			installedPluginNames.add(plugin.getName());
		}
		
		String stringList = PropertiesManagerFork.listToString(installedPluginNames, ",");
		installedPluginProperties.saveProperty(installedPluginPropsListKey, stringList);
	}
	
	public boolean isInstalled(PluginDTO plugin) {
		if(plugin == null || plugin.getName() == null) {
			return false;
		}
		
		ArrayList<String> installed = getInstalledPluginsList();
		return installed.contains(plugin.getName());
	}
	
	public boolean checkPluginHash(PluginDTO pluginToCheck) throws Throwable {
		if(pluginToCheck != null) {
			String checkSum = pluginToCheck.getCheckSum();
			File localZipFile = pluginToCheck.getLocalFile();
			
			if(checkSum != null && !"".equals(checkSum.trim()) && localZipFile != null && localZipFile.exists() && localZipFile.isFile()) {
				Boolean hashOk = DownloadUtils.checkFileHash(localZipFile, checkSum, DEFAULT_HASHING_F_PLUGINS);
				return hashOk;
			}
		}
		
		return false;
	}
	
	// PLUGINS - LOCAL STATIC CACHE IN MEMORY //
	
	public void clearCache() {
		pluginLocalMap.clear();
	}
	
	/**
	 * Check if there is a valid plugin into local cache, checking if Url, local file, and correct hash are present into localCache
	 * 
	 * If found one, it will be returned. if not found a valid one, then method will return null.
	 * @throws Throwable 
	 */
	public PluginDTO getValidFromCache(String pluginName) throws Throwable {
		
		if(pluginName == null || "".equals(pluginName.trim())) {
			return null;
		}
		
		boolean alreadyInCache = pluginLocalMap.containsKey(pluginName);
		PluginDTO pluginCacheVal = pluginLocalMap.get(pluginName);
		boolean usablePlugin = 
				alreadyInCache 
				&& pluginCacheVal != null 
				&& pluginCacheVal.completeURL != null 
				&& !"".equals(pluginCacheVal.completeURL);
				
		Boolean hashOk = DownloadUtils.checkFileHash(pluginCacheVal.localFile, pluginCacheVal.checkSum, DEFAULT_HASHING_F_PLUGINS);
		
		if(usablePlugin && hashOk) {
			return pluginCacheVal;
		} else {
			return null;
		}
	}
	
	public PluginDTO retrieveFromCache(String pluginFileName) {
		return pluginLocalMap.get(pluginFileName);
	}

	public PluginDTO updateCache(PluginDTO pluginToUpdate) {
		if(pluginToUpdate == null) {
			return null;
		}
		
		return pluginLocalMap.put(pluginToUpdate.getName(), pluginToUpdate);
	}

	public ArrayList<PluginDTO> updateCache(ArrayList<PluginDTO> pluginsToUpdate) {
		if(pluginsToUpdate == null || pluginsToUpdate.isEmpty()) {
			return pluginsToUpdate;
		}
		
		ArrayList<PluginDTO> addedList = new ArrayList<>();
		
		for(PluginDTO currentToAdd : pluginsToUpdate) {
			addedList.add(pluginLocalMap.put(currentToAdd.getName(), currentToAdd));
		}
		
		return addedList;
	}

	public PluginDTO removeFromCache(PluginDTO pluginToUpdate) {
		if(pluginToUpdate == null) {
			return null;
		}
		
		return pluginLocalMap.remove(pluginToUpdate.getName());
	}
	
	public ArrayList<PluginDTO> removeFromCache(ArrayList<PluginDTO> pluginsToUpdate) {
		if(pluginsToUpdate == null || pluginsToUpdate.isEmpty()) {
			return pluginsToUpdate;
		}
		
		ArrayList<PluginDTO> removedList = new ArrayList<>();
		
		for(PluginDTO currentToRemove : pluginsToUpdate) {
			if(pluginLocalMap.remove(currentToRemove.getName(), currentToRemove)) {
				removedList.add(currentToRemove);
			}
		}
		
		return removedList;
	}
	
	public String getFileAsStringListFromFolder(File containerFolder) throws IOException {
		ArrayList<File> allFiles = Various.listFiles(containerFolder, new ArrayList<File>());
		ArrayList<String> relativePahs = new ArrayList<>();
		for(File current : allFiles) {
			relativePahs.add(Various.getPathRelativeToOtherFile(current, APPLICATION_ROOT_DIR));
		}
		
		return PropertiesManagerFork.listToString(relativePahs, ",");
	}
}
