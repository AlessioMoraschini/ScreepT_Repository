package gui.panels;

import javax.swing.JFrame;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import gui.GuiUtils;
import gui.JOptionHelper;
import launcherutils.IniFileManager;
import launcherutils.JvmHelper;
import mainlauncher.ScrYpTLauncher;
import net.miginfocom.swing.MigLayout;
import om.OSinfo;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import resources.ImagesPathConfigurator;
import utils.SysUtils;

import javax.swing.JLabel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import java.awt.Font;
import javax.swing.JSlider;
import javax.swing.JSeparator;

public class CustomizeParametersPanel extends JFrame{
	private static final long serialVersionUID = 1712282193344302745L;

	// CustomizeParametersPanel logger
	static Logger logger = Logger.getLogger(CustomizeParametersPanel.class); 
	
	public static final DecimalFormat TWO_DECIMAL_FORMATTER = new DecimalFormat("#.##");
	private static final String TOOLTIP_LEVEL_LOG = "Choose a value between 'OFF'(logging feature disabled) and 'ALL'. This can have an impact for overall speed and memory usage (OFF = max speed)";
	private static final String TOOLTIP_STACK_SIZE = "This is the memory used by java JVM to store variables in ram memory called stack. \nHigh value = more reliability but less free memory for other dynamic values!";
	private static final String TOOLTIP_WARN_DIFFERENT_XSS_XMX_ARGS = "It's recommended to use same value for max and minimum memory, different values can cause a performance decrease (use different values if you need dynamic memory allocation)";
	private static final String TOOLTIP_MIN_RAM_SIZE = "This is the minimum heap ram value used bj JVM to start ScrYpT dynamic data allocation";
	private static final String TOOLTIP_MAX_RAM_SIZE = "This is the maximum heap ram value used bj JVM allocated to ScrYpT for dynamic data allocation";
	private static final String TOOLTIP_DISPLAY_SHELL = "When checked, the launcher will keep an open active command line shell with the logger's output";
	private static final String TOOLTIP_LAUNCH_AS_JAR_WARN = "Works only with centralized build, pugins might not work with this configuration, use this only if you know what you're doing!)";
	private static final String TOOLTIP_LAUNCH_AS_JAR = "ScrYpT will be launched as Jar (works only with centralized build, pugins might not work with this configuration!)";
	private static final String TOOLTIP_LAUNCH_AS_CLASS = "ScrYpT will be launched as .Class (works with centralized build and even with multimodule)";
	private final String TOOLTIP_INFO_JVM = "Select your preferred, but remember that this software is fully supported only for JVMs between version "
			+ IniFileManager.MIN_JRE_VERSION + " <- And -> " + IniFileManager.MAX_JRE_VERSION_FULL_COMPATIBILITY;
	private final String TOOLTIP_WARN_JVM = "It seems that chosen version is not the best match, full software compatibility is granted for JVM versions between "
			+ IniFileManager.MIN_JRE_VERSION + " <- And -> " + IniFileManager.MAX_JRE_VERSION_FULL_COMPATIBILITY;
	
	private static final String TITLE_LAUNCH_AS_JAR = "Launch as .Jar:";
	private static final String TITLE_LAUNCH_AS_CLASS = "Launch as .Class:";
	
	private static final int DEF_FIXED_HEIGHT = 245;
	private static final int DEF_FIXED_WIDTH = 575;
	public static final Dimension DEF_DIMENSION = new Dimension(DEF_FIXED_WIDTH, DEF_FIXED_HEIGHT);
	
	public static boolean active = false;
	private CustomizeParametersPanel thisPanel;
	private LauncherMainPanel parentStarterPanel;
	private JLabel lblMaxMemory;
	private JLabel lblMinMemory;
	private JSpinner spinnerMinMemoryMB;
	private JSpinner spinnerMaxMemoryMB;
	private JButton btnExitConfiguration;
	private JButton btnReloadInitialConfig;
	private JButton btnLoadScryptDefault;
	private JButton btnSaveConfig;
	
