package aES;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.Test;

import amfileutils.FileStuff;
import crypt.aes.AesCipher;

public class AesLongStringTest {

	public static final String bigSourceTxtPath = "C:\\Users\\xx\\Desktop\\asd.txt";
	public static final String smallSourceTxtPath = "C:\\Users\\xx\\Desktop\\BBBBBBBBB.txt";
	public static final String testPassword = "sericlastofretd";
	
	@Test
	public void testBigStringAes() throws IOException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
		
		String fileRead = FileStuff.readFileAsString(new File(bigSourceTxtPath));
		AesCipher crypterAES = new AesCipher(testPassword.toCharArray());
		String encoded = crypterAES.encrypt(fileRead);
		
		String decoded = crypterAES.decrypt(encoded);
		
		assertTrue(fileRead.equals(decoded));
	}
}
