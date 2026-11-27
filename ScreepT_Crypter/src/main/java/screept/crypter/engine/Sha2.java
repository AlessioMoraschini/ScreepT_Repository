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

import java.math.BigInteger;
import java.util.Arrays;

/**
 * SHA-256 / SHA-512 (FIPS 180-4) whose whole state can be copied and wiped: the JDK digests keep copies of the
 * data (and so of HMAC keys derived from passwords) in buffers nobody can clear. The round constants are computed
 * from their definition (fractional parts of square and cube roots of the first primes) instead of being typed in.
 */
final class Sha2 {

	private static final int[] K256 = new int[64];
	private static final long[] K512 = new long[80];
	private static final int[] H256 = new int[8];
	private static final long[] H512 = new long[8];

	static {
		int found = 0;
		for (int p = 2; found < 80; p++) {
			if (BigInteger.valueOf(p).isProbablePrime(50)) {
				long k = root(BigInteger.valueOf(p).shiftLeft(192), 3).longValue(); // low 64 bits = fractional part
				K512[found] = k;
				if (found < 64) {
					K256[found] = (int) (k >>> 32);
				}
				if (found < 8) {
					long h = root(BigInteger.valueOf(p).shiftLeft(128), 2).longValue();
					H512[found] = h;
					H256[found] = (int) (h >>> 32);
				}
				found++;
			}
		}
	}

	/** floor(n^(1/k)) by Newton's method, starting above the root. */
	private static BigInteger root(BigInteger n, int k) {
		BigInteger bk = BigInteger.valueOf(k), bk1 = BigInteger.valueOf(k - 1);
		BigInteger x = BigInteger.ONE.shiftLeft(n.bitLength() / k + 1);
		while (true) {
			BigInteger y = bk1.multiply(x).add(n.divide(x.pow(k - 1))).divide(bk);
			if (y.compareTo(x) >= 0) {
				return x;
			}
			x = y;
		}
	}

	final boolean big;
	final int block;
	final int out;
	private final int[] s32 = new int[8];
	private final int[] w32 = new int[64];
	private final long[] s64 = new long[8];
	private final long[] w64 = new long[80];
	private final byte[] buf;
	private int bufLen;
	private long count;

	Sha2(boolean sha512) {
		big = sha512;
		block = big ? 128 : 64;
		out = big ? 64 : 32;
		buf = new byte[block];
		reset();
	}

	void reset() {
		System.arraycopy(H256, 0, s32, 0, 8);
		System.arraycopy(H512, 0, s64, 0, 8);
		bufLen = 0;
		count = 0;
	}

	void update(byte[] in, int off, int len) {
		count += len;
		if (bufLen > 0) {
			int n = Math.min(block - bufLen, len);
			System.arraycopy(in, off, buf, bufLen, n);
			bufLen += n;
			off += n;
			len -= n;
			if (bufLen == block) {
				compress(buf, 0);
				bufLen = 0;
			}
		}
		while (len >= block) {
			compress(in, off);
			off += block;
			len -= block;
		}
		if (len > 0) {
			System.arraycopy(in, off, buf, 0, len);
			bufLen = len;
		}
	}

	/** Writes the {@link #out} byte digest into {@code dst} and resets. */
	void digest(byte[] dst) {
		long bits = count << 3;
		buf[bufLen++] = (byte) 0x80;
		if (bufLen > block - (big ? 16 : 8)) {
			Arrays.fill(buf, bufLen, block, (byte) 0);
			compress(buf, 0);
			bufLen = 0;
		}
		Arrays.fill(buf, bufLen, block - 8, (byte) 0);
		for (int i = 0; i < 8; i++) {
			buf[block - 1 - i] = (byte) (bits >>> (8 * i));
		}
		compress(buf, 0);
		for (int i = 0; i < out; i++) {
			dst[i] = big ? (byte) (s64[i >>> 3] >>> (56 - 8 * (i & 7))) : (byte) (s32[i >>> 2] >>> (24 - 8 * (i & 3)));
		}
		Arrays.fill(buf, (byte) 0);
		reset();
	}

