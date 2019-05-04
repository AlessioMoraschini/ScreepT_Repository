package gUI.panels;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.Wini;

import gUI.utils.filedialog.INIfileChoosER;
import gUI.utils.tree.FileTreeModel;
import gUI.utils.tree.iniconfmanager.FileCellRendererIni;
import gUI.utils.tree.iniconfmanager.TreeFileMouseListenerIni;
import iNIT.INItializer;
import maingUI.mainGUI;
import net.miginfocom.swing.MigLayout;
import resources.IconsPathConfigurator;
import uTILS.local.ImageWorker;

import java.awt.Font;
import java.awt.Point;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.SystemColor;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JFrame;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.border.LineBorder;
import javax.swing.tree.TreeModel;

import java.awt.Cursor;
import javax.swing.border.BevelBorder;
import javax.swing.JTree;
import javax.swing.JSeparator;

public class ChargeIniLog extends JPanel{
	
	// INItializer logger creation		
	static Logger logger = Logger.getLogger(ChargeIniLog.class); 
	
	// name used when using icons, remember to put in the correct folder or it will lanuch nullpointerException
	public final static String ICON_RELOAD_BLUE = "Resources_Scrypt/Icons/Reload.png";
	
	private INItializer config;
	public mainGUI parent;
	private ChargeIniLog thisPanel;
	
	// GUI ELEMENTS //
	public JTextArea textArea;
	private JScrollPane scrollPaneTreeConfigs;
	private JScrollPane scrollPaneTextArea;

	private JButton btnLoadCustomConfig;
	private JButton btnExportConfig;
	private JButton buttonReload;
	private JButton btnSaveCurrentAsDefault;
	private JButton btnResetOriginalConfig;
	
	public JLabel lblAvviso;
	public JLabel config_PATH;
	public JPanel panelDynPath;
	private JLabel lblPreferredConfigurations;
	private JSeparator separator;

	private JTree tree;
	private JButton btnSave;

