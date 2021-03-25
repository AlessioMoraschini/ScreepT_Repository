package aES;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.Test;

import crypt.aes.AesCipher;
import test.util.utils.Generator;

public class AesLongStringTest {

	public static final String testPassword = "alessioteststringencode";
	public static final int smallSize = 1000;
	public static final int medSize = 10000;
	public static final int bigSize = 100000;
	
	@Test
	public void testBigStringAes() throws IOException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
		
		String fileRead = Generator.getRandomString(smallSize);
		AesCipher crypterAES = new AesCipher(testPassword.toCharArray());
		String encoded = crypterAES.encrypt(fileRead);
		
		String decoded = crypterAES.decrypt(encoded);
		
		assertTrue(fileRead.equals(decoded));
	}
}
