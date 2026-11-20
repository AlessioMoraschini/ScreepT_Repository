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
import java.security.InvalidKeyException;
import java.util.Arrays;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** AES in GCM mode (authenticated). Key size 16 = AES-128, 32 = AES-256. */
public class AesGcmLayer implements CipherLayer {

	public static final int TAG_BYTES = 16;

	private final int id;
	private final String label;
	private final int keyBytes;

	public AesGcmLayer(int id, String label, int keyBytes) {
		if (keyBytes != 16 && keyBytes != 32) {
			throw new IllegalArgumentException("AES key must be 16 or 32 bytes");
		}
		this.id = id;
		this.label = label;
		this.keyBytes = keyBytes;
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
	public byte[] deriveKey(byte[] seed, CryptoKey key) throws CryptoException {
		return Arrays.copyOf(seed, keyBytes);
	}

	@Override
	public byte[] encrypt(byte[] key, byte[] nonce, byte[] aad, byte[] data) throws CryptoException {
		return run(Cipher.ENCRYPT_MODE, key, nonce, aad, data);
	}

	@Override
	public byte[] decrypt(byte[] key, byte[] nonce, byte[] aad, byte[] data) throws CryptoException {
		return run(Cipher.DECRYPT_MODE, key, nonce, aad, data);
	}

	private byte[] run(int mode, byte[] key, byte[] nonce, byte[] aad, byte[] data) throws CryptoException {
		try {
			Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
			c.init(mode, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BYTES * 8, nonce));
			c.updateAAD(aad);
			return c.doFinal(data);
		} catch (AEADBadTagException e) {
			throw new CryptoException("Wrong password or key image, or the file is damaged or has been modified");
		} catch (GeneralSecurityException e) {
			throw new CryptoException("Cipher failure", e);
		}
	}

	/** HMAC-SHA256, used by derived layers to mix extra secrets into the key. */
	static byte[] hmac(byte[] key, byte[] data) throws CryptoException {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(key, "HmacSHA256"));
			return mac.doFinal(data);
		} catch (InvalidKeyException | java.security.NoSuchAlgorithmException e) {
			throw new CryptoException("Key derivation failed", e);
		}
	}
}
