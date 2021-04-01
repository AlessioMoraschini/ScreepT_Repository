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
package screept.general.utils;

import java.awt.Color;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import gui.commons.frameutils.frame.ClipboardFrame;
import gui.commons.frameutils.frame.ColorPickerFrame;
import gui.commons.frameutils.frame.GeneratePasswordFrame;
import gui.commons.frameutils.frame.HashCheckerFrame;
import gui.commons.frameutils.frame.ShellFrame;
import gui.commons.frameutils.frame.arch.ParentFrame;
import gui.commons.frameutils.frame.panels.ShellReadWritePanel;
import gui.commons.frameutils.frame.panels.arch.ParentPanel;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import gui.texteditor.commons.SchemeManagerGui;
import gui.texteditor.commons.filesearch.FileSearchActions;
import gui.texteditor.commons.filesearch.content.FileContentSearchDialog;
import gui.texteditor.commons.om.themes.ThemesWrapper;
import gui.texteditor.commons.utils.RsyntaxUtils;
import hw.commons.main.HwInfoFrame;
import initializer.configs.impl.INItializer;
import maingUI.MainGUI;
import maingUI.MainGuiGeneralAppStarter;
import maingUI.MainGuiManager;
import network.main.NetInfoFrame;
import os.commons.utils.SysUtils;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import resources.SoundsConfigurator;
import resources.SoundsManagerExtended;
import screept.gui.frames.AboutSectionFrame;
import screept.gui.frames.ChangeLogFrame;
import screept.gui.frames.GeneralOptionsFrame;
import screept.gui.frames.GraphicPreferencesFrame;
import screept.gui.frames.ImageConverterFrame;
import screept.gui.frames.ImportExportWorkspcCryptFrame;
import screept.gui.frames.KeyDictionaryFrame;
import screept.gui.frames.KeyGenFrame;
import screept.gui.frames.TutorialFrame;
import various.common.light.files.om.FileNamed;
import various.common.light.files.om.SearchResult;
import various.common.light.gui.GuiUtils;
import various.common.light.gui.LightLicenseViewer;
import various.common.light.gui.LightLicenseViewer.UserLicenseChoice;
import various.common.light.gui.UpdateProgressPanel;
import various.common.light.gui.dialogs.msg.JOptionHelper;
import various.common.light.om.LimitedConcurrentList;

public class WindowCreator {
	static Logger logger = Logger.getLogger(WindowCreator.class);

	MainGUI mainApplication;

	public WindowCreator(MainGUI mainApplication) {
		this.mainApplication = mainApplication;
	}

	public SchemeManagerGui newSchemeManager() {
		SchemeManagerGui schemeManager = SchemeManagerGui.getInstance(
				new File(
					GeneralConfig.THEMES_FOLDER_CUSTOM),
					new File(GeneralConfig.THEMES_FOLDER_DEFAULT),
					MainGUI.currentConfiguration.getTextEditorOpt().font);

		schemeManager.updateIcons(
				new ImageIcon(IconsPathConfigurator.ICON_PAINT_PALETTE),
				new ImageIcon(IconsPathConfigurator.ICON_RELOAD2),
				new ImageIcon(IconsPathConfigurator.ICON_FILE_SAVE),
				new ImageIcon(IconsPathConfigurator.ICON_FILE_SAVE_AS)
		);

		schemeManager.setCloseAction(() -> {
			if(MainGUI.textEditorPanel != null && MainGUI.textEditorPanel.subHeaderPanel != null) {
				MainGUI.textEditorPanel.subHeaderPanel.initComboStyleScheme();
				MainGUI.textEditorPanel.subHeaderPanel.comboStylesScheme
					.setSelectedItem(ThemesWrapper.getFromListByPath(
							schemeManager.getSelectedColorSchemeObj().schemeSource.getAbsolutePath(),
							MainGUI.textEditorPanel.subHeaderPanel.themeList));
				MainGUI.textEditorPanel.textEditorUtils.applySelectedTheme(true);
			}
		});

		schemeManager.setAlwaysOnTop(true);
		schemeManager.setLocationRelativeTo(mainApplication.getMainFrame());

		return schemeManager;
	}

