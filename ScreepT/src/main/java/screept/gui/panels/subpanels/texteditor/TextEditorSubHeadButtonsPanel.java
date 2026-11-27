/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.gui.panels.subpanels.texteditor;

import various.common.light.i18n.I18n;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.UnaryOperator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.border.MatteBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.fife.rsta.ui.GoToDialog;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import com.minhasKamal.ultimateCalculator.CalculatorMainApp;

import gui.commons.dialogutils.JOptionHelperExtended;
import gui.commons.frameutils.frame.EscapeReplacerFrame;
import gui.commons.frameutils.frame.FilePropertiesFrame;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import gui.commons.guistyle.scale.UiScaleManager;
import gui.commons.textarea.om.TextLineNumber;
import gui.texteditor.commons.TextEditTooltips;
import gui.texteditor.commons.dialogs.FindDialogFork;
import gui.texteditor.commons.dialogs.ReplaceDialogFork;
import gui.texteditor.commons.om.SupportedFileTextEditor;
import gui.texteditor.commons.om.TabTextEditorObject;
import gui.texteditor.commons.om.themes.Themes;
import gui.texteditor.commons.om.themes.ThemesWrapper;
import gui.texteditor.commons.utils.RsyntaxUtils;
import various.common.light.files.om.FileNamed;
import gui.texteditor.commons.utils.panels.arch.PanelType;
import initializer.configs.impl.INItializer;
import initializer.configs.impl.om.TextEditorOption;
import maingUI.MainGUI;
import maingUI.MainGuiGeneralAppStarter;
import multimedia.conversion.Text2Speech;
import multimedia.conversion.gui.ImageSelector2TextFrame;
import multimedia.conversion.gui.PdfSelector2TextFrame;
import com.formdev.flatlaf.util.UIScale;
import os.commons.utils.SysUtils;
import resources.GeneralConfig;
import screept.gui.panels.quicknotes.QuickNotesDrawer;
import resources.IconsPathConfigurator;
import screept.gui.panels.TextEditor;
import screept.gui.panels.subpanels.texteditor.toolbar.EditorIcons;
import screept.gui.panels.subpanels.texteditor.toolbar.ScrollableToolbar;
import screept.gui.panels.subpanels.texteditor.toolbar.TextTransforms;
import screept.gui.panels.subpanels.texteditor.toolbar.ToolbarComponents;
import screept.gui.panels.subpanels.texteditor.toolbar.ToolbarStyle;
import screept.gui.utils.filedialog.TemplatesChooserFrame;
import screept.gui.utils.texteditor.TextEditorUtils;
import various.common.light.files.FileVarious;
import various.common.light.files.FileWorker;
import various.common.light.gui.GuiUtils;
import various.common.light.gui.RXTextUtilities;
import various.common.light.gui.dialogs.font.JFontChooserExtended;
import various.common.light.gui.dialogs.msg.JOptionHelper;
import various.common.light.gui.selection.AbstractSelector;
import various.common.light.om.SelectionDtoFull;
import various.common.light.utility.manipulation.EncodingUtils;
import various.common.light.utility.string.StringWorker;
import various.common.light.utility.string.StringWorker.EOL;

/**
 * This panel will contain all buttons to do actions into textEditor, in order to have small and separated module to use into GUI
 * @author Alessio Moraschini
 *
 */
public class TextEditorSubHeadButtonsPanel extends JPanel{
	private static final long serialVersionUID = 5481362170538925466L;

	// TextEditorSubHeadButtonsPanel logger creation
	static Logger logger = Logger.getLogger(TextEditorSubHeadButtonsPanel.class);

	// COLORS DEFAULT DEFINITIONS
	final Color DEFAULT_BCKG_COL;
	final Color DEFAULT_FORE_COL;

	// GENERAL FIELDS //
	public TextEditor parent;
	public TextEditorUtils textEditorUtils;
	public INItializer currentConfiguration;
	public MainGUI mainApplication;

	// SUB-HEADER COMPONENTS

	public JCheckBox chckbxShowTabGrid;
	public JCheckBox chckbxLockLanguage;

	public JLabel lblSIZE;
	public JLabel lblMargin;
	public JLabel lBLMargin;
	public JLabel lblFontSize;
	public JLabel labelTabSize;

	public JButton btnFindText;
	public JButton btnReplaceText;
	public JButton btnReplaceEscapesSeq;
	public JButton btnGoToLine;
	public JButton btntTextFormat;
	public JButton btnLoadTextFile;
	public JButton btnSaveTextFileAs;
	public JButton btnSaveTextFile;
	public JButton btnCloseAllTabs;
	public JButton btnReloadFromDisk;
	public JButton btnSpeak;
	public JButton btnOpenInScreepT;

	public JToggleButton btnTailMode;
	public JButton btnFullscreen;
	public JButton btnParseImageText;
	public JButton btnParsePdfText;
	public JButton btnFontChooser;
	public JButton btnTemplates;
	private JButton btnNextHistory;
	private JButton btnPrevHistory;
//	public JButton btnDefaultOpt;
	public JButton btnUndo;
	public JButton btnClear;
	public JButton btnRedo;
	public JButton btnQuickFileSearch;
	public JButton btnImpExpCryptedWorkspace;
	public JButton btnNewTab;
	public JButton btnRemoveSpaces;
	public JButton btnRemoveWord;
	public JButton btnViewInExplorer;
	public JButton btnViewInCmd;
	public JButton btnPropertiesPanel;
	public JButton btnCalculator;
	public JButton btnColorSampler;
	private JButton btnThemeEditor;
	public JButton btnBase64Conversion;

	public JToggleButton btnToggleFileView;
	public JToggleButton toggleBtnMap;
	public JToggleButton toggleBtnClipboard;
	public JToggleButton toggleBtnConsole;
	public JToggleButton toggleButtonSpellCheck;
	public JToggleButton toggleButtonQuickNotes;

	public JSpinner tabSizeSpinner;

	public JComboBox<SupportedFileTextEditor> comboFileFormat;
	public JComboBox<ThemesWrapper> comboStylesScheme;
	public Vector<ThemesWrapper> themeList = new Vector<ThemesWrapper>();

	public JSlider sliderFontSize;
	public JSlider slider_margin;

	// DIALOGS AND JFRAMES
	public FindDialogFork searchBox;
	public ReplaceDialogFork replaceDialog;
	public GoToDialog goToLineDialog;
	public TemplatesChooserFrame templatesPanel;
	public EscapeReplacerFrame escapeReplacerPanel;
	public ImageSelector2TextFrame imageParserBox;
	public PdfSelector2TextFrame pdfParserBox;
	public TextLineNumber lineNumberPanel;
	public FilePropertiesFrame propertiesFrame;
	public CalculatorMainApp calculatorFrame;
	public Thread calculatorMainThread;

	public Text2Speech objSpeaker; // field containing the current object used to speak text

	public JOptionHelperExtended dialogHelper;

	TextEditorSubHeadButtonsPanel thisHeaderRef;
	private JButton btnFileContentSearch;
	public JButton btnCopyPath;
	private ScrollableToolbar toolbar;
	/** Font size of the editor when the toolbar was built (the saved one): "Reset zoom" goes back to it */
	private int baseFontSize = 16;

	private String lastRemovedWord = "";

