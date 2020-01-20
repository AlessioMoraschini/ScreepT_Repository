package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author amoraschini
 *
 */
public class LinuxFindJvmVersions {

	public static String UPDATE_ALTERNATIVES = "update-alternatives --config java";

	public static ArrayList<File> CommandLineJavaAlternatives() throws IOException{
		
		ArrayList<File> results = new ArrayList<>();
		Process finder = Runtime.getRuntime().exec(UPDATE_ALTERNATIVES);
		InputStream reader = finder.getInputStream();
		ArrayList<String> lines = SysUtils.readProcessOutputLines(reader);
		try {
			
			for(String line : lines) {
				String extracted = extractDirectory(line);
				if (extracted != null && !"".equals(extracted)) {
					results.add(new File(extracted));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			reader.close();
		}
		
		return results;
	}
	
	public static ArrayList<String> CommandLineJavaAlternativesPaths() throws IOException{
		ArrayList<String> pathList = new ArrayList<>();
		for(File version : CommandLineJavaAlternatives()) {
			if (!pathList.contains(version.getAbsolutePath())) {
				pathList.add(version.getAbsolutePath());
			}
		}
		
		return pathList;
	}
	
	public static String extractDirectory(String line) {
		if (line == null || "".equals(line.trim())) {
			return null;
		}

		// [/\\\\] matches all slashes and backslashes - \\s is equivalent to escaped spaces
		Matcher matcher = Pattern.compile("\\s.*[/\\\\](.*[/\\\\].*[a-z]?)\\s").matcher(line);
		StringBuilder builder = new StringBuilder();

		while (matcher.find()) {
			builder.append(matcher.group());
		}

		String[] parts = builder.toString().split("\\s");

		for (String part : parts) {
			if (part.contains("/")) {
				File thisPartFile = new File(part);
				// matches all characters followed by a number followed by another character
				while (thisPartFile != null && !thisPartFile.getName().matches(".*\\d.*")) {
					thisPartFile = thisPartFile.getParentFile();
				}

				return thisPartFile != null ? thisPartFile.getAbsolutePath() : null;
			}
		}

		return null;
	}
	
	public static String extractVersion(String folderName) {
		// Removes every slash, every character, : 
		// Then removes 64 or 32 (used for bit instead of version) and the separator character (-)
		return folderName.replaceAll("[a-zA-Z/\\\\:]", "").replaceAll("-|64|32", "");
	}
}
