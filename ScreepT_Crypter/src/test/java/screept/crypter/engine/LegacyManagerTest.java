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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Random;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import screept.crypter.impl.aes.AesCipher;
import screept.crypter.impl.manager.CryptoMode;
import screept.crypter.impl.manager.ScreeptManager;
import screept.crypter.impl.steg.StegCipher;
import screept.crypter.util.ImageEncodingUtils;

/**
 * The legacy manager (AES-128 CBC, StegaCREEPT key image, both) stays in the program to open old files: what it wrote
 * must always be readable again, whatever the mode. These tests drive the manager the way the GUI facade does.
 */
public class LegacyManagerTest {

	private static final char[] PSW = "legacy manager password 123".toCharArray();

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static byte[] data(int n, long seed) {
		byte[] b = new byte[n];
		new Random(seed).nextBytes(b);
		return b;
	}

	private File source(String name, byte[] content) throws Exception {
		File f = new File(tmp.newFolder(), name);
		Files.write(f.toPath(), content);
		return f;
	}

	private static BufferedImage key() {
		return ImageEncodingUtils.generaKeyImage(48, 48, BufferedImage.TYPE_3BYTE_BGR);
	}

	private static String dirOf(File dir) {
		return dir.getAbsolutePath().replace('\\', '/');
	}

	private ScreeptManager aesManager(File out, boolean shuffle) throws Exception {
		ScreeptManager m = new ScreeptManager(new AesCipher(PSW.clone()));
		m.setOutPath(dirOf(out));
		m.setShuffleResult(shuffle);
		m.setLoggerEnabled(false);
		return m;
	}

	// ------------------------------------------------------------------ ONLY_AES

	@Test
	public void aesFileRoundTripWithAndWithoutShuffle() throws Exception {
		for (boolean shuffle : new boolean[] { false, true }) {
			for (int size : new int[] { 1, 15, 16, 17, 5000, 70_000 }) {
				byte[] plain = data(size, size);
				File src = source("doc" + size + ".bin", plain);
				File encDir = tmp.newFolder();
				ScreeptManager enc = aesManager(encDir, shuffle);
				assertTrue(enc.crypt(src, null));
				File encrypted = new File(encDir, src.getName());
				assertTrue("encrypted file written", encrypted.isFile());
				assertFalse("the file is not stored as it was", Arrays.equals(plain, Files.readAllBytes(encrypted.toPath())));

				File decDir = tmp.newFolder();
				ScreeptManager dec = aesManager(decDir, shuffle);
				assertTrue(dec.deCrypt(encrypted, null));
				assertArrayEquals("size " + size + " shuffle " + shuffle, plain, Files.readAllBytes(new File(decDir, src.getName()).toPath()));
			}
		}
	}

	@Test
	public void aesOutputNeverOverwritesAnExistingFile() throws Exception {
		File src = source("same.txt", data(300, 2));
		File encDir = tmp.newFolder();
		assertTrue(aesManager(encDir, false).crypt(src, null));
		assertTrue(aesManager(encDir, false).crypt(src, null));
		assertEquals("the second output gets another name", 2, encDir.listFiles().length);
	}

	// ------------------------------------------------------------------ ONLY_STEG and COMPLETE

	private ScreeptManager stegManager(File out, boolean withAes) throws Exception {
		StegCipher steg = new StegCipher(key(), BufferedImage.TYPE_3BYTE_BGR);
		ScreeptManager m = withAes ? new ScreeptManager(steg, new AesCipher(PSW.clone())) : new ScreeptManager(steg);
		m.setOutPath(dirOf(out));
		m.setLoggerEnabled(false);
		return m;
	}

	private void stegRoundTrip(boolean withAes, int size) throws Exception {
		Assume.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
		BufferedImage keyImg = key();
		byte[] plain = data(size, 77 + size);
		File src = source("secret" + size + ".dat", plain);
		File encDir = tmp.newFolder();

		StegCipher steg = new StegCipher(keyImg, BufferedImage.TYPE_3BYTE_BGR);
		ScreeptManager enc = withAes ? new ScreeptManager(steg, new AesCipher(PSW.clone())) : new ScreeptManager(steg);
		enc.setOutPath(dirOf(encDir));
		enc.setLoggerEnabled(false);
		assertTrue(enc.crypt(src, null));
		File imagesDir = enc.currentOutFile;
		assertNotNull(imagesDir);
		assertTrue("images folder", imagesDir.isDirectory());
		assertTrue("at least one image", imagesDir.listFiles().length >= 1);

		File decDir = tmp.newFolder();
		StegCipher steg2 = new StegCipher(keyImg, BufferedImage.TYPE_3BYTE_BGR);
		ScreeptManager dec = withAes ? new ScreeptManager(steg2, new AesCipher(PSW.clone())) : new ScreeptManager(steg2);
		dec.setOutPath(dirOf(decDir));
		dec.setLoggerEnabled(false);
		assertTrue(dec.deCrypt(imagesDir, null));
		assertArrayEquals("steg " + (withAes ? "complete" : "only") + " size " + size, plain,
				Files.readAllBytes(new File(decDir, src.getName()).toPath()));
	}

	@Test
	public void stegOnlySmallAndMultiImage() throws Exception {
		stegRoundTrip(false, 10);
		stegRoundTrip(false, 9000);
	}

	@Test
	public void stegCompleteSmallAndMultiImage() throws Exception {
		stegRoundTrip(true, 10);
		stegRoundTrip(true, 9000);
	}

	// ------------------------------------------------------------------ small helpers of the manager

	@Test
	public void sizeHelpersAlwaysGiveMultiplesOfN() {
		for (int n : new int[] { 3, 4, 16 }) {
			for (int v : new int[] { 1, 10, 100, 1025 }) {
				assertEquals(0, ScreeptManager.grantMultipleOfN(v, n) % n);
				assertTrue(ScreeptManager.grantMultipleOfN(v, n) <= v);
				assertEquals(0, ScreeptManager.grantAreaMultipleOfN(v, v + 1, n) % n);
			}
		}
		BufferedImage sub = ScreeptManager.subImgMultOfNpx(key(), 5);
		assertEquals(0, (sub.getWidth() * sub.getHeight()) % 5);
	}

	@Test
	public void settersAndModes() throws Exception {
		ScreeptManager m = new ScreeptManager();
		assertEquals(CryptoMode.COMPLETE, m.getMode());
		m.setMode(CryptoMode.ONLY_AES);
		assertEquals(CryptoMode.ONLY_AES, m.getMode());
		m.setHashed(false);
		assertFalse(m.isHashed());
		m.setShuffleResult(true);
		assertTrue(m.isShuffleResult());
		m.setOutPath("some/where");
		assertEquals("some/where/", m.getOutPath());
		assertNotNull(ScreeptManager.getCryptName());
		assertTrue(ScreeptManager.getAES_BUFFER_SIZE() % 16 == 0);
		assertNotNull(m.pollOutputStats());
		m.setAesCoder(new AesCipher(PSW.clone()));
		assertNotNull(m.getAesCoder());
		m.setImgCoder(null);
		assertEquals(null, m.getImgCoder());
	}
}
