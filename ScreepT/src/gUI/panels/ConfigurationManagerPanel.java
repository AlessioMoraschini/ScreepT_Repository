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
package gUI.panels;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.ini4j.Ini;
import org.ini4j.Wini;

import dialogutils.JOptionHelperExtended;
import files.FileWorker;
import frameutils.frame.arch.ModulePanel;
import frameutils.utils.GuiUtilsExtended;
import gUI.utils.filedialog.INIfileChoosER;
import gUI.utils.tree.iniconfmanager.FileCellRendererIni;
import gUI.utils.tree.iniconfmanager.TreeFileMouseListenerIni;
import gui.dialogs.FindDialogFork;
import guistyle.GUIStyelSettingsExtended;
import maingUI.MainGUI;
import maingUI.MainGuiUtils;
import mainobj.INItializer;
import net.miginfocom.swing.MigLayout;
import om.themes.Themes;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import treeutils.om.FileTreeModel;
import utils.ImageWorkerExtended;
import utils.RsyntaxUtils;
import utils.dialog.ITextAreaDialogs;
import utils.dialog.TextAreaDialogs;

import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.JLabel;
import java.awt.SystemColor;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.JFrame;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.event.ActionEvent;
import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.border.LineBorder;
import javax.swing.tree.TreeModel;

import java.awt.Cursor;
import java.awt.Dialog;

import javax.swing.border.BevelBorder;
import javax.swing.JTree;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import java.awt.Dimension;

public class ConfigurationManagerPanel extends ModulePanel {
	private static final long serialVersionUID = 5166978306767024195L;

	// INItializer logger creation		
	static Logger logger = Logger.getLogger(ConfigurationManagerPanel.class); 
	
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_hh:mm:ss");
	
	public TreeFileMouseListenerIni menuMouseListener;
	
	private INItializer config;
	public MainGUI parent;
	private ConfigurationManagerPanel thisPanel;
	
	// GUI ELEMENTS //
	public RSyntaxTextArea textArea;
	private JScrollPane scrollPaneTreeConfigs;
	private RTextScrollPane scrollPaneTextArea;

	private JButton btnLoadCustomConfig;
	private JButton btnExportConfig;
	private JButton btnCurrentRefresh;
	private JButton btnSaveCurrentAsDefault;
	private JButton btnResetOriginalConfig;
	
	public JLabel lblAvviso;
	public JLabel config_PATH;
	public JPanel panelDynPath;
	private JLabel lblPreferredConfigurations;
	private JSeparator separator;

	private JTree tree;
	private JButton btnSave;
	private JButton btnViewCurrentFile;
	
	private JOptionHelperExtended dialogHelper;
	public JSplitPane splitPane;
	private JButton btnFind;
	
	private TextAreaDialogs textAreaDialogs;
	private FindDialogFork searchBox;

