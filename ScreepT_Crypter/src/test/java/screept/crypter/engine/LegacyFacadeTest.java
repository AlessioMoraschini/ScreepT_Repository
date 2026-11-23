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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import screept.crypter.impl.manager.CryptoMode;
import screept.crypter.impl.manager.ScreeptManagerFileOrFolder;
import screept.crypter.om.CrypterResultDTO;

/** The facade of the GUI with the legacy modes (AES-128 CBC, key image, both), on a file and on a folder. */
public class LegacyFacadeTest {

	private static final char[] PSW = "legacy facade password 456".toCharArray();

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private File keyImage() throws Exception {
		BufferedImage img = new BufferedImage(48, 48, BufferedImage.TYPE_3BYTE_BGR);
		Random r = new Random(11);
		for (int y = 0; y < 48; y++) {
			for (int x = 0; x < 48; x++) {
				img.setRGB(x, y, r.nextInt(0xFFFFFF));
			}
		}
		File f = new File(tmp.newFolder(), "key.png");
		ImageIO.write(img, "png", f);
		return f;
	}

	private static File find(File dir, String name) {
		File[] list = dir.listFiles();
		if (list == null) {
			return null;
		}
		for (File f : list) {
			if (f.isFile() && f.getName().equals(name)) {
				return f;
			}
			File deeper = f.isDirectory() ? find(f, name) : null;
			if (deeper != null) {
				return deeper;
			}
		}
		return null;
	}

	private static File firstChild(File dir) {
		File[] list = dir.listFiles();
		java.util.Arrays.sort(list);
		return list[0];
	}

	private ScreeptManagerFileOrFolder facade() {
		return new ScreeptManagerFileOrFolder(null, null, false);
	}

	@Test
	public void legacyAesFileAndFolder() throws Exception {
		Assume.assumeFalse(GraphicsEnvironment.isHeadless());
		byte[] a = new byte[20_000];
		new Random(1).nextBytes(a);
		File dir = new File(tmp.newFolder(), "docs");
		new File(dir, "sub").mkdirs();
		Files.write(new File(dir, "a.bin").toPath(), a);
		Files.write(new File(dir, "sub/b.txt").toPath(), "hello b".getBytes("UTF-8"));

		// single file
		File single = new File(tmp.newFolder(), "single.dat");
		Files.write(single.toPath(), a);
		File encDir = tmp.newFolder();
		ScreeptManagerFileOrFolder f = facade();
		CrypterResultDTO r = f.crYpt(CryptoMode.ONLY_AES, single, encDir, PSW.clone(), null, false);
		assertTrue(f.getOutputStats(), r.resultOK);
		File encFile = find(encDir, "single.dat");
		assertNotNull(encFile);
		File decDir = tmp.newFolder();
		r = facade().decrYpt(CryptoMode.ONLY_AES, encFile, decDir, PSW.clone(), null, false);
		assertTrue(r.resultOK);
		assertArrayEquals(a, Files.readAllBytes(find(decDir, "single.dat").toPath()));

		// folder
		File encDir2 = tmp.newFolder();
		r = facade().crYpt(CryptoMode.ONLY_AES, dir, encDir2, PSW.clone(), null, true);
		assertTrue(r.resultOK);
		File encRoot = find(encDir2, "a.bin") != null ? find(encDir2, "a.bin").getParentFile() : firstChild(encDir2);
		File decDir2 = tmp.newFolder();
		r = facade().decrYpt(CryptoMode.ONLY_AES, firstChild(encDir2), decDir2, PSW.clone(), null, true);
		assertTrue(r.resultOK);
		assertArrayEquals(a, Files.readAllBytes(find(decDir2, "a.bin").toPath()));
		assertArrayEquals("hello b".getBytes("UTF-8"), Files.readAllBytes(find(decDir2, "b.txt").toPath()));
		assertNotNull(encRoot);
	}

	@Test
	public void legacyKeyImageModesRoundTrip() throws Exception {
		Assume.assumeFalse(GraphicsEnvironment.isHeadless());
		File key = keyImage();
		for (CryptoMode mode : new CryptoMode[] { CryptoMode.ONLY_STEG, CryptoMode.COMPLETE }) {
			byte[] data = new byte[6_000];
			new Random(mode.ordinal()).nextBytes(data);
			File src = new File(tmp.newFolder(), "note.bin");
			Files.write(src.toPath(), data);
			File encDir = tmp.newFolder();
			CrypterResultDTO r = facade().crYpt(mode, src, encDir, PSW.clone(), key, false);
			assertTrue(mode + " crypt", r.resultOK);
			File images = null;
			for (File c : encDir.listFiles()) {
				if (c.isDirectory()) {
					images = c;
				}
			}
			assertNotNull("folder of images", images);
			File decDir = tmp.newFolder();
			r = facade().decrYpt(mode, images, decDir, PSW.clone(), key, false);
			assertTrue(mode + " decrypt", r.resultOK);
			assertArrayEquals(mode.toString(), data, Files.readAllBytes(find(decDir, "note.bin").toPath()));
		}
	}

	@Test
	public void missingSourceOrFolderDoesNotCrash() throws Exception {
		Assume.assumeFalse(GraphicsEnvironment.isHeadless());
		ScreeptManagerFileOrFolder f = facade();
		CrypterResultDTO r = f.crYpt(CryptoMode.ONLY_AES, new File(tmp.getRoot(), "nope.txt"), tmp.newFolder(), PSW.clone(), null, false);
		assertFalse(r.resultOK);
		r = facade().crYpt(CryptoMode.ONLY_AES, null, null, PSW.clone(), null, false);
		assertFalse(r.resultOK);
	}

	@Test
	public void facadeAccessors() {
		ScreeptManagerFileOrFolder f = facade();
		f.setShuffle(true);
		assertTrue(f.isShuffle());
		f.setHashed(false);
		assertFalse(f.isHashed());
		f.setAdvicesEnabled(false);
		f.setLoggerEnabled(false);
		f.setDefaultProgressBarVisible(false);
		assertFalse(f.isDefaultProgressBarVisible());
		assertNotNull(f.getOutputStats());
	}
}
