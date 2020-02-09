package gui;

import java.awt.Font;
import java.io.File;

public class ThemeEditorTest {
	
	public static File themeFolderCustom = new File("./themes/custom");
	
	public static void main(String[] args) {
		new SchemeManagerGui(themeFolderCustom, new Font("Segoe UI", Font.PLAIN, 25));
	}

}
