package frameutils;

import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import net.miginfocom.swing.MigLayout;
import resources.IconsPathConfigurator;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

import amfileutils.FileStuff;

import java.awt.Font;
import javax.swing.JSeparator;
import javax.swing.JTextField;

public class FilePropertiesFrame extends JFrame{

	public File fileUnderAnalysis;
	private JLabel lblSize;
	private JLabel lblPath;
	private JLabel lblFileName;
	private JTextField dynLblFileName;
	private JTextField dynLblPath;
	private JTextField dynLblSize;
	
	public FilePropertiesFrame(File targetFile) {
		
		super();
		
		GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_ABOUT, this);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		panel.setBackground(new Color(255, 255, 240));
		panel.setLayout(new MigLayout("", "[][grow]", "[grow][][grow][][grow]"));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(panel);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		lblFileName = new JLabel("File Name:");
		lblFileName.setFont(new Font("Tahoma", Font.BOLD, 14));
		panel.add(lblFileName, "cell 0 0,alignx center");
		
		dynLblFileName = new JTextField("");
		dynLblFileName.setForeground(Color.DARK_GRAY);
		panel.add(dynLblFileName, "cell 1 0,growx");
		
		JSeparator separator = new JSeparator();
		panel.add(separator, "cell 0 1 2 1,growx");
		
		lblPath = new JLabel("File Path:");
		lblPath.setFont(new Font("Tahoma", Font.BOLD, 14));
		panel.add(lblPath, "cell 0 2,alignx center");
		
		dynLblPath = new JTextField("");
		dynLblPath.setForeground(Color.DARK_GRAY);
		panel.add(dynLblPath, "cell 1 2,growx");
		
		JSeparator separator_1 = new JSeparator();
		panel.add(separator_1, "cell 0 3 2 1,growx");
		
		lblSize = new JLabel("File Size:");
		lblSize.setFont(new Font("Tahoma", Font.BOLD, 14));
		panel.add(lblSize, "cell 0 4,alignx center");
		
		dynLblSize = new JTextField("");
		dynLblSize.setForeground(Color.DARK_GRAY);
		panel.add(dynLblSize, "cell 1 4,growx");
		fileUnderAnalysis = targetFile;
		
		initializeValues();
	}
	
	public void initializeValues() {
		dynLblFileName.setText(fileUnderAnalysis.getName());
		dynLblPath.setText(fileUnderAnalysis.getAbsolutePath());
		dynLblSize.setText(FileStuff.coolFileSize(fileUnderAnalysis));
	}
}
