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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Public key files (like age): a random file key is sealed once per recipient. Each stanza is
 * {@code ephemeralPublic(32) | ChaCha20-Poly1305(fileKey)(64 + 16)}, with a fresh ephemeral X25519 key, and
 * wrapKey = HKDF-SHA256(X25519(ephemeral, recipient), salt = file salt, info = label | ephemeral | recipient).
 * Stanzas carry no recipient name or id: nobody can tell for whom a file is, and they are authenticated twice
 * (their own tag and, as part of the header, by every chunk).
 */
final class Recipients {

	static final int FILE_KEY_BYTES = 64;
	static final int STANZA_BYTES = X25519.KEY_BYTES + FILE_KEY_BYTES + ChaCha20Poly1305Layer.TAG_BYTES;
	private static final byte[] LABEL = "SCP2/x25519/v1".getBytes(StandardCharsets.US_ASCII);
	private static final byte[] AAD = "SCP2 sealed file key".getBytes(StandardCharsets.US_ASCII);
	private static final byte[] ZERO_NONCE = new byte[12];

	private Recipients() {
	}

	static byte[] seal(byte[] fileKey, byte[] recipientPublic, byte[] salt) throws CryptoException {
		if (recipientPublic == null || recipientPublic.length != X25519.KEY_BYTES) {
			throw new CryptoException("Invalid public key");
		}
		byte[] eph = X25519.newPrivateKey();
		byte[] ephPub = X25519.publicKey(eph);
		byte[] shared = X25519.scalarMult(eph, recipientPublic);
		Arrays.fill(eph, (byte) 0);
		try {
			if (X25519.isZero(shared)) {
				throw new CryptoException("Invalid public key");
			}
			byte[] wrapKey = wrapKey(shared, salt, ephPub, recipientPublic);
			byte[] sealed = CipherAlgorithm.CHACHA20_POLY1305.encrypt(wrapKey, ZERO_NONCE, AAD, fileKey);
			Arrays.fill(wrapKey, (byte) 0);
			byte[] stanza = Arrays.copyOf(ephPub, STANZA_BYTES);
			System.arraycopy(sealed, 0, stanza, X25519.KEY_BYTES, sealed.length);
			return stanza;
		} finally {
			Arrays.fill(shared, (byte) 0);
		}
	}

	/** The file key, from the first stanza sealed for this private key. */
	static byte[] open(byte[][] stanzas, byte[] privateKey, byte[] salt) throws CryptoException {
		byte[] myPub = X25519.publicKey(privateKey);
		for (byte[] s : stanzas) {
			byte[] ephPub = Arrays.copyOf(s, X25519.KEY_BYTES);
			byte[] shared = X25519.scalarMult(privateKey, ephPub);
			try {
				if (X25519.isZero(shared)) {
					continue;
				}
				byte[] wrapKey = wrapKey(shared, salt, ephPub, myPub);
				try {
					return CipherAlgorithm.CHACHA20_POLY1305.decrypt(wrapKey, ZERO_NONCE, AAD, Arrays.copyOfRange(s, X25519.KEY_BYTES, s.length));
				} catch (CryptoException notForMe) {
					// another recipient's stanza (or a damaged one): try the next
				} finally {
					Arrays.fill(wrapKey, (byte) 0);
				}
			} finally {
				Arrays.fill(shared, (byte) 0);
			}
		}
		throw new CryptoException("This file was not encrypted for your key (or it is damaged)");
	}

	private static byte[] wrapKey(byte[] shared, byte[] salt, byte[] ephPub, byte[] recipientPub) {
		byte[] info = new byte[LABEL.length + 64];
		System.arraycopy(LABEL, 0, info, 0, LABEL.length);
		System.arraycopy(ephPub, 0, info, LABEL.length, 32);
		System.arraycopy(recipientPub, 0, info, LABEL.length + 32, 32);
		return Pbkdf2.hkdfSha256(shared, salt, info, 32);
	}
}
