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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * Exhaustive round trip and tampering tests of the engine. Every decryption must give back exactly the original bytes,
 * and every modification (bit flip, truncation, extension, reordering, splicing) must end with a CryptoException:
 * never wrong data, never another exception, never a hang. Small chunks (1 KiB) so that many chunks are crossed fast.
 */
public class CrypterTortureTest {

	private static final int CHUNK_LOG2 = 10;
	private static final int CHUNK = 1 << CHUNK_LOG2;
	private static final char[] PSW = "Torture-Test pässwörd 🔑".toCharArray();

	private static final CipherLayer A128 = CipherAlgorithm.AES_128_GCM, A256 = CipherAlgorithm.AES_256_GCM,
			CHA = CipherAlgorithm.CHACHA20_POLY1305, SHU = CipherAlgorithm.SHUFFLE;

	private static final CipherLayer[][] LAYER_SETS = { { A128 }, { A256 }, { CHA }, { A256, CHA }, { CHA, A256 }, { A128, A256 },
			{ A256, CHA, SHU }, { CHA, SHU }, { A128, A256, CHA, SHU }, { A256, A256, CHA, CHA } };

	private static final int[] SIZES = { 0, 1, 2, 15, 16, 17, 63, 64, 65, CHUNK - 1, CHUNK, CHUNK + 1, 2 * CHUNK - 1, 2 * CHUNK,
			2 * CHUNK + 1, 3 * CHUNK + 333, 5 * CHUNK };

	static CryptoProfile profile(KdfAlgorithm kdf, CipherLayer... layers) {
		return new CryptoProfile(kdf, cheapCost(kdf), CHUNK_LOG2, Arrays.asList(layers));
	}

	static int cheapCost(KdfAlgorithm kdf) {
		switch (kdf) {
		case SCRYPT:
			return 10;
		case ARGON2ID:
			return Argon2.cost(13, 1, 2);
		default:
			return 1000;
		}
	}

	static byte[] data(int n, long seed) {
		byte[] b = new byte[n];
		new Random(seed).nextBytes(b);
		return b;
	}

	private static void assertRejected(byte[] enc, char[] psw, String what) {
		try {
			CryptoEngine.decrypt(enc, psw.clone());
			fail("accepted: " + what);
		} catch (CryptoException expected) {
			// the only acceptable outcome
		} catch (Throwable t) {
			throw new AssertionError("not a CryptoException for " + what + ": " + t, t);
		}
	}

	// ---------------------------------------------------------------- round trips

	@Test(timeout = 120000)
	public void everyLayerSetEverySizeRoundTrips() throws Exception {
		for (CipherLayer[] set : LAYER_SETS) {
			CryptoProfile p = profile(KdfAlgorithm.PBKDF2_SHA256, set);
			for (int size : SIZES) {
				byte[] plain = data(size, size * 31L + set.length);
				byte[] enc = CryptoEngine.encrypt(plain, PSW.clone(), p);
				int chunks = size / CHUNK + 1;
				assertEquals("size of " + p + " / " + size, headerLength(p) + size + (long) chunks * p.overheadPerChunk(), enc.length);
				assertArrayEquals(p + " / " + size, plain, CryptoEngine.decrypt(enc, PSW.clone()));
			}
		}
	}

	@Test(timeout = 120000)
	public void everyKdfRoundTripsAndIsRecognisedFromTheHeader() throws Exception {
		for (KdfAlgorithm kdf : KdfAlgorithm.values()) {
			for (CipherLayer[] set : new CipherLayer[][] { { A256 }, { A256, CHA }, { A128, A256, CHA, SHU } }) {
				CryptoProfile p = profile(kdf, set);
				for (int size : new int[] { 0, 1, CHUNK, 2 * CHUNK + 7 }) {
					byte[] plain = data(size, size);
					byte[] enc = CryptoEngine.encrypt(plain, PSW.clone(), p);
					CryptoProfile read = CryptoEngine.readProfile(Arrays.copyOf(enc, 64));
					assertEquals(kdf, read.kdf);
					assertEquals(p.kdfCost, read.kdfCost);
					assertEquals(p.chunkLog2, read.chunkLog2);
					assertEquals(p.getLayers(), read.getLayers());
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					CryptoProfile used = CryptoEngine.decrypt(new ByteArrayInputStream(enc), out, PSW.clone());
					assertEquals(p.describe(), used.describe());
					assertArrayEquals(plain, out.toByteArray());
				}
			}
		}
	}

