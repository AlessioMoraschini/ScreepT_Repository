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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Assume;
import org.junit.Test;

/**
 * Corner cases of the streaming engine: exact container layout, chunk reordering, header fuzzing, hostile headers,
 * passwords of every kind, short reads, failing streams. Every failure must surface as a {@link CryptoException}
 * (or the I/O error of the stream), never as another exception, a wrong plaintext or an OutOfMemoryError.
 */
public class CryptoEngineEdgeCasesTest {

	private static final char[] PSW = "Tr0ub4dor&3 corner".toCharArray();
	private static final int CHUNK_LOG2 = 10;
	private static final int CHUNK = 1 << CHUNK_LOG2;

	/** Fixed part of the header: magic, version, kdf, cost(4), chunkLog2, layer count */
	private static final int FIXED_HEADER = 12;
	private static final int SALT_PLUS_PREFIX = 16 + 8;
	private static final int COST_OFFSET = 6;

	private static byte[] data(int n) {
		byte[] b = new byte[n];
		new Random(n * 31L + 7).nextBytes(b);
		return b;
	}

	private static CryptoProfile fast(CipherLayer... layers) {
		return new CryptoProfile(KdfAlgorithm.PBKDF2_SHA256, 1000, CHUNK_LOG2, Arrays.asList(layers));
	}

	private static CryptoProfile twoLayers() {
		return fast(CipherAlgorithm.AES_128_GCM, CipherAlgorithm.AES_256_GCM);
	}

	private static int headerLength(CryptoProfile p) {
		return FIXED_HEADER + p.getLayers().size() + SALT_PLUS_PREFIX;
	}

	private static void assertRejected(byte[] enc, String what) {
		try {
			byte[] out = CryptoEngine.decrypt(enc, PSW);
			fail(what + ": accepted (" + out.length + " bytes)");
		} catch (CryptoException expected) {
			// ok
		}
	}

	// ------------------------------------------------------------------ container layout

	@Test
	public void containerLengthIsExactlyHeaderPlusFramesForEverySize() throws Exception {
		for (CryptoProfile p : new CryptoProfile[] { fast(CipherAlgorithm.AES_128_GCM), twoLayers() }) {
			int ovh = p.overheadPerChunk();
			assertEquals(16 * p.getLayers().size(), ovh);
			for (int size : new int[] { 0, 1, CHUNK - 1, CHUNK, CHUNK + 1, 2 * CHUNK, 4 * CHUNK, 4 * CHUNK + 1 }) {
				byte[] enc = CryptoEngine.encrypt(data(size), PSW, p);
				// the last chunk is always shorter than the others, possibly empty: size/CHUNK full frames + one last frame
				int expected = headerLength(p) + (size / CHUNK) * (CHUNK + ovh) + (size % CHUNK) + ovh;
				assertEquals("size " + size + " layers " + p.getLayers().size(), expected, enc.length);
				assertArrayEquals(data(size), CryptoEngine.decrypt(enc, PSW));
			}
		}
	}

	@Test
	public void decryptReturnsTheProfileWrittenInTheHeader() throws Exception {
		CryptoProfile p = new CryptoProfile(KdfAlgorithm.SCRYPT, 10, CHUNK_LOG2,
				Arrays.asList(CipherAlgorithm.AES_256_GCM, CipherAlgorithm.AES_128_GCM));
		byte[] enc = CryptoEngine.encrypt(data(300), PSW, p);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		CryptoProfile read = CryptoEngine.decrypt(new ByteArrayInputStream(enc), out, PSW);
		assertEquals(KdfAlgorithm.SCRYPT, read.kdf);
		assertEquals(10, read.kdfCost);
		assertEquals(CHUNK_LOG2, read.chunkLog2);
		assertEquals(2, read.getLayers().size());
		assertEquals(CipherAlgorithm.AES_256_GCM.id(), read.getLayers().get(0).id());
		assertEquals(CipherAlgorithm.AES_128_GCM.id(), read.getLayers().get(1).id());
		assertEquals(read.kdf, CryptoEngine.readProfile(enc).kdf);
	}

