package gui.texteditor.commons;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import gui.commons.dialogutils.ExtensionFileFilter;
import gui.commons.dialogutils.GenericFileChooserDialog;
import gui.commons.dialogutils.JOptionHelperExtended;
import gui.texteditor.commons.om.SupportedFileTextEditor;
import gui.texteditor.commons.utils.RsyntaxUtils;
import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import various.common.light.files.CustomFileFilters;
import various.common.light.files.FileVarious;
import various.common.light.files.FileWorker;
import various.common.light.gui.GuiUtils;
import various.common.light.gui.dialogs.color.ColorSelector;
import various.common.light.gui.dialogs.color.om.ColorSelectionElement;
import various.common.light.utility.log.SafeLogger;
import various.common.light.utility.string.StringWorker;
import various.common.light.utility.xml.XmlWorker;
import various.common.light.validation.arch.DynaObjectValidator;
import various.common.light.validation.arch.DynaValidatorException;
import various.common.light.validation.arch.model.DynaChecker;

public class SchemeManagerGui extends JFrame {
	private static final long serialVersionUID = 510759366291391824L;

	SafeLogger logger = new SafeLogger(SchemeManagerGui.class);

	public final static ExtensionFileFilter[] DEFAULT_SAVE_FILTER = new ExtensionFileFilter[] {new ExtensionFileFilter(new String[] {"ini"}, "Configuration file (.ini)")};

	public static SchemeManagerGui singletonInstance;

	public static File defaultExportDir = new File(System.getProperty("user.home"));

	public static Color defaultElementsBackground = Color.WHITE;
	public static Font DEFAULT_BUTTON_FONT = new Font("Tahoma", Font.PLAIN, 16);

	public Dimension defaultDimension = new Dimension(1400, 800);

	private JOptionHelperExtended dialogHelper;
	private GenericFileChooserDialog fileChooserHelper;

	private File styleFilesFolder;
	private File defaultUnmodificableThemes;
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
	private JButton btnImportTheme;

	private JButton btnSaveAs;
	private JButton btnSaveTheme;
	private JButton btnExportTheme;
	private JButton btnOpenThemeFolder;

	private Runnable closeAction = () -> {};

