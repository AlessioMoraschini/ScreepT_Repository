package gUI.frames;

import frameutils.frame.arch.ParentFrame;
import frameutils.utils.GuiUtilsExtended;
import gUI.panels.CrypterPanel;
import gUI.panels.TextEditor;
import maingUI.MainGUI;
import om.TextEditorOption;
import net.miginfocom.swing.MigLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.border.LineBorder;

import files.om.FileNamed;

import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JSeparator;

public class ImportExportWorkspcCryptFrame extends ParentFrame {
	private static final long serialVersionUID = -8119237171970529701L;
	
	public static ImportExportWorkspcCryptFrame singletonInstance = null;
	
	protected TextEditorOption textOptions;
	protected TextEditor textEditor;
	
	protected JButton btnImport;
	protected JButton btnExport;
	
	protected JLabel lblSelectedForExport;
	protected JComboBox<FileNamed> comboBoxExport;
	private JButton btnSelectImport;
	private JLabel labelToImportDyn;
	
	private File selectedForImport;
	private File selectedForExport;
	private JSeparator separator;

	/**
	 * Text editor option and texteditor are nullable
	 * @param txtOptions
	 * @param textEditor
	 */
	public ImportExportWorkspcCryptFrame(TextEditorOption txtOptions, TextEditor textEditor) {
		super();
		
		dialogHelper.setAdvicesEnabled(false);
		
		singletonInstance = this;
		
		getContentPane().setBackground(Color.DARK_GRAY);
		
		setTitle("Import/Export crypted Workspace");
		
		GuiUtilsExtended.centerComponent(this, WIDTH, HEIGHT);
		
		logger.info("Opening frame: " + getClass().getName());
		
		if(textEditor == null) {
			MainGUI.mainGuiUtils.startTextEditor(true);
			this.textEditor = MainGUI.initTxtWriter;
		} else {
			this.textEditor = textEditor;
		}
		
		this.textOptions = (txtOptions != null) ? txtOptions : MainGUI.currentConfiguration.getTextEditorOpt();
		
		getContentPane().setLayout(new MigLayout("", "[168.00][grow][][100px][:100px:140px][]", "[:40px:40px,grow][grow][][grow]"));
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(Color.GRAY));
		panel.setBackground(Color.BLACK);
		getContentPane().add(panel, "cell 0 0 6 1,grow");
		panel.setLayout(new MigLayout("", "[579px,grow]", "[40px]"));
		
		JLabel lblImportexportCryptedWorkspace = new JLabel("Import/Export crypted workspace");
		lblImportexportCryptedWorkspace.setForeground(Color.WHITE);
		lblImportexportCryptedWorkspace.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
		panel.add(lblImportexportCryptedWorkspace, "cell 0 0,alignx center,growy");
		
		JLabel lblMode = new JLabel("Selected for import:");
		lblMode.setForeground(new Color(210, 180, 140));
		lblMode.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		getContentPane().add(lblMode, "cell 0 1,alignx center,aligny center");
		
		labelToImportDyn = new JLabel("- No workspace selected to import -");
		labelToImportDyn.setForeground(Color.LIGHT_GRAY);
		getContentPane().add(labelToImportDyn, "cell 1 1 2 1,alignx center");
		
		btnSelectImport = new JButton("Select new to import");
		btnSelectImport.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		btnSelectImport.setBackground(new Color(210, 180, 140));
		btnSelectImport.setCursor(GuiUtilsExtended.CURSOR_HAND);
		getContentPane().add(btnSelectImport, "cell 3 1,growx,aligny center");
		
		btnImport = new JButton("Import Selected");
		btnImport.setForeground(Color.BLACK);
		btnImport.setFont(new Font("Segoe UI", Font.BOLD, 14));
		btnImport.setBackground(new Color(205, 133, 63));
		btnImport.setCursor(GuiUtilsExtended.CURSOR_HAND);
		getContentPane().add(btnImport, "cell 4 1 2 1,growx,aligny center");
		
		separator = new JSeparator();
		separator.setForeground(Color.GRAY);
		separator.setBackground(Color.GRAY);
		getContentPane().add(separator, "cell 0 2 6 1,growx");
		
		lblSelectedForExport = new JLabel("Selected for export:");
		lblSelectedForExport.setForeground(new Color(176, 196, 222));
		lblSelectedForExport.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		getContentPane().add(lblSelectedForExport, "cell 0 3,alignx center");
		
		comboBoxExport = new JComboBox<FileNamed>(txtOptions.lastSelectedWorkspaces.getList());
		comboBoxExport.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		comboBoxExport.setCursor(GuiUtilsExtended.CURSOR_HAND);
//		comboBoxExport = new JComboBox<FileNamed>();
		getContentPane().add(comboBoxExport, "cell 1 3 3 1,growx,aligny center");
		
		btnExport = new JButton("Export Selected");
		btnExport.setForeground(Color.BLACK);
		btnExport.setFont(new Font("Segoe UI", Font.BOLD, 14));
		btnExport.setBackground(new Color(119, 136, 153));
		btnExport.setCursor(GuiUtilsExtended.CURSOR_HAND);
		getContentPane().add(btnExport, "cell 4 3 2 1,growx,aligny center");
		
