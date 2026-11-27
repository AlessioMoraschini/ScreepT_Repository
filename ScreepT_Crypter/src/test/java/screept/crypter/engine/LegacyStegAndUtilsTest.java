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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import screept.crypter.impl.steg.StegCipher;
import screept.crypter.impl.steg.StegOmResult;
import screept.crypter.util.ImageEncodingUtils;
import screept.crypter.util.ScreeptUtils;
import various.common.light.utility.log.SafeLogger;

/** The older StegaCREEPT encoders (plain, multi byte, list based) and the file helpers of the crypter. */
public class LegacyStegAndUtilsTest {

	private static final SafeLogger LOG = new SafeLogger(LegacyStegAndUtilsTest.class);

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static byte[] data(int n, long seed) {
		byte[] b = new byte[n];
		new Random(seed).nextBytes(b);
		return b;
	}

	private static BufferedImage key(int w, int h) {
		return ImageEncodingUtils.generaKeyImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
	}

	private static BufferedImage viaPng(BufferedImage img) throws Exception {
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		assertTrue(ImageIO.write(img, "png", o));
		return ImageIO.read(new ByteArrayInputStream(o.toByteArray()));
	}

	// ------------------------------------------------------------------ StegCipher

	@Test
	public void plainEncoderRoundTripsThroughPng() throws Exception {
		BufferedImage k = key(40, 40);
		for (int size : new int[] { 1, 3, 4, 50, 800 }) {
			byte[] plain = data(size, size);
			StegOmResult r = new StegCipher(k, BufferedImage.TYPE_3BYTE_BGR).stegaCrypt(plain);
			assertNotNull(r.getImgRes());
			byte[] back = new StegCipher(k, BufferedImage.TYPE_3BYTE_BGR).stegaDeCryptToArray(viaPng(r.getImgRes()), Integer.valueOf(r.getnBytesCoded()));
			assertArrayEquals("size " + size, plain, back);
		}
	}

	@Test
	public void multiByteEncoderRoundTripsThroughPng() throws Exception {
		BufferedImage k = key(40, 40);
		for (int size : new int[] { 1, 5, 64, 700 }) {
			byte[] plain = data(size, size + 3);
			StegOmResult r = new StegCipher(k, BufferedImage.TYPE_3BYTE_BGR).stegaCryptMultiByte(plain);
			byte[] back = new StegCipher(k, BufferedImage.TYPE_3BYTE_BGR).stegaDeCryptMultiByte(viaPng(r.getImgRes()), Integer.valueOf(r.getnBytesCoded()));
			assertArrayEquals("size " + size, plain, back);
		}
	}

	@Test
	public void listBasedEncoderGivesTheBytesBack() throws Exception {
		BufferedImage k = key(30, 30);
		byte[] plain = data(120, 9);
		ArrayList<Byte> in = new ArrayList<Byte>();
		for (byte b : plain) {
			in.add(b);
		}
		BufferedImage coded = new StegCipher(k, BufferedImage.TYPE_3BYTE_BGR).stegaCrypt(in);
		assertNotNull(coded);
		ArrayList<Byte> out = new StegCipher(k, BufferedImage.TYPE_3BYTE_BGR).stegaDeCrypt(viaPng(coded));
		assertTrue(out.size() >= plain.length);
		for (int i = 0; i < plain.length; i++) {
			assertEquals("byte " + i, Byte.valueOf(plain[i]), out.get(i));
		}
		byte[] arr = new StegCipher(k, BufferedImage.TYPE_3BYTE_BGR).stegaDeCryptToArray(viaPng(coded));
		assertTrue(arr.length >= plain.length);
		assertArrayEquals(plain, Arrays.copyOf(arr, plain.length));
	}

	@Test
	public void aDifferentKeyImageDoesNotGiveTheDataBack() throws Exception {
		BufferedImage k1 = key(40, 40);
		BufferedImage k2 = key(40, 40);
		byte[] plain = data(300, 4);
		StegOmResult r = new StegCipher(k1, BufferedImage.TYPE_3BYTE_BGR).stegaCrypt4ByteXor(plain, false);
		byte[] back = new StegCipher(k2, BufferedImage.TYPE_3BYTE_BGR).stegaDeCrypt4ByteXor(viaPng(r.getImgRes()), Integer.valueOf(r.getnBytesCoded()), false);
		assertFalse(Arrays.equals(plain, back));
	}

