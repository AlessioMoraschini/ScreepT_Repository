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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.Normalizer;
import java.util.Arrays;


/**
 * Password hashing (key derivation) algorithms, all from the JDK or pure Java (no dependencies).
 * "cost" is: iterations for PBKDF2, log2(N) for scrypt (r = 8, p = 1), {@code lanes << 16 | passes << 8 | log2(KiB)}
 * for Argon2id (password normalised to Unicode NFC, then UTF-8).
 * Ids are written in the file header: never change an existing id.
 */
public enum KdfAlgorithm {

	PBKDF2_SHA256(1, "PBKDF2WithHmacSHA256", 600000, "PBKDF2-HMAC-SHA256"),
	PBKDF2_SHA512(2, "PBKDF2WithHmacSHA512", 600000, "PBKDF2-HMAC-SHA512"),
	SCRYPT(3, null, 17, "scrypt (memory-hard)"),
	/** RFC 9106 Argon2id; default 64 MiB, 3 passes, 4 lanes (the RFC second recommendation). Ultra uses 256 MiB. */
	ARGON2ID(4, null, Argon2.cost(16, 3, 4), "Argon2id (memory-hard, modern)"),
	/** Not a password: the file key is sealed for each recipient with X25519 (cost = number of recipients). */
	X25519(5, null, 1, "X25519 public keys");

	/** Most recipients of one file. */
	public static final int MAX_RECIPIENTS = 32;

	public final int id;
	public final String jdkName;
	public final int defaultCost;
	public final String label;

	private KdfAlgorithm(int id, String jdkName, int defaultCost, String label) {
		this.id = id;
		this.jdkName = jdkName;
		this.defaultCost = defaultCost;
		this.label = label;
	}

	public static KdfAlgorithm fromId(int id) throws CryptoException {
		for (KdfAlgorithm a : values()) {
			if (a.id == id) {
				return a;
			}
		}
		throw new CryptoException("Unknown key derivation id " + id);
	}

	/** Bounds, so that a tampered header cannot make the program burn CPU or memory. */
	public boolean isCostAcceptable(int cost) {
		if (this == X25519) {
			return cost >= 1 && cost <= MAX_RECIPIENTS;
		}
		if (this == ARGON2ID) {
			int m = Argon2.memoryLog2(cost), t = Argon2.passes(cost), p = Argon2.lanes(cost);
			return (cost >>> 24) == 0 && m >= 13 && m <= 22 && t >= 1 && t <= 10 && p >= 1 && p <= 8;
		}
		return this == SCRYPT ? (cost >= 10 && cost <= 22) : (cost >= 1000 && cost <= 5000000);
	}

	/** False for public key files (no password is turned into the key). */
	public boolean passwordBased() {
		return this != X25519;
	}

	/** Bytes of memory the derivation needs (0 for PBKDF2). */
	public long memoryBytes(int cost) {
		if (this == SCRYPT) {
			return 1024L << cost; // 128 * r(8) bytes per 2^cost
		}
		return this == ARGON2ID ? 1024L << Argon2.memoryLog2(cost) : 0;
	}

	public byte[] derive(char[] password, byte[] salt, int cost, int outBytes) throws CryptoException {
		try {
			// the file header decides the cost: refuse what the heap cannot hold instead of dying
			long needed = memoryBytes(cost);
			if (needed > Runtime.getRuntime().maxMemory() / 2) {
				throw new CryptoException("Not enough memory for this key derivation (" + (needed >> 20) + " MB needed)");
			}
			if (this == X25519) {
				throw new CryptoException("Public key files are opened with a private key, not a password");
			}
			if (this == SCRYPT) {
				return Scrypt.derive(password, salt, 1 << cost, 8, 1, outBytes);
			}
			if (this == ARGON2ID) {
				byte[] pwd = nfcUtf8(password);
				try {
					return Argon2.hash(Argon2.TYPE_ID, pwd, salt, null, null, Argon2.passes(cost), 1 << Argon2.memoryLog2(cost),
							Argon2.lanes(cost), outBytes);
				} finally {
					Arrays.fill(pwd, (byte) 0);
				}
			}
			return pbkdf2(jdkName, password, salt, cost, outBytes);
		} catch (GeneralSecurityException e) {
			throw new CryptoException("Key derivation failed", e);
		} catch (OutOfMemoryError e) {
			throw new CryptoException("Not enough memory for this key derivation", e);
		} catch (IllegalStateException | IllegalArgumentException e) {
			throw new CryptoException("Key derivation failed", e);
		}
	}

	/** NFC (so that the same password typed on different systems gives the same key), then UTF-8. */
	static byte[] nfcUtf8(char[] password) {
		CharBuffer chars = CharBuffer.wrap(password);
		if (!Normalizer.isNormalized(chars, Normalizer.Form.NFC)) {
			chars = CharBuffer.wrap(Normalizer.normalize(chars, Normalizer.Form.NFC));
		}
		ByteBuffer bb = StandardCharsets.UTF_8.encode(chars);
		byte[] out = Arrays.copyOfRange(bb.array(), bb.position(), bb.limit());
		Arrays.fill(bb.array(), (byte) 0);
		return out;
	}

	/** Same result as the JDK {@code jdkName} (PBKDF2WithHmacSHA256/512), without copies of the password left in memory. */
	static byte[] pbkdf2(String jdkName, char[] password, byte[] salt, int iterations, int outBytes)
			throws GeneralSecurityException {
		return Pbkdf2.derive(jdkName.endsWith("512"), password, salt, iterations, outBytes);
	}
}
