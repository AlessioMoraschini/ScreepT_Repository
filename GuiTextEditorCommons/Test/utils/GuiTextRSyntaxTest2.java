//package utils;
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.Toolkit;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.File;
//import java.io.IOException;
//
//import javax.script.ScriptEngine;
//import javax.script.ScriptEngineManager;
//import javax.swing.AbstractAction;
//import javax.swing.JButton;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JSplitPane;
//
//import org.fife.rsta.ac.AbstractSourceTree;
//import org.fife.rsta.ac.LanguageSupport;
//import org.fife.rsta.ac.LanguageSupportFactory;
//import org.fife.rsta.ac.html.HtmlCompletionProvider;
//import org.fife.rsta.ac.html.HtmlLanguageSupport;
//import org.fife.rsta.ac.js.JavaScriptCompletionProvider;
//import org.fife.rsta.ac.js.JavaScriptHelper;
//import org.fife.rsta.ac.js.JavaScriptLanguageSupport;
//import org.fife.rsta.ac.js.ast.JavaScriptVariableDeclaration;
//import org.fife.rsta.ac.js.ast.type.TypeDeclaration;
//import org.fife.rsta.ac.js.tree.JavaScriptOutlineTree;
//import org.fife.ui.rsyntaxtextarea.ErrorStrip;
//import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
//import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
//import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
//import org.fife.ui.rsyntaxtextarea.spell.SpellingParser;
//import org.fife.ui.rtextarea.RTextScrollPane;
//
//public class GuiTextRSyntaxTest2 extends JFrame{
//
//	public static void main(String[] args) throws IOException {
//		GuiTextRSyntaxTest2 testFrame = new GuiTextRSyntaxTest2();
//		testFrame.setVisible(true);
//		testFrame.setBounds(0,0,Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
//	}
//
//	public GuiTextRSyntaxTest2() throws IOException{
//		RSyntaxTextArea txtArea = new RSyntaxTextArea();
//		txtArea.setAutoIndentEnabled(true);
//		txtArea.setPaintTabLines(true);
//		txtArea.setUseFocusableTips(true);
//		txtArea.setCloseMarkupTags(true);
//		txtArea.setMarkOccurrences(false);
//		txtArea.setPaintMarkOccurrencesBorder(true);
//		txtArea.setRightHandSideCorrection(0);
//		txtArea.setTemplatesEnabled(true);
//		txtArea.setAntiAliasingEnabled(true);
//		txtArea.setCodeFoldingEnabled(true);
//		txtArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
////		AutoComplete autoComplete = new AutoComplete(txtArea);
////		autoComplete.installGrammar("ProgrammingLanguagesWords/Java.md");
//		
//		RTextScrollPane scroller = new RTextScrollPane(txtArea);
//		scroller.setPreferredSize(new Dimension(500,500));
//		
//		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
//		splitPane.setTopComponent(scroller);
//		
//		JPanel panelB = new JPanel();
//		splitPane.setBottomComponent(panelB);
//		
//		JLabel label = new JLabel("I Am a new label");
//		JButton button = new JButton("Hide Test");
//		panelB.add(label);
//		panelB.add(button);
//		
//		add(splitPane);
////		splitPane.setDividerLocation(0.8);
//		splitPane.resetToPreferredSizes();
//		
////		JavaScriptLanguageSupport htmlsupport = (JavaScriptLanguageSupport) lsf.getSupportFor(SyntaxConstants.SYNTAX_STYLE_HTML);
////		htmlsupport.install(txtArea);
//		
////		JavaScriptOutlineTree tree= new JavaScriptOutlineTree();
////		tree.listenTo(txtArea);
////		HtmlCompletionProvider provider = new HtmlCompletionProvider();
////		provider.loadFromXML(LanguageSupport.class.getResourceAsStream("data/html.xml"));
////		HtmlLanguageSupport support = new HtmlLanguageSupport();
////		support.setAutoCompleteEnabled(true);
////		support.install(txtArea);
//	    
//	    
//	    // SPELL CHECK
//	    File zip = new File("english_dic.zip");
//	    boolean usEnglish = false; // "false" will use British English
//	    SpellingParser parser = SpellingParser.createEnglishSpellingParser(zip, usEnglish);
//	    txtArea.addParser(parser);
//	    scroller.setIconRowHeaderEnabled(true);
//	    parser.setSquiggleUnderlineColor(Color.red);
//	    scroller.setRowHeaderView(new ErrorStrip(txtArea));
//	    
//	    new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				parser.setEnabled(true);
//				parser.parse(new RSyntaxDocument(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT), SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
//				
//			}
//		}).start();
//		
//		button.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				if (splitPane.getTopComponent().isVisible()) {
//					splitPane.getTopComponent().hide();
//				}else{
//					splitPane.getTopComponent().show();
//				}
//			}
//		});
//		
//	}
//}
