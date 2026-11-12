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
package screept.launcher.gui.panels;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import mainlauncher.ScreepTLauncher;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import java.awt.Color;
import net.miginfocom.swing.MigLayout;
import os.commons.utils.SysUtils;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import resources.ImagesPathConfigurator;
import resources.SoundsConfigurator;
import resources.SoundsManagerExtended;
import various.common.light.gui.GUIStyelSettings;
import various.common.light.gui.GuiUtils;
import various.common.light.network.proxy.ProxySettingsGUI;

import javax.swing.JLabel;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Dialog.ModalityType;
import java.awt.Image;
import javax.swing.BorderFactory;
import screept.launcher.gui.utils.LauncherTheme;
import screept.launcher.utils.IniFileManager;
import screept.launcher.gui.utils.LauncherBackdropPanel;

public class LauncherMainPanel extends JFrame{
	private static final long serialVersionUID = 5822120722073962394L;

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(LauncherMainPanel.class); 
	
	public static final int TOOLTIP_DELAY = 200;
			
	public static final String WindowsLookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
	public static final String NimbusLookAndFeel = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";

	public ScreepTLauncher LAUNCHER;
	public LauncherMainPanel LAUNCHER_GUI;
	public CustomizeParametersPanel customizerPanel;
	public DependenciesManagerPanel missingDependenciesPanel;
	
	// GUI VARIABLES
	private JPanel mainPanel;
	private JButton btnStartScreept;
	private JButton btnCustomizeParameters;
	private JButton btnDependencyManager;
	private JLabel lblScreeptLauncher;
	private JLabel label;
	private JLabel lblAmDesign;
	
	private final int W = 800;
	private final int H = 590;

	/** Heights of the header and of the controls area: the pixel art icon is centered in the space between */
	private static final int HEADER_HEIGHT = 74;
	private static final int CONTROLS_HEIGHT = 176;
	private JButton btnNetworkConfig;
	private JCheckBox chkHideLauncher;
	/** "Hide launcher at start" as it was when the launcher opened */
	private boolean initialHide;
	