	private int backupMinMemoryXms;
	private int backupMaxMemoryXmx;
	private int backupStackSize;
	private String backupLogLevel;
	private boolean backupViewShell;
	private boolean backupLaunchAsJar;
	private float backupScaleGuiPerc;
	private String backupPreferredJvm;
	
	private static final int defaultMinMemXms = 256;
	private static final int defaultMaxMemXmx = 512;
	private static final int defaultStackSize = 128;
	private static final float defaultScaleGuiPerc = 1.0f;
	
	private JLabel lblMaxWarninig;
	private JLabel lblMinWarninig;
	
	private JSpinner spinnerXssMemory;
	private JLabel lblStackSize;
	private JLabel lblLogLevel;
	private JLabel lblSTackSizeInfo;
	private JLabel lblLogLevelInfo;
	private JComboBox<String> comboLogLevel;
	private JCheckBox chckbxActiveShell;
	private JCheckBox chckBoxLaunchAsJar;
	private JSlider sliderGuiScale;
	private JLabel dynLblScaleGui;
	private JLabel labelGuiScale;
	private JComboBox<String> comboPrefJvm;
	private JLabel lblPreferredJvm;
	private JLabel lblJvmInfo;
	
	private OSinfo currentOS = new OSinfo();
	private JSeparator separator;
	
	public boolean JVM_WARN = false;
	private JLabel lblLaunchJarIconWarn;
	
	//ArrayList<String> foundJvmHomeList = SysUtils.findJvmVersionsInstalled(currentOS);
	