	// ------------------------------------------------------------------ chunk reordering and surgery

	private static byte[][] frames(byte[] enc, CryptoProfile p, int plainSize) {
		int h = headerLength(p);
		int frame = CHUNK + p.overheadPerChunk();
		int n = plainSize / CHUNK + 1;
		byte[][] f = new byte[n + 1][];
		f[0] = Arrays.copyOfRange(enc, 0, h);
		for (int i = 0; i < n; i++) {
			int from = h + i * frame;
			int to = i == n - 1 ? enc.length : from + frame;
			f[i + 1] = Arrays.copyOfRange(enc, from, to);
		}
		return f;
	}

	private static byte[] join(byte[]... parts) {
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		for (byte[] p : parts) {
			o.write(p, 0, p.length);
		}
		return o.toByteArray();
	}

	@Test
	public void reorderedDuplicatedOrRemovedChunksAreRejected() throws Exception {
		CryptoProfile p = twoLayers();
		int size = 3 * CHUNK + 5; // frames: 3 full + 1 last
		byte[] enc = CryptoEngine.encrypt(data(size), PSW, p);
		byte[][] f = frames(enc, p, size); // f[0] header, f[1..4] chunks
		assertArrayEquals(data(size), CryptoEngine.decrypt(join(f[0], f[1], f[2], f[3], f[4]), PSW));
		assertRejected(join(f[0], f[2], f[1], f[3], f[4]), "chunks 0 and 1 swapped");
		assertRejected(join(f[0], f[1], f[3], f[2], f[4]), "chunks 1 and 2 swapped");
		assertRejected(join(f[0], f[1], f[1], f[2], f[3], f[4]), "chunk duplicated");
		assertRejected(join(f[0], f[1], f[3], f[4]), "middle chunk removed");
		assertRejected(join(f[0], f[2], f[3], f[4]), "first chunk removed");
		assertRejected(join(f[0], f[1], f[2], f[3]), "last chunk removed (a full chunk is not a last one)");
		assertRejected(join(f[0], f[1], f[2], f[3], f[4], f[4]), "last chunk duplicated");
	}

	@Test
	public void chunkFromAnotherFileWithSamePasswordIsRejected() throws Exception {
		CryptoProfile p = fast(CipherAlgorithm.AES_256_GCM);
		byte[] a = CryptoEngine.encrypt(data(2 * CHUNK + 1), PSW, p);
		byte[] b = CryptoEngine.encrypt(data(2 * CHUNK + 1), PSW, p);
		byte[][] fa = frames(a, p, 2 * CHUNK + 1);
		byte[][] fb = frames(b, p, 2 * CHUNK + 1);
		assertRejected(join(fa[0], fb[1], fa[2], fa[3]), "chunk spliced from another encryption");
		assertRejected(join(fa[0], fa[1], fa[2], fb[3]), "last chunk spliced from another encryption");
	}

	@Test
	public void everyHeaderByteIsAuthenticated() throws Exception {
		CryptoProfile p = twoLayers();
		byte[] enc = CryptoEngine.encrypt(data(CHUNK + 10), PSW, p);
		int h = headerLength(p);
		for (int pos = 0; pos < h; pos++) {
			if (pos >= COST_OFFSET && pos < COST_OFFSET + 4) {
				continue; // a larger cost is a legitimate (slow) value: covered by hostileKdfCostsAreRefusedBeforeWorking
			}
			for (int bit = 0; bit < 8; bit++) {
				byte[] t = enc.clone();
				t[pos] ^= (byte) (1 << bit);
				assertRejected(t, "header byte " + pos + " bit " + bit);
			}
		}
	}

