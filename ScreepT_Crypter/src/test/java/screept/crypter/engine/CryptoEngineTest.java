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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;

/** End to end tests of the engine on byte arrays (no GUI, no files): all KDFs, ciphers and stacks. */
public class CryptoEngineTest {

	private static final char[] PSW = "correct horse battery staple".toCharArray();
	private static final int CHUNK_LOG2 = 10; // 1 KiB chunks keep multi-chunk tests small

	private static byte[] hex(String s) {
		byte[] b = new byte[s.length() / 2];
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
		}
		return b;
	}

	private static byte[] data(int n) {
		byte[] b = new byte[n];
		new Random(n).nextBytes(b);
		return b;
	}

	/** Fast profiles covering every KDF and every stack of the supported ciphers. */
	private static List<CryptoProfile> allProfiles() {
		List<CryptoProfile> list = new ArrayList<CryptoProfile>();
		List<List<CipherLayer>> stacks = Arrays.asList(
				Arrays.asList(CipherAlgorithm.AES_128_GCM),
				Arrays.asList(CipherAlgorithm.AES_256_GCM),
				Arrays.asList(CipherAlgorithm.AES_128_GCM, CipherAlgorithm.AES_256_GCM),
				Arrays.asList(CipherAlgorithm.AES_256_GCM, CipherAlgorithm.AES_128_GCM));
		for (KdfAlgorithm kdf : CrypterTortureTest.passwordKdfs()) {
			int cost = CrypterTortureTest.cheapCost(kdf);
			for (List<CipherLayer> st : stacks) {
				list.add(new CryptoProfile(kdf, cost, CHUNK_LOG2, st));
			}
		}
		return list;
	}

	@Test
	public void scryptMatchesRfc7914Vectors() throws Exception {
		assertArrayEquals(
				hex("7023bdcb3afd7348461c06cd81fd38ebfda8fbba904f8e3ea9b543f6545da1f2d5432955613f0fcf62d49705242a9af9e61e85dc0d651e40dfcf017b45575887"),
				Scrypt.derive("pleaseletmein".toCharArray(), "SodiumChloride".getBytes("UTF-8"), 16384, 8, 1, 64));
		assertArrayEquals(
				hex("fdbabe1c9d3472007856e7190d01e9fe7c6ad7cbc8237830e77376634b3731622eaf30d92e22a3886ff109279d9830dac727afb94a83ee6d8360cbdfa2cc0640"),
				Scrypt.derive("password".toCharArray(), "NaCl".getBytes("UTF-8"), 1024, 8, 16, 64));
	}

	@Test
	public void roundTripAllProfilesAndSizes() throws Exception {
		int chunk = 1 << CHUNK_LOG2;
		int[] sizes = { 0, 1, 15, chunk - 1, chunk, chunk + 1, 3 * chunk, 3 * chunk + 17 };
		for (CryptoProfile p : allProfiles()) {
			for (int size : sizes) {
				byte[] plain = data(size);
				byte[] enc = CryptoEngine.encrypt(plain, PSW, p);
				assertTrue(CryptoEngine.isEngineData(enc));
				assertArrayEquals(p + " size " + size, plain, CryptoEngine.decrypt(enc, PSW));
			}
		}
	}

	@Test
	public void decryptRecognisesProfileAutomatically() throws Exception {
		for (CryptoProfile p : allProfiles()) {
			byte[] enc = CryptoEngine.encrypt(data(100), PSW, p);
			CryptoProfile read = CryptoEngine.readProfile(enc);
			assertEquals(p.describe(), read.describe());
			assertEquals(p.kdfCost, read.kdfCost);
		}
	}

	@Test
	public void standardProfilesWorkWithDefaultCosts() throws Exception {
		for (CryptoProfile p : Arrays.asList(CryptoProfile.standard(), CryptoProfile.aes128(), CryptoProfile.maximum())) {
			byte[] plain = data(5000);
			assertArrayEquals(plain, CryptoEngine.decrypt(CryptoEngine.encrypt(plain, PSW, p), PSW));
		}
	}

	@Test
	public void sameInputGivesDifferentOutputAndNoPlaintextLeaks() throws Exception {
		CryptoProfile p = allProfiles().get(1);
		byte[] plain = "very recognisable plaintext very recognisable plaintext".getBytes("UTF-8");
		byte[] a = CryptoEngine.encrypt(plain, PSW, p);
		byte[] b = CryptoEngine.encrypt(plain, PSW, p);
		assertFalse(Arrays.equals(a, b));
		assertFalse(new String(a, "ISO-8859-1").contains("recognisable"));
		assertFalse(new String(a, "ISO-8859-1").contains(new String(PSW)));
	}

	@Test
	public void wrongPasswordIsRejected() throws Exception {
		for (CryptoProfile p : allProfiles()) {
			byte[] enc = CryptoEngine.encrypt(data(2500), PSW, p);
			expectFailure(enc, "wrong password".toCharArray());
		}
	}

	@Test
	public void tamperingIsDetectedAnywhere() throws Exception {
		CryptoProfile p = new CryptoProfile(KdfAlgorithm.PBKDF2_SHA256, 1000, CHUNK_LOG2,
				Arrays.asList(CipherAlgorithm.AES_128_GCM, CipherAlgorithm.AES_256_GCM));
		byte[] enc = CryptoEngine.encrypt(data(2500), PSW, p);
		for (int pos = 0; pos < enc.length; pos += 7) {
			byte[] bad = enc.clone();
			bad[pos] ^= 0x01;
			expectFailure(bad, PSW);
		}
	}

	@Test
	public void truncationAndExtensionAreDetected() throws Exception {
		CryptoProfile p = allProfiles().get(2);
		int chunk = 1 << CHUNK_LOG2;
		for (int size : new int[] { 10, chunk, 2 * chunk + 5 }) {
			byte[] enc = CryptoEngine.encrypt(data(size), PSW, p);
			expectFailure(Arrays.copyOf(enc, enc.length - 1), PSW);
			expectFailure(Arrays.copyOf(enc, enc.length - 40), PSW);
			expectFailure(Arrays.copyOf(enc, enc.length + 1), PSW);
		}
		// dropping whole trailing chunks must not pass as a shorter valid file
		byte[] enc = CryptoEngine.encrypt(data(3 * chunk), PSW, p);
		int frame = chunk + 2 * AesGcmLayer.TAG_BYTES;
		int headerLen = enc.length - (3 * frame + 2 * AesGcmLayer.TAG_BYTES);
		expectFailure(Arrays.copyOf(enc, headerLen + 2 * frame), PSW);
	}

	@Test
	public void notEngineDataAndEmptyPasswordAreRejected() throws Exception {
		assertFalse(CryptoEngine.isEngineData("hello".getBytes("UTF-8")));
		assertFalse(CryptoEngine.isEngineData(null));
		expectFailure("hello world, not encrypted".getBytes("UTF-8"), PSW);
		try {
			CryptoEngine.encrypt(data(10), new char[0], CryptoProfile.standard());
			fail("empty password accepted");
		} catch (CryptoException expected) {
			// ok
		}
	}

	@Test
	public void passwordLongerThan16CharsIsFullyUsed() throws Exception {
		CryptoProfile p = allProfiles().get(0);
		char[] a = "0123456789abcdefAAAA".toCharArray();
		char[] b = "0123456789abcdefBBBB".toCharArray();
		byte[] enc = CryptoEngine.encrypt(data(50), a, p);
		expectFailure(enc, b);
	}

	private static void expectFailure(byte[] enc, char[] password) {
		try {
			CryptoEngine.decrypt(enc, password);
			fail("decryption should have failed");
		} catch (CryptoException expected) {
			// ok
		}
	}
}