	public CustomizeParametersPanel(String title, LauncherMainPanel parentPanel) {
		getContentPane().setForeground(new Color(220, 220, 220));
		
		logger.info(this.getClass().getName() + " Starting...");
		
		// CUSTOM SETTINGS
		
		active = true;
		thisPanel = this;
		
		thisPanel.setTitle(title);	
		parentStarterPanel = parentPanel;
		
		GuiUtils.centerFrame(this, DEF_FIXED_WIDTH, DEF_FIXED_HEIGHT);
		thisPanel.setLocationRelativeTo(parentStarterPanel);
		this.setAlwaysOnTop(true);

		// END CUSTOM SETTINGS
		
		getContentPane().setBackground(Color.DARK_GRAY);
		getContentPane().setLayout(new MigLayout("", "[60px,grow][40px][:20px:20px][:60px:90px][25px:n:25px][100px:100px:100px,grow][:20px:20px][80px,grow]", "[grow][grow][grow][grow][][grow]"));
		
		lblMaxMemory = new JLabel("Max memory(MB):");
		lblMaxMemory.setForeground(new Color(220, 220, 220));
		getContentPane().add(lblMaxMemory, "cell 0 0 2 1,alignx center");
		
		lblMaxWarninig = new JLabel("");
		getContentPane().add(lblMaxWarninig, "cell 2 0");
		
		lblMinMemory = new JLabel("Min memory (MB):");
		lblMinMemory.setForeground(new Color(220, 220, 220));
		getContentPane().add(lblMinMemory, "cell 0 1 2 1,alignx center");
		
		lblMinWarninig = new JLabel("");
		getContentPane().add(lblMinWarninig, "cell 2 1");
		
		spinnerMaxMemoryMB = new JSpinner();
		spinnerMaxMemoryMB.setOpaque(true);
		spinnerMaxMemoryMB.setCursor(GuiUtils.CURSOR_HAND);
		spinnerMaxMemoryMB.setModel(new SpinnerNumberModel(defaultMaxMemXmx, 128, SysUtils.getMaxMemoryAdmitted(), 20));
		getContentPane().add(spinnerMaxMemoryMB, "cell 3 0,growx");
		
		lblLogLevel = new JLabel("Log level:");
		lblLogLevel.setForeground(new Color(220, 220, 220));
		getContentPane().add(lblLogLevel, "cell 5 0,alignx right");
		
//		comboLogLevel = new JComboBox<String>(); // Windowbuilder workaround for tests
		comboLogLevel = new JComboBox<String>(IniFileManager.getLogLevelsList());
		comboLogLevel.setCursor(GuiUtils.CURSOR_HAND);
		getContentPane().add(comboLogLevel, "cell 7 0,growx");
		
		spinnerMinMemoryMB = new JSpinner();
		spinnerMinMemoryMB.setOpaque(true);
		spinnerMinMemoryMB.setCursor(GuiUtils.CURSOR_HAND);
		spinnerMinMemoryMB.setModel(new SpinnerNumberModel(defaultMinMemXms, 64, SysUtils.getMaxMemoryAdmitted(), 20));
		getContentPane().add(spinnerMinMemoryMB, "cell 3 1,growx");
		
		lblLogLevelInfo = new JLabel("");
		getContentPane().add(lblLogLevelInfo, "cell 6 0,alignx right");
		
		labelGuiScale = new JLabel("Graphics Scale:");
		labelGuiScale.setForeground(new Color(220, 220, 220));
		getContentPane().add(labelGuiScale, "cell 5 1,alignx right");
		
		dynLblScaleGui = new JLabel("1");
		dynLblScaleGui.setForeground(new Color(220, 220, 220));
		getContentPane().add(dynLblScaleGui, "cell 6 1,alignx center");
		
		sliderGuiScale = new JSlider();
		sliderGuiScale.setMinimum(0);
		sliderGuiScale.setMaximum(100);
		sliderGuiScale.setCursor(GuiUtils.CURSOR_HAND);
		getContentPane().add(sliderGuiScale, "cell 7 1");
		
		lblStackSize = new JLabel("Stack Size(MB)");
		lblStackSize.setForeground(new Color(220, 220, 220));
		getContentPane().add(lblStackSize, "cell 0 2 2 1,alignx center");
		
		lblSTackSizeInfo = new JLabel("");
		getContentPane().add(lblSTackSizeInfo, "cell 2 2,alignx right");
		
		spinnerXssMemory = new JSpinner();
		spinnerXssMemory.setModel(new SpinnerNumberModel(4, 4, 256, 1));
		spinnerXssMemory.setOpaque(true);
		spinnerXssMemory.setCursor(GuiUtils.CURSOR_HAND);
		getContentPane().add(spinnerXssMemory, "cell 3 2,growx");
		
		lblLaunchJarIconWarn = new JLabel("");
		getContentPane().add(lblLaunchJarIconWarn, "cell 4 2,alignx right");
		
		chckBoxLaunchAsJar = new JCheckBox("Launch as .Jar");
		chckBoxLaunchAsJar.setOpaque(false);
		chckBoxLaunchAsJar.setForeground(new Color(220, 220, 220));
		chckBoxLaunchAsJar.setToolTipText(TOOLTIP_LAUNCH_AS_JAR);
		chckBoxLaunchAsJar.setCursor(GuiUtils.CURSOR_HAND);
		chckBoxLaunchAsJar.setSelected(true);
		chckBoxLaunchAsJar.setFont(new Font("SansSerif", Font.BOLD, 12));
		getContentPane().add(chckBoxLaunchAsJar, "cell 5 2 2 1,alignx center,aligny center");
		
		chckbxActiveShell = new JCheckBox("Display Shell");
		chckbxActiveShell.setOpaque(false);
		chckbxActiveShell.setForeground(new Color(220, 220, 220));
		chckbxActiveShell.setToolTipText(TOOLTIP_DISPLAY_SHELL);
		chckbxActiveShell.setCursor(GuiUtils.CURSOR_HAND);
		chckbxActiveShell.setFont(new Font("SansSerif", Font.BOLD, 12));
		getContentPane().add(chckbxActiveShell, "cell 7 2,alignx center");
		
		lblPreferredJvm = new JLabel("Preferred JVM");
		lblPreferredJvm.setForeground(new Color(220, 220, 220));
		getContentPane().add(lblPreferredJvm, "cell 0 3 2 1,alignx center");
		
		String[] jvmArr = new String[1];
		ArrayList<String> JVMS = SysUtils.findJvmVersionsInstalled(currentOS);
		if(ScrYpTLauncher.iniFileManager.manualPathJvm != null && !ScrYpTLauncher.iniFileManager.manualPathJvm.equals("")) {
			JVMS.add(ScrYpTLauncher.iniFileManager.manualPathJvm);
		}
		
		lblJvmInfo = new JLabel("");
		lblJvmInfo.setCursor(GuiUtils.CURSOR_HAND);
		getContentPane().add(lblJvmInfo, "cell 2 3,alignx trailing");
		
		comboPrefJvm = new JComboBox<String>(JVMS.toArray(jvmArr));
//		comboPrefJvm = new JComboBox<String>(); // Window builder bug workaround for test
		comboPrefJvm.setCursor(GuiUtils.CURSOR_HAND);
		getContentPane().add(comboPrefJvm, "cell 3 3 5 1,growx");
		
		separator = new JSeparator();
		getContentPane().add(separator, "cell 0 4 8 1,growx");
		
		btnExitConfiguration = new JButton("Discard");
		btnExitConfiguration.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
		btnExitConfiguration.setBackground(new Color(207, 77, 70));
		btnExitConfiguration.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNDO));
		btnExitConfiguration.setCursor(GuiUtils.CURSOR_HAND);
		getContentPane().add(btnExitConfiguration, "cell 0 5,growx");
		
		btnReloadInitialConfig = new JButton("Reset to Initial Values");
		btnReloadInitialConfig.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
		btnReloadInitialConfig.setForeground(Color.WHITE);
		btnReloadInitialConfig.setEnabled(true);
		btnReloadInitialConfig.setBackground(Color.DARK_GRAY);
		btnReloadInitialConfig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
		btnReloadInitialConfig.setCursor(GuiUtils.CURSOR_HAND);
		getContentPane().add(btnReloadInitialConfig, "cell 1 5 3 1,growx,aligny center");
		
		btnLoadScryptDefault = new JButton();
		btnLoadScryptDefault.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
		btnLoadScryptDefault.setForeground(Color.WHITE);
		btnLoadScryptDefault.setText("Use Defaults");
		btnLoadScryptDefault.setEnabled(true);
		btnLoadScryptDefault.setBackground(Color.DARK_GRAY);
		btnLoadScryptDefault.setIcon(new ImageIcon(IconsPathConfigurator.iconRootPath));
		btnLoadScryptDefault.setCursor(GuiUtils.CURSOR_HAND);
		getContentPane().add(btnLoadScryptDefault, "cell 4 5 3 1,growx,aligny center");

		btnSaveConfig = new JButton("Save Launch Options");
		btnSaveConfig.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
		btnSaveConfig.setBackground(new Color(85, 166, 66));
		btnSaveConfig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SAVE_CONF));
		btnSaveConfig.setCursor(GuiUtils.CURSOR_HAND);
		getContentPane().add(btnSaveConfig, "cell 7 5,growx,aligny center");
		
		// add custom behaviour to frame
		
		setGraphics();
		
		addHandlers();
		
		backupInitialValues();
		
		this.setMinimumSize(DEF_DIMENSION);
		this.pack();
		
		logger.info(this.getClass().getName() + " Started");
		
		JVM_WARN = JvmHelper.warningJVM;
		updateJvmIcon(JVM_WARN);
		
		refreshCheckBoxLaunchAsJar();
	}
	
	private void addHandlers() {
		
		// default close operation(same as reset) 
		thisPanel.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	logger.debug("Closing Frame: CustomizeParametersPanel");	        	
				active = false;
				ScrYpTLauncher.iniFileManager.initFromINIFile();
	        	dispose();
		    }
		});
		
		// LOAD SCRYPT'S DEFAULTS INTO GUI LAUNCHER BOXES
		btnLoadScryptDefault.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						spinnerMaxMemoryMB.setValue(GeneralConfig.DEFAULT_JVM_XMX);
						spinnerMinMemoryMB.setValue(GeneralConfig.DEFAULT_JVM_XMS);
						spinnerXssMemory.setValue(GeneralConfig.DEFAULT_JVM_XSS);
						sliderGuiScale.setValue(getPercFromScale(GeneralConfig.DEFAULT_JVM_GUI));
						comboLogLevel.setSelectedItem(GeneralConfig.DEFAULT_LOG_LEVEL);
						chckBoxLaunchAsJar.setSelected(GeneralConfig.DEFAULT_JVM_JAR_MODE);
						chckbxActiveShell.setSelected(GeneralConfig.DEFAULT_JVM_SHELL_ON);
						
						String javaPath = new JvmHelper("1.9", "1.8").retrieveCorrectJVMS(false);
						selectComboJVM(javaPath);
						thisPanel.revalidate();
						thisPanel.repaint();
					}
				});
			}
		});
		
		chckBoxLaunchAsJar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshCheckBoxLaunchAsJar();
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
				refreshCheckBoxLaunchAsJar();
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
				selectComboJVM(backupPreferredJvm);
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
				String preferredJvm = (String)comboPrefJvm.getSelectedItem();
				
				IniFileManager iniFileManager = ScrYpTLauncher.iniFileManager;
				// update launcher vars and write to config INI file
				iniFileManager.JVM_MEMORY_OPTIONS = args;
				iniFileManager.JVM_STACK_SIZE = stackString;
				iniFileManager.viewShellAfterStart = viewShell;
				iniFileManager.GUI_SCALE_ARG_VALUE = stringGuiPerc;
				
				// WRITE VALUES ON INI FILE
				iniFileManager.writeValueToIniConfiguration(IniFileManager.INI_JVM_OPT, "memoryArgs", args);
				iniFileManager.writeValueToIniConfiguration(IniFileManager.INI_JVM_OPT, "stackSizeArgs", stackSize);
				iniFileManager.writeValueToIniConfiguration(IniFileManager.INI_JVM_OPT, "preferredJvm", checkJvmString(preferredJvm));
				iniFileManager.writeValueToIniConfiguration(IniFileManager.INI_APP_OPT, "logLevelSetting", logLevel);
				iniFileManager.writeValueToIniConfiguration(IniFileManager.INI_APP_OPT, "viewCmdConsole", String.valueOf(viewShell));
				iniFileManager.writeValueToIniConfiguration(IniFileManager.INI_APP_OPT, "scaleGuiPerc", stringGuiPerc);
				
				// UPDATE PROPERTIES FILE TOO
				GeneralConfig.resourcesFilePathsMappings.savePropertiesLayoutApache("FLAG_LAUNCH_JAR_COMMAND", stringLaunchAsJar);
				GeneralConfig.reloadProperties();
				
				iniFileManager.initFromINIFile();
				
				// regenerate command list with new args
				parentStarterPanel.LAUNCHER.analyzeBeforeLauncher();
				new JOptionHelper(thisPanel).info("Settings saved :)", "Settings saved");
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
		
		// Update icon warn or info
		comboPrefJvm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateJvmIcon();
			}
		});
		
	}
	
	private void checkWarningsAndTooltips(int max, int min) {
		if(max != min) {
			lblMaxWarninig.setToolTipText(TOOLTIP_WARN_DIFFERENT_XSS_XMX_ARGS);
			lblMinWarninig.setToolTipText(TOOLTIP_WARN_DIFFERENT_XSS_XMX_ARGS);
			lblMaxWarninig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GEN_WARNING));
			lblMinWarninig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GEN_WARNING));
		}else {
			lblMaxWarninig.setToolTipText(TOOLTIP_MAX_RAM_SIZE);
			lblMinWarninig.setToolTipText(TOOLTIP_MIN_RAM_SIZE);
			lblMaxWarninig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
			lblMinWarninig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
		}
	}
	
	private void updateJvmIcon() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String selectedVersion = (String)comboPrefJvm.getSelectedItem();
				File selectedVersionFile = new File(selectedVersion);
				JVM_WARN = !ScrYpTLauncher.jvmHelper.checkSingleJavaVersionOK(selectedVersionFile.getName(), IniFileManager.MAX_JRE_VERSION_FULL_COMPATIBILITY, IniFileManager.MIN_JRE_VERSION);
				
				if (JVM_WARN) {
					lblJvmInfo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GEN_WARNING));
					lblJvmInfo.setToolTipText(TOOLTIP_WARN_JVM);

				} else {
					lblJvmInfo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
					lblJvmInfo.setToolTipText(TOOLTIP_INFO_JVM);
				}
			}
		});
	}
	
	private void refreshCheckBoxLaunchAsJar() {
		if (chckBoxLaunchAsJar.isSelected()) {
			lblLaunchJarIconWarn.setToolTipText(TOOLTIP_LAUNCH_AS_JAR_WARN);
			lblLaunchJarIconWarn.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GEN_WARNING));
			chckBoxLaunchAsJar.setText(TITLE_LAUNCH_AS_JAR);
			chckBoxLaunchAsJar.setToolTipText(TOOLTIP_LAUNCH_AS_JAR);
		} else {
			lblLaunchJarIconWarn.setToolTipText("");
			lblLaunchJarIconWarn.setIcon(new ImageIcon());
			chckBoxLaunchAsJar.setText((TITLE_LAUNCH_AS_CLASS));
			chckBoxLaunchAsJar.setToolTipText(TOOLTIP_LAUNCH_AS_CLASS);
		}
	}
	
	private void updateJvmIcon(Boolean JVM_WARN) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (JVM_WARN) {
					lblJvmInfo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GEN_WARNING));
					lblJvmInfo.setToolTipText(TOOLTIP_WARN_JVM);

				} else {
					lblJvmInfo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
					lblJvmInfo.setToolTipText(TOOLTIP_INFO_JVM);
				}
			}
		});
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
		backupPreferredJvm  = StringUtils.trimToEmpty(iniFileManager.iniConfigFile.get(IniFileManager.INI_JVM_OPT, "preferredJvm"));
		backupPreferredJvm = checkJvmString(backupPreferredJvm);
		
		// apply to gui elements
		chckbxActiveShell.setSelected(backupViewShell);
		chckBoxLaunchAsJar.setSelected(backupLaunchAsJar);
		spinnerXssMemory.setValue(backupStackSize);
		spinnerMaxMemoryMB.setValue(backupMaxMemoryXmx);
		spinnerMinMemoryMB.setValue(backupMinMemoryXms);
		comboLogLevel.setSelectedItem(backupLogLevel);
		sliderGuiScale.setValue(getPercFromScale(backupScaleGuiPerc));
		selectComboJVM(backupPreferredJvm);
	}
	
	private String checkJvmString(String jvmStr) {
		String result = (jvmStr.equals("") 
				|| !SysUtils.findJvmVersionsInstalled(new OSinfo()).contains(jvmStr) 
				|| !ScrYpTLauncher.jvmHelper.checkSingleJavaVersionOK(new File(jvmStr).getName()))?
						
				ScrYpTLauncher.jvmHelper.retrieveCorrectJVMS(false) : jvmStr;
		return result;
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
	
	private void selectComboJVM(String target) {
		if(target == null) {return;}
		
		for (int i = 0; i < comboPrefJvm.getItemCount(); i++) {
			String currentComboValue = (String)comboPrefJvm.getItemAt(i);
			if (currentComboValue.contains(target) || target.contains(currentComboValue)) {
				comboPrefJvm.setSelectedIndex(i);
				comboPrefJvm.revalidate();
				updateJvmIcon();
				return;
			} 
		}
	}
	
	private void setGraphics() {
		
		GuiUtils.setFrameIcon(ImagesPathConfigurator.MAIN_APP_ICON, thisPanel);
		
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
