package junit;

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.text.BadLocationException;

import org.junit.Rule;
import org.junit.Test;

import configuration.RepeatTest;
import configuration.RepeatedTestRule;
import crypt.aes.AesCipher;
import testUtils.TestUtils;

public class CharBytesAesKeyTest {
	
	@Rule
	public RepeatedTestRule repeatRule = new RepeatedTestRule();
	
	boolean error = false;
	char[] password = {'e', 'w', '9'};
//	char[] password = {'A','D','E','3','5','-','中','è','ò','ç','@','e','4','J','ò','.'};
//	char[] password = {'A','D','E','3','5','g','b','e','r','a','j','e','4','J','5','a'};
//	char[] password = {'哲','學','書','3','電','-','中','è','ò','ç','@','e','化','J','ò','子'};
	String source = "Ciao sono un messaggio di prova per la codifica AES asuhuashduhduahu :D";
	byte[] sourceBytes = source.getBytes(Charset.forName("UTF-8"));
	
	@Test
	@RepeatTest (times = 1)
	public void testCharToBytes() throws BadLocationException {
		AesCipher cipher = null;
		byte[] byteKey;
		try {
			cipher = new AesCipher(password);
			byteKey = cipher.charKeyToByteArrayV2(password);
			
			// only to test length of encoded full bytes
			CharBuffer chBuff = CharBuffer.wrap(password);
			ByteBuffer byBuff = Charset.forName("UTF-8").encode(chBuff);
			byte[] fullBytes = byBuff.array();
			
			TestUtils.printl("=> Char array key length: " + password.length);
			TestUtils.printl("=> Encoded full bytes length: " + fullBytes.length + "\n" + Arrays.toString(fullBytes));
			TestUtils.printl("=> byte array limited length: " + byteKey.length + "\n" + Arrays.toString(byteKey));
			
			assertTrue(byteKey.length == AesCipher.limit);
			assertTrue(cipher.decrypt(cipher.encrypt(source)).equals(source));
			assertTrue(Arrays.equals(cipher.decrypt(cipher.encrypt(sourceBytes, true, false)) , sourceBytes));
			
		} catch (UnsupportedEncodingException e) {
			error = true;
		} catch (NoSuchPaddingException e) {
			error = true;
		} catch (NoSuchAlgorithmException e) {
			error = true;
		} catch (InvalidKeyException e) {
			error = true;
		} catch (InvalidAlgorithmParameterException e) {
			error = true;
		} catch (BadPaddingException e) {
			error = true;
		} catch (IllegalBlockSizeException e) {
			error = true;
		}
		
		assertTrue(!error);
	}
}
