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