	/************************
	*	Main subHeader CONSTRUCTOR
	************************/
	public TextEditorSubHeadButtonsPanel(TextEditor parent) {
		logger.info(this.getClass().getName() + " Starting...");

		putClientProperty(UiScaleManager.CATEGORY_PROPERTY, UiScaleManager.Category.TOOLBAR);
		setBackground(Color.DARK_GRAY);

		thisHeaderRef = this;
		this.parent = parent;
		this.textEditorUtils = parent.textEditorUtils;
		this.currentConfiguration = parent.currentConfiguration;
		this.mainApplication = parent.mainApplication;
		dialogHelper = new JOptionHelperExtended(this.parent);

		DEFAULT_BCKG_COL = currentConfiguration.getTextEditorOpt().backCol;
		DEFAULT_FORE_COL = currentConfiguration.getTextEditorOpt().foreCol;

		// TOOLBAR: one row of groups inside a scroll pane (it scrolls horizontally when the window is narrow).
		// All the public buttons stay: shortcuts and other classes use them; the ones without a place in the row live in the menus.
		setBorder(new MatteBorder(0, 0, 1, 0, ToolbarStyle.LINE));
		setBackground(ToolbarStyle.BACKGROUND);
		setLayout(new BorderLayout());
		toolbar = new ScrollableToolbar();
		add(toolbar, BorderLayout.CENTER);
		final JPanel strip = toolbar.getStrip();

		// FILE
		btnNewTab = new ToolbarComponents.Button(EditorIcons.Kind.NEW_FILE, TextEditTooltips.tooltipNewEmptyTab);
		btnLoadTextFile = new ToolbarComponents.Button(EditorIcons.Kind.OPEN, TextEditTooltips.tooltipLoadTextFile);
		btnSaveTextFile = new ToolbarComponents.Button(EditorIcons.Kind.SAVE, TextEditTooltips.tooltipSaveTextFile);
		btnSaveTextFileAs = new ToolbarComponents.Button(EditorIcons.Kind.SAVE_AS, TextEditTooltips.tooltipSaveTextFileAs);
		btnReloadFromDisk = new ToolbarComponents.Button(EditorIcons.Kind.RELOAD, TextEditTooltips.tooltipReloadFromDisk);
		btnCloseAllTabs = new ToolbarComponents.Button(EditorIcons.Kind.CLOSE_ALL, TextEditTooltips.tooltipRemoveAllDocs);
		addGroup(strip, btnNewTab, btnLoadTextFile, btnSaveTextFile, btnSaveTextFileAs, btnReloadFromDisk, btnCloseAllTabs);

		// EDIT
		btnUndo = new ToolbarComponents.Button(EditorIcons.Kind.UNDO, TextEditTooltips.tooltipUndo);
		btnRedo = new ToolbarComponents.Button(EditorIcons.Kind.REDO, TextEditTooltips.tooltipRedo);
		btnPrevHistory = new ToolbarComponents.Button(EditorIcons.Kind.BACK, TextEditTooltips.tooltipHystoryPrev);
		btnNextHistory = new ToolbarComponents.Button(EditorIcons.Kind.FORWARD, TextEditTooltips.tooltipHystoryNext);
		addGroup(strip, btnUndo, btnRedo, btnPrevHistory, btnNextHistory);

		// FIND
		btnFindText = new ToolbarComponents.Button(EditorIcons.Kind.SEARCH, TextEditTooltips.tooltipFindText);
		btnReplaceText = new ToolbarComponents.Button(EditorIcons.Kind.REPLACE, TextEditTooltips.tooltipReplaceText);
		btnGoToLine = new ToolbarComponents.Button(EditorIcons.Kind.GOTO, TextEditTooltips.tooltipGoToLine);
		btnFileContentSearch = new ToolbarComponents.Button(EditorIcons.Kind.SEARCH_FILES, TextEditTooltips.tooltipFileContentSearch);
		btnQuickFileSearch = new ToolbarComponents.Button(EditorIcons.Kind.QUICK_OPEN, TextEditTooltips.tooltipQuickFileSearch);
		addGroup(strip, btnFindText, btnReplaceText, btnGoToLine, btnFileContentSearch, btnQuickFileSearch);

		// TEXT
		btntTextFormat = new ToolbarComponents.Button(EditorIcons.Kind.FORMAT, TextEditTooltips.tooltipFormatCode);
		btnTemplates = new ToolbarComponents.Button(EditorIcons.Kind.TEMPLATE, TextEditTooltips.tooltipTemplatesPanel);
		btnBase64Conversion = new ToolbarComponents.Button(EditorIcons.Kind.BASE64, I18n.t("app.encode_selected_all_text_to_base"));
		ToolbarComponents.MenuButton textMenu = new ToolbarComponents.MenuButton(EditorIcons.Kind.TEXT, I18n.t("toolbar.text"), I18n.t("toolbar.text_tip"), this::buildTextMenu);
		addGroup(strip, btntTextFormat, btnTemplates, btnBase64Conversion, textMenu);

		// PANELS (on/off)
		btnToggleFileView = new ToolbarComponents.Toggle(EditorIcons.Kind.SIDEBAR, TextEditTooltips.tooltipFileViewBtn);
		toggleBtnMap = new ToolbarComponents.Toggle(EditorIcons.Kind.MAP, TextEditTooltips.tooltipMapViewBtn);
		toggleBtnClipboard = new ToolbarComponents.Toggle(EditorIcons.Kind.CLIPBOARD, TextEditTooltips.tooltipClipboardViewBtn);
		toggleBtnConsole = new ToolbarComponents.Toggle(EditorIcons.Kind.TERMINAL, TextEditTooltips.tooltipConsoleView);
		toggleBtnConsole.setSelected(currentConfiguration.getTextEditorOpt().bottomViewActive);
		// QUICK NOTES drawer ON/OFF (same as Ctrl+Alt+N), the state follows the drawer whoever opens it
		toggleButtonQuickNotes = new ToolbarComponents.Toggle(EditorIcons.Kind.NOTE, I18n.t("app.show_hide_the_quick_notes_ctrl"));
		toggleButtonQuickNotes.addActionListener(e -> QuickNotesDrawer.getInstance(MainGUI.mainFrameScreept).toggle());
		QuickNotesDrawer.addOpenListener(open -> SwingUtilities.invokeLater(() -> toggleButtonQuickNotes.setSelected(open)));
		toggleButtonSpellCheck = new ToolbarComponents.Toggle(EditorIcons.Kind.SPELL, TextEditTooltips.tooltipSpellChecker);
		addGroup(strip, btnToggleFileView, toggleBtnMap, toggleBtnClipboard, toggleBtnConsole, toggleButtonQuickNotes, toggleButtonSpellCheck);

		// CURRENT FILE
		btnViewInExplorer = new ToolbarComponents.Button(EditorIcons.Kind.FOLDER_OPEN, TextEditTooltips.tooltipViewInSysExplorer);
		btnViewInCmd = new ToolbarComponents.Button(EditorIcons.Kind.TERMINAL, TextEditTooltips.tooltipViewInCommandLine);
		btnCopyPath = new ToolbarComponents.Button(EditorIcons.Kind.COPY, I18n.t("toolbar.copy_path_tip"));
		btnPropertiesPanel = new ToolbarComponents.Button(EditorIcons.Kind.INFO, TextEditTooltips.tooltipViewPropertiesCurrFile);
		// TAIL (eye): follows the file as it grows, on/off for the current tab
		btnTailMode = new ToolbarComponents.Toggle(EditorIcons.Kind.TAIL, TextEditTooltips.tooltipTailMode);
		// FULL SCREEN of the viewer in front (PDF, Markdown...): enabled only on those tabs, F11 does the same
		btnFullscreen = new ToolbarComponents.Button(EditorIcons.Kind.FULLSCREEN, I18n.t("toolbar.fullscreen_tip"));
		btnFullscreen.setEnabled(false);
		btnFullscreen.addActionListener(e -> toggleViewerFullscreen());
		addGroup(strip, btnViewInExplorer, btnViewInCmd, btnCopyPath, btnPropertiesPanel, btnTailMode, btnFullscreen);

		// TOOLS and VIEW menus
		ToolbarComponents.MenuButton toolsMenu = new ToolbarComponents.MenuButton(EditorIcons.Kind.TOOLS, I18n.t("toolbar.tools"), I18n.t("toolbar.tools_tip"), this::buildToolsMenu);
		ToolbarComponents.MenuButton viewMenu = new ToolbarComponents.MenuButton(EditorIcons.Kind.VIEW, I18n.t("toolbar.view"), I18n.t("toolbar.view_tip"), this::buildViewMenu);
		addGroup(strip, toolsMenu, viewMenu);

		// functions reached from the menus: they keep their button (listeners, shortcuts) but are not in the row
		btnClear = menuOnlyButton(TextEditTooltips.tooltipClearText);
		btnRemoveSpaces = menuOnlyButton(TextEditTooltips.tooltipRemoveSpaces);
		btnRemoveWord = menuOnlyButton(TextEditTooltips.tooltipRemoveContent);
		btnReplaceEscapesSeq = menuOnlyButton(TextEditTooltips.tooltipReplaceEscapes);
		btnSpeak = menuOnlyButton(TextEditTooltips.tooltipSpeak);
		btnParseImageText = menuOnlyButton(TextEditTooltips.tooltipParseImage);
		btnParsePdfText = menuOnlyButton(TextEditTooltips.tooltipParsePdfTxt);
		btnImpExpCryptedWorkspace = menuOnlyButton(TextEditTooltips.tooltipImpExpCryptedWorkspace);
		btnOpenInScreepT = menuOnlyButton(TextEditTooltips.tooltipOpenInScreept);
		btnCalculator = menuOnlyButton(TextEditTooltips.tooltipBtnCalculator);
		btnColorSampler = menuOnlyButton(TextEditTooltips.tooltipBtnColorSampler);
		btnFontChooser = menuOnlyButton(TextEditTooltips.tooltipFontChooser);
		// font is chosen in Graphic preferences (Text editor section)

		// glue: what follows stays on the right when there is room
		JPanel glue = new JPanel();
		glue.setOpaque(false);
		strip.add(glue, "pushx, growx, w 0:0:");

		// LANGUAGE and THEME
		comboFileFormat = new JComboBox<SupportedFileTextEditor>();
		comboFileFormat.setToolTipText(TextEditTooltips.tooltipComboFileFormat);
		comboFileFormat.setCursor(GuiUtilsExtended.CURSOR_HAND);
		comboFileFormat.setModel(new DefaultComboBoxModel<SupportedFileTextEditor>(SupportedFileTextEditor.values()));
		comboFileFormat.setFont(GeneralConfig.BUTTON_DEFAULT_FONT.deriveFont(10).deriveFont(Font.PLAIN));
		comboFileFormat.setMinimumSize(new Dimension(UIScale.scale(70), UIScale.scale(26)));
		comboFileFormat.setPreferredSize(new Dimension(UIScale.scale(112), UIScale.scale(26)));
		strip.add(comboFileFormat, "gapx 6 0, aligny center");

		chckbxLockLanguage = new JCheckBox(I18n.t("app.lock"));
		chckbxLockLanguage.setOpaque(false);
		chckbxLockLanguage.setToolTipText(TextEditTooltips.tooltipChckbxLockLanguage);
		chckbxLockLanguage.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxLockLanguage.setIconTextGap(2);
		chckbxLockLanguage.setForeground(ToolbarStyle.ICON);
		chckbxLockLanguage.setFont(chckbxLockLanguage.getFont().deriveFont(Font.PLAIN));
		chckbxLockLanguage.setFocusable(false);
		strip.add(chckbxLockLanguage, "gapx 2 4, aligny center");
		strip.add(new ToolbarComponents.Separator());

		comboStylesScheme = new JComboBox<ThemesWrapper>();
		comboStylesScheme.setMinimumSize(new Dimension(UIScale.scale(70), UIScale.scale(26)));
		comboStylesScheme.setPreferredSize(new Dimension(UIScale.scale(132), UIScale.scale(26)));
		initComboStyleScheme();
		comboStylesScheme.setToolTipText(TextEditTooltips.tooltipComboStyleScheme);
		comboStylesScheme.setCursor(GuiUtilsExtended.CURSOR_HAND);
		comboStylesScheme.setFont(GeneralConfig.BUTTON_DEFAULT_FONT.deriveFont(10).deriveFont(Font.PLAIN));
		strip.add(comboStylesScheme, "gapx 4 0, aligny center");

		btnThemeEditor = new ToolbarComponents.Button(EditorIcons.Kind.PALETTE, TextEditTooltips.tooltipThemeEditor);
		strip.add(btnThemeEditor);

		// STATE HOLDERS: tab size, tab grid, margin and font size are set from the View menu and from the graphic preferences
		labelTabSize = new JLabel(I18n.t("app.tab_size"));
		tabSizeSpinner = new JSpinner();
		tabSizeSpinner.setToolTipText(TextEditTooltips.tooltipTabSize);
		tabSizeSpinner.setModel(new SpinnerNumberModel(currentConfiguration.getTextEditorOpt().getTabSize(), 1, 9, 1));

		chckbxShowTabGrid = new JCheckBox(I18n.t("app.show_tabs"));
		chckbxShowTabGrid.setToolTipText(TextEditTooltips.tooltipShowTabGrid);
		chckbxShowTabGrid.setSelected(currentConfiguration.getTextEditorOpt().isTabLineVisible());

		lblMargin = new JLabel("Margin: ");
		lBLMargin = new JLabel("12");
		lblFontSize = new JLabel("Size: ");
		lblFontSize.setToolTipText(TextEditTooltips.tooltipIncreaseTextSize);
		lblSIZE = new JLabel(String.valueOf(GeneralConfig.DEFAULT_FONT.getSize()) + " Px");
		lblSIZE.setToolTipText(TextEditTooltips.tooltipIncreaseTextSize);

		sliderFontSize = new JSlider(4, 100);
		sliderFontSize.setToolTipText(TextEditTooltips.tooltipIncreaseTextSize);
		sliderFontSize.setValue(currentConfiguration.getTextEditorOpt().getFont().getSize());
		baseFontSize = sliderFontSize.getValue();

		slider_margin = new JSlider(0, 100, 0);

		addHandlers();

		if(currentConfiguration.getTextEditorOpt().spellChecker) {
			// Initially not selected
			boolean last = toggleButtonSpellCheck.isSelected();
			toggleButtonSpellCheck.doClick();
			toggleButtonSpellCheck.setSelected(!last);
		}

		logger.info(this.getClass().getName() + ": ready!");
	}

