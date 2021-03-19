package gui;

import java.awt.Font;
import java.io.File;

import gui.dialogs.msg.JOptionHelper;

public class ThemeEditorTest {
	
	public static File themeFolderCustom = new File("./themes/custom");
	public static File themeFolderDefault = new File("./themes");
	
	public static void main(String[] args) {
		SchemeManagerGui schemeManager = new SchemeManagerGui(themeFolderCustom, themeFolderDefault, new Font("Segoe UI", Font.PLAIN, 25));
		schemeManager.setCloseAction(() -> {
			new JOptionHelper(null).info("Close action triggered!");
		});
	}

}
