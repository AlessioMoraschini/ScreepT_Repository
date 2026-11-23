/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.plugins.panel;

import java.awt.Component;
import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JComponent;

import gui.commons.tabs.TabContent;
import gui.commons.tabs.TabImplementor;
import gui.commons.tabs.TabImplementors;
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

	/** Registers (or replaces) the viewer of every plugin with panel mode; force = look again for the plugins instead of using the cache */
	public static void refresh(boolean force) {
		try {
			Set<IPlugin> plugins = AbstractPluginApplicationApi.getAvailablePlugins(PluginType.ROOT, false, !force);
			for (IPlugin plugin : plugins) {
				if (plugin instanceof IPluginPanelMode) {
					TabImplementors.register(new PluginTabImplementor((IPluginPanelMode) plugin));
				}
			}
		} catch (Throwable e) {
			logger.warn("Cannot look for the plugins with panel mode: " + e.getMessage());
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
		public boolean supportsTextTools() {
			return panel.supportsTextTools();
		}

		@Override
		public boolean supportsFullscreen() {
			return !panel.supportsTextTools(); // viewers (PDF, Markdown...), not the plugins that work like the text editor
		}
	}
}
