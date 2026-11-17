/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.gui.utils.tree.iniconfmanager;

import java.io.File;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;

import gui.commons.treeutils.om.FileTreeModel;
import resources.GeneralConfig;
import various.common.light.i18n.I18n;

/**
 * Root of the configurations tree: the configuration in use comes first (it can be anywhere on disk, for example
 * after a custom load), then the folder of the saved configurations.
 */
public final class ConfigPool extends File {
	private static final long serialVersionUID = 1L;

	private final File current;

	public ConfigPool(File current) {
		super(GeneralConfig.DEFAULT_INI_FOLDER);
		this.current = current;
	}

	/** Tree model with the pool as root, so the list always shows the configuration in use */
	public static TreeModel modelFor(File current) {
		return new FileTreeModel(new ConfigPool(current));
	}

	/** Opens every folder of the tree, so the configurations are visible at once */
	public static void expandAll(JTree tree) {
		for (int row = 0; row < tree.getRowCount(); row++) {
			tree.expandRow(row);
		}
	}

	@Override
	public boolean isDirectory() {
		return true;
	}

	@Override
	public boolean isFile() {
		return false;
	}

	@Override
	public String getName() {
		return I18n.t("cfg.configurations_root");
	}

	@Override
	public File[] listFiles() {
		return new File[] { new CurrentConfigFile(current), new File(GeneralConfig.DEFAULT_INI_FOLDER) };
	}

	/** The configuration in use: a real file, shown with its own label in the list */
	public static final class CurrentConfigFile extends File {
		private static final long serialVersionUID = 1L;

		public CurrentConfigFile(File file) {
			super(file.getAbsolutePath());
		}
	}
}