	/** Adds a group of toolbar components to the strip, with a thin separator before it (except for the first group) */
	private static void addGroup(JPanel strip, JComponent... items) {
		if (strip.getComponentCount() > 0) {
			strip.add(new ToolbarComponents.Separator());
		}
		for (JComponent item : items) {
			strip.add(item);
		}
	}

	/** Aligns the tail toggle with the tail mode of the current tab */
	public void syncTailToggle() {
		TabTextEditorObject obj = parent.getCurrentTabEditor().loadedTabObj;
		if (obj != null) {
			btnTailMode.setSelected(obj.getTailMode().get());
		}
	}

	/** Button that is not in the toolbar row: it keeps its listeners (shortcuts, menu entries) */
	private static JButton menuOnlyButton(String tooltip) {
		JButton button = new JButton("");
		button.setToolTipText(tooltip);
		return button;
	}

	private JMenuItem menuItem(EditorIcons.Kind kind, String text, Runnable action) {
		JMenuItem item = new JMenuItem(text, EditorIcons.of(kind, 16));
		item.addActionListener(e -> action.run());
		return item;
	}

	/** Menu entry that runs a button (same listeners and shortcuts), enabled and described like the button */
	private JMenuItem buttonItem(EditorIcons.Kind kind, String text, AbstractButton target) {
		JMenuItem item = menuItem(kind, text, () -> target.doClick(0));
		item.setEnabled(target.isEnabled());
		item.setToolTipText(target.getToolTipText());
		return item;
	}

