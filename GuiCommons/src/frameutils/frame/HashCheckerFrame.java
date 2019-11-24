package frameutils.frame;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFrame;

import frameutils.frame.arch.ParentFrame;
import frameutils.utils.GuiUtils;
import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import textareautil.TxtFieldFileDragNDropManager;
import updater.DownloadUtils;
import updater.HashingFunction;

import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;

import dialogutils.GenericFileChooserDialog;
import dialogutils.JOptionHelper;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSeparator;

public class HashCheckerFrame extends ParentFrame{
	private static final long serialVersionUID = -5018329398511796700L;
	
	private File currentFile;
	public JFrame parentFrame;
	private TxtFieldFileDragNDropManager txtFieldLoadedFile;
	private JTextField txtFieldChecksumResult;
	private JTextField txtFieldChecksumMatcher;
	private JComboBox<HashingFunction> comboBoxHashingAlgos;
	private JButton btnVerifyChecksum;
	private JButton btnCalculateHash;
	private JButton btnSelectFile;
	
	private JOptionHelper dialogHelper;
	private GenericFileChooserDialog fileChooser;
	
	private JFrame thisFrame;
	
	private static Color lightGrayDefault = new Color(211, 211, 211);
	private static Color yellowDefault = new Color(250, 250, 210);
	
	public HashCheckerFrame(File initialFile, JFrame parentFrame) {
		logger.info(this.getClass().getName() + " - Starting...");
		
		setTitle(GeneralConfig.APPLICATION_NAME + " - File Hash Checker");

		this.parentFrame = parentFrame;
		this.dialogHelper = new JOptionHelper(this);
		this.fileChooser = new GenericFileChooserDialog(null);
		thisFrame = this;
		
		setBounds(new Rectangle(850,215));
		GuiUtils.centerComponent(this);
		setVisible(true);
		setAlwaysOnTop(true);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		getContentPane().setBackground(Color.DARK_GRAY);
		getContentPane().setLayout(new MigLayout("fill, insets 2", "[5px][100px][220px,grow][5px]", "[5px,center][30px,center][][35px][][20px,grow][20px,grow][5px,center]"));
		
		btnSelectFile = new JButton("Select File");
		btnSelectFile.setCursor(GuiUtils.CURSOR_HAND);
		btnSelectFile.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnSelectFile.setBackground(new Color(119, 136, 153));
		getContentPane().add(btnSelectFile, "cell 1 1,grow");
		
		GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_HASHING, this);
		setLocationRelativeTo(parentFrame);
		
		txtFieldLoadedFile = new TxtFieldFileDragNDropManager(new Runnable() {
			@Override
			public void run() {
				if (txtFieldLoadedFile != null && txtFieldLoadedFile.loadedFilesList != null && !txtFieldLoadedFile.loadedFilesList.isEmpty()) {
					loadFile(txtFieldLoadedFile.loadedFilesList.get(0));
					txtFieldLoadedFile.loadedFilesList.clear();
				}
			}
		}, txtFieldLoadedFile);
		
		txtFieldLoadedFile.setBackground(lightGrayDefault);
		txtFieldLoadedFile.setPreferredSize(new Dimension(18, 25));
		txtFieldLoadedFile.setForeground(Color.DARK_GRAY);
		getContentPane().add(txtFieldLoadedFile, "cell 2 1,grow");
		
		JSeparator separator_1 = new JSeparator();
		getContentPane().add(separator_1, "cell 1 2 2 1,growx");
		
		JLabel lblHashingAlgorithm = new JLabel("Hashing algorithm:");
		lblHashingAlgorithm.setForeground(Color.LIGHT_GRAY);
		lblHashingAlgorithm.setFont(new Font("Tahoma", Font.BOLD, 14));
		getContentPane().add(lblHashingAlgorithm, "cell 1 3,alignx center");
		
//		comboBoxHashingAlgos = new JComboBox<HashingFunction>();
		comboBoxHashingAlgos = new JComboBox<HashingFunction>(HashingFunction.values());
		comboBoxHashingAlgos.setCursor(GuiUtils.CURSOR_HAND);
		getContentPane().add(comboBoxHashingAlgos, "cell 2 3,growx");
		
		JSeparator separator = new JSeparator();
		getContentPane().add(separator, "cell 1 4 2 1,growx");
		
