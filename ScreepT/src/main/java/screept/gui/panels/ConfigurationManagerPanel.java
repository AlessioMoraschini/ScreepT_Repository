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
package screept.gui.panels;

import various.common.light.i18n.I18n;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.ini4j.Wini;

import gui.commons.dialogutils.JOptionHelperExtended;
import gui.commons.frameutils.frame.arch.ModulePanel;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import gui.commons.guistyle.GUIStyelSettingsExtended;
import gui.commons.frameutils.frame.arch.ToolStyle;
import gui.texteditor.commons.dialogs.FindDialogFork;
import gui.texteditor.commons.om.themes.Themes;
import gui.texteditor.commons.utils.RsyntaxUtils;
import gui.texteditor.commons.utils.dialog.ITextAreaDialogs;
import gui.texteditor.commons.utils.dialog.TextAreaDialogs;
import initializer.configs.impl.INItializer;
import maingUI.MainGUI;
import maingUI.MainGuiUtils;
import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import screept.gui.utils.filedialog.INIfileChoosER;
import screept.gui.utils.tree.iniconfmanager.FileCellRendererIni;
import screept.gui.utils.tree.iniconfmanager.ConfigPool;
import various.common.light.gui.dialogs.msg.JOptionHelper;
import screept.gui.utils.tree.iniconfmanager.TreeFileMouseListenerIni;
import various.common.light.files.FileWorker;

public class ConfigurationManagerPanel extends ModulePanel {
	private static final long serialVersionUID = 5166978306767024195L;

	// INItializer logger creation
	static Logger logger = Logger.getLogger(ConfigurationManagerPanel.class);

	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HH:mm:ss");

	public TreeFileMouseListenerIni menuMouseListener;

	private INItializer config;

	// configuration as last loaded or saved: the differences from it are the unsaved changes
	private String savedSnapshot;
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

		setLayout(new MigLayout("fill, insets 14 18 14 18, wrap 1", "[grow,fill]", "[][][][][grow,fill]"));

		// TITLE
		add(ToolStyle.header(I18n.t("cfg.configuration_manager"), null), "growx");

		// FILE: load, export, save (green), save as default
		JPanel fileCard = ToolStyle.card(new MigLayout("insets 0, fillx, gap 8 8", "[][][][][grow,fill]", "[]"));
		btnLoadCustomConfig = plainButton(I18n.t("cfg.load_custom"), IconsPathConfigurator.ICON_LOAD_CUSTOM_CONF, I18n.t("cfg.tip_load_custom"));
		btnExportConfig = plainButton(I18n.t("common.export"), IconsPathConfigurator.ICON_EXPORT_CONF, I18n.t("cfg.tip_export"));
		btnSave = ToolStyle.accent(plainButton(I18n.t("common.save"), IconsPathConfigurator.ICON_SAVE_CONF, I18n.t("cfg.tip_save")), ToolStyle.GREEN);
		btnSaveCurrentAsDefault = plainButton(I18n.t("cfg.save_as_default"), IconsPathConfigurator.ICON_SAVE_AS_DEFAULT_CONF, I18n.t("cfg.tip_save_as_default"));
		fileCard.add(btnLoadCustomConfig);
		fileCard.add(btnExportConfig);
		fileCard.add(btnSave);
		fileCard.add(btnSaveCurrentAsDefault);
		add(fileCard, "growx");

		// VIEW: current file, volatile values, find, reset to the original (red)
		JPanel viewCard = ToolStyle.card(new MigLayout("insets 0, fillx, gap 8 8", "[][][][grow,fill][]", "[]"));
		btnViewCurrentFile = plainButton(I18n.t("cfg.current_config_file"), IconsPathConfigurator.ICON_RELOAD2, I18n.t("cfg.tip_current_file"));
		btnCurrentRefresh = plainButton(I18n.t("cfg.current_volatile"), IconsPathConfigurator.ICON_RELOAD2, I18n.t("cfg.tip_current_volatile"));
		btnFind = plainButton("", IconsPathConfigurator.ICON_SEARCH, I18n.t("cfg.tip_find"));
		btnResetOriginalConfig = ToolStyle.accent(plainButton(I18n.t("cfg.reset_original"), IconsPathConfigurator.ICON_RESET_CONF, I18n.t("cfg.tip_reset_original")), ToolStyle.RED);
		viewCard.add(btnViewCurrentFile);
		viewCard.add(btnCurrentRefresh);
		viewCard.add(btnFind);
		viewCard.add(new JLabel(), "growx");
		viewCard.add(btnResetOriginalConfig);
		add(viewCard, "growx");

