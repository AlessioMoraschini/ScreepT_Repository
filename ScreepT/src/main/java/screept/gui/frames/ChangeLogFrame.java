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
package screept.gui.frames;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import gui.commons.frameutils.frame.arch.ParentFrame;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import screept.gui.frames.releasenotes.ReleaseNotes;
import screept.gui.frames.releasenotes.ReleaseNotes.Release;
import screept.gui.frames.releasenotes.ReleaseNotesPanel;
import various.common.light.files.FileWorker;
import various.common.light.gui.GuiUtils;

/**
 * Release notes viewer (CHANGELOG.md); falls back to the raw git changelog when the notes are missing.
 */
public class ChangeLogFrame extends ParentFrame{
	private static final long serialVersionUID = -168486278206232881L;

	// initialize Logger
	static Logger logger = Logger.getLogger(ChangeLogFrame.class);

	public static ChangeLogFrame singletonInstance = null;

	public ChangeLogFrame(File ChangeLog) {
		singletonInstance = this;

		GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_DIFFERENCES, this);
		List<Release> releases = readReleaseNotes();
		if (!releases.isEmpty()) {
			add(new ReleaseNotesPanel(releases, GeneralConfig.APP_VERSION, ChangeLog));
		} else if(ChangeLog == null) {
			dialogHelper.error("ChangeLog not found", "ChangeLog not found");
			logger.error("Changelog not found:" + ChangeLog);
			dispose();
		}else {
			JTextArea textArea = new JTextArea();
			textArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
			textArea.setMargin(new Insets(10, 15, 10, 15));
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setViewportView(textArea);
			add(scrollPane);

			try {
				textArea.setText("");
				FileWorker.appendFileTxtArea(ChangeLog, textArea, true);
			} catch (Exception e) {
				String msg = "Cannot read changeLog file: " + ChangeLog;
				textArea.setText(msg);
				logger.error(msg, e);
			}
		}

		addHandlers();

		logger.debug("ChangeLog Frame loaded: " + getClass().getName());
	}

	private static List<Release> readReleaseNotes() {
		try {
			if (GeneralConfig.RELEASE_NOTES_FILE.isFile()) {
				return ReleaseNotes.read(GeneralConfig.RELEASE_NOTES_FILE);
			}
			logger.warn("Release notes not found: " + GeneralConfig.RELEASE_NOTES_FILE.getAbsolutePath());
		} catch (Exception e) {
			logger.error("Cannot read release notes: " + GeneralConfig.RELEASE_NOTES_FILE, e);
		}
		return Collections.emptyList();
	}

	private void addHandlers() {
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {

	        	logger.debug("Closing Frame: " + getClass().getName());
	        	isActive.set(false);
	        	singletonInstance = null;
	        	dispose();
		    }
		});
	}

	@Override
	public Dimension getDefaultDimension() {
		return new Dimension(1000, 760);
	}

	public static ChangeLogFrame getInstance(File changeLog) {
		return singletonInstance == null ? new ChangeLogFrame(changeLog) : singletonInstance;
	}
}