	@Test
	public void everyBodyByteIsAuthenticated() throws Exception {
		CryptoProfile p = twoLayers();
		byte[] enc = CryptoEngine.encrypt(data(CHUNK + 10), PSW, p);
		Random r = new Random(5);
		for (int i = 0; i < 300; i++) {
			int pos = headerLength(p) + r.nextInt(enc.length - headerLength(p));
			byte[] t = enc.clone();
			t[pos] ^= (byte) (1 << r.nextInt(8));
			assertRejected(t, "body byte " + pos);
		}
	}

	@Test
	public void truncatedAtEveryLengthIsRejected() throws Exception {
		CryptoProfile p = fast(CipherAlgorithm.AES_128_GCM);
		byte[] enc = CryptoEngine.encrypt(data(CHUNK + 3), PSW, p);
		for (int len = 0; len < enc.length; len++) {
			assertRejected(Arrays.copyOf(enc, len), "truncated at " + len);
		}
	}

	// ------------------------------------------------------------------ hostile headers

	private static void setCost(byte[] enc, int cost) {
		enc[COST_OFFSET] = (byte) (cost >>> 24);
		enc[COST_OFFSET + 1] = (byte) (cost >>> 16);
		enc[COST_OFFSET + 2] = (byte) (cost >>> 8);
		enc[COST_OFFSET + 3] = (byte) cost;
	}

	@Test
	public void hostileKdfCostsAreRefusedBeforeWorking() throws Exception {
		byte[] pbkdf = CryptoEngine.encrypt(data(10), PSW, fast(CipherAlgorithm.AES_128_GCM));
		for (int cost : new int[] { 0, 1, 999, 20000001, Integer.MAX_VALUE, -1, Integer.MIN_VALUE }) {
			byte[] t = pbkdf.clone();
			setCost(t, cost);
			long start = System.nanoTime();
			assertRejected(t, "pbkdf2 cost " + cost);
			assertTrue("must fail fast, cost " + cost, (System.nanoTime() - start) / 1000000 < 2000);
		}
		byte[] scrypt = CryptoEngine.encrypt(data(10), PSW,
				new CryptoProfile(KdfAlgorithm.SCRYPT, 10, CHUNK_LOG2, Arrays.asList(CipherAlgorithm.AES_128_GCM)));
		for (int cost : new int[] { 0, 9, 23, 31, 32, 1000000, -1 }) {
			byte[] t = scrypt.clone();
			setCost(t, cost);
			long start = System.nanoTime();
			assertRejected(t, "scrypt cost " + cost);
			assertTrue("must fail fast, cost " + cost, (System.nanoTime() - start) / 1000000 < 2000);
		}
	}

	@Test
	public void scryptCostNeedingMoreMemoryThanAvailableIsAnErrorNotAnOutOfMemory() throws Exception {
		// 128 * r(8) * 2^cost bytes: cost 22 asks for 4 GiB, whatever the file says
		long needed = 1L << (22 + 10);
		Assume.assumeTrue("the heap of this JVM is big enough to try it", Runtime.getRuntime().maxMemory() / 2 < needed);
		byte[] scrypt = CryptoEngine.encrypt(data(10), PSW,
				new CryptoProfile(KdfAlgorithm.SCRYPT, 10, CHUNK_LOG2, Arrays.asList(CipherAlgorithm.AES_128_GCM)));
		setCost(scrypt, 22);
		long start = System.nanoTime();
		assertRejected(scrypt, "scrypt cost 22 on a small heap");
		assertTrue("must fail fast", (System.nanoTime() - start) / 1000000 < 5000);
		// and the engine keeps working afterwards
		assertArrayEquals(data(10), CryptoEngine.decrypt(CryptoEngine.encrypt(data(10), PSW, twoLayers()), PSW));
	}

