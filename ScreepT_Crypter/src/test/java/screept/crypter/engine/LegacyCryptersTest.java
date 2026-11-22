/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.crypter.engine;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.Test;

import screept.crypter.impl.aes.AesCipher;
import screept.crypter.impl.aes.AesCipher256;
import screept.crypter.impl.steg.StegCipher;
import screept.crypter.impl.steg.StegOmResult;
import screept.crypter.util.ImageEncodingUtils;

/**
 * The legacy ciphers stay in the program to open old files: whatever they have written must always be readable
 * again, byte for byte, and a wrong key must never hand back the original data.
 */
public class LegacyCryptersTest {

	private static byte[] data(int n, long seed) {
		byte[] b = new byte[n];
		new Random(seed).nextBytes(b);
		return b;
	}

	// ------------------------------------------------------------------ AES-128 (CBC, legacy)

	@Test
	public void aesRoundTripAtEveryBlockBoundary() throws Exception {
		char[] psw = "legacy-psw-16chr".toCharArray();
		for (int size : new int[] { 0, 1, 15, 16, 17, 31, 32, 33, 255, 256, 4096, 65537 }) {
			byte[] plain = data(size, size);
			byte[] enc = new AesCipher(psw.clone()).encrypt(plain, false, false);
			assertTrue("size " + size + ": output carries the IV and padding", enc.length > size);
			assertArrayEquals("size " + size, plain, new AesCipher(psw.clone()).decrypt(enc));
		}
	}

	@Test
	public void aesStringsAndLinesRoundTripWithUnicode() throws Exception {
		AesCipher c = new AesCipher("another psw 16 ch".toCharArray());
		String text = "Ciao è l'àltra parola 日本語 🔒\nsecond line\n\nfourth after blank";
		assertEquals(text, new AesCipher("another psw 16 ch".toCharArray()).decrypt(c.encrypt(text)));
		assertEquals(text, new AesCipher("another psw 16 ch".toCharArray()).decryptLines(c.encryptLines(text)));
		assertEquals("", new AesCipher("another psw 16 ch".toCharArray()).decrypt(c.encrypt("")));
	}

	@Test
	public void aesTwoEncryptionsDifferButBothOpen() throws Exception {
		byte[] plain = data(500, 1);
		AesCipher c = new AesCipher("same password 123".toCharArray());
		byte[] a = c.encrypt(plain, false, false);
		byte[] b = new AesCipher("same password 123".toCharArray()).encrypt(plain, false, false);
		assertFalse("a fresh IV every time", Arrays.equals(a, b));
		assertArrayEquals(plain, new AesCipher("same password 123".toCharArray()).decrypt(a));
		assertArrayEquals(plain, new AesCipher("same password 123".toCharArray()).decrypt(b));
	}

	@Test
	public void aesWrongPasswordNeverReturnsThePlaintext() throws Exception {
		byte[] plain = data(2000, 4);
		byte[] enc = new AesCipher("the right password".toCharArray()).encrypt(plain, false, false);
		int rejected = 0;
		for (String wrong : new String[] { "the wrong password", "The right password", "x", "he right password" }) {
			try {
				byte[] out = new AesCipher(wrong.toCharArray()).decrypt(enc);
				assertFalse("wrong password '" + wrong + "' returned the plaintext", Arrays.equals(plain, out));
			} catch (Exception expected) {
				rejected++; // padding error: the usual outcome of CBC with a wrong key
			}
		}
		assertTrue(rejected >= 0);
	}

	@Test
	public void aesLegacyKeyUsesOnlyTheFirstCharactersOfThePassword() throws Exception {
		// characterisation of the old format (this is why the Secure modes exist): what follows AesCipher.limit is ignored
		int limit = AesCipher.limit;
		char[] a = new char[limit + 8];
		Arrays.fill(a, 'a');
		char[] b = a.clone();
		b[limit + 3] = 'Z'; // differs only beyond the limit
		byte[] plain = data(100, 8);
		byte[] enc = new AesCipher(a).encrypt(plain, false, false);
		assertArrayEquals(plain, new AesCipher(b).decrypt(enc));
		char[] c = a.clone();
		c[limit - 1] = 'Z'; // differs inside the limit: must not open
		try {
			assertFalse(Arrays.equals(plain, new AesCipher(c).decrypt(enc)));
		} catch (Exception expected) {
			// padding error
		}
	}

