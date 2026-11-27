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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End to end: real temporary files and folders, no GUI, every output mode and profile combination. */
public class FileCrypterE2ETest {

	private static final char[] PSW = "p4ssw0rd-longer-than-sixteen-chars".toCharArray();

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static List<CryptoProfile> profiles() {
		List<CryptoProfile> l = new ArrayList<CryptoProfile>();
		l.add(new CryptoProfile(KdfAlgorithm.PBKDF2_SHA512, 1000, Arrays.asList(CipherAlgorithm.AES_128_GCM)));
		l.add(new CryptoProfile(KdfAlgorithm.PBKDF2_SHA256, 1000, Arrays.asList(CipherAlgorithm.AES_256_GCM)));
		l.add(new CryptoProfile(KdfAlgorithm.SCRYPT, 10,
				Arrays.asList(CipherAlgorithm.AES_128_GCM, CipherAlgorithm.AES_256_GCM)));
		return l;
	}

	private File write(File f, int size, long seed) throws IOException {
		f.getParentFile().mkdirs();
		byte[] b = new byte[size];
		new Random(seed).nextBytes(b);
		Files.write(f.toPath(), b);
		return f;
	}

	/** A folder with nesting, an empty file, an empty subfolder, unicode names and a multi-chunk file. */
	private File sampleFolder(String name) throws IOException {
		File root = new File(tmp.newFolder(), name);
		write(new File(root, "a.txt"), 1000, 1);
		write(new File(root, "empty.bin"), 0, 2);
		write(new File(root, "sub/deep/città è bella.dat"), 2_500_000, 3);
		write(new File(root, "sub/x.bin"), 1 << 20, 4); // exactly one chunk
		new File(root, "emptyDir").mkdirs();
		return root;
	}

	private static void assertSameTree(File expected, File actual) throws IOException {
		assertTrue(actual + " missing", actual.exists());
		if (expected.isDirectory()) {
			assertTrue(actual.isDirectory());
			String[] names = expected.list();
			Arrays.sort(names);
			String[] got = actual.list();
			Arrays.sort(got);
			assertArrayEquals(names, got);
			for (String n : names) {
				assertSameTree(new File(expected, n), new File(actual, n));
			}
		} else {
			assertArrayEquals(Files.readAllBytes(expected.toPath()), Files.readAllBytes(actual.toPath()));
		}
	}

