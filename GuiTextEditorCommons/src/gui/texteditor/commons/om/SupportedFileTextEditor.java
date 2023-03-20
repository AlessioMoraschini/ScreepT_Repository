/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package gui.texteditor.commons.om;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import gui.commons.dialogutils.ExtensionFileFilter;
import resources.GeneralResourcesPaths;
import resources.IconsPathConfigurator;

public enum SupportedFileTextEditor {
									 ASSEMBLY_X86(".asm","asm", SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86, null, IconsPathConfigurator.ICON_CONSOLE),
									 ASSEMBLY_6502(".asm","asm", SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_6502, null, IconsPathConfigurator.ICON_CONSOLE),
									 BATCH(".bat","bat", SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH, GeneralResourcesPaths.WORDLIST_BASH, IconsPathConfigurator.ICON_CONSOLE),
									 BASH(".sh","sh", SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL, GeneralResourcesPaths.WORDLIST_BASH, IconsPathConfigurator.ICON_CONSOLE),
									 C(".c","c", SyntaxConstants.SYNTAX_STYLE_C, GeneralResourcesPaths.WORDLIST_C, null),
									 CONFIG(".config", "config", SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE, null, IconsPathConfigurator.F_ICON_PROPERTIES),
									 CLOJURE(".clj","clj", SyntaxConstants.SYNTAX_STYLE_CLOJURE, null, null),
									 CPLUSPLUS(".cpp","cpp", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS, GeneralResourcesPaths.WORDLIST_Cpp, null),
									 CPLUSPLUS_H(".h","h", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS, GeneralResourcesPaths.WORDLIST_Cpp, null),
									 CSHARP(".cs","cs", SyntaxConstants.SYNTAX_STYLE_CSHARP, GeneralResourcesPaths.WORDLIST_Csharp, null),
									 CSS(".css","css", SyntaxConstants.SYNTAX_STYLE_CSS, GeneralResourcesPaths.WORDLIST_CSS, IconsPathConfigurator.F_ICON_CSS),
									 CSV(".csv","csv", SyntaxConstants.SYNTAX_STYLE_CSV, null, null),
									 D(".d","d", SyntaxConstants.SYNTAX_STYLE_D, null, null),
									 DTD(".dtd","dtd", SyntaxConstants.SYNTAX_STYLE_DTD, null, IconsPathConfigurator.F_ICON_CODE),
									 DELPHI(".dpr","dpr", SyntaxConstants.SYNTAX_STYLE_DELPHI, null, null),
									 DOCKER(".yml","yml", SyntaxConstants.SYNTAX_STYLE_DOCKERFILE, null, null),
									 GO(".go","go", SyntaxConstants.SYNTAX_STYLE_GO, null, null),
									 GROOVY(".groovy","groovy", SyntaxConstants.SYNTAX_STYLE_GROOVY, null, null),
									 HOSTS(".hosts", "hosts", SyntaxConstants.SYNTAX_STYLE_HOSTS, null, null),
									 HTACCESS(".htaccess", "htaccess", SyntaxConstants.SYNTAX_STYLE_HTACCESS, null, null),
									 HTML(".html","html", SyntaxConstants.SYNTAX_STYLE_HTML, GeneralResourcesPaths.WORDLIST_HTML, IconsPathConfigurator.F_ICON_HTML),
									 INI(".ini", "ini", SyntaxConstants.SYNTAX_STYLE_INI, null, null),
									 JAVA(".java","java", SyntaxConstants.SYNTAX_STYLE_JAVA, GeneralResourcesPaths.WORDLIST_JAVA, null),
									 JS(".js","js", SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT, GeneralResourcesPaths.WORDLIST_JS, IconsPathConfigurator.F_ICON_JS),
									 JSON(".json","json", SyntaxConstants.SYNTAX_STYLE_JSON, GeneralResourcesPaths.WORDLIST_JS,IconsPathConfigurator.F_ICON_JSON),
									 JSON_WITH_COMMENTS(".json","json", SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS, GeneralResourcesPaths.WORDLIST_JS,IconsPathConfigurator.F_ICON_JSON),
									 JSP(".jsp","jsp", SyntaxConstants.SYNTAX_STYLE_JSP, GeneralResourcesPaths.WORDLIST_JAVA, null),
									 LATEX(".tex","tex", SyntaxConstants.SYNTAX_STYLE_LATEX, null, null),
									 LISP(".lisp","lisp", SyntaxConstants.SYNTAX_STYLE_LISP, null, null),
									 LUA(".lua","lua", SyntaxConstants.SYNTAX_STYLE_LUA, null, null),
									 MARKDOWN(".md","md", SyntaxConstants.SYNTAX_STYLE_MARKDOWN, null, null),
									 NONE(".txt",".txt", SyntaxConstants.SYNTAX_STYLE_NONE, null, IconsPathConfigurator.ICON_NEW_FILE),
									 PERL(".pl", "pl", SyntaxConstants.SYNTAX_STYLE_PERL, null, null),
									 PHP(".php", "php", SyntaxConstants.SYNTAX_STYLE_PHP, GeneralResourcesPaths.WORDLIST_PHP, IconsPathConfigurator.F_ICON_PHP),
									 PROPERTIES(".properties", "properties", SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE, null, IconsPathConfigurator.F_ICON_PROPERTIES),
									 PYTHON(".py", "py", SyntaxConstants.SYNTAX_STYLE_PYTHON, GeneralResourcesPaths.WORDLIST_PYTHON, null),
									 SCALA(".scala","scala", SyntaxConstants.SYNTAX_STYLE_SCALA, null, null),
									 SQL(".sql", "sql", SyntaxConstants.SYNTAX_STYLE_SQL, GeneralResourcesPaths.WORDLIST_SQL, IconsPathConfigurator.F_ICON_DATABASE),
									 TYPESCRIPT(".ts","ts", SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT, GeneralResourcesPaths.WORDLIST_JS, IconsPathConfigurator.F_ICON_HTML),
									 TXT(".txt","txt", SyntaxConstants.SYNTAX_STYLE_NONE, null, IconsPathConfigurator.F_ICON_TXT),
									 VISUAL_BASIC(".vb","vb", SyntaxConstants.SYNTAX_STYLE_VISUAL_BASIC, null, null),
									 XML(".xml","xml", SyntaxConstants.SYNTAX_STYLE_XML, null, IconsPathConfigurator.F_ICON_CODE),
									 XSD(".xsd","xsd", SyntaxConstants.SYNTAX_STYLE_XML, null, IconsPathConfigurator.F_ICON_CODE),
									 WSDL(".wsdl","wsdl", SyntaxConstants.SYNTAX_STYLE_XML, null, IconsPathConfigurator.F_ICON_CODE),
									 YAML(".yml","yml", SyntaxConstants.SYNTAX_STYLE_YAML, null, null);

