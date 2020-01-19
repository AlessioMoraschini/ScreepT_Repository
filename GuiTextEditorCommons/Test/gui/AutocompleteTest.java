package gui;

import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import om.SupportedFileTextEditor;
import resources.GeneralConfig;
import utils.RsyntaxUtils;

public class AutocompleteTest {

	public static void main(String[] args) {
		try {
			
			GeneralConfig.initLazyGeneralConfig();
			
			JFrame frame = new JFrame();
			RSyntaxTextArea textArea = new RSyntaxTextArea();
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setViewportView(textArea);
			
			frame.add(scrollPane);
			
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			GuiUtils.centerComponent(frame, 60);
			
			textArea.setCloseMarkupTags(true);
			textArea.setSyntaxEditingStyle(SupportedFileTextEditor.JAVA.syntax);
			RsyntaxUtils.addAutoCompletion(textArea, SupportedFileTextEditor.JAVA.languageWordsFile);

			frame.setVisible(true);
			
		} catch (HeadlessException e) {
			e.printStackTrace();
		}
	}

}
