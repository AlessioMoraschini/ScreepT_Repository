package gUI.utils.mainGui;

import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import frameutils.GuiUtils;
import gUI.panels.ChargeIniLog;
import gUI.panels.CrypterPanel;
import gUI.panels.TextEditor;
import maingUI.MainGUI;
import media.Sounds;
import resources.SoundsConfigurator;

public class MainGuiUtils {

	public MainGUI mainAppReference;
	
	public MainGuiUtils(MainGUI mainApp) {
		mainAppReference = mainApp;
	}
	
	/**
	 * Use this function to reset panel to initial state
	 * @param className
	 */
	public void resetPanel(String className) {
		if(TextEditor.class.getSimpleName().equals(className)) {
			startTextEditor(true);
		}else if(CrypterPanel.class.getSimpleName().equals(className)) {
			startCrypterPanel(true);
		}else if(ChargeIniLog.class.getSimpleName().equals(className)) {
			startConfigManagerPanel(true);
		}else if("ALL".equalsIgnoreCase(className)) {
			startTextEditor(true);
			startCrypterPanel(true);
			startConfigManagerPanel(true);
		}
	}
	
	public void startConfigManagerPanel(boolean forceStart) {
		JPanel contentPane = (JPanel) mainAppReference.getMainFrame().getContentPane();
		if (mainAppReference.configManagerPanel == null || forceStart) {
			Sounds.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
			mainAppReference.configManagerPanel = new ChargeIniLog(MainGUI.currentConfiguration, mainAppReference);
		}
		contentPane.removeAll();
    	contentPane.add(mainAppReference.configManagerPanel);
    	contentPane.revalidate(); 
    	contentPane.repaint();
	}
	
	public void startCrypterPanel(boolean forceStart) {
		JPanel contentPane = (JPanel) mainAppReference.getMainFrame().getContentPane();
    	if(MainGUI.panelCrypter == null || forceStart) {
    		Sounds.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
    		MainGUI.panelCrypter = new CrypterPanel(MainGUI.currentConfiguration, mainAppReference);
    	}
    	contentPane.removeAll();
    	contentPane.add(MainGUI.panelCrypter);
    	contentPane.revalidate(); 
    	contentPane.repaint();
	}
	
	public void startTextEditor(boolean forceRestart) {
		
		mainAppReference.getMainFrame().setCursor(GuiUtils.CURSOR_WAIT);
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				JPanel contentPane = (JPanel) mainAppReference.getMainFrame().getContentPane();
				if(MainGUI.initTxtWriter == null || forceRestart) {
					Sounds.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
					MainGUI.initTxtWriter = new TextEditor(mainAppReference, MainGUI.currentConfiguration);
				}		    	
		    	contentPane.removeAll();
		    	contentPane.add(MainGUI.initTxtWriter);
		    	contentPane.revalidate(); 
		    	contentPane.repaint();
		    	mainAppReference.getMainFrame().setCursor(GuiUtils.CURSOR_DEFAULT);
			}
		});
	}
	
	/**
	 * Use this to retrieve all panels thare are restartable using the method
	 * ResetPanel(String className) -> just use a class name from the given arrayList or ALL
	 * the reset all panels
	 * @return
	 */
	public static ArrayList<String> getResettablePanels() {
		ArrayList<String> resettablePanels = new ArrayList<String>();
		resettablePanels.add(TextEditor.class.getSimpleName());
		resettablePanels.add(CrypterPanel.class.getSimpleName());
		resettablePanels.add(ChargeIniLog.class.getSimpleName());
		return resettablePanels;
	}
	
}
