package gui.panels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JDialog;

import org.apache.log4j.Logger;

import gui.GuiUtils;
import launcherutils.IniFileManager;
import launcherutils.om.DependencyModel;
import mainlauncher.ScrYpTLauncher;

import java.awt.Button;
import java.awt.Color;
import net.miginfocom.swing.MigLayout;
import om.OSinfo;
import resources.IconsPathConfigurator;
import utils.SysUtils;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;

public class MissingDependenciesPanel extends JDialog{
	private static final long serialVersionUID = 8830965172863577977L;

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(MissingDependenciesPanel.class); 
	
	private StartPanel parentStarterFrame;
	private MissingDependenciesPanel thisPanel;
	private ArrayList<DependencyModel> dependeciesMissing;
	private JTable table;
	private JLabel lblTitleHeaderMessage;
	private static final String[] columnNames = {"Dependency name", "Critical dependency (T/F)", "Dependency actions"};
	private JButton btnIgnoreWarnings;
	private JButton btnRefreshNCheck;
	private JButton btnLocateManually;
	private JButton btnInstallDependency;
	private JButton btnDownloadDependency;
	public static boolean active = false;
	
	// constructor
	public MissingDependenciesPanel(String title, StartPanel parentStarter) {
		
		active = true;
		parentStarterFrame = parentStarter;
		thisPanel = this;
		thisPanel.setTitle(title);
		
		dependeciesMissing = ScrYpTLauncher.dependeciesMissing;
		
		this.setAlwaysOnTop(true);
		getContentPane().setBackground(Color.DARK_GRAY);
		getContentPane().setLayout(new MigLayout("", "[50%,grow][50%,grow][50%,grow]", "[50px:80px][grow][40px:n][40px:n]"));
		
		lblTitleHeaderMessage = new JLabel("If some dependencies are missing and they're critical, you have to solve them before to start ScrYpt!");
		lblTitleHeaderMessage.setForeground(new Color(211, 211, 211));
		lblTitleHeaderMessage.setBackground(Color.BLACK);
		lblTitleHeaderMessage.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		lblTitleHeaderMessage.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitleHeaderMessage.setHorizontalTextPosition(SwingConstants.CENTER);
		lblTitleHeaderMessage.setOpaque(true);
		lblTitleHeaderMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		getContentPane().add(lblTitleHeaderMessage, "cell 0 0 3 1,grow");
		
		table = new JTable();
		JScrollPane tableContainer = new JScrollPane(table);
		table.setBorder(new LineBorder(new Color(0, 0, 0)));
		table.setCellSelectionEnabled(false);
		table.setFillsViewportHeight(true);
		table.setShowHorizontalLines(true);
		table.setShowVerticalLines(true);
		table.setRowHeight(30);
		getContentPane().add(tableContainer, "cell 0 1 3 1,grow");
		
		btnRefreshNCheck = new JButton("Refresh and check again for dependencies");
		btnRefreshNCheck.setFont(new Font("SansSerif", Font.PLAIN, 13));
		btnRefreshNCheck.setBackground(Color.GRAY);
		btnRefreshNCheck.setCursor(GuiUtils.CURSOR_HAND);
		getContentPane().add(btnRefreshNCheck, "cell 0 2,grow");
		
		btnInstallDependency = new JButton("Install Selected");
		btnInstallDependency.setFont(new Font("SansSerif", Font.BOLD, 14));
		btnInstallDependency.setCursor(GuiUtils.CURSOR_HAND);
		btnInstallDependency.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnInstallDependency, "cell 1 2,grow");
		
