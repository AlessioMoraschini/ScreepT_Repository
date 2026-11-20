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

/**
 * One stackable step of the crypter pipeline. A {@link CryptoProfile} is an ordered list of layers: every chunk of
 * the file goes through layer 1, then 2, ... when encrypting, and back in reverse order when decrypting.
 * <p>
 * To add an algorithm: implement this interface, give it a NEW id (ids are written in the file header and must
 * never change or be reused) and register it with {@link CipherAlgorithm#register(CipherLayer)}.
 * Implementations must be stateless and thread safe, and must never log or keep passwords, keys or key images.
 */
public interface CipherLayer {

	/** Stable id stored in the file header (1..255). */
	int id();

	/** Name shown to the user. */
	String label();

	/** Bytes this layer adds to every chunk (16 for an AEAD tag, 0 for a length-preserving transformation). */
	int overhead();

	/** True if the layer detects tampering (wrong key, damaged data). A profile needs at least one. */
	boolean authenticated();

	/** True if the user must also provide a key image to decrypt. */
	boolean needsKeyImage();

	/**
	 * Turns the layer seed (32 random-looking bytes, different for every layer and file, derived from the
	 * password) into the key used by {@link #encrypt}/{@link #decrypt}. Layers that need extra secrets
	 * (like the key image) mix them in here.
	 */
	byte[] deriveKey(byte[] seed, CryptoKey key) throws CryptoException;

	/**
	 * @param nonce 12 bytes, unique for every chunk of the file
	 * @param aad   header + chunk position: authenticated layers must bind it, others may ignore it
	 */
	byte[] encrypt(byte[] key, byte[] nonce, byte[] aad, byte[] data) throws CryptoException;

	/** Inverse of {@link #encrypt}; authenticated layers throw {@link CryptoException} on any mismatch. */
	byte[] decrypt(byte[] key, byte[] nonce, byte[] aad, byte[] data) throws CryptoException;
}
