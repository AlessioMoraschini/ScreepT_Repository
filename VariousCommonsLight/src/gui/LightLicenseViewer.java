package gui;

import java.io.File;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import net.miginfocom.swing.MigLayout;
import utility.string.StringWorker;

import java.awt.Color;
import javax.swing.border.LineBorder;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import files.FileWorker;

import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JButton;

public class LightLicenseViewer extends JDialog {
	private static final long serialVersionUID = -4557971895329584958L;
	
	public enum UserLicenseChoice{
		ACCEPTED,
		REJECTED;
	}
	
	private static final Dimension DEFAULT_PREF_DIMENSION = new Dimension(1200, 650);

	public UserLicenseChoice userChoice = UserLicenseChoice.REJECTED;
	File sourceLicense;
	
	private JScrollPane scrollPane;
	private JEditorPane jEditorPane;
	private JLabel lblNewLabel;
	private JButton btnDecline;
	private JButton btnAccept;
	
	private JOptionHelper dialogHelper;
	
	public boolean licenseLoadedCorrectly;
	
	public static LightLicenseViewer instance = null;
	
	public static boolean mandatoryChoice = true;
	
	public LightLicenseViewer(File sourceLicense, String header, String title) {
		
		String correctHeader = !StringWorker.isEmpty(header) ? header : "Accept licence to continue using this software";
		String correctTitle = !StringWorker.isEmpty(title) ? title : "EULA - acceptance";
		
		this.licenseLoadedCorrectly = false;
		this.sourceLicense = sourceLicense;
		this.dialogHelper = new JOptionHelper(this);
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setPreferredSize(DEFAULT_PREF_DIMENSION);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setIconImage(GuiUtils.iconToImage(UIManager.getIcon("OptionPane.informationIcon")));

		getContentPane().setBackground(Color.DARK_GRAY);
		getContentPane().setLayout(new MigLayout("fill, insets 4", "[27px,grow][27px,grow]", "[:35px:80px][27px,grow][35px:n]"));
		
		setTitle(correctTitle);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		panel.setBackground(Color.LIGHT_GRAY);
		getContentPane().add(panel, "cell 0 0 2 1,grow");
		panel.setLayout(new MigLayout("", "[grow]", "[]"));
		
		lblNewLabel = new JLabel(correctHeader);
		lblNewLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
		panel.add(lblNewLabel, "cell 0 0,alignx center");
		
		scrollPane = new JScrollPane();
		scrollPane.setBorder(new LineBorder(Color.BLACK, 2));
		scrollPane.setPreferredSize(DEFAULT_PREF_DIMENSION);
		int scrollPaneHeigth = mandatoryChoice ? 1 : 2;
		getContentPane().add(scrollPane, "cell 0 1 2 "+scrollPaneHeigth+",alignx center,aligny center");
		
		jEditorPane = new JEditorPane();
		scrollPane.setViewportView(jEditorPane);
		addHtmlStyle();
		
		btnDecline = new JButton("Decline");
		btnDecline.setBackground(Color.LIGHT_GRAY);
		btnDecline.setFont(new Font("Segoe UI", Font.BOLD, 14));
		btnDecline.setMinimumSize(new Dimension(150, 28));
		
		btnAccept = new JButton("Accept");
		btnAccept.setBackground(Color.LIGHT_GRAY);
		btnAccept.setFont(new Font("Segoe UI", Font.BOLD, 14));
		btnAccept.setMinimumSize(new Dimension(150, 28));

		if (mandatoryChoice) {
			getContentPane().add(btnDecline, "cell 0 2,alignx center,growy");
			getContentPane().add(btnAccept, "cell 1 2,alignx center,growy");
		}
		
		addHandlers();
		
		loadFile();
		
		GuiUtils.centerComponent(this, DEFAULT_PREF_DIMENSION);
		setVisible(true);
	}
	
	public void addHandlers() {
		
		// default close operation(same as reset)
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if (mandatoryChoice) {
					askExitRejecting();
				} else {
					dispose();
				}
			}
			
		});
		
		btnAccept.addActionListener((e) -> {
			exitAccepting();
		});

		btnDecline.addActionListener((e) -> {
			askExitRejecting();
		});
	}
	
	private void addHtmlStyle() {
		
		HTMLEditorKit kit = new HTMLEditorKit();
        jEditorPane.setEditorKit(kit);
		
		// add some styles to the html
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body {color:#000; font-family:times; margin: 4px; }");
        styleSheet.addRule("h1 {color: blue;}");
        styleSheet.addRule("h2 {color: #ff0000;}");
        styleSheet.addRule("pre {font : 10px monaco; color : black; background-color : #fafafa; }");

        // create a document, set it on the jeditorpane, then add the html
        Document doc = kit.createDefaultDocument();
        jEditorPane.setDocument(doc);
	}
	
	private void loadFile() {
		String fileContent = "License not found at: " + sourceLicense;
		try {
			fileContent = FileWorker.readFileAsString(sourceLicense);
		} catch (Exception e) {
			dialogHelper.warn("License file not found!");
			licenseLoadedCorrectly = false;
		}
			
		jEditorPane.setText(fileContent);
		licenseLoadedCorrectly = !StringWorker.isEmpty(fileContent);
	}
	
	private void askExitRejecting() {
		if (dialogHelper.yesOrNo("Are you sure to exit license acceptance? It's required to proceed.", "Exit license acceptance?")) {
			userChoice = UserLicenseChoice.REJECTED;
			dispose();
		}
	}

	private void exitAccepting() {
		dialogHelper.info("License accepted! However, if you want to read it again you can find it at: " + sourceLicense, "License accepted");
		userChoice = UserLicenseChoice.ACCEPTED;
		dispose();
	}
	
	public static LightLicenseViewer getInstance(File sourceLicenseFile, String header, String title) {
		return instance == null ? new LightLicenseViewer(sourceLicenseFile, header, title) : instance;
	}
}