	private JCheckBoxMenuItem checkItem(EditorIcons.Kind kind, String text, boolean selected, Runnable action) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(text, EditorIcons.of(kind, 16), selected);
		item.addActionListener(e -> action.run());
		return item;
	}

	/** "Text" menu: clean-up and transformations of the selection (or of the whole text when nothing is selected) */
	private JPopupMenu buildTextMenu() {
		JPopupMenu menu = new JPopupMenu();
		menu.add(menuItem(EditorIcons.Kind.SORT, I18n.t("toolbar.sort_lines_az"), () -> transformText(t -> TextTransforms.sortLines(t, true))));
		menu.add(menuItem(EditorIcons.Kind.SORT, I18n.t("toolbar.sort_lines_za"), () -> transformText(t -> TextTransforms.sortLines(t, false))));
		menu.add(menuItem(EditorIcons.Kind.DEDUPE, I18n.t("toolbar.remove_duplicate_lines"), () -> transformText(TextTransforms::removeDuplicateLines)));
		menu.add(menuItem(EditorIcons.Kind.EMPTY_LINES, I18n.t("toolbar.remove_empty_lines"), () -> transformText(TextTransforms::removeEmptyLines)));
		menu.add(menuItem(EditorIcons.Kind.TRIM, I18n.t("toolbar.trim_trailing_spaces"), () -> transformText(TextTransforms::trimTrailingSpaces)));
		menu.addSeparator();
		menu.add(menuItem(EditorIcons.Kind.CASE, I18n.t("toolbar.uppercase"), () -> transformText(TextTransforms::toUpperCase)));
		menu.add(menuItem(EditorIcons.Kind.CASE, I18n.t("toolbar.lowercase"), () -> transformText(TextTransforms::toLowerCase)));
		menu.add(menuItem(EditorIcons.Kind.CASE, I18n.t("toolbar.title_case"), () -> transformText(TextTransforms::toTitleCase)));
		menu.addSeparator();
		menu.add(buttonItem(EditorIcons.Kind.REMOVE_SPACES, I18n.t("toolbar.remove_spaces"), btnRemoveSpaces));
		menu.add(buttonItem(EditorIcons.Kind.ERASE, I18n.t("toolbar.remove_content"), btnRemoveWord));
		menu.add(buttonItem(EditorIcons.Kind.ESCAPE, I18n.t("toolbar.replace_escapes"), btnReplaceEscapesSeq));
		menu.add(buttonItem(EditorIcons.Kind.TRASH, I18n.t("toolbar.clear_text"), btnClear));
		return menu;
	}

	/** "Tools" menu: the functions used less often (calculator, color sampler, conversions, workspace) */
	private JPopupMenu buildToolsMenu() {
		JPopupMenu menu = new JPopupMenu();
		menu.add(buttonItem(EditorIcons.Kind.CALCULATOR, I18n.t("toolbar.calculator"), btnCalculator));
		menu.add(buttonItem(EditorIcons.Kind.PICKER, I18n.t("toolbar.color_sampler"), btnColorSampler));
		menu.addSeparator();
		menu.add(buttonItem(EditorIcons.Kind.OCR, I18n.t("toolbar.image_to_text"), btnParseImageText));
		menu.add(buttonItem(EditorIcons.Kind.PDF, I18n.t("toolbar.pdf_to_text"), btnParsePdfText));
		menu.add(buttonItem(EditorIcons.Kind.SPEAK, objSpeaker != null ? I18n.t("toolbar.stop_speaking") : I18n.t("toolbar.speak"), btnSpeak));
		menu.addSeparator();
		menu.add(buttonItem(EditorIcons.Kind.LOCK, I18n.t("toolbar.crypted_workspace"), btnImpExpCryptedWorkspace));
		menu.add(buttonItem(EditorIcons.Kind.SCREEPT, I18n.t("toolbar.open_in_screept"), btnOpenInScreepT));
		return menu;
	}

	/** "View" menu: how the text is shown (wrap, invisible characters, tab size, zoom) */
	private JPopupMenu buildViewMenu() {
		JPopupMenu menu = new JPopupMenu();
		final TextEditorOption options = currentConfiguration.getTextEditorOpt();
		menu.add(checkItem(EditorIcons.Kind.WRAP, I18n.t("toolbar.word_wrap"), parent.headerPanel.chkCutWordsTo.isSelected(), () -> parent.headerPanel.chkCutWordsTo.doClick(0)));
		menu.add(checkItem(EditorIcons.Kind.WHITESPACE, I18n.t("toolbar.show_whitespace"), options.whiteSpaceVisible, () -> toggleInvisibleCharacters(true)));
		menu.add(checkItem(EditorIcons.Kind.EOL, I18n.t("toolbar.show_line_ends"), options.markEndOfLine, () -> toggleInvisibleCharacters(false)));
		menu.add(checkItem(EditorIcons.Kind.TAB_SIZE, I18n.t("toolbar.show_tab_grid"), chckbxShowTabGrid.isSelected(), () -> chckbxShowTabGrid.doClick(0)));

		JMenu tabSizeMenu = new JMenu(I18n.t("toolbar.tab_size"));
		tabSizeMenu.setIcon(EditorIcons.of(EditorIcons.Kind.TAB_SIZE, 16));
		ButtonGroup group = new ButtonGroup();
		int currentTabSize = ((Number) tabSizeSpinner.getValue()).intValue();
		for (final int size : new int[] { 2, 3, 4, 6, 8 }) {
			JRadioButtonMenuItem radio = new JRadioButtonMenuItem(String.valueOf(size), size == currentTabSize);
			radio.addActionListener(e -> tabSizeSpinner.setValue(size));
			group.add(radio);
			tabSizeMenu.add(radio);
		}
		menu.add(tabSizeMenu);

		menu.addSeparator();
		menu.add(menuItem(EditorIcons.Kind.ZOOM_IN, I18n.t("toolbar.zoom_in"), () -> sliderFontSize.setValue(sliderFontSize.getValue() + 2)));
		menu.add(menuItem(EditorIcons.Kind.ZOOM_OUT, I18n.t("toolbar.zoom_out"), () -> sliderFontSize.setValue(sliderFontSize.getValue() - 2)));
		menu.add(menuItem(EditorIcons.Kind.ZOOM_RESET, I18n.t("toolbar.zoom_reset"), () -> sliderFontSize.setValue(baseFontSize)));
		return menu;
	}

	/** Shows or hides spaces/tabs (whitespace = true) or the end of line marks, and remembers the choice */
	private void toggleInvisibleCharacters(boolean whitespace) {
		TextEditorOption options = currentConfiguration.getTextEditorOpt();
		if (whitespace) {
			options.whiteSpaceVisible = !options.whiteSpaceVisible;
			for (TabTextEditorObject tab : parent.getCurrentTabEditor().loadedList.getList()) {
				tab.textArea.setWhitespaceVisible(options.whiteSpaceVisible);
				tab.textArea.repaint();
			}
			currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "whiteSpaceVisible", String.valueOf(options.whiteSpaceVisible));
		} else {
			options.markEndOfLine = !options.markEndOfLine;
			for (TabTextEditorObject tab : parent.getCurrentTabEditor().loadedList.getList()) {
				tab.textArea.setEOLMarkersVisible(options.markEndOfLine);
				tab.textArea.repaint();
			}
			currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "markEndOfLine", String.valueOf(options.markEndOfLine));
		}
	}

	/** Applies a text transformation to the selection, or to the whole text when nothing is selected (one undo step) */
	private void transformText(UnaryOperator<String> operation) {
		TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
		RSyntaxTextArea textArea = loadedTabObj.textArea;
		int selectionStart = textArea.getSelectionStart();
		int selectionEnd = textArea.getSelectionEnd();
		boolean hasSelection = selectionEnd > selectionStart;
		String source = hasSelection ? textArea.getSelectedText() : textArea.getText();
		String result = operation.apply(source);
		if (result == null || result.equals(source)) {
			return;
		}

		loadedTabObj.textArea.getUndoManager().beginInternalAtomicEdit();
		try {
			AbstractDocument document = (AbstractDocument) textArea.getDocument();
			int caret = textArea.getCaretPosition();
			int from = hasSelection ? selectionStart : 0;
			int length = hasSelection ? selectionEnd - selectionStart : document.getLength();
			document.replace(from, length, result, null);
			if (hasSelection) {
				textArea.setSelectionStart(from);
				textArea.setSelectionEnd(from + result.length());
			} else {
				textArea.setCaretPosition(Math.min(caret, document.getLength()));
			}
		} catch (BadLocationException e) {
			logger.error("Cannot transform the text from the toolbar", e);
		} finally {
			loadedTabObj.textArea.getUndoManager().endInternalAtomicEdit();
			textEditorUtils.updateCurrentUnsavedFromFileRead();
			parent.getCurrentTabEditor().revalidateFoldingPainted(textArea);
		}
	}

	// Add action handlers (listeners) to gui elements (buttons, combo, spinners, textboxes, ecc...)
	private void addHandlers() {

		// COPY the path of the current file
		btnCopyPath.addActionListener(e -> {
			File currentFile = parent.getCurrentTabEditor().loadedTabObj.loadedFile;
			if (currentFile != null) {
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(currentFile.getAbsolutePath()), null);
			}
		});

		// TAB SIZE SELECTOR
		tabSizeSpinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				parent.getCurrentTabEditor().loadedTabObj.textArea.setTabSize((int)tabSizeSpinner.getValue());
				currentConfiguration.getTextEditorOpt().setTabSize((int)tabSizeSpinner.getValue());
			}
		});

		// PROPERTIES - CURRENT FILE
		btnPropertiesPanel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				if (propertiesFrame == null) {
					loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_WAIT);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							try {
								if (loadedTabObj != null && loadedTabObj.loadedFile != null
										&& loadedTabObj.loadedFile.exists()) {
									propertiesFrame = new FilePropertiesFrame(loadedTabObj.loadedFile, true);
									propertiesFrame.setLocationRelativeTo(parent);
									propertiesFrame.setAlwaysOnTop(true);
									propertiesFrame.setTitle(loadedTabObj.loadedFile.getName());
									propertiesFrame.setVisible(true);
								}
							} catch (Exception e) {
								logger.error(e);
							}
							loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
						}
					});
				} else {
					if (!propertiesFrame.isActive()) {
						if (loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
							propertiesFrame.refreshValues(loadedTabObj.loadedFile);
						}
						propertiesFrame.toFront();
						propertiesFrame.setVisible(true);
					} else {
						propertiesFrame.dispose();
						propertiesFrame = null;
					}
				}
			}
		});

		// FILE VIEW
		btnToggleFileView.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (btnToggleFileView.isSelected()) {
					btnToggleFileView.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
					currentConfiguration.getTextEditorOpt().fileTreeActive = true;
				} else {
					btnToggleFileView.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
					currentConfiguration.getTextEditorOpt().fileTreeActive = false;
				}
				parent.fileTreeShowOrHide(currentConfiguration.getTextEditorOpt());
				textEditorUtils.refreshTree(parent.getCurrentTabEditor().loadedTabObj.loadedFile, false);

			}
		});

		// COPY TO CLIPBOARD KEEPING STYLE
		btnThemeEditor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainGUI.mainAPP.windowCreator.newSchemeManager();
			}
		});

		// MAP VIEW
		toggleBtnMap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (!currentConfiguration.getTextEditorOpt().mapViewActive) {
							toggleBtnMap.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
							currentConfiguration.getTextEditorOpt().mapViewActive = true;
							textEditorUtils.setRightPanelContent(PanelType.MAP_TXTAREA);

						} else {
							toggleBtnMap.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR.darker());
							currentConfiguration.getTextEditorOpt().mapViewActive = false;
						}
						currentConfiguration.getTextEditorOpt().clipboardViewActive = false;
						parent.rightPaneShowOrHide(true);

						if(currentConfiguration.getTextEditorOpt().mapViewActive)
							textEditorUtils.checkRightVisibleIfEnabled();
					}
				});
			}
		});

		// MAP VIEW
		toggleBtnConsole.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if(!toggleBtnConsole.isSelected())
							textEditorUtils.saveBottomDividerLocation();

						textEditorUtils.setBottomDividerActive(toggleBtnConsole.isSelected(), true);
					}
				});
			}
		});

		// SPELLCHECKER ON/OFF
		toggleButtonSpellCheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

						if (toggleButtonSpellCheck.isSelected()) {
							loadedTabObj.updateSpellCheckerParser(currentConfiguration.getTextEditorOpt().prefDictspellChecker, true);
							currentConfiguration.getTextEditorOpt().spellChecker = true;
							currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "spellChecker", true);
							toggleButtonSpellCheck.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
							showSpellCheckerLanguageMenu();

						} else {
							loadedTabObj.disableSpellChecker();
							currentConfiguration.getTextEditorOpt().spellChecker = false;
							currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "spellChecker", false);
							toggleButtonSpellCheck.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
						}
					}
				});
			}
		});

		// CLIPBOARD VIEW
		toggleBtnClipboard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (!currentConfiguration.getTextEditorOpt().clipboardViewActive) {
							toggleBtnClipboard.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
							currentConfiguration.getTextEditorOpt().clipboardViewActive = true;
							textEditorUtils.setRightPanelContent(PanelType.CLIPBOARD_PANEL);
						} else {
							toggleBtnClipboard.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR.darker());
							currentConfiguration.getTextEditorOpt().clipboardViewActive = false;
						}

						currentConfiguration.getTextEditorOpt().mapViewActive = false;
						parent.rightPaneShowOrHide(true);
						if(currentConfiguration.getTextEditorOpt().clipboardViewActive)
							textEditorUtils.checkRightVisibleIfEnabled();
					}
				});
			}
		});

		// OPEN IN SCREEPT CRYPTER
		btnOpenInScreepT.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				if (parent.getCurrentTabEditor().loadedTabObj.loadedFile != null && parent.getCurrentTabEditor().loadedTabObj.loadedFile.exists()) {
					parent.textEditorUtils.openCurrentFileInScreept();
				} else {
					dialogHelper.error(I18n.t("app.cannot_open_current_file_in_screept"), "Cannot open in screept");
				}

			}
		});

		btnViewInCmd.addActionListener((e)->{
			File loadedFileParent = FileVarious.getParent(parent.getCurrentTabEditor().loadedTabObj.loadedFile);
			SysUtils.cdIntoCommandLine(loadedFileParent);
		});

		// OPEN CURRENT IN SYSTEM EXPLORER
		btnViewInExplorer.addActionListener((e) -> {
			File loadedFileParent = FileVarious.getParent(parent.getCurrentTabEditor().loadedTabObj.loadedFile);
			GuiUtilsExtended.openInFileSystem(loadedFileParent);
		});

		Runnable escapeLastaUpdater = new Runnable() {
			@Override
			public void run() {
				currentConfiguration.getTextEditorOpt().setLastEscapeLanguage(escapeReplacerPanel.getCurrentComboLang());
			}
		};

		// OPEN TEMPLATES PANEL
		btnReplaceEscapesSeq.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (!EscapeReplacerFrame.active) {

							escapeReplacerPanel = new EscapeReplacerFrame
							(
								mainApplication.getMainFrame(),
								parent.getCurrentTabEditor().loadedTabObj.textArea,
								btnReplaceEscapesSeq, escapeLastaUpdater
							);

							GuiUtilsExtended.centerComponent(escapeReplacerPanel, escapeReplacerPanel.getDefaultDimension());
							escapeReplacerPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
							escapeReplacerPanel.initComboLang(currentConfiguration.getTextEditorOpt().getLastEscapeLanguage());
							escapeReplacerPanel.setAlwaysOnTop(true);
							escapeReplacerPanel.setVisible(true);
							escapeReplacerPanel.setTitle("Escape<->Special converter - " + GeneralConfig.APPLICATION_NAME);
							btnReplaceEscapesSeq.setEnabled(false);
						} else {
							escapeReplacerPanel.dispatchEvent(new WindowEvent(escapeReplacerPanel, WindowEvent.WINDOW_CLOSING));
						}
						escapeReplacerPanel.setLocationRelativeTo(parent.mainPanel);
					}
				});
			}
		});

		// CALCULATOR OPENER
		btnCalculator.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (calculatorFrame == null || !CalculatorMainApp.active) {
							calculatorMainThread = new CalculatorMainApp(MainGUI.mainAPP.getMainFrame());
						}
					}
				});
			}
		});

		// COLOR SAMPLER
		btnColorSampler.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						mainApplication.windowCreator.newColorPickerFrame();
					}
				});
			}
		});

		// IMAGE PARSE INJECTOR
		btnParseImageText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (imageParserBox == null || !ImageSelector2TextFrame.active) {
							imageParserBox = new ImageSelector2TextFrame(parent.parentFrame, parent.getCurrentTabEditor().loadedTabObj.textArea,
									GeneralConfig.APPLICATION_NAME + " - Image2Text");
							imageParserBox.setVisible(true);
						} else {
							imageParserBox.dispatchEvent(new WindowEvent(imageParserBox, WindowEvent.WINDOW_CLOSING));
						}
						imageParserBox.setLocationRelativeTo(mainApplication.getMainFrame());
						GuiUtilsExtended.extendFramesAndMoveToTop(mainApplication.getMainFrame(), imageParserBox, JFrame.MAXIMIZED_BOTH);
					}
				});
			}
		});

		// PDF PARSER INJECTOR
		btnParsePdfText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (pdfParserBox == null || !PdfSelector2TextFrame.active) {
							pdfParserBox = new PdfSelector2TextFrame(parent.parentFrame, parent.getCurrentTabEditor().loadedTabObj.textArea,
									GeneralConfig.APPLICATION_NAME + " - Pdf2Text");
							pdfParserBox.setVisible(true);
						} else {
							pdfParserBox.dispatchEvent(new WindowEvent(pdfParserBox, WindowEvent.WINDOW_CLOSING));
						}
						pdfParserBox.setLocationRelativeTo(mainApplication.getMainFrame());
						GuiUtilsExtended.extendFramesAndMoveToTop(mainApplication.getMainFrame(), pdfParserBox, JFrame.MAXIMIZED_BOTH);
					}
				});
			}
		});

		// SEARCH INTO FILE
		btnFileContentSearch.addActionListener((e)-> {
			mainApplication.windowCreator.fileContentSearch();
		});

		// SEARCH TEXT
		btnFindText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						textEditorUtils.addCurrentSelectionToLastSearched();
						if (searchBox == null) {
							searchBox = textEditorUtils.textAreaDialogs.getSearchDialog(parent.parentFrame,  null, textEditorUtils,"successAdvice", "errorAdvice");
							GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_SEARCH, searchBox);
							try {
								textEditorUtils.textAreaDialogs.setComboItems(
										searchBox, currentConfiguration.getTextEditorOpt().lastSearchTerms.getList(), true);
							} catch (IllegalArgumentException | IllegalAccessException e) {
								logger.error("Cannot update search dialog combobox");
							}
						} else {
							searchBox.setVisible(true);
							searchBox.toFront();
							searchBox.requestFocus();
						}

						searchBox.addWindowListener(new java.awt.event.WindowAdapter() {
							@Override
							public void windowClosing(java.awt.event.WindowEvent windowEvent) {
								currentConfiguration.getTextEditorOpt().lastSearchTerms.addUniquesToTop(textEditorUtils.textAreaDialogs.searchList.getList());
								currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "lastSearchTerms", currentConfiguration.getTextEditorOpt().lastSearchTerms.toCharSeparatedString(","));

								searchBox.dispose();
							}
						});

						searchBox.setLocationRelativeTo(mainApplication.getMainFrame());
					}
				});
			}
		});

		// REPLACE TEXT
		btnReplaceText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						textEditorUtils.addCurrentSelectionToLastSearched();

						if (replaceDialog == null) {
							replaceDialog = textEditorUtils.textAreaDialogs.getReplaceDialog(
									parent.parentFrame, null, textEditorUtils,"successAdvice", "errorAdvice");
							GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_REPLACE_TXT, replaceDialog);
							try {
								textEditorUtils.textAreaDialogs.setComboItems(
										replaceDialog, currentConfiguration.getTextEditorOpt().lastReplaceTerms.getList(), false);
								textEditorUtils.textAreaDialogs.setComboItems(
										replaceDialog, currentConfiguration.getTextEditorOpt().lastSearchTerms.getList(), true);
							} catch (IllegalArgumentException | IllegalAccessException e) {
								logger.error("Cannot update replace dialog combobox");
							}
						} else {
							replaceDialog.setVisible(true);
							replaceDialog.toFront();
							replaceDialog.requestFocus();
						}

						replaceDialog.addWindowListener(new java.awt.event.WindowAdapter() {
							@Override
							public void windowClosing(java.awt.event.WindowEvent windowEvent) {
								// Backup replaced
								currentConfiguration.getTextEditorOpt().lastReplaceTerms.addUniquesToTop(textEditorUtils.textAreaDialogs.replaceList.getList());
								currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "lastReplaceTerms", currentConfiguration.getTextEditorOpt().lastReplaceTerms.toCharSeparatedString(","));

								// Backup searched
								currentConfiguration.getTextEditorOpt().lastSearchTerms.addUniquesToTop(textEditorUtils.textAreaDialogs.searchList.getList());
								currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "lastSearchTerms", currentConfiguration.getTextEditorOpt().lastSearchTerms.toCharSeparatedString(","));

								replaceDialog.dispose();
							}
						});

						replaceDialog.setLocationRelativeTo(mainApplication.getMainFrame());
					}
				});
			}
		});

		// GOTO LINE
		btnGoToLine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (goToLineDialog == null) {
							goToLineDialog = textEditorUtils.textAreaDialogs.getGoToLineDialog(parent.parentFrame);
						} else {
							goToLineDialog.dispatchEvent(new WindowEvent(mainApplication.getMainFrame(), WindowEvent.WINDOW_CLOSING));
							goToLineDialog = textEditorUtils.textAreaDialogs.getGoToLineDialog(parent.parentFrame);
						}
						goToLineDialog.setLocationRelativeTo(mainApplication.getMainFrame());
					}
				});
			}
		});

		// CONVERT TO/FROM base 64
		btnBase64Conversion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				EncodingUtils encoding64 = new EncodingUtils();
				EncodingUtils.stringEncoding = currentConfiguration.getGenOpt().getCharset();

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						RSyntaxTextArea textArea = parent.getCurrentTabEditor().loadedTabObj.textArea;
						textArea.setCursor(GuiUtilsExtended.CURSOR_WAIT);

						parent.getCurrentTabEditor().loadedTabObj.textArea.getUndoManager().beginInternalAtomicEdit();

						SelectionDtoFull dto = new SelectionDtoFull(textArea);
						String currEncoding = SysUtils.getCurrentSystemEncoding();
						try {
							boolean controlDown = parent.shortcutsListener.isCtrlDown();
							String converted = "";

							if (!controlDown) {
								// Convert only selection an inject in the place of current selection
								converted = encoding64.encodeBase64(dto.getSelectedOrAllText().getBytes(currEncoding));
							} else {
								converted = encoding64.decodeBase64(dto.getSelectedOrAllText());
							}

							if (dto.isSomethingSelected(false)) {
								dto.replaceSelection(converted, false, false);

							} else {
								dto.replaceAllText(converted, false);
							}

						} catch (Exception e1) {
							dialogHelper.error(I18n.t("app.something_has_gone_wrong"), "An Error Occurred");
							logger.error("An error occurred tryin' to convert using base64 (SelectedText = " + dto.isSomethingSelected(false));
						} finally {
							try {
								dto.applySelectionDTO(textArea);
							} catch (Exception e) {
								logger.error("Error with textarea selection reapply after base 64 conversion", e);
							}

							textEditorUtils.updateCurrentUnsavedFromFileRead();
							textArea.setCursor(GuiUtilsExtended.CURSOR_DEFAULT);

							parent.getCurrentTabEditor().revalidateFoldingPainted(textArea);
							parent.getCurrentTabEditor().loadedTabObj.textArea.getUndoManager().endInternalAtomicEdit();
						}
					}
				});
			}
		});

		btnBase64Conversion.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
				parent.shortcutsListener.flagHoverBtnBase64.set(false);
				parent.shortcutsListener.doControlActions();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				parent.shortcutsListener.flagHoverBtnBase64.set(true);
				parent.shortcutsListener.doControlActions();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});

		// VIEW/HIDE tab grids
		chckbxShowTabGrid.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (chckbxShowTabGrid.isSelected()) {
					currentConfiguration.getTextEditorOpt().setTabLineVisible(true);
					parent.getCurrentTabEditor().loadedTabObj.textArea.setPaintTabLines(true);
				} else {
					currentConfiguration.getTextEditorOpt().setTabLineVisible(false);
					parent.getCurrentTabEditor().loadedTabObj.textArea.setPaintTabLines(false);
				}
			}
		});

		// PREV IN HISTORY
		btnPrevHistory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.getCurrentTabEditor().goToPreviousInHistory();
			}
		});

		// NEXT IN HISTORY
		btnNextHistory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.getCurrentTabEditor().goToNextInHistory();
			}
		});

		// BACKGROUND TEXT COLOR
