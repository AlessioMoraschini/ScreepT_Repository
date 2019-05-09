package gUI.om;

import java.awt.Color;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.undo.UndoManager;

public class TabTextEditorObj {

	public File loadedFile;
	public String loadedText;
	public JScrollPane currentScrollPane;
	public UndoManager undoManager;
	public boolean unsaved = false;
	
	public TabTextEditorObj(File file, String text) {
		currentScrollPane = new JScrollPane();
		currentScrollPane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		currentScrollPane.setBackground(Color.DARK_GRAY);
		loadedFile = file;
		loadedText = text;
		undoManager = new UndoManager();
		undoManager.setLimit(1000);
	}
}
