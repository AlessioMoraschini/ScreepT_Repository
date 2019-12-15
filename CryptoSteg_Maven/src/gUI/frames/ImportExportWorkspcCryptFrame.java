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

import crypt.manager.CryptoMode;
import crypt.manager.ScreeptManagerFileOrFolder;

import javax.swing.JRadioButton;
import javax.swing.JComboBox;
import javax.swing.JButton;

public class ImportExportWorkspcCryptFrame extends ParentFrame {
	private static final long serialVersionUID = -8119237171970529701L;
	
	private TextEditorOption txtOptions;
	private TextEditor txtEditor;
	
	private static int WIDTH = 700;
	private static int HEIGHT = 120;
	private JComboBox<CryptoMode> comboBox;
	private JRadioButton rdbtnUseTempFolderOnDesktop;
	private JButton btnImport;
	private JButton btnExport;
	private JButton btnCancel;
	
	private ScreeptManagerFileOrFolder screeptManager;

	public ImportExportWorkspcCryptFrame(TextEditorOption txtOptions, TextEditor textEditor) {
		super();
		getContentPane().setBackground(Color.DARK_GRAY);
		
		GuiUtils.centerComponent(this, WIDTH, HEIGHT);
		setUndecorated(true);
		
		logger.info("Opening frame: " + getClass().getName());
		
		this.txtEditor = textEditor;
		this.txtOptions = (txtOptions != null) ? txtOptions : new TextEditorOption();
		
		getContentPane().setLayout(new MigLayout("", "[::60px,grow][100px:n:25%,grow][grow][::25%][::100px][]", "[:40px:40px,grow][grow]"));
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setBackground(Color.GRAY);
		getContentPane().add(panel, "cell 0 0 6 1,grow");
		panel.setLayout(new MigLayout("", "[579px,grow]", "[40px]"));
		
		JLabel lblImportexportCryptedWorkspace = new JLabel("Import/Export crypted workspace");
		lblImportexportCryptedWorkspace.setForeground(Color.WHITE);
		lblImportexportCryptedWorkspace.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
		panel.add(lblImportexportCryptedWorkspace, "cell 0 0,alignx center,growy");
		
		JLabel lblMode = new JLabel("Mode:");
		lblMode.setForeground(Color.WHITE);
		lblMode.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
		getContentPane().add(lblMode, "cell 0 1,alignx trailing,aligny center");
		
		JComboBox<CryptoMode> comboBox = new JComboBox<CryptoMode>(CryptoMode.values());
//		comboBox = new JComboBox<CryptoMode>();
		getContentPane().add(comboBox, "cell 1 1,growx,aligny center");
		
		rdbtnUseTempFolderOnDesktop = new JRadioButton("Use temp folder on Desktop");
		rdbtnUseTempFolderOnDesktop.setForeground(Color.LIGHT_GRAY);
		rdbtnUseTempFolderOnDesktop.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
		rdbtnUseTempFolderOnDesktop.setOpaque(false);
		getContentPane().add(rdbtnUseTempFolderOnDesktop, "cell 2 1,alignx center,aligny center");
		
		btnImport = new JButton("Import");
		btnImport.setFont(new Font("Segoe UI", Font.BOLD, 14));
		btnImport.setBackground(new Color(119, 136, 153));
		getContentPane().add(btnImport, "cell 3 1,growx,aligny center");
		
		btnExport = new JButton("Export");
		btnExport.setFont(new Font("Segoe UI", Font.BOLD, 14));
		btnExport.setBackground(new Color(119, 136, 153));
		getContentPane().add(btnExport, "cell 4 1,growx,aligny center");
		
		btnCancel = new JButton("Cancel");
		btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
		btnCancel.setBackground(new Color(250, 128, 114));
		getContentPane().add(btnCancel, "cell 5 1,growx,aligny center");
		
		addListeners();
		
		logger.info("Frame loaded: " + getClass().getName());
	}
	
	@Override
	protected void setCloseAction() {
		super.setCloseAction();
	}
	
	private void addListeners() {
		btnExport.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				File userChoice = null;
				
				if(rdbtnUseTempFolderOnDesktop.isSelected()) {
					
				} else {
					
				}
			}
		});
	}
}
