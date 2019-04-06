package gUI.panels;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.Wini;

import gUI.mainGUI;
import gUI.utils.filedialog.INIfileChoosER;
import iNIT.INItializer;
import net.miginfocom.swing.MigLayout;
import uTILS.local.ImageWorker;

import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.SystemColor;
import javax.swing.SwingConstants;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.border.LineBorder;

public class ChargeIniLog extends JPanel{
	
	// INItializer logger creation		
	static Logger logger = Logger.getLogger(ChargeIniLog.class); 
	
	// name used when using icons, remember to put in the correct folder or it will lanuch nullpointerException
	public final static String ICON_RELOAD_BLUE = "Resources_Scrypt/Icons/Reload.png";
	
	private INItializer config;
	private mainGUI parent;
	
	// GUI ELEMENTS //
	private JTextArea textArea;
	private JButton btnLoadCustomConfig;
	private JButton btnExportConfig;
	private JButton buttonReload;
	private JLabel lblAvviso;
	private JLabel config_PATH;
	private JButton btnSaveCurrentAsDefault;
	private JButton btnResetOriginalConfig;

	public ChargeIniLog(INItializer configuration, mainGUI parentFrame) {
		
		parent = parentFrame;
		config = configuration;
		
		logger.info(this.getClass().getName() + " Starting...");
		
		setBackground(Color.LIGHT_GRAY);
		
		setLayout(new MigLayout("", "[117.00][][][][][70,grow][50][100px][:80:250px]", "[:58.00:60px][35px][90%,grow]"));
		
		JScrollPane scrollPane = new JScrollPane();
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setFont(new Font("DejaVu Sans", Font.PLAIN, config.getGuiOpt().getLogFontSize()));
		textArea.append(config.toStringa()); 		
		
		btnExportConfig = new JButton("Export current");
		btnExportConfig.setForeground(Color.WHITE);
		btnExportConfig.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnExportConfig.setBackground(Color.DARK_GRAY);
		add(btnExportConfig, "cell 1 1,grow");
		
		btnSaveCurrentAsDefault = new JButton("Save current as Default");
		btnSaveCurrentAsDefault.setForeground(Color.WHITE);
		btnSaveCurrentAsDefault.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnSaveCurrentAsDefault.setBackground(Color.DARK_GRAY);
		add(btnSaveCurrentAsDefault, "cell 2 1,grow");
		
		btnResetOriginalConfig = new JButton("Reset original Config");
		btnResetOriginalConfig.setForeground(Color.WHITE);
		btnResetOriginalConfig.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnResetOriginalConfig.setBackground(new Color(119, 136, 153));
		add(btnResetOriginalConfig, "cell 3 1,grow");
		add(scrollPane, "cell 0 2 9 1,grow");
		
		lblAvviso = new JLabel("[Configuration loaded]");
		lblAvviso.setForeground(Color.DARK_GRAY);
		add(lblAvviso, "cell 5 1,alignx left");
		
		JLabel lblInitializationFrom = new JLabel("Current .ini file: ");
		lblInitializationFrom.setFont(new Font("Trajan Pro 3", Font.BOLD, 15));
		add(lblInitializationFrom, "cell 7 1,alignx right");
		
		config_PATH = new JLabel("");
		config_PATH.setHorizontalAlignment(SwingConstants.CENTER);
		config_PATH.setFont(new Font("Tahoma", Font.PLAIN, 16));
		add(config_PATH, "cell 8 1,alignx right");
		config_PATH.setText(config.getIniFile().getFile().getPath());
		
		JPanel panel_1 = new JPanel();
		panel_1.setForeground(SystemColor.menu);
		panel_1.setBorder(new LineBorder(Color.DARK_GRAY, 4));
		panel_1.setBackground(new Color(250, 235, 215));
		add(panel_1, "cell 0 0 9 1,grow");
		panel_1.setLayout(new MigLayout("", "[100%]", "[]"));
		
		JLabel lblConfigurationManager = new JLabel("Configuration Manager");
		lblConfigurationManager.setHorizontalAlignment(SwingConstants.CENTER);
		lblConfigurationManager.setForeground(Color.BLACK);
		lblConfigurationManager.setFont(new Font("Lithos Pro Regular", Font.BOLD, 22));
		lblConfigurationManager.setBackground(SystemColor.activeCaptionBorder);
		panel_1.add(lblConfigurationManager, "cell 0 0,alignx center");
		
		btnLoadCustomConfig = new JButton("Load Custom CONFIG");
		btnLoadCustomConfig.setForeground(Color.WHITE);
		btnLoadCustomConfig.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnLoadCustomConfig.setBackground(Color.DARK_GRAY);
		add(btnLoadCustomConfig, "cell 0 1,grow");
		
		buttonReload = new JButton("");
		buttonReload.setForeground(Color.WHITE);
		buttonReload.setBackground(new Color(119, 136, 153));
		buttonReload.setIcon(new ImageIcon(ICON_RELOAD_BLUE));
		add(buttonReload, "cell 4 1,grow");

		addHandlers();
		
		logger.info(this.getClass().getName() + " Started");
	}
	