	@Test(timeout = 60000)
	public void encryptingTwiceNeverGivesTheSameBytes() throws Exception {
		CryptoProfile p = profile(KdfAlgorithm.PBKDF2_SHA512, A256, CHA);
		byte[] plain = data(3000, 1);
		byte[] a = CryptoEngine.encrypt(plain, PSW.clone(), p);
		byte[] b = CryptoEngine.encrypt(plain, PSW.clone(), p);
		assertFalse(Arrays.equals(a, b));
		// fresh salt and nonce prefix every time: not a single equal 16 byte block after the fixed header fields
		int fixed = 12 + p.getLayers().size();
		assertFalse(Arrays.equals(Arrays.copyOfRange(a, fixed, fixed + 24), Arrays.copyOfRange(b, fixed, fixed + 24)));
	}

	/** Streams that return 1..7 bytes per read (pipes, sockets, zip entries) must give exactly the same result. */
	@Test(timeout = 60000)
	public void tricklingStreamsGiveTheSameResult() throws Exception {
		CryptoProfile p = profile(KdfAlgorithm.PBKDF2_SHA256, A256, CHA, SHU);
		for (int size : new int[] { 0, 5, CHUNK, 3 * CHUNK + 1 }) {
			byte[] plain = data(size, 99 + size);
			ByteArrayOutputStream enc = new ByteArrayOutputStream();
			CryptoEngine.encrypt(new Trickle(new ByteArrayInputStream(plain), size), enc, PSW.clone(), p);
			ByteArrayOutputStream dec = new ByteArrayOutputStream();
			CryptoEngine.decrypt(new Trickle(new ByteArrayInputStream(enc.toByteArray()), size + 1), dec, PSW.clone());
			assertArrayEquals(plain, dec.toByteArray());
		}
	}

	@Test(timeout = 60000)
	public void passwordsOfEveryShapeWork() throws Exception {
		CryptoProfile p = profile(KdfAlgorithm.PBKDF2_SHA512, A256);
		StringBuilder longPsw = new StringBuilder();
		for (int i = 0; i < 10000; i++) {
			longPsw.append((char) ('a' + i % 26));
		}
		for (String psw : new String[] { "x", " ", "\t\n", "ñ", "日本語のパスワード", "😀😀", "a\u0000b", longPsw.toString() }) {
			byte[] plain = data(100, psw.length());
			byte[] enc = CryptoEngine.encrypt(plain, psw.toCharArray(), p);
			assertArrayEquals(plain, CryptoEngine.decrypt(enc, psw.toCharArray()));
		}
	}

	@Test(timeout = 60000)
	public void nearlyRightPasswordsAreRejected() throws Exception {
		for (KdfAlgorithm kdf : KdfAlgorithm.values()) {
			byte[] enc = CryptoEngine.encrypt(data(500, 3), PSW.clone(), profile(kdf, A256, CHA));
			String psw = new String(PSW);
			for (String wrong : new String[] { psw.substring(0, psw.length() - 1), psw + " ", " " + psw, psw.toUpperCase(),
					psw.replace('T', 't'), psw.substring(0, 16), "Torture-Test" }) {
				assertRejected(enc, wrong.toCharArray(), kdf + " with '" + wrong + "'");
			}
		}
	}

