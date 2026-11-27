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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import screept.crypter.engine.FileCrypter.OutputMode;

/** Failure paths and odd inputs of the file/folder layer: what a user can really hit, and what must never be left behind. */
public class FileCrypterEdgeCasesTest {

	private static final char[] PSW = "p4ssw0rd-longer-than-sixteen-chars".toCharArray();
	private static final char[] WRONG = "another-wrong-password-here".toCharArray();
	private static final CryptoProfile FAST = new CryptoProfile(KdfAlgorithm.PBKDF2_SHA256, 1000,
			Arrays.asList(CipherAlgorithm.AES_256_GCM));

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private File file(File dir, String name, byte[] content) throws IOException {
		File f = new File(dir, name);
		f.getParentFile().mkdirs();
		Files.write(f.toPath(), content);
		return f;
	}

	private static byte[] random(int n, long seed) {
		byte[] b = new byte[n];
		new Random(seed).nextBytes(b);
		return b;
	}

	private File sampleTree() throws IOException {
		File root = new File(tmp.newFolder(), "project");
		file(root, "readme.txt", "hello".getBytes(StandardCharsets.UTF_8));
		file(root, "sub/deep/data.bin", random(70000, 1));
		file(root, "sub/empty.txt", new byte[0]);
		file(root, "sub/città è perché.txt", "unicode".getBytes(StandardCharsets.UTF_8));
		new File(root, "emptyDir").mkdirs();
		return root;
	}

	private static void assertSameTree(File a, File b) throws IOException {
		String[] names = a.list();
		assertNotNull(a.getPath(), names);
		Arrays.sort(names);
		String[] other = b.list();
		assertNotNull(b.getPath(), other);
		Arrays.sort(other);
		assertArrayEquals(names, other);
		for (String n : names) {
			File x = new File(a, n), y = new File(b, n);
			if (x.isDirectory()) {
				assertTrue(y.isDirectory());
				assertSameTree(x, y);
			} else {
				assertArrayEquals(n, Files.readAllBytes(x.toPath()), Files.readAllBytes(y.toPath()));
			}
		}
	}

	private static void assertFails(Runnable2 r) {
		try {
			r.run();
			fail("expected a CryptoException");
		} catch (CryptoException expected) {
			// ok
		} catch (Exception e) {
			throw new AssertionError("unexpected " + e, e);
		}
	}

	private interface Runnable2 {
		void run() throws Exception;
	}

