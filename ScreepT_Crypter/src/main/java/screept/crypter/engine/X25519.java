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

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * X25519 Diffie-Hellman (RFC 7748), pure Java (the JDK has it only from Java 11). Field arithmetic in 16 limbs of 16 bits,
 * as in TweetNaCl: no branches or table lookups that depend on secrets (constant time ladder with conditional swaps).
 */
final class X25519 {

	static final int KEY_BYTES = 32;
	private static final long[] A24 = { 0xDB41, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private static final byte[] BASE = new byte[32];
	private static final SecureRandom RANDOM = new SecureRandom();

	static {
		BASE[0] = 9;
	}

	private X25519() {
	}

	static byte[] newPrivateKey() {
		byte[] k = new byte[KEY_BYTES];
		RANDOM.nextBytes(k);
		return k;
	}

	static byte[] publicKey(byte[] privateKey) {
		return scalarMult(privateKey, BASE);
	}

	/** Shared secret; all zero for low order public keys (callers must refuse it). */
	static byte[] scalarMult(byte[] scalar, byte[] point) {
		if (scalar.length != KEY_BYTES || point.length != KEY_BYTES) {
			throw new IllegalArgumentException("X25519 keys are 32 bytes");
		}
		byte[] z = Arrays.copyOf(scalar, 32);
		z[31] = (byte) ((z[31] & 127) | 64);
		z[0] &= (byte) 248;
		long[] x = new long[16];
		unpack(x, point);
		long[] a = new long[16], b = x.clone(), c = new long[16], d = new long[16], e = new long[16], f = new long[16];
		a[0] = 1;
		d[0] = 1;
		long[] t = new long[31];
		for (int i = 254; i >= 0; --i) {
			int r = ((z[i >>> 3] & 0xff) >>> (i & 7)) & 1;
			sel(a, b, r);
			sel(c, d, r);
			add(e, a, c);
			sub(a, a, c);
			add(c, b, d);
			sub(b, b, d);
			mul(d, e, e, t);
			mul(f, a, a, t);
			mul(a, c, a, t);
			mul(c, b, e, t);
			add(e, a, c);
			sub(a, a, c);
			mul(b, a, a, t);
			sub(c, d, f);
			mul(a, c, A24, t);
			add(a, a, d);
			mul(c, c, a, t);
			mul(a, d, f, t);
			mul(d, b, x, t);
			mul(b, e, e, t);
			sel(a, b, r);
			sel(c, d, r);
		}
		inv(c, c, t);
		mul(a, a, c, t);
		byte[] out = new byte[32];
		pack(out, a);
		for (long[] v : new long[][] { x, a, b, c, d, e, f, t }) {
			Arrays.fill(v, 0);
		}
		Arrays.fill(z, (byte) 0);
		return out;
	}

	static boolean isZero(byte[] b) {
		int acc = 0;
		for (byte x : b) {
			acc |= x;
		}
		return acc == 0;
	}

	// ---------------------------------------------------------------- GF(2^255 - 19)

	private static void carry(long[] o) {
		for (int i = 0; i < 16; i++) {
			o[i] += 1L << 16;
			long c = o[i] >> 16;
			o[i < 15 ? i + 1 : 0] += c - 1 + (i == 15 ? 37 * (c - 1) : 0);
			o[i] -= c << 16;
		}
	}

	private static void sel(long[] p, long[] q, int bit) {
		long c = ~(bit - 1L);
		for (int i = 0; i < 16; i++) {
			long t = c & (p[i] ^ q[i]);
			p[i] ^= t;
			q[i] ^= t;
		}
	}

	private static void pack(byte[] o, long[] n) {
		long[] m = new long[16], t = n.clone();
		carry(t);
		carry(t);
		carry(t);
		for (int j = 0; j < 2; j++) {
			m[0] = t[0] - 0xffed;
			for (int i = 1; i < 15; i++) {
				m[i] = t[i] - 0xffff - ((m[i - 1] >> 16) & 1);
				m[i - 1] &= 0xffff;
			}
			m[15] = t[15] - 0x7fff - ((m[14] >> 16) & 1);
			int b = (int) ((m[15] >> 16) & 1);
			m[14] &= 0xffff;
			sel(t, m, 1 - b);
		}
		for (int i = 0; i < 16; i++) {
			o[2 * i] = (byte) t[i];
			o[2 * i + 1] = (byte) (t[i] >> 8);
		}
		Arrays.fill(m, 0);
		Arrays.fill(t, 0);
	}

	private static void unpack(long[] o, byte[] n) {
		for (int i = 0; i < 16; i++) {
			o[i] = (n[2 * i] & 0xff) + ((long) (n[2 * i + 1] & 0xff) << 8);
		}
		o[15] &= 0x7fff;
	}

	private static void add(long[] o, long[] a, long[] b) {
		for (int i = 0; i < 16; i++) {
			o[i] = a[i] + b[i];
		}
	}

	private static void sub(long[] o, long[] a, long[] b) {
		for (int i = 0; i < 16; i++) {
			o[i] = a[i] - b[i];
		}
	}

	/** o = a * b (o may be a or b); t is a 31 long scratch array. */
	private static void mul(long[] o, long[] a, long[] b, long[] t) {
		Arrays.fill(t, 0);
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				t[i + j] += a[i] * b[j];
			}
		}
		for (int i = 0; i < 15; i++) {
			t[i] += 38 * t[i + 16];
		}
		System.arraycopy(t, 0, o, 0, 16);
		carry(o);
		carry(o);
	}

	private static void inv(long[] o, long[] in, long[] t) {
		long[] c = in.clone();
		for (int a = 253; a >= 0; a--) {
			mul(c, c, c, t);
			if (a != 2 && a != 4) {
				mul(c, c, in, t);
			}
		}
		System.arraycopy(c, 0, o, 0, 16);
		Arrays.fill(c, 0);
	}
}
