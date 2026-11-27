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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Files and folders: a nasty tree (nested, empty folders and files, unicode and dotted names, many chunks) must come back
 * byte for byte in every output mode and with every kind of profile, recognised from the headers alone. Damaged, truncated
 * or wrong-password archives must fail cleanly: nothing left in the destination and no worker thread left running.
 */
public class FileCrypterTortureTest {

	private static final char[] PSW = "file-torture 🔒".toCharArray();

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static final CryptoProfile[] PROFILES = {
			CrypterTortureTest.profile(KdfAlgorithm.PBKDF2_SHA512, CipherAlgorithm.AES_256_GCM),
			CrypterTortureTest.profile(KdfAlgorithm.ARGON2ID, CipherAlgorithm.AES_256_GCM, CipherAlgorithm.CHACHA20_POLY1305),
			CrypterTortureTest.profile(KdfAlgorithm.SCRYPT, CipherAlgorithm.CHACHA20_POLY1305, CipherAlgorithm.SHUFFLE) };

	private File nastyTree() throws IOException {
		File root = tmp.newFolder("my docs.v2");
		Random rnd = new Random(5);
		write(new File(root, "empty.txt"), new byte[0]);
		write(new File(root, "città ñ 日本 €.txt"), "unicode name\n".getBytes("UTF-8"));
		write(new File(root, "archive.tar.gz"), bytes(rnd, 5000));
		write(new File(root, ".hidden"), bytes(rnd, 1));
		write(new File(root, "no_extension"), bytes(rnd, 1024));
		write(new File(root, "already.scpt.txt"), bytes(rnd, 77));
		new File(root, "empty folder").mkdirs();
		File deep = new File(root, "a/b c/d.e/f");
		deep.mkdirs();
		write(new File(deep, "deep.bin"), bytes(rnd, 3 * 1024 + 17)); // 4 chunks of 1 KiB
		write(new File(root, "a/sibling.dat"), bytes(rnd, 2048));
		return root;
	}

	@Test(timeout = 120000)
	public void nastyTreeComesBackIdenticalInEveryModeAndProfile() throws Exception {
		File src = nastyTree();
		Map<String, String> expected = snapshot(src);
		for (CryptoProfile p : PROFILES) {
			for (FileCrypter.OutputMode mode : FileCrypter.OutputMode.values()) {
				File enc = tmp.newFolder();
				File produced = FileCrypter.encrypt(src, enc, CryptoKey.of(PSW.clone()), p, mode);
				assertTrue(FileCrypter.isEngineInput(produced));
				if (produced.isFile() && mode == FileCrypter.OutputMode.SINGLE_ENCRYPTED_ARCHIVE) {
					assertEquals(p.describe(), FileCrypter.inspect(produced).describe()); // recognised from the header only
				}
				File dec = tmp.newFolder();
				File out = FileCrypter.decrypt(produced, dec, CryptoKey.of(PSW.clone()));
				File restored = out.getName().equals(src.getName()) ? out : new File(dec, src.getName());
				assertEquals(p + " / " + mode, expected, snapshot(restored));
				assertNoWorkers();
			}
		}
	}

