package codeformatter;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;

import javascript.JsRunner;
import resources.GeneralConfig;

public class CustomCodeFormatter {
	
	public static final String ERROR = "ERROR :(";
	
	public JsRunner jsRunner;
	public int indentSize = 3; // default
	
	
	public CustomCodeFormatter(String jsLibFolder) {
		jsRunner = new JsRunner();
		
		if (jsLibFolder != null && new File(jsLibFolder).exists()) {
			jsRunner.initRunnerFromFolderLib(jsLibFolder);
		}else {
			jsRunner.initRunnerFromFolderLib(GeneralConfig.JS_LIB_FOLDER);
		}
	}

	public static String xmlPrettyFormat(String input) throws TransformerException {
		return xmlPrettyFormat(input, 2);
	}
	
	public static String xmlPrettyFormat(String input, int indent) throws TransformerException {
    	Transformer transformer = TransformerFactory.newInstance().newTransformer();
    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    	transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indent));
    	//initialize StreamResult with File object to save to file
    	StreamResult result = new StreamResult(new StringWriter());
    	Document inputParsedDoc = loadXMLfromString(input);
		Node root = inputParsedDoc.getFirstChild();
    	DOMSource source = new DOMSource(root);
    	transformer.transform(source, result);
    	return result.getWriter().toString();
	}
	
	public static Document loadXMLfromFile(String filePath) throws Exception {
	    DocumentBuilderFactory docFactory
	        = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	    return docBuilder.parse(new File(filePath));
	}
	
	public static Document loadXMLfromString(String xmlString){
		
        //Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         
        //API to obtain DOM Document instance
        DocumentBuilder builder = null;
        try{
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();
             
            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

	
	/**
	 * Uses editor standard code formatter to format given code and return back formatted string
	 * @throws FormatterException 
	 */
	public static String googleJavaCodeFormat(String toFormat) throws FormatterException{
		Formatter formatter =  new Formatter();
		return formatter.formatSource(toFormat);
	}
	
	/** JAVASCRIPT EXTERNAL CALLS - HTML/CSS/JS FORMATTER OR STRINGS 
	 * **/
	
	 /**
	 * @throws ScriptException 
	 * @throws NoSuchMethodException **/
	public String jsFormatJSLIB(String source, int indentSize) throws NoSuchMethodException, ScriptException {
		this.indentSize = indentSize;
		return (String)jsRunner.invokeFunction("js_beautify", source);
	}
	
	public String htmlFormatJSLIB(String source, int indentSize) throws NoSuchMethodException, ScriptException {
		this.indentSize = indentSize;
		return (String)jsRunner.invokeFunction("style_html", source);
	}
	
	public String cssFormatJSLIB(String source, int indentSize) throws NoSuchMethodException, ScriptException {
		this.indentSize = indentSize;
		return (String)jsRunner.invokeFunction("css_beautify", source);
	}
	
	public String getJsParams() {
		return "{"+
			      "'indent_size': " +indentSize+ ","+
			      "'indent_char': '\t',"+
			      "'selector_separator': '\t',"+
			      "'end_with_newline': true,"+
			      "'newline_between_rules': true,"+
			      "'space_around_selector_separator': true"+
			      "'indent_with_tabs': true"+
			    "}";
	}
}
