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

import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Keyed shuffle of the bytes of every chunk (length preserving, not authenticated). Put it LAST in a profile:
 * it then scrambles the position of the ciphertext and of the authentication tags. It is an obfuscation on top
 * of the authenticated layers, which keep protecting (and checking) the data; on its own it protects nothing.
 * The permutation is a Fisher-Yates driven by an AES-CTR keystream, different for every chunk.
 */
public class ShuffleLayer implements CipherLayer {

	private final int id;
	private final String label;

	public ShuffleLayer(int id, String label) {
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
		return 0;
	}

	@Override
	public boolean authenticated() {
		return false;
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
		int[] p = permutation(key, nonce, data.length);
		byte[] out = new byte[data.length];
		for (int i = 0; i < p.length; i++) {
			out[i] = data[p[i]];
		}
		return out;
	}

	@Override
	public byte[] decrypt(byte[] key, byte[] nonce, byte[] aad, byte[] data) throws CryptoException {
		int[] p = permutation(key, nonce, data.length);
		byte[] out = new byte[data.length];
		for (int i = 0; i < p.length; i++) {
			out[p[i]] = data[i];
		}
		return out;
	}

	private static int[] permutation(byte[] key, byte[] nonce, int n) throws CryptoException {
		int[] p = new int[n];
		for (int i = 0; i < n; i++) {
			p[i] = i;
		}
		if (n < 2) {
			return p;
		}
		try {
			Cipher ctr = Cipher.getInstance("AES/CTR/NoPadding");
			byte[] iv = new byte[16];
			System.arraycopy(nonce, 0, iv, 0, nonce.length);
			ctr.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
			Stream stream = new Stream(ctr);
			for (int i = n - 1; i > 0; i--) {
				int j = stream.nextInt(i + 1);
				int t = p[i];
				p[i] = p[j];
				p[j] = t;
			}
		} catch (GeneralSecurityException e) {
			throw new CryptoException("Shuffle failure", e);
		}
		return p;
	}

	/** Unbiased random numbers from an AES-CTR keystream. */
	private static final class Stream {
		private final Cipher ctr;
		private byte[] buf = new byte[0];
		private int pos = 0;

		Stream(Cipher ctr) {
			this.ctr = ctr;
		}

		private int next31() {
			if (pos + 4 > buf.length) {
				buf = ctr.update(new byte[1 << 16]);
				pos = 0;
			}
			int v = ((buf[pos] & 0x7f) << 24) | ((buf[pos + 1] & 0xff) << 16) | ((buf[pos + 2] & 0xff) << 8) | (buf[pos + 3] & 0xff);
			pos += 4;
			return v;
		}

		int nextInt(int bound) {
			int bits;
			int val;
			do {
				bits = next31();
				val = bits % bound;
			} while (bits - val + (bound - 1) < 0);
			return val;
		}
	}
}