	@Test(timeout = 60000)
	public void singleFilesOfEverySizeComeBackIdentical() throws Exception {
		Random rnd = new Random(6);
		for (int size : new int[] { 0, 1, 1023, 1024, 1025, 5000 }) {
			File f = new File(tmp.newFolder(), "f" + size + ".bin");
			write(f, bytes(rnd, size));
			for (CryptoProfile p : PROFILES) {
				File enc = FileCrypter.encrypt(f, tmp.newFolder(), CryptoKey.of(PSW.clone()), p, FileCrypter.OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
				assertEquals(f.getName() + FileCrypter.EXT, enc.getName());
				CryptoProfile seen = FileCrypter.inspect(enc);
				assertNotNull(seen);
				assertEquals(p.getLayers(), seen.getLayers());
				assertEquals(p.kdf, seen.kdf);
				File dec = FileCrypter.decrypt(enc, tmp.newFolder(), CryptoKey.of(PSW.clone()));
				assertEquals(f.getName(), dec.getName());
				assertArrayEquals(Files.readAllBytes(f.toPath()), Files.readAllBytes(dec.toPath()));
			}
		}
	}

	@Test(timeout = 60000)
	public void neverOverwritesExistingFiles() throws Exception {
		File f = new File(tmp.newFolder(), "note.txt");
		write(f, "original".getBytes("UTF-8"));
		File encDir = tmp.newFolder();
		File e1 = FileCrypter.encrypt(f, encDir, CryptoKey.of(PSW.clone()), PROFILES[0], FileCrypter.OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
		File e2 = FileCrypter.encrypt(f, encDir, CryptoKey.of(PSW.clone()), PROFILES[0], FileCrypter.OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
		assertFalse(e1.equals(e2));
		File decDir = tmp.newFolder();
		write(new File(decDir, "note.txt"), "do not touch".getBytes("UTF-8"));
		File d = FileCrypter.decrypt(e1, decDir, CryptoKey.of(PSW.clone()));
		assertFalse(d.getName().equals("note.txt"));
		assertEquals("do not touch", new String(Files.readAllBytes(new File(decDir, "note.txt").toPath()), "UTF-8"));
		assertEquals("original", new String(Files.readAllBytes(d.toPath()), "UTF-8"));
	}

	/** Damaged, truncated or wrong password archives: a clean error, an untouched destination, no thread left behind. */
	@Test(timeout = 120000)
	public void badArchivesLeaveNothingBehind() throws Exception {
		File src = nastyTree();
		for (CryptoProfile p : PROFILES) {
			File archive = FileCrypter.encrypt(src, tmp.newFolder(), CryptoKey.of(PSW.clone()), p, FileCrypter.OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
			byte[] ok = Files.readAllBytes(archive.toPath());
			Random rnd = new Random(p.describe().hashCode());
			for (int k = 0; k <= 10; k++) {
				byte[] bad = ok.clone();
				String what;
				if (k < 5) {
					int pos = CrypterTortureTest.headerLength(p) + rnd.nextInt(ok.length - CrypterTortureTest.headerLength(p));
					bad[pos] ^= 1 << rnd.nextInt(8);
					what = "bit flipped at " + pos;
				} else if (k < 10) {
					bad = Arrays.copyOf(ok, CrypterTortureTest.headerLength(p) + rnd.nextInt(ok.length - CrypterTortureTest.headerLength(p)));
					what = "cut at " + bad.length;
				} else {
					what = "wrong password";
				}
				File damaged = new File(tmp.newFolder(), src.getName() + FileCrypter.DIR_EXT);
				write(damaged, bad);
				File dest = tmp.newFolder();
				try {
					FileCrypter.decrypt(damaged, dest, CryptoKey.of(k == 10 ? "wrong".toCharArray() : PSW.clone()));
					fail(p + ": accepted " + what);
				} catch (CryptoException | IOException expected) {
					// ok
				}
				assertNoWorkers();
				String[] left = dest.list();
				assertEquals(p + " / " + what + " left " + Arrays.toString(left), 0, left.length);
			}
		}
	}

	@Test(timeout = 60000)
	public void damagedSingleFileLeavesNothingBehind() throws Exception {
		File f = new File(tmp.newFolder(), "big.bin");
		write(f, bytes(new Random(8), 5000));
		File enc = FileCrypter.encrypt(f, tmp.newFolder(), CryptoKey.of(PSW.clone()), PROFILES[1], FileCrypter.OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
		byte[] b = Files.readAllBytes(enc.toPath());
		b[b.length - 3] ^= 0x10; // last chunk: the first ones are already written when the error is found
		write(enc, b);
		File dest = tmp.newFolder();
		try {
			FileCrypter.decrypt(enc, dest, CryptoKey.of(PSW.clone()));
			fail("damaged file accepted");
		} catch (CryptoException expected) {
			// ok
		}
		assertEquals(0, dest.list().length);
	}

	// ---------------------------------------------------------------- helpers

	private static void assertNoWorkers() throws InterruptedException {
		for (int i = 0; i < 50; i++) {
			boolean alive = false;
			for (Thread t : Thread.getAllStackTraces().keySet()) {
				alive |= t.isAlive() && (t.getName().startsWith("Crypter-") || t.getName().startsWith("Argon2-"));
			}
			if (!alive) {
				return;
			}
			Thread.sleep(20);
		}
		fail("a crypter worker thread is still running");
	}

	private static byte[] bytes(Random rnd, int n) {
		byte[] b = new byte[n];
		rnd.nextBytes(b);
		return b;
	}

	private static void write(File f, byte[] b) throws IOException {
		f.getParentFile().mkdirs();
		Files.write(f.toPath(), b);
	}

	/** Relative path -> content digest ("dir" for folders). */
	private static Map<String, String> snapshot(File root) throws Exception {
		Map<String, String> m = new TreeMap<String, String>();
		snapshot(root, "", m);
		return m;
	}

	private static void snapshot(File dir, String prefix, Map<String, String> m) throws Exception {
		File[] children = dir.listFiles();
		assertNotNull(dir.toString(), children);
		for (File c : children) {
			String rel = prefix + c.getName();
			if (c.isDirectory()) {
				m.put(rel + "/", "dir");
				snapshot(c, rel + "/", m);
			} else {
				m.put(rel, PrimitiveVectorsTest.hex(java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(c.toPath()))));
			}
		}
	}
}
