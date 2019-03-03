package gUI.om;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public enum SupportedFileTextEditor {TXT(".txt","txt"),
									 XML(".xml","xml"),
									 HTML(".html","html"),
									 CSS(".css","css"),
									 JSON(".JSON","JSON"),
									 JAVA(".java","java"),
									 JS(".js","js");
							   
	public String senzapunto;
	public String nomeStringa;
	
	SupportedFileTextEditor(String extension, String senzapunto) {
		this.senzapunto = senzapunto;
		this.nomeStringa = extension;
	}
	
	public static ArrayList<String> getSupportedExtensions(){
		ArrayList<String> extensions = new ArrayList<String>();
		
		for(SupportedFileTextEditor current : SupportedFileTextEditor.values()) {
			extensions.add(current.senzapunto);
		}
		
		return extensions;
	}
}