	@Test
	public void invalidChunkSizeLayerCountAndIdsAreRefused() throws Exception {
		byte[] enc = CryptoEngine.encrypt(data(10), PSW, twoLayers());
		for (int chunkLog2 : new int[] { 0, 9, 27, 255 }) {
			byte[] t = enc.clone();
			t[10] = (byte) chunkLog2;
			assertRejected(t, "chunkLog2 " + chunkLog2);
		}
		for (int layers : new int[] { 0, CryptoProfile.MAX_LAYERS + 1, 255 }) {
			byte[] t = enc.clone();
			t[11] = (byte) layers;
			assertRejected(t, "layer count " + layers);
		}
		byte[] badLayer = enc.clone();
		badLayer[FIXED_HEADER] = (byte) 200;
		assertRejected(badLayer, "unknown layer id");
		byte[] badKdf = enc.clone();
		badKdf[5] = 99;
		assertRejected(badKdf, "unknown kdf id");
		byte[] badVersion = enc.clone();
		badVersion[4] = 2;
		assertRejected(badVersion, "unknown version");
	}

	@Test
	public void notEngineDataIsRecognisedWithoutAPassword() throws Exception {
		assertFalse(CryptoEngine.isEngineData(null));
		assertFalse(CryptoEngine.isEngineData(new byte[0]));
		assertFalse(CryptoEngine.isEngineData(new byte[] { 'S', 'C', 'P' }));
		assertFalse(CryptoEngine.isEngineData("SCP1xxxxxxxxxx".getBytes("US-ASCII")));
		assertTrue(CryptoEngine.isEngineData(new byte[] { 'S', 'C', 'P', '2' }));
		for (byte[] garbage : new byte[][] { null, new byte[0], new byte[] { 'S', 'C', 'P', '2' }, data(40) }) {
			try {
				CryptoEngine.readProfile(garbage);
				fail("garbage accepted as a header");
			} catch (CryptoException expected) {
				// ok
			}
		}
	}

	// ------------------------------------------------------------------ passwords

	@Test
	public void anyKindOfPasswordRoundTripsAndIsExact() throws Exception {
		CryptoProfile p = fast(CipherAlgorithm.AES_256_GCM);
		byte[] plain = data(CHUNK + 1);
		String[] passwords = { "a", " ", "àèìòù", "日本語パスワード",
				"emoji 😀🔒", "tab\tand\nnewline", "with\0nul", "\u0000", repeat("long", 5000) };
		for (String s : passwords) {
			byte[] enc = CryptoEngine.encrypt(plain, s.toCharArray(), p);
			assertArrayEquals("round trip of password of length " + s.length(), plain, CryptoEngine.decrypt(enc, s.toCharArray()));
		}
		// every character counts: case, spaces, one char more or less, and the tail of a very long password
		byte[] enc = CryptoEngine.encrypt(plain, "Secret pass".toCharArray(), p);
		for (String wrong : new String[] { "secret pass", "Secret pass ", " Secret pass", "Secret pas", "Secret  pass", "Secret passs" }) {
			try {
				CryptoEngine.decrypt(enc, wrong.toCharArray());
				fail("accepted password '" + wrong + "'");
			} catch (CryptoException expected) {
				// ok
			}
		}
		String longPsw = repeat("x", 100000);
		byte[] encLong = CryptoEngine.encrypt(plain, longPsw.toCharArray(), p);
		try {
			CryptoEngine.decrypt(encLong, (longPsw.substring(0, 99999) + "y").toCharArray());
			fail("last character of a long password ignored");
		} catch (CryptoException expected) {
			// ok
		}
	}

	private static String repeat(String s, int n) {
		StringBuilder b = new StringBuilder(s.length() * n);
		for (int i = 0; i < n; i++) {
			b.append(s);
		}
		return b.toString();
	}

	@Test
	public void emptyOrNullPasswordIsRefusedForBothDirections() throws Exception {
		byte[] enc = CryptoEngine.encrypt(data(5), PSW, twoLayers());
		for (char[] bad : new char[][] { new char[0], null }) {
			try {
				CryptoEngine.encrypt(data(5), bad, twoLayers());
				fail("encrypt accepted an empty password");
			} catch (CryptoException expected) {
				// ok
			}
			try {
				CryptoEngine.decrypt(enc, bad);
				fail("decrypt accepted an empty password");
			} catch (CryptoException expected) {
				// ok
			}
		}
	}

