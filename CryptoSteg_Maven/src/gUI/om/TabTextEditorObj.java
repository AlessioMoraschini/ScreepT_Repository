package gUI.om;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.undo.UndoManager;

import frameutils.GuiUtils;

public class TabTextEditorObj {

	public File loadedFile;
	public String loadedText;
	public JScrollPane currentScrollPane;
	public UndoManager undoManager;
	public boolean unsaved = false;
	
	public TabTextEditorObj(File file, String text) {
		currentScrollPane = new JScrollPane();
		currentScrollPane.setBorder(new LineBorder(Color.GRAY, 2));
		currentScrollPane.setBackground(Color.DARK_GRAY);
		currentScrollPane.setMaximumSize(new Dimension(GuiUtils.getScreenSize().width, GuiUtils.getScreenSize(70).height));
		loadedFile = file;
		loadedText = text;
		undoManager = new UndoManager();
		undoManager.setLimit(1000);
	}
}