	@Test
	public void emptyOrMissingPasswordIsRefused() throws Exception {
		CryptoProfile p = profile(KdfAlgorithm.PBKDF2_SHA256, A256);
		for (char[] psw : new char[][] { null, new char[0] }) {
			try {
				CryptoEngine.encrypt(data(10, 1), psw, p);
				fail("empty password accepted");
			} catch (CryptoException expected) {
				// ok
			}
		}
		assertRejected(CryptoEngine.encrypt(data(10, 1), PSW.clone(), p), new char[0], "empty password");
	}

	// ---------------------------------------------------------------- tampering

	@Test(timeout = 180000)
	public void everySingleByteModificationIsRejected() throws Exception {
		for (CipherLayer[] set : new CipherLayer[][] { { A256, CHA }, { CHA, SHU } }) {
			// one full chunk and the last one: every position of the header, of both frames and of their tags
			byte[] enc = CryptoEngine.encrypt(data(CHUNK + CHUNK / 2, 11), PSW.clone(), profile(KdfAlgorithm.PBKDF2_SHA256, set));
			Random rnd = new Random(set.length);
			for (int i = 0; i < enc.length; i++) {
				byte[] t = enc.clone();
				t[i] ^= 0x01;
				assertRejected(t, PSW, "bit 0 flipped at " + i);
				// another random bit on a sample (not on the cost field: millions of iterations, covered by the bounds test)
				if (i > 12 && rnd.nextInt(8) == 0) {
					t = enc.clone();
					t[i] ^= 1 << (1 + rnd.nextInt(7));
					assertRejected(t, PSW, "random bit flipped at " + i);
				}
			}
		}
	}

	@Test(timeout = 120000)
	public void everyTruncationIsRejected() throws Exception {
		for (int size : new int[] { 0, CHUNK, CHUNK + 100 }) {
			byte[] enc = CryptoEngine.encrypt(data(size, 12), PSW.clone(), profile(KdfAlgorithm.PBKDF2_SHA256, A256, CHA));
			for (int len = 0; len < enc.length; len++) {
				assertRejected(Arrays.copyOf(enc, len), PSW, "size " + size + " cut at " + len);
			}
		}
	}

	@Test(timeout = 60000)
	public void appendedBytesAreRejected() throws Exception {
		for (int size : new int[] { 0, 10, CHUNK, 2 * CHUNK }) {
			byte[] enc = CryptoEngine.encrypt(data(size, 13), PSW.clone(), profile(KdfAlgorithm.PBKDF2_SHA256, A256));
			for (int extra : new int[] { 1, 15, 16, 17, CHUNK, CHUNK + 16 }) {
				byte[] t = Arrays.copyOf(enc, enc.length + extra);
				assertRejected(t, PSW, "size " + size + " + " + extra + " zero bytes");
				new Random(extra).nextBytes(t = Arrays.copyOf(enc, enc.length + extra));
				System.arraycopy(enc, 0, t, 0, enc.length);
				assertRejected(t, PSW, "size " + size + " + " + extra + " random bytes");
			}
		}
	}

	@Test(timeout = 60000)
	public void chunksCannotBeReorderedDuplicatedOrDropped() throws Exception {
		CryptoProfile p = profile(KdfAlgorithm.PBKDF2_SHA256, A256, CHA);
		byte[] enc = CryptoEngine.encrypt(data(4 * CHUNK + 50, 14), PSW.clone(), p);
		int h = headerLength(p), f = CHUNK + p.overheadPerChunk();
		List<byte[]> frames = frames(enc, h, f);
		assertEquals(5, frames.size());
		byte[] header = Arrays.copyOf(enc, h);
		assertRejected(join(header, frames.get(1), frames.get(0), frames.get(2), frames.get(3), frames.get(4)), PSW, "swap 0/1");
		assertRejected(join(header, frames.get(0), frames.get(1), frames.get(1), frames.get(3), frames.get(4)), PSW, "duplicate");
		assertRejected(join(header, frames.get(0), frames.get(2), frames.get(3), frames.get(4)), PSW, "drop middle");
		assertRejected(join(header, frames.get(0), frames.get(1), frames.get(2), frames.get(3)), PSW, "drop last");
		assertRejected(join(header, frames.get(0), frames.get(1), frames.get(2), frames.get(4)), PSW, "drop before last");
		assertRejected(join(header, frames.get(4)), PSW, "only last");
		assertRejected(join(header), PSW, "no chunk");
	}

