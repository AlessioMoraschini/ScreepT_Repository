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
package om;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rsyntaxtextarea.spell.SpellingParser;
import org.fife.ui.rtextarea.RTextScrollPane;

import files.watcher.FileChangeMonitor;
import frameutils.utils.GuiUtilsExtended;
import gui.GuiUtils;
import gui.MenuSetter;
import gui.TextEditTooltips;
import gui.dnd.FileDrop;
import mainobj.INItializer;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import tabbededitor.IChangeMonManager;
import tabbededitor.ITabsManager;
import utility.string.StringWorker;
import utility.string.TextAreaStringEditor;
import utility.string.StringWorker.EOL;
import utils.RsyntaxUtils;
import utils.SysUtils;

public class TabTextEditorObj {
	
	static Logger logger = Logger.getLogger(TabTextEditorObj.class);
	
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
	public FocusListener focusTxtAreaMonitor;
	public boolean unsaved = false;
	public Parser currentParser;
	
	public FileDrop fileDrop;
	
	public TabTextEditorObj(ITabsManager iTabsManager, File file, String text, DocumentListener docListener, final INItializer configuration, IChangeMonManager checkChangesMonitor, ICommonShortcuts shortcutsManager) {
		
		ColumnTextArea.UNDO_LIMIT = GeneralConfig.UNDO_MAX_LIM;
		
		loadedFile = file;
		this.checkChangesMonitor = checkChangesMonitor;
		this.shortcutsManager = shortcutsManager;
		
		lastLazyInitParserDict = configuration.getTextEditorOpt().prefDictspellChecker;
		
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
		focusTxtAreaMonitor = getFocusListenerTxtCursor();
		
		// TAB FOCUS CHANGES LISTENER
		textArea.addFocusListener(focusTxtAreaMonitor);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				defaultAddsToPopupMenu();
				updateTextAreaPreferences(configuration);
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
	
	public FocusListener getFocusListenerTxtCursor() {
		return new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
					}
				});
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
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
			
			JMenuItem redoItem = (JMenuItem)textArea.getPopupMenu().getComponent(1);
			redoItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REDO));
			
			JMenuItem cutItem = (JMenuItem)textArea.getPopupMenu().getComponent(3);
			cutItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CUT));
			
			JMenuItem copyItem = (JMenuItem)textArea.getPopupMenu().getComponent(4);
			copyItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COPY));
			
			JMenuItem pasteItem = (JMenuItem)textArea.getPopupMenu().getComponent(5);
			pasteItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PASTE));
			
		} catch (Exception e) {}
		
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
		textArea.getPopupMenu().add(new JSeparator());
		final JMenu menuStringActions = new JMenu("Text transformation");
		
		textArea.getPopupMenu().add(menuStringActions);
		enrichPopUpMenuStringActions(menuStringActions);
		menuStringActions.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						enrichPopUpMenuStringActions(menuStringActions);
					}
				});
			}
		});
		
		MenuSetter.setIconTextGap(textArea.getPopupMenu(), iconGap);
		MenuSetter.setMargin(textArea.getPopupMenu(), new Insets(3, 3, 3, 3));
		MenuSetter.setFont(textArea.getPopupMenu(), new Font("Segoe UI", Font.PLAIN, 16));
	}
	
	public void enrichPopUpMenuStringActions(final JMenu stringActions) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {		
				stringActions.removeAll();
				
				final JMenuItem uppercaseItem = new JMenuItem(GuiUtils.encapsulateInHtml("<b>TT</b> - Uppercase text"));
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
			public void run() {
				shortcutsManager.revalidateUnsaved();
				textArea.requestFocus();
				try {
					textArea.setCaretPosition(textArea.getLineStartOffset(linePosition));
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
					shortcutsManager.saveAsShortcut();
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
				GuiUtils.addEscDisposeListener(frame);
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
			textArea.addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {
					SysUtils.openUrlInBrowserNative(e.getURL().toString());
				}
			});
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
}
