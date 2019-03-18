package gui.panels;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import gui.ScrYpTLauncher;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class CustomizeParametersPanel extends JFrame{
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(CustomizeParametersPanel.class); 
	
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
	
	private static final int defaultMinMemXms = 512;
	private static final int defaultMaxMemXmx = 1024;
	
	public CustomizeParametersPanel(String title, StartPanel parentPanel) {
		
		// CUSTOM SETTINGS
		active = true;
		thisPanel = this;
		this.setBounds(160, 180, 540, 190);
		thisPanel.setTitle(title);		
		parentStarterPanel = parentPanel;
		// END CUSTOM SETTINGS
		
		getContentPane().setBackground(Color.WHITE);
		getContentPane().setLayout(new MigLayout("", "[100px,grow][][70px]", "[grow][grow][grow]"));
		
		lblMaxMemory = new JLabel("Max memory(MB):");
		getContentPane().add(lblMaxMemory, "cell 0 0,alignx center");
		
		spinnerMaxMemoryMB = new JSpinner();
		spinnerMaxMemoryMB.setModel(new SpinnerNumberModel(defaultMaxMemXmx, 256, 4000, 20));
		getContentPane().add(spinnerMaxMemoryMB, "cell 1 0 2 1,growx");
		
		lblMinMemory = new JLabel("Min memory (MB):");
		getContentPane().add(lblMinMemory, "cell 0 1,alignx center");
		
		spinnerMinMemoryMB = new JSpinner();
		spinnerMinMemoryMB.setModel(new SpinnerNumberModel(defaultMinMemXms, 128, 1024, 20));
		getContentPane().add(spinnerMinMemoryMB, "cell 1 1 2 1,growx");
		
		btnExitConfiguration = new JButton("Discard and return to Launcher");
		btnExitConfiguration.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnExitConfiguration, "cell 0 2,growx");
		
		btnReloadInitialConfig = new JButton("Reset to Initial Config");
		btnReloadInitialConfig.setToolTipText("Coming soon :)");
		btnReloadInitialConfig.setEnabled(false);
		btnReloadInitialConfig.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnReloadInitialConfig, "cell 1 2,growx");
		
		btnSaveConfig = new JButton("Save Custom Config");
		btnSaveConfig.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnSaveConfig, "cell 2 2,growx");
		
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
				// TODO parse values from String 				
			}
		});
		
		btnSaveConfig.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String maxMemory = String.valueOf((Integer)spinnerMaxMemoryMB.getValue());
				String minMemory = String.valueOf((Integer)spinnerMinMemoryMB.getValue());
				String args = "-Xmx"+maxMemory+"m -Xms"+minMemory;
				
				// update launcher vars and write to config INI file
				String SECTION_ARGS = ScrYpTLauncher.INI_JVM_OPT;
				ScrYpTLauncher.JVM_MEMORY_OPTIONS = args;
				ScrYpTLauncher.writeValueToIniConfiguration(SECTION_ARGS, "memoryArgs", args);
				// regenerate command list with new args
				parentStarterPanel.LAUNCHER.analyzeBeforeLauncher();
				dispose();
				
			}
		});
	}
	
	private void backupInitialValues() {
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
		} catch (NumberFormatException e) {
			logger.error("Error parsing memory args from string", e);
			backupMaxMemoryXmx = defaultMaxMemXmx;
			backupMinMemoryXms = defaultMinMemXms;
		}
		
		spinnerMaxMemoryMB.setValue(backupMaxMemoryXmx);
		spinnerMinMemoryMB.setValue(backupMinMemoryXms);
	}
}
