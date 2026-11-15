package gui.texteditor.commons;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.RTextScrollPane;

import gui.commons.dialogutils.ExtensionFileFilter;
import gui.commons.dialogutils.GenericFileChooserDialog;
import gui.commons.dialogutils.JOptionHelperExtended;
import gui.commons.frameutils.frame.arch.ToolStyle;
import gui.texteditor.commons.om.SupportedFileTextEditor;
import gui.texteditor.commons.utils.RsyntaxUtils;
import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import various.common.light.files.CustomFileFilters;
import various.common.light.files.FileVarious;
import various.common.light.files.FileWorker;
import various.common.light.gui.GuiUtils;
import various.common.light.gui.dialogs.color.om.ColorSelectionElement;
import various.common.light.utility.log.SafeLogger;
import various.common.light.utility.string.StringWorker;
import various.common.light.utility.xml.XmlWorker;
import various.common.light.validation.arch.DynaObjectValidator;
import various.common.light.validation.arch.DynaValidatorException;
import various.common.light.validation.arch.model.DynaChecker;

/**
 * Theme editor: every color and style of an editor theme with a live preview.
 * Left the list of elements (searchable, grouped), right the preview; click a piece of code in the preview to jump to its element.
 */
public class SchemeManagerGui extends JFrame {
	private static final long serialVersionUID = 510759366291391824L;

	SafeLogger logger = new SafeLogger(SchemeManagerGui.class);

	public final static ExtensionFileFilter[] DEFAULT_SAVE_FILTER = new ExtensionFileFilter[] {new ExtensionFileFilter(new String[] {"ini"}, "Configuration file (.ini)")};

	public static SchemeManagerGui singletonInstance;

	public static File defaultExportDir = new File(System.getProperty("user.home"));

	public static Color defaultElementsBackground = Color.WHITE;
	public static Font DEFAULT_BUTTON_FONT = new Font("Tahoma", Font.PLAIN, 16);

	public Dimension defaultDimension = new Dimension(1300, 780);

	private JOptionHelperExtended dialogHelper;
	private GenericFileChooserDialog fileChooserHelper;

	private File styleFilesFolder;
	private File defaultUnmodificableThemes;
	public Font defaultTextFont;
	public List<ColorStyleScheme> schemes;

	public RTextScrollPane scrollPane;
	public RSyntaxTextArea testTextArea;

	private JComboBox<SupportedFileTextEditor> comboSyntax;
	private JComboBox<ColorStyleScheme> comboScheme;

	private JPanel listPanel;
	private JScrollPane scrollPaneElements;
	private JTextField txtFilter;
	private JLabel lblStatus;
	private JLabel lblCount;
	private final List<ThemeRow> rows = new ArrayList<ThemeRow>();
	private final List<JLabel> sectionHeaders = new ArrayList<JLabel>();
	private final Map<Integer, ThemeRow> tokenRows = new LinkedHashMap<Integer, ThemeRow>();
	private ThemeRow selectedRow;

	private JButton btnReloadFromFile;
	private JButton btnImportTheme;
	private JButton btnSaveAs;
	private JButton btnSaveTheme;
	private JButton btnExportTheme;
	private JButton btnOpenThemeFolder;

	/** Colors or styles changed and not saved yet */
	private boolean modified = false;
	/** true while the editor itself changes the combos (no questions, no reload) */
	private boolean adjusting = false;
	private int lastSchemeIndex = 0;

	private Runnable closeAction = () -> {};