	@Test(timeout = 60000)
	public void piecesOfAnotherFileWithTheSamePasswordAreRejected() throws Exception {
		CryptoProfile p = profile(KdfAlgorithm.PBKDF2_SHA256, A256, CHA);
		byte[] a = CryptoEngine.encrypt(data(2 * CHUNK + 5, 15), PSW.clone(), p);
		byte[] b = CryptoEngine.encrypt(data(2 * CHUNK + 5, 16), PSW.clone(), p);
		int h = headerLength(p), f = CHUNK + p.overheadPerChunk();
		List<byte[]> fa = frames(a, h, f), fb = frames(b, h, f);
		assertRejected(join(Arrays.copyOf(a, h), fb.get(0), fb.get(1), fb.get(2)), PSW, "header of a, body of b");
		assertRejected(join(Arrays.copyOf(a, h), fa.get(0), fb.get(1), fa.get(2)), PSW, "one chunk of b in a");
		// same layers, different chunk size in the header: the frames no longer match
		byte[] t = a.clone();
		t[10] = (byte) (CHUNK_LOG2 + 1);
		assertRejected(t, PSW, "chunk size changed");
	}

	@Test(timeout = 60000)
	public void layersChangedInTheHeaderAreRejected() throws Exception {
		CryptoProfile p = profile(KdfAlgorithm.PBKDF2_SHA256, A256, CHA);
		byte[] enc = CryptoEngine.encrypt(data(300, 17), PSW.clone(), p);
		for (int id = 0; id < 256; id++) {
			for (int pos : new int[] { 12, 13 }) {
				if (id == (enc[pos] & 0xff)) {
					continue;
				}
				byte[] t = enc.clone();
				t[pos] = (byte) id;
				assertRejected(t, PSW, "layer id " + id + " at " + pos);
			}
		}
		byte[] swapped = enc.clone();
		swapped[12] = enc[13];
		swapped[13] = enc[12];
		assertRejected(swapped, PSW, "layer order swapped");
	}

	/** Random garbage behind a valid looking header: always a clean CryptoException, quickly. */
	@Test(timeout = 120000)
	public void fuzzedInputsNeverCrashOrHang() throws Exception {
		CryptoProfile p = profile(KdfAlgorithm.PBKDF2_SHA256, A256, CHA, SHU);
		byte[] enc = CryptoEngine.encrypt(data(CHUNK + 10, 18), PSW.clone(), p);
		Random rnd = new Random(2026);
		for (int i = 0; i < 1500; i++) {
			byte[] t = Arrays.copyOf(enc, rnd.nextInt(enc.length + 100));
			int keep = rnd.nextInt(12); // keep the first bytes, so that most inputs pass the cheap checks
			for (int k = keep; k < t.length; k++) {
				if (k < 6 || k > 9) { // never the cost field (no million-iteration headers)
					if (rnd.nextInt(3) == 0) {
						t[k] = (byte) rnd.nextInt(256);
					}
				}
			}
			if (Arrays.equals(t, enc)) {
				continue;
			}
			assertRejected(t, PSW, "fuzz " + i);
		}
		for (int i = 0; i < 300; i++) {
			byte[] t = data(rnd.nextInt(200), i);
			assertRejected(t, PSW, "random bytes " + i);
			if (t.length >= 4) {
				System.arraycopy(CryptoEngine.MAGIC, 0, t, 0, 4);
				assertRejected(t, PSW, "magic + random bytes " + i);
			}
		}
	}

	// ---------------------------------------------------------------- failures of the streams

