package gui;

import java.io.File;

public class ThemeEditorTest {
	
	public static File themeFolderCustom = new File("./themes/custom");
	
	public static void main(String[] args) {
		StyleManagerGui styleManager = new StyleManagerGui(themeFolderCustom);
	}

}
