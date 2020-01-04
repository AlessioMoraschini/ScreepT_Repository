package maingUI;

import java.io.File;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import frameutils.frame.ClipboardFrame;
import frameutils.frame.ColorPickerFrame;
import frameutils.frame.GeneratePasswordFrame;
import frameutils.frame.HashCheckerFrame;
import frameutils.frame.arch.ParentFrame;
import frameutils.utils.GuiUtils;
import gUI.frames.AboutSectionFrame;
import gUI.frames.ChangeLogFrame;
import gUI.frames.GeneralOptionsFrame;
import gUI.frames.GraphicPreferencesFrame;
import gUI.frames.ImageConverterFrame;
import gUI.frames.ImportExportWorkspcCryptFrame;
import gUI.frames.KeyDictionaryFrame;
import gUI.frames.KeyGenFrame;
import gUI.frames.TutorialFrame;
import gui.UpdateProgressPanelMin;
import guiVersion.HwInfoFrame;
import guiVersion.NetInfoFrame;
import media.Sounds;
import resources.GeneralConfig;
import resources.SoundsConfigurator;
import utils.SysUtils;

public class WindowCreator {
	static Logger logger = Logger.getLogger(WindowCreator.class);

	MainGUI mainApplication;
	
	public WindowCreator(MainGUI mainApplication) {
		this.mainApplication = mainApplication;
	}
	
	public HashCheckerFrame newHashChecker() {
		
		File targetFile = (MainGUI.initTxtWriter != null) ? MainGUI.initTxtWriter.getCurrentTabEditor().loadedTabObj.loadedFile : null;
		HashCheckerFrame hashCheckerFrame = HashCheckerFrame.getInstance(
				targetFile, mainApplication.getMainFrame(), MainGUI.currentConfiguration.getTextEditorOpt());
	
		commonGraphicInitAlwaysOnTop(hashCheckerFrame, "File Hash Checker - ");
		
		return hashCheckerFrame;
	}
	
	public ClipboardFrame newClipboardFrame() {
			
		ClipboardFrame clipboardFrame = ClipboardFrame.getInstance(mainApplication.getMainFrame());
		commonGraphicInitAlwaysOnTop(clipboardFrame, "Clipboard Frame - ");
		
		return clipboardFrame;
	}
	
	public ColorPickerFrame newColorPickerFrame() {
		
		ColorPickerFrame colorPickerFrame = ColorPickerFrame.getInstance(mainApplication.getMainFrame());
		commonGraphicInitAlwaysOnTop(colorPickerFrame, "Color Picker Frame");
		
		return colorPickerFrame;
	}
	
	public GeneratePasswordFrame newGeneratePassordFrame(Integer passordSizeNullable) {
		
		GeneratePasswordFrame panelPswKeyGen = GeneratePasswordFrame.getInstance(mainApplication.getMainFrame());
		
		commonGraphicInit(panelPswKeyGen, "Generate Password - ");
		
		if (passordSizeNullable == null) {
			panelPswKeyGen.unforceLentgh(16);
		} else {
			panelPswKeyGen.forceLentgh(passordSizeNullable);
		}
		
		return panelPswKeyGen;
	}
	
	public ImportExportWorkspcCryptFrame newImportExportCryptWorkspace() {
		
		ImportExportWorkspcCryptFrame panelImportExportTxtEditor = ImportExportWorkspcCryptFrame.getInstance(
				MainGUI.currentConfiguration.getTextEditorOpt(), MainGUI.initTxtWriter);
		
		commonGraphicInit(panelImportExportTxtEditor, "Import/Export crypted Workspace - ");

		panelImportExportTxtEditor.setState(java.awt.Frame.NORMAL);
		panelImportExportTxtEditor.syncWithCurrentWOrkspace();
		
		return panelImportExportTxtEditor;
	}
	
	public KeyGenFrame newKeyGenFrame() {
		
		KeyGenFrame panelKeyGen = KeyGenFrame.getInstance(MainGUI.currentConfiguration);
		
		commonGraphicInit(panelKeyGen, "IMG Generator - ");
		
    	return panelKeyGen;
	}
	
	public KeyDictionaryFrame newKeyDIctionaryFrame() {
		
		KeyDictionaryFrame panelKeyDict = KeyDictionaryFrame.getInstance(MainGUI.currentConfiguration);
				
		commonGraphicInit(panelKeyDict, "Key Dictionary - ");
    	
    	return panelKeyDict;
	}
	
	public GraphicPreferencesFrame newGraphicPreferencesFrame() {
		
		GraphicPreferencesFrame panelGuiOptions = GraphicPreferencesFrame.getInstance(MainGUI.currentConfiguration);
		
		commonGraphicInitAlwaysOnTop(panelGuiOptions, "Graphics preferences - ");
		
		return panelGuiOptions;
	}
	