	@Test
	public void theEngineDoesNotChangeOrWipeTheCallersPassword() throws Exception {
		char[] psw = "keep me intact".toCharArray();
		char[] copy = psw.clone();
		byte[] enc = CryptoEngine.encrypt(data(2000), psw, twoLayers());
		assertArrayEquals(copy, psw);
		CryptoEngine.decrypt(enc, psw);
		assertArrayEquals(copy, psw);
		// CryptoKey.clear() is the explicit way to wipe
		CryptoKey key = CryptoKey.of(psw);
		key.clear();
		assertArrayEquals(new char[copy.length], psw);
	}

	// ------------------------------------------------------------------ randomness

	@Test
	public void everyEncryptionUsesFreshSaltAndNonce() throws Exception {
		CryptoProfile p = fast(CipherAlgorithm.AES_128_GCM);
		int n = 60;
		int tail = SALT_PLUS_PREFIX;
		Set<String> saltsAndPrefixes = new HashSet<String>();
		Set<String> bodies = new HashSet<String>();
		byte[] plain = new byte[CHUNK * 2]; // all zeros: the worst case for patterns
		for (int i = 0; i < n; i++) {
			byte[] enc = CryptoEngine.encrypt(plain, PSW, p);
			int h = headerLength(p);
			saltsAndPrefixes.add(Arrays.toString(Arrays.copyOfRange(enc, h - tail, h)));
			bodies.add(Arrays.toString(Arrays.copyOfRange(enc, h, h + 32)));
		}
		assertEquals(n, saltsAndPrefixes.size());
		assertEquals(n, bodies.size());
	}

	@Test
	public void identicalPlainChunksGiveDifferentCiphertextChunks() throws Exception {
		CryptoProfile p = fast(CipherAlgorithm.AES_256_GCM);
		int chunks = 6;
		byte[] plain = new byte[CHUNK * chunks]; // zeros: identical plain chunks
		byte[] enc = CryptoEngine.encrypt(plain, PSW, p);
		byte[][] f = frames(enc, p, plain.length);
		Set<String> seen = new HashSet<String>();
		for (int i = 1; i < f.length; i++) {
			assertTrue("repeated ciphertext chunk " + i, seen.add(Arrays.toString(Arrays.copyOf(f[i], 24))));
		}
	}

	// ------------------------------------------------------------------ streams

	/** Returns at most one byte per read, as the worst network or pipe would */
	private static final class TrickleInputStream extends InputStream {
		private final byte[] data;
		private int pos;

		TrickleInputStream(byte[] data) {
			this.data = data;
		}

		@Override
		public int read() {
			return pos < data.length ? data[pos++] & 0xff : -1;
		}

		@Override
		public int read(byte[] b, int off, int len) {
			if (len == 0) {
				return 0;
			}
			int c = read();
			if (c < 0) {
				return -1;
			}
			b[off] = (byte) c;
			return 1;
		}
	}

	@Test
	public void shortReadsDoNotChangeTheResult() throws Exception {
		byte[] plain = data(3 * CHUNK + 33);
		ByteArrayOutputStream enc = new ByteArrayOutputStream();
		CryptoEngine.encrypt(new TrickleInputStream(plain), enc, PSW, twoLayers());
		assertEquals(headerLength(twoLayers()) + 3 * (CHUNK + 32) + 33 + 32, enc.size());
		ByteArrayOutputStream back = new ByteArrayOutputStream();
		CryptoEngine.decrypt(new TrickleInputStream(enc.toByteArray()), back, PSW);
		assertArrayEquals(plain, back.toByteArray());
	}

	private static final class FailingOutputStream extends OutputStream {
		private int writesLeft;