	@Test
	public void singleFileRoundTripAllProfiles() throws Exception {
		for (CryptoProfile p : profiles()) {
			File src = write(new File(tmp.newFolder(), "doc.txt"), 3_000_000, 7);
			File enc = FileCrypter.encrypt(src, tmp.newFolder(), PSW, p, FileCrypter.OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
			assertEquals("doc.txt.scpt", enc.getName());
			assertEquals(p.describe(), FileCrypter.inspect(enc).describe());
			File dec = FileCrypter.decrypt(enc, tmp.newFolder(), PSW);
			assertEquals("doc.txt", dec.getName());
			assertSameTree(src, dec);
		}
	}

	@Test
	public void folderAsSingleEncryptedArchiveHidesNamesAndRoundTrips() throws Exception {
		for (CryptoProfile p : profiles()) {
			File src = sampleFolder("My Folder");
			File outDir = tmp.newFolder();
			File enc = FileCrypter.encrypt(src, outDir, PSW, p, FileCrypter.OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
			assertEquals("My Folder.dir.scpt", enc.getName());
			assertEquals(1, outDir.list().length);
			String raw = new String(Files.readAllBytes(enc.toPath()), "ISO-8859-1");
			assertFalse(raw.contains("a.txt") || raw.contains("deep"));

			File decDir = tmp.newFolder();
			FileCrypter.decrypt(enc, decDir, PSW);
			assertSameTree(src, new File(decDir, "My Folder"));
		}
	}

	@Test
	public void folderPerFileTreeRoundTrips() throws Exception {
		for (CryptoProfile p : profiles()) {
			File src = sampleFolder("tree");
			File enc = FileCrypter.encrypt(src, tmp.newFolder(), PSW, p, FileCrypter.OutputMode.PER_FILE_TREE);
			assertTrue(new File(enc, "a.txt.scpt").isFile());
			assertTrue(new File(enc, "sub/deep/città è bella.dat.scpt").isFile());
			File decDir = tmp.newFolder();
			File dec = FileCrypter.decrypt(enc, decDir, PSW);
			assertSameTree(src, dec);
		}
	}

	@Test
	public void zipOfEncryptedFilesRoundTripsForFileAndFolder() throws Exception {
		for (CryptoProfile p : profiles()) {
			File src = sampleFolder("zipped");
			File zip = FileCrypter.encrypt(src, tmp.newFolder(), PSW, p, FileCrypter.OutputMode.ZIP_OF_ENCRYPTED_FILES);
			assertEquals("zipped.zip", zip.getName());
			File decDir = tmp.newFolder();
			FileCrypter.decrypt(zip, decDir, PSW);
			// empty folders are kept too (folder entries in the zip)
			assertSameTree(src, new File(decDir, "zipped"));

			File single = write(new File(tmp.newFolder(), "one.bin"), 5000, 9);
			File zip2 = FileCrypter.encrypt(single, tmp.newFolder(), PSW, p, FileCrypter.OutputMode.ZIP_OF_ENCRYPTED_FILES);
			File decDir2 = tmp.newFolder();
			FileCrypter.decrypt(zip2, decDir2, PSW);
			assertSameTree(single, new File(decDir2, "one.bin"));
		}
	}

	@Test
	public void wrongPasswordLeavesNoPlaintextBehind() throws Exception {
		CryptoProfile p = profiles().get(0);
		File src = write(new File(tmp.newFolder(), "secret.txt"), 4000, 5);
		File enc = FileCrypter.encrypt(src, tmp.newFolder(), PSW, p, FileCrypter.OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
		File out = tmp.newFolder();
		try {
			FileCrypter.decrypt(enc, out, "nope nope nope".toCharArray());
			fail("wrong password accepted");
		} catch (CryptoException expected) {
			assertEquals(0, out.list().length);
		}
	}

	@Test
	public void existingFilesAreNeverOverwritten() throws Exception {
		CryptoProfile p = profiles().get(0);
		File src = write(new File(tmp.newFolder(), "f.txt"), 100, 1);
		File outDir = tmp.newFolder();
		File first = FileCrypter.encrypt(src, outDir, PSW, p, FileCrypter.OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
		File second = FileCrypter.encrypt(src, outDir, PSW, p, FileCrypter.OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
		assertFalse(first.equals(second));
		assertEquals(2, outDir.list().length);
	}

	@Test
	public void plainFilesAreNotReportedAsEncrypted() throws Exception {
		File plain = write(new File(tmp.newFolder(), "plain.txt"), 100, 1);
		assertNull(FileCrypter.inspect(plain));
		File enc = FileCrypter.encrypt(plain, tmp.newFolder(), PSW, profiles().get(1), FileCrypter.OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
		assertNotNull(FileCrypter.inspect(enc));
	}

	@Test
	public void zipWithPathTraversalIsRefused() throws Exception {
		CryptoProfile p = profiles().get(0);
		File payload = write(new File(tmp.newFolder(), "evil.txt"), 50, 1);
		File enc = FileCrypter.encrypt(payload, tmp.newFolder(), PSW, p, FileCrypter.OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
		File base = tmp.newFolder();
		File zip = new File(base, "evil.zip");
		try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(zip))) {
			z.putNextEntry(new ZipEntry("../../escaped.txt.scpt"));
			z.write(Files.readAllBytes(enc.toPath()));
			z.closeEntry();
		}
		File dest = new File(base, "out/inner");
		try {
			FileCrypter.decrypt(zip, dest, PSW);
			fail("path traversal accepted");
		} catch (IOException expected) {
			assertFalse(new File(base, "escaped.txt").exists());
			assertFalse(new File(base.getParentFile(), "escaped.txt").exists());
		}
	}
}
