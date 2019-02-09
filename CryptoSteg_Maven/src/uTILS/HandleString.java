package uTILS;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class HandleString {

	// this array contains all char that trigger uppercase next char
	private static char [] upperCaseSignalList = {'.', '<', '>'};
	
	String currentString;	
	
	public HandleString(String input) {
		currentString = new String(input);
	}
	
	public static String xmlPrettyFormat(String input, int indent) {
	    try {
	    	
	        Source xmlInput = new StreamSource(new StringReader(input));
	        StringWriter stringWriter = new StringWriter();
	        StreamResult xmlOutput = new StreamResult(stringWriter);
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        transformerFactory.setAttribute("indent-number", indent);
	        Transformer transformer = transformerFactory.newTransformer(); 
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.transform(xmlInput, xmlOutput);
	        String outXML = xmlOutput.getWriter().toString();
	        return outXML.substring(38,outXML.length());
	    } catch (Exception e) {
	        return "ERROR :("; // simple exception handling, please review it
	    }
	}
	
	/**
	 * this method extract from the source string the elements starting from N elements, and return it. 
	 * the elements are calculated as separated by the given separator.
	 * @param source
	 * @param separator
	 * @param start integer representening from which element String elements have to be kept.
	 * @return new string with only elements from start
	 */
	public static String retrieveFromNelements(String source, String separator, int start) {
		try {
			StringTokenizer tokenizer = new StringTokenizer(source, separator);
			String result = "";
			int i= 0;
			while(tokenizer.hasMoreTokens()) {
				if(i>=start) {
					result += tokenizer.nextToken();
				}
				i++;
			}
			return result;
		} catch (NoSuchElementException e) {
			return source;
		} catch(NullPointerException e) {
			return source;
		}
	}
	
	/**
	 * this method extract from the source string the elements starting from N elements to end element, and return it. 
	 * the elements are calculated as separated by the given separator.
	 * @param source
	 * @param separator
	 * @param start integer representening from which element String elements have to be kept.
	 * @return new string with only elements from start
	 */
	public static String retrieveFromNelements(String source, String separator, int start, int end) {
		try {
			StringTokenizer tokenizer = new StringTokenizer(source, separator);
			String result = "";
			int i= 0;
			while(tokenizer.hasMoreTokens()) {
				if(i>=start && i<=end) {
					result += tokenizer.nextToken();
				}
				i++;
			}
			return result;
		} catch (NoSuchElementException e) {
			return source;
		} catch(NullPointerException e) {
			return source;
		}
	}

	public static String xmlPrettyFormat(String input) {
	    return xmlPrettyFormat(input, 2);
	}
	
	
	public static String uppercaseEveryFirstLetterNoSpace(String sourceString) {
		String result="";
		String separator=" ";
		StringTokenizer tokenizer = new StringTokenizer(sourceString, separator);
		while(tokenizer.hasMoreTokens()) {
			String word = tokenizer.nextToken();
			int spaceEluder = 0;
			boolean eluded=false;
			while(!eluded && spaceEluder<=word.length()) { // elude spaces
				char first = word.charAt(spaceEluder);
				if(!String.valueOf(first).equals(separator)) {
					eluded=true;
					word=word.substring(0, spaceEluder)+String.valueOf(first).toUpperCase()+word.substring(spaceEluder+1, word.length());
				}
				//fine lettera
				spaceEluder++;
			}
			// fine parola
			if(tokenizer.hasMoreElements()) {
				result+=word+separator; // aggiungo spazio solo se sono alla fine
			}
			else {
				result+=word;
			}
		}
		
		return result;
	}
	
	public static String lowerCaseEveryFirstLetterNoSpace(String sourceString) {
		String result="";
		String separator=" ";
		StringTokenizer tokenizer = new StringTokenizer(sourceString, separator); 
		while(tokenizer.hasMoreTokens()) {
			String word = tokenizer.nextToken();
			int spaceEluder = 0;
			boolean eluded=false;
			while(!eluded && spaceEluder<=word.length()) { // elude spaces
				char first = word.charAt(spaceEluder);
				if(!String.valueOf(first).equals(separator)) {
					eluded=true;
					word=word.substring(0, spaceEluder)+String.valueOf(first).toLowerCase()+word.substring(spaceEluder+1, word.length());
				}
				//fine lettera
				spaceEluder++;
			}
			// fine parola
			if(tokenizer.hasMoreElements()) {
				result+=word+separator; // aggiungo spazio solo se la parola continua
			}
			else {
				result+=word;
			}
		}
		
		return result;
	}
	
	public static String removeSpacesAndLines(String source) {
		String result=""; 
		
		// for every char in string if flag is true makeUpper case, else add the same original char
		for(int i=0; i<source.length(); i++) {
			char character_i = source.charAt(i);			
			// finally update flag according to char value il blacklist
			if (!(character_i==' ' || character_i=='\n')){
				result+=String.valueOf(character_i);
			}
		}
		
		
		return result;
	}
	
	public static String removeLines(String source) {
		String result=""; 
		
		// for every char in string if flag is true makeUpper case, else add the same original char
		for(int i=0; i<source.length(); i++) {
			char character_i = source.charAt(i);			
			// finally update flag according to char value il blacklist
			if (!(character_i=='\n')){
				result+=String.valueOf(character_i);
			}
		}
		
		
		return result;
	}
	
	public static String upperAfterDots(String source, char trigger) {
		String result="";
		// first letter uppercase
		boolean flagNewUp = true; 
		
		// for every char in string if flag is true makeUpper case, else add the same original char
		for(int i=0; i<source.length(); i++) {
			char character_i = source.charAt(i);
			if (flagNewUp==true) {
				result+=String.valueOf(character_i).toUpperCase();
				// space and still to uppercase handler
				if(!(character_i==' '||character_i=='<'||character_i=='>'||character_i=='�'||character_i=='�')) {
					flagNewUp=false;
				}		
			}
			else {
				result+=String.valueOf(character_i);
			}
			
			// finally update flag according to char value il blacklist
			if (character_i==trigger || character_i=='?' || character_i=='!'){
				flagNewUp=true;
			}
		}
		
		
		return result;
	}
	
	/**
	 * This method changes the characters from start to end in the string with the given String
	 * 
	 * @param int start : 0<start<end<src.lenght
	 * @param int end : 0<start<end<src.lenght
	 */
	public static String injectStringInSelection(String src, String insert, int start, int end) {
		// check start to valid invalid case values
		if(start<0) {
			start=0;
		}
		if(start>end) {
			start=end;
		}
		if(start>src.length()) {
			start=src.length();
		}
		// check end to valid invalid case values
		if(end<0) {
			end=0;
		}
		if(end<start) {
			start=end;
		}
		if(end>src.length()) {
			end=src.length();
		}
		// initialize result with first segment that has to remain the same
		String result=src.substring(0, start);
		String second=src.substring(end, src.length());
		result+=insert+second;
		return result;
		
	}

	public static String lowerAfterDots(String source, char trigger) {
		String result="";
		// first letter lowercase
		boolean flagNewUp = true; 
		
		// for every char in string if flag is true makeLower case, else add the same original char
		for(int i=0; i<source.length(); i++) {
			char character_i = source.charAt(i);
			if (flagNewUp==true) {
				result+=String.valueOf(character_i).toLowerCase();
				// space and still to uppercase handler
				if(!(character_i==' '||character_i=='<'||character_i=='>'||character_i=='�'||character_i=='�')) {
					flagNewUp=false;
				}		
			}
			else {
				result+=String.valueOf(character_i);
			}
			
			// finally update flag according to char value il blacklist
			if (character_i==trigger || character_i=='?' || character_i=='!'){
				flagNewUp=true;
			}
		}
		
		
		return result;
	}

}