	public SchemeManagerGui(File sourceFolder, File defaultUnmodificableThemes, Font currentFont) {
		singletonInstance = this;

		styleFilesFolder = sourceFolder;
		this.defaultUnmodificableThemes = defaultUnmodificableThemes;
		defaultTextFont = currentFont != null ? currentFont.deriveFont(16f) : new Font("Segoe UI", Font.PLAIN, 16);

		dialogHelper = new JOptionHelperExtended(this);
		fileChooserHelper = new GenericFileChooserDialog(null);

		schemes = retrieveXmlSchemes();

		getContentPane().setBackground(Color.LIGHT_GRAY);
		getContentPane().setLayout(new MigLayout("", "[220px:n:250px,grow][550px:n:550px][grow][59.00][grow]", "[35px:n:35px][5px:n][:83%:85%,grow][-112.00px:n][8%,grow]"));

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
		testTextArea.setEditable(true);
		scrollPane = new RTextScrollPane();
		scrollPane.setViewportView(testTextArea);
		getContentPane().add(scrollPane, "cell 2 2 3 1,grow");

		JSeparator separator_1 = new JSeparator();
		separator_1.setBackground(Color.DARK_GRAY);
		getContentPane().add(separator_1, "cell 0 3 5 1,growx");

		panel = new JPanel();
		panel.setBackground(Color.GRAY);
		getContentPane().add(panel, "cell 0 4 5 1,grow");
		panel.setLayout(new MigLayout("", "[][][grow][][][]", "[grow]"));

		btnReloadFromFile = new JButton("Reload From File");
		btnReloadFromFile.setCursor(GuiUtils.CURSOR_HAND);
		btnReloadFromFile.setFont(DEFAULT_BUTTON_FONT);
		panel.add(btnReloadFromFile, "cell 0 0");

		btnImportTheme = new JButton("Import Theme");
		btnImportTheme.setCursor(GuiUtils.CURSOR_HAND);
		btnImportTheme.setFont(DEFAULT_BUTTON_FONT);
		panel.add(btnImportTheme, "cell 1 0");

		btnOpenThemeFolder = new JButton("Open theme folder");
		btnOpenThemeFolder.setCursor(GuiUtils.CURSOR_HAND);
		btnOpenThemeFolder.setFont(DEFAULT_BUTTON_FONT);
		panel.add(btnOpenThemeFolder, "cell 2 0,alignx left");

		btnSaveTheme = new JButton("Save current");
		btnSaveTheme.setCursor(GuiUtils.CURSOR_HAND);
		btnSaveTheme.setFont(DEFAULT_BUTTON_FONT);
		panel.add(btnSaveTheme, "cell 3 0");

		btnSaveAs = new JButton("Save as...");
		btnSaveAs.setCursor(GuiUtils.CURSOR_HAND);
		btnSaveAs.setFont(DEFAULT_BUTTON_FONT);
		panel.add(btnSaveAs, "cell 4 0");

		// TODO: btnImportTheme

		btnExportTheme = new JButton("Export Theme");
		btnExportTheme.setCursor(GuiUtils.CURSOR_HAND);
		btnExportTheme.setFont(DEFAULT_BUTTON_FONT);
		panel.add(btnExportTheme, "cell 5 0");

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
					closeAction.run();
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
			if(!getSelectedColorSchemeObj().modifiable ||
					dialogHelper.yesOrNo("Are you sure to reload current theme? all unsaved changes will be lost")) {
				try {
					int previousIndex = comboScheme.getSelectedIndex();
					schemes = retrieveXmlSchemes();
					initComboScheme();
					getSelectedTheme(true).apply(testTextArea);
					initGuiFromTheme();

					comboScheme.setSelectedIndex(previousIndex);
				} catch (IllegalArgumentException | IllegalAccessException e1) {
					dialogHelper.error("Cannot load theme, an error occurred");
				}
			}
		});

		btnImportTheme.addActionListener((e)-> {
			File toImport = fileChooserHelper.fileRead(this.getContentPane(), new FileNameExtensionFilter("Configuration file (.xml)", "xml"));
			try {
				validate(toImport);
				File destFile = new File(styleFilesFolder.getAbsolutePath() + File.separator + toImport.getName());
				File imported = FileWorker.copyFile(toImport, destFile, false);

				if(imported != null) {
					dialogHelper.info("Theme imported.", logger);
					try {
						int previousIndex = comboScheme.getSelectedIndex();
						schemes = retrieveXmlSchemes();
						initComboScheme();
						getSelectedTheme(true).apply(testTextArea);
						initGuiFromTheme();

						comboScheme.setSelectedIndex(previousIndex);
					} catch (IllegalArgumentException | IllegalAccessException e1) {
						dialogHelper.error("Cannot load theme, an error occurred");
					}
				} else {
					dialogHelper.error("Theme import failed! (See logs for details)", logger);
				}

			} catch (DynaValidatorException e1) {
				dialogHelper.error("Cannot import: invalid theme", logger, e1);
			}
		});

		btnOpenThemeFolder.addActionListener((e) -> {
			GuiUtils.openInFileSystem(new File(GeneralConfig.THEMES_FOLDER_CUSTOM));
		});

		btnSaveTheme.addActionListener((e) -> {
			if (dialogHelper.yesOrNo("Overwrite " + getSelectedColorSchemeObj() + " ?")) {
				saveCurrentTheme(true, styleFilesFolder);
			}
		});

		btnExportTheme.addActionListener((e) -> {
			saveCurrentTheme(false, defaultExportDir);
		});

		btnSaveAs.addActionListener((e) -> {
			saveCurrentThemeAs(getSelectedColorSchemeObj().modifiable);
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
			return getSelectedColorSchemeObj().loadThemeFromFile();
		} else {
			return getSelectedColorSchemeObj().theme;
		}
	}

	public ColorStyleScheme getSelectedColorSchemeObj() {
		return schemes.get(comboScheme.getSelectedIndex());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void initComboScheme() {
		comboScheme.setModel(new DefaultComboBoxModel(schemes.toArray()));
		comboScheme.setSelectedIndex(0);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void initComboSyntax() {
		comboSyntax.setModel(new DefaultComboBoxModel(SupportedFileTextEditor.values()));
		comboSyntax.setSelectedItem(SupportedFileTextEditor.JAVA);
	}

	private void initGuiFromTheme() throws IllegalArgumentException, IllegalAccessException {

		panelElements.removeAll();
		tokenSelectors = TokensStyleHelp.getColorSelectors(getSelectedTheme(false), testTextArea, defaultElementsBackground);

		SwingUtilities.invokeLater(()->{

			boolean currentModifiable = getSelectedColorSchemeObj().modifiable;
			btnSaveTheme.setEnabled(currentModifiable);

			for(ColorSelector selector : tokenSelectors) {
				panelElements.add(selector);
				selector.setEnabled(currentModifiable);
			}

			scrollPane.revalidate();
			scrollPane.repaint();
			panelElements.revalidate();
			panelElements.repaint();
			testTextArea.repaint();


			testTextArea.setMargin(new Insets(8,8,8,8));
		});
	}

	public void saveCurrentThemeAs(boolean canOverwrite) {
		File uniqueFileOut = FileVarious.uniqueJavaObjFile(getSelectedColorSchemeObj().schemeSource);
		String outName = dialogHelper.askForStringNullable("Specify theme file name", "Insert theme name", uniqueFileOut.getName());

		if (StringWorker.notEmpty(outName)) {
			File outFile = new File(styleFilesFolder + File.separator + outName);
			outFile = FileVarious.changeExtension(outFile, ".xml");
			outFile = canOverwrite ? outFile : FileVarious.uniqueJavaObjFile(outFile);

			saveCurrentTheme(true, outFile, styleFilesFolder);

			schemes = retrieveXmlSchemes();
			initComboScheme();

			// Now set as current selected element in combo
			for(int i = 0; i < schemes.size(); i++) {
				ColorStyleScheme comboSchemeIterator = comboScheme.getItemAt(i);
				if(comboSchemeIterator.schemeSource != null && FileVarious.checkIfSameFile(outFile, comboSchemeIterator.schemeSource)){
					comboScheme.setSelectedIndex(i);
					break;
				}
			}

		} else if(outName != null){
			dialogHelper.warn("Invalid name, theme not saved!");
		}
	}

	private void validate(File document) throws DynaValidatorException {
		DynaObjectValidator<File> validator = getXmlThemeValidator();
		validator.validate(document);
	}

	private DynaObjectValidator<File> getXmlThemeValidator() {
		DynaObjectValidator<File> validator = new DynaObjectValidator<>(null);

		validator.addRuleChecker(new DynaChecker<File>(
			(toImport) -> {
				try {
					XmlWorker.loadXMLfromFile(toImport.getAbsolutePath());
					return true;
				} catch (Exception e) {
					logger.error("Invalid file to import as xml theme", e);
					return false;
				}
			}, (toImport) -> {
				return "Invalid file to import as xml theme";
			}));

		return validator;
	}

	public void saveCurrentTheme(boolean sameLocation, File defaultParentDir) {
		saveCurrentTheme(sameLocation, null, defaultParentDir);
	}

	public void saveCurrentTheme(boolean sameLocation, File outputFile, File defaultParentDir) {

		ColorStyleScheme loadedSchemeObj = getSelectedColorSchemeObj();
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

				if(outFile == null)
					return;

				outFile = FileVarious.changeExtension(outFile, ".xml");

				if(outFile.exists() && !dialogHelper.askOverwriteFile(outFile))
					outFile = FileVarious.uniqueJavaObjFile(outFile);

				outStream = new FileOutputStream(outFile);

				defaultExportDir = outFile.getParentFile();
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
				schemes.add(new ColorStyleScheme(scheme, true));
			}
		}

		if(defaultUnmodificableThemes != null && defaultUnmodificableThemes.exists() && defaultUnmodificableThemes.isDirectory()) {

			File[] xmlSchemes = defaultUnmodificableThemes.listFiles(
					CustomFileFilters.customTypeFilter(CustomFileFilters.FILE_ONLY, "xml", false));

			for(File scheme : xmlSchemes) {
				schemes.add(new ColorStyleScheme(scheme, false));
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

	public Runnable getCloseAction() {
		return closeAction;
	}

	public void setCloseAction(Runnable closeAction) {
		this.closeAction = closeAction;
	}

	public ColorStyleScheme getCurrentColorScheme() {
		return (ColorStyleScheme)comboScheme.getSelectedItem();
	}

	public class ColorStyleScheme {
		public List<ColorSelectionElement> schemeElements;
		public File schemeSource;
		public Theme theme;
		public boolean modifiable;

		public ColorStyleScheme(File source, boolean modifiable) {
			schemeSource = source;
			theme = loadThemeFromFile();
			this.modifiable = modifiable;
		}

		public Theme loadThemeFromFile() {
			return RsyntaxUtils.readAndLoadThemeFromXmlFile(null, schemeSource);
		}

		@Override
		public String toString() {
			String prefix = modifiable ? "Custom theme: " : "Default theme (unmodifiable): ";
			return schemeSource != null ? prefix + schemeSource.getName() : prefix + "Unsaved_theme.xml";
		}
	}

	public static SchemeManagerGui getInstance(File sourceFolder, File unmodifiableThemes, Font currentFont) {
		return singletonInstance == null ? new SchemeManagerGui(sourceFolder, unmodifiableThemes, currentFont) : singletonInstance;
	}
}
