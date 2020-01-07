package utility;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.swing.JTextArea;

public class StringWorker {

	private static SafeLogger logger = new SafeLogger(StringWorker.class);
	
	public static final String UNPRINTABLE_ESCAPES = "[^\\x20-\\x7e]";
	
	String currentString;
	
	public StringWorker(String input) {
		currentString = new String(input);
	}

	public static InputStream stringToInputStream(String src) throws IOException {
	    
		byte[] sourceBytes = src.getBytes(Charset.forName(System.getProperty("file.encoding")));
	    InputStream inputStream = new ByteArrayInputStream(sourceBytes);
	    
	    return inputStream;
	}

	public static String fitToMaxLentgh(String source, int lim) {
		String res = source;
		int length = source.length();
		
		if (length > lim) {
			int half = (int)(float)lim/2;
			res = source.substring(0, half-3)+".."+source.substring(length-half-2, length);
		}
		
		return res;
	}

	public static String forceLengthDeterministic(String src, int length, String padString) {
		return String.format("%1$"+length+ "s", src).substring(0, length).replaceAll(" ", padString);
	}

	/**
	 * this method returns an array of bytes of the same length of given string, even if the source has more bytes for each charachter (chinese, cyrillic , etc)
	 * @param source the source string
	 * @return null if source is null, array of first source.length bytes else
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] extractSameLengthByteArray(String source) throws UnsupportedEncodingException {
		if(source == null) {
			return null;
		}
		
		int sourceLength = source.length();
		byte[] extractedBytes = new byte[source.length()];
		ArrayList<Byte> extractedList = new ArrayList<Byte>();
		
		// extract bytes from source string
		for(int i=0; i<sourceLength; i++) {
			String currentChar = source.substring(i, i+1);
			byte[] byteChar = currentChar.getBytes("UTF-8");
			
			for(byte b : byteChar) {
				extractedList.add(b);
			}
		}
		
		// create array containing bytes
		int i = 0;
		for(byte by : extractedList) {
			if(i >= sourceLength) {
				break;
			}
			extractedBytes[i] = by;
			i++;
		}
		
		return extractedBytes;
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
	 * this function take a string parameter and a jtextarea to remove from all occurrences of parameter
	 * 
	 * @return the integer new selection end of the string
	 */
	public static int removeCharachterFromTxtArea(String toRemove, JTextArea textSourceArea, int startSel, int endSel) {
		textSourceArea.requestFocus();
		textSourceArea.setSelectionStart(startSel);
		textSourceArea.setSelectionEnd(endSel);
		String selText = textSourceArea.getSelectedText();
		String allText = textSourceArea.getText();
		
		int newEnd = 0;
		
		if (selText == null || selText.equals("")) {
			String text = allText;
			text = text.replaceAll(toRemove, "");
			textSourceArea.setText(text);
			newEnd = text.length();
			
		}else {
			
			String prefix = textSourceArea.getText().substring(0, startSel);
			String postFix = textSourceArea.getText().substring(endSel, allText.length());
			selText = selText.replaceAll(toRemove, "");
			newEnd = startSel + selText.length();
			
			textSourceArea.setText(prefix.concat(selText).concat(postFix));
		}
		return newEnd;
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

	public static String[] getUpperCaseArray(String[] sourceStringArr) {
		String[] uppercaseNames = new String[sourceStringArr.length];
		int i = 0;
		for(String s : sourceStringArr) {
			uppercaseNames[i] = s.toUpperCase();
			i++;
		}
		return uppercaseNames;
	}

	public static String[] getLowerCaseArray(String[] sourceStringArr) {
		String[] lowercaseNames = new String[sourceStringArr.length];
		int i = 0;
		for(String s : sourceStringArr) {
			lowercaseNames[i] = s.toLowerCase();
			i++;
		}
		return lowercaseNames;
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

	/**
	 * This method prints key and values from map
	 * @param map
	 */
	public static <K, V> void printMap(Map<K, V> map) {
	    for (Map.Entry<K, V> entry : map.entrySet()) {
	        System.out.println("Key : " + entry.getKey() 
				+ " <=> Value : " + entry.getValue());
	    }
	}

	/**
	 * This method prints values from generic array using toString
	 * @param array
	 */
	public static<E> void printArray(E[] array) {
		int i=0;
	    for (E curr : array) {
	        System.out.println("Val["+i+"] : " + curr);
	        i++;
	    }
	}

	/**
	 * This method prints string representation of int array using toString
	 * @param array
	 */
	public static void printArray(int[] array) {
		int i=0;
	    for (int curr : array) {
	    	System.out.println("Val["+i+"] : " + curr);
	        i++;
	    }
	}

	/**
	 * This method returns string representation from key and values from map
	 * @param map
	 */
	public static <K, V> String stringVal(Map<K, V> map) {
		String out="";
	    for (Map.Entry<K, V> entry : map.entrySet()) {
	        out+= "Key : " + entry.getKey() + " <=> Value : " + entry.getValue()+"\n";
	    }
	    return out;
	}

	/**
	 * This method returns string representation from generic array using toString
	 * @param array
	 */
	public static<E> String stringVal(E[] array) {
		int i=0;
		String out="";
	    for (E curr : array) {
	        out+="Val["+i+"] : " + curr+"\n";
	        i++;
	    }
	    return out;
	}

	/**
	 * This method returns string representation from int array using toString
	 * @param array
	 */
	public static String stringVal(int[] array) {
		int i=0;
		String out="";
	    for (int curr : array) {
	        out += "Val["+i+"] : " + curr+"\n";
	        i++;
	    }
	    return out;
	}

	public static boolean containsIgnoreCase(String first, String second) {
		if(first == null || second == null) {
			return false;
		}
		
		return first.toLowerCase().contains(second.toLowerCase());
	}

	public static boolean contains(String first, String second) {
		if(first == null || second == null) {
			return false;
		}
		
		return first.contains(second);
	}

	public static boolean isEmpty(String source) {
		return source != null && "".equals(source.trim());
	}

	public static String trimToEmpty(String source) {
		if (source != null) {
			return source.trim();
		} else {
			return "";
		}
	}

	public static boolean containsOneOfMatchesChar(char[] password, char[] targetMatches) {
		for(char iterator : password) {
			for (char match : targetMatches) {
				if (iterator == match) {
					return true;
				} 
			}
		}
		
		return false;
	}

	public static boolean containsMatch(char[] password, char targetMatch) {
		for(char iterator : password) {
			if(iterator == targetMatch) {
				return true;
			}
		}
		
		return false;
	}

}
