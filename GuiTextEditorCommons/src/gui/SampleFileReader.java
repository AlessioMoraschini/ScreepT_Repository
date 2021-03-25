package gui;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import om.SupportedFileTextEditor;
import various.common.light.files.FileWorker;

public class SampleFileReader {

	public static File defaultSamplesFolder = new File("./Resources_ScreepT/ProgrammingLanguagesWords/samples");
	
	public static String getFileTextFromSyntax(SupportedFileTextEditor syntax, File sampleSourceFolder) throws IOException {
		
		File sampleSource = sampleSourceFolder != null && sampleSourceFolder.exists() ? sampleSourceFolder : defaultSamplesFolder;
		try {
			
			File[] samples = sampleSource.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.equals(syntax.name());
				}
			});
			
			if(samples.length == 0) {
				samples = sampleSource.listFiles(new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String name) {
						return name.equals(SupportedFileTextEditor.JAVA.name());
					}
				});
			}
			
			return FileWorker.readFileAsString(samples[0]);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "";
	}

	public static File getDefaultSamplesFolder() {
		return defaultSamplesFolder;
	}

	public static void setDefaultSamplesFolder(File defaultSamplesFolder) {
		SampleFileReader.defaultSamplesFolder = defaultSamplesFolder;
	}
}
