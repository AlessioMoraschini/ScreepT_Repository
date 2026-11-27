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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Random;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import screept.crypter.impl.manager.CryptoMode;
import screept.crypter.impl.manager.ScreeptManagerFileOrFolder;
import screept.crypter.om.CrypterResultDTO;

/** The facade used by the GUI: engine modes encrypt, and decrypt recognises the files whatever mode is selected. */
public class ManagerEnginePathTest {

	private static final char[] PSW = "a password with more than sixteen characters".toCharArray();

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void everyEngineModeRoundTripsAndDecryptIgnoresSelectedMode() throws Exception {
		for (CryptoMode mode : new CryptoMode[] { CryptoMode.SECURE_128, CryptoMode.SECURE_256 }) {
			File dir = new File(tmp.newFolder(), "docs");
			new File(dir, "sub").mkdirs();
			byte[] a = new byte[300_000];
			new Random(1).nextBytes(a);
			Files.write(new File(dir, "sub/a.bin").toPath(), a);

			ScreeptManagerFileOrFolder m = new ScreeptManagerFileOrFolder(null, null, false);
			File encDir = tmp.newFolder();
			CrypterResultDTO r = m.crYpt(mode, dir, encDir, PSW.clone(), null, false);
			assertTrue(m.getOutputStats(), r.resultOK);
			File enc = new File(encDir, "docs.dir.scpt");
			assertTrue(enc.isFile());

			File decDir = tmp.newFolder();
			// legacy mode selected on purpose: the engine file must be recognised anyway
			r = m.decrYpt(CryptoMode.ONLY_AES, enc, decDir, PSW.clone(), null, false);
			assertTrue(m.getOutputStats(), r.resultOK);
			assertArrayEquals(a, Files.readAllBytes(new File(decDir, "docs/sub/a.bin").toPath()));

			File badDir = tmp.newFolder();
			r = m.decrYpt(CryptoMode.ONLY_AES, enc, badDir, "wrong".toCharArray(), null, false);
			assertFalse(r.resultOK);
		}
	}

	@Test
	public void keyImageModeNeedsTheSamePictureToDecrypt() throws Exception {
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(20, 20, java.awt.image.BufferedImage.TYPE_INT_RGB);
		Random r = new Random(5);
		for (int y = 0; y < 20; y++) {
			for (int x = 0; x < 20; x++) {
				img.setRGB(x, y, r.nextInt(0xFFFFFF));
			}
		}
		File key = new File(tmp.newFolder(), "key.png");
		javax.imageio.ImageIO.write(img, "png", key);
		File other = new File(key.getParentFile(), "other.png");
		img.setRGB(0, 0, img.getRGB(0, 0) ^ 0x10101);
		javax.imageio.ImageIO.write(img, "png", other);

		File src = new File(tmp.newFolder(), "note.txt");
		Files.write(src.toPath(), "hello".getBytes("UTF-8"));
		ScreeptManagerFileOrFolder m = new ScreeptManagerFileOrFolder(null, null, false);
		File encDir = tmp.newFolder();
		assertTrue(m.crYpt(CryptoMode.SECURE_KEYIMAGE, src, encDir, PSW.clone(), key, false).resultOK);

		File ok = tmp.newFolder();
		assertTrue(m.decrYpt(CryptoMode.SECURE_KEYIMAGE, new File(encDir, "note.txt.scpt"), ok, PSW.clone(), key, false).resultOK);
		assertArrayEquals("hello".getBytes("UTF-8"), Files.readAllBytes(new File(ok, "note.txt").toPath()));

		assertFalse(m.decrYpt(CryptoMode.SECURE_KEYIMAGE, new File(encDir, "note.txt.scpt"), tmp.newFolder(), PSW.clone(), other, false).resultOK);
		assertFalse(m.decrYpt(CryptoMode.SECURE_KEYIMAGE, new File(encDir, "note.txt.scpt"), tmp.newFolder(), PSW.clone(), null, false).resultOK);
	}
}
