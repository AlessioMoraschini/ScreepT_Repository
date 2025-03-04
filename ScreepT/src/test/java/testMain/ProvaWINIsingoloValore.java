package testMain;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.ini4j.Wini;

public class ProvaWINIsingoloValore {

	public static void main(String[] args) {

		File iniFile = new File("INI_FILES/Ciao.ini");
		System.out.println(FilenameUtils.getExtension(iniFile.getName()));
		try {
			Wini iniProva = new Wini(iniFile);
			iniProva.put("AES_OPTIONS", "aesKey", "ciaociao");
			iniProva.store();
			System.out.println("Finished :)");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
