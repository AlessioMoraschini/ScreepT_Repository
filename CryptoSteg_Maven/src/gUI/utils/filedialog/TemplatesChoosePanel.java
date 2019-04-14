package gUI.utils.filedialog;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import amfileutils.FileStuff;

import java.awt.SystemColor;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import gUI.om.SupportedFileTextEditor;
import local.HandleString;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.awt.event.ActionEvent;

public class TemplatesChoosePanel extends JFrame{
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(TemplatesChoosePanel.class);
		
	private final static String TEMPLATES_FOLDER_NAME = "Resources_Scrypt/Templates/";
	private final static File TEMPLATES_FOLDER = new File(TEMPLATES_FOLDER_NAME);

	public static boolean active = false;
	
	private JLabel lblTemplates;
	private JPanel panel;
	private JComboBox comboTemplateSelector;
	private JButton btnInject;
	private JButton btnAddSelectionToTemplates;
	private JButton AddCustomFromFile;
	private JButton buttonInvoker;
	private JTextArea outputTextArea;
	private JComboBox templatesCombo;
	
	// MAIN CONSTRUCTOR //
	public TemplatesChoosePanel(JTextArea outputText, JComboBox templatesComb, JButton buttonInvoke) {
		
		active = true;
		
		outputTextArea = outputText;
		templatesCombo = templatesComb;
		buttonInvoker = buttonInvoke;
		
		getContentPane().setBackground(Color.DARK_GRAY);
		getContentPane().setLayout(new MigLayout("", "[100px,grow][70px]", "[42px][30px,grow][30px,grow]"));
		
		panel = new JPanel();
		panel.setForeground(Color.WHITE);
		panel.setBorder(new LineBorder(Color.GRAY, 4));
		panel.setBackground(Color.BLACK);
		getContentPane().add(panel, "cell 0 0 2 1,growx,aligny top");
		panel.setLayout(new MigLayout("", "[][][][][][]", "[]"));
		
		lblTemplates = new JLabel("Templates");
		lblTemplates.setHorizontalAlignment(SwingConstants.CENTER);
		lblTemplates.setForeground(Color.LIGHT_GRAY);
		lblTemplates.setFont(new Font("Lithos Pro Regular", Font.BOLD, 19));
		lblTemplates.setBackground(SystemColor.activeCaptionBorder);
		panel.add(lblTemplates, "cell 5 0,alignx center");
		
		comboTemplateSelector = new JComboBox<String>(retrieveTemplatesFilesFromDir(TEMPLATES_FOLDER));
		getContentPane().add(comboTemplateSelector, "cell 0 1,growx");
		
		AddCustomFromFile = new JButton("Add Custom Template");
		getContentPane().add(AddCustomFromFile, "cell 1 1,growx");
		
		btnInject = new JButton("Inject template");
		btnInject.setFont(new Font("Segoe UI", Font.BOLD, 13));
		btnInject.setBackground(Color.GRAY);
		getContentPane().add(btnInject, "cell 0 2,growx");
		
		btnAddSelectionToTemplates = new JButton("Add Selection to Templates");
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
					selectedTemplate = "\n"+(FileStuff.readFileAsString(selectedFile)) + "\n";
				} catch (IOException e1) {
					logger.error("An error occurred reading template from file "+selectedFile.getAbsolutePath(), e1);
				}
				String modified = HandleString.injectStringInSelection(outputTextArea.getText(), selectedTemplate, outputTextArea.getSelectionStart(), outputTextArea.getSelectionEnd());
				outputTextArea.setText(modified);
			}
		});

		// add custom template from file
		AddCustomFromFile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				File selected = GenericFileChooserDialog.fileRead(null);
				if(selected != null) {
					String selName = selected.getName();
					String ext = FilenameUtils.getExtension(selName);
					if (selected.exists() && SupportedFileTextEditor.getSupportedExtensions().contains(ext)) {
						File outputFile = retrieveTemplateByName(selName);
						int choose = JOptionPane.YES_OPTION;
						
						if(outputFile.exists()) {
							choose = JOptionPane.showConfirmDialog(outputTextArea, "This name already exists, wanna overwrite?", "Name already existing", JOptionPane.YES_NO_OPTION);
						}
						
						if(choose==JOptionPane.YES_OPTION) {
							try {
								FileUtils.copyFile(selected, outputFile);
								reloadTemplates();
							} catch (IOException e1) {
								logger.error("An error occurred copying template from file "+selected.getAbsolutePath(), e1);
								JOptionPane.showMessageDialog(outputTextArea, "An error occurred copying "+selected.getAbsolutePath(), "An error occurred", JOptionPane.ERROR_MESSAGE);
							}
							JOptionPane.showMessageDialog(outputTextArea, "File added to templates :)");
						}
					}else {
						JOptionPane.showMessageDialog(outputTextArea, "File not existing or not in supported types ["+Arrays.toString(SupportedFileTextEditor.values())+"]");
					}
				}
			}
		});
		
		// add selection to templates
		btnAddSelectionToTemplates.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String selected = outputTextArea.getSelectedText();
				if(!selected.equals("")) {
					String inputTemplateName = JOptionPane.showInputDialog("Please specify a name for the template:", "New Template.txt");
					inputTemplateName = (!inputTemplateName.equals(""))? inputTemplateName : "New Template.txt";
					File templateFile = retrieveTemplateByName(inputTemplateName);
					String ext = FilenameUtils.getExtension(inputTemplateName);
					if(!SupportedFileTextEditor.getSupportedExtensions().contains(ext)) {
						templateFile = FileStuff.changeExtension(templateFile, ".txt");
					}
					int choose = JOptionPane.YES_OPTION;
					
					if(templateFile.exists()) {
						choose = JOptionPane.showConfirmDialog(outputTextArea, "This name already exists, wanna overwrite?", "Name already existing", JOptionPane.YES_NO_OPTION);
					}
					
					if(choose==JOptionPane.YES_OPTION) {
						try {
							boolean result = FileStuff.writeStringToFile(selected, templateFile, true);
							reloadTemplates();
						} catch (IOException e1) {
							logger.error("An error occurred reading template from file "+templateFile.getAbsolutePath(), e1);
						}
					}
					
				}else {
					JOptionPane.showMessageDialog(outputTextArea, "No text is selected");
				}
				
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
	
	private void reloadTemplates() {
		comboTemplateSelector.removeAllItems();
		DefaultComboBoxModel model = new DefaultComboBoxModel( retrieveTemplatesFilesFromDir(TEMPLATES_FOLDER) );
		comboTemplateSelector.setModel( model );
		//comboTemplateSelector = new JComboBox<String>(retrieveTemplatesFilesFromDir(TEMPLATES_FOLDER));
		comboTemplateSelector.revalidate();
		comboTemplateSelector.repaint();
	}
	
}