	public ChargeIniLog(INItializer configuration, mainGUI parentFrame) {
		setBorder(new BevelBorder(BevelBorder.LOWERED, new Color(112, 128, 144), new Color(105, 105, 105), new Color(0, 0, 0), new Color(0, 0, 0)));
		
		thisPanel = this;
		parent = parentFrame;
		config = configuration;
		
		logger.info(this.getClass().getName() + " Starting...");
		
		setBackground(new Color(211, 211, 211));
		
		setLayout(new MigLayout("", "[117.00][70px][50px][120px][30px][][140px:140px][grow][50][100px,grow,leading][100px,left]", "[:58.00:60px][35px][][20px][400px,grow]"));
		
		scrollPaneTextArea = new JScrollPane();
		scrollPaneTextArea.setAutoscrolls(false);
		scrollPaneTextArea.setBorder(new LineBorder(new Color(64, 64, 64), 3, true));
		textArea = new JTextArea();
		textArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		textArea.setBorder(null);
		scrollPaneTextArea.setViewportView(textArea);
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setFont(new Font("DejaVu Sans", Font.PLAIN, config.getGuiOpt().getLogFontSize()));
		textArea.append(config.toStringa()); 		
		
		btnExportConfig = new JButton("Export");
		btnExportConfig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_EXPORT_CONF));
		btnExportConfig.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnExportConfig.setForeground(Color.BLACK);
		btnExportConfig.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnExportConfig.setBackground(new Color(176, 196, 222));
		add(btnExportConfig, "cell 1 1,grow");
		
		btnSave = new JButton("Save");
		btnSave.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SAVE_CONF));
		btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSave.setForeground(Color.BLACK);
		btnSave.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnSave.setBackground(new Color(176, 196, 222));
		add(btnSave, "cell 2 1,grow");
		
		btnSaveCurrentAsDefault = new JButton("Save as Default");
		btnSaveCurrentAsDefault.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SAVE_AS_DEFAULT_CONF));
		btnSaveCurrentAsDefault.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSaveCurrentAsDefault.setForeground(Color.BLACK);
		btnSaveCurrentAsDefault.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnSaveCurrentAsDefault.setBackground(new Color(176, 196, 222));
		add(btnSaveCurrentAsDefault, "cell 3 1 2 1,grow");
		
		buttonReload = new JButton("Reload");
		buttonReload.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		buttonReload.setForeground(Color.BLACK);
		buttonReload.setBackground(new Color(176, 196, 222));
		buttonReload.setIcon(new ImageIcon(ICON_RELOAD_BLUE));
		add(buttonReload, "cell 5 1,grow");
		
		panelDynPath = new JPanel();
		panelDynPath.setOpaque(false);
		add(panelDynPath, "cell 7 1 3 1,alignx center,growy");
		panelDynPath.setLayout(new MigLayout("", "[grow][150px,grow]", "[]"));
		
		JLabel lblInitializationFrom = new JLabel("Current .ini file: ");
		panelDynPath.add(lblInitializationFrom, "cell 0 0,growx");
		lblInitializationFrom.setFont(new Font("Segoe UI", Font.BOLD, 15));
		
		config_PATH = new JLabel("");
		panelDynPath.add(config_PATH, "cell 1 0,growx");
		config_PATH.setHorizontalAlignment(SwingConstants.LEFT);
		config_PATH.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		config_PATH.setText(config.getIniFile().getFile().getPath());
		
		btnResetOriginalConfig = new JButton("Reset Original");
		btnResetOriginalConfig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RESET_CONF));
		btnResetOriginalConfig.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnResetOriginalConfig.setForeground(Color.BLACK);
		btnResetOriginalConfig.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnResetOriginalConfig.setBackground(new Color(250, 128, 114));
		add(btnResetOriginalConfig, "cell 10 1,grow");
		
		separator = new JSeparator();
		add(separator, "cell 0 2 11 1,growx");
		
		lblPreferredConfigurations = new JLabel("Preferred Configurations");
		lblPreferredConfigurations.setFont(new Font("Segoe UI", Font.BOLD, 15));
		add(lblPreferredConfigurations, "cell 0 3 4 1,alignx center,aligny bottom");
		add(scrollPaneTextArea, "cell 4 3 7 2,grow");
		
		lblAvviso = new JLabel("[Configuration loaded]");
		lblAvviso.setForeground(Color.DARK_GRAY);
		add(lblAvviso, "cell 6 1,growx");
		
		JPanel panel_1 = new JPanel();
		panel_1.setForeground(SystemColor.menu);
		panel_1.setBorder(new LineBorder(Color.DARK_GRAY, 4));
		panel_1.setBackground(new Color(176, 196, 222));
		add(panel_1, "cell 0 0 11 1,grow");
		panel_1.setLayout(new MigLayout("", "[100%]", "[grow]"));
		
		JLabel lblConfigurationManager = new JLabel("Configuration Manager");
		lblConfigurationManager.setHorizontalAlignment(SwingConstants.CENTER);
		lblConfigurationManager.setForeground(Color.BLACK);
		lblConfigurationManager.setFont(new Font("Lithos Pro Regular", Font.BOLD, 22));
		lblConfigurationManager.setBackground(SystemColor.activeCaptionBorder);
		panel_1.add(lblConfigurationManager, "cell 0 0,alignx center");
		
		btnLoadCustomConfig = new JButton("Load Custom");
		btnLoadCustomConfig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LOAD_CUSTOM_CONF));
		btnLoadCustomConfig.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnLoadCustomConfig.setForeground(Color.BLACK);
		btnLoadCustomConfig.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnLoadCustomConfig.setBackground(new Color(176, 196, 222));
		add(btnLoadCustomConfig, "cell 0 1,grow");
		
		scrollPaneTreeConfigs = new JScrollPane();
		scrollPaneTreeConfigs.setBorder(new LineBorder(Color.DARK_GRAY, 3, true));
		scrollPaneTreeConfigs.setOpaque(true);
		scrollPaneTreeConfigs.setBackground(new Color(255, 255, 240));
		add(scrollPaneTreeConfigs, "cell 0 4 4 1,grow");
		
		TreeModel treeModel = new FileTreeModel(new File(mainGUI.DEFAULT_INI_FOLDER));
		tree = new JTree(treeModel);
		TreeFileMouseListenerIni menuListener = new TreeFileMouseListenerIni(config, this, tree);
		tree.setCellRenderer(new FileCellRendererIni());
		tree.setBorder(null);
		tree.setBackground(new Color(255, 255, 240));
		scrollPaneTreeConfigs.setViewportView(tree);

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
					lblAvviso.setText("[Configuration re-loaded!]");
					lblAvviso.setForeground(ImageWorker.randomColorRGB(4f));
					config_PATH.setText(config.getIniFile().getFile().getPath());
					textArea.setText("\n\n### CONFIGURATION - ACTUAL FILE CONFIG & VALUES ###\n\n" +config.toStringa());
					textAreaToTop();
					parent.updateGuiFromCurrConfig();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(parent.getMainFrame(), "Error during read of the file: " + config.getIniFile().getFile().getName(), "Load Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		// LOAD CUSTOM
		btnLoadCustomConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.debug("Load CONFIG clicked");
				INIfileChoosER panel = new INIfileChoosER(config, textArea, lblAvviso);
		    	panel.setBounds(20,20,parent.getMainFrame().getWidth(),parent.getMainFrame().getHeight()/2);  	
				lblAvviso.setForeground(ImageWorker.randomColorRGB(4f));
				config_PATH.setText(config.getIniFile().getFile().getPath());
				textArea.setText("\n\n### CONFIGURATION - ACTUAL FILE CONFIG & VALUES ###\n\n" +config.toStringa());
				textAreaToTop();
				parent.updateGuiFromCurrConfig();
			}
		});
		
		// SAVE CURRENT CONFIG
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.debug("SAVE CONFIG clicked");
				if(saveCurrentConf(config, parent.getMainFrame())) {
					lblAvviso.setText("Configuration saved");
					lblAvviso.setForeground(ImageWorker.randomColorRGB(4f));
				}
			}
		});
		
		// EXPORT CURRENT CONFIG
		btnExportConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.debug("EXPORT CONFIG clicked");
		    	INIfileChoosER panel = new INIfileChoosER(config, lblAvviso);
		    	panel.setBounds(20,20,400,300);
		    	lblAvviso.setForeground(ImageWorker.randomColorRGB(4f));
			}
		});
		
		// RELOAD ORIGINAL SCRYPT CONFIGURATION
		btnResetOriginalConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(parent.getMainFrame(), "Really want to Reset standard ScrypT configuration? Unsaved changes will be lost! ",
						"Warning - Reset INI standard config?",
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					try {
						FileUtils.copyFile(new File(mainGUI.BACKUP_INI), new File(mainGUI.DEFAULT_INI));
						config.setIniReadFile(new Wini(new File(mainGUI.DEFAULT_INI)));
						config.setIniFile(new Wini(new File(mainGUI.DEFAULT_INI)));
						lblAvviso.setText("[Configuration Resetted!]");
						String name = mainGUI.askUsername();
						JOptionPane.showMessageDialog(parent.getMainFrame(), "File '" + config.getIniFile().getFile().getName() + "' loaded!");
						config.readConfigIniFile();
						config.getGenOpt().setUserName(name);
						parent.updateGuiFromCurrConfig();
					} catch (IOException exc) {
						JOptionPane.showMessageDialog(parent.getMainFrame(),
								"Error resetting standard configuration: it does not exist or IO error occurred, retry later!");
						logger.error("Exception happened!", exc);
					}
					
					lblAvviso.setForeground(ImageWorker.randomColorRGB(4f));
					config_PATH.setText(config.getIniFile().getFile().getPath());
					textArea.setText("\n\n### CONFIGURATION - ACTUAL FILE CONFIG & VALUES ###\n\n" +config.toStringa());
					
					textAreaToTop();
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
					File backupIni = config.getIniFile().getFile();
					if (JOptionPane.showConfirmDialog(parent.getMainFrame(), "Really want to overwrite default configuration?",
							"Warning - Overwrite default configuration?",
							JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
						
						config.setIniFile(new Wini(new File(mainGUI.DEFAULT_INI)));
						config.setIniReadFile(new Ini(new File(mainGUI.DEFAULT_INI)));
						config.writeCurrentConfigIniFile();
						lblAvviso.setText("[Configuration Saved as new Default!]");
						config_PATH.setText(config.getIniFile().getFile().getPath());
						textArea.setText("\n\n### CONFIGURATION - ACTUAL FILE CONFIG & VALUES ###\n\n" +config.toStringa());
						textAreaToTop();
						JOptionPane.showMessageDialog(parent.getMainFrame(),"File " + config.getIniFile().getFile().getAbsolutePath() + " saved!");
					} else {
						JOptionPane.showMessageDialog(parent.getMainFrame(), "Configuration not saved!");
					}
					config.setIniFile(new Wini(backupIni));
					config.setIniReadFile(new Ini(backupIni));
					parent.updateGuiFromCurrConfig();
					lblAvviso.setForeground(ImageWorker.randomColorRGB(4f));
				} catch (IOException e1) {
					logger.error("Exception happened!", e1);
					JOptionPane.showMessageDialog(parent.getMainFrame(), "An error occurred :(", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}
	
	private void textAreaToTop() {
		scrollPaneTextArea.getViewport().setViewPosition(new Point(0,0));
	}
	
	public static boolean saveCurrentConf(INItializer configuration, JFrame parent) {
		int choose = JOptionPane.showConfirmDialog(parent, "Are you sure to overwrite current configuration?", "Overwrite current Config?", JOptionPane.YES_NO_CANCEL_OPTION);
		if(choose == JOptionPane.OK_OPTION) {
			configuration.writeCurrentConfigIniFile();
			JOptionPane.showMessageDialog(parent,  configuration.getIniFile().getFile().getAbsolutePath() + " -> Saved", "Configuration saved", JOptionPane.INFORMATION_MESSAGE);
			return true;
		}
		return false; 
	}
}