		btnDownloadDependency = new JButton("Download Selected");
		btnDownloadDependency.setFont(new Font("SansSerif", Font.BOLD, 14));
		btnDownloadDependency.setCursor(GuiUtils.CURSOR_HAND);
		btnDownloadDependency.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnDownloadDependency, "cell 2 2,grow");
		
		btnIgnoreWarnings = new JButton("Ignore warnings (some functions will be disabled)");
		btnIgnoreWarnings.setFont(new Font("SansSerif", Font.PLAIN, 13));
		btnIgnoreWarnings.setBackground(new Color(255, 250, 205));
		btnIgnoreWarnings.setCursor(GuiUtils.CURSOR_HAND);
		getContentPane().add(btnIgnoreWarnings, "cell 0 3,grow");
		
		btnLocateManually = new JButton("Add installation path manually");
		btnLocateManually.setFont(new Font("SansSerif", Font.BOLD, 14));
		btnLocateManually.setBackground(new Color(169, 169, 169));
		btnLocateManually.setCursor(GuiUtils.CURSOR_HAND);
		getContentPane().add(btnLocateManually, "cell 1 3 2 1,grow");
		
		setGraphics();
		
		checkCriticalMissingDependencies();
		
		updateDependencyIssueTable();
		
		addHandlers();
		
		thisPanel.setBounds(120, 120, 700, 450 );
		
		logger.info(this.getClass().getName() + " Started");
	}
	
	private void addHandlers() {
		
		// default close operation(same as reset) 
		thisPanel.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
		    	if(!ScrYpTLauncher.criticalDependenciesOK) {
		    		askIfSureToExit();
		    	}
		    	askIfSureToCloseIgnoring();
	        	logger.debug("Closing Frame: " + thisPanel.getClass().getName());	        	

		    }
		});
		
		btnIgnoreWarnings.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				askIfSureToCloseIgnoring();
			}
		});
		
		btnRefreshNCheck.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				parentStarterFrame.LAUNCHER.analyzeBeforeLauncher();
				checkCriticalMissingDependencies();
				dependeciesMissing = ScrYpTLauncher.dependeciesMissing;
				if(dependeciesMissing.size() == 0) {
					JOptionPane.showMessageDialog(thisPanel, "All dependencies are OK, you're ready to use all ScrYpT functions :)", "No missing dependencies found!", JOptionPane.INFORMATION_MESSAGE);
					parentStarterFrame.setEnabled(true);
					active = false;
					dispose();
				}else {
					updateDependencyIssueTable();
				}
			}
		});
		
		btnLocateManually.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(table.getSelectedRows().length > 0) {
					
					locateDependencies(table.getSelectedRows());
					
				}
			}
		});
		
		btnInstallDependency.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(table.getSelectedRows().length > 0) {
					
					installDependencies(table.getSelectedRows());
					
					parentStarterFrame.LAUNCHER.analyzeBeforeLauncher();
					checkCriticalMissingDependencies();
					updateDependencyIssueTable();
				}
			}
		});
		
		btnDownloadDependency.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(table.getSelectedRows().length > 0) {
					
					downloadDependencies(table.getSelectedRows());
					
				}
			}
		});
		
		// listener for row selected: if there is no installer disable button
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(table.getSelectedRow() != -1){
					try{
						DependencyModel selectedDependency = dependeciesMissing.get(table.getSelectedRow());
						File localInstaller = new File(selectedDependency.relativePathToInstaller.getCurrentOSPath());
						// check local installer
						if (!localInstaller.exists()) {
							btnInstallDependency.setEnabled(false);
							btnInstallDependency
									.setToolTipText("There is no installer in current version of ScrYpt for "
											+ selectedDependency.dependencyName);
						} else {
							btnInstallDependency.setEnabled(true);
							btnInstallDependency.setToolTipText(
									"Click to install using local installer " + selectedDependency.dependencyName);
						}
						// check remote URL for download
						if (selectedDependency.URL == null || selectedDependency.URL.equals("")) {
							btnDownloadDependency.setEnabled(false);
							btnDownloadDependency
									.setToolTipText("There is no URL specified in current version of ScrYpt for "
											+ selectedDependency.dependencyName);
						} else {
							btnDownloadDependency.setEnabled(true);
							btnDownloadDependency.setToolTipText(
									"Click to open download page for " + selectedDependency.dependencyName);
						} 
					}catch(ArrayIndexOutOfBoundsException ex) {
						logger.warn("Table selection listener triggered, but no rows selected ", ex);
					} 
				} // END IF sel != -1				
			}
		}); // END table listener
		
	}
	
	private void setGraphics(){
		thisPanel.setIconImage(new ImageIcon(IconsPathConfigurator.ICON_SCREEPT).getImage());
		setLocationRelativeTo(parentStarterFrame);
		
		logger.debug("icon image setted to main window");
		UIManager.put("ToolTip.background", new Color(255, 250, 205));
	}
	
	// methods used to check dependency status and install //
	
	private void checkCriticalMissingDependencies() {
		if(!ScrYpTLauncher.criticalDependenciesOK) {
			// if critical problem then disable window closing possibility
			parentStarterFrame.setEnabled(false);
			btnIgnoreWarnings.setEnabled(false);
		}
		if(ScrYpTLauncher.dependeciesMissing.size() != 0) {
			parentStarterFrame.setEnabled(false);
		}else{
			parentStarterFrame.setEnabled(true);
			active = false;
			dispose();
		}
	}
	
	private DefaultTableModel getTableModel() {
		DefaultTableModel model = new DefaultTableModel(null, columnNames) {
			@Override
		    public boolean isCellEditable(int row, int column) {
		       //all cells false
		       return false;
		    }
		};
		
		table.setGridColor(Color.DARK_GRAY);
		table.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		table.setBackground(Color.WHITE);
		table.getTableHeader().setFont(new Font("Segoe UI Black", Font.BOLD, 14));
		table.getTableHeader().setVisible(true);
		table.getTableHeader().setResizingAllowed(true);
		table.setRowSelectionAllowed(true);
		table.setSelectionBackground(new Color(245, 222, 179));
		table.setSelectionForeground(Color.BLACK);
		table.setOpaque(true);
		return model;
	}
	
	private void updateDependencyIssueTable() {
		
		if(thisPanel.dependeciesMissing.size() == 0) {
			parentStarterFrame.setEnabled(true);
			active = false;
			dispose();
			return;
		}
		// first empty the table
		table.removeAll();
		table.setModel(getTableModel());
		
		for(DependencyModel currentDependency : dependeciesMissing) {
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			model.addRow(new Object[]{currentDependency.dependencyName, currentDependency.isCritical, currentDependency.solution});
		}
		table.revalidate();
		table.repaint();
		
	}
	
	private void askIfSureToCloseIgnoring() {
		String msg = "Are you sure to continue anyway? Some ScrYpT functions will be disabled by default.";
		JCheckBox checkbox = new JCheckBox("Do not show this section next Time");
		checkbox.setOpaque(false);
		Object[] params = {msg, checkbox};
		if (dependeciesMissing.size() != 0) {
			int response = JOptionPane.showConfirmDialog(parentStarterFrame, params, "Confirm before continue",
					JOptionPane.WARNING_MESSAGE);
			if (response == JOptionPane.OK_OPTION) {
				if (checkbox.isSelected()) {
					IniFileManager iniFileManager = parentStarterFrame.LAUNCHER.iniFileManager;
					iniFileManager.writeValueToIniConfiguration(IniFileManager.INI_APP_OPT, "autoShowInstallOpt", "false");
				}
				ScrYpTLauncher.ignoreWarnings = true;
				parentStarterFrame.setEnabled(true);
				active = false;
				dispose();
			} else {
				thisPanel.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			}
		}else {
			parentStarterFrame.setEnabled(true);
			active = false;
			dispose();
		}
	}
	
	private void askIfSureToExit() {
		String msg = "Are you sure to close? Scrypt cannot be launched without required dependencies, launcher will close if continue";
		int response = JOptionPane.showConfirmDialog(parentStarterFrame, msg, "Confirm before continue", JOptionPane.WARNING_MESSAGE);
		if (response == JOptionPane.OK_OPTION) {
			System.exit(1);
    		logger.warn("Critical dependencies missing: Launcher closed by user");
		}else {
			thisPanel.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		}
	}
	
	private void locateDependencies(int[] selectedDependenciesIndex) {
		for(DependencyModel currentDependency : dependeciesMissing) {
			java.util.Date now = new java.util.Date();
			ActionListener locator = currentDependency.locateManually;
			ActionEvent event = new ActionEvent(new Button(), ActionEvent.ACTION_PERFORMED, "Anything", now.getTime(), 0);
			if(locator != null) {
				locator.actionPerformed(event);
				parentStarterFrame.LAUNCHER.analyzeBeforeLauncher();
				checkCriticalMissingDependencies();
				updateDependencyIssueTable();
			}
		}
	}
	
	private void installDependencies(int[] selectedDependenciesIndex) {
		for(int currentIndex : selectedDependenciesIndex) {
			DependencyModel currentDependency = dependeciesMissing.get(currentIndex);
			File localInstaller = new File(currentDependency.relativePathToInstaller.getCurrentOSPath());	
			if (localInstaller.exists()) {
				launchInstaller(currentDependency.relativePathToInstaller.getCurrentOSPath());
			}else {
				JOptionPane.showMessageDialog(thisPanel, ("There is no installer for "+currentDependency.dependencyName), "Installer missing", JOptionPane.ERROR_MESSAGE);
			}
		}
		
	}
	
	private void downloadDependencies(int[] selectedDependenciesIndex) {
		for(int currentIndex : selectedDependenciesIndex) {
			DependencyModel currentDependency = dependeciesMissing.get(currentIndex);
			if(currentDependency.URL != null && !currentDependency.URL.equals("")){
				SysUtils.openUrlInBrowser(currentDependency.URL);
			}else {
				JOptionPane.showMessageDialog(thisPanel, ("Error opening URL for "+currentDependency.dependencyName), "URL missing", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	
	// launch a batch/bash installer according to OS used
	private void launchInstaller(String relativePathToLauncher) {
		OSinfo OS = SysUtils.retrieveOS();
		
		if(OS.isWindows()) {
			try {
				List<String> cmds = Arrays.asList("cmd.exe", "/C", "start",relativePathToLauncher);
				Process windowsProcess = new ProcessBuilder(cmds).start();
				InputStream inStream = windowsProcess.getInputStream();
				logger.info(SysUtils.readProcessOutput(inStream));
				inStream.close();
			} catch (IOException e) {
				logger.error("Error launching Windows installer " + relativePathToLauncher, e);
			}
		}else if(OS.isLinux()) {
			try {
				Process linuxProcess =  new ProcessBuilder(relativePathToLauncher).start();
				InputStream inStream = linuxProcess.getInputStream();
				logger.info(SysUtils.readProcessOutput(inStream));
				inStream.close();
				
			} catch (IOException e) {
				logger.error("Error launching Linux installer " + relativePathToLauncher, e);
			}
		}else if(OS.isMac()) {
			try {
				Process macProcess = new ProcessBuilder(relativePathToLauncher).start();
				InputStream inStream = macProcess.getInputStream();
				logger.info(SysUtils.readProcessOutput(inStream));
				inStream.close();
			} catch (IOException e) {
				logger.error("Error launching MacOS installer " + relativePathToLauncher, e);
			}
		}
	}
}
