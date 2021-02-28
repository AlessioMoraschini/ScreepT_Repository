package gui;

import java.io.File;

import javax.swing.SwingUtilities;

import gui.filesearch.name.FileNameSearchDialog;

public class FileNameSearchGUITest {

	private static final File rootDir = new File("./");
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(()->{
			FileNameSearchDialog dialog = new FileNameSearchDialog(rootDir);
			dialog.setDoubleClickItemAction((e) ->{
				System.out.println("File clicked: " + e.getAbsolutePath());
			});
			dialog.setVisible(true);
		});
	}
}