		addListeners();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateToExport();
				dialogHelper.setAdvicesEnabled(true);
				setVisible(true);
				setAlwaysOnTop(true);
			}
		});
		
		logger.info("Frame loaded: " + getClass().getName());
	}
	
	public void syncWithCurrentWOrkspace() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String lastWorkspaceName = new File(textOptions.defaultWorkspacePath).getName();
				
				int match = GuiUtilsExtended.getFirstIndexMatchFromFileNamedCombo(comboBoxExport, lastWorkspaceName);
				comboBoxExport.setSelectedIndex(match);
				comboBoxExport.repaint();
			}
		});
	}
	
	protected void addListeners() {
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
		    	singletonInstance = null;
		    	
	        	logger.debug("Closing Frame: " + getClass().getName());	        	
	        	isActive.set(false);
	        	
	        	dispose();
		    }
		});
		
		btnSelectImport.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				File selectedToImport = fileChooser.directoRead(MainGUI.mainAPP.getMainFrame(), "Select crypted workspace to import");
				
				updateToImport(selectedToImport);
			}
		});

		comboBoxExport.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateToExport();
			}
		});

		btnExport.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (selectedForExport != null && selectedForExport.exists() && selectedForExport.isDirectory()) {
						textEditor.textEditorUtils.openFileInScreeptWithReturnAction(selectedForExport, getAfterExportRunnable());
					} else {
						dialogHelper.error("Please select a workspace before to export", "Invalid selection");
					}
				} catch (Exception e1) {
					String errorMessage = "Cannot proceed with export, an error occurred!";
					logger.error(errorMessage, e1);
					dialogHelper.error(errorMessage, "An error occurred");
				}
			}
		});

		btnImport.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (selectedForImport != null && selectedForImport.exists() && selectedForImport.isDirectory()) {
						textEditor.textEditorUtils.openFileInScreeptWithReturnAction(selectedForImport, getAfterImportRunnable(selectedForImport));
					} else {
						dialogHelper.error("Please select a workspace before to import", "Invalid selection");
					}
				} catch (Exception e1) {
					String errorMessage = "Cannot proceed with import, an error occurred!";
					logger.error(errorMessage, e1);
					dialogHelper.error(errorMessage, "An error occurred");
				}
			}
		});
	}
	
	public Runnable getAfterExportRunnable() {
		return new Runnable() {
			
			@Override
			public void run() {
				try {
					CrypterPanel cipherPanel = MainGUI.mainAPP.getPanelCrypter();
					File completed = new File(cipherPanel.getCompletedDirectoryImportExport().getCanonicalPath() + File.separator + selectedForExport.getName());
					if(completed != null && completed.exists() && completed.isDirectory()) {
						MainGUI.mainGuiUtils.startTextEditor(false);
						
						resetCrypterActions();
					}
				} catch(Exception e) {
					logger.error("Cannot do after export action!", e);
				}
			}
		};
	}
	
	public Runnable getAfterImportRunnable(final File selectedForImport) {
		return new Runnable() {
			
			@Override
			public void run() {
				try {
					final CrypterPanel cipherPanel = MainGUI.mainAPP.getPanelCrypter();
					if(cipherPanel != null) {
						File completed = new File(cipherPanel.getCompletedDirectoryImportExport().getCanonicalPath() + File.separator + selectedForImport.getName());
						if(completed != null && completed.exists() && completed.isDirectory()) {
							try {
								textEditor.setWorkspace(completed);
								textOptions.addFileToLimitedList(completed, textOptions.lastSelectedWorkspaces, "lastSelectedWorkspaces");
								MainGUI.mainAPP.updateLastOpenedWorkspaces(textOptions.lastSelectedWorkspaces);
								textEditor.textEditorUtils.refreshFromWorkspace(completed);
								
								MainGUI.mainGuiUtils.startTextEditor(true);
								
								resetCrypterActions();
							} catch (Exception e) {
								logger.error("Cannot set imported workspace: ", e);
							}
						}
					}
				} catch (IOException e1) {
					logger.error("Cannot do action after import!", e1);
				}
			}
		};
	}
	
	private void resetCrypterActions() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final CrypterPanel cipherPanel = MainGUI.mainAPP.getPanelCrypter();
				cipherPanel.setAfterCompleteRunnable(null);
				cipherPanel.setCompletedDirectoryImportExport(null);
			}
		});
	}
	
	public void updateToImport(File toImport) {
		
		if (toImport != null) {
			try {
				if (toImport.exists() && toImport.isDirectory()) {
					selectedForImport = toImport;
					labelToImportDyn.setText(toImport.getCanonicalPath());
				} else {
					dialogHelper.error("Invalid selection.", "Invalid selction");
				}
			} catch (Exception e) {
				String errorMessage = "Cannot update file to import, an error occurred.";
				logger.error(errorMessage, e);
				dialogHelper.error(errorMessage, "An error occurred");
			} 
		}
	}
	
	public void updateToExport() {
		
		try {
			File toExport = ((FileNamed) comboBoxExport.getSelectedItem()).mFile;
			
			if (toExport.exists() && toExport.isDirectory()) {
				selectedForExport = toExport;
			} else {
				dialogHelper.error("Invalid selection.", "Invalid selction");
			}
		} catch (Exception e) {
			String errorMessage = "Cannot update file to export, an error occurred.";
			logger.error(errorMessage, e);
		} 
	}
	
	@Override
	public Dimension getDefaultDimension() {
		return new Dimension(800, 180);
	}
	
	public static ImportExportWorkspcCryptFrame getInstance(TextEditorOption txtOptions, TextEditor textEditor) {
		return singletonInstance == null ? new ImportExportWorkspcCryptFrame(txtOptions, textEditor) : singletonInstance;
	}
}
