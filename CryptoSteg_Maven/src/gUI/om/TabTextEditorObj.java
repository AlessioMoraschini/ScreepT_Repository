package gUI.om;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

import javax.swing.border.LineBorder;
import javax.swing.undo.UndoManager;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import frameutils.GuiUtils;

public class TabTextEditorObj {

	public File loadedFile;
	public String loadedText;
	public RTextScrollPane currentScrollPane;
	public RSyntaxTextArea textArea;
	public UndoManager undoManager;
	public boolean unsaved = false;
	
	public TabTextEditorObj(File file, String text) {
		currentScrollPane = new RTextScrollPane();
		currentScrollPane.setBorder(new LineBorder(Color.GRAY, 2));
		currentScrollPane.setBackground(Color.DARK_GRAY);
		currentScrollPane.setMaximumSize(new Dimension(GuiUtils.getScreenSize().width, GuiUtils.getScreenSize(70).height));
		loadedFile = file;
		loadedText = text;
		textArea = new RSyntaxTextArea();
		textArea.setCodeFoldingEnabled(true);
		textArea.setAntiAliasingEnabled(true);
		textArea.setAutoIndentEnabled(true);
		undoManager = new UndoManager();
		undoManager.setLimit(1000);
	}
}
