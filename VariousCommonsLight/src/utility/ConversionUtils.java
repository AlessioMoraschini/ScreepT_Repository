package utility;

import java.io.File;
import java.text.DecimalFormat;

public class ConversionUtils {
	
	public static final String baseUppercaseString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final char[] baseUppercase = baseUppercaseString.toCharArray();
	
	public static final String baseLowercaseString = "abcdefghijklmnopqrstuvwxyz";
	public static final char[] baseLowercase = baseLowercaseString.toCharArray();
	
	public static final String baseString = baseUppercaseString.concat(baseLowercaseString);
	public static final char[] baseCharacters = baseString.toCharArray();
	
	public static final String numberString = "0123456789";
	public static final char[] numberCharacters = numberString.toCharArray();
	
	public static final String symbolString = "@&#$%";
	public static final char[] symbolCharacters = symbolString.toCharArray();
	
	public static final String specialString = "`!\\\"'()*+,-./:;<=>?[]^_`{|}~";
	public static final char[] specialCharacters = specialString.toCharArray();

	public static int getPercent(int part, int total) {
		return (int)((float)part/total * 100.0f);
	}

	public static int getPercent(double percentDecimal) {
		return (int)(percentDecimal * 100.0f);
	}

	public static long getPercent(long part, long total) {
		return (long)((float)part/total * 100.0f);
	}
	
	/**
	 * Convert double given in range 0.0 - 1.0 -> 0-100%
	 * @param percentDecimal
	 * @return
	 */
	public static String getPercentString(double percentDecimal) {
		return String.valueOf((int)(percentDecimal * 100.0f)) + "%";
	}
	
	public static String getPercentString(int part, int total) {
		return getPercent(part, total) + "%";
	}

	public static String getPercentString(long part, long total) {
		return getPercent(part, total) + "%";
	}
	
	/**
	 *  return a string containing the frequency in hertx formatted correctly
	 * @param size is the size in bytes to represent in cool way
	 * @return cool formatted string representing the file size with correct measure unit
	 */
	public static String coolFrequency(Long hertz){
		if(hertz <= 0) return "0";
	    final String[] units = new String[] { "Hz", "kHz", "MHz", "GHz", "THz"};
	    int digitGroups = (int) (Math.log10(hertz)/Math.log10(1000));
	    return new DecimalFormat("#,##0.##").format(hertz/Math.pow(1000, digitGroups)) + " " + units[digitGroups];
	}

	/**
	 *  return a string containing the size of the file formatted correctly according to his size
	 * 
	 * @return cool formatted string representing the file size with correct measure unit
	 */
	public static String coolFileSize(File file){
		long size = file.length();
		if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB", "ExaB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.##").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	/**
	 *  return a string containing the size formatted correctly according to his size
	 * @param size is the size in bytes to represent in cool way
	 * @return cool formatted string representing the file size with correct measure unit
	 */
	public static String coolFileSize(Long size){
		if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB", "ExaB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.##").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	/**
	 *  return a string containing the size formatted correctly in bits according to his size
	 * @param size is the size in bytes to represent in cool way
	 * @return cool formatted string representing the file size in bits with correct measure unit
	 */
	public static String coolFileSizeBits(File file){
		long size = file.length();
		if(size <= 0) return "0";
	    final String[] units = new String[] { "b", "kb", "Mb", "Gb", "Tb", "Exab" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.##").format(size*8/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	/**
	 *  return a string containing the size formatted correctly in bits according to his size
	 * @param size is the size in bytes to represent in cool way
	 * @return cool formatted string representing the file size in bits with correct measure unit
	 */
	public static String coolFileSizeBits(Long size){
		if(size <= 0) return "0";
	    final String[] units = new String[] { "b", "kb", "Mb", "Gb", "Tb", "Exab" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.##").format(size*8/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
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
