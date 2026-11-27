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

import java.security.MessageDigest;
import java.util.Arrays;

/**
 * ChaCha20-Poly1305 (RFC 8439), authenticated, 256 bit key. Pure Java (the JDK has it only from Java 11).
 * A different family from AES (add-rotate-xor instead of S-boxes): stacked with an AES layer, a break of one
 * of the two still leaves the other one protecting the data.
 */
public class ChaCha20Poly1305Layer implements CipherLayer {

	public static final int TAG_BYTES = 16;

	private final int id;
	private final String label;

	public ChaCha20Poly1305Layer(int id, String label) {
		this.id = id;
		this.label = label;
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public String label() {
		return label;
	}

	@Override
	public int overhead() {
		return TAG_BYTES;
	}

	@Override
	public boolean authenticated() {
		return true;
	}

	@Override
	public boolean needsKeyImage() {
		return false;
	}

	@Override
	public byte[] deriveKey(byte[] seed, CryptoKey key) {
		return Arrays.copyOf(seed, 32);
	}

	@Override
	public byte[] encrypt(byte[] key, byte[] nonce, byte[] aad, byte[] data) throws CryptoException {
		check(key, nonce);
		byte[] out = Arrays.copyOf(data, data.length + TAG_BYTES);
		xor(key, nonce, 1, out, data.length);
		byte[] tag = tag(key, nonce, aad, out, data.length);
		System.arraycopy(tag, 0, out, data.length, TAG_BYTES);
		return out;
	}

	@Override
	public byte[] decrypt(byte[] key, byte[] nonce, byte[] aad, byte[] data) throws CryptoException {
		check(key, nonce);
		if (data.length < TAG_BYTES) {
			throw new CryptoException("The file is truncated or damaged");
		}
		int n = data.length - TAG_BYTES;
		byte[] expected = tag(key, nonce, aad, data, n);
		if (!MessageDigest.isEqual(expected, Arrays.copyOfRange(data, n, data.length))) { // constant time
			throw new CryptoException("Wrong password or key image, or the file is damaged or has been modified");
		}
		byte[] out = Arrays.copyOf(data, n);
		xor(key, nonce, 1, out, n);
		return out;
	}

	private static void check(byte[] key, byte[] nonce) throws CryptoException {
		if (key.length != 32 || nonce.length != 12) {
			throw new CryptoException("ChaCha20 needs a 32 byte key and a 12 byte nonce");
		}
	}

	// ---------------------------------------------------------------- ChaCha20

	/** XORs the first {@code len} bytes of {@code data} with the keystream starting at block {@code counter}. */
	static void xor(byte[] key, byte[] nonce, int counter, byte[] data, int len) throws CryptoException {
		if (((long) len + 63) / 64 + (counter & 0xffffffffL) > 0x100000000L) {
			throw new CryptoException("Chunk too big for ChaCha20");
		}
		int[] state = initState(key, nonce, counter);
		int[] ks = new int[16];
		for (int off = 0; off < len; off += 64) {
			block(state, ks);
			state[12]++;
			int n = Math.min(64, len - off);
			for (int i = 0; i < n; i++) {
				data[off + i] ^= (byte) (ks[i >>> 2] >>> (8 * (i & 3)));
			}
		}
		Arrays.fill(state, 0);
		Arrays.fill(ks, 0);
	}

	static int[] initState(byte[] key, byte[] nonce, int counter) {
		int[] s = new int[16];
		s[0] = 0x61707865;
		s[1] = 0x3320646e;
		s[2] = 0x79622d32;
		s[3] = 0x6b206574;
		for (int i = 0; i < 8; i++) {
			s[4 + i] = le32(key, i * 4);
		}
		s[12] = counter;
		s[13] = le32(nonce, 0);
		s[14] = le32(nonce, 4);
		s[15] = le32(nonce, 8);
		return s;
	}

	static void block(int[] in, int[] out) {
		System.arraycopy(in, 0, out, 0, 16);
		for (int i = 0; i < 10; i++) {
			qr(out, 0, 4, 8, 12);
			qr(out, 1, 5, 9, 13);
			qr(out, 2, 6, 10, 14);
			qr(out, 3, 7, 11, 15);
			qr(out, 0, 5, 10, 15);
			qr(out, 1, 6, 11, 12);
			qr(out, 2, 7, 8, 13);
			qr(out, 3, 4, 9, 14);
		}
		for (int i = 0; i < 16; i++) {
			out[i] += in[i];
		}
	}

	private static void qr(int[] x, int a, int b, int c, int d) {
		x[a] += x[b];
		x[d] = Integer.rotateLeft(x[d] ^ x[a], 16);
		x[c] += x[d];
		x[b] = Integer.rotateLeft(x[b] ^ x[c], 12);
		x[a] += x[b];
		x[d] = Integer.rotateLeft(x[d] ^ x[a], 8);
		x[c] += x[d];
		x[b] = Integer.rotateLeft(x[b] ^ x[c], 7);
	}

	// ---------------------------------------------------------------- Poly1305 AEAD tag

	private static byte[] tag(byte[] key, byte[] nonce, byte[] aad, byte[] ct, int ctLen) {
		int[] state = initState(key, nonce, 0);
		int[] ks = new int[16];
		block(state, ks);
		byte[] otk = new byte[32];
		for (int i = 0; i < 32; i++) {
			otk[i] = (byte) (ks[i >>> 2] >>> (8 * (i & 3)));
		}
		Arrays.fill(ks, 0);
		Arrays.fill(state, 0);
		Poly1305 p = new Poly1305(otk);
		Arrays.fill(otk, (byte) 0);
		p.update(aad, 0, aad.length, true);
		p.update(ct, 0, ctLen, true);
		byte[] lens = new byte[16];
		for (int i = 0; i < 8; i++) {
			lens[i] = (byte) ((long) aad.length >>> (8 * i));
			lens[8 + i] = (byte) ((long) ctLen >>> (8 * i));
		}
		p.update(lens, 0, 16, false);
		return p.finish();
	}

	/** Poly1305 one-time authenticator (26 bit limbs). */
	static final class Poly1305 {
		private static final long M26 = 0x3ffffff;
		private long r0, r1, r2, r3, r4, s1, s2, s3, s4;
		private int pad0, pad1, pad2, pad3;
		private long h0, h1, h2, h3, h4;

		Poly1305(byte[] k) {
			r0 = le32(k, 0) & 0x3ffffff;
			r1 = (le32(k, 3) >>> 2) & 0x3ffff03;
			r2 = (le32(k, 6) >>> 4) & 0x3ffc0ff;
			r3 = (le32(k, 9) >>> 6) & 0x3f03fff;
			r4 = (le32(k, 12) >>> 8) & 0x00fffff;
			s1 = r1 * 5;
			s2 = r2 * 5;
			s3 = r3 * 5;
			s4 = r4 * 5;
			pad0 = le32(k, 16);
			pad1 = le32(k, 20);
			pad2 = le32(k, 24);
			pad3 = le32(k, 28);
		}

		/** @param padTo16 true for the AEAD fields that are zero padded to 16 bytes (RFC 8439 2.8) */
		void update(byte[] m, int off, int len, boolean padTo16) {
			byte[] blk = new byte[16];
			for (int pos = 0; pos < len; pos += 16) {
				int n = Math.min(16, len - pos);
				if (n == 16) {
					block(m, off + pos, 1L << 24);
				} else if (padTo16) {
					Arrays.fill(blk, (byte) 0);
					System.arraycopy(m, off + pos, blk, 0, n);
					block(blk, 0, 1L << 24);
				} else {
					Arrays.fill(blk, (byte) 0);
					System.arraycopy(m, off + pos, blk, 0, n);
					blk[n] = 1;
					block(blk, 0, 0);
				}
			}
		}

		private void block(byte[] m, int o, long hibit) {
			h0 += le32(m, o) & 0xffffffffL & M26;
			h1 += (le32u(m, o + 3) >>> 2) & M26;
			h2 += (le32u(m, o + 6) >>> 4) & M26;
			h3 += (le32u(m, o + 9) >>> 6) & M26;
			h4 += (le32u(m, o + 12) >>> 8) | hibit;

			long d0 = h0 * r0 + h1 * s4 + h2 * s3 + h3 * s2 + h4 * s1;
			long d1 = h0 * r1 + h1 * r0 + h2 * s4 + h3 * s3 + h4 * s2;
			long d2 = h0 * r2 + h1 * r1 + h2 * r0 + h3 * s4 + h4 * s3;
			long d3 = h0 * r3 + h1 * r2 + h2 * r1 + h3 * r0 + h4 * s4;
			long d4 = h0 * r4 + h1 * r3 + h2 * r2 + h3 * r1 + h4 * r0;

			long c = d0 >>> 26;
			h0 = d0 & M26;
			d1 += c;
			c = d1 >>> 26;
			h1 = d1 & M26;
			d2 += c;
			c = d2 >>> 26;
			h2 = d2 & M26;
			d3 += c;
			c = d3 >>> 26;
			h3 = d3 & M26;
			d4 += c;
			c = d4 >>> 26;
			h4 = d4 & M26;
			h0 += c * 5;
			c = h0 >>> 26;
			h0 &= M26;
			h1 += c;
		}

		byte[] finish() {
			long c = h1 >>> 26;
			h1 &= M26;
			h2 += c;
			c = h2 >>> 26;
			h2 &= M26;
			h3 += c;
			c = h3 >>> 26;
			h3 &= M26;
			h4 += c;
			c = h4 >>> 26;
			h4 &= M26;
			h0 += c * 5;
			c = h0 >>> 26;
			h0 &= M26;
			h1 += c;

			long g0 = h0 + 5;
			c = g0 >>> 26;
			g0 &= M26;
			long g1 = h1 + c;
			c = g1 >>> 26;
			g1 &= M26;
			long g2 = h2 + c;
			c = g2 >>> 26;
			g2 &= M26;
			long g3 = h3 + c;
			c = g3 >>> 26;
			g3 &= M26;
			long g4 = h4 + c - (1L << 26);

			long mask = (g4 >>> 63) - 1; // all ones if h >= p (take g), zero otherwise (keep h)
			h0 = (h0 & ~mask) | (g0 & mask);
			h1 = (h1 & ~mask) | (g1 & mask);
			h2 = (h2 & ~mask) | (g2 & mask);
			h3 = (h3 & ~mask) | (g3 & mask);
			h4 = (h4 & ~mask) | (g4 & mask);

			long f0 = ((h0) | (h1 << 26)) & 0xffffffffL;
			long f1 = ((h1 >>> 6) | (h2 << 20)) & 0xffffffffL;
			long f2 = ((h2 >>> 12) | (h3 << 14)) & 0xffffffffL;
			long f3 = ((h3 >>> 18) | (h4 << 8)) & 0xffffffffL;

			long f = f0 + (pad0 & 0xffffffffL);
			f0 = f & 0xffffffffL;
			f = f1 + (pad1 & 0xffffffffL) + (f >>> 32);
			f1 = f & 0xffffffffL;
			f = f2 + (pad2 & 0xffffffffL) + (f >>> 32);
			f2 = f & 0xffffffffL;
			f = f3 + (pad3 & 0xffffffffL) + (f >>> 32);
			f3 = f & 0xffffffffL;

			byte[] out = new byte[16];
			long[] w = { f0, f1, f2, f3 };
			for (int i = 0; i < 16; i++) {
				out[i] = (byte) (w[i >>> 2] >>> (8 * (i & 3)));
			}
			h0 = h1 = h2 = h3 = h4 = 0; // the one-time key is forgotten
			r0 = r1 = r2 = r3 = r4 = s1 = s2 = s3 = s4 = 0;
			pad0 = pad1 = pad2 = pad3 = 0;
			Arrays.fill(w, 0);
			return out;
		}
	}

	static int le32(byte[] b, int o) {
		return (b[o] & 0xff) | (b[o + 1] & 0xff) << 8 | (b[o + 2] & 0xff) << 16 | (b[o + 3] & 0xff) << 24;
	}

	private static long le32u(byte[] b, int o) {
		return le32(b, o) & 0xffffffffL;
	}
}
