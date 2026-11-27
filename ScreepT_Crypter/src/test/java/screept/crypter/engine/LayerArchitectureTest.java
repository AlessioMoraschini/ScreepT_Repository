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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** The layers are interchangeable and combinable: key image, shuffle, custom registered layers. */
public class LayerArchitectureTest {

	private static final char[] PSW = "layers need a long enough password".toCharArray();

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private File image(long seed) throws Exception {
		BufferedImage img = new BufferedImage(40, 30, BufferedImage.TYPE_INT_RGB);
		Random r = new Random(seed);
		for (int y = 0; y < 30; y++) {
			for (int x = 0; x < 40; x++) {
				img.setRGB(x, y, r.nextInt(0xFFFFFF));
			}
		}
		File f = new File(tmp.getRoot(), "key" + seed + ".png");
		ImageIO.write(img, "png", f);
		return f;
	}

	private static byte[] data(int n) {
		byte[] b = new byte[n];
		new Random(n).nextBytes(b);
		return b;
	}

	private static CryptoProfile fast(CipherLayer... layers) {
		return new CryptoProfile(KdfAlgorithm.PBKDF2_SHA256, 1000, 10, Arrays.asList(layers));
	}

	@Test
	public void keyImageIsASecondFactor() throws Exception {
		CryptoProfile p = fast(CipherAlgorithm.AES_256_GCM, CipherAlgorithm.STEGA_CREEPT);
		assertTrue(p.needsKeyImage());
		byte[] plain = data(5000);
		byte[] enc = CryptoEngine.encrypt(plain, CryptoKey.of(PSW, image(1)), p);

		assertArrayEquals(plain, CryptoEngine.decrypt(enc, CryptoKey.of(PSW, image(1))));
		expectFailure(enc, CryptoKey.of(PSW, image(2)));  // another picture
		expectFailure(enc, CryptoKey.of(PSW));             // no picture at all
		expectFailure(enc, CryptoKey.of("wrong password".toCharArray(), image(1)));
	}

	@Test
	public void encryptingWithKeyImageLayerButNoImageIsRefused() throws Exception {
		try {
			CryptoEngine.encrypt(data(10), PSW, fast(CipherAlgorithm.AES_256_GCM, CipherAlgorithm.STEGA_CREEPT));
			fail("key image layer without key image accepted");
		} catch (CryptoException expected) {
			// ok
		}
	}

	@Test
	public void shuffleLayerKeepsLengthChangesOrderAndRoundTrips() throws Exception {
		CipherLayer shuffle = CipherAlgorithm.SHUFFLE;
		byte[] key = new byte[32];
		byte[] nonce = new byte[12];
		byte[] plain = data(3000);
		byte[] shuffled = shuffle.encrypt(key, nonce, new byte[0], plain);
		assertEquals(plain.length, shuffled.length);
		assertFalse(Arrays.equals(plain, shuffled));
		byte[] sortedA = plain.clone();
		byte[] sortedB = shuffled.clone();
		Arrays.sort(sortedA);
		Arrays.sort(sortedB);
		assertArrayEquals("a shuffle only permutes bytes", sortedA, sortedB);
		assertArrayEquals(plain, shuffle.decrypt(key, nonce, new byte[0], shuffled));
		nonce[0] = 1; // a different chunk gets a different permutation
		assertFalse(Arrays.equals(shuffled, shuffle.encrypt(key, nonce, new byte[0], plain)));
	}

	@Test
	public void everyCombinationOfLayersRoundTrips() throws Exception {
		CipherLayer[] pool = { CipherAlgorithm.AES_128_GCM, CipherAlgorithm.AES_256_GCM, CipherAlgorithm.STEGA_CREEPT,
				CipherAlgorithm.SHUFFLE };
		CryptoKey key = CryptoKey.of(PSW, image(7));
		int checked = 0;
		for (int mask = 1; mask < (1 << pool.length); mask++) {
			CipherLayer[] chosen = new CipherLayer[Integer.bitCount(mask)];
			int k = 0;
			for (int i = 0; i < pool.length; i++) {
				if ((mask & (1 << i)) != 0) {
					chosen[k++] = pool[i];
				}
			}
			if (chosen.length == 1 && chosen[0] == CipherAlgorithm.SHUFFLE) {
				continue; // shuffle alone is not authenticated: the profile refuses it (tested below)
			}
			CryptoProfile p = fast(chosen);
			for (int size : new int[] { 0, 1, 1023, 1024, 1025, 4000 }) {
				byte[] plain = data(size);
				assertArrayEquals(p + " size " + size, plain, CryptoEngine.decrypt(CryptoEngine.encrypt(plain, key, p), key));
			}
			checked++;
		}
		assertEquals(14, checked);
	}

	@Test
	public void shuffleOnTopStillDetectsTampering() throws Exception {
		CryptoKey key = CryptoKey.of(PSW);
		CryptoProfile p = fast(CipherAlgorithm.AES_256_GCM, CipherAlgorithm.SHUFFLE);
		byte[] enc = CryptoEngine.encrypt(data(3000), key, p);
		for (int pos = 0; pos < enc.length; pos += 11) {
			byte[] bad = enc.clone();
			bad[pos] ^= 0x01;
			expectFailure(bad, key);
		}
	}

	@Test
	public void profileNeedsAnAuthenticatedLayer() {
		try {
			fast(CipherAlgorithm.SHUFFLE);
			fail("profile without authenticated layer accepted");
		} catch (IllegalArgumentException expected) {
			// ok
		}
	}

	@Test
	public void customLayersCanBeRegisteredAndUsed() throws Exception {
		CipherLayer custom = new AesGcmLayer(201, "Test AES-256 (custom)", 32);
		CipherAlgorithm.register(custom);
		CipherAlgorithm.register(custom); // registering the same instance again is harmless
		try {
			CipherAlgorithm.register(new AesGcmLayer(201, "Other", 16));
			fail("id reuse accepted");
		} catch (IllegalArgumentException expected) {
			// ok
		}
		CryptoProfile p = fast(CipherAlgorithm.AES_128_GCM, custom);
		byte[] plain = data(2500);
		byte[] enc = CryptoEngine.encrypt(plain, PSW, p);
		assertEquals("AES-128 + Test AES-256 (custom) / PBKDF2-HMAC-SHA256", CryptoEngine.readProfile(enc).describe());
		assertArrayEquals(plain, CryptoEngine.decrypt(enc, PSW));
		assertTrue(CipherAlgorithm.all().contains(custom));
	}

	@Test
	public void unknownLayerIdIsReportedClearly() throws Exception {
		byte[] enc = CryptoEngine.encrypt(data(10), PSW, fast(CipherAlgorithm.AES_256_GCM));
		enc[12] = (byte) 250; // first layer id in the header
		try {
			CryptoEngine.decrypt(enc, PSW);
			fail("unknown layer accepted");
		} catch (CryptoException e) {
			assertTrue(e.getMessage().contains("Unknown cipher id 250"));
		}
	}

	private static void expectFailure(byte[] enc, CryptoKey key) {
		try {
			CryptoEngine.decrypt(enc, key);
			fail("decryption should have failed");
		} catch (CryptoException expected) {
			// ok
		}
	}
}