	public final String DEFAULT_ICON = IconsPathConfigurator.F_ICON_TXT;
	public String extension;
	public String senzapunto;
	public String syntax;
	public String languageWordsFile;
	public String iconPath;

	SupportedFileTextEditor(String extension, String senzapunto, String syntax, String languageFile, String iconPath) {
		this.extension = extension;
		this.senzapunto = senzapunto;
		this.syntax = syntax;
		this.languageWordsFile = languageFile;
		this.iconPath =  (iconPath == null)? DEFAULT_ICON : iconPath;
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
			dotsExtensions.add(current.extension);
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

	public static boolean isSupported(File target) {
		boolean supported = false;
		if(target != null && target.exists()) {
			String extension = FilenameUtils.getExtension(target.getName());
			for (String supportedExt : getSupportedExtensions()) {
				if (supportedExt.equalsIgnoreCase(extension)) {
					supported = true;
					break;
				}
			}
		}

		return supported;
	}

	public ExtensionFileFilter getRelatedFilter() {

		for(SupportedFileTextEditor type : values()) {
			if (this.equals(type)) {
				return new ExtensionFileFilter(new String[] { type.senzapunto }, type.coolDescription());
			}
		}

		return new ExtensionFileFilter(new String[] { TXT.senzapunto }, TXT.coolDescription());
	}

	public static ExtensionFileFilter[] getMultipleFileFilter() {
		SupportedFileTextEditor[] values = values();
		ExtensionFileFilter[] extensionList = new ExtensionFileFilter[values.length];

		int i = 0;
		for(SupportedFileTextEditor type : values) {
			extensionList[i] = new ExtensionFileFilter(new String[] {type.senzapunto}, type.coolDescription());
			i++;
		}

		return extensionList;
	}

	public static ExtensionFileFilter[] getMultipleFileFilter(SupportedFileTextEditor... values) {
		ExtensionFileFilter[] extensionList = new ExtensionFileFilter[values.length];

		int i = 0;
		for(SupportedFileTextEditor type : values) {
			extensionList[i] = new ExtensionFileFilter(new String[] {type.senzapunto}, type.coolDescription());
			i++;
		}

		return extensionList;
	}

	public String coolDescription() {
		return this.name() + " (" + this.extension + ")";
	}

}