	/** Becomes an exact copy of {@code o} (no allocation). */
	void copyFrom(Sha2 o) {
		System.arraycopy(o.s32, 0, s32, 0, 8);
		System.arraycopy(o.s64, 0, s64, 0, 8);
		System.arraycopy(o.buf, 0, buf, 0, block);
		bufLen = o.bufLen;
		count = o.count;
	}

	void wipe() {
		Arrays.fill(s32, 0);
		Arrays.fill(w32, 0);
		Arrays.fill(s64, 0);
		Arrays.fill(w64, 0);
		Arrays.fill(buf, (byte) 0);
		bufLen = 0;
		count = 0;
	}

	private void compress(byte[] b, int o) {
		if (big) {
			compress512(b, o);
		} else {
			compress256(b, o);
		}
	}

	private void compress256(byte[] b, int o) {
		int[] w = w32;
		for (int t = 0; t < 16; t++, o += 4) {
			w[t] = (b[o] & 0xff) << 24 | (b[o + 1] & 0xff) << 16 | (b[o + 2] & 0xff) << 8 | (b[o + 3] & 0xff);
		}
		for (int t = 16; t < 64; t++) {
			int x = w[t - 15], y = w[t - 2];
			int s0 = Integer.rotateRight(x, 7) ^ Integer.rotateRight(x, 18) ^ (x >>> 3);
			int s1 = Integer.rotateRight(y, 17) ^ Integer.rotateRight(y, 19) ^ (y >>> 10);
			w[t] = w[t - 16] + s0 + w[t - 7] + s1;
		}
		int a = s32[0], bb = s32[1], c = s32[2], d = s32[3], e = s32[4], f = s32[5], g = s32[6], h = s32[7];
		for (int t = 0; t < 64; t++) {
			int t1 = h + (Integer.rotateRight(e, 6) ^ Integer.rotateRight(e, 11) ^ Integer.rotateRight(e, 25)) + ((e & f) ^ (~e & g))
					+ K256[t] + w[t];
			int t2 = (Integer.rotateRight(a, 2) ^ Integer.rotateRight(a, 13) ^ Integer.rotateRight(a, 22)) + ((a & bb) ^ (a & c) ^ (bb & c));
			h = g;
			g = f;
			f = e;
			e = d + t1;
			d = c;
			c = bb;
			bb = a;
			a = t1 + t2;
		}
		s32[0] += a;
		s32[1] += bb;
		s32[2] += c;
		s32[3] += d;
		s32[4] += e;
		s32[5] += f;
		s32[6] += g;
		s32[7] += h;
	}

	private void compress512(byte[] b, int o) {
		long[] w = w64;
		for (int t = 0; t < 16; t++, o += 8) {
			w[t] = Blake2b.le64(b, o);
			w[t] = Long.reverseBytes(w[t]); // big endian
		}
		for (int t = 16; t < 80; t++) {
			long x = w[t - 15], y = w[t - 2];
			long s0 = Long.rotateRight(x, 1) ^ Long.rotateRight(x, 8) ^ (x >>> 7);
			long s1 = Long.rotateRight(y, 19) ^ Long.rotateRight(y, 61) ^ (y >>> 6);
			w[t] = w[t - 16] + s0 + w[t - 7] + s1;
		}
		long a = s64[0], bb = s64[1], c = s64[2], d = s64[3], e = s64[4], f = s64[5], g = s64[6], h = s64[7];
		for (int t = 0; t < 80; t++) {
			long t1 = h + (Long.rotateRight(e, 14) ^ Long.rotateRight(e, 18) ^ Long.rotateRight(e, 41)) + ((e & f) ^ (~e & g)) + K512[t]
					+ w[t];
			long t2 = (Long.rotateRight(a, 28) ^ Long.rotateRight(a, 34) ^ Long.rotateRight(a, 39)) + ((a & bb) ^ (a & c) ^ (bb & c));
			h = g;
			g = f;
			f = e;
			e = d + t1;
			d = c;
			c = bb;
			bb = a;
			a = t1 + t2;
		}
		s64[0] += a;
		s64[1] += bb;
		s64[2] += c;
		s64[3] += d;
		s64[4] += e;
		s64[5] += f;
		s64[6] += g;
		s64[7] += h;
	}
}
