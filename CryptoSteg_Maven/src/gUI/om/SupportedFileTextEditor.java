package gUI.om;

import java.awt.image.BufferedImage;

public enum SupportedFileTextEditor {TXT(".txt","txt"),
									 XML(".xml","xml"),
									 HTML(".html","html"),
									 CSS(".css","css"),
									 JS(".js","js");
							   
	public String senzapunto;
	public String nomeStringa;
	
	SupportedFileTextEditor(String extension, String senzapunto) {
		this.senzapunto = senzapunto;
		this.nomeStringa = extension;
	}
}