	@Test(timeout = 30000)
	public void brokenStreamsFailCleanly() throws Exception {
		CryptoProfile p = profile(KdfAlgorithm.PBKDF2_SHA256, A256, CHA);
		final byte[] plain = data(3 * CHUNK, 19);
		try {
			CryptoEngine.encrypt(new Failing(new ByteArrayInputStream(plain), 2 * CHUNK), new ByteArrayOutputStream(), PSW.clone(), p);
			fail("read error ignored");
		} catch (IOException expected) {
			// ok
		}
		byte[] enc = CryptoEngine.encrypt(plain, PSW.clone(), p);
		try {
			CryptoEngine.decrypt(new Failing(new ByteArrayInputStream(enc), CHUNK + 100), new ByteArrayOutputStream(), PSW.clone());
			fail("read error ignored");
		} catch (IOException expected) {
			// ok
		}
		OutputStream full = new OutputStream() {
			int n;

			@Override
			public void write(int b) throws IOException {
				write(new byte[] { (byte) b }, 0, 1);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				if ((n += len) > CHUNK) {
					throw new IOException("disk full");
				}
			}
		};
		try {
			CryptoEngine.decrypt(new ByteArrayInputStream(enc), full, PSW.clone());
			fail("write error ignored");
		} catch (IOException expected) {
			// ok
		}
	}

	// ---------------------------------------------------------------- concurrency

