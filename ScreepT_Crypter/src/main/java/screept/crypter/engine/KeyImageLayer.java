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
 * StegaCREEPT: AES-256-GCM whose key also depends on a secret key image. Without the exact picture the file cannot
 * be decrypted, even with the right password: the image is a second factor. Unlike the legacy Steg mode it does not
 * produce PNG files; the protected data stays a normal encrypted file.
 */
public class KeyImageLayer extends AesGcmLayer {

	public KeyImageLayer(int id, String label) {
		super(id, label, 32);
	}

	@Override
	public boolean needsKeyImage() {
		return true;
	}

	@Override
	public byte[] deriveKey(byte[] seed, CryptoKey key) throws CryptoException {
		if (key == null || !key.hasKeyImage()) {
			throw new CryptoException("This file also needs the key image it was protected with");
		}
		return hmac(seed, key.keyImageDigest());
	}
}
