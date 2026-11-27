/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package screept.launcher.gui.panels;

import various.common.light.i18n.I18n;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import mainlauncher.ScreepTLauncher;

import java.awt.Button;
import java.awt.Color;
import screept.launcher.gui.utils.LauncherTheme;

import net.miginfocom.swing.MigLayout;
import os.commons.om.OSinfo;
import os.commons.utils.SysUtils;
import resources.IconsPathConfigurator;
import screept.launcher.utils.IniFileManager;
import screept.launcher.utils.om.DependencyModel;
import various.common.light.gui.GuiUtils;
import various.common.light.gui.dialogs.msg.JOptionHelper;

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
import javax.swing.SwingUtilities;
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

public class DependenciesManagerPanel extends JFrame{
	private static final long serialVersionUID = 8830965172863577977L;

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(DependenciesManagerPanel.class); 
	
	private static final String[] columnNames = {"Dependency name", "Critical dependency (T/F)", "Dependency actions"};

	private JOptionHelper dialogHelper;
	
	private LauncherMainPanel parentStarterFrame;
	private DependenciesManagerPanel thisPanel;
	private ArrayList<DependencyModel> dependeciesMissing;
	private JTable table;
	private JLabel lblTitleHeaderMessage;
	private JButton btnIgnoreWarnings;
	private JButton btnRefreshNCheck;
	private JButton btnLocateManually;
	private JButton btnInstallDependency;
	private JButton btnDownloadDependency;
	
	
	public static boolean active = false;
	
	// constructor
	public DependenciesManagerPanel(String title, LauncherMainPanel parentStarter) {
		
		active = true;
		parentStarterFrame = parentStarter;
		thisPanel = this;
		thisPanel.setTitle(title);
		
		dialogHelper = new JOptionHelper(this);
		
		dependeciesMissing = ScreepTLauncher.missingDependenciesList;
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setAlwaysOnTop(true);
		getContentPane().setBackground(Color.DARK_GRAY);
		getContentPane().setLayout(new MigLayout("", "[50%,grow][50%,grow][50%,grow]", "[50px:80px][grow][40px:n][40px:n]"));
		
		lblTitleHeaderMessage = new JLabel(I18n.t("launcher.if_some_dependencies_are_missing_and"));
		lblTitleHeaderMessage.setForeground(new Color(211, 211, 211));
		lblTitleHeaderMessage.setBackground(Color.BLACK);
		lblTitleHeaderMessage.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		lblTitleHeaderMessage.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitleHeaderMessage.setHorizontalTextPosition(SwingConstants.CENTER);
		lblTitleHeaderMessage.setOpaque(true);
		lblTitleHeaderMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		getContentPane().add(lblTitleHeaderMessage, "cell 0 0 3 1,grow");
		
		table = new JTable() {
			private static final long serialVersionUID = -906822605094698109L;

			//  Returning the Class of each column will allow different
            //  renderers to be used based on Class
            public Class<?> getColumnClass(int column)
            {
                return getValueAt(0, column).getClass();
            }
		};
		JScrollPane tableContainer = new JScrollPane(table);
		table.setBorder(new LineBorder(new Color(0, 0, 0)));
		table.setCellSelectionEnabled(false);
		table.setFillsViewportHeight(true);
		table.setShowHorizontalLines(true);
		table.setShowVerticalLines(true);
		table.setRowHeight(30);
		getContentPane().add(tableContainer, "cell 0 1 3 1,grow");
		
		btnRefreshNCheck = new JButton(I18n.t("launcher.refresh_and_check_again_for_dependencies"));
		btnRefreshNCheck.setFont(new Font("SansSerif", Font.PLAIN, 13));
		btnRefreshNCheck.setBackground(Color.GRAY);
		btnRefreshNCheck.setCursor(GuiUtils.CURSOR_HAND);
		getContentPane().add(btnRefreshNCheck, "cell 0 2,grow");
		
		btnInstallDependency = new JButton(I18n.t("launcher.install_selected"));
		btnInstallDependency.setFont(new Font("SansSerif", Font.BOLD, 14));
		btnInstallDependency.setCursor(GuiUtils.CURSOR_HAND);
		btnInstallDependency.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnInstallDependency, "cell 1 2,grow");
		
