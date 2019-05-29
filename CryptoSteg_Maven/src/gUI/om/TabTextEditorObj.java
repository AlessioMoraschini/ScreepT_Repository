package gUI.om;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;

import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

public class TabTextEditorObj {
	
	public static final Font DEFAULT_FONT = new Font("Segoe UI", Font.BOLD, 13);

	public static final int TXT_AREA_BASE = 0;
	public static final int TXT_AREA_MEDIUM = 1;
	public static final int TXT_AREA_HIGH = 2;
	public static final int TXT_AREA_MAX = 3;
	
	public int txtAreaInitType = TXT_AREA_HIGH; // default value
	
	public File loadedFile;
	public RTextScrollPane currentScrollPane;
	public RSyntaxTextArea textArea;
	public boolean unsaved = false;
	
	public TabTextEditorObj(File file, String text, DocumentListener docListener) {
		loadedFile = file;
		textArea = new RSyntaxTextArea();
		textArea.setMinimumSize(new Dimension(500,300));
		
		
		currentScrollPane = new RTextScrollPane(textArea);
		currentScrollPane.setBorder(new LineBorder(Color.GRAY, 2));
		currentScrollPane.setBackground(Color.DARK_GRAY);
		currentScrollPane.setMinimumSize(new Dimension(500,300));
		currentScrollPane.getGutter().setBackground(new Color(30,30,30));
		currentScrollPane.getGutter().setForeground(Color.LIGHT_GRAY);
		currentScrollPane.getGutter().setFont(DEFAULT_FONT);
		currentScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		currentScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		textArea.setCodeFoldingEnabled(true);
		textArea.setDragEnabled(true);// try
		textArea.setUseFocusableTips(true);
		textArea.setMarkOccurrences(true);
		textArea.setPaintMarkOccurrencesBorder(true);
		textArea.setCloseMarkupTags(true);
		textArea.setCloseCurlyBraces(true);
		textArea.setFadeCurrentLineHighlight(true);
		textArea.setRoundedSelectionEdges(true);
		textArea.setAntiAliasingEnabled(true);
		textArea.setAnimateBracketMatching(true);
		textArea.setAutoIndentEnabled(true);
		textArea.setBracketMatchingEnabled(true);
		textArea.setCloseMarkupTags(true);
		textArea.setAutoIndentEnabled(true);
		
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
					break;
				}case 1:{
					textArea.setCodeFoldingEnabled(true);
					textArea.setAutoIndentEnabled(true);
					textArea.setMarkOccurrences(true);
					textArea.setCloseMarkupTags(true);
					textArea.setCloseCurlyBraces(true);
					textArea.setCloseMarkupTags(true);
					textArea.setAntiAliasingEnabled(true);
					break;
				}case 2:{
					textArea.setCodeFoldingEnabled(true);
					textArea.setAutoIndentEnabled(true);
					textArea.setMarkOccurrences(true);
					textArea.setCloseMarkupTags(true);
					textArea.setCloseCurlyBraces(true);
					textArea.setCloseMarkupTags(true);
					textArea.setAntiAliasingEnabled(true);
					textArea.setAnimateBracketMatching(true);
					textArea.setBracketMatchingEnabled(true);
					textArea.setFadeCurrentLineHighlight(true);
					textArea.setUseFocusableTips(true);
					break;
				}case 3:{
					textArea.setCodeFoldingEnabled(true);
					textArea.setAutoIndentEnabled(true);
					textArea.setMarkOccurrences(true);
					textArea.setCloseMarkupTags(true);
					textArea.setCloseCurlyBraces(true);
					textArea.setCloseMarkupTags(true);
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
}
