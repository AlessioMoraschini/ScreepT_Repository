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
package gui.texteditor.commons.utils.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;

import javax.swing.Scrollable;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import gui.commons.panel.clipboard.ClipboardView;
import gui.texteditor.commons.utils.RsyntaxUtils;
import gui.texteditor.commons.utils.dialog.ITextAreaDialogs;
import gui.texteditor.commons.utils.panels.arch.IParentPanel;
import gui.texteditor.commons.utils.panels.arch.TogglePanelTextArea;
import initializer.configs.impl.INItializer;
import initializer.configs.impl.om.TextEditorOption;
import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;

/**
 * Clipboard panel of the text editor (Alt+A): the shared {@link ClipboardView} with the syntax text area, styled
 * as the editor (theme of the file panel or of the editor, according to the option).
 */
public class ClipboardPanel extends TogglePanelTextArea implements IParentPanel, Scrollable {
	private static final long serialVersionUID = 4527321717233899589L;

	static Logger logger = Logger.getLogger(ClipboardPanel.class);

	public static ClipboardPanel singletonInstance = null;

	public RSyntaxTextArea txtAreaClipboard;
	private final ClipboardView view;
	private final INItializer config;
	private final TextEditorOption options;

	/**
	 * @param dialogManager dialogs and theme of the editor (used to copy its theme in the clipboard text area)
	 * @param configuration application configuration (null: default one)
	 */
	public ClipboardPanel(ITextAreaDialogs dialogManager, INItializer configuration) {
		singletonInstance = this;
		this.dialogManager = dialogManager;
		this.config = (configuration != null) ? configuration : new INItializer();
		this.options = config.getTextEditorOpt();

		txtAreaClipboard = new RSyntaxTextArea();
		txtAreaClipboard.setAntiAliasingEnabled(true);
		txtAreaClipboard.setWrapStyleWord(true);
		txtAreaClipboard.setLineWrap(true);
		view = new ClipboardView(txtAreaClipboard, new Runnable() {
			@Override
			public void run() {
				styleArea();
			}
		}, options, null);
		view.onLoaded = new Runnable() {
			@Override
			public void run() {
				txtAreaClipboard.discardAllEdits();
			}
		};
		setLayout(new MigLayout("fill, insets 0", "[grow,fill]", "[grow,fill]"));
		add(view, "grow");
		styleArea();
	}

	/** Theme, colors and font of the text area */
	private void styleArea() {
		if (options.clipboardSyncStyle && dialogManager != null) {
			dialogManager.loadThemeIntoTarget(txtAreaClipboard);
		} else {
			Color back = options.getFilePanelBackCol().darker();
			txtAreaClipboard.setBackground(back);
			RsyntaxUtils.setBackground(txtAreaClipboard, back);
			txtAreaClipboard.setForeground(Color.WHITE);
			RsyntaxUtils.setForeground(txtAreaClipboard, txtAreaClipboard.getForeground());
		}
		txtAreaClipboard.setSelectionColor(options.getFilePanelBackCol().brighter().brighter());
		txtAreaClipboard.setCurrentLineHighlightColor(txtAreaClipboard.getBackground().brighter());
		if (GeneralConfig.TXT_EDITOR_CLIPBOARD_FONT != null) {
			RsyntaxUtils.setFont(txtAreaClipboard, GeneralConfig.TXT_EDITOR_CLIPBOARD_FONT);
		}
		txtAreaClipboard.setWrapStyleWord(true);
		txtAreaClipboard.setLineWrap(true);
		txtAreaClipboard.repaint();
	}

	/** Style and content in step with the editor (panel shown, preferences applied) */
	@Override
	public void sync() {
		view.restyle();
		view.refreshFromDisk();
	}

	/** Shows the clipboard with the given file name */
	@Override
	public void reload(String target) throws IOException {
		view.select(target);
	}

	/** Writes the text now */
	@Override
	public void doAction(String target) throws IOException {
		view.save();
	}

	/** Writes the text if there is something unsaved (application closing) */
	public void saveCurrClipboardSilent() {
		view.flush();
	}

	public void selectFromComboClipboards(final String key) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				view.select(key);
			}
		});
	}

	public String getCrrentFromCombo() {
		return view.getLoadedName();
	}

	public ITextAreaDialogs getDialogManager() {
		return dialogManager;
	}

	/**
	 * Get a shared instance of text area, useful if used only one at once
	 */
	public static ClipboardPanel getSingletonLazy(ITextAreaDialogs dialogManager, INItializer config) {
		singletonInstance = (singletonInstance == null) ? new ClipboardPanel(dialogManager, config) : singletonInstance;
		return singletonInstance;
	}

	// the editor wraps the panel in a scroll pane: the panel always fills it

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 16;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 64;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return true;
	}
}
