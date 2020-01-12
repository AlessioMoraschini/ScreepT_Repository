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
package gUI.utils.filedialog;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import dialogutils.JOptionHelperExtended;
import files.FileWorker;
import files.FileVarious;
import frameutils.frame.arch.ParentFrame;
import frameutils.utils.GuiUtilsExtended;
import maingUI.MainGuiGeneralAppStarter;

import java.awt.SystemColor;
import net.miginfocom.swing.MigLayout;
import om.SupportedFileTextEditor;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import resources.ImagesPathConfigurator;

import javax.swing.JLabel;

import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.awt.event.ActionEvent;

public class TemplatesChooserFrame extends ParentFrame {
	private static final long serialVersionUID = 6657650710605549352L;

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(TemplatesChooserFrame.class);
		
	private final static String TEMPLATES_FOLDER_NAME = GeneralConfig.RESOURCES_DIR  + "Templates/";
	private final static File TEMPLATES_FOLDER = new File(TEMPLATES_FOLDER_NAME);

	public static boolean active = false;
	
	private JLabel lblTemplates;
	private JPanel panel;
	private JComboBox<String> comboTemplateSelector;
	private JButton btnInject;
	private JButton btnAddSelectionToTemplates;
	private JButton AddCustomFromFile;
	private JButton buttonInvoker;
	private JButton btnOpenInFilesystem;
	private JButton btnReloadTemplates;
	public JTextArea outputTextArea;
	private JOptionHelperExtended dialogHelper;
	
	
	// MAIN CONSTRUCTOR //
	public TemplatesChooserFrame(JFrame parent, JTextArea outputText, JButton buttonInvoke) {
		
		active = true;
		
		dialogHelper = new JOptionHelperExtended(this);
		
		outputTextArea = outputText;
		buttonInvoker = buttonInvoke;
		GuiUtilsExtended.showOnSameScreen(this, parent);
		GuiUtilsExtended.setFrameIcon(ImagesPathConfigurator.MAIN_APP_ICON, this);
		this.setResizable(false);
		
		getContentPane().setBackground(Color.DARK_GRAY);
		getContentPane().setLayout(new MigLayout(
				GeneralConfig.DEBUG_GRAPHICS+"insets 2, fill",
				"[100px,grow][70px]",
				"[45px][35px,grow][30px,grow]"));
		
		panel = new JPanel();
		panel.setForeground(Color.WHITE);
		panel.setBorder(new LineBorder(Color.GRAY, 2));
		panel.setBackground(Color.BLACK);
		getContentPane().add(panel, "cell 0 0 2 1,growx,aligny top");
		panel.setLayout(new MigLayout("insets 2, fill", "[][][][grow][][]", "[40px]"));
		
		lblTemplates = new JLabel("Templates Manager");
		lblTemplates.setHorizontalAlignment(SwingConstants.CENTER);
		lblTemplates.setForeground(Color.LIGHT_GRAY);
		lblTemplates.setFont(new Font("Segoe UI", Font.BOLD, 15));
		lblTemplates.setBackground(SystemColor.activeCaptionBorder);
		panel.add(lblTemplates, "cell 0 0,alignx center");
		
		btnOpenInFilesystem = new JButton("");
		panel.add(btnOpenInFilesystem, "cell 5 0,grow");
		btnOpenInFilesystem.setToolTipText("Open Templates folder with OS File Explorer");
		btnOpenInFilesystem.setPreferredSize(new Dimension(20, 20));
		btnOpenInFilesystem.setMinimumSize(new Dimension(20, 9));
		btnOpenInFilesystem.setMaximumSize(new Dimension(20, 20));
		btnOpenInFilesystem.setCursor(GuiUtilsExtended.CURSOR_HAND);
		GuiUtilsExtended.makeTransparentJButton(btnOpenInFilesystem);
		btnOpenInFilesystem.setIcon(new ImageIcon(IconsPathConfigurator.iconFolderPath));
		btnOpenInFilesystem.setForeground(Color.BLACK);
		
		btnReloadTemplates = new JButton("");
		panel.add(btnReloadTemplates, "cell 4 0,grow");
		btnReloadTemplates.setToolTipText("Reload Templates List");
		btnReloadTemplates.setPreferredSize(new Dimension(20, 20));
		btnReloadTemplates.setMinimumSize(new Dimension(20, 9));
		btnReloadTemplates.setMaximumSize(new Dimension(20, 20));
		btnReloadTemplates.setCursor(GuiUtilsExtended.CURSOR_HAND);
		GuiUtilsExtended.makeTransparentJButton(btnReloadTemplates);
		btnReloadTemplates.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD4));
		btnReloadTemplates.setForeground(Color.BLACK);
		
		comboTemplateSelector = new JComboBox<String>(retrieveTemplatesFilesFromDir(TEMPLATES_FOLDER));
		comboTemplateSelector.setMinimumSize(new Dimension(33, 35));
		comboTemplateSelector.setCursor(GuiUtilsExtended.CURSOR_HAND);
		getContentPane().add(comboTemplateSelector, "cell 0 1,growx");
		
		AddCustomFromFile = new JButton("Add Custom Template");
		AddCustomFromFile.setMinimumSize(new Dimension(185, 35));
		AddCustomFromFile.setCursor(GuiUtilsExtended.CURSOR_HAND);
		getContentPane().add(AddCustomFromFile, "cell 1 1,growx");
		
		btnInject = new JButton("Inject template");
		btnInject.setMinimumSize(new Dimension(108, 35));
		btnInject.setCursor(GuiUtilsExtended.CURSOR_HAND);
		btnInject.setFont(new Font("Segoe UI", Font.BOLD, 13));
		btnInject.setBackground(Color.GRAY);
		getContentPane().add(btnInject, "cell 0 2,growx");
		
		btnAddSelectionToTemplates = new JButton("Add Selection to Templates");
		btnAddSelectionToTemplates.setMinimumSize(new Dimension(185, 35));
		btnAddSelectionToTemplates.setCursor(GuiUtilsExtended.CURSOR_HAND);
		getContentPane().add(btnAddSelectionToTemplates, "cell 1 2,growx");
		
		addHandlers();
		
	}
	
	private void addHandlers() {
		
		// DEFAULT CLOSE ACTIONS 
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	active = false;
	        	buttonInvoker.setEnabled(true);
	        	
				logger.debug("Closing "+getClass().getName()+" - Configuration resetted.\n");
	        	dispose();
		    }
		});
		
		// append template into text area at current selection
		btnInject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selectedTemplate = "";
				File selectedFile = retrieveTemplateByName((String)comboTemplateSelector.getSelectedItem());
				try {
					selectedTemplate = (FileWorker.readFileAsString(selectedFile));
				} catch (IOException e1) {
					dialogHelper.error("An error occurred reading template from file "+selectedFile.getAbsolutePath(), "ERROR reading Template");
					logger.error("An error occurred reading template from file "+selectedFile.getAbsolutePath(), e1);
				}
				outputTextArea.insert(selectedTemplate, outputTextArea.getCaretPosition());
			}
		});

		// add custom template from file
		AddCustomFromFile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				File selected = MainGuiGeneralAppStarter.fileDialogHelper.fileRead(null);
				if(selected != null) {
					String selName = selected.getName();
					String ext = FilenameUtils.getExtension(selName);
					if (selected.exists() && SupportedFileTextEditor.getSupportedExtensions().contains(ext)) {
						File outputFile = retrieveTemplateByName(selName);
						boolean choose = true;
						
						if(outputFile.exists()) {
							choose = dialogHelper.yesOrNo("This name already exists, wanna overwrite?", "Name already existing");
						}
						
						if(choose) {
							try {
								FileUtils.copyFile(selected, outputFile);
								reloadTemplates();
							} catch (IOException e1) {
								logger.error("An error occurred copying template from file "+selected.getAbsolutePath(), e1);
								dialogHelper.error("An error occurred copying "+selected.getAbsolutePath(), "An error occurred");
							}
							dialogHelper.info("File added to templates :)", "File added to templates!");
						}
					}else {
						dialogHelper.warn("File not existing or not one of supported types ["+Arrays.toString(SupportedFileTextEditor.values())+"]", "File not existing or unsupported");
					}
				}
			}
		});
		
		// add selection to templates
		btnAddSelectionToTemplates.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String selected = outputTextArea.getSelectedText();
				if(selected != null && !selected.equals("")) {
					String inputTemplateName = dialogHelper.askForStringNullable("Please specify a name for the template:", "New Template.txt",  "New Template.txt");
					boolean cancel = (inputTemplateName == null)? true : false;
					inputTemplateName = (!cancel && inputTemplateName.equals("")) ? "New Template.txt" : inputTemplateName;

					if (!cancel) {
						File templateFile = retrieveTemplateByName(inputTemplateName);
						String ext = FilenameUtils.getExtension(inputTemplateName);
						if (!SupportedFileTextEditor.getSupportedExtensions().contains(ext)) {
							templateFile = FileVarious.changeExtension(templateFile, ".txt");
						}
						boolean choose = true;
						if (templateFile.exists()) {
							choose = dialogHelper.yesOrNo("This name already exists, wanna overwrite?", "Name already existing");
						}
						if (choose) {
							try {
								boolean result = FileWorker.writeStringToFile(selected, templateFile, true);
								logger.info("Template written with esit: " + result);
								dialogHelper.info("Template Added to collection.", "Template Added");
								reloadTemplates();
							} catch (IOException e1) {
								dialogHelper.info("An error occurred writing template to file: " + templateFile.getAbsolutePath(), "An error occurred");
								logger.error("An error occurred writing template to file: " + templateFile.getAbsolutePath(), e1);
							}
						} 
					} else {
						logger.debug("Template addition canceled by user");
					}
					
				}else {
					dialogHelper.warn("No text is selected", "NO TEXT SELECTED");
				}
				
			}
		});
		
		// OPEN CURRENT IN SYSTEM EXPLORER
		btnOpenInFilesystem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						GuiUtilsExtended.openInFileSystem(new File(GeneralConfig.TEMPLATES_FOLDER));
					}
				}).start();
			}
		});

		// RELOAD TEMPLATES
		btnReloadTemplates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						reloadTemplates();
					}
				});
			}
		});
		
	}
	
	// USEFUL METHODS //
	
	private String[] retrieveTemplatesFilesFromDir(File sourceDir){
		
		return sourceDir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				String ext = FilenameUtils.getExtension(name);
				File current = new File(TEMPLATES_FOLDER_NAME+name);
				
				if(current.exists() && current.isFile() && SupportedFileTextEditor.getSupportedExtensions().contains(ext)) {
					return true;
				}else {
					return false;
				}
			}
		});
	}
	
	private File retrieveTemplateByName(String name) {
		return new File(TEMPLATES_FOLDER_NAME+name);
	}
	
	@Override
	public Dimension getDefaultDimension() {
		return new Dimension(550, 170);
	}
	
	private void reloadTemplates() {
		comboTemplateSelector.removeAllItems();
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>( retrieveTemplatesFilesFromDir(TEMPLATES_FOLDER) );
		comboTemplateSelector.setModel( model );
		//comboTemplateSelector = new JComboBox<String>(retrieveTemplatesFilesFromDir(TEMPLATES_FOLDER));
		comboTemplateSelector.revalidate();
		comboTemplateSelector.repaint();
	}
	
}
