package variousutils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import resources.GeneralConfig;

@SuppressWarnings("deprecation")
public class HandleString {

	public static final String UNPRINTABLE_ESCAPES = "[^\\x20-\\x7e]";

	// this array contains all char that trigger uppercase next char
	String currentString;	
	
	public HandleString(String input) {
		currentString = new String(input);
	}
	
	public static String forceLength(String src, int length) {
		return replaceOccurrencesWithRandom(String.format("%1$"+length+ "s", src), " ").substring(0, length);
	}
	
	public static String forceLength(String src, int length, char[] possibleCharacters) {
		return replaceOccurrencesWithRandom(String.format("%1$"+length+ "s", src), " ", possibleCharacters).substring(0, length);
	}
	
	public static String replaceOccurrencesWithRandom(String source, String match, char[] possibleCharacters) {
		
		while(source.contains(match)){
			source = source.replaceFirst(match, RandomStringUtils.random(1, possibleCharacters));
		}
		
		return source;
	}

	public static String replaceOccurrencesWithRandom(String source, String match) {
		
		while(source.contains(match)){
			source = source.replaceFirst(match, RandomStringUtils.randomAscii(1));
		}
		
		return source;
	}
	
	public static String lowerAfterDots(String source, char trigger) {
		String result="";
		// first letter lowercase
		boolean flagNewUp = false; 
		boolean firstChanged = false;
		
		// for every char in string if flag is true makeLower case, else add the same original char
		for(int i=0; i<source.length(); i++) {
			char character_i = source.charAt(i);
			if(!firstChanged && Character.isAlphabetic(character_i)) {
				// First alphabetic letter is always changed
				result+=String.valueOf(character_i).toLowerCase();
				firstChanged = true;
				
			}else if (flagNewUp==true) {
				result+=String.valueOf(character_i).toLowerCase();
				// space and still to lowercase handler
				if(!(character_i == ' ') && !(GeneralConfig.TXT_EDITOR_UPPERCASE_SEPARATORS.contains(String.valueOf(character_i)))) {
					flagNewUp=false;
				}		
			} else {
				result+=String.valueOf(character_i);
			}
				
			// finally update flag according to char value il blacklist
			if (character_i==trigger || GeneralConfig.TXT_EDITOR_UPPERCASE_SEPARATORS.contains(String.valueOf(character_i))){
				flagNewUp=true;
			}
		}
		return result;
	}
	
	public static String upperAfterDots(String source, char trigger) {
		String result="";
		// first letter uppercase
		boolean flagNewUp = false; 
		boolean firstChanged = false;
		
		// for every char in string if flag is true makeUpper case, else add the same original char
		for(int i=0; i<source.length(); i++) {
			char character_i = source.charAt(i);
			
			if(!firstChanged && Character.isAlphabetic(character_i)) {
				// First alphabetic letter is always changed
				result+=String.valueOf(character_i).toUpperCase();
				firstChanged = true;
				
			} else if (flagNewUp==true) {
				result+=String.valueOf(character_i).toUpperCase();
				// space and still to uppercase handler
				if(!(character_i == ' ') && !(GeneralConfig.TXT_EDITOR_UPPERCASE_SEPARATORS.contains(String.valueOf(character_i)))) {
					flagNewUp=false;
				}		
				
			} else {
				result+=String.valueOf(character_i);
			}
			
			// finally update flag according to char value il blacklist
			if (character_i==trigger || GeneralConfig.TXT_EDITOR_UPPERCASE_SEPARATORS.contains(String.valueOf(character_i))){
				flagNewUp=true;
			}
		}
		
		
		return result;
	}
	
	/**
     * Convert given string to the same string but with replaced special chars with escape sequences.
     * The language used correspond to the type specified in type parameter.
     * @param source the source string into which replace found special chars
     * @param type the type of language to convert from/to
     * @return the source string with replaced special charachters
     */
    public static String replaceSpecialWithEscapes(String source, EscapeType type) {
    	if (EscapeType.HTML_3.equals(type)) {
			return StringEscapeUtils.escapeHtml3(source);
		}else if (EscapeType.HTML_4.equals(type)) {
			return StringEscapeUtils.escapeHtml4(source);
		}else if (EscapeType.ECMASCRIPT.equals(type)) {
			return StringEscapeUtils.escapeEcmaScript(source);
		}else if (EscapeType.JSON.equals(type)) {
			return StringEscapeUtils.escapeJson(source);
		}else if (EscapeType.JAVA.equals(type)) {
			return StringEscapeUtils.escapeJava(source);
		}else if (EscapeType.XML.equals(type)) {
			return StringEscapeUtils.escapeXml(source);
		}else if (EscapeType.XML_10.equals(type)) {
			return StringEscapeUtils.escapeXml10(source);
		}else if (EscapeType.XML_11.equals(type)) {
			return StringEscapeUtils.escapeXml11(source);
		}else {
			// if no match found
			return source;
		}
    }
    
    /**
     * Convert given string to the same string but with replaced escape sequences with original special chars.
     * The language used correspond to the type specified in type parameter.
     * @param source the source string into which replace found escape sequences.
     * @param type the type of language to convert from/to
     * @return the source string with replaced escape sequences.
     */
    public static String replaceEscapesWithSpecials(String source, EscapeType type) {
    	if (EscapeType.HTML_3.equals(type)) {
			return StringEscapeUtils.unescapeHtml3(source);
		}else if (EscapeType.HTML_4.equals(type)) {
			return StringEscapeUtils.unescapeHtml4(source);
		}else if (EscapeType.ECMASCRIPT.equals(type)) {
			return StringEscapeUtils.unescapeEcmaScript(source);
		}else if (EscapeType.JSON.equals(type)) {
			return StringEscapeUtils.unescapeJson(source);
		}else if (EscapeType.JAVA.equals(type)) {
			return StringEscapeUtils.unescapeJava(source);
		}else if (EscapeType.XML.equals(type) || EscapeType.XML_10.equals(type) || EscapeType.XML_11.equals(type)) {
			return StringEscapeUtils.unescapeXml(source);
		}else {
			// if no match found
			return source;
		}
    }
}
