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
package utility;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import om.HslColor;
import utility.string.StringWorker;

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

	/**
	 * parse font from a representing string
	 */
	public static Font getFontFromString(String stringFont) {
		String[] font = stringFont.split("-");
		String type;
		int style;
		int size;
		try {
			if (font.length != 3) {
				return new Font("Segoe UI", Font.PLAIN, 18);
			}
			type = font[0];
			style = Integer.parseInt(font[1]);
			size = Integer.parseInt(font[2]);
		} catch (Exception e) {
			return new Font("Segoe UI", Font.PLAIN, 18);
		}
		
		return new Font(type, style, size);
	}

	/**
	 * parse font from a representing string
	 */
	public static Font getFontFromString(String stringFont, Font defaultFont) {
		String[] font = stringFont.split("-");
		String type;
		int style;
		int size;
		try {
			if (font.length != 3) {
				return defaultFont;
			}
			type = font[0];
			style = Integer.parseInt(font[1]);
			size = Integer.parseInt(font[2]);
		} catch (Exception e) {
			return defaultFont;
		}
		
		return new Font(type, style, size);
	}

	public static Color hsvToRgb(float hue, float saturation, float value) throws RuntimeException{
	
	    int h = (int)(hue * 6);
	    value = value * 256;
	    int f = (int)hue * 6 - h * 256;
	    int p = (int)(value * (1 - saturation) * 256);
	    int q = (int)(value * (1 - f * saturation) * 256);
	    int t = (int)(value * (1 - (1 - f) * saturation) * 256);
	
	    switch (h) {
	      case 0: return new Color(value, t, p);
	      case 1: return new Color(q, value, p);
	      case 2: return new Color(p, value, t);
	      case 3: return new Color(p, q, value);
	      case 4: return new Color(t, p, value);
	      case 5: return new Color(value, p, q);
	      default: throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value);
	    }
	}

	/**
	 * create color from String RGB separed with "-" representation (red-green-blue)
	 */
	public static Color getRgbColorFromString(String color) {
		String[] rgb = color.split("-");
		if(rgb.length != 3) {
			return new Color(0,0,0);
		}
		int red = Integer.parseInt(rgb[0]);
		int green = Integer.parseInt(rgb[1]);
		int blue = Integer.parseInt(rgb[2]);
		
		return new Color(red, green, blue);
	}

	/**
	 * return font in string format
	 * @return
	 */
	public static String getFontString(Font srcFont) {
		String stringFont = srcFont.getFontName()+"-"+srcFont.getStyle()+"-"+srcFont.getSize();
		return stringFont;
	}

	/**
	 * Convert color to string HSL
	 */
	public static String getHslString(Color sourceColor, String separator) {
		float[] hsl = HslColor.fromRGB(sourceColor);
		String result = "";
		for(float curr : hsl) {
			String currString = String.valueOf((int)curr);
			result = result.concat(currString).concat(separator);
		}
		if (result.length() > 1) {
			result = result.substring(0, result.length()-2);
		}    	
		return result;
	}

	/**
	 * Convert color to string HSB (where B is brightness, same as Value where referred as HSV)
	 */
	public static String getHsbString(Color sourceColor, String separator) {
		float[] hsv = new float[3];
		float[] hsb = Color.RGBtoHSB(sourceColor.getRed(), sourceColor.getGreen(), sourceColor.getBlue(), hsv);
		String result = "";
		for(float curr : hsb) {
			String currString = String.format("%.2f", curr);
			result = result.concat(currString).concat(separator);
		}
		if (result.length() > 1) {
			result = result.substring(0, result.length()-2);
		}
		return result;
	}

	/**
	 * Convert color to string Hexadecimal
	 */
	public static String getHexString(Color sourceColor) {
		String hex = String.format("#%02x%02x%02x", sourceColor.getRed(), sourceColor.getGreen(), sourceColor.getBlue());
		return hex;
	}

	/**
	 * Convert color to string RGB with custom separator
	 */
	public static String getRgbString(Color sourceColor, String separator) {
		separator = (StringWorker.isEmpty(separator))? "-" : separator;
		StringBuilder builder = new StringBuilder();
		builder.append(sourceColor.getRed()).append(separator)
			   .append(sourceColor.getGreen()).append(separator)
			   .append(sourceColor.getBlue());
		String result = builder.toString();
		return result;
	}

	/**
	 * Convert color to string RGB
	 */
	public static String getRgbString(Color sourceColor) {
		StringBuilder builder = new StringBuilder();
		builder.append(sourceColor.getRed()).append("-")
			   .append(sourceColor.getGreen()).append("-")
			   .append(sourceColor.getBlue());
		String result = builder.toString();
		return result;
	}

	public static Double getAvgValue(List<Double> valueList) {
		double sum = valueList.stream().mapToDouble(Double::doubleValue).sum();
		int size = valueList.size();
		return (sum/size);
		
	}

	/**
	 * Converts given raw string with separator String, to a list of elements that were separated by such separator
	 * @param rawList
	 * @param separator
	 * @return
	 */
	public static ArrayList<String> getStringListFromRawString(String rawList, String separator) {
		ArrayList<String> result = new ArrayList<String>();
		String[] elements = rawList.split(separator);
		result.addAll(Arrays.asList(elements));
		
		return result;
	}

}
