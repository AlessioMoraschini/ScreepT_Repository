package JUnit;

import java.io.File;
import java.io.IOException;

import javax.script.ScriptException;
import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.googlejavaformat.java.FormatterException;

import amfileutils.FileStuff;
import codeformatter.CustomCodeFormatter;
import testUtils.TestUtils;

public class CodeFormatTest {
	
	
	public static final String jsLibraryPath = "./Resources_ScreepT/lib/js/javascript.js";
	public static final String codeJavaSourceFilePath = "TestFiles/CodeFormatTest/formatTest.java";
	public static final String codeXmlSourceFilePath = "TestFiles/CodeFormatTest/formatTest.xml";
	public static final String codeJsSourceFilePath = "TestFiles/CodeFormatTest/formatTest.js";
	public static final String codeCssSourceFilePath = "TestFiles/CodeFormatTest/formatTest.css";
	public static final String codeHtmlSourceFilePath = "TestFiles/CodeFormatTest/formatTest.html";
	public static final boolean FOLDER_LIB = true;
	
	public File codeSourceFile;
	CustomCodeFormatter codeFormatter;
	
	@Before
	public void init() throws ScriptException, IOException {
		if (FOLDER_LIB) {
			codeFormatter = new CustomCodeFormatter("/Resources_ScreepT/lib/js/");
		}else{
			codeFormatter = new CustomCodeFormatter(jsLibraryPath);
		}
	}

	@Test
	public void testGoogleJavaConverter() throws IOException, FormatterException{
		codeSourceFile = new File(codeJavaSourceFilePath);
		String source = FileStuff.readFileAsString(codeSourceFile);
		String formatted = CustomCodeFormatter.googleJavaCodeFormat(source);
		
		TestUtils.printHeader("/", 50, "RAW SOURCE CODE - from " + codeSourceFile.getAbsolutePath());
		TestUtils.printl(source);

		TestUtils.printHeader("/", 50, "FORMATTED SOURCE CODE - from " + codeSourceFile.getAbsolutePath());
		TestUtils.printl(formatted);
	}

	@Test
	public void testJsConverter() throws IOException, FormatterException, NoSuchMethodException, ScriptException{
		codeSourceFile = new File(codeJsSourceFilePath);
		String source = FileStuff.readFileAsString(codeSourceFile);
		String formatted = (String)codeFormatter.jsFormatJSLIB(source);
		
		TestUtils.printHeader("/", 50, "RAW JS - SOURCE CODE - from " + codeSourceFile.getAbsolutePath());
		TestUtils.printl(source);
		
		TestUtils.printHeader("/", 50, "FORMATTED JS - SOURCE CODE - from " + codeSourceFile.getAbsolutePath());
		TestUtils.printl(formatted);
	}
	
	@Test
	public void testCssConverter() throws IOException, FormatterException, NoSuchMethodException, ScriptException{
		codeSourceFile = new File(codeCssSourceFilePath);
		String source = FileStuff.readFileAsString(codeSourceFile);
		String formatted = (String)codeFormatter.cssFormatJSLIB(source);
		
		TestUtils.printHeader("/", 50, "RAW CSS - SOURCE CODE - from " + codeSourceFile.getAbsolutePath());
		TestUtils.printl(source);
		
		TestUtils.printHeader("/", 50, "FORMATTED CSS - SOURCE CODE - from " + codeSourceFile.getAbsolutePath());
		TestUtils.printl(formatted);
	}
	
	@Test
	public void testHtmlConverter() throws IOException, FormatterException, NoSuchMethodException, ScriptException{
		codeSourceFile = new File(codeHtmlSourceFilePath);
		String source = FileStuff.readFileAsString(codeSourceFile);
		String formatted = (String)codeFormatter.htmlFormatJSLIB(source);
		
		TestUtils.printHeader("/", 50, "RAW HTML - SOURCE CODE - from " + codeSourceFile.getAbsolutePath());
		TestUtils.printl(source);
		
		TestUtils.printHeader("/", 50, "FORMATTED HTML - SOURCE CODE - from " + codeSourceFile.getAbsolutePath());
		TestUtils.printl(formatted);
	}

	@Test
	public void testXmlConverter() throws IOException, FormatterException, NoSuchMethodException, ScriptException, TransformerException{
		codeSourceFile = new File(codeXmlSourceFilePath);
		String source = FileStuff.readFileAsString(codeSourceFile);
		String formatted = CustomCodeFormatter.xmlPrettyFormat(source);
		
		TestUtils.printHeader("/", 50, "RAW XML - SOURCE CODE - from " + codeSourceFile.getAbsolutePath());
		TestUtils.printl(source);
		
		TestUtils.printHeader("/", 50, "FORMATTED XML - SOURCE CODE - from " + codeSourceFile.getAbsolutePath());
		TestUtils.printl(formatted);
	}
}
