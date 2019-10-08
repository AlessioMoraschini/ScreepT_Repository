package gui.panels;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import frameutils.utils.GuiUtils;
import launcherutils.IniFileManager;
import mainlauncher.ScrYpTLauncher;
import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import utils.SysUtils;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import java.awt.Font;
import javax.swing.JSlider;

public class CustomizeParametersPanel extends JFrame{
	
	// CustomizeParametersPanel logger
	static Logger logger = Logger.getLogger(CustomizeParametersPanel.class); 
	
	public static final DecimalFormat TWO_DECIMAL_FORMATTER = new DecimalFormat("#.##");
	private static final String TOOLTIP_LEVEL_LOG = "Choose a value between 'OFF'(logging feature disabled) and 'ALL'. This can have an impact for overall speed and memory usage (OFF = max speed)";
	private static final String TOOLTIP_STACK_SIZE = "This is the memory used by java JVM to store variables in ram memory called stack. \nHigh value = more reliability but less free memory for other dynamic values!";
	private static final String TOOLTIP_WARN_MIN_RAM_SIZE = "It's recommended to use lower value for minimum memory than max, equal values cause performance decrease! ";
	private static final String TOOLTIP_WARN_MAX_RAM_SIZE = "It's recommended to use bigger value for max memory than minimum, equal values cause performance decrease! ";
	private static final String TOOLTIP_MIN_RAM_SIZE = "This is the minimum heap ram value used bj JVM to start ScrYpT dynamic data allocation";
	private static final String TOOLTIP_MAX_RAM_SIZE = "This is the maximum heap ram value used bj JVM allocated to ScrYpT for dynamic data allocation";
	private static final String TOOLTIP_DISPLAY_SHELL = "When checked, the launcher will keep an open active command line shell with the logger's output";
	private static final String TOOLTIP_LAUNCH_AS_JAR = "ScrYpT will be launched as Jar (works only with centralized build)";
	private static final String TOOLTIP_LAUNCH_AS_CLASS = "ScrYpT will be launched as .Class (works with centralized build and even with multimodule)";
	
	private static final String TITLE_LAUNCH_AS_JAR = "Launch as .Jar:";
	private static final String TITLE_LAUNCH_AS_CLASS = "Launch as .Class:";
	
	private static final int DEF_FIXED_HEIGHT = 190;
	private static final int DEF_FIXED_WIDTH = 550;
	public static final Dimension DEF_DIMENSION = new Dimension(DEF_FIXED_WIDTH, DEF_FIXED_HEIGHT);
	
	public static boolean active = false;
	private CustomizeParametersPanel thisPanel;
	private StartPanel parentStarterPanel;
	private JLabel lblMaxMemory;
	private JLabel lblMinMemory;
	private JSpinner spinnerMinMemoryMB;
	private JSpinner spinnerMaxMemoryMB;
	private JButton btnExitConfiguration;
	private JButton btnReloadInitialConfig;
	private JButton btnSaveConfig;
	
	private int backupMinMemoryXms;
	private int backupMaxMemoryXmx;
	private int backupStackSize;
	private String backupLogLevel;
	private boolean backupViewShell;
	private boolean backupLaunchAsJar;
	private float backupScaleGuiPerc;
	
	private static final int defaultMinMemXms = 512;
	private static final int defaultMaxMemXmx = 1024;
	private static final int defaultStackSize = 128;
	private static final float defaultScaleGuiPerc = 1.0f;
	
	private JLabel lblMaxWarninig;
	private JLabel lblMinWarninig;
	
	private JSpinner spinnerXssMemory;
	private JLabel lblStackSize;
	private JLabel lblLogLevel;
	private JLabel lblSTackSizeInfo;
	private JLabel lblLogLevelInfo;
	private JComboBox comboLogLevel;
	private JCheckBox chckbxActiveShell;
	private JCheckBox chckBoxLaunchAsJar;
	private JSlider sliderGuiScale;
	private JLabel dynLblScaleGui;
	private JLabel labelGuiScale;
	
