package updater;

import static java.lang.System.out;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import forks.merged.PropertiesManagerFork;
import forks.merged.Various;
import gui.UpdateProgressPanelMin;
import main.MainStandAloneUpdater;

public class PluginManager {

	public static PropertiesManagerFork resourcesFilePathsMappings = new PropertiesManagerFork(MainStandAloneUpdater.resourcesMappingDefPath);
	
	public static String LOCAL_PLUGINS_DIR = resourcesFilePathsMappings.getProperty("PLUGINS_DIR", "PLUGINS/");
	public static String WEB_PLUGINS_BASE_URL = resourcesFilePathsMappings.getProperty("WEB_PLUGINS_BASE_URL", "https://www.am-design-development.com/ScrYpT/Plugins/");                                                                                                               
	public static String WEB_PLUGINS_MANIFEST_URL = resourcesFilePathsMappings.getProperty("WEB_PLUGINS_MANIFEST_URL", "https://www.am-design-development.com/ScrYpT/Plugins/manifest.properties");                                                                                                               
	public static String LOCAL_PLUGINS_MANIFEST = LOCAL_PLUGINS_DIR + "manifest.properties";                                                                                                               
	public static final String PLUGIN_LIST_MANIFET_KEY = "AvailablePluginsList";

	public ArrayList<PluginDTO> pluginsList;
	
	public PluginManager() {
		pluginsList = new ArrayList<>();
	}
	
	///// PLUGINS /////////////
	
	// MISSING TODO: 
	
	// - unzip plugins, 
	// - install plugins (create a temporary merge of "/Resource_Scrypt" inside PLUGINS, with extracted zip files),
	// - connect to GUI function 
	
	public ArrayList<PluginDTO> downloadPlugins(ArrayList<PluginDTO> pluginsToDownload){
		ArrayList<PluginDTO> downloadedList = new ArrayList<>();
		
		UpdateProgressPanelMin pluginManagerProgbar = null;
		try {
			pluginManagerProgbar = new UpdateProgressPanelMin(true);
			pluginManagerProgbar.getPb().setIndeterminate(true);
			
			for(PluginDTO currentRemote : pluginsToDownload) {
				pluginManagerProgbar.update(20, "Downloading "+currentRemote.completeURL, true);
				String localOut = LOCAL_PLUGINS_DIR + currentRemote.name;
				currentRemote.setLocalFile(DownloadUtils.downloadFile(false, new URL(currentRemote.completeURL), localOut, pluginManagerProgbar));
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			pluginManagerProgbar.forceClose();
		}
		
		return downloadedList;
	}
	
	/**
	 * Retrieve from web Last available plugins and return them as list of objects of type PluginDTO
	 */
	public ArrayList<PluginDTO> getLatestPlugins(){
		
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
		} catch (Throwable e){}
		
		return plugins;
	}
}