	public void addHandlers() {
		
		// RELOAD CONFIG
		buttonReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					config.setIniFile(new Wini(config.getIniFile().getFile()));
					config.setIniReadFile(new Wini(config.getIniFile().getFile()));
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(parent.getMainFrame(), "Error during read of the file: " + config.getIniFile().getFile().getName(), "Load Error", JOptionPane.ERROR_MESSAGE);
				}
				lblAvviso.setText("[Configuration re-loaded!]");
				config_PATH.setText(config.getIniFile().getFile().getPath());
				textArea.setText("\n\n### CONFIGURATION - ACTUAL FILE CONFIG & VALUES ###\n\n" +config.toStringa());
			}
		});
		
		// LOAD CUSTOM
		btnLoadCustomConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.debug("Load CONFIG clicked");
				INIfileChoosER panel = new INIfileChoosER(config, textArea);
		    	panel.setBounds(20,20,parent.getMainFrame().getWidth(),parent.getMainFrame().getHeight()/2);  	
				lblAvviso.setForeground(ImageWorker.randomColorRGB());
		    	lblAvviso.setText("[Configuration Loaded!]");
				config_PATH.setText(config.getIniFile().getFile().getPath());
				textArea.setText("\n\n### CONFIGURATION - ACTUAL FILE CONFIG & VALUES ###\n\n" +config.toStringa());
			}
		});
		
		// SAVE CURRENT AS DEFAULT CONFIG
		btnExportConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.debug("Save CONFIG clicked");
		    	INIfileChoosER panel = new INIfileChoosER(config);
		    	panel.setBounds(20,20,400,300);
		    	lblAvviso.setText("[Configuration Exported!]");
			}
		});
		
		// RELOAD STANDARD SCRYPT CONFIGURATION
		btnResetOriginalConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(parent.getMainFrame(), "Really want to Reset standard ScrypT configuration? Unsaved changes will be lost! ",
						"Warning - Reset INI standard config?",
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					try {
						FileUtils.copyFile(new File(mainGUI.BACKUP_INI), new File(mainGUI.DEFAULT_INI));
						config.setIniReadFile(new Wini(new File(mainGUI.DEFAULT_INI)));
						config.setIniFile(new Wini(new File(mainGUI.DEFAULT_INI)));
					} catch (IOException exc) {
						JOptionPane.showMessageDialog(parent.getMainFrame(),
								"Error resetting standard configuration: it does not exist or IO error occurred, retry later!");
						logger.error("Exception happened!", exc);
					}
					String name = mainGUI.askUsername();
					JOptionPane.showMessageDialog(parent.getMainFrame(), "File '" + config.getIniFile().getFile().getName() + "' loaded!");
					config.readConfigIniFile();
					config.getGenOpt().setUserName(name);
					config_PATH.setText(config.getIniFile().getFile().getPath());
					lblAvviso.setText("[Configuration Resetted!]");
					textArea.setText("\n\n### CONFIGURATION - ACTUAL FILE CONFIG & VALUES ###\n\n" +config.toStringa());
				} else {
					JOptionPane.showMessageDialog(parent.getMainFrame(),"Configuration not loaded, current CONFIG mantained!");
				} 
			}
		});
		
		// SAVE CURRENT AS DEFAULT
		btnSaveCurrentAsDefault.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					config.setIniFile(new Wini(new File(mainGUI.DEFAULT_INI)));
					config.setIniReadFile(new Ini(new File(mainGUI.DEFAULT_INI)));

				} catch (IOException e1) {
					logger.error("Exception happened!", e1);
				}
				if (JOptionPane.showConfirmDialog(parent.getMainFrame(), "Really want to overwrite default configuration?",
						"Warning - Overwrite default configuration?",
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					config.writeCurrentConfigIniFile();
					lblAvviso.setText("[Configuration Saved as new Default!]");
					config_PATH.setText(config.getIniFile().getFile().getPath());
					textArea.setText("\n\n### CONFIGURATION - ACTUAL FILE CONFIG & VALUES ###\n\n" +config.toStringa());
					JOptionPane.showMessageDialog(parent.getMainFrame(),"File " + config.getIniFile().getFile().getAbsolutePath() + " saved!");
				} else {
					JOptionPane.showMessageDialog(parent.getMainFrame(), "Configuration not saved!");
				}
			}
		});
	}
}
