package gui.panels;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import frameutils.GuiUtils;
import gui.ScrYpTLauncher;
import launcherutils.SysUtils;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
	private JLabel lblMaxWarninig;
	private JLabel lblMinWarninig;
	
	private Image warningImage;
	
	public CustomizeParametersPanel(String title, StartPanel parentPanel) {
		
		logger.info(this.getClass().getName() + " Starting...");
		
		// CUSTOM SETTINGS
		active = true;
		thisPanel = this;
		GuiUtils.centerFrame(this, 540, 190);
		thisPanel.setTitle(title);		
		parentStarterPanel = parentPanel;
		
		// END CUSTOM SETTINGS
		
		getContentPane().setBackground(Color.WHITE);
		getContentPane().setLayout(new MigLayout("", "[100px,grow][20px][][70px]", "[grow][grow][grow]"));
		
		lblMaxMemory = new JLabel("Max memory(MB):");
		getContentPane().add(lblMaxMemory, "cell 0 0,alignx center");
		
		lblMaxWarninig = new JLabel("");
		getContentPane().add(lblMaxWarninig, "cell 1 0");
		
		spinnerMaxMemoryMB = new JSpinner();
		spinnerMaxMemoryMB.setOpaque(true);
		spinnerMaxMemoryMB.setModel(new SpinnerNumberModel(defaultMaxMemXmx, 256, SysUtils.getMaxMemoryAdmitted(), 20));
		getContentPane().add(spinnerMaxMemoryMB, "cell 2 0 2 1,growx");
		
		lblMinMemory = new JLabel("Min memory (MB):");
		getContentPane().add(lblMinMemory, "cell 0 1,alignx center");
		
		lblMinWarninig = new JLabel("");
		getContentPane().add(lblMinWarninig, "cell 1 1");
		
		spinnerMinMemoryMB = new JSpinner();
		spinnerMinMemoryMB.setOpaque(true);
		spinnerMinMemoryMB.setModel(new SpinnerNumberModel(defaultMinMemXms, 128, SysUtils.getMaxMemoryAdmitted(), 20));
		getContentPane().add(spinnerMinMemoryMB, "cell 2 1 2 1,growx");
		
		btnExitConfiguration = new JButton("Discard and return to Launcher");
		btnExitConfiguration.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnExitConfiguration, "cell 0 2 2 1,growx");
		
		btnReloadInitialConfig = new JButton("Reset to Initial Config");
		btnReloadInitialConfig.setEnabled(true);
		btnReloadInitialConfig.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnReloadInitialConfig, "cell 2 2,growx");
		
		btnSaveConfig = new JButton("Save Launch Options");
		btnSaveConfig.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnSaveConfig, "cell 3 2,growx");
		
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
			}
		});
		
		btnSaveConfig.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String maxMemory = String.valueOf((Integer)spinnerMaxMemoryMB.getValue());
				String minMemory = String.valueOf((Integer)spinnerMinMemoryMB.getValue());
				String args = "-Xmx"+maxMemory+"m -Xms"+minMemory+"m";
				
				// update launcher vars and write to config INI file
				String SECTION_ARGS = ScrYpTLauncher.INI_JVM_OPT;
				ScrYpTLauncher.JVM_MEMORY_OPTIONS = args;
				ScrYpTLauncher.writeValueToIniConfiguration(SECTION_ARGS, "memoryArgs", args);
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

				checkEqualMinMaxMemory(max, min);
				
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
				
				checkEqualMinMaxMemory(max, min);
				
				if(max < min) {
					spinnerMaxMemoryMB.setValue(min);
				}
			}
		});
	}
	
	private void checkEqualMinMaxMemory(int max, int min) {
		if(max == min) {
			lblMaxWarninig.setVisible(true);
			lblMinWarninig.setVisible(true);
			lblMaxWarninig.setToolTipText("It's recommended to use bigger value for max memory than minimum, equal values cause performance decrease! ");
			lblMinWarninig.setToolTipText("It's recommended to use lower value for minimum memory than max, equal values cause performance decrease! ");
		}else {
			lblMaxWarninig.setVisible(false);
			lblMinWarninig.setVisible(false);
			lblMaxWarninig.setToolTipText(null);
			lblMinWarninig.setToolTipText(null);
		}
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
	
	private void setGraphics() {
		try {
			thisPanel.setIconImage(ImageIO.read(new File("Resources_Scrypt/icon.jpg")));
			logger.debug("icon image setted to main window");
		} catch (IOException e1) {
			logger.error("Exception happened!", e1);
		}
		
		try {
			warningImage = ImageIO.read(new File("Resources_Scrypt/Icons/Warning.png"));
			lblMaxWarninig.setIcon(new ImageIcon(warningImage));
			lblMaxWarninig.setVisible(false);
			lblMinWarninig.setIcon(new ImageIcon(warningImage));
			lblMinWarninig.setVisible(false);
		}catch(IOException e) {
			logger.error("Error initializing warning icons", e);
		}
	}
}