		// STATUS: last action, file in use
		JPanel status = new JPanel(new MigLayout("insets 0, fillx, gap 10 0", "[][][grow,fill]", "[]"));
		status.setOpaque(false);
		lblAvviso = ToolStyle.caption(I18n.t("cfg.status_loaded"));
		lblAvviso.setForeground(ToolStyle.BLUE);
		config_PATH = ToolStyle.mutedLabel(config.getIniFile().getFile().getPath());
		status.add(lblAvviso);
		status.add(ToolStyle.caption(I18n.t("cfg.current_ini_file")));
		status.add(config_PATH, "wmin 0");
		add(status, "growx");

		// TREE of the ini files (left) and the text of the chosen one (right)
		tree = new JTree(ConfigPool.modelFor(configuration.getIniFile().getFile()));
		menuMouseListener = new TreeFileMouseListenerIni(config, this, tree);
		tree.setCellRenderer(new FileCellRendererIni());
		ConfigPool.expandAll(tree);
		scrollPaneTreeConfigs = new JScrollPane(tree);

		JPanel treeCard = ToolStyle.card(new MigLayout("insets 0, fill, wrap 1", "[grow,fill]", "[][grow,fill]"));
		treeCard.add(ToolStyle.caption(I18n.t("cfg.preferred_configurations")));
		treeCard.add(scrollPaneTreeConfigs, "grow");

		textArea = new RSyntaxTextArea();
		textArea.setUseSelectedTextColor(true);
		textArea.setBorder(null);
		textArea.setEditable(false);
		scrollPaneTextArea = new RTextScrollPane(textArea);
		scrollPaneTextArea.setAutoscrolls(false);
		scrollPaneTextArea.setBorder(null);

		JPanel textCard = ToolStyle.card(new MigLayout("insets 0, fill", "[grow,fill]", "[grow,fill]"));
		textCard.add(scrollPaneTextArea, "grow");

		splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		splitPane.setOpaque(false);
		splitPane.setBorder(null);
		splitPane.setLeftComponent(treeCard);
		splitPane.setRightComponent(textCard);
		splitPane.setDividerLocation(300);
		add(splitPane, "grow");

		addHandlers();

		initGuiFromConfig();