	public ConfigurationManagerPanel(INItializer configuration, MainGUI parentFrame) {
		setBorder(new BevelBorder(BevelBorder.LOWERED, new Color(112, 128, 144), new Color(105, 105, 105), new Color(0, 0, 0), new Color(0, 0, 0)));
		
		thisPanel = this;
		parent = parentFrame;
		config = configuration;
		
		textAreaDialogs = new TextAreaDialogs(new ITextAreaDialogs() {
			
			@Override
			public void loadThemeIntoTarget(RSyntaxTextArea txtArea) {
			}
			
			@Override
			public RSyntaxTextArea getTargetTextArea() {
				return textArea;
			}
			
			@Override
			public String getCurrentClipboard() {
				return null;
			}

			@Override
			public Dialog getSearchDialog() {
				return searchBox;
			}

			@Override
			public Dialog getReplaceDialog() {
				return null;
			}
		});
		
		dialogHelper = new JOptionHelperExtended(parent.getMainFrame());
		
		logger.info(this.getClass().getName() + " Starting...");
		
		setBackground(Color.DARK_GRAY);
		
		setLayout(new MigLayout("", 
				"[120px:130px][80px:90px][70px:80px][135px:150px][30px][150px:160px][140px:140px][][140px:140px,grow][grow][50][100px,grow,leading][120px:130px,grow]", 
				"[:35px:40px][28px][][20px][400px,grow]"));
		
		textArea = new RSyntaxTextArea();
		textArea.setUseSelectedTextColor(true);
		textArea.setBorder(null);
		textArea.setEditable(false);

		scrollPaneTextArea = new RTextScrollPane(textArea);
		scrollPaneTextArea.setAutoscrolls(false);
		scrollPaneTextArea.setBorder(new LineBorder(Color.BLACK, 2));
		
		btnExportConfig = new JButton("Export");
		btnExportConfig.setMinimumSize(new Dimension(62, 20));
		btnExportConfig.setPreferredSize(new Dimension(62, 20));
		btnExportConfig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_EXPORT_CONF));
		btnExportConfig.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnExportConfig.setForeground(Color.BLACK);
		btnExportConfig.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnExportConfig.setBackground(Color.LIGHT_GRAY);
		add(btnExportConfig, "cell 1 1,grow");
		
		btnSave = new JButton("Save");
		btnSave.setPreferredSize(new Dimension(55, 20));
		btnSave.setMinimumSize(new Dimension(55, 20));
		btnSave.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SAVE_CONF));
		btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSave.setForeground(Color.BLACK);
		btnSave.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnSave.setBackground(Color.LIGHT_GRAY);
		add(btnSave, "cell 2 1,grow");
		
		btnSaveCurrentAsDefault = new JButton("Save as Default");
		btnSaveCurrentAsDefault.setMinimumSize(new Dimension(114, 20));
		btnSaveCurrentAsDefault.setPreferredSize(new Dimension(114, 20));
		btnSaveCurrentAsDefault.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SAVE_AS_DEFAULT_CONF));
		btnSaveCurrentAsDefault.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSaveCurrentAsDefault.setForeground(Color.BLACK);
		btnSaveCurrentAsDefault.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnSaveCurrentAsDefault.setBackground(Color.LIGHT_GRAY);
		add(btnSaveCurrentAsDefault, "cell 3 1 2 1,grow");
		
		btnViewCurrentFile = new JButton("Current Config. File");
		btnViewCurrentFile.setPreferredSize(new Dimension(134, 20));
		btnViewCurrentFile.setMinimumSize(new Dimension(134, 20));
		btnViewCurrentFile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnViewCurrentFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD2));
		btnViewCurrentFile.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnViewCurrentFile.setForeground(Color.BLACK);
		btnViewCurrentFile.setBackground(new Color(176, 196, 222));
		add(btnViewCurrentFile, "cell 5 1,grow");
		
		btnCurrentRefresh = new JButton("Current Volatile");
		btnCurrentRefresh.setMinimumSize(new Dimension(112, 20));
		btnCurrentRefresh.setPreferredSize(new Dimension(112, 20));
		btnCurrentRefresh.setForeground(Color.BLACK);
		btnCurrentRefresh.setBackground(new Color(176, 196, 222));
		btnCurrentRefresh.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnCurrentRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnCurrentRefresh.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD2));
		add(btnCurrentRefresh, "cell 6 1,grow");
		
		btnFind = new JButton("");
		btnFind.setForeground(Color.BLACK);
		btnFind.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnFind.setBackground(Color.GRAY);
		btnFind.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnFind.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SEARCH));
		add(btnFind, "cell 7 1,grow");
		
		panelDynPath = new JPanel();
		panelDynPath.setOpaque(false);
		add(panelDynPath, "cell 9 1 3 1,alignx center,growy");
		panelDynPath.setLayout(new MigLayout("", "[grow][150px,grow]", "[]"));
		
		btnResetOriginalConfig = new JButton("Reset Original");
		btnResetOriginalConfig.setMinimumSize(new Dimension(120, 20));
		btnResetOriginalConfig.setPreferredSize(new Dimension(120, 20));
		btnResetOriginalConfig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RESET_CONF));
		btnResetOriginalConfig.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnResetOriginalConfig.setForeground(Color.BLACK);
		btnResetOriginalConfig.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnResetOriginalConfig.setBackground(new Color(250, 128, 114));
		add(btnResetOriginalConfig, "cell 11 1 2 1,grow");
		
		separator = new JSeparator();
		add(separator, "cell 0 2 13 1,growx");
		
		lblPreferredConfigurations = new JLabel("Preferred Configurations");
		lblPreferredConfigurations.setForeground(Color.WHITE);
		lblPreferredConfigurations.setFont(new Font("Segoe UI", Font.BOLD, 15));
		add(lblPreferredConfigurations, "cell 0 3 4 1,alignx center,aligny bottom");
		
		JLabel lblInitializationFrom = new JLabel("Current .ini file: ");
		lblInitializationFrom.setForeground(Color.WHITE);
		add(lblInitializationFrom, "cell 5 3 3 1");
		lblInitializationFrom.setFont(new Font("Segoe UI", Font.BOLD, 15));
		
		config_PATH = new JLabel("");
		config_PATH.setForeground(Color.WHITE);
		add(config_PATH, "cell 8 3 5 1");
		config_PATH.setHorizontalAlignment(SwingConstants.LEFT);
		config_PATH.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		config_PATH.setText(config.getIniFile().getFile().getPath());
		
		lblAvviso = new JLabel("[Configuration loaded]");
		lblAvviso.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		lblAvviso.setForeground(Color.DARK_GRAY);
		add(lblAvviso, "cell 8 1,growx");
		
		JPanel panel_1 = new JPanel();
		panel_1.setForeground(SystemColor.menu);
		panel_1.setBorder(new LineBorder(Color.BLACK, 2));
		panel_1.setBackground(Color.LIGHT_GRAY);
		add(panel_1, "cell 0 0 13 1,grow");
		panel_1.setLayout(new MigLayout("", "[100%]", "[grow]"));
		
		JLabel lblConfigurationManager = new JLabel("Configuration Manager");
		lblConfigurationManager.setHorizontalAlignment(SwingConstants.CENTER);
		lblConfigurationManager.setForeground(Color.BLACK);
		lblConfigurationManager.setFont(new Font("Lithos Pro Regular", Font.BOLD, 15));
		lblConfigurationManager.setBackground(SystemColor.activeCaptionBorder);
		panel_1.add(lblConfigurationManager, "cell 0 0,alignx center");
		
		btnLoadCustomConfig = new JButton("Load Custom");
		btnLoadCustomConfig.setPreferredSize(new Dimension(103, 20));
		btnLoadCustomConfig.setMinimumSize(new Dimension(103, 20));
		btnLoadCustomConfig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LOAD_CUSTOM_CONF));
		btnLoadCustomConfig.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnLoadCustomConfig.setForeground(Color.BLACK);
		btnLoadCustomConfig.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
		btnLoadCustomConfig.setBackground(Color.LIGHT_GRAY);
		add(btnLoadCustomConfig, "cell 0 1,grow");
		
		scrollPaneTreeConfigs = new JScrollPane();
		scrollPaneTreeConfigs.setBorder(new LineBorder(Color.BLACK, 2));
		scrollPaneTreeConfigs.setOpaque(true);
		scrollPaneTreeConfigs.setBackground(new Color(255, 255, 240));
		
		TreeModel treeModel = new FileTreeModel(new File(GeneralConfig.DEFAULT_INI_FOLDER));
		tree = new JTree(treeModel);
		menuMouseListener = new TreeFileMouseListenerIni(config, this, tree);
		tree.setCellRenderer(new FileCellRendererIni());
		tree.setBorder(null);
		tree.setBackground(new Color(255, 255, 240));
		scrollPaneTreeConfigs.setViewportView(tree);
		
		splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		splitPane.setBackground(Color.DARK_GRAY);
		splitPane.setLeftComponent(scrollPaneTreeConfigs);
		splitPane.setRightComponent(scrollPaneTextArea);
		splitPane.setDividerLocation(300);
		add(splitPane, "cell 0 4 13 1,grow");

		addHandlers();
		
		initGuiFromConfig();
		
		logger.info(this.getClass().getName() + " Started");
	}
	
	public void initGuiFromConfig() {
		GUIStyelSettingsExtended.setStyle(config.getGuiOpt().getPreferredStyle(), config.getGuiOpt().nimbusSize);
		GUIStyelSettingsExtended.applyNimbusSize(null, config.getGuiOpt().nimbusSize);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				applyTextAreaStyle();
			}
		});
	}
	
	public void applyTextAreaStyle(){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				boolean usePreloadedStringXml = GeneralConfig.XML_THEMES_CONTENT_MAP != null
						&& GeneralConfig.XML_THEMES_CONTENT_MAP.containsKey(Themes.DARK.fileName);
				if (usePreloadedStringXml) {
					// If available use lazy initialized in memory
					RsyntaxUtils.changeStyleViaThemeXmlString(textArea, GeneralConfig.XML_THEMES_CONTENT_MAP.get(Themes.DARK.fileName));
				} else {
					// if not available read from file
					RsyntaxUtils.changeStyleViaThemeXml(textArea, Themes.DARK.defaultPath);
				}
				textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_INI);
				textArea.setFocusable(true);
				textArea.setRequestFocusEnabled(true);
				textArea.setLineWrap(true);
				textArea.setEditable(true);
				textArea.setEnabled(true);
				textArea.setSelectedTextColor(Color.LIGHT_GRAY);
				textArea.setMargin(new Insets(20, 20, 20, 20));
				textArea.setFont(new Font("Segoe UI", Font.PLAIN, 18));
				FileWorker.appendFileTxtArea(config.getIniReadFile().getFile(), textArea, true);
				textArea.revalidate();
				textArea.repaint();
			}
		});
	}
	
	public void addHandlers() {
		
		// LOAD CURRENT INI FILE LOADED
		btnViewCurrentFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lblAvviso.setText("[Current Configuration file]");
				lblAvviso.setForeground(ImageWorkerExtended.randomColorRGB(0.2f));
				config_PATH.setText(config.getIniFile().getFile().getPath());
				textArea.setText("\n\n### CONFIGURATION - ACTUAL FILE CONFIG & VALUES (on Disk) ###\n\n");
				FileWorker.appendFileTxtArea(config.getIniFile().getFile(), textArea, true);
			}
		});

		// REFRESH VIEW CURRENT CONFIG VOLATILE MEMORY
		btnCurrentRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					config.setIniFile(new Wini(config.getIniFile().getFile()));
					config.setIniReadFile(new Wini(config.getIniFile().getFile()));
					lblAvviso.setText("[Refreshed temp Config]");
					lblAvviso.setForeground(ImageWorkerExtended.randomColorRGB(4f));
					config_PATH.setText(config.getIniFile().getFile().getPath());
					textArea.setText("\n\n### CONFIGURATION - ACTUAL FILE CONFIG & VALUES (in Volatile Memory) ###\n\n");
					MainGUI.now = new Date();
					textArea.append("#### "+sdf.format(MainGUI.now) + " ####\n\n");
					textArea.append(config.toStringa());
					textAreaToTop();
					// update all panels with current configuration
					//parent.updateGuiFromCurrConfig();
				} catch (IOException e1) {
					dialogHelper.error("Error during read of the file: " + config.getIniFile().getFile().getName(), "Load Error");
				}
			}
		});
		
		// LOAD CUSTOM
		btnLoadCustomConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.debug("Load CONFIG clicked");
				INIfileChoosER.readNloadIniWithTextAreaOutput(config, textArea, lblAvviso);
				lblAvviso.setForeground(ImageWorkerExtended.randomColorRGB(4f));
				config_PATH.setText(config.getIniFile().getFile().getPath());
				textArea.setText("\n\n### CONFIGURATION - ACTUAL FILE CONFIG & VALUES ###\n\n");
				FileWorker.appendFileTxtArea(config.getIniReadFile().getFile(), textArea, true); 	
				textAreaToTop();
				MainGUI.updateGuiFromCurrConfig();
			}
		});
		
		// SAVE CURRENT CONFIG
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.debug("SAVE CONFIG clicked");
				if(INIfileChoosER.saveCurrentConf(config, parent.getMainFrame())) {
					lblAvviso.setText("Configuration saved on disk!");
					lblAvviso.setForeground(ImageWorkerExtended.randomColorRGB(4f));
				}
			}
		});
		
		// EXPORT CURRENT CONFIG
		btnExportConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.debug("EXPORT CONFIG clicked");
		    	INIfileChoosER.exportIniFile(config, lblAvviso);
		    	lblAvviso.setForeground(ImageWorkerExtended.randomColorRGB(4f));
			}
		});
		
		// RELOAD ORIGINAL SCREEPT CONFIGURATION
		btnResetOriginalConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (dialogHelper.yesOrNo("Really want to Reset standard ScreepT configuration? Unsaved changes will be lost! ", "Warning - Reset INI standard config?")) {
					try {
						FileUtils.copyFile(new File(GeneralConfig.BACKUP_INI), new File(GeneralConfig.DEFAULT_INI));
						config.setIniReadFile(new Wini(new File(GeneralConfig.DEFAULT_INI)));
						config.setIniFile(new Wini(new File(GeneralConfig.DEFAULT_INI)));
						lblAvviso.setText("[Configuration Resetted!]");
						MainGuiUtils.askNameAtStartIfNotDefined();
						dialogHelper.info("File '" + config.getIniFile().getFile().getName() + "' loaded!", "");
						config.readConfigIniFile();
						MainGUI.updateGuiFromCurrConfig();
					} catch (IOException exc) {
						dialogHelper.error("Error resetting standard configuration: it does not exist or IO error occurred, retry later!", "");
						logger.error("Exception happened!", exc);
					}
					
					lblAvviso.setForeground(ImageWorkerExtended.randomColorRGB(4f));
					config_PATH.setText(config.getIniFile().getFile().getPath());
					textArea.setText("\n\n### CONFIGURATION - ACTUAL FILE CONFIG & VALUES ###\n\n");
					FileWorker.appendFileTxtArea(config.getIniReadFile().getFile(), textArea, true); 	
					
					textAreaToTop();
				} else {
					dialogHelper.warn("Configuration not loaded, current CONFIG mantained!", "");
				} 
			}
		});
		
		// SAVE CURRENT AS DEFAULT
		btnSaveCurrentAsDefault.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					File backupIni = config.getIniFile().getFile();
					if (dialogHelper.yesOrNo("Really want to overwrite default configuration?",	"Warning - Overwrite default configuration?")) {
						
						config.setIniFile(new Wini(new File(GeneralConfig.DEFAULT_INI)));
						config.setIniReadFile(new Ini(new File(GeneralConfig.DEFAULT_INI)));
						config.writeCurrentConfigIniFile();
						lblAvviso.setText("[Configuration Saved as new Default!]");
						config_PATH.setText(config.getIniFile().getFile().getPath());
						textArea.setText("\n\n### CONFIGURATION - ACTUAL FILE CONFIG & VALUES ###\n\n" );
						FileWorker.appendFileTxtArea(config.getIniReadFile().getFile(), textArea, true); 	
						textAreaToTop();
						dialogHelper.info("File " + config.getIniFile().getFile().getAbsolutePath() + " saved!", "");
					} else {
						dialogHelper.info("Configuration not saved!", "");
					}
					config.setIniFile(new Wini(backupIni));
					config.setIniReadFile(new Ini(backupIni));
					MainGUI.updateGuiFromCurrConfig();
					lblAvviso.setForeground(ImageWorkerExtended.randomColorRGB(4f));
				} catch (IOException e1) {
					logger.error("Exception happened!", e1);
					dialogHelper.error("An error occurred :(", "Error");
				}
			}
		});
		
		// search text into configuration
		btnFind.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (searchBox == null) {
							searchBox = textAreaDialogs.getSearchDialog(thisPanel.parent.getMainFrame(),  null, null,"successAdvice", "errorAdvice");
							GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_SEARCH, searchBox);
						} else {
							searchBox.setVisible(true);
							searchBox.toFront();
							searchBox.requestFocus();
						}
						searchBox.setLocationRelativeTo(thisPanel.parent.getMainFrame());
						GuiUtilsExtended.changeFrameStateLaterUnchecked(thisPanel.parent.getMainFrame(), searchBox, JFrame.MAXIMIZED_BOTH);
					}
				});
			}
		});
	}
	
	private void textAreaToTop() {
		try {
			scrollPaneTextArea.getViewport().setViewPosition(new Point(0,0));
			textArea.setCaretPosition(0);
			textArea.setSelectionStart(0);
			textArea.setSelectionEnd(0);
		} catch (Exception e) {
		}
	}
}
