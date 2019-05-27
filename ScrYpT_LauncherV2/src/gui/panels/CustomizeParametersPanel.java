package gui.panels;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import frameutils.GuiUtils;
import mainlauncher.ScrYpTLauncher;
import net.miginfocom.swing.MigLayout;
import resources.IconsPathConfigurator;
import utils.SysUtils;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import java.awt.Font;

public class CustomizeParametersPanel extends JFrame{
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(CustomizeParametersPanel.class); 
	
	private static final String TOOLTIP_LEVEL_LOG = "Choose a value between 'OFF'(logging feature disabled) and 'ALL'. This can have an impact for overall speed and memory usage (OFF = max speed)";
	private static final String TOOLTIP_STACK_SIZE = "This is the memory used by java JVM to store variables in ram memory called stack. \nHigh value = more reliability but less free memory for other dynamic values!";
	private static final String TOOLTIP_MIN_RAM_SIZE = "It's recommended to use lower value for minimum memory than max, equal values cause performance decrease! ";
	private static final String TOOLTIP_MAX_RAM_SIZE = "It's recommended to use bigger value for max memory than minimum, equal values cause performance decrease! ";
	
	private static final int DEF_FIXED_HEIGHT = 180;
	private static final int DEF_FIXED_WIDTH = 525;
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
	
	private static final int defaultMinMemXms = 512;
	private static final int defaultMaxMemXmx = 1024;
	private static final int defaultStackSize = 128;
	private static final String defaultLogLevel = ScrYpTLauncher.DEFAULT_LOG_LEVEL+"";
	private static final boolean defaultViewShell = false;
	
	private JLabel lblMaxWarninig;
	private JLabel lblMinWarninig;
	
	private JSpinner spinnerXssMemory;
	private JLabel lblStackSize;
	private JLabel lblLogLevel;
	private JLabel lblSTackSizeInfo;
	private JLabel lblLogLevelInfo;
	private JComboBox comboLogLevel;
	private JCheckBox chckbxActiveShell;
	
