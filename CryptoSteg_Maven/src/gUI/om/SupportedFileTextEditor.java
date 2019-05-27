package gUI.om;

import java.util.ArrayList;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import resources.GeneralResourcesPaths;

public enum SupportedFileTextEditor {TXT(".txt","txt", SyntaxConstants.SYNTAX_STYLE_NONE, null),
									 XML(".xml","xml", SyntaxConstants.SYNTAX_STYLE_XML, null),
									 YAML(".yml","yml", SyntaxConstants.SYNTAX_STYLE_YAML, null),
									 HTML(".html","html", SyntaxConstants.SYNTAX_STYLE_HTML, GeneralResourcesPaths.WORDLIST_HTML),
									 JS(".js","js", SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT, GeneralResourcesPaths.WORDLIST_JS),
									 JSON(".json","json", SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS, GeneralResourcesPaths.WORDLIST_JS),
									 CSS(".css","css", SyntaxConstants.SYNTAX_STYLE_CSS, GeneralResourcesPaths.WORDLIST_CSS),
									 JAVA(".java","java", SyntaxConstants.SYNTAX_STYLE_JAVA, GeneralResourcesPaths.WORDLIST_JAVA),
									 JSP(".jsp","jsp", SyntaxConstants.SYNTAX_STYLE_JSP, GeneralResourcesPaths.WORDLIST_JAVA),
									 PHP(".php", "php", SyntaxConstants.SYNTAX_STYLE_PHP, GeneralResourcesPaths.WORDLIST_PHP),
									 PYTHON(".py", "py", SyntaxConstants.SYNTAX_STYLE_PYTHON, GeneralResourcesPaths.WORDLIST_PYTHON),
									 C(".c","c", SyntaxConstants.SYNTAX_STYLE_C, GeneralResourcesPaths.WORDLIST_C),
									 CSHARP(".cs","cs", SyntaxConstants.SYNTAX_STYLE_CSHARP, GeneralResourcesPaths.WORDLIST_Csharp),
									 CPLUSPLUS(".cpp","cpp", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS, GeneralResourcesPaths.WORDLIST_Cpp),
									 SQL(".sql", "sql", SyntaxConstants.SYNTAX_STYLE_SQL, GeneralResourcesPaths.WORDLIST_SQL),
									 BATCH(".bat","bat", SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH, GeneralResourcesPaths.WORDLIST_BASH),
									 BASH(".sh","sh", SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL, GeneralResourcesPaths.WORDLIST_BASH),
									 INI(".ini", "ini", SyntaxConstants.SYNTAX_STYLE_INI, null),
									 HOSTS(".hosts", "hosts", SyntaxConstants.SYNTAX_STYLE_HOSTS, null),
									 HTACCESS(".htaccess", "htaccess", SyntaxConstants.SYNTAX_STYLE_HTACCESS, null);
							   
	public String nomeStringa;
	public String senzapunto;
	public String syntax;
	public String languageWordsFile;
	
	SupportedFileTextEditor(String extension, String senzapunto, String syntax, String languageFile) {
		this.nomeStringa = extension;
		this.senzapunto = senzapunto;
		this.syntax = syntax;
		this.languageWordsFile = languageFile;
	}
	
	public static ArrayList<String> getSupportedExtensions(){
		ArrayList<String> extensions = new ArrayList<String>();
		
		for(SupportedFileTextEditor current : SupportedFileTextEditor.values()) {
			extensions.add(current.senzapunto);
		}
		
		return extensions;
	}
	
	public static ArrayList<String> getSupportedExtensionsWithDots(){
		ArrayList<String> dotsExtensions = new ArrayList<String>();
		
		for(SupportedFileTextEditor current : SupportedFileTextEditor.values()) {
			dotsExtensions.add(current.nomeStringa);
		}
		
		return dotsExtensions;
	}
	
	public static ArrayList<String> getSupportedSyntaxList(){
		ArrayList<String> syntaxList = new ArrayList<String>();
		
		for(SupportedFileTextEditor current : SupportedFileTextEditor.values()) {
			syntaxList.add(current.syntax);
		}
		
		return syntaxList;
	}
	
}