//		btnBackgroundCol.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				btnBackgroundCol.setEnabled(false);
//				Color esit = new ColorDialogUtils().askForColor(parent.getCurrentTabEditor().loadedTabObj.textArea, false);
//				if (esit != null) {
//					currentConfiguration.getTextEditorOpt().setBackCol(esit);
//					RsyntaxUtils.setBackground(parent.getCurrentTabEditor().loadedTabObj.textArea, esit);
//				}
//				btnBackgroundCol.setEnabled(true);
//			}
//		});

		btnTailMode.addActionListener((e)->{
			TabTextEditorObject obj = parent.getCurrentTabEditor().loadedTabObj;
			boolean tailModeWasActive = obj.getTailMode().get();
			obj.setTailMode(new AtomicBoolean(!tailModeWasActive), ()->{
				if(!tailModeWasActive && (obj.unsaved || obj.loadedFile == null)) {
					int choiceY = dialogHelper.yesNoOrCanc(
							I18n.t("app.file_has_unsaved_changes_do_you"),
							"There are some unsaved files in file Editor, do you want to save them?");

					if(choiceY == JOptionHelper.CANC)
						return false;

					if(choiceY == JOptionHelper.NO) {
						parent.getCurrentTabEditor().reloadCurrentTab(false);
						textEditorUtils.refreshMessageAndTabTooltip("[File Reloaded]");
						textEditorUtils.syncMiniTextAreaContent();
					}

					if(choiceY == JOptionHelper.OK) {
						saveCurrentTabText();
					}
				}

				return true;
			});

			parent.getCurrentTabEditor().updateSavedLabelNTab(null);
			syncTailToggle(); // the change can be refused (cancelled, file missing)
		});
		// the toggle shows the tail state of the tab in front
		SwingUtilities.invokeLater(() -> parent.getCurrentTabEditor().mainTabList.addChangeListener(e -> SwingUtilities.invokeLater(this::syncTailToggle)));

		// FOREGROUND TEXT COLOR
		btnImpExpCryptedWorkspace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainGUI.mainAPP.windowCreator.newImportExportCryptWorkspace();
			}
		});

		// FONT CHOOSER PANEL
		btnFontChooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				btnFontChooser.setEnabled(false);

				Font chosenFont = currentConfiguration.getTextEditorOpt().getFont();
				JFontChooserExtended fontChooser = new JFontChooserExtended();
				fontChooser.setSelectedFont(chosenFont);
				int choice = fontChooser.showDialog(MainGUI.mainAPP.getMainFrame());

				if (choice == JFontChooserExtended.OK_OPTION) {
					chosenFont = fontChooser.getSelectedFont();
					currentConfiguration.getTextEditorOpt().setFont(chosenFont);
					logger.info("Selected Font : " + chosenFont.toString());
					RsyntaxUtils.setFont(loadedTabObj.textArea, loadedTabObj.textArea.getFont());
					loadedTabObj.textArea.setFont(chosenFont);
					parent.getCurrentTabEditor().applyFontToOtherTabs(chosenFont);
					parent.applyFontToOtherPanels(chosenFont);
					sliderFontSize.setValue(loadedTabObj.textArea.getFont().getSize());
					lblSIZE.setText(String.valueOf(loadedTabObj.textArea.getFont().getSize()) + " Px");
				}

				btnFontChooser.setEnabled(true);
			}
		});

		// TEXT AREA FORMATTER
		btntTextFormat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GuiUtilsExtended.formatTextLater(parent.getCurrentTabEditor().loadedTabObj.textArea, parent.CURRENT_EXT_OUT, currentConfiguration.getTextEditorOpt().tabSize);
			}
		});

		// SLIDER FONT CHANGED
		sliderFontSize.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {

				final TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				loadedTabObj.textArea.setHighlightCurrentLine(true);

				SwingUtilities.invokeLater(() -> {

					Font font = loadedTabObj.textArea.getFont();
					font = new Font(font.getName(), font.getStyle(), sliderFontSize.getValue());
					lblSIZE.setText(String.valueOf(loadedTabObj.textArea.getFont().getSize()) + " Px");
					currentConfiguration.getTextEditorOpt().setFont(font);
					loadedTabObj.textArea.setFont(font);
					parent.getCurrentTabEditor().applyFontToOtherTabs(font);
					parent.applyFontToOtherPanels(font);

					Font gutterFont = font.getSize() < 20 ? font.deriveFont(font.getSize()-2) : font.deriveFont(20F);
					loadedTabObj.currentScrollPane.getGutter().setLineNumberFont(gutterFont);

					RXTextUtilities.centerLineFullInScrollPane(loadedTabObj.textArea);

					GuiUtils.launchThreadSafeSwing(new Runnable() {
						@Override
						public void run() {
							loadedTabObj.textArea.revalidate();
							loadedTabObj.textArea.setHighlightCurrentLine(true);
							loadedTabObj.textArea.repaint(500);
						}
					});
				});
			}
		});

		// SPEAK TEXT
		btnSpeak.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				new Thread(new Runnable() {

					@Override
					public void run() {
						TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

						String selected = (loadedTabObj.textArea.getSelectedText() != null && loadedTabObj.textArea.getSelectedText().length() != 0)?
								loadedTabObj.textArea.getSelectedText() : loadedTabObj.textArea.getText();

						if (objSpeaker != null && objSpeaker.getVoice() != null) {
							objSpeaker.getVoice().deallocate();
							btnSpeak.setBackground(new Color(176, 196, 222));
						} else {
							// else speak
							objSpeaker = new Text2Speech();
							btnSpeak.setBackground(Color.RED);
//							float volume = currentConfiguration.getGenOpt().getGeneralVolume();
							float volume = 1.0F;
							objSpeaker.dospeak(selected , "kevin16", (parent.headerPanel.sliderSpokenSpeed.getValue() / 100.0f), volume);
							objSpeaker = null;
							btnSpeak.setBackground(new Color(176, 196, 222));
						}
					}
				}).start();
			}
		});

		// UNDO LAST OPERATION ON TEXT
		btnUndo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (parent.getCurrentTabEditor().loadedTabObj.isEmbeddedViewer()) {
					parent.getCurrentTabEditor().loadedTabObj.viewer.undo();
				} else if (parent.getCurrentTabEditor().loadedTabObj.textArea.canUndo()) {
					parent.getCurrentTabEditor().loadedTabObj.textArea.undoLastAction();
					textEditorUtils.updateCurrentUnsavedFromFileRead();
				}
			}
		});

		// REDO LAST OPERATION ON TEXT
		btnRedo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				if (parent.getCurrentTabEditor().loadedTabObj.isEmbeddedViewer()) {
					parent.getCurrentTabEditor().loadedTabObj.viewer.redo();
				} else if (parent.getCurrentTabEditor().loadedTabObj.textArea.canRedo()) {
					parent.getCurrentTabEditor().loadedTabObj.textArea.redoLastAction();
					textEditorUtils.updateCurrentUnsavedFromFileRead();
				}

			}
		});

		// CLEAR TEXT AREA
		btnClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				if (dialogHelper.yesOrNo(I18n.t("app.clear_text"))) {
					parent.getCurrentTabEditor().loadedTabObj.textArea.setText("");
				}
			}
		});

		// FILE FORMAT SELECTOR
		comboFileFormat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyFileFormatSyntaxFull();
				currentConfiguration.getTextEditorOpt().setPreferredSyntaxIfNotSync(getCurrentFileType().toString());
				currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "preferredSyntaxIfNotSync", currentConfiguration.getTextEditorOpt().preferredSyntaxIfNotSync);
				GuiUtils.giveFocusToComponent(comboFileFormat);
				checkFormatterEnabled();
			}
		});
		checkFormatterEnabled();

		//  LOCK/UNLOCK auto recognizer of file type and syntax
		chckbxLockLanguage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textEditorUtils.findRightSyntaxFromComboNSelect(parent.getCurrentTabEditor().loadedTabObj.loadedFile, !chckbxLockLanguage.isSelected());
				currentConfiguration.getTextEditorOpt().setDefaultAutoSyntaxSync(!chckbxLockLanguage.isSelected());
				currentConfiguration.getTextEditorOpt().setPreferredSyntaxIfNotSync(getCurrentFileType().toString());
				currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "preferredSyntaxIfNotSync", currentConfiguration.getTextEditorOpt().preferredSyntaxIfNotSync);
			}
		});

		// style graphics
		comboStylesScheme.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textEditorUtils.applySelectedTheme(false);
				GuiUtils.giveFocusToComponent(comboStylesScheme);
			}
		});

		// OPEN TEMPLATES PANEL
		btnTemplates.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!TemplatesChooserFrame.active) {
					templatesPanel = new TemplatesChooserFrame(mainApplication.getMainFrame(), parent.getCurrentTabEditor().loadedTabObj.textArea, btnTemplates);
					templatesPanel.setiSelector(new AbstractSelector<String>() {

						@Override
						public String getLastSelected() {
							return currentConfiguration.getTextEditorOpt().lastSelectedTemplate;
						}

						@Override
						public String setLastSelected(String last) {
							currentConfiguration.getTextEditorOpt().setLastSelectedTemplate(last);
							return last;
						}

					}, true);

					templatesPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					templatesPanel.setAlwaysOnTop(true);
					templatesPanel.setVisible(true);
					templatesPanel.setTitle("Templates manager - " + GeneralConfig.APPLICATION_NAME);
					btnTemplates.setEnabled(false);
				}
				templatesPanel.setLocationRelativeTo(parent.mainPanel);
				GuiUtils.centerComponent(templatesPanel, templatesPanel.getDefaultDimension());
			}
		});

		// MARGIN TEXT AREA SELECTOR
		slider_margin.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				textEditorUtils.updateMargin();
			}
		});

		btnRemoveWord.addActionListener((e)->{
			TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
			SelectionDtoFull dto = new SelectionDtoFull(loadedTabObj.textArea, loadedTabObj.loadedFile);
			loadedTabObj.textArea.getUndoManager().beginInternalAtomicEdit();

			String toRemove = dialogHelper.askForString(I18n.t("app.specify_the_word_you_want_to"), "Remove all word occurrences", StringWorker.nullToEmpty(lastRemovedWord));
			if(StringWorker.isEmptyNoTrim(toRemove)) {
				return;
			}

			lastRemovedWord = toRemove;

			dto = StringWorker.removeCharacterFromTxtArea(toRemove, dto);

			GuiUtils.launchRunnable(()->{
				try {
					loadedTabObj.textArea.getUndoManager().endInternalAtomicEdit();
					textEditorUtils.updateCurrentUnsavedFromFileRead();
					loadedTabObj.storeSelectionDto(true);
				} catch (Throwable e2) {
					logger.warn(e2);
				}
			}, false, true, true);
		});

		// REMOVE WHITE SPACES OR CHARACTER (CTRL PRESSED)
		btnRemoveSpaces.addActionListener((e) -> {
			TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
			SelectionDtoFull dto = new SelectionDtoFull(loadedTabObj.textArea, loadedTabObj.loadedFile);

			loadedTabObj.textArea.getUndoManager().beginInternalAtomicEdit();
			dto = StringWorker.removeCharacterFromTxtArea(" ", dto);

			GuiUtils.launchRunnable(()->{
				try {
					loadedTabObj.textArea.getUndoManager().endInternalAtomicEdit();
					textEditorUtils.updateCurrentUnsavedFromFileRead();
					loadedTabObj.storeSelectionDto(true);
				} catch (Throwable e2) {
					logger.warn(e2);
				}
			}, false, true, true);
		});

		// QUICK FILE SEARCH
		btnQuickFileSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainGUI.mainGuiUtils.openQuickSearchIntoWorkspace();
			}
		});

		// SAVE TEXT FILE
		btnSaveTextFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				saveCurrentTabText();
			}
		});

		// SAVE TEXT FILE AS
		btnSaveTextFileAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveCurrentFileAs(null);
			}
		});

		// REMOVE ALL LOADED TABS
		btnCloseAllTabs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startCloseAllTabsProcedure();
			}
		});

		// RELOAD INTO TEXT AREA FROM DISK
		btnReloadFromDisk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						parent.getCurrentTabEditor().reloadCurrentTabFromDisk();
						textEditorUtils.refreshMessageAndTabTooltip("[File Reloaded]");
						textEditorUtils.syncMiniTextAreaContent();
					}
				});
			}
		});

		// LOAD TEXT FILE
		btnLoadTextFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				if (!parent.getCurrentTabEditor().alreadyLoading) {

					File source = MainGuiGeneralAppStarter.fileDialogHelper.fileRead(parent, SupportedFileTextEditor.getMultipleFileFilter());

					parent.getCurrentTabEditor().loadFile(source);
				}
			}
		});

		// ADD NEW TAB
		btnNewTab.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				parent.getCurrentTabEditor().addTab(null);
			}
		});

		enableDependenciesFunctions();
	}

	private void saveCurrentTabText() {
		final TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

		// integrated viewer (image...): it saves by itself, the empty text area must never be written over the file
		if (loadedTabObj.isEmbeddedViewer()) {
			loadedTabObj.viewer.save();
			parent.getCurrentTabEditor().updateSavedLabelNTab(null);
			return;
		}

		boolean done = false;
		if (loadedTabObj.loadedFile == null || !loadedTabObj.loadedFile.exists()) {
			saveCurrentFileAs(null);
			return;
		} else {
			try {
				String outString = StringWorker.normalizeStringToEol(loadedTabObj.textArea.getText(), parent.getCurrentTabEditor().eolFor(loadedTabObj));

				done = parent.getCurrentTabEditor().writeTabToFile(loadedTabObj, outString, loadedTabObj.loadedFile);
			} catch (Exception e1) {
				dialogHelper.error(I18n.t("common.error_saving_the_selected_file"));
				logger.error("Exception happened!", e1);
			}

			if (done) {
				loadedTabObj.unsaved = false;
				parent.getCurrentTabEditor().updateSavedLabelNTab(null);
				textEditorUtils.refreshMessageAndTabTooltip("[File saved]");

				SwingUtilities.invokeLater(() -> {
					// Clean memorized changes triggered by save
					logger.debug("Cleaning changes from tabbedPane history");
					parent.getCurrentTabEditor().changeMonManager.removePendingFiredChanges(loadedTabObj.loadedFile);
					logger.debug("Cleaned changes from tabbedPane history");
				});

			} else {
				textEditorUtils.errorAdvice("[Error - not saved]");
				parent.footerPanel.lblCurrentFile.setText("(*)" + loadedTabObj.loadedFile.getName());
				parent.footerPanel.lblCurrentFile.setForeground(Color.RED);
			}
			try {
				parent.footerPanel.lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath());
			} catch (IOException e1) {
				logger.error(e1);
			}
		}
	}

	private void checkFormatterEnabled() {
		if(GuiUtilsExtended.supportedTypesList.contains(getCurrentFileType().senzapunto.toLowerCase())) {
			btntTextFormat.setEnabled(true);
			btntTextFormat.setToolTipText(TextEditTooltips.tooltipFormatCode);
		} else {
			btntTextFormat.setEnabled(false);
			btntTextFormat.setToolTipText(TextEditTooltips.tooltipFormatCodeDisabled);
		}
	}

	public void saveCurrentFileAs(EOL lineSeparator) {
		TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

		if (loadedTabObj.isEmbeddedViewer()) {
			loadedTabObj.viewer.saveAs();
			parent.getCurrentTabEditor().updateSavedLabelNTab(null);
			return;
		}
		boolean skipRefresh = true;

		if (!parent.getCurrentTabEditor().alreadySaving) {
			// update signal value
			parent.getCurrentTabEditor().alreadySaving = true;
			boolean done = false;
			String exportNamePreset = (loadedTabObj.loadedFile != null && !loadedTabObj.loadedFile.getName().equals(""))?
					loadedTabObj.loadedFile.getName() : "NewFile" + getCurrentFileType().extension;
			exportNamePreset = FileVarious.ensureValidExtension(exportNamePreset, getCurrentFileType().extension);

			try {
				File currentParent = FileVarious.getParent(loadedTabObj.loadedFile);
				File selectedFile = MainGuiGeneralAppStarter.fileDialogHelper.fileWrite(
						parent.parentFrame, exportNamePreset, currentParent, SupportedFileTextEditor.getMultipleFileFilter());

				if (selectedFile != null) {
					skipRefresh = false;
					if (!loadedTabObj.isEncrypted()) {
						selectedFile = textEditorUtils.adaptFileExt(selectedFile); // an encrypted file keeps the name chosen by the user
					}

					String outString = StringWorker.normalizeStringToEol(loadedTabObj.textArea.getText(), lineSeparator == null ? parent.getCurrentTabEditor().eolFor(loadedTabObj) : lineSeparator);

					if (selectedFile.exists()) {
						if (dialogHelper.yesOrNo(various.common.light.i18n.I18n.fmt("app.file_x_exists_overwrite_it", selectedFile.getName()), I18n.t("common.overwrite"))) {
							done = parent.getCurrentTabEditor().writeTabToFile(loadedTabObj, outString, selectedFile);
						}
					} else {
						// same writer of a normal save: encoding of the tab, encrypted again for an encrypted one
						done = parent.getCurrentTabEditor().writeTabToFile(loadedTabObj, outString, selectedFile);
					}
					if (done) {
						String msgOk = "File " + selectedFile.getName() + " succesfully saved!";
						dialogHelper.info(msgOk, "File Saved!");

						if (loadedTabObj.isEncrypted()) {
							loadedTabObj.loadedFile = selectedFile; // the next saves go to the new encrypted file
							loadedTabObj.unsaved = false;
						}
						if (loadedTabObj.loadedFile == null) {
							// Set as current tabObject's file
							loadedTabObj.loadedFile = selectedFile;
							if (parent.getCurrentTabEditor().loadedTabObj.fileMonitorManager == null) {
								if (loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
									parent.getCurrentTabEditor().changeMonManager.updateCurrMonitor(loadedTabObj.loadedFile);
								}
							} else {
								parent.getCurrentTabEditor().changeMonManager.removePendingFiredChanges(loadedTabObj.loadedFile);
							}
							loadedTabObj.unsaved = false;
						}
						parent.getCurrentTabEditor().updateSavedLabelNTab(null);

						parent.footerPanel.lblCurrentFile.setText(loadedTabObj.loadedFile.getName());
						parent.footerPanel.lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath());
						textEditorUtils.refreshMessageAndTabTooltip("[File copy saved]");
						currentConfiguration.getFileOpt().setLastDstFolderPath(FileVarious.getParentPath(loadedTabObj.loadedFile));
					} else {
						skipRefresh = true;
						textEditorUtils.errorAdvice("[Error while saving a copy]");
					}
				}

			} catch (IOException e1) {
				skipRefresh = true;
				textEditorUtils.errorAdvice("[Error Saving selected file]");
				dialogHelper.error(I18n.t("common.error_saving_the_selected_file"));
				logger.error("Exception happened!", e1);
			} finally {

				// update signal value
				parent.getCurrentTabEditor().alreadySaving = false;
			}
		}
		if (!skipRefresh) {
			textEditorUtils.refreshTree(loadedTabObj.loadedFile, true);
		}
	}

	public void applyFileFormatSyntaxFull() {
		TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

		// backup unsaved to avoid changing saved info with theme change
		textEditorUtils.applyFileFormatFromCombo(false);

		if (currentConfiguration.getTextEditorOpt().spellChecker) {
			RsyntaxUtils.removeAllParsers(loadedTabObj.textArea);
			loadedTabObj.updateSpellCheckerParser(currentConfiguration.getTextEditorOpt().prefDictspellChecker, true);
		}else {
			RsyntaxUtils.removeAllParsers(loadedTabObj.textArea);
		}

		parent.getCurrentTabEditor().updateSavedLabelNTab(null);

		if (!currentConfiguration.getTextEditorOpt().clipboardSyncStyle) {
			currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "preferredSyntaxIfNotSync",
					currentConfiguration.getTextEditorOpt().preferredSyntaxIfNotSync);
			currentConfiguration.getTextEditorOpt().setPreferredSyntaxIfNotSync(((SupportedFileTextEditor) comboFileFormat.getSelectedItem()).toString());
		}
	}

	/**
	 * Read default value from currentConfiguration file and apply read theme using it: NB current configuration in memory will be updated with it
	 */
	public void applyDefaultStyle() {
		try {
			String prefThemeFromFile = currentConfiguration.readFromFile(INItializer.TEXT_EDITOR_OPT, "preferredTheme");
			if (!StringWorker.trimToEmpty(prefThemeFromFile).equals("")) {
				comboStylesScheme.setSelectedItem(ThemesWrapper.getFromListByPath(prefThemeFromFile, themeList));
			}
		} catch (Exception e) {
			logger.error("Cannot set default theme from configuration file", e);
		}
	}

	/**
	 * Read default value from currentConfiguration file and apply syntax using it: NB current configuration in memory will be updated with it
	 */
	public void applyDefaultFileFormatSyntax() {
		try {
			String prefSyntaxFromConfigFile = currentConfiguration.readFromFile(INItializer.TEXT_EDITOR_OPT, "preferredTheme");
			if (!StringWorker.trimToEmpty(prefSyntaxFromConfigFile).equals("")) {
				applyFileFormatSyntaxFull();
			}
		} catch (Exception e) {
			logger.error("Cannot set default style from configuration file", e);
		}
	}

	public void enableDependenciesFunctions() {
		String koMsg = " not found on this installation: update your libraries plugin to use this function!";
		enableDependency(btnSpeak, MainGuiGeneralAppStarter.voiceSpeakerAvailable, TextEditTooltips.tooltipSpeak, "TextToSpeech" + koMsg);
		enableDependency(btnParseImageText, MainGuiGeneralAppStarter.tesseractAvailable, TextEditTooltips.tooltipParseImage, "Tesseract" + koMsg);
		enableDependency(btnParsePdfText, MainGuiGeneralAppStarter.tesseractAvailable, TextEditTooltips.tooltipParsePdfTxt, "Tesseract" + koMsg);
	}

	/**
	 * Enables or disables the functions that work on the text of the current tab: a tab hosting a viewer that has no use for them
	 * (image, HTML, LogSearch...) shows them disabled, a text tab (or a viewer that supports them) gets them back.
	 */
	public void applyTabKind(boolean textTools) {
		javax.swing.JComponent[] textFunctions = {
				btnFindText, btnReplaceText, btnReplaceEscapesSeq, btnGoToLine, btnSpeak, btnParseImageText,
				btnParsePdfText, btnFontChooser, btnTemplates, btnUndo, btnRedo, btnClear, btnRemoveSpaces, btnRemoveWord,
				btnBase64Conversion, btnOpenInScreepT, btnTailMode, toggleBtnMap, toggleButtonSpellCheck,
				tabSizeSpinner, comboFileFormat, comboStylesScheme, sliderFontSize, slider_margin, chckbxShowTabGrid, chckbxLockLanguage
		};
		for (javax.swing.JComponent component : textFunctions) {
			if (component != null) {
				component.setEnabled(textTools);
			}
		}
		if (textTools) {
			enableDependenciesFunctions(); // speech and OCR depend on their libraries too
			checkFormatterEnabled();       // the formatter depends on the file type
		} else if (btntTextFormat != null) {
			btntTextFormat.setEnabled(false);
		}
	}

	/** Undo and redo work also on the viewers that keep their own history */
	public void applyHistoryAvailable(boolean viewerHistory) {
		if (viewerHistory) {
			btnUndo.setEnabled(true);
			btnRedo.setEnabled(true);
		}
	}

	/** Full screen button: enabled only when the tab in front holds a viewer that supports it */
	public void applyFullscreenAvailable(boolean available) {
		if (btnFullscreen != null) {
			btnFullscreen.setEnabled(available);
		}
	}

	/** Full screen on/off for the viewer of the current tab (F11 and the toolbar button) */
	public void toggleViewerFullscreen() {
		TabTextEditorObject tab = parent.getCurrentTabEditor().loadedTabObj;
		if (tab != null) {
			tab.toggleFullscreen();
		}
	}

	private static void enableDependency(JButton target, boolean enable, String tooltipY, String tooltipN) {
			target.setEnabled(enable);
			target.setToolTipText(enable ? tooltipY : tooltipN);
	}

	/**
	 * Close all tabs asking to user dinamically, if cancel is pressed false will be returned
	 * @return
	 */
	public boolean startCloseAllTabsProcedure() {
		int askIfUnsaved = dialogHelper.yesNoOrCanc(I18n.t("app.do_you_want_to_be_asked"), "Close All Tabs");

		if(askIfUnsaved == JOptionHelperExtended.CANC)
			return false;

		if(askIfUnsaved == JOptionHelperExtended.OK) {
			parent.getCurrentTabEditor().removeAllTabs(true);

		}else if(askIfUnsaved == JOptionHelperExtended.NO){
			if (dialogHelper.yesOrNo(I18n.t("app.are_you_sure_to_remove_all"))) {
				parent.getCurrentTabEditor().removeAllTabs(false);
			}
		}

		return true;
	}

	public Vector<ThemesWrapper> initComboStyleScheme() {
		Vector<ThemesWrapper> wrappers = new Vector<ThemesWrapper>();
		try {
			wrappers = ThemesWrapper.getDefaultAndCustomWrappers();
			logger.info("\n\n !! Custom themes initialized from folder: " + GeneralConfig.THEMES_FOLDER_CUSTOM + "\n");
		} catch (IOException e1) {
			wrappers = ThemesWrapper.getDefaultWrappers();
			logger.error("Error retrieving themes, using defaults from "+GeneralConfig.THEMES_FOLDER_DEFAULT
					+"... custom folder with errors was: " + GeneralConfig.THEMES_FOLDER_CUSTOM);
		}
		comboStylesScheme.setModel(new DefaultComboBoxModel<ThemesWrapper>(wrappers));

		comboStylesScheme.setSelectedItem(Themes.getThemesWrapperFromName(currentConfiguration.getTextEditorOpt().selectedTheme, wrappers));
		comboStylesScheme.revalidate();
		comboStylesScheme.repaint();

		themeList = wrappers;

		return wrappers;
	}

	public void setComboFileFormatByIndex(SupportedFileTextEditor fileType) {
		for(int i = 0; i < comboFileFormat.getItemCount(); i++) {
			if(comboFileFormat.getItemAt(i).equals(fileType)) {
				comboFileFormat.setSelectedIndex(i);
			}
		}
	}

	public void initGuiFromConfig(boolean refreshTheme) {
		final TextEditorOption txtOptions = currentConfiguration.getTextEditorOpt();

		SwingUtilities.invokeLater(() -> {

			slider_margin.setValue(txtOptions.getMargin());
			sliderFontSize.setValue(txtOptions.getFont().getSize());
			// AUTO LANGUAGE SYNC
			chckbxLockLanguage.setSelected(!txtOptions.defaultAutoSyntaxSync);

			if (refreshTheme) {
				comboStylesScheme.setSelectedItem(ThemesWrapper.getFromListByPath(txtOptions.selectedTheme, themeList));
			}

			if(chckbxLockLanguage.isSelected()) {
				comboFileFormat.setSelectedItem(SupportedFileTextEditor.valueOf(currentConfiguration.getTextEditorOpt().preferredSyntaxIfNotSync));
			}

			textEditorUtils.updateMargin(); // SetValue does not trigger action itself

			parent.revalidate();
			parent.repaint();

			textEditorUtils.setFooterVisibile(currentConfiguration.getTextEditorOpt().footerVisible);
			textEditorUtils.setHeaderVisibileLevel1(currentConfiguration.getTextEditorOpt().headerVisible);
			textEditorUtils.setHeaderVisibileLevel2(currentConfiguration.getTextEditorOpt().subHeaderVisible);

		});
	}

	public SupportedFileTextEditor getCurrentFileType() {
		return (SupportedFileTextEditor)comboFileFormat.getSelectedItem();
	}

	/** Pop-up with the available spellchecker languages, shown right after enabling the spellchecker. */
	private void showSpellCheckerLanguageMenu() {
		FileNamed[] dicts = RsyntaxUtils.getSpellCheckerDIctionaries();
		if (dicts == null || dicts.length < 2) {
			return;
		}

		String current = new File(currentConfiguration.getTextEditorOpt().prefDictspellChecker).getAbsolutePath();
		JPopupMenu menu = new JPopupMenu();
		ButtonGroup group = new ButtonGroup();

		for (FileNamed dict : dicts) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(RsyntaxUtils.getDictionaryLanguage(dict.mFile));
			item.setSelected(dict.mFile.getAbsolutePath().equals(current));
			item.addActionListener(ev -> {
				String path = GeneralConfig.SPELLCHECKER_DICT_FOLDER + dict.mFile.getName();
				currentConfiguration.getTextEditorOpt().prefDictspellChecker = path;
				currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "prefDictspellChecker", path);
				parent.getCurrentTabEditor().loadedTabObj.updateSpellCheckerParser(path, true);
			});
			group.add(item);
			menu.add(item);
		}

		menu.show(toggleButtonSpellCheck, 0, toggleButtonSpellCheck.getHeight());
	}
}