	public CustomizeParametersPanel(String title, StartPanel parentPanel) {
		
		logger.info(this.getClass().getName() + " Starting...");
		
		// CUSTOM SETTINGS
		active = true;
		thisPanel = this;
		GuiUtils.centerFrame(this, DEF_FIXED_WIDTH, DEF_FIXED_HEIGHT);
		thisPanel.setTitle(title);		
		parentStarterPanel = parentPanel;
		
		// END CUSTOM SETTINGS
		
		getContentPane().setBackground(new Color(240, 248, 255));
		getContentPane().setLayout(new MigLayout("", "[60px][40px][:20px:20px][:100px:100px][100px][:20px:20px][:100px:100px,grow]", "[grow][grow][grow]"));
		
		lblMaxMemory = new JLabel("Max memory(MB):");
		getContentPane().add(lblMaxMemory, "cell 0 0 2 1,alignx right");
		
		lblMaxWarninig = new JLabel("");
		getContentPane().add(lblMaxWarninig, "cell 2 0");
		
		spinnerMaxMemoryMB = new JSpinner();
		spinnerMaxMemoryMB.setOpaque(true);
		spinnerMaxMemoryMB.setModel(new SpinnerNumberModel(defaultMaxMemXmx, 256, SysUtils.getMaxMemoryAdmitted(), 20));
		getContentPane().add(spinnerMaxMemoryMB, "cell 3 0,growx");
		
		lblStackSize = new JLabel("Stack Size(MB)");
		getContentPane().add(lblStackSize, "cell 4 0,alignx right");
		
		lblSTackSizeInfo = new JLabel("");
		getContentPane().add(lblSTackSizeInfo, "cell 5 0,alignx right");
		
		spinnerXssMemory = new JSpinner();
		spinnerXssMemory.setModel(new SpinnerNumberModel(4, 4, 256, 1));
		spinnerXssMemory.setOpaque(true);
		getContentPane().add(spinnerXssMemory, "cell 6 0,growx");
		
		lblMinMemory = new JLabel("Min memory (MB):");
		getContentPane().add(lblMinMemory, "cell 0 1 2 1,alignx right");
		
		lblMinWarninig = new JLabel("");
		getContentPane().add(lblMinWarninig, "cell 2 1");
		
		spinnerMinMemoryMB = new JSpinner();
		spinnerMinMemoryMB.setOpaque(true);
		spinnerMinMemoryMB.setModel(new SpinnerNumberModel(defaultMinMemXms, 128, SysUtils.getMaxMemoryAdmitted(), 20));
		getContentPane().add(spinnerMinMemoryMB, "cell 3 1,growx");
		
		lblLogLevel = new JLabel("Log level");
		getContentPane().add(lblLogLevel, "cell 4 1,alignx right");
		
		lblLogLevelInfo = new JLabel("");
		getContentPane().add(lblLogLevelInfo, "cell 5 1,alignx right");
		
		comboLogLevel = new JComboBox<String>(ScrYpTLauncher.getLogLevelsList());
//		comboLogLevel = new JComboBox<String>();
		getContentPane().add(comboLogLevel, "cell 6 1,growx");
		
		btnExitConfiguration = new JButton("Discard");
		btnExitConfiguration.setBackground(new Color(250, 128, 114));
		btnExitConfiguration.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		getContentPane().add(btnExitConfiguration, "cell 0 2,growx");
		
		btnReloadInitialConfig = new JButton("Reset to Initial Config");
		btnReloadInitialConfig.setEnabled(true);
		btnReloadInitialConfig.setBackground(new Color(250, 250, 210));
		btnReloadInitialConfig.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		getContentPane().add(btnReloadInitialConfig, "cell 1 2 3 1,alignx left");
		
		chckbxActiveShell = new JCheckBox("Shell");
		chckbxActiveShell.setFont(new Font("Segoe UI", Font.BOLD, 13));
		getContentPane().add(chckbxActiveShell, "cell 4 2,alignx center");
		
		btnSaveConfig = new JButton("Save Launch Options");
		btnSaveConfig.setBackground(new Color(143, 188, 143));
		btnSaveConfig.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		getContentPane().add(btnSaveConfig, "cell 5 2 2 1,alignx right");
		
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
		
		btnExitConfiguration.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				active = false;
				thisPanel.dispose();
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
			}
		});
		
		btnSaveConfig.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String maxMemory = String.valueOf((Integer)spinnerMaxMemoryMB.getValue());
				String minMemory = String.valueOf((Integer)spinnerMinMemoryMB.getValue());
				String stackSize = String.valueOf((Integer)spinnerXssMemory.getValue());
				String logLevel = (String)comboLogLevel.getSelectedItem();
				String args = "-Xmx"+maxMemory+"m -Xms"+minMemory+"m";
				String stackString = " -Xss"+stackSize+"m";
				boolean viewShell = chckbxActiveShell.isSelected();
				
				// update launcher vars and write to config INI file
				ScrYpTLauncher.JVM_MEMORY_OPTIONS = args;
				ScrYpTLauncher.JVM_STACK_SIZE = stackString;
				ScrYpTLauncher.viewShellAfterStart = viewShell;
				ScrYpTLauncher.writeValueToIniConfiguration(ScrYpTLauncher.INI_JVM_OPT, "memoryArgs", args);
				ScrYpTLauncher.writeValueToIniConfiguration(ScrYpTLauncher.INI_JVM_OPT, "stackSizeArgs", stackSize);
				ScrYpTLauncher.writeValueToIniConfiguration(ScrYpTLauncher.INI_APP_OPT, "logLevelSetting", logLevel);
				ScrYpTLauncher.writeValueToIniConfiguration(ScrYpTLauncher.INI_APP_OPT, "viewCmdConsole", String.valueOf(viewShell));
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
			lblMaxWarninig.setVisible(true);
			lblMinWarninig.setVisible(true);
			lblMaxWarninig.setToolTipText(TOOLTIP_MAX_RAM_SIZE);
			lblMinWarninig.setToolTipText(TOOLTIP_MIN_RAM_SIZE);
		}else {
			lblMaxWarninig.setVisible(false);
			lblMinWarninig.setVisible(false);
			lblMaxWarninig.setToolTipText(null);
			lblMinWarninig.setToolTipText(null);
		}
	}
	
	private void backupInitialValues() {
		backupLogLevel = ScrYpTLauncher.iniConfigFile.get(ScrYpTLauncher.INI_APP_OPT, "logLevelSetting");
		try {
			String memArgsBackup = ScrYpTLauncher.JVM_MEMORY_OPTIONS;
			String[] splittedStringArgs = memArgsBackup.split(" ");
			for(String currentParam : splittedStringArgs) {
				if(currentParam.indexOf("-Xmx") >= 0) {
					backupMaxMemoryXmx = Integer.valueOf(currentParam.replaceAll("\\D+",""));
				
				}else if(currentParam.indexOf("-Xms") >= 0) {
					backupMinMemoryXms = Integer.valueOf(currentParam.replaceAll("\\D+",""));
				}
			}
			backupStackSize = Integer.valueOf(ScrYpTLauncher.iniConfigFile.get(ScrYpTLauncher.INI_JVM_OPT, "stackSizeArgs"));
		} catch (NumberFormatException e) {
			logger.error("Error parsing memory args from string", e);
			backupMaxMemoryXmx = defaultMaxMemXmx;
			backupMinMemoryXms = defaultMinMemXms;
			backupStackSize = defaultStackSize;
		}
		backupViewShell = Boolean.parseBoolean(ScrYpTLauncher.iniConfigFile.get(ScrYpTLauncher.INI_APP_OPT, "viewCmdConsole"));
		
		// apply to gui elements
		chckbxActiveShell.setSelected(backupViewShell);
		spinnerXssMemory.setValue(backupStackSize);
		spinnerMaxMemoryMB.setValue(backupMaxMemoryXmx);
		spinnerMinMemoryMB.setValue(backupMinMemoryXms);
		comboLogLevel.setSelectedItem(backupLogLevel);
	}
	
	private void setGraphics() {
		
		GuiUtils.setFrameIcon("Resources_Scrypt/icon.jpg", thisPanel);
		
		lblMaxWarninig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GEN_WARNING));
		lblMaxWarninig.setVisible(false);
		lblMinWarninig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GEN_WARNING));
		lblMinWarninig.setVisible(false);
		
		lblLogLevelInfo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
		lblLogLevelInfo.setToolTipText(TOOLTIP_LEVEL_LOG);
		lblSTackSizeInfo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
		lblSTackSizeInfo.setToolTipText(TOOLTIP_STACK_SIZE);
	}
}
