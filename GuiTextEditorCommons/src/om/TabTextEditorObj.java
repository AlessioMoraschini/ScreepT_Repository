package om;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;

import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rsyntaxtextarea.spell.SpellingParser;
import org.fife.ui.rtextarea.RTextScrollPane;

import amfileutils.filechangewatcher.FileChangeMonitor;
import mainobj.INItializer;
import resources.GeneralConfig;
import shortcuts.ICommonShortcuts;
import tabbededitor.IChangeMonManager;
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
		
		updateTextAreaPreferences(configuration.getTextEditorOpt());
		
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
	
	public void updateTextAreaPreferences(TextEditorOption txtOptions) {
		
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
		textArea.setFadeCurrentLineHighlight(txtOptions.fadeCurrentLineHighlight);
		textArea.setUseFocusableTips(txtOptions.useFocusableTips);
		textArea.setRoundedSelectionEdges(txtOptions.roundedSelectionEdges);
		textArea.setPaintMarkOccurrencesBorder(txtOptions.paintMarkOccurrencesBorder);
		textArea.setClearWhitespaceLinesEnabled(txtOptions.clearWhitespaceLinesEnabled);
		textArea.setHighlightSecondaryLanguages(txtOptions.highlightSecondaryLanguages);
		textArea.setFractionalFontMetricsEnabled(txtOptions.fractionalFontMetricsEnabled);
		textArea.setTabsEmulated(txtOptions.tabSpacesEmulated);
		hyperLinksEnabled(txtOptions.hyperLinksEnabled);
		textArea.setShowMatchedBracketPopup(txtOptions.showBracketsPopup);
		textArea.setEOLMarkersVisible(txtOptions.markEndOfLine);
		textArea.setDragEnabled(true);
		textArea.setMarkOccurrencesDelay(200);
		
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