	public HashCheckerFrame newHashChecker() {

		File targetFile = (MainGUI.textEditorPanel != null) ? MainGUI.textEditorPanel.getCurrentTabEditor().loadedTabObj.loadedFile : null;
		HashCheckerFrame hashCheckerFrame = HashCheckerFrame.getInstance(
				targetFile, mainApplication.getMainFrame(), MainGUI.currentConfiguration.getTextEditorOpt());

		commonGraphicInitAlwaysOnTop(hashCheckerFrame, "File Hash Checker - ", true);

		return hashCheckerFrame;
	}

	public ClipboardFrame newClipboardFrame() {

		ClipboardFrame clipboardFrame = ClipboardFrame.getInstance(
				mainApplication.getMainFrame(), MainGUI.currentConfiguration.getTextEditorOpt());
		commonGraphicInitAlwaysOnTop(clipboardFrame, "Clipboard Frame - ", true);

		return clipboardFrame;
	}

	private File getLastValidFileFromList(LimitedConcurrentList<FileNamed> list) {
		for(int i = list.getList().size() - 1; i >= 0; i--) {
			FileNamed curr = list.getList().get(i);
			if(curr != null && curr.mFile != null && curr.mFile.exists())
				return curr.mFile;
		}

		return null;
	}

	private void addNWriteLastShellFrameFileToConfig(File toAdd) {

		if(toAdd == null)
			return;

		try {
			MainGUI.currentConfiguration.getTextEditorOpt().getLastOpenedShellFrameFiles()
				.addLast(new FileNamed(toAdd.getCanonicalPath()));
			MainGUI.currentConfiguration.writeSingleValue(
					INItializer.TEXT_EDITOR_OPT, "lastOpenedShellFiles",
					MainGUI.currentConfiguration.getTextEditorOpt().getLastOpenedShellFrameFiles().toCharSeparatedString(";"));
		} catch (IOException e) {
			logger.error("Cannot update last shell file opened in configuration", e);
		}
	}

	public ShellFrame newShellFrame(boolean dark) {
		File lastValid = getLastValidFileFromList(MainGUI.currentConfiguration.getTextEditorOpt().getLastOpenedShellFrameFiles());

		ShellFrame shellFrame = ShellFrame.getInstance(
				getDarkTextArea(),
				mainApplication.getMainFrame(),
				true,
				lastValid);

		shellFrame.payloadPanel.fileChooser = MainGuiGeneralAppStarter.fileDialogHelper;

		shellFrame.setCloseAction(() -> {
			addNWriteLastShellFrameFileToConfig(shellFrame.getLoadedFile());
		});

		((ShellReadWritePanel)shellFrame.payloadPanel).setAfterLoadAction(() -> {
			addNWriteLastShellFrameFileToConfig(shellFrame.getLoadedFile());
		});

		((ShellReadWritePanel)shellFrame.payloadPanel).setAfterSaveAction(() -> {
			addNWriteLastShellFrameFileToConfig(shellFrame.getLoadedFile());
		});

		if(dark)
			((ShellReadWritePanel)shellFrame.payloadPanel).darker();

		commonGraphicInit(shellFrame, "Shell Frame - ", true);

		return shellFrame;
	}

	public RSyntaxTextArea getDarkTextArea() {
		RSyntaxTextArea txtArea = new RSyntaxTextArea();
		txtArea.setCurrentLineHighlightColor(Color.DARK_GRAY.darker());
		txtArea.setSelectedTextColor(Color.DARK_GRAY);
		txtArea.setSelectionColor(Color.DARK_GRAY);
		txtArea.setFont(ParentPanel.DEFAULT_TEXT_AREA_FONT.deriveFont(GeneralConfig.SHELL_PANEL_FONT_SIZE));
		RsyntaxUtils.setFont(txtArea, ParentPanel.DEFAULT_TEXT_AREA_FONT.deriveFont(GeneralConfig.SHELL_PANEL_FONT_SIZE));
		return txtArea;
	}

