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
package maingUI;

import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import files.om.SearchResult;
import frameutils.frame.ClipboardFrame;
import frameutils.frame.ColorPickerFrame;
import frameutils.frame.GeneratePasswordFrame;
import frameutils.frame.HashCheckerFrame;
import frameutils.frame.ShellFrame;
import frameutils.frame.arch.ParentFrame;
import frameutils.utils.GuiUtilsExtended;
import gUI.frames.AboutSectionFrame;
import gUI.frames.ChangeLogFrame;
import gUI.frames.GeneralOptionsFrame;
import gUI.frames.GraphicPreferencesFrame;
import gUI.frames.ImageConverterFrame;
import gUI.frames.ImportExportWorkspcCryptFrame;
import gUI.frames.KeyDictionaryFrame;
import gUI.frames.KeyGenFrame;
import gUI.frames.TutorialFrame;
import gui.GuiUtils;
import gui.LightLicenseViewer;
import gui.SchemeManagerGui;
import gui.UpdateProgressPanel;
import gui.LightLicenseViewer.UserLicenseChoice;
import gui.dialogs.msg.JOptionHelper;
import gui.filesearch.FileSearchActions;
import gui.filesearch.FileSearchDialog;
import guiVersion.HwInfoFrame;
import guiVersion.NetInfoFrame;
import om.themes.ThemesWrapper;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import resources.SoundsConfigurator;
import resources.SoundsManagerExtended;
import utils.SysUtils;

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
	
	public ShellFrame newShellFrame() {
		
		ShellFrame shellFrame = ShellFrame.getInstance(mainApplication.getMainFrame(), true);
		commonGraphicInitAlwaysOnTop(shellFrame, "Shell Frame - ", true);
		
		return shellFrame;
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
	
	public FileSearchDialog fileContentSearch() {
		
		FileSearchDialog searchDialog = FileSearchDialog.getLazyInstance((JFrame)null, new FileSearchActions() {

			@Override
			public void resultView(SearchResult searchResult, int viewLine) {
				if(!MainGUI.mainGuiUtils.isTextEditorActive()) {
					MainGUI.mainGuiUtils.startTextEditor(MainGUI.textEditorPanel == null);
				}
				
				SwingUtilities.invokeLater(()->{
					while (MainGUI.textEditorPanel == null) {
						try {
							Thread.sleep(250);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					MainGUI.textEditorPanel.getCurrentTabEditor().loadFile(searchResult.ownerFile);
				
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
		searchDialog.setVisible(true);
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

	public LightLicenseViewer newLicenseFrame(boolean mandatoryDeclineAcceptOptions, boolean closeOnDecline) {
		
		LightLicenseViewer licenseViewer;
		
		if (mandatoryDeclineAcceptOptions) {
			LightLicenseViewer.mandatoryChoice = true;
			UserLicenseChoice choice;
			do{
				licenseViewer = LightLicenseViewer.getInstance(GeneralConfig.LICENSE_FILE, "You must accept this EULA before to start using ScreepT", "License acceptance");
				choice = licenseViewer.userChoice;
				if(choice.equals(UserLicenseChoice.REJECTED)) {
					if (new JOptionHelper(null).yesOrNo("You cannot proceed without accepting this License, are you sure to stop this process?")) {
						if (closeOnDecline) {
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
	public void commonGraphicInit(final ParentFrame newFrame, String title, boolean autoExtend) {
		
		SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
		
		newFrame.setMinimumSize(newFrame.getMinimumDimension());
		newFrame.setPreferredSize(newFrame.getDefaultDimension());
		
		newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		newFrame.setTitle(title + GeneralConfig.APPLICATION_NAME);
		
		SwingUtilities.invokeLater(new Runnable() {
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