		FailingOutputStream(int writesBeforeFailure) {
			this.writesLeft = writesBeforeFailure;
		}

		@Override
		public void write(int b) throws IOException {
			write(new byte[] { (byte) b }, 0, 1);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			if (writesLeft-- <= 0) {
				throw new IOException("disk full (simulated)");
			}
		}
	}

	private static final class FailingInputStream extends InputStream {
		private final byte[] data;
		private final int failAt;
		private int pos;

		FailingInputStream(byte[] data, int failAt) {
			this.data = data;
			this.failAt = failAt;
		}

		@Override
		public int read() throws IOException {
			if (pos >= failAt) {
				throw new IOException("read error (simulated)");
			}
			return pos < data.length ? data[pos++] & 0xff : -1;
		}
	}

	@Test
	public void ioErrorsOfTheStreamsAreReportedNotSwallowed() throws Exception {
		byte[] plain = data(3 * CHUNK);
		for (int ok = 0; ok < 4; ok++) {
			try {
				CryptoEngine.encrypt(new ByteArrayInputStream(plain), new FailingOutputStream(ok), PSW, twoLayers());
				fail("write error swallowed by encrypt, writes ok " + ok);
			} catch (IOException expected) {
				assertTrue(expected.getMessage().contains("simulated"));
			}
		}
		byte[] enc = CryptoEngine.encrypt(plain, PSW, twoLayers());
		for (int ok = 0; ok < 3; ok++) {
			try {
				CryptoEngine.decrypt(new ByteArrayInputStream(enc), new FailingOutputStream(ok), PSW);
				fail("write error swallowed by decrypt, writes ok " + ok);
			} catch (IOException expected) {
				assertTrue(expected.getMessage().contains("simulated"));
			}
		}
		try {
			CryptoEngine.encrypt(new FailingInputStream(plain, CHUNK + 5), new ByteArrayOutputStream(), PSW, twoLayers());
			fail("read error swallowed by encrypt");
		} catch (IOException expected) {
			assertTrue(expected.getMessage().contains("simulated"));
		}
		try {
			CryptoEngine.decrypt(new FailingInputStream(enc, 2 * CHUNK), new ByteArrayOutputStream(), PSW);
			fail("read error swallowed by decrypt");
		} catch (IOException expected) {
			assertTrue(expected.getMessage().contains("simulated"));
		}
	}

	@Test
	public void aFailedDecryptNeverReleasesTheTamperedChunk() throws Exception {
		CryptoProfile p = fast(CipherAlgorithm.AES_256_GCM);
		byte[] plain = data(3 * CHUNK + 9);
		byte[] enc = CryptoEngine.encrypt(plain, PSW, p);
		int tampered = headerLength(p) + 2 * (CHUNK + 16) + 7; // inside the third chunk
		enc[tampered] ^= 0x40;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			CryptoEngine.decrypt(new ByteArrayInputStream(enc), out, PSW);
			fail("tampering not detected");
		} catch (CryptoException expected) {
			// chunks before the damage were already authenticated and delivered; nothing of the damaged one
			assertTrue(out.size() <= 2 * CHUNK);
			assertArrayEquals(Arrays.copyOf(plain, out.size()), out.toByteArray());
		}
	}

	@Test
	public void keysAreIndependentOfTheProfileInstance() throws Exception {
		// two separately built equal profiles decrypt each other's output (only the header matters)
		byte[] plain = data(CHUNK * 2 + 1);
		byte[] enc = CryptoEngine.encrypt(plain, PSW, twoLayers());
		assertArrayEquals(plain, CryptoEngine.decrypt(enc, PSW));
		byte[] enc2 = CryptoEngine.encrypt(plain, PSW, twoLayers());
		assertNotEquals(Arrays.toString(enc), Arrays.toString(enc2));
		assertArrayEquals(plain, CryptoEngine.decrypt(enc2, PSW));
	}
}
