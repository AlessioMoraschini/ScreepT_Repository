package gUI.utils.mainGui;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import frameutils.utils.GuiUtils;
import gUI.panels.ChargeIniLog;
import gUI.panels.CrypterPanel;
import gUI.panels.TextEditor;
import maingUI.MainGUI;
import mainobj.INItializer;
import media.Sounds;
import resources.SoundsConfigurator;

public class MainGuiUtils {
	
	// MainGuiUtils logger
	static Logger logger = Logger.getLogger(MainGuiUtils.class);

	public MainGUI mainAppReference;
	
	public MainGuiUtils(MainGUI mainApp) {
		mainAppReference = mainApp;
	}
	
	/**
	 * Use this function to reset panel to initial state
	 * @param <T>
	 * @param target if null all will be resetted, else use this to specify class to reset
	 */
	public <T> void resetPanel(Class<T>target, JFrame instance) {
		final boolean txtEditor, crypter, configuration, home;
		
		String targetStr = (target != null)? target.getName() : "All Panels";
		logger.info(" Starting reset -> " +targetStr);
		
		txtEditor = (instance.getContentPane().getComponent(0).equals(MainGUI.initTxtWriter))? true : false;
		crypter = (instance.getContentPane().getComponent(0).equals(MainGUI.panelCrypter))? true : false;
		configuration = (instance.getContentPane().getComponent(0).equals(mainAppReference.configManagerPanel))? true : false;
		home = (!txtEditor && !crypter && !configuration)? true : false;
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				// start correct panel
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
		
				// Now select last selected before refresh
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
		
		logger.info("Reset finished [" +targetStr + "]");
	}
	
	public void startConfigManagerPanel(boolean forceStart) {
		
		mainAppReference.isOnHomePage.set(false);
		
		JPanel contentPane = (JPanel) mainAppReference.getMainFrame().getContentPane();

		// reset all listeners
		if (forceStart) {
			removeAllListeners(mainAppReference.configManagerPanel);
		}
		
		if (mainAppReference.configManagerPanel == null || forceStart) {
			Sounds.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
			mainAppReference.configManagerPanel = new ChargeIniLog(MainGUI.currentConfiguration, mainAppReference);
		}
		
		contentPane.removeAll();
    	contentPane.add(mainAppReference.configManagerPanel);
    	contentPane.revalidate(); 
    	contentPane.repaint();
    	
    	MainGUI.currentConfiguration.getGenOpt().setLastPanelOpened(ChargeIniLog.class);
    	MainGUI.currentConfiguration.writeSingleValue(
    			INItializer.GEN_OPT, "lastPanelOpened", ChargeIniLog.class.getCanonicalName());
	}
	
	public void startCrypterPanel(final boolean forceStart) {
		
		mainAppReference.isOnHomePage.set(false);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainAppReference.getMainFrame().setCursor(GuiUtils.CURSOR_WAIT);
				final JPanel contentPane = (JPanel) mainAppReference.getMainFrame().getContentPane();
				// reset all listeners
				if (forceStart) {
					removeAllListeners(MainGUI.panelCrypter);
				}
				if (MainGUI.panelCrypter == null || forceStart) {
					Sounds.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
					MainGUI.panelCrypter = new CrypterPanel(MainGUI.currentConfiguration, mainAppReference);
				}
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						contentPane.removeAll();
						contentPane.add(MainGUI.panelCrypter);
						contentPane.revalidate();
						contentPane.repaint();
						MainGUI.currentConfiguration.getGenOpt().setLastPanelOpened(CrypterPanel.class);
						MainGUI.currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "lastPanelOpened", CrypterPanel.class.getCanonicalName());
					}
				});
				mainAppReference.getMainFrame().setCursor(GuiUtils.CURSOR_DEFAULT);
			}
		});
	}
	
	public void startTextEditor(final boolean forceRestart) {
		
		mainAppReference.isOnHomePage.set(false);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainAppReference.getMainFrame().setCursor(GuiUtils.CURSOR_WAIT);
				final JPanel contentPane = (JPanel) mainAppReference.getMainFrame().getContentPane();
				// reset shortcut listener
				if (forceRestart && MainGUI.initTxtWriter != null && MainGUI.initTxtWriter.shortcutsListener != null) {
					KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(MainGUI.initTxtWriter.shortcutsListener);
					removeAllListeners(MainGUI.panelCrypter);
				}
				if (MainGUI.initTxtWriter == null || forceRestart) {
					Sounds.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
					MainGUI.initTxtWriter = new TextEditor(mainAppReference, MainGUI.currentConfiguration);
				}
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						contentPane.removeAll();
						contentPane.add(MainGUI.initTxtWriter);
						contentPane.revalidate();
						contentPane.repaint();
						MainGUI.updateGuiFromCurrConfig();
					}
				});
				MainGUI.currentConfiguration.getGenOpt().setLastPanelOpened(TextEditor.class);
				MainGUI.currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "lastPanelOpened", TextEditor.class.getCanonicalName());
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
	@Deprecated
	public static ArrayList<String> getResettablePanels() {
		ArrayList<String> resettablePanels = new ArrayList<String>();
		resettablePanels.add(TextEditor.class.getSimpleName());
		resettablePanels.add(CrypterPanel.class.getSimpleName());
		resettablePanels.add(ChargeIniLog.class.getSimpleName());
		return resettablePanels;
	}
	
	private void removeAllListeners(Component target) {
		if(target == null) {
			return;
		}
		ComponentListener[] listeners = target.getComponentListeners();
		for(ComponentListener listener : listeners) {
			target.removeComponentListener(listener);
		}
		
	}
	
	private void removeKeyListeners(Component target) {
		if(target == null) {
			return;
		}
		KeyListener[] listeners = target.getKeyListeners();
		for(KeyListener listener : listeners) {
			target.removeKeyListener(listener);
		}
	}

	private void removeMouseListeners(Component target) {
		if(target == null) {
			return;
		}
		MouseListener[] listeners = target.getMouseListeners();
		for(MouseListener listener : listeners) {
			target.removeMouseListener(listener);
		}
	}

	private void removeMouseWheelListeners(Component target) {
		if(target == null) {
			return;
		}
		MouseWheelListener[] listeners = target.getMouseWheelListeners();
		for(MouseWheelListener listener : listeners) {
			target.removeMouseWheelListener(listener);
		}
	}
	
}
