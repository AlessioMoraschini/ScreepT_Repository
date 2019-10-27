package frameutils.frame;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import net.miginfocom.swing.MigLayout;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.border.LineBorder;

import amfileutils.FileStuff;
import frameutils.utils.GuiUtils;
import general.MyFileView;

import java.awt.Font;
import javax.swing.JSeparator;
import javax.swing.JTextField;

public class FilePropertiesFrame extends JFrame{
	private static final long serialVersionUID = -1506126616913194676L;

	public static final Color defaultRed = Color.ORANGE;
	public static final Color defaultBlue = Color.CYAN;
	public static final Color defaultGreen = Color.GREEN;
	public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd - hh:mm:ss");
	
	private JTextField dynLblFileName;
	private JTextField dynLblPath;
	private JLabel lblSize;
	private JLabel lblPath;
	private JLabel lblFileName;
	private JLabel dynLblSize;
	private JLabel lblLastModified;
	private JLabel dynLblLastChange;
	private JPanel footerPanel;
	private JLabel dynLblCanRead;
	private JLabel dynLblCanWrite;
	private JLabel dynLblCanExecute;
	private JLabel dynLblIsHidden;

	public File fileUnderAnalysis;
	
	public FilePropertiesFrame(File targetFile, boolean alwaysOnTop) {
		
		super();
		
		GuiUtils.setFrameIcon(MyFileView.getExtIcon(targetFile), this);
		this.setAlwaysOnTop(alwaysOnTop);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		panel.setBackground(Color.DARK_GRAY);
		panel.setLayout(new MigLayout("", "[][25%][][grow]", "[grow][][grow][][grow][30px:n:40px]"));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(panel);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		lblFileName = new JLabel("File Name:");
		lblFileName.setForeground(Color.LIGHT_GRAY);
		lblFileName.setFont(new Font("Tahoma", Font.BOLD, 14));
		panel.add(lblFileName, "cell 0 0,alignx center");
		
		dynLblFileName = new JTextField("");
		dynLblFileName.setForeground(Color.DARK_GRAY);
		panel.add(dynLblFileName, "cell 1 0 3 1,growx");
		
		JSeparator separator = new JSeparator();
		panel.add(separator, "cell 0 1 4 1,growx");
		
		lblPath = new JLabel("File Path:");
		lblPath.setForeground(Color.LIGHT_GRAY);
		lblPath.setFont(new Font("Tahoma", Font.BOLD, 14));
		panel.add(lblPath, "cell 0 2,alignx center");
		
		dynLblPath = new JTextField("");
		dynLblPath.setForeground(Color.DARK_GRAY);
		panel.add(dynLblPath, "cell 1 2 3 1,growx");
		
		JSeparator separator_1 = new JSeparator();
		panel.add(separator_1, "cell 0 3 4 1,growx");
		
		lblSize = new JLabel("File Size:");
		lblSize.setForeground(Color.LIGHT_GRAY);
		lblSize.setFont(new Font("Tahoma", Font.BOLD, 14));
		panel.add(lblSize, "cell 0 4,alignx center");
		
		dynLblSize = new JLabel("");
		dynLblSize.setForeground(new Color(176, 196, 222));
		panel.add(dynLblSize, "cell 1 4,growx");
		
		lblLastModified = new JLabel("Last Change:");
		lblLastModified.setForeground(Color.LIGHT_GRAY);
		lblLastModified.setFont(new Font("Tahoma", Font.BOLD, 14));
		panel.add(lblLastModified, "cell 2 4,alignx center");
		
		dynLblLastChange = new JLabel((String) null);
		dynLblLastChange.setForeground(new Color(176, 196, 222));
		panel.add(dynLblLastChange, "cell 3 4,alignx left");
		
		footerPanel = new JPanel();
		footerPanel.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		footerPanel.setForeground(Color.WHITE);
		footerPanel.setBackground(Color.GRAY);
		panel.add(footerPanel, "cell 0 5 4 1,grow");
		footerPanel.setLayout(new MigLayout("", "[1px,grow][1px,grow][1px,grow][1px,grow]", "[1px,grow]"));
		
		dynLblCanRead = new JLabel((String) null);
		dynLblCanRead.setForeground(Color.WHITE);
		footerPanel.add(dynLblCanRead, "cell 0 0,alignx center,growy");
		
		dynLblCanWrite = new JLabel((String) null);
		dynLblCanWrite.setForeground(Color.WHITE);
		footerPanel.add(dynLblCanWrite, "cell 1 0");
		
		dynLblCanExecute = new JLabel((String) null);
		dynLblCanExecute.setForeground(Color.WHITE);
		footerPanel.add(dynLblCanExecute, "cell 2 0");
		
		dynLblIsHidden = new JLabel((String) null);
		dynLblIsHidden.setForeground(Color.WHITE);
		footerPanel.add(dynLblIsHidden, "cell 3 0");
		fileUnderAnalysis = targetFile;
		
		initializeValues();
		
		adjustLabelsLength();
	}
	
	public void adjustLabelsLength() {
		dynLblFileName.setPreferredSize(new Dimension(18, 25));
		dynLblPath.setPreferredSize(new Dimension(18, 25));
	}
	
	public void initializeValues() {
		dynLblFileName.setText(fileUnderAnalysis.getName());
		dynLblPath.setText(fileUnderAnalysis.getAbsolutePath());
		dynLblSize.setText(FileStuff.coolFileSize(fileUnderAnalysis));
		updateFooter();
	}
	
	/**
	 * Update labels with informations about the given file
	 * @param fileToAnalyze
	 */
	public void refreshValues(File fileToAnalyze) {
		if(fileToAnalyze == null || !fileToAnalyze.exists()) {
			return;
		}
		GuiUtils.setFrameIcon(MyFileView.getExtIcon(fileToAnalyze), this);
		this.fileUnderAnalysis = fileToAnalyze;
		adjustLabelsLength();
		this.initializeValues();
		this.repaint();
	}
	
	public void updateFooter() {
		dynLblLastChange.setText(" " + dateFormatter.format(new Date(fileUnderAnalysis.lastModified())));
		
		// CANREAD
		if (fileUnderAnalysis.canRead()) {
			dynLblCanRead.setText("Can Read");
			dynLblCanRead.setForeground(defaultGreen);
		}else {
			dynLblCanRead.setText("Cannot Read");
			dynLblCanRead.setForeground(defaultRed);
		}
		
		// CANWRITE
		if (fileUnderAnalysis.canWrite()) {
			dynLblCanWrite.setText("Can Write");
			dynLblCanWrite.setForeground(defaultGreen);
		}else {
			dynLblCanWrite.setText("Cannot Write");
			dynLblCanWrite.setForeground(defaultRed);
		}
		
		// CAN EXECUTE
		if (fileUnderAnalysis.canExecute()) {
			dynLblCanExecute.setText("Can Execute");
			dynLblCanExecute.setForeground(defaultGreen);
		}else {
			dynLblCanExecute.setText("Cannot Execute");
			dynLblCanExecute.setForeground(defaultRed);
		}
		
		// HIDDEN FILE
		if (fileUnderAnalysis.isHidden()) {
			dynLblIsHidden.setText("Hidden File");
			dynLblIsHidden.setForeground(defaultBlue);
		}else {
			dynLblIsHidden.setText("Visible File");
			dynLblIsHidden.setForeground(defaultGreen);
		}
		
	}
}
