package gUI.om;

import java.util.ArrayList;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

public enum SupportedFileTextEditor {TXT(".txt","txt", SyntaxConstants.SYNTAX_STYLE_NONE),
									 XML(".xml","xml", SyntaxConstants.SYNTAX_STYLE_XML),
									 HTML(".html","html", SyntaxConstants.SYNTAX_STYLE_HTML),
									 CSS(".css","css", SyntaxConstants.SYNTAX_STYLE_CSS),
									 JSON(".JSON","JSON", SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS),
									 JAVA(".java","java", SyntaxConstants.SYNTAX_STYLE_JAVA),
									 Cpp(".cpp","C++", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS),
									 CSHARP(".csh","C#", SyntaxConstants.SYNTAX_STYLE_CSHARP),
									 C(".h","C", SyntaxConstants.SYNTAX_STYLE_C),
									 JS(".js","js", SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
							   
	public String senzapunto;
	public String nomeStringa;
	public String syntax;
	
	SupportedFileTextEditor(String extension, String senzapunto, String syntax) {
		this.senzapunto = senzapunto;
		this.nomeStringa = extension;
		this.syntax = syntax;
	}
	
	public static ArrayList<String> getSupportedExtensions(){
		ArrayList<String> extensions = new ArrayList<String>();
		
		for(SupportedFileTextEditor current : SupportedFileTextEditor.values()) {
			extensions.add(current.senzapunto);
		}
		
		return extensions;
	}
}
