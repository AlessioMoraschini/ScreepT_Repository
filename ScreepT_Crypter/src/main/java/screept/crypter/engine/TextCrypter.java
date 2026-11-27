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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * Text encryption for the file editor: the text (all of it, line breaks included) becomes one SCP2 container written
 * as a readable block that survives copy and paste, mail and chat:
 *
 * <pre>
 * -----BEGIN SCREEPT ENCRYPTED TEXT-----
 * base64 lines (76 chars)
 * -----END SCREEPT ENCRYPTED TEXT-----
 * </pre>
 *
 * Decryption finds every block in the text by itself (anything around them stays as it is) and needs only the
 * password: the algorithms are in the container header. Spaces, tabs and line breaks inside a block are ignored.
 * Linear scan, no regular expressions: fast on texts of any size.
 */
public final class TextCrypter {

	public static final String BEGIN = "-----BEGIN SCREEPT ENCRYPTED TEXT-----";
	public static final String END = "-----END SCREEPT ENCRYPTED TEXT-----";

	private TextCrypter() {
	}

	/** True if the text contains at least one complete encrypted block. */
	public static boolean containsEncrypted(String text) {
		int b = text == null ? -1 : text.indexOf(BEGIN);
		return b >= 0 && text.indexOf(END, b + BEGIN.length()) >= 0;
	}

	public static String encrypt(String text, CryptoKey key, CryptoProfile profile) throws CryptoException {
		byte[] plain = text.getBytes(StandardCharsets.UTF_8);
		try {
			byte[] enc = CryptoEngine.encrypt(plain, key, profile);
			return BEGIN + "\n" + Base64.getMimeEncoder(76, new byte[] { '\n' }).encodeToString(enc) + "\n" + END;
		} finally {
			Arrays.fill(plain, (byte) 0);
		}
	}

	/** Decrypts every block found in the text; the rest is kept. Fails as a whole if any block is wrong. */
	public static String decrypt(String text, CryptoKey key) throws CryptoException {
		StringBuilder out = new StringBuilder(text.length());
		int last = 0;
		boolean found = false;
		for (int b; (b = text.indexOf(BEGIN, last)) >= 0;) {
			int e = text.indexOf(END, b + BEGIN.length());
			if (e < 0) {
				break; // an unterminated block is left as it is
			}
			found = true;
			out.append(text, last, b);
			byte[] plain = CryptoEngine.decrypt(decodeBase64(text, b + BEGIN.length(), e), key);
			out.append(new String(plain, StandardCharsets.UTF_8));
			Arrays.fill(plain, (byte) 0);
			last = e + END.length();
		}
		if (!found) {
			throw new CryptoException("No ScreepT encrypted text found");
		}
		return out.append(text, last, text.length()).toString();
	}

	/** Strict base64 (whitespace ignored): any other character means the block was damaged or edited. */
	private static byte[] decodeBase64(String text, int from, int to) throws CryptoException {
		StringBuilder b64 = new StringBuilder(to - from);
		for (int i = from; i < to; i++) {
			char c = text.charAt(i);
			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '+' || c == '/' || c == '=') {
				b64.append(c);
			} else if (!Character.isWhitespace(c)) {
				throw new CryptoException("The encrypted text is damaged");
			}
		}
		try {
			return Base64.getDecoder().decode(b64.toString());
		} catch (IllegalArgumentException e) {
			throw new CryptoException("The encrypted text is damaged", e);
		}
	}
}