	@Test
	public void missingSourceOrUnusableDestinationAreRefused() throws Exception {
		final File dest = tmp.newFolder();
		assertFails(new Runnable2() {
			public void run() throws Exception {
				FileCrypter.encrypt(new File(dest, "nothing"), dest, PSW, FAST, OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
			}
		});
		assertFails(new Runnable2() {
			public void run() throws Exception {
				FileCrypter.encrypt(null, dest, PSW, FAST, OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
			}
		});
		assertFails(new Runnable2() {
			public void run() throws Exception {
				FileCrypter.decrypt(new File(dest, "nothing.scpt"), dest, PSW);
			}
		});
		final File src = file(dest, "a.txt", "a".getBytes());
		final File blocker = file(dest, "blocker", "i am a file".getBytes());
		assertFails(new Runnable2() {
			public void run() throws Exception {
				FileCrypter.encrypt(src, new File(blocker, "sub"), PSW, FAST, OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
			}
		});
		final File enc = FileCrypter.encrypt(src, tmp.newFolder(), PSW, FAST, OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
		assertFails(new Runnable2() {
			public void run() throws Exception {
				FileCrypter.decrypt(enc, new File(blocker, "sub"), PSW);
			}
		});
	}

	@Test
	public void destinationFolderIsCreatedAndNamesAreRestored() throws Exception {
		File src = file(tmp.newFolder(), "notes.txt", random(3000, 2));
		File dest = new File(tmp.newFolder(), "a/b/c");
		File enc = FileCrypter.encrypt(src, dest, PSW, FAST, OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
		assertEquals("notes.txt" + FileCrypter.EXT, enc.getName());
		File back = FileCrypter.decrypt(enc, new File(tmp.getRoot(), "out/x"), PSW);
		assertEquals("notes.txt", back.getName());
		assertArrayEquals(Files.readAllBytes(src.toPath()), Files.readAllBytes(back.toPath()));
		// a file that does not carry the extension gets a visible suffix instead of losing its name
		File renamed = new File(enc.getParentFile(), "mystery");
		assertTrue(enc.renameTo(renamed));
		assertEquals("mystery.decrypted", FileCrypter.decrypt(renamed, tmp.newFolder(), PSW).getName());
	}

	@Test
	public void emptyAndUnicodeContentsRoundTripInEveryMode() throws Exception {
		File src = sampleTree();
		for (OutputMode mode : OutputMode.values()) {
			File enc = FileCrypter.encrypt(src, tmp.newFolder(), PSW, FAST, mode);
			assertTrue(mode + " produced " + enc, enc.exists());
			File outDir = tmp.newFolder();
			File dec = FileCrypter.decrypt(enc, outDir, PSW);
			File root = dec.getName().equals("project") ? dec : new File(dec, "project");
			assertTrue(mode + " restored into " + dec + " " + Arrays.toString(dec.list()), root.isDirectory());
			assertArrayEquals(mode.toString(), Files.readAllBytes(new File(src, "sub/deep/data.bin").toPath()),
					Files.readAllBytes(new File(root, "sub/deep/data.bin").toPath()));
			assertEquals(0, new File(root, "sub/empty.txt").length());
			assertTrue(new File(root, "sub/città è perché.txt").isFile());
			if (mode == OutputMode.PER_FILE_TREE || mode == OutputMode.SINGLE_ENCRYPTED_ARCHIVE) {
				assertTrue("empty folders are kept in " + mode, new File(root, "emptyDir").isDirectory());
			}
		}
	}

	@Test
	public void emptyFileRoundTrips() throws Exception {
		File src = file(tmp.newFolder(), "empty.dat", new byte[0]);
		File enc = FileCrypter.encrypt(src, tmp.newFolder(), PSW, FAST, OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
		assertTrue("the header is there even without content", enc.length() > 0);
		assertEquals(0, FileCrypter.decrypt(enc, tmp.newFolder(), PSW).length());
	}

	@Test
	public void wrongPasswordOnTreeZipAndArchiveLeavesNothingReadable() throws Exception {
		File src = sampleTree();
		for (OutputMode mode : OutputMode.values()) {
			final File enc = FileCrypter.encrypt(src, tmp.newFolder(), PSW, FAST, mode);
			final File out = tmp.newFolder();
			assertFails(new Runnable2() {
				public void run() throws Exception {
					FileCrypter.decrypt(enc, out, WRONG);
				}
			});
			assertEquals(mode + " left " + Arrays.toString(out.list()), 0, countFiles(out));
		}
	}

	private static int countFiles(File dir) {
		int n = 0;
		File[] c = dir.listFiles();
		if (c != null) {
			for (File f : c)
				n += f.isDirectory() ? countFiles(f) : 1;
		}
		return n;
	}

	@Test
	public void truncatedOrAlteredFilesAreRejectedWithoutOutput() throws Exception {
		File src = file(tmp.newFolder(), "big.bin", random(200000, 3));
		File enc = FileCrypter.encrypt(src, tmp.newFolder(), PSW, FAST, OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
		// cut in the middle
		final File cut = new File(tmp.newFolder(), "cut.bin" + FileCrypter.EXT);
		byte[] all = Files.readAllBytes(enc.toPath());
		Files.write(cut.toPath(), Arrays.copyOf(all, all.length / 2));
		final File out = tmp.newFolder();
		assertFails(new Runnable2() {
			public void run() throws Exception {
				FileCrypter.decrypt(cut, out, PSW);
			}
		});
		assertEquals(0, countFiles(out));
		// one flipped bit deep in the data
		final File flipped = new File(tmp.newFolder(), "flip.bin" + FileCrypter.EXT);
		Files.write(flipped.toPath(), all);
		try (RandomAccessFile raf = new RandomAccessFile(flipped, "rw")) {
			raf.seek(all.length - 100);
			int b = raf.read();
			raf.seek(all.length - 100);
			raf.write(b ^ 1);
		}
		assertFails(new Runnable2() {
			public void run() throws Exception {
				FileCrypter.decrypt(flipped, out, PSW);
			}
		});
		assertEquals(0, countFiles(out));
		// not an engine file at all
		final File plain = file(tmp.newFolder(), "plain.txt" + FileCrypter.EXT, "this is not encrypted".getBytes());
		assertFails(new Runnable2() {
			public void run() throws Exception {
				FileCrypter.decrypt(plain, out, PSW);
			}
		});
		assertEquals(0, countFiles(out));
	}

	@Test
	public void truncatedFolderArchiveLeavesNoPartialFolder() throws Exception {
		File src = sampleTree();
		File enc = FileCrypter.encrypt(src, tmp.newFolder(), PSW, FAST, OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
		final File cut = new File(tmp.newFolder(), enc.getName());
		byte[] all = Files.readAllBytes(enc.toPath());
		Files.write(cut.toPath(), Arrays.copyOf(all, all.length - 40));
		final File out = tmp.newFolder();
		assertFails(new Runnable2() {
			public void run() throws Exception {
				FileCrypter.decrypt(cut, out, PSW);
			}
		});
		assertEquals("no half-extracted folder", 0, countFiles(out));
	}

	@Test
	public void inspectReadsTheProfileFromTheHeaderOnly() throws Exception {
		File src = file(tmp.newFolder(), "a.txt", random(500, 4));
		File enc = FileCrypter.encrypt(src, tmp.newFolder(), PSW, FAST, OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
		CryptoProfile p = FileCrypter.inspect(enc);
		assertNotNull(p);
		assertEquals(1, p.getLayers().size());
		assertNull("a plain file", FileCrypter.inspect(src));
		assertNull("a folder", FileCrypter.inspect(src.getParentFile()));
		assertNull("missing", FileCrypter.inspect(new File(tmp.getRoot(), "ghost")));
		assertNull("null", FileCrypter.inspect(null));
		assertNull("empty file", FileCrypter.inspect(file(tmp.newFolder(), "empty", new byte[0])));
	}

	@Test
	public void recognisesWhatTheEngineProduced() throws Exception {
		File dir = sampleTree();
		File plainZip = new File(tmp.newFolder(), "plain.zip");
		try (ZipOutputStream z = new ZipOutputStream(Files.newOutputStream(plainZip.toPath()))) {
			z.putNextEntry(new ZipEntry("a.txt"));
			z.write(1);
			z.closeEntry();
		}
		assertFalse("plain folder", FileCrypter.isEngineInput(dir));
		assertFalse("zip of plain files", FileCrypter.isEngineInput(plainZip));
		assertFalse("missing", FileCrypter.isEngineInput(new File(tmp.getRoot(), "ghost")));
		assertFalse("null", FileCrypter.isEngineInput(null));
		assertFalse("a file that is not a zip, named zip", FileCrypter.isEngineInput(file(tmp.newFolder(), "fake.zip", "no".getBytes())));
		for (OutputMode mode : OutputMode.values()) {
			File enc = FileCrypter.encrypt(dir, tmp.newFolder(), PSW, FAST, mode);
			assertTrue(mode + " output is recognised", FileCrypter.isEngineInput(enc));
		}
		File single = FileCrypter.encrypt(new File(dir, "readme.txt"), tmp.newFolder(), PSW, FAST, OutputMode.ZIP_OF_ENCRYPTED_FILES);
		assertTrue(single.getName().endsWith(".zip") && FileCrypter.isEngineInput(single));
	}

	@Test
	public void zipOfEncryptedFilesRefusesEntriesOutsideTheDestination() throws Exception {
		final File evil = new File(tmp.newFolder(), "evil.zip");
		try (ZipOutputStream z = new ZipOutputStream(Files.newOutputStream(evil.toPath()))) {
			z.putNextEntry(new ZipEntry("../../escaped.txt" + FileCrypter.EXT));
			z.write(new byte[] { 1, 2, 3 });
			z.closeEntry();
		}
		final File out = new File(tmp.newFolder(), "safe");
		try {
			FileCrypter.decrypt(evil, out, PSW);
			fail("an entry outside the destination was accepted");
		} catch (IOException | CryptoException expected) {
			// refused
		}
		assertFalse(new File(out.getParentFile().getParentFile(), "escaped.txt").exists());
	}

	@Test
	public void existingOutputsGetNumberedNames() throws Exception {
		File src = file(tmp.newFolder(), "doc.txt", random(100, 5));
		File dest = tmp.newFolder();
		File first = FileCrypter.encrypt(src, dest, PSW, FAST, OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
		File second = FileCrypter.encrypt(src, dest, PSW, FAST, OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
		File third = FileCrypter.encrypt(src, dest, PSW, FAST, OutputMode.SINGLE_ENCRYPTED_ARCHIVE);
		assertEquals("doc.txt.scpt", first.getName());
		assertEquals("doc (1).txt.scpt", second.getName());
		assertEquals("doc (2).txt.scpt", third.getName());
		File out = tmp.newFolder();
		FileCrypter.decrypt(first, out, PSW);
		assertEquals("doc (1).txt", FileCrypter.decrypt(first, out, PSW).getName());
	}
}