		btnVerifyChecksum = new JButton("Verify Checksum");
		btnVerifyChecksum.setCursor(GuiUtils.CURSOR_HAND);
		btnVerifyChecksum.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnVerifyChecksum.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnVerifyChecksum, "cell 1 5,grow");
		
		txtFieldChecksumMatcher = new JTextField((String) null);
		txtFieldChecksumMatcher.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		txtFieldChecksumMatcher.setBackground(yellowDefault);
		txtFieldChecksumMatcher.setPreferredSize(new Dimension(18, 25));
		txtFieldChecksumMatcher.setForeground(Color.DARK_GRAY);
		getContentPane().add(txtFieldChecksumMatcher, "cell 2 5,grow");
		
		btnCalculateHash = new JButton("Calculate Hash");
		btnCalculateHash.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnCalculateHash.setCursor(GuiUtils.CURSOR_HAND);
		btnCalculateHash.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnCalculateHash, "cell 1 6,grow");
		
		txtFieldChecksumResult = new JTextField((String) null);
		txtFieldChecksumResult.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		txtFieldChecksumResult.setBackground(yellowDefault);
		txtFieldChecksumResult.setPreferredSize(new Dimension(18, 25));
		txtFieldChecksumResult.setForeground(Color.DARK_GRAY);
		getContentPane().add(txtFieldChecksumResult, "cell 2 6,grow");
		
		loadFile(initialFile);
		
		addHandlers();
		
		logger.info(this.getClass().getName() + " - Started!");
	}
	
	public void loadFile(File toLoadFile) {
		if (toLoadFile != null && toLoadFile.exists() && toLoadFile.isFile()) {
			currentFile = toLoadFile;
			txtFieldLoadedFile.setText(currentFile.getAbsolutePath());
			txtFieldLoadedFile.setBackground(lightGrayDefault);
			txtFieldChecksumResult.setText("Click on 'calculate checksum' to get hash Cheksum for current file");
			if (txtFieldChecksumMatcher.getText().equals("")) {
				btnCalculateHash.doClick();
			} else {
				btnVerifyChecksum.doClick();
			}
		}
	}

	private void addHandlers() {
		// DEFAULT CLOSE ACTIONS 
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	isActive.set(false);
	        	
				logger.debug("Closing "+getClass().getName());
//	        	dispose();
		    }
		});
		
		txtFieldLoadedFile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				loadFile(new File(txtFieldLoadedFile.getText()));
			}
		});
		
		btnSelectFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File readFile = fileChooser.fileRead(thisFrame);
				if(readFile != null && readFile.exists()) {
					loadFile(readFile);
				}
			}
		});
		
		btnCalculateHash.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if(currentFile != null && currentFile.exists() && currentFile.isFile()) {
						txtFieldChecksumResult.setText(DownloadUtils.getFileHash(currentFile, getSelectedFunction()));
						txtFieldChecksumResult.setBackground(GeneralConfig.ADVICE_FOOTER_OK_COLOR);
					} else {
						txtFieldLoadedFile.setBackground(yellowDefault);
						dialogHelper.warn("File not existing or not selected!", "File not found");
					}
					
				} catch (Throwable e1) {
					logger.error("An error occurred while calculating hash for current file: "+currentFile, e1);
					dialogHelper.error("An error occurred while calculating hash!", "An error occurred");
					txtFieldChecksumResult.setBackground(GeneralConfig.ADVICE_FOOTER_KO_COLOR);
				}
			}
		});
		
		btnVerifyChecksum.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if(currentFile != null && currentFile.exists() && currentFile.isFile()) {
						String hash = DownloadUtils.getFileHash(currentFile, getSelectedFunction());
						txtFieldChecksumResult.setText(hash);
						txtFieldChecksumResult.setBackground(GeneralConfig.ADVICE_FOOTER_OK_COLOR);
						if(txtFieldChecksumMatcher.getText().equals(hash) || txtFieldChecksumMatcher.getText().contains(hash)) {
							txtFieldChecksumMatcher.setBackground(GeneralConfig.ADVICE_FOOTER_OK_COLOR);
							dialogHelper.info("File matches the given checksum!", "File matched!");
						} else {
							txtFieldChecksumMatcher.setBackground(GeneralConfig.ADVICE_FOOTER_KO_COLOR);
							dialogHelper.error("File does not match the given checksum!", "File does not match!");
						}
					} else {
						txtFieldLoadedFile.setBackground(yellowDefault);
						dialogHelper.warn("File not existing or not selected!", "File not found");
					}
					
				} catch (Throwable e1) {
					logger.error("An error occurred while calculating hash for current file: "+currentFile, e1);
					dialogHelper.error("An error occurred while calculating hash!", "An error occurred");
					txtFieldChecksumMatcher.setBackground(GeneralConfig.ADVICE_FOOTER_KO_COLOR);
					txtFieldChecksumResult.setBackground(GeneralConfig.ADVICE_FOOTER_KO_COLOR);
				}
			}
		});
	}
	
	private HashingFunction getSelectedFunction() {
		HashingFunction selected = (HashingFunction) comboBoxHashingAlgos.getSelectedItem();
		return selected;
	}
}
