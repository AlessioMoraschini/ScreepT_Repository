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

import java.security.GeneralSecurityException;
import java.util.Arrays;

/** scrypt (RFC 7914) in pure Java on top of the JDK PBKDF2-HMAC-SHA256. */
final class Scrypt {

	private Scrypt() {
	}

	static byte[] derive(char[] password, byte[] salt, int n, int r, int p, int outBytes) throws GeneralSecurityException {
		if (n < 2 || (n & (n - 1)) != 0) {
			throw new IllegalArgumentException("N must be a power of 2");
		}
		byte[] b = KdfAlgorithm.pbkdf2("PBKDF2WithHmacSHA256", password, salt, 1, p * 128 * r);
		int len = 32 * r;
		int[] x = new int[len];
		int[] v = new int[len * n];
		int[] tmp = new int[len];
		int[] x16 = new int[16];
		int[] s16 = new int[16];
		for (int i = 0; i < p; i++) {
			int off = i * 128 * r;
			for (int k = 0; k < len; k++) {
				int o = off + k * 4;
				x[k] = (b[o] & 0xff) | (b[o + 1] & 0xff) << 8 | (b[o + 2] & 0xff) << 16 | (b[o + 3] & 0xff) << 24;
			}
			roMix(x, v, tmp, x16, s16, n, r);
			for (int k = 0; k < len; k++) {
				int o = off + k * 4;
				b[o] = (byte) x[k];
				b[o + 1] = (byte) (x[k] >>> 8);
				b[o + 2] = (byte) (x[k] >>> 16);
				b[o + 3] = (byte) (x[k] >>> 24);
			}
		}
		Arrays.fill(v, 0);
		Arrays.fill(x, 0);
		Arrays.fill(tmp, 0);
		try {
			return KdfAlgorithm.pbkdf2("PBKDF2WithHmacSHA256", password, b, 1, outBytes);
		} finally {
			Arrays.fill(b, (byte) 0);
		}
	}

	private static void roMix(int[] x, int[] v, int[] tmp, int[] x16, int[] s16, int n, int r) {
		int len = 32 * r;
		for (int i = 0; i < n; i++) {
			System.arraycopy(x, 0, v, i * len, len);
			blockMix(x, tmp, x16, s16, r);
		}
		for (int i = 0; i < n; i++) {
			int j = x[(2 * r - 1) * 16] & (n - 1);
			int base = j * len;
			for (int k = 0; k < len; k++) {
				x[k] ^= v[base + k];
			}
			blockMix(x, tmp, x16, s16, r);
		}
	}

	private static void blockMix(int[] b, int[] y, int[] x, int[] s16, int r) {
		System.arraycopy(b, (2 * r - 1) * 16, x, 0, 16);
		for (int i = 0; i < 2 * r; i++) {
			for (int k = 0; k < 16; k++) {
				x[k] ^= b[i * 16 + k];
			}
			salsa20_8(x, s16);
			System.arraycopy(x, 0, y, i * 16, 16);
		}
		for (int i = 0; i < r; i++) {
			System.arraycopy(y, (2 * i) * 16, b, i * 16, 16);
			System.arraycopy(y, (2 * i + 1) * 16, b, (i + r) * 16, 16);
		}
	}

	private static void salsa20_8(int[] b, int[] x) {
		System.arraycopy(b, 0, x, 0, 16);
		for (int i = 0; i < 8; i += 2) {
			x[4] ^= Integer.rotateLeft(x[0] + x[12], 7);
			x[8] ^= Integer.rotateLeft(x[4] + x[0], 9);
			x[12] ^= Integer.rotateLeft(x[8] + x[4], 13);
			x[0] ^= Integer.rotateLeft(x[12] + x[8], 18);
			x[9] ^= Integer.rotateLeft(x[5] + x[1], 7);
			x[13] ^= Integer.rotateLeft(x[9] + x[5], 9);
			x[1] ^= Integer.rotateLeft(x[13] + x[9], 13);
			x[5] ^= Integer.rotateLeft(x[1] + x[13], 18);
			x[14] ^= Integer.rotateLeft(x[10] + x[6], 7);
			x[2] ^= Integer.rotateLeft(x[14] + x[10], 9);
			x[6] ^= Integer.rotateLeft(x[2] + x[14], 13);
			x[10] ^= Integer.rotateLeft(x[6] + x[2], 18);
			x[3] ^= Integer.rotateLeft(x[15] + x[11], 7);
			x[7] ^= Integer.rotateLeft(x[3] + x[15], 9);
			x[11] ^= Integer.rotateLeft(x[7] + x[3], 13);
			x[15] ^= Integer.rotateLeft(x[11] + x[7], 18);
			x[1] ^= Integer.rotateLeft(x[0] + x[3], 7);
			x[2] ^= Integer.rotateLeft(x[1] + x[0], 9);
			x[3] ^= Integer.rotateLeft(x[2] + x[1], 13);
			x[0] ^= Integer.rotateLeft(x[3] + x[2], 18);
			x[6] ^= Integer.rotateLeft(x[5] + x[4], 7);
			x[7] ^= Integer.rotateLeft(x[6] + x[5], 9);
			x[4] ^= Integer.rotateLeft(x[7] + x[6], 13);
			x[5] ^= Integer.rotateLeft(x[4] + x[7], 18);
			x[11] ^= Integer.rotateLeft(x[10] + x[9], 7);
			x[8] ^= Integer.rotateLeft(x[11] + x[10], 9);
			x[9] ^= Integer.rotateLeft(x[8] + x[11], 13);
			x[10] ^= Integer.rotateLeft(x[9] + x[8], 18);
			x[12] ^= Integer.rotateLeft(x[15] + x[14], 7);
			x[13] ^= Integer.rotateLeft(x[12] + x[15], 9);
			x[14] ^= Integer.rotateLeft(x[13] + x[12], 13);
			x[15] ^= Integer.rotateLeft(x[14] + x[13], 18);
		}
		for (int i = 0; i < 16; i++) {
			b[i] += x[i];
		}
	}
}
