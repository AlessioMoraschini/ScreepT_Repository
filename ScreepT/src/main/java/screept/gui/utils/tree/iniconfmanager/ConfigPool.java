/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
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
