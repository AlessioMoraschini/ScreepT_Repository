package screept.plugins.version.extractor;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import plugin.api.AbstractPluginApplicationApi;
import plugin.api.AbstractPluginApplicationApi.PluginType;
import plugin.external.arch.IPlugin;
import updater.module.plugins.PluginManager;
import various.common.light.utility.log.SafeLogger;
import various.common.light.utility.properties.PropertiesManager;
import various.common.light.utility.string.StringWorker;

public class PluginVersionExtractor {

	private static SafeLogger logger = new SafeLogger(PluginVersionExtractor.class);

	public static void main(String[] args) throws IOException {

		logger.setLoggerAvailable(false);

		String manifestFile = args.length > 0 ? args[0] : "./PLUGINS_VANILLA/manifest.properties";
		File outputFile = args.length > 1 ? new File(args[1]) : null;

		logger.info("Reading manifest: " + manifestFile);
		PropertiesManager properties = new PropertiesManager(manifestFile);
		Set<IPlugin> plugins = AbstractPluginApplicationApi.getAvailablePlugins(PluginType.ROOT, false);

		for(IPlugin plugin : plugins) {

			logger.info("Updating plugin data: " + plugin.toString());

			if(!plugin.getVersion().equals(IPlugin.DEFAULT_VERSION)) {
				properties.dynamicFileBindingsDef.put(PluginManager.PLUGIN_VERSION_PROP+plugin.getPluginZipName(), plugin.getVersion());
			}

			if (!StringWorker.isEmpty(plugin.getDescription())) {
				properties.dynamicFileBindingsDef.put(PluginManager.DESCRIPTION_PROP_PREFIX + plugin.getPluginZipName(), plugin.getDescription());
			}

			if (plugin.getWarnings() != null && !plugin.getWarnings().isEmpty()) {
				properties.dynamicFileBindingsDef.put(PluginManager.WARNING_MESSAGES_PROP_PREFIX + plugin.getPluginZipName(),
						PropertiesManager.listToString(plugin.getWarnings(), PluginManager.WARNING_MESSAGES_ITEM_SEPARATOR));
			}
		}

		properties.saveAllProperties();

		if(outputFile != null) {
			outputFile.getParentFile().mkdirs();
			FileUtils.copyFile(properties.dynamicFileBindingsDefSource, outputFile, true);
		}
	}
}