	public GeneralOptionsFrame newGeneralOptionsFrame() {
		
		GeneralOptionsFrame panelGeneralOptions = GeneralOptionsFrame.getInstance(MainGUI.currentConfiguration);
		
		commonGraphicInitAlwaysOnTop(panelGeneralOptions, "Generic Options - ");

		panelGeneralOptions.setMinimumSize(panelGeneralOptions.getDefaultDimension());
		GuiUtils.centerComponent(panelGeneralOptions);
		
		return panelGeneralOptions;
	}
	
	public ImageConverterFrame newImageConverterFrame() {
		
		ImageConverterFrame panelImgConv = ImageConverterFrame.getInstance(MainGUI.currentConfiguration);
		
		commonGraphicInit(panelImgConv, "IMG Generator - ");
		
		panelImgConv.setResizable(false);
		
    	return panelImgConv;
	}
	
	public void newNetInfoFrame() {
		
		GuiUtils.launchThreadSafeSwing(new Runnable() {
			public void run() {
				UpdateProgressPanelMin prog = new UpdateProgressPanelMin(false);
				prog.getPb().setIndeterminate(true);
				prog.getLabel().setText("Loading Network informations...");
				prog.getPb().setString("Loading...");

				NetInfoFrame panelNetInfo = NetInfoFrame.getInstance();
				panelNetInfo.setResizable(false);
				commonGraphicInitAlwaysOnTop(panelNetInfo, "Network info - ");

				prog.forceClose();
			}
		});
	}
	
	public void newHwInfoFrame() {
		
		GuiUtils.launchThreadSafeSwing(new Runnable() {
			@Override
			public void run() {
				UpdateProgressPanelMin prog = new UpdateProgressPanelMin(false);
				prog.getPb().setIndeterminate(true);
				prog.getLabel().setText("Loading System informations...");
				prog.getPb().setString("Loading...");
				
				HwInfoFrame panelHwInfo = HwInfoFrame.getInstance();
				commonGraphicInitAlwaysOnTop(panelHwInfo, "Hardware info - ");
				panelHwInfo.setResizable(false);
				
				prog.forceClose();
			}
		});
	}
	
	public AboutSectionFrame newAboutSectionFrame() {
		
		AboutSectionFrame panelAbout = AboutSectionFrame.getInstance(mainApplication);
		
		commonGraphicInit(panelAbout, "About - ");
		
		panelAbout.setResizable(false);
		
		return panelAbout;
	}
	
	public TutorialFrame newTutorialFrame(boolean visible) {
		
		try {
			TutorialFrame tutorialFrame = TutorialFrame.getInstance();
			
			if(visible) {
				Sounds.playSound(SoundsConfigurator.CLICK_2, null);
				commonGraphicInit(tutorialFrame, "Documentation - ");
				tutorialFrame.setResizable(true);
			}
			
			return tutorialFrame;
		} catch (Throwable e) {
			
			if (visible) {
				logger.error("Error loading HTML panel" + TutorialFrame.class, e);
				File htmlTutorialFile = new File(GeneralConfig.SCRYPT_TUTORIAL_HTML_PATH);
				boolean openWithBrowser = MainGUI.dialogHelper.yesOrNo("Current Java version does not support html reader, open with default browser?", null);
				if (openWithBrowser) {
					SysUtils.openUrlInBrowser(htmlTutorialFile.toURI().toString());
				}
			}
			
			return null;
		}
	}
	
	public ChangeLogFrame newChangeLogFrame() {
		
		ChangeLogFrame panelChangeLog = ChangeLogFrame.getInstance(GeneralConfig.CHANGE_LOG_FILE);
		
		commonGraphicInit(panelChangeLog, "ChangeLog - ");
		
		panelChangeLog.setResizable(true);
		
		return panelChangeLog;
	}
	
	// COMMONS INITIALIZATION METHODS //
	public void commonGraphicInit(ParentFrame newFrame, String title) {
		
		Sounds.playSound(SoundsConfigurator.CLICK_2, null);
		
		newFrame.setMinimumSize(newFrame.getDefaultDimension());
		newFrame.setPreferredSize(newFrame.getDefaultDimension());
		newFrame.setMaximumSize(newFrame.getDefaultDimension());
		
		newFrame.setLocationRelativeTo(mainApplication.getMainFrame());
		newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		newFrame.setTitle(title + GeneralConfig.APPLICATION_NAME);
		GuiUtils.extendFramesAndMoveToTop(mainApplication.getMainFrame(), newFrame,JFrame.MAXIMIZED_BOTH);
		GuiUtils.centerComponent(newFrame, newFrame.getDefaultDimension());
		
		newFrame.pack();
		
		newFrame.setVisible(true);
	}
	
	public void commonGraphicInitAlwaysOnTop(ParentFrame newFrame, String title) {
		commonGraphicInit(newFrame, title);
		newFrame.setAlwaysOnTop(true);
	}
}
