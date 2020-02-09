package gui;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import dialogutils.ExtensionFileFilter;
import dialogutils.GenericFileChooserDialog;
import dialogutils.JOptionHelperExtended;
import files.CustomFileFilters;
import files.FileVarious;
import gui.selection.color.ColorSelector;
import gui.selection.color.om.ColorSelectionElement;
import utils.RsyntaxUtils;
import java.awt.Color;
import net.miginfocom.swing.MigLayout;
import om.SupportedFileTextEditor;
import utility.SafeLogger;
import utility.string.StringWorker;

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.JScrollPane;
import javax.swing.JButton;

public class SchemeManagerGui extends JFrame {
	private static final long serialVersionUID = 510759366291391824L;
	
	SafeLogger logger = new SafeLogger(SchemeManagerGui.class);

	public final static ExtensionFileFilter[] DEFAULT_SAVE_FILTER = new ExtensionFileFilter[] {new ExtensionFileFilter(new String[] {"ini"}, "Configuration file (.ini)")};

	public static SchemeManagerGui singletonInstance;
	
	public static Color defaultElementsBackground = Color.WHITE;
	public static Font DEFAULT_BUTTON_FONT = new Font("Tahoma", Font.PLAIN, 16);
	
	public Dimension defaultDimension = new Dimension(1400, 750);
	
	private JOptionHelperExtended dialogHelper;
	private GenericFileChooserDialog fileChooserHelper;

	private File styleFilesFolder;
	public Font defaultTextFont;
	public List<ColorStyleScheme> schemes;
	
	public RTextScrollPane scrollPane;
	public RSyntaxTextArea testTextArea;
	
	private JLabel lblSyntax;
	private JLabel lblStyle;
	
	private JComboBox<SupportedFileTextEditor> comboSyntax;
	private JComboBox<ColorStyleScheme> comboScheme;
	
	private JScrollPane scrollPaneElements;
	private JPanel panelElements;
	private ColorSelector[] tokenSelectors;
	private JPanel panel;
	private JButton btnReloadFromFile;
	
	private JButton btnSaveAs;
	private JButton btnSaveTheme;
	private JButton btnExportTheme;
	
	// TODO link to main application
	
