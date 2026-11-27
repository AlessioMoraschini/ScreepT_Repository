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
import java.nio.file.Files;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import screept.crypter.impl.manager.CryptoMode;
import screept.crypter.impl.manager.ScreeptManagerFileOrFolder;

/**
 * Steganography: exact round trips, only +-1 changes in few values, nothing found without the right password, clean
 * errors when the picture was changed, and the whole Crypter path (file and folder hidden, PNG recognised by itself).
 */
public class StegoPictureTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static final char[] PSW = "stego-password 🖼".toCharArray();

	/** A photo-like picture: smooth gradients plus a little noise (and black and white areas, for the 0/255 edges). */
	static BufferedImage photo(int w, int h, int type, long seed) {
		BufferedImage img = new BufferedImage(w, h, type);
		Random r = new Random(seed);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int red = Math.min(255, Math.max(0, x * 255 / w + r.nextInt(7) - 3));
				int green = Math.min(255, Math.max(0, y * 255 / h + r.nextInt(7) - 3));
				int blue = (x + y) % 64 < 8 ? 0 : (x * y) % 97 < 5 ? 255 : 128 + r.nextInt(20);
				img.setRGB(x, y, 0xff000000 | red << 16 | green << 8 | blue);
			}
		}
		return img;
	}

	@Test(timeout = 120000)
	public void hiddenDataComesBackExactlyAndThePictureBarelyChanges() throws Exception {
		BufferedImage cover = photo(320, 240, BufferedImage.TYPE_INT_RGB, 1);
		long cap = StegoPicture.capacity(320, 240);
		assertEquals(320L * 240 * 3 / 4 / 8 - 8, cap);
		for (long size : new long[] { 0, 1, 100, 4096, cap }) {
			byte[] data = CrypterTortureTest.data((int) size, size);
			BufferedImage stego = StegoPicture.hide(cover, data, PSW.clone());
			// through a real PNG file, as the Crypter does
			File f = new File(tmp.getRoot(), "s" + size + ".png");
			ImageIO.write(stego, "png", f);
			assertArrayEquals("size " + size, data, StegoPicture.reveal(ImageIO.read(f), PSW.clone()));

			int changed = 0;
			for (int y = 0; y < 240; y++) {
				for (int x = 0; x < 320; x++) {
					int a = cover.getRGB(x, y), b = stego.getRGB(x, y);
					for (int s = 0; s < 24; s += 8) {
						int d = Math.abs(((a >>> s) & 0xff) - ((b >>> s) & 0xff));
						assertTrue("only +-1 changes", d <= 1);
						changed += d;
					}
				}
			}
			long bits = (size + 8) * 8;
			assertTrue("changed " + changed + " of " + bits + " bits", changed <= bits);
			if (size == cap) {
				// about half of the bits already match: far less than a full LSB replacement
				assertTrue(changed > bits * 0.4 && changed < bits * 0.6);
			}
		}
	}

	@Test(timeout = 60000)
	public void everyKindOfCoverWorks() throws Exception {
		for (int type : new int[] { BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_4BYTE_ABGR }) {
			BufferedImage cover = photo(97, 61, type, type);
			byte[] data = CrypterTortureTest.data(300, type);
			BufferedImage stego = StegoPicture.hide(cover, data, PSW.clone());
			File f = new File(tmp.getRoot(), "t" + type + ".png");
			ImageIO.write(stego, "png", f);
			assertArrayEquals("type " + type, data, StegoPicture.reveal(ImageIO.read(f), PSW.clone()));
		}
		// a JPEG photo as cover (decoded, then saved as PNG by the Crypter)
		File jpg = new File(tmp.getRoot(), "cover.jpg");
		ImageIO.write(photo(200, 150, BufferedImage.TYPE_INT_RGB, 7), "jpg", jpg);
		byte[] data = CrypterTortureTest.data(500, 8);
		File png = new File(tmp.getRoot(), "fromjpg.png");
		ImageIO.write(StegoPicture.hide(ImageIO.read(jpg), data, PSW.clone()), "png", png);
		assertArrayEquals(data, StegoPicture.reveal(ImageIO.read(png), PSW.clone()));
	}

	@Test(timeout = 60000)
	public void nothingIsFoundWithoutTheRightPassword() throws Exception {
		BufferedImage cover = photo(160, 120, BufferedImage.TYPE_INT_RGB, 2);
		BufferedImage stego = StegoPicture.hide(cover, CrypterTortureTest.data(1000, 1), PSW.clone());
		for (String wrong : new String[] { "stego-password", "x", new String(PSW).toUpperCase() }) {
			expectNothing(stego, wrong);
		}
		expectNothing(cover, new String(PSW)); // a normal picture holds nothing
		expectNothing(photo(160, 120, BufferedImage.TYPE_INT_RGB, 3), new String(PSW));
	}

	@Test(timeout = 60000)
	public void changedPicturesFailCleanly() throws Exception {
		BufferedImage cover = photo(200, 150, BufferedImage.TYPE_INT_RGB, 4);
		byte[] data = CrypterTortureTest.data(2000, 2);
		BufferedImage stego = StegoPicture.hide(cover, data, PSW.clone());
		// saved as JPEG
		File jpg = new File(tmp.getRoot(), "x.jpg");
		ImageIO.write(stego, "jpg", jpg);
		try {
			byte[] got = StegoPicture.reveal(ImageIO.read(jpg), PSW.clone());
			assertFalse("JPEG kept the data unchanged", java.util.Arrays.equals(got, data));
		} catch (CryptoException expected) {
			// usual outcome
		}
		// cropped by one column: other size, other keys
		expectNothing(stego.getSubimage(0, 0, 199, 150), new String(PSW));
		// a few pixels painted over: the hidden SCP2 data would be refused by its own authentication
		BufferedImage painted = ImageIO.read(write(stego, "p.png"));
		for (int x = 0; x < 200; x += 3) {
			painted.setRGB(x, 75, 0xffffff);
		}
		try {
			assertFalse(java.util.Arrays.equals(data, StegoPicture.reveal(painted, PSW.clone())));
		} catch (CryptoException expected) {
			// also fine
		}
	}

	@Test
	public void badCoversAndOversizedDataAreRefused() throws Exception {
		BufferedImage gray = new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_GRAY);
		BufferedImage small = photo(20, 20, BufferedImage.TYPE_INT_RGB, 5);
		Object[][] cases = { { gray, new byte[10], PSW }, { small, new byte[(int) StegoPicture.capacity(20, 20) + 1], PSW },
				{ small, new byte[1], new char[0] }, { null, new byte[1], PSW } };
		for (Object[] c : cases) {
			try {
				StegoPicture.hide((BufferedImage) c[0], (byte[]) c[1], (char[]) c[2]);
				fail("accepted");
			} catch (CryptoException expected) {
				// ok
			}
		}
	}

	// ---------------------------------------------------------------- whole Crypter path

	@Test(timeout = 120000)
	public void filesAndFoldersHiddenInAPictureComeBackIdentical() throws Exception {
		File cover = write(photo(400, 300, BufferedImage.TYPE_INT_RGB, 6), "holiday.jpg.png");
		File file = new File(tmp.newFolder(), "secret notes.txt");
		Files.write(file.toPath(), "città, ñ, 日本\n".getBytes("UTF-8"));
		File folder = tmp.newFolder("my folder");
		Files.write(new File(folder, "a.bin").toPath(), CrypterTortureTest.data(3000, 1));
		new File(folder, "empty").mkdirs();
		new File(folder, "sub").mkdirs();
		Files.write(new File(folder, "sub/b.txt").toPath(), "b".getBytes("UTF-8"));
		for (CryptoProfile p : new CryptoProfile[] { CrypterTortureTest.profile(KdfAlgorithm.ARGON2ID, CipherAlgorithm.AES_256_GCM),
				CrypterTortureTest.profile(KdfAlgorithm.PBKDF2_SHA512, CipherAlgorithm.AES_256_GCM, CipherAlgorithm.CHACHA20_POLY1305,
						CipherAlgorithm.SHUFFLE) }) {
			for (File src : new File[] { file, folder }) {
				File out = FileCrypter.encryptIntoPicture(src, cover, tmp.newFolder(), CryptoKey.of(PSW.clone()), p);
				assertEquals("holiday.jpg.png", out.getName());
				assertTrue(FileCrypter.isPicture(out));
				assertEquals(null, FileCrypter.inspect(out)); // nothing tells it is a ScreepT file
				File dest = tmp.newFolder();
				File back = FileCrypter.decrypt(out, dest, CryptoKey.of(PSW.clone()));
				assertEquals(src.getName(), back.getName());
				LayerMatrixRoundTripTest.assertSameTree(src, back, false);
				assertEquals("only the result in the destination", 1, dest.list().length);

				File wrongDest = tmp.newFolder();
				try {
					FileCrypter.decrypt(out, wrongDest, CryptoKey.of("wrong".toCharArray()));
					fail("wrong password accepted");
				} catch (CryptoException expected) {
					// ok
				}
				assertEquals(0, wrongDest.list().length);
			}
		}
	}

	@Test(timeout = 60000)
	public void tooMuchDataAndPublicKeyModesAreRefusedBeforeWritingAnything() throws Exception {
		File cover = write(photo(40, 30, BufferedImage.TYPE_INT_RGB, 7), "tiny.png");
		File big = new File(tmp.newFolder(), "big.bin");
		Files.write(big.toPath(), CrypterTortureTest.data(5000, 3));
		File dest = tmp.newFolder();
		try {
			FileCrypter.encryptIntoPicture(big, cover, dest, CryptoKey.of(PSW.clone()), CryptoProfile.standard());
			fail("too much data accepted");
		} catch (CryptoException expected) {
			assertTrue(expected.getMessage().contains("at most"));
		}
		try {
			FileCrypter.encryptIntoPicture(big, cover, dest, CryptoKey.of(PSW.clone()), CryptoProfile.forRecipients());
			fail("public key mode accepted");
		} catch (CryptoException expected) {
			// ok
		}
		assertEquals(0, dest.list().length);
	}

	@Test(timeout = 120000)
	public void managerHidesAndFindsThePictureByItself() throws Exception {
		File cover = write(photo(300, 200, BufferedImage.TYPE_INT_RGB, 8), "cat.png");
		File src = new File(tmp.newFolder(), "diary.txt");
		Files.write(src.toPath(), "dear diary".getBytes("UTF-8"));
		ScreeptManagerFileOrFolder m = new ScreeptManagerFileOrFolder(null, null, false);
		m.setCoverPicture(cover);
		File encDir = tmp.newFolder();
		assertTrue(m.crYpt(CryptoMode.SECURE_256, src, encDir, PSW.clone(), null, false).resultOK);
		File png = new File(encDir, "cat.png");
		assertTrue(png.isFile());
		m.setCoverPicture(null);
		File out = tmp.newFolder();
		assertTrue(m.decrYpt(CryptoMode.SECURE_ULTRA, png, out, PSW.clone(), null, false).resultOK);
		assertEquals("dear diary", new String(Files.readAllBytes(new File(out, "diary.txt").toPath()), "UTF-8"));
		assertFalse(m.decrYpt(CryptoMode.SECURE_256, png, tmp.newFolder(), "nope".toCharArray(), null, false).resultOK);
	}

	@Test(timeout = 60000)
	public void hidingPlacesSpreadOverThePicture() throws Exception {
		BufferedImage cover = photo(256, 256, BufferedImage.TYPE_INT_RGB, 9);
		BufferedImage stego = StegoPicture.hide(cover, CrypterTortureTest.data(2000, 4), PSW.clone());
		Map<Integer, Integer> perBand = new TreeMap<Integer, Integer>();
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 256; x++) {
				if (cover.getRGB(x, y) != stego.getRGB(x, y)) {
					perBand.merge(y / 32, 1, Integer::sum);
				}
			}
		}
		// 8 horizontal bands: every band has some changes and none has more than twice the average
		assertEquals(8, perBand.size());
		int total = 0;
		for (int v : perBand.values()) {
			total += v;
		}
		for (int v : perBand.values()) {
			assertTrue(perBand.toString(), v < total / 8 * 2);
		}
	}

	private File write(BufferedImage img, String name) throws Exception {
		File f = new File(tmp.getRoot(), name);
		ImageIO.write(img, "png", f);
		return f;
	}

	private static void expectNothing(BufferedImage img, String psw) {
		try {
			StegoPicture.reveal(img, psw.toCharArray());
			fail("data found with '" + psw + "'");
		} catch (CryptoException expected) {
			// ok
		}
	}
}
