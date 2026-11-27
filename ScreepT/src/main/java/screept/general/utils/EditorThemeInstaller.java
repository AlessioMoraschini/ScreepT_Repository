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
package screept.general.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.log4j.Logger;

import resources.GeneralConfig;

/**
 * Makes sure the "ScreepT Dark" text editor theme exists on disk. The normal way it reaches an installation is the
 * update package; this is the safety net for installations where the file is missing (partial update, deleted by hand,
 * installed from an old release): it is recreated from the copy bundled in the jar. An existing file is never touched,
 * so user edits to the theme survive.
 */
public final class EditorThemeInstaller {

	private static final Logger logger = Logger.getLogger(EditorThemeInstaller.class);

	private static final String FILE_NAME = "screept-dark.xml";
	private static final String BUNDLED_RESOURCE = "/screept/bundled/" + FILE_NAME;

	private EditorThemeInstaller() {
	}

	/** Call at startup, before the themes folder is read */
	public static void ensureInstalled() {
		try {
			File target = new File(GeneralConfig.THEMES_FOLDER_DEFAULT, FILE_NAME);
			if (target.exists()) {
				return;
			}
			InputStream in = EditorThemeInstaller.class.getResourceAsStream(BUNDLED_RESOURCE);
			if (in == null) {
				logger.warn("Bundled editor theme not found in the jar: " + BUNDLED_RESOURCE);
				return;
			}
			try {
				File folder = target.getParentFile();
				if (folder != null && !folder.exists() && !folder.mkdirs()) {
					logger.warn("Cannot create the themes folder: " + folder);
					return;
				}
				Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
				logger.info("Editor theme restored: " + target);
			} finally {
				in.close();
			}
		} catch (IOException | RuntimeException e) {
			logger.warn("Editor theme not restored", e);
		}
	}
}