	public ShellReadWritePanel getShellPanel(boolean dark) {
		File preloadFile = getLastValidFileFromList(MainGUI.currentConfiguration.getTextEditorOpt().getLastOpenedShellFrameFiles());
		ShellReadWritePanel panel = new ShellReadWritePanel(80000, mainApplication.getMainFrame(), getDarkTextArea());
		panel.darker();

		panel.fileChooser = MainGuiGeneralAppStarter.fileDialogHelper;

		if(preloadFile != null && preloadFile.isFile() && preloadFile.exists())
			panel.loadAction(preloadFile).run();

		panel.setSyncAction(() -> {
			if(MainGUI.mainGuiUtils.isTextEditorActive(false))
				MainGUI.textEditorPanel.textEditorUtils.updateBottomDividerLocation();
		});

		panel.setAfterLoadAction(() -> {
			addNWriteLastShellFrameFileToConfig(panel.loadedFile);
		});

		panel.setAfterSaveAction(() -> {
			addNWriteLastShellFrameFileToConfig(panel.loadedFile);
		});

		if(dark)
			panel.darker();

		panel.pinToTopButton.setEnabled(false);
		panel.pinToTopButton.setVisible(false);

		return panel;
	}

	public ColorPickerFrame newColorPickerFrame() {

		ColorPickerFrame colorPickerFrame = ColorPickerFrame.getInstance(mainApplication.getMainFrame());
		commonGraphicInitAlwaysOnTop(colorPickerFrame, "Color Picker Frame", true);

		return colorPickerFrame;
	}

	public GeneratePasswordFrame newGeneratePassordFrame(Integer passordSizeNullable) {

		GeneratePasswordFrame panelPswKeyGen = GeneratePasswordFrame.getInstance(mainApplication.getMainFrame());

		commonGraphicInit(panelPswKeyGen, "Generate Password - ", true);

		if (passordSizeNullable == null) {
			panelPswKeyGen.unforceLentgh(16);
		} else {
			panelPswKeyGen.forceLentgh(passordSizeNullable);
		}

		return panelPswKeyGen;
	}

	public ImportExportWorkspcCryptFrame newImportExportCryptWorkspace() {

		ImportExportWorkspcCryptFrame panelImportExportTxtEditor = ImportExportWorkspcCryptFrame.getInstance(
				MainGUI.currentConfiguration.getTextEditorOpt(), MainGUI.textEditorPanel);

		commonGraphicInit(panelImportExportTxtEditor, "Import/Export crypted Workspace - ", true);

		panelImportExportTxtEditor.setState(java.awt.Frame.NORMAL);
		panelImportExportTxtEditor.syncWithCurrentWOrkspace();

		return panelImportExportTxtEditor;
	}

	public KeyGenFrame newKeyGenFrame() {

		KeyGenFrame panelKeyGen = KeyGenFrame.getInstance(MainGUI.currentConfiguration);

		commonGraphicInit(panelKeyGen, "IMG Generator - ", true);

    	return panelKeyGen;
	}

	public KeyDictionaryFrame newKeyDIctionaryFrame() {

		KeyDictionaryFrame panelKeyDict = KeyDictionaryFrame.getInstance(MainGUI.currentConfiguration);

		commonGraphicInit(panelKeyDict, "Key Dictionary - ", true);

    	return panelKeyDict;
	}

	public GraphicPreferencesFrame newGraphicPreferencesFrame() {

		GraphicPreferencesFrame panelGuiOptions = GraphicPreferencesFrame.getInstance(MainGUI.currentConfiguration);

		commonGraphicInitAlwaysOnTop(panelGuiOptions, "Graphics preferences - ", true);

		return panelGuiOptions;
	}

	public GeneralOptionsFrame newGeneralOptionsFrame() {

		GeneralOptionsFrame panelGeneralOptions = GeneralOptionsFrame.getInstance(MainGUI.currentConfiguration);

		commonGraphicInitAlwaysOnTop(panelGeneralOptions, "Generic Options - ", true);

		panelGeneralOptions.setMinimumSize(panelGeneralOptions.getDefaultDimension());
		GuiUtilsExtended.centerComponent(panelGeneralOptions);

		return panelGeneralOptions;
	}