	public LauncherMainPanel(String frameTitle, ScreepTLauncher launcher) {
		
		logger.info(this.getClass().getName() + " Starting...");
		
		LAUNCHER_GUI = this;
		
		setGraphics();
		
		this.LAUNCHER = launcher;
		this.setTitle(frameTitle);

		GuiUtils.centerFrame(this, W, H);
		this.setResizable(false);
		this.setAlwaysOnTop(!ScreepTLauncher.settingsOnly);
		
		// animated pixel art background (teal code rain drawing the ScreepT icon, like the home page)
		mainPanel = new LauncherBackdropPanel(HEADER_HEIGHT, CONTROLS_HEIGHT);
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(new MigLayout("insets 0, fill", "[grow]", "[" + HEADER_HEIGHT + "!][grow,fill][" + CONTROLS_HEIGHT + "!]"));

		// header over the animation
		JPanel header = new JPanel(new MigLayout("insets 16 24 0 24, fillx", "[grow][]", "[]2[]"));
		header.setOpaque(false);

		lblScreeptLauncher = new JLabel("ScreepT Launcher");
		lblScreeptLauncher.setFont(lblScreeptLauncher.getFont().deriveFont(Font.BOLD, 24f));
		lblScreeptLauncher.setForeground(new Color(0xE8FFFC));
		header.add(lblScreeptLauncher, "cell 0 0");

		JLabel lblWelcomeToScreept = new JLabel(ScreepTLauncher.settingsOnly ? "Launcher settings: they apply from the next start of ScreepT" : "Choose your preferences, or just start :)");
		lblWelcomeToScreept.setForeground(new Color(0x7FBFB8));
		header.add(lblWelcomeToScreept, "cell 0 1");

		label = new JLabel("Launcher V1.0");
		label.setForeground(new Color(0x7FBFB8));
		label.setFont(label.getFont().deriveFont(11f));
		header.add(label, "cell 1 0,alignx right,aligny top");
		mainPanel.add(header, "cell 0 0,growx");

		// the animation shows through here
		JPanel stage = new JPanel();
		stage.setOpaque(false);
		mainPanel.add(stage, "cell 0 1,grow");

		// controls: always visible at the bottom, Start is big
		JPanel controls = new LauncherBackdropPanel.Glass(new MigLayout("insets 16 26 10 26, fillx", "[grow][grow][]", "[]12[]6[]"));

		btnCustomizeParameters = new JButton("Customize launch parameters");
		btnCustomizeParameters.setToolTipText("Memory, Java version, log level and the other start options");
		btnCustomizeParameters.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		controls.add(btnCustomizeParameters, "cell 0 0,grow,h 36!");

		btnDependencyManager = new JButton("Open Dependency Manager");
		btnDependencyManager.setToolTipText("Check and install what ScreepT needs (Java and libraries)");
		btnDependencyManager.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		controls.add(btnDependencyManager, "cell 1 0,grow,h 36!");

		btnNetworkConfig = new JButton("");
		btnNetworkConfig.setToolTipText("Network and proxy settings");
		btnNetworkConfig.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnNetworkConfig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_NETWORK));
		controls.add(btnNetworkConfig, "cell 2 0,h 36!,w 48!");

		btnStartScreept = new JButton(ScreepTLauncher.settingsOnly ? "Close" : "Start ScreepT");
		btnStartScreept.setToolTipText(ScreepTLauncher.settingsOnly ? "Close the launcher settings (nothing is started)" : "Start ScreepT with the current options");
		btnStartScreept.setFont(btnStartScreept.getFont().deriveFont(Font.BOLD, 24f));
		btnStartScreept.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		controls.add(btnStartScreept, "cell 0 1 3 1,grow,h 66!");

		lblAmDesign = new JLabel("A.M. Design & Development");
		lblAmDesign.setForeground(new Color(0x6C8F8B));
		lblAmDesign.setFont(lblAmDesign.getFont().deriveFont(11f));
		initialHide = ScreepTLauncher.iniFileManager.hideNStartDirect;
		chkHideLauncher = new JCheckBox("Hide launcher at start");
		chkHideLauncher.setOpaque(false);
		chkHideLauncher.setForeground(new Color(0xBFE8E3));
		chkHideLauncher.setSelected(initialHide);
		chkHideLauncher.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chkHideLauncher.setToolTipText("<html>ScreepT starts directly, without showing this window.<br>"
				+ "To bring the launcher back: in ScreepT, Preferences &gt; Launcher Settings.</html>");
		// in the settings-only window nothing is started, so the choice is saved right away
		chkHideLauncher.addActionListener(e -> {
			if (ScreepTLauncher.settingsOnly) {
				writeHideFlag(chkHideLauncher.isSelected());
			}
		});
		controls.add(chkHideLauncher, "cell 0 2,alignx left,aligny bottom");
		controls.add(lblAmDesign, "cell 1 2 2 1,alignx right,aligny bottom");
		mainPanel.add(controls, "cell 0 2,growx");

		addHandlers();
		
		SoundsManagerExtended.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
		logger.info(this.getClass().getName() + " Started");
	}
	
	// button handler settings
	private void addHandlers() {
		
		// default close operation(same as reset) 
		LAUNCHER_GUI.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	logger.debug("Closing Frame: " + LAUNCHER_GUI.getClass().getName());	        	
	        	System.exit(0);
		    }
		});
		
		btnStartScreept.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (ScreepTLauncher.settingsOnly) {
					LAUNCHER_GUI.dispatchEvent(new WindowEvent(LAUNCHER_GUI, WindowEvent.WINDOW_CLOSING));
					return;
				}
				if (!confirmHideChoice()) {
					return;
				}
				SoundsManagerExtended.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
				try {
					Thread.sleep(400);
				} catch (InterruptedException e1) {
				}
				SysUtils.launchBatchOrBash(ScreepTLauncher.generatedNativeLauncherPath, !ScreepTLauncher.iniFileManager.viewShellAfterStart);
				LAUNCHER_GUI.dispatchEvent(new WindowEvent(LAUNCHER_GUI, WindowEvent.WINDOW_CLOSING));
			}
		});
		
		btnCustomizeParameters.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(customizerPanel == null || !CustomizeParametersPanel.active) {
					customizerPanel = new CustomizeParametersPanel("Customize start options", LAUNCHER_GUI);
					customizerPanel.setVisible(true);
					customizerPanel.setAlwaysOnTop(true);
				}else {
					customizerPanel.setState(java.awt.Frame.NORMAL);
					customizerPanel.requestFocus();
				}
			}
		});
		
		btnDependencyManager.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (DependenciesManagerPanel.active == false) {
					missingDependenciesPanel = new DependenciesManagerPanel("Dependency Manager", LAUNCHER_GUI);
					missingDependenciesPanel.setVisible(true);
					missingDependenciesPanel.requestFocus();
				} else {
					missingDependenciesPanel.toFront();
				}
			}
		});
		
		btnNetworkConfig.addActionListener((e) -> {
			GeneralConfig.reloadProperties();
			ProxySettingsGUI.frameIcon = new ImageIcon(IconsPathConfigurator.ICON_NETWORK);
			new ProxySettingsGUI(ModalityType.TOOLKIT_MODAL, false, GeneralConfig.DEF_PROXY_CONFIG_STRING);
		});
	}
	
	private void writeHideFlag(boolean hide) {
		ScreepTLauncher.iniFileManager.writeValueToIniConfiguration(IniFileManager.INI_APP_OPT, "flagHideNStartDirect", String.valueOf(hide));
		ScreepTLauncher.iniFileManager.hideNStartDirect = hide;
		initialHide = hide;
	}

	/**
	 * Applies the "Hide launcher at start" choice when Start is pressed. Turning it on (it was off when the launcher opened)
	 * asks for confirmation and explains how to bring the launcher back.
	 *
	 * @return false if the user cancelled the start
	 */
	private boolean confirmHideChoice() {
		boolean wantHide = chkHideLauncher.isSelected();
		if (wantHide == initialHide) {
			return true;
		}
		if (!wantHide) {
			writeHideFlag(false);
			return true;
		}
		String text = "<html><b>Hide the launcher from now on?</b><br><br>"
				+ "ScreepT will start directly, without showing this window.<br><br>"
				+ "To bring the launcher back:<br>"
				+ "&bull; in ScreepT open <b>Preferences &gt; Launcher Settings</b> and untick <i>Hide launcher at start</i>;<br>"
				+ "&bull; or open <b>Preferences &gt; General Options</b> and untick <i>Hide launcher at start</i>;<br>"
				+ "&bull; or use <b>Help &gt; Restart</b>: it asks if the launcher must open this time.</html>";
		Object[] options = {"Hide and start", "Start, keep showing", "Cancel"};
		int choice = JOptionPane.showOptionDialog(this, text, "Hide launcher at start", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (choice == 0) {
			writeHideFlag(true);
			return true;
		}
		if (choice == 1) {
			chkHideLauncher.setSelected(false);
			return true;
		}
		return false;
	}

	private void setGraphics(){
		// SW window icon
		try {
			LAUNCHER_GUI.setIconImage(ImageIO.read(new File(ImagesPathConfigurator.MAIN_APP_ICON)));
			logger.debug("icon image setted to main window");
			ToolTipManager.sharedInstance().setInitialDelay(TOOLTIP_DELAY);
		} catch (IOException e1) {
			logger.error("Exception happened!", e1);
		}
		// initialize default UI
		LauncherTheme.apply();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUIStyelSettings.setFocusDefaultBtn();
			}
		});
	}
	
	private boolean themed = false;

	@Override
	public void setVisible(boolean visible) {
		if (visible && !themed) {
			themed = true;
			// colors are already defined by this panel: only the main action gets the accent
			LauncherTheme.primary(btnStartScreept);
		}
		super.setVisible(visible);
	}
}
