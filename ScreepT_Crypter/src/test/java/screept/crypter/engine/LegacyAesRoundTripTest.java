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

import java.util.Random;

import org.junit.Test;

import screept.crypter.impl.aes.AesCipher;

/** The legacy AES-128 (CBC) byte API must round-trip with every combination of the hash and shuffle flags. */
public class LegacyAesRoundTripTest {

	@Test
	public void roundTripWithEveryFlagCombination() throws Exception {
		byte[] plain = new byte[100_000];
		new Random(3).nextBytes(plain);
		for (boolean hashed : new boolean[] { false, true }) {
			for (boolean shuffle : new boolean[] { false, true }) {
				AesCipher cipher = new AesCipher("legacyPassword16".toCharArray());
				byte[] enc = cipher.encrypt(plain, hashed, shuffle);
				AesCipher other = new AesCipher("legacyPassword16".toCharArray());
				assertArrayEquals("hashed=" + hashed + " shuffle=" + shuffle, plain, other.decrypt(enc));
			}
		}
	}
}
