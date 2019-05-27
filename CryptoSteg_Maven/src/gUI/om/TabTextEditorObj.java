package gUI.om;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;

import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import frameutils.GuiUtils;
import gUI.panels.TextEditor;

public class TabTextEditorObj {
	
	public static final Font DEFAULT_FONT = new Font("Segoe UI", Font.BOLD, 13);

	public File loadedFile;
	public RTextScrollPane currentScrollPane;
	public RSyntaxTextArea textArea;
	public boolean unsaved = false;
	
	public TabTextEditorObj(File file, String text) {
		loadedFile = file;
		textArea = new RSyntaxTextArea();
		textArea.setCodeFoldingEnabled(true);
		textArea.setAntiAliasingEnabled(true);
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
		textArea.setAutoIndentEnabled(true);
		
		// TEXT AREA LISTENER
		textArea.getDocument().addDocumentListener(getChangeListener());
	}
	
	private DocumentListener getChangeListener() {
		return new DocumentListener() {
			
			public void removeUpdate(DocumentEvent e) {
				TextEditor.fileChangedLblUpdate();
			}
			public void insertUpdate(DocumentEvent e) {
				TextEditor.fileChangedLblUpdate();
			}
			public void changedUpdate(DocumentEvent e) {
				TextEditor.fileChangedLblUpdate();
			}
		};
	}
}