	public ImageConverterFrame newImageConverterFrame() {

		ImageConverterFrame panelImgConv = ImageConverterFrame.getInstance(MainGUI.currentConfiguration);

		commonGraphicInit(panelImgConv, "IMG Converter - ", true);

		panelImgConv.setResizable(false);

    	return panelImgConv;
	}

	public void newNetInfoFrame() {

		GuiUtilsExtended.launchThreadSafeSwing(new Runnable() {
			@Override
			public void run() {
				UpdateProgressPanel prog = new UpdateProgressPanel(false);
				prog.setLocationRelativeTo(MainGUI.mainFrameScreept);
				prog.getPb().setIndeterminate(true);
				prog.getLabel().setText("Loading Network informations...");
				prog.getPb().setString("Loading...");

				NetInfoFrame panelNetInfo = NetInfoFrame.getInstance();
				panelNetInfo.setResizable(false);
				commonGraphicInitAlwaysOnTop(panelNetInfo, "Network info - ", true);

				prog.forceClose();
			}
		});
	}

	public FileContentSearchDialog fileContentSearch(List<File> preloadedSourceFiles) {
		FileContentSearchDialog searchDialog = fileContentSearch();
		searchDialog.setSelectedFiles(preloadedSourceFiles);
		return searchDialog;
	}

	public FileContentSearchDialog fileContentSearch() {

		FileContentSearchDialog searchDialog = FileContentSearchDialog.getLazyInstance((JFrame)null, new FileSearchActions() {

			@Override
			public void resultView(SearchResult searchResult, int viewLine) {

				MainGUI.mainGuiUtils.openFilesInTextEditor(new File[] {searchResult.ownerFile}, ()->{
					try {
						JTextArea textArea = MainGUI.textEditorPanel.getCurrentTabEditor().loadedTabObj.textArea;
						textArea.requestFocus();
						int lineCaretOffset = textArea.getLineStartOffset(viewLine-1);
						textArea.setSelectionStart(lineCaretOffset + searchResult.resultLines.get(viewLine).get(0).start);
						textArea.setSelectionEnd(lineCaretOffset + searchResult.resultLines.get(viewLine).get(0).end);
					} catch (Exception e) {
						logger.warn("Cannot select current result into loaded textArea", e);
					}
				});
			}

		});

		searchDialog.setIconImage(new ImageIcon(IconsPathConfigurator.ICON_FILE_SEARCH).getImage());
		searchDialog.setOptions(MainGUI.currentConfiguration.getFileOpt(), true);
		searchDialog.setVisible(true);
		commonGraphicInit(searchDialog, null);
		return searchDialog;
	}

	public void newHwInfoFrame() {

		GuiUtilsExtended.launchThreadSafeSwing(new Runnable() {
			@Override
			public void run() {
				UpdateProgressPanel prog = new UpdateProgressPanel(false);
				prog.setLocationRelativeTo(MainGUI.mainFrameScreept);
				prog.getPb().setIndeterminate(true);
				prog.getLabel().setText("Loading System informations...");
				prog.getPb().setString("Loading...");

				HwInfoFrame panelHwInfo = HwInfoFrame.getInstance();
				commonGraphicInitAlwaysOnTop(panelHwInfo, "Hardware info - ", true);
				panelHwInfo.setResizable(false);

				prog.forceClose();
			}
		});
	}

	public AboutSectionFrame newAboutSectionFrame() {

		AboutSectionFrame panelAbout = AboutSectionFrame.getInstance(mainApplication);

		commonGraphicInit(panelAbout, "About - ", true);

		panelAbout.setResizable(false);

		return panelAbout;
	}

	public LightLicenseViewer newLicenseFrame(boolean mandatoryDeclineAcceptOptions, boolean closeOnDecline, boolean... visibleFlags) {
		return newLicenseFrame(GeneralConfig.LICENSE_FILE, mandatoryDeclineAcceptOptions, closeOnDecline, visibleFlags);
	}