	public SchemeManagerGui(File sourceFolder, Font currentFont) {
		singletonInstance = this;
		
		styleFilesFolder = sourceFolder;
		defaultTextFont = currentFont != null ? currentFont.deriveFont(16f) : new Font("Segoe UI", Font.PLAIN, 16);
		
		dialogHelper = new JOptionHelperExtended(this);
		fileChooserHelper = new GenericFileChooserDialog(null);
		
		schemes = retrieveXmlSchemes();
		
		getContentPane().setBackground(Color.LIGHT_GRAY);
		getContentPane().setLayout(new MigLayout("", "[60px:n:70px,grow][550px:n:550px][grow][59.00][grow]", "[35px:n:35px][5px:n][:83%:85%,grow][-112.00px:n][8%,grow]"));
		
		lblStyle = new JLabel("Style");
		lblStyle.setFont(new Font("Segoe UI", Font.BOLD, 15));
		getContentPane().add(lblStyle, "cell 0 0,alignx center");
		
		comboScheme = new JComboBox<ColorStyleScheme>();
		comboScheme.setCursor(GuiUtils.CURSOR_HAND);
		comboScheme.setFont(DEFAULT_BUTTON_FONT);
		getContentPane().add(comboScheme, "cell 1 0 2 1,grow");
		
		lblSyntax = new JLabel("Syntax");
		lblSyntax.setFont(new Font("Segoe UI", Font.BOLD, 15));
		getContentPane().add(lblSyntax, "cell 3 0,alignx center");
		
		comboSyntax = new JComboBox<SupportedFileTextEditor>();
		comboSyntax.setCursor(GuiUtils.CURSOR_HAND);
		comboSyntax.setFont(DEFAULT_BUTTON_FONT);
		getContentPane().add(comboSyntax, "cell 4 0,grow");
		
		JSeparator separator = new JSeparator();
		separator.setBackground(Color.DARK_GRAY);
		getContentPane().add(separator, "cell 0 1 5 1,growx");
		
		scrollPaneElements = new JScrollPane();
		scrollPaneElements.getVerticalScrollBar().setUnitIncrement(25);
		scrollPaneElements.getHorizontalScrollBar().setUnitIncrement(25);
		scrollPaneElements.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		scrollPaneElements.setBackground(defaultElementsBackground);
		getContentPane().add(scrollPaneElements, "cell 0 2 2 1,grow");
		
		panelElements = new JPanel(new GridLayout(TokensStyleHelp.schemeTokensNumber, 1));
		panelElements.setBackground(defaultElementsBackground);
		scrollPaneElements.setViewportView(panelElements);
		
		testTextArea = new RSyntaxTextArea();
		scrollPane = new RTextScrollPane();
		scrollPane.setViewportView(testTextArea);
		getContentPane().add(scrollPane, "cell 2 2 3 1,grow");
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBackground(Color.DARK_GRAY);
		getContentPane().add(separator_1, "cell 0 3 5 1,growx");
		
		panel = new JPanel();
		panel.setBackground(Color.GRAY);
		getContentPane().add(panel, "cell 0 4 5 1,grow");
		panel.setLayout(new MigLayout("", "[][grow][][][]", "[grow]"));
		
		btnReloadFromFile = new JButton("Reload From File");
		btnReloadFromFile.setCursor(GuiUtils.CURSOR_HAND);
		btnReloadFromFile.setFont(DEFAULT_BUTTON_FONT);
		
		panel.add(btnReloadFromFile, "cell 0 0");
		
		btnSaveTheme = new JButton("Save current");
		btnSaveTheme.setCursor(GuiUtils.CURSOR_HAND);
		btnSaveTheme.setFont(DEFAULT_BUTTON_FONT);
		panel.add(btnSaveTheme, "cell 2 0");
		
		btnSaveAs = new JButton("Save as...");
		btnSaveAs.setCursor(GuiUtils.CURSOR_HAND);
		btnSaveAs.setFont(DEFAULT_BUTTON_FONT);
		panel.add(btnSaveAs, "cell 3 0");
		
		btnExportTheme = new JButton("Export Theme");
		btnExportTheme.setCursor(GuiUtils.CURSOR_HAND);
		btnExportTheme.setFont(DEFAULT_BUTTON_FONT);
		panel.add(btnExportTheme, "cell 4 0");

		addHandlers();
		
		SwingUtilities.invokeLater(() -> {
			initComboScheme();
			initComboSyntax();
			
			try {
				initGuiFromTheme();
			} catch (Exception e) {
				logger.error("Cannot initialize gui from selected scheme", e);
			}
		});
		
		setTitle("Scheme Manager");
		setMinimumSize(defaultDimension);
		setPreferredSize(defaultDimension);

		GuiUtils.centerComponent(this);
		
		setVisible(true);
	}
	
