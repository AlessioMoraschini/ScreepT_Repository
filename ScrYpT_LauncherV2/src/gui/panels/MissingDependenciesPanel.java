package gui.panels;

import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import gui.ScrYpTLauncher;
import launcherutils.SysUtils;
import launcherutils.om.DependencyModel;
import launcherutils.om.OSinfo;

import java.awt.Button;
import java.awt.Color;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;

import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;

public class MissingDependenciesPanel extends JDialog{
	
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
	
	// constructor
	public MissingDependenciesPanel(String title, StartPanel parentStarter) {
		
		parentStarterFrame = parentStarter;
		thisPanel = this;
		thisPanel.setTitle(title);
		
		dependeciesMissing = ScrYpTLauncher.dependeciesMissing;
		
		getContentPane().setBackground(Color.DARK_GRAY);
		getContentPane().setLayout(new MigLayout("", "[50%,grow][50%,grow]", "[50px:80px][grow][40px:n][40px:n]"));
		
		lblTitleHeaderMessage = new JLabel("Some dependencies are missing, if they're critical you have to solve them before to start ScrYpt!");
		lblTitleHeaderMessage.setBackground(new Color(211, 211, 211));
		lblTitleHeaderMessage.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		lblTitleHeaderMessage.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitleHeaderMessage.setHorizontalTextPosition(SwingConstants.CENTER);
		lblTitleHeaderMessage.setOpaque(true);
		lblTitleHeaderMessage.setFont(new Font("Segoe UI", Font.BOLD, 14));
		getContentPane().add(lblTitleHeaderMessage, "cell 0 0 2 1,grow");
		
		table = new JTable();
		JScrollPane tableContainer = new JScrollPane(table);
		table.setBorder(new LineBorder(new Color(0, 0, 0)));
		table.setCellSelectionEnabled(false);
		table.setFillsViewportHeight(true);
		table.setShowHorizontalLines(true);
		table.setShowVerticalLines(true);
		table.setRowHeight(30);
		getContentPane().add(tableContainer, "cell 0 1 2 1,grow");
		
		btnLocateManually = new JButton("Add installation path manually");
		btnLocateManually.setFont(new Font("SansSerif", Font.BOLD, 14));
		btnLocateManually.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnLocateManually, "cell 0 2,grow");
		
		btnInstallDependency = new JButton("Install Dependency / go to download page");
		btnInstallDependency.setFont(new Font("SansSerif", Font.BOLD, 14));
		btnInstallDependency.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnInstallDependency, "cell 1 2,grow");
		
		btnIgnoreWarnings = new JButton("Ignore warnings (some functions will be disabled)");
		btnIgnoreWarnings.setFont(new Font("SansSerif", Font.PLAIN, 13));
		btnIgnoreWarnings.setBackground(new Color(176, 196, 222));
		getContentPane().add(btnIgnoreWarnings, "cell 0 3,grow");
		
		btnRefreshNCheck = new JButton("Refresh and check again for dependencies");
		btnRefreshNCheck.setFont(new Font("SansSerif", Font.PLAIN, 13));
		btnRefreshNCheck.setBackground(new Color(176, 196, 222));
		getContentPane().add(btnRefreshNCheck, "cell 1 3,grow");
		
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
		    	askIfSureToClose();
	        	logger.debug("Closing Frame: " + thisPanel.getClass().getName());	        	

		    }
		});
		
		btnIgnoreWarnings.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				askIfSureToClose();
			}
		});
		
		btnRefreshNCheck.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				parentStarterFrame.LAUNCHER.analyzeBeforeLauncher();
				checkCriticalMissingDependencies();
				dependeciesMissing = ScrYpTLauncher.dependeciesMissing;
				if(dependeciesMissing.size() == 0) {
					parentStarterFrame.setEnabled(true);
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
					
					parentStarterFrame.LAUNCHER.analyzeBeforeLauncher();
					checkCriticalMissingDependencies();
					updateDependencyIssueTable();
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
	}
	
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
			dispose();
		}
	}
	
	private DefaultTableModel getTableModel() {
		// TODO define custom table class
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
			dispose();
			return;
		}
		// first empty the table
		table.removeAll();
		table.setModel(getTableModel());
		
		for(DependencyModel currentDependency : dependeciesMissing) {
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			model.addRow(new Object[]{currentDependency.dependencyName, currentDependency.isCritical, ""});
		}
		table.revalidate();
		table.repaint();
		
	}
	
	private void askIfSureToClose() {
		String msg = "Are you sure to continue anyway? Some ScrYpT functions will be disabled by default.";
		JCheckBox checkbox = new JCheckBox("Do not show this section next Time");
		Object[] params = {msg, checkbox};
		int response = JOptionPane.showConfirmDialog(parentStarterFrame, params, "Confirm before continue", JOptionPane.WARNING_MESSAGE);
		if (response == JOptionPane.OK_OPTION) {
			if(checkbox.isSelected()) {
				ScrYpTLauncher.writeValueToIniConfiguration(ScrYpTLauncher.INI_APP_OPT, "autoShowInstallOpt", "false");
			}
			parentStarterFrame.setEnabled(true);
			dispose();
		}else {
			thisPanel.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
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
			}
		}
	}
	
	private void installDependencies(int[] selectedDependenciesIndex) {
		for (DependencyModel currentDependency : dependeciesMissing) {
			File localInstaller = new File(currentDependency.relativePathToInstaller.getCurrentOSPath());	
			if (localInstaller.exists()) {
				launchInstaller(currentDependency.relativePathToInstaller.getCurrentOSPath());
			} else if(currentDependency.URL != null && !currentDependency.URL.equals("")){
				SysUtils.openUrlInBrowser(currentDependency.URL);
			} 
		}
	}
	
	// launch a batch/bash installer according to OS used
	private void launchInstaller(String relativePathToLauncher) {
		OSinfo OS = SysUtils.retrieveOS();
		
		if(OS.isWindows()) {
			try {
				Process windowsProcess = new ProcessBuilder(relativePathToLauncher).start();
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
