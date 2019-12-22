package gUI.frames;

import frameutils.frame.arch.ParentFrame;
import frameutils.utils.GuiUtils;
import gUI.panels.TextEditor;
import om.TextEditorOption;
import net.miginfocom.swing.MigLayout;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.border.LineBorder;

import amfileutils.om.FileNamed;

import javax.swing.JComboBox;
import javax.swing.JButton;

public class ImportExportWorkspcCryptFrame extends ParentFrame {
	private static final long serialVersionUID = -8119237171970529701L;
	
	protected TextEditorOption txtOptions;
	protected TextEditor txtEditor;
	
	protected static int WIDTH = 700;
	protected static int HEIGHT = 120;
	protected JButton btnImport;
	protected JButton btnExport;
	
	protected JLabel lblSelectedForExport;
	protected JComboBox<FileNamed> comboBoxExport;
	private JButton btnSelect;
	private JLabel lblNewLabel;

	/**
	 * Text editor option and texteditor are nullable
	 * @param txtOptions
	 * @param textEditor
	 */
	public ImportExportWorkspcCryptFrame(TextEditorOption txtOptions, TextEditor textEditor) {
		super();
		getContentPane().setBackground(Color.DARK_GRAY);
		
		GuiUtils.centerComponent(this, WIDTH, HEIGHT);
		
		logger.info("Opening frame: " + getClass().getName());
		
		this.txtEditor = textEditor;
		this.txtOptions = (txtOptions != null) ? txtOptions : new TextEditorOption();
		
		getContentPane().setLayout(new MigLayout("", "[168.00][grow][][100px][:100px:140px][]", "[:40px:40px,grow][grow][grow]"));
		
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
		lblMode.setForeground(Color.WHITE);
		lblMode.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		getContentPane().add(lblMode, "cell 0 1,alignx center,aligny center");
		
		lblNewLabel = new JLabel("- No workspace selected to import -");
		lblNewLabel.setForeground(Color.LIGHT_GRAY);
		getContentPane().add(lblNewLabel, "cell 1 1 2 1,alignx center");
		
		btnSelect = new JButton("Select new to import");
		btnSelect.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		btnSelect.setBackground(Color.GRAY);
		getContentPane().add(btnSelect, "cell 3 1,growx,aligny center");
		
		btnImport = new JButton("Import Selected");
		btnImport.setForeground(Color.BLACK);
		btnImport.setFont(new Font("Segoe UI", Font.BOLD, 14));
		btnImport.setBackground(Color.LIGHT_GRAY);
		getContentPane().add(btnImport, "cell 4 1 2 1,growx,aligny center");
		
		lblSelectedForExport = new JLabel("Selected for export:");
		lblSelectedForExport.setForeground(Color.WHITE);
		lblSelectedForExport.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		getContentPane().add(lblSelectedForExport, "cell 0 2,alignx center");
		
		comboBoxExport = new JComboBox<FileNamed>(txtOptions.lastSelectedWorkspaces.getList());
		comboBoxExport.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//		comboBoxExport = new JComboBox<FileNamed>();
		getContentPane().add(comboBoxExport, "cell 1 2 3 1,growx,aligny center");
		
		btnExport = new JButton("Export Selected");
		btnExport.setForeground(Color.BLACK);
		btnExport.setFont(new Font("Segoe UI", Font.BOLD, 14));
		btnExport.setBackground(Color.LIGHT_GRAY);
		getContentPane().add(btnExport, "cell 4 2 2 1,growx,aligny center");
		
		addListeners();
		
		setVisible(true);
		
		logger.info("Frame loaded: " + getClass().getName());
	}
	
	@Override
	protected void setCloseAction() {
		super.setCloseAction();
	}
	
	protected void addListeners() {
		
		btnSelect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO
			}
		});

		btnExport.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO
			}
		});

		btnImport.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO
			}
		});
	}
}
