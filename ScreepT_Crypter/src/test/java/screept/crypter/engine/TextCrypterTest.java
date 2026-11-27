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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

/** Text encryption of the file editor: exact round trips, blocks found anywhere, any damage refused. */
public class TextCrypterTest {

	private static final CryptoProfile FAST = CrypterTortureTest.profile(KdfAlgorithm.ARGON2ID, CipherAlgorithm.AES_256_GCM);

	private static CryptoKey key(String psw) {
		return CryptoKey.of(psw.toCharArray());
	}

	@Test(timeout = 60000)
	public void everyKindOfTextComesBackExactly() throws Exception {
		StringBuilder big = new StringBuilder();
		Random rnd = new Random(4);
		while (big.length() < 300000) {
			big.append("riga ").append(rnd.nextInt()).append(rnd.nextBoolean() ? "\r\n" : "\n").append("àèìòù €");
		}
		for (String text : new String[] { "", "a", "\n", "\r\n\r\n", "trailing newlines\n\n\n", "  spaces  ", "line1\nline2\r\nline3\rline4",
				"日本語 🔑 emoji", "-----BEGIN fake", big.toString() }) {
			String enc = TextCrypter.encrypt(text, key("text-psw"), FAST);
			assertTrue(enc.startsWith(TextCrypter.BEGIN) && enc.endsWith(TextCrypter.END));
			assertTrue(TextCrypter.containsEncrypted(enc));
			for (String line : enc.split("\n")) {
				assertTrue("short lines", line.length() <= 76);
			}
			assertEquals(text, TextCrypter.decrypt(enc, key("text-psw")));
		}
	}

	@Test(timeout = 60000)
	public void realStandardProfileWorks() throws Exception {
		String enc = TextCrypter.encrypt("secret note", key("real-password-123"), CryptoProfile.standard());
		assertEquals("secret note", TextCrypter.decrypt(enc, key("real-password-123")));
		assertEquals(KdfAlgorithm.ARGON2ID, CryptoEngine.readProfile(java.util.Base64.getMimeDecoder()
				.decode(enc.substring(TextCrypter.BEGIN.length(), enc.length() - TextCrypter.END.length()))).kdf);
	}

	@Test(timeout = 60000)
	public void blocksAreFoundAnywhereAndTheRestIsKept() throws Exception {
		String a = TextCrypter.encrypt("first", key("p"), FAST);
		String b = TextCrypter.encrypt("second\nsecret", key("p"), FAST);
		String mixed = "Hello,\n" + a + "\nin the middle\n\t" + b.replace("\n", "\r\n") + " end";
		assertEquals("Hello,\nfirst\nin the middle\n\tsecond\nsecret end", TextCrypter.decrypt(mixed, key("p")));
		// indented or re-wrapped blocks (mail, chat) still work
		String indented = a.replace("\n", "\n    ");
		assertEquals("first", TextCrypter.decrypt(indented, key("p")));
	}

	@Test(timeout = 60000)
	public void wrongPasswordDamageAndEditsAreRefused() throws Exception {
		String enc = TextCrypter.encrypt("do not change me", key("right"), FAST);
		expectFailure(enc, "wrong");
		expectFailure(enc, "Right");
		expectFailure("no block here", "right");
		expectFailure(TextCrypter.BEGIN + "\nAAAA", "right"); // unterminated
		expectFailure(TextCrypter.BEGIN + "\n" + TextCrypter.END, "right"); // empty
		int body = TextCrypter.BEGIN.length() + 1;
		for (int i = body; i < enc.length() - TextCrypter.END.length() - 1; i += 3) {
			char c = enc.charAt(i);
			char next = enc.charAt(i + 1);
			// the last base64 character may carry unused bits (the decoder ignores them): skip it
			if (c == '\n' || c == '=' || next == '=' || (next == '\n' && enc.charAt(i + 2) == '-')) {
				continue;
			}
			char other = c == 'A' ? 'B' : 'A';
			expectFailure(enc.substring(0, i) + other + enc.substring(i + 1), "right");
		}
		expectFailure(enc.replace("\n", "\n> "), "right"); // quoted lines are not base64
		expectFailure(enc.substring(0, body) + enc.substring(body + 4), "right"); // 4 characters removed
	}

	@Test
	public void legacyTextsAreNotMistakenForBlocks() {
		assertFalse(TextCrypter.containsEncrypted("U2FsdGVkX1+abc==\nQUJD"));
		assertFalse(TextCrypter.containsEncrypted(null));
		assertFalse(TextCrypter.containsEncrypted(TextCrypter.END + TextCrypter.BEGIN));
	}

	@Test
	public void keyIsWipedByClear() {
		char[] psw = "wipe me".toCharArray();
		CryptoKey k = CryptoKey.of(psw);
		k.clear();
		char[] zero = new char[psw.length];
		assertTrue(Arrays.equals(zero, psw));
	}

	private static void expectFailure(String text, String psw) {
		try {
			TextCrypter.decrypt(text, key(psw));
			fail("accepted: " + text.substring(0, Math.min(80, text.length())));
		} catch (CryptoException expected) {
			// ok
		}
	}
}
