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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import screept.crypter.engine.FileCrypter.OutputMode;

/**
 * Every valid ordered combination of 1 to 3 layers (AES-128, AES-256, key image, shuffle) with the three key
 * derivations, on a file and on a folder in every output mode. The decryption is never told how the data was
 * protected: it must recognise the profile from the header alone and give back exactly the same bytes.
 */
public class LayerMatrixRoundTripTest {

	private static final char[] PSW = "matrix-password-longer-than-sixteen".toCharArray();
	private static final CipherLayer[] LAYERS = { CipherAlgorithm.AES_128_GCM, CipherAlgorithm.AES_256_GCM,
			CipherAlgorithm.STEGA_CREEPT, CipherAlgorithm.SHUFFLE };
	private static final int MIN_CHUNK_LOG2 = 10; // 1 KiB chunks
	private static final KdfAlgorithm[] KDFS = { KdfAlgorithm.PBKDF2_SHA256, KdfAlgorithm.PBKDF2_SHA512, KdfAlgorithm.SCRYPT };

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	/** All the valid ordered sequences, small chunks (1 KiB) so a few KB of data already span several of them. */
	static List<CryptoProfile> allProfiles() {
		List<CryptoProfile> profiles = new ArrayList<CryptoProfile>();
		int n = 0;
		for (int length = 1; length <= 3; length++) {
			int total = (int) Math.pow(LAYERS.length, length);
			for (int code = 0; code < total; code++) {
				List<CipherLayer> sequence = new ArrayList<CipherLayer>();
				int rest = code;
				for (int i = 0; i < length; i++) {
					sequence.add(LAYERS[rest % LAYERS.length]);
					rest /= LAYERS.length;
				}
				boolean authenticated = false;
				for (CipherLayer layer : sequence) {
					authenticated |= layer.authenticated();
				}
				if (!authenticated) {
					continue; // the engine refuses it (tested in LayerArchitectureTest)
				}
				KdfAlgorithm kdf = KDFS[n++ % KDFS.length];
				profiles.add(new CryptoProfile(kdf, kdf == KdfAlgorithm.SCRYPT ? 10 : 1000, MIN_CHUNK_LOG2, sequence));
			}
		}
		return profiles;
	}

	static File write(File f, int size, long seed) throws IOException {
		f.getParentFile().mkdirs();
		byte[] b = new byte[size];
		new Random(seed).nextBytes(b);
		Files.write(f.toPath(), b);
		return f;
	}

	private File sampleFolder() throws IOException {
		File root = new File(tmp.newFolder(), "matrix dir");
		write(new File(root, "a.txt"), 700, 1);
		write(new File(root, "empty.bin"), 0, 2);
		write(new File(root, "sub/città è bella.dat"), 5000, 3);
		write(new File(root, "sub/deeper/exact.bin"), 2048, 4); // exactly two chunks
		new File(root, "emptyDir").mkdirs();
		return root;
	}

	static File keyImage(File folder) throws IOException {
		BufferedImage image = new BufferedImage(48, 48, BufferedImage.TYPE_INT_RGB);
		Random random = new Random(99);
		for (int x = 0; x < 48; x++) {
			for (int y = 0; y < 48; y++) {
				image.setRGB(x, y, random.nextInt(0x1000000));
			}
		}
		File file = new File(folder, "key.png");
		ImageIO.write(image, "png", file);
		return file;
	}

	/** Names inside a folder; a zip of encrypted files carries files only, so empty folders are not expected there */
	private static String[] names(File folder, boolean filesOnly) {
		List<String> names = new ArrayList<String>();
		for (String name : folder.list()) {
			File child = new File(folder, name);
			if (!filesOnly || !child.isDirectory() || names(child, true).length > 0) {
				names.add(name);
			}
		}
		String[] result = names.toArray(new String[0]);
		Arrays.sort(result);
		return result;
	}

