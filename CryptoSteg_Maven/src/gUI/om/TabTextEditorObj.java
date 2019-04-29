package gUI.om;

import java.io.File;

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
		loadedFile = file;
		loadedText = text;
		undoManager = new UndoManager();
		undoManager.setLimit(1000);
	}
}
