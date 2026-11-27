/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package screept.plugins.panel;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

import gui.commons.tabs.TabContent;
import gui.commons.tabs.TabImplementor;
import gui.commons.tabs.TabImplementors;
import initializer.configs.impl.INItializer;
import initializer.configs.impl.om.TextEditorOption;
import plugin.api.AbstractPluginApplicationApi;
import plugin.api.AbstractPluginApplicationApi.PluginType;
import plugin.external.arch.IPlugin;
import plugin.external.arch.IPluginPanelMode;
import plugin.external.arch.PluginPanel;
import various.common.light.utility.log.SafeLogger;

/**
 * Makes the plugins that implement {@link IPluginPanelMode} available as viewers of the file editor tabs: they appear in
 * Graphic preferences > File editor > Viewers (where they can be chosen as default of an extension) and in "Open with".
 * Plugins are found at runtime: also the ones installed while ScreepT is running.
 */
public final class PluginPanelBridge {

	private static final SafeLogger logger = new SafeLogger(PluginPanelBridge.class);

	private PluginPanelBridge() {
	}

	/** Installs the provider in the viewer registry: the plugins are searched when the registry needs them (and when a plugin is installed) */
	public static void install() {
		TabImplementors.setDynamicProvider(PluginPanelBridge::refresh);
	}

	/** The configuration where the viewers already seen and the extension associations are kept */
	private static volatile INItializer configuration;

	public static void install(INItializer currentConfiguration) {
		configuration = currentConfiguration;
		install();
	}

	/** Registers (or replaces) the viewer of every plugin with panel mode; force = look again for the plugins instead of using the cache */
	public static void refresh(boolean force) {
		try {
			Set<IPlugin> plugins = AbstractPluginApplicationApi.getAvailablePlugins(PluginType.ROOT, false, !force);
			List<IPluginPanelMode> viewers = new ArrayList<IPluginPanelMode>();
			for (IPlugin plugin : plugins) {
				if (plugin instanceof IPluginPanelMode) {
					TabImplementors.register(new PluginTabImplementor((IPluginPanelMode) plugin));
					viewers.add((IPluginPanelMode) plugin);
				}
			}
			associateNewViewers(viewers);
		} catch (Throwable e) {
			logger.warn("Cannot look for the plugins with panel mode: " + e.getMessage());
		}
	}

	/**
	 * A plugin downloaded after this feature became the viewer of its extensions (unless the user already chose another viewer
	 * for one of them): the first time its viewer is seen. The viewers already there at the first run are only recorded, so
	 * nobody finds his files opening in another viewer after an update of ScreepT.
	 */
	static synchronized void associateNewViewers(List<IPluginPanelMode> viewers) {
		INItializer conf = configuration;
		if (conf == null) {
			return;
		}
		TextEditorOption options = conf.getTextEditorOpt();
		String seen = options.pluginViewersSeen == null ? "" : options.pluginViewersSeen;
		boolean started = seen.startsWith("|");
		if (!started && viewers.isEmpty()) {
			return; // nothing found yet (the embedded viewers are always there): the list is started at the first real search
		}
		StringBuilder updated = new StringBuilder(started ? seen : "|");
		boolean associationsChanged = false;
		boolean seenChanged = !started;
		for (IPluginPanelMode viewer : viewers) {
			String id = idOf((IPlugin) viewer);
			if (updated.indexOf("|" + id + "|") >= 0) {
				continue;
			}
			updated.append(id).append('|');
			seenChanged = true;
			if (started) {
				Map<String, String> chosen = TabImplementors.getOverrides();
				String[] extensions = viewer.getPanelExtensions();
				for (String extension : extensions == null ? new String[0] : extensions) {
					String ext = extension == null ? "" : extension.trim().toLowerCase(Locale.ROOT);
					if (!ext.isEmpty() && !chosen.containsKey(ext)) {
						TabImplementors.associate(ext, id);
						associationsChanged = true;
						logger.info("New plugin viewer " + id + " is now the viewer of ." + ext + " files");
					}
				}
			}
		}
		if (!seenChanged) {
			return;
		}
		options.pluginViewersSeen = updated.toString();
		conf.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "pluginViewersSeen", options.pluginViewersSeen);
		if (associationsChanged) {
			options.viewerAssociations = TabImplementors.serializeOverrides();
			conf.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "viewerAssociations", options.viewerAssociations);
		}
	}

	/** Id of the viewer of a plugin: stable between sessions, it is what the preferences save */
	public static String idOf(IPlugin plugin) {
		return TabImplementors.PLUGIN_ID_PREFIX + plugin.getClass().getSimpleName().toLowerCase();
	}

	private static final class PluginTabImplementor implements TabImplementor {

		private final IPluginPanelMode plugin;
		private final String id;

		PluginTabImplementor(IPluginPanelMode plugin) {
			this.plugin = plugin;
			this.id = idOf(plugin);
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getDisplayName() {
			return plugin.getPanelName() + " (plugin)";
		}

		/** A plugin is never the default of an extension: the user chooses it in the preferences */
		@Override
		public Set<String> getDefaultExtensions() {
			return Collections.emptySet();
		}

		@Override
		public Set<String> getSupportedExtensions() {
			Set<String> extensions = new LinkedHashSet<String>();
			String[] declared = plugin.getPanelExtensions();
			if (declared != null) {
				for (String extension : declared) {
					if (extension != null && !extension.trim().isEmpty()) {
						extensions.add(extension.trim().toLowerCase());
					}
				}
			}
			return extensions;
		}

		@Override
		public boolean canOpen(File file) {
			try {
				return plugin.canOpenInPanel(file);
			} catch (Throwable e) {
				return false;
			}
		}

		@Override
		public TabContent open(File file) throws Exception {
			return new PluginTabContent(plugin.createPanel(file));
		}
	}

	/** The panel of a plugin seen as content of a tab */
	private static final class PluginTabContent implements TabContent {

		private final PluginPanel panel;

		PluginTabContent(PluginPanel panel) {
			this.panel = panel;
		}

		@Override
		public JComponent getComponent() {
			return panel.getComponent();
		}

		@Override
		public String getTitle() {
			return panel.getTitle();
		}

		@Override
		public File getFile() {
			return panel.getFile();
		}

		@Override
		public boolean isModified() {
			return panel.isModified();
		}

		@Override
		public boolean save() {
			return panel.save();
		}

		@Override
		public boolean confirmClose(Component parent) {
			return panel.confirmClose(parent);
		}

		@Override
		public void onSelected() {
			panel.onSelected();
		}

		@Override
		public void dispose() {
			panel.dispose();
		}

		@Override
		public void setChangeListener(Runnable listener) {
			panel.setChangeListener(listener);
		}

		@Override
		public void setCloseHandler(Runnable closeTab) {
			panel.setCloseHandler(closeTab);
		}

		@Override
		public boolean saveAs() {
			return panel.saveAs();
		}

		@Override
		public boolean supportsEditHistory() {
			return panel.supportsEditHistory();
		}

		@Override
		public void undo() {
			panel.undo();
		}

		@Override
		public void redo() {
			panel.redo();
		}

		@Override
		public boolean supportsTextTools() {
			return panel.supportsTextTools();
		}

		@Override
		public boolean supportsFullscreen() {
			return !panel.supportsTextTools(); // viewers (PDF, Markdown...), not the plugins that work like the text editor
		}
	}
}
