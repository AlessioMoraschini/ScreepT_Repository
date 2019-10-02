package gUI.utils.mainGui;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import frameutils.GuiUtils;
import gUI.panels.ChargeIniLog;
import gUI.panels.CrypterPanel;
import gUI.panels.TextEditor;
import maingUI.MainGUI;
import mainobj.INItializer;
import media.Sounds;
import resources.SoundsConfigurator;

public class MainGuiUtils {

	public MainGUI mainAppReference;
	
	public MainGuiUtils(MainGUI mainApp) {
		mainAppReference = mainApp;
	}
	
	/**
	 * Use this function to reset panel to initial state
	 * @param <T>
	 * @param className
	 */
	public <T> void resetPanel(Class<T>target, Class<?> selectednow, JFrame instance) {
		final boolean txtEditor, crypter, configuration, home;
		
		txtEditor = (instance.getContentPane().getComponent(0).equals(MainGUI.initTxtWriter))? true : false;
		crypter = (instance.getContentPane().getComponent(0).equals(MainGUI.panelCrypter))? true : false;
		configuration = (instance.getContentPane().getComponent(0).equals(mainAppReference.configManagerPanel))? true : false;
		home = (!txtEditor && !crypter && !configuration)? true : false;
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(ChargeIniLog.class.equals(target)) {
					startConfigManagerPanel(true);
				}else if(TextEditor.class.equals(target)) {
					startTextEditor(true);
				}else if(CrypterPanel.class.equals(target)) {
					startCrypterPanel(true);
				}else if(target == null) {
					startConfigManagerPanel(true);
					startCrypterPanel(true);
					startTextEditor(true);
				}
		
				if (txtEditor) {
					mainAppReference.getMntmTextEditor().doClick();
				} else if (crypter) {
					mainAppReference.getMntmCrypter().doClick();
				} else if (configuration) {
					mainAppReference.getMntmInspectCurrentConfiguration().doClick();
				} else if (home) {
					mainAppReference.getMntmReturnToWelcome().doClick();
				}
			}
		});
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
    	
    	mainAppReference.currentConfiguration.writeSingleValue(
    			INItializer.GEN_OPT, "lastPanelOpened", ChargeIniLog.class.getCanonicalName());
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
    	mainAppReference.currentConfiguration.writeSingleValue(
    			INItializer.GEN_OPT, "lastPanelOpened", CrypterPanel.class.getCanonicalName());
	}
	
	public void startTextEditor(boolean forceRestart) {
		
		mainAppReference.getMainFrame().setCursor(GuiUtils.CURSOR_WAIT);
		
		JPanel contentPane = (JPanel) mainAppReference.getMainFrame().getContentPane();
		if(MainGUI.initTxtWriter == null || forceRestart) {
			Sounds.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
			MainGUI.initTxtWriter = new TextEditor(mainAppReference, MainGUI.currentConfiguration);
		}		    	
    	contentPane.removeAll();
    	contentPane.add(MainGUI.initTxtWriter);
    	contentPane.revalidate(); 
    	contentPane.repaint();
    	
    	mainAppReference.currentConfiguration.writeSingleValue(
    			INItializer.GEN_OPT, "lastPanelOpened", TextEditor.class.getCanonicalName());
    	mainAppReference.getMainFrame().setCursor(GuiUtils.CURSOR_DEFAULT);
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