		markSaved();
		logger.info(this.getClass().getName() + " Started");
	}

	public void initGuiFromConfig() {
		GUIStyelSettingsExtended.setStyle(config.getGuiOpt().getPreferredStyle(), config.getGuiOpt().nimbusSize);
		GUIStyelSettingsExtended.applyNimbusSize(null, config.getGuiOpt().nimbusSize);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				applyTextAreaStyle();
			}
		});
	}

	public void applyTextAreaStyle(){
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// same rule of the window: the syntax colors follow the look and feel
				Themes theme = ToolStyle.isDark() ? Themes.DARK : Themes.DEFAULT;

				boolean usePreloadedStringXml = GeneralConfig.XML_THEMES_CONTENT_MAP != null
						&& GeneralConfig.XML_THEMES_CONTENT_MAP.containsKey(theme.fileName);
				if (usePreloadedStringXml) {
					// If available use lazy initialized in memory
					RsyntaxUtils.changeStyleViaThemeXmlString(textArea, GeneralConfig.XML_THEMES_CONTENT_MAP.get(theme.fileName));
				} else {
					// if not available read from file
					RsyntaxUtils.changeStyleViaThemeXml(textArea, theme.defaultPath);
				}
				textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_INI);
				textArea.setFocusable(true);
				textArea.setRequestFocusEnabled(true);
				textArea.setLineWrap(true);
				textArea.setEditable(false);
				textArea.setEnabled(true);
				textArea.setMargin(new Insets(10, 12, 10, 12));
				textArea.setFont(ToolStyle.mono(1f));
				FileWorker.appendFileTxtArea(config.getIniReadFile().getFile(), textArea, true);
				textArea.revalidate();
				textArea.repaint();
			}
		});
	}

	public void addHandlers() {

		// LOAD CURRENT INI FILE LOADED
		btnViewCurrentFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				lblAvviso.setText(I18n.t("cfg.status_current_file"));
				lblAvviso.setForeground(ToolStyle.BLUE);
				config_PATH.setText(config.getIniFile().getFile().getPath());
				textArea.setText(I18n.t("cfg.configuration_actual_file_config_values_on"));
				FileWorker.appendFileTxtArea(config.getIniFile().getFile(), textArea, true);
			}
		});

		// REFRESH VIEW CURRENT CONFIG VOLATILE MEMORY
		btnCurrentRefresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (!confirmLoadingOtherConfig()) {
						return;
					}
					config.setIniFile(new Wini(config.getIniFile().getFile()));
					config.setIniReadFile(new Wini(config.getIniFile().getFile()));
					lblAvviso.setText(I18n.t("cfg.status_refreshed"));
					afterLoad();
					lblAvviso.setForeground(ToolStyle.BLUE);
					config_PATH.setText(config.getIniFile().getFile().getPath());
					textArea.setText(I18n.t("cfg.configuration_actual_file_config_values_in"));
					MainGUI.now = new Date();
					textArea.append("#### "+sdf.format(MainGUI.now) + " ####\n\n");
					textArea.append(config.toStringa());
					textAreaToTop();
					// update all panels with current configuration
					//parent.updateGuiFromCurrConfig();
				} catch (IOException e1) {
					dialogHelper.error(I18n.t("cfg.error_during_read_of_the_file") + config.getIniFile().getFile().getName(), I18n.t("cfg.load_error"));
				}
			}
		});

		// LOAD CUSTOM
		btnLoadCustomConfig.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.debug("Load CONFIG clicked");
				if (!confirmLoadingOtherConfig()) {
					return;
				}
				String previousPath = config.getIniFile().getFile().getPath();
				INIfileChoosER.readNloadIniWithTextAreaOutput(config, textArea, lblAvviso);
				if (!previousPath.equals(config.getIniFile().getFile().getPath())) {
					afterLoad();
				}
				lblAvviso.setForeground(ToolStyle.BLUE);
				config_PATH.setText(config.getIniFile().getFile().getPath());
				textArea.setText(I18n.t("cfg.configuration_actual_file_config_values"));
				FileWorker.appendFileTxtArea(config.getIniReadFile().getFile(), textArea, true);
				textAreaToTop();
				MainGUI.updateGuiFromCurrConfig();
			}
		});

		// SAVE CURRENT CONFIG
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.debug("SAVE CONFIG clicked");
				if(INIfileChoosER.saveCurrentConf(config, parent.getMainFrame())) {
					lblAvviso.setText(I18n.t("cfg.configuration_saved_on_disk"));
					markSaved();
					lblAvviso.setForeground(ToolStyle.BLUE);
				}
			}
		});

		// EXPORT CURRENT CONFIG
		btnExportConfig.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.debug("EXPORT CONFIG clicked");
		    	INIfileChoosER.exportIniFile(config, lblAvviso);
		    	lblAvviso.setForeground(ToolStyle.BLUE);
			}
		});

		// RELOAD ORIGINAL SCREEPT CONFIGURATION
		btnResetOriginalConfig.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (dialogHelper.yesOrNo(I18n.t("cfg.really_want_to_reset_standard_screept"), I18n.t("cfg.warning_reset_ini_standard_config"))) {
					try {
						FileUtils.copyFile(new File(GeneralConfig.BACKUP_INI), new File(GeneralConfig.DEFAULT_INI));
						config.setIniReadFile(new Wini(new File(GeneralConfig.DEFAULT_INI)));
						config.setIniFile(new Wini(new File(GeneralConfig.DEFAULT_INI)));
						lblAvviso.setText(I18n.t("cfg.status_reset"));
						afterLoad();
						MainGuiUtils.askNameAtStartIfNotDefined(MainGUI.mainAPP.getMainFrame());
						dialogHelper.info(I18n.t("cfg.file") + config.getIniFile().getFile().getName() + I18n.t("cfg.loaded"), "");
						config.readConfigIniFile();
						markSaved(); // the memory has just been replaced: the snapshot taken by afterLoad() is stale
						MainGUI.updateGuiFromCurrConfig();
					} catch (IOException exc) {
						dialogHelper.error(I18n.t("cfg.error_resetting_standard_configuration_it_do"), "");
						logger.error("Exception happened!", exc);
					}

					lblAvviso.setForeground(ToolStyle.BLUE);
					config_PATH.setText(config.getIniFile().getFile().getPath());
					textArea.setText(I18n.t("cfg.configuration_actual_file_config_values"));
					FileWorker.appendFileTxtArea(config.getIniReadFile().getFile(), textArea, true);

					textAreaToTop();
				} else {
					dialogHelper.warn(I18n.t("cfg.configuration_not_loaded_current_config_mant"), "");
				}
			}
		});

		// SAVE CURRENT AS DEFAULT
		btnSaveCurrentAsDefault.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					File backupIni = config.getIniFile().getFile();
					if (dialogHelper.yesOrNo(I18n.t("app.really_want_to_overwrite_default_configurati"),	I18n.t("cfg.warning_overwrite_default_configuration"))) {

						config.setIniFile(new Wini(new File(GeneralConfig.DEFAULT_INI)));
						config.setIniReadFile(new Wini(new File(GeneralConfig.DEFAULT_INI)));
						config.writeCurrentConfigIniFile();
						markSaved();
						lblAvviso.setText(I18n.t("cfg.status_saved_default"));
						config_PATH.setText(config.getIniFile().getFile().getPath());
						textArea.setText(I18n.t("cfg.configuration_actual_file_config_values") );
						FileWorker.appendFileTxtArea(config.getIniReadFile().getFile(), textArea, true);
						textAreaToTop();
						dialogHelper.info(I18n.t("cfg.file_2") + config.getIniFile().getFile().getAbsolutePath() + I18n.t("cfg.saved"), "");
					} else {
						dialogHelper.info(I18n.t("app.configuration_not_saved"), "");
					}
					config.setIniFile(new Wini(backupIni));
					config.setIniReadFile(new Wini(backupIni));
					config_PATH.setText(config.getIniFile().getFile().getPath());
					MainGUI.updateGuiFromCurrConfig();
					lblAvviso.setForeground(ToolStyle.BLUE);
				} catch (IOException e1) {
					logger.error("Exception happened!", e1);
					dialogHelper.error(I18n.t("app.an_error_occurred"), "Error");
				}
			}
		});

		// search text into configuration
		btnFind.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
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

	private static JButton plainButton(String text, String iconPath, String tooltip) {
		JButton button = new JButton(text);
		button.setIcon(new ImageIcon(iconPath));
		button.setToolTipText(tooltip);
		return ToolStyle.hand(button);
	}

	/** Remembers the configuration in memory as the saved one */
	public void markSaved() {
		savedSnapshot = config.toStringa();
	}

	/** True when the configuration in memory differs from the last loaded or saved one */
	public boolean hasUnsavedChanges() {
		return savedSnapshot != null && !savedSnapshot.equals(config.toStringa());
	}

	/**
	 * Before replacing the configuration in use: when it has unsaved changes, asks to save them.
	 *  false when the user cancels, so nothing must be loaded
	 */
	public boolean confirmLoadingOtherConfig() {
		if (!hasUnsavedChanges()) {
			return true;
		}
		int choice = dialogHelper.yesNoOrCanc(I18n.t("cfg.unsaved_changes_question"), I18n.t("cfg.unsaved_changes_title"));
		if (choice == JOptionHelper.OK) {
			config.writeCurrentConfigIniFile();
			markSaved();
			return true;
		}
		return choice == JOptionHelper.NO;
	}

	/** After a configuration has been loaded: it becomes the reference for the unsaved changes, and the list is rebuilt */
	public void afterLoad() {
		markSaved();
		menuMouseListener.refreshTree();
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