	public LightLicenseViewer newLicenseFrame(File licenseHtml, boolean mandatoryDeclineAcceptOptions, boolean exitOnDecline, boolean... visibleFlags) {

		LightLicenseViewer licenseViewer;

		if (mandatoryDeclineAcceptOptions) {
			LightLicenseViewer.mandatoryChoice = true;
			UserLicenseChoice choice;
			do{
				licenseViewer = LightLicenseViewer.getInstance(
						licenseHtml,
						"You must accept this EULA before to start using ScreepT",
						"License acceptance",
						visibleFlags);
				choice = licenseViewer.userChoice;
				if(choice.equals(UserLicenseChoice.REJECTED)) {
					if (new JOptionHelper(null).yesOrNo("You cannot proceed without accepting this License, are you sure to stop this process?")) {
						if (exitOnDecline) {
							MainGuiManager.closeApplication(1);
						} else {
							return licenseViewer;
						}
					}
				}else {
					GeneralConfig.licenseProperties.saveCommentedProperty("LICENSE_ACCEPTED", String.valueOf(true));
				}

			} while(choice.equals(UserLicenseChoice.REJECTED));
		} else {

			LightLicenseViewer.mandatoryChoice = false;
			licenseViewer = LightLicenseViewer.getInstance(GeneralConfig.LICENSE_FILE, "Here is the EULA (End User License Agreement)", "ScreepT - License (EULA)");
		}

		return licenseViewer;
	}

	public TutorialFrame newTutorialFrame(boolean visible) {

		boolean unsupported = false;

		try {
			TutorialFrame tutorialFrame = new TutorialFrame();

			if(!SysUtils.getOsInfoLazy().isLinux() && visible) {
				tutorialFrame.reloadHtml();
				SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
				commonGraphicInit(tutorialFrame, "Documentation - ", true);
				tutorialFrame.setResizable(true);
			}

			return tutorialFrame;
		} catch (Throwable e) {

			unsupported = true;
			logger.error("Unsupported internal html viewer" + TutorialFrame.class, e);
			return null;

		} finally {
			if ((SysUtils.getOsInfoLazy().isLinux() || unsupported) && visible) {
				File htmlTutorialFile = new File(GeneralConfig.SCREEPT_TUTORIAL_HTML_PATH);
				boolean openWithBrowser = MainGUI.dialogHelper.yesOrNo("Internal html reader not supported, open with default browser?", "Unsupported internal html reader");
				if (openWithBrowser) {
					SysUtils.openUrlInBrowserOrdered(htmlTutorialFile.toURI().toString());
				}
			}
		}
	}

	public ChangeLogFrame newChangeLogFrame() {

		ChangeLogFrame panelChangeLog = ChangeLogFrame.getInstance(GeneralConfig.CHANGE_LOG_FILE);

		commonGraphicInit(panelChangeLog, "ChangeLog - ", true);

		panelChangeLog.setResizable(true);

		return panelChangeLog;
	}

	// COMMONS INITIALIZATION METHODS //
	public void commonGraphicInit(final Dialog newFrame, String title) {

		SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);

		if(title != null)
			newFrame.setTitle(title + " " + GeneralConfig.APPLICATION_NAME);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				newFrame.setLocationRelativeTo(mainApplication.getMainFrame());
				newFrame.setVisible(true);
			}
		});
	}

	public void commonGraphicInit(final ParentFrame newFrame, String title, boolean autoExtend) {

		SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);

		newFrame.setMinimumSize(newFrame.getMinimumDimension());
		newFrame.setPreferredSize(newFrame.getDefaultDimension());

		newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		newFrame.setTitle(title + GeneralConfig.APPLICATION_NAME);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
//				if (autoExtend) {
//					GuiUtils.extendFramesAndMoveToTop(mainApplication.getMainFrame(), newFrame, JFrame.MAXIMIZED_BOTH);
//				}
				GuiUtils.centerComponent(newFrame, newFrame.getDefaultDimension());
				newFrame.setLocationRelativeTo(mainApplication.getMainFrame());
				newFrame.setVisible(true);
			}
		});
	}

	public void commonGraphicInitAlwaysOnTop(ParentFrame newFrame, String title, boolean autoExtend) {
		newFrame.setAlwaysOnTop(true);
		commonGraphicInit(newFrame, title, autoExtend);
	}
}