	public SchemeManagerGui(File sourceFolder, File defaultUnmodificableThemes, Font currentFont) {
		singletonInstance = this;

		styleFilesFolder = sourceFolder;
		this.defaultUnmodificableThemes = defaultUnmodificableThemes;
		defaultTextFont = currentFont != null ? currentFont.deriveFont(16f) : new Font("Segoe UI", Font.PLAIN, 16);

		dialogHelper = new JOptionHelperExtended(this);
		fileChooserHelper = new GenericFileChooserDialog(null);

		schemes = retrieveXmlSchemes();

		getContentPane().setLayout(new MigLayout("insets 0, gap 0, fill", "[grow,fill]", "[][grow,fill][]"));

		// ---- top bar: theme, buttons
		JPanel top = new JPanel(new MigLayout("insets 10 12 8 12, gapx 8, fillx", "[][grow,fill,200:350:600][][][]push[][][]", "[]"));
		JLabel lblStyle = new JLabel("Theme");
		lblStyle.setFont(lblStyle.getFont().deriveFont(Font.BOLD));
		top.add(lblStyle);

		comboScheme = new JComboBox<ColorStyleScheme>();
		ToolStyle.hand(comboScheme);
		comboScheme.setToolTipText("Theme being edited. Default themes can be edited too: use Save as... to keep your version");
		top.add(comboScheme, "wmin 0");

		btnReloadFromFile = ToolStyle.button("Reload", null, "Discard the changes and read the theme from file again");
		top.add(btnReloadFromFile);
		btnImportTheme = ToolStyle.button("Import", null, "Copy a theme .xml file into your themes");
		top.add(btnImportTheme);
		btnOpenThemeFolder = ToolStyle.button("Folder", null, "Open the folder of your themes");
		top.add(btnOpenThemeFolder);

		btnSaveTheme = ToolStyle.button("Save", null, "Overwrite the current custom theme (Ctrl+S)");
		top.add(btnSaveTheme);
		btnSaveAs = ToolStyle.button("Save as...", null, "Save a copy as a new theme");
		top.add(btnSaveAs);
		btnExportTheme = ToolStyle.button("Export", null, "Save a copy anywhere on disk");
		top.add(btnExportTheme);
		getContentPane().add(top, "growx, wrap");

		// ---- left: filter + elements
		JPanel left = new JPanel(new MigLayout("insets 8 12 8 6, gap 0, fill", "[grow,fill]", "[][][grow,fill]"));
		txtFilter = new JTextField();
		txtFilter.putClientProperty("JTextField.placeholderText", "Search an element (comment, caret, gutter...)");
		txtFilter.setToolTipText("Filters the list below. Tip: click a piece of code in the preview to jump to its color");
		left.add(txtFilter, "growx, gapbottom 6, wrap");

		JPanel header = new JPanel(new MigLayout("insets 0 10 4 8, gap 6", "[grow,fill][46!,center][46!,center][56!,center]", "[]"));
		header.setOpaque(false);
		header.add(ToolStyle.mutedLabel("Element"));
		header.add(ToolStyle.mutedLabel("Text"));
		header.add(ToolStyle.mutedLabel("Fill"));
		header.add(ToolStyle.mutedLabel("Style"));
		left.add(header, "growx, wrap");

		listPanel = new JPanel(new MigLayout("insets 0, wrap 1, gap 0, hidemode 3", "[grow,fill]", ""));
		scrollPaneElements = new JScrollPane(listPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPaneElements.getVerticalScrollBar().setUnitIncrement(24);
		scrollPaneElements.setBorder(BorderFactory.createLineBorder(borderColor()));
		left.add(scrollPaneElements, "grow");

		// ---- right: syntax + size + preview
		JPanel right = new JPanel(new MigLayout("insets 8 6 8 12, gap 6, fill", "[][grow,fill,120:200:260][]push[][]", "[][grow,fill]"));
		right.add(new JLabel("Preview"));
		comboSyntax = new JComboBox<SupportedFileTextEditor>();
		ToolStyle.hand(comboSyntax);
		comboSyntax.setToolTipText("Language of the sample text");
		right.add(comboSyntax, "wmin 0");
		right.add(ToolStyle.mutedLabel("Size"), "gapleft 8");
		JSpinner spinSize = new JSpinner(new SpinnerNumberModel(defaultTextFont.getSize(), 8, 40, 1));
		spinSize.setToolTipText("Size of the sample text (only the preview)");
		right.add(spinSize, "w 60!, wrap");

		testTextArea = new RSyntaxTextArea();
		testTextArea.setEditable(true);
		testTextArea.setFont(defaultTextFont.deriveFont(Font.PLAIN, defaultTextFont.getSize2D()));
		scrollPane = new RTextScrollPane(testTextArea, true);
		scrollPane.setFoldIndicatorEnabled(true);
		testTextArea.setCodeFoldingEnabled(true);
		scrollPane.setBorder(BorderFactory.createLineBorder(borderColor()));
		right.add(scrollPane, "span 5, grow");
		spinSize.addChangeListener(e -> testTextArea.setFont(testTextArea.getFont().deriveFont(((Number) spinSize.getValue()).floatValue())));

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
		split.setResizeWeight(0.0);
		split.setDividerLocation(500);
		split.setContinuousLayout(true);
		split.setBorder(null);
		split.setOneTouchExpandable(false);
		getContentPane().add(split, "grow, wrap");

		// ---- status bar
		JPanel status = new JPanel(new MigLayout("insets 6 12 8 12, fillx", "[grow,fill][]", "[]"));
		lblStatus = new JLabel(" ");
		lblCount = ToolStyle.mutedLabel(" ");
		status.add(lblStatus, "wmin 0");
		status.add(lblCount);
		getContentPane().add(status, "growx");

		addHandlers();

		SwingUtilities.invokeLater(() -> {
			adjusting = true;
			initComboScheme();
			initComboSyntax();
			adjusting = false;

			try {
				getSelectedTheme(false).apply(testTextArea);
				testTextArea.setSyntaxEditingStyle(((SupportedFileTextEditor) comboSyntax.getSelectedItem()).syntax);
				testTextArea.setText(SampleFileReader.getFileTextFromSyntax((SupportedFileTextEditor) comboSyntax.getSelectedItem(), SampleFileReader.defaultSamplesFolder));
				testTextArea.setCaretPosition(0);
				initGuiFromTheme();
			} catch (Exception e) {
				logger.error("Cannot initialize gui from selected scheme", e);
			}
		});

		setTitle("Theme Editor");
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension size = new Dimension(Math.min(defaultDimension.width, (int) (screen.width * 0.92)), Math.min(defaultDimension.height, (int) (screen.height * 0.88)));
		setMinimumSize(new Dimension(Math.min(820, size.width), Math.min(520, size.height)));
		setPreferredSize(size);
		pack();
		setSize(size);

		GuiUtils.centerComponent(this);

		setVisible(true);
	}

	private static Color borderColor() {
		Color c = UIManager.getColor("Component.borderColor");
		return c != null ? c : Color.GRAY;
	}

	private void addHandlers() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	if (!modified || dialogHelper.yesOrNo("There are changes not saved: close the theme editor and lose them?", "Close theme editor?")) {
		    		singletonInstance = null;
		    		closeAction.run();
		    		dispose();
		    	}
		    }
		});

		ToolStyle.bindKey(this, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, various.common.light.gui.ShortcutKeys.MENU_DOWN_MASK), "saveTheme", () -> {
			if (btnSaveTheme.isEnabled()) {
				btnSaveTheme.doClick();
			}
		});

		txtFilter.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				applyFilter();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				applyFilter();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				applyFilter();
			}
		});

		comboScheme.addActionListener((e) -> {
			if (adjusting) {
				return;
			}
			int newIndex = comboScheme.getSelectedIndex();
			if (modified && !dialogHelper.yesOrNo("The changes of the current theme are not saved: switch theme and lose them?", "Unsaved changes")) {
				adjusting = true;
				comboScheme.setSelectedIndex(lastSchemeIndex);
				adjusting = false;
				return;
			}
			try {
				if (modified) {
					// discard: the cached theme still holds the edits
					schemes.get(lastSchemeIndex).theme = schemes.get(lastSchemeIndex).loadThemeFromFile();
				}
				lastSchemeIndex = newIndex;
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
			if (adjusting) {
				return;
			}
			try {
				testTextArea.setCursor(GuiUtils.CURSOR_WAIT);
				SupportedFileTextEditor selected = (SupportedFileTextEditor)comboSyntax.getSelectedItem();
				testTextArea.setSyntaxEditingStyle(selected.syntax);
				testTextArea.setText(SampleFileReader.getFileTextFromSyntax(selected, SampleFileReader.defaultSamplesFolder));
				testTextArea.setCaretPosition(0);
			} catch (Exception e1) {
				dialogHelper.error("Cannot load syntax scheme, an error occurred");
				logger.error("Cannot load syntax into text area!", e1);

			} finally {
				testTextArea.setCursor(GuiUtils.CURSOR_TEXT);
			}
		});

		// click a piece of code: select the element that colors it
		testTextArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					selectRowOfTokenAt(testTextArea.viewToModel(e.getPoint()));
				}
			}
		});

		btnReloadFromFile.addActionListener((e) -> {
			if(!modified || dialogHelper.yesOrNo("Are you sure to reload current theme? all unsaved changes will be lost")) {
				try {
					int previousIndex = comboScheme.getSelectedIndex();
					schemes = retrieveXmlSchemes();
					adjusting = true;
					initComboScheme();
					comboScheme.setSelectedIndex(Math.max(0, Math.min(previousIndex, schemes.size() - 1)));
					adjusting = false;
					lastSchemeIndex = comboScheme.getSelectedIndex();
					getSelectedTheme(true).apply(testTextArea);
					initGuiFromTheme();
				} catch (Exception e1) {
					adjusting = false;
					dialogHelper.error("Cannot load theme, an error occurred");
				}
			}
		});

		btnImportTheme.addActionListener((e)-> {
			File toImport = fileChooserHelper.fileRead(this.getContentPane(), new FileNameExtensionFilter("Configuration file (.xml)", "xml"));
			if (toImport == null) {
				return; // cancelled by user
			}
			try {
				validate(toImport);
				File destFile = new File(styleFilesFolder.getAbsolutePath() + File.separator + toImport.getName());
				File imported = FileWorker.copyFile(toImport, destFile, false);

				if(imported != null) {
					dialogHelper.info("Theme imported.", logger);
					try {
						int previousIndex = comboScheme.getSelectedIndex();
						schemes = retrieveXmlSchemes();
						adjusting = true;
						initComboScheme();
						comboScheme.setSelectedIndex(Math.max(0, Math.min(previousIndex, schemes.size() - 1)));
						adjusting = false;
						lastSchemeIndex = comboScheme.getSelectedIndex();
						getSelectedTheme(true).apply(testTextArea);
						initGuiFromTheme();
					} catch (Exception e1) {
						adjusting = false;
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
				setModified(false);
			}
		});

		btnExportTheme.addActionListener((e) -> {
			saveCurrentTheme(false, defaultExportDir);
		});

		btnSaveAs.addActionListener((e) -> {
			saveCurrentThemeAs(getSelectedColorSchemeObj().modifiable);
			setModified(false);
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
			ColorStyleScheme current = getSelectedColorSchemeObj();
			current.theme = current.loadThemeFromFile();
			return current.theme;
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

	private void setModified(boolean value) {
		modified = value;
		updateStatus();
	}

	private void updateStatus() {
		boolean custom = comboScheme.getSelectedIndex() >= 0 && schemes != null && !schemes.isEmpty() && getSelectedColorSchemeObj().modifiable;
		btnSaveTheme.setEnabled(custom && modified);
		String state = custom ? "Custom theme" : "Default theme (read-only: Save as... keeps your version)";
		lblStatus.setText(state + (modified ? "   -   unsaved changes" : ""));
		lblStatus.setForeground(modified ? ToolStyle.ORANGE : ToolStyle.muted());
		setTitle("Theme Editor" + (modified ? " *" : ""));
	}

	private void initGuiFromTheme() {
		Theme theme = getSelectedTheme(false);
		List<ThemeEntry> entries = ThemeEntries.build(theme, testTextArea);

		listPanel.removeAll();
		rows.clear();
		sectionHeaders.clear();
		tokenRows.clear();
		selectedRow = null;

		String currentSection = null;
		for (ThemeEntry entry : entries) {
			if (!entry.section.equals(currentSection)) {
				currentSection = entry.section;
				JLabel header = new JLabel(currentSection.toUpperCase());
				header.setFont(header.getFont().deriveFont(Font.BOLD, header.getFont().getSize2D() - 1f));
				header.setForeground(ToolStyle.muted());
				header.setBorder(BorderFactory.createEmptyBorder(12, 10, 4, 8));
				header.putClientProperty("section", currentSection);
				sectionHeaders.add(header);
				listPanel.add(header, "growx");
			}
			final Runnable push = entry.onChange;
			entry.onChange = () -> {
				push.run();
				setModified(true);
			};
			ThemeRow row = new ThemeRow(entry);
			rows.add(row);
			if (entry.tokenType >= 0) {
				tokenRows.put(entry.tokenType, row);
			}
			listPanel.add(row, "growx");
		}

		setModified(false);
		applyFilter();

		SwingUtilities.invokeLater(() -> {
			testTextArea.setMargin(new Insets(8,8,8,8));
			listPanel.revalidate();
			listPanel.repaint();
			scrollPane.revalidate();
			scrollPane.repaint();
			testTextArea.repaint();
			scrollPaneElements.getVerticalScrollBar().setValue(0);
		});
	}

	private void applyFilter() {
		String filter = txtFilter.getText() == null ? "" : txtFilter.getText().trim().toLowerCase();
		int visible = 0;
		Map<String, Boolean> sectionHasVisible = new LinkedHashMap<String, Boolean>();
		for (ThemeRow row : rows) {
			boolean show = row.matches(filter);
			row.setVisible(show);
			if (show) {
				visible++;
				sectionHasVisible.put(row.entry.section, true);
			}
		}
		for (JLabel header : sectionHeaders) {
			header.setVisible(Boolean.TRUE.equals(sectionHasVisible.get((String) header.getClientProperty("section"))));
		}
		lblCount.setText(visible + " of " + rows.size() + " elements");
		listPanel.revalidate();
		listPanel.repaint();
	}

	/** Finds the syntax token under the offset of the preview and brings its row in view */
	private void selectRowOfTokenAt(int offset) {
		try {
			if (offset < 0 || !(testTextArea.getDocument() instanceof RSyntaxDocument)) {
				return;
			}
			int line = testTextArea.getLineOfOffset(offset);
			Token token = ((RSyntaxDocument) testTextArea.getDocument()).getTokenListForLine(line);
			while (token != null && token.isPaintable()) {
				if (token.containsPosition(offset)) {
					ThemeRow row = tokenRows.get(token.getType());
					if (row != null) {
						revealRow(row);
					}
					return;
				}
				token = token.getNextToken();
			}
		} catch (Exception e) {
			logger.warn("Cannot find the token under the click", e);
		}
	}

	private void revealRow(ThemeRow row) {
		if (!row.isVisible()) {
			txtFilter.setText("");
			applyFilter();
		}
		if (selectedRow != null) {
			selectedRow.setRowSelected(false);
		}
		selectedRow = row;
		row.setRowSelected(true);
		SwingUtilities.invokeLater(() -> {
			Rectangle bounds = SwingUtilities.convertRectangle(row.getParent(), row.getBounds(), listPanel);
			bounds.grow(0, 40);
			listPanel.scrollRectToVisible(bounds);
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
			dialogHelper.info(various.common.light.i18n.I18n.fmt("{0} saved!", outFile.getName()));

		} catch (Exception e) {
			dialogHelper.info(GuiUtils.encapsulateInHtml(
					various.common.light.i18n.I18n.fmt("An error occurred: cannot save theme to file.<br>{0}", loadedSchemeObj)));
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