		btnDownloadDependency = new JButton(I18n.t("launcher.download_selected"));
		btnDownloadDependency.setFont(new Font("SansSerif", Font.BOLD, 14));
		btnDownloadDependency.setCursor(GuiUtils.CURSOR_HAND);
		btnDownloadDependency.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnDownloadDependency, "cell 2 2,grow");
		
		btnIgnoreWarnings = new JButton(I18n.t("launcher.ignore_warnings_some_functions_will_be"));
		btnIgnoreWarnings.setFont(new Font("SansSerif", Font.PLAIN, 13));
		btnIgnoreWarnings.setBackground(new Color(255, 250, 205));
		btnIgnoreWarnings.setCursor(GuiUtils.CURSOR_HAND);
		getContentPane().add(btnIgnoreWarnings, "cell 0 3,grow");
		
		btnLocateManually = new JButton(I18n.t("launcher.add_installation_path_manually"));
		btnLocateManually.setFont(new Font("SansSerif", Font.BOLD, 14));
		btnLocateManually.setBackground(new Color(169, 169, 169));
		btnLocateManually.setCursor(GuiUtils.CURSOR_HAND);
		getContentPane().add(btnLocateManually, "cell 1 3 2 1,grow");
		
		setGraphics();
		
		checkCriticalMissingDependencies();
		
		updateDependencyIssueTable();
		
		addHandlers();
		
		GuiUtils.centerComponent(this,700, 450);
		
		this.setVisible(true);
		
		logger.info(this.getClass().getName() + " Started");
	}
	
	private void addHandlers() {
		
		// default close operation(same as reset) 
		thisPanel.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
		    	if(!ScreepTLauncher.criticalDependenciesOK) {
		    		askIfSureToExit();
		    	} else if(anyMissingCritical(dependeciesMissing)) {
		    		askIfSureToCloseIgnoring();
		    	} else {
		    		active = false;
		    		logger.debug("Closing Frame: " + thisPanel.getClass().getName());	        	
		    		dispose();
		    	}
		    	
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
				dependeciesMissing = ScreepTLauncher.missingDependenciesList;
				if(dependeciesMissing.size() == 0) {
					dialogHelper.info(I18n.t("launcher.all_dependencies_are_ok_you_re"), I18n.t("launcher.no_missing_dependencies_found"));
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
									.setToolTipText(I18n.t("launcher.there_is_no_installer_in_current")
											+ selectedDependency.dependencyName);
						} else {
							btnInstallDependency.setEnabled(true);
							btnInstallDependency.setToolTipText(
									I18n.t("launcher.click_to_install_using_local_installer") + selectedDependency.dependencyName);
						}
						// check remote URL for download
						if (selectedDependency.URL == null || selectedDependency.URL.equals("")) {
							btnDownloadDependency.setEnabled(false);
							btnDownloadDependency
									.setToolTipText(I18n.t("launcher.there_is_no_url_specified_in")
											+ selectedDependency.dependencyName);
						} else {
							btnDownloadDependency.setEnabled(true);
							btnDownloadDependency.setToolTipText(
									I18n.t("launcher.click_to_open_download_page_for") + selectedDependency.dependencyName);
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
		// tooltips follow the look and feel (the light yellow one is unreadable on the dark theme)
	}
	
	// methods used to check dependency status and install //
	
	private void checkCriticalMissingDependencies() {
		if(!ScreepTLauncher.criticalDependenciesOK) {
			// if critical problem then disable window closing possibility
			parentStarterFrame.setEnabled(false);
			btnIgnoreWarnings.setEnabled(false);
		}
		if(ScreepTLauncher.missingDependenciesList.size() != 0 && anyMissingCritical(ScreepTLauncher.missingDependenciesList)) {
			parentStarterFrame.setEnabled(false);
		}else{
			parentStarterFrame.setEnabled(true);
		}
	}
	
	public static boolean anyMissingCritical(ArrayList<DependencyModel> dependenciesMissing) {
		if(dependenciesMissing == null || dependenciesMissing.isEmpty()) {
			return false;
		}
		
		for(DependencyModel current : dependenciesMissing) {
			if(current.isCritical) {
				return true;
			}
		}
		
		return false;
	}
	
	private DefaultTableModel getTableModel() {
		DefaultTableModel model = new DefaultTableModel(null, columnNames) {
			private static final long serialVersionUID = -135868054973366582L;

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
		}
		
		// first empty the table
		table.removeAll();
		table.setModel(getTableModel());
		
		for(DependencyModel currentDependency : dependeciesMissing) {
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			model.addRow(new Object[]{currentDependency.dependencyName, String.valueOf(currentDependency.isCritical), currentDependency.solution});
		}
		table.revalidate();
		table.repaint();
		
	}
	
	private void askIfSureToCloseIgnoring() {
		String msg = I18n.t("launcher.are_you_sure_to_continue_anyway");
		JCheckBox checkbox = new JCheckBox(I18n.t("launcher.do_not_show_this_section_next"));
		checkbox.setOpaque(false);
		Object[] params = {msg, checkbox};
		if (dependeciesMissing.size() != 0) {
			int response = JOptionPane.showConfirmDialog(parentStarterFrame, params, I18n.t("launcher.confirm_before_continue"), JOptionPane.WARNING_MESSAGE);
			if (response == JOptionPane.OK_OPTION) {
				if (checkbox.isSelected()) {
					IniFileManager iniFileManager = ScreepTLauncher.iniFileManager;
					iniFileManager.writeValueToIniConfiguration(IniFileManager.INI_APP_OPT, "autoShowInstallOpt", "false");
				}
				ScreepTLauncher.ignoreWarnings = true;
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
		String msg = I18n.t("launcher.are_you_sure_to_close_screept");
		
		if (dialogHelper.yesOrNo(msg, I18n.t("launcher.confirm_before_continue"))) {
			System.exit(1);
    		logger.warn("Critical dependencies missing: Launcher closed by user");
		}else {
			thisPanel.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		}
	}
	
	private void locateDependencies(int[] selectedDependenciesIndex) {
		for(DependencyModel currentDependency : dependeciesMissing) {
			java.util.Date now = new java.util.Date();
			final ActionListener locator = currentDependency.locateManually;
			final ActionEvent event = new ActionEvent(new Button(), ActionEvent.ACTION_PERFORMED, "Anything", now.getTime(), 0);
			if(locator != null) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						locator.actionPerformed(event);
						parentStarterFrame.LAUNCHER.analyzeBeforeLauncher();
						checkCriticalMissingDependencies();
						updateDependencyIssueTable();
					}
				});
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
				dialogHelper.error(I18n.t("launcher.there_is_no_installer_for")+currentDependency.dependencyName, I18n.t("launcher.installer_missing"));
			}
		}
		
	}
	
	private void downloadDependencies(int[] selectedDependenciesIndex) {
		for(int currentIndex : selectedDependenciesIndex) {
			DependencyModel currentDependency = dependeciesMissing.get(currentIndex);
			if(currentDependency.URL != null && !currentDependency.URL.equals("")){
				SysUtils.openUrlInBrowser(currentDependency.URL);
			}else {
				dialogHelper.error(I18n.t("launcher.error_opening_url_for")+currentDependency.dependencyName, I18n.t("launcher.url_missing"));
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
	private boolean themed = false;

	@Override
	public void setVisible(boolean visible) {
		if (visible && !themed) {
			themed = true;
			LauncherTheme.polish(this);
			LauncherTheme.primary(btnInstallDependency);
			LauncherTheme.primary(btnDownloadDependency);
		}
		super.setVisible(visible);
	}
}
