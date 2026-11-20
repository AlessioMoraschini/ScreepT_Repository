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

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password hashing (key derivation) algorithms, all from the JDK or pure Java (no dependencies).
 * "cost" is: iterations for PBKDF2, log2(N) for scrypt (r = 8, p = 1).
 * Ids are written in the file header: never change an existing id.
 */
public enum KdfAlgorithm {

	PBKDF2_SHA256(1, "PBKDF2WithHmacSHA256", 600000, "PBKDF2-HMAC-SHA256"),
	PBKDF2_SHA512(2, "PBKDF2WithHmacSHA512", 600000, "PBKDF2-HMAC-SHA512"),
	SCRYPT(3, null, 15, "scrypt (memory-hard)");

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
		return this == SCRYPT ? (cost >= 10 && cost <= 22) : (cost >= 1000 && cost <= 20000000);
	}

	public byte[] derive(char[] password, byte[] salt, int cost, int outBytes) throws CryptoException {
		try {
			if (this == SCRYPT) {
				return Scrypt.derive(password, salt, 1 << cost, 8, 1, outBytes);
			}
			return pbkdf2(jdkName, password, salt, cost, outBytes);
		} catch (GeneralSecurityException e) {
			throw new CryptoException("Key derivation failed", e);
		}
	}

	static byte[] pbkdf2(String jdkName, char[] password, byte[] salt, int iterations, int outBytes)
			throws GeneralSecurityException {
		PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, outBytes * 8);
		try {
			return SecretKeyFactory.getInstance(jdkName).generateSecret(spec).getEncoded();
		} finally {
			spec.clearPassword();
		}
	}
}