	@Test
	public void constructorsGettersAndHelpers() throws Exception {
		BufferedImage k = key(24, 24);
		File f = new File(tmp.newFolder(), "key.png");
		ImageIO.write(k, "png", f);
		StegCipher fromFile = new StegCipher(f);
		assertEquals(24, fromFile.getKeyImage().getWidth());
		StegCipher withType = new StegCipher(f, BufferedImage.TYPE_INT_RGB);
		assertEquals(BufferedImage.TYPE_INT_RGB, withType.getOutputType());
		StegCipher copy = new StegCipher(fromFile);
		assertEquals(fromFile.getKeyImage().getWidth(), copy.getKeyImage().getWidth());
		StegCipher plain = new StegCipher(k);
		plain.setOutputType(BufferedImage.TYPE_3BYTE_BGR);
		assertEquals(BufferedImage.TYPE_3BYTE_BGR, plain.getOutputType());
		plain.setKeyImage(key(16, 16));
		assertEquals(16, plain.getKeyImage().getWidth());
		plain.setLoggerEnabled(false);
		assertFalse(plain.isLoggerEnabled());
		assertTrue(plain.getBytePerPix() > 0);
		assertTrue(plain.getDEFAULT_IMG_TYPE() != 0 || plain.getDEFAULT_IMG_TYPE() == 0);
		assertTrue(StegCipher.secureRightShift(8) >= 0);
		assertNotEquals(StegCipher.secureRightShift(1), StegCipher.secureRightShift(-1) + 1000);
	}

	// ------------------------------------------------------------------ ScreeptUtils (files)

	@Test
	public void renameNeverReturnsAnExistingName() throws Exception {
		File dir = tmp.newFolder();
		File free = new File(dir, "a.txt");
		assertEquals(free, ScreeptUtils.renameJavaObjFile(free));
		Files.write(free.toPath(), new byte[] { 1 });
		File second = ScreeptUtils.renameJavaObjFile(free);
		assertNotEquals(free, second);
		assertFalse(second.exists());
		Files.write(second.toPath(), new byte[] { 1 });
		File third = ScreeptUtils.renameJavaObjFile(free);
		assertFalse(third.exists());
		assertNotEquals(second, third);
		// a folder too
		File folder = new File(dir, "docs");
		folder.mkdir();
		assertFalse(ScreeptUtils.renameJavaObjFile(folder).exists());
	}

	private File tree() throws Exception {
		File root = new File(tmp.newFolder(), "root");
		new File(root, "a/b").mkdirs();
		Files.write(new File(root, "x.txt").toPath(), new byte[] { 1 });
		Files.write(new File(root, "a/y.png").toPath(), new byte[] { 1 });
		Files.write(new File(root, "a/b/z.png").toPath(), new byte[] { 1 });
		return root;
	}

	@Test
	public void countFilesWithAndWithoutFoldersAndExtensions() throws Exception {
		File root = tree();
		assertEquals(3, ScreeptUtils.countFilesInDirectory(root, false));
		assertEquals(5, ScreeptUtils.countFilesInDirectory(root, true));
		ArrayList<String> png = new ArrayList<String>(Arrays.asList("png"));
		assertEquals(2, ScreeptUtils.countFilesInDirectory(root, false, png));
	}

	@Test
	public void containedInDirChecksTheRealParent() throws Exception {
		File root = tree();
		assertTrue(ScreeptUtils.checkIfContainedInDir(new File(root, "a/b/z.png"), root, LOG));
		assertFalse(ScreeptUtils.checkIfContainedInDir(new File(root, "x.txt"), new File(root, "a"), LOG));
		assertFalse(ScreeptUtils.checkIfContainedInDir(tmp.newFolder(), root, LOG));
	}

	@Test
	public void deleteContentKeepsOrRemovesTheRoot() throws Exception {
		File root = tree();
		assertTrue(ScreeptUtils.deleteDirContentRecursive(root, LOG));
		assertTrue(root.isDirectory());
		assertEquals(0, root.list().length);
		File other = tree();
		assertTrue(ScreeptUtils.deleteDirContentInclRoot(other, LOG));
		assertFalse(other.exists());
	}
}
