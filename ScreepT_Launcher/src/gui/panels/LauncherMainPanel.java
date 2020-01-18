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
package gui.panels;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import gui.GUIStyelSettings;
import gui.GuiUtils;
import mainlauncher.ScreepTLauncher;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import java.awt.BorderLayout;
import java.awt.Color;
import net.miginfocom.swing.MigLayout;
import resources.ImagesPathConfigurator;
import resources.SoundsConfigurator;
import resources.SoundsManagerExtended;

import javax.swing.JLabel;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.awt.Cursor;
import java.awt.Insets;

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
	
	private final int W = 570;
	private final int H = 170;
	
	public LauncherMainPanel(String frameTitle, ScreepTLauncher launcher) {
		
		logger.info(this.getClass().getName() + " Starting...");
		
		LAUNCHER_GUI = this;
		
		setGraphics();
		
		this.LAUNCHER = launcher;
		this.setTitle(frameTitle);

		GuiUtils.centerFrame(this, W, H);
		this.setResizable(false);
		this.setAlwaysOnTop(true);
		
		mainPanel = new JPanel();
		mainPanel.setBackground(Color.DARK_GRAY);
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(new MigLayout("insets 4, fill", "[grow][40px:n,grow][40px:n,grow][20px:n,grow][40px:n,grow][20px]", "[30px,grow][:27px:27px][:27px:27px][25px]"));
		
		JLabel lblWelcomeToScreept = new JLabel("Welcome to ScreepT, choose your preferences... or just start me :)");
		lblWelcomeToScreept.setForeground(Color.WHITE);
		lblWelcomeToScreept.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
		mainPanel.add(lblWelcomeToScreept, "cell 0 0 6 1,alignx center");
		
		btnStartScreept = new JButton("Start ScreepT");
		btnStartScreept.setMargin(new Insets(10, 10, 10, 10));
		btnStartScreept.setForeground(new Color(0, 0, 0));
		btnStartScreept.setBorder(null);
		btnStartScreept.setBackground(new Color(102, 153, 51));
		btnStartScreept.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnStartScreept.setFont(new Font("Segoe UI", Font.BOLD, 20));
		mainPanel.add(btnStartScreept, "cell 3 1 3 2,grow");
		
		btnCustomizeParameters = new JButton("Customize launch parameters");
		btnCustomizeParameters.setBorder(null);
		btnCustomizeParameters.setForeground(new Color(0, 0, 0));
		btnCustomizeParameters.setBackground(new Color(169, 169, 169));
		btnCustomizeParameters.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnCustomizeParameters.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		mainPanel.add(btnCustomizeParameters, "cell 0 1 3 1,grow");
		
		btnDependencyManager = new JButton("Open Dependency Manager");
		btnDependencyManager.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnDependencyManager.setForeground(Color.BLACK);
		btnDependencyManager.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		btnDependencyManager.setBorder(null);
		btnDependencyManager.setBackground(new Color(169, 169, 169));
		mainPanel.add(btnDependencyManager, "cell 0 2 3 1,grow");
		
		lblAmDesign = new JLabel("A.M. Design & Development");
		lblAmDesign.setForeground(Color.LIGHT_GRAY);
		lblAmDesign.setFont(new Font("SansSerif", Font.PLAIN, 10));
		mainPanel.add(lblAmDesign, "cell 0 3 2 1,alignx left,aligny bottom");
		
		lblScreeptLauncher = new JLabel("ScreepT Launcher");
		lblScreeptLauncher.setFont(new Font("SansSerif", Font.PLAIN, 12));
		lblScreeptLauncher.setForeground(new Color(240, 248, 255));
		mainPanel.add(lblScreeptLauncher, "cell 2 3 2 1,alignx center,aligny bottom");
		
		label = new JLabel("ScreepT Launcher - V1.0");
		label.setForeground(Color.LIGHT_GRAY);
		label.setFont(new Font("SansSerif", Font.PLAIN, 10));
		mainPanel.add(label, "cell 4 3 2 1,alignx right,aligny bottom");
	
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
				
				SoundsManagerExtended.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
				try {
					Thread.sleep(400);
				} catch (InterruptedException e1) {
				}
				ScreepTLauncher.launchCommands(ScreepTLauncher.initPath, ScreepTLauncher.iniFileManager.viewShellAfterStart);
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
		GUIStyelSettings.setStyle(GUIStyelSettings.NimbusLookAndFeel, 1);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUIStyelSettings.setFocusDefaultBtn();
			}
		});
	}
	
}