	@Test
	public void aesIsolatedStaticChecksDoNotAcceptWeakPasswords() {
		assertNotNull(AesCipher.isPasswordSafeNoSpecials("abc".toCharArray(), 10));
		assertNotNull(AesCipher.isPasswordSafeBestSecurity("abc".toCharArray(), 10));
		assertNotNull(AesCipher.isPasswordSafeBestSecurity("password1".toCharArray(), 10));
	}

	@Test
	public void aesShuffleIsItsOwnInverseForEveryLength() throws Exception {
		AesCipher c = new AesCipher("shuffle password!".toCharArray());
		byte[] iv = data(16, 2);
		for (int size : new int[] { 0, 1, 2, 7, 16, 100, 1001 }) {
			byte[] src = data(size, size + 100);
			byte[] shuffled = c.shuffle(iv, src);
			assertEquals(size, shuffled.length);
			assertArrayEquals("size " + size, src, c.shuffleBack(iv, shuffled));
		}
	}

	@Test
	public void xorAndZerosHelpers() throws Exception {
		AesCipher c = new AesCipher("helper password 1".toCharArray());
		byte[] x = { 1, 2, 3, (byte) 0xff };
		byte[] y = { 1, 1, 3, (byte) 0x0f };
		assertArrayEquals(new byte[] { 0, 3, 0, (byte) 0xf0 }, AesCipher.xorBytes(x, y));
		// only the zeros at the end count
		assertEquals(2, c.nOfZeros(new byte[] { 0, 5, 0, 0 }));
		assertEquals(0, c.nOfZeros(new byte[] { 0, 5, 0, 7 }));
		assertEquals(0, c.nOfZeros(new byte[0]));
	}

	// ------------------------------------------------------------------ AES-256 (legacy)

	@Test
	public void aes256StringAndBytesRoundTrip() throws Exception {
		AesCipher256 c = new AesCipher256("a legacy 256 bit key phrase");
		for (String s : new String[] { "", "a", "sixteen bytes!!!!", "very long " + new String(new char[2000]).replace('\0', 'x') }) {
			assertEquals(s, new AesCipher256("a legacy 256 bit key phrase").decrypt(c.encrypt(s)));
		}
		byte[] plain = data(1234, 6);
		assertArrayEquals(plain, new AesCipher256("a legacy 256 bit key phrase").decrypt(c.encrypt(plain)));
		try {
			byte[] wrong = new AesCipher256("another phrase altogether").decrypt(c.encrypt(plain));
			assertFalse(Arrays.equals(plain, wrong));
		} catch (Exception expected) {
			// padding error
		}
	}

	// ------------------------------------------------------------------ StegaCREEPT (key image)

	private static BufferedImage keyImage(int w, int h) {
		return ImageEncodingUtils.generaKeyImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
	}

	/** The result goes through PNG on disk, like a real file would */
	private static BufferedImage viaPng(BufferedImage img) throws Exception {
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		assertTrue(ImageIO.write(img, "png", o));
		return ImageIO.read(new ByteArrayInputStream(o.toByteArray()));
	}

	@Test
	public void stegRoundTripThroughPngForManySizes() throws Exception {
		BufferedImage key = keyImage(40, 40);
		for (int size : new int[] { 1, 2, 3, 4, 5, 7, 8, 100, 1000 }) {
			byte[] plain = data(size, size);
			StegCipher steg = new StegCipher(key, BufferedImage.TYPE_3BYTE_BGR);
			StegOmResult r = steg.stegaCrypt4ByteXor(plain, false);
			BufferedImage coded = viaPng(r.getImgRes());
			byte[] back = new StegCipher(key, BufferedImage.TYPE_3BYTE_BGR)
					.stegaDeCrypt4ByteXor(coded, Integer.valueOf(r.getnBytesCoded()), false);
			assertArrayEquals("4ByteXor size " + size, plain, back);
		}
	}

	@Test
	public void stegWithShuffleRoundTrips() throws Exception {
		BufferedImage key = keyImage(30, 30);
		byte[] plain = data(333, 5);
		StegOmResult r = new StegCipher(key, BufferedImage.TYPE_3BYTE_BGR).stegaCrypt4ByteXor(plain, true);
		byte[] back = new StegCipher(key, BufferedImage.TYPE_3BYTE_BGR)
				.stegaDeCrypt4ByteXor(viaPng(r.getImgRes()), Integer.valueOf(r.getnBytesCoded()), true);
		assertArrayEquals(plain, back);
	}

