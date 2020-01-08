package om;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rsyntaxtextarea.spell.SpellingParser;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.RUndoManager;

import files.watcher.FileChangeMonitor;
import frameutils.utils.GuiUtilsExtended;
import gui.GuiUtils;
import mainobj.INItializer;
import resources.GeneralConfig;
import shortcuts.ICommonShortcuts;
import tabbededitor.IChangeMonManager;
import utility.StringWorker;
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

	// Normal Pojo fields
	public FileChangeMonitor fileMonitorManager;
	public File loadedFile;
	public RTextScrollPane currentScrollPane;
	public ColumnTextArea textArea;
	public RUndoManager undoManager;
	public FocusListener focusTxtAreaMonitor;
	public boolean unsaved = false;
	public Parser currentParser;
	
	public TabTextEditorObj(File file, String text, DocumentListener docListener, final INItializer configuration, IChangeMonManager checkChangesMonitor, ICommonShortcuts shortcutsManager) {
		
		loadedFile = file;
		this.checkChangesMonitor = checkChangesMonitor;
		this.shortcutsManager = shortcutsManager;
		
		lastLazyInitParserDict = configuration.getTextEditorOpt().prefDictspellChecker;
		
		textArea = new ColumnTextArea(GeneralConfig.RECTANGULAR_TXT_SELECTION_ALLOWED);
		textArea.setMinimumSize(new Dimension(300,100));
		textArea.setParserDelay(TXT_AREA_PARSER_DELAY);
		undoManager = new RUndoManager(textArea);
		undoManager.setLimit(GeneralConfig.UNDO_MAX_LIM);
		textArea.setEditable(true);
		
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
		loadedFile = null;
		undoManager.discardAllEdits();
		textArea.setText("");
		unsaved = false;
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
		textArea.setCloseMarkupTags(txtOptions.closeMarkupTags);
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
		
		textArea.setUseFocusableTips(true);
	}
	
	public void enrichJPopUpMenu(ArrayList<Component> menuItemList) {
		
		if (menuItemList != null) {
			for (Component menuItem : menuItemList) {
				textArea.getPopupMenu().add(menuItem);
			}
		}
	}
	
	private void defaultAddsToPopupMenu() {
		textArea.getPopupMenu().add(new JSeparator());
		JMenuItem showFileLineTerminators = new JMenuItem("View lineFeeds (From File)");
		showFileLineTerminators.addActionListener(getShowLineTerminatorsPreview(true));
		textArea.getPopupMenu().add(showFileLineTerminators);

		JMenuItem showTextLineTerminators = new JMenuItem("View lineFeeds (From Loaded Text)");
		showTextLineTerminators.addActionListener(getShowLineTerminatorsPreview(false));
		textArea.getPopupMenu().add(showTextLineTerminators);
	}
	
	public AbstractAction getShowLineTerminatorsPreview(boolean fromFile) {
		return new AbstractAction() {
			private static final long serialVersionUID = 5019052743964361436L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame frame = new JFrame(loadedFile != null ? loadedFile.getName() : "unsaved file");
				RSyntaxTextArea textAreaS = new RSyntaxTextArea();
				JScrollPane scrollPane = new JScrollPane(textAreaS);
				textAreaS.setBackground(new Color(254, 255, 205));
				
				if(fromFile)try {
					textAreaS.setText( loadedFile != null? StringWorker.makeCRLFvisible(loadedFile) : "Undefined File");
				} catch (IOException e1) {}
				else {
					textAreaS.setText( textArea != null? StringWorker.makeCRLFvisible(textArea.getText()) : "Text unavailable");
				}
				
				textAreaS.setFont(new Font("DejaVu Sans Mono", Font.PLAIN, 13));
				textAreaS.setCaretPosition(textArea.getCaretPosition());
				scrollPane.setViewportView(textAreaS);
				frame.add(scrollPane);
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setUndecorated(true);
				frame.setAlwaysOnTop(true);
				frame.setLocationRelativeTo(textArea);
				GuiUtils.addEscDisposeListener(frame);
				textArea.addMouseListener(GuiUtils.getClickOutDisposeListener(frame));
				textArea.addMouseMotionListener(GuiUtils.getMouseExitedDisposeListener(frame));
				textAreaS.addFocusListener(GuiUtils.getFocusOutDisposeListener(frame));
				GuiUtils.centerComponent(frame, 50);
				GuiUtils.moveFrame(frame, 0, 100);
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
}