	public CustomizeParametersPanel(String title, StartPanel parentPanel) {
		
		logger.info(this.getClass().getName() + " Starting...");
		
		// CUSTOM SETTINGS
		
		active = true;
		thisPanel = this;
		
		thisPanel.setTitle(title);		
		parentStarterPanel = parentPanel;
		
		GuiUtils.centerFrame(this, DEF_FIXED_WIDTH, DEF_FIXED_HEIGHT);
		GuiUtils.showOnSameScreen(parentStarterPanel);

		// END CUSTOM SETTINGS
		
		getContentPane().setBackground(new Color(240, 248, 255));
		getContentPane().setLayout(new MigLayout("", "[60px][40px][:20px:20px][:100px:100px][130px][:20px:20px][:100px:100px,grow]", "[grow][grow][grow][grow]"));
		
		lblMaxMemory = new JLabel("Max memory(MB):");
		getContentPane().add(lblMaxMemory, "cell 0 0 2 1,alignx right");
		
		lblMaxWarninig = new JLabel("");
		getContentPane().add(lblMaxWarninig, "cell 2 0");
		
		lblMinMemory = new JLabel("Min memory (MB):");
		getContentPane().add(lblMinMemory, "cell 0 1 2 1,alignx right");
		
		lblMinWarninig = new JLabel("");
		getContentPane().add(lblMinWarninig, "cell 2 1");
		
		spinnerMaxMemoryMB = new JSpinner();
		spinnerMaxMemoryMB.setOpaque(true);
		spinnerMaxMemoryMB.setModel(new SpinnerNumberModel(defaultMaxMemXmx, 128, SysUtils.getMaxMemoryAdmitted(), 20));
		getContentPane().add(spinnerMaxMemoryMB, "cell 3 0,growx");
		
		lblLogLevel = new JLabel("Log level:");
		getContentPane().add(lblLogLevel, "cell 4 0,alignx right");
		
		comboLogLevel = new JComboBox<String>(IniFileManager.getLogLevelsList());
//		comboLogLevel = new JComboBox<String>(); // ONLY FOR VISUALIZE IN WINDOWBUILDER
		getContentPane().add(comboLogLevel, "cell 6 0,growx");
		
		spinnerMinMemoryMB = new JSpinner();
		spinnerMinMemoryMB.setOpaque(true);
		spinnerMinMemoryMB.setModel(new SpinnerNumberModel(defaultMinMemXms, 64, SysUtils.getMaxMemoryAdmitted(), 20));
		getContentPane().add(spinnerMinMemoryMB, "cell 3 1,growx");
		
		lblLogLevelInfo = new JLabel("");
		getContentPane().add(lblLogLevelInfo, "cell 5 0,alignx right");
		
		labelGuiScale = new JLabel("Graphics Scale:");
		getContentPane().add(labelGuiScale, "cell 4 1,alignx right");
		
		dynLblScaleGui = new JLabel("1");
		getContentPane().add(dynLblScaleGui, "cell 5 1,alignx center");
		
		sliderGuiScale = new JSlider();
		sliderGuiScale.setMinimum(0);
		sliderGuiScale.setMaximum(100);
		getContentPane().add(sliderGuiScale, "cell 6 1");
		
		lblStackSize = new JLabel("Stack Size(MB)");
		getContentPane().add(lblStackSize, "cell 0 2 2 1,alignx right");
		
		lblSTackSizeInfo = new JLabel("");
		getContentPane().add(lblSTackSizeInfo, "cell 2 2,alignx right");
		
		spinnerXssMemory = new JSpinner();
		spinnerXssMemory.setModel(new SpinnerNumberModel(4, 4, 256, 1));
		spinnerXssMemory.setOpaque(true);
		getContentPane().add(spinnerXssMemory, "cell 3 2,growx");
		
		chckBoxLaunchAsJar = new JCheckBox(TITLE_LAUNCH_AS_JAR);
		chckBoxLaunchAsJar.setToolTipText(TOOLTIP_LAUNCH_AS_JAR);
		chckBoxLaunchAsJar.setSelected(true);
		chckBoxLaunchAsJar.setFont(new Font("SansSerif", Font.BOLD, 12));
		getContentPane().add(chckBoxLaunchAsJar, "cell 4 2,alignx center");
		
		chckbxActiveShell = new JCheckBox("Display Shell");
		chckbxActiveShell.setToolTipText(TOOLTIP_DISPLAY_SHELL);
		chckbxActiveShell.setFont(new Font("SansSerif", Font.BOLD, 12));
		getContentPane().add(chckbxActiveShell, "cell 5 2 2 1,alignx center");
		
		btnExitConfiguration = new JButton("Discard");
		btnExitConfiguration.setBackground(new Color(250, 128, 114));
		btnExitConfiguration.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNDO));
		btnExitConfiguration.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		getContentPane().add(btnExitConfiguration, "cell 0 3,growx");
		
		btnReloadInitialConfig = new JButton("Reset to Initial Config");
		btnReloadInitialConfig.setEnabled(true);
		btnReloadInitialConfig.setBackground(new Color(250, 250, 210));
		btnReloadInitialConfig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
		btnReloadInitialConfig.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		getContentPane().add(btnReloadInitialConfig, "cell 1 3 3 1,alignx left");
		
		btnSaveConfig = new JButton("Save Launch Options");
		btnSaveConfig.setBackground(new Color(143, 188, 143));
		btnSaveConfig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SAVE_CONF));
		btnSaveConfig.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		getContentPane().add(btnSaveConfig, "cell 4 3 3 1,alignx right");
		
		// add custom behaviour to frame
		
		setGraphics();
		
		addHandlers();
		
		backupInitialValues();
		
		logger.info(this.getClass().getName() + " Started");
	}
	
	private void addHandlers() {
		
		// default close operation(same as reset) 
		thisPanel.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	logger.debug("Closing Frame: CustomizeParametersPanel");	        	
				active = false;
	        	dispose();
		    }
		});
		
		sliderGuiScale.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int perc = sliderGuiScale.getValue();
				String retrieved = getScaleFromPerc(perc);
				dynLblScaleGui.setText(String.valueOf(retrieved));
			}
		});
		
		btnExitConfiguration.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				active = false;
				thisPanel.dispose();
			}
		});
		
		chckBoxLaunchAsJar.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				GeneralConfig.linkedMappings.setProperty("FLAG_LAUNCH_JAR_COMMAND", ((chckBoxLaunchAsJar.isSelected())? "true" : "false"));
				GeneralConfig.reloadProperties();
				if(chckBoxLaunchAsJar.isSelected()) {
					chckBoxLaunchAsJar.setText(TITLE_LAUNCH_AS_JAR);
					chckBoxLaunchAsJar.setToolTipText(TOOLTIP_LAUNCH_AS_JAR);
				}else {
					chckBoxLaunchAsJar.setText((TITLE_LAUNCH_AS_CLASS));
					chckBoxLaunchAsJar.setToolTipText(TOOLTIP_LAUNCH_AS_CLASS);
				}
			}
		});
		
		btnReloadInitialConfig.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerMaxMemoryMB.setValue(backupMaxMemoryXmx);
				spinnerMinMemoryMB.setValue(backupMinMemoryXms);		
				spinnerXssMemory.setValue(backupStackSize);
				comboLogLevel.setSelectedItem(backupLogLevel);
				chckbxActiveShell.setSelected(backupViewShell);
				chckBoxLaunchAsJar.setSelected(backupLaunchAsJar);
				sliderGuiScale.setValue(getPercFromScale(backupScaleGuiPerc));
			}
		});
		
		btnSaveConfig.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// pre-calculate values
				String maxMemory = String.valueOf((Integer)spinnerMaxMemoryMB.getValue());
				String minMemory = String.valueOf((Integer)spinnerMinMemoryMB.getValue());
				String stackSize = String.valueOf((Integer)spinnerXssMemory.getValue());
				String logLevel = (String)comboLogLevel.getSelectedItem();
				String args = "-Xmx"+maxMemory+"m -Xms"+minMemory+"m";
				String stackString = " -Xss"+stackSize+"m";
				boolean viewShell = chckbxActiveShell.isSelected();
				String stringGuiPerc = getScaleFromPerc((Integer)sliderGuiScale.getValue());
				String stringLaunchAsJar = (chckBoxLaunchAsJar.isSelected())? "true" : "false";
				
				IniFileManager iniFileManager = ScrYpTLauncher.iniFileManager;
				// update launcher vars and write to config INI file
				iniFileManager.JVM_MEMORY_OPTIONS = args;
				iniFileManager.JVM_STACK_SIZE = stackString;
				iniFileManager.viewShellAfterStart = viewShell;
				iniFileManager.GUI_SCALE_ARG_VALUE = stringGuiPerc;
				
				// WRITE VALUES ON INI FILE
				iniFileManager.writeValueToIniConfiguration(IniFileManager.INI_JVM_OPT, "memoryArgs", args);
				iniFileManager.writeValueToIniConfiguration(IniFileManager.INI_JVM_OPT, "stackSizeArgs", stackSize);
				iniFileManager.writeValueToIniConfiguration(IniFileManager.INI_APP_OPT, "logLevelSetting", logLevel);
				iniFileManager.writeValueToIniConfiguration(IniFileManager.INI_APP_OPT, "viewCmdConsole", String.valueOf(viewShell));
				iniFileManager.writeValueToIniConfiguration(IniFileManager.INI_APP_OPT, "scaleGuiPerc", stringGuiPerc);
				
				// UPDATE PROPERTIES FILE TOO
				GeneralConfig.resourcesFilePathsMappings.savePropertiesLayoutApache("FLAG_LAUNCH_JAR_COMMAND", stringLaunchAsJar);
				GeneralConfig.reloadProperties();
				
				// regenerate command list with new args
				parentStarterPanel.LAUNCHER.analyzeBeforeLauncher();
				JOptionPane.showMessageDialog(thisPanel, "Settings saved :)", "Settings saved", JOptionPane.INFORMATION_MESSAGE);
				active = false;
				dispose();
				
			}
		});
		
		// add listeners to spinners to prevent max<min and min>max //
		
		spinnerMaxMemoryMB.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				int max = (int)spinnerMaxMemoryMB.getValue();
				int min = (int)spinnerMinMemoryMB.getValue();

				checkWarningsAndTooltips(max, min);
				
				if(max < min) {
					spinnerMinMemoryMB.setValue(max);
				}
			}
		});
		
		spinnerMinMemoryMB.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				int max = (int)spinnerMaxMemoryMB.getValue();
				int min = (int)spinnerMinMemoryMB.getValue();
				
				checkWarningsAndTooltips(max, min);
				
				if(max < min) {
					spinnerMaxMemoryMB.setValue(min);
				}
			}
		});
		
	}
	
	private void checkWarningsAndTooltips(int max, int min) {
		if((max - min) < 100) {
			lblMaxWarninig.setToolTipText(TOOLTIP_WARN_MAX_RAM_SIZE);
			lblMinWarninig.setToolTipText(TOOLTIP_WARN_MIN_RAM_SIZE);
			lblMaxWarninig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GEN_WARNING));
			lblMinWarninig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GEN_WARNING));
		}else {
			lblMaxWarninig.setToolTipText(TOOLTIP_MAX_RAM_SIZE);
			lblMinWarninig.setToolTipText(TOOLTIP_MIN_RAM_SIZE);
			lblMaxWarninig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
			lblMinWarninig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
		}
	}
	
	private void backupInitialValues() {
		IniFileManager iniFileManager = ScrYpTLauncher.iniFileManager;
		backupLogLevel = ScrYpTLauncher.iniFileManager.iniConfigFile.get(IniFileManager.INI_APP_OPT, "logLevelSetting");
		try {
			String memArgsBackup = iniFileManager.JVM_MEMORY_OPTIONS;
			String[] splittedStringArgs = memArgsBackup.split(" ");
			for(String currentParam : splittedStringArgs) {
				if(currentParam.indexOf("-Xmx") >= 0) {
					backupMaxMemoryXmx = Integer.valueOf(currentParam.replaceAll("\\D+",""));
				
				}else if(currentParam.indexOf("-Xms") >= 0) {
					backupMinMemoryXms = Integer.valueOf(currentParam.replaceAll("\\D+",""));
				}
			}
			backupStackSize = Integer.valueOf(iniFileManager.iniConfigFile.get(IniFileManager.INI_JVM_OPT, "stackSizeArgs"));
		} catch (NumberFormatException e) {
			logger.error("Error parsing memory args from string", e);
			backupMaxMemoryXmx = defaultMaxMemXmx;
			backupMinMemoryXms = defaultMinMemXms;
			backupStackSize = defaultStackSize;
		}
		backupViewShell = Boolean.parseBoolean(iniFileManager.iniConfigFile.get(IniFileManager.INI_APP_OPT, "viewCmdConsole"));
		
		try {
			backupScaleGuiPerc = Float.parseFloat(iniFileManager.iniConfigFile.get(IniFileManager.INI_APP_OPT, "scaleGuiPerc"));
		} catch (NumberFormatException e) {
			logger.warn(e);
			backupScaleGuiPerc = defaultScaleGuiPerc;
		}
		backupLaunchAsJar = Boolean.valueOf(GeneralConfig.linkedMappings.getProperty("FLAG_LAUNCH_JAR_COMMAND"))? true : false;
		
		// apply to gui elements
		chckbxActiveShell.setSelected(backupViewShell);
		chckBoxLaunchAsJar.setSelected(backupLaunchAsJar);
		spinnerXssMemory.setValue(backupStackSize);
		spinnerMaxMemoryMB.setValue(backupMaxMemoryXmx);
		spinnerMinMemoryMB.setValue(backupMinMemoryXms);
		comboLogLevel.setSelectedItem(backupLogLevel);
		sliderGuiScale.setValue(getPercFromScale(backupScaleGuiPerc));
	}
	
	private String getScaleFromPerc(int perc) {
		float base = 1.0f;
		float result = base + (float)perc/100;
		
		return TWO_DECIMAL_FORMATTER.format(result).replace(",", ".");
	}
	
	private int getPercFromScale(float scale) {
		int base = 1;
		int result = (int)((scale - base)*100);
		return result;
	}
	
	private void setGraphics() {
		
		GuiUtils.setFrameIcon(IconsPathConfigurator.MAIN_APP_ICON, thisPanel);
		
		lblMaxWarninig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
		lblMaxWarninig.setVisible(true);
		lblMaxWarninig.setToolTipText(TOOLTIP_MAX_RAM_SIZE);
		lblMinWarninig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
		lblMinWarninig.setVisible(true);
		lblMinWarninig.setToolTipText(TOOLTIP_MIN_RAM_SIZE);
		
		lblLogLevelInfo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
		lblLogLevelInfo.setToolTipText(TOOLTIP_LEVEL_LOG);
		lblLogLevelInfo.setVisible(true);
		lblSTackSizeInfo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
		lblSTackSizeInfo.setToolTipText(TOOLTIP_STACK_SIZE);
		lblSTackSizeInfo.setVisible(true);
	}
}
