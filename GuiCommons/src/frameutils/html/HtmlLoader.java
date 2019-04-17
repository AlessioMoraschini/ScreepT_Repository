package frameutils.html;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.apache.log4j.Logger;

public class HtmlLoader {

	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(HtmlLoader.class);
		
	public static HTMLEditorKit editor;
	
	public static boolean loadHtmlInEditPane(JEditorPane panel, String pathToHtml) {
		boolean esit = true;
		
		try {
			panel.setPage(pathToHtml);
			logger.debug(pathToHtml + " : loaded!");
		} catch (IOException e) {
			esit = false;
			logger.error(e);
		}
		
		return esit;		
	}
	
	@Deprecated
	public static void loadHtmlInEditPaneV2(JEditorPane panel, String pathToHtml) throws MalformedURLException, IOException, BadLocationException {
		editor = new HTMLEditorKit();
		InputStream input = new URL(pathToHtml).openStream();
		
		editor.read(input, new HTMLDocument(), 0);
		panel.setEditorKit(editor);
	}
	
	public static void loadCss(JEditorPane panel, String pathToCss) throws MalformedURLException {
		
		editor = new HTMLEditorKit();
		StyleSheet css = new StyleSheet();
		css.importStyleSheet(new URL(pathToCss));
		editor.setStyleSheet(css);
		panel.setEditorKit(editor);
	}
}
