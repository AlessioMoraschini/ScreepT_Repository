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
package gui.texteditor.commons.om;

import static java.awt.Event.ALT_MASK;
import static java.awt.Event.SHIFT_MASK;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rsyntaxtextarea.spell.SpellingParser;
import org.fife.ui.rtextarea.RTextScrollPane;

import gui.commons.frameutils.utils.GuiUtilsExtended;
import gui.commons.jmenu.BrowserOpenerMenu;
import gui.texteditor.commons.TextEditTooltips;
import gui.texteditor.commons.tabbededitor.IChangeMonManager;
import gui.texteditor.commons.tabbededitor.ITabsManager;
import gui.texteditor.commons.utils.RsyntaxUtils;
import initializer.configs.impl.INItializer;
import initializer.configs.impl.om.TextEditorOption;
import os.commons.utils.SysUtils;
import plugin.api.AbstractPluginApplicationApi;
import plugin.api.AbstractPluginApplicationApi.PluginType;
import plugin.api.PluginJMenuCreator;
import plugin.external.arch.IPlugin;
import plugin.external.arch.IPluginParentExecutor.FunctionType;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import various.common.light.files.FileVarious;
import various.common.light.files.watcher.FileChangeMonitor;
import various.common.light.gui.GuiUtils;
import various.common.light.gui.MenuSetter;
import various.common.light.gui.RXTextUtilities;
import various.common.light.gui.dnd.FileDrop;
import various.common.light.om.ICommonShortcuts;
import various.common.light.om.SelectionDtoFull;
import various.common.light.utility.string.StringWorker;
import various.common.light.utility.string.StringWorker.EOL;
import various.common.light.utility.string.TextAreaStringEditor;
import various.common.light.utility.time.AmAsyncAction;

public class TabTextEditorObject {

	static Logger logger = Logger.getLogger(TabTextEditorObject.class);

	public static final Font DEFAULT_FONT = new Font("Segoe UI", Font.BOLD, 18);

	public static final int TXT_AREA_PARSER_DELAY = 500;

	public Parser lastLazyInitParser;
	public String lastLazyInitParserDict;

	// Interfaces for file monitor actions and shortcuts commons
	public ICommonShortcuts shortcutsManager;
	public IChangeMonManager checkChangesMonitor;

	public ITabsManager iTabsManager;

	// Normal Pojo fields
	public FileChangeMonitor fileMonitorManager;
	public File loadedFile;
	public RTextScrollPane currentScrollPane;
	public ColumnTextArea textArea;
//	public FocusListener focusTxtAreaMonitor;
	public boolean unsaved = false;
	public Parser currentParser;

	public FileDrop fileDrop;

	public INItializer currentConfig;

	private HyperlinkListener hyperLinkListener;
	private MouseMotionListener mouseMotionListener;
	private java.awt.event.MouseListener mouseListener;

	private TabTextEditorObject selfReference;

	private AtomicInteger lastLine;

	private SelectionDtoFull textAreaContentBackup;
	private int linePositionBackup = 0;

