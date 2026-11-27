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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * PBKDF2 (RFC 8018) and HMAC (RFC 2104) on {@link Sha2}: same results as the JDK {@code PBKDF2WithHmacSHA256/512}
 * and {@code HmacSHA256/512}, but every buffer holding the password or a key is wiped at the end, and the inner and
 * outer HMAC states are computed once (about twice as fast as the JDK).
 */
final class Pbkdf2 {

	private Pbkdf2() {
	}

	static byte[] derive(boolean sha512, char[] password, byte[] salt, int iterations, int outBytes) {
		byte[] pwd = utf8(password);
		try {
			return derive(sha512, pwd, salt, iterations, outBytes);
		} finally {
			Arrays.fill(pwd, (byte) 0);
		}
	}

	static byte[] derive(boolean sha512, byte[] password, byte[] salt, int iterations, int outBytes) {
		if (iterations < 1 || outBytes < 1) {
			throw new IllegalArgumentException("Invalid PBKDF2 parameters");
		}
		Hmac h = new Hmac(sha512, password);
		int hl = h.length();
		byte[] out = new byte[outBytes];
		byte[] u = new byte[hl];
		byte[] t = new byte[hl];
		byte[] first = Arrays.copyOf(salt, salt.length + 4);
		try {
			for (int block = 1, pos = 0; pos < outBytes; block++, pos += hl) {
				first[salt.length] = (byte) (block >>> 24);
				first[salt.length + 1] = (byte) (block >>> 16);
				first[salt.length + 2] = (byte) (block >>> 8);
				first[salt.length + 3] = (byte) block;
				h.mac(first, first.length, u);
				System.arraycopy(u, 0, t, 0, hl);
				for (int j = 1; j < iterations; j++) {
					h.mac(u, hl, u);
					for (int k = 0; k < hl; k++) {
						t[k] ^= u[k];
					}
				}
				System.arraycopy(t, 0, out, pos, Math.min(hl, outBytes - pos));
			}
			return out;
		} finally {
			Arrays.fill(u, (byte) 0);
			Arrays.fill(t, (byte) 0);
			h.wipe();
		}
	}

	/** UTF-8 like the JDK PBKDF2 (malformed chars become '?'), with the temporary buffer wiped. */
	static byte[] utf8(char[] chars) {
		ByteBuffer bb = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
		byte[] out = Arrays.copyOfRange(bb.array(), bb.position(), bb.limit());
		Arrays.fill(bb.array(), (byte) 0);
		return out;
	}

	static byte[] hmacSha256(byte[] key, byte[] data) {
		Hmac h = new Hmac(false, key);
		byte[] out = new byte[h.length()];
		h.mac(data, data.length, out);
		h.wipe();
		return out;
	}

	/** HKDF-SHA256 (RFC 5869): extract then expand. */
	static byte[] hkdfSha256(byte[] ikm, byte[] salt, byte[] info, int outBytes) {
		byte[] prk = hmacSha256(salt == null || salt.length == 0 ? new byte[32] : salt, ikm);
		Hmac h = new Hmac(false, prk);
		Arrays.fill(prk, (byte) 0);
		byte[] out = new byte[outBytes];
		byte[] t = new byte[0];
		try {
			for (int i = 1, pos = 0; pos < outBytes; i++) {
				byte[] in = new byte[t.length + info.length + 1];
				System.arraycopy(t, 0, in, 0, t.length);
				System.arraycopy(info, 0, in, t.length, info.length);
				in[in.length - 1] = (byte) i;
				Arrays.fill(t, (byte) 0);
				t = new byte[h.length()];
				h.mac(in, in.length, t);
				Arrays.fill(in, (byte) 0);
				System.arraycopy(t, 0, out, pos, Math.min(t.length, outBytes - pos));
				pos += t.length;
			}
			return out;
		} finally {
			Arrays.fill(t, (byte) 0);
			h.wipe();
		}
	}

	/** HMAC with precomputed inner and outer states. */
	static final class Hmac {
		private final Sha2 inner;
		private final Sha2 outer;
		private final Sha2 work;
		private final byte[] tmp;

		Hmac(boolean sha512, byte[] key) {
			work = new Sha2(sha512);
			int bl = work.block;
			byte[] k = new byte[bl];
			if (key.length > bl) {
				work.update(key, 0, key.length);
				work.digest(k);
			} else {
				System.arraycopy(key, 0, k, 0, key.length);
			}
			for (int i = 0; i < bl; i++) {
				k[i] ^= 0x36;
			}
			inner = new Sha2(sha512);
			inner.update(k, 0, bl);
			for (int i = 0; i < bl; i++) {
				k[i] ^= 0x36 ^ 0x5c;
			}
			outer = new Sha2(sha512);
			outer.update(k, 0, bl);
			Arrays.fill(k, (byte) 0);
			tmp = new byte[work.out];
		}

		int length() {
			return work.out;
		}

		/** dst = HMAC(msg[0..len)); dst may be msg. */
		void mac(byte[] msg, int len, byte[] dst) {
			work.copyFrom(inner);
			work.update(msg, 0, len);
			work.digest(tmp);
			work.copyFrom(outer);
			work.update(tmp, 0, tmp.length);
			work.digest(dst);
		}

		void wipe() {
			inner.wipe();
			outer.wipe();
			work.wipe();
			Arrays.fill(tmp, (byte) 0);
		}
	}
}
