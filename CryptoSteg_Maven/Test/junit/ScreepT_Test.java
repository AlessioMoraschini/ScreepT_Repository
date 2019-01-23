package junit;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import org.ini4j.InvalidFileFormatException;
import org.junit.Ignore;
import org.junit.Test;

import uTILS.FileStuff;
import crypt.aes.EncryptDecrypt;
import crypt.manager.ScreeptManagerStub;

public class ScreepT_Test {

	private final String TEMP_DIR = "Resources_Scrypt/temp/";
	private final String AES_KEY = "ciaosonoalessio";
	private EncryptDecrypt AEScoder;
	private File srcFileAES = new File("TEST_FILES/Source.txt");
	private File codedFileAES = new File("TEST_FILES/AesCrypted_Source.txt");
	
	private ScreeptManagerStub screepTManager;
	
	@Test
	@Ignore
	public void testOnlyAEScrypt() throws InvalidFileFormatException, IOException, NoSuchPaddingException, NoSuchAlgorithmException{
		
		try {
			init();
			
			assertTrue(screepTManager.crypt(srcFileAES));
			
			File resultDir =new File(TEMP_DIR);
			
			for(File result : resultDir.listFiles()) {
				File out = new File("TEST_FILES/" + result.getName());
				if (out.exists()) {
					out.delete();
				}

//				out = FileStuff.renameJavaObjFile(out);
				assertTrue(result.renameTo(out));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@Ignore
	public void testOnlyAESdecrypt() throws InvalidFileFormatException, IOException, NoSuchPaddingException, NoSuchAlgorithmException{
		
		try {
			init();
			
			assertTrue(screepTManager.deCrypt(codedFileAES));
			
			File resultDir =new File(TEMP_DIR);
			
			for(File result : resultDir.listFiles()) {
				
				File out = new File("TEST_FILES/" + result.getName());
				out = FileStuff.renameJavaObjFile(out);
				
				assertTrue(result.renameTo(out));

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void init() throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException {
		AEScoder = new EncryptDecrypt(AES_KEY);
		screepTManager = new ScreeptManagerStub(AEScoder);
	}
	
}