	static void assertSameTree(File expected, File actual, boolean filesOnly) throws IOException {
		assertTrue(actual + " missing", actual.exists());
		if (expected.isDirectory()) {
			assertTrue(actual + " is not a folder", actual.isDirectory());
			String[] names = names(expected, filesOnly);
			assertArrayEquals(names, names(actual, false));
			for (String name : names) {
				assertSameTree(new File(expected, name), new File(actual, name), filesOnly);
			}
		} else {
			assertArrayEquals(Files.readAllBytes(expected.toPath()), Files.readAllBytes(actual.toPath()));
		}
	}

	private CryptoKey keyFor(CryptoProfile profile, File keyImage) throws CryptoException {
		return profile.needsKeyImage() ? CryptoKey.of(PSW.clone(), keyImage) : CryptoKey.of(PSW.clone());
	}

	@Test
	public void matrixHasTheExpectedSize() {
		// 4 + 16 + 64 sequences, minus the 3 made only of shuffle
		assertEquals(81, allProfiles().size());
	}

	@Test
	public void everyCombinationRoundTripsAFileAndIsRecognisedFromTheHeader() throws Exception {
		File keyImage = keyImage(tmp.newFolder());
		for (CryptoProfile profile : allProfiles()) {
			File src = write(new File(tmp.newFolder(), "doc.bin"), 5000, 11);
			File enc = FileCrypter.encrypt(src, tmp.newFolder(), keyFor(profile, keyImage), profile, OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
			CryptoProfile seen = FileCrypter.inspect(enc);
			assertNotNull(profile.describe(), seen);
			assertEquals(profile.describe(), seen.describe());
			assertEquals(profile.needsKeyImage(), seen.needsKeyImage());

			File dec = FileCrypter.decrypt(enc, tmp.newFolder(), keyFor(seen, keyImage));
			assertSameTree(src, dec, false);
		}
	}

	@Test
	public void everyCombinationRoundTripsAFolderInEveryOutputMode() throws Exception {
		File keyImage = keyImage(tmp.newFolder());
		File src = sampleFolder();
		for (CryptoProfile profile : allProfiles()) {
			for (OutputMode mode : OutputMode.values()) {
				String where = profile.describe() + " / " + mode;
				File enc = FileCrypter.encrypt(src, tmp.newFolder(), keyFor(profile, keyImage), profile, mode);
				assertTrue(where, FileCrypter.isEngineInput(enc));

				File decDir = tmp.newFolder();
				File dec = FileCrypter.decrypt(enc, decDir, keyFor(profile, keyImage));
				File result = dec.getName().equals(src.getName()) ? dec : new File(decDir, src.getName());
				try {
					assertSameTree(src, result, mode == OutputMode.ZIP_OF_ENCRYPTED_FILES);
				} catch (AssertionError e) {
					throw new AssertionError(where + ": " + e.getMessage());
				}
			}
		}
	}

	@Test
	public void keyImageCombinationsRefuseAMissingOrWrongImage() throws Exception {
		File keyImage = keyImage(tmp.newFolder());
		File other = keyImage(tmp.newFolder());
		BufferedImage changed = ImageIO.read(other);
		changed.setRGB(0, 0, changed.getRGB(0, 0) ^ 0x010101);
		ImageIO.write(changed, "png", other);

		int checked = 0;
		for (CryptoProfile profile : allProfiles()) {
			if (!profile.needsKeyImage()) {
				continue;
			}
			File src = write(new File(tmp.newFolder(), "s.bin"), 3000, 5);
			File enc = FileCrypter.encrypt(src, tmp.newFolder(), keyFor(profile, keyImage), profile, OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
			for (CryptoKey bad : new CryptoKey[] { CryptoKey.of(PSW.clone()), CryptoKey.of(PSW.clone(), other),
					CryptoKey.of("wrong password, long enough".toCharArray(), keyImage) }) {
				File out = tmp.newFolder();
				try {
					FileCrypter.decrypt(enc, out, bad);
					fail("accepted a wrong key for " + profile.describe());
				} catch (CryptoException expected) {
					assertEquals("plaintext left behind for " + profile.describe(), 0, out.list().length);
				}
			}
			checked++;
		}
		assertTrue(checked > 0);
	}
}
