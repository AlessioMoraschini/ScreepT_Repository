package gUI.om;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;

import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import frameutils.GuiUtils;
import gUI.panels.TextEditor;

public class TabTextEditorObj {
	
	public static final Font DEFAULT_FONT = new Font("Segoe UI", Font.PLAIN, 16);

	public File loadedFile;
	public RTextScrollPane currentScrollPane;
	public RSyntaxTextArea textArea;
	public UndoManager undoManager;
	public boolean unsaved = false;
	
	public TabTextEditorObj(File file, String text) {
		loadedFile = file;
		textArea = new RSyntaxTextArea();
		textArea.setCodeFoldingEnabled(true);
		textArea.setAntiAliasingEnabled(true);
		textArea.setMaximumSize(new Dimension(GuiUtils.getScreenSize().width-40, GuiUtils.getScreenSize(70).height));
		currentScrollPane = new RTextScrollPane(textArea);
		currentScrollPane.setBorder(new LineBorder(Color.GRAY, 2));
		currentScrollPane.setBackground(Color.DARK_GRAY);
		currentScrollPane.getGutter().setBackground(new Color(30,30,30));
		currentScrollPane.getGutter().setForeground(Color.LIGHT_GRAY);
		currentScrollPane.getGutter().setFont(DEFAULT_FONT);
		currentScrollPane.setMaximumSize(new Dimension(GuiUtils.getScreenSize().width-40, GuiUtils.getScreenSize(70).height));
		textArea.setAutoIndentEnabled(true);
		undoManager = new UndoManager();
		undoManager.setLimit(1000);
		
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
