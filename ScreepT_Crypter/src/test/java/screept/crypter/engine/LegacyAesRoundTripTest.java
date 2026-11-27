/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
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
