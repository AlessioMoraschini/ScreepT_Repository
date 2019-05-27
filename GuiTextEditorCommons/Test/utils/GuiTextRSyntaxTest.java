package utils;

import java.awt.Toolkit;

import javax.swing.JFrame;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class GuiTextRSyntaxTest extends JFrame{

	public static void main(String[] args) {
		GuiTextRSyntaxTest testFrame = new GuiTextRSyntaxTest();
		testFrame.setVisible(true);
		testFrame.setBounds(0,0,Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
	}

	public GuiTextRSyntaxTest(){
		RSyntaxTextArea txtArea = new RSyntaxTextArea();
		txtArea.setAutoIndentEnabled(true);
		txtArea.setAntiAliasingEnabled(true);
		txtArea.setCodeFoldingEnabled(true);
		txtArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		AutoComplete autoComplete = new AutoComplete(txtArea);
		autoComplete.installGrammar("ProgrammingLanguagesWords/Java.md");
		
		RTextScrollPane scroller = new RTextScrollPane(txtArea);
		
		add(scroller);
	}
}