	@Test(timeout = 120000)
	public void manyThreadsAtOnceNeverMixAnything() throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(8);
		try {
			List<Future<Void>> results = new ArrayList<Future<Void>>();
			for (int t = 0; t < 32; t++) {
				final int id = t;
				results.add(pool.submit(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						KdfAlgorithm kdf = KdfAlgorithm.values()[id % KdfAlgorithm.values().length];
						CipherLayer[] set = LAYER_SETS[id % LAYER_SETS.length];
						char[] psw = ("thread-" + id).toCharArray();
						for (int i = 0; i < 4; i++) {
							byte[] plain = data(id * 97 + i * CHUNK, id * 10 + i);
							byte[] enc = CryptoEngine.encrypt(plain, psw.clone(), profile(kdf, set));
							assertArrayEquals(plain, CryptoEngine.decrypt(enc, psw.clone()));
						}
						return null;
					}
				}));
			}
			for (Future<Void> f : results) {
				f.get(100, TimeUnit.SECONDS);
			}
		} finally {
			pool.shutdownNow();
		}
	}

	// ---------------------------------------------------------------- real costs

	/** The real presets, with their real costs: must round trip and stay usable (no endless waits). */
	@Test(timeout = 120000)
	public void realPresetsRoundTripInReasonableTime() throws Exception {
		for (CryptoProfile p : Arrays.asList(CryptoProfile.standard(), CryptoProfile.aes128(), CryptoProfile.maximum(), CryptoProfile.ultra())) {
			byte[] plain = data((1 << 20) * 2 + 12345, 20);
			long t0 = System.nanoTime();
			byte[] enc = CryptoEngine.encrypt(plain, PSW.clone(), p);
			long t1 = System.nanoTime();
			assertArrayEquals(plain, CryptoEngine.decrypt(enc, PSW.clone()));
			long t2 = System.nanoTime();
			System.out.println(String.format("%-55s encrypt %5d ms, decrypt %5d ms (2.0 MB)", p.describe(), (t1 - t0) / 1000000, (t2 - t1) / 1000000));
			assertTrue(p.describe() + " too slow", (t2 - t0) / 1000000 < 40000);
		}
	}

	@Test(timeout = 120000)
	public void bigStreamRoundTripsChunkByChunk() throws Exception {
		CryptoProfile p = new CryptoProfile(KdfAlgorithm.PBKDF2_SHA256, 1000, Arrays.asList(A256, CHA));
		final long size = 24L << 20; // 24 MiB, default 1 MiB chunks
		java.security.MessageDigest in = java.security.MessageDigest.getInstance("SHA-256");
		java.io.File f = java.io.File.createTempFile("torture", ".scpt");
		try {
			long t0 = System.nanoTime();
			try (OutputStream os = new java.io.BufferedOutputStream(new java.io.FileOutputStream(f))) {
				CryptoEngine.encrypt(new java.security.DigestInputStream(new Generated(size), in), os, PSW.clone(), p);
			}
			java.security.MessageDigest out = java.security.MessageDigest.getInstance("SHA-256");
			try (InputStream is = new java.io.BufferedInputStream(new java.io.FileInputStream(f));
					OutputStream os = new java.security.DigestOutputStream(new ByteArrayOutputStream() {
						@Override
						public void write(byte[] b, int off, int len) {
							// discard: only the digest matters
						}
					}, out)) {
				CryptoEngine.decrypt(is, os, PSW.clone());
			}
			System.out.println(String.format("AES-256 + ChaCha20 on 24 MiB: %d ms encrypt+decrypt", (System.nanoTime() - t0) / 1000000));
			assertArrayEquals(in.digest(), out.digest());
			assertEquals(headerLength(p) + size + (size / (1 << 20) + 1) * p.overheadPerChunk(), f.length());
		} finally {
			assertTrue(f.delete());
		}
	}

	@Test
	public void profilesAreValidated() {
		for (List<CipherLayer> bad : Arrays.asList(Arrays.<CipherLayer>asList(), Arrays.asList(SHU),
				Arrays.asList(A256, A256, A256, A256, A256, A256, A256))) {
			try {
				new CryptoProfile(KdfAlgorithm.PBKDF2_SHA256, 1000, bad);
				fail("invalid profile accepted: " + bad);
			} catch (IllegalArgumentException expected) {
				// ok
			}
		}
		assertNotNull(new CryptoProfile(KdfAlgorithm.PBKDF2_SHA256, 1000, Arrays.asList(CHA)));
		assertTrue(CryptoProfile.ultra().describe().contains("ChaCha20"));
		assertEquals(KdfAlgorithm.ARGON2ID, CryptoProfile.ultra().kdf);
	}

	// ---------------------------------------------------------------- helpers

	static int headerLength(CryptoProfile p) {
		return 4 + 1 + 1 + 4 + 1 + 1 + p.getLayers().size() + 16 + 8;
	}

	private static List<byte[]> frames(byte[] enc, int h, int f) {
		List<byte[]> l = new ArrayList<byte[]>();
		for (int pos = h; pos < enc.length; pos += f) {
			l.add(Arrays.copyOfRange(enc, pos, Math.min(enc.length, pos + f)));
		}
		return l;
	}

	private static byte[] join(byte[]... parts) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		for (byte[] p : parts) {
			b.write(p, 0, p.length);
		}
		return b.toByteArray();
	}

	/** Returns at most a few bytes per read. */
	static final class Trickle extends FilterInputStream {
		private final Random rnd;

		Trickle(InputStream in, long seed) {
			super(in);
			rnd = new Random(seed);
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return super.read(b, off, Math.min(len, 1 + rnd.nextInt(7)));
		}
	}

	/** Fails after {@code limit} bytes. */
	static final class Failing extends FilterInputStream {
		private long left;

		Failing(InputStream in, long limit) {
			super(in);
			left = limit;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (left <= 0) {
				throw new IOException("device error");
			}
			int n = super.read(b, off, (int) Math.min(len, left));
			left -= Math.max(n, 0);
			return n;
		}
	}

	/** Deterministic pseudo random content of any size, without holding it in memory. */
	static final class Generated extends InputStream {
		private final Random rnd = new Random(42);
		private long left;

		Generated(long size) {
			left = size;
		}

		@Override
		public int read() {
			byte[] one = new byte[1];
			return read(one, 0, 1) < 0 ? -1 : one[0] & 0xff;
		}

		@Override
		public int read(byte[] b, int off, int len) {
			if (left <= 0) {
				return -1;
			}
			int n = (int) Math.min(len, left);
			byte[] tmp = new byte[n];
			rnd.nextBytes(tmp);
			System.arraycopy(tmp, 0, b, off, n);
			left -= n;
			return n;
		}
	}

	static byte[] utf8(String s) {
		return s.getBytes(StandardCharsets.UTF_8);
	}
}