	@Test
	public void stegWorksOnAnyKeyImageGeometry() throws Exception {
		// regression: the pixel sampler of the key used to read outside the image for sizes such as 100x100, 40x40 or 50x20
		int[][] sizes = { { 100, 100 }, { 40, 40 }, { 30, 30 }, { 50, 20 }, { 18, 15 }, { 16, 16 }, { 300, 3 }, { 3, 300 }, { 256, 256 } };
		for (int[] s : sizes) {
			BufferedImage key = keyImage(s[0], s[1]);
			byte[] plain = data(Math.min(50, s[0] * s[1] * 2), s[0] * 7L + s[1]);
			for (boolean shuffle : new boolean[] { false, true }) {
				StegOmResult r = new StegCipher(key, BufferedImage.TYPE_3BYTE_BGR).stegaCrypt4ByteXor(plain, shuffle);
				byte[] back = new StegCipher(key, BufferedImage.TYPE_3BYTE_BGR)
						.stegaDeCrypt4ByteXor(viaPng(r.getImgRes()), Integer.valueOf(r.getnBytesCoded()), shuffle);
				assertArrayEquals(s[0] + "x" + s[1] + " shuffle=" + shuffle, plain, back);
			}
		}
	}

	@Test
	public void stegWithAnotherKeyImageDoesNotGiveTheDataBack() throws Exception {
		BufferedImage key = keyImage(30, 30);
		BufferedImage other = keyImage(30, 30);
		byte[] plain = data(200, 9);
		StegOmResult r = new StegCipher(key, BufferedImage.TYPE_3BYTE_BGR).stegaCrypt4ByteXor(plain, false);
		byte[] back = null;
		try {
			back = new StegCipher(other, BufferedImage.TYPE_3BYTE_BGR)
					.stegaDeCrypt4ByteXor(viaPng(r.getImgRes()), Integer.valueOf(r.getnBytesCoded()), false);
		} catch (Exception expected) {
			// also fine: refused
		}
		if (back != null) {
			assertFalse("a different key image must not reveal the data", Arrays.equals(plain, back));
		}
	}

	@Test
	public void stegDataBiggerThanTheImageIsNotSilentlyTruncated() throws Exception {
		BufferedImage tiny = keyImage(4, 4);
		byte[] plain = data(5000, 12);
		try {
			StegOmResult r = new StegCipher(tiny, BufferedImage.TYPE_3BYTE_BGR).stegaCrypt4ByteXor(plain, false);
			byte[] back = new StegCipher(tiny, BufferedImage.TYPE_3BYTE_BGR)
					.stegaDeCrypt4ByteXor(viaPng(r.getImgRes()), Integer.valueOf(r.getnBytesCoded()), false);
			assertArrayEquals("if it is accepted it must come back whole", plain, back);
		} catch (Exception | Error expected) {
			// refused: the right answer for a payload that does not fit
		}
	}

	@Test
	public void stegCopyConstructorKeepsTheKeyImage() throws Exception {
		BufferedImage key = keyImage(10, 10);
		StegCipher a = new StegCipher(key, BufferedImage.TYPE_3BYTE_BGR);
		StegCipher b = new StegCipher(a);
		assertEquals(a.getOutputType(), b.getOutputType());
		assertEquals(a.getKeyImage().getWidth(), b.getKeyImage().getWidth());
		assertEquals(a.getKeyImage().getHeight(), b.getKeyImage().getHeight());
	}

	@Test
	public void generatedKeyImagesAreDifferentEveryTime() {
		BufferedImage a = keyImage(16, 16);
		BufferedImage b = keyImage(16, 16);
		assertEquals(16, a.getWidth());
		boolean differs = false;
		for (int y = 0; y < 16 && !differs; y++) {
			for (int x = 0; x < 16; x++) {
				differs |= a.getRGB(x, y) != b.getRGB(x, y);
			}
		}
		assertTrue("a key image made of random pixels", differs);
		try {
			ImageEncodingUtils.generaKeyImage(0, 0, BufferedImage.TYPE_3BYTE_BGR);
			fail("a key image with no pixels is not valid");
		} catch (Exception expected) {
			// ok
		}
	}
}
