package gui;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

import files.FileWorker;
import om.SupportedFileTextEditor;

public class SampleFileReader {

	public static File defaultSamplesFolder = new File("./Resources_ScreepT/ProgrammingLanguagesWords/samples");
	
	public static String getFileTextFromSyntax(SupportedFileTextEditor syntax, File sampleSourceFolder) throws IOException {
		
		try {
			
			File[] samples = sampleSourceFolder.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.contains(syntax.name());
				}
			});
			
			if(samples.length == 0) {
				samples = sampleSourceFolder.listFiles(new FileFilter() {
					
					@Override
					public boolean accept(File pathname) {
						return pathname.isFile();
					}
				});
			}
			
			return FileWorker.readFileAsString(samples[0]);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "";
	}
}