	private void addHandlers() {
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	
		    	String message = "All unsaved modifications will be lost, are you sure to close theme manager?";
		    	
		    	if (dialogHelper.yesOrNo(message, "Close theme manager?")) {
		    		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					singletonInstance = null;
					dispose();
				
		    	} else {
		    		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				}
		    }
		});
		
		comboScheme.addActionListener((e) -> {
			try {
				testTextArea.setCursor(GuiUtils.CURSOR_WAIT);
				getSelectedTheme(false).apply(testTextArea);
				initGuiFromTheme();
			} catch (Exception e1) {
				dialogHelper.error("Cannot load theme, an error occurred");
				logger.error("Cannot load style into text area!", e1);
				
			} finally {
				testTextArea.setCursor(GuiUtils.CURSOR_TEXT);
			}
		});
		
		comboSyntax.addActionListener((e) -> {
			try {
				testTextArea.setCursor(GuiUtils.CURSOR_WAIT);
				SupportedFileTextEditor selected = (SupportedFileTextEditor)comboSyntax.getSelectedItem();
				testTextArea.setSyntaxEditingStyle(selected.syntax);
				initGuiFromTheme();
				testTextArea.setText(SampleFileReader.getFileTextFromSyntax(selected, SampleFileReader.defaultSamplesFolder));
			} catch (Exception e1) {
				dialogHelper.error("Cannot load syntax scheme, an error occurred");
				logger.error("Cannot load syntax into text area!", e1);
			
			} finally {
				testTextArea.setCursor(GuiUtils.CURSOR_TEXT);
			}
		});
		
		btnReloadFromFile.addActionListener((e) -> {
			try {
				getSelectedTheme(true).apply(testTextArea);
				initGuiFromTheme();
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				dialogHelper.error("Cannot load theme, an error occurred");
			}
		});
		
		btnSaveTheme.addActionListener((e) -> {
			if (dialogHelper.yesOrNo("Overwrite " + getSelectedColorSchemeObj(false) + " ?")) {
				saveCurrentTheme(true, styleFilesFolder);
			}
		});

		btnSaveAs.addActionListener((e) -> {
			saveCurrentThemeAs();
		});
	}
	
	public void updateIcons(ImageIcon frameIcon, ImageIcon reloadIcon, ImageIcon saveIcon, ImageIcon exportIcon) {
		
		if(frameIcon != null) {
			setIconImage(frameIcon.getImage());
		}
		
		btnReloadFromFile.setIcon(reloadIcon);
		btnSaveTheme.setIcon(saveIcon);
		btnSaveAs.setIcon(exportIcon);
		
		btnReloadFromFile.repaint();
		btnSaveTheme.repaint();
		btnSaveAs.repaint();
	}
	
	public Theme getSelectedTheme(boolean loadFromFile) {
		if (loadFromFile) {
			return getSelectedColorSchemeObj(loadFromFile).loadThemeFromFile();
		} else {
			return getSelectedColorSchemeObj(loadFromFile).theme;
		}
	}

	public ColorStyleScheme getSelectedColorSchemeObj(boolean loadFromFile) {
		if (loadFromFile) {
			return schemes.get(comboScheme.getSelectedIndex());
		} else {
			return schemes.get(comboScheme.getSelectedIndex());
		}
	}
	
	public void initComboScheme() {
		comboScheme.setModel(new DefaultComboBoxModel(schemes.toArray()));
		comboScheme.setSelectedIndex(0);
	}
	
	public void initComboSyntax() {
		comboSyntax.setModel(new DefaultComboBoxModel(SupportedFileTextEditor.values()));
		comboSyntax.setSelectedIndex(0);
	}
	
	private void initGuiFromTheme() throws IllegalArgumentException, IllegalAccessException {
		
		panelElements.removeAll();
		tokenSelectors = TokensStyleHelp.getColorSelectors(getSelectedTheme(false), testTextArea, defaultElementsBackground);
		
		SwingUtilities.invokeLater(()->{
			
			for(ColorSelector selector : tokenSelectors) {
				panelElements.add(selector);
			}
		
			scrollPane.revalidate();
			scrollPane.repaint();
			panelElements.revalidate();
			panelElements.repaint();
			testTextArea.repaint();
			
			testTextArea.setMargin(new Insets(8,8,8,8));
			RsyntaxUtils.setFont(testTextArea, defaultTextFont);
		});
	}
	
	public void saveCurrentThemeAs() {
		File uniqueFileOut = FileVarious.renameJavaObjFile(getSelectedColorSchemeObj(false).schemeSource);
		String outName = dialogHelper.askForStringNullable("Specify theme file name", "Insert theme name", uniqueFileOut.getName());
		
		if (StringWorker.notEmpty(outName)) {
			File outFile = new File(styleFilesFolder + File.separator + outName);
			outFile = FileVarious.changeExtension(outFile, ".xml");
			saveCurrentTheme(true, outFile, styleFilesFolder);
			schemes = retrieveXmlSchemes();
			initComboScheme();
		
		} else if(outName != null){
			dialogHelper.warn("Invalid name, theme not saved!");
		}
	}
	
	public void saveCurrentTheme(boolean sameLocation, File defaultParentDir) {
		saveCurrentTheme(sameLocation, null, defaultParentDir);
	}
	
	@SuppressWarnings("unused")
	public void saveCurrentTheme(boolean sameLocation, File outputFile, File defaultParentDir) {
		
		ColorStyleScheme loadedSchemeObj = getSelectedColorSchemeObj(false);
		FileOutputStream outStream = null;
		
		File outFile = outputFile;
		try {
			if(sameLocation) {
				outFile = outFile == null
						? loadedSchemeObj != null ? loadedSchemeObj.schemeSource : null
						: outFile;
				
				outStream = new FileOutputStream(outFile);
			} else {
				outFile = outFile == null 
						? fileChooserHelper.fileWrite(this, loadedSchemeObj.schemeSource.getName(), defaultParentDir, DEFAULT_SAVE_FILTER)
						: outFile;
				
				if(outFile == null){
					return;
				} 
				
				if(outFile.exists() && !dialogHelper.askOverwriteFile(outFile)) {
					outFile = FileVarious.renameJavaObjFile(outFile);
				}
				outStream = new FileOutputStream(outFile);
			}

			loadedSchemeObj.theme.save(outStream);
			dialogHelper.info(outFile.getName() + " saved!");
		
		} catch (Exception e) {
			dialogHelper.info(GuiUtils.encapsulateInHtml(
					"An error occurred: cannot save theme to file.<br>" + loadedSchemeObj));
			logger.error("Cannot save theme: " + loadedSchemeObj.schemeSource, e);
		
		} finally {
			try {
				outStream.close();
			} catch (Exception e) {}
		}
	}
	
	public List<ColorStyleScheme> retrieveXmlSchemes() {
		
		ArrayList<ColorStyleScheme> schemes = new ArrayList<>();
		
		if(styleFilesFolder != null && styleFilesFolder.exists() && styleFilesFolder.isDirectory()) {
			
			File[] xmlSchemes = styleFilesFolder.listFiles(
					CustomFileFilters.customTypeFilter(CustomFileFilters.FILE_ONLY, "xml", false));
			
			for(File scheme : xmlSchemes) {
				schemes.add(new ColorStyleScheme(scheme));
			}
		}
		
		return schemes;
	}
	
	public Dimension getDefaultDimension() {
		return defaultDimension;
	}

	public void setDefaultDimension(Dimension defaultDimension) {
		this.defaultDimension = defaultDimension;
	}

	public File getStyleFilesFolder() {
		return styleFilesFolder;
	}

	public void setStyleFilesFolder(File styleFilesFolder) {
		this.styleFilesFolder = styleFilesFolder;
	}

	public List<ColorStyleScheme> getSchemes() {
		return schemes;
	}

	public void setSchemes(List<ColorStyleScheme> styles) {
		this.schemes = styles;
	}
	
	public class ColorStyleScheme {
		public List<ColorSelectionElement> schemeElements;
		public File schemeSource;
		public Theme theme;
		
		public ColorStyleScheme(File source) {
			schemeSource = source;
			theme = loadThemeFromFile();
		}
		
		public Theme loadThemeFromFile() {
			return RsyntaxUtils.readAndLoadThemeFromXmlFile(null, schemeSource);
		}

		@Override
		public String toString() {
			return schemeSource != null ? schemeSource.getName() : "Unsaved_theme.xml";
		}
	}
	
	public static SchemeManagerGui getInstance(File sourceFolder, Font currentFont) {
		return singletonInstance == null ? new SchemeManagerGui(sourceFolder, currentFont) : singletonInstance;
	}
}