	public TabTextEditorObject(ITabsManager iTabsManager, File file, String text, DocumentListener docListener, final INItializer configuration, IChangeMonManager checkChangesMonitor, ICommonShortcuts shortcutsManager) {

		ColumnTextArea.UNDO_LIMIT = GeneralConfig.UNDO_MAX_LIM;

		selfReference = this;
		loadedFile = file;
		this.checkChangesMonitor = checkChangesMonitor;
		this.shortcutsManager = shortcutsManager;

		this.currentConfig = configuration;

		this.iTabsManager = iTabsManager;

		lastLazyInitParserDict = currentConfig.getTextEditorOpt().prefDictspellChecker;

		textArea = new ColumnTextArea(GeneralConfig.RECTANGULAR_TXT_SELECTION_ALLOWED, shortcutsManager);
		textArea.setMinimumSize(new Dimension(300,100));
		textArea.setParserDelay(TXT_AREA_PARSER_DELAY);
		textArea.setEditable(true);

		Border dropBorder = new MatteBorder(12,12,12,12, new Color(61, 111, 191,80));
		fileDrop = new FileDrop(textArea, dropBorder,
				(files)->{SwingUtilities.invokeLater(()->{iTabsManager.loadFiles(files);});});

		currentScrollPane = new RTextScrollPane(textArea);

		currentScrollPane.setBorder(new LineBorder(Color.GRAY, 2));
		currentScrollPane.setBackground(Color.DARK_GRAY);
		currentScrollPane.setMinimumSize(new Dimension(300,100));

		currentScrollPane.getGutter().setBackground(new Color(30,30,30));
		currentScrollPane.getGutter().setForeground(Color.LIGHT_GRAY);
		currentScrollPane.getGutter().setLineNumberFont(DEFAULT_FONT);

		currentScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		currentScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// TEXT AREA LISTENER
		textArea.getDocument().addDocumentListener(docListener);
//		focusTxtAreaMonitor = getFocusListenerTxtCursor();

		// TAB FOCUS CHANGES LISTENER
//		textArea.addFocusListener(focusTxtAreaMonitor);

		lastLine = new AtomicInteger(textArea.getCaretLineNumber());

		textArea.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				int currentLine = textArea.getCaretLineNumber();
				if(Math.abs(lastLine.get() - currentLine) > GeneralConfig.HISTORY_MOUSECLICK_LINES_DIFF_TO_ADD) {
					lastLine.set(currentLine);
					selfReference.iTabsManager.addToHystory(selfReference);
				}
			}
		});

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				defaultAddsToPopupMenu();
				updateTextAreaPreferences(currentConfig);
				textArea.repaint();
			}
		});
	}

	public void resetData() {
		try {
			loadedFile = null;
			textArea.setText("");
			unsaved = false;
			if (textArea.undoManager != null) {
				textArea.undoManager.discardAllEdits();
			}
		} catch (Exception e) {
			logger.error("An error ocurred while resetting data");
		}
	}

	public void updateSpellCheckerParser(String newDictionaryPath, boolean forceReload) {
		RsyntaxUtils.removeAllParsers(textArea);
		try {
			if (forceReload || lastLazyInitParser == null || !lastLazyInitParserDict.equals(newDictionaryPath)) {
				try {
					currentParser = RsyntaxUtils.createNapplySpellingParser(newDictionaryPath, textArea);
				} catch (IOException e) {
					logger.error("Error refreshing current spellChecker parser: ", e);
				}
			}else {
				try {
					RsyntaxUtils.applySpellingParser((SpellingParser)lastLazyInitParser, textArea);
				} catch (IOException e) {
					logger.error("Error refreshing current lazyInitialized spellChecker parser: ", e);
				}
			}
		} catch (Exception e) {
		}

	}

	public boolean storeSelectionDto(boolean onlyCaretPsition) {
		if(textArea != null) {
			if(!onlyCaretPsition)
				textAreaContentBackup = new SelectionDtoFull(textArea);

			linePositionBackup = textArea.getCaretPosition();
		}
		else return false;

		return true;
	}

	public boolean restoreSelectionDto(boolean lineOnly) {
		boolean done = true;
		if(textAreaContentBackup == null)
			done = storeSelectionDto(lineOnly);

		if(lineOnly) {
			textArea.setCaretPosition(linePositionBackup);
			RXTextUtilities.centerLineInScrollPane(textArea);
		} else {
			textAreaContentBackup.applySelectionDTO(textArea);
		}

		return done;
	}

	public synchronized void freeRenderingMemory(boolean runGc) {
		storeSelectionDto(false);
		textArea.setText(null);
		if(runGc) {
			System.runFinalization();
			Runtime.getRuntime().gc();
		}
	}

	public synchronized void resetFromRenderingMemory() {
		restoreSelectionDto(false);
	}

	public FocusListener getFocusListenerTxtCursor() {
		return new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
					}
				});
			}

			@Override
			public void focusGained(FocusEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						checkChangesMonitor.checkChanges();
						textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
					}
				});
			}
		};
	}


	public void updateTextAreaPreferences(INItializer configuration) {

		TextEditorOption txtOptions = configuration.getTextEditorOpt();

		textArea.setTabSize(txtOptions.tabSize);
		textArea.setPaintTabLines(txtOptions.isTabLineVisible());
		textArea.setAntiAliasingEnabled(txtOptions.antialiasedTxtArea);
		textArea.setCodeFoldingEnabled(txtOptions.codeFoldingEnabled);
		textArea.setAutoIndentEnabled(txtOptions.autoIndentEnabled);
		textArea.setMarkOccurrences(txtOptions.markOccurrences);
		textArea.setCloseCurlyBraces(txtOptions.closeCurlyBraces);
		textArea.setAntiAliasingEnabled(txtOptions.antialiasedTxtArea);
		textArea.setAnimateBracketMatching(txtOptions.animateBracketMatching);
		textArea.setBracketMatchingEnabled(txtOptions.bracketMatchingEnabled);
		textArea.setPaintMatchedBracketPair(txtOptions.paintMatchedBracketPair);
		textArea.setFadeCurrentLineHighlight(txtOptions.fadeCurrentLineHighlight);
		textArea.setUseFocusableTips(txtOptions.useFocusableTips);
		textArea.setHighlightSecondaryLanguages(txtOptions.highlightSecondaryLanguages);
		textArea.setFractionalFontMetricsEnabled(txtOptions.fractionalFontMetricsEnabled);
		textArea.setTabsEmulated(txtOptions.tabSpacesEmulated);
		hyperLinksEnabled(txtOptions.hyperLinksEnabled);
		textArea.setShowMatchedBracketPopup(txtOptions.showBracketsPopup);
		textArea.setEOLMarkersVisible(txtOptions.markEndOfLine);
		textArea.setDragEnabled(true);
		textArea.setMarkOccurrencesDelay(200);
		textArea.setPaintMarkOccurrencesBorder(txtOptions.paintMarkOccurrencesBorder);
		textArea.setRoundedSelectionEdges(txtOptions.roundedSelectionEdges);
		textArea.setClearWhitespaceLinesEnabled(txtOptions.clearWhitespaceLinesEnabled);
		textArea.setWhitespaceVisible(txtOptions.whiteSpaceVisible);
		textArea.setLocale(configuration.getGenOpt().getLocale());
		textArea.setCloseMarkupTags(txtOptions.closeMarkupTags);
		ColumnTextArea.INTERLINE = txtOptions.interline;

		textArea.setUseFocusableTips(true);

		textArea.repaint();
	}

	public void enrichJPopUpMenu(ArrayList<Component> menuItemList) {

		if (menuItemList != null) {
			for (Component menuItem : menuItemList) {
				textArea.getPopupMenu().add(menuItem);
			}
		}
	}

	private void defaultAddsToPopupMenu() {

		int iconGap = 10;
		try {
			JMenuItem undoItem = (JMenuItem)textArea.getPopupMenu().getComponent(0);
			undoItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNDO));
			undoItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_Z, KeyEvent.CTRL_MASK));

			JMenuItem redoItem = (JMenuItem)textArea.getPopupMenu().getComponent(1);
			redoItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REDO));
			redoItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_Y, KeyEvent.CTRL_MASK));

			JMenuItem cutItem = (JMenuItem)textArea.getPopupMenu().getComponent(3);
			cutItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CUT));
			cutItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_X, KeyEvent.CTRL_MASK));

			JMenuItem copyItem = (JMenuItem)textArea.getPopupMenu().getComponent(4);
			copyItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COPY));
			copyItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_C, KeyEvent.CTRL_MASK));

			JMenuItem pasteItem = (JMenuItem)textArea.getPopupMenu().getComponent(5);
			pasteItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PASTE));
			pasteItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_V, KeyEvent.CTRL_MASK));

			JMenuItem selectAllItem = (JMenuItem)textArea.getPopupMenu().getComponent(8);
			selectAllItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_A, KeyEvent.CTRL_MASK));

		} catch (Exception e) {}

		// Copy with syntax highlighting
		JMenuItem copySyntaxHighlightItem = new JMenuItem("Copy with syntax highlighting");
		copySyntaxHighlightItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PAINT_PALETTE));
		copySyntaxHighlightItem.addActionListener((e) -> {
			textArea.copyAsStyledText();
		});
		textArea.getPopupMenu().add(new JSeparator());
		textArea.getPopupMenu().add(copySyntaxHighlightItem);

		// OPEN IN WEB BROWSER
		BrowserOpenerMenu browserOpener = new BrowserOpenerMenu("Open with browser", (Insets)null, (Font)null, () -> {
			try {
				return loadedFile.getCanonicalPath();
			} catch (IOException e1) {
				return null;
			}
		});
		browserOpener.setIcon(new ImageIcon(IconsPathConfigurator.ICON_NETWORK));
		textArea.getPopupMenu().add(browserOpener);


		// PLUGINS
		JMenu pluginsMenu = new JMenu("Plugins");
		textArea.getPopupMenu().add(pluginsMenu);

		Set<IPlugin> plugins = AbstractPluginApplicationApi.getAvailablePlugins(PluginType.TEXT_EDITOR, false, true);
		Iterator<IPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IPlugin plugin = iterator.next();
			if (plugin != null) {

				pluginsMenu.add(getPluginsMenu(
								FunctionType.TEXT_AREA_RIGHT_CLICK_MENU,
								getSelectionDto(),
								plugin));
			}
		}

		// Color picker
		JMenuItem showColorPicker = new JMenuItem("Color picker/converter");
		showColorPicker.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COLOR_SAMPLER));
		showColorPicker.addActionListener((e)->{
			iTabsManager.openColorPicker();
		});
		textArea.getPopupMenu().add(showColorPicker);

		// EOL

		JMenuItem showFileLineTerminators = new JMenuItem("View lineFeeds (From File)");
		showFileLineTerminators.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LINES_FILE));
		showFileLineTerminators.addActionListener(getShowLineTerminatorsPreview(true));

		JMenuItem showTextLineTerminators = new JMenuItem("View lineFeeds (From Loaded Text)");
		showTextLineTerminators.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LINES_SCREEN));
		showTextLineTerminators.addActionListener(getShowLineTerminatorsPreview(false));

		textArea.getPopupMenu().add(new JSeparator());
		JMenu eolActions = new JMenu("Normalize End Of Line");
		textArea.getPopupMenu().add(showFileLineTerminators);
		textArea.getPopupMenu().add(showTextLineTerminators);
		textArea.getPopupMenu().add(new JSeparator());
		textArea.getPopupMenu().add(eolActions);
		enrichPopUpMenuEolActions(eolActions);
		eolActions.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				enrichPopUpMenuEolActions(eolActions);
			}
		});

		// TEXT TRANSFORMATION
		final JMenu menuStringActions = new JMenu("Text transformation");

		textArea.getPopupMenu().add(menuStringActions);
		enrichPopUpMenuStringActions(menuStringActions);
		menuStringActions.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						enrichPopUpMenuStringActions(menuStringActions);
					}
				});
			}
		});

		JMenuItem filePropertiesItem = new JMenuItem("File properties");
		filePropertiesItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
		filePropertiesItem.addActionListener((e)->{
			try {
				ICommonShortcuts.triggerKeyEvent(true, true, false, true, false, KeyEvent.VK_ENTER);
			} catch (AWTException e1) {
				logger.error("Can't trigger properties frame keyAction");
			}
		});
		textArea.getPopupMenu().add(new JSeparator());
		textArea.getPopupMenu().add(filePropertiesItem);

		MenuSetter.setIconTextGap(textArea.getPopupMenu(), iconGap);
		MenuSetter.setMargin(textArea.getPopupMenu(), new Insets(3, 3, 3, 3));
		MenuSetter.setFont(textArea.getPopupMenu(), new Font("Segoe UI", Font.PLAIN, 16));


		filePropertiesItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_ENTER, ALT_MASK));
		copySyntaxHighlightItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_C, ALT_MASK));
		showColorPicker.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_P, KeyEvent.CTRL_MASK | SHIFT_MASK | ALT_MASK));


		// SHOW/HIDE listener
		textArea.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				String selection = textArea.getSelectedText();
				if(!StringWorker.isEmpty(selection)) {
					copySyntaxHighlightItem.setEnabled(true);
				} else {
					copySyntaxHighlightItem.setEnabled(false);
				}

				if(FileVarious.validateFileOrDir(loadedFile)) {
					showFileLineTerminators.setEnabled(true);
					filePropertiesItem.setEnabled(true);
					browserOpener.setEnabled(true);
				} else {
					showFileLineTerminators.setEnabled(false);
					filePropertiesItem.setEnabled(false);
					browserOpener.setEnabled(false);
				}
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
	}

	public SelectionDtoFull getSelectionDto() {
		return new SelectionDtoFull(textArea, loadedFile);
	}

	public static JMenu getPluginsMenu(FunctionType type, Object param, IPlugin plugin) {
		JMenu newOccurrence = new PluginJMenuCreator()
				.getPluginMenu(param, type, plugin, false);

		newOccurrence.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
		MenuSetter.setFont(newOccurrence, GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
		if(!StringWorker.isEmpty(plugin.getTooltip()))
			newOccurrence.setToolTipText(plugin.getTooltip());

		return newOccurrence;
	}

	public void enrichPopUpMenuStringActions(final JMenu stringActions) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				stringActions.removeAll();

				final JMenuItem uppercaseItem = new JMenuItem(GuiUtils.encapsulateInHtml("<b>TT</b> - Uppercase/Lowercase text"));
				uppercaseItem.setToolTipText(TextEditTooltips.tooltipTT);
				uppercaseItem.addActionListener(getUppercaseListener());

				final JMenuItem uppercaseFirstItem = new JMenuItem(GuiUtils.encapsulateInHtml("<b>Tt Tt</b> - Uppercase first letters"));
				uppercaseFirstItem.setToolTipText(TextEditTooltips.tooltipTtTt);
				uppercaseFirstItem.addActionListener(getWordsUppercaseListener());

				final JMenuItem uppercasePostDotsItem = new JMenuItem(GuiUtils.encapsulateInHtml("<b>.Tt</b> - Uppercase after significant brokers"));
				uppercasePostDotsItem.setToolTipText(TextEditTooltips.tooltipUpperPostDots);
				uppercasePostDotsItem.addActionListener(getPostDotsUppercaseListener());

				final JMenuItem lowercasePostDotsItem = new JMenuItem(GuiUtils.encapsulateInHtml("<b>.tt</b> - Lowercase after significant brokers"));
				uppercasePostDotsItem.setToolTipText(TextEditTooltips.tooltipLowerPostDots);
				lowercasePostDotsItem.addActionListener(getPostDotsLowercaseListener());

				final JMenuItem encapsulateTag = new JMenuItem(GuiUtils.encapsulateInHtml("<b>Sorround with tag</b> - Ctrl+Alt+T"));
				encapsulateTag.addActionListener(getEncapsulateTagListener());

				String tabHint = "(See tabSize in textEditor subHead panel)";

				String tooltipTabsToSpaces = "Replace every tab with " + textArea.getTabSize() + " spaces " + tabHint;
				final JMenuItem replaceTabsWithSpaces = new JMenuItem(tooltipTabsToSpaces);
				replaceTabsWithSpaces.setToolTipText(tooltipTabsToSpaces);
				replaceTabsWithSpaces.addActionListener(
					e -> {
						int linePosition = textArea.getCaretLineNumber();
						textArea.setText(StringWorker.replaceTabsWithSpaces(textArea.getText(), textArea.getTabSize()));
						resetPositionAndRevalidate(linePosition);
					});

				String tooltipSpacesToTabs = "Replace every " + textArea.getTabSize() + " spaces with a tab " + tabHint;
				final JMenuItem replaceSpacesWithTabs = new JMenuItem(tooltipSpacesToTabs);
				replaceSpacesWithTabs.setToolTipText(tooltipSpacesToTabs);
				replaceSpacesWithTabs.addActionListener(
					e -> {
						int linePosition = textArea.getCaretLineNumber();
						textArea.setText(StringWorker.replaceSpacesWithTabs(textArea.getText(), textArea.getTabSize()));
						resetPositionAndRevalidate(linePosition);
					});

				stringActions.add(uppercaseItem);
				stringActions.add(uppercaseFirstItem);
				stringActions.add(uppercasePostDotsItem);
				stringActions.add(lowercasePostDotsItem);
				stringActions.add(encapsulateTag);
				stringActions.add(new JSeparator());
				stringActions.add(replaceTabsWithSpaces);
				stringActions.add(replaceSpacesWithTabs);

				MenuSetter.setMargin(stringActions, new Insets(3, 3, 3, 3));
				MenuSetter.setFont(stringActions, new Font("Segoe UI", Font.PLAIN, 16));
			}
		});
	}

	private void resetPositionAndRevalidate(int linePosition) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				shortcutsManager.revalidateUnsaved();
				textArea.requestFocus();
				try {
					textArea.setCaretPosition(textArea.getLineStartOffset(linePosition));
					RXTextUtilities.centerLineFullInScrollPane(textArea);
				} catch (BadLocationException e1) {
					logger.warn("Cannot reset old line: " + linePosition);
				}
				textArea.revalidate();
				textArea.repaint();
			}
		});
	}

	private void enrichPopUpMenuEolActions(JMenu eolActions) {
		eolActions.removeAll();

		JMenuItem replacePrintedToLF = new JMenuItem("Normalize to LF (\\n) - Linux's default");
		replacePrintedToLF.addActionListener(getReplaceEol(EOL.LF, false));
		JMenuItem replacePrintedToCR = new JMenuItem("Normalize to Mac CR (\\r) - Old MAC's default");
		replacePrintedToCR.addActionListener(getReplaceEol(EOL.CR, false));
		JMenuItem replacePrintedToCRLF = new JMenuItem("Normalize to Windows CRLF (\\r\\n) - Windows' default");
		replacePrintedToCRLF.addActionListener(getReplaceEol(EOL.CRLF, false));

		JSeparator separator = new JSeparator();

		JMenuItem replacePrintedToFileLF = new JMenuItem("Normalize to LF and write to file (\\n) - Linux's default");
		replacePrintedToFileLF.addActionListener(getReplaceEol(EOL.LF, true));
		JMenuItem replacePrintedToFileCR = new JMenuItem("Normalize to Mac CR and write to file (\\r) - Old MAC's default");
		replacePrintedToFileCR.addActionListener(getReplaceEol(EOL.CR, true));
		JMenuItem replacePrintedToFileCRLF = new JMenuItem("Normalize to Windows CRLF and write to file (\\r\\n) - Windows' default");
		replacePrintedToFileCRLF.addActionListener(getReplaceEol(EOL.CRLF, true));

		eolActions.add(replacePrintedToLF);
		eolActions.add(replacePrintedToCR);
		eolActions.add(replacePrintedToCRLF);

		eolActions.add(separator);

		eolActions.add(replacePrintedToFileLF);
		eolActions.add(replacePrintedToFileCR);
		eolActions.add(replacePrintedToFileCRLF);

		MenuSetter.setMargin(eolActions, new Insets(3, 3, 3, 3));
		MenuSetter.setFont(eolActions, new Font("Segoe UI", Font.PLAIN, 16));
	}

	private AbstractAction getReplaceEol(EOL eolType, boolean saveToFile) {
		return new AbstractAction() {
			private static final long serialVersionUID = -6402837968440937281L;

			@Override
			public void actionPerformed(ActionEvent e) {

				textArea.setText(StringWorker.normalizeStringToEol(textArea.getText(), eolType));
				if(saveToFile) {
					shortcutsManager.saveAsShortcut(eolType);
				}
			}
		};
	}

	private AbstractAction getShowLineTerminatorsPreview(boolean fromFile) {
		return new AbstractAction() {
			private static final long serialVersionUID = 5019052743964361436L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame frame = new JFrame(loadedFile != null ? loadedFile.getName() : "unsaved file");
				RSyntaxTextArea textAreaPopUp = new RSyntaxTextArea();
				JScrollPane scrollPane = new JScrollPane(textAreaPopUp);
				textAreaPopUp.setBackground(new Color(254, 255, 205));
				textAreaPopUp.setMargin(new Insets(12, 12, 12, 12));

				if(fromFile)try {
					textAreaPopUp.setText( loadedFile != null? StringWorker.makeCRLFvisible(loadedFile) : "Undefined File");
				} catch (IOException e1) {}
				else {
					textAreaPopUp.setText( textArea != null? StringWorker.makeCRLFvisible(textArea.getText()) : "Text unavailable");
					textAreaPopUp.setCaretPosition(textArea.getCaretPosition());
				}

				textAreaPopUp.setFont(new Font("DejaVu Sans Mono", Font.PLAIN, 13));
				scrollPane.setViewportView(textAreaPopUp);
				frame.add(scrollPane);
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setUndecorated(true);
				frame.setAlwaysOnTop(true);
				GuiUtils.centerComponent(frame, 65);
				frame.setLocationRelativeTo(textArea.getTopLevelAncestor());
				GuiUtils.addEscAdapter(frame);
				try {
					if(mouseListener != null)
						textArea.removeMouseListener(mouseListener);
					if(mouseMotionListener != null)
						textArea.removeMouseMotionListener(mouseMotionListener);
				} catch (Exception e1) {
					logger.error("Can't remove mouse listeners from textarea");
				}
				textArea.addMouseListener(GuiUtils.getClickOutDisposeListener(frame));
				textArea.addMouseMotionListener(GuiUtils.getMouseExitedDisposeListener(frame));
				textAreaPopUp.addFocusListener(GuiUtils.getFocusOutDisposeListener(frame));
				frame.setVisible(true);

			}
		};
	}

	public void hyperLinksEnabled(boolean enabled) {
		textArea.setHyperlinksEnabled(enabled);
		if (enabled) {
			hyperLinkListener = hyperLinkListener != null ? hyperLinkListener : new HyperlinkListener() {
				AtomicBoolean canOpenAgain = new AtomicBoolean(true);
				AmAsyncAction timer = new AmAsyncAction(() -> {canOpenAgain.set(true);}, 4000);

				@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if(e != null && e.getURL() != null && canOpenAgain.get()) {
						canOpenAgain.set(false);
						timer.executeDelayedAsync();
						SysUtils.openUrlInBrowserNative(e.getURL().toString());
					}
				}
			};
			textArea.addHyperlinkListener(hyperLinkListener);
		} else if(hyperLinkListener != null){
			try {
				textArea.removeHyperlinkListener(hyperLinkListener);
			} catch (Exception e) {
				logger.error("Cannot remove hyperlink listener from textarea");
			}
		}
	}

	@Override
	public String toString() {
		try {
			return "TabTextEditorObj [loadedFile=" + loadedFile + ", currentScrollPane=" + currentScrollPane
					+ ", textArea=" + textArea + ", unsaved=" + unsaved + "]";
		} catch (Exception e) {
			return "EXCEPTION_WHILE_LOGGING";
		}
	}

	public ActionListener getLowercaseListener() {
		return e -> {
			int linePosition = textArea.getCaretLineNumber();
			TextAreaStringEditor.makeLowercaseSelection(textArea);
			resetPositionAndRevalidate(linePosition);
			shortcutsManager.revalidateUnsaved();
		};
	}

	public ActionListener getUppercaseListener() {
		return e -> {
			int linePosition = textArea.getCaretLineNumber();
			TextAreaStringEditor.makeToggleUppercase(textArea);
			resetPositionAndRevalidate(linePosition);
			shortcutsManager.revalidateUnsaved();
		};
	}

	public ActionListener getWordsUppercaseListener() {
		return e -> {
			int linePosition = textArea.getCaretLineNumber();
			TextAreaStringEditor.makeWordsUppercase(textArea);
			resetPositionAndRevalidate(linePosition);
			shortcutsManager.revalidateUnsaved();
		};
	}

	public ActionListener getPostDotsUppercaseListener() {
		return e -> {
			int linePosition = textArea.getCaretLineNumber();
			TextAreaStringEditor.makeUppercasePostDotsNSeparators(textArea);
			resetPositionAndRevalidate(linePosition);
			shortcutsManager.revalidateUnsaved();
		};
	}

	public ActionListener getPostDotsLowercaseListener() {
		return e -> {
			int linePosition = textArea.getCaretLineNumber();
			TextAreaStringEditor.makeLowercasePostDotsNSeparators(textArea);
			resetPositionAndRevalidate(linePosition);
			shortcutsManager.revalidateUnsaved();
		};
	}

	public ActionListener getEncapsulateTagListener() {
		return e -> {
			boolean htmlLike = false;
			if(textArea.getSyntaxEditingStyle().equals(SyntaxConstants.SYNTAX_STYLE_XML) ||
			   textArea.getSyntaxEditingStyle().equals(SyntaxConstants.SYNTAX_STYLE_MXML) ||
			   textArea.getSyntaxEditingStyle().equals(SyntaxConstants.SYNTAX_STYLE_HTML)) {
				htmlLike = true;
			}
			int linePosition = textArea.getCaretLineNumber();
			Vector<String> tags = currentConfig.getTextEditorOpt().lastTagsUsed.getList();
			String tag = RsyntaxUtils.askForStringComboNullable(currentScrollPane, tags, "Specify the tag to use for sorrounding", "Specify tag");

//				String tag = new JOptionHelper(currentScrollPane).askForStringNullable("Specify the tag to use for sorrounding", "Specify tag");
			if(tag != null) {
				tags.add(tag);
				currentConfig.getTextEditorOpt().lastTagsUsed.setList(tags);
				currentConfig.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "lastTagsUsed", currentConfig.getTextEditorOpt().lastTagsUsed.toCharSeparatedString(";"));

				String text = StringWorker.trimToEmpty(textArea.getSelectedText());
				tag = htmlLike ? GuiUtils.encapsulateInTag(text, tag) : tag+text+tag;
				int selStart = textArea.getSelectionStart();
				textArea.replaceSelection(tag);
				resetPositionAndRevalidate(linePosition);
				shortcutsManager.revalidateUnsaved();

				int selLength = tag.length();

				GuiUtils.launchThreadSafeSwing(()-> {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) {}
					textArea.setSelectionStart(selStart);
					textArea.setSelectionEnd(selStart + selLength);
				});
			}
		};
	}
}
