package om;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;

import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import utils.SysUtils;

public class TabTextEditorObj {
	
	public static final Font DEFAULT_FONT = new Font("Segoe UI", Font.BOLD, 18);

	// TEXT AREA DETAIL LEVELS
	public static final int TXT_AREA_BASE = 0;
	public static final int TXT_AREA_MEDIUM = 1;
	public static final int TXT_AREA_HIGH = 2;
	public static final int TXT_AREA_MAX = 3;
	
	public int txtAreaInitType = TXT_AREA_HIGH; // default value
	
	public static final boolean TXT_AREA_CLEAR_LINES_WSPACES_ONLY = false;
	public static final boolean TXT_AREA_HILIGHT_SECONDARY_LANGUAGE = false;
	public static final boolean TXT_AREA_FRACTIONAL_FONT_ENABLED = true;
	public static final int TXT_AREA_PARSER_DELAY = 500;
	public static final boolean TXT_AREA_HYPERLINKS_ENABLED = true;
	
	public File loadedFile;
	public RTextScrollPane currentScrollPane;
	public RSyntaxTextArea textArea;
	public boolean unsaved = false;
	
	public TabTextEditorObj(File file, String text, DocumentListener docListener, Integer initTxtAreaType) {
		loadedFile = file;
		txtAreaInitType = (initTxtAreaType != null && initTxtAreaType >= 0 && initTxtAreaType < 4)? initTxtAreaType : txtAreaInitType;
		textArea = new RSyntaxTextArea();
		textArea.setMinimumSize(new Dimension(300,100));
		textArea.setClearWhitespaceLinesEnabled(TXT_AREA_CLEAR_LINES_WSPACES_ONLY);
		textArea.setHighlightSecondaryLanguages(TXT_AREA_HILIGHT_SECONDARY_LANGUAGE);
		textArea.setParserDelay(TXT_AREA_PARSER_DELAY);
		textArea.setFractionalFontMetricsEnabled(TXT_AREA_FRACTIONAL_FONT_ENABLED);
		textArea.setHyperlinksEnabled(TXT_AREA_HYPERLINKS_ENABLED);
		textArea.addHyperlinkListener(new HyperlinkListener() {
			
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				SysUtils.openUrlInBrowserNative(e.getURL().toString());
			}
		});
		
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
		
		setPreferences(txtAreaInitType);
	}
	
	public void setPreferences(int type) {
		if(0 <= type && type < 4) {
			txtAreaInitType = type;
		}
		
		if(textArea != null) {
			switch(txtAreaInitType) {
				case 0:{
					textArea.setCodeFoldingEnabled(true);
					textArea.setAutoIndentEnabled(true);
					textArea.setMarkOccurrences(true);
					textArea.setCloseMarkupTags(true);
					textArea.setCloseCurlyBraces(true);
					textArea.setAntiAliasingEnabled(false);
					textArea.setAnimateBracketMatching(false);
					textArea.setBracketMatchingEnabled(false);
					textArea.setFadeCurrentLineHighlight(false);
					textArea.setUseFocusableTips(false);
					textArea.setRoundedSelectionEdges(false);
					textArea.setDragEnabled(false);// try
					textArea.setPaintMarkOccurrencesBorder(false);
					break;
				}case 1:{
					textArea.setCodeFoldingEnabled(true);
					textArea.setAutoIndentEnabled(true);
					textArea.setMarkOccurrences(true);
					textArea.setCloseMarkupTags(true);
					textArea.setCloseCurlyBraces(true);
					textArea.setAntiAliasingEnabled(true);
					textArea.setAnimateBracketMatching(false);
					textArea.setBracketMatchingEnabled(false);
					textArea.setFadeCurrentLineHighlight(false);
					textArea.setUseFocusableTips(false);
					textArea.setRoundedSelectionEdges(false);
					textArea.setDragEnabled(false);// try
					textArea.setPaintMarkOccurrencesBorder(false);
					break;
				}case 2:{
					textArea.setCodeFoldingEnabled(true);
					textArea.setAutoIndentEnabled(true);
					textArea.setMarkOccurrences(true);
					textArea.setCloseMarkupTags(true);
					textArea.setCloseCurlyBraces(true);
					textArea.setAntiAliasingEnabled(true);
					textArea.setAnimateBracketMatching(true);
					textArea.setBracketMatchingEnabled(true);
					textArea.setUseFocusableTips(true);
					textArea.setRoundedSelectionEdges(false);
					textArea.setDragEnabled(false);// try
					textArea.setPaintMarkOccurrencesBorder(false);
					break;
				}case 3:{
					textArea.setCodeFoldingEnabled(true);
					textArea.setAutoIndentEnabled(true);
					textArea.setMarkOccurrences(true);
					textArea.setCloseMarkupTags(true);
					textArea.setCloseCurlyBraces(true);
					textArea.setAntiAliasingEnabled(true);
					textArea.setAnimateBracketMatching(true);
					textArea.setBracketMatchingEnabled(true);
					textArea.setFadeCurrentLineHighlight(true);
					textArea.setUseFocusableTips(true);
					textArea.setRoundedSelectionEdges(true);
					textArea.setDragEnabled(true);// try
					textArea.setPaintMarkOccurrencesBorder(true);
					break;
				}
			}
		}
	}

	@Override
	public String toString() {
		try {
			return "TabTextEditorObj [txtAreaInitType=" + txtAreaInitType + ", loadedFile=" + loadedFile + ", currentScrollPane=" + currentScrollPane
					+ ", textArea=" + textArea + ", unsaved=" + unsaved + "]";
		} catch (Exception e) {
			return "EXCEPTION_WHILE_LOGGING";
		}
	}
}
